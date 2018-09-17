package huawei.android.hardware.usb;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.hardware.usb.IHwUsbManagerEx.Stub;
import java.io.FileDescriptor;

public class HwUsbManagerEx {
    private static final String TAG = "HwUsbManagerEx";
    private static volatile HwUsbManagerEx mInstance = null;

    public static synchronized HwUsbManagerEx getInstance() {
        HwUsbManagerEx hwUsbManagerEx;
        synchronized (HwUsbManagerEx.class) {
            if (mInstance == null) {
                mInstance = new HwUsbManagerEx();
            }
            hwUsbManagerEx = mInstance;
        }
        return hwUsbManagerEx;
    }

    private HwUsbManagerEx() {
        Slog.i(TAG, "construction");
    }

    private IHwUsbManagerEx getService() {
        return Stub.asInterface(ServiceManager.getService(HwContextEx.HW_USB_EX_SERVICE));
    }

    public void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.allowUsbHDB(alwaysAllow, publicKey);
            } else {
                Slog.w(TAG, "allowUsbHDB service is null!");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbEx service binder error!");
        }
    }

    public void denyUsbHDB() {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.denyUsbHDB();
            } else {
                Slog.w(TAG, "denyUsbHDB service is null!");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void clearUsbHDBKeys() {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.clearUsbHDBKeys();
            } else {
                Slog.w(TAG, "clearUsbHDBKeys service is null!");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void setHdbEnabled(boolean enabled) {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.setHdbEnabled(enabled);
            } else {
                Slog.w(TAG, "setHdbEnabled service is null!");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void dump(FileDescriptor fd, String[] args) {
        try {
            IBinder b = ServiceManager.getService(HwContextEx.HW_USB_EX_SERVICE);
            if (b != null) {
                b.dump(fd, args);
            } else {
                Slog.w(TAG, "dump service is null!");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }
}
