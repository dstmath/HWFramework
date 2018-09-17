package com.android.server.backup;

import android.app.backup.BlobBackupHelper;
import android.app.usage.UsageStatsManagerInternal;
import android.content.Context;
import com.android.server.LocalServices;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UsageStatsBackupHelper extends BlobBackupHelper {
    static final int BLOB_VERSION = 1;
    static final boolean DEBUG = false;
    static final String KEY_USAGE_STATS = "usage_stats";
    static final String TAG = "UsgStatsBackupHelper";

    public UsageStatsBackupHelper(Context context) {
        super(1, new String[]{KEY_USAGE_STATS});
    }

    protected byte[] getBackupPayload(String key) {
        if (!KEY_USAGE_STATS.equals(key)) {
            return null;
        }
        UsageStatsManagerInternal localUsageStatsManager = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            out.writeInt(0);
            out.write(localUsageStatsManager.getBackupPayload(0, key));
        } catch (IOException e) {
            baos.reset();
        }
        return baos.toByteArray();
    }

    protected void applyRestoredPayload(String key, byte[] payload) {
        if (KEY_USAGE_STATS.equals(key)) {
            UsageStatsManagerInternal localUsageStatsManager = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            try {
                int user = in.readInt();
                byte[] restoreData = new byte[(payload.length - 4)];
                in.read(restoreData, 0, restoreData.length);
                localUsageStatsManager.applyRestoredPayload(user, key, restoreData);
            } catch (IOException e) {
            }
        }
    }
}
