package org.example.config;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.JWTOptions;

public class JWTConfig {
    public static JWTAuth create(Vertx vertx, JsonObject config) {
        JsonObject jwtConfig = config.getJsonObject("jwt", new JsonObject());
        String secret = jwtConfig.getString("secret");
        int expiration = jwtConfig.getInteger("expiration", 86400);

        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }

        JWTAuthOptions authOptions = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(secret)
                        .setSymmetric(true));

        JWTOptions jwtOptions = new JWTOptions()
                .setExpiresInSeconds(expiration)
                .setIssuer("vertx-items-service")
                .setSubject("authentication");

        authOptions.setJWTOptions(jwtOptions);

        return JWTAuth.create(vertx, authOptions);
    }
}