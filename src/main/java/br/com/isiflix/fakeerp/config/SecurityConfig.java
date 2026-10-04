package br.com.isiflix.fakeerp.config;

import br.com.isiflix.fakeerp.security.AppUserDetailsService;
import br.com.isiflix.fakeerp.security.JwtAuthenticationFilter;
import br.com.isiflix.fakeerp.security.ProblemResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração do Spring Security: stateless + JWT, liberando login, Swagger e H2 console.
 * Cada endpoint de negócio exige um escopo específico do JWT:
 * report:read, policy:read, credit:write e credit:approve (só humano).
 * CORS liberado para qualquer client.
 */
@Configuration
public class SecurityConfig {

    public static final String SCOPE_REPORT_READ = "SCOPE_report:read";
    public static final String SCOPE_POLICY_READ = "SCOPE_policy:read";
    public static final String SCOPE_CREDIT_WRITE = "SCOPE_credit:write";
    public static final String SCOPE_CREDIT_APPROVE = "SCOPE_credit:approve";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Suporta prefixos {bcrypt}, {noop}, etc. no banco.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AppUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 console é renderizado em frames -> permite frames de mesma origem
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // Controle de acesso por escopo (claim "scope" do JWT), não só por autenticação
                        .requestMatchers(HttpMethod.GET, "/report/**", "/company/**").hasAuthority(SCOPE_REPORT_READ)
                        .requestMatchers(HttpMethod.GET, "/credit-policy").hasAuthority(SCOPE_POLICY_READ)
                        .requestMatchers(HttpMethod.POST, "/credit-decision").hasAuthority(SCOPE_CREDIT_WRITE)
                        // Aprovação final: escopo que nenhum agente recebe (human-in-the-loop)
                        .requestMatchers(HttpMethod.PATCH, "/credit-decision/*/approve").hasAuthority(SCOPE_CREDIT_APPROVE)
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> ProblemResponses.write(
                                response, HttpStatus.UNAUTHORIZED, "Token ausente, inválido ou expirado",
                                request.getRequestURI()))
                        .accessDeniedHandler((request, response, e) -> ProblemResponses.write(
                                response, HttpStatus.FORBIDDEN,
                                "O token não possui o escopo necessário para esta operação",
                                request.getRequestURI())))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
