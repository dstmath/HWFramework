package android.hardware.usb;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.util.concurrent.TimeoutException;

public class UsbDeviceConnection {
    private static final String TAG = "UsbDeviceConnection";
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private Context mContext;
    private final UsbDevice mDevice;
    private long mNativeContext;

    private native int native_bulk_request(int i, byte[] bArr, int i2, int i3, int i4);

    private native boolean native_claim_interface(int i, boolean z);

    private native void native_close();

    private native int native_control_request(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6, int i7);

    private native byte[] native_get_desc();

    private native int native_get_fd();

    private native String native_get_serial();

    private native boolean native_open(String str, FileDescriptor fileDescriptor);

    private native boolean native_release_interface(int i);

    private native UsbRequest native_request_wait(long j) throws TimeoutException;

    private native boolean native_reset_device();

    private native boolean native_set_configuration(int i);

    private native boolean native_set_interface(int i, int i2);

    public UsbDeviceConnection(UsbDevice device) {
        this.mDevice = device;
    }

    boolean open(String name, ParcelFileDescriptor pfd, Context context) {
        this.mContext = context.getApplicationContext();
        boolean wasOpened = native_open(name, pfd.getFileDescriptor());
        if (wasOpened) {
            this.mCloseGuard.open("close");
        }
        return wasOpened;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void close() {
        if (this.mNativeContext != 0) {
            native_close();
            this.mCloseGuard.close();
        }
    }

    public int getFileDescriptor() {
        return native_get_fd();
    }

    public byte[] getRawDescriptors() {
        return native_get_desc();
    }

    public boolean claimInterface(UsbInterface intf, boolean force) {
        return native_claim_interface(intf.getId(), force);
    }

    public boolean releaseInterface(UsbInterface intf) {
        return native_release_interface(intf.getId());
    }

    public boolean setInterface(UsbInterface intf) {
        return native_set_interface(intf.getId(), intf.getAlternateSetting());
    }

    public boolean setConfiguration(UsbConfiguration configuration) {
        return native_set_configuration(configuration.getId());
    }

    public int controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int length, int timeout) {
        return controlTransfer(requestType, request, value, index, buffer, 0, length, timeout);
    }

    public int controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int offset, int length, int timeout) {
        checkBounds(buffer, offset, length);
        return native_control_request(requestType, request, value, index, buffer, offset, length, timeout);
    }

    public int bulkTransfer(UsbEndpoint endpoint, byte[] buffer, int length, int timeout) {
        return bulkTransfer(endpoint, buffer, 0, length, timeout);
    }

    public int bulkTransfer(UsbEndpoint endpoint, byte[] buffer, int offset, int length, int timeout) {
        checkBounds(buffer, offset, length);
        return native_bulk_request(endpoint.getAddress(), buffer, offset, length, timeout);
    }

    public boolean resetDevice() {
        return native_reset_device();
    }

    public UsbRequest requestWait() {
        UsbRequest request = null;
        try {
            request = native_request_wait(-1);
        } catch (TimeoutException e) {
        }
        if (request != null) {
            request.dequeue(this.mContext.getApplicationInfo().targetSdkVersion >= 26);
        }
        return request;
    }

    public UsbRequest requestWait(long timeout) throws TimeoutException {
        UsbRequest request = native_request_wait(Preconditions.checkArgumentNonnegative(timeout, "timeout"));
        if (request != null) {
            request.dequeue(true);
        }
        return request;
    }

    public String getSerial() {
        return native_get_serial();
    }

    private static void checkBounds(byte[] buffer, int start, int length) {
        int bufferLength = buffer != null ? buffer.length : 0;
        if (length < 0 || start < 0 || start + length > bufferLength) {
            throw new IllegalArgumentException("Buffer start or length out of bounds.");
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
        } finally {
            super.finalize();
        }
    }
}
