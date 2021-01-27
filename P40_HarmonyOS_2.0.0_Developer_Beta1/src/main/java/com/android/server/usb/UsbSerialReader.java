package com.android.server.usb;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbSerialReader;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import com.android.internal.util.ArrayUtils;

/* access modifiers changed from: package-private */
public class UsbSerialReader extends IUsbSerialReader.Stub {
    private final Context mContext;
    private Object mDevice;
    private final String mSerialNumber;
    private final UsbSettingsManager mSettingsManager;

    UsbSerialReader(Context context, UsbSettingsManager settingsManager, String serialNumber) {
        this.mContext = context;
        this.mSettingsManager = settingsManager;
        this.mSerialNumber = serialNumber;
    }

    public void setDevice(Object device) {
        this.mDevice = device;
    }

    public String getSerial(String packageName) throws RemoteException {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (uid != 1000) {
            enforcePackageBelongsToUid(uid, packageName);
            long token = Binder.clearCallingIdentity();
            try {
                try {
                    if (this.mContext.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.targetSdkVersion >= 29 && this.mContext.checkPermission("android.permission.MANAGE_USB", pid, uid) == -1) {
                        UsbUserSettingsManager settings = this.mSettingsManager.getSettingsForUser(UserHandle.getUserId(uid));
                        if (this.mDevice instanceof UsbDevice) {
                            settings.checkPermission((UsbDevice) this.mDevice, packageName, pid, uid);
                        } else {
                            settings.checkPermission((UsbAccessory) this.mDevice, uid);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RemoteException("package " + packageName + " cannot be found");
            }
        }
        return this.mSerialNumber;
    }

    private void enforcePackageBelongsToUid(int uid, String packageName) {
        if (!ArrayUtils.contains(this.mContext.getPackageManager().getPackagesForUid(uid), packageName)) {
            throw new IllegalArgumentException(packageName + " does to belong to the " + uid);
        }
    }
}
