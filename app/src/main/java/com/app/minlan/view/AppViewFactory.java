package com.app.minlan.view;

import android.content.Context;
import com.app.minlan.ReloadCallback;

public final class AppViewFactory {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FAVOURITE = 1;
    public static final int TYPE_HIDDEN = 2;

    private AppViewFactory(){
    }

    public static AbstractAppView createAppView(Context context, ReloadCallback reloadCallback, int viewType) {
        switch (viewType) {
            case TYPE_HIDDEN:
                return new HiddenAppView(context, reloadCallback);
            case TYPE_FAVOURITE:
                return new FavouriteAppView(context, reloadCallback);
            case TYPE_NORMAL:
            default:
                return new NormalAppView(context, reloadCallback);
        }
    }
}
