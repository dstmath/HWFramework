package com.android.internal.telephony.metrics;

import com.android.internal.telephony.nano.TelephonyProto;

public class SmsSessionEventBuilder {
    TelephonyProto.SmsSession.Event mEvent = new TelephonyProto.SmsSession.Event();

    public TelephonyProto.SmsSession.Event build() {
        return this.mEvent;
    }

    public SmsSessionEventBuilder(int type) {
        this.mEvent.type = type;
    }

    public SmsSessionEventBuilder setDelay(int delay) {
        this.mEvent.delay = delay;
        return this;
    }

    public SmsSessionEventBuilder setTech(int tech) {
        this.mEvent.tech = tech;
        return this;
    }

    public SmsSessionEventBuilder setErrorCode(int code) {
        this.mEvent.errorCode = code;
        return this;
    }

    public SmsSessionEventBuilder setRilErrno(int errno) {
        this.mEvent.error = errno;
        return this;
    }

    public SmsSessionEventBuilder setImsServiceErrno(int errno) {
        this.mEvent.imsError = errno;
        return this;
    }

    public SmsSessionEventBuilder setSettings(TelephonyProto.TelephonySettings settings) {
        this.mEvent.settings = settings;
        return this;
    }

    public SmsSessionEventBuilder setServiceState(TelephonyProto.TelephonyServiceState state) {
        this.mEvent.serviceState = state;
        return this;
    }

    public SmsSessionEventBuilder setImsConnectionState(TelephonyProto.ImsConnectionState state) {
        this.mEvent.imsConnectionState = state;
        return this;
    }

    public SmsSessionEventBuilder setImsCapabilities(TelephonyProto.ImsCapabilities capabilities) {
        this.mEvent.imsCapabilities = capabilities;
        return this;
    }

    public SmsSessionEventBuilder setDataCalls(TelephonyProto.RilDataCall[] dataCalls) {
        this.mEvent.dataCalls = dataCalls;
        return this;
    }

    public SmsSessionEventBuilder setRilRequestId(int id) {
        this.mEvent.rilRequestId = id;
        return this;
    }

    public SmsSessionEventBuilder setFormat(int format) {
        this.mEvent.format = format;
        return this;
    }

    public SmsSessionEventBuilder setCellBroadcastMessage(TelephonyProto.SmsSession.Event.CBMessage msg) {
        this.mEvent.cellBroadcastMessage = msg;
        return this;
    }

    public SmsSessionEventBuilder setIncompleteSms(TelephonyProto.SmsSession.Event.IncompleteSms msg) {
        this.mEvent.incompleteSms = msg;
        return this;
    }

    public SmsSessionEventBuilder setBlocked(boolean blocked) {
        this.mEvent.blocked = blocked;
        return this;
    }

    public SmsSessionEventBuilder setSmsType(int type) {
        this.mEvent.smsType = type;
        return this;
    }
}
