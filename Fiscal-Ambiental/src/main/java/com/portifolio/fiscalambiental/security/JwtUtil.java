package com.portifolio.fiscalambiental.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "EstaChaveSuperSecretaEDificilDeQuebrarParaOProjetoPortifolioJWT2026!";
    private final long EXPIRATION_TIME = 86400000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String cpf, String cargo) {
        return Jwts.builder()
                .subject(cpf)
                .claim("cargo", cargo)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractCpf(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractCargo(String token) {
        return extractAllClaims(token).get("cargo", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String cpf) {
        final String extractedCpf = extractCpf(token);
        return (extractedCpf.equals(cpf) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
