package com.blog.Security.OAuth2;

import com.blog.Model.Role;
import com.blog.Model.User;
import com.blog.Repository.UserRepository;
import com.blog.Security.BlogUserDetailsService;
import com.blog.Security.JwtService;
import com.blog.Security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final BlogUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private static final String FRONTEND_REDIRECT_URL = "http://localhost:3000/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        if (email == null) {
            log.error("OAuth2 login failed: Google did not return an email address");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 login failed: email not provided by Google");
            return;
        }
        log.info("OAuth2 login success for Google account: {}", email);
        String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("New OAuth2 user — creating account for: {} (username: {})", email, username);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setFullName(name != null ? name : username);
            newUser.setGender("N");
            newUser.setPasswordHash("{oauth2}no-local-password");
            newUser.setRole(Role.READER);
            newUser.setCreatedAt(LocalDateTime.now());

            return userRepository.save(newUser);
        });

        log.info("OAuth2 user resolved: username={}, role={}", user.getUsername(), user.getRole());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        refreshTokenService.store(refreshToken, user.getUsername());

        log.info("JWT tokens issued for OAuth2 user: {}", user.getUsername());

        String redirectUrl = FRONTEND_REDIRECT_URL + "?accessToken="  + accessToken + "&refreshToken=" + refreshToken + "&tokenType=Bearer";
        response.sendRedirect(redirectUrl);
    }
}
