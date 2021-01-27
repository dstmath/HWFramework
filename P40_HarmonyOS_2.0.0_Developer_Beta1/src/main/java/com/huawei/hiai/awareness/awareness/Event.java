package com.huawei.hiai.awareness.awareness;

import java.util.Locale;

public class Event {
    private int mEventConfidence;
    private int mEventCurAction;
    private int mEventCurStatus;
    private int mEventCurType;
    private long mEventSensorTime;
    private long mEventTime;
    private int mEventTriggerStatus = -1;

    public int getEventCurType() {
        return this.mEventCurType;
    }

    public void setEventCurType(int eventCurType) {
        this.mEventCurType = eventCurType;
    }

    public int getEventCurStatus() {
        return this.mEventCurStatus;
    }

    public void setEventCurStatus(int eventCurStatus) {
        this.mEventCurStatus = eventCurStatus;
    }

    public int getEventCurAction() {
        return this.mEventCurAction;
    }

    public void setEventCurAction(int eventCurAction) {
        this.mEventCurAction = eventCurAction;
    }

    public int getEventTriggerStatus() {
        return this.mEventTriggerStatus;
    }

    public void setEventTriggerStatus(int eventTriggerStatus) {
        this.mEventTriggerStatus = eventTriggerStatus;
    }

    public long getEventTime() {
        return this.mEventTime;
    }

    public void setEventTime(long eventTime) {
        this.mEventTime = eventTime;
    }

    public int getEventConfidence() {
        return this.mEventConfidence;
    }

    public void setEventConfidence(int eventConfidence) {
        this.mEventConfidence = eventConfidence;
    }

    public long getEventSensorTime() {
        return this.mEventSensorTime;
    }

    public void setEventSensorTime(long eventSensorTime) {
        this.mEventSensorTime = eventSensorTime;
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "Event{mEventCurType = %d, mEventCurStatus = %d, mEventCurAction = %d, mEventTriggerStatus = %d, mEventTime = %d, mEventSensorTime = %d, mEventConfidence = %d}", Integer.valueOf(this.mEventCurType), Integer.valueOf(this.mEventCurStatus), Integer.valueOf(this.mEventCurAction), Integer.valueOf(this.mEventTriggerStatus), Long.valueOf(this.mEventTime), Long.valueOf(this.mEventSensorTime), Integer.valueOf(this.mEventConfidence));
    }
}
