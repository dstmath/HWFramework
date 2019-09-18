package android.test;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;

@Deprecated
public class SyncBaseInstrumentation extends InstrumentationTestCase {
    private static final int MAX_TIME_FOR_SYNC_IN_MINS = 20;
    ContentResolver mContentResolver;
    private Context mTargetContext;

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        SyncBaseInstrumentation.super.setUp();
        this.mTargetContext = getInstrumentation().getTargetContext();
        this.mContentResolver = this.mTargetContext.getContentResolver();
    }

    /* access modifiers changed from: protected */
    public void syncProvider(Uri uri, String accountName, String authority) throws Exception {
        Bundle extras = new Bundle();
        extras.putBoolean("ignore_settings", true);
        Account account = new Account(accountName, "com.google");
        ContentResolver.requestSync(account, authority, extras);
        long endTimeInMillis = 1200000 + SystemClock.elapsedRealtime();
        int counter = 0;
        while (counter < 2) {
            Thread.sleep(1000);
            if (SystemClock.elapsedRealtime() <= endTimeInMillis) {
                if (ContentResolver.isSyncActive(account, authority)) {
                    counter = 0;
                } else {
                    counter++;
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void cancelSyncsandDisableAutoSync() {
        ContentResolver.setMasterSyncAutomatically(false);
        ContentResolver.cancelSync(null, null);
    }
}
