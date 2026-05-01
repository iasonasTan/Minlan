package com.app.minlan;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;

public final class Greeter {
    private final Context context;

    public Greeter(Context context) {
        this.context = context;
    }

    public void showHints() {
        boolean shown = context.getSharedPreferences("greeterPrefs", Context.MODE_PRIVATE)
                .getBoolean("hintsShown", false);
        if(!shown) { showDialog1(); }
    }

    private void showDialog1() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dyk)
                .setMessage(context.getString(R.string.dyk_settings))
                .setNeutralButton(R.string.next, (a,b) -> showDialog2())
                .setCancelable(false)
                .show();
    }

    private void showDialog2() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dyk)
                .setMessage(context.getString(R.string.dyk_hidden_apps))
                .setNeutralButton(R.string.next, (a,b)->showDialog3())
                .setCancelable(false)
                .show();
    }

    private void showDialog3() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dyk)
                .setMessage(context.getString(R.string.dyk_app_menu))
                .setNeutralButton(R.string.ok, (a,b)->markShown())
                .setCancelable(false)
                .show();
    }

    private void markShown() {
        context.getSharedPreferences("greeterPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("hintsShown", true)
                .apply();
    }
}