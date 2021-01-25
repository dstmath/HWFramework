package com.android.internal.telephony.metrics;

import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.internal.telephony.nano.TelephonyProto;
import java.util.Arrays;

public class TelephonyEventBuilder {
    private final TelephonyProto.TelephonyEvent mEvent;

    public TelephonyProto.TelephonyEvent build() {
        return this.mEvent;
    }

    public TelephonyEventBuilder() {
        this(-1);
    }

    public TelephonyEventBuilder(int phoneId) {
        this(SystemClock.elapsedRealtime(), phoneId);
    }

    public TelephonyEventBuilder(long timestamp, int phoneId) {
        this.mEvent = new TelephonyProto.TelephonyEvent();
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.timestampMillis = timestamp;
        telephonyEvent.phoneId = phoneId;
    }

    public TelephonyEventBuilder setSettings(TelephonyProto.TelephonySettings settings) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 1;
        telephonyEvent.settings = settings;
        return this;
    }

    public TelephonyEventBuilder setServiceState(TelephonyProto.TelephonyServiceState state) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 2;
        telephonyEvent.serviceState = state;
        return this;
    }

    public TelephonyEventBuilder setImsConnectionState(TelephonyProto.ImsConnectionState state) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 3;
        telephonyEvent.imsConnectionState = state;
        return this;
    }

    public TelephonyEventBuilder setImsCapabilities(TelephonyProto.ImsCapabilities capabilities) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 4;
        telephonyEvent.imsCapabilities = capabilities;
        return this;
    }

    public TelephonyEventBuilder setDataStallRecoveryAction(int action) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 10;
        telephonyEvent.dataStallAction = action;
        return this;
    }

    public TelephonyEventBuilder setSetupDataCall(TelephonyProto.TelephonyEvent.RilSetupDataCall request) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 5;
        telephonyEvent.setupDataCall = request;
        return this;
    }

    public TelephonyEventBuilder setSetupDataCallResponse(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse rsp) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 6;
        telephonyEvent.setupDataCallResponse = rsp;
        return this;
    }

    public TelephonyEventBuilder setDeactivateDataCall(TelephonyProto.TelephonyEvent.RilDeactivateDataCall request) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 8;
        telephonyEvent.deactivateDataCall = request;
        return this;
    }

    public TelephonyEventBuilder setDeactivateDataCallResponse(int errno) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 9;
        telephonyEvent.error = errno;
        return this;
    }

    public TelephonyEventBuilder setDataCalls(TelephonyProto.RilDataCall[] dataCalls) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 7;
        telephonyEvent.dataCalls = dataCalls;
        return this;
    }

    public TelephonyEventBuilder setNITZ(long timestamp) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 12;
        telephonyEvent.nitzTimestampMillis = timestamp;
        return this;
    }

    public TelephonyEventBuilder setModemRestart(TelephonyProto.TelephonyEvent.ModemRestart modemRestart) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 11;
        telephonyEvent.modemRestart = modemRestart;
        return this;
    }

    public TelephonyEventBuilder setCarrierIdMatching(TelephonyProto.TelephonyEvent.CarrierIdMatching carrierIdMatching) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 13;
        telephonyEvent.carrierIdMatching = carrierIdMatching;
        return this;
    }

    public TelephonyEventBuilder setUpdatedEmergencyNumber(TelephonyProto.EmergencyNumberInfo emergencyNumberInfo) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 21;
        telephonyEvent.updatedEmergencyNumber = emergencyNumberInfo;
        return this;
    }

    public TelephonyEventBuilder setCarrierKeyChange(TelephonyProto.TelephonyEvent.CarrierKeyChange carrierKeyChange) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 14;
        telephonyEvent.carrierKeyChange = carrierKeyChange;
        return this;
    }

    public TelephonyEventBuilder setSimStateChange(SparseArray<Integer> simStates) {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.simState = new int[phoneCount];
        Arrays.fill(telephonyEvent.simState, 0);
        this.mEvent.type = 18;
        for (int i = 0; i < simStates.size(); i++) {
            int key = simStates.keyAt(i);
            if (key >= 0 && key < phoneCount) {
                this.mEvent.simState[key] = simStates.get(key).intValue();
            }
        }
        return this;
    }

    public TelephonyEventBuilder setActiveSubscriptionInfoChange(TelephonyProto.ActiveSubscriptionInfo info) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 19;
        telephonyEvent.activeSubscriptionInfo = info;
        return this;
    }

    public TelephonyEventBuilder setEnabledModemBitmap(int enabledModemBitmap) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 20;
        telephonyEvent.enabledModemBitmap = enabledModemBitmap;
        return this;
    }

    public TelephonyEventBuilder setDataSwitch(TelephonyProto.TelephonyEvent.DataSwitch dataSwitch) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 15;
        telephonyEvent.dataSwitch = dataSwitch;
        return this;
    }

    public TelephonyEventBuilder setNetworkValidate(int networkValidationState) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 16;
        telephonyEvent.networkValidationState = networkValidationState;
        return this;
    }

    public TelephonyEventBuilder setOnDemandDataSwitch(TelephonyProto.TelephonyEvent.OnDemandDataSwitch onDemandDataSwitch) {
        TelephonyProto.TelephonyEvent telephonyEvent = this.mEvent;
        telephonyEvent.type = 17;
        telephonyEvent.onDemandDataSwitch = onDemandDataSwitch;
        return this;
    }
}
