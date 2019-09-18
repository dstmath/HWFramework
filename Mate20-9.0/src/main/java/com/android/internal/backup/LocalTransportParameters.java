package com.android.internal.backup;

import android.content.ContentResolver;
import android.os.Handler;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.KeyValueSettingObserver;

class LocalTransportParameters extends KeyValueSettingObserver {
    private static final String KEY_FAKE_ENCRYPTION_FLAG = "fake_encryption_flag";
    private static final String KEY_NON_INCREMENTAL_ONLY = "non_incremental_only";
    private static final String SETTING = "backup_local_transport_parameters";
    private static final String TAG = "LocalTransportParams";
    private boolean mFakeEncryptionFlag;
    private boolean mIsNonIncrementalOnly;

    LocalTransportParameters(Handler handler, ContentResolver resolver) {
        super(handler, resolver, Settings.Secure.getUriFor(SETTING));
    }

    /* access modifiers changed from: package-private */
    public boolean isFakeEncryptionFlag() {
        return this.mFakeEncryptionFlag;
    }

    /* access modifiers changed from: package-private */
    public boolean isNonIncrementalOnly() {
        return this.mIsNonIncrementalOnly;
    }

    public String getSettingValue(ContentResolver resolver) {
        return Settings.Secure.getString(resolver, SETTING);
    }

    public void update(KeyValueListParser parser) {
        this.mFakeEncryptionFlag = parser.getBoolean(KEY_FAKE_ENCRYPTION_FLAG, false);
        this.mIsNonIncrementalOnly = parser.getBoolean(KEY_NON_INCREMENTAL_ONLY, false);
    }
}
