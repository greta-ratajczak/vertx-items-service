package org.example.repositories;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class ItemRepositoryTest {
    private ItemRepository itemRepository;
    private MongoClient mongoClient;
    private UUID testUserId;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "test_db");

        mongoClient = MongoClient.create(vertx, config);
        itemRepository = new ItemRepository(mongoClient);
        testUserId = UUID.randomUUID();

        mongoClient.dropCollection("items")
                .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void shouldSaveItem(VertxTestContext testContext) {
        Item item = new Item(testUserId, "Test Item");

        itemRepository.save(item)
                .compose(id -> mongoClient.findOne("items",
                        new JsonObject().put("_id", item.getId().toString()), null))
                .onComplete(testContext.succeeding(result -> {
                    testContext.verify(() -> {
                        assertNotNull(result);
                        assertEquals(item.getId().toString(), result.getString("_id"));
                        assertEquals(testUserId.toString(), result.getString("owner"));
                        assertEquals("Test Item", result.getString("title"));
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldFindItemsByOwner(VertxTestContext testContext) {
        Item item1 = new Item(testUserId, "Test Item 1");
        Item item2 = new Item(testUserId, "Test Item 2");

        itemRepository.save(item1)
                .compose(id -> itemRepository.save(item2))
                .compose(id -> itemRepository.findByOwner(testUserId))
                .onComplete(testContext.succeeding(items -> {
                    testContext.verify(() -> {
                        assertEquals(2, items.size());
                        assertTrue(items.stream()
                                .allMatch(item -> item.getOwner().equals(testUserId)));
                        var titles = items.stream()
                                .map(Item::getTitle)
                                .toList();
                        assertTrue(titles.contains("Test Item 1"));
                        assertTrue(titles.contains("Test Item 2"));
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void shouldReturnEmptyListForNonExistentOwner(VertxTestContext testContext) {
        UUID nonExistentUserId = UUID.randomUUID();

        itemRepository.findByOwner(nonExistentUserId)
                .onComplete(testContext.succeeding(items -> {
                    testContext.verify(() -> {
                        assertTrue(items.isEmpty());
                        testContext.completeNow();
                    });
                }));
    }
}