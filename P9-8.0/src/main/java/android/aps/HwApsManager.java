package android.aps;

import android.aps.IHwApsManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import java.util.List;

public class HwApsManager implements IApsManager {
    private static final String TAG = "HwApsManager";
    private static HwApsManager sInstance;
    private IHwApsManager mApsService;
    private float mDynamicResolutionRatio = -1.0f;
    private int mDynamiceFPS = -1;

    private HwApsManager() {
    }

    private boolean checkApsManagerService() {
        if (this.mApsService == null) {
            this.mApsService = Stub.asInterface(ServiceManager.getService("aps_service"));
        }
        if (this.mApsService != null) {
            return true;
        }
        Slog.e(TAG, "checkApsManagerService->service is not started yet");
        return false;
    }

    private boolean checkApsInfo(ApsAppInfo info) {
        if (info == null) {
            return false;
        }
        float resolutionRatio = info.getResolutionRatio();
        int fps = info.getFrameRatio();
        int brightnessPercent = info.getBrightnessPercent();
        int texturePercent = info.getTexturePercent();
        if (resolutionRatio <= 0.25f || resolutionRatio > 1.0f) {
            Slog.e(TAG, "check APSinfo invalid resolution ratio =" + resolutionRatio);
            return false;
        } else if (fps < 15 || fps > 60) {
            Slog.e(TAG, "check APSinfo invalid fps  =" + fps);
            return false;
        } else if (brightnessPercent < 50 || brightnessPercent > 100) {
            Slog.e(TAG, "check APS info invalid brightnessPercent=" + brightnessPercent);
            return false;
        } else if (texturePercent >= 50 && texturePercent <= 100) {
            return true;
        } else {
            Slog.e(TAG, "check APS info invalid texturePercent=" + texturePercent);
            return false;
        }
    }

    public static synchronized HwApsManager getDefault() {
        HwApsManager hwApsManager;
        synchronized (HwApsManager.class) {
            if (sInstance == null) {
                sInstance = new HwApsManager();
            }
            hwApsManager = sInstance;
        }
        return hwApsManager;
    }

    public int setLowResolutionMode(int lowResolutionMode) {
        int result = -1;
        if (!checkApsManagerService()) {
            return -2;
        }
        try {
            result = this.mApsService.setLowResolutionMode(lowResolutionMode);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setLowResolutionMode,ex:" + ex);
        }
        return result;
    }

    public int setResolution(String pkgName, float ratio, boolean switchable) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (ratio < 0.25f || 1.0f < ratio) {
            Slog.e(TAG, "setResolution, invalid param ratio = " + ratio);
            return -1;
        }
        int result = -1;
        try {
            result = this.mApsService.setResolution(pkgName, ratio, switchable);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setResolution,ex:" + ex);
        }
        return result;
    }

    public int setDescentGradeResolution(String pkgName, int reduceLevel, boolean switchable) {
        int result = -1;
        try {
            return this.mApsService.setDescentGradeResolution(pkgName, reduceLevel, switchable);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setDescentGradeResolution,ex:" + ex);
            return result;
        }
    }

    public int setFps(String pkgName, int fps) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (fps == -1 || (fps >= 15 && 60 >= fps)) {
            int result = -1;
            try {
                result = this.mApsService.setFps(pkgName, fps);
            } catch (RemoteException ex) {
                Slog.w(TAG, "setFps,ex:" + ex);
            }
            return result;
        }
        Slog.e(TAG, "setFps, invalid param fps = " + fps);
        Log.i(TAG, "APSLog -> setFps:[pkg:" + pkgName + ",fps:" + fps + ",retCode:-1->invalid param]");
        return -1;
    }

    public int setBrightness(String pkgName, int ratioPercent) {
        if (!checkApsManagerService()) {
            return -2;
        }
        int result = -1;
        try {
            result = this.mApsService.setBrightness(pkgName, ratioPercent);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setBrightness,ex:" + ex);
        }
        return result;
    }

    public int setTexture(String pkgName, int ratioPercent) {
        if (!checkApsManagerService()) {
            return -2;
        }
        int result = -1;
        try {
            result = this.mApsService.setTexture(pkgName, ratioPercent);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setTexture,ex:" + ex);
        }
        return result;
    }

    public int setPackageApsInfo(String pkgName, ApsAppInfo info) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (!checkApsInfo(info)) {
            return -1;
        }
        int result = -1;
        try {
            result = this.mApsService.setPackageApsInfo(pkgName, info);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setPackageApsInfo,ex:" + ex);
        }
        return result;
    }

    public ApsAppInfo getPackageApsInfo(String pkgName) {
        if (!checkApsManagerService()) {
            return null;
        }
        ApsAppInfo info = null;
        try {
            info = this.mApsService.getPackageApsInfo(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getPackageApsInfo,ex:" + ex);
        }
        return info;
    }

    public float getResolution(String pkgName) {
        if (!checkApsManagerService()) {
            return -1.0f;
        }
        float resolution = -1.0f;
        try {
            resolution = this.mApsService.getResolution(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getResolution,ex:" + ex);
        }
        return resolution;
    }

    public int getFps(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        int fps = -1;
        try {
            fps = this.mApsService.getFps(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getFps,ex:" + ex);
        }
        return fps;
    }

    public int getBrightness(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        int brightness = -1;
        try {
            brightness = this.mApsService.getBrightness(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "brightness,ex:" + ex);
        }
        return brightness;
    }

    public int getTexture(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        int texture = -1;
        try {
            texture = this.mApsService.getTexture(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getTexture,ex:" + ex);
        }
        return texture;
    }

    public boolean deletePackageApsInfo(String pkgName) {
        if (!checkApsManagerService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mApsService.deletePackageApsInfo(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "deletePackageApsInfo,ex:" + ex);
        }
        return result;
    }

    public int isFeaturesEnabled(int bitmask) {
        if (!checkApsManagerService()) {
            return -1;
        }
        int result = -1;
        try {
            result = this.mApsService.isFeaturesEnabled(bitmask);
        } catch (RemoteException ex) {
            Slog.w(TAG, "isFeaturesEnabled,ex:" + ex);
        }
        return result;
    }

    public boolean disableFeatures(int bitmask) {
        if (!checkApsManagerService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mApsService.disableFeatures(bitmask);
        } catch (RemoteException ex) {
            Slog.w(TAG, "disableFeatures,ex:" + ex);
        }
        return result;
    }

    public boolean enableFeatures(int bitmask) {
        if (!checkApsManagerService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mApsService.enableFeatures(bitmask);
        } catch (RemoteException ex) {
            Slog.w(TAG, "enableFeatures,ex:" + ex);
        }
        return result;
    }

    public List<ApsAppInfo> getAllPackagesApsInfo() {
        if (!checkApsManagerService()) {
            return null;
        }
        List<ApsAppInfo> result = null;
        try {
            result = this.mApsService.getAllPackagesApsInfo();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getAllPackagesApsInfo,ex:" + ex);
        }
        return result;
    }

    public List<String> getAllApsPackages() {
        if (!checkApsManagerService()) {
            return null;
        }
        List<String> result = null;
        try {
            result = this.mApsService.getAllApsPackages();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getAllApsPackages,ex:" + ex);
        }
        return result;
    }

    public boolean updateApsInfo(List<ApsAppInfo> infos) {
        if (!checkApsManagerService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mApsService.updateApsInfo(infos);
        } catch (RemoteException ex) {
            Slog.w(TAG, "updateApsInfo,ex:" + ex);
        }
        return result;
    }

    public boolean registerCallback(IApsManagerServiceCallback callback) {
        if (!checkApsManagerService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mApsService.registerCallback(callback);
        } catch (RemoteException ex) {
            Slog.w(TAG, "registerCallback,ex:" + ex);
        }
        return result;
    }

    public float getSeviceVersion() {
        if (!checkApsManagerService()) {
            return -1.0f;
        }
        float result = 0.0f;
        try {
            result = this.mApsService.getSeviceVersion();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getSeviceVersion,ex:" + ex);
        }
        return result;
    }

    public boolean stopPackages(List<String> pkgs) {
        if (!checkApsManagerService()) {
            return false;
        }
        boolean result = false;
        try {
            result = this.mApsService.stopPackages(pkgs);
        } catch (RemoteException ex) {
            Slog.w(TAG, "stopPackages,ex:" + ex);
        }
        return result;
    }

    public int setDynamicResolutionRatio(float ratio) {
        if (ratio < 0.25f || 1.0f < ratio) {
            Slog.e(TAG, "setDynamicResolutionRatio, invalid param ratio = " + ratio);
            return -1;
        }
        this.mDynamicResolutionRatio = ratio;
        return 0;
    }

    public float getDynamicResolutionRatio() {
        return this.mDynamicResolutionRatio;
    }

    public int setDynamicFPS(int fps) {
        if (fps < 15 || 60 < fps) {
            Slog.e(TAG, "setDynamicFPS, invalid param fps = " + fps);
            return -1;
        }
        this.mDynamiceFPS = fps;
        return 0;
    }

    public int getDynamicFPS() {
        return this.mDynamiceFPS;
    }
}
