package com.android.server.timezone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.util.Slog;
import java.util.List;

final class PackageTrackerHelperImpl implements ConfigHelper, PackageManagerHelper {
    private static final String TAG = "PackageTrackerHelperImpl";
    private final Context mContext;
    private final PackageManager mPackageManager;

    PackageTrackerHelperImpl(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
    }

    @Override // com.android.server.timezone.ConfigHelper
    public boolean isTrackingEnabled() {
        return this.mContext.getResources().getBoolean(17891553);
    }

    @Override // com.android.server.timezone.ConfigHelper
    public String getUpdateAppPackageName() {
        return this.mContext.getResources().getString(17039881);
    }

    @Override // com.android.server.timezone.ConfigHelper
    public String getDataAppPackageName() {
        return this.mContext.getResources().getString(17039880);
    }

    @Override // com.android.server.timezone.ConfigHelper
    public int getCheckTimeAllowedMillis() {
        return this.mContext.getResources().getInteger(17694900);
    }

    @Override // com.android.server.timezone.ConfigHelper
    public int getFailedCheckRetryCount() {
        return this.mContext.getResources().getInteger(17694899);
    }

    @Override // com.android.server.timezone.PackageManagerHelper
    public long getInstalledPackageVersion(String packageName) throws PackageManager.NameNotFoundException {
        return this.mPackageManager.getPackageInfo(packageName, 32768).getLongVersionCode();
    }

    @Override // com.android.server.timezone.PackageManagerHelper
    public boolean isPrivilegedApp(String packageName) throws PackageManager.NameNotFoundException {
        return this.mPackageManager.getPackageInfo(packageName, 32768).applicationInfo.isPrivilegedApp();
    }

    @Override // com.android.server.timezone.PackageManagerHelper
    public boolean usesPermission(String packageName, String requiredPermissionName) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 36864);
        if (packageInfo.requestedPermissions == null) {
            return false;
        }
        for (String requestedPermission : packageInfo.requestedPermissions) {
            if (requiredPermissionName.equals(requestedPermission)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.timezone.PackageManagerHelper
    public boolean contentProviderRegistered(String authority, String requiredPackageName) {
        ProviderInfo providerInfo = this.mPackageManager.resolveContentProviderAsUser(authority, 32768, UserHandle.SYSTEM.getIdentifier());
        if (providerInfo == null) {
            Slog.i(TAG, "contentProviderRegistered: No content provider registered with authority=" + authority);
            return false;
        } else if (requiredPackageName.equals(providerInfo.applicationInfo.packageName)) {
            return true;
        } else {
            Slog.i(TAG, "contentProviderRegistered: App with packageName=" + requiredPackageName + " does not expose the a content provider with authority=" + authority);
            return false;
        }
    }

    @Override // com.android.server.timezone.PackageManagerHelper
    public boolean receiverRegistered(Intent intent, String requiredPermissionName) throws PackageManager.NameNotFoundException {
        List<ResolveInfo> resolveInfo = this.mPackageManager.queryBroadcastReceiversAsUser(intent, 32768, UserHandle.SYSTEM);
        if (resolveInfo.size() != 1) {
            Slog.i(TAG, "receiverRegistered: Zero or multiple broadcast receiver registered for intent=" + intent + ", found=" + resolveInfo);
            return false;
        }
        boolean requiresPermission = requiredPermissionName.equals(resolveInfo.get(0).activityInfo.permission);
        if (!requiresPermission) {
            Slog.i(TAG, "receiverRegistered: Broadcast receiver registered for intent=" + intent + " must require permission " + requiredPermissionName);
        }
        return requiresPermission;
    }
}
