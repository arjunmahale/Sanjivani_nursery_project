package com.example.nursery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:{noop}admin123}")
    private String adminPassword;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (adminUsername.equals(username)) {
                return User.withUsername(adminUsername)
                        .password(adminPassword)
                        .roles("ADMIN")
                        .build();
            }
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Delegating encoder supports {noop}, {bcrypt}, {pbkdf2}, etc.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Use AntPathRequestMatcher to avoid ambiguity when multiple servlets exist (e.g. H2 console).
        AntPathRequestMatcher loginMatcher = new AntPathRequestMatcher("/login");
        AntPathRequestMatcher cssMatcher = new AntPathRequestMatcher("/css/**");
        AntPathRequestMatcher jsMatcher = new AntPathRequestMatcher("/js/**");
        AntPathRequestMatcher imgMatcher = new AntPathRequestMatcher("/images/**");
        AntPathRequestMatcher billMatcher = new AntPathRequestMatcher("/orders/*/bill");
        AntPathRequestMatcher h2ConsoleMatcher = new AntPathRequestMatcher("/h2-console/**");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(loginMatcher, cssMatcher, jsMatcher, imgMatcher, billMatcher, h2ConsoleMatcher).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
            )
            // disable CSRF for demo; enable in production and configure properly
            .csrf(csrf -> csrf.disable());

        // H2 console uses frames -> allow frames (only for dev)
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}