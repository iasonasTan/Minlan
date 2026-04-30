package com.app.minlan.apps;

import android.content.Context;
import java.util.*;

public final class AppHider {
    public void hideApp(Context context, String pkg) {
        List<String> hidden = getPackages(context);
        hidden.add(pkg);
        setHiddenApps(context, hidden);
    }

    public void showApp(Context context, String pkg) {
        List<String> hidden = getPackages(context);
        hidden.remove(pkg);
        setHiddenApps(context, hidden);
    }

    private List<String> getPackages(Context context) {
        String hiddenAppsStrRaw = context
                .getSharedPreferences("apps", Context.MODE_PRIVATE)
                .getString("hidden_apps", "");
        return new ArrayList<>(
            Arrays.asList(hiddenAppsStrRaw.split(","))
        );
    }

    private void setHiddenApps(Context context, List<String> pkgs) {
        final StringBuilder pkgsBuilder = new StringBuilder();
        for(String pkg: pkgs) {
            pkgsBuilder
                .append(",")
                .append(pkg);
        }
        context.getSharedPreferences("apps", Context.MODE_PRIVATE)
                .edit()
                .putString("hidden_apps", pkgsBuilder.toString())
                .apply();
    }
}