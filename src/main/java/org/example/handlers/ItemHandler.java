package org.example.handlers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;
import org.example.exceptions.ApiException;
import org.example.exceptions.ErrorReason;
import org.example.services.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ItemHandler {
    private static final Logger logger = LoggerFactory.getLogger(ItemHandler.class);
    private final ItemService itemService;

    public ItemHandler(ItemService itemService) {
        this.itemService = itemService;
    }

    public void createItem(RoutingContext ctx) {
        try {
            UUID userId = getUserId(ctx);
            JsonObject body = ctx.body().asJsonObject();
            String title = body.getString("title");

            if (title == null || title.trim().isEmpty()) {
                throw new ApiException(ErrorReason.INVALID_REQUEST);
            }

            itemService.createItem(userId, title)
                    .onSuccess(v -> ctx.response().setStatusCode(204).end())
                    .onFailure(err -> handleError(ctx, err));
        } catch (DecodeException e) {
            handleError(ctx, new ApiException(ErrorReason.INVALID_REQUEST));
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    public void getItems(RoutingContext ctx) {
        try {
            UUID userId = getUserId(ctx);
            itemService.getUserItems(userId)
                    .onSuccess(items -> ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(items.encode()))
                    .onFailure(err -> handleError(ctx, err));
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    private UUID getUserId(RoutingContext ctx) {
        try {
            String userId = ctx.user().principal().getString("userId");
            return UUID.fromString(userId);
        } catch (Exception e) {
            logger.error("Failed to get user ID from token", e);
            throw new ApiException(ErrorReason.UNAUTHORIZED);
        }
    }

    private void handleError(RoutingContext ctx, Throwable err) {
        if (err instanceof ApiException apiException) {
            ctx.response()
                    .setStatusCode(apiException.getStatusCode())
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", apiException.getMessage())
                            .encode());
        } else {
            logger.error("Unexpected error", err);
            ctx.response()
                    .setStatusCode(ErrorReason.INTERNAL_SERVER_ERROR.getStatusCode())
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("error", ErrorReason.INTERNAL_SERVER_ERROR.getMessage())
                            .encode());
        }
    }
}