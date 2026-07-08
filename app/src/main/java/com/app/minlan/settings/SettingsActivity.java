package com.app.minlan.settings;

import static com.app.minlan.MainActivity.SETTINGS_DARK_ICONS;
import static com.app.minlan.MainActivity.SETTINGS_SHOW_CLOCK;
import static com.app.minlan.MainActivity.SETTINGS_TEXT_COLOR;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
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
    }

    private void setListeners() {
        CheckBox darkIconsCB = findViewById(R.id.dark_icons_cb);
        darkIconsCB.setOnCheckedChangeListener((a, b) -> {
            getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(SETTINGS_DARK_ICONS, darkIconsCB.isChecked())
                    .apply();
        });

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

        findViewById(R.id.show_hints).setOnClickListener(v -> {
            new Greeter(this).forceShow();
        });

        CheckBox clockCB = findViewById(R.id.show_clock_cb);
        clockCB.setOnCheckedChangeListener((a, b) -> {
            getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(SETTINGS_SHOW_CLOCK, clockCB.isChecked())
                    .apply();
        });
    }
}
