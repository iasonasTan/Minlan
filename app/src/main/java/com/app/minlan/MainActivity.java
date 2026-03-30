package com.app.minlan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;

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
        mApplicationsInfo = Collections.unmodifiableList(mPackageManager.queryIntentActivities(intent, 0));

        mAppViewsLayout = findViewById(R.id.app_container);
        addAppsToLayout(mAppViewsLayout, "");

        mAppNameInput = findViewById(R.id.app_name_input);
        mAppNameInput.addTextChangedListener(new InputTextChangeListener());
        mAppNameInput.setOnEditorActionListener(new InputActionListener());

        ImageButton button = findViewById(R.id.clear_button);
        button.setOnClickListener(v -> mAppNameInput.setText(""));
    }

    private void addAppsToLayout(ViewGroup appsLayout,String requestedName){
        appsLayout.removeAllViews();
        SharedPreferences preferences = getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE);
        for(ResolveInfo app: mApplicationsInfo) {
            final String appName = app.loadLabel(mPackageManager).toString();
            final String appPackageName = app.activityInfo.packageName;
            final boolean isFavourite = preferences.getBoolean(appPackageName, false);
            if(isFavourite&&requestedName.isEmpty() ||appName.toLowerCase().replace(" ", "").contains(requestedName)) {
                AbstractAppView normalAppView;
                if(isFavourite) {
                    normalAppView = new FavouriteAppView(this, appName, app.loadIcon(mPackageManager));
                } else {
                    normalAppView = new NormalAppView(this, appName, app.loadIcon(mPackageManager));
                }
                normalAppView.setOnClickListener(new AppViewClickListener(appPackageName));
                normalAppView.setOnLongClickListener(new AppViewLongClickListener(appPackageName, isFavourite, requestedName));
                final int index = isFavourite ? 0 : appsLayout.getChildCount();
                appsLayout.addView(normalAppView, index);
            }
        }
    }

    private final class InputTextChangeListener implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            String requestedName = s.toString().toLowerCase().replace(" ", "");
            addAppsToLayout(mAppViewsLayout, requestedName);
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
            addAppsToLayout(mAppViewsLayout, mRequestedName);
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

//    private Drawable tryAdaptive(Drawable raw) {
//        if (raw instanceof AdaptiveIconDrawable) {
//            Drawable monochrome = raw.getCurrent();
//            int color = MaterialColors.getColor(
//                    this,
//                    com.google.android.material.R.attr.colorPrimaryFixed,
//                    Color.WHITE
//            );
//            //monochrome.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//            monochrome.setTint(color);
//            return monochrome;
//        }
//        return raw;
//    }
}