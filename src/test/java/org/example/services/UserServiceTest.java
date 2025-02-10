package org.example.services;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.config.JWTConfig;
import org.example.exceptions.ApiException;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class UserServiceTest {
    private UserService userService;
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;

        JsonObject config = new JsonObject()
                .put("jwt", new JsonObject()
                        .put("secret", "test-secret-that-is-at-least-32-characters")
                        .put("expiration", 3600));

        JWTAuth jwtAuth = JWTConfig.create(vertx, config);
        UserRepository userRepository = new UserRepository(null);
        userService = new UserService(userRepository, jwtAuth);
    }

    @Test
    void shouldRejectInvalidEmail(VertxTestContext testContext) {
        userService.register("invalid-email", "Password123!")
                .onComplete(testContext.failing(err -> {
                    testContext.verify(() -> {
                        assertTrue(err instanceof ApiException);
                        assertEquals("Invalid request", err.getMessage());
                    });
                    testContext.completeNow();
                }));
    }

    @Test
    void shouldRejectShortPassword(VertxTestContext testContext) {
        userService.register("test@example.com", "short")
                .onComplete(testContext.failing(err -> {
                    testContext.verify(() -> {
                        assertTrue(err instanceof ApiException);
                        assertEquals("Invalid request", err.getMessage());
                    });
                    testContext.completeNow();
                }));
    }

    @Test
    void shouldRejectNullCredentials(VertxTestContext testContext) {
        userService.authenticate(null, null)
                .onComplete(testContext.failing(err -> {
                    testContext.verify(() -> {
                        assertTrue(err instanceof ApiException);
                        assertEquals("Invalid credentials", err.getMessage());
                    });
                    testContext.completeNow();
                }));
    }
}