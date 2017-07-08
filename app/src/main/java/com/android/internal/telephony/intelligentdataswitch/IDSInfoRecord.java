package com.android.internal.telephony.intelligentdataswitch;

import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.SubscriptionController;

public class IDSInfoRecord {
    private boolean mAirplaneOn;
    private boolean mIdsState;
    private boolean[] mInVoicecall;
    private boolean[] mIsSlotRoaming;
    private boolean mIsUserDataEnabled;
    private boolean mIsWifiConnected;
    private ServiceState[] mServiceState;
    private SignalStrength[] mSignalStrength;
    private int mUserDefaultDataSlot;

    public IDSInfoRecord() {
        this.mIdsState = false;
        this.mIsWifiConnected = false;
        this.mAirplaneOn = false;
        this.mIsUserDataEnabled = false;
        this.mIsSlotRoaming = new boolean[]{false, false};
        this.mInVoicecall = new boolean[]{false, false};
        this.mServiceState = new ServiceState[]{null, null};
        this.mSignalStrength = new SignalStrength[]{null, null};
        this.mUserDefaultDataSlot = SubscriptionController.getInstance().getDefaultDataSubId();
    }

    protected void setIdsState(boolean state) {
        this.mIdsState = state;
    }

    protected boolean getIdsState() {
        return this.mIdsState;
    }

    protected void setWifiState(boolean state) {
        this.mIsWifiConnected = state;
    }

    protected boolean getWifiState() {
        return this.mIsWifiConnected;
    }

    protected void setAirplaneState(boolean state) {
        this.mAirplaneOn = state;
    }

    protected boolean getAirplaneState() {
        return this.mAirplaneOn;
    }

    protected void setUserDcState(boolean state) {
        this.mIsUserDataEnabled = state;
    }

    protected boolean getUserDcState() {
        return this.mIsUserDataEnabled;
    }

    protected void setSlotRoamingState(int slotId, boolean state) {
        this.mIsSlotRoaming[slotId] = state;
    }

    protected boolean getSlotRoamingState(int slotId) {
        return this.mIsSlotRoaming[slotId];
    }

    protected void updataUserDefaultDataSlot(int newDataSub) {
        this.mUserDefaultDataSlot = newDataSub;
    }

    protected int getUserDefaultDataSlot() {
        return this.mUserDefaultDataSlot;
    }

    protected void setVoiceCallStatus(int slotId, int status) {
        if (status != 0) {
            this.mInVoicecall[slotId] = true;
        } else {
            this.mInVoicecall[slotId] = false;
        }
    }

    protected boolean getVoicecallStatus(int slotId) {
        return this.mInVoicecall[slotId];
    }

    protected void saveSignalStrength(int slotId, SignalStrength signalStrength) {
        this.mSignalStrength[slotId] = signalStrength;
    }

    protected SignalStrength getSignalStrength(int slotId) {
        return this.mSignalStrength[slotId];
    }

    protected void saveServiceState(int slotId, ServiceState serviceState) {
        this.mServiceState[slotId] = serviceState;
        this.mIsSlotRoaming[slotId] = this.mServiceState[slotId].getRoaming();
    }

    protected ServiceState getServiceState(int slotId) {
        return this.mServiceState[slotId];
    }

    protected boolean isRoamingOn() {
        return !this.mIsSlotRoaming[0] ? this.mIsSlotRoaming[1] : true;
    }

    protected boolean isOnlyDataSubInVoiceCall() {
        return this.mInVoicecall[this.mUserDefaultDataSlot] && !this.mInVoicecall[1 - this.mUserDefaultDataSlot];
    }

    protected boolean isBothDataSubInVoiceCall() {
        return this.mInVoicecall[this.mUserDefaultDataSlot] ? this.mInVoicecall[1 - this.mUserDefaultDataSlot] : false;
    }

    protected boolean noVoiceCall() {
        return (this.mInVoicecall[this.mUserDefaultDataSlot] || this.mInVoicecall[1 - this.mUserDefaultDataSlot]) ? false : true;
    }

    protected boolean isAllSignalStrengthValid() {
        return (this.mSignalStrength[0] == null || this.mSignalStrength[1] == null) ? false : true;
    }
}
