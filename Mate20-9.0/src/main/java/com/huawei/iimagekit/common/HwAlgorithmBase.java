package com.huawei.iimagekit.common;

import android.os.SystemProperties;
import android.util.Log;

public class HwAlgorithmBase {
    public static final int ALGO_CPUBOXBLUR = 10;
    public static final int ALGO_CPUFASTBLUR = 11;
    public static final int ALGO_GPUBOXBLUR = 2;
    public static final int ALGO_GPUGAUSSBLUR = 1;
    public static final int ALGO_NONEBLUR = 0;
    public static final int ALGO_SHADOW_BOXBLUR = 6;
    public static final int ALGO_SHADOW_GPUBOXBLUR = 9;
    public static final int ALGO_SHADOW_GPUGAUSSBLUR = 8;
    public static final int ALGO_SHADOW_NONESHADOW = 5;
    public static final int BLUR_SUCCESS = 0;
    protected static final boolean DEBUG_ENABLE = false;
    public static final int ERRBLUR_BLUR_DISABLE = 5;
    public static final int ERRBLUR_INPUT = 1;
    public static final int ERRBLUR_INPUT_CONTEXT = 8;
    public static final int ERRBLUR_INPUT_SIZE = 7;
    public static final int ERRBLUR_OUTPUT = 2;
    public static final int ERRBLUR_RADIUS = 4;
    public static final int ERRBLUR_SHADOW_DISABLE = 6;
    public static final int ERRBLUR_SIZE = 3;
    public static final boolean IMAGEKIT_BLUR_PROP = SystemProperties.getBoolean("ro.config.hw_emui_iimagekit_blur", true);
    public static final boolean IMAGEKIT_SHADOW_PROP = SystemProperties.getBoolean("ro.config.hw_emui_iimagekit_shadow", true);
    protected static final int LOG_DEBUG = 2;
    protected static final int LOG_ERROR = 4;
    protected static final int LOG_INFO = 1;
    protected static final int LOG_WARNING = 3;
    public static final int MAX_RADIUS = 25;
    public static final int MIN_RADIUS = 2;
    public static final int MIN_SIZE = 3;
    protected static final String TAG = "iimagekit";

    public void localLog(int logLevel, String logInfo) {
        switch (logLevel) {
            case 3:
                Log.v(TAG, logInfo);
                return;
            case 4:
                Log.e(TAG, logInfo);
                return;
            default:
                return;
        }
    }
}
