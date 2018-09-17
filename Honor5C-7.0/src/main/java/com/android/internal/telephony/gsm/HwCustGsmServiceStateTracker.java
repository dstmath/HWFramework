package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ServiceStateTracker;

public class HwCustGsmServiceStateTracker {
    protected Context mContext;
    protected GsmCdmaPhone mGsmPhone;

    public HwCustGsmServiceStateTracker(GsmCdmaPhone gsmPhone) {
        this.mGsmPhone = gsmPhone;
        this.mContext = gsmPhone.getContext();
    }

    public void updateRomingVoicemailNumber(ServiceState currentState) {
    }

    public void setPsCell(ServiceState ss, GsmCellLocation newCellLoc, String[] states) {
    }

    public void setRadioPower(CommandsInterface ci, boolean enabled) {
    }

    public String setEmergencyToNoService(ServiceState mSS, String plmn, boolean mEmergencyOnly) {
        return plmn;
    }

    public boolean isInServiceState(int combinedregstate) {
        return false;
    }

    public boolean isInServiceState(ServiceState ss) {
        return false;
    }

    public void setExtPlmnSent(boolean value) {
    }

    public void custHandlePollStateResult(int what, AsyncResult ar, int[] pollingContext) {
    }

    public void handleNetworkRejection(int regState, String[] states) {
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
    }

    public boolean isStopUpdateName() {
        return false;
    }

    public boolean isStopUpdateName(boolean SimCardsLoaded) {
        return false;
    }

    public boolean isUpdateLacAndCidCust(ServiceStateTracker sst) {
        return false;
    }

    public boolean handleMessage(Message msg) {
        return false;
    }

    public void getLteFreqWithWlanCoex(CommandsInterface ci, ServiceStateTracker sst) {
    }
}
