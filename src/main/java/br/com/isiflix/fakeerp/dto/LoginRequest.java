package br.com.isiflix.fakeerp.dto;

/**
 * Corpo da requisição de login.
 */
public record LoginRequest(String login, String password) {
}
