package com.blog.Security;

import com.blog.Model.Role;
import com.blog.Model.User;
import com.blog.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleId = (String) attributes.get("sub");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not returned by Google OAuth2");
        }
        String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        Optional<User> existingOpt = userRepository.findByEmail(email);
        User user;
        if (existingOpt.isPresent()) {
            user = existingOpt.get();
            log.info("[OAuth2] Existing user logged in via Google: {} (role={})", email, user.getRole());
        } else {
            user = new User();
            user.setUsername(ensureUniqueUsername(username));
            user.setEmail(email);
            user.setFullName(name != null ? name : username);
            user.setGender("N");
            user.setPasswordHash("{oauth2}" + googleId);
            user.setRole(Role.READER);
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);
            log.info("[OAuth2] New user registered via Google: {} → role=READER", email);
        }
        String authority = "ROLE_" + user.getRole().name();
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(authority)), attributes, "email");
    }
    private String ensureUniqueUsername(String base) {
        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
