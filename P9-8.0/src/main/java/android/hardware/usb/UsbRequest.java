package android.hardware.usb;

import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.nio.ByteBuffer;

public class UsbRequest {
    private static final int MAX_USBFS_BUFFER_SIZE = 16384;
    private static final String TAG = "UsbRequest";
    private ByteBuffer mBuffer;
    private Object mClientData;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpoint;
    private boolean mIsUsingNewQueue;
    private int mLength;
    private final Object mLock = new Object();
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

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
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

    public boolean queue(ByteBuffer buffer) {
        boolean z;
        boolean wasQueued;
        boolean z2 = true;
        if (this.mNativeContext != 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkState(z, "request is not initialized");
        Preconditions.checkState(this.mIsUsingNewQueue ^ 1, "this request is currently queued");
        boolean isSend = this.mEndpoint.getDirection() == 0;
        synchronized (this.mLock) {
            this.mBuffer = buffer;
            if (buffer == null) {
                this.mIsUsingNewQueue = true;
                wasQueued = native_queue(null, 0, 0);
            } else {
                Preconditions.checkArgumentInRange(buffer.remaining(), 0, 16384, "number of remaining bytes");
                if (buffer.isReadOnly()) {
                    z2 = isSend;
                }
                Preconditions.checkArgument(z2, "buffer can not be read-only when receiving data");
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
        if (!wasQueued) {
            this.mIsUsingNewQueue = false;
            this.mTempBuffer = null;
            this.mBuffer = null;
        }
        return wasQueued;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.hardware.usb.UsbRequest.dequeue(boolean):void, dom blocks: [B:24:0x0040, B:41:0x0071]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    void dequeue(boolean r9) {
        /*
        r8 = this;
        r4 = r8.mEndpoint;
        r4 = r4.getDirection();
        if (r4 != 0) goto L_0x0023;
    L_0x0008:
        r3 = 1;
    L_0x0009:
        r5 = r8.mLock;
        monitor-enter(r5);
        r4 = r8.mIsUsingNewQueue;	 Catch:{ all -> 0x0036 }
        if (r4 == 0) goto L_0x005d;	 Catch:{ all -> 0x0036 }
    L_0x0010:
        r1 = r8.native_dequeue_direct();	 Catch:{ all -> 0x0036 }
        r4 = 0;	 Catch:{ all -> 0x0036 }
        r8.mIsUsingNewQueue = r4;	 Catch:{ all -> 0x0036 }
        r4 = r8.mBuffer;	 Catch:{ all -> 0x0036 }
        if (r4 != 0) goto L_0x0025;	 Catch:{ all -> 0x0036 }
    L_0x001b:
        r4 = 0;	 Catch:{ all -> 0x0036 }
        r8.mBuffer = r4;	 Catch:{ all -> 0x0036 }
        r4 = 0;	 Catch:{ all -> 0x0036 }
        r8.mLength = r4;	 Catch:{ all -> 0x0036 }
        monitor-exit(r5);
        return;
    L_0x0023:
        r3 = 0;
        goto L_0x0009;
    L_0x0025:
        r4 = r8.mTempBuffer;	 Catch:{ all -> 0x0036 }
        if (r4 != 0) goto L_0x0039;	 Catch:{ all -> 0x0036 }
    L_0x0029:
        r4 = r8.mBuffer;	 Catch:{ all -> 0x0036 }
        r6 = r8.mBuffer;	 Catch:{ all -> 0x0036 }
        r6 = r6.position();	 Catch:{ all -> 0x0036 }
        r6 = r6 + r1;	 Catch:{ all -> 0x0036 }
        r4.position(r6);	 Catch:{ all -> 0x0036 }
        goto L_0x001b;
    L_0x0036:
        r4 = move-exception;
        monitor-exit(r5);
        throw r4;
    L_0x0039:
        r4 = r8.mTempBuffer;	 Catch:{ all -> 0x0036 }
        r4.limit(r1);	 Catch:{ all -> 0x0036 }
        if (r3 == 0) goto L_0x0050;
    L_0x0040:
        r4 = r8.mBuffer;	 Catch:{ all -> 0x0058 }
        r6 = r8.mBuffer;	 Catch:{ all -> 0x0058 }
        r6 = r6.position();	 Catch:{ all -> 0x0058 }
        r6 = r6 + r1;	 Catch:{ all -> 0x0058 }
        r4.position(r6);	 Catch:{ all -> 0x0058 }
    L_0x004c:
        r4 = 0;
        r8.mTempBuffer = r4;	 Catch:{ all -> 0x0036 }
        goto L_0x001b;
    L_0x0050:
        r4 = r8.mBuffer;	 Catch:{ all -> 0x0058 }
        r6 = r8.mTempBuffer;	 Catch:{ all -> 0x0058 }
        r4.put(r6);	 Catch:{ all -> 0x0058 }
        goto L_0x004c;
    L_0x0058:
        r4 = move-exception;
        r6 = 0;
        r8.mTempBuffer = r6;	 Catch:{ all -> 0x0036 }
        throw r4;	 Catch:{ all -> 0x0036 }
    L_0x005d:
        r4 = r8.mBuffer;	 Catch:{ all -> 0x0036 }
        r4 = r4.isDirect();	 Catch:{ all -> 0x0036 }
        if (r4 == 0) goto L_0x00ae;	 Catch:{ all -> 0x0036 }
    L_0x0065:
        r1 = r8.native_dequeue_direct();	 Catch:{ all -> 0x0036 }
    L_0x0069:
        if (r1 < 0) goto L_0x001b;	 Catch:{ all -> 0x0036 }
    L_0x006b:
        r4 = r8.mLength;	 Catch:{ all -> 0x0036 }
        r0 = java.lang.Math.min(r1, r4);	 Catch:{ all -> 0x0036 }
        r4 = r8.mBuffer;	 Catch:{ IllegalArgumentException -> 0x0077 }
        r4.position(r0);	 Catch:{ IllegalArgumentException -> 0x0077 }
        goto L_0x001b;
    L_0x0077:
        r2 = move-exception;
        if (r9 == 0) goto L_0x00bb;
    L_0x007a:
        r4 = "UsbRequest";	 Catch:{ all -> 0x0036 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0036 }
        r6.<init>();	 Catch:{ all -> 0x0036 }
        r7 = "Buffer ";	 Catch:{ all -> 0x0036 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0036 }
        r7 = r8.mBuffer;	 Catch:{ all -> 0x0036 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0036 }
        r7 = " does not have enough space to read ";	 Catch:{ all -> 0x0036 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0036 }
        r6 = r6.append(r0);	 Catch:{ all -> 0x0036 }
        r7 = " bytes";	 Catch:{ all -> 0x0036 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0036 }
        r6 = r6.toString();	 Catch:{ all -> 0x0036 }
        android.util.Log.e(r4, r6, r2);	 Catch:{ all -> 0x0036 }
        r4 = new java.nio.BufferOverflowException;	 Catch:{ all -> 0x0036 }
        r4.<init>();	 Catch:{ all -> 0x0036 }
        throw r4;	 Catch:{ all -> 0x0036 }
    L_0x00ae:
        r4 = r8.mBuffer;	 Catch:{ all -> 0x0036 }
        r4 = r4.array();	 Catch:{ all -> 0x0036 }
        r6 = r8.mLength;	 Catch:{ all -> 0x0036 }
        r1 = r8.native_dequeue_array(r4, r6, r3);	 Catch:{ all -> 0x0036 }
        goto L_0x0069;	 Catch:{ all -> 0x0036 }
    L_0x00bb:
        throw r2;	 Catch:{ all -> 0x0036 }
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbRequest.dequeue(boolean):void");
    }

    public boolean cancel() {
        return native_cancel();
    }
}
