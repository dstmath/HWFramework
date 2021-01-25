package android.util;

import android.util.IMonitor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* access modifiers changed from: package-private */
public final class IMonitorEventStreamImpl extends IMonitor.EventStream {
    private long mEventHandle = 0;

    protected IMonitorEventStreamImpl(long eventHandle) {
        this.mEventHandle = eventHandle;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /* access modifiers changed from: protected */
    @Override // android.util.IMonitor.EventStream
    public long getHandle() {
        return this.mEventHandle;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, Boolean value) {
        setParam(paramID, value.booleanValue() ? 1 : 0);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, byte value) {
        setParam(paramID, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, short value) {
        setParam(paramID, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, int value) {
        setParam(paramID, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, long value) {
        if (isHandleValid()) {
            IMonitorNative.setParam(this.mEventHandle, paramID, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, float value) {
        if (isHandleValid()) {
            IMonitorNative.setParamFloat(this.mEventHandle, paramID, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, String value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.setParamString(this.mEventHandle, paramID, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, Date value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.format(value);
        setParam(paramID, sdf.format(value));
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(short paramID, IMonitor.EventStream value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.setParamClass(this.mEventHandle, paramID, value.getHandle());
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, Boolean value) {
        fillArrayParam(paramID, value.booleanValue() ? 1 : 0);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, byte value) {
        fillArrayParam(paramID, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, short value) {
        fillArrayParam(paramID, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, int value) {
        fillArrayParam(paramID, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, long value) {
        if (isHandleValid()) {
            IMonitorNative.fillArrayParam(this.mEventHandle, paramID, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, float value) {
        if (isHandleValid()) {
            IMonitorNative.fillArrayParamFloat(this.mEventHandle, paramID, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, String value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.fillArrayParamString(this.mEventHandle, paramID, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, Date value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.format(value);
        fillArrayParam(paramID, sdf.format(value));
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(short paramID, IMonitor.EventStream value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.fillArrayParamClass(this.mEventHandle, paramID, value.getHandle());
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, Boolean value) {
        setParam(param, value.booleanValue() ? 1 : 0);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, byte value) {
        setParam(param, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, short value) {
        setParam(param, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, int value) {
        setParam(param, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, long value) {
        if (isHandleValid()) {
            IMonitorNative.setParam(this.mEventHandle, param, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, float value) {
        if (isHandleValid()) {
            IMonitorNative.setParamFloat(this.mEventHandle, param, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, String value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.setParamString(this.mEventHandle, param, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, Date value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.format(value);
        setParam(param, sdf.format(value));
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setParam(String param, IMonitor.EventStream value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.setParamClass(this.mEventHandle, param, value.getHandle());
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, Boolean value) {
        fillArrayParam(param, value.booleanValue() ? 1 : 0);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, byte value) {
        fillArrayParam(param, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, short value) {
        fillArrayParam(param, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, int value) {
        fillArrayParam(param, (long) value);
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, long value) {
        if (isHandleValid()) {
            IMonitorNative.fillArrayParam(this.mEventHandle, param, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, float value) {
        if (isHandleValid()) {
            IMonitorNative.fillArrayParamFloat(this.mEventHandle, param, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, String value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.fillArrayParamString(this.mEventHandle, param, value);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, Date value) {
        if (value == null) {
            return this;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.format(value);
        fillArrayParam(param, sdf.format(value));
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream fillArrayParam(String param, IMonitor.EventStream value) {
        if (value != null && isHandleValid()) {
            IMonitorNative.fillArrayParamClass(this.mEventHandle, param, value.getHandle());
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream unsetParam(short paramID) {
        if (isHandleValid()) {
            IMonitorNative.unsetParam(this.mEventHandle, paramID);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream unsetParam(String param) {
        if (isHandleValid()) {
            IMonitorNative.unsetParam(this.mEventHandle, param);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream setTime(long milliSeconds) {
        if (isHandleValid()) {
            IMonitorNative.setTime(this.mEventHandle, milliSeconds / 1000);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream addDynamicPath(String path) {
        if (path != null && isHandleValid() && !path.isEmpty()) {
            IMonitorNative.addDynamicPath(this.mEventHandle, path);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public IMonitor.EventStream addAndDelDynamicPath(String path) {
        if (path != null && isHandleValid() && !path.isEmpty()) {
            IMonitorNative.addAndDelDynamicPath(this.mEventHandle, path);
        }
        return this;
    }

    @Override // android.util.IMonitor.EventStream
    public boolean commit() {
        if (isHandleValid()) {
            return IMonitorNative.sendEvent(this.mEventHandle);
        }
        return false;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        long j = this.mEventHandle;
        if (j != 0) {
            IMonitorNative.destoryEvent(j);
            this.mEventHandle = 0;
        }
    }

    private boolean isHandleValid() {
        return this.mEventHandle != 0;
    }
}
