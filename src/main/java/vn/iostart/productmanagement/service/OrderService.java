package vn.iostart.productmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iostart.productmanagement.entity.*;
import vn.iostart.productmanagement.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    @Transactional
    public Order placeOrderCod(UserAccount user, String fullName, String phone, String address) {
        // Đổi từ getOrCreateCart -> getCartWithItems để fetch sẵn items + product
        Cart cart = cartService.getCartWithItems(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể đặt hàng");
        }

        Order order = Order.builder()
                .user(user)
                .fullName(fullName)
                .phone(phone)
                .address(address)
                .totalAmount(cart.getTotalPrice())
                .status(OrderStatus.NEW)
                .paymentMethod("COD")
                .orderDate(LocalDateTime.now())
                .build();

        // Chuyển CartItem thành OrderDetail
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            // Kiểm tra tồn kho lần cuối
            if (item.getQuantity() > product.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ hàng");
            }

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.getOrderDetails().add(detail);

            // Trừ tồn kho
            int soLuongConLai = product.getQuantity() - item.getQuantity();
            product.setQuantity(soLuongConLai);
            productRepository.save(product);
        }

        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt
        cart.getItems().clear();
        cartService.getOrCreateCart(user); // Lưu lại giỏ trống

        return savedOrder;
    }

    public List<Order> getOrderHistory(UserAccount user, OrderStatus status) {
        if (status == null) {
            return orderRepository.findByUserWithDetails(user);
        }
        return orderRepository.findByUserAndStatusWithDetails(user, status);
    }
}