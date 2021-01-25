package com.android.server.backup.encryption.keys;

import android.content.Context;
import android.security.keystore.recovery.InternalRecoveryServiceException;
import android.security.keystore.recovery.RecoveryController;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import javax.crypto.SecretKey;

public class RecoverableKeyStoreSecondaryKey {
    private static final String TAG = "RecoverableKeyStoreSecondaryKey";
    private final String mAlias;
    private final SecretKey mSecretKey;

    public @interface Status {
        public static final int DESTROYED = 3;
        public static final int NOT_SYNCED = 1;
        public static final int SYNCED = 2;
    }

    public RecoverableKeyStoreSecondaryKey(String alias, SecretKey secretKey) {
        this.mAlias = (String) Preconditions.checkNotNull(alias);
        this.mSecretKey = (SecretKey) Preconditions.checkNotNull(secretKey);
    }

    public String getAlias() {
        return this.mAlias;
    }

    public SecretKey getSecretKey() {
        return this.mSecretKey;
    }

    @Status
    public int getStatus(Context context) {
        try {
            return getStatusInternal(context);
        } catch (InternalRecoveryServiceException e) {
            Slog.wtf(TAG, "Internal error getting recovery status", e);
            return 1;
        }
    }

    @Status
    private int getStatusInternal(Context context) throws InternalRecoveryServiceException {
        int status = RecoveryController.getInstance(context).getRecoveryStatus(this.mAlias);
        if (status == 0) {
            return 2;
        }
        if (status == 1) {
            return 1;
        }
        if (status == 3) {
            return 3;
        }
        throw new InternalRecoveryServiceException("Unexpected status from getRecoveryStatus: " + status);
    }
}
