package com.android.server.aps;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.aps.ApsAppInfo;
import android.aps.IApsManagerServiceCallback;
import android.common.HwFrameworkFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.gesture.GestureNavConst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class HwApsManagerServiceConfig {
    private static final int APS_MSG_FORCESTOP_APK = 310;
    private static final int APS_MSG_WRITE = 300;
    private static final String GET_FPS_SOURCE_CONFIG = "Aps Config";
    private static final String GET_FPS_SOURCE_SERVICE = "Aps Service";
    private static final String GET_FPS_SOURCE_UNKOWN = "unkown source";
    public static final int PERFORMANCE_POWER_MODE_VALUE = 3;
    private static final String TAG = "HwApsManagerConfig";
    private static final float VR_APP_RATIO = 0.625f;
    private static final String mHwApsPackagesListPath = "/data/system/hwaps-packages-compat.xml";
    private static int mLowResolutionMode = 0;
    private final HwApsManagerService mApsService;
    private int mApsSupportValue = 0;
    private final AtomicFile mFile;
    private final ApsHandler mHandler;
    private final ConcurrentHashMap<String, Boolean> mNewFbSkipSwitchMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> mNewHighpToLowpSwitchMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> mNewMipMapSwitchMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Float> mNewResolutionRatioMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> mNewShadowMapSwitchMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> mNewTextureQualityMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ApsAppInfo> mPackages = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IApsManagerServiceCallback> mPkgnameCallbackMap = new ConcurrentHashMap<>();
    private IVRSystemServiceManager mVrMananger;
    private ConcurrentHashMap<String, Integer> newFpsMap = new ConcurrentHashMap<>();

    private final class ApsHandler extends Handler {
        public ApsHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 300) {
                HwApsManagerServiceConfig.this.saveApsAppInfo();
            } else if (i == HwApsManagerServiceConfig.APS_MSG_FORCESTOP_APK) {
                HwApsManagerServiceConfig.this.stopApsCompatPackages();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x00aa A[SYNTHETIC, Splitter:B:13:0x00aa] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x00a2  */
    public HwApsManagerServiceConfig(HwApsManagerService service, Handler handler) {
        int eventType;
        mLowResolutionMode = service.getLowResolutionSwitchState();
        this.mApsSupportValue = SystemProperties.getInt("sys.aps.support", 0);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        this.mFile = new AtomicFile(new File(mHwApsPackagesListPath));
        this.mHandler = new ApsHandler(handler.getLooper());
        this.mApsService = service;
        String str = null;
        FileInputStream fis = null;
        try {
            fis = this.mFile.openRead();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            int eventType2 = parser.getEventType();
            while (true) {
                eventType = eventType2;
                int i = 2;
                if (eventType != 2 && eventType != 1) {
                    eventType2 = parser.next();
                } else if (eventType != 1) {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                        }
                    }
                    return;
                } else {
                    if ("hwaps-compat-packages".equals(parser.getName())) {
                        int eventType3 = parser.next();
                        while (true) {
                            if (eventType3 == i) {
                                String tagName = parser.getName();
                                if (parser.getDepth() == i && "pkg".equals(tagName)) {
                                    String pkg = parser.getAttributeValue(str, "name");
                                    String resolutionratio = parser.getAttributeValue(str, "resolutionratio");
                                    String framerate = parser.getAttributeValue(str, "framerate");
                                    String maxframerate = parser.getAttributeValue(str, "maxframerate");
                                    String texturepercent = parser.getAttributeValue(str, "texturepercent");
                                    String brightnesspercent = parser.getAttributeValue(str, "brightnesspercent");
                                    String switchable = parser.getAttributeValue(str, "switchable");
                                    float resolutionratioFloat = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                                    int framerateInt = 0;
                                    int maxframerateInt = 0;
                                    int texturepercentInt = 0;
                                    int brightnesspercentInt = 0;
                                    boolean switchableBoolean = true;
                                    try {
                                        resolutionratioFloat = Float.parseFloat(resolutionratio);
                                        framerateInt = Integer.parseInt(framerate);
                                        maxframerateInt = Integer.parseInt(maxframerate);
                                        texturepercentInt = Integer.parseInt(texturepercent);
                                        brightnesspercentInt = Integer.parseInt(brightnesspercent);
                                        try {
                                            switchableBoolean = Boolean.parseBoolean(switchable);
                                        } catch (NumberFormatException e2) {
                                        }
                                    } catch (NumberFormatException e3) {
                                        String str2 = switchable;
                                    }
                                    String str3 = brightnesspercent;
                                    String str4 = texturepercent;
                                    String str5 = maxframerate;
                                    String str6 = framerate;
                                    String str7 = resolutionratio;
                                    ApsAppInfo apsInfo = new ApsAppInfo(pkg, resolutionratioFloat, framerateInt, maxframerateInt, texturepercentInt, brightnesspercentInt, switchableBoolean);
                                    this.mPackages.put(pkg, apsInfo);
                                }
                                String str8 = tagName;
                            }
                            eventType3 = parser.next();
                            if (eventType3 == 1) {
                                break;
                            }
                            str = null;
                            i = 2;
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e4) {
                        }
                    }
                    return;
                }
            }
            if (eventType != 1) {
            }
        } catch (XmlPullParserException e5) {
            Slog.w(TAG, "Error reading hwaps-compat-packages", e5);
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e6) {
            if (fis != null) {
                Slog.w(TAG, "Error reading hwaps-compat-packages", e6);
            }
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e7) {
                }
            }
            throw th2;
        }
    }

    public void stopApsCompatPackages() {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null) {
            for (String pkgName : this.mPackages.keySet()) {
                Slog.i(TAG, "stopApsCompatPackages pkgName = " + pkgName);
                if (pkgName != null) {
                    ApsAppInfo info = this.mPackages.get(pkgName);
                    if (info != null && info.getSwitchable()) {
                        try {
                            am.forceStopPackage(pkgName, -1);
                        } catch (RemoteException e) {
                            Slog.e(TAG, "Failed to kill aps package of " + pkgName);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stopAllAppsInLowResolution() {
        IActivityManager am = ActivityManagerNative.getDefault();
        if (am != null) {
            for (Map.Entry<String, ApsAppInfo> entry : this.mPackages.entrySet()) {
                String pkgName = entry.getKey();
                ApsAppInfo info = entry.getValue();
                if (info.getSwitchable()) {
                    if (info.getSwitchable()) {
                        int i = mLowResolutionMode;
                        HwApsManagerService hwApsManagerService = this.mApsService;
                        if (i != 1) {
                        }
                    }
                }
                try {
                    am.forceStopPackage(pkgName, -1);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to kill aps package of " + pkgName + " when stop all low resolution apps.");
                }
            }
        }
    }

    public boolean findPackageInListLocked(String pkgName) {
        if (this.mPackages.get(pkgName) == null) {
            return false;
        }
        return true;
    }

    public boolean registerCallbackLocked(String pkgName, IApsManagerServiceCallback callback) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        if (callback != null) {
            this.mPkgnameCallbackMap.put(pkgName, callback);
            Slog.i(TAG, "registerCallbackLocked success, pkgName:" + pkgName + ", callback_count:" + this.mPkgnameCallbackMap.size());
            doCallbackAtFirstRegisterLocked(pkgName, callback);
        } else {
            Slog.i(TAG, "unregisterCallback from service, pkg:" + pkgName);
            this.mPkgnameCallbackMap.remove(pkgName);
        }
        return true;
    }

    public int notifyApsManagerServiceCallback(String pkgName, int apsCallbackCode, int data) {
        IApsManagerServiceCallback callback = this.mPkgnameCallbackMap.get(pkgName);
        if (callback == null) {
            Slog.d(TAG, "notifyApsManagerServiceCallback, pkgName:" + pkgName + " , callback is not found.");
            return -1;
        }
        try {
            Slog.d(TAG, "notifyApsManagerServiceCallback, pkgName:" + pkgName + ", apsCallbackCode:" + apsCallbackCode + ", data:" + data);
            callback.doCallback(apsCallbackCode, data);
            return 0;
        } catch (RemoteException ex) {
            this.mPkgnameCallbackMap.remove(pkgName);
            Slog.w(TAG, "notifyApsManagerServiceCallback,ex:" + ex + ", remove " + pkgName + " from mPkgnameCallbackMap.");
            return -5;
        }
    }

    public void doCallbackAtFirstRegisterLocked(String pkgName, IApsManagerServiceCallback callback) {
        try {
            Slog.i(TAG, "doCallbackAtFirstRegisterLocked, start ! pkgName:" + pkgName);
            if (this.mNewFbSkipSwitchMap.get(pkgName) != null) {
                int data = this.mNewFbSkipSwitchMap.get(pkgName).booleanValue();
                Slog.i(TAG, "doCallbackAtFirstRegisterLocked, callback:4, data: " + ((int) data) + " , from new config.");
                callback.doCallback(4, data);
            }
            if (this.mNewHighpToLowpSwitchMap.get(pkgName) != null) {
                int data2 = this.mNewHighpToLowpSwitchMap.get(pkgName).booleanValue();
                Slog.i(TAG, "doCallbackAtFirstRegisterLocked, callback:5, data: " + ((int) data2) + " , from new config.");
                callback.doCallback(5, data2);
            }
            if (this.mNewShadowMapSwitchMap.get(pkgName) != null) {
                int data3 = this.mNewShadowMapSwitchMap.get(pkgName).intValue();
                Slog.i(TAG, "doCallbackAtFirstRegisterLocked, callback:6, data: " + data3 + " , from new config.");
                callback.doCallback(6, data3);
            }
            if (this.mNewMipMapSwitchMap.get(pkgName) != null) {
                int data4 = this.mNewMipMapSwitchMap.get(pkgName).intValue();
                Slog.i(TAG, "doCallbackAtFirstRegisterLocked, callback:7, data: " + data4 + " , from new config.");
                callback.doCallback(7, data4);
            }
            if (this.mNewResolutionRatioMap.get(pkgName) != null) {
                int data5 = (int) (this.mNewResolutionRatioMap.get(pkgName).floatValue() * 100000.0f);
                Slog.i(TAG, "doCallbackAtFirstRegisterLocked, callback:1, data: " + data5);
                callback.doCallback(1, data5);
            }
            if (this.newFpsMap.get(pkgName) != null) {
                int data6 = this.newFpsMap.get(pkgName).intValue();
                Slog.i(TAG, "doCallbackAtFirstRegisterLocked, callback:0, data: " + data6 + " , from new config.");
                callback.doCallback(0, data6);
            }
        } catch (RemoteException ex) {
            Slog.w(TAG, "doCallbackAtFirstRegisterLocked, pkgName: " + pkgName + ", ex:" + ex);
        }
    }

    public int setResolutionLocked(String pkgName, float ratio, boolean switchable) {
        ApsAppInfo apsInfo = this.mPackages.get(pkgName);
        if (apsInfo == null) {
            ApsAppInfo apsAppInfo = new ApsAppInfo(pkgName, ratio, 60, 100, 100, switchable);
            apsInfo = apsAppInfo;
        } else {
            apsInfo.setResolutionRatio(ratio, switchable);
        }
        this.mPackages.put(pkgName, apsInfo);
        scheduleWrite();
        return 0;
    }

    public int setLowResolutionModeLocked(int lowResolutionMode) {
        Slog.i(TAG, "setLowResolutionModeLocked, lowReosulotionMode = " + lowResolutionMode);
        mLowResolutionMode = lowResolutionMode;
        this.mHandler.removeMessages(APS_MSG_FORCESTOP_APK);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(APS_MSG_FORCESTOP_APK));
        return 0;
    }

    public int setFpsLocked(String pkgName, int fps) {
        ApsAppInfo apsInfo = this.mPackages.get(pkgName);
        if (apsInfo == null) {
            Slog.w(TAG, "setFpsLocked can not get ApsAppInfo. We will create a new one");
            ApsAppInfo apsAppInfo = new ApsAppInfo(pkgName, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, fps, 100, 100, true);
            apsInfo = apsAppInfo;
        } else {
            apsInfo.setFps(fps);
        }
        this.mPackages.put(pkgName, apsInfo);
        scheduleWrite();
        return 0;
    }

    public int setMaxFpsLocked(String pkgName, int fps) {
        return -1;
    }

    public int setBrightnessLocked(String pkgName, int ratioPercent) {
        return -1;
    }

    public int setTextureLocked(String pkgName, int texture) {
        if (texture < 5 || texture > 100) {
            return -6;
        }
        try {
            this.mNewTextureQualityMap.put(pkgName, Integer.valueOf(texture));
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, setTextureLocked, exception:" + e);
            return -6;
        }
    }

    public int setFbSkipLocked(String pkgName, boolean onoff) {
        try {
            this.mNewFbSkipSwitchMap.put(pkgName, Boolean.valueOf(onoff));
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, setFbSkipLocked, exception:" + e);
            return -6;
        }
    }

    public int setHighpToLowpLocked(String pkgName, boolean onoff) {
        try {
            this.mNewHighpToLowpSwitchMap.put(pkgName, Boolean.valueOf(onoff));
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, setHighpToLowpLocked, exception:" + e);
            return -6;
        }
    }

    public int setShadowMapLocked(String pkgName, int status) {
        try {
            this.mNewShadowMapSwitchMap.put(pkgName, Integer.valueOf(status));
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, setShadowMapLocked, exception:" + e);
            return -6;
        }
    }

    public int setMipMapLocked(String pkgName, int status) {
        try {
            this.mNewMipMapSwitchMap.put(pkgName, Integer.valueOf(status));
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, setMipMapLocked, exception:" + e);
            return -6;
        }
    }

    public boolean getFbSkipLocked(String pkgName) {
        try {
            if (this.mNewFbSkipSwitchMap.get(pkgName) == null) {
                return false;
            }
            return this.mNewFbSkipSwitchMap.get(pkgName).booleanValue();
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, getFbSkip, exception:" + e);
            return false;
        }
    }

    public boolean getHighpToLowpLocked(String pkgName) {
        try {
            if (this.mNewHighpToLowpSwitchMap.get(pkgName) == null) {
                return false;
            }
            return this.mNewHighpToLowpSwitchMap.get(pkgName).booleanValue();
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, getHighpToLowp, exception:" + e);
            return false;
        }
    }

    public int getShadowMapLocked(String pkgName) {
        try {
            if (this.mNewShadowMapSwitchMap.get(pkgName) == null) {
                return 0;
            }
            return this.mNewShadowMapSwitchMap.get(pkgName).intValue();
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, getShadowMap, exception:" + e);
            return 0;
        }
    }

    public int getMipMapLocked(String pkgName) {
        try {
            if (this.mNewMipMapSwitchMap.get(pkgName) == null) {
                return 0;
            }
            return this.mNewMipMapSwitchMap.get(pkgName).intValue();
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, getMipMap, exception:" + e);
            return 0;
        }
    }

    public int setDynamicResolutionRatioLocked(String pkgName, float ratio) {
        if (ratio <= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || ratio > 1.0f) {
            Slog.i(TAG, "setDynamicResolutionRatioLocked, pkg:" + pkgName + ", ratio:" + ratio + ", return APS_ERRNO_RUNAS_CONFIG]");
            return -4;
        }
        if (ratio == 1.0f) {
            try {
                this.mNewResolutionRatioMap.remove(pkgName);
            } catch (Exception e) {
                Slog.e(TAG, "APS, HwApsManagerServiceConfig, setDynamicResolutionRatioLocked, exception:" + e);
                return -6;
            }
        } else {
            this.mNewResolutionRatioMap.put(pkgName, Float.valueOf(ratio));
        }
        Slog.i(TAG, "APSLog, setDynamicResolutionRatioLocked, pkg:" + pkgName + ", ratio:" + ratio + ", retCode:0");
        return 0;
    }

    public float getDynamicResolutionRatioLocked(String pkgName) {
        try {
            if (this.mNewResolutionRatioMap.get(pkgName) == null) {
                return 1.0f;
            }
            return this.mNewResolutionRatioMap.get(pkgName).floatValue();
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, getDynamicResolutionRatioLocked, exception:" + e);
            return 1.0f;
        }
    }

    public int setDynamicFpsLocked(String pkgName, int fps) {
        try {
            ApsAppInfo apsInfo = this.mPackages.get(pkgName);
            if (apsInfo != null && 60 >= apsInfo.getMaxFrameRatio() && fps > apsInfo.getFrameRatio() && !isPerformanceMode()) {
                Slog.i(TAG, "APSLog, setDynamicFpsLocked: powermode: pkg:" + pkgName + ",fps:" + fps + ",retCode:-4->APS_ERRNO_RUNAS_CONFIG, config fps:" + apsInfo.getFrameRatio());
                return -4;
            } else if (fps <= 60 || !isPerformanceMode()) {
                if (fps == -1) {
                    this.newFpsMap.remove(pkgName);
                } else {
                    this.newFpsMap.put(pkgName, Integer.valueOf(fps));
                }
                Slog.i(TAG, "APSLog, setDynamicFpsLocked: pkg:" + pkgName + ",fps:" + fps + ",retCode:0 ");
                return 0;
            } else {
                Slog.i(TAG, "APSLog, setDynamicFpsLocked: perforcemancemode: pkg:" + pkgName + ",fps:" + fps + ",retCode:-4->APS_ERRNO_RUNAS_CONFIG, config fps: 60");
                return -4;
            }
        } catch (Exception e) {
            Slog.e(TAG, "APS, HwApsManagerServiceConfig, setDynamicFPSLocked, exception:" + e);
            return -6;
        }
    }

    public int getDynamicFpsLocked(String pkgName) {
        if (pkgName == null) {
            try {
                Slog.e(TAG, "getResolutionLocked input invalid param!");
                return -1;
            } catch (Exception e) {
                Slog.e(TAG, "APS, HwApsManagerServiceConfig, getDynamicFPSLocked, exception:" + e);
                return -6;
            }
        } else {
            int retFps = -1;
            String fpsSourceForPrintLog = GET_FPS_SOURCE_UNKOWN;
            ApsAppInfo apsInfo = this.mPackages.get(pkgName);
            if (this.newFpsMap.containsKey(pkgName)) {
                retFps = this.newFpsMap.get(pkgName).intValue();
                fpsSourceForPrintLog = GET_FPS_SOURCE_SERVICE;
            } else if (apsInfo != null) {
                retFps = apsInfo.getFrameRatio();
                fpsSourceForPrintLog = GET_FPS_SOURCE_CONFIG;
            }
            Slog.i(TAG, "APSLog -> getFps:[from:" + fpsSourceForPrintLog + ",pkgName:" + pkgName + ",fps:" + retFps + "]");
            return retFps;
        }
    }

    public int setPackageApsInfoLocked(String pkgName, ApsAppInfo info) {
        if (info == null) {
            return -1;
        }
        this.mPackages.put(pkgName, new ApsAppInfo(info));
        scheduleWrite();
        return 0;
    }

    public ApsAppInfo getPackageApsInfoLocked(String pkgName) {
        if (pkgName != null) {
            return this.mPackages.get(pkgName);
        }
        Slog.e(TAG, "getPackageApsInfoLocked input invalid param!");
        return null;
    }

    public float getResolutionLocked(String pkgName) {
        if (pkgName == null) {
            Slog.e(TAG, "getResolutionLocked input invalid param!");
            return -1.0f;
        } else if ((this.mApsSupportValue & 32768) == 0) {
            Log.i(TAG, "HwApsManagerServiceConfig.getResoutionLocked, application low resolution is not supported.");
            return -1.0f;
        } else if (this.mApsService.mInCarMode) {
            return -1.0f;
        } else {
            ApsAppInfo info = this.mPackages.get(pkgName);
            if (this.mVrMananger.isVRDeviceConnected() && this.mVrMananger.isVRLowPowerApp(pkgName)) {
                Slog.w(TAG, "return VR App pkgName = " + pkgName + " Resolution = " + VR_APP_RATIO);
                return VR_APP_RATIO;
            } else if (info == null || (info.getSwitchable() && mLowResolutionMode == 0)) {
                return -1.0f;
            } else {
                int defaultWidth = SystemProperties.getInt("persist.sys.aps.defaultWidth", 0);
                int curWidth = SystemProperties.getInt("persist.sys.rog.width", 0);
                float ratio = info.getResolutionRatio();
                if (!(defaultWidth == 0 || curWidth == 0 || defaultWidth == curWidth || this.mVrMananger.isVRLowPowerApp(pkgName))) {
                    ratio = ratio >= ((float) curWidth) / ((float) defaultWidth) ? -1.0f : (((float) defaultWidth) * ratio) / ((float) curWidth);
                }
                return ratio;
            }
        }
    }

    public int getTextureLocked(String pkgName) {
        if (pkgName == null) {
            try {
                Slog.e(TAG, "getTextureLocked input invalid param!");
                return -1;
            } catch (Exception e) {
                Slog.e(TAG, "APS, HwApsManagerServiceConfig, getTextureLocked, exception:" + e);
                return -1;
            }
        } else if (this.mNewTextureQualityMap.get(pkgName) != null) {
            return this.mNewTextureQualityMap.get(pkgName).intValue();
        } else {
            ApsAppInfo apsAppInfo = this.mPackages.get(pkgName);
            if (apsAppInfo == null) {
                return -1;
            }
            return apsAppInfo.getTexturePercent();
        }
    }

    public int getFpsLocked(String pkgName) {
        if (isPerformanceMode()) {
            Slog.e(TAG, "getFpsLocked: performance mode and the limit fps is 60!");
            return -1;
        } else if (pkgName == null) {
            Slog.e(TAG, "getFpsLocked input invalid param!");
            return -1;
        } else {
            ApsAppInfo info = this.mPackages.get(pkgName);
            if (info == null) {
                return -1;
            }
            return info.getFrameRatio();
        }
    }

    public int getMaxFpsLocked(String pkgName) {
        if (pkgName == null) {
            Slog.e(TAG, "getMaxFpsLocked input invalid param!");
            return -1;
        }
        ApsAppInfo info = this.mPackages.get(pkgName);
        if (info == null) {
            return -1;
        }
        return info.getMaxFrameRatio();
    }

    public int getBrightnessLocked(String pkgName) {
        if (pkgName == null) {
            Slog.e(TAG, "getResolutionLocked input invalid param!");
            return -1;
        }
        ApsAppInfo info = this.mPackages.get(pkgName);
        if (info == null) {
            return -1;
        }
        return info.getBrightnessPercent();
    }

    public boolean deletePackageApsInfoLocked(String pkgName) {
        if (pkgName == null) {
            Slog.e(TAG, "deletePackageApsInfoLocked input invalid param!");
            return false;
        } else if (this.mPackages.get(pkgName) == null) {
            return false;
        } else {
            this.mPackages.remove(pkgName);
            scheduleWrite();
            return true;
        }
    }

    public List<ApsAppInfo> getAllPackagesApsInfoLocked() {
        List<ApsAppInfo> apsAppInfolist = new ArrayList<>();
        for (Map.Entry<String, ApsAppInfo> entry : this.mPackages.entrySet()) {
            apsAppInfolist.add(entry.getValue());
        }
        return apsAppInfolist;
    }

    public List<String> getAllApsPackagesLocked() {
        List<String> apsPackageNameList = new ArrayList<>();
        for (String pkgName : this.mPackages.keySet()) {
            apsPackageNameList.add(pkgName);
        }
        return apsPackageNameList;
    }

    public boolean updateApsInfoLocked(List<ApsAppInfo> infos) {
        for (ApsAppInfo apsInfoUpdate : infos) {
            if (apsInfoUpdate == null) {
                Slog.e(TAG, "updateApsInfoLocked error, can not find apsInfo.");
                return false;
            }
            String pkgNameUpdate = apsInfoUpdate.getBasePackageName();
            for (String pkgName : this.mPackages.keySet()) {
                if (pkgName.equals(pkgNameUpdate)) {
                    ApsAppInfo apsAppInfo = this.mPackages.get(pkgName);
                }
            }
        }
        return false;
    }

    public boolean isPerformanceMode() {
        return 3 == SystemProperties.getInt("persist.sys.smart_power", 0);
    }

    private void scheduleWrite() {
        this.mHandler.removeMessages(300);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(300), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
    public void saveApsAppInfo() {
        HashMap<String, ApsAppInfo> pkgs;
        HashMap<String, ApsAppInfo> pkgs2;
        synchronized (this.mApsService) {
            pkgs = new HashMap<>(this.mPackages);
        }
        String str = null;
        FileOutputStream fos = null;
        try {
            fos = this.mFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "hwaps-compat-packages");
            for (Map.Entry<String, ApsAppInfo> entry : pkgs.entrySet()) {
                String pkg = entry.getKey();
                ApsAppInfo apsInfo = entry.getValue();
                if (apsInfo == null) {
                    pkgs2 = pkgs;
                } else {
                    out.startTag(str, "pkg");
                    out.attribute(str, "name", pkg);
                    float rr = apsInfo.getResolutionRatio();
                    int fr = apsInfo.getFrameRatio();
                    int maxfr = apsInfo.getMaxFrameRatio();
                    int tp = apsInfo.getTexturePercent();
                    int bp = apsInfo.getBrightnessPercent();
                    boolean sa = apsInfo.getSwitchable();
                    pkgs2 = pkgs;
                    try {
                        out.attribute(null, "resolutionratio", Float.toString(rr));
                        out.attribute(null, "framerate", Integer.toString(fr));
                        out.attribute(null, "maxframerate", Integer.toString(maxfr));
                        out.attribute(null, "texturepercent", Integer.toString(tp));
                        out.attribute(null, "brightnesspercent", Integer.toString(bp));
                        out.attribute(null, "switchable", Boolean.toString(sa));
                        out.endTag(null, "pkg");
                    } catch (IOException e) {
                        e = e;
                        Slog.e(TAG, "Error writing hwaps compat packages", e);
                        if (fos == null) {
                        }
                    }
                }
                pkgs = pkgs2;
                str = null;
            }
            out.endTag(null, "hwaps-compat-packages");
            out.endDocument();
            this.mFile.finishWrite(fos);
        } catch (IOException e2) {
            e = e2;
            HashMap<String, ApsAppInfo> hashMap = pkgs;
            Slog.e(TAG, "Error writing hwaps compat packages", e);
            if (fos == null) {
                this.mFile.failWrite(fos);
            }
        }
    }
}
