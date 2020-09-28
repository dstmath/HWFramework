package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import com.huawei.displayengine.DisplayEngineDbManager;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.sidetouch.TpCommandConstant;
import huawei.android.utils.HwEyeProtectionSpline;
import huawei.android.utils.HwEyeProtectionSplineImpl;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwNormalizedSpline extends Spline {
    private static final float BRIGHTNESS_WITHDELTA_MAX = 230.0f;
    private static boolean DEBUG = false;
    private static final float DEFAULT_CALIBRATION_RATIO = 1.0f;
    private static final float DEFAULT_MIN_DELTA = 1.0f;
    private static final float DEFAULT_RATIO = 1.0f;
    private static final float DEFAULT_TWO_POINT_OFFSET_BRIGHTNESS = 0.0f;
    private static final float DEFAULT_TWO_POINT_OFFSET_LUX = -1.0f;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final String TAG = "HwNormalizedSpline";
    private static final String TOUCH_OEM_INFO_PATH = "/sys/touchscreen/touch_oem_info";
    private static final int VALID_BRIGHTNESS_MIN_NIT = 380;
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    private static final Object mLock = new Object();
    private static final float maxBrightness = 255.0f;
    private static final float minBrightness = 4.0f;
    private float mAmLux;
    private float mAmLuxOffset;
    private float mAmLuxOffsetHigh;
    private float mAmLuxOffsetHighSaved;
    private float mAmLuxOffsetLow;
    private float mAmLuxOffsetLowHighDelta;
    private float mAmLuxOffsetLowSaved;
    private float mAmLuxOffsetSaved;
    private float mAmLuxOffsetTh;
    private float mAmLuxOffsetTmp;
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
    List<PointF> mBrightnessCurveDefault;
    List<PointF> mBrightnessCurveDefaultTmp;
    List<PointF> mBrightnessCurveHigh;
    List<PointF> mBrightnessCurveHighTmp;
    List<PointF> mBrightnessCurveLow;
    List<PointF> mBrightnessCurveLowTmp;
    List<PointF> mBrightnessCurveMiddle;
    List<PointF> mBrightnessCurveMiddleTmp;
    private float mBrightnessForLog;
    private boolean mBrightnessOffsetLuxModeEnable;
    private boolean mBrightnessOffsetTmpValidEnable;
    private float mCalibrationRatio;
    private boolean mCalibrtionModeBeforeEnable;
    private int mCalibrtionTest;
    List<PointF> mCameraBrighnessLinePointsList;
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
    private List<PointF> mDarkAdaptedBrightnessPointsList;
    private int mDarkAdaptingBrightness0LuxLevel;
    private List<PointF> mDarkAdaptingBrightnessPointsList;
    private boolean mDarkModeBrightnessEnable;
    private int mDarkModeMinOffsetBrightness;
    private float mDarkenOffsetLuxTh1;
    private float mDarkenOffsetLuxTh2;
    private float mDarkenOffsetLuxTh3;
    private float mDarkenOffsetNoValidBrightenLuxTh1;
    private float mDarkenOffsetNoValidBrightenLuxTh2;
    private float mDarkenOffsetNoValidBrightenLuxTh3;
    private float mDarkenOffsetNoValidBrightenLuxTh4;
    List<PointF> mDayBrighnessLinePointsList;
    private boolean mDayModeAlgoEnable;
    private boolean mDayModeEnable;
    private float mDayModeMinimumBrightness;
    private int mDayModeModifyMinBrightness;
    private int mDayModeModifyNumPoint;
    List<PointF> mDefaultBrighnessLinePointsList;
    List<PointF> mDefaultBrighnessLinePointsListCaliBefore;
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
    List<PointF> mGameModeBrightnessLinePointsList;
    private boolean mGameModeEnable;
    private float mGameModeOffsetLux;
    private float mGameModePosBrightness;
    private float mGameModeStartLuxDefaultBrightness;
    private float mHighBrightenOffsetNoValidBrightenLuxTh;
    private float mHighBrightenOffsetNoValidDarkenLuxTh;
    private float mHighDarkenOffsetNoValidBrightenLuxTh;
    private float mHighDarkenOffsetNoValidDarkenLuxTh;
    private boolean mIsReboot;
    private volatile boolean mIsReset;
    private boolean mIsUserChange;
    private boolean mIsUserChangeSaved;
    private float mLastBrightnessEndOrig;
    private float mLastBrightnessEndOrigIn;
    private float mLastGameModeBrightness;
    private float mLastLuxDefaultBrightness;
    private float mLastLuxDefaultBrightnessSaved;
    private float mLastLuxForTwoPointOffset;
    private float mLowBrightenOffsetNoValidBrightenLuxTh;
    private float mLowBrightenOffsetNoValidDarkenLuxTh;
    private float mLowDarkenOffsetDarkenBrightnessRatio;
    private float mLowDarkenOffsetNoValidBrightenLuxTh;
    private float mLowDarkenOffsetNoValidDarkenLuxTh;
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
    List<PointF> mOminLevelBrighnessLinePointsList;
    private int mOminLevelCount;
    private boolean mOminLevelCountEnable;
    List<PointF> mOminLevelCountLevelPointsList;
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
    private float mPosBrightnessHigh;
    private float mPosBrightnessHighSaved;
    private float mPosBrightnessLow;
    private float mPosBrightnessLowSaved;
    private float mPosBrightnessSaved;
    private float mPosBrightnessTmp;
    private boolean mPowerOnEanble;
    private float mPowerSavingAmluxThreshold;
    private boolean mPowerSavingBrighnessLineEnable;
    List<PointF> mPowerSavingBrighnessLinePointsList;
    private boolean mPowerSavingModeEnable;
    List<PointF> mReadingBrighnessLinePointsList;
    private boolean mReadingModeEnable;
    private boolean mRebootNewCurveEnable;
    private int mSceneLevel;
    private float mStartLuxDefaultBrightness;
    private float mStartLuxDefaultBrightnessSaved;
    private boolean mTwoPointOffsetEnable;
    private boolean mTwoPointOffsetLowResetEnable;
    private float mTwoPointOffsetNoValidLuxTh;
    private boolean mUsePowerSavingModeCurveEnable;
    private float mVehicleModeBrighntess;
    private boolean mVehicleModeBrightnessEnable;
    private boolean mVehicleModeClearOffsetEnable;
    private boolean mVehicleModeEnable;
    private boolean mVehicleModeKeepMinBrightnessEnable;
    private float mVehicleModeLuxThreshold;
    public boolean mVehicleModeQuitForPowerOnEnable;
    private boolean mVideoFullScreenModeBrightnessEnable;
    List<PointF> mVideoFullScreenModeBrightnessLinePointsList;
    private boolean mVideoFullScreenModeEnable;

    public enum BrightnessModeState {
        CAMERA_MODE,
        GAME_MODE,
        VIDEO_FULLSCREEN_MODE,
        READING_MODE,
        NEWCURVE_MODE,
        POWERSAVING_MODE,
        EYE_PROTECTION_MODE,
        CALIBRATION_MODE,
        DARKADAPT_MODE,
        OMINLEVEL_MODE,
        DARK_MODE,
        DAY_MODE,
        DEFAULT_MODE
    }

    public enum DarkAdaptState {
        UNADAPTED,
        ADAPTING,
        ADAPTED
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
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
        this.mAmLux = DEFAULT_TWO_POINT_OFFSET_LUX;
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
        this.mAmLuxOffset = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mAmLuxOffsetSaved = DEFAULT_TWO_POINT_OFFSET_LUX;
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
        this.mLastGameModeBrightness = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mDeltaTmp = 0.0f;
        this.mGameModeOffsetLux = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mGameModeBrightnessLinePointsList = new ArrayList();
        this.mGameModeStartLuxDefaultBrightness = DEFAULT_TWO_POINT_OFFSET_LUX;
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
        this.mLastBrightnessEndOrigIn = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mLastBrightnessEndOrig = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mVideoFullScreenModeEnable = false;
        this.mVideoFullScreenModeBrightnessEnable = false;
        this.mVideoFullScreenModeBrightnessLinePointsList = new ArrayList<PointF>() {
            /* class android.util.HwNormalizedSpline.AnonymousClass1 */

            {
                add(new PointF(0.0f, HwNormalizedSpline.minBrightness));
                add(new PointF(25.0f, 46.5f));
                add(new PointF(1995.0f, 140.7f));
                add(new PointF(4000.0f, HwNormalizedSpline.maxBrightness));
                add(new PointF(40000.0f, HwNormalizedSpline.maxBrightness));
            }
        };
        this.mDarkModeBrightnessEnable = false;
        this.mDarkModeMinOffsetBrightness = 4;
        this.mTwoPointOffsetEnable = false;
        this.mAmLuxOffsetTh = 50.0f;
        this.mAmLuxOffsetLowHighDelta = 50.0f;
        this.mTwoPointOffsetNoValidLuxTh = 50.0f;
        this.mAmLuxOffsetLow = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mAmLuxOffsetLowSaved = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mPosBrightnessLow = 0.0f;
        this.mPosBrightnessLowSaved = 0.0f;
        this.mAmLuxOffsetHigh = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mAmLuxOffsetHighSaved = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mPosBrightnessHigh = 0.0f;
        this.mPosBrightnessHighSaved = 0.0f;
        this.mAmLuxOffsetTmp = DEFAULT_TWO_POINT_OFFSET_LUX;
        this.mPosBrightnessTmp = 0.0f;
        this.mLowDarkenOffsetNoValidBrightenLuxTh = 500.0f;
        this.mLowBrightenOffsetNoValidBrightenLuxTh = 500.0f;
        this.mLowBrightenOffsetNoValidDarkenLuxTh = 0.0f;
        this.mLowDarkenOffsetNoValidDarkenLuxTh = 0.0f;
        this.mLowDarkenOffsetDarkenBrightnessRatio = 0.2f;
        this.mHighBrightenOffsetNoValidBrightenLuxTh = 10000.0f;
        this.mHighDarkenOffsetNoValidBrightenLuxTh = 10000.0f;
        this.mHighBrightenOffsetNoValidDarkenLuxTh = 10.0f;
        this.mHighDarkenOffsetNoValidDarkenLuxTh = 10.0f;
        this.mTwoPointOffsetLowResetEnable = false;
        this.mVehicleModeKeepMinBrightnessEnable = false;
        this.mUsePowerSavingModeCurveEnable = false;
        this.mVehicleModeQuitForPowerOnEnable = false;
        this.mBrightnessForLog = DEFAULT_TWO_POINT_OFFSET_LUX;
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
        String xmlPath = String.format("/xml/lcd/%s_%s%s", XML_NAME_NOEXT, "factory", XML_EXT);
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile != null) {
            return xmlFile;
        }
        Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
        return null;
    }

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
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
                return new String(name, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
                return null;
            }
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private String getVersionFromTouchOemInfo() {
        int productMonth;
        int productDay;
        String version = null;
        try {
            File file = new File(String.format("%s", TOUCH_OEM_INFO_PATH));
            if (file.exists()) {
                String touch_oem_info = FileUtils.readTextFile(file, 0, null).trim();
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
        return version;
    }

    private String getVersionFromLCD() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
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
            String panelVersion = null;
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

    private String getLcdIcName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        if (binder == null) {
            Slog.e(TAG, "getLcdIcName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdIcName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 5, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdIcName() getEffect failed! ret=" + ret);
                return null;
            }
            String[] lcdNameAllArray = new String(name, StandardCharsets.UTF_8).trim().split(TpCommandConstant.SEPARATE);
            if (lcdNameAllArray.length < 3) {
                return null;
            }
            int index = lcdNameAllArray[2].indexOf("ICTYPE:");
            Slog.i(TAG, "getLcdIcName() index=" + index + ",LcdIcNameALL=" + lcdNameAllArray[2]);
            if (index != -1) {
                return lcdNameAllArray[2].substring("ICTYPE:".length() + index);
            }
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "getLcdIcName() RemoteException " + e);
            return null;
        }
    }

    private File getNormalXmlFile() {
        String lcdname = getLcdPanelName();
        String lcdversion = getVersionFromTouchOemInfo();
        String lcdversionNew = getVersionFromLCD();
        String screenColor = SystemProperties.get("ro.config.devicecolor");
        Slog.i(TAG, "screenColor=" + screenColor);
        String lcdIcName = getLcdIcName();
        ArrayList<String> xmlPathList = new ArrayList<>();
        if (!(lcdversionNew == null || lcdIcName == null)) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdversionNew, lcdIcName, screenColor));
        }
        if (lcdversion != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdversion, screenColor));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdversion));
        }
        if (lcdversionNew != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdversionNew, screenColor));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdversionNew));
        }
        if (lcdIcName != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdIcName, screenColor));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdname, lcdIcName));
            xmlPathList.add(String.format("/xml/lcd/%s_%s", XML_NAME_NOEXT, lcdIcName));
        }
        xmlPathList.add(String.format("/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdname, screenColor));
        xmlPathList.add(String.format("/xml/lcd/%s_%s", XML_NAME_NOEXT, lcdname));
        xmlPathList.add(String.format("/xml/lcd/%s_%s", XML_NAME_NOEXT, screenColor));
        xmlPathList.add(String.format("/xml/lcd/%s", XML_NAME_NOEXT));
        File xmlFile = null;
        int listsize = xmlPathList.size();
        for (int i = 0; i < listsize; i++) {
            String xmlPath = xmlPathList.get(i);
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath + XML_EXT, 2);
            if (xmlFile != null) {
                File xmlFileForABTest = getABTestXmlFile(xmlPathList.get(i));
                return xmlFileForABTest != null ? xmlFileForABTest : xmlFile;
            }
        }
        Slog.e(TAG, "get failed!");
        return xmlFile;
    }

    private File getABTestXmlFile(String xmlPath) {
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) != 3) {
            return null;
        }
        try {
            String serial = Build.getSerial();
            if (serial == null || serial.isEmpty() || serial.equals("unknown")) {
                Slog.i(TAG, "getABTestXmlFile, get number failed, skip AB test.");
                return null;
            }
            try {
                if ((serial.charAt(serial.length() - 1) & 1) == 1) {
                    Slog.i(TAG, "getABTestXmlFile, using orginal xml.");
                    return null;
                }
                File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath + "_Test" + XML_EXT, 2);
                if (xmlFile == null) {
                    Slog.i(TAG, "getABTestXmlFile, using orginal xml.");
                } else {
                    Slog.i(TAG, "getABTestXmlFile, using B test xml!");
                }
                return xmlFile;
            } catch (IndexOutOfBoundsException e) {
                Slog.i(TAG, "getABTestXmlFile, IndexOutOfBoundsException, skip AB test: " + e);
                return null;
            }
        } catch (SecurityException e2) {
            Slog.w(TAG, "SecurityException in getABTestXmlFile, " + e2);
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b7, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00bc, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00bd, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c0, code lost:
        throw r6;
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
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
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
                getDayBrightnessLinePoints();
                updateNewBrightnessCurve();
                inputStream.close();
                return true;
            }
            inputStream.close();
            return false;
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "getConfig : FileNotFoundException");
        }
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
        } else {
            if (this.mOffsetBrightenRatioLeft > 0.0f) {
                float f = this.mOffsetBrightenAlphaLeft;
                if (f >= 0.0f && ((double) f) <= 1.0d) {
                    float f2 = this.mOffsetBrightenAlphaRight;
                    if (f2 < 0.0f || ((double) f2) > 1.0d) {
                        loadDefaultConfig();
                        Slog.e(TAG, "LoadXML false, mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
                        return false;
                    }
                    float f3 = this.mOffsetDarkenAlphaLeft;
                    if (f3 < 0.0f || ((double) f3) > 1.0d) {
                        loadDefaultConfig();
                        Slog.e(TAG, "LoadXML false, mOffsetDarkenAlphaLeft=" + this.mOffsetDarkenAlphaLeft);
                        return false;
                    }
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
                    List<PointF> list = this.mDarkAdaptingBrightnessPointsList;
                    if (list == null || checkPointsListIsOK(list)) {
                        List<PointF> list2 = this.mDarkAdaptedBrightnessPointsList;
                        if (list2 == null || checkPointsListIsOK(list2)) {
                            float f4 = this.mVehicleModeBrighntess;
                            if (f4 < 0.0f || f4 > maxBrightness || this.mVehicleModeLuxThreshold < 0.0f) {
                                loadDefaultConfig();
                                Slog.e(TAG, "VehicleBrightMode LoadDefaultConfig!,mVehicleModeBrighntess=" + this.mVehicleModeBrighntess + ",mVehicleModeLuxThreshold=" + this.mVehicleModeLuxThreshold);
                                return false;
                            } else if (this.mDayModeMinimumBrightness > maxBrightness) {
                                loadDefaultConfig();
                                Slog.e(TAG, "DayModeMinimumBrightness LoadDefaultConfig!,mDayModeMinimumBrightness=" + this.mDayModeMinimumBrightness);
                                return false;
                            } else if (!DEBUG) {
                                return true;
                            } else {
                                Slog.i(TAG, "checkConfigLoadedFromXML success!");
                                return true;
                            }
                        } else {
                            loadDefaultConfig();
                            Slog.e(TAG, "checkPointsList mDarkAdaptedBrightnessPointsList is wrong, LoadDefaultConfig!");
                            return false;
                        }
                    } else {
                        loadDefaultConfig();
                        Slog.e(TAG, "checkPointsList mDarkAdaptingBrightnessPointsList is wrong, LoadDefaultConfig!");
                        return false;
                    }
                }
            }
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
            return false;
        }
    }

    private void initLinePointsList() {
        int listSize = this.mDefaultBrighnessLinePointsList.size();
        for (int i = 0; i < listSize; i++) {
            PointF tempPoint = new PointF();
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

    private void brightnessCalibration(List<PointF> LinePointsList, int actulBrightnessNit, int standardBrightnessNit) {
        if (actulBrightnessNit < VALID_BRIGHTNESS_MIN_NIT || actulBrightnessNit > 1000 || standardBrightnessNit > 1000 || standardBrightnessNit <= 0) {
            this.mCalibrationRatio = 1.0f;
            Slog.e(TAG, "error input brightnessNit:mStandardBrightnessNit=" + standardBrightnessNit + ",mActulBrightnessNit=" + actulBrightnessNit);
        } else {
            this.mCalibrationRatio = ((float) standardBrightnessNit) / ((float) actulBrightnessNit);
            if (DEBUG) {
                Slog.i(TAG, "mCalibrationRatio=" + this.mCalibrationRatio + ",mStandardBrightnessNit=" + standardBrightnessNit + ",mActulBrightnessNit=" + actulBrightnessNit);
            }
        }
        int listSize = LinePointsList.size();
        for (int i = 1; i < listSize; i++) {
            PointF pointTemp = LinePointsList.get(i);
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
        for (PointF temp : LinePointsList) {
            if (DEBUG) {
                Slog.i(TAG, "LoadXMLConfig_NewCalibrationBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
            }
        }
    }

    private void updateLinePointsListForCalibration() {
        List<PointF> list;
        List<PointF> list2;
        List<PointF> list3;
        if (this.mBrightnessCalibrationEnabled && Math.abs(this.mCalibrationRatio - 1.0f) > 1.0E-7f) {
            if (this.mPowerSavingBrighnessLineEnable && (list2 = this.mPowerSavingBrighnessLinePointsList) != null) {
                updateNewLinePointsListForCalibration(list2);
                Slog.i(TAG, "update PowerSavingBrighnessLinePointsList for calibration");
                if (DEBUG && (list3 = this.mPowerSavingBrighnessLinePointsList) != null) {
                    for (PointF temp : list3) {
                        Slog.i(TAG, "LoadXMLConfig_NewCalibrationPowerSavingPointsList x = " + temp.x + ", y = " + temp.y);
                    }
                }
            }
            List<PointF> list4 = this.mCameraBrighnessLinePointsList;
            if (list4 != null) {
                updateNewLinePointsListForCalibration(list4);
                if (DEBUG) {
                    Slog.i(TAG, "update mCameraBrighnessLinePointsList for calibration");
                }
            }
            List<PointF> list5 = this.mGameModeBrightnessLinePointsList;
            if (list5 != null) {
                updateNewLinePointsListForCalibration(list5);
                if (DEBUG) {
                    Slog.i(TAG, "update mGameModeBrightnessLinePointsList for calibration");
                }
                if (DEBUG && (list = this.mGameModeBrightnessLinePointsList) != null) {
                    for (PointF temp2 : list) {
                        Slog.i(TAG, "LoadXMLConfig_GameModeBrightnessLinePointsList x = " + temp2.x + ", y = " + temp2.y);
                    }
                }
            }
            List<PointF> list6 = this.mReadingBrighnessLinePointsList;
            if (list6 != null) {
                updateNewLinePointsListForCalibration(list6);
                if (DEBUG) {
                    Slog.i(TAG, "update mReadingBrighnessLinePointsList for calibration");
                    for (PointF temp3 : this.mReadingBrighnessLinePointsList) {
                        Slog.i(TAG, "LoadXMLConfig_ReadingModeBrightnessLinePointsList x = " + temp3.x + ", y = " + temp3.y);
                    }
                }
            }
        }
    }

    private void updateNewLinePointsListForCalibration(List<PointF> LinePointsList) {
        int listSize = LinePointsList.size();
        for (int i = 1; i < listSize; i++) {
            PointF pointTemp = LinePointsList.get(i);
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

    private boolean checkPointsListIsOK(List<PointF> LinePointsList) {
        if (LinePointsList == null) {
            Slog.e(TAG, "LoadXML false for mLinePointsList == null");
            return false;
        } else if (LinePointsList.size() <= 2 || LinePointsList.size() >= 100) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF tmpPoint : LinePointsList) {
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
        int size = this.mDefaultBrighnessLinePointsList.size();
        int i = this.mDayModeModifyNumPoint;
        if (size < i) {
            Slog.e(TAG, "mDefaultBrighnessLinePointsList.size < mDayModeModifyNumPoint");
            return true;
        } else if (this.mDefaultBrighnessLinePointsList.get(i - 1).y >= ((float) this.mDayModeModifyMinBrightness)) {
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
                PointF temp = this.mDefaultBrighnessLinePointsList.get(this.mDayModeModifyNumPoint - 1);
                u = ((temp.y * 1.0f) - (((float) this.mDayModeModifyMinBrightness) * 1.0f)) / ((temp.y * 1.0f) - minBrightness);
                v = (temp.y * ((((float) this.mDayModeModifyMinBrightness) * 1.0f) - minBrightness)) / ((temp.y * 1.0f) - minBrightness);
                Slog.i(TAG, "DayMode:u=" + u + ", v=" + v);
            } else {
                u = 1.0f;
                v = 0.0f;
                Slog.e(TAG, "error DayBrightnessLinePoints input!");
            }
            List<PointF> list = this.mDayBrighnessLinePointsList;
            if (list == null) {
                this.mDayBrighnessLinePointsList = new ArrayList();
            } else {
                list.clear();
            }
            int cntPoint = 0;
            for (PointF temp2 : this.mDefaultBrighnessLinePointsList) {
                cntPoint++;
                if (cntPoint > this.mDayModeModifyNumPoint) {
                    this.mDayBrighnessLinePointsList.add(temp2);
                } else {
                    this.mDayBrighnessLinePointsList.add(new PointF(temp2.x, (temp2.y * u) + v));
                }
            }
            for (PointF temp3 : this.mDayBrighnessLinePointsList) {
                if (DEBUG) {
                    Slog.i(TAG, "DayMode:DayBrightnessLine: =" + temp3.x + ", y=" + temp3.y);
                }
            }
        }
    }

    private void getOminLevelBrighnessLinePoints() {
        List<PointF> list = this.mOminLevelBrighnessLinePointsList;
        if (list == null) {
            this.mOminLevelBrighnessLinePointsList = new ArrayList();
        } else {
            list.clear();
        }
        List<PointF> list2 = this.mDayBrighnessLinePointsList;
        if (list2 != null) {
            for (PointF temp : list2) {
                this.mOminLevelBrighnessLinePointsList.add(temp);
                if (DEBUG) {
                    Slog.i(TAG, "mOminLevelMode:LinePointsList: x=" + temp.x + ", y=" + temp.y);
                }
            }
            updateOminLevelBrighnessLinePoints();
            return;
        }
        Slog.w(TAG, "mOminLevelMode getLineFailed, mDayBrighnessLinePointsList==null");
    }

    public void updateOminLevelBrighnessLinePoints() {
        List<PointF> list = this.mOminLevelBrighnessLinePointsList;
        if (list == null) {
            Slog.w(TAG, "mOminLevelMode mOminLevelBrighnessLinePointsList==null,return");
            return;
        }
        int listsize = list.size();
        int countThMin = getOminLevelCountThMin(this.mOminLevelCountLevelPointsList);
        int countThMax = getOminLevelCountThMax(this.mOminLevelCountLevelPointsList);
        if (listsize >= 2) {
            PointF temp = this.mOminLevelBrighnessLinePointsList.get(0);
            PointF temp1 = this.mOminLevelBrighnessLinePointsList.get(1);
            int i = this.mOminLevelCount;
            if (i >= countThMin) {
                temp.y = getOminLevelFromCount(i);
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
                Slog.i(TAG, "mOminLevelMode updateMinLevel x(0)=" + temp.x + ",y(0)=" + temp.y);
                return;
            }
            return;
        }
        Slog.w(TAG, "mOminLevelMode mOminLevelBrighnessLinePointsList==null");
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

    private float getOminLevelFromCountInternal(List<PointF> linePointsList, float levelCount) {
        float brightnessLevel = minBrightness;
        if (linePointsList == null) {
            Slog.i(TAG, "mOminLevelMode linePointsListIn==null,return minBrightness");
            return minBrightness;
        }
        PointF temp1 = null;
        for (PointF temp : linePointsList) {
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
        List<PointF> list = this.mDefaultBrighnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mDefaultBrighnessLinePointsList = new ArrayList();
        }
        this.mDefaultBrighnessLinePointsList.add(new PointF(0.0f, minBrightness));
        this.mDefaultBrighnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mDefaultBrighnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mDefaultBrighnessLinePointsList.add(new PointF(4000.0f, maxBrightness));
        this.mDefaultBrighnessLinePointsList.add(new PointF(40000.0f, maxBrightness));
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
        List<PointF> list = this.mCameraBrighnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mCameraBrighnessLinePointsList = new ArrayList();
        }
        this.mCameraBrighnessLinePointsList.add(new PointF(0.0f, minBrightness));
        this.mCameraBrighnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mCameraBrighnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mCameraBrighnessLinePointsList.add(new PointF(4000.0f, maxBrightness));
        this.mCameraBrighnessLinePointsList.add(new PointF(40000.0f, maxBrightness));
    }

    private void loadReadingDefaultBrightnessLine() {
        List<PointF> list = this.mReadingBrighnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mReadingBrighnessLinePointsList = new ArrayList();
        }
        this.mReadingBrighnessLinePointsList.add(new PointF(0.0f, minBrightness));
        this.mReadingBrighnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mReadingBrighnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mReadingBrighnessLinePointsList.add(new PointF(4000.0f, maxBrightness));
        this.mReadingBrighnessLinePointsList.add(new PointF(40000.0f, maxBrightness));
    }

    private void loadGameModeDefaultBrightnessLine() {
        List<PointF> list = this.mGameModeBrightnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mGameModeBrightnessLinePointsList = new ArrayList();
        }
        this.mGameModeBrightnessLinePointsList.add(new PointF(0.0f, minBrightness));
        this.mGameModeBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mGameModeBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mGameModeBrightnessLinePointsList.add(new PointF(4000.0f, maxBrightness));
        this.mGameModeBrightnessLinePointsList.add(new PointF(40000.0f, maxBrightness));
    }

    private void loadPowerSavingDefaultBrightnessLine() {
        List<PointF> list = this.mPowerSavingBrighnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mPowerSavingBrighnessLinePointsList = new ArrayList();
        }
        this.mPowerSavingBrighnessLinePointsList.add(new PointF(0.0f, minBrightness));
        this.mPowerSavingBrighnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mPowerSavingBrighnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mPowerSavingBrighnessLinePointsList.add(new PointF(4000.0f, maxBrightness));
        this.mPowerSavingBrighnessLinePointsList.add(new PointF(40000.0f, maxBrightness));
    }

    private void loadOminLevelCountLevelPointsList() {
        List<PointF> list = this.mOminLevelCountLevelPointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mOminLevelCountLevelPointsList = new ArrayList();
        }
        this.mOminLevelCountLevelPointsList.add(new PointF(5.0f, 6.0f));
        this.mOminLevelCountLevelPointsList.add(new PointF(10.0f, 7.0f));
        this.mOminLevelCountLevelPointsList.add(new PointF(20.0f, 8.0f));
    }

    private void printConfigFromXML() {
        List<PointF> list;
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
        for (PointF temp : this.mDefaultBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_DefaultBrighnessLinePoints x = " + temp.x + ", y = " + temp.y);
        }
        for (PointF temp2 : this.mCameraBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_CameraBrighnessLinePointsList x = " + temp2.x + ", y = " + temp2.y);
        }
        for (PointF temp3 : this.mReadingBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_ReadingBrighnessLinePointsList x = " + temp3.x + ", y = " + temp3.y);
        }
        for (PointF temp4 : this.mGameModeBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_mGameModeBrightnessLinePointsList x = " + temp4.x + ", y = " + temp4.y);
        }
        for (PointF temp5 : this.mPowerSavingBrighnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_mPowerSavingBrighnessLinePointsList x = " + temp5.x + ", y = " + temp5.y);
        }
        if (this.mOminLevelModeEnable && (list = this.mOminLevelCountLevelPointsList) != null) {
            for (PointF temp6 : list) {
                Slog.i(TAG, "LoadXMLConfig_mOminLevelCountLevelPointsList x = " + temp6.x + ", y = " + temp6.y);
            }
        }
        if (this.mDarkAdaptEnable) {
            Slog.i(TAG, "LoadXMLConfig_DarkAdaptingBrightness0LuxLevel = " + this.mDarkAdaptingBrightness0LuxLevel + ", Adapted = " + this.mDarkAdaptedBrightness0LuxLevel);
        }
        Slog.i(TAG, "LoadXMLConfig_DayModeMinimumBrightness = " + this.mDayModeMinimumBrightness);
    }

    /* JADX WARNING: Removed duplicated region for block: B:298:0x0813 A[LOOP:0: B:6:0x003a->B:298:0x0813, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x08aa A[EDGE_INSN: B:331:0x08aa->B:312:0x08aa ?: BREAK  , SYNTHETIC] */
    private boolean getConfigFromXML(InputStream inStream) {
        String name;
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean CameraBrightnessLinePointsListsLoadStarted = false;
        boolean CameraBrightnessLinePointsListLoaded = false;
        String name2 = null;
        boolean ReadingBrightnessLinePointsListLoaded = false;
        boolean GameModeBrightnessLinePointsListsLoadStarted = false;
        boolean GameModeBrightnessLinePointsListLoaded = false;
        boolean PowerSavingBrightnessLinePointsListsLoadStarted = false;
        boolean PowerSavingBrightnessLinePointsListLoaded = false;
        boolean OminLevelCountLevelLinePointsListsLoadStarted = false;
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        boolean DefaultBrightnessLoaded = false;
        XmlPullParser parser = Xml.newPullParser();
        boolean DefaultBrighnessLinePointsListsLoadStarted = false;
        boolean DefaultBrighnessLinePointsListLoaded = false;
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType == 1) {
                    break;
                }
                if (eventType != 2) {
                    if (eventType != 3) {
                        CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                        ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                        GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                        PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                        OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                        PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                        GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                        name2 = name2;
                    } else {
                        try {
                            name = parser.getName();
                            try {
                                if (name.equals(XML_NAME_NOEXT) && configGroupLoadStarted) {
                                    loadFinished = true;
                                    CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                    ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                    GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                    PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                    OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                    PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                    GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                    name2 = name2;
                                } else if (configGroupLoadStarted) {
                                    if (name.equals("DefaultBrightnessPoints")) {
                                        try {
                                            if (this.mDefaultBrighnessLinePointsList != null) {
                                                DefaultBrighnessLinePointsListsLoadStarted = false;
                                                DefaultBrighnessLinePointsListLoaded = true;
                                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no DefaultBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e) {
                                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e2) {
                                            Slog.e(TAG, "getConfigFromXML : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e3) {
                                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if (name.equals("CameraBrightnessPoints")) {
                                        CameraBrightnessLinePointsListsLoadStarted = false;
                                        if (this.mCameraBrighnessLinePointsList != null) {
                                            CameraBrightnessLinePointsListLoaded = true;
                                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                            name2 = name2;
                                        } else {
                                            Slog.e(TAG, "no CameraBrightnessPoints loaded!");
                                            return false;
                                        }
                                    } else if (name.equals("ReadingBrightnessPoints")) {
                                        try {
                                            if (this.mReadingBrighnessLinePointsList != null) {
                                                ReadingBrightnessLinePointsListLoaded = true;
                                                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = null;
                                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                            } else {
                                                Slog.e(TAG, "no ReadingBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e4) {
                                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e5) {
                                            Slog.e(TAG, "getConfigFromXML : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e6) {
                                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if (name.equals("GameModeBrightnessPoints")) {
                                        GameModeBrightnessLinePointsListsLoadStarted = false;
                                        try {
                                            if (this.mGameModeBrightnessLinePointsList != null) {
                                                GameModeBrightnessLinePointsListLoaded = true;
                                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no GameModeBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e7) {
                                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e8) {
                                            Slog.e(TAG, "getConfigFromXML : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e9) {
                                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if (name.equals("PowerSavingBrightnessPoints")) {
                                        PowerSavingBrightnessLinePointsListsLoadStarted = false;
                                        try {
                                            if (this.mPowerSavingBrighnessLinePointsList != null) {
                                                PowerSavingBrightnessLinePointsListLoaded = true;
                                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no PowerSavingBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e10) {
                                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e11) {
                                            Slog.e(TAG, "getConfigFromXML : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e12) {
                                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if (name.equals("OminLevelCountLevelLinePoints")) {
                                        OminLevelCountLevelLinePointsListsLoadStarted = false;
                                        try {
                                            if (this.mOminLevelCountLevelPointsList != null) {
                                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no OminLevelCountLevelPointsList loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e13) {
                                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e14) {
                                            Slog.e(TAG, "getConfigFromXML : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e15) {
                                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    }
                                }
                            } catch (XmlPullParserException e16) {
                                Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (IOException e17) {
                                Slog.e(TAG, "getConfigFromXML : IOException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (NumberFormatException e18) {
                                Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            }
                        } catch (XmlPullParserException e19) {
                            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        } catch (IOException e20) {
                            Slog.e(TAG, "getConfigFromXML : IOException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        } catch (NumberFormatException e21) {
                            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        }
                    }
                    if (loadFinished) {
                        break;
                    }
                    eventType = parser.next();
                } else {
                    name = parser.getName();
                    if (name.equals(XML_NAME_NOEXT)) {
                        if (this.mDeviceActualBrightnessLevel == 0) {
                            if (DEBUG) {
                                Slog.i(TAG, "actualDeviceLevel = 0, load started");
                            }
                            configGroupLoadStarted = true;
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else {
                            String deviceLevelString = parser.getAttributeValue(null, "level");
                            if (deviceLevelString == null || deviceLevelString.length() == 0) {
                                if (DEBUG) {
                                    Slog.i(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                                }
                                configGroupLoadStarted = true;
                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                name2 = name2;
                            } else {
                                if (Integer.parseInt(deviceLevelString) == this.mDeviceActualBrightnessLevel) {
                                    if (DEBUG) {
                                        Slog.i(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", find matched level in XML, load start");
                                    }
                                    configGroupLoadStarted = true;
                                }
                                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                                name2 = name2;
                            }
                        }
                    } else if (configGroupLoadStarted) {
                        if (name.equals("DefaultBrightness")) {
                            this.mDefaultBrightness = Float.parseFloat(parser.nextText());
                            DefaultBrightnessLoaded = true;
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
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
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if (name.equals("Point") && DefaultBrighnessLinePointsListsLoadStarted) {
                            PointF currentPoint = new PointF();
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
                            PointF currentPoint2 = new PointF();
                            String s2 = parser.nextText();
                            currentPoint2.x = Float.parseFloat(s2.split(",")[0]);
                            currentPoint2.y = Float.parseFloat(s2.split(",")[1]);
                            if (this.mCameraBrighnessLinePointsList == null) {
                                this.mCameraBrighnessLinePointsList = new ArrayList();
                            }
                            this.mCameraBrighnessLinePointsList.add(currentPoint2);
                        } else if (name.equals("ReadingBrightnessPoints")) {
                            if (this.mReadingBrighnessLinePointsList != null) {
                                this.mReadingBrighnessLinePointsList.clear();
                            }
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                            name2 = 1;
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                        } else if (name.equals("Point") && name2 != null) {
                            PointF currentPoint3 = new PointF();
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
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if (name.equals("Point") && GameModeBrightnessLinePointsListsLoadStarted) {
                            PointF currentPoint4 = new PointF();
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
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if (name.equals("Point") && PowerSavingBrightnessLinePointsListsLoadStarted) {
                            PointF currentPoint5 = new PointF();
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
                            CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                            ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                            GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                            PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                            GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if (name.equals("Point") && OminLevelCountLevelLinePointsListsLoadStarted) {
                            PointF currentPoint6 = new PointF();
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
                    if (loadFinished) {
                    }
                }
                CameraBrightnessLinePointsListLoaded = CameraBrightnessLinePointsListLoaded;
                ReadingBrightnessLinePointsListLoaded = ReadingBrightnessLinePointsListLoaded;
                GameModeBrightnessLinePointsListLoaded = GameModeBrightnessLinePointsListLoaded;
                PowerSavingBrightnessLinePointsListLoaded = PowerSavingBrightnessLinePointsListLoaded;
                OminLevelCountLevelLinePointsListsLoadStarted = OminLevelCountLevelLinePointsListsLoadStarted;
                PowerSavingBrightnessLinePointsListsLoadStarted = PowerSavingBrightnessLinePointsListsLoadStarted;
                GameModeBrightnessLinePointsListsLoadStarted = GameModeBrightnessLinePointsListsLoadStarted;
                name2 = name2;
                if (loadFinished) {
                }
            }
            if (!DefaultBrightnessLoaded || !DefaultBrighnessLinePointsListLoaded) {
                if (!configGroupLoadStarted) {
                    Slog.e(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                    return false;
                }
                Slog.e(TAG, "getConfigFromeXML false!");
                return false;
            } else if (!DEBUG) {
                return true;
            } else {
                Slog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e22) {
            Slog.e(TAG, "getConfigFromXML : XmlPullParserException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        } catch (IOException e23) {
            Slog.e(TAG, "getConfigFromXML : IOException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        } catch (NumberFormatException e24) {
            Slog.e(TAG, "getConfigFromXML : NumberFormatException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        }
    }

    public void updateCurrentUserId(int userId) {
        if (DEBUG) {
            Slog.i(TAG, "save old user's paras and load new user's paras when user change ");
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
        this.mAmLuxOffset = Settings.System.getFloatForUser(this.mContentResolver, "spline_ambient_lux_offset", DEFAULT_TWO_POINT_OFFSET_LUX, this.mCurrentUserId);
        if (this.mOminLevelModeEnable) {
            this.mOminLevelCountSaved = Settings.System.getIntForUser(this.mContentResolver, "spline_ominlevel_count", 0, this.mCurrentUserId);
            this.mOminLevelCount = this.mOminLevelCountSaved;
            this.mOminLevelCountResetLongSetTimeSaved = Settings.System.getIntForUser(this.mContentResolver, "spline_ominlevel_time", 0, this.mCurrentUserId);
            this.mOminLevelCountResetLongSetTime = this.mOminLevelCountResetLongSetTimeSaved;
            Slog.i(TAG, "mOminLevelMode read mOminLevelCount=" + this.mOminLevelCount + ",mOminLevelCountResetLongSetTime=" + this.mOminLevelCountResetLongSetTime);
        }
        if (this.mManualMode) {
            float f = this.mStartLuxDefaultBrightness;
            int i = this.mManualBrightnessMaxLimit;
            if (f >= ((float) i) && this.mPosBrightness == ((float) i)) {
                this.mDelta = 0.0f;
                this.mDeltaNew = 0.0f;
                Slog.i(TAG, "updateLevel outdoor no offset set mDelta=0");
            }
        }
        loadTwoPointOffsetParameters();
        if (DEBUG) {
            Slog.i(TAG, "Read:userId=" + this.mCurrentUserId + ",mPosBrightness=" + this.mPosBrightness + ",mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mDelta=" + this.mDelta + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset);
        }
    }

    private void saveOffsetParas() {
        float f = this.mPosBrightness;
        if (((int) (f * 10.0f)) != ((int) (this.mPosBrightnessSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", f / maxBrightness, this.mCurrentUserId);
            this.mPosBrightnessSaved = this.mPosBrightness;
        }
        float f2 = this.mDeltaNew;
        if (((int) (f2 * 10.0f)) != ((int) (this.mDeltaSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_delta", f2, this.mCurrentUserId);
            this.mDeltaSaved = this.mDeltaNew;
        }
        boolean z = this.mIsUserChange;
        if (z != this.mIsUserChangeSaved) {
            ContentResolver contentResolver = this.mContentResolver;
            int i = 1;
            if (!z) {
                i = 0;
            }
            Settings.System.putIntForUser(contentResolver, "spline_is_user_change", i, this.mCurrentUserId);
            this.mIsUserChangeSaved = this.mIsUserChange;
        }
        float f3 = this.mOffsetBrightness_last;
        if (((int) (f3 * 10.0f)) != ((int) (this.mOffsetBrightness_lastSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_offset_brightness_last", f3, this.mCurrentUserId);
            this.mOffsetBrightness_lastSaved = this.mOffsetBrightness_last;
        }
        float f4 = this.mLastLuxDefaultBrightness;
        if (((int) (f4 * 10.0f)) != ((int) (this.mLastLuxDefaultBrightnessSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", f4, this.mCurrentUserId);
            this.mLastLuxDefaultBrightnessSaved = this.mLastLuxDefaultBrightness;
        }
        float f5 = this.mStartLuxDefaultBrightness;
        if (((int) (f5 * 10.0f)) != ((int) (this.mStartLuxDefaultBrightnessSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", f5, this.mCurrentUserId);
            this.mStartLuxDefaultBrightnessSaved = this.mStartLuxDefaultBrightness;
        }
        float f6 = this.mAmLux;
        if (((int) (f6 * 10.0f)) != ((int) (this.mAmLuxSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_ambient_lux", f6, this.mCurrentUserId);
            this.mAmLuxSaved = this.mAmLux;
        }
        float f7 = this.mAmLuxOffset;
        if (((int) (f7 * 10.0f)) != ((int) (this.mAmLuxOffsetSaved * 10.0f))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_ambient_lux_offset", f7, this.mCurrentUserId);
            this.mAmLuxOffsetSaved = this.mAmLuxOffset;
        }
        int i2 = this.mOminLevelCount;
        if (i2 != this.mOminLevelCountSaved) {
            Settings.System.putIntForUser(this.mContentResolver, "spline_ominlevel_count", i2, this.mCurrentUserId);
            this.mOminLevelCountSaved = this.mOminLevelCount;
            Slog.i(TAG, "mOminLevelMode saved mOminLevelCount=" + this.mOminLevelCount);
        }
        int i3 = this.mOminLevelCountResetLongSetTime;
        if (i3 != this.mOminLevelCountResetLongSetTimeSaved) {
            Settings.System.putIntForUser(this.mContentResolver, "spline_ominlevel_time", i3, this.mCurrentUserId);
            this.mOminLevelCountResetLongSetTimeSaved = this.mOminLevelCountResetLongSetTime;
            Slog.i(TAG, "mOminLevelMode saved mOminLevelCountResetLongSetTime=" + this.mOminLevelCountResetLongSetTime);
        }
        saveTwoPointOffsetParameters();
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

    /* JADX WARNING: Removed duplicated region for block: B:72:0x016c  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0181  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01ac  */
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
            if (!this.mDayModeAlgoEnable || !this.mDayModeEnable || getBrightnessMode() != BrightnessModeState.NEWCURVE_MODE) {
                this.mStartLuxDefaultBrightness = defaultBrightness;
            } else {
                float oldBrightness = this.mStartLuxDefaultBrightness;
                float f = this.mDayModeMinimumBrightness;
                if (defaultBrightness > f) {
                    f = defaultBrightness;
                }
                this.mStartLuxDefaultBrightness = f;
                if (DEBUG && oldBrightness != this.mStartLuxDefaultBrightness) {
                    Slog.i(TAG, "updateLevel DayMode: defaultBrightness =" + defaultBrightness + ", mDayModeMinimumBrightness =" + this.mDayModeMinimumBrightness);
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
                Slog.i(TAG, "updateLevel DarkAdapt: mDefaultBrightness = " + this.mStartLuxDefaultBrightness + ", locked = " + this.mDarkAdaptLineLocked);
            }
        } else if (!this.mOminLevelCountEnable || !this.mOminLevelModeEnable) {
            if (!this.mDayModeAlgoEnable || !this.mDayModeEnable) {
                this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
            } else {
                this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDayBrighnessLinePointsList, lux);
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel DayMode: mDefaultBrightnessFromLux =" + this.mStartLuxDefaultBrightness);
                }
            }
        } else if ((!this.mDayModeAlgoEnable || !this.mDayModeEnable) && !this.mOminLevelDayModeEnable) {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
        } else {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mOminLevelBrighnessLinePointsList, lux);
            if (DEBUG) {
                Slog.i(TAG, "updateLevel mOminLevelMode:mDefaultBrightness=" + this.mStartLuxDefaultBrightness);
            }
        }
        this.mPosBrightness = PosBrightness;
        if (this.mManualMode) {
            float f2 = this.mStartLuxDefaultBrightness;
            int i = this.mManualBrightnessMaxLimit;
            if (f2 >= ((float) i) && this.mPosBrightness == ((float) i)) {
                this.mAmLuxOffset = DEFAULT_TWO_POINT_OFFSET_LUX;
                this.mDelta = 0.0f;
                this.mDeltaNew = 0.0f;
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel outdoor no offset mDelta=0");
                }
                if (this.mTwoPointOffsetEnable) {
                    clearHighOffset();
                }
                if (this.mPosBrightness == 0.0f) {
                    this.mAmLuxOffset = DEFAULT_TWO_POINT_OFFSET_LUX;
                    this.mDelta = 0.0f;
                    this.mDeltaNew = 0.0f;
                    this.mOffsetBrightness_last = 0.0f;
                    this.mLastLuxDefaultBrightness = 0.0f;
                    this.mStartLuxDefaultBrightness = 0.0f;
                    this.mDarkAdaptLineLocked = false;
                    clearGameOffsetDelta();
                }
                if (!this.mOminLevelModeEnable) {
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
                updateTwoPointOffset(this.mPosBrightness, this.mAmLuxOffset);
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel:mDelta=" + this.mDelta + ",mDeltaNew=" + this.mDeltaNew + ",mPosBrightness=" + this.mPosBrightness + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",lux =" + lux);
                }
                saveOffsetParas();
            }
        }
        float f3 = this.mPosBrightness;
        float f4 = this.mStartLuxDefaultBrightness;
        this.mDelta = f3 - f4;
        this.mDeltaNew = f3 - f4;
        if (this.mPosBrightness == 0.0f) {
        }
        if (!this.mOminLevelModeEnable) {
        }
        this.mVehicleModeClearOffsetEnable = true;
        Slog.i(TAG, "VehicleBrightMode updateLevel in VehicleBrightnessMode");
        checkErrorCorrectionOffset();
        updateTwoPointOffset(this.mPosBrightness, this.mAmLuxOffset);
        if (DEBUG) {
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
            Slog.i(TAG, "GameBrightMode updateLevelTmp:mDeltaTmp=" + this.mDeltaTmp + ",mGameModePosBrightness=" + this.mGameModePosBrightness + ",mGameModeStartLuxDefaultBrightness=" + this.mGameModeStartLuxDefaultBrightness + ",lux=" + lux);
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
                Slog.i(TAG, "GameBrightMode updateLevelTmp clearGameOffsetDelta,mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux);
            }
            this.mDeltaTmp = 0.0f;
            this.mGameModeOffsetLux = DEFAULT_TWO_POINT_OFFSET_LUX;
            this.mGameModePosBrightness = 0.0f;
        }
    }

    private boolean isUpdateOminLevelValid() {
        BrightnessModeState currentMode = getBrightnessMode();
        if (currentMode == BrightnessModeState.GAME_MODE || currentMode == BrightnessModeState.VIDEO_FULLSCREEN_MODE || currentMode == BrightnessModeState.CAMERA_MODE) {
            return false;
        }
        if (!this.mVehicleModeEnable || !this.mVehicleModeBrightnessEnable) {
            return true;
        }
        return false;
    }

    public void updateOminLevelCount(float lux) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        if (isUpdateOminLevelValid()) {
            int currentMinuteTime = (int) (System.currentTimeMillis() / 60000);
            int deltaMinuteTime = currentMinuteTime - this.mOminLevelCountResetLongSetTime;
            if (deltaMinuteTime >= this.mOminLevelCountResetLongTimeTh || deltaMinuteTime < 0) {
                this.mOminLevelCount = resetOminLevelCount(this.mOminLevelCountLevelPointsList, (float) this.mOminLevelCount);
                this.mOminLevelCountResetLongSetTime = currentMinuteTime;
                if (DEBUG) {
                    Slog.i(TAG, "mOminLevelMode reset mOminLevelCount=" + this.mOminLevelCount + ",deltaMinuteTime=" + deltaMinuteTime + ",currenTime=" + currentMinuteTime);
                }
            }
            if (lux >= 0.0f && lux <= ((float) this.mOminLevelCountValidLuxTh)) {
                long currentTime = SystemClock.uptimeMillis();
                float mBrightenDefaultBrightness = getDefaultBrightnessLevelNew(this.mOminLevelBrighnessLinePointsList, lux);
                long deltaTime = currentTime - this.mOminLevelCountSetTime;
                if (deltaTime / 1000 >= ((long) this.mOminLevelCountValidTimeTh)) {
                    if (DEBUG) {
                        Slog.i(TAG, "mOminLevelMode deltaTime=" + deltaTime + ",ValidTime");
                    }
                    if (Math.abs(this.mPosBrightness) < 1.0E-7f) {
                        int i6 = this.mOminLevelCount;
                        if (i6 > 0 && this.mOminLevelOffsetCountEnable) {
                            this.mOminLevelCount = i6 - 1;
                            this.mOminLevelValidCount = 0;
                            this.mOminLevelCountSetTime = currentTime;
                            Slog.i(TAG, "mOminLevelMode resetoffset-- count=" + this.mOminLevelCount);
                        }
                    } else {
                        float f = this.mPosBrightness;
                        if (f - mBrightenDefaultBrightness > 0.0f) {
                            if (this.mOminLevelCount < getOminLevelCountThMax(this.mOminLevelCountLevelPointsList)) {
                                this.mOminLevelCount++;
                                this.mOminLevelValidCount = 1;
                                this.mOminLevelCountSetTime = currentTime;
                                Slog.i(TAG, "mOminLevelMode brighten++ count=" + this.mOminLevelCount);
                            }
                        } else if (f - mBrightenDefaultBrightness < 0.0f && (i5 = this.mOminLevelCount) > 0) {
                            this.mOminLevelCount = i5 - 1;
                            this.mOminLevelValidCount = -1;
                            this.mOminLevelCountSetTime = currentTime;
                            Slog.i(TAG, "mOminLevelMode darken-- count=" + this.mOminLevelCount);
                        }
                    }
                } else {
                    if (DEBUG) {
                        Slog.i(TAG, "mOminLevelMode deltaTime=" + deltaTime);
                    }
                    if (Math.abs(this.mPosBrightness) < 1.0E-7f) {
                        int i7 = this.mOminLevelCount;
                        if (i7 > 0 && (i4 = this.mOminLevelValidCount) >= 0 && this.mOminLevelOffsetCountEnable) {
                            this.mOminLevelCount = i7 - 1;
                            this.mOminLevelValidCount = i4 - 1;
                            this.mOminLevelCountSetTime = currentTime;
                            Slog.i(TAG, "mOminLevelMode resetoffset-- count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                        }
                    } else {
                        float f2 = this.mPosBrightness;
                        if (f2 - mBrightenDefaultBrightness > 0.0f) {
                            if (this.mOminLevelCount < getOminLevelCountThMax(this.mOminLevelCountLevelPointsList) && (i3 = this.mOminLevelValidCount) <= 0) {
                                this.mOminLevelCount++;
                                this.mOminLevelValidCount = i3 + 1;
                                this.mOminLevelCountSetTime = currentTime;
                                Slog.i(TAG, "mOminLevelMode brighten++ count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                            }
                        } else if (f2 - mBrightenDefaultBrightness < 0.0f && (i = this.mOminLevelCount) > 0 && (i2 = this.mOminLevelValidCount) >= 0) {
                            this.mOminLevelCount = i - 1;
                            this.mOminLevelValidCount = i2 - 1;
                            this.mOminLevelCountSetTime = currentTime;
                            Slog.i(TAG, "mOminLevelMode darken-- count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                        }
                    }
                }
                updateOminLevelBrighnessLinePoints();
            }
        }
    }

    private int resetOminLevelCount(List<PointF> linePointsList, float levelCount) {
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
            PointF temp1 = null;
            for (PointF temp : linePointsList) {
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

    private int getOminLevelCountThMin(List<PointF> linePointsList) {
        if (linePointsList.size() > 0) {
            return (int) linePointsList.get(0).x;
        }
        return 0;
    }

    private int getOminLevelCountThMax(List<PointF> linePointsList) {
        int listSize;
        if (linePointsList != null && (listSize = linePointsList.size()) > 0) {
            return (int) linePointsList.get(listSize - 1).x;
        }
        return 0;
    }

    private float getOminLevelThMin(List<PointF> linePointsList) {
        if (linePointsList.size() > 0) {
            return linePointsList.get(0).y;
        }
        return minBrightness;
    }

    private float getOminLevelThMax(List<PointF> linePointsList) {
        int listSize;
        if (linePointsList != null && (listSize = linePointsList.size()) > 0) {
            return linePointsList.get(listSize - 1).y;
        }
        return minBrightness;
    }

    public boolean getPowerSavingModeBrightnessChangeEnable(float lux, boolean usePowerSavingModeCurveEnable) {
        boolean powerSavingModeBrightnessChangeEnable = false;
        if (this.mPowerSavingBrighnessLineEnable && lux > this.mPowerSavingAmluxThreshold && this.mUsePowerSavingModeCurveEnable != usePowerSavingModeCurveEnable) {
            float mPowerSavingDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mPowerSavingBrighnessLinePointsList, lux);
            float mDefaultBrightnessFromLux2 = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
            if (((int) mPowerSavingDefaultBrightnessFromLux) != ((int) mDefaultBrightnessFromLux2)) {
                powerSavingModeBrightnessChangeEnable = true;
                if (DEBUG) {
                    Slog.i(TAG, "PowerSavingMode lux=" + lux + ",usePgEnable=" + usePowerSavingModeCurveEnable + ",pgBrightness=" + mPowerSavingDefaultBrightnessFromLux + ",mDefaultBrightness=" + mDefaultBrightnessFromLux2);
                }
            }
        }
        this.mUsePowerSavingModeCurveEnable = usePowerSavingModeCurveEnable;
        return powerSavingModeBrightnessChangeEnable;
    }

    public void updateNewBrightnessCurve() {
        if (this.mPersonalizedBrightnessCurveLoadEnable) {
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
        } else if (DEBUG) {
            Slog.i(TAG, "not updateNewBrightnessCurve,mPersonalizedBrightnessCurveLoadEnable=" + this.mPersonalizedBrightnessCurveLoadEnable);
        }
    }

    public List<PointF> getBrightnessListFromDB(String brightnessCurveTag) {
        List<PointF> brightnessList = new ArrayList<>();
        DisplayEngineManager displayEngineManager = this.mManager;
        if (displayEngineManager == null) {
            return brightnessList;
        }
        List<Bundle> records = displayEngineManager.getAllRecords(brightnessCurveTag, new Bundle());
        if (records == null || records.isEmpty()) {
            Slog.i(TAG, "NewCurveMode brightnessList curve=null,tag=" + brightnessCurveTag);
            return brightnessList;
        }
        int listSize = records.size();
        for (int i = 0; i < listSize; i++) {
            Bundle data = records.get(i);
            if (data != null) {
                brightnessList.add(new PointF(data.getFloat("AmbientLight"), data.getFloat(DisplayEngineDbManager.BrightnessCurveKey.BL)));
            }
        }
        return brightnessList;
    }

    private boolean checkBrightnessListIsOK(List<PointF> linePointsList) {
        if (linePointsList == null) {
            Slog.e(TAG, "linePointsListin == null");
            return false;
        } else if (linePointsList.size() <= 2 || linePointsList.size() >= 100) {
            Slog.e(TAG, "linePointsListin number is wrong");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF tmpPoint : linePointsList) {
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

    private List<PointF> cloneList(List<PointF> list) {
        if (list == null) {
            return null;
        }
        List<PointF> newList = new ArrayList<>();
        for (PointF point : list) {
            newList.add(new PointF(point.x, point.y));
        }
        return newList;
    }

    public void updateNewBrightnessCurveTmp() {
        this.mNewCurveEnableTmp = false;
        if (this.mPersonalizedBrightnessEnable && this.mPersonalizedBrightnessCurveLoadEnable) {
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
                Slog.i(TAG, "NewCurveMode updateNewBrightnessCurveTmp success!");
            }
            if (!this.mPowerOnEanble) {
                updateNewBrightnessCurveFromTmp();
            }
        } else if (DEBUG) {
            Slog.d(TAG, "not updateNewBrightnessCurveTmp,mPersonalizedBrightnessEnable=" + this.mPersonalizedBrightnessEnable + ",mPersonalizedBrightnessCurveLoadEnable=" + this.mPersonalizedBrightnessCurveLoadEnable);
        }
    }

    public List<Short> getPersonalizedDefaultCurve() {
        if (this.mBrightnessCurveDefaultTmp.isEmpty()) {
            return null;
        }
        List<Short> curveList = new ArrayList<>();
        for (PointF point : this.mBrightnessCurveDefaultTmp) {
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
        List<Bundle> records;
        DisplayEngineManager displayEngineManager = this.mManager;
        if (displayEngineManager == null || (records = displayEngineManager.getAllRecords("AlgorithmESCW", new Bundle())) == null || records.isEmpty()) {
            return null;
        }
        List<Float> algoParamList = new ArrayList<>();
        for (Bundle bundle : records) {
            if (bundle != null) {
                algoParamList.add(Float.valueOf(bundle.getFloat(DisplayEngineDbManager.AlgorithmEscwKey.ESCW)));
            }
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
                Slog.i(TAG, "VehicleBrightMode curveLevel=" + curveLevel + ",VEnable=" + this.mVehicleModeBrightnessEnable);
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
                if (this.mTwoPointOffsetEnable) {
                    clearLowOffset();
                    clearTmpOffset();
                    Slog.i(TAG, "VehicleBrightMode clear low and tmp brightnessOffset");
                }
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

    public void setNewCurveEnable(boolean enable) {
        if (enable && this.mPersonalizedBrightnessCurveLoadEnable) {
            Slog.i(TAG, "NewCurveMode updateNewBrightnessCurveReal starting..,mNewCurveEnable=" + this.mNewCurveEnable + ",mNewCurveEnableTmp=" + this.mNewCurveEnableTmp);
            updateNewBrightnessCurveFromTmp();
        }
    }

    public void clearBrightnessOffset() {
        if (Math.abs(this.mPosBrightness) > 1.0E-7f) {
            this.mPosBrightness = 0.0f;
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            this.mIsUserChange = false;
            this.mAmLuxOffset = DEFAULT_TWO_POINT_OFFSET_LUX;
            saveOffsetParas();
            if (DEBUG) {
                Slog.i(TAG, "NewCurveMode clear tmp brighntess offset");
            }
        }
    }

    private float getLimitedGameModeBrightness(float brightnessIn) {
        float brightnessOut;
        if (!this.mGameModeEnable || !this.mGameModeBrightnessLimitationEnable || !this.mGameModeBrightnessEnable) {
            return brightnessIn;
        }
        float f = this.mLastGameModeBrightness;
        if (f > 0.0f && f >= brightnessIn) {
            float f2 = this.mGameModeBrightnessFloor;
            if (brightnessIn <= f2) {
                if (f >= f2) {
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
        }
        this.mLastGameModeBrightness = brightnessIn;
        return brightnessIn;
    }

    private float getCurrentBrightness(float lux) {
        List<PointF> brightnessList;
        Object obj;
        new ArrayList();
        switch (getBrightnessMode()) {
            case CAMERA_MODE:
                brightnessList = this.mCameraBrighnessLinePointsList;
                break;
            case GAME_MODE:
                brightnessList = this.mGameModeBrightnessLinePointsList;
                break;
            case VIDEO_FULLSCREEN_MODE:
                brightnessList = this.mVideoFullScreenModeBrightnessLinePointsList;
                break;
            case READING_MODE:
                brightnessList = this.mReadingBrighnessLinePointsList;
                break;
            case NEWCURVE_MODE:
                brightnessList = getCurrentNewCureLine();
                break;
            case POWERSAVING_MODE:
                brightnessList = this.mPowerSavingBrighnessLinePointsList;
                break;
            case EYE_PROTECTION_MODE:
                return this.mEyeProtectionSpline.getEyeProtectionBrightnessLevel(lux);
            case CALIBRATION_MODE:
                brightnessList = this.mDefaultBrighnessLinePointsListCaliBefore;
                break;
            case DARKADAPT_MODE:
                brightnessList = getCurrentDarkAdaptLine();
                break;
            case OMINLEVEL_MODE:
                brightnessList = this.mOminLevelBrighnessLinePointsList;
                break;
            case DARK_MODE:
            case DAY_MODE:
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
            float f = this.mVehicleModeBrighntess;
            if (brightness > f) {
                f = brightness;
            }
            brightness = f;
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
            HwEyeProtectionSpline hwEyeProtectionSpline = this.mEyeProtectionSpline;
            if (hwEyeProtectionSpline == null) {
                obj = "false";
            } else {
                obj = Boolean.valueOf(hwEyeProtectionSpline.isEyeProtectionMode());
            }
            sb.append(obj);
            sb.append(",mVehicleModeBrightnessEnable=");
            sb.append(this.mVehicleModeBrightnessEnable);
            Slog.i(TAG, sb.toString());
        }
        this.mBrightnessForLog = brightness2;
        return brightness2;
    }

    private List<PointF> getCurrentNewCureLine() {
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
        int i = 0;
        while (true) {
            float[] fArr = this.mLuxPonits;
            if (i < fArr.length) {
                if (this.mDayModeAlgoEnable) {
                    brightness = getDefaultBrightnessLevelNew(this.mDayBrighnessLinePointsList, fArr[i]);
                } else {
                    brightness = getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, fArr[i]);
                }
                brightnessList.add(new PointF(this.mLuxPonits[i], brightness));
                i++;
            } else {
                Slog.i(TAG, "NewCurveMode getCurrentDefaultNewCurveLine,mDayModeAlgoEnable=" + this.mDayModeAlgoEnable);
                return brightnessList;
            }
        }
    }

    public boolean getPersonalizedBrightnessCurveEnable() {
        if (!this.mNewCurveEnable || !this.mPersonalizedBrightnessEnable || !this.mPersonalizedBrightnessCurveLoadEnable) {
            return false;
        }
        return true;
    }

    public float getDefaultBrightness(float lux) {
        return getDefaultBrightnessLevelNew(this.mDefaultBrighnessLinePointsList, lux);
    }

    public float getNewDefaultBrightness(float lux) {
        List<PointF> brightnessList;
        new ArrayList();
        if (getBrightnessMode() == BrightnessModeState.NEWCURVE_MODE) {
            brightnessList = this.mBrightnessCurveDefault;
        } else {
            brightnessList = this.mDefaultBrighnessLinePointsList;
        }
        return getDefaultBrightnessLevelNew(brightnessList, lux);
    }

    public float getNewCurrentBrightness(float lux) {
        List<PointF> brightnessList;
        new ArrayList();
        if (getBrightnessMode() == BrightnessModeState.NEWCURVE_MODE) {
            brightnessList = getCurrentNewCureLine();
        } else {
            brightnessList = this.mDefaultBrighnessLinePointsList;
        }
        return getDefaultBrightnessLevelNew(brightnessList, lux);
    }

    public BrightnessModeState getBrightnessMode() {
        if (this.mCameraModeEnable) {
            return BrightnessModeState.CAMERA_MODE;
        }
        if (this.mGameModeEnable && this.mGameModeBrightnessEnable) {
            return BrightnessModeState.GAME_MODE;
        }
        if (this.mVideoFullScreenModeEnable && this.mVideoFullScreenModeBrightnessEnable) {
            return BrightnessModeState.VIDEO_FULLSCREEN_MODE;
        }
        if (this.mReadingModeEnable) {
            return BrightnessModeState.READING_MODE;
        }
        if (this.mNewCurveEnable) {
            return BrightnessModeState.NEWCURVE_MODE;
        }
        if (this.mPowerSavingModeEnable && this.mPowerSavingBrighnessLineEnable && this.mAmLux > this.mPowerSavingAmluxThreshold) {
            return BrightnessModeState.POWERSAVING_MODE;
        }
        HwEyeProtectionSpline hwEyeProtectionSpline = this.mEyeProtectionSpline;
        if (hwEyeProtectionSpline != null && hwEyeProtectionSpline.isEyeProtectionMode() && this.mEyeProtectionSplineEnable) {
            return BrightnessModeState.EYE_PROTECTION_MODE;
        }
        if (this.mCalibrtionModeBeforeEnable) {
            return BrightnessModeState.CALIBRATION_MODE;
        }
        if (this.mDarkAdaptEnable) {
            return BrightnessModeState.DARKADAPT_MODE;
        }
        if (this.mOminLevelCountEnable && this.mOminLevelModeEnable) {
            return BrightnessModeState.OMINLEVEL_MODE;
        }
        if (this.mDarkModeBrightnessEnable) {
            return BrightnessModeState.DARK_MODE;
        }
        if (!this.mDayModeAlgoEnable || !this.mDayModeEnable) {
            return BrightnessModeState.DEFAULT_MODE;
        }
        return BrightnessModeState.DAY_MODE;
    }

    public boolean getPowerSavingBrighnessLineEnable() {
        return this.mPowerSavingBrighnessLineEnable;
    }

    public float getInterpolatedValue(float PositionBrightness, float lux) {
        float offsetBrightness;
        HwEyeProtectionSpline hwEyeProtectionSpline;
        float PosBrightness = PositionBrightness;
        boolean inDarkAdaptMode = false;
        if (this.mPersonalizedBrightnessEnable) {
            float defaultBrightness = getCurrentBrightness(lux);
            if (((int) this.mDefaultBrightnessFromLux) != ((int) defaultBrightness) && DEBUG) {
                Slog.i(TAG, "BrightenssCurve mode=" + getBrightnessMode() + ",lux=" + lux + ",defaultBrightness=" + defaultBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mPis=" + this.mPosBrightness + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mVehicleEnable=" + this.mVehicleModeBrightnessEnable + ",OffsetNeedClear=" + this.mErrorCorrectionOffsetNeedClear);
            }
            if (!this.mDayModeAlgoEnable || !this.mDayModeEnable || getBrightnessMode() != BrightnessModeState.NEWCURVE_MODE) {
                this.mDefaultBrightnessFromLux = defaultBrightness;
            } else {
                float oldBrightness = this.mDefaultBrightnessFromLux;
                float f = this.mDayModeMinimumBrightness;
                if (defaultBrightness > f) {
                    f = defaultBrightness;
                }
                this.mDefaultBrightnessFromLux = f;
                if (DEBUG && oldBrightness != this.mDefaultBrightnessFromLux) {
                    Slog.i(TAG, "getInterpolatedValue DayMode: defaultBrightness =" + defaultBrightness + ", mDayModeMinimumBrightness =" + this.mDayModeMinimumBrightness);
                }
            }
        } else if (!this.mReadingModeEnable && this.mPowerSavingModeEnable && this.mPowerSavingBrighnessLineEnable && this.mAmLux > this.mPowerSavingAmluxThreshold) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mPowerSavingBrighnessLinePointsList, lux);
            Slog.i(TAG, "PowerSavingMode defaultbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux + ",mCalibrationRatio=" + this.mCalibrationRatio);
        } else if (this.mCameraModeEnable) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mCameraBrighnessLinePointsList, lux);
            Slog.i(TAG, "CameraMode defaultbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux);
        } else if (this.mGameModeEnable && this.mGameModeBrightnessEnable) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mGameModeBrightnessLinePointsList, lux);
            Slog.i(TAG, "GameBrightMode defaultbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux);
        } else if (this.mReadingModeEnable) {
            this.mDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mReadingBrighnessLinePointsList, lux);
            Slog.i(TAG, "ReadingMode defaultbrightness=" + this.mDefaultBrightnessFromLux + ",lux=" + lux);
        } else {
            HwEyeProtectionSpline hwEyeProtectionSpline2 = this.mEyeProtectionSpline;
            if (hwEyeProtectionSpline2 == null || !hwEyeProtectionSpline2.isEyeProtectionMode() || !this.mEyeProtectionSplineEnable) {
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
                if (DEBUG && (hwEyeProtectionSpline = this.mEyeProtectionSpline) != null && hwEyeProtectionSpline.isEyeProtectionMode() && !this.mEyeProtectionSplineEnable) {
                    Slog.i(TAG, "getEyeProtectionBrightnessLevel");
                }
            } else {
                this.mDefaultBrightnessFromLux = this.mEyeProtectionSpline.getEyeProtectionBrightnessLevel(lux);
                Slog.i(TAG, "getEyeProtectionBrightnessLevel lux =" + lux + ", mDefaultBrightnessFromLux =" + this.mDefaultBrightnessFromLux);
            }
        }
        if (this.mIsReboot) {
            float f2 = this.mDefaultBrightnessFromLux;
            this.mLastLuxDefaultBrightness = f2;
            this.mStartLuxDefaultBrightness = f2;
            this.mOffsetBrightness_last = f2;
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
                Slog.i(TAG, "error state for default state");
            }
        }
        float f3 = this.mDefaultBrightnessFromLux;
        if ((!this.mGameModeEnable || !this.mGameModeBrightnessEnable || this.mDeltaTmp != 0.0f) && ((this.mGameModeBrightnessEnable || PosBrightness != 0.0f) && !this.mCoverModeNoOffsetEnable)) {
            offsetBrightness = (!this.mGameModeEnable || !this.mGameModeBrightnessEnable) ? inDarkAdaptMode ? getDarkAdaptOffset(PosBrightness, lux) : this.mTwoPointOffsetEnable ? getInterpolatedValueFromTwoPointOffset(PosBrightness, lux) : getOffsetBrightnessLevel_new(this.mStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, PosBrightness) : getOffsetBrightnessLevel_withDelta(this.mGameModeStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, this.mGameModePosBrightness, this.mDeltaTmp);
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
            Slog.i(TAG, "offsetBrightness=" + offsetBrightness + ",mOffsetBrightness_last" + this.mOffsetBrightness_last + ",lux=" + lux + ",mPosBrightness=" + this.mPosBrightness + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",mDefaultBrightnessFromLux=" + this.mDefaultBrightnessFromLux + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
        if (DEBUG && this.mGameModeBrightnessEnable && ((int) offsetBrightness) != ((int) this.mOffsetBrightness_last)) {
            Slog.i(TAG, "GameBrightMode mGameModeStartLuxDefaultBrightness=" + this.mGameModeStartLuxDefaultBrightness + ",offsetBrightness=" + offsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModePosBrightness=" + this.mGameModePosBrightness + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux);
        }
        this.mLastLuxDefaultBrightness = this.mDefaultBrightnessFromLux;
        this.mOffsetBrightness_last = offsetBrightness;
        return offsetBrightness;
    }

    public float getDefaultBrightnessLevelNew(List<PointF> linePointsList, float lux) {
        int count = 0;
        float brightnessLevel = this.mDefaultBrightness;
        PointF temp1 = null;
        for (PointF temp : linePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (lux >= temp.x) {
                temp1 = temp;
                brightnessLevel = temp1.y;
                count++;
            } else if (temp.x > temp1.x) {
                return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
            } else {
                float brightnessLevel2 = this.mDefaultBrightness;
                if (!DEBUG) {
                    return brightnessLevel2;
                }
                Slog.i(TAG, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel2;
            }
        }
        return brightnessLevel;
    }

    /* access modifiers changed from: package-private */
    public float getOffsetBrightnessLevel_new(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        return getOffsetBrightnessLevel_withDelta(brightnessStartOrig, brightnessEndOrig, brightnessStartNew, this.mDelta);
    }

    /* access modifiers changed from: package-private */
    public float getOffsetBrightnessLevel_withDelta(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float delta) {
        if (this.mIsUserChange) {
            this.mIsUserChange = false;
        }
        float ratio = 1.0f;
        float ratio2 = 1.0f;
        if (brightnessStartOrig < brightnessEndOrig) {
            if (delta > 0.0f) {
                float ratio22 = (((-delta) * (Math.abs(brightnessStartOrig - brightnessEndOrig) / (delta + 1.0E-7f))) / ((maxBrightness - brightnessStartOrig) + 1.0E-7f)) + 1.0f;
                if (((int) this.mLastBrightnessEndOrig) != ((int) brightnessEndOrig) && DEBUG) {
                    Slog.i(TAG, "Orig_ratio2=" + ratio22 + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
                }
                float ratio23 = getBrightenOffsetBrightenRaio(((((1.0f - this.mOffsetBrightenAlphaRight) * Math.max(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetBrightenAlphaRight * ((delta * ratio22) + brightnessEndOrig))) - brightnessEndOrig) / (delta + 1.0E-7f), brightnessStartOrig, brightnessEndOrig, brightnessStartNew, delta);
                if (ratio23 < 0.0f) {
                    ratio2 = 0.0f;
                } else {
                    ratio2 = ratio23;
                }
            }
            if (delta < 0.0f) {
                ratio = getDarkenOffsetBrightenRatio((((-delta) * (Math.abs(brightnessStartOrig - brightnessEndOrig) / (delta - 1.0E-7f))) / ((maxBrightness - brightnessStartOrig) + 1.0E-7f)) + 1.0f, brightnessStartOrig, brightnessEndOrig, brightnessStartNew);
                if (ratio < 0.0f) {
                    ratio = 0.0f;
                }
            }
        }
        int i = (brightnessStartOrig > brightnessEndOrig ? 1 : (brightnessStartOrig == brightnessEndOrig ? 0 : -1));
        float offsetBrightnessTemp = minBrightness;
        if (i > 0) {
            if (delta < 0.0f) {
                ratio2 = ((((1.0f - this.mOffsetDarkenAlphaLeft) * Math.min(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetDarkenAlphaLeft * ((delta * (((delta * (Math.abs(brightnessStartOrig - brightnessEndOrig) / (delta - 1.0E-7f))) / ((minBrightness - brightnessStartOrig) - 1.0E-7f)) + 1.0f)) + brightnessEndOrig))) - brightnessEndOrig) / (delta - 1.0E-7f);
                if (ratio2 < 0.0f) {
                    ratio2 = 0.0f;
                }
            }
            if (delta > 0.0f) {
                float ratioTmp = (float) Math.pow((double) (brightnessEndOrig / (brightnessStartOrig + 1.0E-7f)), (double) this.mOffsetBrightenRatioLeft);
                float f = this.mOffsetBrightenAlphaLeft;
                ratio = getBrightenOffsetDarkenRatio(((f * brightnessEndOrig) / (brightnessStartOrig + 1.0E-7f)) + ((1.0f - f) * ratioTmp), brightnessStartOrig, brightnessEndOrig, brightnessStartNew);
                if (((int) this.mLastBrightnessEndOrig) != ((int) brightnessEndOrig) && DEBUG) {
                    Slog.i(TAG, "ratio=" + ratio + ",ratioTmp=" + ratioTmp + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
                }
            }
        }
        this.mDeltaNew = delta * ratio2 * ratio;
        if (this.mLastBrightnessEndOrig != brightnessEndOrig && DEBUG) {
            Slog.i(TAG, "mDeltaNew=" + this.mDeltaNew + ",mDeltaStart=" + delta + ",ratio2=" + ratio2 + ",ratio=" + ratio);
        }
        this.mLastBrightnessEndOrig = brightnessEndOrig;
        float brightnessAndDelta = this.mDeltaNew + brightnessEndOrig;
        if (brightnessAndDelta > minBrightness) {
            offsetBrightnessTemp = brightnessAndDelta;
        }
        if (offsetBrightnessTemp < maxBrightness) {
            return offsetBrightnessTemp;
        }
        return maxBrightness;
    }

    public float getAmbientValueFromDb() {
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
        boolean calibrationTestEable;
        int calibrtionTest = Settings.System.getIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        if (calibrtionTest == 0) {
            this.mCalibrtionModeBeforeEnable = false;
            return false;
        }
        int calibrtionTestLow = calibrtionTest & 65535;
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
        } else {
            calibrationTestEable = false;
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
        HwEyeProtectionSpline hwEyeProtectionSpline = this.mEyeProtectionSpline;
        if (hwEyeProtectionSpline != null) {
            hwEyeProtectionSpline.setEyeProtectionControlFlag(inControlTime);
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
        if (this.mBrightnessOffsetTmpValidEnable && this.mErrorCorrectionOffsetNeedClear && offsetResetEnable) {
            this.mErrorCorrectionOffsetNeedClear = false;
            clearBrightnessOffset();
        } else if (this.mDarkModeBrightnessEnable && offsetResetEnable && Math.abs(this.mPosBrightness) < ((float) this.mDarkModeMinOffsetBrightness) && Math.abs(this.mPosBrightness) > SMALL_VALUE) {
            Slog.i(TAG, "updateLevel: clear offset on DarkBrightMode, mPosBrightness=" + this.mPosBrightness);
            clearBrightnessOffset();
        } else if (offsetResetEnable && Math.abs(this.mPosBrightness) > 1.0E-7f) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > 1.0E-7f) {
                if (((float) minOffsetBrightness) > minBrightness) {
                    float f = this.mCalibrationRatio;
                    if (((float) minOffsetBrightness) * f > minBrightness) {
                        minOffsetBrightness = (int) (((float) minOffsetBrightness) * f);
                    }
                }
                if (((float) maxOffsetBrightness) < maxBrightness) {
                    float f2 = this.mCalibrationRatio;
                    if (((float) maxOffsetBrightness) * f2 < maxBrightness) {
                        maxOffsetBrightness = (int) (((float) maxOffsetBrightness) * f2);
                    }
                }
            }
            if (this.mPosBrightness < ((float) minOffsetBrightness)) {
                this.mPosBrightness = (float) minOffsetBrightness;
                this.mOffsetBrightness_last = (float) minOffsetBrightness;
                this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
                this.mIsReset = true;
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel:resetMin mPosBrightness=" + this.mPosBrightness + ",min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDelta=" + this.mDelta + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
            if (this.mPosBrightness > ((float) maxOffsetBrightness)) {
                this.mPosBrightness = (float) maxOffsetBrightness;
                this.mOffsetBrightness_last = (float) maxOffsetBrightness;
                this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
                this.mIsReset = true;
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel:resetMax mPosBrightness=" + this.mPosBrightness + ",min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDelta=" + this.mDelta + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
        }
    }

    public void resetGameModeOffsetFromHumanFactor(int minOffsetBrightness, int maxOffsetBrightness) {
        if (Math.abs(this.mDeltaTmp) > 1.0E-7f) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > 1.0E-7f) {
                if (((float) minOffsetBrightness) > minBrightness) {
                    float f = this.mCalibrationRatio;
                    if (((float) minOffsetBrightness) * f > minBrightness) {
                        minOffsetBrightness = (int) (((float) minOffsetBrightness) * f);
                    }
                }
                if (((float) maxOffsetBrightness) < maxBrightness) {
                    float f2 = this.mCalibrationRatio;
                    if (((float) maxOffsetBrightness) * f2 < maxBrightness) {
                        maxOffsetBrightness = (int) (((float) maxOffsetBrightness) * f2);
                    }
                }
            }
            float f3 = this.mGameModeStartLuxDefaultBrightness;
            float positionBrightness = this.mDeltaTmp + f3;
            if (positionBrightness < ((float) minOffsetBrightness)) {
                this.mGameModePosBrightness = (float) minOffsetBrightness;
                this.mDeltaTmp = ((float) minOffsetBrightness) - f3;
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel GameMode:resetMin, min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
            if (positionBrightness > ((float) maxOffsetBrightness)) {
                this.mGameModePosBrightness = (float) maxOffsetBrightness;
                this.mDeltaTmp = ((float) maxOffsetBrightness) - this.mGameModeStartLuxDefaultBrightness;
                if (DEBUG) {
                    Slog.i(TAG, "updateLevel GameMode:resetMax, min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
        }
    }

    public void resetGameBrightnessLimitation() {
        this.mLastGameModeBrightness = DEFAULT_TWO_POINT_OFFSET_LUX;
        if (DEBUG) {
            Slog.i(TAG, "resetGameBrightnessLimitation, mLastGameModeBrightness set to -1!");
        }
    }

    private void fillDarkAdaptPointsList() {
        int i;
        int i2 = this.mDarkAdaptingBrightness0LuxLevel;
        if (i2 != 0 && (i = this.mDarkAdaptedBrightness0LuxLevel) != 0) {
            if (i > i2) {
                Slog.w(TAG, "fillDarkAdaptPointsList() error adapted=" + this.mDarkAdaptedBrightness0LuxLevel + " is larger than adapting=" + this.mDarkAdaptingBrightness0LuxLevel);
                return;
            }
            List<PointF> list = this.mDefaultBrighnessLinePointsList;
            if (list != null) {
                float defaultBrighness0LuxLevel = list.get(0).y;
                int i3 = this.mDarkAdaptingBrightness0LuxLevel;
                if (((float) i3) > defaultBrighness0LuxLevel) {
                    Slog.w(TAG, "fillDarkAdaptPointsList() error adapting=" + this.mDarkAdaptingBrightness0LuxLevel + " is larger than default=" + defaultBrighness0LuxLevel);
                    return;
                }
                this.mDarkAdaptingBrightnessPointsList = cloneListAndReplaceFirstElement(this.mDefaultBrighnessLinePointsList, new PointF(0.0f, (float) i3));
                this.mDarkAdaptedBrightnessPointsList = cloneListAndReplaceFirstElement(this.mDefaultBrighnessLinePointsList, new PointF(0.0f, (float) this.mDarkAdaptedBrightness0LuxLevel));
                this.mDarkAdaptEnable = true;
            }
        }
    }

    private List<PointF> cloneListAndReplaceFirstElement(List<PointF> list, PointF element) {
        List<PointF> newList = null;
        if (list == null || element == null) {
            return null;
        }
        for (PointF point : list) {
            if (newList == null) {
                newList = new ArrayList<>();
                newList.add(element);
            } else {
                newList.add(new PointF(point.x, point.y));
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

    /* access modifiers changed from: package-private */
    /* renamed from: android.util.HwNormalizedSpline$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState = new int[DarkAdaptState.values().length];

        static {
            try {
                $SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[DarkAdaptState.UNADAPTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[DarkAdaptState.ADAPTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[DarkAdaptState.ADAPTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState = new int[BrightnessModeState.values().length];
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.CAMERA_MODE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.GAME_MODE.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.VIDEO_FULLSCREEN_MODE.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.READING_MODE.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.NEWCURVE_MODE.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.POWERSAVING_MODE.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.EYE_PROTECTION_MODE.ordinal()] = 7;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.CALIBRATION_MODE.ordinal()] = 8;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.DARKADAPT_MODE.ordinal()] = 9;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.OMINLEVEL_MODE.ordinal()] = 10;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.DARK_MODE.ordinal()] = 11;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$android$util$HwNormalizedSpline$BrightnessModeState[BrightnessModeState.DAY_MODE.ordinal()] = 12;
            } catch (NoSuchFieldError e15) {
            }
        }
    }

    private List<PointF> getCurrentDarkAdaptLine() {
        int i = AnonymousClass2.$SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[this.mDarkAdaptState.ordinal()];
        if (i == 1) {
            return this.mDefaultBrighnessLinePointsList;
        }
        if (i == 2) {
            return this.mDarkAdaptingBrightnessPointsList;
        }
        if (i != 3) {
            return this.mDefaultBrighnessLinePointsList;
        }
        return this.mDarkAdaptedBrightnessPointsList;
    }

    private float getDarkAdaptOffset(float positionBrightness, float lux) {
        float offsetMinLimit;
        float currentOffset = getOffsetBrightnessLevel_new(this.mStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, positionBrightness);
        float f = this.mDelta;
        if (f >= 0.0f) {
            if (HWDEBUG) {
                Slog.d(TAG, String.format("getDarkAdaptOffset() mDelta = %.1f, current = %.1f", Float.valueOf(f), Float.valueOf(currentOffset)));
            }
            return currentOffset;
        } else if (this.mDarkAdaptLineLocked) {
            if (HWDEBUG) {
                Slog.d(TAG, String.format("getDarkAdaptOffset() locked, current = %.1f", Float.valueOf(currentOffset)));
            }
            return currentOffset;
        } else {
            int i = AnonymousClass2.$SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[this.mDarkAdaptState.ordinal()];
            if (i == 1) {
                offsetMinLimit = getDefaultBrightnessLevelNew(this.mDarkAdaptingBrightnessPointsList, lux);
            } else if (i == 2) {
                offsetMinLimit = (getDefaultBrightnessLevelNew(this.mDarkAdaptingBrightnessPointsList, lux) + getDefaultBrightnessLevelNew(this.mDarkAdaptedBrightnessPointsList, lux)) / 2.0f;
            } else if (i != 3) {
                offsetMinLimit = minBrightness;
            } else {
                offsetMinLimit = getDefaultBrightnessLevelNew(this.mDarkAdaptedBrightnessPointsList, lux);
            }
            if (HWDEBUG) {
                Slog.d(TAG, String.format("getDarkAdaptOffset() %s, current = %.1f, minLimit = %.1f", this.mDarkAdaptState, Float.valueOf(currentOffset), Float.valueOf(offsetMinLimit)));
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

    public void setVideoFullScreenModeBrightnessEnable(boolean enable) {
        if (this.mVideoFullScreenModeBrightnessEnable != enable) {
            this.mVideoFullScreenModeBrightnessEnable = enable;
            if (DEBUG) {
                Slog.i(TAG, "VideoBrightMode mVideoFullScreenModeBrightnessEnable=" + this.mVideoFullScreenModeBrightnessEnable);
            }
        }
    }

    public void initVideoFullScreenModeBrightnessPara(boolean enable, List<PointF> list) {
        this.mVideoFullScreenModeEnable = enable;
        Slog.i(TAG, "initVideoFullScreenModeBrightnessPara VideoFullScreenModeEnable=" + this.mVideoFullScreenModeEnable);
        if (this.mVideoFullScreenModeEnable) {
            initVideoFullScreenModeBrightnessLinePointsList(list);
        }
    }

    private void initVideoFullScreenModeBrightnessLinePointsList(List<PointF> list) {
        this.mVideoFullScreenModeBrightnessLinePointsList.clear();
        this.mVideoFullScreenModeBrightnessLinePointsList.addAll(list);
        if (checkBrightnessListIsOK(this.mVideoFullScreenModeBrightnessLinePointsList)) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                updateNewLinePointsListForCalibration(this.mVideoFullScreenModeBrightnessLinePointsList);
                if (DEBUG) {
                    Slog.i(TAG, "update mVideoFullScreenModeBrightnessLinePointsList for calibration");
                }
            }
            for (PointF point : this.mVideoFullScreenModeBrightnessLinePointsList) {
                if (DEBUG) {
                    Slog.i(TAG, "LoadXMLConfig_mVideoFullScreenModeBrightnessLinePointsList x = " + point.x + ", y = " + point.y);
                }
            }
            return;
        }
        loadVideoFullScreenModeDefaultBrightnessLine();
        Slog.w(TAG, "loadVideoFullScreenModeDefaultBrightnessLine");
    }

    private void loadVideoFullScreenModeDefaultBrightnessLine() {
        this.mVideoFullScreenModeBrightnessLinePointsList.clear();
        this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(0.0f, minBrightness));
        this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(4000.0f, maxBrightness));
        this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(40000.0f, maxBrightness));
    }

    public void initDayModeBrightnessPara(boolean dayModeNewCurveEnable, List<PointF> dayModeBrightnessLinePoints) {
        Slog.i(TAG, "initDayModeBrightnessPara NewDayModeBrightnessEnable=" + dayModeNewCurveEnable);
        if (dayModeNewCurveEnable) {
            this.mDayModeAlgoEnable = dayModeNewCurveEnable;
            initDayModeBrightnessLinePointsList(dayModeBrightnessLinePoints);
        }
    }

    private void initDayModeBrightnessLinePointsList(List<PointF> list) {
        this.mDayBrighnessLinePointsList.clear();
        this.mDayBrighnessLinePointsList.addAll(list);
        if (checkBrightnessListIsOK(this.mDayBrighnessLinePointsList)) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                updateNewLinePointsListForCalibration(this.mDayBrighnessLinePointsList);
                if (DEBUG) {
                    Slog.i(TAG, "update NewDayBrighnessLinePointsList for calibration,mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
            for (PointF point : this.mDayBrighnessLinePointsList) {
                if (DEBUG) {
                    Slog.i(TAG, "LoadXMLConfig_NewDayBrighnessLinePointsList x = " + point.x + ", y = " + point.y);
                }
            }
            if (this.mOminLevelModeEnable) {
                getOminLevelBrighnessLinePoints();
                return;
            }
            return;
        }
        loadDefaultConfig();
        Slog.w(TAG, "loadDefaultConfig");
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
        if (getBrightnessMode() == BrightnessModeState.GAME_MODE) {
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
        if (getBrightnessMode() == BrightnessModeState.GAME_MODE) {
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
        if (getBrightnessMode() == BrightnessModeState.GAME_MODE) {
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

    public void updateDarkModeBrightness(boolean enable, int minOffsetBrightness) {
        if (DEBUG) {
            Slog.i(TAG, "DarkBrightMode mDarkModeBrightnessEnable=" + this.mDarkModeBrightnessEnable + "->enable=" + enable + ",minOffsetBrightness=" + minOffsetBrightness);
        }
        this.mDarkModeBrightnessEnable = enable;
        this.mDarkModeMinOffsetBrightness = minOffsetBrightness;
    }

    private void loadTwoPointOffsetParameters() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "mContentResolver==null, no need loadTwoPointOffsetParameters");
            return;
        }
        this.mAmLuxOffsetLowSaved = Settings.System.getFloatForUser(contentResolver, "spline_two_point_offset_lowlux", DEFAULT_TWO_POINT_OFFSET_LUX, this.mCurrentUserId);
        this.mAmLuxOffsetLow = this.mAmLuxOffsetLowSaved;
        this.mPosBrightnessLowSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_two_point_offset_lowlux_level", 0.0f, this.mCurrentUserId);
        this.mPosBrightnessLow = this.mPosBrightnessLowSaved;
        this.mAmLuxOffsetHighSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux", DEFAULT_TWO_POINT_OFFSET_LUX, this.mCurrentUserId);
        this.mAmLuxOffsetHigh = this.mAmLuxOffsetHighSaved;
        this.mPosBrightnessHighSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux_level", 0.0f, this.mCurrentUserId);
        this.mPosBrightnessHigh = this.mPosBrightnessHighSaved;
        Slog.i(TAG, "loadTwoPointOffsetParametersFromDb mAmLuxOffsetLowSaved=" + this.mAmLuxOffsetLowSaved + ",mPosBrightnessLowSaved=" + this.mPosBrightnessLowSaved + ",mAmLuxOffsetHighSaved=" + this.mAmLuxOffsetHighSaved + ",mPosBrightnessHighSaved=" + this.mPosBrightnessHighSaved);
    }

    private void saveTwoPointOffsetParameters() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "mContentResolver==null, no need saveTwoPointOffsetParameters");
            return;
        }
        float f = this.mAmLuxOffsetLow;
        if (f != this.mAmLuxOffsetLowSaved) {
            Settings.System.putFloatForUser(contentResolver, "spline_two_point_offset_lowlux", f, this.mCurrentUserId);
            if (DEBUG) {
                Slog.i(TAG, "twoPointOffset saved mAmLuxOffsetLow=" + this.mAmLuxOffsetLow);
            }
            this.mAmLuxOffsetLowSaved = this.mAmLuxOffsetLow;
        }
        float f2 = this.mPosBrightnessLow;
        if (f2 != this.mPosBrightnessLowSaved) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_two_point_offset_lowlux_level", f2, this.mCurrentUserId);
            if (DEBUG) {
                Slog.i(TAG, "twoPointOffset saved mPosBrightnessLow=" + this.mPosBrightnessLow);
            }
            this.mPosBrightnessLowSaved = this.mPosBrightnessLow;
        }
        float f3 = this.mAmLuxOffsetHigh;
        if (f3 != this.mAmLuxOffsetHighSaved) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux", f3, this.mCurrentUserId);
            if (DEBUG) {
                Slog.i(TAG, "twoPointOffset saved mAmLuxOffsetHigh=" + this.mAmLuxOffsetHigh);
            }
            this.mAmLuxOffsetHighSaved = this.mAmLuxOffsetHigh;
        }
        float f4 = this.mPosBrightnessHigh;
        if (f4 != this.mPosBrightnessHighSaved) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux_level", f4, this.mCurrentUserId);
            if (DEBUG) {
                Slog.i(TAG, "twoPointOffset saved mPosBrightnessHigh=" + this.mPosBrightnessHigh);
            }
            this.mPosBrightnessHighSaved = this.mPosBrightnessHigh;
        }
    }

    private void updateTwoPointOffset(float posBrightness, float offsetLux) {
        if (this.mTwoPointOffsetEnable) {
            boolean z = false;
            if (posBrightness == 0.0f) {
                this.mVehicleModeKeepMinBrightnessEnable = false;
                clearTwoPointOffset();
            } else if (offsetLux < 0.0f) {
                Slog.w(TAG, "offsetLux < 0, offsetLux=" + offsetLux + ",no updateTwoPointOffset");
            } else if (posBrightness > 0.0f && offsetLux >= 0.0f) {
                float defaultBrightness = getCurrentBrightness(offsetLux);
                float brightnessDelta = posBrightness - defaultBrightness;
                if (offsetLux < this.mAmLuxOffsetTh) {
                    if (this.mDelta == 0.0f) {
                        Slog.i(TAG, "updateLevel mTwoPointOffsetLowResetEnable=" + this.mTwoPointOffsetLowResetEnable);
                        this.mTwoPointOffsetLowResetEnable = true;
                    }
                    if (this.mVehicleModeEnable) {
                        if (!this.mVehicleModeBrightnessEnable && posBrightness < this.mVehicleModeBrighntess) {
                            z = true;
                        }
                        this.mVehicleModeKeepMinBrightnessEnable = z;
                    }
                    this.mPosBrightnessLow = posBrightness;
                    this.mAmLuxOffsetLow = offsetLux;
                    Slog.i(TAG, "updateLevel=======add low offset===[lux=" + offsetLux + ",Level=" + posBrightness + ",brightnessDelta=" + brightnessDelta + "]");
                    updateLowTwoPointOffset();
                } else {
                    this.mPosBrightnessHigh = posBrightness;
                    this.mAmLuxOffsetHigh = offsetLux;
                    Slog.i(TAG, "updateLevel=======add high offset===[lux=" + offsetLux + ",Level=" + posBrightness + ",brightnessDelta=" + brightnessDelta + "]");
                    updateHighTwoPointOffset();
                }
                Slog.i(TAG, "updateLevel mPosBrightness=" + this.mPosBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset + ",defaultBrightness=" + defaultBrightness + ",brightnessDelta=" + (posBrightness - defaultBrightness) + ",mVehicleModeKeepMinBrightnessEnable=" + this.mVehicleModeKeepMinBrightnessEnable + getTwoPointOffsetStrings());
            }
        }
    }

    private void updateLowTwoPointOffset() {
        float f = this.mAmLuxOffsetHigh;
        if (f - this.mAmLuxOffsetLow >= this.mAmLuxOffsetLowHighDelta || f < 0.0f) {
            float f2 = this.mPosBrightnessLow;
            float f3 = this.mPosBrightnessHigh;
            if (f2 <= f3 || f3 <= 0.0f) {
                float f4 = this.mAmLuxOffsetTmp;
                if (f4 - this.mAmLuxOffsetLow > this.mAmLuxOffsetLowHighDelta && f4 >= 0.0f) {
                    float f5 = this.mPosBrightnessTmp;
                    if (f5 > this.mPosBrightnessLow && f5 > 0.0f) {
                        Slog.i(TAG, "updateLevel reset backup high offset");
                        resetHighOffset();
                        return;
                    }
                }
                float f6 = this.mAmLuxOffsetTmp;
                if (f6 < this.mAmLuxOffsetTh && f6 >= 0.0f) {
                    Slog.i(TAG, "updateLevel clear tmp offset, sameLowLuxOffset");
                    clearTmpOffset();
                } else if (DEBUG) {
                    Slog.i(TAG, "no need updateLowTwoPointOffset");
                }
            } else {
                Slog.i(TAG, "updateLevel backup high offset, mPosBrightnessLow=" + this.mPosBrightnessLow + " > mPosBrightnessHigh=" + this.mPosBrightnessHigh);
                backupHighOffset();
            }
        } else {
            Slog.i(TAG, "updateLevel clear high offset,deltaLux=" + (this.mAmLuxOffsetHigh - this.mAmLuxOffsetLow));
            clearHighOffset();
        }
    }

    private void updateHighTwoPointOffset() {
        float f = this.mAmLuxOffsetHigh;
        float f2 = this.mAmLuxOffsetLow;
        if (f - f2 >= this.mAmLuxOffsetLowHighDelta || f2 < 0.0f) {
            float f3 = this.mPosBrightnessLow;
            if (f3 <= this.mPosBrightnessHigh || f3 <= 0.0f) {
                float f4 = this.mAmLuxOffsetHigh;
                float f5 = this.mAmLuxOffsetTmp;
                if (f4 - f5 > this.mAmLuxOffsetLowHighDelta && f5 >= 0.0f) {
                    float f6 = this.mPosBrightnessHigh;
                    float f7 = this.mPosBrightnessTmp;
                    if (f6 > f7 && f7 > 0.0f) {
                        Slog.i(TAG, "updateLevel reset backup low offset");
                        resetLowOffset();
                        return;
                    }
                }
                float f8 = this.mAmLuxOffsetTmp;
                if (f8 >= this.mAmLuxOffsetTh && f8 >= 0.0f) {
                    Slog.i(TAG, "updateLevel clear high offset, sameLuxOffset");
                    clearTmpOffset();
                } else if (DEBUG) {
                    Slog.i(TAG, "no need reUpdateHighTwoPointOffset");
                }
            } else {
                Slog.i(TAG, "updateLevel backup low offset, mPosBrightnessLow=" + this.mPosBrightnessLow + " > mPosBrightnessHigh=" + this.mPosBrightnessHigh);
                backupLowOffset();
            }
        } else {
            Slog.i(TAG, "updateLevel clear low offset,deltaLux=" + (this.mAmLuxOffsetHigh - this.mAmLuxOffsetLow));
            clearLowOffset();
        }
    }

    private void clearTwoPointOffset() {
        clearLowOffset();
        clearHighOffset();
        clearTmpOffset();
        if (DEBUG) {
            Slog.i(TAG, "updateLevel clearTwoPointOffset");
        }
    }

    private void backupLowOffset() {
        this.mPosBrightnessTmp = this.mPosBrightnessLow;
        this.mAmLuxOffsetTmp = this.mAmLuxOffsetLow;
        clearLowOffset();
    }

    private void resetLowOffset() {
        float f = this.mAmLuxOffsetTmp;
        if (f < this.mAmLuxOffsetTh) {
            this.mPosBrightnessLow = this.mPosBrightnessTmp;
            this.mAmLuxOffsetLow = f;
        }
        clearTmpOffset();
    }

    private void backupHighOffset() {
        this.mPosBrightnessTmp = this.mPosBrightnessHigh;
        this.mAmLuxOffsetTmp = this.mAmLuxOffsetHigh;
        clearHighOffset();
    }

    private void resetHighOffset() {
        float f = this.mAmLuxOffsetTmp;
        if (f >= this.mAmLuxOffsetTh) {
            this.mPosBrightnessHigh = this.mPosBrightnessTmp;
            this.mAmLuxOffsetHigh = f;
        }
        clearTmpOffset();
    }

    private void clearTmpOffset() {
        this.mPosBrightnessTmp = 0.0f;
        this.mAmLuxOffsetTmp = DEFAULT_TWO_POINT_OFFSET_LUX;
    }

    private void clearLowOffset() {
        this.mPosBrightnessLow = 0.0f;
        this.mAmLuxOffsetLow = DEFAULT_TWO_POINT_OFFSET_LUX;
    }

    private void clearHighOffset() {
        this.mPosBrightnessHigh = 0.0f;
        this.mAmLuxOffsetHigh = DEFAULT_TWO_POINT_OFFSET_LUX;
    }

    private float getInterpolatedValueFromTwoPointOffset(float positionBrightness, float lux) {
        float noValidBrightnessLowDarkHighBright;
        boolean isNeedUpdateLowDarkHighBright;
        float noValidBrightnessLowBrightHighDark;
        boolean isNeedUpdateLowBrightHighDark;
        float defaultBrightness = getCurrentBrightness(lux);
        if (this.mAmLuxOffsetLow == DEFAULT_TWO_POINT_OFFSET_LUX && this.mAmLuxOffsetHigh == DEFAULT_TWO_POINT_OFFSET_LUX) {
            Slog.i(TAG, "noTwoPoitOffset,offsetBrightness=default=" + defaultBrightness + ",lux=" + lux);
            return defaultBrightness;
        }
        boolean isNeedUpdateLowDarkHighBright2 = false;
        float noValidBrightnessLowDarkHighBright2 = defaultBrightness;
        float f = this.mAmLuxOffsetLow;
        if (f >= 0.0f && this.mPosBrightnessLow != 0.0f && this.mAmLuxOffsetHigh >= 0.0f && this.mPosBrightnessHigh != 0.0f) {
            float defaultLow = getCurrentBrightness(f);
            float defaultHigh = getCurrentBrightness(this.mAmLuxOffsetHigh);
            if (this.mPosBrightnessLow < defaultLow && this.mPosBrightnessHigh > defaultHigh) {
                noValidBrightnessLowDarkHighBright2 = getCurrentBrightness(this.mAmLuxOffsetTh);
                isNeedUpdateLowDarkHighBright2 = true;
            }
            float novalidLux = this.mPosBrightnessLow;
            if (novalidLux > defaultLow) {
                float f2 = this.mPosBrightnessHigh;
                if (f2 < defaultHigh) {
                    isNeedUpdateLowDarkHighBright = isNeedUpdateLowDarkHighBright2;
                    noValidBrightnessLowDarkHighBright = noValidBrightnessLowDarkHighBright2;
                    noValidBrightnessLowBrightHighDark = (novalidLux + f2) / 2.0f;
                    isNeedUpdateLowBrightHighDark = true;
                    float offsetBrightnessLow = getLowLuxOffsetBrightness(lux, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
                    float offsetBrightnessHigh = getHighLuxOffsetBrightness(lux, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
                    float offsetBrightness = getTwoPointOffsetBrightness(getCurrentBrightness(lux), offsetBrightnessLow, offsetBrightnessHigh, noValidBrightnessLowDarkHighBright, noValidBrightnessLowBrightHighDark);
                    if (DEBUG && this.mLastLuxForTwoPointOffset != lux) {
                        Slog.i(TAG, "offsetBrightness=" + offsetBrightness + ",defaultBrightness=" + defaultBrightness + ",lux=" + lux + ",offsetLow=" + offsetBrightnessLow + ",offsetHigh=" + offsetBrightnessHigh + ",mVehicleModeKeepMinBrightnessEnable=" + this.mVehicleModeKeepMinBrightnessEnable + getTwoPointOffsetStrings());
                    }
                    this.mLastLuxForTwoPointOffset = lux;
                    return offsetBrightness;
                }
            }
        }
        isNeedUpdateLowDarkHighBright = isNeedUpdateLowDarkHighBright2;
        noValidBrightnessLowDarkHighBright = noValidBrightnessLowDarkHighBright2;
        isNeedUpdateLowBrightHighDark = false;
        noValidBrightnessLowBrightHighDark = defaultBrightness;
        float offsetBrightnessLow2 = getLowLuxOffsetBrightness(lux, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
        float offsetBrightnessHigh2 = getHighLuxOffsetBrightness(lux, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
        float offsetBrightness2 = getTwoPointOffsetBrightness(getCurrentBrightness(lux), offsetBrightnessLow2, offsetBrightnessHigh2, noValidBrightnessLowDarkHighBright, noValidBrightnessLowBrightHighDark);
        Slog.i(TAG, "offsetBrightness=" + offsetBrightness2 + ",defaultBrightness=" + defaultBrightness + ",lux=" + lux + ",offsetLow=" + offsetBrightnessLow2 + ",offsetHigh=" + offsetBrightnessHigh2 + ",mVehicleModeKeepMinBrightnessEnable=" + this.mVehicleModeKeepMinBrightnessEnable + getTwoPointOffsetStrings());
        this.mLastLuxForTwoPointOffset = lux;
        return offsetBrightness2;
    }

    private String getTwoPointOffsetStrings() {
        return "[luxLow=" + this.mAmLuxOffsetLow + ",Level=" + this.mPosBrightnessLow + "]" + "[luxHigh=" + this.mAmLuxOffsetHigh + ",Level=" + this.mPosBrightnessHigh + "]" + "[luxTmp=" + this.mAmLuxOffsetTmp + ",Level=" + this.mPosBrightnessTmp + "]";
    }

    private float getLowLuxOffsetBrightness(float lux, boolean isNeedUpdateLowBrightHighDark, float noValidBrightnessLowBrightHighDark, boolean isNeedUpdateLowDarkHighBright, float noValidBrightnessLowDarkHighBright) {
        float offsetBrightnessLow = getCurrentBrightness(lux);
        if (this.mVehicleModeEnable && this.mVehicleModeBrightnessEnable && this.mVehicleModeKeepMinBrightnessEnable) {
            return offsetBrightnessLow;
        }
        float f = this.mAmLuxOffsetLow;
        if (f < 0.0f || this.mPosBrightnessLow == 0.0f) {
            return offsetBrightnessLow;
        }
        float deltaLow = this.mPosBrightnessLow - getCurrentBrightness(f);
        if (lux > this.mAmLuxOffsetLow) {
            if (deltaLow > 0.0f) {
                return getBrightenOffsetBrightenBrightness(lux, true, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark);
            }
            return getDarkenOffsetBrightenBrightness(lux, true, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
        } else if (deltaLow > 0.0f) {
            return getBrightenOffsetDarkenBrightness(lux, true, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
        } else {
            return getDarkenOffsetDarkenBrightness(lux, true, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark);
        }
    }

    private float getHighLuxOffsetBrightness(float lux, boolean isNeedUpdateLowBrightHighDark, float noValidBrightnessLowBrightHighDark, boolean isNeedUpdateLowDarkHighBright, float noValidBrightnessLowDarkHighBright) {
        float offsetBrightnessHigh = getCurrentBrightness(lux);
        float f = this.mAmLuxOffsetHigh;
        if (f < 0.0f || this.mPosBrightnessHigh == 0.0f) {
            return offsetBrightnessHigh;
        }
        float deltaHigh = this.mPosBrightnessHigh - getCurrentBrightness(f);
        if (lux > this.mAmLuxOffsetHigh) {
            if (deltaHigh > 0.0f) {
                return getBrightenOffsetBrightenBrightness(lux, false, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark);
            }
            return getDarkenOffsetBrightenBrightness(lux, false, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
        } else if (deltaHigh > 0.0f) {
            return getBrightenOffsetDarkenBrightness(lux, false, isNeedUpdateLowDarkHighBright, noValidBrightnessLowDarkHighBright);
        } else {
            return getDarkenOffsetDarkenBrightness(lux, false, isNeedUpdateLowBrightHighDark, noValidBrightnessLowBrightHighDark);
        }
    }

    private float getBrightenOffsetBrightenBrightness(float lux, boolean isLowOffsetEnable, boolean isNeedUpdateLowBrightHighDark, float noValidBrightnessLowBrightHighDark) {
        float noOffsetBrightness;
        float posBrightness;
        float brightnessDelta;
        float brightnessStartOrig;
        float ratio;
        if (isLowOffsetEnable) {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetLow);
            brightnessDelta = this.mPosBrightnessLow - brightnessStartOrig;
            posBrightness = this.mPosBrightnessLow;
            noOffsetBrightness = getCurrentBrightness(this.mLowBrightenOffsetNoValidBrightenLuxTh);
            if (isNeedUpdateLowBrightHighDark) {
                noOffsetBrightness = noValidBrightnessLowBrightHighDark;
            }
        } else {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetHigh);
            brightnessDelta = this.mPosBrightnessHigh - brightnessStartOrig;
            noOffsetBrightness = getCurrentBrightness(this.mHighBrightenOffsetNoValidBrightenLuxTh);
            posBrightness = this.mPosBrightnessHigh;
            if (brightnessStartOrig >= noOffsetBrightness) {
                noOffsetBrightness = maxBrightness;
            }
        }
        float brightnessDefault = getCurrentBrightness(lux);
        if (posBrightness >= noOffsetBrightness) {
            return posBrightness > brightnessDefault ? posBrightness : brightnessDefault;
        }
        if (noOffsetBrightness - brightnessStartOrig < SMALL_VALUE) {
            ratio = 1.0f;
        } else {
            ratio = (noOffsetBrightness - brightnessDefault) / (noOffsetBrightness - brightnessStartOrig);
        }
        if (ratio < 0.0f || ratio > 1.0f) {
            ratio = 0.0f;
        }
        float offsetBrightness = (brightnessDelta * ratio) + brightnessDefault;
        if (!DEBUG || this.mLastLuxForTwoPointOffset == lux) {
            return offsetBrightness;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(isLowOffsetEnable ? "lowLux-" : "highLux-");
        sb.append("BrightenOffset--brighten, offsetBrightness=");
        sb.append(offsetBrightness);
        sb.append(",brightnessDelta=");
        sb.append(brightnessDelta);
        sb.append(",ratio=");
        sb.append(ratio);
        sb.append(",lux=");
        sb.append(lux);
        sb.append(",noOffsetBrightness=");
        sb.append(noOffsetBrightness);
        Slog.i(TAG, sb.toString());
        return offsetBrightness;
    }

    private float getDarkenOffsetBrightenBrightness(float lux, boolean isLowOffsetEnable, boolean isNeedUpdateLowDarkHighBright, float noValidBrightnessLowDarkHighBright) {
        float noOffsetBrightness;
        float brightnessDelta;
        float brightnessStartOrig;
        float ratio;
        if (isLowOffsetEnable) {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetLow);
            brightnessDelta = this.mPosBrightnessLow - brightnessStartOrig;
            float f = this.mPosBrightnessLow;
            noOffsetBrightness = getCurrentBrightness(this.mLowBrightenOffsetNoValidBrightenLuxTh);
            if (isNeedUpdateLowDarkHighBright) {
                noOffsetBrightness = noValidBrightnessLowDarkHighBright;
            }
        } else {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetHigh);
            brightnessDelta = this.mPosBrightnessHigh - brightnessStartOrig;
            noOffsetBrightness = getCurrentBrightness(this.mHighDarkenOffsetNoValidDarkenLuxTh);
            float f2 = this.mPosBrightnessHigh;
            if (brightnessStartOrig >= noOffsetBrightness) {
                noOffsetBrightness = maxBrightness;
            }
        }
        float brightnessDefault = getCurrentBrightness(lux);
        if (noOffsetBrightness - brightnessStartOrig < SMALL_VALUE) {
            ratio = 1.0f;
        } else {
            ratio = (noOffsetBrightness - brightnessDefault) / (noOffsetBrightness - brightnessStartOrig);
        }
        if (ratio < 0.0f || ratio > 1.0f) {
            ratio = 0.0f;
        }
        float offsetBrightness = (brightnessDelta * ratio) + brightnessDefault;
        if (DEBUG && this.mLastLuxForTwoPointOffset != lux) {
            StringBuilder sb = new StringBuilder();
            sb.append(isLowOffsetEnable ? "lowLux-" : "highLux-");
            sb.append("DarkenOffset--brighten, offsetBrightness=");
            sb.append(offsetBrightness);
            sb.append(",brightnessDelta=");
            sb.append(brightnessDelta);
            sb.append(",ratio=");
            sb.append(ratio);
            sb.append(",lux=");
            sb.append(lux);
            sb.append(",noOffsetBrightness=");
            sb.append(noOffsetBrightness);
            Slog.i(TAG, sb.toString());
        }
        return offsetBrightness;
    }

    private float getBrightenOffsetDarkenBrightness(float lux, boolean isLowOffsetEnable, boolean isNeedUpdateLowDarkHighBright, float noValidBrightnessLowDarkHighBright) {
        float noOffsetBrightness;
        float brightnessDelta;
        float brightnessStartOrig;
        float ratio;
        if (isLowOffsetEnable) {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetLow);
            brightnessDelta = this.mPosBrightnessLow - brightnessStartOrig;
            float f = this.mPosBrightnessLow;
            noOffsetBrightness = getCurrentBrightness(this.mLowBrightenOffsetNoValidDarkenLuxTh) - minBrightness;
        } else {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetHigh);
            brightnessDelta = this.mPosBrightnessHigh - brightnessStartOrig;
            noOffsetBrightness = getCurrentBrightness(this.mHighBrightenOffsetNoValidDarkenLuxTh);
            float f2 = this.mPosBrightnessHigh;
            if (isNeedUpdateLowDarkHighBright) {
                noOffsetBrightness = noValidBrightnessLowDarkHighBright;
            }
        }
        float brightnessDefault = getCurrentBrightness(lux);
        if (brightnessStartOrig - noOffsetBrightness < SMALL_VALUE) {
            ratio = 1.0f;
        } else {
            ratio = (brightnessDefault - noOffsetBrightness) / (brightnessStartOrig - noOffsetBrightness);
        }
        if (ratio < 0.0f || ratio > 1.0f) {
            ratio = 0.0f;
        }
        float offsetBrightness = (brightnessDelta * ratio) + brightnessDefault;
        if (DEBUG && this.mLastLuxForTwoPointOffset != lux) {
            StringBuilder sb = new StringBuilder();
            sb.append(isLowOffsetEnable ? "lowLux-" : "highLux-");
            sb.append("BrightenOffset--darken, offsetBrightness=");
            sb.append(offsetBrightness);
            sb.append(",brightnessDelta=");
            sb.append(brightnessDelta);
            sb.append(",ratio=");
            sb.append(ratio);
            sb.append(",lux=");
            sb.append(lux);
            sb.append(",noOffsetBrightness=");
            sb.append(noOffsetBrightness);
            Slog.i(TAG, sb.toString());
        }
        return offsetBrightness;
    }

    private float getDarkenOffsetDarkenBrightness(float lux, boolean isLowOffsetEnable, boolean isNeedUpdateLowBrightHighDark, float noValidBrightnessLowBrightHighDark) {
        float noOffsetBrightness;
        float posBrightness;
        float brightnessDelta;
        float brightnessStartOrig;
        float ratio;
        if (isLowOffsetEnable) {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetLow);
            brightnessDelta = this.mPosBrightnessLow - brightnessStartOrig;
            posBrightness = this.mPosBrightnessLow;
            noOffsetBrightness = getDarkenOffsetLowDarkenNoOffsetBrightness(brightnessStartOrig, brightnessDelta, posBrightness);
        } else {
            brightnessStartOrig = getCurrentBrightness(this.mAmLuxOffsetHigh);
            brightnessDelta = this.mPosBrightnessHigh - brightnessStartOrig;
            posBrightness = this.mPosBrightnessHigh;
            noOffsetBrightness = getCurrentBrightness(this.mHighBrightenOffsetNoValidBrightenLuxTh);
            if (isNeedUpdateLowBrightHighDark) {
                noOffsetBrightness = noValidBrightnessLowBrightHighDark;
            }
        }
        float brightnessDefault = getCurrentBrightness(lux);
        if (posBrightness <= noOffsetBrightness) {
            return posBrightness < brightnessDefault ? posBrightness : brightnessDefault;
        }
        if (brightnessStartOrig - noOffsetBrightness < SMALL_VALUE) {
            ratio = 1.0f;
        } else {
            ratio = (brightnessDefault - noOffsetBrightness) / (brightnessStartOrig - noOffsetBrightness);
        }
        if (ratio < 0.0f || ratio > 1.0f) {
            ratio = 0.0f;
        }
        float offsetBrightness = (brightnessDelta * ratio) + brightnessDefault;
        if (!DEBUG || this.mLastLuxForTwoPointOffset == lux) {
            return offsetBrightness;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(isLowOffsetEnable ? "lowLux-" : "highLux-");
        sb.append("DarkenOffset--darken, offsetBrightness=");
        sb.append(offsetBrightness);
        sb.append(",brightnessDelta=");
        sb.append(brightnessDelta);
        sb.append(",ratio=");
        sb.append(ratio);
        sb.append(",lux=");
        sb.append(lux);
        sb.append(",noOffsetBrightness=");
        sb.append(noOffsetBrightness);
        Slog.i(TAG, sb.toString());
        return offsetBrightness;
    }

    private float getDarkenOffsetLowDarkenNoOffsetBrightness(float brightnessStartOrig, float brightnessDelta, float posBrightness) {
        float noOffsetBrightness;
        if (brightnessStartOrig < SMALL_VALUE) {
            noOffsetBrightness = minBrightness;
        } else {
            noOffsetBrightness = (-brightnessDelta) / brightnessStartOrig > this.mLowDarkenOffsetDarkenBrightnessRatio ? 4.0f : getCurrentBrightness(this.mLowDarkenOffsetNoValidDarkenLuxTh);
        }
        if (posBrightness <= getCurrentBrightness(this.mLowDarkenOffsetNoValidDarkenLuxTh)) {
            noOffsetBrightness = getCurrentBrightness(this.mLowDarkenOffsetNoValidDarkenLuxTh);
        }
        if (posBrightness <= minBrightness) {
            return minBrightness;
        }
        return noOffsetBrightness;
    }

    private float getTwoPointOffsetBrightness(float defaultBrightness, float lowOffsetBrightness, float highOffsetBrightnss, float noValidBrightnessLowDarkHighBright, float noValidBrightnessLowBrightHighDark) {
        if (this.mAmLuxOffsetLow >= 0.0f && this.mPosBrightnessLow != 0.0f && this.mAmLuxOffsetHigh >= 0.0f && this.mPosBrightnessHigh != 0.0f) {
            return getLowHightBrightness(defaultBrightness, lowOffsetBrightness, highOffsetBrightnss, noValidBrightnessLowDarkHighBright, noValidBrightnessLowBrightHighDark);
        }
        if (this.mAmLuxOffsetLow < 0.0f || this.mPosBrightnessLow == 0.0f || this.mAmLuxOffsetHigh != DEFAULT_TWO_POINT_OFFSET_LUX) {
            return (this.mAmLuxOffsetHigh < 0.0f || this.mPosBrightnessHigh == 0.0f || this.mAmLuxOffsetLow != DEFAULT_TWO_POINT_OFFSET_LUX) ? defaultBrightness : highOffsetBrightnss;
        }
        return lowOffsetBrightness;
    }

    private float getLowHightBrightness(float defaultBrightness, float lowOffsetBrightness, float highOffsetBrightnss, float noValidBrightnessLowDarkHighBright, float noValidBrightnessLowBrightHighDark) {
        float dLow = this.mPosBrightnessLow - getCurrentBrightness(this.mAmLuxOffsetLow);
        float dHigh = this.mPosBrightnessHigh - getCurrentBrightness(this.mAmLuxOffsetHigh);
        if (this.mVehicleModeEnable && this.mVehicleModeBrightnessEnable && this.mVehicleModeKeepMinBrightnessEnable) {
            dLow = 0.0f;
        }
        if (dLow > 0.0f && dHigh > 0.0f) {
            return lowOffsetBrightness > highOffsetBrightnss ? lowOffsetBrightness : highOffsetBrightnss;
        }
        if (dLow < 0.0f && dHigh < 0.0f) {
            return lowOffsetBrightness > highOffsetBrightnss ? highOffsetBrightnss : lowOffsetBrightness;
        }
        if (dLow < 0.0f && dHigh > 0.0f) {
            return defaultBrightness > noValidBrightnessLowDarkHighBright ? lowOffsetBrightness > highOffsetBrightnss ? lowOffsetBrightness : highOffsetBrightnss : lowOffsetBrightness > highOffsetBrightnss ? highOffsetBrightnss : lowOffsetBrightness;
        }
        if (dLow > 0.0f && dHigh < 0.0f) {
            return defaultBrightness > noValidBrightnessLowBrightHighDark ? lowOffsetBrightness > highOffsetBrightnss ? highOffsetBrightnss : lowOffsetBrightness : lowOffsetBrightness > highOffsetBrightnss ? lowOffsetBrightness : highOffsetBrightnss;
        }
        if (Math.abs(dLow) < SMALL_VALUE) {
            return highOffsetBrightnss;
        }
        return Math.abs(dHigh) < SMALL_VALUE ? lowOffsetBrightness : defaultBrightness;
    }

    public void initTwoPointOffsetPara(boolean twoPointOffsetEnable, float twoPointOffsetLuxTh, float twoPointOffsetAdjionLuxTh, float twoPointOffsetNoValidLuxTh) {
        if (twoPointOffsetEnable) {
            this.mTwoPointOffsetEnable = twoPointOffsetEnable;
            this.mAmLuxOffsetTh = twoPointOffsetLuxTh;
            this.mAmLuxOffsetLowHighDelta = twoPointOffsetAdjionLuxTh;
            this.mTwoPointOffsetNoValidLuxTh = twoPointOffsetNoValidLuxTh;
            updateTwoPointOffsetPara();
            Slog.i(TAG, "init TwoPointOffsetPara mTwoPointOffsetEnable=" + this.mTwoPointOffsetEnable + ",mAmLuxOffsetTh=" + this.mAmLuxOffsetTh + ",mAmLuxOffsetLowHighDelta=" + this.mAmLuxOffsetLowHighDelta + ",mTwoPointOffsetNoValidLuxTh=" + this.mTwoPointOffsetNoValidLuxTh);
        }
    }

    public void initTwoPointOffsetLowLuxPara(float lowBrightenOffsetNoValidBrightenLuxTh, float lowDarkenOffsetNoValidBrightenLuxTh, float lowBrightenOffsetNoValidDarkenLuxTh, float lowDarkenOffsetNoValidDarkenLuxTh, float lowDarkenOffsetDarkenBrightnessRatio) {
        if (this.mTwoPointOffsetEnable) {
            this.mLowBrightenOffsetNoValidBrightenLuxTh = lowBrightenOffsetNoValidBrightenLuxTh;
            this.mLowDarkenOffsetNoValidBrightenLuxTh = lowDarkenOffsetNoValidBrightenLuxTh;
            this.mLowBrightenOffsetNoValidDarkenLuxTh = lowBrightenOffsetNoValidDarkenLuxTh;
            this.mLowDarkenOffsetNoValidDarkenLuxTh = lowDarkenOffsetNoValidDarkenLuxTh;
            this.mLowDarkenOffsetDarkenBrightnessRatio = lowDarkenOffsetDarkenBrightnessRatio;
            Slog.i(TAG, "init TwoPointOffsetPara mLowBrightenOffsetNoValidBrightenLuxTh=" + this.mLowBrightenOffsetNoValidBrightenLuxTh + ",mLowDarkenOffsetNoValidBrightenLuxTh=" + this.mLowDarkenOffsetNoValidBrightenLuxTh + ",mLowBrightenOffsetNoValidDarkenLuxTh=" + this.mLowBrightenOffsetNoValidDarkenLuxTh + ",mLowDarkenOffsetNoValidDarkenLuxTh=" + this.mLowDarkenOffsetNoValidDarkenLuxTh + ",mLowDarkenOffsetDarkenBrightnessRatio=" + this.mLowDarkenOffsetDarkenBrightnessRatio);
        }
    }

    public void initTwoPointOffsetHighLuxPara(float highBrightenOffsetNoValidBrightenLuxTh, float highDarkenOffsetNoValidBrightenLuxTh, float highBrightenOffsetNoValidDarkenLuxTh, float highDarkenOffsetNoValidDarkenLuxTh) {
        if (this.mTwoPointOffsetEnable) {
            this.mHighBrightenOffsetNoValidBrightenLuxTh = highBrightenOffsetNoValidBrightenLuxTh;
            this.mHighDarkenOffsetNoValidBrightenLuxTh = highDarkenOffsetNoValidBrightenLuxTh;
            this.mHighBrightenOffsetNoValidDarkenLuxTh = highBrightenOffsetNoValidDarkenLuxTh;
            this.mHighDarkenOffsetNoValidDarkenLuxTh = highDarkenOffsetNoValidDarkenLuxTh;
            Slog.i(TAG, "init TwoPointOffsetPara mHighBrightenOffsetNoValidBrightenLuxTh=" + this.mHighBrightenOffsetNoValidBrightenLuxTh + ",mHighDarkenOffsetNoValidBrightenLuxTh=" + this.mHighDarkenOffsetNoValidBrightenLuxTh + ",mHighBrightenOffsetNoValidDarkenLuxTh=" + this.mHighBrightenOffsetNoValidDarkenLuxTh + ",mHighDarkenOffsetNoValidDarkenLuxTh=" + this.mHighDarkenOffsetNoValidDarkenLuxTh);
        }
    }

    public float getCurrentLowAmbientLuxForTwoPointOffset() {
        if (!this.mTwoPointOffsetEnable) {
            return DEFAULT_TWO_POINT_OFFSET_LUX;
        }
        return this.mAmLuxOffsetLow;
    }

    public float getCurrentHighAmbientLuxForTwoPointOffset() {
        if (!this.mTwoPointOffsetEnable) {
            return DEFAULT_TWO_POINT_OFFSET_LUX;
        }
        return this.mAmLuxOffsetHigh;
    }

    public float getCurrentTmpAmbientLuxForTwoPointOffset() {
        if (!this.mTwoPointOffsetEnable) {
            return DEFAULT_TWO_POINT_OFFSET_LUX;
        }
        return this.mAmLuxOffsetTmp;
    }

    public void resetTwoPointOffsetLowFromHumanFactor(boolean enable, int minBrightness2, int maxBrightness2) {
        if (!enable || !this.mTwoPointOffsetEnable || !this.mTwoPointOffsetLowResetEnable) {
            float minOffsetBrightness = (float) minBrightness2;
            float maxOffsetBrightness = (float) maxBrightness2;
            if (enable && this.mTwoPointOffsetEnable && this.mPosBrightnessLow > 0.0f && this.mAmLuxOffsetLow >= 0.0f) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                    if (minOffsetBrightness > ((float) minBrightness2)) {
                        float f = this.mCalibrationRatio;
                        if (minOffsetBrightness * f > ((float) minBrightness2)) {
                            minOffsetBrightness = (float) ((int) (f * minOffsetBrightness));
                        }
                    }
                    if (maxOffsetBrightness < ((float) maxBrightness2)) {
                        float f2 = this.mCalibrationRatio;
                        if (maxOffsetBrightness * f2 < ((float) maxBrightness2)) {
                            maxOffsetBrightness = (float) ((int) (f2 * maxOffsetBrightness));
                        }
                    }
                }
                if (this.mPosBrightnessLow < minOffsetBrightness) {
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetLowFromHumanFactor mPosLow=" + this.mPosBrightnessLow + "-->minOffsetBrightness=" + minOffsetBrightness + ",mAmLuxOffsetLow=" + this.mAmLuxOffsetLow);
                    this.mPosBrightnessLow = (float) minBrightness2;
                }
                if (this.mPosBrightnessLow > maxOffsetBrightness) {
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetLowFromHumanFactor mPosLow=" + this.mPosBrightnessLow + "-->maxOffsetBrightness=" + maxOffsetBrightness + ",mAmLuxOffsetLow=" + this.mAmLuxOffsetLow);
                    this.mPosBrightnessLow = maxOffsetBrightness;
                    return;
                }
                return;
            }
            return;
        }
        Slog.i(TAG, "clearLowOffset for resetTwoPointOffsetLowFromHumanFactor");
        this.mTwoPointOffsetLowResetEnable = false;
        clearLowOffset();
    }

    public void resetTwoPointOffsetHighFromHumanFactor(boolean enable, int minBrightness2, int maxBrightness2) {
        float minOffsetBrightness = (float) minBrightness2;
        float maxOffsetBrightness = (float) maxBrightness2;
        if (enable && this.mTwoPointOffsetEnable && this.mPosBrightnessHigh > 0.0f && this.mAmLuxOffsetHigh >= 0.0f) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                if (minOffsetBrightness > ((float) minBrightness2)) {
                    float f = this.mCalibrationRatio;
                    if (minOffsetBrightness * f > ((float) minBrightness2)) {
                        minOffsetBrightness = (float) ((int) (f * minOffsetBrightness));
                    }
                }
                if (maxOffsetBrightness < ((float) maxBrightness2)) {
                    float f2 = this.mCalibrationRatio;
                    if (maxOffsetBrightness * f2 < ((float) maxBrightness2)) {
                        maxOffsetBrightness = (float) ((int) (f2 * maxOffsetBrightness));
                    }
                }
            }
            if (this.mPosBrightnessHigh < minOffsetBrightness) {
                Slog.i(TAG, "updateLevel resetTwoPointOffsetHighFromHumanFactor mPosHigh=" + this.mPosBrightnessHigh + "-->minOffsetBrightness=" + minOffsetBrightness + ",mAmLuxOffsetHigh=" + this.mAmLuxOffsetHigh);
                this.mPosBrightnessHigh = minOffsetBrightness;
            }
            if (this.mPosBrightnessHigh > maxOffsetBrightness) {
                Slog.i(TAG, "updateLevel resetTwoPointOffsetHighFromHumanFactor mPosHigh=" + this.mPosBrightnessHigh + "-->maxOffsetBrightness=" + maxOffsetBrightness + ",mAmLuxOffsetHigh=" + this.mAmLuxOffsetHigh);
                this.mPosBrightnessHigh = maxOffsetBrightness;
            }
        }
    }

    public void resetTwoPointOffsetTmpFromHumanFactor(boolean enable, int minBrightness2, int maxBrightness2) {
        if (enable && this.mTwoPointOffsetEnable) {
            float f = this.mPosBrightnessTmp;
            if (f > 0.0f && this.mAmLuxOffsetTmp >= 0.0f) {
                if (f < ((float) minBrightness2)) {
                    clearTmpOffset();
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetTmpFromHumanFactor clearTmp min");
                    this.mPosBrightnessTmp = (float) minBrightness2;
                }
                if (this.mPosBrightnessTmp > ((float) maxBrightness2)) {
                    clearTmpOffset();
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetTmpFromHumanFactor clearTmp max");
                    this.mPosBrightnessTmp = (float) maxBrightness2;
                }
            }
        }
    }

    private void updateTwoPointOffsetPara() {
        float f = this.mPosBrightnessLow;
        if (f > 0.0f) {
            this.mVehicleModeKeepMinBrightnessEnable = f < this.mVehicleModeBrighntess;
        }
        if (DEBUG) {
            Slog.i(TAG, "int mVehicleModeKeepMinBrightnessEnable=" + this.mVehicleModeKeepMinBrightnessEnable);
        }
    }
}
