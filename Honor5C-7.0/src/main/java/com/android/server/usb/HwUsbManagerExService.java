package com.android.server.usb;

import android.content.Context;
import huawei.android.hardware.usb.IHwUsbManagerEx.Stub;

public class HwUsbManagerExService extends Stub {
    static final String TAG = "HwUsbManagerExService";
    private Context mContext;

    public HwUsbManagerExService(Context contextUsb) {
        this.mContext = contextUsb;
    }

    public void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        if (HwUsbHDBManagerImpl.isCreated()) {
            HwUsbHDBManagerImpl.getInstance(this.mContext).allowUsbHDB(alwaysAllow, publicKey);
        }
    }

    public void denyUsbHDB() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        if (HwUsbHDBManagerImpl.isCreated()) {
            HwUsbHDBManagerImpl.getInstance(this.mContext).denyUsbHDB();
        }
    }

    public void clearUsbHDBKeys() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USB", null);
        if (HwUsbHDBManagerImpl.isCreated()) {
            HwUsbHDBManagerImpl.getInstance(this.mContext).clearUsbHDBKeys();
        }
    }
}
