package vn.iostart.productmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iostart.productmanagement.dto.auth.LoginRequest;
import vn.iostart.productmanagement.dto.auth.RegisterRequest;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserAccount signup(RegisterRequest input) {
        UserAccount user = new UserAccount();
        user.setFullName(input.getFullName());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        // Bạn có thể cần set thêm username nếu Entity yêu cầu
        // user.setUsername(input.getEmail()); 
        return userRepository.save(user);
    }

    public UserAccount authenticate(LoginRequest input) {
        // 1. Xác thực qua AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );
        
        // 2. Nếu thành công, tìm user trong DB trả về
        return userRepository.findByEmailIgnoreCase(input.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }
}