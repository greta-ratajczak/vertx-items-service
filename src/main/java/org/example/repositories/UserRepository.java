package org.example.repositories;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.example.exceptions.ApiException;
import org.example.exceptions.ErrorReason;
import org.example.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final MongoClient mongoClient;
    private static final String COLLECTION = "users";

    public UserRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public Future<String> save(User user) {
        try {
            if (user == null) {
                return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
            }

            JsonObject document = new JsonObject()
                    .put("_id", user.getId().toString())
                    .put("login", user.getLogin())
                    .put("password", user.getPassword());

            return mongoClient.insert(COLLECTION, document)
                    .map(user.getId().toString());

        } catch (Exception e) {
            logger.error("Failed to save user", e);
            return Future.failedFuture(new ApiException(ErrorReason.INTERNAL_SERVER_ERROR));
        }
    }

    public Future<User> findByLogin(String login) {
        try {
            if (login == null || login.trim().isEmpty()) {
                return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
            }

            JsonObject query = new JsonObject().put("login", login);

            return mongoClient.findOne(COLLECTION, query, null)
                    .map(result -> {
                        if (result == null) {
                            return null;
                        }
                        try {
                            User user = new User();
                            user.setId(UUID.fromString(result.getString("_id")));
                            user.setLogin(result.getString("login"));
                            user.setPassword(result.getString("password"));
                            return user;
                        } catch (Exception e) {
                            logger.error("Failed to map user data", e);
                            throw new ApiException(ErrorReason.INTERNAL_SERVER_ERROR);
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to find user by login", e);
            return Future.failedFuture(new ApiException(ErrorReason.INTERNAL_SERVER_ERROR));
        }
    }

    public Future<Boolean> existsByLogin(String login) {
        try {
            if (login == null || login.trim().isEmpty()) {
                return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
            }

            JsonObject query = new JsonObject().put("login", login);
            return mongoClient.count(COLLECTION, query)
                    .map(count -> count > 0);
        } catch (Exception e) {
            logger.error("Failed to check if user exists", e);
            return Future.failedFuture(new ApiException(ErrorReason.INTERNAL_SERVER_ERROR));
        }
    }
}