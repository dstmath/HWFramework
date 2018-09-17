package android.mtp;

import android.common.HwFrameworkFactory;
import com.android.internal.util.Preconditions;

public class MtpServer implements Runnable {
    private final MtpDatabase mDatabase;
    private long mNativeContext;
    private final Runnable mOnTerminate;

    private final native void native_add_storage(MtpStorage mtpStorage);

    private final native void native_cleanup();

    public static final native void native_configure(boolean z);

    private final native void native_remove_storage(int i);

    private final native void native_run();

    private final native void native_send_all_storage_info_changed_if_needed();

    private final native void native_send_device_property_changed(int i);

    private final native void native_send_object_added(int i);

    private final native void native_send_object_info_changed(int i);

    private final native void native_send_object_removed(int i);

    private final native void native_setup(MtpDatabase mtpDatabase, boolean z, String str, String str2, String str3, String str4);

    static {
        System.loadLibrary("media_jni");
    }

    public MtpServer(MtpDatabase database, boolean usePtp, Runnable onTerminate, String deviceInfoManufacturer, String deviceInfoModel, String deviceInfoDeviceVersion, String deviceInfoSerialNumber) {
        this.mDatabase = (MtpDatabase) Preconditions.checkNotNull(database);
        this.mOnTerminate = (Runnable) Preconditions.checkNotNull(onTerminate);
        native_setup(database, usePtp, deviceInfoManufacturer, deviceInfoModel, deviceInfoDeviceVersion, deviceInfoSerialNumber);
        database.setServer(this);
    }

    public void start() {
        new Thread(this, "MtpServer").start();
    }

    public void run() {
        native_run();
        native_cleanup();
        this.mDatabase.close();
        this.mOnTerminate.run();
    }

    public void sendObjectAdded(int handle) {
        native_send_object_added(handle);
    }

    public void sendObjectRemoved(int handle) {
        native_send_object_removed(handle);
    }

    public void sendDevicePropertyChanged(int property) {
        native_send_device_property_changed(property);
    }

    public void addStorage(MtpStorage storage) {
        native_add_storage(storage);
    }

    public void removeStorage(MtpStorage storage) {
        native_remove_storage(storage.getStorageId());
    }

    public static void configure(boolean usePtp) {
        native_configure(usePtp);
    }

    public void sendAllStorageInfoChangedIfNeed() {
        native_send_all_storage_info_changed_if_needed();
    }

    public void sendObjectInfoChanged(int handle) {
        HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        native_send_object_info_changed(handle);
    }
}
