package com.app.minlan;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String SHARED_APPS_PREFS = "favourite_apps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

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
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        for(ResolveInfo app: apps) {
            final String appName = app.loadLabel(pm).toString();
            final String packageName = app.activityInfo.packageName;
            final boolean favourite = getSharedPreferences(SHARED_APPS_PREFS, Context.MODE_PRIVATE)
                    .getBoolean(packageName, false);
            if(favourite||appName.toLowerCase().replace(" ", "").contains(requestedName)) {
                AbstractAppView normalAppView;
                if(favourite) {
                    normalAppView = new FavouriteAppView(this, appName, app.loadIcon(pm));
                } else {
                    normalAppView = new NormalAppView(this, appName, app.loadIcon(pm));
                }
                var layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                normalAppView.setOnClickListener(v -> {
                    Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
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