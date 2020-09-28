package android.aps;

import java.util.List;

public interface IApsManager {
    public static final int APS_BRIGHTNESS_MAX = 100;
    public static final int APS_BRIGHTNESS_MIN = 50;
    public static final int APS_CALLBACK_CODE_BRIGHTNESS = 3;
    public static final int APS_CALLBACK_CODE_FB_SKIP = 4;
    public static final int APS_CALLBACK_CODE_FPS = 0;
    public static final int APS_CALLBACK_CODE_HIGHP_TO_LOWP = 5;
    public static final int APS_CALLBACK_CODE_MIPMAP = 7;
    public static final int APS_CALLBACK_CODE_RESOLUTION = 1;
    public static final int APS_CALLBACK_CODE_SHADOWMAP = 6;
    public static final int APS_CALLBACK_CODE_TEXTURE = 2;
    public static final int APS_CALLBACK_ENLARGE_FACTOR = 100000;
    public static final int APS_ERRNO_CALLBACK_FAIL = -5;
    public static final int APS_ERRNO_RUNAS_CONFIG = -4;
    public static final int APS_ERRNO_UNKNOW_ERROR = -6;
    public static final int APS_FEATURE_DISABLE = 0;
    public static final int APS_FRAMERATE_EXT = -1;
    public static final int APS_FRAMERATE_MAX = 120;
    public static final int APS_FRAMERATE_MIN = 15;
    public static final int APS_INVALID_PARAM = -1;
    public static final int APS_MIPMAP_SWITCH_1_ON = 1;
    public static final int APS_MIPMAP_SWITCH_2_ON = 2;
    public static final int APS_MIPMAP_SWITCH_3_ON = 3;
    public static final int APS_MIPMAP_SWITCH_OFF = 0;
    public static final int APS_NO_PERMISSION = -3;
    public static final int APS_OK = 0;
    public static final String APS_PERMISSION = "com.huawei.aps.permission.UPDATE_APS_INFO";
    public static final float APS_RESOLUTION_MAX = 1.0f;
    public static final float APS_RESOLUTION_MIN = 0.25f;
    public static final int APS_SDR_SCREEN_COMPAT = 32768;
    public static final String APS_SERVICE = "aps_service";
    public static final int APS_SERVICE_NOTREADY = -2;
    public static final int APS_SHADOWMAP_SWITCH_1_ON = 1;
    public static final int APS_SHADOWMAP_SWITCH_2_ON = 2;
    public static final int APS_SHADOWMAP_SWITCH_3_ON = 3;
    public static final int APS_SHADOWMAP_SWITCH_OFF = 0;
    public static final int APS_TEXTURE_MAX = 100;
    public static final int APS_TEXTURE_MIN = 50;
    public static final int BRIGHTNESS = 8;
    public static final int FRAMERATE = 2;
    public static final int HISI_MASK_FB_SKIP = 16;
    public static final int HISI_MASK_HIGHP_TO_LOWP = 32;
    public static final int HISI_MASK_MIPMAP = 128;
    public static final int HISI_MASK_SHADOWMAP = 64;
    public static final int RESOLUTION = 1;
    public static final int TEXTURE = 4;

    boolean deletePackageApsInfo(String str);

    boolean disableFeatures(int i);

    boolean enableFeatures(int i);

    List<String> getAllApsPackages();

    List<ApsAppInfo> getAllPackagesApsInfo();

    int getBrightness(String str);

    int getDynamicFps(String str);

    float getDynamicResolutionRatio(String str);

    int getFps(String str);

    int getMaxFps(String str);

    ApsAppInfo getPackageApsInfo(String str);

    float getResolution(String str);

    float getSeviceVersion();

    int getTexture(String str);

    int isFeaturesEnabled(int i);

    boolean registerCallback(String str, IApsManagerServiceCallback iApsManagerServiceCallback);

    int setBrightness(String str, int i);

    int setDynamicFps(String str, int i);

    int setDynamicResolutionRatio(String str, float f);

    int setFps(String str, int i);

    int setLowResolutionMode(int i);

    int setMaxFps(String str, int i);

    int setPackageApsInfo(String str, ApsAppInfo apsAppInfo);

    int setResolution(String str, float f, boolean z);

    int setTexture(String str, int i);

    boolean stopPackages(List<String> list);

    boolean updateApsInfo(List<ApsAppInfo> list);
}
