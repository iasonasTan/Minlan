package com.app.minlan.view;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.app.minlan.ReloadCallback;

public final class AppViewFactory {

    private AppViewFactory(){
    }

    public static AbstractAppView createAppView(Context context, ResolveInfo resoleInfo, ReloadCallback reloadCallback, boolean fav, boolean hid) {
        if(hid) {
            return new HiddenAppView(context, resoleInfo, reloadCallback);
        }
        if(fav) {
            return new FavouriteAppView(context, resoleInfo, reloadCallback);
        } else {
            return new NormalAppView(context, resoleInfo, reloadCallback);
        }
    }
}
