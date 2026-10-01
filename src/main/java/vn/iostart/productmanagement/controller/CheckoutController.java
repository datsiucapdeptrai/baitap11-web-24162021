package vn.iostart.productmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iostart.productmanagement.entity.Order;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.UserAccountRepository;
import vn.iostart.productmanagement.service.CartService;
import vn.iostart.productmanagement.service.OrderService;
import vn.iostart.productmanagement.security.CustomUserDetails;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserAccountRepository userRepository;

    private UserAccount getCurrentUser(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUser) {
            return userRepository.findById(customUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user có ID: " + customUser.getId()));
        }
        return userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .or(() -> userRepository.findByEmailIgnoreCase(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + userDetails.getUsername()));
    }

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserAccount user = getCurrentUser(userDetails);
        // ★ ĐỔI sang getCartWithItems để fetch sẵn items + product
        model.addAttribute("cart", cartService.getCartWithItems(user));
        model.addAttribute("user", user);
        return "checkout/index";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UserAccount user = getCurrentUser(userDetails);
            Order order = orderService.placeOrderCod(user, fullName, phone, address);
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn: " + order.getId());
            return "redirect:/orders/history";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }
}