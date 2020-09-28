package com.android.server.backup;

import android.accounts.AccountManagerInternal;
import android.app.backup.BlobBackupHelper;
import android.util.Slog;
import com.android.server.LocalServices;

public class AccountManagerBackupHelper extends BlobBackupHelper {
    private static final boolean DEBUG = false;
    private static final String KEY_ACCOUNT_ACCESS_GRANTS = "account_access_grants";
    private static final int STATE_VERSION = 1;
    private static final String TAG = "AccountsBackup";

    public AccountManagerBackupHelper() {
        super(1, KEY_ACCOUNT_ACCESS_GRANTS);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.backup.BlobBackupHelper
    public byte[] getBackupPayload(String key) {
        AccountManagerInternal am = (AccountManagerInternal) LocalServices.getService(AccountManagerInternal.class);
        char c = 65535;
        try {
            if (key.hashCode() == 1544100736 && key.equals(KEY_ACCOUNT_ACCESS_GRANTS)) {
                c = 0;
            }
            if (c == 0) {
                return am.backupAccountAccessPermissions(0);
            }
            Slog.w(TAG, "Unexpected backup key " + key);
            return new byte[0];
        } catch (Exception e) {
            Slog.e(TAG, "Unable to store payload " + key);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.backup.BlobBackupHelper
    public void applyRestoredPayload(String key, byte[] payload) {
        AccountManagerInternal am = (AccountManagerInternal) LocalServices.getService(AccountManagerInternal.class);
        char c = 65535;
        try {
            if (key.hashCode() == 1544100736 && key.equals(KEY_ACCOUNT_ACCESS_GRANTS)) {
                c = 0;
            }
            if (c != 0) {
                Slog.w(TAG, "Unexpected restore key " + key);
                return;
            }
            am.restoreAccountAccessPermissions(payload, 0);
        } catch (Exception e) {
            Slog.w(TAG, "Unable to restore key " + key);
        }
    }
}
