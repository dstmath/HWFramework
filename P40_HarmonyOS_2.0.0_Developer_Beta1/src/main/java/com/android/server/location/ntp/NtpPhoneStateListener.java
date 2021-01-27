package com.android.server.location.ntp;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import com.android.server.location.LBSLog;

public class NtpPhoneStateListener extends PhoneStateListener {
    private static final boolean DBG = true;
    private static final String TAG = "NtpPhoneStateListener";
    private boolean mIsCdma = false;

    public NtpPhoneStateListener(int subId) {
        super(Integer.valueOf(subId));
        LBSLog.i(TAG, false, "NtpPhoneStateListener create subId:%{public}d", Integer.valueOf(subId));
    }

    @Override // android.telephony.PhoneStateListener
    public void onServiceStateChanged(ServiceState state) {
        if (state != null) {
            this.mIsCdma = ServiceState.isCdma(state.getRilVoiceRadioTechnology());
            LBSLog.i(TAG, false, "onServiceStateChanged subId:%{public}d , isCdma= %{public}b", this.mSubId, Boolean.valueOf(this.mIsCdma));
        }
    }

    public boolean isCdma() {
        return this.mIsCdma;
    }
}
