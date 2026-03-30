package com.app.minlan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_APPS_PREFS = "favourite_apps";

    private List<ResolveInfo> mApplicationsInfo;
    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        mPackageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApplicationsInfo = Collections.unmodifiableList(mPackageManager.queryIntentActivities(intent, 0));

        TextInputEditText appNameInput = findViewById(R.id.app_name_input);
        ViewGroup appsLayout = findViewById(R.id.app_container);
        addAppsToLayout(appsLayout, "");

        appNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String requestedName = s.toString().toLowerCase().replace(" ", "");
                addAppsToLayout(appsLayout, requestedName);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void addAppsToLayout(ViewGroup appsLayout,String requestedName){
        appsLayout.removeAllViews();
        SharedPreferences preferences = getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE);
        for(ResolveInfo app: mApplicationsInfo) {
            final String appName = app.loadLabel(mPackageManager).toString();
            final String packageName = app.activityInfo.packageName;
            final boolean favourite = preferences.getBoolean(packageName, false);
            if(favourite&&requestedName.isEmpty() ||appName.toLowerCase().replace(" ", "").contains(requestedName)) {
                AbstractAppView normalAppView;
                if(favourite) {
                    normalAppView = new FavouriteAppView(this, appName, app.loadIcon(mPackageManager));
                } else {
                    normalAppView = new NormalAppView(this, appName, app.loadIcon(mPackageManager));
                }
                var layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                normalAppView.setOnClickListener(v -> {
                    Intent launchIntent = mPackageManager.getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                });
                normalAppView.setOnLongClickListener(v -> {
                    getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(packageName, !favourite)
                            .apply();
                    addAppsToLayout(appsLayout, requestedName);
                    return true;
                });
                normalAppView.setLayoutParams(layoutParams);
                int index = favourite ? 0 : appsLayout.getChildCount();
                appsLayout.addView(normalAppView, index);
            }
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