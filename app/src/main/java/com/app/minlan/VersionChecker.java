package com.app.minlan;

import android.app.Activity;

import com.lib.version.checker.AbstractVersionChecker;

public class VersionChecker extends AbstractVersionChecker {
    public VersionChecker(Activity activity) {
        super(activity);
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
