package com.android.internal.telephony.timezone;

import android.content.Context;
import android.telephony.CellLocation;
import android.util.Log;
import com.android.internal.telephony.HwPartOptTelephonyFactory;
import com.huawei.internal.telephony.PhoneExt;

public class HwTimeZoneManager {
    private static final String TAG = "HwTimeZoneManager";
    private static HwTimeZoneManager sInstance;
    private DefaultHwTimeZoneManager mHwTimeZoneManager = HwPartOptTelephonyFactory.getTelephonyFactory().getTimeZoneFactory().getHwTimeZoneManager();

    private HwTimeZoneManager() {
        Log.d(TAG, "add " + this.mHwTimeZoneManager.getClass().getCanonicalName() + " to memory");
    }

    public static HwTimeZoneManager getInstance() {
        if (sInstance == null) {
            sInstance = new HwTimeZoneManager();
        }
        return sInstance;
    }

    public boolean isHwTimeZoneSupported() {
        return this.mHwTimeZoneManager.isHwTimeZoneSupported();
    }

    public void initHwTimeZoneUpdater(Context context) {
        this.mHwTimeZoneManager.initHwTimeZoneUpdater(context);
    }

    public boolean isNeedLocationTimeZoneUpdate(PhoneExt phone, String zoneId) {
        return this.mHwTimeZoneManager.isNeedLocationTimeZoneUpdate(phone, zoneId);
    }

    public void sendNitzTimeZoneUpdateMessage(CellLocation cellLoc) {
        this.mHwTimeZoneManager.sendNitzTimeZoneUpdateMessage(cellLoc);
    }

    public boolean allowUpdateTimeFromNitz(PhoneExt phone, long nitzTime) {
        return this.mHwTimeZoneManager.allowUpdateTimeFromNitz(phone, nitzTime);
    }
}
