package com.android.internal.telephony.timezone;

import android.content.Context;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.internal.telephony.HwDualCardsLocationTimeZoneUpdate;
import com.android.internal.telephony.HwDualCardsTimeUpdate;
import com.android.internal.telephony.HwLocationBasedTimeZoneUpdater;
import com.android.internal.telephony.HwTimeZoneUpdater;
import com.huawei.internal.telephony.PhoneExt;

public class HwTimeZoneManagerImpl extends DefaultHwTimeZoneManager {
    private static final int INVALID_NUMBER = -1;
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwTimeZoneManagerImpl";
    private static HwTimeZoneManagerImpl sInstance = null;

    public static HwTimeZoneManagerImpl getInstance() {
        HwTimeZoneManagerImpl hwTimeZoneManagerImpl;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwTimeZoneManagerImpl();
            }
            hwTimeZoneManagerImpl = sInstance;
        }
        return hwTimeZoneManagerImpl;
    }

    public boolean isHwTimeZoneSupported() {
        return true;
    }

    public void initHwTimeZoneUpdater(Context context) {
        new HwTimeZoneUpdater(context);
    }

    public boolean isNeedLocationTimeZoneUpdate(PhoneExt phone, String zoneId) {
        return HwDualCardsLocationTimeZoneUpdate.getDefault().isNeedLocationTimeZoneUpdate(phone, zoneId);
    }

    public void sendNitzTimeZoneUpdateMessage(CellLocation cellLoc) {
        int lac = INVALID_NUMBER;
        if (cellLoc instanceof GsmCellLocation) {
            lac = ((GsmCellLocation) cellLoc).getLac();
            Log.i(LOG_TAG, "sendNitzTimeZoneUpdateMessage");
        }
        HwLocationBasedTimeZoneUpdater hwLocTzUpdater = HwLocationBasedTimeZoneUpdater.getInstance();
        Handler handle = hwLocTzUpdater != null ? hwLocTzUpdater.getHandler() : null;
        if (handle != null) {
            handle.sendMessage(handle.obtainMessage(1, Integer.valueOf(lac)));
        }
    }

    public boolean allowUpdateTimeFromNitz(PhoneExt phone, long nitzTime) {
        return HwDualCardsTimeUpdate.getDefault().allowUpdateTimeFromNitz(phone, nitzTime);
    }
}
