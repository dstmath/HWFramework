package com.android.server.location.ntp;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.util.Log;

public class NtpPhoneStateListener extends PhoneStateListener {
    private static boolean DBG = true;
    private static final String TAG = "NtpPhoneStateListener";
    private boolean mIsCdma = false;

    public NtpPhoneStateListener(int subId) {
        super(Integer.valueOf(subId));
        if (DBG) {
            Log.d(TAG, "NtpPhoneStateListener create subId:" + subId);
        }
    }

    public void onServiceStateChanged(ServiceState state) {
        if (state != null) {
            this.mIsCdma = ServiceState.isCdma(state.getRilVoiceRadioTechnology());
            if (DBG) {
                Log.d(TAG, "onServiceStateChanged subId:" + this.mSubId + " isCdma=" + this.mIsCdma);
            }
        }
    }

    public boolean isCdma() {
        return this.mIsCdma;
    }
}
