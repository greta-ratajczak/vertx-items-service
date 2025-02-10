package org.example.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class DatabaseConfig {
    public static MongoClient createMongoClient(Vertx vertx, JsonObject config) {
        JsonObject mongoConfig = config.getJsonObject("mongodb", new JsonObject());
        String connectionString = mongoConfig.getString("connection_string", "mongodb://localhost:27017");
        String dbName = mongoConfig.getString("db_name", "vertx-items-service");

        if (connectionString == null || dbName == null) {
            throw new IllegalStateException("MongoDB configuration is incomplete");
        }

        return MongoClient.create(vertx, new JsonObject()
                .put("connection_string", connectionString)
                .put("db_name", dbName));
    }
}