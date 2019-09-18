package huawei.android.app;

import android.app.ApplicationPackageManager;
import android.common.HwPackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.Log;

public class HwApplicationPackageManager implements HwPackageManager {
    private static HwPackageManager sInstance = null;

    public static HwPackageManager getDefault() {
        if (sInstance == null) {
            sInstance = new HwApplicationPackageManager();
        }
        return sInstance;
    }

    public CharSequence getAppLabelText(PackageManager pm, String packageName, int resid, ApplicationInfo appInfo) {
        if (pm instanceof ApplicationPackageManager) {
            ApplicationPackageManager apm = (ApplicationPackageManager) pm;
            ApplicationPackageManager.ResourceName name = new ApplicationPackageManager.ResourceName("label_" + packageName, resid);
            CharSequence text = apm.getCachedString(name);
            if (text != null) {
                return text;
            }
            if (appInfo == null) {
                try {
                    appInfo = apm.getApplicationInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    return null;
                }
            }
            try {
                CharSequence text2 = apm.getResourcesForApplication(appInfo).getText(resid);
                if (text2 != null) {
                    String[] labels = text2.toString().split(",");
                    int brandId = getBrand();
                    if (labels != null && labels.length > 0) {
                        String str = labels[0];
                        if (labels.length > 1 && brandId < labels.length) {
                            String label = labels[brandId];
                        }
                        apm.putCachedString(name, labels[brandId]);
                        return labels[brandId];
                    }
                }
            } catch (PackageManager.NameNotFoundException e2) {
                Log.w("PackageManager", "Failure retrieving resources for" + appInfo.packageName);
            } catch (RuntimeException e3) {
                Log.w("PackageManager", "Failure retrieving text 0x" + Integer.toHexString(resid) + " in package " + packageName, e3);
            }
        }
        return null;
    }

    public static final int getBrand() {
        return getDeliverInfo(0);
    }

    public static final boolean isIOTVersion() {
        return 1 == getDeliverInfo(4);
    }

    private static final int getDeliverInfo(int index) {
        String[] infos = SystemProperties.get("ro.config.hw_channel_info", "0,0,460,1,0").split(",");
        if (infos.length >= index + 1 && infos[index] != null) {
            try {
                return Integer.parseInt(infos[index]);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    public Drawable getBadgedIconForTrustSpace(PackageManager pm) {
        if (pm == null) {
            return null;
        }
        return pm.getDrawable("androidhwext", 33751239, null);
    }
}
