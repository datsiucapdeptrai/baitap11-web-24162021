package vn.iostart.productmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.entity.Role;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.RoleRepository;
import vn.iostart.productmanagement.repository.UserAccountRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityDataInitializer {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_SECURITY_PASSWORD}")
    private String adminPassword;

    @Value("${APP_USER_PASSWORD}")
    private String userPassword;

    @Bean
    CommandLineRunner initializeSecurityData() {

        return arguments -> {

            Role adminRole = roleRepository
                    .findByName("ROLE_ADMIN")
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .name("ROLE_ADMIN")
                                            .build()
                            )
                    );

            Role userRole = roleRepository
                    .findByName("ROLE_USER")
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder()
                                            .name("ROLE_USER")
                                            .build()
                            )
                    );

            if (!userRepository
                    .existsByUsernameIgnoreCase("admin")) {

                UserAccount admin =
                        UserAccount.builder()
                                .username("admin")
                                .email("admin@product.local")
                                .password(
                                    passwordEncoder.encode(
                                        adminPassword
                                    )
                                )
                                .fullName(
                                    "Quản trị viên"
                                )
                                .enabled(true)
                                .role(adminRole)
                                .build();

                userRepository.save(admin);
            }

            if (!userRepository
                    .existsByUsernameIgnoreCase("user")) {

                UserAccount user =
                        UserAccount.builder()
                                .username("user")
                                .email("user@product.local")
                                .password(
                                    passwordEncoder.encode(
                                        userPassword
                                    )
                                )
                                .fullName(
                                    "Người dùng"
                                )
                                .enabled(true)
                                .role(userRole)
                                .build();

                userRepository.save(user);
            }
        };
    }
}