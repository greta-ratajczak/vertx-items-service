package org.example.handlers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;
import org.example.exceptions.ApiException;
import org.example.exceptions.ErrorReason;
import org.example.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);
    private final UserService userService;

    public AuthHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(RoutingContext ctx) {
        try {
            JsonObject body = ctx.body().asJsonObject();
            String login = body.getString("login");
            String password = body.getString("password");

            if (login == null || password == null) {
                throw new ApiException(ErrorReason.INVALID_REQUEST);
            }

            userService.register(login, password)
                    .onSuccess(v -> ctx.response().setStatusCode(204).end())
                    .onFailure(err -> handleError(ctx, err));
        } catch (DecodeException e) {
            handleError(ctx, new ApiException(ErrorReason.INVALID_REQUEST));
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    public void login(RoutingContext ctx) {
        try {
            JsonObject body = ctx.body().asJsonObject();
            String login = body.getString("login");
            String password = body.getString("password");

            if (login == null || password == null) {
                throw new ApiException(ErrorReason.INVALID_REQUEST);
            }

            userService.authenticate(login, password)
                    .onSuccess(token -> {
                        JsonObject response = new JsonObject().put("token", token);
                        ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(response.encode());
                    })
                    .onFailure(err -> handleError(ctx, err));
        } catch (DecodeException e) {
            handleError(ctx, new ApiException(ErrorReason.INVALID_REQUEST));
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    public void logout(RoutingContext ctx) {
        try {
            JsonObject principal = ctx.user().principal();
            if (principal == null || principal.getString("access_token") == null) {
                throw new ApiException(ErrorReason.UNAUTHORIZED);
            }
            String token = principal.getString("access_token");

            userService.logout(token)
                    .onSuccess(v -> ctx.response()
                            .setStatusCode(204)
                            .end())
                    .onFailure(err -> handleError(ctx, err));
        } catch (Exception e) {
            handleError(ctx, e);
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