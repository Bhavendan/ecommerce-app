package org.example.ecommerceapp.serviceIMPL;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.ecommerceapp.dto.LoginRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JWTService {
    private final String SECRET = "mysupersecretkeyforecommerceapp-mysupersecretkeyforecommerceapp";

    public String generateToke(LoginRequest request) {
        return Jwts.builder().subject(request.getEmail()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)).signWith(getSignKey()).compact();
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {

        return Jwts.parser().verifyWith((SecretKey) getSignKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token) {

        try {
            Jwts.parser().verifyWith((SecretKey) getSignKey()).build().parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
