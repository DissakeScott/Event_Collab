package com.eventcollab.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtService jwtService;

    // Spring injecte JwtService via ce constructeur
    public JwtAuthFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Requete sans token vers : {}",
                        exchange.getRequest().getPath());
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            if (!jwtService.isValid(token)) {
                log.warn("Token invalide vers : {}",
                        exchange.getRequest().getPath());
                return unauthorized(exchange);
            }

            String email  = jwtService.extractSubject(token);
            String userId = jwtService.extractClaim(token, "userId");
            String role   = jwtService.extractClaim(token, "role");

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.set("X-User-Email", email);
                        headers.set("X-User-Id",    userId);
                        headers.set("X-User-Role",  role);
                    }))
                    .build();

            log.debug("JWT valide pour {} [{}] -> {}",
                    email, role, exchange.getRequest().getPath());

            return chain.filter(mutatedExchange);
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Pas de parametres necessaires
    }
}