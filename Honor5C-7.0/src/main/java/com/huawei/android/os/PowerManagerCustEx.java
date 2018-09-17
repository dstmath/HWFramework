package com.huawei.android.os;

import android.app.Application;
import android.content.Context;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.android.util.NoExtAPIException;

public class PowerManagerCustEx {
    public static int setColorTemperature(IPowerManager svc, int operation) {
        try {
            return svc.setColorTemperature(operation);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static void setApplicationAndContext(Application application, Context context) {
    }

    public static int updateRgbGamma(float red, float green, float blue) {
        try {
            return Stub.asInterface(ServiceManager.getService("power")).updateRgbGamma(red, green, blue);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static void setStartDreamFromOtherFlag(PowerManager mPowerManager, boolean flag) {
        throw new NoExtAPIException("method not support");
    }

    public static boolean startDream(PowerManager mPowerManager) {
        throw new NoExtAPIException("method not support");
    }

    public static boolean stopDream(PowerManager mPowerManager) {
        throw new NoExtAPIException("method not support");
    }
}
