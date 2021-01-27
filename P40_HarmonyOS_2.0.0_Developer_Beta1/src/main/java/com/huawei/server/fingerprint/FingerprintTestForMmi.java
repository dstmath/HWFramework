package com.huawei.server.fingerprint;

import android.util.Log;
import com.huawei.android.biometric.FingerprintSupportEx;
import com.huawei.android.biometric.TouchscreenEx;
import com.huawei.displayengine.DisplayEngineManager;
import java.util.NoSuchElementException;

public class FingerprintTestForMmi {
    private static final int DISABLE_THP_FP = 0;
    private static final int ENABLE_THP_FP = 1;
    private static final int FLAG_THP_FP = 2;
    private static final int INVALID_VALUE = -1;
    private static final int[] NEED_OPEN_HBM_MODES = {FingerprintSupportEx.MMI_TYPE_SNR_SINGAL_IMAGE_UD, FingerprintSupportEx.MMI_TYPE_SNR_WHITE_IMAGE_UD, FingerprintSupportEx.MMI_TYPE_BUBBLE_TEST_UD, FingerprintSupportEx.MMI_TYPE_OPTICAL_CALIBRARION_UD, FingerprintSupportEx.MMI_TYPE_CALIBRARION_OR_LOCATION_UD, FingerprintSupportEx.MMI_TYPE_LOCATION_CIRCLE_TEST_UD, FingerprintSupportEx.MMI_TYPE_REMOVE_LOCATION_CIRCLE, FingerprintSupportEx.MMI_TYPE_ENROL_IDENITFY_TEST_UD};
    private static final int OPEN_HBM_DELAY = 20;
    private static final String TAG = "FpTest";
    private static final String THP_FP_DISABLE_CONFIG = "1,0";
    private static final String THP_FP_ENABLE_CONFIG = "1,1";
    private static int sCurrentTpFpState = 0;
    private static DisplayEngineManager sDisplayEngineManager = null;
    private static boolean sIsHbm = false;
    private static TouchscreenEx sProxy = null;

    public FingerprintTestForMmi(TouchscreenEx proxy, DisplayEngineManager displayEngineManager) {
        sProxy = proxy;
        sDisplayEngineManager = displayEngineManager;
    }

    public int testRun(int type, String param) {
        int ret;
        if (type == FingerprintSupportEx.MMI_TYPE_CANCEL_TEST_UD) {
            closeHbmMode();
        }
        if (isNeedOpenHbmMode(type)) {
            openHbmMode();
        }
        if (type == FingerprintSupportEx.MMI_TYPE_INTERRUPT_TEST_UD && sCurrentTpFpState != 1) {
            setTpState(1);
        }
        if (param == null) {
            ret = HwFpServiceToHalUtils.sendCommandToHal(type, -1);
        } else {
            ret = HwFpServiceToHalUtils.sendDataToHal(type, param);
        }
        if (type == FingerprintSupportEx.MMI_TYPE_GET_RESULT_UD && (ret < FingerprintSupportEx.FP_MMI_TESTING || ret > FingerprintSupportEx.FP_MMI_TEST_EXIT_LOCATION_TEST)) {
            closeHbmMode();
            if (sCurrentTpFpState == 1) {
                setTpState(0);
            }
        }
        Log.i(TAG, "FPT cmd = " + type + ", ret = " + ret);
        return ret;
    }

    private boolean isNeedOpenHbmMode(int testType) {
        for (int type : NEED_OPEN_HBM_MODES) {
            if (testType == type) {
                return true;
            }
        }
        return false;
    }

    private void closeHbmMode() {
        if (sIsHbm) {
            try {
                if (sDisplayEngineManager == null) {
                    sDisplayEngineManager = new DisplayEngineManager();
                }
                sDisplayEngineManager.setScene(FingerprintSupportEx.getDeSceneFingerprintHbm(), 0);
                sIsHbm = false;
            } catch (SecurityException e) {
                Log.e(TAG, "close Hbm Mode fail se.");
            } catch (IllegalArgumentException e2) {
                Log.e(TAG, "close Hbm Mode fail ig.");
            }
        }
    }

    private void openHbmMode() {
        if (!sIsHbm) {
            try {
                if (sDisplayEngineManager == null) {
                    sDisplayEngineManager = new DisplayEngineManager();
                }
                int dbvLevel = HwFpServiceToHalUtils.sendCommandToHal(FingerprintSupportEx.MMI_TYPE_GET_HIGHLIGHT_LEVEL, -1);
                Log.i(TAG, "dbvLevel = " + dbvLevel);
                sDisplayEngineManager.setScene(FingerprintSupportEx.getDeSceneFingerprintHbm(), dbvLevel);
                sIsHbm = true;
                try {
                    Thread.sleep(20);
                    Log.i(TAG, "delay 20ms for scence");
                } catch (InterruptedException e) {
                    Log.e(TAG, "delay 20ms for scence again");
                }
            } catch (SecurityException e2) {
                Log.e(TAG, "open Hbm Mode fail se.");
            } catch (IllegalArgumentException e3) {
                Log.e(TAG, "open Hbm Mode fail ig.");
            }
        }
    }

    private void setTpState(int state) {
        if (state == 1) {
            try {
                if (sProxy.hwSetFeatureConfig(2, THP_FP_ENABLE_CONFIG) == 0) {
                    sCurrentTpFpState = state;
                    Log.i(TAG, "Set tp enable success");
                    return;
                }
            } catch (NoSuchElementException e) {
                Log.e(TAG, "Set tp service not found.");
                return;
            }
        }
        if (state == 0 && sProxy.hwSetFeatureConfig(2, THP_FP_DISABLE_CONFIG) == 0) {
            sCurrentTpFpState = state;
            Log.i(TAG, "Set tp disable success");
            return;
        }
        Log.e(TAG, "Set tp error. new state = " + state + " current state = " + sCurrentTpFpState);
    }
}
