package com.huawei.hiai.awareness.awareness;

public class Event {
    private int eventConfidence;
    private int eventCurAction;
    private int eventCurStatus;
    private int eventCurType;
    private long eventSensorTime;
    private long eventTime;
    private int eventTriggerStatus = -1;

    public int getEventCurType() {
        return this.eventCurType;
    }

    public void setEventCurType(int eventCurType2) {
        this.eventCurType = eventCurType2;
    }

    public int getEventCurStatus() {
        return this.eventCurStatus;
    }

    public void setEventCurStatus(int eventCurStatus2) {
        this.eventCurStatus = eventCurStatus2;
    }

    public int getEventCurAction() {
        return this.eventCurAction;
    }

    public void setEventCurAction(int eventCurAction2) {
        this.eventCurAction = eventCurAction2;
    }

    public int getEventTriggerStatus() {
        return this.eventTriggerStatus;
    }

    public void setEventTriggerStatus(int eventTriggerStatus2) {
        this.eventTriggerStatus = eventTriggerStatus2;
    }

    public long getEventTime() {
        return this.eventTime;
    }

    public void setEventTime(long eventTime2) {
        this.eventTime = eventTime2;
    }

    public int getEventConfidence() {
        return this.eventConfidence;
    }

    public void setEventConfidence(int eventConfidence2) {
        this.eventConfidence = eventConfidence2;
    }

    public long getEventSensorTime() {
        return this.eventSensorTime;
    }

    public void setEventSensorTime(long eventSensorTime2) {
        this.eventSensorTime = eventSensorTime2;
    }

    public String toString() {
        return String.format("Event{eventCurType = %d, eventCurStatus = %d, eventCurAction = %d, triggerStatus = %d, time = %d, sensorTime = %d, confidence = %d}", new Object[]{Integer.valueOf(this.eventCurType), Integer.valueOf(this.eventCurStatus), Integer.valueOf(this.eventCurAction), Integer.valueOf(this.eventTriggerStatus), Long.valueOf(this.eventTime), Long.valueOf(this.eventSensorTime), Integer.valueOf(this.eventConfidence)});
    }
}
