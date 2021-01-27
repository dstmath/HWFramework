package ohos.usb;

import java.io.FileDescriptor;
import java.util.concurrent.TimeoutException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.common.LBSLog;

public class USBDevicePipe {
    public final USBDevice localDevice;
    private UsbDevicePipeKitAdapter usbDevicePipeKitAdapter = UsbDevicePipeKitAdapter.getInstance();

    public USBDevicePipe(USBDevice uSBDevice) {
        this.localDevice = uSBDevice;
    }

    public int performBulkTransfer(USBInterface uSBInterface, int i, byte[] bArr, int i2, int i3) {
        return performBulkTransfer(uSBInterface, i, bArr, 0, i2, i3);
    }

    public int performBulkTransfer(USBInterface uSBInterface, int i, byte[] bArr, int i2, int i3, int i4) {
        return this.usbDevicePipeKitAdapter.performBulkTransfer(uSBInterface, i, bArr, i2, i3, i4);
    }

    public boolean claimAccessInterface(USBInterface uSBInterface, boolean z) {
        return this.usbDevicePipeKitAdapter.claimAccessInterface(uSBInterface, z);
    }

    public void close() {
        this.usbDevicePipeKitAdapter.close();
    }

    public int performControlTransfer(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6, int i7) {
        return this.usbDevicePipeKitAdapter.performControlTransfer(i, i2, i3, i4, bArr, i5, i6, i7);
    }

    public int performControlTransfer(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6) {
        return performControlTransfer(i, i2, i3, i4, bArr, 0, i5, i6);
    }

    public int obtainFileDescriptor() {
        return this.usbDevicePipeKitAdapter.obtainFileDescriptor();
    }

    public byte[] obtainRawDescriptors() {
        return this.usbDevicePipeKitAdapter.obtainRawDescriptors();
    }

    public String obtainSerial() {
        return this.usbDevicePipeKitAdapter.obtainSerial();
    }

    public boolean releaseAccessInterface(USBInterface uSBInterface) {
        return this.usbDevicePipeKitAdapter.releaseAccessInterface(uSBInterface);
    }

    public USBRequestPacket performRequestWait(long j) throws TimeoutException {
        return this.usbDevicePipeKitAdapter.performRequestWait(j);
    }

    public USBRequestPacket performRequestWait() {
        return this.usbDevicePipeKitAdapter.performRequestWait();
    }

    public boolean setDeviceConfiguration(USBDevice uSBDevice, int i) {
        return this.usbDevicePipeKitAdapter.setDeviceConfiguration(uSBDevice, i);
    }

    public boolean setDeviceInterface(USBInterface uSBInterface) {
        return this.usbDevicePipeKitAdapter.setDeviceInterface(uSBInterface);
    }

    public boolean openByInt(String str, int i) {
        return this.usbDevicePipeKitAdapter.openByInt(str, i);
    }

    /* access modifiers changed from: private */
    public static class UsbDevicePipeKitAdapter {
        private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "UsbDevicePipeKitAdapter");
        private static final Object LOCK = new Object();
        private static volatile UsbDevicePipeKitAdapter instance;
        private long mNativeContext;

        private native int localBulkRequest(int i, byte[] bArr, int i2, int i3, int i4);

        private native boolean localClaimInterface(int i, boolean z);

        private native void localClose();

        private native int localControlRequest(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6, int i7);

        private native byte[] localGetDesc();

        private native int localGetFd();

        private native String localGetSerial();

        private native boolean localOpen(String str, FileDescriptor fileDescriptor);

        private native boolean localOpenByInt(String str, int i);

        private native boolean localReleaseInterface(int i);

        private native USBRequestPacket localRequestWait(long j) throws TimeoutException;

        private native boolean localResetDevice();

        private native boolean localSetConfiguration(int i);

        private native boolean localSetInterface(int i, int i2);

        private UsbDevicePipeKitAdapter() {
        }

        public static UsbDevicePipeKitAdapter getInstance() {
            UsbDevicePipeKitAdapter usbDevicePipeKitAdapter;
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new UsbDevicePipeKitAdapter();
                }
                usbDevicePipeKitAdapter = instance;
            }
            return usbDevicePipeKitAdapter;
        }

        public boolean open(String str, FileDescriptor fileDescriptor) {
            return localOpen(str, fileDescriptor);
        }

        public boolean openByInt(String str, int i) {
            return localOpenByInt(str, i);
        }

        private static void checkBulkTransferBounds(USBInterface uSBInterface, int i, byte[] bArr, int i2, int i3, int i4) {
            int length = bArr != null ? bArr.length : 0;
            if (i3 < 0 || i3 > length) {
                throw new IllegalArgumentException("Buffer start or length out of bounds.");
            } else if (i4 < 0 || i2 < 0 || uSBInterface == null || i <= 0) {
                throw new IllegalArgumentException("interfaceObj or other parameter out of bounds.");
            }
        }

        private static void checkControlTransferBounds(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6, int i7) {
            int length = bArr != null ? bArr.length : 0;
            if (i6 < 0 || i6 > length) {
                throw new IllegalArgumentException("Buffer start or length out of bounds.");
            } else if (i < 0 || i2 < 0 || i3 < 0 || i4 < 0 || i7 < 0 || i5 < 0) {
                throw new IllegalArgumentException("requestType or other parameter out of bounds.");
            }
        }

        public int performBulkTransfer(USBInterface uSBInterface, int i, byte[] bArr, int i2, int i3) {
            return performBulkTransfer(uSBInterface, i, bArr, 0, i2, i3);
        }

        public int performBulkTransfer(USBInterface uSBInterface, int i, byte[] bArr, int i2, int i3, int i4) {
            checkBulkTransferBounds(uSBInterface, i, bArr, i2, i3, i4);
            return localBulkRequest(uSBInterface.obtainEndpointAddress(i), bArr, i2, i3, i4);
        }

        public boolean claimAccessInterface(USBInterface uSBInterface, boolean z) {
            if (uSBInterface == null) {
                return false;
            }
            return localClaimInterface(uSBInterface.obtainInterfaceId(), z);
        }

        public void close() {
            if (this.mNativeContext != 0) {
                localClose();
            }
        }

        public int performControlTransfer(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6, int i7) {
            checkControlTransferBounds(i, i2, i3, i4, bArr, i5, i6, i7);
            return localControlRequest(i, i2, i3, i4, bArr, i5, i6, i7);
        }

        public int performControlTransfer(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6) {
            return performControlTransfer(i, i2, i3, i4, bArr, 0, i5, i6);
        }

        public int obtainFileDescriptor() {
            return localGetFd();
        }

        public byte[] obtainRawDescriptors() {
            return localGetDesc();
        }

        public String obtainSerial() {
            return localGetSerial();
        }

        public boolean releaseAccessInterface(USBInterface uSBInterface) {
            return localReleaseInterface(uSBInterface.obtainInterfaceId());
        }

        public USBRequestPacket performRequestWait(long j) throws TimeoutException {
            USBRequestPacket localRequestWait = localRequestWait(j);
            if (localRequestWait != null) {
                localRequestWait.dequeue(true);
            }
            return localRequestWait;
        }

        public USBRequestPacket performRequestWait() {
            USBRequestPacket uSBRequestPacket;
            try {
                uSBRequestPacket = localRequestWait(-1);
            } catch (TimeoutException unused) {
                HiLog.info(LABEL, "performRequestWait error!", new Object[0]);
                uSBRequestPacket = null;
            }
            if (uSBRequestPacket != null) {
                uSBRequestPacket.dequeue(true);
            }
            return uSBRequestPacket;
        }

        public boolean setDeviceConfiguration(USBDevice uSBDevice, int i) {
            return localSetConfiguration(i);
        }

        public boolean setDeviceInterface(USBInterface uSBInterface) {
            return localSetInterface(uSBInterface.obtainInterfaceId(), uSBInterface.obtainAlternateSetting());
        }
    }
}
