package com.android.server.wifi.MSS;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;
import com.android.server.wifi.MSS.HwMSSArbitrager.MSSState;

public class HwMSSUtils {
    private static final /* synthetic */ int[] -com-android-server-wifi-MSS-HwMSSArbitrager$MSSStateSwitchesValues = null;
    private static final boolean HWDBG = true;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_ERROR = 0;
    public static final int LOG_LEVEL_VERBOSE = 3;
    private static int MSSDBG = 2;
    private static final int MSS_DEBUG_ALLOW = 1;
    private static final int MSS_DEBUG_DENY = 0;
    private static final int MSS_DEBUG_UNINITIAL = 2;
    public static final int MSS_MIMO_TO_SISO = 1;
    public static final int MSS_SISO_TO_MIMO = 2;
    private static final String TAG = "HwMSSUtils";

    private static /* synthetic */ int[] -getcom-android-server-wifi-MSS-HwMSSArbitrager$MSSStateSwitchesValues() {
        if (-com-android-server-wifi-MSS-HwMSSArbitrager$MSSStateSwitchesValues != null) {
            return -com-android-server-wifi-MSS-HwMSSArbitrager$MSSStateSwitchesValues;
        }
        int[] iArr = new int[MSSState.values().length];
        try {
            iArr[MSSState.ABSMIMO.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[MSSState.ABSMRC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[MSSState.ABSSWITCHING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[MSSState.MSSMIMO.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[MSSState.MSSSISO.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[MSSState.MSSUNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-server-wifi-MSS-HwMSSArbitrager$MSSStateSwitchesValues = iArr;
        return iArr;
    }

    private static void initMSSDebug() {
        if (MSSDBG == 2) {
            synchronized (HwMSSUtils.class) {
                MSSDBG = SystemProperties.getInt("runtime.hwmss.debug", 0);
            }
        }
    }

    public static void switchToast(Context cxt, MSSState state) {
        String value = "switch to state: UNKNOWN";
        switch (-getcom-android-server-wifi-MSS-HwMSSArbitrager$MSSStateSwitchesValues()[state.ordinal()]) {
            case 1:
                value = "switch to state: ABSMIMO";
                break;
            case 2:
                value = "switch to state: ABSMRC";
                break;
            case 3:
                value = "switch to state: ABSSWITCHING";
                break;
            case 4:
                value = "switch to state: MSSMIMO";
                break;
            case 5:
                value = "switch to state: MSSSISO";
                break;
        }
        toast(cxt, value);
    }

    public static void toast(Context cxt, String value) {
        initMSSDebug();
        if (cxt != null && value != null && MSSDBG == 1) {
            Toast.makeText(cxt, value, 1).show();
        }
    }

    public static void log(int level, String msg) {
        log(level, TAG, msg);
    }

    public static void log(int level, String tag, String msg) {
        initMSSDebug();
        if (level == 0) {
            Log.e(tag, msg);
        } else if (level == 1) {
            Log.d(tag, msg);
        } else if (MSSDBG == 1) {
            Log.d(tag, msg);
        }
    }
}
