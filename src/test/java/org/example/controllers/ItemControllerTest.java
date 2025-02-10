package org.example.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.config.JWTConfig;
import org.example.handlers.ItemHandler;
import org.example.repositories.ItemRepository;
import org.example.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
class ItemControllerTest {
    private Vertx vertx;
    private Router router;
    private ItemController itemController;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
        this.router = Router.router(vertx);

        JsonObject config = new JsonObject()
                .put("jwt", new JsonObject()
                        .put("secret", "test-secret-that-is-at-least-32-characters")
                        .put("expiration", 3600));

        JWTAuth jwtAuth = JWTConfig.create(vertx, config);
        ItemRepository itemRepository = new ItemRepository(null);
        ItemService itemService = new ItemService(itemRepository);
        ItemHandler itemHandler = new ItemHandler(itemService);

        itemController = new ItemController(router, itemHandler, jwtAuth);
    }

    @Test
    void shouldSetupRoutes(VertxTestContext testContext) {
        itemController.setupRoutes();

        testContext.verify(() -> {
            assertNotNull(router.get("/items"));
            assertNotNull(router.post("/items"));
        });

        testContext.completeNow();
    }
}