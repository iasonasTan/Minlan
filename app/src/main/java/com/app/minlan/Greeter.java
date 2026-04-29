package com.app.minlan;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;

public final class Greeter {
    public void showHints(Context context) {
        boolean shown = context.getSharedPreferences("greeterPrefs", Context.MODE_PRIVATE)
                .getBoolean("hintsShown", false);
        if(!shown) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.dyk)
                    .setMessage(context.getString(R.string.dyk_settings))
                    .setNeutralButton(R.string.ok, new OkListener(context))
                    .setCancelable(false)
                    .show();
        }
    }

    private static final class OkListener implements DialogInterface.OnClickListener {
        private final Context context;

        public OkListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(DialogInterface dialog,int which) {
            context.getSharedPreferences("greeterPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("hintsShown", true)
                .apply();
            dialog.dismiss();
        }
    }
}