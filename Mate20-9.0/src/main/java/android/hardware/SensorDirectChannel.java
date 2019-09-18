package android.hardware;

import android.os.MemoryFile;
import dalvik.system.CloseGuard;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.channels.Channel;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SensorDirectChannel implements Channel {
    public static final int RATE_FAST = 2;
    public static final int RATE_NORMAL = 1;
    public static final int RATE_STOP = 0;
    public static final int RATE_VERY_FAST = 3;
    public static final int TYPE_HARDWARE_BUFFER = 2;
    public static final int TYPE_MEMORY_FILE = 1;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final SensorManager mManager;
    private final int mNativeHandle;
    private final long mSize;
    private final int mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface MemoryType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RateLevel {
    }

    public boolean isOpen() {
        return !this.mClosed.get();
    }

    @Deprecated
    public boolean isValid() {
        return isOpen();
    }

    public void close() {
        if (this.mClosed.compareAndSet(false, true)) {
            this.mCloseGuard.close();
            this.mManager.destroyDirectChannel(this);
        }
    }

    public int configure(Sensor sensor, int rateLevel) {
        return this.mManager.configureDirectChannelImpl(this, sensor, rateLevel);
    }

    SensorDirectChannel(SensorManager manager, int id, int type, long size) {
        this.mManager = manager;
        this.mNativeHandle = id;
        this.mType = type;
        this.mSize = size;
        this.mCloseGuard.open("SensorDirectChannel");
    }

    /* access modifiers changed from: package-private */
    public int getNativeHandle() {
        return this.mNativeHandle;
    }

    static long[] encodeData(MemoryFile ashmem) {
        int fd;
        try {
            fd = ashmem.getFileDescriptor().getInt$();
        } catch (IOException e) {
            fd = -1;
        }
        return new long[]{1, 0, (long) fd};
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }
}
