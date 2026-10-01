package vn.iostart.productmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.UserAccountRepository;
import vn.iostart.productmanagement.service.CartService;
import vn.iostart.productmanagement.security.CustomUserDetails;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserAccountRepository userRepository;

    private UserAccount getCurrentUser(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUser) {
            return userRepository.findById(customUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user có ID: " + customUser.getId()));
        }
        // Fallback nếu không phải CustomUserDetails
        return userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .or(() -> userRepository.findByEmailIgnoreCase(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + userDetails.getUsername()));
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserAccount user = getCurrentUser(userDetails);
        // Dùng getCartWithItems để fetch sẵn items + product → tránh LazyInitializationException
        model.addAttribute("cart", cartService.getCartWithItems(user));
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            cartService.addToCart(getCurrentUser(userDetails), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm vào giỏ hàng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/update")
    public String updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long itemId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes
    ) {
        try {
            cartService.updateQuantity(getCurrentUser(userDetails), itemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật số lượng");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long itemId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            cartService.removeFromCart(getCurrentUser(userDetails), itemId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
}