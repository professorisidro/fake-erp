package br.com.isiflix.fakeerp.dto;

/**
 * Resposta do login contendo o token JWT e os escopos concedidos.
 */
public record LoginResponse(String token, String type, long expiresInMs, String scope) {

    public static LoginResponse bearer(String token, long expiresInMs, String scope) {
        return new LoginResponse(token, "Bearer", expiresInMs, scope);
    }
}
