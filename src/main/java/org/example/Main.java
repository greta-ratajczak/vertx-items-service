package org.example;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        JsonObject config = new JsonObject()
                .put("http.port", 3000)
                .put("mongodb", new JsonObject()
                        .put("connection_string", "mongodb://localhost:27017")
                        .put("db_name", "vertx-items-service"))
                .put("jwt", new JsonObject()
                        .put("secret", "your-256-bit-secret-key-change-this-in-production")
                        .put("expiration", 86400));

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(config);

        vertx.deployVerticle(new MainVerticle(), options)
                .onSuccess(id -> logger.info("Application started successfully"))
                .onFailure(err -> {
                    logger.error("Failed to start application", err);
                    System.exit(1);
                });
    }
}