package server.application.adaptors;

import server.domain.UserRole;
import java.util.Optional;

public interface UserAccountService {
    Optional<String> login(String emailAddress, String password);

    void registerUser(String username, String emailAddress, String password, UserRole role);

    boolean deleteUserByEmail(String emailAddress);

    void updateUserInformationByEmail(String emailAddress, String newEmailAddress, String newPassword,
            UserRole newRole, String newUsername);

    Optional<String> getUsernameByEmail(String emailAddress);

    Optional<UserRole> getUserRoleByEmail(String emailAddress);

}
