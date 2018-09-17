package com.android.internal.telephony.metrics;

import android.os.SystemClock;
import com.android.internal.telephony.nano.TelephonyProto.ImsCapabilities;
import com.android.internal.telephony.nano.TelephonyProto.ImsConnectionState;
import com.android.internal.telephony.nano.TelephonyProto.RilDataCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.ModemRestart;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.RilDeactivateDataCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.RilSetupDataCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyEvent.RilSetupDataCallResponse;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyServiceState;
import com.android.internal.telephony.nano.TelephonyProto.TelephonySettings;

public class TelephonyEventBuilder {
    private final TelephonyEvent mEvent;

    public TelephonyEvent build() {
        return this.mEvent;
    }

    public TelephonyEventBuilder(int phoneId) {
        this(SystemClock.elapsedRealtime(), phoneId);
    }

    public TelephonyEventBuilder(long timestamp, int phoneId) {
        this.mEvent = new TelephonyEvent();
        this.mEvent.timestampMillis = timestamp;
        this.mEvent.phoneId = phoneId;
    }

    public TelephonyEventBuilder setSettings(TelephonySettings settings) {
        this.mEvent.type = 1;
        this.mEvent.settings = settings;
        return this;
    }

    public TelephonyEventBuilder setServiceState(TelephonyServiceState state) {
        this.mEvent.type = 2;
        this.mEvent.serviceState = state;
        return this;
    }

    public TelephonyEventBuilder setImsConnectionState(ImsConnectionState state) {
        this.mEvent.type = 3;
        this.mEvent.imsConnectionState = state;
        return this;
    }

    public TelephonyEventBuilder setImsCapabilities(ImsCapabilities capabilities) {
        this.mEvent.type = 4;
        this.mEvent.imsCapabilities = capabilities;
        return this;
    }

    public TelephonyEventBuilder setDataStallRecoveryAction(int action) {
        this.mEvent.type = 10;
        this.mEvent.dataStallAction = action;
        return this;
    }

    public TelephonyEventBuilder setSetupDataCall(RilSetupDataCall request) {
        this.mEvent.type = 5;
        this.mEvent.setupDataCall = request;
        return this;
    }

    public TelephonyEventBuilder setSetupDataCallResponse(RilSetupDataCallResponse rsp) {
        this.mEvent.type = 6;
        this.mEvent.setupDataCallResponse = rsp;
        return this;
    }

    public TelephonyEventBuilder setDeactivateDataCall(RilDeactivateDataCall request) {
        this.mEvent.type = 8;
        this.mEvent.deactivateDataCall = request;
        return this;
    }

    public TelephonyEventBuilder setDeactivateDataCallResponse(int errno) {
        this.mEvent.type = 9;
        this.mEvent.error = errno;
        return this;
    }

    public TelephonyEventBuilder setDataCalls(RilDataCall[] dataCalls) {
        this.mEvent.type = 7;
        this.mEvent.dataCalls = dataCalls;
        return this;
    }

    public TelephonyEventBuilder setNITZ(long timestamp) {
        this.mEvent.type = 12;
        this.mEvent.nitzTimestampMillis = timestamp;
        return this;
    }

    public TelephonyEventBuilder setModemRestart(ModemRestart modemRestart) {
        this.mEvent.type = 11;
        this.mEvent.modemRestart = modemRestart;
        return this;
    }
}
