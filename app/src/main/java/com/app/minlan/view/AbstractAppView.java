package com.app.minlan.view;

import static com.app.minlan.MainActivity.SETTINGS_TEXT_COLOR;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.app.minlan.R;
import com.app.minlan.ReloadCallback;

import java.util.Optional;

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
        String packageName = resolveInfo.activityInfo.packageName;
        PackageManager packageManager = getContext().getPackageManager();

        String drawableKey;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            drawableKey = packageName + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            drawableKey = packageName;
        }

        // Check cache
        Optional<Drawable> iconOptional = IconCache.instance.getDrawable(drawableKey);
        if(iconOptional.isPresent()) {
            // If it's in cache, get it from there.
            mIconView.setImageDrawable(iconOptional.get());
        } else {
            // If it's not in cache, create it and put it in cache.
            Drawable drawable = resolveInfo.loadIcon(packageManager);
            mIconView.setImageDrawable(drawable);
            IconCache.instance.putDrawable(packageName, drawable);
        }

        int color = getContext()
                .getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                .getInt(SETTINGS_TEXT_COLOR, Color.WHITE);
        mNameView.setTextColor(color);
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
            popup.setBackgroundDrawable(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.app_menu_backgound, getContext().getTheme())
            );
            final View anchor = mIconView; // On which view the PopupWindow will be based.
            popupView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED); // Force popupView to calculate its bounds.

            int windowOffsetY = getWindowOffsetY(anchor, popupView);
            popup.showAsDropDown(anchor, 0, windowOffsetY);
            return true;
        }

        private int getWindowOffsetY(View anchor, View popupView) {
            // Get anchor location (popupView doesn't have location yet so we use anchor's location instead)
            final int[] anchorLocation = new int[2];
            anchor.getLocationOnScreen(anchorLocation);

            int popupWindowBottom = (anchorLocation[1] +anchor.getHeight()) +popupView.getMeasuredHeight(); // Calculate where the bottom of the popupView be
            int rootHeight = getRootView().getHeight(); // Get height of screen (root view of app has the same height as the screen since launcher are always full screen)
            return Math.min(0, rootHeight -popupWindowBottom -100/*extra margin*/); // Calculate difference between bottom of window and bottom of screen and if it's negative, return it, return 0 otherwise.
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
