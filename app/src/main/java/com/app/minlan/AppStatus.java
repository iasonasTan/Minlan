package com.app.minlan;

public enum AppStatus {
    FAVOURITE,
    NORMAL,
    WHICHEVER,
    HIDDEN;

    @SuppressWarnings("all")
    public AppStatus opposite() {
        switch (this) {
            case FAVOURITE:
                return NORMAL;
            case NORMAL:
                return FAVOURITE;
            case HIDDEN:
                return NORMAL;
            case WHICHEVER:
                return WHICHEVER;
        }
        return WHICHEVER;
    }
}
