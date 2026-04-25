package com.app.minlan.view;

import static com.app.minlan.MainActivity.SETTINGS_TEXT_COLOR;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.minlan.R;

@SuppressLint("ViewConstructor")
public abstract class AbstractAppView extends LinearLayout {
    private final TextView mNameView;
    private final ImageView mIconView;

    public AbstractAppView(Context context, String name, Drawable icon) {
        super(context);

        inflateLayout(context);

        mIconView = findViewById(R.id.icon_view);
        mIconView.setImageDrawable(icon);

        mNameView = findViewById(R.id.name_view);
        mNameView.setTextColor(
                context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE)
                        .getInt(SETTINGS_TEXT_COLOR, Color.WHITE)
        );
        mNameView.setText(name);
    }

    protected abstract void inflateLayout(Context context);

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        super.setOnClickListener(listener);
        mNameView.setOnClickListener(listener);
        mIconView.setOnClickListener(listener);
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener listener) {
        super.setOnLongClickListener(listener);
        mNameView.setOnLongClickListener(listener);
        mIconView.setOnLongClickListener(listener);
    }
}
