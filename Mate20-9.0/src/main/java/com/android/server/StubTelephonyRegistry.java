package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.util.Log;
import com.android.internal.telephony.IPhoneStateListener;
import com.huawei.hsm.permission.LocationPermission;

class StubTelephonyRegistry extends TelephonyRegistry {
    private static final String TAG = "StubTelephonyRegistry";
    private Context mContext;
    private LocationPermission mLocationPermission = new LocationPermission(this.mContext);

    StubTelephonyRegistry(Context context) {
        super(context);
        this.mContext = context;
    }

    public void listenForSubscriber(int subId, String pkgForDebug, IPhoneStateListener callback, int events, boolean notifyNow) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        Log.d(TAG, "uid:" + uid + " pid:" + pid + " PhoneStateListener.LISTEN_CELL_LOCATION:" + 16 + " events:" + events);
        if ((events & 16) != 0) {
            Log.d(TAG, "LISTEN_CELL_LOCATION uid:" + uid);
            if (this.mLocationPermission.isLocationBlocked()) {
                Log.d(TAG, "LISTEN_CELL_LOCATION is blocked by huawei's permission manager uid:" + uid);
                events &= -17;
            }
        }
        StubTelephonyRegistry.super.listenForSubscriber(subId, pkgForDebug, callback, events, notifyNow);
    }
}
