package com.medconnect.config;

import com.medconnect.service.CustomAuthenticationException;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // THÊM DÒNG NÀY ĐỂ CHO PHÉP TÀI NGUYÊN TĨNH
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // Các URL công khai
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/", "/index", "/register", "/login", "/forgot-password", "/verify", "/search-doctors", "/doctor-profile-view/**").permitAll()

                        // Các URL theo vai trò
                        .requestMatchers("/patient-profile", "/patient-dashboard", "/book-appointment", "/payment", "/payment-callback", "/cancel-appointment", "/review").hasRole("Patient")
                        .requestMatchers("/doctor-profile", "/doctor-schedule", "/doctor-schedule/**", "/doctor-dashboard/**", "/write-summary", "/delete-document/**").hasRole("Doctor")
                        .requestMatchers("/admin-doctor-approval", "/admin-specializations", "/admin-doctor-list", "/admin-patient-list").hasRole("Admin")

                        // Tất cả các request khác cần xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        // THAY THẾ BỘ XỬ LÝ LỖI BẰNG CODE NGAY TẠI ĐÂY
                        .failureHandler((request, response, exception) -> {
                            String errorMessage;
                            if (exception instanceof BadCredentialsException) {
                                errorMessage = "MSG05: Email hoặc mật khẩu không chính xác.";
                            } else {
                                errorMessage = exception.getMessage();
                            }
                            HttpSession session = request.getSession();
                            session.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, new CustomAuthenticationException(errorMessage));
                            response.sendRedirect(request.getContextPath() + "/login");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/"; // URL mặc định

            for (var authority : authentication.getAuthorities()) {
                String authorityName = authority.getAuthority();
                if ("ROLE_Admin".equals(authorityName)) {
                    redirectUrl = "/admin-doctor-approval";
                    break;
                } else if ("ROLE_Doctor".equals(authorityName)) {
                    redirectUrl = "/doctor-dashboard";
                    break;
                } else if ("ROLE_Patient".equals(authorityName)) {
                    redirectUrl = "/patient-dashboard";
                    break;
                }
            }
            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }
}