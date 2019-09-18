package com.huawei.android.os;

import android.app.Application;
import android.content.Context;
import android.os.IPowerManager;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.displayengine.DisplayEngineManager;

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
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).updateRgbGamma(red, green, blue);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static int updateRgbGamma(float red, float green, float blue, int featureType, int dataType) {
        if (featureType < 0 || featureType >= 32 || dataType < 0 || dataType >= 12 || red < 0.0f || red > 1.0f || green < 0.0f || green > 1.0f || blue < 0.0f || blue > 1.0f) {
            return -1;
        }
        DisplayEngineManager displayEngineManager = new DisplayEngineManager();
        if (1 == displayEngineManager.getSupported(featureType)) {
            PersistableBundle bundle = new PersistableBundle();
            bundle.putIntArray("Buffer", new int[]{(int) (red * 32768.0f), (int) (green * 32768.0f), (int) (32768.0f * blue)});
            bundle.putInt("BufferLength", 12);
            if (displayEngineManager.setData(dataType, bundle) != 0) {
                return -1;
            }
        } else {
            updateRgbGamma(red, green, blue);
        }
        return 0;
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
