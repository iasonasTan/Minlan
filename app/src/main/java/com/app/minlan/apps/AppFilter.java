package com.app.minlan.apps;

import android.content.Context;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;


public final class AppFilter {
    private final List<String> mHiddenPackages;

    public AppFilter(Context context) {
        final String hiddenPackagesUnsplit = context
                .getSharedPreferences("apps", Context.MODE_PRIVATE)
                .getString("hidden_apps", "");
        mHiddenPackages = Collections.unmodifiableList(
            Arrays.asList(hiddenPackagesUnsplit.split(","))
        );
    }

    public List<String> getHidden() {
        return Collections.unmodifiableList(mHiddenPackages);
    }

    public boolean isHidden(String pkg) {
        return mHiddenPackages.contains(pkg);
    }
}