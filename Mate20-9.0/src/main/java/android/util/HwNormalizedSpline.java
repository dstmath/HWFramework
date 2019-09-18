package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import com.huawei.displayengine.DisplayEngineDBManager;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import huawei.android.hwcolorpicker.HwColorPicker;
import huawei.android.utils.HwEyeProtectionSpline;
import huawei.android.utils.HwEyeProtectionSplineImpl;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwNormalizedSpline extends Spline {
    private static final float BRIGHTNESS_WITHDELTA_MAX = 230.0f;
    private static boolean DEBUG = false;
    private static final float DEFAULT_MIN_DELTA = 1.0f;
    private static final float DEFAULT_RATIO = 1.0f;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final String TAG = "HwNormalizedSpline";
    private static final String TOUCH_OEM_INFO_PATH = "/sys/touchscreen/touch_oem_info";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    private static final Object mLock = new Object();
    private static final float maxBrightness = 255.0f;
    private static final float minBrightness = 4.0f;
    private float mAmLux;
    private float mAmLuxOffset;
    private float mAmLuxOffsetSaved;
    private float mAmLuxSaved;
    private boolean mBrightenOffsetEffectMinLuxEnable;
    private float mBrightenOffsetLuxTh1;
    private float mBrightenOffsetLuxTh2;
    private float mBrightenOffsetLuxTh3;
    private float mBrightenOffsetNoValidBrightenLuxTh1;
    private float mBrightenOffsetNoValidBrightenLuxTh2;
    private float mBrightenOffsetNoValidBrightenLuxTh3;
    private float mBrightenOffsetNoValidBrightenLuxTh4;
    private float mBrightenOffsetNoValidDarkenLuxTh1;
    private float mBrightenOffsetNoValidDarkenLuxTh2;
    private float mBrightenOffsetNoValidDarkenLuxTh3;
    private float mBrightenOffsetNoValidDarkenLuxTh4;
    private float mBrightenOffsetNoValidSavedLuxTh1;
    private float mBrightenOffsetNoValidSavedLuxTh2;
    private boolean mBrightnessCalibrationEnabled;
    List<Point> mBrightnessCurveDefault;
    List<Point> mBrightnessCurveDefaultTmp;
    List<Point> mBrightnessCurveHigh;
    List<Point> mBrightnessCurveHighTmp;
    List<Point> mBrightnessCurveLow;
    List<Point> mBrightnessCurveLowTmp;
    List<Point> mBrightnessCurveMiddle;
    List<Point> mBrightnessCurveMiddleTmp;
    private float mBrightnessForLog;
    private boolean mBrightnessOffsetLuxModeEnable;
    private boolean mBrightnessOffsetTmpValidEnable;
    private float mCalibrationRatio;
    private boolean mCalibrtionModeBeforeEnable;
    private int mCalibrtionTest;
    List<Point> mCameraBrighnessLinePointsList;
    private boolean mCameraModeEnable;
    private ContentResolver mContentResolver;
    private boolean mCoverModeNoOffsetEnable;
    private int mCurrentCurveLevel;
    private int mCurrentUserId;
    private int mCurveLevel;
    private boolean mDarkAdaptEnable;
    private boolean mDarkAdaptLineLocked;
    private DarkAdaptState mDarkAdaptState;
    private DarkAdaptState mDarkAdaptStateDetected;
    private int mDarkAdaptedBrightness0LuxLevel;
    private List<Point> mDarkAdaptedBrightnessPointsList;
    private int mDarkAdaptingBrightness0LuxLevel;
    private List<Point> mDarkAdaptingBrightnessPointsList;
    private float mDarkenOffsetLuxTh1;
    private float mDarkenOffsetLuxTh2;
    private float mDarkenOffsetLuxTh3;
    private float mDarkenOffsetNoValidBrightenLuxTh1;
    private float mDarkenOffsetNoValidBrightenLuxTh2;
    private float mDarkenOffsetNoValidBrightenLuxTh3;
    private float mDarkenOffsetNoValidBrightenLuxTh4;
    List<Point> mDayBrighnessLinePointsList;
    private boolean mDayModeAlgoEnable;
    private boolean mDayModeEnable;
    private float mDayModeMinimumBrightness;
    private int mDayModeModifyMinBrightness;
    private int mDayModeModifyNumPoint;
    List<Point> mDefaultBrighnessLinePointsList;
    List<Point> mDefaultBrighnessLinePointsListCaliBefore;
    private float mDefaultBrightness;
    private float mDefaultBrightnessFromLux;
    private float mDelta;
    private float mDeltaNew;
    private float mDeltaSaved;
    private float mDeltaTmp;
    private final int mDeviceActualBrightnessLevel;
    private int mDeviceActualBrightnessNit;
    private int mDeviceStandardBrightnessNit;
    private boolean mErrorCorrectionOffsetNeedClear;
    private HwEyeProtectionSpline mEyeProtectionSpline;
    private boolean mEyeProtectionSplineEnable;
    private boolean mGameModeBrightnessEnable;
    private float mGameModeBrightnessFloor;
    private boolean mGameModeBrightnessLimitationEnable;
    List<Point> mGameModeBrightnessLinePointsList;
    private boolean mGameModeEnable;
    private float mGameModeOffsetLux;
    private float mGameModePosBrightness;
    private float mGameModeStartLuxDefaultBrightness;
    private boolean mIsReboot;
    private volatile boolean mIsReset;
    private boolean mIsUserChange;
    private boolean mIsUserChangeSaved;
    private float mLastBrightnessEndOrig;
    private float mLastBrightnessEndOrigIn;
    private float mLastGameModeBrightness;
    private float mLastLuxDefaultBrightness;
    private float mLastLuxDefaultBrightnessSaved;
    private float[] mLuxPonits;
    private DisplayEngineManager mManager;
    private int mManualBrightnessMaxLimit;
    private boolean mManualMode;
    private boolean mNewCurveEnable;
    private boolean mNewCurveEnableTmp;
    private float mOffsetBrightenAlphaLeft;
    private float mOffsetBrightenAlphaRight;
    private float mOffsetBrightenRatioLeft;
    private float mOffsetBrightness_last;
    private float mOffsetBrightness_lastSaved;
    private float mOffsetDarkenAlphaLeft;
    private float mOminLevel;
    List<Point> mOminLevelBrighnessLinePointsList;
    private int mOminLevelCount;
    private boolean mOminLevelCountEnable;
    List<Point> mOminLevelCountLevelPointsList;
    private int mOminLevelCountResetLongSetTime;
    private int mOminLevelCountResetLongSetTimeSaved;
    private int mOminLevelCountResetLongTimeTh;
    private int mOminLevelCountSaved;
    private long mOminLevelCountSetTime;
    private int mOminLevelCountValidLuxTh;
    private int mOminLevelCountValidTimeTh;
    private boolean mOminLevelDayModeEnable;
    private boolean mOminLevelModeEnable;
    private boolean mOminLevelOffsetCountEnable;
    private int mOminLevelValidCount;
    private boolean mPersonalizedBrightnessCurveLoadEnable;
    private boolean mPersonalizedBrightnessEnable;
    private float mPosBrightness;
    private float mPosBrightnessSaved;
    private boolean mPowerOnEanble;
    private float mPowerSavingAmluxThreshold;
    private boolean mPowerSavingBrighnessLineEnable;
    List<Point> mPowerSavingBrighnessLinePointsList;
    private boolean mPowerSavingModeEnable;
    List<Point> mReadingBrighnessLinePointsList;
    private boolean mReadingModeEnable;
    private boolean mRebootNewCurveEnable;
    private int mSceneLevel;
    private float mStartLuxDefaultBrightness;
    private float mStartLuxDefaultBrightnessSaved;
    private boolean mUsePowerSavingModeCurveEnable;
    private float mVehicleModeBrighntess;
    private boolean mVehicleModeBrightnessEnable;
    private boolean mVehicleModeClearOffsetEnable;
    private boolean mVehicleModeEnable;
    private float mVehicleModeLuxThreshold;
    public boolean mVehicleModeQuitForPowerOnEnable;

    public enum BrightnessModeState {
        CameraMode,
        ReadingMode,
        GameMode,
        NewCurveMode,
        PowerSavingMode,
        EyeProtectionMode,
        CalibrtionMode,
        DarkAdaptMode,
        OminLevelMode,
        DayMode,
        DefaultMode
    }

    public enum DarkAdaptState {
        UNADAPTED,
        ADAPTING,
        ADAPTED
    }

    private static class Point {
        float x;
        float y;

        public Point() {
        }

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        DEBUG = z;
    }

    private HwNormalizedSpline(Context context, int deviceActualBrightnessLevel, int deviceActualBrightnessNit, int deviceStandardBrightnessNit) {
        this.mDelta = 0.0f;
        this.mDeltaNew = 0.0f;
        this.mOffsetBrightenRatioLeft = 1.0f;
        this.mOffsetBrightenAlphaLeft = 1.0f;
        this.mOffsetBrightenAlphaRight = 1.0f;
        this.mOffsetDarkenAlphaLeft = 1.0f;
        this.mPosBrightness = minBrightness;
        this.mIsReboot = false;
        this.mIsUserChange = false;
        this.mOffsetBrightness_last = minBrightness;
        this.mLastLuxDefaultBrightness = minBrightness;
        this.mStartLuxDefaultBrightness = minBrightness;
        this.mCurrentUserId = 0;
        this.mAmLux = -1.0f;
        this.mCoverModeNoOffsetEnable = false;
        this.mReadingModeEnable = false;
        this.mCameraModeEnable = false;
        this.mManualMode = false;
        this.mManualBrightnessMaxLimit = 255;
        this.mDayModeEnable = false;
        this.mDayModeAlgoEnable = false;
        this.mDayModeModifyNumPoint = 3;
        this.mDayModeModifyMinBrightness = 6;
        this.mPowerSavingBrighnessLineEnable = false;
        this.mPowerSavingModeEnable = false;
        this.mPowerSavingAmluxThreshold = 25.0f;
        this.mAmLuxOffset = -1.0f;
        this.mAmLuxOffsetSaved = -1.0f;
        this.mCalibrationRatio = 1.0f;
        this.mOminLevelModeEnable = false;
        this.mOminLevelOffsetCountEnable = false;
        this.mOminLevelCountEnable = false;
        this.mOminLevelDayModeEnable = false;
        this.mOminLevelCount = 0;
        this.mOminLevelCountSaved = 0;
        this.mOminLevel = 6.0f;
        this.mOminLevelCountValidLuxTh = 5;
        this.mOminLevelCountValidTimeTh = 60;
        this.mOminLevelCountSetTime = -1;
        this.mOminLevelCountResetLongTimeTh = 20160;
        this.mOminLevelCountResetLongSetTime = -1;
        this.mOminLevelValidCount = 0;
        this.mEyeProtectionSplineEnable = true;
        this.mOminLevelBrighnessLinePointsList = null;
        this.mOminLevelCountLevelPointsList = null;
        this.mIsReset = false;
        this.mDayModeMinimumBrightness = minBrightness;
        this.mDefaultBrighnessLinePointsList = null;
        this.mDefaultBrighnessLinePointsListCaliBefore = null;
        this.mEyeProtectionSpline = null;
        this.mCameraBrighnessLinePointsList = null;
        this.mReadingBrighnessLinePointsList = null;
        this.mDayBrighnessLinePointsList = null;
        this.mPowerSavingBrighnessLinePointsList = null;
        this.mBrightnessCurveDefault = new ArrayList();
        this.mBrightnessCurveLow = new ArrayList();
        this.mBrightnessCurveMiddle = new ArrayList();
        this.mBrightnessCurveHigh = new ArrayList();
        this.mBrightnessCurveDefaultTmp = new ArrayList();
        this.mBrightnessCurveLowTmp = new ArrayList();
        this.mBrightnessCurveMiddleTmp = new ArrayList();
        this.mBrightnessCurveHighTmp = new ArrayList();
        this.mRebootNewCurveEnable = true;
        this.mNewCurveEnable = false;
        this.mNewCurveEnableTmp = false;
        this.mCurveLevel = -1;
        this.mPowerOnEanble = false;
        this.mPersonalizedBrightnessEnable = false;
        this.mPersonalizedBrightnessCurveLoadEnable = true;
        this.mLuxPonits = new float[]{0.0f, 2.0f, 5.0f, 10.0f, 15.0f, 20.0f, 30.0f, 50.0f, 70.0f, 100.0f, 150.0f, 200.0f, 250.0f, 300.0f, 350.0f, 400.0f, 500.0f, 600.0f, 700.0f, 800.0f, 900.0f, 1000.0f, 1200.0f, 1400.0f, 1800.0f, 2400.0f, 3000.0f, 4000.0f, 5000.0f, 6000.0f, 8000.0f, 10000.0f, 20000.0f, 30000.0f, 40000.0f};
        this.mVehicleModeBrightnessEnable = false;
        this.mVehicleModeClearOffsetEnable = false;
        this.mVehicleModeEnable = false;
        this.mVehicleModeBrighntess = minBrightness;
        this.mVehicleModeLuxThreshold = 0.0f;
        this.mSceneLevel = -1;
        this.mGameModeEnable = false;
        this.mGameModeBrightnessEnable = false;
        this.mGameModeBrightnessLimitationEnable = false;
        this.mGameModeBrightnessFloor = minBrightness;
        this.mLastGameModeBrightness = -1.0f;
        this.mDeltaTmp = 0.0f;
        this.mGameModeOffsetLux = -1.0f;
        this.mGameModeBrightnessLinePointsList = new ArrayList();
        this.mGameModeStartLuxDefaultBrightness = -1.0f;
        this.mGameModePosBrightness = 0.0f;
        this.mDarkAdaptState = DarkAdaptState.UNADAPTED;
        this.mDarkAdaptStateDetected = DarkAdaptState.UNADAPTED;
        this.mBrightnessOffsetLuxModeEnable = false;
        this.mBrightenOffsetLuxTh1 = 0.0f;
        this.mBrightenOffsetLuxTh2 = 0.0f;
        this.mBrightenOffsetLuxTh3 = 0.0f;
        this.mBrightenOffsetNoValidDarkenLuxTh1 = 0.0f;
        this.mBrightenOffsetNoValidDarkenLuxTh2 = 0.0f;
        this.mBrightenOffsetNoValidDarkenLuxTh3 = 0.0f;
        this.mBrightenOffsetNoValidDarkenLuxTh4 = 0.0f;
        this.mBrightenOffsetNoValidBrightenLuxTh1 = 0.0f;
        this.mBrightenOffsetNoValidBrightenLuxTh2 = 0.0f;
        this.mBrightenOffsetNoValidBrightenLuxTh3 = 0.0f;
        this.mBrightenOffsetNoValidBrightenLuxTh4 = 0.0f;
        this.mDarkenOffsetLuxTh1 = 0.0f;
        this.mDarkenOffsetLuxTh2 = 0.0f;
        this.mDarkenOffsetLuxTh3 = 0.0f;
        this.mDarkenOffsetNoValidBrightenLuxTh1 = 0.0f;
        this.mDarkenOffsetNoValidBrightenLuxTh2 = 0.0f;
        this.mDarkenOffsetNoValidBrightenLuxTh3 = 0.0f;
        this.mDarkenOffsetNoValidBrightenLuxTh4 = 0.0f;
        this.mBrightenOffsetEffectMinLuxEnable = false;
        this.mBrightnessOffsetTmpValidEnable = false;
        this.mBrightenOffsetNoValidSavedLuxTh1 = 0.0f;
        this.mBrightenOffsetNoValidSavedLuxTh2 = 0.0f;
        this.mErrorCorrectionOffsetNeedClear = false;
        this.mLastBrightnessEndOrigIn = -1.0f;
        this.mLastBrightnessEndOrig = -1.0f;
        this.mUsePowerSavingModeCurveEnable = false;
        this.mVehicleModeQuitForPowerOnEnable = false;
        this.mBrightnessForLog = -1.0f;
        this.mCurrentCurveLevel = -1;
        this.mManager = new DisplayEngineManager();
        this.mIsReboot = true;
        this.mContentResolver = context.getContentResolver();
        this.mDeviceActualBrightnessLevel = deviceActualBrightnessLevel;
        this.mDeviceActualBrightnessNit = deviceActualBrightnessNit;
        this.mDeviceStandardBrightnessNit = deviceStandardBrightnessNit;
        loadCameraDefaultBrightnessLine();
        loadReadingDefaultBrightnessLine();
        loadPowerSavingDefaultBrightnessLine();
        loadOminLevelCountLevelPointsList();
        loadGameModeDefaultBrightnessLine();
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (IOException e) {
            Slog.e(TAG, "IOException : loadDefaultConfig");
            loadDefaultConfig();
        }
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mEyeProtectionSpline = new HwEyeProtectionSplineImpl(context);
        }
        if (Settings.System.getIntForUser(this.mContentResolver, "screen_brightness_mode", 0, this.mCurrentUserId) == 0) {
            float mPosBrightnessSaved2 = Settings.System.getFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId) * maxBrightness;
            if (Math.abs(mPosBrightnessSaved2) > 1.0E-7f) {
                Settings.System.putFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId);
                Slog.i(TAG, "clear autobrightness offset,orig mPosBrightnessSaved=" + mPosBrightnessSaved2);
            }
        }
        updateLinePointsListForCalibration();
        loadOffsetParas();
        if (this.mOminLevelModeEnable) {
            getOminLevelBrighnessLinePoints();
        }
    }

    private File getFactoryXmlFile() {
        String xmlPath = String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, "factory", XML_EXT});
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile != null) {
            return xmlFile;
        }
        Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
        return null;
    }

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        String panelName = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            return panelName;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private String getVersionFromTouchOemInfo() {
        String touch_oem_info;
        int productMonth;
        int productDay;
        String version;
        String version2 = null;
        try {
            File file = new File(String.format("%s", new Object[]{TOUCH_OEM_INFO_PATH}));
            if (file.exists()) {
                Slog.i(TAG, "touch_oem_info=" + touch_oem_info);
                String[] versionInfo = touch_oem_info.split(",");
                if (versionInfo.length > 15) {
                    try {
                        int productYear = Integer.parseInt(versionInfo[12]);
                        int productMonth2 = Integer.parseInt(versionInfo[13]);
                        int productDay2 = Integer.parseInt(versionInfo[14]);
                        Slog.i(TAG, "lcdversionInfo orig productYear=" + productYear + ",productMonth=" + productMonth2 + ",productDay=" + productDay2);
                        if (productYear < 48 || productYear > 57) {
                            Slog.i(TAG, "lcdversionInfo not valid productYear=" + productYear);
                            return null;
                        }
                        int productYear2 = productYear - 48;
                        if (productMonth2 >= 48 && productMonth2 <= 57) {
                            productMonth = productMonth2 - 48;
                        } else if (productMonth2 < 65 || productMonth2 > 67) {
                            Slog.i(TAG, "lcdversionInfo not valid productMonth=" + productMonth2);
                            return null;
                        } else {
                            productMonth = (productMonth2 - 65) + 10;
                        }
                        if (productDay2 >= 48 && productDay2 <= 57) {
                            productDay = productDay2 - 48;
                        } else if (productDay2 < 65 || productDay2 > 88) {
                            Slog.i(TAG, "lcdversionInfo not valid productDay=" + productDay2);
                            return null;
                        } else {
                            productDay = (productDay2 - 65) + 10;
                        }
                        if (productYear2 > 8) {
                            version = "vn2";
                        } else if (productYear2 == 8 && productMonth > 1) {
                            version = "vn2";
                        } else if (productYear2 == 8 && productMonth == 1 && productDay >= 22) {
                            version = "vn2";
                        } else {
                            Slog.i(TAG, "lcdversionInfo not valid version;productYear=" + productYear2 + ",productMonth=" + productMonth + ",productDay=" + productDay);
                            return null;
                        }
                        version2 = version;
                        Slog.i(TAG, "lcdversionInfo real vn2,productYear=" + productYear2 + ",productMonth=" + productMonth + ",productDay=" + productDay);
                    } catch (NumberFormatException e) {
                        Slog.i(TAG, "lcdversionInfo versionfile num is not valid,no need version");
                        return null;
                    }
                } else {
                    Slog.i(TAG, "lcdversionInfo versionfile info length is not valid,no need version");
                }
            } else {
                Slog.i(TAG, "lcdversionInfo versionfile is not exists, no need version,filePath=/sys/touchscreen/touch_oem_info");
            }
        } catch (IOException e2) {
            Slog.w(TAG, "Error reading touch_oem_info", e2);
        }
        return version2;
    }

    private String getVersionFromLCD() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        String panelVersion = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[32];
        try {
            int ret = mService.getEffect(14, 3, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                String lcdVersion = new String(name, "UTF-8").trim();
                int index = lcdVersion.indexOf("VER:");
                Slog.i(TAG, "getVersionFromLCD() index=" + index + ",lcdVersion=" + lcdVersion);
                if (index != -1) {
                    panelVersion = lcdVersion.substring("VER:".length() + index);
                }
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            Slog.i(TAG, "getVersionFromLCD() panelVersion=" + panelVersion);
            return panelVersion;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private File getNormalXmlFile() {
        String lcdname = getLcdPanelName();
        String lcdversion = getVersionFromTouchOemInfo();
        String lcdversionNew = getVersionFromLCD();
        String screenColor = SystemProperties.get("ro.config.devicecolor");
        Slog.i(TAG, "screenColor=" + screenColor);
        ArrayList<String> xmlPathList = new ArrayList<>();
        int i = 0;
        if (lcdversion != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversion, screenColor, XML_EXT}));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversion, XML_EXT}));
        }
        if (lcdversionNew != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversionNew, screenColor, XML_EXT}));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversionNew, XML_EXT}));
        }
        xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, screenColor, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, screenColor, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s", new Object[]{HW_LABC_CONFIG_FILE}));
        File xmlFile = null;
        int listsize = xmlPathList.size();
        while (true) {
            int i2 = i;
            if (i2 < listsize) {
                xmlFile = HwCfgFilePolicy.getCfgFile(xmlPathList.get(i2), 2);
                if (xmlFile != null) {
                    return xmlFile;
                }
                i = i2 + 1;
            } else {
                Slog.e(TAG, "get failed!");
                return xmlFile;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d3, code lost:
        if (r3 == null) goto L_0x00d6;
     */
    private boolean getConfig() throws IOException {
        File xmlFile;
        String currentMode = SystemProperties.get("ro.runmode");
        Slog.i(TAG, "currentMode=" + currentMode);
        if (currentMode == null) {
            xmlFile = getNormalXmlFile();
            if (xmlFile == null) {
                return false;
            }
        } else if (currentMode.equals("factory")) {
            xmlFile = getFactoryXmlFile();
            if (xmlFile == null) {
                return false;
            }
        } else if (currentMode.equals("normal")) {
            xmlFile = getNormalXmlFile();
            if (xmlFile == null) {
                return false;
            }
        } else {
            xmlFile = getNormalXmlFile();
            if (xmlFile == null) {
                return false;
            }
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlFile);
            if (getConfigFromXML(inputStream)) {
                fillDarkAdaptPointsList();
                if (true == checkConfigLoadedFromXML()) {
                    if (DEBUG) {
                        printConfigFromXML();
                    }
                    initLinePointsList();
                    if (DEBUG) {
                        Slog.i(TAG, "mBrightnessCalibrationEnabled=" + this.mBrightnessCalibrationEnabled + ",mDeviceActualBrightnessNit=" + this.mDeviceActualBrightnessNit + ",mDeviceStandardBrightnessNit=" + this.mDeviceStandardBrightnessNit);
                    }
                    if (this.mBrightnessCalibrationEnabled) {
                        brightnessCalibration(this.mDefaultBrighnessLinePointsList, this.mDeviceActualBrightnessNit, this.mDeviceStandardBrightnessNit);
                    }
                }
                inputStream.close();
                getDayBrightnessLinePoints();
                updateNewBrightnessCurve();
                inputStream.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "getConfig : FileNotFoundException");
        } catch (IOException e2) {
            Slog.e(TAG, "getConfig : IOException");
            if (inputStream != null) {
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                inputStream.close();
            }
            throw th;
        }
        inputStream.close();
        return false;
    }

    private boolean checkConfigLoadedFromXML() {
        if (this.mDefaultBrightness <= 0.0f) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false for mDefaultBrightness <= 0, LoadDefaultConfig!");
            return false;
        } else if (!checkPointsListIsOK(this.mDefaultBrighnessLinePointsList)) {
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mDefaultBrighnessLinePointsList is wrong, LoadDefaultConfig!");
            return false;
        } else if (this.mOffsetBrightenRatioLeft <= 0.0f || this.mOffsetBrightenAlphaLeft < 0.0f || ((double) this.mOffsetBrightenAlphaLeft) > 1.0d) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
            return false;
        } else if (this.mOffsetBrightenAlphaRight < 0.0f || ((double) this.mOffsetBrightenAlphaRight) > 1.0d) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
            return false;
        } else if (this.mOffsetDarkenAlphaLeft < 0.0f || ((double) this.mOffsetDarkenAlphaLeft) > 1.0d) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOffsetDarkenAlphaLeft=" + this.mOffsetDarkenAlphaLeft);
            return false;
        } else {
            if (this.mOminLevelModeEnable) {
                if (this.mOminLevelCountValidLuxTh < 0) {
                    loadDefaultConfig();
                    Slog.e(TAG, "LoadXML false, mOminLevelCountValidLuxTh=" + this.mOminLevelCountValidLuxTh);
                    return false;
                } else if (this.mOminLevelCountValidTimeTh < 0) {
                    loadDefaultConfig();
                    Slog.e(TAG, "LoadXML false, mOminLevelCountValidTimeTh=" + this.mOminLevelCountValidTimeTh);
                    return false;
                } else if (!checkPointsListIsOK(this.mOminLevelCountLevelPointsList)) {
                    loadDefaultConfig();
                    Slog.e(TAG, "checkPointsList mOminLevelPointsList is wrong, LoadDefaultConfig!");
                    return false;
                }
            }
            if (this.mDarkAdaptingBrightnessPointsList != null && !checkPointsListIsOK(this.mDarkAdaptingBrightnessPointsList)) {
                loadDefaultConfig();
                Slog.e(TAG, "checkPointsList mDarkAdaptingBrightnessPointsList is wrong, LoadDefaultConfig!");
                return false;
            } else if (this.mDarkAdaptedBrightnessPointsList != null && !checkPointsListIsOK(this.mDarkAdaptedBrightnessPointsList)) {
                loadDefaultConfig();
                Slog.e(TAG, "checkPointsList mDarkAdaptedBrightnessPointsList is wrong, LoadDefaultConfig!");
                return false;
            } else if (this.mVehicleModeBrighntess < 0.0f || this.mVehicleModeBrighntess > maxBrightness || this.mVehicleModeLuxThreshold < 0.0f) {
                loadDefaultConfig();
                Slog.e(TAG, "VehicleBrightMode LoadDefaultConfig!,mVehicleModeBrighntess=" + this.mVehicleModeBrighntess + ",mVehicleModeLuxThreshold=" + this.mVehicleModeLuxThreshold);
                return false;
            } else if (this.mDayModeMinimumBrightness > maxBrightness) {
                loadDefaultConfig();
                Slog.e(TAG, "DayModeMinimumBrightness LoadDefaultConfig!,mDayModeMinimumBrightness=" + this.mDayModeMinimumBrightness);
                return false;
            } else {
                if (DEBUG) {
                    Slog.i(TAG, "checkConfigLoadedFromXML success!");
                }
                return true;
            }
        }
    }

    private void initLinePointsList() {
        int listSize = this.mDefaultBrighnessLinePointsList.size();
        for (int i = 0; i < listSize; i++) {
            Point tempPoint = new Point();
            tempPoint.x = this.mDefaultBrighnessLinePointsList.get(i).x;
            tempPoint.y = this.mDefaultBrighnessLinePointsList.get(i).y;
            if (this.mDefaultBrighnessLinePointsListCaliBefore == null) {
                this.mDefaultBrighnessLinePointsListCaliBefore = new ArrayList();
            }
            this.mDefaultBrighnessLinePointsListCaliBefore.add(tempPoint);
        }
        Settings.System.putIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        if (DEBUG) {
            Slog.i(TAG, "init list_DefaultBrighnessLinePointsBeforeCali");
        }
    }

    private void brightnessCalibration(List<Point> LinePointsList, int actulBrightnessNit, int standardBrightnessNit) {
        List<Point> mLinePointsList = LinePointsList;
        int mActulBrightnessNit = actulBrightnessNit;
        int mStandardBrightnessNit = standardBrightnessNit;
        if (mActulBrightnessNit < 400 || mActulBrightnessNit > 1000 || mStandardBrightnessNit > 1000 || mStandardBrightnessNit <= 0) {
            this.mCalibrationRatio = 1.0f;
            Slog.e(TAG, "error input brightnessNit:mStandardBrightnessNit=" + mStandardBrightnessNit + ",mActulBrightnessNit=" + mActulBrightnessNit);
        } else {
            this.mCalibrationRatio = ((float) mStandardBrightnessNit) / ((float) mActulBrightnessNit);
            if (DEBUG) {
                Slog.i(TAG, "mCalibrationRatio=" + this.mCalibrationRatio + ",mStandardBrightnessNit=" + mStandardBrightnessNit + ",mActulBrightnessNit=" + mActulBrightnessNit);
            }
        }
        int listSize = mLinePointsList.size();
        for (int i = 1; i < listSize; i++) {
            Point pointTemp = mLinePointsList.get(i);
            if (pointTemp.y > minBrightness && pointTemp.y < maxBrightness) {
                pointTemp.y *= this.mCalibrationRatio;
                if (pointTemp.y <= minBrightness) {
                    pointTemp.y = minBrightness;
                }
                if (pointTemp.y >= maxBrightness) {
                    pointTemp.y = maxBrightness;
                }
            }
        }
        for (Point temp : mLinePointsList) {
            if (DEBUG) {
                Slog.i(TAG, "LoadXMLConfig_NewCalibrationBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
            }
        }
    }

    private void updateLinePointsListForCalibration() {
        if (this.mBrightnessCalibrationEnabled && Math.abs(this.mCalibrationRatio - 1.0f) > 1.0E-7f) {
            if (this.mPowerSavingBrighnessLineEnable && this.mPowerSavingBrighnessLinePointsList != null) {
                updateNewLinePointsListForCalibration(this.mPowerSavingBrighnessLinePointsList);
                Slog.i(TAG, "update PowerSavingBrighnessLinePointsList for calibration");
                if (DEBUG && this.mPowerSavingBrighnessLinePointsList != null) {
                    for (Point temp : this.mPowerSavingBrighnessLinePointsList) {
                        Slog.d(TAG, "LoadXMLConfig_NewCalibrationPowerSavingPointsList x = " + temp.x + ", y = " + temp.y);
                    }
                }
            }
            if (this.mCameraBrighnessLinePointsList != null) {
                updateNewLinePointsListForCalibration(this.mCameraBrighnessLinePointsList);
                if (DEBUG) {
                    Slog.i(TAG, "update mCameraBrighnessLinePointsList for calibration");
                }
            }
            if (this.mGameModeBrightnessLinePointsList != null) {
                updateNewLinePointsListForCalibration(this.mGameModeBrightnessLinePointsList);
                if (DEBUG) {
                    Slog.i(TAG, "update mGameModeBrightnessLinePointsList for calibration");
                }
                if (DEBUG && this.mGameModeBrightnessLinePointsList != null) {
                    for (Point temp2 : this.mGameModeBrightnessLinePointsList) {
                        Slog.d(TAG, "LoadXMLConfig_GameModeBrightnessLinePointsList x = " + temp2.x + ", y = " + temp2.y);
                    }
                }
            }
        }
    }

    private void updateNewLinePointsListForCalibration(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        int listSize = mLinePointsList.size();
        for (int i = 1; i < listSize; i++) {
            Point pointTemp = mLinePointsList.get(i);
            if (pointTemp.y > minBrightness && pointTemp.y < maxBrightness) {
                pointTemp.y *= this.mCalibrationRatio;
                if (pointTemp.y <= minBrightness) {
                    pointTemp.y = minBrightness;
                }
                if (pointTemp.y >= maxBrightness) {
                    pointTemp.y = maxBrightness;
                }
            }
        }
    }

    private boolean checkPointsListIsOK(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        if (mLinePointsList == null) {
            Slog.e(TAG, "LoadXML false for mLinePointsList == null");
            return false;
        } else if (mLinePointsList.size() <= 2 || mLinePointsList.size() >= 100) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return false;
        } else {
            Point lastPoint = null;
            for (Point tmpPoint : mLinePointsList) {
                if (lastPoint == null) {
                    lastPoint = tmpPoint;
                } else if (lastPoint.x >= tmpPoint.x) {
                    loadDefaultConfig();
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return false;
                } else {
                    lastPoint = tmpPoint;
                }
            }
            return true;
        }
    }

    private boolean checkDayBrightness() {
        if (this.mDefaultBrighnessLinePointsList.size() < this.mDayModeModifyNumPoint) {
            Slog.e(TAG, "mDefaultBrighnessLinePointsList.size < mDayModeModifyNumPoint");
            return true;
        } else if (this.mDefaultBrighnessLinePointsList.get(this.mDayModeModifyNumPoint - 1).y >= ((float) this.mDayModeModifyMinBrightness)) {
            return false;
        } else {
            Slog.e(TAG, "temp.y < mDayModeModifyMinBrightness");
            return true;
        }
    }

    private void getDayBrightnessLinePoints() {
        float v;
        float u;
        if (this.mDefaultBrighnessLinePointsList != null) {
            if (!checkDayBrightness()) {
                Point temp = this.mDefaultBrighnessLinePointsList.get(this.mDayModeModifyNumPoint - 1);
                u = ((temp.y * 1.0f) - (((float) this.mDayModeModifyMinBrightness) * 1.0f)) / ((temp.y * 1.0f) - minBrightness);
                v = (temp.y * ((((float) this.mDayModeModifyMinBrightness) * 1.0f) - minBrightness)) / ((temp.y * 1.0f) - minBrightness);
                Slog.i(TAG, "DayMode:u=" + u + ", v=" + v);
            } else {
                u = 1.0f;
                v = 0.0f;
                Slog.e(TAG, "error DayBrightnessLinePoints input!");
            }
            if (this.mDayBrighnessLinePointsList == null) {
                this.mDayBrighnessLinePointsList = new ArrayList();
            } else {
                this.mDayBrighnessLinePointsList.clear();
            }
            int cntPoint = 0;
            for (Point temp2 : this.mDefaultBrighnessLinePointsList) {
                cntPoint++;
                if (cntPoint > this.mDayModeModifyNumPoint) {
                    this.mDayBrighnessLinePointsList.add(temp2);
                } else {
                    this.mDayBrighnessLinePointsList.add(new Point(temp2.x, (temp2.y * u) + v));
                }
            }
            for (Point temp3 : this.mDayBrighnessLinePointsList) {
                if (DEBUG) {
                    Slog.i(TAG, "DayMode:DayBrightnessLine: =" + temp3.x + ", y=" + temp3.y);
                }
            }
        }
    }

    private void getOminLevelBrighnessLinePoints() {
        if (this.mOminLevelBrighnessLinePointsList == null) {
            this.mOminLevelBrighnessLinePointsList = new ArrayList();
        } else {
            this.mOminLevelBrighnessLinePointsList.clear();
        }
        if (this.mDayBrighnessLinePointsList != null) {
            for (Point temp : this.mDayBrighnessLinePointsList) {
                this.mOminLevelBrighnessLinePointsList.add(new Point(temp.x, temp.y));
                if (DEBUG) {
                    Slog.d(TAG, "mOminLevelMode:LinePointsList: x=" + temp.x + ", y=" + temp.y);
                }
            }
            updateOminLevelBrighnessLinePoints();
            return;
        }
        Slog.w(TAG, "mOminLevelMode getLineFailed, mDayBrighnessLinePointsList==null");
    }

    public void updateOminLevelBrighnessLinePoints() {
        if (this.mOminLevelBrighnessLinePointsList == null) {
            Slog.w(TAG, "mOminLevelMode mOminLevelBrighnessLinePointsList==null,return");
            return;
        }
        int listsize = this.mOminLevelBrighnessLinePointsList.size();
        int countThMin = getOminLevelCountThMin(this.mOminLevelCountLevelPointsList);
        int countThMax = getOminLevelCountThMax(this.mOminLevelCountLevelPointsList);
        if (listsize >= 2) {
            Point temp = this.mOminLevelBrighnessLinePointsList.get(0);
            Point temp1 = this.mOminLevelBrighnessLinePointsList.get(1);
            if (this.mOminLevelCount >= countThMin) {
                temp.y = getOminLevelFromCount(this.mOminLevelCount);
                this.mOminLevelCountEnable = true;
            } else {
                temp.y = getOminLevelThMin(this.mOminLevelCountLevelPointsList);
                this.mOminLevelCountEnable = false;
            }
            if (temp.y > temp1.y) {
                temp.y = temp1.y;
                Slog.w(TAG, "mOminLevelMode updateMinLevel x(0)=" + temp.x + ",y(0)=" + temp.y + ",y(0)==y(1)");
            }
            if (DEBUG && this.mOminLevelCountEnable && this.mOminLevelCount < countThMax) {
                Slog.d(TAG, "mOminLevelMode updateMinLevel x(0)=" + temp.x + ",y(0)=" + temp.y);
            }
        } else {
            Slog.w(TAG, "mOminLevelMode mOminLevelBrighnessLinePointsList==null");
        }
    }

    private float getOminLevelFromCount(int ominLevelCount) {
        int countThMin = getOminLevelCountThMin(this.mOminLevelCountLevelPointsList);
        int countThMax = getOminLevelCountThMax(this.mOminLevelCountLevelPointsList);
        float levelMin = getOminLevelThMin(this.mOminLevelCountLevelPointsList);
        float levelMax = getOminLevelThMax(this.mOminLevelCountLevelPointsList);
        if (ominLevelCount < countThMin) {
            this.mOminLevel = levelMin;
        } else if (ominLevelCount >= countThMax) {
            this.mOminLevel = levelMax;
        } else {
            this.mOminLevel = getOminLevelFromCountInternal(this.mOminLevelCountLevelPointsList, (float) ominLevelCount);
            Slog.i(TAG, "mOminLevelMode ominLevelCount=" + ominLevelCount + ",mOminLevel=" + this.mOminLevel + ",cmin=" + countThMin + ",cmax=" + countThMax);
        }
        return this.mOminLevel;
    }

    private float getOminLevelFromCountInternal(List<Point> linePointsList, float levelCount) {
        List<Point> linePointsListIn = linePointsList;
        float brightnessLevel = minBrightness;
        if (linePointsListIn == null) {
            Slog.i(TAG, "mOminLevelMode linePointsListIn==null,return minBrightness");
            return minBrightness;
        }
        Point temp1 = null;
        for (Point temp : linePointsListIn) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (levelCount < temp.x) {
                brightnessLevel = temp1.y;
            } else {
                temp1 = temp;
            }
        }
        return brightnessLevel;
    }

    private void loadDefaultConfig() {
        this.mDefaultBrightness = 100.0f;
        this.mBrightnessCalibrationEnabled = false;
        if (this.mDefaultBrighnessLinePointsList != null) {
            this.mDefaultBrighnessLinePointsList.clear();
        } else {
            this.mDefaultBrighnessLinePointsList = new ArrayList();
        }
        this.mDefaultBrighnessLinePointsList.add(new Point(0.0f, minBrightness));
        this.mDefaultBrighnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mDefaultBrighnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mDefaultBrighnessLinePointsList.add(new Point(4000.0f, maxBrightness));
        this.mDefaultBrighnessLinePointsList.add(new Point(40000.0f, maxBrightness));
        loadCameraDefaultBrightnessLine();
        loadPowerSavingDefaultBrightnessLine();
        loadOminLevelCountLevelPointsList();
        this.mOminLevelModeEnable = false;
        this.mDarkAdaptingBrightnessPointsList = null;
        this.mDarkAdaptingBrightness0LuxLevel = 0;
        this.mDarkAdaptedBrightnessPointsList = null;
        this.mDarkAdaptedBrightness0LuxLevel = 0;
        this.mDarkAdaptEnable = false;
        this.mDayModeMinimumBrightness = minBrightness;
        this.mPersonalizedBrightnessEnable = false;
        this.mPersonalizedBrightnessCurveLoadEnable = false;
        if (DEBUG) {
            printConfigFromXML();
        }
    }

    private void loadCameraDefaultBrightnessLine() {
        if (this.mCameraBrighnessLinePointsList != null) {
            this.mCameraBrighnessLinePointsList.clear();
        } else {
            this.mCameraBrighnessLinePointsList = new ArrayList();
        }
        this.mCameraBrighnessLinePointsList.add(new Point(0.0f, minBrightness));
        this.mCameraBrighnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mCameraBrighnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mCameraBrighnessLinePointsList.add(new Point(4000.0f, maxBrightness));
        this.mCameraBrighnessLinePointsList.add(new Point(40000.0f, maxBrightness));
    }

    private void loadReadingDefaultBrightnessLine() {
        if (this.mReadingBrighnessLinePointsList != null) {
            this.mReadingBrighnessLinePointsList.clear();
        } else {
            this.mReadingBrighnessLinePointsList = new ArrayList();
        }
        this.mReadingBrighnessLinePointsList.add(new Point(0.0f, minBrightness));
        this.mReadingBrighnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mReadingBrighnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mReadingBrighnessLinePointsList.add(new Point(4000.0f, maxBrightness));
        this.mReadingBrighnessLinePointsList.add(new Point(40000.0f, maxBrightness));
    }

    private void loadGameModeDefaultBrightnessLine() {
        if (this.mGameModeBrightnessLinePointsList != null) {
            this.mGameModeBrightnessLinePointsList.clear();
        } else {
            this.mGameModeBrightnessLinePointsList = new ArrayList();
        }
        this.mGameModeBrightnessLinePointsList.add(new Point(0.0f, minBrightness));
        this.mGameModeBrightnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mGameModeBrightnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mGameModeBrightnessLinePointsList.add(new Point(4000.0f, maxBrightness));
        this.mGameModeBrightnessLinePointsList.add(new Point(40000.0f, maxBrightness));
    }

    private void loadPowerSavingDefaultBrightnessLine() {
        if (this.mPowerSavingBrighnessLinePointsList != null) {
            this.mPowerSavingBrighnessLinePointsList.clear();
        } else {
            this.mPowerSavingBrighnessLinePointsList = new ArrayList();
        }
        this.mPowerSavingBrighnessLinePointsList.add(new Point(0.0f, minBrightness));
        this.mPowerSavingBrighnessLinePointsList.add(new Point(25.0f, 46.5f));
        this.mPowerSavingBrighnessLinePointsList.add(new Point(1995.0f, 140.7f));
        this.mPowerSavingBrighnessLinePointsList.add(new Point(4000.0f, maxBrightness));
        this.mPowerSavingBrighnessLinePointsList.add(new Point(40000.0f, maxBrightness));
    }

    private void loadOminLevelCountLevelPointsList() {
        if (this.mOminLevelCountLevelPointsList != null) {
            this.mOminLevelCountLevelPointsList.clear();
        } else {
            this.mOminLevelCountLevelPointsList = new ArrayList();
        }
        this.mOminLevelCountLevelPointsList.add(new Point(5.0f, 6.0f));
        this.mOminLevelCountLevelPointsList.add(new Point(10.0f, 7.0f));
        this.mOminLevelCountLevelPointsList.add(new Point(20.0f, 8.0f));
    }

    private void printConfigFromXML() {
        Slog.i(TAG, "LoadXMLConfig_DefaultBrightness=" + this.mDefaultBrightness);
        Slog.i(TAG, "LoadXMLConfig_mBrightnessCalibrationEnabled=" + this.mBrightnessCalibrationEnabled + ",mPowerSavingBrighnessLineEnable=" + this.mPowerSavingBrighnessLineEnable + ",mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight + ",mOffsetDarkenAlphaLeft=" + this.mOffsetDarkenAlphaLeft + ",mManualMode=" + this.mManualMode + ",mManualBrightnessMaxLimit=" + this.mManualBrightnessMaxLimit + ",mPersonalizedBrightnessEnable=" + this.mPersonalizedBrightnessEnable + ",mVehicleModeEnable=" + this.mVehicleModeEnable + ",mVehicleModeBrighntess=" + this.mVehicleModeBrighntess + ",mVehicleModeLuxThreshold=" + this.mVehicleModeLuxThreshold + ",mGameModeEnable=" + this.mGameModeEnable + ",mPersonalizedBrightnessCurveLoadEnable=" + this.mPersonalizedBrightnessCurveLoadEnable);
        StringBuilder sb = new StringBuilder();
        sb.append("LoadXMLConfig_mGameModeBrightnessLimitationEnable=");
        sb.append(this.mGameModeBrightnessLimitationEnable);
        sb.append("mGameModeBrightnessFloor=");
        sb.append(this.mGameModeBrightnessFloor);
        Slog.i(TAG, sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("LoadXMLConfig_mOminLevelMode=");
        sb2.append(this.mOminLevelModeEnable);
        sb2.append(",mCountEnable=");
        sb2.append(this.mOminLevelOffsetCountEnable);
        sb2.append(",mDayEn=");
        sb2.append(this.mOminLevelDayModeEnable);
        sb2.append(",ValidLux=");
        sb2.append(this.mOminLevelCountValidLuxTh);
        sb2.append(",ValidTime=");
        sb2.append(this.mOminLevelCountValidTimeTh);
        sb2.append(",mLongTime=");
        sb2.append(this.mOminLevelCountResetLongTimeTh);
        sb2.append(",EyeEn=");
        sb2.append(this.mEyeProtectionSplineEnable);
        Slog.i(TAG, sb2.toString());
        for (Point temp : this.mDefaultBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_DefaultBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
        }
        for (Point temp2 : this.mCameraBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_CameraBrighnessLinePointsList x = " + temp2.x + ", y = " + temp2.y);
        }
        for (Point temp3 : this.mReadingBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_ReadingBrighnessLinePointsList x = " + temp3.x + ", y = " + temp3.y);
        }
        for (Point temp4 : this.mGameModeBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_mGameModeBrightnessLinePointsList x = " + temp4.x + ", y = " + temp4.y);
        }
        for (Point temp5 : this.mPowerSavingBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_mPowerSavingBrighnessLinePointsList x = " + temp5.x + ", y = " + temp5.y);
        }
        if (this.mOminLevelModeEnable && this.mOminLevelCountLevelPointsList != null) {
            for (Point temp6 : this.mOminLevelCountLevelPointsList) {
                Slog.i(TAG, "LoadXMLConfig_mOminLevelCountLevelPointsList x = " + temp6.x + ", y = " + temp6.y);
            }
        }
        if (this.mDarkAdaptEnable) {
            Slog.i(TAG, "LoadXMLConfig_DarkAdaptingBrightness0LuxLevel = " + this.mDarkAdaptingBrightness0LuxLevel + ", Adapted = " + this.mDarkAdaptedBrightness0LuxLevel);
        }
        Slog.i(TAG, "LoadXMLConfig_DayModeMinimumBrightness = " + this.mDayModeMinimumBrightness);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0062, code lost:
        r4 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:332:0x074b, code lost:
        if (r16 == false) goto L_0x0751;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:0x074d, code lost:
        r2 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:?, code lost:
        r0 = r3.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x0759, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x075a, code lost:
        r30 = r3;
        r2 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x0760, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:340:0x0761, code lost:
        r30 = r3;
        r2 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:0x0767, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:342:0x0768, code lost:
        r30 = r3;
        r2 = r20;
     */
    private boolean getConfigFromXML(InputStream inStream) {
        boolean DefaultBrightnessLoaded;
        boolean DefaultBrightnessLoaded2;
        StringBuilder sb;
        boolean DefaultBrighnessLinePointsListLoaded;
        boolean CameraBrightnessLinePointsListLoaded;
        boolean CameraBrightnessLinePointsListLoaded2;
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean DefaultBrighnessLinePointsListLoaded2 = false;
        boolean CameraBrightnessLinePointsListsLoadStarted = false;
        boolean CameraBrightnessLinePointsListLoaded3 = false;
        boolean ReadingBrightnessLinePointsListsLoadStarted = false;
        boolean GameModeBrightnessLinePointsListsLoadStarted = false;
        boolean PowerSavingBrightnessLinePointsListsLoadStarted = false;
        boolean OminLevelCountLevelLinePointsListsLoadStarted = false;
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        boolean DefaultBrightnessLoaded3 = false;
        boolean DefaultBrighnessLinePointsListsLoadStarted = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType != 1) {
                    switch (eventType) {
                        case 2:
                            int i = eventType;
                            DefaultBrighnessLinePointsListLoaded = DefaultBrighnessLinePointsListLoaded2;
                            try {
                                String name = parser.getName();
                                try {
                                    if (!name.equals(XML_NAME_NOEXT)) {
                                        CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded3;
                                        if (configGroupLoadStarted) {
                                            if (name.equals("DefaultBrightness")) {
                                                this.mDefaultBrightness = Float.parseFloat(parser.nextText());
                                                DefaultBrightnessLoaded3 = true;
                                            } else if (name.equals("BrightnessCalibrationEnabled")) {
                                                this.mBrightnessCalibrationEnabled = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("OffsetBrightenRatioLeft")) {
                                                this.mOffsetBrightenRatioLeft = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("OffsetBrightenAlphaLeft")) {
                                                this.mOffsetBrightenAlphaLeft = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("OffsetBrightenAlphaRight")) {
                                                this.mOffsetBrightenAlphaRight = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("OffsetDarkenAlphaLeft")) {
                                                this.mOffsetDarkenAlphaLeft = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("PersonalizedBrightnessCurveEnable")) {
                                                this.mPersonalizedBrightnessEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("PersonalizedBrightnessCurveLoadEnable")) {
                                                this.mPersonalizedBrightnessCurveLoadEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("DayModeMinimumBrightness")) {
                                                this.mDayModeMinimumBrightness = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("VehicleModeEnable")) {
                                                this.mVehicleModeEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("VehicleModeBrighntess")) {
                                                this.mVehicleModeBrighntess = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("VehicleModeLuxThreshold")) {
                                                this.mVehicleModeLuxThreshold = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("GameModeEnable")) {
                                                this.mGameModeEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("GameModeBrightnessLimitationEnable")) {
                                                this.mGameModeBrightnessLimitationEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("GameModeBrightnessFloor")) {
                                                this.mGameModeBrightnessFloor = Float.parseFloat(parser.nextText());
                                            } else if (name.equals("DefaultBrightnessPoints")) {
                                                DefaultBrighnessLinePointsListsLoadStarted = true;
                                            } else if (name.equals("Point") && DefaultBrighnessLinePointsListsLoadStarted) {
                                                Point currentPoint = new Point();
                                                String s = parser.nextText();
                                                currentPoint.x = Float.parseFloat(s.split(",")[0]);
                                                currentPoint.y = Float.parseFloat(s.split(",")[1]);
                                                if (this.mDefaultBrighnessLinePointsList == null) {
                                                    this.mDefaultBrighnessLinePointsList = new ArrayList();
                                                }
                                                this.mDefaultBrighnessLinePointsList.add(currentPoint);
                                            } else if (name.equals("CameraBrightnessPoints")) {
                                                CameraBrightnessLinePointsListsLoadStarted = true;
                                                if (this.mCameraBrighnessLinePointsList != null) {
                                                    this.mCameraBrighnessLinePointsList.clear();
                                                }
                                            } else if (name.equals("Point") && CameraBrightnessLinePointsListsLoadStarted) {
                                                Point currentPoint2 = new Point();
                                                String s2 = parser.nextText();
                                                currentPoint2.x = Float.parseFloat(s2.split(",")[0]);
                                                currentPoint2.y = Float.parseFloat(s2.split(",")[1]);
                                                if (this.mCameraBrighnessLinePointsList == null) {
                                                    this.mCameraBrighnessLinePointsList = new ArrayList();
                                                }
                                                this.mCameraBrighnessLinePointsList.add(currentPoint2);
                                            } else if (name.equals("ReadingBrightnessPoints")) {
                                                ReadingBrightnessLinePointsListsLoadStarted = true;
                                                if (this.mReadingBrighnessLinePointsList != null) {
                                                    this.mReadingBrighnessLinePointsList.clear();
                                                }
                                            } else if (name.equals("Point") && ReadingBrightnessLinePointsListsLoadStarted) {
                                                Point currentPoint3 = new Point();
                                                String s3 = parser.nextText();
                                                currentPoint3.x = Float.parseFloat(s3.split(",")[0]);
                                                currentPoint3.y = Float.parseFloat(s3.split(",")[1]);
                                                if (this.mReadingBrighnessLinePointsList == null) {
                                                    this.mReadingBrighnessLinePointsList = new ArrayList();
                                                }
                                                this.mReadingBrighnessLinePointsList.add(currentPoint3);
                                            } else if (name.equals("GameModeBrightnessPoints")) {
                                                GameModeBrightnessLinePointsListsLoadStarted = true;
                                                if (this.mGameModeBrightnessLinePointsList != null) {
                                                    this.mGameModeBrightnessLinePointsList.clear();
                                                }
                                            } else if (name.equals("Point") && GameModeBrightnessLinePointsListsLoadStarted) {
                                                Point currentPoint4 = new Point();
                                                String s4 = parser.nextText();
                                                currentPoint4.x = Float.parseFloat(s4.split(",")[0]);
                                                currentPoint4.y = Float.parseFloat(s4.split(",")[1]);
                                                if (this.mGameModeBrightnessLinePointsList == null) {
                                                    this.mGameModeBrightnessLinePointsList = new ArrayList();
                                                }
                                                this.mGameModeBrightnessLinePointsList.add(currentPoint4);
                                            } else if (name.equals("PowerSavingBrightnessPoints")) {
                                                PowerSavingBrightnessLinePointsListsLoadStarted = true;
                                                if (this.mPowerSavingBrighnessLinePointsList != null) {
                                                    this.mPowerSavingBrighnessLinePointsList.clear();
                                                }
                                            } else if (name.equals("Point") && PowerSavingBrightnessLinePointsListsLoadStarted) {
                                                Point currentPoint5 = new Point();
                                                String s5 = parser.nextText();
                                                currentPoint5.x = Float.parseFloat(s5.split(",")[0]);
                                                currentPoint5.y = Float.parseFloat(s5.split(",")[1]);
                                                if (this.mPowerSavingBrighnessLinePointsList == null) {
                                                    this.mPowerSavingBrighnessLinePointsList = new ArrayList();
                                                }
                                                this.mPowerSavingBrighnessLinePointsList.add(currentPoint5);
                                            } else if (name.equals("PowerSavingBrighnessLineEnable")) {
                                                this.mPowerSavingBrighnessLineEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("ManualMode")) {
                                                if (Integer.parseInt(parser.nextText()) == 1) {
                                                    this.mManualMode = true;
                                                }
                                            } else if (name.equals("ManualBrightnessMaxLimit")) {
                                                if (this.mManualMode) {
                                                    this.mManualBrightnessMaxLimit = Integer.parseInt(parser.nextText());
                                                }
                                            } else if (name.equals("DayModeAlgoEnable")) {
                                                this.mDayModeAlgoEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("DayModeModifyNumPoint")) {
                                                this.mDayModeModifyNumPoint = Integer.parseInt(parser.nextText());
                                            } else if (name.equals("DayModeModifyMinBrightness")) {
                                                this.mDayModeModifyMinBrightness = Integer.parseInt(parser.nextText());
                                            } else if (name.equals("OminLevelModeEnable")) {
                                                this.mOminLevelModeEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("OminLevelOffsetCountEnable")) {
                                                this.mOminLevelOffsetCountEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("OminLevelDayModeEnable")) {
                                                this.mOminLevelDayModeEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("OminLevelCountValidLuxTh")) {
                                                this.mOminLevelCountValidLuxTh = Integer.parseInt(parser.nextText());
                                            } else if (name.equals("OminLevelCountValidTimeTh")) {
                                                this.mOminLevelCountValidTimeTh = Integer.parseInt(parser.nextText());
                                            } else if (name.equals("OminLevelCountResetLongTimeTh")) {
                                                this.mOminLevelCountResetLongTimeTh = Integer.parseInt(parser.nextText());
                                            } else if (name.equals("EyeProtectionSplineEnable")) {
                                                this.mEyeProtectionSplineEnable = Boolean.parseBoolean(parser.nextText());
                                            } else if (name.equals("OminLevelCountLevelLinePoints")) {
                                                OminLevelCountLevelLinePointsListsLoadStarted = true;
                                                if (this.mOminLevelCountLevelPointsList != null) {
                                                    this.mOminLevelCountLevelPointsList.clear();
                                                }
                                            } else if (name.equals("Point") && OminLevelCountLevelLinePointsListsLoadStarted) {
                                                Point currentPoint6 = new Point();
                                                String s6 = parser.nextText();
                                                currentPoint6.x = Float.parseFloat(s6.split(",")[0]);
                                                currentPoint6.y = Float.parseFloat(s6.split(",")[1]);
                                                if (this.mOminLevelCountLevelPointsList == null) {
                                                    this.mOminLevelCountLevelPointsList = new ArrayList();
                                                }
                                                this.mOminLevelCountLevelPointsList.add(currentPoint6);
                                            } else if (name.equals("AdaptingBrightness0LuxLevel")) {
                                                this.mDarkAdaptingBrightness0LuxLevel = Integer.parseInt(parser.nextText());
                                            } else if (name.equals("AdaptedBrightness0LuxLevel")) {
                                                this.mDarkAdaptedBrightness0LuxLevel = Integer.parseInt(parser.nextText());
                                            }
                                        }
                                    } else if (this.mDeviceActualBrightnessLevel == 0) {
                                        try {
                                            if (DEBUG) {
                                                Slog.i(TAG, "actualDeviceLevel = 0, load started");
                                            }
                                            configGroupLoadStarted = true;
                                            String str = name;
                                            break;
                                        } catch (XmlPullParserException e) {
                                            e = e;
                                            String str2 = name;
                                            XmlPullParser xmlPullParser = parser;
                                            XmlPullParserException xmlPullParserException = e;
                                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e2) {
                                            e = e2;
                                            String str3 = name;
                                            XmlPullParser xmlPullParser2 = parser;
                                            IOException iOException = e;
                                            Slog.e(TAG, "getConfigFromXML : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e3) {
                                            e = e3;
                                            String str4 = name;
                                            XmlPullParser xmlPullParser3 = parser;
                                            NumberFormatException numberFormatException = e;
                                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else {
                                        String deviceLevelString = parser.getAttributeValue(null, "level");
                                        if (deviceLevelString == null) {
                                            CameraBrightnessLinePointsListLoaded2 = CameraBrightnessLinePointsListLoaded3;
                                        } else if (deviceLevelString.length() == 0) {
                                            String str5 = deviceLevelString;
                                            CameraBrightnessLinePointsListLoaded2 = CameraBrightnessLinePointsListLoaded3;
                                        } else {
                                            int deviceLevel = Integer.parseInt(deviceLevelString);
                                            String str6 = deviceLevelString;
                                            if (deviceLevel == this.mDeviceActualBrightnessLevel) {
                                                if (DEBUG) {
                                                    int i2 = deviceLevel;
                                                    StringBuilder sb2 = new StringBuilder();
                                                    CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded3;
                                                    try {
                                                        sb2.append("actualDeviceLevel = ");
                                                        sb2.append(this.mDeviceActualBrightnessLevel);
                                                        sb2.append(", find matched level in XML, load start");
                                                        Slog.i(TAG, sb2.toString());
                                                    } catch (XmlPullParserException e4) {
                                                        e = e4;
                                                        String str7 = name;
                                                        XmlPullParser xmlPullParser4 = parser;
                                                        boolean z = DefaultBrightnessLoaded3;
                                                        boolean z2 = DefaultBrighnessLinePointsListLoaded;
                                                        boolean z3 = CameraBrightnessLinePointsListLoaded;
                                                        XmlPullParserException xmlPullParserException2 = e;
                                                        Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                                        Slog.e(TAG, "getConfigFromeXML false!");
                                                        return false;
                                                    } catch (IOException e5) {
                                                        e = e5;
                                                        String str8 = name;
                                                        XmlPullParser xmlPullParser5 = parser;
                                                        boolean z4 = DefaultBrightnessLoaded3;
                                                        boolean z5 = DefaultBrighnessLinePointsListLoaded;
                                                        boolean z6 = CameraBrightnessLinePointsListLoaded;
                                                        IOException iOException2 = e;
                                                        Slog.e(TAG, "getConfigFromXML : IOException");
                                                        Slog.e(TAG, "getConfigFromeXML false!");
                                                        return false;
                                                    } catch (NumberFormatException e6) {
                                                        e = e6;
                                                        String str9 = name;
                                                        XmlPullParser xmlPullParser6 = parser;
                                                        boolean z7 = DefaultBrightnessLoaded3;
                                                        boolean z8 = DefaultBrighnessLinePointsListLoaded;
                                                        boolean z9 = CameraBrightnessLinePointsListLoaded;
                                                        NumberFormatException numberFormatException2 = e;
                                                        Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                                        Slog.e(TAG, "getConfigFromeXML false!");
                                                        return false;
                                                    }
                                                } else {
                                                    CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded3;
                                                }
                                                configGroupLoadStarted = true;
                                            } else {
                                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded3;
                                            }
                                        }
                                        if (DEBUG) {
                                            Slog.i(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                                        }
                                        configGroupLoadStarted = true;
                                    }
                                    DefaultBrighnessLinePointsListLoaded2 = DefaultBrighnessLinePointsListLoaded;
                                    CameraBrightnessLinePointsListLoaded3 = CameraBrightnessLinePointsListLoaded;
                                    break;
                                } catch (XmlPullParserException e7) {
                                    e = e7;
                                    boolean z10 = CameraBrightnessLinePointsListLoaded3;
                                    String str10 = name;
                                    XmlPullParserException xmlPullParserException22 = e;
                                    Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                    Slog.e(TAG, "getConfigFromeXML false!");
                                    return false;
                                } catch (IOException e8) {
                                    e = e8;
                                    boolean z11 = CameraBrightnessLinePointsListLoaded3;
                                    String str11 = name;
                                    IOException iOException22 = e;
                                    Slog.e(TAG, "getConfigFromXML : IOException");
                                    Slog.e(TAG, "getConfigFromeXML false!");
                                    return false;
                                } catch (NumberFormatException e9) {
                                    e = e9;
                                    boolean z12 = CameraBrightnessLinePointsListLoaded3;
                                    String str12 = name;
                                    NumberFormatException numberFormatException22 = e;
                                    Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                    Slog.e(TAG, "getConfigFromeXML false!");
                                    return false;
                                }
                            } catch (XmlPullParserException e10) {
                                e = e10;
                                boolean z13 = CameraBrightnessLinePointsListLoaded3;
                                XmlPullParserException xmlPullParserException222 = e;
                                Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (IOException e11) {
                                e = e11;
                                boolean z14 = CameraBrightnessLinePointsListLoaded3;
                                IOException iOException222 = e;
                                Slog.e(TAG, "getConfigFromXML : IOException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (NumberFormatException e12) {
                                e = e12;
                                boolean z15 = CameraBrightnessLinePointsListLoaded3;
                                NumberFormatException numberFormatException222 = e;
                                Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            }
                        case 3:
                            try {
                                DefaultBrighnessLinePointsListLoaded = DefaultBrighnessLinePointsListLoaded2;
                                String name2 = parser.getName();
                                try {
                                    if (!name2.equals(XML_NAME_NOEXT) || !configGroupLoadStarted) {
                                        if (!configGroupLoadStarted) {
                                            int i3 = eventType;
                                        } else if (name2.equals("DefaultBrightnessPoints")) {
                                            int i4 = eventType;
                                            try {
                                                if (this.mDefaultBrighnessLinePointsList != null) {
                                                    DefaultBrighnessLinePointsListsLoadStarted = false;
                                                    String str13 = name2;
                                                    DefaultBrighnessLinePointsListLoaded2 = true;
                                                    break;
                                                } else {
                                                    try {
                                                        Slog.e(TAG, "no DefaultBrightnessPoints loaded!");
                                                        return false;
                                                    } catch (XmlPullParserException e13) {
                                                        e = e13;
                                                        XmlPullParser xmlPullParser7 = parser;
                                                        String str14 = name2;
                                                        boolean z16 = DefaultBrightnessLoaded3;
                                                        boolean z17 = DefaultBrighnessLinePointsListLoaded;
                                                        XmlPullParserException xmlPullParserException2222 = e;
                                                        Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                                        Slog.e(TAG, "getConfigFromeXML false!");
                                                        return false;
                                                    } catch (IOException e14) {
                                                        e = e14;
                                                        XmlPullParser xmlPullParser8 = parser;
                                                        String str15 = name2;
                                                        boolean z18 = DefaultBrightnessLoaded3;
                                                        boolean z19 = DefaultBrighnessLinePointsListLoaded;
                                                        IOException iOException2222 = e;
                                                        Slog.e(TAG, "getConfigFromXML : IOException");
                                                        Slog.e(TAG, "getConfigFromeXML false!");
                                                        return false;
                                                    } catch (NumberFormatException e15) {
                                                        e = e15;
                                                        XmlPullParser xmlPullParser9 = parser;
                                                        String str16 = name2;
                                                        boolean z20 = DefaultBrightnessLoaded3;
                                                        boolean z21 = DefaultBrighnessLinePointsListLoaded;
                                                        NumberFormatException numberFormatException2222 = e;
                                                        Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                                        Slog.e(TAG, "getConfigFromeXML false!");
                                                        return false;
                                                    }
                                                }
                                            } catch (XmlPullParserException e16) {
                                                e = e16;
                                                XmlPullParser xmlPullParser10 = parser;
                                                String str17 = name2;
                                                boolean DefaultBrighnessLinePointsListsLoadStarted2 = DefaultBrightnessLoaded3;
                                                boolean z22 = DefaultBrighnessLinePointsListLoaded;
                                                XmlPullParserException xmlPullParserException22222 = e;
                                                Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                                Slog.e(TAG, "getConfigFromeXML false!");
                                                return false;
                                            } catch (IOException e17) {
                                                e = e17;
                                                XmlPullParser xmlPullParser11 = parser;
                                                String str18 = name2;
                                                boolean DefaultBrighnessLinePointsListsLoadStarted3 = DefaultBrightnessLoaded3;
                                                boolean z23 = DefaultBrighnessLinePointsListLoaded;
                                                IOException iOException22222 = e;
                                                Slog.e(TAG, "getConfigFromXML : IOException");
                                                Slog.e(TAG, "getConfigFromeXML false!");
                                                return false;
                                            } catch (NumberFormatException e18) {
                                                e = e18;
                                                XmlPullParser xmlPullParser12 = parser;
                                                String str19 = name2;
                                                boolean DefaultBrighnessLinePointsListsLoadStarted4 = DefaultBrightnessLoaded3;
                                                boolean z24 = DefaultBrighnessLinePointsListLoaded;
                                                NumberFormatException numberFormatException22222 = e;
                                                Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                                Slog.e(TAG, "getConfigFromeXML false!");
                                                return false;
                                            }
                                        } else {
                                            int i5 = eventType;
                                            if (name2.equals("CameraBrightnessPoints")) {
                                                CameraBrightnessLinePointsListsLoadStarted = false;
                                                if (this.mCameraBrighnessLinePointsList != null) {
                                                    CameraBrightnessLinePointsListLoaded3 = true;
                                                } else {
                                                    Slog.e(TAG, "no CameraBrightnessPoints loaded!");
                                                    return false;
                                                }
                                            } else if (name2.equals("ReadingBrightnessPoints")) {
                                                ReadingBrightnessLinePointsListsLoadStarted = false;
                                                if (this.mReadingBrighnessLinePointsList == null) {
                                                    Slog.e(TAG, "no ReadingBrightnessPoints loaded!");
                                                    return false;
                                                }
                                            } else if (name2.equals("GameModeBrightnessPoints")) {
                                                GameModeBrightnessLinePointsListsLoadStarted = false;
                                                if (this.mGameModeBrightnessLinePointsList == null) {
                                                    Slog.e(TAG, "no GameModeBrightnessPoints loaded!");
                                                    return false;
                                                }
                                            } else if (name2.equals("PowerSavingBrightnessPoints")) {
                                                PowerSavingBrightnessLinePointsListsLoadStarted = false;
                                                if (this.mPowerSavingBrighnessLinePointsList == null) {
                                                    Slog.e(TAG, "no PowerSavingBrightnessPoints loaded!");
                                                    return false;
                                                }
                                            } else if (name2.equals("OminLevelCountLevelLinePoints")) {
                                                OminLevelCountLevelLinePointsListsLoadStarted = false;
                                                if (this.mOminLevelCountLevelPointsList == null) {
                                                    Slog.e(TAG, "no OminLevelCountLevelPointsList loaded!");
                                                    return false;
                                                }
                                            }
                                        }
                                        String str20 = name2;
                                        DefaultBrighnessLinePointsListLoaded2 = DefaultBrighnessLinePointsListLoaded;
                                        break;
                                    } else {
                                        int i6 = eventType;
                                        loadFinished = true;
                                    }
                                    break;
                                } catch (XmlPullParserException e19) {
                                    e = e19;
                                    XmlPullParser xmlPullParser13 = parser;
                                    String str21 = name2;
                                    XmlPullParserException xmlPullParserException222222 = e;
                                    Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                    Slog.e(TAG, "getConfigFromeXML false!");
                                    return false;
                                } catch (IOException e20) {
                                    e = e20;
                                    XmlPullParser xmlPullParser14 = parser;
                                    String str22 = name2;
                                    IOException iOException222222 = e;
                                    Slog.e(TAG, "getConfigFromXML : IOException");
                                    Slog.e(TAG, "getConfigFromeXML false!");
                                    return false;
                                } catch (NumberFormatException e21) {
                                    e = e21;
                                    XmlPullParser xmlPullParser15 = parser;
                                    String str23 = name2;
                                    NumberFormatException numberFormatException222222 = e;
                                    Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                    Slog.e(TAG, "getConfigFromeXML false!");
                                    return false;
                                }
                            } catch (XmlPullParserException e22) {
                                e = e22;
                                boolean z25 = DefaultBrighnessLinePointsListLoaded2;
                                XmlPullParser xmlPullParser16 = parser;
                                boolean z26 = DefaultBrightnessLoaded3;
                                XmlPullParserException xmlPullParserException2222222 = e;
                                Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (IOException e23) {
                                e = e23;
                                boolean z27 = DefaultBrighnessLinePointsListLoaded2;
                                XmlPullParser xmlPullParser17 = parser;
                                boolean z28 = DefaultBrightnessLoaded3;
                                IOException iOException2222222 = e;
                                Slog.e(TAG, "getConfigFromXML : IOException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (NumberFormatException e24) {
                                e = e24;
                                boolean z29 = DefaultBrighnessLinePointsListLoaded2;
                                XmlPullParser xmlPullParser18 = parser;
                                boolean z30 = DefaultBrightnessLoaded3;
                                NumberFormatException numberFormatException2222222 = e;
                                Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            }
                        default:
                            int i7 = eventType;
                            boolean z31 = DefaultBrighnessLinePointsListLoaded2;
                            boolean z32 = CameraBrightnessLinePointsListLoaded3;
                            break;
                    }
                } else {
                    int i8 = eventType;
                    boolean z33 = DefaultBrighnessLinePointsListLoaded2;
                    boolean z34 = CameraBrightnessLinePointsListLoaded3;
                    DefaultBrightnessLoaded = DefaultBrightnessLoaded3;
                }
                InputStream inputStream = inStream;
            }
            if (!DefaultBrightnessLoaded || !DefaultBrighnessLinePointsListLoaded2) {
                DefaultBrightnessLoaded2 = DefaultBrightnessLoaded;
                if (!configGroupLoadStarted) {
                    try {
                        sb = new StringBuilder();
                        XmlPullParser xmlPullParser19 = parser;
                    } catch (XmlPullParserException e25) {
                        e = e25;
                        XmlPullParser xmlPullParser20 = parser;
                        boolean z35 = DefaultBrightnessLoaded2;
                        XmlPullParserException xmlPullParserException22222222 = e;
                        Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return false;
                    } catch (IOException e26) {
                        e = e26;
                        XmlPullParser xmlPullParser21 = parser;
                        boolean z36 = DefaultBrightnessLoaded2;
                        IOException iOException22222222 = e;
                        Slog.e(TAG, "getConfigFromXML : IOException");
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return false;
                    } catch (NumberFormatException e27) {
                        e = e27;
                        XmlPullParser xmlPullParser22 = parser;
                        boolean z37 = DefaultBrightnessLoaded2;
                        NumberFormatException numberFormatException22222222 = e;
                        Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return false;
                    }
                    try {
                        sb.append("actualDeviceLevel = ");
                        sb.append(this.mDeviceActualBrightnessLevel);
                        sb.append(", can't find matched level in XML, load failed!");
                        Slog.e(TAG, sb.toString());
                        return false;
                    } catch (XmlPullParserException e28) {
                        e = e28;
                        XmlPullParserException xmlPullParserException222222222 = e;
                        Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return false;
                    } catch (IOException e29) {
                        e = e29;
                        IOException iOException222222222 = e;
                        Slog.e(TAG, "getConfigFromXML : IOException");
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return false;
                    } catch (NumberFormatException e30) {
                        e = e30;
                        NumberFormatException numberFormatException222222222 = e;
                        Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return false;
                    }
                } else {
                    XmlPullParser xmlPullParser23 = parser;
                    boolean z38 = DefaultBrightnessLoaded2;
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                }
            } else {
                try {
                    if (DEBUG) {
                        DefaultBrightnessLoaded2 = DefaultBrightnessLoaded;
                        try {
                            Slog.i(TAG, "getConfigFromeXML success!");
                        } catch (XmlPullParserException e31) {
                            e = e31;
                            XmlPullParser xmlPullParser24 = parser;
                        } catch (IOException e32) {
                            e = e32;
                            XmlPullParser xmlPullParser25 = parser;
                            IOException iOException2222222222 = e;
                            Slog.e(TAG, "getConfigFromXML : IOException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        } catch (NumberFormatException e33) {
                            e = e33;
                            XmlPullParser xmlPullParser26 = parser;
                            NumberFormatException numberFormatException2222222222 = e;
                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        }
                    } else {
                        boolean z39 = DefaultBrightnessLoaded;
                    }
                    return true;
                } catch (XmlPullParserException e34) {
                    e = e34;
                    boolean z40 = DefaultBrightnessLoaded;
                    XmlPullParser xmlPullParser27 = parser;
                    XmlPullParserException xmlPullParserException2222222222 = e;
                    Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                } catch (IOException e35) {
                    e = e35;
                    boolean z41 = DefaultBrightnessLoaded;
                    XmlPullParser xmlPullParser28 = parser;
                    IOException iOException22222222222 = e;
                    Slog.e(TAG, "getConfigFromXML : IOException");
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                } catch (NumberFormatException e36) {
                    e = e36;
                    boolean z42 = DefaultBrightnessLoaded;
                    XmlPullParser xmlPullParser29 = parser;
                    NumberFormatException numberFormatException22222222222 = e;
                    Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                    Slog.e(TAG, "getConfigFromeXML false!");
                    return false;
                }
            }
        } catch (XmlPullParserException e37) {
            e = e37;
            XmlPullParser xmlPullParser30 = parser;
            XmlPullParserException xmlPullParserException22222222222 = e;
            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        } catch (IOException e38) {
            e = e38;
            XmlPullParser xmlPullParser31 = parser;
            IOException iOException222222222222 = e;
            Slog.e(TAG, "getConfigFromXML : IOException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        } catch (NumberFormatException e39) {
            e = e39;
            XmlPullParser xmlPullParser32 = parser;
            NumberFormatException numberFormatException222222222222 = e;
            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        }
    }

    public void updateCurrentUserId(int userId) {
        if (DEBUG) {
            Slog.d(TAG, "save old user's paras and load new user's paras when user change ");
        }
        saveOffsetParas();
        this.mCurrentUserId = userId;
        loadOffsetParas();
        unlockDarkAdaptLine();
    }

    public void loadOffsetParas() {
        this.mPosBrightnessSaved = Settings.System.getFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId) * maxBrightness;
        this.mPosBrightness = this.mPosBrightnessSaved;
        this.mDeltaSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_delta", 0.0f, this.mCurrentUserId);
        this.mDeltaNew = this.mDeltaSaved;
        boolean z = true;
        if (Settings.System.getIntForUser(this.mContentResolver, "spline_is_user_change", 0, this.mCurrentUserId) != 1) {
            z = false;
        }
        this.mIsUserChangeSaved = z;
        this.mIsUserChange = this.mIsUserChangeSaved;
        this.mOffsetBrightness_lastSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_offset_brightness_last", minBrightness, this.mCurrentUserId);
        this.mOffsetBrightness_last = this.mOffsetBrightness_lastSaved;
        this.mLastLuxDefaultBrightnessSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", minBrightness, this.mCurrentUserId);
        this.mLastLuxDefaultBrightness = this.mLastLuxDefaultBrightnessSaved;
        this.mStartLuxDefaultBrightnessSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", minBrightness, this.mCurrentUserId);
        this.mStartLuxDefaultBrightness = this.mStartLuxDefaultBrightnessSaved;
        this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        this.mAmLuxOffset = Settings.System.getFloatForUser(this.mContentResolver, "spline_ambient_lux_offset", -1.0f, this.mCurrentUserId);
        if (this.mOminLevelModeEnable) {
            this.mOminLevelCountSaved = Settings.System.getIntForUser(this.mContentResolver, "spline_ominlevel_count", 0, this.mCurrentUserId);
            this.mOminLevelCount = this.mOminLevelCountSaved;
            this.mOminLevelCountResetLongSetTimeSaved = Settings.System.getIntForUser(this.mContentResolver, "spline_ominlevel_time", 0, this.mCurrentUserId);
            this.mOminLevelCountResetLongSetTime = this.mOminLevelCountResetLongSetTimeSaved;
            Slog.i(TAG, "mOminLevelMode read mOminLevelCount=" + this.mOminLevelCount + ",mOminLevelCountResetLongSetTime=" + this.mOminLevelCountResetLongSetTime);
        }
        if (this.mManualMode && this.mStartLuxDefaultBrightness >= ((float) this.mManualBrightnessMaxLimit) && this.mPosBrightness == ((float) this.mManualBrightnessMaxLimit)) {
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            Slog.i(TAG, "updateLevel outdoor no offset set mDelta=0");
        }
        if (DEBUG) {
            Slog.d(TAG, "Read:userId=" + this.mCurrentUserId + ",mPosBrightness=" + this.mPosBrightness + ",mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mDelta=" + this.mDelta + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset);
        }
    }

    private void saveOffsetParas() {
        if (((int) (this.mPosBrightness * 10.0f)) != ((int) (this.mPosBrightnessSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", this.mPosBrightness / maxBrightness, this.mCurrentUserId);
            this.mPosBrightnessSaved = this.mPosBrightness;
        }
        if (((int) (this.mDeltaNew * 10.0f)) != ((int) (this.mDeltaSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_delta", this.mDeltaNew, this.mCurrentUserId);
            this.mDeltaSaved = this.mDeltaNew;
        }
        if (this.mIsUserChange != this.mIsUserChangeSaved) {
            ContentResolver contentResolver = this.mContentResolver;
            int i = 1;
            if (!this.mIsUserChange) {
                i = 0;
            }
            Settings.System.putIntForUser(contentResolver, "spline_is_user_change", i, this.mCurrentUserId);
            this.mIsUserChangeSaved = this.mIsUserChange;
        }
        if (((int) (this.mOffsetBrightness_last * 10.0f)) != ((int) (this.mOffsetBrightness_lastSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_offset_brightness_last", this.mOffsetBrightness_last, this.mCurrentUserId);
            this.mOffsetBrightness_lastSaved = this.mOffsetBrightness_last;
        }
        if (((int) (this.mLastLuxDefaultBrightness * 10.0f)) != ((int) (this.mLastLuxDefaultBrightnessSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", this.mLastLuxDefaultBrightness, this.mCurrentUserId);
            this.mLastLuxDefaultBrightnessSaved = this.mLastLuxDefaultBrightness;
        }
        if (((int) (this.mStartLuxDefaultBrightness * 10.0f)) != ((int) (this.mStartLuxDefaultBrightnessSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", this.mStartLuxDefaultBrightness, this.mCurrentUserId);
            this.mStartLuxDefaultBrightnessSaved = this.mStartLuxDefaultBrightness;
        }
        if (((int) (this.mAmLux * 10.0f)) != ((int) (this.mAmLuxSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_ambient_lux", this.mAmLux, this.mCurrentUserId);
            this.mAmLuxSaved = this.mAmLux;
        }
        if (((int) (this.mAmLuxOffset * 10.0f)) != ((int) (this.mAmLuxOffsetSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_ambient_lux_offset", this.mAmLuxOffset, this.mCurrentUserId);
            this.mAmLuxOffsetSaved = this.mAmLuxOffset;
        }
        if (this.mOminLevelCount != this.mOminLevelCountSaved) {
            Settings.System.putIntForUser(this.mContentResolver, "spline_ominlevel_count", this.mOminLevelCount, this.mCurrentUserId);
            this.mOminLevelCountSaved = this.mOminLevelCount;
            Slog.i(TAG, "mOminLevelMode saved mOminLevelCount=" + this.mOminLevelCount);
        }
        if (this.mOminLevelCountResetLongSetTime != this.mOminLevelCountResetLongSetTimeSaved) {
            Settings.System.putIntForUser(this.mContentResolver, "spline_ominlevel_time", this.mOminLevelCountResetLongSetTime, this.mCurrentUserId);
            this.mOminLevelCountResetLongSetTimeSaved = this.mOminLevelCountResetLongSetTime;
            Slog.i(TAG, "mOminLevelMode saved mOminLevelCountResetLongSetTime=" + this.mOminLevelCountResetLongSetTime);
        }
        if (HWDEBUG) {
            Slog.d(TAG, "write:userId=" + this.mCurrentUserId + ",mPosBrightness =" + this.mPosBrightness + ",mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness + ",mAmLux=" + this.mAmLux + ",mAmLuxOffset=" + this.mAmLuxOffset);
        }
    }

    public static HwNormalizedSpline createHwNormalizedSpline(Context context, int deviceActualBrightnessLevel, int deviceActualBrightnessNit, int deviceStandardBrightnessNit) {
        return new HwNormalizedSpline(context, deviceActualBrightnessLevel, deviceActualBrightnessNit, deviceStandardBrightnessNit);
    }

    public String toString() {
        return new StringBuilder().toString();
    }

    public float interpolate(float x) {
        this.mAmLux = x;
        if (this.mPosBrightness == 0.0f) {
            this.mIsReboot = true;
        } else {
            this.mIsReboot = false;
        }
        if (HWDEBUG) {
            Slog.d(TAG, "interpolate:mPosBrightness=" + this.mPosBrightness + "lux=" + x + ",mIsReboot=" + this.mIsReboot + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta);
        }
        float value_interp = getInterpolatedValue(this.mPosBrightness, x) / maxBrightness;
        saveOffsetParas();
        return value_interp;
    }

    public void updateLevelWithLux(float PosBrightness, float lux) {
        if (lux < 0.0f) {
            Slog.e(TAG, "error input lux,lux=" + lux);
            return;
        }
        if (!this.mIsReboot) {
            this.mIsUserChange = true;
        }
        this.mAmLuxOffset = lux;
        if (this.mPersonalizedBrightnessEnable) {
            float defaultBrightness = getCurrentBrightness(lux);
            if (!this.mDayModeAlgoEnable || !this.mDayModeEnable || getBrightnessMode() != BrightnessModeState.NewCurveMode) {
                this.mStartLuxDefaultBrightness = defaultBrightness;
            } else {
                float oldBrightness = this.mStartLuxDefaultBrightness;
                this.mStartLuxDefaultBrightness = defaultBrightness > this.mDayModeMinimumBrightness ? defaultBrightness : this.mDayModeMinimumBrightness;
                if (DEBUG && oldBrightness != this.mStartLuxDefaultBrightness) {
                    Slog.d(TAG, "updateLevel DayMode: defaultBrightness =" + defaultBrightness + ", mDayModeMinimumBrightness =" + this.mDayModeMinimumBrightness);
                }
            }
        } else if (this.mDarkAdaptEnable) {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(getCurrentDarkAdaptLine(), lux);
            if (PosBrightness == 0.0f || PosBrightness >= this.mStartLuxDefaultBrightness) {
                this.mDarkAdaptLineLocked = false;
            } else {
                this.mDarkAdaptLineLocked = true;
            }
            if (DEBUG) {
                Slog.d(TAG, "updateLevel DarkAdapt: mDefaultBrightness = " + this.mStartLuxDefaultBrightness + ", locked = " + this.mDarkAdaptLineLocked);
            }
        } else if (!this.mOminLevelCountEnable || !this.mOminLevelModeEnable) {
            if (!this.mDayModeAlgoEnable || !this.mDayModeEnable) {
                this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
            } else {
                this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDayBrighnessLinePointsList, lux);
                if (DEBUG) {
                    Slog.d(TAG, "updateLevel DayMode: mDefaultBrightnessFromLux =" + this.mDefaultBrightnessFromLux);
                }
            }
        } else if ((!this.mDayModeAlgoEnable || !this.mDayModeEnable) && !this.mOminLevelDayModeEnable) {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
        } else {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mOminLevelBrighnessLinePointsList, lux);
            if (DEBUG) {
                Slog.d(TAG, "updateLevel mOminLevelMode:mDefaultBrightness=" + this.mDefaultBrightnessFromLux);
            }
        }
        this.mPosBrightness = PosBrightness;
        if (!this.mManualMode || this.mStartLuxDefaultBrightness < ((float) this.mManualBrightnessMaxLimit) || this.mPosBrightness != ((float) this.mManualBrightnessMaxLimit)) {
            this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
            this.mDeltaNew = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        } else {
            this.mAmLuxOffset = -1.0f;
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            if (DEBUG) {
                Slog.i(TAG, "updateLevel outdoor no offset mDelta=0");
            }
        }
        if (this.mPosBrightness == 0.0f) {
            this.mAmLuxOffset = -1.0f;
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            this.mOffsetBrightness_last = 0.0f;
            this.mLastLuxDefaultBrightness = 0.0f;
            this.mStartLuxDefaultBrightness = 0.0f;
            this.mDarkAdaptLineLocked = false;
            clearGameOffsetDelta();
        }
        if (this.mOminLevelModeEnable) {
            updateOminLevelCount(lux);
        } else {
            this.mOminLevelCountResetLongSetTime = 0;
            this.mOminLevelCount = 0;
        }
        if (this.mVehicleModeEnable && this.mVehicleModeBrightnessEnable && lux < this.mVehicleModeLuxThreshold) {
            this.mVehicleModeClearOffsetEnable = true;
            Slog.i(TAG, "VehicleBrightMode updateLevel in VehicleBrightnessMode");
        }
        checkErrorCorrectionOffset();
        if (DEBUG) {
            Slog.d(TAG, "updateLevel:mDelta=" + this.mDelta + ",mDeltaNew=" + this.mDeltaNew + ",mPosBrightness=" + this.mPosBrightness + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",lux=" + lux);
        }
        saveOffsetParas();
    }

    public void updateLevelGameWithLux(float PosBrightness, float lux) {
        this.mGameModePosBrightness = PosBrightness;
        if (PosBrightness != 0.0f) {
            this.mGameModeStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mGameModeBrightnessLinePointsList, lux);
            this.mDeltaTmp = PosBrightness - this.mGameModeStartLuxDefaultBrightness;
            this.mGameModeOffsetLux = lux;
        } else {
            clearGameOffsetDelta();
        }
        if (DEBUG) {
            Slog.d(TAG, "GameBrightMode updateLevelTmp:mDeltaTmp=" + this.mDeltaTmp + ",mGameModePosBrightness=" + this.mGameModePosBrightness + ",mGameModeStartLuxDefaultBrightness=" + this.mGameModeStartLuxDefaultBrightness + ",lux=" + lux);
        }
    }

    public void setGameCurveLevel(int curveLevel) {
        if (curveLevel == 21) {
            setGameModeEnable(true);
        } else {
            setGameModeEnable(false);
        }
    }

    public void setGameModeEnable(boolean gameModeBrightnessEnable) {
        this.mGameModeBrightnessEnable = gameModeBrightnessEnable;
    }

    public void clearGameOffsetDelta() {
        if (this.mDeltaTmp != 0.0f) {
            if (DEBUG) {
                Slog.d(TAG, "GameBrightMode updateLevelTmp clearGameOffsetDelta,mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux);
            }
            this.mDeltaTmp = 0.0f;
            this.mGameModeOffsetLux = -1.0f;
            this.mGameModePosBrightness = 0.0f;
        }
    }

    public void updateOminLevelCount(float lux) {
        int currentMinuteTime = (int) (System.currentTimeMillis() / 60000);
        int deltaMinuteTime = currentMinuteTime - this.mOminLevelCountResetLongSetTime;
        if (deltaMinuteTime >= this.mOminLevelCountResetLongTimeTh || deltaMinuteTime < 0) {
            this.mOminLevelCount = resetOminLevelCount(this.mOminLevelCountLevelPointsList, (float) this.mOminLevelCount);
            this.mOminLevelCountResetLongSetTime = currentMinuteTime;
            if (DEBUG) {
                Slog.d(TAG, "mOminLevelMode reset mOminLevelCount=" + this.mOminLevelCount + ",deltaMinuteTime=" + deltaMinuteTime + ",currenTime=" + currentMinuteTime);
            }
        }
        if (lux >= 0.0f && lux <= ((float) this.mOminLevelCountValidLuxTh)) {
            long currentTime = SystemClock.uptimeMillis();
            float mBrightenDefaultBrightness = getDefaultBrightnessLevelNew(this.mOminLevelBrighnessLinePointsList, lux);
            if ((currentTime - this.mOminLevelCountSetTime) / 1000 >= ((long) this.mOminLevelCountValidTimeTh)) {
                if (DEBUG) {
                    Slog.d(TAG, "mOminLevelMode deltaTime=" + deltaTime + ",ValidTime");
                }
                if (Math.abs(this.mPosBrightness) < 1.0E-7f) {
                    if (this.mOminLevelCount > 0 && this.mOminLevelOffsetCountEnable) {
                        this.mOminLevelCount--;
                        this.mOminLevelValidCount = 0;
                        this.mOminLevelCountSetTime = currentTime;
                        Slog.i(TAG, "mOminLevelMode resetoffset-- count=" + this.mOminLevelCount);
                    }
                } else if (this.mPosBrightness - mBrightenDefaultBrightness > 0.0f) {
                    if (this.mOminLevelCount < getOminLevelCountThMax(this.mOminLevelCountLevelPointsList)) {
                        this.mOminLevelCount++;
                        this.mOminLevelValidCount = 1;
                        this.mOminLevelCountSetTime = currentTime;
                        Slog.i(TAG, "mOminLevelMode brighten++ count=" + this.mOminLevelCount);
                    }
                } else if (this.mPosBrightness - mBrightenDefaultBrightness < 0.0f && this.mOminLevelCount > 0) {
                    this.mOminLevelCount--;
                    this.mOminLevelValidCount = -1;
                    this.mOminLevelCountSetTime = currentTime;
                    Slog.i(TAG, "mOminLevelMode darken-- count=" + this.mOminLevelCount);
                }
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "mOminLevelMode deltaTime=" + deltaTime);
                }
                if (Math.abs(this.mPosBrightness) < 1.0E-7f) {
                    if (this.mOminLevelCount > 0 && this.mOminLevelValidCount >= 0 && this.mOminLevelOffsetCountEnable) {
                        this.mOminLevelCount--;
                        this.mOminLevelValidCount--;
                        this.mOminLevelCountSetTime = currentTime;
                        Slog.i(TAG, "mOminLevelMode resetoffset-- count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                    }
                } else if (this.mPosBrightness - mBrightenDefaultBrightness > 0.0f) {
                    if (this.mOminLevelCount < getOminLevelCountThMax(this.mOminLevelCountLevelPointsList) && this.mOminLevelValidCount <= 0) {
                        this.mOminLevelCount++;
                        this.mOminLevelValidCount++;
                        this.mOminLevelCountSetTime = currentTime;
                        Slog.i(TAG, "mOminLevelMode brighten++ count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                    }
                } else if (this.mPosBrightness - mBrightenDefaultBrightness < 0.0f && this.mOminLevelCount > 0 && this.mOminLevelValidCount >= 0) {
                    this.mOminLevelCount--;
                    this.mOminLevelValidCount--;
                    this.mOminLevelCountSetTime = currentTime;
                    Slog.i(TAG, "mOminLevelMode darken-- count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                }
            }
            updateOminLevelBrighnessLinePoints();
        }
    }

    private int resetOminLevelCount(List<Point> linePointsList, float levelCount) {
        List<Point> linePointsListIn = linePointsList;
        int ominLevelCount = 0;
        if (linePointsList == null) {
            Slog.e(TAG, "mOminLevelMode linePointsList input error!");
            return 0;
        } else if (levelCount <= ((float) getOminLevelCountThMin(linePointsList))) {
            return 0;
        } else {
            if (levelCount >= ((float) getOminLevelCountThMax(linePointsList))) {
                return getOminLevelCountThMax(linePointsList);
            }
            Point temp1 = null;
            for (Point temp : linePointsListIn) {
                if (temp1 == null) {
                    temp1 = temp;
                }
                if (levelCount < temp.x) {
                    ominLevelCount = (int) temp1.x;
                } else {
                    temp1 = temp;
                }
            }
            return ominLevelCount;
        }
    }

    private int getOminLevelCountThMin(List<Point> linePointsList) {
        List<Point> linePointsListIn = linePointsList;
        if (linePointsListIn.size() > 0) {
            return (int) linePointsListIn.get(0).x;
        }
        return 0;
    }

    private int getOminLevelCountThMax(List<Point> linePointsList) {
        List<Point> linePointsListIn = linePointsList;
        int countMax = 0;
        if (linePointsListIn == null) {
            return 0;
        }
        int listSize = linePointsListIn.size();
        if (listSize > 0) {
            countMax = (int) linePointsListIn.get(listSize - 1).x;
        }
        return countMax;
    }

    private float getOminLevelThMin(List<Point> linePointsList) {
        List<Point> linePointsListIn = linePointsList;
        if (linePointsListIn.size() > 0) {
            return linePointsListIn.get(0).y;
        }
        return minBrightness;
    }

    private float getOminLevelThMax(List<Point> linePointsList) {
        List<Point> linePointsListIn = linePointsList;
        float levelMax = minBrightness;
        if (linePointsListIn == null) {
            return minBrightness;
        }
        int listSize = linePointsListIn.size();
        if (listSize > 0) {
            levelMax = linePointsListIn.get(listSize - 1).y;
        }
        return levelMax;
    }

    public boolean getPowerSavingModeBrightnessChangeEnable(float lux, boolean usePowerSavingModeCurveEnable) {
        boolean powerSavingModeBrightnessChangeEnable = false;
        if (this.mPowerSavingBrighnessLineEnable && lux > this.mPowerSavingAmluxThreshold && this.mUsePowerSavingModeCurveEnable != usePowerSavingModeCurveEnable) {
            float mPowerSavingDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mPowerSavingBrighnessLinePointsList, lux);
            float mDefaultBrightnessFromLux2 = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
            if (((int) mPowerSavingDefaultBrightnessFromLux) != ((int) mDefaultBrightnessFromLux2)) {
                powerSavingModeBrightnessChangeEnable = true;
                if (DEBUG) {
                    Slog.d(TAG, "PowerSavingMode lux=" + lux + ",usePgEnable=" + usePowerSavingModeCurveEnable + ",pgBrightness=" + mPowerSavingDefaultBrightnessFromLux + ",mDefaultBrightness=" + mDefaultBrightnessFromLux2);
                }
            }
        }
        this.mUsePowerSavingModeCurveEnable = usePowerSavingModeCurveEnable;
        return powerSavingModeBrightnessChangeEnable;
    }

    public void updateNewBrightnessCurve() {
        if (!this.mPersonalizedBrightnessCurveLoadEnable) {
            if (DEBUG) {
                Slog.d(TAG, "not updateNewBrightnessCurve,mPersonalizedBrightnessCurveLoadEnable=" + this.mPersonalizedBrightnessCurveLoadEnable);
            }
            return;
        }
        this.mNewCurveEnable = false;
        if (this.mBrightnessCurveLow.size() > 0) {
            this.mBrightnessCurveLow.clear();
        }
        this.mBrightnessCurveLow = getBrightnessListFromDB("BrightnessCurveLow");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveLow)) {
            this.mBrightnessCurveLow.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveLow");
            return;
        }
        if (this.mBrightnessCurveMiddle.size() > 0) {
            this.mBrightnessCurveMiddle.clear();
        }
        this.mBrightnessCurveMiddle = getBrightnessListFromDB("BrightnessCurveMiddle");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveMiddle)) {
            this.mBrightnessCurveMiddle.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveMiddle");
            return;
        }
        if (this.mBrightnessCurveHigh.size() > 0) {
            this.mBrightnessCurveHigh.clear();
        }
        this.mBrightnessCurveHigh = getBrightnessListFromDB("BrightnessCurveHigh");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveHigh)) {
            this.mBrightnessCurveHigh.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveHigh");
            return;
        }
        if (this.mBrightnessCurveDefault.size() > 0) {
            this.mBrightnessCurveDefault.clear();
        }
        this.mBrightnessCurveDefault = getBrightnessListFromDB("BrightnessCurveDefault");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveDefault)) {
            this.mBrightnessCurveDefault.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveDefault");
            return;
        }
        if (this.mRebootNewCurveEnable) {
            this.mRebootNewCurveEnable = false;
            this.mNewCurveEnableTmp = false;
            this.mNewCurveEnable = true;
            Slog.i(TAG, "NewCurveMode reboot first updateNewBrightnessCurve success!");
        }
        if (this.mNewCurveEnableTmp) {
            this.mNewCurveEnableTmp = false;
            this.mNewCurveEnable = true;
            clearBrightnessOffset();
            Slog.i(TAG, "NewCurveMode updateNewBrightnessCurve success!");
        }
    }

    public List<Point> getBrightnessListFromDB(String brightnessCurveTag) {
        List<Point> brightnessList = new ArrayList<>();
        if (this.mManager != null) {
            List<Bundle> records = this.mManager.getAllRecords(brightnessCurveTag, new Bundle());
            if (records != null) {
                new StringBuilder();
                for (int i = 0; i < records.size(); i++) {
                    Bundle data = records.get(i);
                    brightnessList.add(new Point(data.getFloat("AmbientLight"), data.getFloat(DisplayEngineDBManager.BrightnessCurveKey.BL)));
                }
            } else {
                Slog.i(TAG, "NewCurveMode brightnessList curve=null,tag=" + brightnessCurveTag);
            }
        }
        return brightnessList;
    }

    private boolean checkBrightnessListIsOK(List<Point> linePointsList) {
        List<Point> linePointsListin = linePointsList;
        if (linePointsListin == null) {
            Slog.e(TAG, "linePointsListin == null");
            return false;
        } else if (linePointsListin.size() <= 2 || linePointsListin.size() >= 100) {
            Slog.e(TAG, "linePointsListin number is wrong");
            return false;
        } else {
            Point lastPoint = null;
            for (Point tmpPoint : linePointsListin) {
                if (lastPoint == null) {
                    lastPoint = tmpPoint;
                } else if (lastPoint.x - tmpPoint.x > -1.0E-6f) {
                    Slog.e(TAG, "linePointsListin list.y is false, lastPoint.x = " + lastPoint.x + ", tmpPoint.x = " + tmpPoint.x);
                    return false;
                } else if (((int) lastPoint.y) > ((int) tmpPoint.y)) {
                    Slog.e(TAG, "linePointsListin check list.y false, lastPoint.y = " + lastPoint.y + ", tmpPoint.y = " + tmpPoint.y);
                    return false;
                } else {
                    lastPoint = tmpPoint;
                }
            }
            return true;
        }
    }

    public void updateNewBrightnessCurveFromTmp() {
        synchronized (mLock) {
            if (this.mNewCurveEnableTmp) {
                this.mNewCurveEnable = false;
                if (this.mBrightnessCurveLow.size() > 0) {
                    this.mBrightnessCurveLow.clear();
                }
                this.mBrightnessCurveLow = cloneList(this.mBrightnessCurveLowTmp);
                if (this.mBrightnessCurveMiddle.size() > 0) {
                    this.mBrightnessCurveMiddle.clear();
                }
                this.mBrightnessCurveMiddle = cloneList(this.mBrightnessCurveMiddleTmp);
                if (this.mBrightnessCurveHigh.size() > 0) {
                    this.mBrightnessCurveHigh.clear();
                }
                this.mBrightnessCurveHigh = cloneList(this.mBrightnessCurveHighTmp);
                if (this.mBrightnessCurveDefault.size() > 0) {
                    this.mBrightnessCurveDefault.clear();
                }
                this.mBrightnessCurveDefault = cloneList(this.mBrightnessCurveDefaultTmp);
                if (this.mNewCurveEnableTmp) {
                    this.mNewCurveEnableTmp = false;
                    this.mNewCurveEnable = true;
                    clearBrightnessOffset();
                    Slog.i(TAG, "NewCurveMode updateNewBrightnessCurve from tmp, success!");
                }
            }
        }
    }

    private List<Point> cloneList(List<Point> list) {
        if (list == null) {
            return null;
        }
        List<Point> newList = new ArrayList<>();
        for (Point point : list) {
            newList.add(new Point(point.x, point.y));
        }
        return newList;
    }

    public void updateNewBrightnessCurveTmp() {
        this.mNewCurveEnableTmp = false;
        if (!this.mPersonalizedBrightnessEnable || !this.mPersonalizedBrightnessCurveLoadEnable) {
            if (DEBUG) {
                Slog.d(TAG, "not updateNewBrightnessCurveTmp,mPersonalizedBrightnessEnable=" + this.mPersonalizedBrightnessEnable + ",mPersonalizedBrightnessCurveLoadEnable=" + this.mPersonalizedBrightnessCurveLoadEnable);
            }
            return;
        }
        if (this.mBrightnessCurveLowTmp.size() > 0) {
            this.mBrightnessCurveLowTmp.clear();
        }
        this.mBrightnessCurveLowTmp = getBrightnessListFromDB("BrightnessCurveLow");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveLowTmp)) {
            this.mBrightnessCurveLowTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveLow");
            return;
        }
        if (this.mBrightnessCurveMiddleTmp.size() > 0) {
            this.mBrightnessCurveMiddleTmp.clear();
        }
        this.mBrightnessCurveMiddleTmp = getBrightnessListFromDB("BrightnessCurveMiddle");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveMiddleTmp)) {
            this.mBrightnessCurveMiddleTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveMiddle");
            return;
        }
        if (this.mBrightnessCurveHighTmp.size() > 0) {
            this.mBrightnessCurveHighTmp.clear();
        }
        this.mBrightnessCurveHighTmp = getBrightnessListFromDB("BrightnessCurveHigh");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveHighTmp)) {
            this.mBrightnessCurveHighTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveHigh");
            return;
        }
        if (this.mBrightnessCurveDefaultTmp.size() > 0) {
            this.mBrightnessCurveDefaultTmp.clear();
        }
        this.mBrightnessCurveDefaultTmp = getBrightnessListFromDB("BrightnessCurveDefault");
        if (!checkBrightnessListIsOK(this.mBrightnessCurveDefaultTmp)) {
            this.mBrightnessCurveDefaultTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveDefault");
            return;
        }
        this.mNewCurveEnableTmp = true;
        if (DEBUG) {
            Slog.d(TAG, "NewCurveMode updateNewBrightnessCurveTmp success!");
        }
        if (!this.mPowerOnEanble) {
            updateNewBrightnessCurveFromTmp();
        }
    }

    public List<Short> getPersonalizedDefaultCurve() {
        if (this.mBrightnessCurveDefaultTmp.isEmpty()) {
            return null;
        }
        List<Short> curveList = new ArrayList<>();
        for (Point point : this.mBrightnessCurveDefaultTmp) {
            int bright = Math.round(point.y);
            int i = 32767;
            if (bright < 32767) {
                i = bright;
            }
            curveList.add(Short.valueOf((short) i));
        }
        return curveList;
    }

    public List<Float> getPersonalizedAlgoParam() {
        if (this.mManager == null) {
            return null;
        }
        List<Bundle> records = this.mManager.getAllRecords("AlgorithmESCW", new Bundle());
        if (records == null || records.isEmpty()) {
            return null;
        }
        List<Float> algoParamList = new ArrayList<>();
        for (Bundle bundle : records) {
            algoParamList.add(Float.valueOf(bundle.getFloat(DisplayEngineDBManager.AlgorithmESCWKey.ESCW)));
        }
        return algoParamList;
    }

    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
        if (this.mCurveLevel != curveLevel) {
            Slog.i(TAG, "NewCurveMode setPersonalizedBrightnessCurveLevel curveLevel=" + curveLevel);
        }
        this.mCurveLevel = curveLevel;
    }

    public void setSceneCurveLevel(int curveLevel) {
        if (this.mVehicleModeEnable) {
            if (curveLevel == 19) {
                this.mVehicleModeBrightnessEnable = true;
                this.mVehicleModeQuitForPowerOnEnable = false;
            } else if (curveLevel == 18) {
                this.mVehicleModeQuitForPowerOnEnable = true;
            }
            if (DEBUG && this.mSceneLevel != curveLevel && (curveLevel == 19 || curveLevel == 18)) {
                Slog.d(TAG, "VehicleBrightMode curveLevel=" + curveLevel + ",VEnable=" + this.mVehicleModeBrightnessEnable);
            }
            this.mSceneLevel = curveLevel;
        }
    }

    public boolean getVehicleModeQuitForPowerOnEnable() {
        return this.mVehicleModeQuitForPowerOnEnable;
    }

    public boolean getVehicleModeBrightnessEnable() {
        return this.mVehicleModeBrightnessEnable;
    }

    public void setVehicleModeQuitEnable() {
        if (this.mVehicleModeBrightnessEnable) {
            this.mVehicleModeBrightnessEnable = false;
            this.mVehicleModeQuitForPowerOnEnable = false;
            if (this.mVehicleModeClearOffsetEnable) {
                this.mVehicleModeClearOffsetEnable = false;
                clearBrightnessOffset();
                Slog.i(TAG, "VehicleBrightMode clear brightnessOffset");
            }
            if (DEBUG) {
                Slog.i(TAG, "VehicleBrightMode set mVehicleModeBrightnessEnable=" + this.mVehicleModeBrightnessEnable);
            }
        }
    }

    public boolean getNewCurveEableTmp() {
        return this.mNewCurveEnableTmp && this.mPersonalizedBrightnessCurveLoadEnable;
    }

    public boolean getNewCurveEable() {
        return this.mNewCurveEnable;
    }

    public void setPowerStatus(boolean powerOnEanble) {
        if (this.mPowerOnEanble != powerOnEanble) {
            this.mPowerOnEanble = powerOnEanble;
        }
    }

    public boolean setNewCurveEnable(boolean enable) {
        if (!enable || !this.mPersonalizedBrightnessCurveLoadEnable) {
            return false;
        }
        Slog.i(TAG, "NewCurveMode updateNewBrightnessCurveReal starting..,mNewCurveEnable=" + this.mNewCurveEnable + ",mNewCurveEnableTmp=" + this.mNewCurveEnableTmp);
        updateNewBrightnessCurveFromTmp();
        return true;
    }

    public void clearBrightnessOffset() {
        if (Math.abs(this.mPosBrightness) > 1.0E-7f) {
            this.mPosBrightness = 0.0f;
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            this.mIsUserChange = false;
            this.mAmLuxOffset = -1.0f;
            saveOffsetParas();
            if (DEBUG) {
                Slog.d(TAG, "NewCurveMode clear tmp brighntess offset");
            }
        }
    }

    private float getLimitedGameModeBrightness(float brightnessIn) {
        float brightnessOut;
        if (!this.mGameModeEnable || !this.mGameModeBrightnessLimitationEnable || !this.mGameModeBrightnessEnable) {
            return brightnessIn;
        }
        if (this.mLastGameModeBrightness <= 0.0f || this.mLastGameModeBrightness < brightnessIn || brightnessIn > this.mGameModeBrightnessFloor) {
            this.mLastGameModeBrightness = brightnessIn;
            return brightnessIn;
        }
        if (this.mLastGameModeBrightness >= this.mGameModeBrightnessFloor) {
            brightnessOut = this.mGameModeBrightnessFloor;
        } else {
            brightnessOut = this.mLastGameModeBrightness;
        }
        if (HWDEBUG && ((int) brightnessOut) != ((int) this.mLastGameModeBrightness)) {
            Slog.i(TAG, "getLimitedGameModeBrightness, brightnessOut = " + brightnessOut);
        }
        this.mLastGameModeBrightness = brightnessOut;
        return brightnessOut;
    }

    private float getCurrentBrightness(float lux) {
        List<Point> brightnessList;
        Object obj;
        new ArrayList();
        switch (getBrightnessMode()) {
            case CameraMode:
                brightnessList = this.mCameraBrighnessLinePointsList;
                break;
            case ReadingMode:
                brightnessList = this.mReadingBrighnessLinePointsList;
                break;
            case GameMode:
                brightnessList = this.mGameModeBrightnessLinePointsList;
                break;
            case NewCurveMode:
                brightnessList = getCurrentNewCureLine();
                break;
            case PowerSavingMode:
                brightnessList = this.mPowerSavingBrighnessLinePointsList;
                break;
            case EyeProtectionMode:
                return this.mEyeProtectionSpline.getEyeProtectionBrightnessLevel(lux);
            case CalibrtionMode:
                brightnessList = this.mDefaultBrighnessLinePointsListCaliBefore;
                break;
            case DarkAdaptMode:
                brightnessList = getCurrentDarkAdaptLine();
                break;
            case OminLevelMode:
                brightnessList = this.mOminLevelBrighnessLinePointsList;
                break;
            case DayMode:
                brightnessList = this.mDayBrighnessLinePointsList;
                break;
            default:
                brightnessList = this.mDefaultBrighnessLinePointsList;
                break;
        }
        if (brightnessList == null || brightnessList.size() == 0) {
            brightnessList = this.mDefaultBrighnessLinePointsList;
            Slog.i(TAG, "NewCurveMode brightnessList null,set mDefaultBrighnessLinePointsList");
        }
        float brightness = getDefaultBrightnessLevelNew(brightnessList, lux);
        if (this.mVehicleModeEnable && this.mVehicleModeBrightnessEnable && lux < this.mVehicleModeLuxThreshold) {
            brightness = brightness > this.mVehicleModeBrighntess ? brightness : this.mVehicleModeBrighntess;
        }
        float brightness2 = getLimitedGameModeBrightness(brightness);
        if (((int) this.mBrightnessForLog) != ((int) brightness2) && HWDEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("NewCurveMode mode=");
            sb.append(getBrightnessMode());
            sb.append(",brightness=");
            sb.append(brightness2);
            sb.append(",lux=");
            sb.append(lux);
            sb.append(",mPis=");
            sb.append(this.mPosBrightness);
            sb.append(",eyeanble=");
            if (this.mEyeProtectionSpline == null) {
                obj = "false";
            } else {
                obj = Boolean.valueOf(this.mEyeProtectionSpline.isEyeProtectionMode());
            }
            sb.append(obj);
            sb.append(",mVehicleModeBrightnessEnable=");
            sb.append(this.mVehicleModeBrightnessEnable);
            Slog.i(TAG, sb.toString());
        }
        this.mBrightnessForLog = brightness2;
        return brightness2;
    }

    private List<Point> getCurrentNewCureLine() {
        if (this.mNewCurveEnable) {
            int currentCurveLevel = getCurrentCurveLevel();
            if (DEBUG && currentCurveLevel != this.mCurrentCurveLevel) {
                if (currentCurveLevel == 0) {
                    Slog.i(TAG, "NewCurveMode mBrightnessCurveLow NewCurveMode=" + this.mCurveLevel);
                } else if (currentCurveLevel == 1) {
                    Slog.i(TAG, "NewCurveMode mBrightnessCurveMiddle NewCurveMode=" + this.mCurveLevel);
                } else if (currentCurveLevel == 2) {
                    Slog.i(TAG, "NewCurveMode mBrightnessCurveHigh NewCurveMode=" + this.mCurveLevel);
                } else {
                    Slog.i(TAG, "NewCurveMode defualt NewCurveMode=" + this.mCurveLevel);
                }
                this.mCurrentCurveLevel = currentCurveLevel;
            }
            if (currentCurveLevel == 0) {
                return this.mBrightnessCurveLow;
            }
            if (currentCurveLevel == 1) {
                return this.mBrightnessCurveMiddle;
            }
            if (currentCurveLevel == 2) {
                return this.mBrightnessCurveHigh;
            }
            return this.mBrightnessCurveDefault;
        }
        Slog.i(TAG, "NewCurveMode NewCurveMode=false,return mDefaultBrighnessLinePointsList");
        return this.mDefaultBrighnessLinePointsList;
    }

    private int getCurrentCurveLevel() {
        return this.mCurveLevel;
    }

    public List<PointF> getCurrentDefaultNewCurveLine() {
        float brightness;
        List<PointF> brightnessList = new ArrayList<>();
        for (int i = 0; i < this.mLuxPonits.length; i++) {
            if (this.mDayModeAlgoEnable) {
                brightness = getDefaultBrightnessLevelNew(this.mDayBrighnessLinePointsList, this.mLuxPonits[i]);
            } else {
                brightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, this.mLuxPonits[i]);
            }
            brightnessList.add(new PointF(this.mLuxPonits[i], brightness));
        }
        Slog.i(TAG, "NewCurveMode getCurrentDefaultNewCurveLine,mDayModeAlgoEnable=" + this.mDayModeAlgoEnable);
        return brightnessList;
    }

    public boolean getPersonalizedBrightnessCurveEnable() {
        boolean z = false;
        if (!this.mNewCurveEnable) {
            return false;
        }
        if (this.mPersonalizedBrightnessEnable && this.mPersonalizedBrightnessCurveLoadEnable) {
            z = true;
        }
        return z;
    }

    public float getDefaultBrightness(float lux) {
        return getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
    }

    public float getNewDefaultBrightness(float lux) {
        List<Point> brightnessList;
        new ArrayList();
        if (getBrightnessMode() == BrightnessModeState.NewCurveMode) {
            brightnessList = this.mBrightnessCurveDefault;
        } else {
            brightnessList = this.mDefaultBrighnessLinePointsList;
        }
        return getDefaultBrightnessLevelNew(brightnessList, lux);
    }

    public float getNewCurrentBrightness(float lux) {
        List<Point> brightnessList;
        new ArrayList();
        if (getBrightnessMode() == BrightnessModeState.NewCurveMode) {
            brightnessList = getCurrentNewCureLine();
        } else {
            brightnessList = this.mDefaultBrighnessLinePointsList;
        }
        return getDefaultBrightnessLevelNew(brightnessList, lux);
    }

    public BrightnessModeState getBrightnessMode() {
        if (this.mCameraModeEnable) {
            return BrightnessModeState.CameraMode;
        }
        if (this.mReadingModeEnable) {
            return BrightnessModeState.ReadingMode;
        }
        if (this.mGameModeEnable && this.mGameModeBrightnessEnable) {
            return BrightnessModeState.GameMode;
        }
        if (this.mNewCurveEnable) {
            return BrightnessModeState.NewCurveMode;
        }
        if (this.mPowerSavingModeEnable && this.mPowerSavingBrighnessLineEnable && this.mAmLux > this.mPowerSavingAmluxThreshold) {
            return BrightnessModeState.PowerSavingMode;
        }
        if (this.mEyeProtectionSpline != null && this.mEyeProtectionSpline.isEyeProtectionMode() && this.mEyeProtectionSplineEnable) {
            return BrightnessModeState.EyeProtectionMode;
        }
        if (this.mCalibrtionModeBeforeEnable) {
            return BrightnessModeState.CalibrtionMode;
        }
        if (this.mDarkAdaptEnable) {
            return BrightnessModeState.DarkAdaptMode;
        }
        if (this.mOminLevelCountEnable && this.mOminLevelModeEnable) {
            return BrightnessModeState.OminLevelMode;
        }
        if (!this.mDayModeAlgoEnable || !this.mDayModeEnable) {
            return BrightnessModeState.DefaultMode;
        }
        return BrightnessModeState.DayMode;
    }

    public boolean getPowerSavingBrighnessLineEnable() {
        return this.mPowerSavingBrighnessLineEnable;
    }

    public float getInterpolatedValue(float PositionBrightness, float lux) {
        float offsetBrightness;
        float PosBrightness = PositionBrightness;
        boolean inDarkAdaptMode = false;
        if (this.mPersonalizedBrightnessEnable) {
            float defaultBrightness = getCurrentBrightness(lux);
            if (((int) this.mDefaultBrightnessFromLux) != ((int) defaultBrightness) && DEBUG) {
                Slog.i(TAG, "BrightenssCurve mode=" + getBrightnessMode() + ",lux=" + lux + ",defaultBrightness=" + defaultBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mPis=" + this.mPosBrightness + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mVehicleEnable=" + this.mVehicleModeBrightnessEnable + ",OffsetNeedClear=" + this.mErrorCorrectionOffsetNeedClear);
            }
            if (!this.mDayModeAlgoEnable || !this.mDayModeEnable || getBrightnessMode() != BrightnessModeState.NewCurveMode) {
                this.mDefaultBrightnessFromLux = defaultBrightness;
            } else {
                float oldBrightness = this.mDefaultBrightnessFromLux;
                this.mDefaultBrightnessFromLux = defaultBrightness > this.mDayModeMinimumBrightness ? defaultBrightness : this.mDayModeMinimumBrightness;
                if (DEBUG && oldBrightness != this.mDefaultBrightnessFromLux) {
                    Slog.d(TAG, "getInterpolatedValue DayMode: defaultBrightness =" + defaultBrightness + ", mDayModeMinimumBrightness =" + this.mDayModeMinimumBrightness);
                }
            }
        } else if (!this.mReadingModeEnable && this.mPowerSavingModeEnable && this.mPowerSavingBrighnessLineEnable && this.mAmLux > this.mPowerSavingAmluxThreshold) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mPowerSavingBrighnessLinePointsList, lux);
            Slog.i(TAG, "PowerSavingMode defualtbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux + ",mCalibrationRatio=" + this.mCalibrationRatio);
        } else if (this.mCameraModeEnable) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mCameraBrighnessLinePointsList, lux);
            Slog.i(TAG, "CameraMode defualtbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux);
        } else if (this.mReadingModeEnable) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mReadingBrighnessLinePointsList, lux);
            Slog.i(TAG, "ReadingMode defaultbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux);
        } else if (this.mGameModeEnable && this.mGameModeBrightnessEnable) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mGameModeBrightnessLinePointsList, lux);
            Slog.i(TAG, "GameBrightMode defaultbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux);
        } else if (this.mEyeProtectionSpline == null || !this.mEyeProtectionSpline.isEyeProtectionMode() || !this.mEyeProtectionSplineEnable) {
            if (this.mCalibrtionModeBeforeEnable) {
                this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsListCaliBefore, lux);
            } else if (this.mDarkAdaptEnable) {
                inDarkAdaptMode = true;
                updateDarkAdaptState();
                this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(getCurrentDarkAdaptLine(), lux);
            } else if (!this.mOminLevelCountEnable || !this.mOminLevelModeEnable) {
                if (!this.mDayModeAlgoEnable || !this.mDayModeEnable) {
                    this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
                } else {
                    this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mDayBrighnessLinePointsList, lux);
                    Slog.i(TAG, "DayMode:getBrightnessLevel lux =" + lux + ", mDefaultBrightnessFromLux =" + this.mDefaultBrightnessFromLux);
                }
            } else if ((!this.mDayModeAlgoEnable || !this.mDayModeEnable) && !this.mOminLevelDayModeEnable) {
                this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
                Slog.i(TAG, "mOminLevelMode:night getBrightnessLevel lux =" + lux + ",mDefaultBrightness=" + this.mDefaultBrightnessFromLux + ",mOCount=" + this.mOminLevelCount);
            } else {
                this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mOminLevelBrighnessLinePointsList, lux);
                Slog.i(TAG, "mOminLevelMode:Day getBrightnessLevel lux =" + lux + ",mDefaultBrightness=" + this.mDefaultBrightnessFromLux + ",mOCount=" + this.mOminLevelCount);
            }
            if (DEBUG && this.mEyeProtectionSpline != null && this.mEyeProtectionSpline.isEyeProtectionMode() && !this.mEyeProtectionSplineEnable) {
                Slog.i(TAG, "getEyeProtectionBrightnessLevel");
            }
        } else {
            this.mDefaultBrightnessFromLux = this.mEyeProtectionSpline.getEyeProtectionBrightnessLevel(lux);
            Slog.i(TAG, "getEyeProtectionBrightnessLevel lux =" + lux + ", mDefaultBrightnessFromLux =" + this.mDefaultBrightnessFromLux);
        }
        if (this.mIsReboot) {
            this.mLastLuxDefaultBrightness = this.mDefaultBrightnessFromLux;
            this.mStartLuxDefaultBrightness = this.mDefaultBrightnessFromLux;
            this.mOffsetBrightness_last = this.mDefaultBrightnessFromLux;
            this.mIsReboot = false;
            this.mIsUserChange = false;
        }
        if (this.mLastLuxDefaultBrightness <= 0.0f && this.mPosBrightness != 0.0f) {
            this.mPosBrightness = 0.0f;
            PosBrightness = 0.0f;
            this.mDelta = 0.0f;
            this.mOffsetBrightness_last = 0.0f;
            this.mLastLuxDefaultBrightness = 0.0f;
            this.mStartLuxDefaultBrightness = 0.0f;
            this.mIsUserChange = false;
            saveOffsetParas();
            if (DEBUG) {
                Slog.d(TAG, "error state for default state");
            }
        }
        float f = this.mDefaultBrightnessFromLux;
        if ((!this.mGameModeEnable || !this.mGameModeBrightnessEnable || this.mDeltaTmp != 0.0f) && ((this.mGameModeBrightnessEnable || PosBrightness != 0.0f) && !this.mCoverModeNoOffsetEnable)) {
            offsetBrightness = (!this.mGameModeEnable || !this.mGameModeBrightnessEnable) ? inDarkAdaptMode ? getDarkAdaptOffset(PosBrightness, lux) : getOffsetBrightnessLevel_new(this.mStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, PosBrightness) : getOffsetBrightnessLevel_withDelta(this.mGameModeStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, this.mGameModePosBrightness, this.mDeltaTmp);
        } else {
            offsetBrightness = this.mDefaultBrightnessFromLux;
            if (this.mCoverModeNoOffsetEnable) {
                this.mCoverModeNoOffsetEnable = false;
                if (DEBUG) {
                    Slog.i(TAG, "set mCoverModeNoOffsetEnable=" + this.mCoverModeNoOffsetEnable);
                }
            }
        }
        if (DEBUG && ((int) offsetBrightness) != ((int) this.mOffsetBrightness_last)) {
            Slog.d(TAG, "offsetBrightness=" + offsetBrightness + ",mOffsetBrightness_last" + this.mOffsetBrightness_last + ",lux=" + lux + ",mPosBrightness=" + this.mPosBrightness + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",mDefaultBrightnessFromLux=" + this.mDefaultBrightnessFromLux + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
        if (DEBUG && this.mGameModeBrightnessEnable && ((int) offsetBrightness) != ((int) this.mOffsetBrightness_last)) {
            Slog.i(TAG, "GameBrightMode mGameModeStartLuxDefaultBrightness=" + this.mGameModeStartLuxDefaultBrightness + ",offsetBrightness=" + offsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModePosBrightness=" + this.mGameModePosBrightness + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux);
        }
        this.mLastLuxDefaultBrightness = this.mDefaultBrightnessFromLux;
        this.mOffsetBrightness_last = offsetBrightness;
        return offsetBrightness;
    }

    public float getDefaultBrightnessLevelNew(List<Point> linePointsList, float lux) {
        int count = 0;
        float brightnessLevel = this.mDefaultBrightness;
        Point temp1 = null;
        for (Point temp : linePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                Point temp2 = temp;
                if (temp2.x > temp1.x) {
                    return (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                float brightnessLevel2 = this.mDefaultBrightness;
                if (!DEBUG) {
                    return brightnessLevel2;
                }
                Slog.i(TAG, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel2;
            }
            temp1 = temp;
            brightnessLevel = temp1.y;
            count++;
        }
        return brightnessLevel;
    }

    /* access modifiers changed from: package-private */
    public float getOffsetBrightnessLevel_new(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        return getOffsetBrightnessLevel_withDelta(brightnessStartOrig, brightnessEndOrig, brightnessStartNew, this.mDelta);
    }

    /* access modifiers changed from: package-private */
    public float getOffsetBrightnessLevel_withDelta(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float delta) {
        float f = brightnessStartOrig;
        float f2 = brightnessEndOrig;
        float f3 = brightnessStartNew;
        if (this.mIsUserChange) {
            this.mIsUserChange = false;
        }
        float ratio = 1.0f;
        float ratio2 = 1.0f;
        float mDeltaStart = delta;
        if (f < f2) {
            if (mDeltaStart > 0.0f) {
                float diff = Math.abs(f - f2);
                float ratio22 = (((-mDeltaStart) * (diff / (mDeltaStart + 1.0E-7f))) / ((maxBrightness - f) + 1.0E-7f)) + 1.0f;
                if (((int) this.mLastBrightnessEndOrig) != ((int) f2) && DEBUG) {
                    Slog.i(TAG, "Orig_ratio2=" + ratio22 + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
                }
                float ratio23 = getBrightenOffsetBrightenRaio(((((1.0f - this.mOffsetBrightenAlphaRight) * Math.max(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetBrightenAlphaRight * ((mDeltaStart * ratio22) + f2))) - f2) / (mDeltaStart + 1.0E-7f), f, f2, f3, mDeltaStart);
                if (ratio23 < 0.0f) {
                    ratio23 = 0.0f;
                }
                ratio2 = ratio23;
            }
            if (mDeltaStart < 0.0f) {
                ratio = getDarkenOffsetBrightenRatio((((-mDeltaStart) * (Math.abs(f - f2) / (mDeltaStart - 1.0E-7f))) / ((maxBrightness - f) + 1.0E-7f)) + 1.0f, f, f2, f3);
                if (ratio < 0.0f) {
                    ratio = 0.0f;
                }
            }
        }
        int i = (f > f2 ? 1 : (f == f2 ? 0 : -1));
        float f4 = minBrightness;
        if (i > 0) {
            if (mDeltaStart < 0.0f) {
                float diff2 = Math.abs(f - f2);
                float ratio24 = ((((1.0f - this.mOffsetDarkenAlphaLeft) * Math.min(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetDarkenAlphaLeft * ((mDeltaStart * (((mDeltaStart * (diff2 / (mDeltaStart - 1.0E-7f))) / ((minBrightness - f) - 1.0E-7f)) + 1.0f)) + f2))) - f2) / (mDeltaStart - 1.0E-7f);
                if (ratio24 < 0.0f) {
                    ratio2 = 0.0f;
                    float f5 = diff2;
                } else {
                    float f6 = diff2;
                    ratio2 = ratio24;
                }
            }
            if (mDeltaStart > 0.0f) {
                float ratioTmp = (float) Math.pow((double) (f2 / (f + 1.0E-7f)), (double) this.mOffsetBrightenRatioLeft);
                ratio = getBrightenOffsetDarkenRatio(((this.mOffsetBrightenAlphaLeft * f2) / (f + 1.0E-7f)) + ((1.0f - this.mOffsetBrightenAlphaLeft) * ratioTmp), f, f2, f3);
                if (((int) this.mLastBrightnessEndOrig) != ((int) f2) && DEBUG) {
                    Slog.d(TAG, "ratio=" + ratio + ",ratioTmp=" + ratioTmp + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
                }
            }
        }
        this.mDeltaNew = mDeltaStart * ratio2 * ratio;
        if (this.mLastBrightnessEndOrig != f2 && DEBUG) {
            Slog.d(TAG, "mDeltaNew=" + this.mDeltaNew + ",mDeltaStart=" + mDeltaStart + ",ratio2=" + ratio2 + ",ratio=" + ratio);
        }
        this.mLastBrightnessEndOrig = f2;
        float brightnessAndDelta = this.mDeltaNew + f2;
        if (brightnessAndDelta > minBrightness) {
            f4 = brightnessAndDelta;
        }
        float offsetBrightnessTemp = f4;
        return offsetBrightnessTemp < maxBrightness ? offsetBrightnessTemp : maxBrightness;
    }

    public float getAmbientValueFromDB() {
        float ambientValue = Settings.System.getFloatForUser(this.mContentResolver, "spline_ambient_lux", 100.0f, this.mCurrentUserId);
        if (((int) ambientValue) < 0) {
            Slog.e(TAG, "error inputValue<min,ambientValue=" + ambientValue);
            ambientValue = (float) 0;
        }
        if (((int) ambientValue) <= 40000) {
            return ambientValue;
        }
        Slog.e(TAG, "error inputValue>max,ambientValue=" + ambientValue);
        return (float) 40000;
    }

    public boolean getCalibrationTestEable() {
        boolean calibrationTestEable = false;
        int calibrtionTest = Settings.System.getIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        if (calibrtionTest == 0) {
            this.mCalibrtionModeBeforeEnable = false;
            return false;
        }
        int calibrtionTestLow = calibrtionTest & HwColorPicker.MASK_RESULT_INDEX;
        int calibrtionTestHigh = 65535 & (calibrtionTest >> 16);
        if (calibrtionTestLow != calibrtionTestHigh) {
            Slog.e(TAG, "error db, clear DB,,calibrtionTestLow=" + calibrtionTestLow + ",calibrtionTestHigh=" + calibrtionTestHigh);
            Settings.System.putIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
            this.mCalibrtionModeBeforeEnable = false;
            return false;
        }
        int calibrtionModeBeforeEnableInt = (calibrtionTestLow >> 1) & 1;
        int calibrationTestEnableInt = calibrtionTestLow & 1;
        if (calibrtionModeBeforeEnableInt == 1) {
            this.mCalibrtionModeBeforeEnable = true;
        } else {
            this.mCalibrtionModeBeforeEnable = false;
        }
        if (calibrationTestEnableInt == 1) {
            calibrationTestEable = true;
        }
        if (calibrtionTest != this.mCalibrtionTest) {
            this.mCalibrtionTest = calibrtionTest;
            if (DEBUG) {
                Slog.d(TAG, "mCalibrtionTest=" + this.mCalibrtionTest + ",calibrationTestEnableInt=" + calibrationTestEnableInt + ",calibrtionModeBeforeEnableInt=" + calibrtionModeBeforeEnableInt);
            }
        }
        return calibrationTestEable;
    }

    public void setEyeProtectionControlFlag(boolean inControlTime) {
        if (this.mEyeProtectionSpline != null) {
            this.mEyeProtectionSpline.setEyeProtectionControlFlag(inControlTime);
        }
    }

    public void setReadingModeEnable(boolean readingModeEnable) {
        this.mReadingModeEnable = readingModeEnable;
    }

    public void setNoOffsetEnable(boolean noOffsetEnable) {
        this.mCoverModeNoOffsetEnable = noOffsetEnable;
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode CoverModeNoOffsetEnable=" + this.mCoverModeNoOffsetEnable);
        }
    }

    public void setCameraModeEnable(boolean cameraModeEnable) {
        this.mCameraModeEnable = cameraModeEnable;
    }

    public void setPowerSavingModeEnable(boolean powerSavingModeEnable) {
        this.mPowerSavingModeEnable = powerSavingModeEnable;
    }

    public float getCurrentDefaultBrightnessNoOffset() {
        return this.mDefaultBrightnessFromLux;
    }

    public float getCurrentAmbientLuxForBrightness() {
        return this.mAmLux;
    }

    public float getCurrentAmbientLuxForOffset() {
        return this.mAmLuxOffset;
    }

    public float getGameModeAmbientLuxForOffset() {
        return this.mGameModeOffsetLux;
    }

    public void setDayModeEnable(boolean dayModeEnable) {
        this.mDayModeEnable = dayModeEnable;
    }

    public void reSetOffsetFromHumanFactor(boolean offsetResetEnable, int minOffsetBrightness, int maxOffsetBrightness) {
        if (!this.mBrightnessOffsetTmpValidEnable || !this.mErrorCorrectionOffsetNeedClear || !offsetResetEnable) {
            if (offsetResetEnable && Math.abs(this.mPosBrightness) > 1.0E-7f) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > 1.0E-7f) {
                    if (((float) minOffsetBrightness) > minBrightness && ((float) minOffsetBrightness) * this.mCalibrationRatio > minBrightness) {
                        minOffsetBrightness = (int) (((float) minOffsetBrightness) * this.mCalibrationRatio);
                    }
                    if (((float) maxOffsetBrightness) < maxBrightness && ((float) maxOffsetBrightness) * this.mCalibrationRatio < maxBrightness) {
                        maxOffsetBrightness = (int) (((float) maxOffsetBrightness) * this.mCalibrationRatio);
                    }
                }
                if (this.mPosBrightness < ((float) minOffsetBrightness)) {
                    this.mPosBrightness = (float) minOffsetBrightness;
                    this.mOffsetBrightness_last = (float) minOffsetBrightness;
                    this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
                    this.mIsReset = true;
                    if (DEBUG) {
                        Slog.d(TAG, "updateLevel:resetMin mPosBrightness=" + this.mPosBrightness + ",min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDelta=" + this.mDelta + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mCalibrationRatio=" + this.mCalibrationRatio);
                    }
                }
                if (this.mPosBrightness > ((float) maxOffsetBrightness)) {
                    this.mPosBrightness = (float) maxOffsetBrightness;
                    this.mOffsetBrightness_last = (float) maxOffsetBrightness;
                    this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
                    this.mIsReset = true;
                    if (DEBUG) {
                        Slog.d(TAG, "updateLevel:resetMax mPosBrightness=" + this.mPosBrightness + ",min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDelta=" + this.mDelta + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mCalibrationRatio=" + this.mCalibrationRatio);
                    }
                }
            }
            return;
        }
        this.mErrorCorrectionOffsetNeedClear = false;
        clearBrightnessOffset();
    }

    public void resetGameModeOffsetFromHumanFactor(int minOffsetBrightness, int maxOffsetBrightness) {
        if (Math.abs(this.mDeltaTmp) > 1.0E-7f) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > 1.0E-7f) {
                if (((float) minOffsetBrightness) > minBrightness && ((float) minOffsetBrightness) * this.mCalibrationRatio > minBrightness) {
                    minOffsetBrightness = (int) (((float) minOffsetBrightness) * this.mCalibrationRatio);
                }
                if (((float) maxOffsetBrightness) < maxBrightness && ((float) maxOffsetBrightness) * this.mCalibrationRatio < maxBrightness) {
                    maxOffsetBrightness = (int) (((float) maxOffsetBrightness) * this.mCalibrationRatio);
                }
            }
            float positionBrightness = this.mGameModeStartLuxDefaultBrightness + this.mDeltaTmp;
            if (positionBrightness < ((float) minOffsetBrightness)) {
                this.mGameModePosBrightness = (float) minOffsetBrightness;
                this.mDeltaTmp = ((float) minOffsetBrightness) - this.mGameModeStartLuxDefaultBrightness;
                if (DEBUG) {
                    Slog.d(TAG, "updateLevel GameMode:resetMin, min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
            if (positionBrightness > ((float) maxOffsetBrightness)) {
                this.mGameModePosBrightness = (float) maxOffsetBrightness;
                this.mDeltaTmp = ((float) maxOffsetBrightness) - this.mGameModeStartLuxDefaultBrightness;
                if (DEBUG) {
                    Slog.d(TAG, "updateLevel GameMode:resetMax, min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
        }
    }

    public void resetGameBrightnessLimitation() {
        this.mLastGameModeBrightness = -1.0f;
        if (DEBUG) {
            Slog.i(TAG, "resetGameBrightnessLimitation, mLastGameModeBrightness set to -1!");
        }
    }

    private void fillDarkAdaptPointsList() {
        if (this.mDarkAdaptingBrightness0LuxLevel != 0 && this.mDarkAdaptedBrightness0LuxLevel != 0) {
            if (this.mDarkAdaptedBrightness0LuxLevel > this.mDarkAdaptingBrightness0LuxLevel) {
                Slog.w(TAG, "fillDarkAdaptPointsList() error adapted=" + this.mDarkAdaptedBrightness0LuxLevel + " is larger than adapting=" + this.mDarkAdaptingBrightness0LuxLevel);
            } else if (this.mDefaultBrighnessLinePointsList != null) {
                float defaultBrighness0LuxLevel = this.mDefaultBrighnessLinePointsList.get(0).y;
                if (((float) this.mDarkAdaptingBrightness0LuxLevel) > defaultBrighness0LuxLevel) {
                    Slog.w(TAG, "fillDarkAdaptPointsList() error adapting=" + this.mDarkAdaptingBrightness0LuxLevel + " is larger than default=" + defaultBrighness0LuxLevel);
                    return;
                }
                this.mDarkAdaptingBrightnessPointsList = cloneListAndReplaceFirstElement(this.mDefaultBrighnessLinePointsList, new Point(0.0f, (float) this.mDarkAdaptingBrightness0LuxLevel));
                this.mDarkAdaptedBrightnessPointsList = cloneListAndReplaceFirstElement(this.mDefaultBrighnessLinePointsList, new Point(0.0f, (float) this.mDarkAdaptedBrightness0LuxLevel));
                this.mDarkAdaptEnable = true;
            }
        }
    }

    private List<Point> cloneListAndReplaceFirstElement(List<Point> list, Point element) {
        List<Point> newList = null;
        if (list == null || element == null) {
            return null;
        }
        for (Point point : list) {
            if (newList == null) {
                newList = new ArrayList<>();
                newList.add(element);
            } else {
                newList.add(new Point(point.x, point.y));
            }
        }
        return newList;
    }

    private void updateDarkAdaptState() {
        if (!this.mDarkAdaptLineLocked && this.mDarkAdaptState != this.mDarkAdaptStateDetected) {
            if (DEBUG) {
                Slog.i(TAG, "updateDarkAdaptState() " + this.mDarkAdaptState + " -> " + this.mDarkAdaptStateDetected);
            }
            this.mDarkAdaptState = this.mDarkAdaptStateDetected;
        }
    }

    private List<Point> getCurrentDarkAdaptLine() {
        switch (this.mDarkAdaptState) {
            case UNADAPTED:
                return this.mDefaultBrighnessLinePointsList;
            case ADAPTING:
                return this.mDarkAdaptingBrightnessPointsList;
            case ADAPTED:
                return this.mDarkAdaptedBrightnessPointsList;
            default:
                return this.mDefaultBrighnessLinePointsList;
        }
    }

    private float getDarkAdaptOffset(float positionBrightness, float lux) {
        float offsetMinLimit;
        float currentOffset = getOffsetBrightnessLevel_new(this.mStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, positionBrightness);
        if (this.mDelta >= 0.0f) {
            if (HWDEBUG) {
                Slog.d(TAG, String.format("getDarkAdaptOffset() mDelta = %.1f, current = %.1f", new Object[]{Float.valueOf(this.mDelta), Float.valueOf(currentOffset)}));
            }
            return currentOffset;
        } else if (this.mDarkAdaptLineLocked) {
            if (HWDEBUG) {
                Slog.d(TAG, String.format("getDarkAdaptOffset() locked, current = %.1f", new Object[]{Float.valueOf(currentOffset)}));
            }
            return currentOffset;
        } else {
            switch (this.mDarkAdaptState) {
                case UNADAPTED:
                    offsetMinLimit = getDefaultBrightnessLevelNew(this.mDarkAdaptingBrightnessPointsList, lux);
                    break;
                case ADAPTING:
                    offsetMinLimit = (getDefaultBrightnessLevelNew(this.mDarkAdaptingBrightnessPointsList, lux) + getDefaultBrightnessLevelNew(this.mDarkAdaptedBrightnessPointsList, lux)) / 2.0f;
                    break;
                case ADAPTED:
                    offsetMinLimit = getDefaultBrightnessLevelNew(this.mDarkAdaptedBrightnessPointsList, lux);
                    break;
                default:
                    offsetMinLimit = minBrightness;
                    break;
            }
            if (HWDEBUG) {
                Slog.d(TAG, String.format("getDarkAdaptOffset() %s, current = %.1f, minLimit = %.1f", new Object[]{this.mDarkAdaptState, Float.valueOf(currentOffset), Float.valueOf(offsetMinLimit)}));
            }
            return currentOffset > offsetMinLimit ? currentOffset : offsetMinLimit;
        }
    }

    public void setDarkAdaptState(DarkAdaptState state) {
        if (this.mDarkAdaptEnable && state != null) {
            this.mDarkAdaptStateDetected = state;
        }
    }

    public void unlockDarkAdaptLine() {
        if (this.mDarkAdaptEnable && this.mDarkAdaptLineLocked) {
            this.mDarkAdaptLineLocked = false;
            if (DEBUG) {
                Slog.i(TAG, "unlockDarkAdaptLine()");
            }
        }
    }

    public boolean isDeltaValid() {
        return this.mPosBrightness > 0.0f && !this.mIsReset;
    }

    public void resetUserDragLimitFlag() {
        this.mIsReset = false;
    }

    public void initBrightenOffsetLux(boolean brightnessOffsetLuxModeEnable, float brightenOffsetLuxTh1, float brightenOffsetLuxTh2, float brightenOffsetLuxTh3) {
        this.mBrightnessOffsetLuxModeEnable = brightnessOffsetLuxModeEnable;
        this.mBrightenOffsetLuxTh1 = brightenOffsetLuxTh1;
        this.mBrightenOffsetLuxTh2 = brightenOffsetLuxTh2;
        this.mBrightenOffsetLuxTh3 = brightenOffsetLuxTh3;
        if (DEBUG) {
            Slog.i(TAG, "initBrightnessOffsetPara,OffsetLuxModeEnable=" + brightnessOffsetLuxModeEnable);
        }
    }

    public void initBrightenOffsetNoValidDarkenLux(boolean brightenOffsetEffectMinLuxEnable, float brightenOffsetNoValidDarkenLuxTh1, float brightenOffsetNoValidDarkenLuxTh2, float brightenOffsetNoValidDarkenLuxTh3, float brightenOffsetNoValidDarkenLuxTh4) {
        this.mBrightenOffsetEffectMinLuxEnable = brightenOffsetEffectMinLuxEnable;
        this.mBrightenOffsetNoValidDarkenLuxTh1 = brightenOffsetNoValidDarkenLuxTh1;
        this.mBrightenOffsetNoValidDarkenLuxTh2 = brightenOffsetNoValidDarkenLuxTh2;
        this.mBrightenOffsetNoValidDarkenLuxTh3 = brightenOffsetNoValidDarkenLuxTh3;
        this.mBrightenOffsetNoValidDarkenLuxTh4 = brightenOffsetNoValidDarkenLuxTh4;
    }

    public void initBrightenOffsetNoValidBrightenLux(float brightenOffsetNoValidBrightenLuxTh1, float brightenOffsetNoValidBrightenLuxTh2, float brightenOffsetNoValidBrightenLuxTh3, float brightenOffsetNoValidBrightenLuxTh4) {
        this.mBrightenOffsetNoValidBrightenLuxTh1 = brightenOffsetNoValidBrightenLuxTh1;
        this.mBrightenOffsetNoValidBrightenLuxTh2 = brightenOffsetNoValidBrightenLuxTh2;
        this.mBrightenOffsetNoValidBrightenLuxTh3 = brightenOffsetNoValidBrightenLuxTh3;
        this.mBrightenOffsetNoValidBrightenLuxTh4 = brightenOffsetNoValidBrightenLuxTh4;
    }

    public void initDarkenOffsetLux(float darkenOffsetLuxTh1, float darkenOffsetLuxTh2, float darkenOffsetLuxTh3) {
        this.mDarkenOffsetLuxTh1 = darkenOffsetLuxTh1;
        this.mDarkenOffsetLuxTh2 = darkenOffsetLuxTh2;
        this.mDarkenOffsetLuxTh3 = darkenOffsetLuxTh3;
    }

    public void initDarkenOffsetNoValidBrightenLux(float darkenOffsetNoValidBrightenLuxTh1, float darkenOffsetNoValidBrightenLuxTh2, float darkenOffsetNoValidBrightenLuxTh3, float darkenOffsetNoValidBrightenLuxTh4) {
        this.mDarkenOffsetNoValidBrightenLuxTh1 = darkenOffsetNoValidBrightenLuxTh1;
        this.mDarkenOffsetNoValidBrightenLuxTh2 = darkenOffsetNoValidBrightenLuxTh2;
        this.mDarkenOffsetNoValidBrightenLuxTh3 = darkenOffsetNoValidBrightenLuxTh3;
        this.mDarkenOffsetNoValidBrightenLuxTh4 = darkenOffsetNoValidBrightenLuxTh4;
    }

    public void initBrightnessOffsetTmpValidPara(boolean brightnessOffsetTmpValidEnable, float brightenOffsetNoValidSavedLuxTh1, float brightenOffsetNoValidSavedLuxTh2) {
        this.mBrightnessOffsetTmpValidEnable = brightnessOffsetTmpValidEnable;
        this.mBrightenOffsetNoValidSavedLuxTh1 = brightenOffsetNoValidSavedLuxTh1;
        this.mBrightenOffsetNoValidSavedLuxTh2 = brightenOffsetNoValidSavedLuxTh2;
        if (DEBUG) {
            Slog.i(TAG, "initBrightnessOffsetPara,OffsetTmpValidEnable=" + this.mBrightnessOffsetTmpValidEnable);
        }
    }

    private float getBrightenOffsetBrightenRaio(float ratio, float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float delta) {
        float brightenRatio = ratio;
        float ambientLuxOffset = (float) ((int) this.mAmLuxOffset);
        if (BrightnessModeState.GameMode == getBrightnessMode()) {
            ambientLuxOffset = (float) ((int) this.mGameModeOffsetLux);
        }
        if (this.mBrightnessOffsetLuxModeEnable && ambientLuxOffset >= 0.0f && Math.abs(this.mPosBrightness) > 1.0E-7f) {
            float noValidBrightenLuxTh = getBrightenOffsetNoValidBrightenLux(ambientLuxOffset);
            float brightenOffsetDelta = 0.0f;
            if (noValidBrightenLuxTh >= 0.0f) {
                brightenOffsetDelta = getCurrentBrightness(noValidBrightenLuxTh) - maxBrightness;
            }
            if (this.mPosBrightness - maxBrightness > brightenOffsetDelta) {
                if (delta < 1.0f) {
                    brightenRatio = 1.0f;
                } else {
                    brightenRatio = ((brightnessEndOrig > brightnessStartNew ? brightnessEndOrig : brightnessStartNew) - brightnessEndOrig) / (delta + 1.0E-7f);
                }
            } else if ((maxBrightness - brightnessStartOrig) + brightenOffsetDelta < 1.0f) {
                brightenRatio = 1.0f;
            } else {
                brightenRatio = ((maxBrightness - brightnessEndOrig) + brightenOffsetDelta) / (((maxBrightness - brightnessStartOrig) + 1.0E-7f) + brightenOffsetDelta);
            }
            if (brightenRatio < 0.0f || brightenRatio > 1.0f) {
                brightenRatio = 0.0f;
            }
            if (((int) this.mLastBrightnessEndOrigIn) != ((int) brightnessEndOrig)) {
                this.mLastBrightnessEndOrigIn = brightnessEndOrig;
                if (DEBUG) {
                    Slog.i(TAG, "BrightenOffset origRatio=" + ratio + "-->brightenRatio=" + brightenRatio + ",mode=" + getBrightnessMode() + ",noValidBrightenLuxTh=" + noValidBrightenLuxTh + ",mPosBrightness=" + this.mPosBrightness + ",BrightenOffsetBrightnessMax=" + (maxBrightness + brightenOffsetDelta) + ",ambientLuxOffset=" + ambientLuxOffset);
                }
            }
        }
        return brightenRatio;
    }

    private float getBrightenOffsetNoValidBrightenLux(float lux) {
        if (lux < this.mBrightenOffsetLuxTh1) {
            return this.mBrightenOffsetNoValidBrightenLuxTh1;
        }
        if (lux < this.mBrightenOffsetLuxTh2) {
            return this.mBrightenOffsetNoValidBrightenLuxTh2;
        }
        if (lux < this.mBrightenOffsetLuxTh3) {
            return this.mBrightenOffsetNoValidBrightenLuxTh3;
        }
        return this.mBrightenOffsetNoValidBrightenLuxTh4;
    }

    private float getDarkenOffsetBrightenRatio(float ratio, float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        float noValidBrightenLuxTh;
        float brightenRatio = ratio;
        float ambientLuxOffset = (float) ((int) this.mAmLuxOffset);
        if (BrightnessModeState.GameMode == getBrightnessMode()) {
            ambientLuxOffset = (float) ((int) this.mGameModeOffsetLux);
        }
        if (this.mBrightnessOffsetLuxModeEnable && ambientLuxOffset >= 0.0f && Math.abs(this.mPosBrightness) > 1.0E-7f) {
            float darkenOffsetDelta = 0.0f;
            if (ambientLuxOffset < this.mDarkenOffsetLuxTh1) {
                noValidBrightenLuxTh = this.mDarkenOffsetNoValidBrightenLuxTh1;
            } else if (ambientLuxOffset < this.mDarkenOffsetLuxTh2) {
                noValidBrightenLuxTh = this.mDarkenOffsetNoValidBrightenLuxTh2;
            } else if (ambientLuxOffset < this.mDarkenOffsetLuxTh3) {
                noValidBrightenLuxTh = this.mDarkenOffsetNoValidBrightenLuxTh3;
            } else {
                noValidBrightenLuxTh = this.mDarkenOffsetNoValidBrightenLuxTh4;
            }
            if (noValidBrightenLuxTh >= 0.0f) {
                darkenOffsetDelta = getCurrentBrightness(noValidBrightenLuxTh) - maxBrightness;
            }
            if ((maxBrightness - brightnessStartOrig) + darkenOffsetDelta < 1.0f) {
                brightenRatio = 1.0f;
            } else {
                brightenRatio = ((maxBrightness - brightnessEndOrig) + darkenOffsetDelta) / (((maxBrightness - brightnessStartOrig) + darkenOffsetDelta) + 1.0E-7f);
            }
            if (brightenRatio < 0.0f || brightenRatio > 1.0f) {
                brightenRatio = 0.0f;
            }
            if (((int) this.mLastBrightnessEndOrigIn) != ((int) brightnessEndOrig)) {
                this.mLastBrightnessEndOrigIn = brightnessEndOrig;
                if (DEBUG) {
                    Slog.i(TAG, "DarkenOffset origRatio=" + ratio + "-->brightenRatio=" + brightenRatio + ",mode=" + getBrightnessMode() + ",noValidBrightenLuxTh=" + noValidBrightenLuxTh + ",DarkenOffsetBrightnessMin=" + (maxBrightness + darkenOffsetDelta) + ",ambientLuxOffset=" + ambientLuxOffset);
                }
            }
        }
        return brightenRatio;
    }

    private float getBrightenOffsetDarkenRatio(float ratio, float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        float noValidBrightenLuxTh;
        float darkenRatio = ratio;
        float ambientLuxOffset = (float) ((int) this.mAmLuxOffset);
        if (BrightnessModeState.GameMode == getBrightnessMode()) {
            ambientLuxOffset = (float) ((int) this.mGameModeOffsetLux);
        }
        if (this.mBrightnessOffsetLuxModeEnable && ambientLuxOffset >= 0.0f && Math.abs(this.mPosBrightness) > 1.0E-7f) {
            float brightenOffsetBrightnessMin = 0.0f;
            if (ambientLuxOffset < this.mBrightenOffsetLuxTh1) {
                noValidBrightenLuxTh = this.mBrightenOffsetNoValidDarkenLuxTh1;
            } else if (ambientLuxOffset < this.mBrightenOffsetLuxTh2) {
                noValidBrightenLuxTh = this.mBrightenOffsetNoValidDarkenLuxTh2;
            } else if (ambientLuxOffset < this.mBrightenOffsetLuxTh3) {
                noValidBrightenLuxTh = this.mBrightenOffsetNoValidDarkenLuxTh3;
            } else {
                noValidBrightenLuxTh = this.mBrightenOffsetNoValidDarkenLuxTh4;
            }
            if (noValidBrightenLuxTh > 0.0f || (noValidBrightenLuxTh == 0.0f && !this.mBrightenOffsetEffectMinLuxEnable)) {
                brightenOffsetBrightnessMin = getCurrentBrightness(noValidBrightenLuxTh);
            }
            float darkenRatio2 = (brightnessEndOrig - brightenOffsetBrightnessMin) / (brightnessStartOrig + 1.0E-7f);
            if (darkenRatio2 < 0.0f || darkenRatio2 > 1.0f) {
                darkenRatio = 0.0f;
            } else {
                darkenRatio = darkenRatio2;
            }
            if (((int) this.mLastBrightnessEndOrigIn) != ((int) brightnessEndOrig)) {
                this.mLastBrightnessEndOrigIn = brightnessEndOrig;
                if (DEBUG) {
                    Slog.i(TAG, "BrightenOffset OrigRatio=" + ratio + "-->darkenRatio=" + darkenRatio + ",mode=" + getBrightnessMode() + ",noValidBrightenLuxTh=" + noValidBrightenLuxTh + ",BrightenOffsetBrightnessMin=" + brightenOffsetBrightnessMin + ",mEffectMinLuxEnable=" + this.mBrightenOffsetEffectMinLuxEnable + ",ambientLuxOffset=" + ambientLuxOffset);
                }
            }
        }
        return darkenRatio;
    }

    private void checkErrorCorrectionOffset() {
        this.mErrorCorrectionOffsetNeedClear = false;
        if (this.mBrightnessOffsetTmpValidEnable) {
            float ambientLuxOffset = (float) ((int) this.mAmLuxOffset);
            if (Math.abs(this.mPosBrightness) > 1.0E-7f && this.mDelta > 0.0f && ambientLuxOffset >= this.mBrightenOffsetNoValidSavedLuxTh1 && ambientLuxOffset < this.mBrightenOffsetNoValidSavedLuxTh2) {
                this.mErrorCorrectionOffsetNeedClear = true;
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel ErrorCorrectOffset, OffsetNeedClear=" + this.mErrorCorrectionOffsetNeedClear);
                }
            }
        }
    }
}
