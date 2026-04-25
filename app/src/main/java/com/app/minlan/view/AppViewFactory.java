package com.app.minlan.view;

import android.content.Context;
import android.graphics.drawable.Drawable;

public final class AppViewFactory {

    private AppViewFactory(){
    }

    public static AbstractAppView createAppView(Context context, String name, Drawable icon, boolean fav) {
        if(fav) {
            return new FavouriteAppView(context, name, icon);
        } else {
            return new NormalAppView(context, name, icon);
        }
    }
}
