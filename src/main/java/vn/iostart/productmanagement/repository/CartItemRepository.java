package vn.iostart.productmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iostart.productmanagement.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}