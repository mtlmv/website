package com.project.website.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET_KEY =
            "mySecretKeyForWebsiteProjectMySecretKey123456";

    private static SecretKey getKey(){
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes()
        );

    }
    public static String generateToken(String email){

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                +1000*60*60*24
                        )
                )
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
