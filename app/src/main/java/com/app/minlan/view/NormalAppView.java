package com.app.minlan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import com.app.minlan.R;

@SuppressLint("ViewConstructor")
public class NormalAppView extends AbstractAppView {
    public NormalAppView(Context context, String name, Drawable icon) {
        super(context, name, icon);
    }

    @Override
    protected void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.normal_app_view, this, true);
    }
}
