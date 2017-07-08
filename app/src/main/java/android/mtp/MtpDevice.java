package android.mtp;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import com.android.internal.util.Preconditions;
import java.io.IOException;

public final class MtpDevice {
    private static final String TAG = "MtpDevice";
    private final UsbDevice mDevice;
    private long mNativeContext;

    /* renamed from: android.mtp.MtpDevice.1 */
    class AnonymousClass1 implements OnCancelListener {
        final /* synthetic */ int val$handle;

        AnonymousClass1(int val$handle) {
            this.val$handle = val$handle;
        }

        public void onCancel() {
            MtpDevice.this.native_discard_event_request(this.val$handle);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.mtp.MtpDevice.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.mtp.MtpDevice.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.MtpDevice.<clinit>():void");
    }

    private native void native_close();

    private native boolean native_delete_object(int i);

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

    public MtpDevice(UsbDevice device) {
        this.mDevice = device;
    }

    public boolean open(UsbDeviceConnection connection) {
        boolean result = native_open(this.mDevice.getDeviceName(), connection.getFileDescriptor());
        if (!result) {
            connection.close();
        }
        return result;
    }

    public void close() {
        native_close();
    }

    protected void finalize() throws Throwable {
        try {
            native_close();
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
        boolean z = false;
        int handle = native_submit_event_request();
        if (handle >= 0) {
            z = true;
        }
        Preconditions.checkState(z, "Other thread is reading an event.");
        if (signal != null) {
            signal.setOnCancelListener(new AnonymousClass1(handle));
        }
        try {
            MtpEvent native_reap_event_request = native_reap_event_request(handle);
            return native_reap_event_request;
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
