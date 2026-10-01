package vn.iostart.productmanagement.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.dto.auth.RegisterRequest;
import vn.iostart.productmanagement.entity.Role;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.RoleRepository;
import vn.iostart.productmanagement.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private static final int OTP_EXPIRATION_MINUTES = 5;

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public UserAccount register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String fullName = request.getFullName().trim();

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username đã được sử dụng");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new IllegalStateException("Không tìm thấy ROLE_USER")
                );

        String otp = generateOtp();

        UserAccount user = UserAccount.builder()
                .username(username)
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .verificationCode(otp)
                .verificationCodeExpiresAt(
                        LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES)
                )
                .role(userRole)
                .build();

        UserAccount savedUser = userRepository.save(user);
        emailService.sendVerificationOtp(savedUser.getEmail(), otp);

        return savedUser;
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        UserAccount user = findByEmail(email);

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản đã được kích hoạt");
        }

        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã OTP");
        }

        if (user.getVerificationCode() == null
                || !user.getVerificationCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }

        if (user.getVerificationCodeExpiresAt() == null
                || user.getVerificationCodeExpiresAt()
                        .isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã."
            );
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);
    }

    @Transactional
    public void resendOtp(String email) {
        UserAccount user = findByEmail(email);

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Tài khoản đã được kích hoạt");
        }

        String newOtp = generateOtp();

        user.setVerificationCode(newOtp);
        user.setVerificationCodeExpiresAt(
                LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES)
        );

        userRepository.save(user);
        emailService.sendVerificationOtp(user.getEmail(), newOtp);
    }

    private UserAccount findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }

        return userRepository.findByEmailIgnoreCase(
                email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() ->
                new IllegalArgumentException("Không tìm thấy tài khoản")
        );
    }

    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}