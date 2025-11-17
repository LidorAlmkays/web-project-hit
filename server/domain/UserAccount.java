package server.domain;

public class UserAccount {
    private String username;
    private String emailAddress;
    private String password;
    private UserRole role;

    public UserAccount(String username, String emailAddress, String password, UserRole role) {
        if (username == null) {
            throw new IllegalArgumentException("username must not be null");
        }
        if (emailAddress == null) {
            throw new IllegalArgumentException("emailAddress must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.username = username;
        this.emailAddress = emailAddress;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        this.password = password;
    }

    public void setRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.role = role;
    }

    public void setUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username must not be null");
        }
        this.username = username;
    }

    public void setEmailAddress(String emailAddress) {
        if (emailAddress == null) {
            throw new IllegalArgumentException("emailAddress must not be null");
        }
        if (emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("emailAddress must not be empty");
        }
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "username='" + username + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", role=" + role +
                '}';
    }
}
