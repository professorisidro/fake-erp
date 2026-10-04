package br.com.isiflix.fakeerp.controller;

import br.com.isiflix.fakeerp.dto.LoginRequest;
import br.com.isiflix.fakeerp.dto.LoginResponse;
import br.com.isiflix.fakeerp.entity.AppUser;
import br.com.isiflix.fakeerp.repository.UserRepository;
import br.com.isiflix.fakeerp.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Endpoint de autenticação: recebe login/senha e retorna um token JWT.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica o usuário e retorna um token JWT com os escopos dele (claim \"scope\")")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.login(), request.password()));

            String scopes = userRepository.findByUsername(authentication.getName())
                    .map(AppUser::getScopes)
                    .orElse("");
            String token = jwtService.generateToken(authentication.getName(), scopes);
            return ResponseEntity.ok(LoginResponse.bearer(token, jwtService.getExpirationMs(), scopes));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos");
        }
    }
}
