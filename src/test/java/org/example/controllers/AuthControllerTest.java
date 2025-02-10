package org.example.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.config.JWTConfig;
import org.example.handlers.AuthHandler;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
class AuthControllerTest {
    private Vertx vertx;
    private Router router;
    private AuthController authController;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
        this.router = Router.router(vertx);

        JsonObject config = new JsonObject()
                .put("jwt", new JsonObject()
                        .put("secret", "test-secret-that-is-at-least-32-characters")
                        .put("expiration", 3600));

        JWTAuth jwtAuth = JWTConfig.create(vertx, config);
        UserRepository userRepository = new UserRepository(null);
        UserService userService = new UserService(userRepository, jwtAuth);
        AuthHandler authHandler = new AuthHandler(userService);

        authController = new AuthController(router, authHandler, jwtAuth);
    }

    @Test
    void shouldSetupRoutes(VertxTestContext testContext) {
        authController.setupRoutes();

        testContext.verify(() -> {
            assertNotNull(router.get("/login"));
            assertNotNull(router.get("/register"));
            assertNotNull(router.get("/logout"));
        });

        testContext.completeNow();
    }
}