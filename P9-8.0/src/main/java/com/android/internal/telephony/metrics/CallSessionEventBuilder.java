package com.android.internal.telephony.metrics;

import com.android.internal.telephony.nano.TelephonyProto.ImsCapabilities;
import com.android.internal.telephony.nano.TelephonyProto.ImsConnectionState;
import com.android.internal.telephony.nano.TelephonyProto.ImsReasonInfo;
import com.android.internal.telephony.nano.TelephonyProto.RilDataCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyCallSession.Event;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyCallSession.Event.RilCall;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyServiceState;
import com.android.internal.telephony.nano.TelephonyProto.TelephonySettings;

public class CallSessionEventBuilder {
    private final Event mEvent = new Event();

    public Event build() {
        return this.mEvent;
    }

    public CallSessionEventBuilder(int type) {
        this.mEvent.type = type;
    }

    public CallSessionEventBuilder setDelay(int delay) {
        this.mEvent.delay = delay;
        return this;
    }

    public CallSessionEventBuilder setRilRequest(int rilRequestType) {
        this.mEvent.rilRequest = rilRequestType;
        return this;
    }

    public CallSessionEventBuilder setRilRequestId(int rilRequestId) {
        this.mEvent.rilRequestId = rilRequestId;
        return this;
    }

    public CallSessionEventBuilder setRilError(int rilError) {
        this.mEvent.error = rilError;
        return this;
    }

    public CallSessionEventBuilder setCallIndex(int callIndex) {
        this.mEvent.callIndex = callIndex;
        return this;
    }

    public CallSessionEventBuilder setCallState(int state) {
        this.mEvent.callState = state;
        return this;
    }

    public CallSessionEventBuilder setSrvccState(int srvccState) {
        this.mEvent.srvccState = srvccState;
        return this;
    }

    public CallSessionEventBuilder setImsCommand(int imsCommand) {
        this.mEvent.imsCommand = imsCommand;
        return this;
    }

    public CallSessionEventBuilder setImsReasonInfo(ImsReasonInfo reasonInfo) {
        this.mEvent.reasonInfo = reasonInfo;
        return this;
    }

    public CallSessionEventBuilder setSrcAccessTech(int tech) {
        this.mEvent.srcAccessTech = tech;
        return this;
    }

    public CallSessionEventBuilder setTargetAccessTech(int tech) {
        this.mEvent.targetAccessTech = tech;
        return this;
    }

    public CallSessionEventBuilder setSettings(TelephonySettings settings) {
        this.mEvent.settings = settings;
        return this;
    }

    public CallSessionEventBuilder setServiceState(TelephonyServiceState state) {
        this.mEvent.serviceState = state;
        return this;
    }

    public CallSessionEventBuilder setImsConnectionState(ImsConnectionState state) {
        this.mEvent.imsConnectionState = state;
        return this;
    }

    public CallSessionEventBuilder setImsCapabilities(ImsCapabilities capabilities) {
        this.mEvent.imsCapabilities = capabilities;
        return this;
    }

    public CallSessionEventBuilder setDataCalls(RilDataCall[] dataCalls) {
        this.mEvent.dataCalls = dataCalls;
        return this;
    }

    public CallSessionEventBuilder setPhoneState(int phoneState) {
        this.mEvent.phoneState = phoneState;
        return this;
    }

    public CallSessionEventBuilder setNITZ(long timestamp) {
        this.mEvent.nitzTimestampMillis = timestamp;
        return this;
    }

    public CallSessionEventBuilder setRilCalls(RilCall[] rilCalls) {
        this.mEvent.calls = rilCalls;
        return this;
    }
}
