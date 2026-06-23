/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.auth.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openlmis.auth.domain.User;
import org.openlmis.auth.dto.SaveBatchResultDto;
import org.openlmis.auth.dto.UnlockResponseDto;
import org.openlmis.auth.dto.UserAuthDetailsResponseDto;
import org.openlmis.auth.dto.UserDto;
import org.openlmis.auth.i18n.MessageKeys;
import org.openlmis.auth.repository.UnsuccessfulAuthenticationAttemptRepository;
import org.openlmis.auth.repository.UserRepository;
import org.openlmis.auth.web.UserDtoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UnsuccessfulAuthenticationAttemptRepository attemptCounterRepository;

  @Autowired
  private UserDtoValidator userDtoValidator;

  // self-reference so per-user unlockUser runs through the proxy and keeps its REQUIRES_NEW tx
  @Autowired
  private UserService self;

  /**
   * Creates a new user or updates an existing one.
   *
   * @param request user to be saved.
   * @return saved user.
   */
  public UserDto saveUser(UserDto request) {
    User savedUser;
    Optional<User> dbUser = userRepository.findById(request.getId());
    
    if (dbUser.isPresent()) {
      savedUser = dbUser.get();
      savedUser.updateFrom(request);
    } else {
      savedUser = User.newInstance(request);
    }

    return toDto(userRepository.save(savedUser));
  }

  /**
   * Saves auth user details.
   *
   * @param userDto user contact details object
   * @return {@link SaveBatchResultDto} object with saving results
   */
  public SaveBatchResultDto<UserAuthDetailsResponseDto.UserAuthResponse> saveAuthUserDetails(
      UserDto userDto) {
    List<UserAuthDetailsResponseDto.UserAuthResponse> successfulResults = new ArrayList<>();
    List<UserAuthDetailsResponseDto.UserAuthResponse> failedResults = new ArrayList<>();
    try {
      BindingResult bindingResult = new BeanPropertyBindingResult(userDto, "userDto");
      userDtoValidator.validate(userDto, bindingResult);
      List<String> errors;
      if (bindingResult.hasErrors()) {
        errors = bindingResult.getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());
        failedResults.add(
            new UserAuthDetailsResponseDto.FailedUserDetailsResponse(userDto.getId(), errors));
      } else {
        saveUser(userDto);
        successfulResults.add(new UserAuthDetailsResponseDto.UserAuthResponse(userDto.getId()));
      }
    } catch (Exception ex) {
      String errorMessage = String.format("%s: %s", MessageKeys.ERROR_SAVING_BATCH_AUTH_DETAILS,
          ex.getMessage());
      failedResults.add(new UserAuthDetailsResponseDto.FailedUserDetailsResponse(
          userDto.getId(),
          Collections.singletonList(errorMessage)));
    }

    return new SaveBatchResultDto<>(successfulResults, failedResults);
  }

  /**
   * Gets user auth details.
   *
   * @return user auth details
   */
  public List<User> findAll() {
    return StreamSupport.stream(userRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  /**
   * Returns all auth users as DTOs (without password), optionally filtered by lockout state.
   *
   * @param lockedOut when not {@code null}, only users with the matching lockout state are returned
   * @return matching auth users
   */
  public List<UserDto> getAuthUsers(Boolean lockedOut) {
    return findAll().stream()
        .filter(user -> lockedOut == null || user.isLockedOut() == lockedOut)
        .map(user -> {
          UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getEnabled());
          dto.setLockedOut(user.isLockedOut());
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * Deletes user auth details.
   *
   * @param userIds user ids for whom auth details will be removed
   */
  @Transactional
  public void deleteByUserIds(Set<UUID> userIds) {
    userRepository.deleteByUserIds(userIds);
  }

  /**
   * Bulk-unlocks the given users, grouping the result into unlocked, notFound and failed buckets.
   * Each user is unlocked in its own transaction (so one failure does not affect the others) and an
   * INFO audit line is logged per unlocked user.
   *
   * @param userIds ids of the users to unlock
   * @param actor   username of the administrator performing the unlock, for the audit log
   * @return the per-user unlock result
   */
  public UnlockResponseDto unlockUsers(List<UUID> userIds, String actor) {
    UnlockResponseDto response = new UnlockResponseDto();

    for (UUID userId : userIds) {
      try {
        String username = self.unlockUser(userId);
        if (null == username) {
          response.getNotFound().add(userId);
        } else {
          response.getUnlocked().add(userId);
          LOGGER.info("User '{}' unlocked user '{}' (id: {})", actor, username, userId);
        }
      } catch (Exception ex) {
        LOGGER.warn("Failed to unlock user with id: {}", userId, ex);
        response.getFailed().add(userId);
      }
    }

    return response;
  }

  /**
   * Unlocks a single user in its own transaction. Resets the failed-attempt counter, then clears
   * the lockout flag (order matters for the unlock/login race). Returns the username, or
   * {@code null} if the user does not exist.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public String unlockUser(UUID userId) {
    // pessimistic write lock to serialize with a concurrent authentication attempt for this user
    Optional<User> optionalUser = userRepository.findByIdForUpdate(userId);
    if (!optionalUser.isPresent()) {
      return null;
    }

    User user = optionalUser.get();

    attemptCounterRepository.findByUserId(userId).ifPresent(counter -> {
      counter.resetCounter();
      attemptCounterRepository.save(counter);
    });

    user.setLockedOut(false);
    userRepository.save(user);

    return user.getUsername();
  }

  private UserDto toDto(User existing) {
    UserDto dto = new UserDto();
    existing.export(dto);

    return dto;
  }

}
