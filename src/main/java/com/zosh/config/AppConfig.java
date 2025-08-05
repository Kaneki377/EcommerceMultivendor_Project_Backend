package com.zosh.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class AppConfig {
    //Thiết lập chế độ stateless (không dùng session) , Bảo vệ các API /api/** bằng JWT
    //Cho phép truy cập công khai một số endpoint
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.sessionManagement(management->management.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
        )).authorizeHttpRequests(authorize->authorize

                .requestMatchers("/api/products/*/reviews").permitAll()
                .requestMatchers("/api/koc/create").hasAnyRole("CUSTOMER", "MANAGER")
                .requestMatchers("/api/koc/**").authenticated() // Dựa vào @PreAuthorize
                .requestMatchers("/api/**").authenticated() //bắt buộc yêu cầu phải đã xác thực.

                .anyRequest().permitAll()
        ).addFilterBefore(new JwtTokenValidator(), BasicAuthenticationFilter.class)
                .csrf(csrf->csrf.disable())
                .cors(cors->cors.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    //cấu hình CORS để cho phép frontend từ bất kỳ domain nào gọi API
    private CorsConfigurationSource corsConfigurationSource() {
       return new CorsConfigurationSource(){
           @Override
           public CorsConfiguration getCorsConfiguration(HttpServletRequest request){
               CorsConfiguration cfg = new CorsConfiguration();
               cfg.setAllowedOrigins(Arrays.asList("https://zosh-bazzar-zosh.vercel.app",
                       "http://localhost:3000",
                       "http://localhost:5173"));
               cfg.setAllowedMethods(Collections.singletonList("*"));  //Cho phép tất cả phương thức HTTP
               cfg.setAllowedHeaders(Collections.singletonList("*"));
               cfg.setAllowCredentials(true);                           // Cho phép gửi cookie or token (credentials)
               cfg.setExposedHeaders(Collections.singletonList("Authorization"));
               cfg.setMaxAge(3600L);
               return cfg;
           }
       };
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
