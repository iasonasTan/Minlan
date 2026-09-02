package com.app.minlan.view;

import android.content.Context;
import com.app.minlan.ReloadCallback;

import java.util.function.Consumer;

public final class AppViewFactory {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FAVOURITE = 1;
    public static final int TYPE_HIDDEN = 2;

    private AppViewFactory(){
    }

    public static AbstractAppView createAppView(Context context, ReloadCallback reloadCallback, Consumer<String> onLaunchAppListener, int viewType) {
        switch (viewType) {
            case TYPE_HIDDEN:
                return new HiddenAppView(context, reloadCallback, onLaunchAppListener);
            case TYPE_FAVOURITE:
                return new FavouriteAppView(context, reloadCallback, onLaunchAppListener);
            case TYPE_NORMAL:
            default:
                return new NormalAppView(context, reloadCallback, onLaunchAppListener);
        }
    }
}
