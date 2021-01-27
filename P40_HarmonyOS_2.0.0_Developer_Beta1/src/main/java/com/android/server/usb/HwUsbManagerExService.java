package com.android.server.usb;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import huawei.android.hardware.usb.IHwUsbManagerEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwUsbManagerExService extends IHwUsbManagerEx.Stub {
    private static final String MANAGE_USB_PERMISSION = "android.permission.MANAGE_USB";
    private static final String TAG = "HwUsbHDBManager";
    private static HwUsbManagerExService sInstance;
    private Context mContext;
    private HwUsbHDBManagerImpl mImpl = null;

    private HwUsbManagerExService(Context context) {
        this.mContext = context;
        initImpl();
    }

    public static synchronized HwUsbManagerExService getInstance(Context context) {
        HwUsbManagerExService hwUsbManagerExService;
        synchronized (HwUsbManagerExService.class) {
            if (sInstance == null) {
                sInstance = new HwUsbManagerExService(context);
            }
            hwUsbManagerExService = sInstance;
        }
        return hwUsbManagerExService;
    }

    private void initImpl() {
        if (!SystemProperties.getBoolean("ro.adb.secure", false)) {
            Slog.i(TAG, "prop adb secure is false, do not init HwUsbHDBManagerImpl");
        } else {
            this.mImpl = HwUsbHDBManagerImpl.getInstance(this.mContext);
        }
    }

    public void allowUsbHDB(boolean isAlwaysAllow, String publicKey) {
        Slog.i(TAG, "allowUsbHDB" + isAlwaysAllow);
        if (Binder.getCallingUid() == 1000 || this.mContext.checkCallingPermission(MANAGE_USB_PERMISSION) == 0) {
            HwUsbHDBManagerImpl hwUsbHDBManagerImpl = this.mImpl;
            if (hwUsbHDBManagerImpl != null) {
                hwUsbHDBManagerImpl.allowUsbHDB(isAlwaysAllow, publicKey);
                return;
            }
            return;
        }
        Slog.e(TAG, "allowUsbHDB, permission MANAGE_USB not allowed");
        throw new SecurityException("allowUsbHDB, permission MANAGE_USB not allowed");
    }

    public void denyUsbHDB() {
        Slog.i(TAG, "denyUsbHDB");
        if (Binder.getCallingUid() == 1000 || this.mContext.checkCallingPermission(MANAGE_USB_PERMISSION) == 0) {
            HwUsbHDBManagerImpl hwUsbHDBManagerImpl = this.mImpl;
            if (hwUsbHDBManagerImpl != null) {
                hwUsbHDBManagerImpl.denyUsbHDB();
                return;
            }
            return;
        }
        Slog.e(TAG, "denyUsbHDB, permission MANAGE_USB not allowed");
        throw new SecurityException("denyUsbHDB, permission MANAGE_USB not allowed");
    }

    public void clearUsbHDBKeys() {
        Slog.i(TAG, "clearUsbHDBKeys");
        if (Binder.getCallingUid() == 1000 || this.mContext.checkCallingPermission(MANAGE_USB_PERMISSION) == 0) {
            HwUsbHDBManagerImpl hwUsbHDBManagerImpl = this.mImpl;
            if (hwUsbHDBManagerImpl != null) {
                hwUsbHDBManagerImpl.clearUsbHDBKeys();
                return;
            }
            return;
        }
        Slog.e(TAG, "clearUsbHDBKeys, permission MANAGE_USB not allowed");
        throw new SecurityException("clearUsbHDBKeys, permission MANAGE_USB not allowed");
    }

    public void setHdbEnabled(boolean isEnabled) {
        Slog.i(TAG, "setHdbEnabled " + isEnabled);
        if (Binder.getCallingUid() == 1000 || this.mContext.checkCallingPermission(MANAGE_USB_PERMISSION) == 0) {
            HwUsbHDBManagerImpl hwUsbHDBManagerImpl = this.mImpl;
            if (hwUsbHDBManagerImpl != null) {
                hwUsbHDBManagerImpl.setHdbEnabled(isEnabled);
                return;
            }
            return;
        }
        Slog.e(TAG, "setHdbEnabled, permission MANAGE_USB not allowed");
        throw new SecurityException("setHdbEnabled, permission MANAGE_USB not allowed");
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        HwUsbHDBManagerImpl hwUsbHDBManagerImpl;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer) && (hwUsbHDBManagerImpl = this.mImpl) != null) {
            hwUsbHDBManagerImpl.dump(fd, writer, args);
        }
    }
}
