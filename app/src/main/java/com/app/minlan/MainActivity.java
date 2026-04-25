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

import com.app.minlan.view.AbstractAppView;
import com.app.minlan.view.AppViewFactory;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_APPS_PREFS = "favourite_apps";

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
        addAppsToLayout(mAppViewsLayout, "", true, true);
        addAppsToLayout(mAppViewsLayout, "", false,false);

        mAppNameInput = findViewById(R.id.app_name_input);
        mAppNameInput.addTextChangedListener(new InputTextChangeListener());
        mAppNameInput.setOnEditorActionListener(new InputActionListener());

        ImageButton button = findViewById(R.id.clear_button);
        button.setOnClickListener(v -> mAppNameInput.setText(""));
    }

    private void addAppsToLayout(ViewGroup appsLayout,String requestedName,boolean favourite,boolean clear){
        Function<String, Boolean> compareName = name ->
                name.toLowerCase().replace(" ", "").contains(requestedName.toLowerCase().replace(" ", ""));
        if(clear) appsLayout.removeAllViews();
        SharedPreferences preferences = getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE);
        for(ResolveInfo app: mApplicationsInfo) {
            final String appName = app.loadLabel(mPackageManager).toString();
            final String appPackageName = app.activityInfo.packageName;
            final boolean isFavourite = preferences.getBoolean(appPackageName, false);
            if(isFavourite==favourite&&compareName.apply(appName)||isFavourite==favourite&&requestedName.isEmpty()) {
                Drawable icon = app.loadIcon(mPackageManager);
                AbstractAppView appView = AppViewFactory.createAppView(this, appName, icon, isFavourite);
                appView.setOnClickListener(new AppViewClickListener(appPackageName));
                appView.setOnLongClickListener(new AppViewLongClickListener(appPackageName, isFavourite, requestedName));
                appsLayout.addView(appView);
            }
        }
    }

    private final class InputTextChangeListener implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            String requestedName = s.toString().toLowerCase().replace(" ", "");
            addAppsToLayout(mAppViewsLayout, requestedName, true, true);
            addAppsToLayout(mAppViewsLayout, requestedName, false,false);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private final class AppViewClickListener implements View.OnClickListener {
        private final String mAppPackageName;

        private AppViewClickListener(String appPackageName) {
            this.mAppPackageName = appPackageName;
        }

        @Override
        public void onClick(View v) {
            mAppNameInput.setText("");
            Intent launchIntent = mPackageManager.getLaunchIntentForPackage(mAppPackageName);
            startActivity(launchIntent);
        }
    }

    private final class AppViewLongClickListener implements View.OnLongClickListener {
        private final String mAppPackageName;
        private final boolean mIsFavourite;
        private final String mRequestedName;

        private AppViewLongClickListener(String appPackageName, boolean isFavourite, String requestedName) {
            this.mAppPackageName = appPackageName;
            this.mIsFavourite = isFavourite;
            this.mRequestedName = requestedName;
        }

        @Override
        public boolean onLongClick(View v) {
            getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(mAppPackageName, !mIsFavourite)
                    .apply();
            addAppsToLayout(mAppViewsLayout, mRequestedName, true, true);
            addAppsToLayout(mAppViewsLayout, mRequestedName, false,false);
            return true;
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