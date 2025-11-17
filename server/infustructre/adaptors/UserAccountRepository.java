package server.infustructre.adaptors;

import server.domain.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmailAddress(String emailAddress);

    void save(UserAccount userAccount);

    void updateUserByEmailAddress(String emailAddress, UserAccount userAccount);

    void deleteByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);
}
