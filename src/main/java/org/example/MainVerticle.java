package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.example.config.DatabaseConfig;
import org.example.config.JWTConfig;
import org.example.controllers.AuthController;
import org.example.controllers.ItemController;
import org.example.handlers.AuthHandler;
import org.example.handlers.ItemHandler;
import org.example.repositories.ItemRepository;
import org.example.repositories.UserRepository;
import org.example.services.ItemService;
import org.example.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private MongoClient mongoClient;
    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            validateConfig(config());

            mongoClient = DatabaseConfig.createMongoClient(vertx, config());

            JWTAuth jwtAuth = JWTConfig.create(vertx, config());

            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            setupCors(router);

            UserRepository userRepository = new UserRepository(mongoClient);
            ItemRepository itemRepository = new ItemRepository(mongoClient);

            UserService userService = new UserService(userRepository, jwtAuth);
            ItemService itemService = new ItemService(itemRepository);

            AuthHandler authHandler = new AuthHandler(userService);
            ItemHandler itemHandler = new ItemHandler(itemService);

            new AuthController(router, authHandler, jwtAuth).setupRoutes();
            new ItemController(router, itemHandler, jwtAuth).setupRoutes();

            int port = config().getInteger("http.port", 3000);
            httpServer = vertx.createHttpServer()
                    .requestHandler(router);

            httpServer.listen(port)
                    .onSuccess(server -> {
                        logger.info("HTTP server started on port {}", server.actualPort());
                        startPromise.complete();
                    })
                    .onFailure(err -> {
                        logger.error("Failed to start HTTP server", err);
                        startPromise.fail(err);
                    });
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            startPromise.fail(e);
        }
    }

    private void validateConfig(JsonObject config) {
        if (config == null) {
            throw new IllegalStateException("Configuration is missing");
        }

        Integer httpPort = config.getInteger("http.port");
        if (httpPort == null) {
            throw new IllegalStateException("HTTP port configuration is missing");
        }

        JsonObject mongoConfig = config.getJsonObject("mongodb");
        if (mongoConfig == null ||
                mongoConfig.getString("connection_string") == null ||
                mongoConfig.getString("db_name") == null) {
            throw new IllegalStateException("MongoDB configuration is incomplete");
        }

        JsonObject jwtConfig = config.getJsonObject("jwt");
        if (jwtConfig == null ||
                jwtConfig.getString("secret") == null) {
            throw new IllegalStateException("JWT configuration is incomplete");
        }
    }

    private void setupCors(Router router) {
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization"));
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        CompositeFuture.all(
                mongoClient != null ? mongoClient.close() : Future.succeededFuture(),
                httpServer != null ? httpServer.close() : Future.succeededFuture()
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                logger.info("Application stopped successfully");
                stopPromise.complete();
            } else {
                logger.error("Error while stopping application", ar.cause());
                stopPromise.fail(ar.cause());
            }
        });
    }
}