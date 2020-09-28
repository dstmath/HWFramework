package com.android.internal.telephony;

import android.content.Context;
import android.telephony.ServiceState;
import android.util.Log;

public class HwPhoneServiceEx {
    private static final String TAG = "HwPhoneServiceEx";
    private Context mContext;
    private HwPhoneService mHwPhoneService;

    public HwPhoneServiceEx(HwPhoneService hwPhoneService, Context context) {
        this.mHwPhoneService = hwPhoneService;
        this.mContext = context;
    }

    public int getLevelForSa(int phoneId, int nrLevel, int primaryLevel) {
        Phone phoneSa = PhoneFactory.getPhone(phoneId);
        ServiceStateTracker sst = phoneSa != null ? phoneSa.getServiceStateTracker() : null;
        if (sst == null) {
            Log.e(TAG, "sst is null.");
            return primaryLevel;
        }
        ServiceState ss = sst.getmSSHw();
        if (ss == null || ss.getDataNetworkType() != 20 || nrLevel == 0) {
            return primaryLevel;
        }
        return nrLevel;
    }

    public int getRrcConnectionState(int slotId) {
        enforceReadPermission();
        HwServiceStateTrackerEx serviceStateTrackerEx = HwServiceStateTrackerEx.getInstance(slotId);
        if (serviceStateTrackerEx != null) {
            return serviceStateTrackerEx.getRrcConnectionState();
        }
        return -1;
    }

    private void enforceReadPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", null);
    }
}
