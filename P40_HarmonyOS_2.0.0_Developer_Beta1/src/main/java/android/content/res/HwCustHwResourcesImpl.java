package android.content.res;

import android.os.SystemProperties;

public class HwCustHwResourcesImpl extends HwCustHwResources {
    private boolean mIsUseThemeIconAndBackground = SystemProperties.getBoolean("ro.config.hw_theme_icon_bg_use", true);

    public boolean isUseThemeIconAndBackground() {
        return this.mIsUseThemeIconAndBackground;
    }
}
