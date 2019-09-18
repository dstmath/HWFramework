package android.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public interface HwPackageManager {
    CharSequence getAppLabelText(PackageManager packageManager, String str, int i, ApplicationInfo applicationInfo);

    Drawable getBadgedIconForTrustSpace(PackageManager packageManager);
}
