package com.android.server.backup;

import android.app.backup.BlobBackupHelper;
import android.app.slice.ISliceManager;
import android.content.Context;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;

public class SliceBackupHelper extends BlobBackupHelper {
    static final int BLOB_VERSION = 1;
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String KEY_SLICES = "slices";
    static final String TAG = "SliceBackupHelper";

    public SliceBackupHelper(Context context) {
        super(1, new String[]{KEY_SLICES});
    }

    /* access modifiers changed from: protected */
    public byte[] getBackupPayload(String key) {
        if (!KEY_SLICES.equals(key)) {
            return null;
        }
        try {
            return ISliceManager.Stub.asInterface(ServiceManager.getService("slice")).getBackupPayload(0);
        } catch (Exception e) {
            Slog.e(TAG, "Couldn't communicate with slice manager");
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void applyRestoredPayload(String key, byte[] payload) {
        if (DEBUG) {
            Slog.v(TAG, "Got restore of " + key);
        }
        if (KEY_SLICES.equals(key)) {
            try {
                ISliceManager.Stub.asInterface(ServiceManager.getService("slice")).applyRestore(payload, 0);
            } catch (Exception e) {
                Slog.e(TAG, "Couldn't communicate with slice manager");
            }
        }
    }
}
