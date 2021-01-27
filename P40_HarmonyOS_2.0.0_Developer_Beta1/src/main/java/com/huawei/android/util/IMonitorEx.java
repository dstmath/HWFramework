package com.huawei.android.util;

import android.util.IMonitor;
import java.util.Date;

public class IMonitorEx {

    public static class EventStreamEx {
        private IMonitor.EventStream mEventStream;

        private EventStreamEx(IMonitor.EventStream eStream) {
            this.mEventStream = eStream;
        }

        /* access modifiers changed from: protected */
        public IMonitor.EventStream getEventStream() {
            return this.mEventStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramId, int value) {
            this.mEventStream.setParam(paramId, value);
            return eStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramId, long value) {
            this.mEventStream.setParam(paramId, value);
            return eStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramId, float value) {
            this.mEventStream.setParam(paramId, value);
            return eStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, short paramId, String value) {
            this.mEventStream.setParam(paramId, value);
            return eStream;
        }

        public EventStreamEx setParam(EventStreamEx eStream, String param, Object value) {
            if (value instanceof Integer) {
                this.mEventStream.setParam(param, ((Integer) value).intValue());
            } else if (value instanceof Long) {
                this.mEventStream.setParam(param, ((Long) value).longValue());
            } else if (value instanceof String) {
                this.mEventStream.setParam(param, (String) value);
            } else if (value instanceof Float) {
                this.mEventStream.setParam(param, ((Float) value).floatValue());
            } else if (value instanceof Date) {
                this.mEventStream.setParam(param, (Date) value);
            } else if (value instanceof Short) {
                this.mEventStream.setParam(param, ((Short) value).shortValue());
            } else if (value instanceof Boolean) {
                this.mEventStream.setParam(param, (Boolean) value);
            } else if (value instanceof Byte) {
                this.mEventStream.setParam(param, ((Byte) value).byteValue());
            } else if (value instanceof EventStreamEx) {
                this.mEventStream.setParam(param, ((EventStreamEx) value).getEventStream());
            }
            return eStream;
        }

        public EventStreamEx fillArrayParam(EventStreamEx eStream, String param, Object value) {
            if (value instanceof Integer) {
                this.mEventStream.fillArrayParam(param, ((Integer) value).intValue());
            } else if (value instanceof Long) {
                this.mEventStream.fillArrayParam(param, ((Long) value).longValue());
            } else if (value instanceof Boolean) {
                this.mEventStream.fillArrayParam(param, (Boolean) value);
            } else if (value instanceof Byte) {
                this.mEventStream.fillArrayParam(param, ((Byte) value).byteValue());
            } else if (value instanceof Short) {
                this.mEventStream.fillArrayParam(param, ((Short) value).shortValue());
            } else if (value instanceof String) {
                this.mEventStream.fillArrayParam(param, (String) value);
            } else if (value instanceof EventStreamEx) {
                this.mEventStream.fillArrayParam(param, ((EventStreamEx) value).getEventStream());
            }
            return eStream;
        }

        public EventStreamEx addDynamicPath(EventStreamEx eStream, String path) {
            this.mEventStream.addDynamicPath(path);
            return eStream;
        }

        public EventStreamEx addAndDelDynamicPath(EventStreamEx eStream, String path) {
            this.mEventStream.addAndDelDynamicPath(path);
            return eStream;
        }
    }

    public static EventStreamEx openEventStream(int eventId) {
        return new EventStreamEx(IMonitor.openEventStream(eventId));
    }

    public static void closeEventStream(EventStreamEx eStream) {
        IMonitor.closeEventStream(eStream.getEventStream());
    }

    public static boolean sendEvent(EventStreamEx eStream) {
        return IMonitor.sendEvent(eStream.getEventStream());
    }
}
