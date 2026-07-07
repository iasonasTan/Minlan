package com.app.minlan.view;

import static com.app.minlan.MainActivity.SETTINGS_TEXT_COLOR;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.net.Uri;
import android.provider.Settings;

import androidx.core.content.res.ResourcesCompat;

import com.app.minlan.R;
import com.app.minlan.ReloadCallback;

@SuppressLint("ViewConstructor")
public abstract class AbstractAppView extends LinearLayout {
    protected final TextView mNameView;
    protected final ImageView mIconView;

    protected ResolveInfo resolveInfo;
    protected final ReloadCallback reloadCallback;

    public AbstractAppView(Context context, ReloadCallback reloadCallback) {
        super(context);
        this.reloadCallback = reloadCallback;

        inflateLayout(context);

        mIconView = findViewById(R.id.icon_view);
        mNameView = findViewById(R.id.name_view);

        AppViewListener avl = new AppViewListener();
        setOnLongClickListener(avl);
        setOnClickListener(avl);
    }

    public void bind(ResolveInfo resolveInfo) {
        this.resolveInfo = resolveInfo;
        PackageManager packageManager = getContext().getPackageManager();

        mIconView.setImageDrawable(resolveInfo.loadIcon(packageManager));
        mNameView.setTextColor(
                getContext().getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                        .getInt(SETTINGS_TEXT_COLOR, Color.WHITE)
        );
        mNameView.setText(resolveInfo.loadLabel(packageManager));
    }

    protected abstract int getMenuLayoutId();
    protected abstract void inflateLayout(Context context);

    protected void addListenersToPopup(View popupView, PopupHider popupHiderCallback) {
        popupView.findViewById(R.id.info).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + resolveInfo.activityInfo.packageName));
            getContext().startActivity(intent);
            popupHiderCallback.hidePopup();
        });
    }

    @Override
    public final void setOnClickListener(View.OnClickListener listener) {
        super.setOnClickListener(listener);
        mNameView.setOnClickListener(listener);
        mIconView.setOnClickListener(listener);
    }

    @Override
    public final void setOnLongClickListener(View.OnLongClickListener listener) {
        super.setOnLongClickListener(listener);
        mNameView.setOnLongClickListener(listener);
        mIconView.setOnLongClickListener(listener);
    }

    private final class AppViewListener implements View.OnLongClickListener, View.OnClickListener {
        @Override
        public boolean onLongClick(View v) {
            View popupView = LayoutInflater.from(getContext()).inflate(getMenuLayoutId(), null);
            PopupWindow popup = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            addListenersToPopup(popupView, popup::dismiss);
            popup.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.app_menu_backgound, getContext().getTheme()));
            popup.showAsDropDown(mNameView, 0, 0);
            return true;
        }

        @Override
        public void onClick(View v) {
            Intent launchIntent = getContext()
                    .getPackageManager()
                    .getLaunchIntentForPackage(resolveInfo.activityInfo.packageName);
            getContext().startActivity(launchIntent);
        }
    }
}
