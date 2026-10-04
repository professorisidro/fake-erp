package br.com.isiflix.fakeerp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Geração e validação de tokens JWT assinados com HMAC-SHA256.
 * Os escopos do usuário vão na claim "scope" (separados por espaço, como no OAuth2).
 */
@Service
public class JwtService {

    public static final String SCOPE_CLAIM = "scope";

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateToken(String username, String scopes) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim(SCOPE_CLAIM, scopes)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Valida a assinatura/expiração e retorna as claims do token.
     * Lança {@link io.jsonwebtoken.JwtException} se o token for inválido.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Converte a string de escopos ("report:read policy:read") em lista.
     */
    public static List<String> splitScopes(String scopes) {
        if (scopes == null || scopes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scopes.trim().split("\\s+")).toList();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
