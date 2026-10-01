package com.app.minlan.settings;

import static com.app.minlan.MainActivity.SETTINGS_DARK_ICONS;
import static com.app.minlan.MainActivity.SETTINGS_ICONS_VISIBLE;
import static com.app.minlan.MainActivity.SETTINGS_SHOW_CLOCK;
import static com.app.minlan.MainActivity.SETTINGS_TEXT_COLOR;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.minlan.Greeter;
import com.app.minlan.R;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        setListeners();
        loadSettings();
    }

    private void loadSettings() {
        CheckBox darkIconsCB = findViewById(R.id.dark_icons_cb);
        darkIconsCB.setChecked(getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(SETTINGS_DARK_ICONS, false));
        CheckBox clockCB = findViewById(R.id.show_clock_cb);
        clockCB.setChecked(getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(SETTINGS_SHOW_CLOCK, false));
        CheckBox iconsVisibleCB = findViewById(R.id.show_icons_cb);
        iconsVisibleCB.setChecked(getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(SETTINGS_ICONS_VISIBLE, true));
    }

    private void setListeners() {
        CheckBox iconsVisibleCB = findViewById(R.id.show_icons_cb);
        iconsVisibleCB.setOnCheckedChangeListener(new CheckBoxStateSaveListener(this, SETTINGS_ICONS_VISIBLE));

        CheckBox darkIconsCB = findViewById(R.id.dark_icons_cb);
        darkIconsCB.setOnCheckedChangeListener(new CheckBoxStateSaveListener(this, SETTINGS_DARK_ICONS));

        final var listener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(SETTINGS_TEXT_COLOR, color)
                        .apply();
            }
            @Override public void onCancel(AmbilWarnaDialog dialog) {
            }
        };
        final int defaultColor = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE).getInt(SETTINGS_TEXT_COLOR, Color.WHITE);
        findViewById(R.id.select_color_b).setOnClickListener(v -> new AmbilWarnaDialog(this, defaultColor, false, listener).show());

        findViewById(R.id.show_hints).setOnClickListener(v -> new Greeter(this).forceShow());

        CheckBox clockCB = findViewById(R.id.show_clock_cb);
        clockCB.setOnCheckedChangeListener(new CheckBoxStateSaveListener(this, SETTINGS_SHOW_CLOCK));
    }

    private static final class CheckBoxStateSaveListener implements CompoundButton.OnCheckedChangeListener {
        private final Activity mActivity;
        private final String mPrefs;

        private CheckBoxStateSaveListener(Activity activity, String prefs) {
            this.mActivity = activity;
            this.mPrefs = prefs;
        }

        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            mActivity.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(mPrefs, isChecked)
                    .apply();
        }
    }
}
