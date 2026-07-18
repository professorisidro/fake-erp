package br.com.isiflix.fakeerp.dto;

/**
 * Resposta do login contendo o token JWT.
 */
public record LoginResponse(String token, String type, long expiresInMs) {

    public static LoginResponse bearer(String token, long expiresInMs) {
        return new LoginResponse(token, "Bearer", expiresInMs);
    }
}
