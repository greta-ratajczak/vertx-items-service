package org.example.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Item {
    private UUID id = UUID.randomUUID();
    private UUID owner;
    private String title;

    public Item(UUID owner, String title) {
        this.owner = owner;
        this.title = title;
    }
}