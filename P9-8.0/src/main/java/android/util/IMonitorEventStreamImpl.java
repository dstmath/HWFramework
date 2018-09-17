package android.util;

import android.util.IMonitor.EventStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class IMonitorEventStreamImpl extends EventStream {
    private long mEventHandle = 0;

    protected IMonitorEventStreamImpl(long eventHandle) {
        this.mEventHandle = eventHandle;
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    protected long getHandle() {
        return this.mEventHandle;
    }

    public EventStream setParam(short paramID, Boolean value) {
        setParam(paramID, value.booleanValue() ? 1 : 0);
        return this;
    }

    public EventStream setParam(short paramID, byte value) {
        setParam(paramID, (long) value);
        return this;
    }

    public EventStream setParam(short paramID, short value) {
        setParam(paramID, (long) value);
        return this;
    }

    public EventStream setParam(short paramID, int value) {
        setParam(paramID, (long) value);
        return this;
    }

    public EventStream setParam(short paramID, long value) {
        if (isHandleValid()) {
            IMonitorNative.setParam(this.mEventHandle, paramID, value);
        }
        return this;
    }

    public EventStream setParam(short paramID, float value) {
        if (isHandleValid()) {
            IMonitorNative.setParamFloat(this.mEventHandle, paramID, value);
        }
        return this;
    }

    public EventStream setParam(short paramID, String value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.setParamString(this.mEventHandle, paramID, value);
        }
        return this;
    }

    public EventStream setParam(short paramID, Date value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.format(value);
        setParam(paramID, sdf.format(value));
        return this;
    }

    public EventStream setParam(short paramID, EventStream value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.setParam(this.mEventHandle, paramID, value.getHandle());
        }
        return this;
    }

    public EventStream fillArrayParam(short paramID, Boolean value) {
        fillArrayParam(paramID, value.booleanValue() ? 1 : 0);
        return this;
    }

    public EventStream fillArrayParam(short paramID, byte value) {
        fillArrayParam(paramID, (long) value);
        return this;
    }

    public EventStream fillArrayParam(short paramID, short value) {
        fillArrayParam(paramID, (long) value);
        return this;
    }

    public EventStream fillArrayParam(short paramID, int value) {
        fillArrayParam(paramID, (long) value);
        return this;
    }

    public EventStream fillArrayParam(short paramID, long value) {
        if (isHandleValid()) {
            IMonitorNative.fillArrayParam(this.mEventHandle, paramID, value);
        }
        return this;
    }

    public EventStream fillArrayParam(short paramID, float value) {
        if (isHandleValid()) {
            IMonitorNative.fillArrayParamFloat(this.mEventHandle, paramID, value);
        }
        return this;
    }

    public EventStream fillArrayParam(short paramID, String value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.fillArrayParamString(this.mEventHandle, paramID, value);
        }
        return this;
    }

    public EventStream fillArrayParam(short paramID, Date value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.format(value);
        fillArrayParam(paramID, sdf.format(value));
        return this;
    }

    public EventStream fillArrayParam(short paramID, EventStream value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.fillArrayParam(this.mEventHandle, paramID, value.getHandle());
        }
        return this;
    }

    public EventStream unsetParam(short paramID) {
        if (isHandleValid()) {
            IMonitorNative.unsetParam(this.mEventHandle, paramID);
        }
        return this;
    }

    public EventStream setTime(long milliSeconds) {
        if (isHandleValid()) {
            IMonitorNative.setTime(this.mEventHandle, milliSeconds / 1000);
        }
        return this;
    }

    public EventStream addDynamicPath(String path) {
        if (!(path == null || !isHandleValid() || (path.isEmpty() ^ 1) == 0)) {
            IMonitorNative.addDynamicPath(this.mEventHandle, path);
        }
        return this;
    }

    public EventStream addAndDelDynamicPath(String path) {
        if (!(path == null || !isHandleValid() || (path.isEmpty() ^ 1) == 0)) {
            IMonitorNative.addAndDelDynamicPath(this.mEventHandle, path);
        }
        return this;
    }

    public boolean commit() {
        if (isHandleValid()) {
            return IMonitorNative.sendEvent(this.mEventHandle);
        }
        return false;
    }

    public void close() throws IOException {
        if (this.mEventHandle != 0) {
            IMonitorNative.destoryEvent(this.mEventHandle);
            this.mEventHandle = 0;
        }
    }

    private boolean isHandleValid() {
        return 0 != this.mEventHandle;
    }
}
