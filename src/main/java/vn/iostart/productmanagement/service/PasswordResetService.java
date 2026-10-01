package vn.iostart.productmanagement.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int OTP_EXPIRATION_MINUTES = 5;

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email");
        }

        UserAccount user = userRepository
                .findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElse(null);

        // Phản hồi cho email không tồn tại giống như email hợp lệ.
        if (user == null || !user.isEnabled()) {
            return;
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        user.setPasswordResetCode(otp);
        user.setPasswordResetCodeExpiresAt(
                LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(user.getEmail());
        message.setSubject("Ma OTP dat lai mat khau - Product Management");
        message.setText("Ma OTP dat lai mat khau cua ban la: " + otp
                + "\nMa co hieu luc trong 5 phut."
                + "\nNeu ban khong yeu cau, hay bo qua email nay.");
        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(String email, String otp,
                              String newPassword, String confirmPassword) {
        if (email == null || email.isBlank()
                || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Email hoặc mã OTP không hợp lệ");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới cần ít nhất 8 ký tự");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        UserAccount user = userRepository
                .findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã OTP không đúng hoặc đã hết hạn"));

        if (!user.isEnabled()
                || user.getPasswordResetCode() == null
                || !user.getPasswordResetCode().equals(otp.trim())
                || user.getPasswordResetCodeExpiresAt() == null
                || !LocalDateTime.now().isBefore(
                        user.getPasswordResetCodeExpiresAt())) {
            throw new IllegalArgumentException("Mã OTP không đúng hoặc đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetCode(null);
        user.setPasswordResetCodeExpiresAt(null);
        userRepository.save(user);
    }
}
