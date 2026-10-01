package vn.iostart.productmanagement.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.iostart.productmanagement.dto.auth.JwtResponse;
import vn.iostart.productmanagement.dto.auth.LoginRequest;
import vn.iostart.productmanagement.dto.auth.RegisterRequest;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.security.JwtService;
import vn.iostart.productmanagement.service.UserRegistrationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRegistrationService userRegistrationService; // Đã đổi tên cho rõ nghĩa

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Xác thực thông tin đăng nhập
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // 2. Lấy thông tin UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Tạo JWT Token
        String jwt = jwtService.generateToken(userDetails);

        // 4. Trả về response
        return ResponseEntity.ok(new JwtResponse(jwt, jwtService.getExpirationTime()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            UserAccount user = userRegistrationService.register(registerRequest);
            return ResponseEntity.ok("Đăng ký thành công. Vui lòng kiểm tra email để lấy OTP.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}