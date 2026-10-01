package com.app.minlan;

import android.app.Activity;
import android.util.Log;

import com.lib.version.checker.AbstractVersionChecker;

public class VersionChecker extends AbstractVersionChecker {
    public VersionChecker(Activity activity) {
        super(activity);
        Log.d("vch", "Checking for updates...");
    }

    @Override
    protected String NewVersionWebpageUrl() {
        return "https://github.com/iasonasTan/Minlan/releases";
    }

    @Override
    protected String latestVersionFileWebUrl() {
        return "https://raw.githubusercontent.com/iasonasTan/Minlan/master/latest_version.txt";
    }
}
