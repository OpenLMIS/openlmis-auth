package org.openlmis.auth;

import org.openlmis.auth.domain.ClientDetails;
import org.openlmis.auth.domain.ClientRole;
import org.openlmis.auth.domain.RoleGrantedAuthority;
import org.openlmis.auth.domain.Scope;
import org.openlmis.auth.domain.User;
import org.openlmis.auth.domain.UserRole;
import org.openlmis.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;

@Component
public class EntityLoader {

  @Autowired
  JdbcClientDetailsService clientDetailsService;

  @Autowired
  UserRepository userRepository;

  @PostConstruct
  public void initData() {
    initUsers();
    initClients();
  }

  private void initUsers() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    User user = new User();
    user.setUsername("user");
    user.setPassword(encoder.encode("password"));
    user.setRole(UserRole.USER);
    user.setEnabled(true);
    userRepository.save(user);

    User admin = new User();
    admin.setUsername("admin");
    admin.setPassword(encoder.encode("password"));
    admin.setRole(UserRole.ADMIN);
    admin.setEnabled(true);
    userRepository.save(admin);
  }

  private void initClients() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new RoleGrantedAuthority(ClientRole.CLIENT));

    Set<String> resourceIds = new HashSet<>();
    resourceIds.add("example");

    Set<String> scopes = new HashSet<>();
    scopes.add(Scope.READ.toString());
    scopes.add(Scope.WRITE.toString());

    ClientDetails client = new ClientDetails();
    client.setClientId("client");

    client.setAuthorities(authorities);
    client.setResourceIds(resourceIds);
    client.setScope(scopes);

    Set<String> grantTypes = new HashSet<>();
    grantTypes.add("authorization_code");
    grantTypes.add("implicit");
    client.setAuthorizedGrantTypes(grantTypes);

    clientDetailsService.addClientDetails(client);

    client = new ClientDetails();
    client.setClientId("trusted-client");
    client.setClientSecret("secret");

    client.setAuthorities(authorities);
    client.setResourceIds(resourceIds);
    client.setScope(scopes);

    grantTypes = new HashSet<>();
    grantTypes.add("client_credentials");
    grantTypes.add("password");
    client.setAuthorizedGrantTypes(grantTypes);

    clientDetailsService.addClientDetails(client);
  }

}
