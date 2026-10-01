package vn.iostart.productmanagement.dto.auth;

import lombok.Data;

@Data // Annotation này của Lombok sẽ tự sinh ra các hàm getUsername(), getConfirmPassword(), v.v.
public class RegisterRequest {
    private String username;        // <-- BẠN ĐANG THIẾU DÒNG NÀY
    private String email;
    private String password;
    private String confirmPassword; // <-- BẠN ĐANG THIẾU DÒNG NÀY
    private String fullName;
}