package org.example.repositories;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class UserRepositoryTest {
    private UserRepository userRepository;
    private MongoClient mongoClient;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "test_db");

        mongoClient = MongoClient.create(vertx, config);
        userRepository = new UserRepository(mongoClient);

        mongoClient.dropCollection("users")
                .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void shouldSaveUser(VertxTestContext testContext) {
        User user = new User("test@example.com", "password123");

        userRepository.save(user)
                .compose(id -> mongoClient.findOne("users",
                        new JsonObject().put("_id", user.getId().toString()), null))
                .onComplete(testContext.succeeding(result -> {
                    testContext.verify(() -> {
                        assertNotNull(result);
                        assertEquals(user.getId().toString(), result.getString("_id"));
                        assertEquals(user.getLogin(), result.getString("login"));
                        assertNotNull(result.getString("password"));
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldFindUserByLogin(VertxTestContext testContext) {
        User user = new User("test@example.com", "password123");

        userRepository.save(user)
                .compose(id -> userRepository.findByLogin(user.getLogin()))
                .onComplete(testContext.succeeding(foundUser -> {
                    testContext.verify(() -> {
                        assertNotNull(foundUser);
                        assertEquals(user.getLogin(), foundUser.getLogin());
                        assertEquals(user.getId(), foundUser.getId());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldReturnNullForNonExistentUser(VertxTestContext testContext) {
        userRepository.findByLogin("nonexistent@example.com")
                .onComplete(testContext.succeeding(result -> {
                    testContext.verify(() -> {
                        assertNull(result);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldCheckIfUserExists(VertxTestContext testContext) {
        User user = new User("test@example.com", "password123");

        userRepository.save(user)
                .compose(id -> userRepository.existsByLogin(user.getLogin()))
                .onComplete(testContext.succeeding(exists -> {
                    testContext.verify(() -> {
                        assertTrue(exists);
                        testContext.completeNow();
                    });
                }));
    }
}