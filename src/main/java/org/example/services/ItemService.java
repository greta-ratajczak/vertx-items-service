package org.example.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.exceptions.ApiException;
import org.example.exceptions.ErrorReason;
import org.example.models.Item;
import org.example.repositories.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Future<Void> createItem(UUID userId, String title) {
        if (title == null || title.trim().isEmpty()) {
            return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
        }
        Item item = new Item(userId, title);
        return itemRepository.save(item)
                .map(id -> null);
    }

    public Future<JsonArray> getUserItems(UUID userId) {
        return itemRepository.findByOwner(userId)
                .map(items -> items.stream()
                        .map(item -> new JsonObject()
                                .put("id", item.getId().toString())
                                .put("title", item.getTitle()))
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    }
}