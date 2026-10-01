package vn.iostart.productmanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iostart.productmanagement.dto.auth.RegisterRequest;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.service.UserRegistrationService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationService registrationService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage()
            );
            return "auth/register";
        }

        try {
            UserAccount user = registrationService.register(request);
            redirectAttributes.addAttribute("email", user.getEmail());
            return "redirect:/verify-otp";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(
            @RequestParam(value = "email", required = false) String email,
            Model model
    ) {
        model.addAttribute("email", email == null ? "" : email);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp,
            Model model
    ) {
        try {
            registrationService.verifyOtp(email, otp);
            return "redirect:/login?verified=true";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("email", email);
            model.addAttribute("error", exception.getMessage());
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            registrationService.resendOtp(email);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Mã OTP mới đã được gửi đến email của bạn."
            );
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        redirectAttributes.addAttribute("email", email);
        return "redirect:/verify-otp";
    }
}