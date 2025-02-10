package org.example.repositories;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.example.exceptions.ApiException;
import org.example.exceptions.ErrorReason;
import org.example.models.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class ItemRepository {
    private static final Logger logger = LoggerFactory.getLogger(ItemRepository.class);
    private final MongoClient mongoClient;
    private static final String COLLECTION = "items";

    public ItemRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public Future<String> save(Item item) {
        try {
            if (item == null) {
                return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
            }

            JsonObject document = new JsonObject()
                    .put("_id", item.getId().toString())
                    .put("owner", item.getOwner().toString())
                    .put("title", item.getTitle());

            return mongoClient.insert(COLLECTION, document)
                    .map(item.getId().toString());

        } catch (Exception e) {
            logger.error("Failed to save item", e);
            return Future.failedFuture(new ApiException(ErrorReason.INTERNAL_SERVER_ERROR));
        }
    }

    public Future<List<Item>> findByOwner(UUID ownerId) {
        try {
            if (ownerId == null) {
                return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
            }

            JsonObject query = new JsonObject()
                    .put("owner", ownerId.toString());

            return mongoClient.find(COLLECTION, query)
                    .map(documents -> documents.stream()
                            .map(this::mapToItem)
                            .toList())
                    .otherwise(err -> {
                        logger.error("Failed to find items by owner", err);
                        throw new ApiException(ErrorReason.INTERNAL_SERVER_ERROR);
                    });
        } catch (Exception e) {
            logger.error("Failed to find items", e);
            return Future.failedFuture(new ApiException(ErrorReason.INTERNAL_SERVER_ERROR));
        }
    }

    private Item mapToItem(JsonObject json) {
        try {
            Item item = new Item();
            item.setId(UUID.fromString(json.getString("_id")));
            item.setOwner(UUID.fromString(json.getString("owner")));
            item.setTitle(json.getString("title"));
            return item;
        } catch (Exception e) {
            logger.error("Failed to map document to Item", e);
            throw new ApiException(ErrorReason.INTERNAL_SERVER_ERROR);
        }
    }
}