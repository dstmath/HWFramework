package com.huawei.android.util;

import android.util.IMonitor;
import com.huawei.annotation.HwSystemApi;
import java.util.Date;

@HwSystemApi
public class IMonitorExt {

    public static class EventStreamExt {
        private IMonitor.EventStream mEventStream;

        public IMonitor.EventStream getEventStream() {
            return this.mEventStream;
        }

        public void setEventStream(IMonitor.EventStream eStream) {
            this.mEventStream = eStream;
        }

        public EventStreamExt setParam(String paramID, int value) {
            EventStreamExt eStreamext = new EventStreamExt();
            eStreamext.setEventStream(this.mEventStream.setParam(paramID, value));
            return eStreamext;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, int value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, long value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, float value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, String value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, boolean value) {
            this.mEventStream.setParam(paramID, Boolean.valueOf(value));
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, byte value) {
            this.mEventStream.setParam(paramID, value);
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, short paramID, EventStreamExt value) {
            if (value != null) {
                this.mEventStream.setParam(paramID, value.getEventStream());
            }
            return eStream;
        }

        public EventStreamExt setParam(EventStreamExt eStream, String param, Object value) {
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
            } else if (value instanceof EventStreamExt) {
                this.mEventStream.setParam(param, ((EventStreamExt) value).getEventStream());
            }
            return eStream;
        }

        public EventStreamExt fillArrayParam(EventStreamExt eStream, String param, Object value) {
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
            } else if (value instanceof EventStreamExt) {
                this.mEventStream.fillArrayParam(param, ((EventStreamExt) value).getEventStream());
            }
            return eStream;
        }

        public EventStreamExt addDynamicPath(EventStreamExt eStream, String path) {
            this.mEventStream.addDynamicPath(path);
            return eStream;
        }

        public EventStreamExt addAndDelDynamicPath(EventStreamExt eStream, String path) {
            this.mEventStream.addAndDelDynamicPath(path);
            return eStream;
        }
    }

    public static EventStreamExt openEventStream(int eventID) {
        IMonitor.EventStream eventStream = IMonitor.openEventStream(eventID);
        EventStreamExt eventStreamExt = new EventStreamExt();
        eventStreamExt.setEventStream(eventStream);
        return eventStreamExt;
    }

    public static void closeEventStream(EventStreamExt eStream) {
        IMonitor.closeEventStream(eStream.getEventStream());
    }

    public static boolean sendEvent(EventStreamExt eStream) {
        return IMonitor.sendEvent(eStream.getEventStream());
    }
}
