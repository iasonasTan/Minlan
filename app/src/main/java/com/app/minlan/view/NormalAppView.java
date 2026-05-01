package com.app.minlan.view;

import static com.app.minlan.MainActivity.SHARED_APPS_PREFS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;

import com.app.minlan.AppStatus;
import com.app.minlan.R;
import com.app.minlan.ReloadCallback;
import com.app.minlan.apps.AppHider;

@SuppressLint("ViewConstructor")
public class NormalAppView extends AbstractAppView {
    public NormalAppView(Context context, ResolveInfo resoleInfo, ReloadCallback reloadCallback) {
        super(context, resoleInfo, reloadCallback);
    }

    @Override
    protected void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.normal_app_view, this, true);
    }

    @Override
    protected int getMenuLayoutId() {
        return R.layout.normal_app_menu;
    }

    @Override
    protected void addListenersToPopup(View popupView, PopupHider hideCallback) {
        popupView.findViewById(R.id.mark_favourite).setOnClickListener(v -> {
            getContext().getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(resolveInfo.activityInfo.packageName, AppStatus.FAVOURITE.toString())
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
