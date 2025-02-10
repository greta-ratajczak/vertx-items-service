package org.example;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class MainVerticleTest {
    private WebClient webClient;
    private MongoClient mongoClient;
    private static final int TEST_PORT = 8888;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        webClient = WebClient.create(vertx);

        JsonObject config = new JsonObject()
                .put("http.port", TEST_PORT)
                .put("mongodb", new JsonObject()
                        .put("connection_string", "mongodb://localhost:27017")
                        .put("db_name", "test_db"))
                .put("jwt", new JsonObject()
                        .put("secret", "test-secret-key-that-is-at-least-32-chars-long")
                        .put("expiration", 3600));

        mongoClient = MongoClient.create(vertx, config.getJsonObject("mongodb"));

        mongoClient.dropCollection("users")
                .compose(v -> mongoClient.dropCollection("items"))
                .compose(v -> {
                    DeploymentOptions options = new DeploymentOptions()
                            .setConfig(config);

                    return vertx.deployVerticle(new MainVerticle(), options);
                })
                .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void serverShouldStart(VertxTestContext testContext) {
        webClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/")
                .send()
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> {
                        assertEquals(404, response.statusCode()); // Oczekujemy 404, bo nie mamy zdefiniowanej ścieżki "/"
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldRejectInvalidRegistration(VertxTestContext testContext) {
        JsonObject invalidUser = new JsonObject()
                .put("login", "invalid-email")
                .put("password", "short");

        webClient.request(HttpMethod.POST, TEST_PORT, "localhost", "/register")
                .sendJsonObject(invalidUser)
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> {
                        assertEquals(400, response.statusCode());
                        assertTrue(response.bodyAsJsonObject().getString("error").contains("Invalid request"));
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldRejectUnauthenticatedItemsAccess(VertxTestContext testContext) {
        webClient.request(HttpMethod.GET, TEST_PORT, "localhost", "/items")
                .send()
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> {
                        assertEquals(401, response.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldRegisterAndLogin(VertxTestContext testContext) {
        JsonObject user = new JsonObject()
                .put("login", "test@example.com")
                .put("password", "Password123!");

        mongoClient.count("users", new JsonObject())
                .compose(count -> {
                    assertEquals(0, count);

                    return webClient.request(HttpMethod.POST, TEST_PORT, "localhost", "/register")
                            .sendJsonObject(user);
                })
                .compose(registerResponse -> {
                    assertEquals(204, registerResponse.statusCode());

                    return webClient.request(HttpMethod.POST, TEST_PORT, "localhost", "/login")
                            .sendJsonObject(user);
                })
                .onComplete(testContext.succeeding(loginResponse -> {
                    testContext.verify(() -> {
                        assertEquals(200, loginResponse.statusCode());
                        assertNotNull(loginResponse.bodyAsJsonObject().getString("token"));
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldHandleCORS(VertxTestContext testContext) {
        webClient.request(HttpMethod.OPTIONS, TEST_PORT, "localhost", "/items")
                .putHeader("Origin", "http://localhost:3000")
                .putHeader("Access-Control-Request-Method", "GET")
                .send()
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> {
                        assertEquals(204, response.statusCode());
                        assertNotNull(response.getHeader("Access-Control-Allow-Origin"));
                        assertNotNull(response.getHeader("Access-Control-Allow-Methods"));
                        testContext.completeNow();
                    });
                }));
    }
}