package vn.iostart.productmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iostart.productmanagement.entity.*;
import vn.iostart.productmanagement.repository.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // Lấy hoặc tạo mới giỏ hàng cho user (KHÔNG fetch items)
    public Cart getOrCreateCart(UserAccount user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    /**
     * Lấy giỏ hàng CÓ fetch sẵn items và product.
     * Dùng cho việc render template để tránh LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public Cart getCartWithItems(UserAccount user) {
        return cartRepository.findByUserWithItems(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    @Transactional
    public void addToCart(UserAccount user, Long productId, int quantity) {
        Cart cart = getCartWithItems(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra số lượng tồn kho
        if (quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng trong kho không đủ. Chỉ còn " + product.getQuantity());
        }

        // Nếu sản phẩm đã có trong giỏ, cộng dồn số lượng
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > product.getQuantity()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho. Chỉ còn " + product.getQuantity());
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(UserAccount user, Long cartItemId, int newQuantity) {
        Cart cart = getCartWithItems(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        // Đảm bảo item thuộc giỏ hàng của user này
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa sản phẩm này");
        }

        if (newQuantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }
        if (newQuantity > item.getProduct().getQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho. Chỉ còn " + item.getProduct().getQuantity());
        }
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(UserAccount user, Long cartItemId) {
        Cart cart = getCartWithItems(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa sản phẩm này");
        }
        cart.getItems().remove(item);
        cartRepository.save(cart);
    }
}