package com.app.minlan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
