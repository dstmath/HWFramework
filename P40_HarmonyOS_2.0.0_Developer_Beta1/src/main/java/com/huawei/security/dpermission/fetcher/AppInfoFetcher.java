package com.huawei.security.dpermission.fetcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.huawei.android.content.pm.PackageManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.utils.DataValidUtil;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AppInfoFetcher {
    public static final int APP_ATTRIBUTE_PRESET = 1;
    public static final int APP_ATTRIBUTE_PRIVILEGED = 2;
    public static final int APP_ATTRIBUTE_SIGNED_WITH_PLATFORM_KEY = 4;
    private static final int INVALID_APP_ATTRIBUTE = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "AppInfoFetcher");
    private Context mContext;

    private AppInfoFetcher() {
    }

    public static AppInfoFetcher getInstance() {
        return AppInfoFetcherHolder.INSTANCE;
    }

    public void init(Context context) {
        if (context == null) {
            HiLog.warn(LABEL, "init context is null", new Object[0]);
        } else {
            this.mContext = context.getApplicationContext();
        }
    }

    public int getAppAttribute(int i) {
        if (!DataValidUtil.isUidValid(i)) {
            HiLog.error(LABEL, "getAppAttribute: invalid uid: %{public}d", new Object[]{Integer.valueOf(i)});
            return 0;
        }
        Optional<PackageManager> packageManager = getPackageManager();
        if (!packageManager.isPresent()) {
            HiLog.error(LABEL, "getAppAttribute: cannot get PackageManager.", new Object[0]);
            return 0;
        }
        String[] packagesForUid = packageManager.get().getPackagesForUid(i);
        if (isPackageNamesEmpty(packagesForUid)) {
            HiLog.error(LABEL, "getAppAttribute: packageNames is empty!", new Object[0]);
            return 0;
        }
        int userId = UserHandleEx.getUserId(i);
        int i2 = 0;
        for (String str : packagesForUid) {
            try {
                ApplicationInfo applicationInfoAsUser = PackageManagerEx.getApplicationInfoAsUser(packageManager.get(), str, 128, userId);
                if ((applicationInfoAsUser.flags & 1) != 0) {
                    i2 |= 1;
                }
                if ((applicationInfoAsUser.privateFlags & 8) != 0) {
                    i2 |= 2;
                }
                if ((applicationInfoAsUser.privateFlags & 1048576) != 0) {
                    i2 |= 4;
                }
            } catch (PackageManager.NameNotFoundException unused) {
                HiLog.error(LABEL, "getAppAttribute: %{public}s, name not found", new Object[]{str});
            }
        }
        return i2;
    }

    private Optional<PackageManager> getPackageManager() {
        Context context = this.mContext;
        if (context != null) {
            return Optional.ofNullable(context.getPackageManager());
        }
        HiLog.error(LABEL, "getPackageManager: context is null.", new Object[0]);
        return Optional.empty();
    }

    private boolean isPackageNamesEmpty(String[] strArr) {
        return strArr == null || strArr.length == 0;
    }

    private static final class AppInfoFetcherHolder {
        private static final AppInfoFetcher INSTANCE = new AppInfoFetcher();

        private AppInfoFetcherHolder() {
        }
    }
}
