package com.blog.Security.OAuth2;

import com.blog.Security.BlogUserDetailsService;
import com.blog.Security.JwtService;
import com.blog.Security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final BlogUserDetailsService userDetailsService;

    @Value("${oauth2.redirect-uri:http://localhost:8080/oauth2/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        log.info("[OAuth2] Authentication success for email: {}", email);

        String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        refreshTokenService.store(refreshToken, userDetails.getUsername());

        log.info("[OAuth2] JWT tokens issued for user: {} ({})", userDetails.getUsername(), email);
        String targetUrl = redirectUri + "?accessToken="  + accessToken + "&refreshToken=" + refreshToken + "&type=Bearer";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
