package huawei.android.hardware.usb;

import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import huawei.android.content.HwContextEx;
import huawei.android.hardware.usb.IHwUsbManagerEx;
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
        SlogEx.i(TAG, "construction");
    }

    private IHwUsbManagerEx getService() {
        return IHwUsbManagerEx.Stub.asInterface(ServiceManagerEx.getService(HwContextEx.HW_USB_EX_SERVICE));
    }

    public void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.allowUsbHDB(alwaysAllow, publicKey);
            } else {
                SlogEx.w(TAG, "allowUsbHDB service is null!");
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwUsbEx service binder error!");
        }
    }

    public void denyUsbHDB() {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.denyUsbHDB();
            } else {
                SlogEx.w(TAG, "denyUsbHDB service is null!");
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void clearUsbHDBKeys() {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.clearUsbHDBKeys();
            } else {
                SlogEx.w(TAG, "clearUsbHDBKeys service is null!");
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void setHdbEnabled(boolean enabled) {
        try {
            IHwUsbManagerEx service = getService();
            if (service != null) {
                service.setHdbEnabled(enabled);
            } else {
                SlogEx.w(TAG, "setHdbEnabled service is null!");
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void dump(FileDescriptor fd, String[] args) {
        try {
            IBinder b = ServiceManagerEx.getService(HwContextEx.HW_USB_EX_SERVICE);
            if (b != null) {
                b.dump(fd, args);
            } else {
                SlogEx.w(TAG, "dump service is null!");
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }
}
