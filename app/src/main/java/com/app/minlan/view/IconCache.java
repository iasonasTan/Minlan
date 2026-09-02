package com.app.minlan.view;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class IconCache {
    /**
     * Singleton pattern.
     */
    public static final IconCache instance = new IconCache();

    private final int MAX_MEMORY = (int)(Runtime.getRuntime().maxMemory() / 1024);
    private final int CACHE_SIZE = MAX_MEMORY / 8;

    private final LruCache<String, Drawable> mMemoryCache = new LruCache<>(CACHE_SIZE){
        @Override
        public int sizeOf(@NonNull String key, Drawable value) {
            int width = value.getIntrinsicWidth() > 0 ? value.getIntrinsicWidth() : 100;
            int height= value.getIntrinsicHeight()> 0 ? value.getIntrinsicHeight(): 100;
            return (width * height * 4) / 1024;
        }
    };

    public Optional<Drawable> getDrawable(@NotNull String key) {
        return Optional.ofNullable(mMemoryCache.get(key));
    }

    public void putDrawable(@NotNull String key, Drawable value) {
        if(getDrawable(key).isEmpty()) {
            mMemoryCache.put(key, value);
        }
    }

    /*
     * To clear memory cache execute:
     * mMemoryCache.evictAll();
     */

    /**
     * Private constructor to prevent instantiation.
     */
    private IconCache() {
    }
}
