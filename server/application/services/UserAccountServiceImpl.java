package server.application.services;

import server.application.adaptors.UserAccountService;
import server.domain.UserAccount;
import server.domain.UserRole;
import server.infustructre.adaptors.UserAccountRepository;
import java.util.Optional;

public class UserAccountServiceImpl implements UserAccountService {
    private final UserAccountRepository repository;

    public UserAccountServiceImpl(UserAccountRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository cannot be null");
        }
        this.repository = repository;
    }

    @Override
    public boolean login(String emailAddress, String password) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (password == null) {
            throw new IllegalArgumentException("password cannot be null");
        }

        Optional<UserAccount> user = repository.findByEmailAddress(emailAddress);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            System.out.println("User logged in: " + emailAddress);
            return true;
        }
        return false;
    }

    @Override
    public boolean registerUser(String username, String emailAddress, String password, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("email address is required");
        }
        if (password == null) {
            throw new IllegalArgumentException("password cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }

        // check if email already taken no douplicate emails allowed
        if (repository.existsByEmailAddress(emailAddress)) {
            return false;
        }

        UserAccount user = new UserAccount(username, emailAddress, password, role);
        repository.save(user);
        System.out.println("User registered: " + emailAddress);
        return true;
    }

    @Override
    public boolean deleteUserByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (!repository.existsByEmailAddress(emailAddress)) {
            return false;
        }

        repository.deleteByEmailAddress(emailAddress);
        return true;
    }

    @Override
    public void updateUserInformationByEmail(String emailAddress, String newEmailAddress, String newPassword,
            UserRole newRole, String newUsername) {// if the fields are null we dont change them we update whats not
                                                   // null
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        Optional<UserAccount> userOpt = repository.findByEmailAddress(emailAddress);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + emailAddress);
        }

        UserAccount user = userOpt.get();

        if (newUsername != null) {
            if (newUsername.trim().isEmpty()) {
                throw new IllegalArgumentException("username cannot be empty");
            }
            user.setUsername(newUsername);
        }

        if (newPassword != null) {
            user.setPassword(newPassword);
        }

        if (newRole != null) {
            user.setRole(newRole);
        }

        if (newEmailAddress != null && !newEmailAddress.trim().isEmpty()) {
            user.setEmailAddress(newEmailAddress);
        }

        repository.updateUserByEmailAddress(emailAddress, user);
    }

    @Override
    public Optional<String> getUsernameByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        Optional<UserAccount> userOpt = repository.findByEmailAddress(emailAddress);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        UserAccount user = userOpt.get();
        return Optional.of(user.getUsername());
    }

    @Override
    public Optional<UserRole> getUserRoleByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        Optional<UserAccount> userOpt = repository.findByEmailAddress(emailAddress);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        UserAccount user = userOpt.get();
        return Optional.of(user.getRole());
    }
}
