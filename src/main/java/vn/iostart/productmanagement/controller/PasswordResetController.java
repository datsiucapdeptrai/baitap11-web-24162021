package vn.iostart.productmanagement.controller;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.service.PasswordResetService;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestReset(@RequestParam("email") String email,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.requestReset(email);
            redirectAttributes.addAttribute("email", email.trim());
            redirectAttributes.addFlashAttribute("success",
                    "Nếu email có tài khoản đã kích hoạt, mã OTP sẽ được gửi đến email đó.");
            return "redirect:/reset-password";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            return "auth/forgot-password";
        } catch (MailException ex) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Không gửi được email. Vui lòng thử lại sau.");
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(value = "email", required = false) String email,
            Model model) {
        model.addAttribute("email", email == null ? "" : email);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("otp") String otp,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.resetPassword(
                    email, otp, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success",
                    "Đổi mật khẩu thành công. Bạn có thể đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("email", email);
            model.addAttribute("error", ex.getMessage());
            return "auth/reset-password";
        }
    }
}
