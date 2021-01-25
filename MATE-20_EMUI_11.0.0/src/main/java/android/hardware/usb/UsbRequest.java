package android.hardware.usb;

import android.annotation.UnsupportedAppUsage;
import android.util.Log;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class UsbRequest {
    static final int MAX_USBFS_BUFFER_SIZE = 16384;
    private static final String TAG = "UsbRequest";
    @UnsupportedAppUsage
    private ByteBuffer mBuffer;
    private Object mClientData;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpoint;
    private boolean mIsUsingNewQueue;
    @UnsupportedAppUsage
    private int mLength;
    private final Object mLock = new Object();
    @UnsupportedAppUsage
    private long mNativeContext;
    private ByteBuffer mTempBuffer;

    private native boolean native_cancel();

    private native void native_close();

    private native int native_dequeue_array(byte[] bArr, int i, boolean z);

    private native int native_dequeue_direct();

    private native boolean native_init(UsbDeviceConnection usbDeviceConnection, int i, int i2, int i3, int i4);

    private native boolean native_queue(ByteBuffer byteBuffer, int i, int i2);

    private native boolean native_queue_array(byte[] bArr, int i, boolean z);

    private native boolean native_queue_direct(ByteBuffer byteBuffer, int i, boolean z);

    public boolean initialize(UsbDeviceConnection connection, UsbEndpoint endpoint) {
        this.mEndpoint = endpoint;
        this.mConnection = (UsbDeviceConnection) Preconditions.checkNotNull(connection, "connection");
        boolean wasInitialized = native_init(connection, endpoint.getAddress(), endpoint.getAttributes(), endpoint.getMaxPacketSize(), endpoint.getInterval());
        if (wasInitialized) {
            this.mCloseGuard.open("close");
        }
        return wasInitialized;
    }

    public void close() {
        if (this.mNativeContext != 0) {
            this.mEndpoint = null;
            this.mConnection = null;
            native_close();
            this.mCloseGuard.close();
        }
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

    public UsbEndpoint getEndpoint() {
        return this.mEndpoint;
    }

    public Object getClientData() {
        return this.mClientData;
    }

    public void setClientData(Object data) {
        this.mClientData = data;
    }

    @Deprecated
    public boolean queue(ByteBuffer buffer, int length) {
        boolean result;
        boolean out = this.mEndpoint.getDirection() == 0;
        if (this.mConnection.getContext().getApplicationInfo().targetSdkVersion < 28 && length > 16384) {
            length = 16384;
        }
        synchronized (this.mLock) {
            this.mBuffer = buffer;
            this.mLength = length;
            if (buffer.isDirect()) {
                result = native_queue_direct(buffer, length, out);
            } else if (buffer.hasArray()) {
                result = native_queue_array(buffer.array(), length, out);
            } else {
                throw new IllegalArgumentException("buffer is not direct and has no array");
            }
            if (!result) {
                this.mBuffer = null;
                this.mLength = 0;
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0068  */
    public boolean queue(ByteBuffer buffer) {
        boolean wasQueued;
        boolean z;
        Preconditions.checkState(this.mNativeContext != 0, "request is not initialized");
        Preconditions.checkState(!this.mIsUsingNewQueue, "this request is currently queued");
        boolean isSend = this.mEndpoint.getDirection() == 0;
        synchronized (this.mLock) {
            this.mBuffer = buffer;
            if (buffer == null) {
                this.mIsUsingNewQueue = true;
                wasQueued = native_queue(null, 0, 0);
            } else {
                if (this.mConnection.getContext().getApplicationInfo().targetSdkVersion < 28) {
                    Preconditions.checkArgumentInRange(buffer.remaining(), 0, 16384, "number of remaining bytes");
                }
                if (buffer.isReadOnly()) {
                    if (!isSend) {
                        z = false;
                        Preconditions.checkArgument(z, "buffer can not be read-only when receiving data");
                        if (!buffer.isDirect()) {
                            this.mTempBuffer = ByteBuffer.allocateDirect(this.mBuffer.remaining());
                            if (isSend) {
                                this.mBuffer.mark();
                                this.mTempBuffer.put(this.mBuffer);
                                this.mTempBuffer.flip();
                                this.mBuffer.reset();
                            }
                            buffer = this.mTempBuffer;
                        }
                        this.mIsUsingNewQueue = true;
                        wasQueued = native_queue(buffer, buffer.position(), buffer.remaining());
                    }
                }
                z = true;
                Preconditions.checkArgument(z, "buffer can not be read-only when receiving data");
                if (!buffer.isDirect()) {
                }
                this.mIsUsingNewQueue = true;
                wasQueued = native_queue(buffer, buffer.position(), buffer.remaining());
            }
        }
        if (!wasQueued) {
            this.mIsUsingNewQueue = false;
            this.mTempBuffer = null;
            this.mBuffer = null;
        }
        return wasQueued;
    }

    /* access modifiers changed from: package-private */
    public void dequeue(boolean useBufferOverflowInsteadOfIllegalArg) {
        int bytesTransferred;
        boolean isSend = this.mEndpoint.getDirection() == 0;
        synchronized (this.mLock) {
            if (this.mIsUsingNewQueue) {
                int bytesTransferred2 = native_dequeue_direct();
                this.mIsUsingNewQueue = false;
                if (this.mBuffer != null) {
                    if (this.mTempBuffer == null) {
                        this.mBuffer.position(this.mBuffer.position() + bytesTransferred2);
                    } else {
                        this.mTempBuffer.limit(bytesTransferred2);
                        if (isSend) {
                            try {
                                this.mBuffer.position(this.mBuffer.position() + bytesTransferred2);
                            } catch (Throwable th) {
                                this.mTempBuffer = null;
                                throw th;
                            }
                        } else {
                            this.mBuffer.put(this.mTempBuffer);
                        }
                        this.mTempBuffer = null;
                    }
                }
            } else {
                if (this.mBuffer.isDirect()) {
                    bytesTransferred = native_dequeue_direct();
                } else {
                    bytesTransferred = native_dequeue_array(this.mBuffer.array(), this.mLength, isSend);
                }
                if (bytesTransferred >= 0) {
                    int bytesToStore = Math.min(bytesTransferred, this.mLength);
                    try {
                        this.mBuffer.position(bytesToStore);
                    } catch (IllegalArgumentException e) {
                        if (useBufferOverflowInsteadOfIllegalArg) {
                            Log.e(TAG, "Buffer " + this.mBuffer + " does not have enough space to read " + bytesToStore + " bytes", e);
                            throw new BufferOverflowException();
                        }
                        throw e;
                    }
                }
            }
            this.mBuffer = null;
            this.mLength = 0;
        }
    }

    public boolean cancel() {
        return native_cancel();
    }
}
