package vn.iostart.productmanagement.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iostart.productmanagement.entity.UserAccount;
import vn.iostart.productmanagement.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserAccountRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(
            String login
    ) throws UsernameNotFoundException {

        String normalizedLogin =
                login == null ? "" : login.trim();

        UserAccount user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                        normalizedLogin,
                        normalizedLogin
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Không tìm thấy tài khoản: "
                                        + normalizedLogin
                        )
                );

        return new CustomUserDetails(user);
    }
}