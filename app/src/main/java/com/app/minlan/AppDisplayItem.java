package com.app.minlan;

import android.content.pm.ResolveInfo;

public class AppDisplayItem {
    private final ResolveInfo resolveInfo;
    private final AppStatus status;
    private final boolean isSearchingForHidden;

    public AppDisplayItem(ResolveInfo resolveInfo, AppStatus status, boolean isSearchingForHidden) {
        this.resolveInfo = resolveInfo;
        this.status = status;
        this.isSearchingForHidden = isSearchingForHidden;
    }

    public ResolveInfo getResolveInfo() {
        return resolveInfo;
    }

    public AppStatus getStatus() {
        return status;
    }

    public boolean isSearchingForHidden() {
        return isSearchingForHidden;
    }
}
