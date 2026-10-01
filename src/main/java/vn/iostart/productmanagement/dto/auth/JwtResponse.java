package vn.iostart.productmanagement.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private long expiresIn;
    
    // Constructor dùng cho service
    public JwtResponse(String token) {
        this.token = token;
    }
}