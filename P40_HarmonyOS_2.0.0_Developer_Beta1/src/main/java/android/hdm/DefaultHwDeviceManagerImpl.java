package android.hdm;

import android.content.Intent;
import android.hdm.HwDeviceManager;
import java.util.List;

public class DefaultHwDeviceManagerImpl implements HwDeviceManager.IHwDeviceManager {
    private static final String TAG = "DefaultHwDeviceManagerImpl";
    private static HwDeviceManager.IHwDeviceManager sInstance = null;

    public static HwDeviceManager.IHwDeviceManager getDefault() {
        HwDeviceManager.IHwDeviceManager iHwDeviceManager;
        synchronized (DefaultHwDeviceManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwDeviceManagerImpl();
            }
            iHwDeviceManager = sInstance;
        }
        return iHwDeviceManager;
    }

    @Override // android.hdm.HwDeviceManager.IHwDeviceManager
    public boolean disallowOp(int type) {
        return false;
    }

    @Override // android.hdm.HwDeviceManager.IHwDeviceManager
    public boolean disallowOp(int type, String param) {
        return false;
    }

    @Override // android.hdm.HwDeviceManager.IHwDeviceManager
    public boolean disallowOp(int type, Intent intent) {
        return false;
    }

    @Override // android.hdm.HwDeviceManager.IHwDeviceManager
    public List<String> getList(int type) {
        return null;
    }

    @Override // android.hdm.HwDeviceManager.IHwDeviceManager
    public String getString(int type) {
        return null;
    }
}
