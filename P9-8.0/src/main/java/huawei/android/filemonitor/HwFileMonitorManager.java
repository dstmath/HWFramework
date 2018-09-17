package huawei.android.filemonitor;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.filemonitor.IFileMonitorManager.Stub;
import java.util.List;

public class HwFileMonitorManager {
    private static final String TAG = "HwFileMonitorManager";
    private static volatile HwFileMonitorManager mInstance = null;

    public static synchronized HwFileMonitorManager getInstance() {
        HwFileMonitorManager hwFileMonitorManager;
        synchronized (HwFileMonitorManager.class) {
            if (mInstance == null) {
                mInstance = new HwFileMonitorManager();
            }
            hwFileMonitorManager = mInstance;
        }
        return hwFileMonitorManager;
    }

    private HwFileMonitorManager() {
    }

    private IFileMonitorManager getService() {
        return Stub.asInterface(ServiceManager.getService(HwContextEx.HW_FILE_MONITOR_SERVICE));
    }

    public int request(Bundle bundle) {
        try {
            IFileMonitorManager service = getService();
            if (service != null && bundle != null) {
                return service.request(bundle);
            }
            Slog.d(TAG, "request service or bundle null");
            return -1;
        } catch (RemoteException e) {
            Slog.e(TAG, "FileMonitor binder error!");
            return 0;
        }
    }

    public List<String> getPolicy() {
        try {
            IFileMonitorManager service = getService();
            if (service == null) {
                return null;
            }
            return service.getPolicy();
        } catch (RemoteException e) {
            Slog.e(TAG, "FileMonitor binder error!");
            return null;
        }
    }

    public boolean setPolicy(List<String> policy) {
        if (policy == null || policy.isEmpty()) {
            return false;
        }
        try {
            IFileMonitorManager service = getService();
            if (service == null) {
                return false;
            }
            return service.setPolicy(policy);
        } catch (RemoteException e) {
            Slog.e(TAG, "FileMonitor binder error!");
            return false;
        }
    }
}
