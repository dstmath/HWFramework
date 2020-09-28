package com.huawei.android.os;

import android.app.Application;
import android.content.Context;
import android.os.IPowerManager;
import android.os.PersistableBundle;
import android.os.PowerManager;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.displayengine.DisplayEngineManager;

public class PowerManagerCustEx {
    private static final int BLUE_INDEX = 2;
    private static final int BUFFER_LENGTH = 12;
    private static final int GREEN_INDEX = 1;
    private static final int RED_INDEX = 0;
    private static final int RGB_BITMASK = 32768;
    private static final int XCC_COEF_LENGTH = 3;

    public static int setColorTemperature(IPowerManager service, int operation) {
        return -1;
    }

    public static void setApplicationAndContext(Application application, Context context) {
    }

    public static int updateRgbGamma(float red, float green, float blue) {
        return -1;
    }

    public static int updateRgbGamma(float red, float green, float blue, int featureType, int dataType) {
        if (featureType < 0 || featureType >= 37 || dataType < 0 || dataType >= 15 || red < 0.0f || red > 1.0f || green < 0.0f || green > 1.0f || blue < 0.0f || blue > 1.0f) {
            return -1;
        }
        DisplayEngineManager displayEngineManager = new DisplayEngineManager();
        if (1 == displayEngineManager.getSupported(featureType)) {
            PersistableBundle bundle = new PersistableBundle();
            bundle.putIntArray("Buffer", new int[]{(int) (red * 32768.0f), (int) (green * 32768.0f), (int) (32768.0f * blue)});
            bundle.putInt("BufferLength", BUFFER_LENGTH);
            if (displayEngineManager.setData(dataType, bundle) != 0) {
                return -1;
            }
        } else {
            updateRgbGamma(red, green, blue);
        }
        return 0;
    }

    public static void setStartDreamFromOtherFlag(PowerManager powerManager, boolean flag) {
        throw new NoExtAPIException("method not support");
    }

    public static boolean startDream(PowerManager powerManager) {
        throw new NoExtAPIException("method not support");
    }

    public static boolean stopDream(PowerManager powerManager) {
        throw new NoExtAPIException("method not support");
    }
}
