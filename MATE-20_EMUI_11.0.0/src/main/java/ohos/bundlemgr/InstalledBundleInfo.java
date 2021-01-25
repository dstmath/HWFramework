package ohos.bundlemgr;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import java.util.ArrayList;
import ohos.appexecfwk.utils.AppConstants;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;

public class InstalledBundleInfo {
    private static final HiLogLabel BMS_ADAPTER_LABEL = new HiLogLabel(3, 218108160, "BundleMgrAdapter");
    private String appId;
    private String bundleName;
    private String[] enabledAbilities = new String[0];
    private boolean isSystemApp = false;
    private String sourceDir;
    private String[] splitNames = new String[0];
    private int uid = -1;
    private int userId = 0;

    public InstalledBundleInfo(PackageInfo packageInfo, String str, int i) {
        if (packageInfo != null) {
            this.bundleName = packageInfo.packageName;
            if (packageInfo.splitNames != null) {
                this.splitNames = (String[]) packageInfo.splitNames.clone();
            }
            if (packageInfo.applicationInfo != null) {
                this.uid = packageInfo.applicationInfo.uid;
                this.sourceDir = getBundleSourceDir(packageInfo.applicationInfo.sourceDir);
                this.isSystemApp = packageInfo.applicationInfo.isSystemApp();
            }
        }
        this.userId = i;
        this.appId = str;
        getEnabledAbilities(packageInfo);
    }

    private String getBundleSourceDir(String str) {
        int lastIndexOf = str.lastIndexOf(47);
        return lastIndexOf != -1 ? str.substring(0, lastIndexOf) : str;
    }

    private void getEnabledAbilities(PackageInfo packageInfo) {
        ArrayList arrayList = new ArrayList();
        if (packageInfo != null) {
            if (packageInfo.activities != null) {
                ActivityInfo[] activityInfoArr = packageInfo.activities;
                for (ActivityInfo activityInfo : activityInfoArr) {
                    int lastIndexOf = activityInfo.name.lastIndexOf(AppConstants.SHELL_ACTIVITY_SUFFIX);
                    if (lastIndexOf != -1) {
                        arrayList.add(activityInfo.name.substring(0, lastIndexOf));
                    }
                }
            }
            if (packageInfo.services != null) {
                ServiceInfo[] serviceInfoArr = packageInfo.services;
                for (ServiceInfo serviceInfo : serviceInfoArr) {
                    int lastIndexOf2 = serviceInfo.name.lastIndexOf(AppConstants.SHELL_SERVICE_SUFFIX);
                    if (lastIndexOf2 != -1) {
                        arrayList.add(serviceInfo.name.substring(0, lastIndexOf2));
                    }
                }
            }
            if (packageInfo.providers != null) {
                ProviderInfo[] providerInfoArr = packageInfo.providers;
                for (ProviderInfo providerInfo : providerInfoArr) {
                    int lastIndexOf3 = providerInfo.name.lastIndexOf(AppConstants.SHELL_PROVIDER_SUFFIX);
                    if (lastIndexOf3 != -1) {
                        arrayList.add(providerInfo.name.substring(0, lastIndexOf3));
                    }
                }
            }
            int size = arrayList.size();
            if (size > 0) {
                String[] strArr = new String[size];
                arrayList.toArray(strArr);
                this.enabledAbilities = (String[]) strArr.clone();
            }
            AppLog.d(BMS_ADAPTER_LABEL, "enabledAbilities length : %{public}d", Integer.valueOf(size));
        }
    }
}
