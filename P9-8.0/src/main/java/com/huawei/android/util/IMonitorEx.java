package com.huawei.android.util;

import android.util.IMonitor;
import android.util.IMonitor.EventStream;

public class IMonitorEx {

    public static class EventStreamEx {
        private EventStream mEventStream;

        /* synthetic */ EventStreamEx(EventStream eStream, EventStreamEx -this1) {
            this(eStream);
        }

        private EventStreamEx(EventStream eStream) {
            this.mEventStream = eStream;
        }

        protected EventStream getEventStream() {
            return this.mEventStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramID, int value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramID, long value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramID, String value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }
    }

    public static EventStreamEx openEventStream(int eventID) {
        return new EventStreamEx(IMonitor.openEventStream(eventID), null);
    }

    public static void closeEventStream(EventStreamEx eStream) {
        IMonitor.closeEventStream(eStream.getEventStream());
    }

    public static boolean sendEvent(EventStreamEx eStream) {
        return IMonitor.sendEvent(eStream.getEventStream());
    }
}
