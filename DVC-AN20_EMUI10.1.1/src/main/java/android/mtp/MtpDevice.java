package android.mtp;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.UserManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.IOException;

public final class MtpDevice {
    private static final String TAG = "MtpDevice";
    @GuardedBy({"mLock"})
    private CloseGuard mCloseGuard = CloseGuard.get();
    @GuardedBy({"mLock"})
    private UsbDeviceConnection mConnection;
    private final UsbDevice mDevice;
    private final Object mLock = new Object();
    private long mNativeContext;

    private native void native_close();

    private native boolean native_delete_object(int i);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void native_discard_event_request(int i);

    private native MtpDeviceInfo native_get_device_info();

    private native byte[] native_get_object(int i, long j);

    private native int[] native_get_object_handles(int i, int i2, int i3);

    private native MtpObjectInfo native_get_object_info(int i);

    private native long native_get_object_size_long(int i, int i2) throws IOException;

    private native int native_get_parent(int i);

    private native long native_get_partial_object(int i, long j, long j2, byte[] bArr) throws IOException;

    private native int native_get_partial_object_64(int i, long j, long j2, byte[] bArr) throws IOException;

    private native int native_get_storage_id(int i);

    private native int[] native_get_storage_ids();

    private native MtpStorageInfo native_get_storage_info(int i);

    private native byte[] native_get_thumbnail(int i);

    private native boolean native_import_file(int i, int i2);

    private native boolean native_import_file(int i, String str);

    private native boolean native_open(String str, int i);

    private native MtpEvent native_reap_event_request(int i) throws IOException;

    private native boolean native_send_object(int i, long j, int i2);

    private native MtpObjectInfo native_send_object_info(MtpObjectInfo mtpObjectInfo);

    private native int native_submit_event_request() throws IOException;

    static {
        System.loadLibrary("media_jni");
    }

    public MtpDevice(UsbDevice device) {
        Preconditions.checkNotNull(device);
        this.mDevice = device;
    }

    public boolean open(UsbDeviceConnection connection) {
        boolean result = false;
        Context context = connection.getContext();
        synchronized (this.mLock) {
            if (context != null) {
                try {
                    if (!((UserManager) context.getSystemService("user")).hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
                        result = native_open(this.mDevice.getDeviceName(), connection.getFileDescriptor());
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (!result) {
                connection.close();
            } else {
                this.mConnection = connection;
                this.mCloseGuard.open("close");
            }
        }
        return result;
    }

    public void close() {
        synchronized (this.mLock) {
            if (this.mConnection != null) {
                this.mCloseGuard.close();
                native_close();
                this.mConnection.close();
                this.mConnection = null;
            }
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

    public String getDeviceName() {
        return this.mDevice.getDeviceName();
    }

    public int getDeviceId() {
        return this.mDevice.getDeviceId();
    }

    public String toString() {
        return this.mDevice.getDeviceName();
    }

    public MtpDeviceInfo getDeviceInfo() {
        return native_get_device_info();
    }

    public int[] getStorageIds() {
        return native_get_storage_ids();
    }

    public int[] getObjectHandles(int storageId, int format, int objectHandle) {
        return native_get_object_handles(storageId, format, objectHandle);
    }

    public byte[] getObject(int objectHandle, int objectSize) {
        Preconditions.checkArgumentNonnegative(objectSize, "objectSize should not be negative");
        return native_get_object(objectHandle, (long) objectSize);
    }

    public long getPartialObject(int objectHandle, long offset, long size, byte[] buffer) throws IOException {
        return native_get_partial_object(objectHandle, offset, size, buffer);
    }

    public long getPartialObject64(int objectHandle, long offset, long size, byte[] buffer) throws IOException {
        return (long) native_get_partial_object_64(objectHandle, offset, size, buffer);
    }

    public byte[] getThumbnail(int objectHandle) {
        return native_get_thumbnail(objectHandle);
    }

    public MtpStorageInfo getStorageInfo(int storageId) {
        return native_get_storage_info(storageId);
    }

    public MtpObjectInfo getObjectInfo(int objectHandle) {
        return native_get_object_info(objectHandle);
    }

    public boolean deleteObject(int objectHandle) {
        return native_delete_object(objectHandle);
    }

    public long getParent(int objectHandle) {
        return (long) native_get_parent(objectHandle);
    }

    public long getStorageId(int objectHandle) {
        return (long) native_get_storage_id(objectHandle);
    }

    public boolean importFile(int objectHandle, String destPath) {
        return native_import_file(objectHandle, destPath);
    }

    public boolean importFile(int objectHandle, ParcelFileDescriptor descriptor) {
        return native_import_file(objectHandle, descriptor.getFd());
    }

    public boolean sendObject(int objectHandle, long size, ParcelFileDescriptor descriptor) {
        return native_send_object(objectHandle, size, descriptor.getFd());
    }

    public MtpObjectInfo sendObjectInfo(MtpObjectInfo info) {
        return native_send_object_info(info);
    }

    public MtpEvent readEvent(CancellationSignal signal) throws IOException {
        final int handle = native_submit_event_request();
        Preconditions.checkState(handle >= 0, "Other thread is reading an event.");
        if (signal != null) {
            signal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                /* class android.mtp.MtpDevice.AnonymousClass1 */

                @Override // android.os.CancellationSignal.OnCancelListener
                public void onCancel() {
                    MtpDevice.this.native_discard_event_request(handle);
                }
            });
        }
        try {
            return native_reap_event_request(handle);
        } finally {
            if (signal != null) {
                signal.setOnCancelListener(null);
            }
        }
    }

    public long getObjectSizeLong(int handle, int format) throws IOException {
        return native_get_object_size_long(handle, format);
    }
}
