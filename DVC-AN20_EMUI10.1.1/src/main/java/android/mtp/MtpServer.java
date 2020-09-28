package android.mtp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.HwMediaFactory;
import android.util.ByteStringUtils;
import com.android.internal.util.Preconditions;
import java.io.FileDescriptor;
import java.util.Random;

public class MtpServer implements Runnable {
    private static final int sID_LEN_BYTES = 16;
    private static final int sID_LEN_STR = 32;
    private final Context mContext = this.mDatabase.getContext();
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

    private final native void native_setup(MtpDatabase mtpDatabase, FileDescriptor fileDescriptor, boolean z, String str, String str2, String str3, String str4);

    static {
        System.loadLibrary("media_jni");
    }

    public MtpServer(MtpDatabase database, FileDescriptor controlFd, boolean usePtp, Runnable onTerminate, String deviceInfoManufacturer, String deviceInfoModel, String deviceInfoDeviceVersion) {
        String strRandomId;
        this.mDatabase = (MtpDatabase) Preconditions.checkNotNull(database);
        this.mOnTerminate = (Runnable) Preconditions.checkNotNull(onTerminate);
        String strRandomId2 = null;
        SharedPreferences sharedPref = this.mContext.getSharedPreferences("mtp-cfg", 0);
        if (sharedPref.contains("mtp-id")) {
            strRandomId2 = sharedPref.getString("mtp-id", null);
            if (strRandomId2.length() != 32) {
                strRandomId2 = null;
            } else {
                int ii = 0;
                while (true) {
                    if (ii >= strRandomId2.length()) {
                        break;
                    } else if (Character.digit(strRandomId2.charAt(ii), 16) == -1) {
                        strRandomId2 = null;
                        break;
                    } else {
                        ii++;
                    }
                }
            }
        }
        if (strRandomId2 == null) {
            String strRandomId3 = getRandId();
            sharedPref.edit().putString("mtp-id", strRandomId3).apply();
            strRandomId = strRandomId3;
        } else {
            strRandomId = strRandomId2;
        }
        native_setup(database, controlFd, usePtp, deviceInfoManufacturer, deviceInfoModel, deviceInfoDeviceVersion, strRandomId);
        database.setServer(this);
    }

    private String getRandId() {
        byte[] randomBytes = new byte[16];
        new Random().nextBytes(randomBytes);
        return ByteStringUtils.toHexString(randomBytes);
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
        HwMediaFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        native_send_object_info_changed(handle);
    }
}
