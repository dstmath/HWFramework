package com.android.internal.telephony.metrics;

import android.os.SystemClock;
import com.android.internal.telephony.nano.TelephonyProto;

public class TelephonyEventBuilder {
    private final TelephonyProto.TelephonyEvent mEvent;

    public TelephonyProto.TelephonyEvent build() {
        return this.mEvent;
    }

    public TelephonyEventBuilder(int phoneId) {
        this(SystemClock.elapsedRealtime(), phoneId);
    }

    public TelephonyEventBuilder(long timestamp, int phoneId) {
        this.mEvent = new TelephonyProto.TelephonyEvent();
        this.mEvent.timestampMillis = timestamp;
        this.mEvent.phoneId = phoneId;
    }

    public TelephonyEventBuilder setSettings(TelephonyProto.TelephonySettings settings) {
        this.mEvent.type = 1;
        this.mEvent.settings = settings;
        return this;
    }

    public TelephonyEventBuilder setServiceState(TelephonyProto.TelephonyServiceState state) {
        this.mEvent.type = 2;
        this.mEvent.serviceState = state;
        return this;
    }

    public TelephonyEventBuilder setImsConnectionState(TelephonyProto.ImsConnectionState state) {
        this.mEvent.type = 3;
        this.mEvent.imsConnectionState = state;
        return this;
    }

    public TelephonyEventBuilder setImsCapabilities(TelephonyProto.ImsCapabilities capabilities) {
        this.mEvent.type = 4;
        this.mEvent.imsCapabilities = capabilities;
        return this;
    }

    public TelephonyEventBuilder setDataStallRecoveryAction(int action) {
        this.mEvent.type = 10;
        this.mEvent.dataStallAction = action;
        return this;
    }

    public TelephonyEventBuilder setSetupDataCall(TelephonyProto.TelephonyEvent.RilSetupDataCall request) {
        this.mEvent.type = 5;
        this.mEvent.setupDataCall = request;
        return this;
    }

    public TelephonyEventBuilder setSetupDataCallResponse(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse rsp) {
        this.mEvent.type = 6;
        this.mEvent.setupDataCallResponse = rsp;
        return this;
    }

    public TelephonyEventBuilder setDeactivateDataCall(TelephonyProto.TelephonyEvent.RilDeactivateDataCall request) {
        this.mEvent.type = 8;
        this.mEvent.deactivateDataCall = request;
        return this;
    }

    public TelephonyEventBuilder setDeactivateDataCallResponse(int errno) {
        this.mEvent.type = 9;
        this.mEvent.error = errno;
        return this;
    }

    public TelephonyEventBuilder setDataCalls(TelephonyProto.RilDataCall[] dataCalls) {
        this.mEvent.type = 7;
        this.mEvent.dataCalls = dataCalls;
        return this;
    }

    public TelephonyEventBuilder setNITZ(long timestamp) {
        this.mEvent.type = 12;
        this.mEvent.nitzTimestampMillis = timestamp;
        return this;
    }

    public TelephonyEventBuilder setModemRestart(TelephonyProto.TelephonyEvent.ModemRestart modemRestart) {
        this.mEvent.type = 11;
        this.mEvent.modemRestart = modemRestart;
        return this;
    }

    public TelephonyEventBuilder setCarrierIdMatching(TelephonyProto.TelephonyEvent.CarrierIdMatching carrierIdMatching) {
        this.mEvent.type = 13;
        this.mEvent.carrierIdMatching = carrierIdMatching;
        return this;
    }

    public TelephonyEventBuilder setCarrierKeyChange(TelephonyProto.TelephonyEvent.CarrierKeyChange carrierKeyChange) {
        this.mEvent.type = 14;
        this.mEvent.carrierKeyChange = carrierKeyChange;
        return this;
    }
}
