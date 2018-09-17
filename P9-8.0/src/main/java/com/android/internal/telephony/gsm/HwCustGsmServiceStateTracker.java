package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.WorkSource;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ServiceStateTracker;
import java.util.List;

public class HwCustGsmServiceStateTracker {
    protected Context mContext;
    protected GsmCdmaPhone mGsmPhone;

    public HwCustGsmServiceStateTracker(GsmCdmaPhone gsmPhone) {
        this.mGsmPhone = gsmPhone;
        this.mContext = gsmPhone.getContext();
    }

    public void updateRomingVoicemailNumber(ServiceState currentState) {
    }

    public void dispose(GsmCdmaPhone phone) {
    }

    public void initOnce(GsmCdmaPhone phone, CommandsInterface ci) {
    }

    public boolean isDataOffForbidLTE() {
        return false;
    }

    public void processEnforceLTENetworkTypePending() {
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

    public void setLTEUsageForRomaing(boolean isRoaming) {
    }

    public boolean handleMessage(Message msg) {
        return false;
    }

    public void getLteFreqWithWlanCoex(CommandsInterface ci, ServiceStateTracker sst) {
    }

    public void handleNetworkRejectionEx(int rejcode, int rejrat) {
    }

    public boolean isUpdateCAByCell(ServiceState newSS) {
        return true;
    }

    public List<CellInfo> getAllEnhancedCellInfo(WorkSource workSource) {
        return null;
    }

    public boolean isCsPopShow(int notifyType) {
        return false;
    }

    public void updateLTEBandWidth(ServiceState newSS) {
    }
}
