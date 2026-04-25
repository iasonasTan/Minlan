package com.app.minlan.view;

import static com.app.minlan.MainActivity.SETTINGS_DARK_ICONS;
import static com.app.minlan.MainActivity.SHARED_SETTINGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import com.app.minlan.R;

@SuppressLint("ViewConstructor")
public class FavouriteAppView extends AbstractAppView {
    public FavouriteAppView(Context context, String name, Drawable icon) {
        super(context, name, icon);
        if(context.getSharedPreferences(SHARED_SETTINGS, Context.MODE_PRIVATE).getBoolean(SETTINGS_DARK_ICONS, false)) {
            ImageView imageView = findViewById(R.id.star_view);
            imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.star_dark));
        }
    }

    @Override
    protected void inflateLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.fav_app_view, this, true);
    }
}
