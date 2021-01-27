package ohos.vibrator.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.vibrator.common.VibratorConstant;
import ohos.vibrator.common.VibratorEffectUtil;

public class VibratorManager {
    private static final int DISABLE_VIBRATOR_OK = 0;
    private static final int ENABLE_VIBRATOR_OK = 0;
    private static final int INTENSITIES_LEN = 0;
    private static final int NATIVE_INIT_OK = 0;
    private static final int SET_VIBRATOR_OK = 0;
    private static final int SINGLE_EFFECT_COUNT = 1;
    private static final int SINGLE_EFFECT_LEN = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218113810, "VibratorManager");
    private static final int TIMING_LEN = 0;
    private static final Object VIBRATOR_INIT_LOCK = new Object();
    private static boolean hasGetVibratorIdList = false;
    private static volatile VibratorManager instance;
    private static volatile boolean isInitNative = false;
    private static List<Integer> vibratorIdVector = new ArrayList();

    private static native int nativeClassInit();

    private static native int nativeDisableVibrator(int i, String str);

    private static native int nativeEnableVibrator(int i, String str, boolean z);

    private static native int nativeEnableVibrator(int i, int[] iArr, int[] iArr2, int i2);

    private static native ArrayList<Integer> nativeGetAllVibrator();

    private static native String nativeGetVibratorParameter(int i, String str);

    private static native int nativeSetVibratorParameter(int i, String str);

    private static native int nativeVibrate(int i, int i2);

    private static native boolean nativeVibratorEffectSupport(int i, String str);

    static {
        System.loadLibrary("vibrator_jni.z");
    }

    private VibratorManager() {
    }

    public static VibratorManager getInstance() {
        if (instance == null) {
            HiLog.debug(TAG, "getInstance instance class load begin", new Object[0]);
            synchronized (VibratorManager.class) {
                if (instance == null) {
                    instance = new VibratorManager();
                }
            }
            HiLog.debug(TAG, "getInstance instance class load end", new Object[0]);
        }
        return instance;
    }

    public List<Integer> getVibratorIdList() {
        if (!isNativeInitSuccess()) {
            return Collections.emptyList();
        }
        if (!vibratorIdVector.isEmpty()) {
            return vibratorIdVector;
        }
        HiLog.error(TAG, "getVibratorIdList the vibratorId list cannot be empty", new Object[0]);
        return Collections.emptyList();
    }

    public boolean isSupport(int i) {
        if (!isNativeInitSuccess()) {
            return false;
        }
        return isVibratorIdValid(i);
    }

    public boolean isEffectSupport(int i, String str) {
        if (!isNativeInitSuccess() || !isVibratorIdValid(i) || !isEffectIdValid(str)) {
            return false;
        }
        if (nativeVibratorEffectSupport(i, str)) {
            return true;
        }
        HiLog.error(TAG, "isEffectSupport the vibrator effect is not support", new Object[0]);
        return false;
    }

    public boolean vibrate(int i, String str) {
        return vibrate(i, str, false);
    }

    public boolean vibrate(int i, String str, boolean z) {
        if (!isNativeInitSuccess() || !isVibratorIdValid(i) || !isEffectIdValid(str)) {
            return false;
        }
        if (nativeEnableVibrator(i, str, z) == 0) {
            return true;
        }
        HiLog.error(TAG, "vibrator enable with effect ID failed", new Object[0]);
        return false;
    }

    public boolean vibrate(int i, int i2) {
        if (!isNativeInitSuccess() || !isVibratorIdValid(i)) {
            return false;
        }
        if (nativeVibrate(i, i2) == 0) {
            return true;
        }
        HiLog.error(TAG, "vibrator enable with duration failed", new Object[0]);
        return false;
    }

    public boolean vibrate(int i, VibratorEffectUtil vibratorEffectUtil) {
        int i2;
        int[] iArr;
        int[] iArr2;
        if (!(isNativeInitSuccess() && isVibratorIdValid(i))) {
            return false;
        }
        if (vibratorEffectUtil == null) {
            HiLog.error(TAG, "vibrator define effect is invalid", new Object[0]);
            return false;
        }
        if (vibratorEffectUtil.getIntensities().length != 0) {
            iArr = vibratorEffectUtil.getTiming();
            iArr2 = vibratorEffectUtil.getIntensities();
            i2 = vibratorEffectUtil.getCount();
        } else if (vibratorEffectUtil.getTiming().length != 0) {
            iArr = vibratorEffectUtil.getTiming();
            iArr2 = new int[iArr.length];
            for (int i3 = 0; i3 < iArr.length; i3++) {
                iArr2[i3] = vibratorEffectUtil.getIntensity();
            }
            i2 = vibratorEffectUtil.getCount();
        } else {
            int[] iArr3 = new int[2];
            int[] iArr4 = new int[2];
            iArr3[1] = vibratorEffectUtil.getDuration();
            iArr4[1] = vibratorEffectUtil.getIntensity();
            i2 = 1;
            iArr2 = iArr4;
            iArr = iArr3;
        }
        if (nativeEnableVibrator(i, iArr, iArr2, i2) == 0) {
            return true;
        }
        HiLog.error(TAG, "vibrator vibrate enable failed", new Object[0]);
        return false;
    }

    public boolean stop(int i, String str) {
        HiLog.debug(TAG, "stop begin", new Object[0]);
        if (!isNativeInitSuccess() || !isVibratorIdValid(i)) {
            return false;
        }
        if (!VibratorConstant.VIBRATOR_STOP_MODE_LIST.contains(str)) {
            HiLog.error(TAG, "stop mode is invalid", new Object[0]);
            return false;
        } else if (nativeDisableVibrator(i, str) != 0) {
            HiLog.error(TAG, "stop vibrate failed", new Object[0]);
            return false;
        } else {
            HiLog.debug(TAG, "stop end", new Object[0]);
            return true;
        }
    }

    public boolean setVibratorParameter(int i, String str) {
        HiLog.debug(TAG, "setVibratorParameter begin", new Object[0]);
        if (!isNativeInitSuccess() || !isVibratorIdValid(i)) {
            return false;
        }
        if (!VibratorConstant.VIBRATOR_SET_COMMAND_PARAMETER_LIST.contains(str)) {
            HiLog.error(TAG, "setVibratorParameter the vibrator's set command is not exist", new Object[0]);
            return false;
        } else if (nativeSetVibratorParameter(i, str) != 0) {
            HiLog.error(TAG, "setVibratorParameter the vibrator set command is failed", new Object[0]);
            return false;
        } else {
            HiLog.debug(TAG, "setVibratorParameter end", new Object[0]);
            return true;
        }
    }

    public String getVibratorParameter(int i, String str) {
        HiLog.debug(TAG, "getVibratorParameter begin", new Object[0]);
        if (!isNativeInitSuccess() || !isVibratorIdValid(i)) {
            return "";
        }
        if (!VibratorConstant.VIBRATOR_GET_COMMAND_PARAMETER_LIST.contains(str)) {
            HiLog.error(TAG, "getVibratorParameter the vibrator's get command is not exist", new Object[0]);
            return "";
        }
        HiLog.debug(TAG, "getVibratorParameter end", new Object[0]);
        return nativeGetVibratorParameter(i, str);
    }

    private boolean isNativeInitSuccess() {
        synchronized (VIBRATOR_INIT_LOCK) {
            if (!isInitNative) {
                if (nativeClassInit() == 0) {
                    isInitNative = true;
                    getAllVibrator();
                    HiLog.debug(TAG, "isNativeInitSuccess nativeClassInit success", new Object[0]);
                } else {
                    HiLog.error(TAG, "isNativeInitSuccess nativeClassInit failed", new Object[0]);
                    return false;
                }
            }
            return true;
        }
    }

    private static void getAllVibrator() {
        if (!hasGetVibratorIdList) {
            vibratorIdVector = nativeGetAllVibrator();
            hasGetVibratorIdList = true;
            HiLog.debug(TAG, "getAllVibrator get vibrator id list is successful", new Object[0]);
        }
        if (vibratorIdVector.isEmpty()) {
            HiLog.error(TAG, "getAllVibrator get vibrator id list is failed", new Object[0]);
        }
    }

    private boolean isVibratorIdValid(int i) {
        if (vibratorIdVector.contains(Integer.valueOf(i))) {
            return true;
        }
        HiLog.error(TAG, "isVibratorIdValid the vibrator Id is not exist", new Object[0]);
        return false;
    }

    private boolean isEffectIdValid(String str) {
        if (VibratorConstant.VIBRATOR_EFFECT_ID_LIST.contains(str)) {
            return true;
        }
        HiLog.error(TAG, "isEffectIdValid the vibrator effect Id is invalid", new Object[0]);
        return false;
    }
}
