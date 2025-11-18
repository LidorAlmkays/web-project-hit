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
            throw new IllegalArgumentException("UserAccountRepository must not be null");
        }
        this.repository = repository;
    }

    @Override
    public boolean login(String emailAddress, String password) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
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
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        if (emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address must not be empty");
        }

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
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address must not be null");
        }

        if (!repository.existsByEmailAddress(emailAddress)) {
            return false;
        }

        repository.deleteByEmailAddress(emailAddress);
        return true;
    }

    @Override
    public void updateUserInformationByEmail(String emailAddress, String newEmailAddress, String newPassword,
            UserRole newRole, String newUsername) {
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address must not be null");
        }

        Optional<UserAccount> userOpt = repository.findByEmailAddress(emailAddress);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + emailAddress);
        }

        UserAccount user = userOpt.get();

        if (newUsername != null) {
            if (newUsername.trim().isEmpty()) {
                throw new IllegalArgumentException("New username must not be empty");
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
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address must not be null");
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
        if (emailAddress == null) {
            throw new IllegalArgumentException("Email address must not be null");
        }

        Optional<UserAccount> userOpt = repository.findByEmailAddress(emailAddress);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        UserAccount user = userOpt.get();
        return Optional.of(user.getRole());
    }
}
