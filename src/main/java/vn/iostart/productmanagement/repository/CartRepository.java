package vn.iostart.productmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iostart.productmanagement.entity.Cart;
import vn.iostart.productmanagement.entity.UserAccount;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(UserAccount user);

    /**
     * Query này fetch luôn items và product trong 1 lần,
     * tránh lỗi LazyInitializationException khi render template Thymeleaf.
     */
    @Query("""
        SELECT DISTINCT c FROM Cart c
        LEFT JOIN FETCH c.items items
        LEFT JOIN FETCH items.product
        WHERE c.user = :user
    """)
    Optional<Cart> findByUserWithItems(@Param("user") UserAccount user);
}