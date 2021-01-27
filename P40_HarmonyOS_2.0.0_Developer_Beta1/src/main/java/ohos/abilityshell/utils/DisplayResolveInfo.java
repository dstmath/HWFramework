package ohos.abilityshell.utils;

import android.graphics.drawable.Drawable;

public class DisplayResolveInfo {
    private Drawable mDisplayIcon;
    private String mLabel;
    private String mResolveBundleName;

    public DisplayResolveInfo() {
    }

    public DisplayResolveInfo(String str, String str2) {
        this.mResolveBundleName = str;
        this.mLabel = str2;
    }

    public Drawable getDisplayIcon() {
        return this.mDisplayIcon;
    }

    public boolean hasDisplayIcon() {
        return this.mDisplayIcon != null;
    }

    public void setDisplayIcon(Drawable drawable) {
        this.mDisplayIcon = drawable;
    }

    public String getResolveBundleName() {
        return this.mResolveBundleName;
    }

    public void setResolveBundleName(String str) {
        this.mResolveBundleName = str;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String str) {
        this.mLabel = str;
    }
}
