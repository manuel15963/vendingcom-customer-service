package com.vendingcom.customer_service.web;

import com.vendingcom.customer_service.application.dto.response.PagedResponse;
import com.vendingcom.customer_service.application.port.input.CustomerUseCase;
import com.vendingcom.customer_service.application.service.AuditLogCleanupService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
class CustomerSecurityTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerUseCase customerUseCase;

    // Neutraliza el job @Scheduled de limpieza para que no toque la BD en el test.
    @MockitoBean
    private AuditLogCleanupService auditLogCleanupService;

    @Value("${jwt.secret}")
    private String secret;

    private String token(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("admin")
                .claim("userId", 1)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }

    @Test
    void sinToken_devuelve401() {
        webTestClient.get().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void conRolOperador_devuelve403() {
        webTestClient.get().uri("/api/v1/customers")
                .header("Authorization", "Bearer " + token(List.of("OPERADOR")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void conRolAdmin_devuelve200() {
        when(customerUseCase.search(any(), any(), any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Mono.just(PagedResponse.of(List.of(), 0, 20, 0)));

        webTestClient.get().uri("/api/v1/customers")
                .header("Authorization", "Bearer " + token(List.of("ADMIN")))
                .exchange()
                .expectStatus().isOk();
    }
}
