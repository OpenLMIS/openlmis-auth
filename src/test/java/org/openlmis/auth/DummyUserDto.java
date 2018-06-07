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

package org.openlmis.auth;

import java.util.UUID;
import org.openlmis.auth.dto.referencedata.UserDto;

public final class DummyUserDto extends UserDto {
  public static final String AUTH_ID = "51f6bdc1-4932-4bc3-9589-368646ef7ad3";
  public static final String REFERENCE_ID = "35316636-6264-6331-2d34-3933322d3462";
  public static final String USERNAME = "admin";
  public static final String PASSWORD = "password";
  public static final String EMAIL = "test@openlmis.org";

  public DummyUserDto() {
    super(USERNAME, "Admin", "User", EMAIL, null, null, false, false, false, null, null, null);
    setId(UUID.fromString(REFERENCE_ID));
  }

}
