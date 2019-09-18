package com.android.server.usb;

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.pm.DumpState;

class UsbSettingsManager {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = UsbSettingsManager.class.getSimpleName();
    private final Context mContext;
    @GuardedBy("mSettingsByProfileGroup")
    private final SparseArray<UsbProfileGroupSettingsManager> mSettingsByProfileGroup = new SparseArray<>();
    @GuardedBy("mSettingsByUser")
    private final SparseArray<UsbUserSettingsManager> mSettingsByUser = new SparseArray<>();
    private UserManager mUserManager;

    public UsbSettingsManager(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    /* access modifiers changed from: package-private */
    public UsbUserSettingsManager getSettingsForUser(int userId) {
        UsbUserSettingsManager settings;
        synchronized (this.mSettingsByUser) {
            settings = this.mSettingsByUser.get(userId);
            if (settings == null) {
                settings = new UsbUserSettingsManager(this.mContext, new UserHandle(userId));
                this.mSettingsByUser.put(userId, settings);
            }
        }
        return settings;
    }

    /* access modifiers changed from: package-private */
    public UsbProfileGroupSettingsManager getSettingsForProfileGroup(UserHandle user) {
        UserHandle parentUser;
        UsbProfileGroupSettingsManager settings;
        UserInfo parentUserInfo = this.mUserManager.getProfileParent(user.getIdentifier());
        if (parentUserInfo != null) {
            parentUser = parentUserInfo.getUserHandle();
        } else {
            parentUser = user;
        }
        synchronized (this.mSettingsByProfileGroup) {
            settings = this.mSettingsByProfileGroup.get(parentUser.getIdentifier());
            if (settings == null) {
                settings = new UsbProfileGroupSettingsManager(this.mContext, parentUser, this);
                this.mSettingsByProfileGroup.put(parentUser.getIdentifier(), settings);
            }
        }
        return settings;
    }

    /* access modifiers changed from: package-private */
    public void remove(UserHandle userToRemove) {
        synchronized (this.mSettingsByUser) {
            this.mSettingsByUser.remove(userToRemove.getIdentifier());
        }
        synchronized (this.mSettingsByProfileGroup) {
            if (this.mSettingsByProfileGroup.indexOfKey(userToRemove.getIdentifier()) >= 0) {
                this.mSettingsByProfileGroup.remove(userToRemove.getIdentifier());
            } else {
                int numProfileGroups = this.mSettingsByProfileGroup.size();
                for (int i = 0; i < numProfileGroups; i++) {
                    this.mSettingsByProfileGroup.valueAt(i).removeAllDefaultsForUser(userToRemove);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(DualDumpOutputStream dump, String idName, long id) {
        int i;
        long token = dump.start(idName, id);
        synchronized (this.mSettingsByUser) {
            int numUsers = this.mSettingsByUser.size();
            for (int i2 = 0; i2 < numUsers; i2++) {
                this.mSettingsByUser.valueAt(i2).dump(dump, "user_settings", 2246267895809L);
            }
        }
        synchronized (this.mSettingsByProfileGroup) {
            int numProfileGroups = this.mSettingsByProfileGroup.size();
            for (i = 0; i < numProfileGroups; i++) {
                this.mSettingsByProfileGroup.valueAt(i).dump(dump, "profile_group_settings", 2246267895810L);
            }
        }
        dump.end(token);
    }

    /* access modifiers changed from: package-private */
    public void usbDeviceRemoved(UsbDevice device) {
        synchronized (this.mSettingsByUser) {
            for (int i = 0; i < this.mSettingsByUser.size(); i++) {
                this.mSettingsByUser.valueAt(i).removeDevicePermissions(device);
            }
        }
        Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_DETACHED");
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        intent.putExtra("device", device);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: package-private */
    public void usbAccessoryRemoved(UsbAccessory accessory) {
        synchronized (this.mSettingsByUser) {
            for (int i = 0; i < this.mSettingsByUser.size(); i++) {
                this.mSettingsByUser.valueAt(i).removeAccessoryPermissions(accessory);
            }
        }
        Intent intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_DETACHED");
        intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        intent.putExtra("accessory", accessory);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }
}
