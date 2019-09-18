package com.android.server.backup;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;

@VisibleForTesting
public class BackupPolicyEnforcer {
    private Context mContext;
    private DevicePolicyManager mDevicePolicyManager;

    public BackupPolicyEnforcer(Context context) {
        this.mContext = context;
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
    }

    public ComponentName getMandatoryBackupTransport() {
        if (this.mDevicePolicyManager == null) {
            Context context = this.mContext;
            Context context2 = this.mContext;
            this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
            Slog.w("BackupPolicyEnforcer", "mDevicePolicyManager is null");
        }
        if (this.mDevicePolicyManager != null) {
            return this.mDevicePolicyManager.getMandatoryBackupTransport();
        }
        return null;
    }
}
