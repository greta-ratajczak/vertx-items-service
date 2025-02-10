package org.example.services;

import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.exceptions.ApiException;
import org.example.repositories.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class ItemServiceTest {
    private ItemService itemService;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        ItemRepository itemRepository = new ItemRepository(null);
        itemService = new ItemService(itemRepository);
        testUserId = UUID.randomUUID();
    }

    @Test
    void shouldRejectEmptyTitle(VertxTestContext testContext) {
        itemService.createItem(testUserId, "")
                .onComplete(testContext.failing(err -> {
                    testContext.verify(() -> {
                        assertTrue(err instanceof ApiException);
                        assertEquals("Invalid request", err.getMessage());
                    });
                    testContext.completeNow();
                }));
    }

    @Test
    void shouldRejectNullTitle(VertxTestContext testContext) {
        itemService.createItem(testUserId, null)
                .onComplete(testContext.failing(err -> {
                    testContext.verify(() -> {
                        assertTrue(err instanceof ApiException);
                        assertEquals("Invalid request", err.getMessage());
                    });
                    testContext.completeNow();
                }));
    }

    @Test
    void shouldReturnEmptyListForNewUser(VertxTestContext testContext) {
        UUID newUserId = UUID.randomUUID();
        itemService.getUserItems(newUserId)
                .onComplete(testContext.succeeding(items -> {
                    testContext.verify(() -> {
                        assertNotNull(items);
                        assertTrue(items instanceof JsonArray);
                        assertEquals(0, items.size());
                    });
                    testContext.completeNow();
                }));
    }
}