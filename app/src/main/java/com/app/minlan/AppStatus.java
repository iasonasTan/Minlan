package com.app.minlan;

public enum AppStatus {
    FAVOURITE,
    NORMAL,
    WHICHEVER,
    HIDDEN;

    public AppStatus opposite() {
        switch (this) {
            case FAVOURITE:
                return NORMAL;
            case NORMAL:
                return FAVOURITE;
            case HIDDEN:
                return NORMAL;
        }
        return WHICHEVER;
    }

    public boolean isFav() {
        return this == AppStatus.FAVOURITE;
    }
}
