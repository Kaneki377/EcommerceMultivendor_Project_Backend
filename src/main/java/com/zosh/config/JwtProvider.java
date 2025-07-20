package com.zosh.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtProvider {

    //Tạo SecretKey dùng thuật toán HMAC-SHA (HS256).
    SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

    //tạo JWT token chứa thông tin quyền của người dùng, có hiệu lực 24h, dùng để xác thực ở các request sau.
    public String generateToken(Authentication auth) {

        //Lấy danh sách quyền người dùng để nhúng vào JWT
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        String roles = populateAuthorities(authorities);

        return Jwts.builder().setIssuedAt(new Date())
                        .setExpiration(new Date(new Date().getTime()+86400000))
                        .claim("username",auth.getName())
                        .claim("authorities",roles)
                        .signWith(key)
                        .compact();
      //  return jwt;
    }
    // Trích xuất username từ JWT token.
    public String getUsernameFromJwtToken(String jwt) {
        jwt=jwt.substring(7);

        Claims claims=Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
        String username=String.valueOf(claims.get("username"));

        return username;
    }

    // Biến danh sách quyền (roles) thành một chuỗi, ngăn cách bằng dấu phẩy.

    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths=new HashSet<>();

        for(GrantedAuthority authority:authorities) {
            auths.add(authority.getAuthority());
        }
        return String.join(",",auths);
    }
}
