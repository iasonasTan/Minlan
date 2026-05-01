package com.app.minlan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.app.minlan.settings.SettingsActivity;
import com.app.minlan.view.AbstractAppView;
import com.app.minlan.view.AppViewFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.app.minlan.apps.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity implements ReloadCallback {
    public static final String SHARED_APPS_PREFS  = "favourite_apps";
    public static final String SHARED_SETTINGS    = "settings";
    public static final String SETTINGS_DARK_ICONS= "dark_icons";
    public static final String SETTINGS_TEXT_COLOR= "text_color";

    private List<ResolveInfo> mApplicationsInfo;
    private PackageManager mPackageManager;
    private TextInputEditText mInput;
    private ViewGroup mAppViewsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        mPackageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(intent, 0);

        apps.sort((a, b) ->
                a.loadLabel(mPackageManager).toString()
                        .compareToIgnoreCase(b.loadLabel(mPackageManager).toString()));

        mApplicationsInfo = Collections.unmodifiableList(apps);

        mAppViewsLayout = findViewById(R.id.app_container);
        addAppsToLayout("", AppStatus.WHICHEVER);

        mInput = findViewById(R.id.app_name_input);

        InputListener il = new InputListener();
        mInput.addTextChangedListener(il);
        mInput.setOnEditorActionListener(il);

        ImageButton button = findViewById(R.id.clear_button);
        button.setOnClickListener(v -> mInput.setText(""));
        button.setOnLongClickListener(v -> {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        });

        new Greeter().showHints(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageButton button = findViewById(R.id.clear_button);
        final int resourceId = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE).getBoolean(SETTINGS_DARK_ICONS, false) ?
                R.drawable.clear_dark : R.drawable.clear;
        Drawable drawableImage = AppCompatResources.getDrawable(this, resourceId);
        button.setImageDrawable(drawableImage);
        addAppsToLayout("", AppStatus.WHICHEVER);
    }

    @Override
    public void reload() {
        CharSequence charSeq = mInput.getText();
        if(charSeq != null) {
            String requestedName = charSeq.toString().toLowerCase().replace(" ", "");
            AppStatus appStatus = requestedName.equals("@hidden") ?
                    AppStatus.HIDDEN :
                    AppStatus.WHICHEVER;
            addAppsToLayout(requestedName, appStatus);
        }
    }

    private void addAppsToLayout(String requestedName, AppStatus status) {
        Log.d("app_manager", "Adding apps to layout, RequestedName: "+requestedName+", AppStatus: "+status);
        Function<String, Boolean> compareName = name -> name.toLowerCase().replace(" ", "").contains(
                requestedName.toLowerCase().replace(" ", "")
        );

        if(status == AppStatus.HIDDEN) {
            mAppViewsLayout.removeAllViews();
            addAppsToLayout(requestedName, AppStatus.NORMAL);
            return;
        }

        if(status == AppStatus.WHICHEVER) {
            mAppViewsLayout.removeAllViews();
            addAppsToLayout(requestedName, AppStatus.FAVOURITE);
            addAppsToLayout(requestedName, AppStatus.NORMAL);
            return;
        }

        AppFilter appFilter = new AppFilter(this);
        SharedPreferences preferences = getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE);
        for (ResolveInfo app : mApplicationsInfo) {
            final String appName        = app.loadLabel(mPackageManager).toString();
            final String appPackageName = app.activityInfo.packageName;
            final AppStatus appStatus   = Enum.valueOf(AppStatus.class, preferences.getString(appPackageName, "NORMAL"));

            final boolean searchingForHidden = appFilter.isHidden(appPackageName) && requestedName.equals("@hidden");
            final boolean appMatched         = status == appStatus && compareName.apply(appName) &&
                    !appPackageName.equals(getPackageName()) && !appFilter.isHidden(appPackageName);

            if (appMatched||searchingForHidden) {
                AbstractAppView appView = AppViewFactory.createAppView(this, app, this, appStatus.isFav(), searchingForHidden);
                mAppViewsLayout.addView(appView);
            }
        }
    }

    private final class InputListener implements TextView.OnEditorActionListener, TextWatcher {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager inputMethodManager = getSystemService(InputMethodManager.class);
                inputMethodManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
            }
            return true;
        }

        @Override
        public void afterTextChanged(Editable s) {
            String requestedName = s.toString().toLowerCase().replace(" ", "");
            AppStatus appStatus = requestedName.equals("@hidden") ?
                    AppStatus.HIDDEN :
                    AppStatus.WHICHEVER;
            addAppsToLayout(requestedName, appStatus);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}