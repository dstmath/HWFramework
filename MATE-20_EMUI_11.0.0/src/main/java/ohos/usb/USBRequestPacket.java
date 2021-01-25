package ohos.usb;

import android.annotation.UnsupportedAppUsage;
import android.hardware.usb.UsbEndpoint;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.common.LBSLog;

public class USBRequestPacket {
    private UsbRequestPacketAdapter usbRequestPacketAdapter = UsbRequestPacketAdapter.getInstance();

    public void abort() {
        this.usbRequestPacketAdapter.abort();
    }

    public void free() {
        this.usbRequestPacketAdapter.free();
    }

    public boolean initializeRequest(USBDevicePipe uSBDevicePipe, USBInterface uSBInterface, int i) {
        return this.usbRequestPacketAdapter.initializeRequest(uSBDevicePipe, uSBInterface, i);
    }

    public boolean queueBuffer(ByteBuffer byteBuffer) {
        return this.usbRequestPacketAdapter.queueBuffer(byteBuffer);
    }

    public boolean queueBuffer(ByteBuffer byteBuffer, int i) {
        return this.usbRequestPacketAdapter.queueBuffer(byteBuffer, i);
    }

    public void dequeue(boolean z) {
        this.usbRequestPacketAdapter.dequeue(z);
    }

    /* access modifiers changed from: private */
    public static class UsbRequestPacketAdapter {
        private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "UsbRequestPacketAdapter");
        private static final Object LOCK = new Object();
        static final int MAX_USBFS_BUFFER_SIZE = 16384;
        private static volatile UsbRequestPacketAdapter instance;
        @UnsupportedAppUsage
        private ByteBuffer mByteArr;
        private USBDevicePipe mDevicePipe;
        private UsbEndpoint mEPoint;
        private boolean mIsNewQueue;
        @UnsupportedAppUsage
        private int mLen;
        private ByteBuffer mLocalBuffer;
        @UnsupportedAppUsage
        private long mLocalContext;
        private Object mObjectData;

        private native boolean localCancel();

        private native void localClose();

        private native int localDequeueArray(byte[] bArr, int i, boolean z);

        private native int localDequeueDirect();

        private native boolean localInit(USBDevicePipe uSBDevicePipe, int i, int i2, int i3, int i4);

        private native boolean localQueue(ByteBuffer byteBuffer, int i, int i2);

        private native boolean localQueueArray(byte[] bArr, int i, boolean z);

        private native boolean localQueueDirect(ByteBuffer byteBuffer, int i, boolean z);

        private UsbRequestPacketAdapter() {
        }

        public static UsbRequestPacketAdapter getInstance() {
            UsbRequestPacketAdapter usbRequestPacketAdapter;
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new UsbRequestPacketAdapter();
                }
                usbRequestPacketAdapter = instance;
            }
            return usbRequestPacketAdapter;
        }

        public void abort() {
            localCancel();
        }

        public void free() {
            this.mEPoint = null;
            this.mDevicePipe = null;
            localClose();
        }

        public boolean initializeRequest(USBDevicePipe uSBDevicePipe, USBInterface uSBInterface, int i) {
            if (uSBDevicePipe == null || uSBInterface == null) {
                return false;
            }
            this.mDevicePipe = uSBDevicePipe;
            return localInit(uSBDevicePipe, uSBInterface.obtainEndpointAddress(i), uSBInterface.obtainEndpointAttributes(i), uSBInterface.obtainEndpointMaxPacketSize(i), uSBInterface.obtainEndpointInterval(i));
        }

        public boolean queueBuffer(ByteBuffer byteBuffer) {
            boolean z;
            boolean z2 = this.mEPoint.getDirection() == 0;
            synchronized (LOCK) {
                this.mByteArr = byteBuffer;
                if (byteBuffer == null) {
                    this.mIsNewQueue = true;
                    z = localQueue(null, 0, 0);
                } else {
                    if (!byteBuffer.isDirect()) {
                        this.mLocalBuffer = ByteBuffer.allocateDirect(this.mByteArr.remaining());
                        if (z2) {
                            this.mByteArr.mark();
                            this.mLocalBuffer.put(this.mByteArr);
                            this.mLocalBuffer.flip();
                            this.mByteArr.reset();
                        }
                        byteBuffer = this.mLocalBuffer;
                    }
                    this.mIsNewQueue = true;
                    z = localQueue(byteBuffer, byteBuffer.position(), byteBuffer.remaining());
                }
                if (!z) {
                    this.mIsNewQueue = false;
                    this.mLocalBuffer = null;
                    this.mByteArr = null;
                }
            }
            return z;
        }

        public boolean queueBuffer(ByteBuffer byteBuffer, int i) {
            boolean z;
            if (byteBuffer == null) {
                return false;
            }
            boolean z2 = this.mEPoint.getDirection() == 0;
            synchronized (LOCK) {
                this.mByteArr = byteBuffer;
                this.mLen = i;
                if (byteBuffer.isDirect()) {
                    z = localQueueDirect(byteBuffer, i, z2);
                } else if (byteBuffer.hasArray()) {
                    z = localQueueArray(byteBuffer.array(), i, z2);
                } else {
                    throw new IllegalArgumentException("buffer is not direct and has no array");
                }
                if (!z) {
                    this.mByteArr = null;
                    this.mLen = 0;
                }
            }
            return z;
        }

        /* access modifiers changed from: package-private */
        public void dequeue(boolean z) {
            int i;
            boolean z2 = this.mEPoint.getDirection() == 0;
            synchronized (LOCK) {
                if (this.mIsNewQueue) {
                    int localDequeueDirect = localDequeueDirect();
                    this.mIsNewQueue = false;
                    if (this.mByteArr == null) {
                        HiLog.info(LABEL, "mByteArr == null", new Object[0]);
                    } else if (this.mLocalBuffer == null) {
                        this.mByteArr.position(this.mByteArr.position() + localDequeueDirect);
                    } else {
                        this.mLocalBuffer.limit(localDequeueDirect);
                        if (z2) {
                            try {
                                this.mByteArr.position(this.mByteArr.position() + localDequeueDirect);
                            } catch (Throwable th) {
                                this.mLocalBuffer = null;
                                throw th;
                            }
                        } else {
                            this.mByteArr.put(this.mLocalBuffer);
                        }
                        this.mLocalBuffer = null;
                    }
                } else {
                    if (this.mByteArr.isDirect()) {
                        i = localDequeueDirect();
                    } else {
                        i = localDequeueArray(this.mByteArr.array(), this.mLen, z2);
                    }
                    if (i >= 0) {
                        if (i >= this.mLen) {
                            i = this.mLen;
                        }
                        try {
                            this.mByteArr.position(i);
                        } catch (IllegalArgumentException e) {
                            if (z) {
                                throw new BufferOverflowException();
                            }
                            throw e;
                        }
                    }
                }
                this.mByteArr = null;
                this.mLen = 0;
            }
        }
    }
}
