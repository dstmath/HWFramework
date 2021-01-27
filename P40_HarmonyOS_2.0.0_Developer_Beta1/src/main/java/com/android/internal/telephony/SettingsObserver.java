package com.android.internal.telephony;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.telephony.Rlog;
import java.util.HashMap;
import java.util.Map;

public class SettingsObserver extends ContentObserver {
    private static final String TAG = "SettingsObserver";
    private final Context mContext;
    private final Handler mHandler;
    private final Map<Uri, Integer> mUriEventMap = new HashMap();

    public SettingsObserver(Context context, Handler handler) {
        super(null);
        this.mContext = context;
        this.mHandler = handler;
    }

    public void observe(Uri uri, int what) {
        this.mUriEventMap.put(uri, Integer.valueOf(what));
        this.mContext.getContentResolver().registerContentObserver(uri, false, this);
    }

    public void unobserve() {
        this.mContext.getContentResolver().unregisterContentObserver(this);
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        Rlog.e(TAG, "Should never be reached.");
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        Integer what = this.mUriEventMap.get(uri);
        if (what != null) {
            this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            return;
        }
        Rlog.e(TAG, "No matching event to send for URI=" + uri);
    }
}
