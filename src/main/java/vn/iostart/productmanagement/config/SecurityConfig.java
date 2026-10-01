package vn.iostart.productmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.iostart.productmanagement.security.CustomUserDetailsService;
import vn.iostart.productmanagement.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Cần bean này để gọi trong ApiAuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/graphql",
                    "/api/**",       // Cho phép API không cần CSRF
                    "/cart/**",      // Cho phép POST giỏ hàng
                    "/checkout/**",  // Cho phép POST thanh toán
                    "/orders/**"     // Cho phép POST lọc đơn
                )
            )

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(authorize -> authorize

                // ============================================================
                // 1. CÁC ĐƯỜNG DẪN CÔNG KHAI (Web & API)
                // ============================================================
                .requestMatchers(
                    "/login",
                    "/register",
                    "/verify-otp",
                    "/resend-otp",
                    "/forgot-password",
                    "/reset-password",
                    "/error",
                    "/access-denied",
                    "/favicon.ico",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/uploads/**",
                    "/api/auth/**",         // API login/register JWT
                    // Swagger UI (để tiện test)
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // ============================================================
                // 2. ADMIN và USER đều được xem chi tiết sản phẩm.
                // ============================================================
                .requestMatchers(
                    HttpMethod.GET,
                    "/products/{id:[0-9]+}"
                ).authenticated()

                // ============================================================
                // 3. CHỈ USER VÀ ADMIN ĐƯỢC TRUY CẬP GIỎ HÀNG, THANH TOÁN, ĐƠN HÀNG
                // ============================================================
                .requestMatchers(
                    "/cart/**",
                    "/checkout/**",
                    "/orders/**"
                ).hasAnyRole("USER", "ADMIN")

                // ============================================================
                // 4. CHỈ ADMIN ĐƯỢC TRUY CẬP CHỨC NĂNG QUẢN LÝ
                // ============================================================
                .requestMatchers(
                    "/products/**",
                    "/categories/**",
                    "/api/**",       // Lưu ý: Nếu muốn USER gọi API thì cần chỉnh lại dòng này
                    "/graphiql",
                    "/graphiql/**"
                ).hasRole("ADMIN")

                // ============================================================
                // 5. CÁC ĐƯỜNG DẪN CÒN LẠI YÊU CẦU ĐĂNG NHẬP
                // ============================================================
                .anyRequest().authenticated()
            )

            // Cấu hình Session: IF_REQUIRED để vừa hỗ trợ Form Login (Web) vừa hỗ trợ JWT (API)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // Thêm JWT Filter vào trước bộ lọc xác thực mặc định
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .rememberMe(remember -> remember
                .key("product-management-remember-key")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )

            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}