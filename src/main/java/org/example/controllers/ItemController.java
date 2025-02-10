package org.example.controllers;

import io.vertx.ext.web.Router;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.example.handlers.ItemHandler;

public class ItemController {
    private final Router router;
    private final ItemHandler itemHandler;
    private final JWTAuth jwtAuth;

    public ItemController(Router router, ItemHandler itemHandler, JWTAuth jwtAuth) {
        this.router = router;
        this.itemHandler = itemHandler;
        this.jwtAuth = jwtAuth;
    }

    public void setupRoutes() {
        router.route("/items*").handler(JWTAuthHandler.create(jwtAuth));
        router.post("/items").handler(itemHandler::createItem);
        router.get("/items").handler(itemHandler::getItems);
    }
}