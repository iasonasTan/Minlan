package com.app.minlan.view;

import static com.app.minlan.MainActivity.SETTINGS_DARK_ICONS;
import static com.app.minlan.MainActivity.SHARED_APPS_PREFS;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import com.app.minlan.AppStatus;
import com.app.minlan.R;
import com.app.minlan.ReloadCallback;
import com.app.minlan.apps.AppHider;

@SuppressLint("ViewConstructor")
public class FavouriteAppView extends AbstractAppView {
    public FavouriteAppView(Context context, ResolveInfo resoleInfo, ReloadCallback reloadCallback) {
        super(context, resoleInfo, reloadCallback);
        if(context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE).getBoolean(SETTINGS_DARK_ICONS, false)) {
            ImageView imageView = findViewById(R.id.star_view);
            imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.star_dark));
        }
    }

    @Override
    protected int getMenuLayoutId() {
        return R.layout.fav_app_menu;
    }

    @Override
    protected void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.fav_app_view, this, true);
    }

    @Override
    protected void addListenersToPopup(View popupView, PopupHider hideCallback) {
        popupView.findViewById(R.id.mark_not_favourite).setOnClickListener(v -> {
            getContext().getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(resolveInfo.activityInfo.packageName, AppStatus.NORMAL.toString())
                    .apply();
            hideCallback.hidePopup();
            reloadCallback.reload();
        });
        popupView.findViewById(R.id.hide).setOnClickListener(v -> {
            new AppHider().hideApp(getContext(), resolveInfo.activityInfo.packageName);
            hideCallback.hidePopup();
            reloadCallback.reload();
        });
    }
}
