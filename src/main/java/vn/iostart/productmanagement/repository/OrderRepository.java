package vn.iostart.productmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iostart.productmanagement.entity.Order;
import vn.iostart.productmanagement.entity.OrderStatus;
import vn.iostart.productmanagement.entity.UserAccount;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(UserAccount user);

    List<Order> findByUserAndStatusOrderByOrderDateDesc(UserAccount user, OrderStatus status);

    /**
     * Query fetch sẵn orderDetails và product để tránh LazyInitializationException.
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.product
        WHERE o.user = :user
        ORDER BY o.orderDate DESC
    """)
    List<Order> findByUserWithDetails(@Param("user") UserAccount user);

    /**
     * Query fetch sẵn orderDetails và product, lọc theo trạng thái.
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.product
        WHERE o.user = :user AND o.status = :status
        ORDER BY o.orderDate DESC
    """)
    List<Order> findByUserAndStatusWithDetails(
            @Param("user") UserAccount user,
            @Param("status") OrderStatus status
    );
}