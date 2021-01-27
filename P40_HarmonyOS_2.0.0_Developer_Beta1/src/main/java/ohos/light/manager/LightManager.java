package ohos.light.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.light.common.LightColor;
import ohos.light.common.LightConstant;
import ohos.light.common.LightEffectUtil;

public class LightManager {
    private static final int DISABLE_LIGHT_OK = 0;
    private static final int ENABLE_LIGHT_OK = 0;
    private static final int GREEN_GAIN = 256;
    private static final Object LIGHT_INIT_LOCK = new Object();
    private static final int NATIVE_INIT_OK = 0;
    private static final int RED_GAIN = 65536;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218113809, "LightManager");
    private static boolean hasGetLightIdList = false;
    private static volatile LightManager instance;
    private static volatile boolean isInitNative = false;
    private static List<Integer> lightIdVector = new ArrayList();

    private static native int nativeClassInit();

    private static native int nativeDisableLight(int i);

    private static native int nativeEnableLight(int i, long j, int i2, int i3);

    private static native int nativeEnableLight(int i, String str);

    private static native ArrayList<Integer> nativeGetAllLight();

    private static native boolean nativeLightEffectSupport(int i, String str);

    static {
        System.loadLibrary("light_jni.z");
    }

    private LightManager() {
    }

    public static LightManager getInstance() {
        if (instance == null) {
            HiLog.debug(TAG, "getInstance start instance class load", new Object[0]);
            synchronized (LightManager.class) {
                if (instance == null) {
                    instance = new LightManager();
                }
            }
            HiLog.debug(TAG, "getInstance end instance class load", new Object[0]);
        }
        return instance;
    }

    public boolean isSupport(int i) {
        if (!isNativeInitSuccess()) {
            return false;
        }
        return isLightIdValid(i);
    }

    public List<Integer> getLightIdList() {
        if (!isNativeInitSuccess()) {
            return Collections.emptyList();
        }
        if (!lightIdVector.isEmpty()) {
            return lightIdVector;
        }
        HiLog.error(TAG, "getLightIdList the lightId list cannot be empty", new Object[0]);
        return Collections.emptyList();
    }

    public boolean isEffectSupport(int i, String str) {
        if (!isNativeInitSuccess() || !isLightIdValid(i) || !isLightEffectIdValid(str)) {
            return false;
        }
        if (nativeLightEffectSupport(i, str)) {
            return true;
        }
        HiLog.error(TAG, "isEffectSupport the light effect is not support", new Object[0]);
        return false;
    }

    public boolean turnOn(int i, LightEffectUtil lightEffectUtil) {
        HiLog.debug(TAG, "light turnOn with lightEffect begin", new Object[0]);
        if (!isNativeInitSuccess() || !isLightIdValid(i)) {
            return false;
        }
        if (lightEffectUtil == null) {
            HiLog.error(TAG, "turnOn the light lightEffect is invalid", new Object[0]);
            return false;
        }
        LightColor color = lightEffectUtil.getColor();
        if (nativeEnableLight(i, (((long) color.getRed()) * 65536) + (((long) color.getGreen()) * 256) + ((long) color.getBlue()), lightEffectUtil.getOnMs(), lightEffectUtil.getOffMs()) != 0) {
            HiLog.error(TAG, "the light turnOn with lightEffect is failed", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "light turnOn with lightEffect end", new Object[0]);
        return true;
    }

    public boolean turnOn(int i, String str) {
        HiLog.debug(TAG, "light turnOn with effectId begin", new Object[0]);
        if (!isNativeInitSuccess() || !isLightIdValid(i) || !isLightEffectIdValid(str)) {
            return false;
        }
        if (nativeEnableLight(i, str) != 0) {
            HiLog.error(TAG, "the light turnOn with effectId is failed", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "light turnOn with effectId end", new Object[0]);
        return true;
    }

    public boolean turnOff(int i) {
        HiLog.debug(TAG, "light turnOff begin", new Object[0]);
        if (!isNativeInitSuccess() || !isLightIdValid(i)) {
            return false;
        }
        if (nativeDisableLight(i) != 0) {
            HiLog.error(TAG, "light turnOff failed", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "light turnOff end", new Object[0]);
        return true;
    }

    private boolean isNativeInitSuccess() {
        synchronized (LIGHT_INIT_LOCK) {
            if (!isInitNative) {
                if (nativeClassInit() == 0) {
                    isInitNative = true;
                    getAllLight();
                    HiLog.debug(TAG, "isNativeInitSuccess nativeClassInit success", new Object[0]);
                } else {
                    HiLog.error(TAG, "isNativeInitSuccess nativeClassInit failed", new Object[0]);
                    return false;
                }
            }
            return true;
        }
    }

    private static void getAllLight() {
        if (!hasGetLightIdList) {
            lightIdVector = nativeGetAllLight();
            hasGetLightIdList = true;
            HiLog.debug(TAG, "getAllLight get light id list is success", new Object[0]);
        }
    }

    private boolean isLightIdValid(int i) {
        if (lightIdVector.contains(Integer.valueOf(i))) {
            return true;
        }
        HiLog.error(TAG, "isLightIdValid the light Id is not exist", new Object[0]);
        return false;
    }

    private boolean isLightEffectIdValid(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(TAG, "isLightEffectIdValid the light effectId cannot be empty", new Object[0]);
            return false;
        } else if (LightConstant.LIGHT_EFFECT_ID_LIST.contains(str)) {
            return true;
        } else {
            HiLog.error(TAG, "isLightEffectIdValid the light effectId is not exist", new Object[0]);
            return false;
        }
    }
}
