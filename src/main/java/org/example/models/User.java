package org.example.models;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class User {
    private UUID id = UUID.randomUUID();
    private String login;
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public boolean checkPassword(String password) {
        return BCrypt.verifyer().verify(password.toCharArray(), this.password).verified;
    }
}