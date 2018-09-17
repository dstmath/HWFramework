package android.pc;

import android.os.IBinder;
import android.os.ServiceManager;
import android.pc.IHwPCManager.Stub;
import huawei.android.content.HwContextEx;

public class HwPCManagerImpl implements HwPCManager {
    private static final String TAG = "HwPCManagerImpl";
    private static volatile HwPCManagerImpl mInstance = null;
    private IHwPCManager mService = null;

    public static synchronized HwPCManagerImpl getDefault() {
        HwPCManagerImpl hwPCManagerImpl;
        synchronized (HwPCManagerImpl.class) {
            if (mInstance == null || mInstance.getService() == null) {
                mInstance = new HwPCManagerImpl();
            }
            hwPCManagerImpl = mInstance;
        }
        return hwPCManagerImpl;
    }

    public IHwPCManager getService() {
        return this.mService;
    }

    private HwPCManagerImpl() {
        IBinder iBinder = ServiceManager.getService(HwContextEx.HW_PCM_SERVICE);
        if (iBinder != null) {
            this.mService = Stub.asInterface(iBinder);
        }
    }
}
