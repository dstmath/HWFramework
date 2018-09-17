package android.aps;

import java.util.List;

public interface IApsManager {
    public static final int APS_BRIGHTNESS_MAX = 100;
    public static final int APS_BRIGHTNESS_MIN = 50;
    public static final int APS_ERRNO_SFR_RUNAS_CONFIG = -4;
    public static final int APS_FEATURE_DISABLE = 268435456;
    public static final int APS_FRAMERATE_EXT = -1;
    public static final int APS_FRAMERATE_MAX = 60;
    public static final int APS_FRAMERATE_MIN = 15;
    public static final int APS_INVALID_PARAM = -1;
    public static final int APS_NO_PERMISSION = -3;
    public static final int APS_OK = 0;
    public static final String APS_PERMISSION = "com.huawei.aps.permission.UPDATE_APS_INFO";
    public static final float APS_RESOLUTION_MAX = 1.0f;
    public static final float APS_RESOLUTION_MIN = 0.25f;
    public static final String APS_SERVICE = "aps_service";
    public static final int APS_SERVICE_NOTREADY = -2;
    public static final int APS_TEXTURE_MAX = 100;
    public static final int APS_TEXTURE_MIN = 50;
    public static final int BRIGHTNESS = 8;
    public static final int FRAMERATE = 2;
    public static final int RESOLUTION = 1;
    public static final int TEXTURE = 4;

    boolean deletePackageApsInfo(String str);

    boolean disableFeatures(int i);

    boolean enableFeatures(int i);

    List<String> getAllApsPackages();

    List<ApsAppInfo> getAllPackagesApsInfo();

    int getBrightness(String str);

    int getDynamicFPS();

    float getDynamicResolutionRatio();

    int getFps(String str);

    ApsAppInfo getPackageApsInfo(String str);

    float getResolution(String str);

    float getSeviceVersion();

    int getTexture(String str);

    int isFeaturesEnabled(int i);

    boolean registerCallback(IApsManagerServiceCallback iApsManagerServiceCallback);

    int setBrightness(String str, int i);

    int setDescentGradeResolution(String str, int i, boolean z);

    int setDynamicFPS(int i);

    int setDynamicResolutionRatio(float f);

    int setFps(String str, int i);

    int setLowResolutionMode(int i);

    int setPackageApsInfo(String str, ApsAppInfo apsAppInfo);

    int setResolution(String str, float f, boolean z);

    int setTexture(String str, int i);

    boolean stopPackages(List<String> list);

    boolean updateApsInfo(List<ApsAppInfo> list);
}
