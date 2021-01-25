package com.android.internal.telephony.timezone;

import android.content.Context;
import android.telephony.CellLocation;
import com.huawei.internal.telephony.PhoneExt;

/* access modifiers changed from: package-private */
public class DefaultHwTimeZoneManager {
    private static final String TAG = "DefaultHwTimeZoneManager";
    private static DefaultHwTimeZoneManager sInstance = new DefaultHwTimeZoneManager();

    DefaultHwTimeZoneManager() {
    }

    public static DefaultHwTimeZoneManager getInstance() {
        return sInstance;
    }

    public boolean isHwTimeZoneSupported() {
        return false;
    }

    public void initHwTimeZoneUpdater(Context context) {
    }

    public boolean isNeedLocationTimeZoneUpdate(PhoneExt phone, String zoneId) {
        return false;
    }

    public void sendNitzTimeZoneUpdateMessage(CellLocation cellLoc) {
    }

    public boolean allowUpdateTimeFromNitz(PhoneExt phone, long nitzTime) {
        return true;
    }
}
