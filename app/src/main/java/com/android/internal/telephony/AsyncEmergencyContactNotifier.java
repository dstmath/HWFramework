package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.BlockedNumberContract.SystemContract;
import android.telephony.Rlog;

public class AsyncEmergencyContactNotifier extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "AsyncEmergencyContactNotifier";
    private final Context mContext;

    public AsyncEmergencyContactNotifier(Context context) {
        this.mContext = context;
    }

    protected Void doInBackground(Void... params) {
        try {
            SystemContract.notifyEmergencyContact(this.mContext);
        } catch (Exception e) {
            Rlog.e(TAG, "Exception notifying emergency contact: " + e);
        }
        return null;
    }
}
