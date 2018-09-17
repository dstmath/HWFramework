package com.android.internal.telephony.metrics;

import com.android.internal.telephony.nano.TelephonyProto.ImsCapabilities;
import com.android.internal.telephony.nano.TelephonyProto.ImsConnectionState;
import com.android.internal.telephony.nano.TelephonyProto.RilDataCall;
import com.android.internal.telephony.nano.TelephonyProto.SmsSession.Event;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyServiceState;
import com.android.internal.telephony.nano.TelephonyProto.TelephonySettings;

public class SmsSessionEventBuilder {
    Event mEvent = new Event();

    public Event build() {
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

    public SmsSessionEventBuilder setSettings(TelephonySettings settings) {
        this.mEvent.settings = settings;
        return this;
    }

    public SmsSessionEventBuilder setServiceState(TelephonyServiceState state) {
        this.mEvent.serviceState = state;
        return this;
    }

    public SmsSessionEventBuilder setImsConnectionState(ImsConnectionState state) {
        this.mEvent.imsConnectionState = state;
        return this;
    }

    public SmsSessionEventBuilder setImsCapabilities(ImsCapabilities capabilities) {
        this.mEvent.imsCapabilities = capabilities;
        return this;
    }

    public SmsSessionEventBuilder setDataCalls(RilDataCall[] dataCalls) {
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
}
