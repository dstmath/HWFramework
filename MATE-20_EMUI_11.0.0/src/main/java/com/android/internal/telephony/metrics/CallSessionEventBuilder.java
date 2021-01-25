package com.android.internal.telephony.metrics;

import com.android.internal.telephony.nano.TelephonyProto;

public class CallSessionEventBuilder {
    private final TelephonyProto.TelephonyCallSession.Event mEvent = new TelephonyProto.TelephonyCallSession.Event();

    public TelephonyProto.TelephonyCallSession.Event build() {
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

    public CallSessionEventBuilder setImsReasonInfo(TelephonyProto.ImsReasonInfo reasonInfo) {
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

    public CallSessionEventBuilder setSettings(TelephonyProto.TelephonySettings settings) {
        this.mEvent.settings = settings;
        return this;
    }

    public CallSessionEventBuilder setServiceState(TelephonyProto.TelephonyServiceState state) {
        this.mEvent.serviceState = state;
        return this;
    }

    public CallSessionEventBuilder setImsConnectionState(TelephonyProto.ImsConnectionState state) {
        this.mEvent.imsConnectionState = state;
        return this;
    }

    public CallSessionEventBuilder setImsCapabilities(TelephonyProto.ImsCapabilities capabilities) {
        this.mEvent.imsCapabilities = capabilities;
        return this;
    }

    public CallSessionEventBuilder setDataCalls(TelephonyProto.RilDataCall[] dataCalls) {
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

    public CallSessionEventBuilder setRilCalls(TelephonyProto.TelephonyCallSession.Event.RilCall[] rilCalls) {
        this.mEvent.calls = rilCalls;
        return this;
    }

    public CallSessionEventBuilder setAudioCodec(int audioCodec) {
        this.mEvent.audioCodec = audioCodec;
        return this;
    }

    public CallSessionEventBuilder setCallQuality(TelephonyProto.TelephonyCallSession.Event.CallQuality callQuality) {
        this.mEvent.callQuality = callQuality;
        return this;
    }

    public CallSessionEventBuilder setCallQualitySummaryDl(TelephonyProto.TelephonyCallSession.Event.CallQualitySummary callQualitySummary) {
        this.mEvent.callQualitySummaryDl = callQualitySummary;
        return this;
    }

    public CallSessionEventBuilder setCallQualitySummaryUl(TelephonyProto.TelephonyCallSession.Event.CallQualitySummary callQualitySummary) {
        this.mEvent.callQualitySummaryUl = callQualitySummary;
        return this;
    }

    public CallSessionEventBuilder setIsImsEmergencyCall(boolean isImsEmergencyCall) {
        this.mEvent.isImsEmergencyCall = isImsEmergencyCall;
        return this;
    }

    public CallSessionEventBuilder setImsEmergencyNumberInfo(TelephonyProto.EmergencyNumberInfo imsEmergencyNumberInfo) {
        this.mEvent.imsEmergencyNumberInfo = imsEmergencyNumberInfo;
        return this;
    }
}
