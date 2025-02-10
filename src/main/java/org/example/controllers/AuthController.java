package org.example.controllers;

import io.vertx.ext.web.Router;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.handler.JWTAuthHandler;
import org.example.handlers.AuthHandler;

public class AuthController {
    private final Router router;
    private final AuthHandler authHandler;
    private final JWTAuth jwtAuth;

    public AuthController(Router router, AuthHandler authHandler, JWTAuth jwtAuth) {
        this.router = router;
        this.authHandler = authHandler;
        this.jwtAuth = jwtAuth;
    }

    public void setupRoutes() {
        router.post("/register").handler(authHandler::register);
        router.post("/login").handler(authHandler::login);
        router.post("/logout")
                .handler(JWTAuthHandler.create(jwtAuth))
                .handler(authHandler::logout);
    }
}