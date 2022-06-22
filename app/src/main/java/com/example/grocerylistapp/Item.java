package com.example.grocerylistapp;

import java.time.LocalDateTime;

public class Item {
    String name, itemId;
    Boolean checked;
    Long creationTime;

    public Item(String name, Boolean checked, Long creationTime, String itemId) {
        this.name = name;
        this.checked = checked;
        this.creationTime = creationTime;
        this.itemId = itemId;
    }
}
