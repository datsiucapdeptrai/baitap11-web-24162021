package vn.iostart.productmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iostart.productmanagement.entity.OrderStatus;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.UserAccountRepository;
import vn.iostart.productmanagement.service.OrderService;
import vn.iostart.productmanagement.security.CustomUserDetails;
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderService orderService;
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

    @GetMapping("/history")
    public String orderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "status", required = false) OrderStatus status,
            Model model
    ) {
        UserAccount user = getCurrentUser(userDetails);
        model.addAttribute("orders", orderService.getOrderHistory(user, status));
        model.addAttribute("currentStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "orders/history";
    }
}