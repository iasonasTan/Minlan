package com.app.minlan.settings;

import static com.app.minlan.MainActivity.SETTINGS_DARK_ICONS;
import static com.app.minlan.MainActivity.SETTINGS_TEXT_COLOR;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
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

        CheckBox checkBox = findViewById(R.id.dark_icons_cb);
        checkBox.setChecked(
                getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                        .getBoolean(SETTINGS_DARK_ICONS, false)
        );
        checkBox.setOnCheckedChangeListener((a, b) -> {
            getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(SETTINGS_DARK_ICONS, checkBox.isChecked())
                    .apply();
        });

        Button selectColorBtn = findViewById(R.id.select_color_b);
        final var listener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(SETTINGS_TEXT_COLOR, color)
                        .apply();
            }
            @Override public void onCancel(AmbilWarnaDialog dialog) {}
        };
        final int defaultColor = getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                                        .getInt(SETTINGS_TEXT_COLOR, Color.WHITE);
        selectColorBtn.setOnClickListener(v -> 
                new AmbilWarnaDialog(this, defaultColor, false, listener).show()
        );

        findViewById(R.id.show_hints).setOnClickListener(v -> {
            new Greeter(this).forceShow();
        });
    }
}
