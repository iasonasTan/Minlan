package com.app.minlan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_APPS_PREFS  = "favourite_apps";
    public static final String SHARED_SETTINGS    = "settings";
    public static final String SETTINGS_DARK_ICONS= "dark_icons";
    public static final String SETTINGS_TEXT_COLOR= "text_color";

    private List<ResolveInfo> mApplicationsInfo;
    private PackageManager mPackageManager;
    private TextInputEditText mAppNameInput;
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

        mAppNameInput = findViewById(R.id.app_name_input);
        mAppNameInput.addTextChangedListener(new InputTextChangeListener());
        mAppNameInput.setOnEditorActionListener(new InputActionListener());

        ImageButton button = findViewById(R.id.clear_button);
        button.setOnClickListener(v -> mAppNameInput.setText(""));
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
        Drawable drawableImage = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE).getBoolean(SETTINGS_DARK_ICONS, false) ?
                AppCompatResources.getDrawable(this, R.drawable.clear_dark) :
                AppCompatResources.getDrawable(this, R.drawable.clear);
        button.setImageDrawable(drawableImage);
        addAppsToLayout("", AppStatus.WHICHEVER);
    }

    private void addAppsToLayout(String requestedName, AppStatus status) {
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
            final boolean appIsHidden   = appFilter.getHidden().contains(appPackageName)&&requestedName.equals("@hidden");

            if ((status == appStatus &&
                    compareName.apply(appName) && 
                    !appPackageName.equals(getPackageName()) &&
                    appFilter.check(appPackageName)
                )||
                appIsHidden) {

                Drawable icon = app.loadIcon(mPackageManager);
                AbstractAppView appView = AppViewFactory.createAppView(this, appName, icon, appStatus.isFav(), appIsHidden);
                AppViewListener listener = new AppViewListener(appPackageName, appStatus, requestedName, appIsHidden);
                appView.setOnClickListener(listener);
                appView.setOnLongClickListener(listener);
                mAppViewsLayout.addView(appView);
            }
        }
    }

    private enum AppStatus {
        FAVOURITE,
        NORMAL,
        WHICHEVER,
        HIDDEN;

        public AppStatus opposite() {
            switch(this) {
                case FAVOURITE: return NORMAL;
                case NORMAL:    return FAVOURITE;
                case HIDDEN:    return NORMAL;
            }
            return WHICHEVER;
        }

        public boolean isFav() {
            return this == AppStatus.FAVOURITE;
        }
    }

    private final class InputTextChangeListener implements TextWatcher {
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

    private final class AppViewListener implements View.OnLongClickListener, View.OnClickListener {
        private final String mAppPackageName, mRequestedName;
        private final AppStatus mStatus;
        private final boolean mIsAppHidden;

        private AppViewListener(String appPackageName, AppStatus status, String requestedName, boolean isHidden) {
            this.mAppPackageName = appPackageName;
            this.mStatus         = status;
            this.mRequestedName  = requestedName;
            this.mIsAppHidden    = isHidden;
        }

        @Override
        public boolean onLongClick(View v) {
            // getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE)
            //         .edit()
            //         .putString(mAppPackageName, mStatus.opposite().toString())
            //         .apply();
            AppHider appHider = new AppHider();
            if(!mIsAppHidden)
                appHider.hideApp(MainActivity.this, mAppPackageName);
            else
                appHider.showApp(MainActivity.this, mAppPackageName);

            addAppsToLayout(mRequestedName, AppStatus.WHICHEVER);
            return true;
        }

        @Override
        public void onClick(View v) {
            mAppNameInput.setText("");
            Intent launchIntent = mPackageManager.getLaunchIntentForPackage(mAppPackageName);
            startActivity(launchIntent);
        }
    }

    private final class InputActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                InputMethodManager inputMethodManager = getSystemService(InputMethodManager.class);
                inputMethodManager.hideSoftInputFromWindow(mAppNameInput.getWindowToken(), 0);
            }
            return true;
        }
    }
}