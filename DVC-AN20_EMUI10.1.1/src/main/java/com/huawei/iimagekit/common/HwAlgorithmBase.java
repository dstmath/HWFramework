package com.huawei.iimagekit.common;

import android.os.SystemProperties;
import android.util.Log;

public class HwAlgorithmBase {
    public static final int ALGO_CPUBOXBLUR = 3;
    public static final int ALGO_CPUFASTBLUR = 4;
    public static final int ALGO_CPUGAUSSIANBLUR = 5;
    public static final int ALGO_NONEBLUR = 0;
    public static final int ALGO_SHADOW_BOXBLUR = 2;
    public static final int ALGO_SHADOW_NONESHADOW = 1;
    public static final int BLUR_SUCCESS = 0;
    public static final int ERRBLUR_BLUR_DISABLE = 5;
    public static final int ERRBLUR_INPUT = 1;
    public static final int ERRBLUR_INPUT_CONTEXT = 8;
    public static final int ERRBLUR_INPUT_SIZE = 7;
    public static final int ERRBLUR_OUTPUT = 2;
    public static final int ERRBLUR_RADIUS = 4;
    public static final int ERRBLUR_SHADOW_DISABLE = 6;
    public static final int ERRBLUR_SIZE = 3;
    protected static final boolean IS_DEBUG_ENABLE = false;
    public static final boolean IS_IMAGEKIT_BLUR_PROP = SystemProperties.getBoolean("ro.config.hw_emui_iimagekit_blur", true);
    public static final boolean IS_IMAGEKIT_SHADOW_PROP = SystemProperties.getBoolean("ro.config.hw_emui_iimagekit_shadow", true);
    public static final int MAX_RADIUS = 25;
    public static final int MIN_RADIUS = 2;
    public static final int MIN_SIZE = 3;
    protected static final String TAG = "iimagekit";

    public enum LogLevel {
        LOG_INFO,
        LOG_DEBUG,
        LOG_WARNING,
        LOG_ERROR
    }

    /* renamed from: com.huawei.iimagekit.common.HwAlgorithmBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$iimagekit$common$HwAlgorithmBase$LogLevel = new int[LogLevel.values().length];

        static {
            try {
                $SwitchMap$com$huawei$iimagekit$common$HwAlgorithmBase$LogLevel[LogLevel.LOG_INFO.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$iimagekit$common$HwAlgorithmBase$LogLevel[LogLevel.LOG_DEBUG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$iimagekit$common$HwAlgorithmBase$LogLevel[LogLevel.LOG_WARNING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$iimagekit$common$HwAlgorithmBase$LogLevel[LogLevel.LOG_ERROR.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static void localLog(LogLevel logLevel, String logInfo) {
        int i = AnonymousClass1.$SwitchMap$com$huawei$iimagekit$common$HwAlgorithmBase$LogLevel[logLevel.ordinal()];
        if (i != 1 && i != 2) {
            if (i == 3) {
                Log.v(TAG, logInfo);
            } else if (i == 4) {
                Log.e(TAG, logInfo);
            }
        }
    }
}
