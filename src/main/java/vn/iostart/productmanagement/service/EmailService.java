package vn.iostart.productmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendVerificationOtp(
            String receiverEmail,
            String otp
    ) {
        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(receiverEmail);
            message.setSubject(
                    "Mã xác nhận tài khoản Product Management"
            );

            message.setText(
                    """
                    Xin chào,

                    Mã OTP kích hoạt tài khoản của bạn là:

                    %s

                    Mã OTP có hiệu lực trong 5 phút.

                    Nếu bạn không thực hiện đăng ký, hãy bỏ qua email này.

                    Product Management
                    """.formatted(otp)
            );

            mailSender.send(message);

        } catch (MailException exception) {
            throw new IllegalStateException(
                    "Không thể gửi email OTP. "
                    + "Vui lòng kiểm tra cấu hình Gmail.",
                    exception
            );
        }
    }
}