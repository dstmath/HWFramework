package android.aps;

import android.aps.IHwApsManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import java.util.List;

public class HwApsManager implements IApsManager {
    private static final String TAG = "HwApsManager";
    private static HwApsManager sInstance;
    private IHwApsManager mApsService;

    private HwApsManager() {
    }

    private boolean checkApsManagerService() {
        if (this.mApsService == null) {
            this.mApsService = IHwApsManager.Stub.asInterface(ServiceManager.getService("aps_service"));
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
        if ((resolutionRatio <= 0.25f || resolutionRatio > 1.0f) && (fps < 15 || fps > 120)) {
            Slog.e(TAG, "check APSinfo invalid resolution ratio =" + resolutionRatio + "; fps = " + fps);
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
        if (!checkApsManagerService()) {
            return -2;
        }
        try {
            return this.mApsService.setLowResolutionMode(lowResolutionMode);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setLowResolutionMode,ex:" + ex);
            return -1;
        }
    }

    public int setResolution(String pkgName, float ratio, boolean switchable) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (ratio < 0.25f || 1.0f < ratio) {
            Slog.e(TAG, "setResolution, invalid param ratio = " + ratio);
            return -1;
        }
        try {
            return this.mApsService.setResolution(pkgName, ratio, switchable);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setResolution,ex:" + ex);
            return -1;
        }
    }

    public int setFps(String pkgName, int fps) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (fps < 15 || 120 < fps) {
            Slog.e(TAG, "setFps, invalid param fps = " + fps);
            return -1;
        }
        try {
            return this.mApsService.setFps(pkgName, fps);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setFps,ex:" + ex);
            return -1;
        }
    }

    public int setMaxFps(String pkgName, int fps) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (fps < 15 || 120 < fps) {
            Slog.e(TAG, "setMaxFps, invalid param fps = " + fps);
            return -1;
        }
        try {
            return this.mApsService.setMaxFps(pkgName, fps);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setMaxFps,ex:" + ex);
            return -1;
        }
    }

    public int setBrightness(String pkgName, int ratioPercent) {
        if (!checkApsManagerService()) {
            return -2;
        }
        try {
            return this.mApsService.setBrightness(pkgName, ratioPercent);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setBrightness,ex:" + ex);
            return -1;
        }
    }

    public int setTexture(String pkgName, int ratioPercent) {
        if (!checkApsManagerService()) {
            return -2;
        }
        try {
            return this.mApsService.setTexture(pkgName, ratioPercent);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setTexture,ex:" + ex);
            return -1;
        }
    }

    public int setPackageApsInfo(String pkgName, ApsAppInfo info) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (!checkApsInfo(info)) {
            return -1;
        }
        try {
            return this.mApsService.setPackageApsInfo(pkgName, info);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setPackageApsInfo,ex:" + ex);
            return -1;
        }
    }

    public ApsAppInfo getPackageApsInfo(String pkgName) {
        if (!checkApsManagerService()) {
            return null;
        }
        try {
            return this.mApsService.getPackageApsInfo(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getPackageApsInfo,ex:" + ex);
            return null;
        }
    }

    public float getResolution(String pkgName) {
        if (!checkApsManagerService()) {
            return -1.0f;
        }
        try {
            return this.mApsService.getResolution(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getResolution,ex:" + ex);
            return -1.0f;
        }
    }

    public int getFps(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        try {
            return this.mApsService.getFps(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getFps,ex:" + ex);
            return -1;
        }
    }

    public int getMaxFps(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        try {
            return this.mApsService.getMaxFps(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getMaxFps,ex:" + ex);
            return -1;
        }
    }

    public int getBrightness(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        try {
            return this.mApsService.getBrightness(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "brightness,ex:" + ex);
            return -1;
        }
    }

    public int getTexture(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        try {
            return this.mApsService.getTexture(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getTexture,ex:" + ex);
            return -1;
        }
    }

    public boolean deletePackageApsInfo(String pkgName) {
        if (!checkApsManagerService()) {
            return false;
        }
        try {
            return this.mApsService.deletePackageApsInfo(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "deletePackageApsInfo,ex:" + ex);
            return false;
        }
    }

    public int isFeaturesEnabled(int bitmask) {
        if (!checkApsManagerService()) {
            return -1;
        }
        try {
            return this.mApsService.isFeaturesEnabled(bitmask);
        } catch (RemoteException ex) {
            Slog.w(TAG, "isFeaturesEnabled,ex:" + ex);
            return -1;
        }
    }

    public boolean disableFeatures(int bitmask) {
        if (!checkApsManagerService()) {
            return false;
        }
        try {
            return this.mApsService.disableFeatures(bitmask);
        } catch (RemoteException ex) {
            Slog.w(TAG, "disableFeatures,ex:" + ex);
            return false;
        }
    }

    public boolean enableFeatures(int bitmask) {
        if (!checkApsManagerService()) {
            return false;
        }
        try {
            return this.mApsService.enableFeatures(bitmask);
        } catch (RemoteException ex) {
            Slog.w(TAG, "enableFeatures,ex:" + ex);
            return false;
        }
    }

    public List<ApsAppInfo> getAllPackagesApsInfo() {
        if (!checkApsManagerService()) {
            return null;
        }
        try {
            return this.mApsService.getAllPackagesApsInfo();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getAllPackagesApsInfo,ex:" + ex);
            return null;
        }
    }

    public List<String> getAllApsPackages() {
        if (!checkApsManagerService()) {
            return null;
        }
        try {
            return this.mApsService.getAllApsPackages();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getAllApsPackages,ex:" + ex);
            return null;
        }
    }

    public boolean updateApsInfo(List<ApsAppInfo> infos) {
        if (!checkApsManagerService()) {
            return false;
        }
        try {
            return this.mApsService.updateApsInfo(infos);
        } catch (RemoteException ex) {
            Slog.w(TAG, "updateApsInfo,ex:" + ex);
            return false;
        }
    }

    public boolean registerCallback(String pkgName, IApsManagerServiceCallback callback) {
        if (!checkApsManagerService()) {
            return false;
        }
        try {
            Slog.w(TAG, "HwApsManagerService, registerCallback, start !");
            return this.mApsService.registerCallback(pkgName, callback);
        } catch (RemoteException ex) {
            Slog.w(TAG, "registerCallback,ex:" + ex);
            return false;
        }
    }

    public float getSeviceVersion() {
        if (!checkApsManagerService()) {
            return -1.0f;
        }
        try {
            return this.mApsService.getSeviceVersion();
        } catch (RemoteException ex) {
            Slog.w(TAG, "getSeviceVersion,ex:" + ex);
            return 0.0f;
        }
    }

    public boolean stopPackages(List<String> pkgs) {
        if (!checkApsManagerService()) {
            return false;
        }
        try {
            return this.mApsService.stopPackages(pkgs);
        } catch (RemoteException ex) {
            Slog.w(TAG, "stopPackages,ex:" + ex);
            return false;
        }
    }

    public int setDynamicResolutionRatio(String pkgName, float ratio) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (ratio < 0.25f || 1.0f < ratio) {
            Slog.e(TAG, "setDynamicResolutionRatio, invalid param ratio = " + ratio);
            return -1;
        }
        try {
            return this.mApsService.setDynamicResolutionRatio(pkgName, ratio);
        } catch (RemoteException ex) {
            Slog.w(TAG, "setDynamicResolutionRatio,ex:" + ex);
            return -1;
        }
    }

    public float getDynamicResolutionRatio(String pkgName) {
        if (!checkApsManagerService()) {
            return -1.0f;
        }
        try {
            return this.mApsService.getDynamicResolutionRatio(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getResolution,ex:" + ex);
            return -1.0f;
        }
    }

    public int setDynamicFps(String pkgName, int fps) {
        if (!checkApsManagerService()) {
            return -2;
        }
        if (fps == -1 || (fps >= 15 && 120 >= fps)) {
            try {
                return this.mApsService.setDynamicFps(pkgName, fps);
            } catch (RemoteException ex) {
                Slog.w(TAG, "setDynamicFps,ex:" + ex);
                return -1;
            }
        } else {
            Slog.e(TAG, "setDynamicFps, invalid param fps = " + fps);
            Slog.i(TAG, "APSLog, setDynamicFps: pkg:" + pkgName + ", fps:" + fps + ",retCode:-1(invalid param)");
            return -1;
        }
    }

    public int getDynamicFps(String pkgName) {
        if (!checkApsManagerService()) {
            return -1;
        }
        try {
            return this.mApsService.getDynamicFps(pkgName);
        } catch (RemoteException ex) {
            Slog.w(TAG, "getFps,ex:" + ex);
            return -1;
        }
    }
}
