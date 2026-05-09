package org.example.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                Claims claims = jwtService.parseClaims(token);
                String username = claims.getSubject();
                String role = String.valueOf(claims.get("role"));

                if (role == null || role.isBlank() || "null".equals(role)) {
                    throw new IllegalArgumentException("JWT does not contain 'role' claim");
                }

                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT auth ok: user={}, role={}, path={}, method={}", username, role, request.getRequestURI(), request.getMethod());
            } catch (Exception ex) {
                log.warn(
                        "JWT auth failed: path={}, method={}, headerStartsWithBearer={}, message={}",
                        request.getRequestURI(),
                        request.getMethod(),
                        header != null && header.startsWith("Bearer "),
                        ex.getMessage()
                );
                SecurityContextHolder.clearContext();
            }
        } else if (header != null) {
            log.warn(
                    "Authorization header present but not Bearer: path={}, method={}, headerPrefix={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    header.length() > 20 ? header.substring(0, 20) : header
            );
        }

        filterChain.doFilter(request, response);
    }
}
