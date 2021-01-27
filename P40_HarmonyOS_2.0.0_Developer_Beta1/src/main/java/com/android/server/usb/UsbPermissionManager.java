package com.android.server.usb;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Binder;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.dump.DualDumpOutputStream;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public class UsbPermissionManager {
    private static final String LOG_TAG = UsbPermissionManager.class.getSimpleName();
    @GuardedBy({"mLock"})
    private final HashMap<UsbAccessory, SparseBooleanArray> mAccessoryPermissionMap = new HashMap<>();
    @GuardedBy({"mLock"})
    private final HashMap<String, SparseBooleanArray> mDevicePermissionMap = new HashMap<>();
    private final boolean mDisablePermissionDialogs;
    private final Object mLock = new Object();
    private final UserHandle mUser;

    UsbPermissionManager(Context context, UserHandle user) {
        this.mUser = user;
        this.mDisablePermissionDialogs = context.getResources().getBoolean(17891410);
    }

    /* access modifiers changed from: package-private */
    public void removeAccessoryPermissions(UsbAccessory accessory) {
        synchronized (this.mLock) {
            this.mAccessoryPermissionMap.remove(accessory);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeDevicePermissions(UsbDevice device) {
        synchronized (this.mLock) {
            this.mDevicePermissionMap.remove(device.getDeviceName());
        }
    }

    /* access modifiers changed from: package-private */
    public void grantDevicePermission(UsbDevice device, int uid) {
        synchronized (this.mLock) {
            String deviceName = device.getDeviceName();
            SparseBooleanArray uidList = this.mDevicePermissionMap.get(deviceName);
            if (uidList == null) {
                uidList = new SparseBooleanArray(1);
                this.mDevicePermissionMap.put(deviceName, uidList);
            }
            uidList.put(uid, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void grantAccessoryPermission(UsbAccessory accessory, int uid) {
        synchronized (this.mLock) {
            SparseBooleanArray uidList = this.mAccessoryPermissionMap.get(accessory);
            if (uidList == null) {
                uidList = new SparseBooleanArray(1);
                this.mAccessoryPermissionMap.put(accessory, uidList);
            }
            uidList.put(uid, true);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPermission(UsbDevice device, int uid) {
        synchronized (this.mLock) {
            if (uid != 1000) {
                if (!this.mDisablePermissionDialogs) {
                    SparseBooleanArray uidList = this.mDevicePermissionMap.get(device.getDeviceName());
                    if (uidList == null) {
                        return false;
                    }
                    return uidList.get(uid);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPermission(UsbAccessory accessory, int uid) {
        synchronized (this.mLock) {
            if (uid != 1000) {
                if (!this.mDisablePermissionDialogs) {
                    SparseBooleanArray uidList = this.mAccessoryPermissionMap.get(accessory);
                    if (uidList == null) {
                        return false;
                    }
                    return uidList.get(uid);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void requestPermissionDialog(UsbDevice device, UsbAccessory accessory, boolean canBeDefault, String packageName, int uid, Context userContext, PendingIntent pi) {
        long identity = Binder.clearCallingIdentity();
        Intent intent = new Intent();
        if (device != null) {
            intent.putExtra("device", device);
        } else {
            intent.putExtra("accessory", accessory);
        }
        intent.putExtra("android.intent.extra.INTENT", pi);
        intent.putExtra("android.intent.extra.UID", uid);
        intent.putExtra("android.hardware.usb.extra.CAN_BE_DEFAULT", canBeDefault);
        intent.putExtra("android.hardware.usb.extra.PACKAGE", packageName);
        intent.setClassName("com.android.systemui", "com.android.systemui.usb.UsbPermissionActivity");
        intent.addFlags(268435456);
        try {
            userContext.startActivityAsUser(intent, this.mUser);
        } catch (ActivityNotFoundException e) {
            Slog.e(LOG_TAG, "unable to start UsbPermissionActivity");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
    }

    /* access modifiers changed from: package-private */
    public void dump(DualDumpOutputStream dump) {
        synchronized (this.mLock) {
            for (String deviceName : this.mDevicePermissionMap.keySet()) {
                long devicePermissionToken = dump.start("device_permissions", 2246267895810L);
                dump.write("device_name", 1138166333441L, deviceName);
                SparseBooleanArray uidList = this.mDevicePermissionMap.get(deviceName);
                int count = uidList.size();
                for (int i = 0; i < count; i++) {
                    dump.write("uids", 2220498092034L, uidList.keyAt(i));
                }
                dump.end(devicePermissionToken);
            }
            for (UsbAccessory accessory : this.mAccessoryPermissionMap.keySet()) {
                long accessoryPermissionToken = dump.start("accessory_permissions", 2246267895811L);
                dump.write("accessory_description", 1138166333441L, accessory.getDescription());
                SparseBooleanArray uidList2 = this.mAccessoryPermissionMap.get(accessory);
                int count2 = uidList2.size();
                for (int i2 = 0; i2 < count2; i2++) {
                    dump.write("uids", 2220498092034L, uidList2.keyAt(i2));
                }
                dump.end(accessoryPermissionToken);
            }
        }
    }
}
