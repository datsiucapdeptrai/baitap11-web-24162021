package vn.iostart.productmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iostart.productmanagement.entity.UserAccount;

@Repository
public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(
            String username
    );

    Optional<UserAccount> findByEmailIgnoreCase(
            String email
    );

    /*
     * Cho phép đăng nhập bằng username hoặc email.
     */
    Optional<UserAccount>
            findByUsernameIgnoreCaseOrEmailIgnoreCase(
                    String username,
                    String email
            );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByEmailIgnoreCase(
            String email
    );
}