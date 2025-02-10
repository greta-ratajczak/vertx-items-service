package org.example.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.JWTOptions;
import org.example.exceptions.ApiException;
import org.example.exceptions.ErrorReason;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final JWTAuth jwtAuth;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public UserService(UserRepository userRepository, JWTAuth jwtAuth) {
        this.userRepository = userRepository;
        this.jwtAuth = jwtAuth;
    }

    public Future<Void> register(String login, String password) {
        if (!isValidEmail(login)) {
            return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
        }

        if (password == null || password.length() < 8) {
            return Future.failedFuture(new ApiException(ErrorReason.INVALID_REQUEST));
        }

        return userRepository.findByLogin(login)
                .compose(existingUser -> {
                    if (existingUser != null) {
                        return Future.failedFuture(new ApiException(ErrorReason.USER_ALREADY_EXISTS));
                    }
                    User newUser = new User(login, password);
                    return userRepository.save(newUser)
                            .map(id -> null);
                });
    }

    public Future<String> authenticate(String login, String password) {
        if (!isValidEmail(login) || password == null) {
            return Future.failedFuture(new ApiException(ErrorReason.INVALID_CREDENTIALS));
        }

        return userRepository.findByLogin(login)
                .compose(user -> {
                    if (user == null || !user.checkPassword(password)) {
                        return Future.failedFuture(new ApiException(ErrorReason.INVALID_CREDENTIALS));
                    }
                    return Future.succeededFuture(generateToken(user));
                });
    }

    public Future<Void> logout(String token) {
        logger.debug("User logged out successfully");
        return Future.succeededFuture();
    }

    private String generateToken(User user) {
        JsonObject claims = new JsonObject()
                .put("userId", user.getId().toString())
                .put("login", user.getLogin())
                .put("iat", System.currentTimeMillis() / 1000);

        JWTOptions options = new JWTOptions()
                .setExpiresInSeconds(86400)
                .setIssuer("vertx-items-service")
                .setSubject(user.getId().toString());

        return jwtAuth.generateToken(claims, options);
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}