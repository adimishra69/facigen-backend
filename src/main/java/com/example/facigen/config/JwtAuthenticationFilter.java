package com.example.facigen.config;

import com.example.facigen.Entity.User;
import com.example.facigen.Entity.UserSession;
import com.example.facigen.repository.UserRepository;
import com.example.facigen.repository.UserSessionRepository;
import com.example.facigen.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authorization.substring(7);

        try {

            // 1. Validate JWT
            if (!jwtService.isTokenValid(jwtToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extract user information from JWT
            Long userId =
                    jwtService.extractUserId(jwtToken);

            String jwtRole =
                    jwtService.extractRole(jwtToken);

            // 3. Find user
            User user =
                    userRepository.findById(userId)
                            .orElse(null);

            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Verify role
            if (!user.getRole().name().equals(jwtRole)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Create authenticated user
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" +
                                                    user.getRole().name()
                                    )
                            )
                    );

            // 6. Store authentication
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {
            // Invalid JWT → remains unauthenticated
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}