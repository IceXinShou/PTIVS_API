package com.test.util;

public enum PageKey {
    ABSENT("010010"),
    HISTORY_ABSENT("010030"),
    REWARDS("010040"),
    SCORE("010090"),
    HISTORY_REWARDS("010050"),
    PUNISHED_CANCEL_LOG("010060"),
    CLUBS("010070"),
    CADRES("010080"),
    HISTORY_SCORE("010110"),
    CLASS_TABLE("010130");

    private final String id;

    PageKey(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}