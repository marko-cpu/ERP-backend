package com.app.erp.entity;

public enum Category {
    ELECTRONICS("Electronics"),
    FASHION("Fashion"),
    HOME("Home"),
    BEAUTY("Beauty"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}