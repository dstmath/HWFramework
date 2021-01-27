package android.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;

public class IMonitor {
    public static final String TAG = "IMonitor";

    public static abstract class EventStream implements Closeable, Cloneable {
        public abstract EventStream addAndDelDynamicPath(String str);

        public abstract EventStream addDynamicPath(String str);

        public abstract boolean commit();

        public abstract EventStream fillArrayParam(String str, byte b);

        public abstract EventStream fillArrayParam(String str, float f);

        public abstract EventStream fillArrayParam(String str, int i);

        public abstract EventStream fillArrayParam(String str, long j);

        public abstract EventStream fillArrayParam(String str, EventStream eventStream);

        public abstract EventStream fillArrayParam(String str, Boolean bool);

        public abstract EventStream fillArrayParam(String str, String str2);

        public abstract EventStream fillArrayParam(String str, Date date);

        public abstract EventStream fillArrayParam(String str, short s);

        public abstract EventStream fillArrayParam(short s, byte b);

        public abstract EventStream fillArrayParam(short s, float f);

        public abstract EventStream fillArrayParam(short s, int i);

        public abstract EventStream fillArrayParam(short s, long j);

        public abstract EventStream fillArrayParam(short s, EventStream eventStream);

        public abstract EventStream fillArrayParam(short s, Boolean bool);

        public abstract EventStream fillArrayParam(short s, String str);

        public abstract EventStream fillArrayParam(short s, Date date);

        public abstract EventStream fillArrayParam(short s, short s2);

        /* access modifiers changed from: protected */
        public abstract long getHandle();

        public abstract EventStream setParam(String str, byte b);

        public abstract EventStream setParam(String str, float f);

        public abstract EventStream setParam(String str, int i);

        public abstract EventStream setParam(String str, long j);

        public abstract EventStream setParam(String str, EventStream eventStream);

        public abstract EventStream setParam(String str, Boolean bool);

        public abstract EventStream setParam(String str, String str2);

        public abstract EventStream setParam(String str, Date date);

        public abstract EventStream setParam(String str, short s);

        public abstract EventStream setParam(short s, byte b);

        public abstract EventStream setParam(short s, float f);

        public abstract EventStream setParam(short s, int i);

        public abstract EventStream setParam(short s, long j);

        public abstract EventStream setParam(short s, EventStream eventStream);

        public abstract EventStream setParam(short s, Boolean bool);

        public abstract EventStream setParam(short s, String str);

        public abstract EventStream setParam(short s, Date date);

        public abstract EventStream setParam(short s, short s2);

        public abstract EventStream setTime(long j);

        public abstract EventStream unsetParam(String str);

        public abstract EventStream unsetParam(short s);

        protected EventStream() {
        }
    }

    public static EventStream openEventStream(int eventID) {
        long handle = 0;
        try {
            handle = IMonitorNative.createEvent(eventID);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "openEventStream failed for no implementation of native");
        }
        return new IMonitorEventStreamImpl(handle);
    }

    public static void closeEventStream(EventStream eventStream) {
        if (eventStream != null) {
            try {
                eventStream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeEventStream IOException");
            }
        }
    }

    public static boolean sendEvent(EventStream eventStream) {
        if (eventStream == null) {
            return false;
        }
        return eventStream.commit();
    }
}
