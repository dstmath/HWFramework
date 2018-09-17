package com.android.server.usb;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.am.HwBroadcastRadarUtil;
import java.util.HashMap;

class UsbUserSettingsManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsbUserSettingsManager";
    private final HashMap<UsbAccessory, SparseBooleanArray> mAccessoryPermissionMap = new HashMap();
    private final HashMap<String, SparseBooleanArray> mDevicePermissionMap = new HashMap();
    private final boolean mDisablePermissionDialogs;
    private final Object mLock = new Object();
    private final PackageManager mPackageManager;
    private final UserHandle mUser;
    private final Context mUserContext;

    public UsbUserSettingsManager(Context context, UserHandle user) {
        try {
            this.mUserContext = context.createPackageContextAsUser("android", 0, user);
            this.mPackageManager = this.mUserContext.getPackageManager();
            this.mUser = user;
            this.mDisablePermissionDialogs = context.getResources().getBoolean(17956927);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Missing android package");
        }
    }

    void removeDevicePermissions(UsbDevice device) {
        synchronized (this.mLock) {
            this.mDevicePermissionMap.remove(device.getDeviceName());
        }
    }

    void removeAccessoryPermissions(UsbAccessory accessory) {
        synchronized (this.mLock) {
            this.mAccessoryPermissionMap.remove(accessory);
        }
    }

    public boolean hasPermission(UsbDevice device) {
        synchronized (this.mLock) {
            int uid = Binder.getCallingUid();
            if (uid == 1000 || this.mDisablePermissionDialogs) {
                return true;
            }
            SparseBooleanArray uidList = (SparseBooleanArray) this.mDevicePermissionMap.get(device.getDeviceName());
            if (uidList == null) {
                return false;
            }
            boolean z = uidList.get(uid);
            return z;
        }
    }

    public boolean hasPermission(UsbAccessory accessory) {
        synchronized (this.mLock) {
            int uid = Binder.getCallingUid();
            if (uid == 1000 || this.mDisablePermissionDialogs) {
                return true;
            }
            SparseBooleanArray uidList = (SparseBooleanArray) this.mAccessoryPermissionMap.get(accessory);
            if (uidList == null) {
                return false;
            }
            boolean z = uidList.get(uid);
            return z;
        }
    }

    public void checkPermission(UsbDevice device) {
        if (!hasPermission(device)) {
            throw new SecurityException("User has not given permission to device " + device);
        }
    }

    public void checkPermission(UsbAccessory accessory) {
        if (!hasPermission(accessory)) {
            throw new SecurityException("User has not given permission to accessory " + accessory);
        }
    }

    private void requestPermissionDialog(Intent intent, String packageName, PendingIntent pi) {
        int uid = Binder.getCallingUid();
        try {
            if (this.mPackageManager.getApplicationInfo(packageName, 0).uid != uid) {
                throw new IllegalArgumentException("package " + packageName + " does not match caller's uid " + uid);
            }
            long identity = Binder.clearCallingIdentity();
            intent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbPermissionActivity");
            intent.addFlags(268435456);
            intent.putExtra("android.intent.extra.INTENT", pi);
            intent.putExtra(HwBroadcastRadarUtil.KEY_PACKAGE, packageName);
            intent.putExtra("android.intent.extra.UID", uid);
            try {
                this.mUserContext.startActivityAsUser(intent, this.mUser);
            } catch (ActivityNotFoundException e) {
                Slog.e(TAG, "unable to start UsbPermissionActivity");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (NameNotFoundException e2) {
            throw new IllegalArgumentException("package " + packageName + " not found");
        }
    }

    public void requestPermission(UsbDevice device, String packageName, PendingIntent pi) {
        Intent intent = new Intent();
        if (hasPermission(device)) {
            intent.putExtra("device", device);
            intent.putExtra("permission", true);
            try {
                pi.send(this.mUserContext, 0, intent);
            } catch (CanceledException e) {
            }
            return;
        }
        intent.putExtra("device", device);
        requestPermissionDialog(intent, packageName, pi);
    }

    public void requestPermission(UsbAccessory accessory, String packageName, PendingIntent pi) {
        Intent intent = new Intent();
        if (hasPermission(accessory)) {
            intent.putExtra("accessory", accessory);
            intent.putExtra("permission", true);
            try {
                pi.send(this.mUserContext, 0, intent);
            } catch (CanceledException e) {
            }
            return;
        }
        intent.putExtra("accessory", accessory);
        requestPermissionDialog(intent, packageName, pi);
    }

    public void grantDevicePermission(UsbDevice device, int uid) {
        synchronized (this.mLock) {
            String deviceName = device.getDeviceName();
            SparseBooleanArray uidList = (SparseBooleanArray) this.mDevicePermissionMap.get(deviceName);
            if (uidList == null) {
                uidList = new SparseBooleanArray(1);
                this.mDevicePermissionMap.put(deviceName, uidList);
            }
            uidList.put(uid, true);
        }
    }

    public void grantAccessoryPermission(UsbAccessory accessory, int uid) {
        synchronized (this.mLock) {
            SparseBooleanArray uidList = (SparseBooleanArray) this.mAccessoryPermissionMap.get(accessory);
            if (uidList == null) {
                uidList = new SparseBooleanArray(1);
                this.mAccessoryPermissionMap.put(accessory, uidList);
            }
            uidList.put(uid, true);
        }
    }

    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            SparseBooleanArray uidList;
            int count;
            int i;
            pw.println("Device permissions:");
            for (String deviceName : this.mDevicePermissionMap.keySet()) {
                pw.print("  " + deviceName + ": ");
                uidList = (SparseBooleanArray) this.mDevicePermissionMap.get(deviceName);
                count = uidList.size();
                for (i = 0; i < count; i++) {
                    pw.print(Integer.toString(uidList.keyAt(i)) + " ");
                }
                pw.println();
            }
            pw.println("Accessory permissions:");
            for (UsbAccessory accessory : this.mAccessoryPermissionMap.keySet()) {
                pw.print("  " + accessory + ": ");
                uidList = (SparseBooleanArray) this.mAccessoryPermissionMap.get(accessory);
                count = uidList.size();
                for (i = 0; i < count; i++) {
                    pw.print(Integer.toString(uidList.keyAt(i)) + " ");
                }
                pw.println();
            }
        }
    }
}
