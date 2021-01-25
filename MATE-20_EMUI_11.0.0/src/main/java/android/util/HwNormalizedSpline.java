package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.PointF;
import android.hardware.display.HwFoldScreenState;
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
import com.huawei.internal.widget.ConstantValues;
import com.huawei.sidetouch.TpCommandConstant;
import huawei.android.utils.HwEyeProtectionSpline;
import huawei.android.utils.HwEyeProtectionSplineImpl;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class HwNormalizedSpline extends Spline {
    private static final float ADAPTING_MIN_BRIGHTNESS_FACTOR = 2.0f;
    private static final float AMBIENT_VALID_MAX_LUX = 40000.0f;
    private static final float AMBIENT_VALID_MIN_LUX = 0.0f;
    private static final int BRIGHTNESS_HIGH_CURVE_LEVEL = 2;
    private static final int BRIGHTNESS_LOW_CURVE_LEVEL = 0;
    private static final int BRIGHTNESS_MIDDLE_CURVE_LEVEL = 1;
    private static final float BRIGHTNESS_WITHDELTA_MAX = 230.0f;
    private static final int CALIBRATION_MODE_HIGH_TEST_BIT = 16;
    private static final int CURVE_LIST_VALID_MAX_NUM = 100;
    private static final int CURVE_LIST_VALID_MIN_NUM = 2;
    private static final float DEFAULT_CALIBRATION_RATIO = 1.0f;
    private static final int DEFAULT_DAY_MODE_MODIFY_NUMBER = 3;
    private static final float DEFAULT_MIN_DELTA = 1.0f;
    private static final float DEFAULT_NO_OFFSET_LUX = -1.0f;
    private static final float DEFAULT_OFFSET_BRIGHTNESS = 0.0f;
    private static final float DEFAULT_OFFSET_DELTA = 0.0f;
    private static final float DEFAULT_OFFSET_RATIO = 1.0f;
    private static final int DEFAULT_VERSION = 0;
    private static final float DFAULT_AUTO_BRIGHTNESS_ADJ = 0.0f;
    private static final int DOMESTIC_BETA_VERSION = 3;
    private static final int EYE_PROTECTION_MODE_OFF = 0;
    private static final int EYE_PROTECTION_MODE_ON = 1;
    private static final int FAILED_RETURN_VALUE = -1;
    private static final float GAIN_FACTOR = 10.0f;
    private static final int GAME_MODE_ENTER = 21;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int LCD_NAME_ALL_ARRAY_IC_NAME_INDEX = 2;
    private static final int LCD_NAME_ALL_ARRAY_MAX_NUM = 3;
    private static final int LCD_NAME_INIT_LENGTH = 32;
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final Object LOCK = new Object();
    private static final float MAX_DEFAULT_BRIGHTNESS = 255.0f;
    private static final float MIN_DEFAULT_BRIGHTNESS = 4.0f;
    private static final float MIN_OFFSET_RATIO = 0.0f;
    private static final int NAME_INIT_LENGTH = 128;
    private static final int OMIN_BRIGHTEN_COUNT_STATE = 1;
    private static final int OMIN_DARKEN_COUNT_STATE = -1;
    private static final int OMIN_DEFAULT_COUNT_STATE = 0;
    private static final int SECOND_TIME_FACTOR = 1000;
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "HwNormalizedSpline";
    private static final int TIME_FACTOR = 60000;
    private static final int TOUCH_OEMINFO_DAY_INDEX = 12;
    private static final int TOUCH_OEMINFO_DAY_MAX = 88;
    private static final int TOUCH_OEMINFO_DAY_MIN = 65;
    private static final int TOUCH_OEMINFO_DAY_VALID = 22;
    private static final int TOUCH_OEMINFO_INIT_VALUE = -1;
    private static final int TOUCH_OEMINFO_MAX_NUM = 15;
    private static final int TOUCH_OEMINFO_MONTH_INDEX = 12;
    private static final int TOUCH_OEMINFO_MONTH_MAX = 67;
    private static final int TOUCH_OEMINFO_MONTH_MIN = 65;
    private static final int TOUCH_OEMINFO_MONTH_VALID = 1;
    private static final int TOUCH_OEMINFO_NUM_MAX = 57;
    private static final int TOUCH_OEMINFO_NUM_MIN = 48;
    private static final int TOUCH_OEMINFO_NUM_RATIO = 10;
    private static final String TOUCH_OEMINFO_PATH = "/sys/touchscreen/touch_oem_info";
    private static final int TOUCH_OEMINFO_YEAR_INDEX = 12;
    private static final int TOUCH_OEMINFO_YEAR_VALID = 8;
    private static final float TWO_POINT_OFFSET_BRIGHTNESS_FACTOR = 2.0f;
    private static final int VALID_BRIGHTNESS_MAX_NIT = 1000;
    private static final int VALID_BRIGHTNESS_MIN_NIT = 380;
    private static final int VEHICLE_MOE_ENTER = 19;
    private static final int VEHICLE_MOE_QUIT = 18;
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    private static String sDefaultPanelName = null;
    private static String sDefaultPanelVersion = null;
    private static String sInwardPanelName = null;
    private static String sInwardPanelVersion = null;
    private static String sOutwardPanelName = null;
    private static String sOutwardPanelVersion = null;
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
    private List<PointF> mBrightnessCurveDefault;
    private List<PointF> mBrightnessCurveDefaultTmp;
    private List<PointF> mBrightnessCurveHigh;
    private List<PointF> mBrightnessCurveHighTmp;
    private List<PointF> mBrightnessCurveLow;
    private List<PointF> mBrightnessCurveLowTmp;
    private Map<BrightnessModeState, BrightnessCurveList> mBrightnessCurveMap;
    private List<PointF> mBrightnessCurveMiddle;
    private List<PointF> mBrightnessCurveMiddleTmp;
    private float mBrightnessForLog;
    private float mCalibrationRatio;
    private int mCalibrationTest;
    private List<PointF> mCameraBrightnessLinePointsList;
    private ContentResolver mContentResolver;
    private int mCurrentCurveLevel;
    private int mCurrentUserId;
    private int mCurveLevel;
    private DarkAdaptState mDarkAdaptState;
    private DarkAdaptState mDarkAdaptStateDetected;
    private int mDarkAdaptedBrightness0LuxLevel;
    private List<PointF> mDarkAdaptedBrightnessPointsList;
    private int mDarkAdaptingBrightness0LuxLevel;
    private List<PointF> mDarkAdaptingBrightnessPointsList;
    private List<PointF> mDarkModeBrightnessLinePointsList;
    private int mDarkModeMinOffsetBrightness;
    private float mDarkenOffsetLuxTh1;
    private float mDarkenOffsetLuxTh2;
    private float mDarkenOffsetLuxTh3;
    private float mDarkenOffsetNoValidBrightenLuxTh1;
    private float mDarkenOffsetNoValidBrightenLuxTh2;
    private float mDarkenOffsetNoValidBrightenLuxTh3;
    private float mDarkenOffsetNoValidBrightenLuxTh4;
    private List<PointF> mDayBrightnessLinePointsList;
    private float mDayModeMinimumBrightness;
    private int mDayModeModifyMinBrightness;
    private int mDayModeModifyNumPoint;
    private float mDefaultBrightness;
    private float mDefaultBrightnessFromLux;
    private List<PointF> mDefaultBrightnessLinePointsList;
    private List<PointF> mDefaultBrightnessLinePointsListCaliBefore;
    private float mDelta;
    private float mDeltaNew;
    private float mDeltaSaved;
    private float mDeltaTmp;
    private final int mDeviceActualBrightnessLevel;
    private int mDeviceActualBrightnessNit;
    private int mDeviceStandardBrightnessNit;
    private HwEyeProtectionSpline mEyeProtectionSpline;
    private float mGameModeBrightnessFloor;
    private List<PointF> mGameModeBrightnessLinePointsList;
    private float mGameModeOffsetLux;
    private float mGameModePosBrightness;
    private float mGameModeStartLuxDefaultBrightness;
    private List<PointF> mHdrModeBrightnessLinePointsList;
    private float mHighBrightenOffsetNoValidBrightenLuxTh;
    private float mHighBrightenOffsetNoValidDarkenLuxTh;
    private float mHighDarkenOffsetNoValidBrightenLuxTh;
    private float mHighDarkenOffsetNoValidDarkenLuxTh;
    private boolean mIsBrightenOffsetEffectMinLuxEnable;
    private boolean mIsBrightnessCalibrationEnabled;
    private boolean mIsBrightnessOffsetLuxModeEnable;
    private boolean mIsBrightnessOffsetTmpValidEnable;
    private boolean mIsCalibrationModeBeforeEnable;
    private boolean mIsCameraModeEnable;
    private boolean mIsCoverModeClearOffsetEnable;
    private boolean mIsDarkAdaptEnable;
    private boolean mIsDarkAdaptLineLocked;
    private boolean mIsDarkModeBrightnessEnable;
    private boolean mIsDarkModeCurveEnable;
    private boolean mIsDayModeAlgoEnable;
    private boolean mIsDayModeEnable;
    private boolean mIsErrorCorrectionOffsetNeedClear;
    private boolean mIsEyeProtectionSplineEnable;
    private boolean mIsGameModeBrightnessEnable;
    private boolean mIsGameModeBrightnessLimitationEnable;
    private boolean mIsGameModeEnable;
    private boolean mIsHdrModeCurveEnable;
    private boolean mIsHdrModeEnable;
    private boolean mIsManualMode;
    private boolean mIsNewCurveEnable;
    private boolean mIsNewCurveEnableTmp;
    private boolean mIsOminLevelCountEnable;
    private boolean mIsOminLevelDayModeEnable;
    private boolean mIsOminLevelModeEnable;
    private boolean mIsOminLevelOffsetCountEnable;
    private boolean mIsPersonalizedBrightnessCurveLoadEnable;
    private boolean mIsPersonalizedBrightnessEnable;
    private boolean mIsPowerOnEnable;
    private boolean mIsPowerSavingBrightnessLineEnable;
    private boolean mIsPowerSavingModeEnable;
    private boolean mIsReadingModeEnable;
    private boolean mIsReboot;
    private boolean mIsRebootNewCurveEnable;
    private volatile boolean mIsReset;
    private boolean mIsTwoPointOffsetEnable;
    private boolean mIsTwoPointOffsetLowResetEnable;
    private boolean mIsUsePowerSavingModeCurveEnable;
    private boolean mIsUserChange;
    private boolean mIsUserChangeSaved;
    private boolean mIsVehicleModeBrightnessEnable;
    private boolean mIsVehicleModeClearOffsetEnable;
    private boolean mIsVehicleModeEnable;
    private boolean mIsVehicleModeKeepMinBrightnessEnable;
    private boolean mIsVehicleModeQuitForPowerOnEnable;
    private boolean mIsVideoFullScreenModeBrightnessEnable;
    private boolean mIsVideoFullScreenModeEnable;
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
    private float mOffsetBrightenAlphaLeft;
    private float mOffsetBrightenAlphaRight;
    private float mOffsetBrightenRatioLeft;
    private float mOffsetBrightnessLast;
    private float mOffsetBrightnessLastSaved;
    private float mOffsetDarkenAlphaLeft;
    private float mOminLevel;
    private List<PointF> mOminLevelBrightnessLinePointsList;
    private int mOminLevelCount;
    private List<PointF> mOminLevelCountLevelPointsList;
    private int mOminLevelCountResetLongSetTime;
    private int mOminLevelCountResetLongSetTimeSaved;
    private int mOminLevelCountResetLongTimeTh;
    private int mOminLevelCountSaved;
    private long mOminLevelCountSetTime;
    private int mOminLevelCountValidLuxTh;
    private int mOminLevelCountValidTimeTh;
    private int mOminLevelValidCount;
    private float mPosBrightness;
    private float mPosBrightnessHigh;
    private float mPosBrightnessHighSaved;
    private float mPosBrightnessLow;
    private float mPosBrightnessLowSaved;
    private float mPosBrightnessSaved;
    private float mPosBrightnessTmp;
    private float mPowerSavingAmluxThreshold;
    private List<PointF> mPowerSavingBrightnessLinePointsList;
    private List<PointF> mReadingBrightnessLinePointsList;
    private int mSceneLevel;
    private float mStartLuxDefaultBrightness;
    private float mStartLuxDefaultBrightnessSaved;
    private float mTwoPointOffsetNoValidLuxTh;
    private float mVehicleModeBrightness;
    private float mVehicleModeLuxThreshold;
    private List<PointF> mVideoFullScreenModeBrightnessLinePointsList;

    /* access modifiers changed from: private */
    public interface BrightnessCurveList {
        List<PointF> getBrightnessCurveList();
    }

    /* access modifiers changed from: private */
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
        HDR_MODE,
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
        HWFLOW = z;
    }

    private HwNormalizedSpline(Context context, int deviceActualBrightnessLevel, int deviceActualBrightnessNit, int deviceStandardBrightnessNit) {
        this.mDelta = 0.0f;
        this.mDeltaNew = 0.0f;
        this.mOffsetBrightenRatioLeft = 1.0f;
        this.mOffsetBrightenAlphaLeft = 1.0f;
        this.mOffsetBrightenAlphaRight = 1.0f;
        this.mOffsetDarkenAlphaLeft = 1.0f;
        this.mPosBrightness = MIN_DEFAULT_BRIGHTNESS;
        this.mIsReboot = false;
        this.mIsUserChange = false;
        this.mOffsetBrightnessLast = MIN_DEFAULT_BRIGHTNESS;
        this.mLastLuxDefaultBrightness = MIN_DEFAULT_BRIGHTNESS;
        this.mStartLuxDefaultBrightness = MIN_DEFAULT_BRIGHTNESS;
        this.mCurrentUserId = 0;
        this.mAmLux = DEFAULT_NO_OFFSET_LUX;
        this.mIsCoverModeClearOffsetEnable = false;
        this.mIsReadingModeEnable = false;
        this.mIsCameraModeEnable = false;
        this.mIsManualMode = false;
        this.mManualBrightnessMaxLimit = ConstantValues.MAX_CHANNEL_VALUE;
        this.mIsDayModeEnable = false;
        this.mIsDayModeAlgoEnable = false;
        this.mDayModeModifyNumPoint = 3;
        this.mDayModeModifyMinBrightness = 6;
        this.mIsPowerSavingBrightnessLineEnable = false;
        this.mIsPowerSavingModeEnable = false;
        this.mPowerSavingAmluxThreshold = 25.0f;
        this.mAmLuxOffset = DEFAULT_NO_OFFSET_LUX;
        this.mAmLuxOffsetSaved = DEFAULT_NO_OFFSET_LUX;
        this.mCalibrationRatio = 1.0f;
        this.mIsOminLevelModeEnable = false;
        this.mIsOminLevelOffsetCountEnable = false;
        this.mIsOminLevelCountEnable = false;
        this.mIsOminLevelDayModeEnable = false;
        this.mOminLevelCount = 0;
        this.mOminLevelCountSaved = 0;
        this.mOminLevel = 6.0f;
        this.mOminLevelCountValidLuxTh = 5;
        this.mOminLevelCountValidTimeTh = 60;
        this.mOminLevelCountSetTime = -1;
        this.mOminLevelCountResetLongTimeTh = 20160;
        this.mOminLevelCountResetLongSetTime = -1;
        this.mOminLevelValidCount = 0;
        this.mIsEyeProtectionSplineEnable = true;
        this.mOminLevelBrightnessLinePointsList = null;
        this.mOminLevelCountLevelPointsList = null;
        this.mIsReset = false;
        this.mDayModeMinimumBrightness = MIN_DEFAULT_BRIGHTNESS;
        this.mDefaultBrightnessLinePointsList = null;
        this.mDefaultBrightnessLinePointsListCaliBefore = null;
        this.mEyeProtectionSpline = null;
        this.mCameraBrightnessLinePointsList = null;
        this.mReadingBrightnessLinePointsList = null;
        this.mDayBrightnessLinePointsList = null;
        this.mPowerSavingBrightnessLinePointsList = null;
        this.mBrightnessCurveDefault = new ArrayList();
        this.mBrightnessCurveLow = new ArrayList();
        this.mBrightnessCurveMiddle = new ArrayList();
        this.mBrightnessCurveHigh = new ArrayList();
        this.mBrightnessCurveDefaultTmp = new ArrayList();
        this.mBrightnessCurveLowTmp = new ArrayList();
        this.mBrightnessCurveMiddleTmp = new ArrayList();
        this.mBrightnessCurveHighTmp = new ArrayList();
        this.mIsRebootNewCurveEnable = true;
        this.mIsNewCurveEnable = false;
        this.mIsNewCurveEnableTmp = false;
        this.mCurveLevel = -1;
        this.mIsPowerOnEnable = false;
        this.mIsPersonalizedBrightnessEnable = false;
        this.mIsPersonalizedBrightnessCurveLoadEnable = true;
        this.mLuxPonits = new float[]{0.0f, 2.0f, 5.0f, GAIN_FACTOR, 15.0f, 20.0f, 30.0f, 50.0f, 70.0f, 100.0f, 150.0f, 200.0f, 250.0f, 300.0f, 350.0f, 400.0f, 500.0f, 600.0f, 700.0f, 800.0f, 900.0f, 1000.0f, 1200.0f, 1400.0f, 1800.0f, 2400.0f, 3000.0f, 4000.0f, 5000.0f, 6000.0f, 8000.0f, 10000.0f, 20000.0f, 30000.0f, AMBIENT_VALID_MAX_LUX};
        this.mIsVehicleModeBrightnessEnable = false;
        this.mIsVehicleModeClearOffsetEnable = false;
        this.mIsVehicleModeEnable = false;
        this.mVehicleModeBrightness = MIN_DEFAULT_BRIGHTNESS;
        this.mVehicleModeLuxThreshold = 0.0f;
        this.mSceneLevel = -1;
        this.mIsGameModeEnable = false;
        this.mIsGameModeBrightnessEnable = false;
        this.mIsGameModeBrightnessLimitationEnable = false;
        this.mGameModeBrightnessFloor = MIN_DEFAULT_BRIGHTNESS;
        this.mLastGameModeBrightness = DEFAULT_NO_OFFSET_LUX;
        this.mDeltaTmp = 0.0f;
        this.mGameModeOffsetLux = DEFAULT_NO_OFFSET_LUX;
        this.mGameModeBrightnessLinePointsList = new ArrayList();
        this.mGameModeStartLuxDefaultBrightness = DEFAULT_NO_OFFSET_LUX;
        this.mGameModePosBrightness = 0.0f;
        this.mDarkAdaptState = DarkAdaptState.UNADAPTED;
        this.mDarkAdaptStateDetected = DarkAdaptState.UNADAPTED;
        this.mIsBrightnessOffsetLuxModeEnable = false;
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
        this.mIsBrightenOffsetEffectMinLuxEnable = false;
        this.mIsBrightnessOffsetTmpValidEnable = false;
        this.mBrightenOffsetNoValidSavedLuxTh1 = 0.0f;
        this.mBrightenOffsetNoValidSavedLuxTh2 = 0.0f;
        this.mIsErrorCorrectionOffsetNeedClear = false;
        this.mLastBrightnessEndOrigIn = DEFAULT_NO_OFFSET_LUX;
        this.mLastBrightnessEndOrig = DEFAULT_NO_OFFSET_LUX;
        this.mIsVideoFullScreenModeEnable = false;
        this.mIsVideoFullScreenModeBrightnessEnable = false;
        this.mBrightnessForLog = DEFAULT_NO_OFFSET_LUX;
        this.mCurrentCurveLevel = -1;
        this.mVideoFullScreenModeBrightnessLinePointsList = new ArrayList<PointF>() {
            /* class android.util.HwNormalizedSpline.AnonymousClass1 */

            {
                add(new PointF(0.0f, HwNormalizedSpline.MIN_DEFAULT_BRIGHTNESS));
                add(new PointF(25.0f, 46.5f));
                add(new PointF(1995.0f, 140.7f));
                add(new PointF(4000.0f, HwNormalizedSpline.MAX_DEFAULT_BRIGHTNESS));
                add(new PointF(HwNormalizedSpline.AMBIENT_VALID_MAX_LUX, HwNormalizedSpline.MAX_DEFAULT_BRIGHTNESS));
            }
        };
        this.mIsDarkModeBrightnessEnable = false;
        this.mDarkModeMinOffsetBrightness = 4;
        this.mIsDarkModeCurveEnable = false;
        this.mDarkModeBrightnessLinePointsList = new ArrayList<PointF>() {
            /* class android.util.HwNormalizedSpline.AnonymousClass2 */

            {
                add(new PointF(0.0f, HwNormalizedSpline.MIN_DEFAULT_BRIGHTNESS));
                add(new PointF(25.0f, 46.5f));
                add(new PointF(1995.0f, 140.7f));
                add(new PointF(4000.0f, HwNormalizedSpline.MAX_DEFAULT_BRIGHTNESS));
                add(new PointF(HwNormalizedSpline.AMBIENT_VALID_MAX_LUX, HwNormalizedSpline.MAX_DEFAULT_BRIGHTNESS));
            }
        };
        this.mIsTwoPointOffsetEnable = false;
        this.mAmLuxOffsetTh = 50.0f;
        this.mAmLuxOffsetLowHighDelta = 50.0f;
        this.mTwoPointOffsetNoValidLuxTh = 50.0f;
        this.mAmLuxOffsetLow = DEFAULT_NO_OFFSET_LUX;
        this.mAmLuxOffsetLowSaved = DEFAULT_NO_OFFSET_LUX;
        this.mPosBrightnessLow = 0.0f;
        this.mPosBrightnessLowSaved = 0.0f;
        this.mAmLuxOffsetHigh = DEFAULT_NO_OFFSET_LUX;
        this.mAmLuxOffsetHighSaved = DEFAULT_NO_OFFSET_LUX;
        this.mPosBrightnessHigh = 0.0f;
        this.mPosBrightnessHighSaved = 0.0f;
        this.mAmLuxOffsetTmp = DEFAULT_NO_OFFSET_LUX;
        this.mPosBrightnessTmp = 0.0f;
        this.mLowDarkenOffsetNoValidBrightenLuxTh = 500.0f;
        this.mLowBrightenOffsetNoValidBrightenLuxTh = 500.0f;
        this.mLowBrightenOffsetNoValidDarkenLuxTh = 0.0f;
        this.mLowDarkenOffsetNoValidDarkenLuxTh = 0.0f;
        this.mLowDarkenOffsetDarkenBrightnessRatio = 0.2f;
        this.mHighBrightenOffsetNoValidBrightenLuxTh = 10000.0f;
        this.mHighDarkenOffsetNoValidBrightenLuxTh = 10000.0f;
        this.mHighBrightenOffsetNoValidDarkenLuxTh = GAIN_FACTOR;
        this.mHighDarkenOffsetNoValidDarkenLuxTh = GAIN_FACTOR;
        this.mIsTwoPointOffsetLowResetEnable = false;
        this.mIsVehicleModeKeepMinBrightnessEnable = false;
        this.mIsUsePowerSavingModeCurveEnable = false;
        this.mIsVehicleModeQuitForPowerOnEnable = false;
        this.mBrightnessCurveMap = new HashMap();
        this.mHdrModeBrightnessLinePointsList = new ArrayList<PointF>() {
            /* class android.util.HwNormalizedSpline.AnonymousClass3 */

            {
                add(new PointF(0.0f, HwNormalizedSpline.MIN_DEFAULT_BRIGHTNESS));
                add(new PointF(25.0f, 46.5f));
                add(new PointF(1995.0f, 140.7f));
                add(new PointF(4000.0f, HwNormalizedSpline.MAX_DEFAULT_BRIGHTNESS));
                add(new PointF(HwNormalizedSpline.AMBIENT_VALID_MAX_LUX, HwNormalizedSpline.MAX_DEFAULT_BRIGHTNESS));
            }
        };
        this.mManager = new DisplayEngineManager();
        this.mIsReboot = true;
        if (context != null) {
            this.mContentResolver = context.getContentResolver();
        }
        this.mDeviceActualBrightnessLevel = deviceActualBrightnessLevel;
        this.mDeviceActualBrightnessNit = deviceActualBrightnessNit;
        this.mDeviceStandardBrightnessNit = deviceStandardBrightnessNit;
        loadCameraDefaultBrightnessLine();
        loadReadingDefaultBrightnessLine();
        loadPowerSavingDefaultBrightnessLine();
        loadOminLevelCountLevelPointsList();
        loadGameModeDefaultBrightnessLine();
        try {
            if (!isLoadConfigedParametersValid()) {
                Slog.e(TAG, "isLoadConfigedParametersValid failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (IOException e) {
            Slog.e(TAG, "IOException : loadDefaultConfig");
            loadDefaultConfig();
        }
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 1) != 0) {
            this.mEyeProtectionSpline = new HwEyeProtectionSplineImpl(context);
        }
        updateAutoBrightnessMode();
        updateLinePointsListForCalibration();
        loadOffsetParas();
        if (this.mIsOminLevelModeEnable) {
            getOminLevelBrightnessLinePoints();
        }
        initBrightnessCurveMap();
    }

    private void updateAutoBrightnessMode() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "updateAutoBrightnessMode failed, mContentResolver==null");
        } else if (Settings.System.getIntForUser(contentResolver, "screen_brightness_mode", 0, this.mCurrentUserId) == 0) {
            float posBrightnessSaved = Settings.System.getFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId) * MAX_DEFAULT_BRIGHTNESS;
            if (Math.abs(posBrightnessSaved) > SMALL_VALUE) {
                Settings.System.putFloatForUser(this.mContentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId);
                Slog.i(TAG, "clear autobrightness offset,orig posBrightnessSaved=" + posBrightnessSaved);
            }
        }
    }

    private void initBrightnessCurveMap() {
        this.mBrightnessCurveMap.clear();
        this.mBrightnessCurveMap.put(BrightnessModeState.CAMERA_MODE, new CameraModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.GAME_MODE, new GameModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.HDR_MODE, new HdrModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.VIDEO_FULLSCREEN_MODE, new VideoFullScreenModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.READING_MODE, new ReadingModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.NEWCURVE_MODE, new NewCurveModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.POWERSAVING_MODE, new PowerSavingModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.EYE_PROTECTION_MODE, new EyeProtectionModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.CALIBRATION_MODE, new CalibrationModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.DARKADAPT_MODE, new DarkAdaptModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.OMINLEVEL_MODE, new OminLevelModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.DARK_MODE, new DarkModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.DAY_MODE, new DayModeCurveList());
        this.mBrightnessCurveMap.put(BrightnessModeState.DEFAULT_MODE, new DefaultModeCurveList());
        Slog.i(TAG, "Init brightness curve map");
    }

    private Optional<File> getFactoryXmlFile() {
        Optional<String> lcdName;
        updatePanelName();
        ArrayList<String> xmlPathList = new ArrayList<>();
        Optional.empty();
        if (HwFoldScreenState.isInwardFoldDevice()) {
            lcdName = Optional.ofNullable(sInwardPanelName);
        } else {
            lcdName = Optional.ofNullable(sDefaultPanelName);
        }
        if (lcdName.isPresent()) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s%s", XML_NAME_NOEXT, lcdName.get(), "factory", XML_EXT));
        }
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s%s", XML_NAME_NOEXT, "factory", XML_EXT));
        Optional.empty();
        int listSize = xmlPathList.size();
        for (int i = 0; i < listSize; i++) {
            Optional<File> xmlFile = Optional.ofNullable(HwCfgFilePolicy.getCfgFile(xmlPathList.get(i), 2));
            if (xmlFile.isPresent()) {
                Slog.i(TAG, "get factory xmlFile=" + xmlPathList.get(i));
                return xmlFile;
            }
        }
        Slog.w(TAG, "get factory xmlFile=LABCConfig failed!");
        return Optional.empty();
    }

    private void updatePanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        if (binder == null) {
            Slog.w(TAG, "updatePanelName() binder is null!");
            return;
        }
        IDisplayEngineServiceEx displayEngineExService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (displayEngineExService == null) {
            Slog.w(TAG, "updatePanelName() displayEngineExService is null!");
            return;
        }
        Bundle data = new Bundle();
        try {
            int ret = displayEngineExService.getEffectEx(14, 13, data);
            if (ret != 0) {
                Slog.w(TAG, "updatePanelName() getEffect failed! ret=" + ret);
                return;
            }
            updatePanelNameFromPanelInfos(data);
        } catch (RemoteException e) {
            Slog.w(TAG, "updatePanelName() RemoteException ");
        }
    }

    private void updatePanelNameFromPanelInfos(Bundle data) {
        if (data == null) {
            Slog.w(TAG, "updatePanelNameFromPanelInfos() failed! data=null");
        } else if (HwFoldScreenState.isInwardFoldDevice()) {
            sInwardPanelName = data.getString("FullpanelName");
            sOutwardPanelName = data.getString("MainpanelName");
            sInwardPanelVersion = data.getString("FulllcdPanelVersion");
            sOutwardPanelVersion = data.getString("MainlcdPanelVersion");
            sInwardPanelName = parsePanelName(sInwardPanelName);
            sOutwardPanelName = parsePanelName(sOutwardPanelName);
            sInwardPanelVersion = parsePanelVersion(sInwardPanelVersion);
            sOutwardPanelVersion = parsePanelVersion(sOutwardPanelVersion);
            if (sOutwardPanelVersion != null) {
                sOutwardPanelVersion = "O" + sOutwardPanelVersion;
            }
            if (HWDEBUG) {
                Slog.i(TAG, "sInwardPanelName=" + sInwardPanelName + ",sOutwardPanelName=" + sOutwardPanelName + ",sInwardPanelVersion=" + sInwardPanelVersion + ",sOutwardPanelVersion=" + sOutwardPanelVersion);
            }
            Slog.i(TAG, "sInwardPanelVersion=" + sInwardPanelVersion + ",sOutwardPanelVersion=" + sOutwardPanelVersion);
        } else {
            sDefaultPanelName = data.getString(DisplayEngineManager.NAME_PANEL_NAME);
            sDefaultPanelVersion = data.getString(DisplayEngineManager.NAME_LCD_PANEL_VERSION);
            sDefaultPanelName = parsePanelName(sDefaultPanelName);
            sDefaultPanelVersion = parsePanelVersion(sDefaultPanelVersion);
            if (HWDEBUG) {
                Slog.i(TAG, "sDefaultPanelName=" + sDefaultPanelName + ",sDefaultPanelVersion=" + sDefaultPanelVersion);
            }
            Slog.i(TAG, "sDefaultPanelVersion=" + sDefaultPanelVersion);
        }
    }

    private String parsePanelName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replace(' ', '_');
    }

    private String parsePanelVersion(String name) {
        String lcdVersion;
        int index;
        if (name == null || (index = (lcdVersion = name.trim()).indexOf("VER:")) == -1) {
            return null;
        }
        return lcdVersion.substring("VER:".length() + index);
    }

    private Optional<String> getVersionFromTouchOeminfo() {
        String version = null;
        try {
            File file = new File(String.format(Locale.ENGLISH, "%s", TOUCH_OEMINFO_PATH));
            if (!file.exists()) {
                Slog.i(TAG, "lcdversionInfo versionfile is not exists, no need version,filePath=/sys/touchscreen/touch_oem_info");
                return Optional.empty();
            }
            String touchOeminfo = FileUtils.readTextFile(file, 0, null).trim();
            Slog.i(TAG, "touchOeminfo=" + touchOeminfo);
            String[] versionInfo = touchOeminfo.split(",");
            if (versionInfo.length <= 15) {
                Slog.i(TAG, "lcdversionInfo versionfile info length is not valid,no need version");
                return Optional.empty();
            }
            try {
                int productYear = Integer.parseInt(versionInfo[12]);
                int productMonth = Integer.parseInt(versionInfo[12]);
                int productDay = Integer.parseInt(versionInfo[12]);
                Slog.i(TAG, "lcdversionInfo orig productYear=" + productYear + ",productMonth=" + productMonth + ",productDay=" + productDay);
                int productYear2 = getTouchOeminfoRealProductYear(productYear);
                int productMonth2 = getTouchOeminfoRealProductMonth(productMonth);
                int productDay2 = getTouchOeminfoRealProductDay(productDay);
                if (!getVerisonFromTouchOeminfo(productYear2, productMonth2, productDay2).isPresent()) {
                    return Optional.empty();
                }
                version = getVerisonFromTouchOeminfo(productYear2, productMonth2, productDay2).get();
                return Optional.ofNullable(version);
            } catch (NumberFormatException e) {
                Slog.i(TAG, "lcdversionInfo versionfile num is not valid,no need version");
                return Optional.empty();
            }
        } catch (IOException e2) {
            Slog.w(TAG, "Error reading touch_OEMINFO");
        }
    }

    private int getTouchOeminfoRealProductYear(int productYear) {
        if (productYear >= 48 && productYear <= TOUCH_OEMINFO_NUM_MAX) {
            return productYear - 48;
        }
        Slog.i(TAG, "lcdversionInfo not valid productYear=" + productYear);
        return -1;
    }

    private int getTouchOeminfoRealProductMonth(int productMonth) {
        if (productMonth >= 48 && productMonth <= TOUCH_OEMINFO_NUM_MAX) {
            return productMonth - 48;
        }
        if (productMonth >= 65 && productMonth <= 67) {
            return (productMonth - 65) + 10;
        }
        Slog.i(TAG, "lcdversionInfo not valid productMonth=" + productMonth);
        return -1;
    }

    private int getTouchOeminfoRealProductDay(int productDay) {
        if (productDay >= 48 && productDay <= TOUCH_OEMINFO_NUM_MAX) {
            return productDay - 48;
        }
        if (productDay >= 65 && productDay <= TOUCH_OEMINFO_DAY_MAX) {
            return (productDay - 65) + 10;
        }
        Slog.i(TAG, "lcdversionInfo not valid productDay=" + productDay);
        return -1;
    }

    private Optional<String> getVerisonFromTouchOeminfo(int productYear, int productMonth, int productDay) {
        String version;
        if (productYear == -1) {
            return Optional.empty();
        }
        if (productMonth == -1) {
            return Optional.empty();
        }
        if (productDay == -1) {
            return Optional.empty();
        }
        if (productYear > 8) {
            version = "vn2";
        } else if (productYear == 8 && productMonth > 1) {
            version = "vn2";
        } else if (productYear == 8 && productMonth == 1 && productDay >= 22) {
            version = "vn2";
        } else {
            Slog.i(TAG, "lcdversionInfo not valid version;productYear=" + productYear + ",productMonth=" + productMonth + ",productDay=" + productDay);
            return Optional.empty();
        }
        Slog.i(TAG, "lcdversionInfo real vn2,productYear=" + productYear + ",productMonth=" + productMonth + ",productDay=" + productDay);
        return Optional.ofNullable(version);
    }

    private Optional<String> getLcdIcName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        if (binder == null) {
            Slog.e(TAG, "getLcdIcName() binder is null!");
            return Optional.empty();
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdIcName() mService is null!");
            return Optional.empty();
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 5, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdIcName() getEffect failed! ret=" + ret);
                return Optional.empty();
            }
            String lcdIcName = null;
            String[] lcdNameAllArray = new String(name, StandardCharsets.UTF_8).trim().split(TpCommandConstant.SEPARATE);
            if (lcdNameAllArray.length >= 3) {
                int index = lcdNameAllArray[2].indexOf("ICTYPE:");
                Slog.i(TAG, "getLcdIcName() index=" + index + ",LcdIcNameALL=" + lcdNameAllArray[2]);
                if (index != -1) {
                    lcdIcName = lcdNameAllArray[2].substring("ICTYPE:".length() + index);
                }
            }
            return Optional.ofNullable(lcdIcName);
        } catch (RemoteException e) {
            Slog.e(TAG, "getLcdIcName() RemoteException");
            return Optional.empty();
        }
    }

    private Optional<File> getNormalXmlFile() {
        updatePanelName();
        Optional.empty();
        Optional.empty();
        List<String> xmlPathList = new ArrayList<>();
        if (HwFoldScreenState.isInwardFoldDevice()) {
            Optional<String> lcdName = Optional.ofNullable(sInwardPanelName);
            Optional<String> lcdVersionNew = Optional.ofNullable(sInwardPanelVersion);
            Optional<String> outwardlcdVersionNew = Optional.ofNullable(sOutwardPanelVersion);
            if (lcdName.isPresent() && lcdVersionNew.isPresent() && outwardlcdVersionNew.isPresent()) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersionNew.get(), outwardlcdVersionNew.get()));
            }
            if (lcdName.isPresent() && lcdVersionNew.isPresent()) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersionNew.get()));
            }
            if (lcdName.isPresent() && outwardlcdVersionNew.isPresent()) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), outwardlcdVersionNew.get()));
            }
            if (lcdName.isPresent()) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, lcdName.get()));
            }
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s", XML_NAME_NOEXT));
        } else {
            xmlPathList = getDefaultXmlPathList();
        }
        Optional<File> xmlFile = getXmlFromXmlPathList(xmlPathList);
        if (xmlFile.isPresent()) {
            return xmlFile;
        }
        Slog.e(TAG, "get failed!");
        return Optional.empty();
    }

    /* access modifiers changed from: package-private */
    public List<String> getDefaultXmlPathList() {
        List<String> xmlPathList = new ArrayList<>();
        Optional<String> lcdName = Optional.ofNullable(sDefaultPanelName);
        Optional<String> lcdVersionNew = Optional.ofNullable(sDefaultPanelVersion);
        Optional<String> lcdVersion = getVersionFromTouchOeminfo();
        String screenColor = SystemProperties.get("ro.config.devicecolor");
        Optional<String> lcdIcName = getLcdIcName();
        if (HWFLOW) {
            Slog.i(TAG, "screenColor=" + screenColor);
            Slog.i(TAG, "lcdIcName=" + lcdIcName);
        }
        if (lcdVersionNew.isPresent() && lcdIcName.isPresent() && lcdName.isPresent()) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersionNew.get(), lcdIcName.get(), screenColor));
        }
        if (lcdVersion.isPresent() && lcdName.isPresent()) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersion.get(), screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersion.get()));
        }
        if (lcdVersionNew.isPresent() && lcdName.isPresent()) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersionNew.get(), screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdVersionNew.get()));
        }
        if (lcdIcName.isPresent() && lcdName.isPresent()) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdIcName.get(), screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), lcdIcName.get()));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, lcdIcName.get()));
        }
        if (lcdName.isPresent()) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, lcdName.get(), screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, lcdName.get()));
        }
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, screenColor));
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s", XML_NAME_NOEXT));
        return xmlPathList;
    }

    private Optional<File> getXmlFromXmlPathList(List<String> xmlPathList) {
        if (xmlPathList == null) {
            return Optional.empty();
        }
        int listSize = xmlPathList.size();
        for (int i = 0; i < listSize; i++) {
            File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPathList.get(i) + XML_EXT, 2);
            if (xmlFile != null) {
                Optional<File> xmlFileForABTest = getAbTestXmlFile(xmlPathList.get(i));
                return xmlFileForABTest.isPresent() ? xmlFileForABTest : Optional.of(xmlFile);
            }
        }
        return Optional.empty();
    }

    private Optional<File> getAbTestXmlFile(String xmlPath) {
        if (xmlPath == null) {
            return Optional.empty();
        }
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) != 3) {
            return Optional.empty();
        }
        try {
            String serial = Build.getSerial();
            if (serial == null || serial.isEmpty() || "unknown".equals(serial)) {
                Slog.i(TAG, "getAbTestXmlFile, get number failed, skip AB test.");
                return Optional.empty();
            }
            try {
                if ((serial.charAt(serial.length() - 1) & 1) == 1) {
                    Slog.i(TAG, "getAbTestXmlFile, using orginal xml.");
                    return Optional.empty();
                }
                File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath + "_Test" + XML_EXT, 2);
                if (xmlFile == null) {
                    Slog.i(TAG, "getAbTestXmlFile, using orginal xml.");
                } else {
                    Slog.i(TAG, "getAbTestXmlFile, using B test xml!");
                }
                return Optional.ofNullable(xmlFile);
            } catch (IndexOutOfBoundsException e) {
                Slog.i(TAG, "getAbTestXmlFile, IndexOutOfBoundsException, skip AB test");
                return Optional.empty();
            }
        } catch (SecurityException e2) {
            Slog.w(TAG, "SecurityException in getAbTestXmlFile");
            return Optional.empty();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0083, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0084, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0087, code lost:
        throw r5;
     */
    private boolean isLoadConfigedParametersValid() throws IOException {
        Optional<File> xmlFile = getXmlFile();
        if (!xmlFile.isPresent()) {
            return false;
        }
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile.get());
            if (isConfigedParametersFromXmlValid(inputStream)) {
                fillDarkAdaptPointsList();
                if (isConfigLoadedFromXmlValid()) {
                    if (HWFLOW) {
                        printConfigFromXml();
                    }
                    initLinePointsList();
                    if (HWFLOW) {
                        Slog.i(TAG, "mIsBrightnessCalibrationEnabled=" + this.mIsBrightnessCalibrationEnabled + ",mDeviceActualBrightnessNit=" + this.mDeviceActualBrightnessNit + ",mDeviceStandardBrightnessNit=" + this.mDeviceStandardBrightnessNit);
                    }
                    if (this.mIsBrightnessCalibrationEnabled) {
                        updateBrightnessCalibration(this.mDefaultBrightnessLinePointsList, this.mDeviceActualBrightnessNit, this.mDeviceStandardBrightnessNit);
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
            Slog.e(TAG, "isLoadConfigedParametersValid : FileNotFoundException");
        } catch (NoSuchElementException e2) {
            Slog.e(TAG, "isLoadConfigedParametersValid : NoSuchElementException");
        }
    }

    private Optional<File> getXmlFile() {
        Optional<File> xmlFile;
        String currentMode = SystemProperties.get("ro.runmode");
        Slog.i(TAG, "currentMode=" + currentMode);
        Optional.empty();
        if (currentMode == null) {
            xmlFile = getNormalXmlFile();
            if (!xmlFile.isPresent()) {
                return Optional.empty();
            }
        } else if ("factory".equals(currentMode)) {
            xmlFile = getFactoryXmlFile();
            if (!xmlFile.isPresent()) {
                return Optional.empty();
            }
        } else if ("normal".equals(currentMode)) {
            xmlFile = getNormalXmlFile();
            if (!xmlFile.isPresent()) {
                return Optional.empty();
            }
        } else {
            xmlFile = getNormalXmlFile();
            if (!xmlFile.isPresent()) {
                return Optional.empty();
            }
        }
        String xmlCanonicalPath = null;
        try {
            xmlCanonicalPath = xmlFile.get().getCanonicalPath();
        } catch (IOException e) {
            Slog.e(TAG, "get xmlCanonicalPath error IOException!");
        } catch (NoSuchElementException e2) {
            Slog.e(TAG, "get xmlCanonicalPath error NoSuchElementException!");
        }
        if (HWDEBUG) {
            Slog.i(TAG, "get xmlCanonicalPath=" + xmlCanonicalPath);
        }
        return xmlFile;
    }

    private boolean isConfigLoadedFromXmlValid() {
        float f = this.mDefaultBrightness;
        if (f < MIN_DEFAULT_BRIGHTNESS || f > MAX_DEFAULT_BRIGHTNESS) {
            loadDefaultConfig();
            Slog.e(TAG, "DefaultBrightness LoadDefaultConfig! mDefaultBrightness=" + this.mDefaultBrightness);
            return false;
        } else if (!isLineListParaFromXmlValid() || !isBrightnessOffsetParaFromXmlValid() || !isOminLevelParaFromXmlValid() || !isVehicleModeParaFromXmlValid() || !isDayModeParaFromXmlValid()) {
            return false;
        } else {
            float f2 = this.mGameModeBrightnessFloor;
            if (f2 < MIN_DEFAULT_BRIGHTNESS || f2 > MAX_DEFAULT_BRIGHTNESS) {
                loadDefaultConfig();
                Slog.e(TAG, "GameModeBrightnessFloor LoadDefaultConfig! mGameModeBrightnessFloor=" + this.mGameModeBrightnessFloor);
                return false;
            }
            int i = this.mManualBrightnessMaxLimit;
            if (((float) i) < MIN_DEFAULT_BRIGHTNESS || ((float) i) > MAX_DEFAULT_BRIGHTNESS) {
                loadDefaultConfig();
                Slog.e(TAG, "ManualBrightnessMaxLimit LoadDefaultConfig! mManualBrightnessMaxLimit=" + this.mManualBrightnessMaxLimit);
                return false;
            } else if (!HWFLOW) {
                return true;
            } else {
                Slog.i(TAG, "isConfigLoadedFromXmlValid success!");
                return true;
            }
        }
    }

    private boolean isBrightnessOffsetParaFromXmlValid() {
        if (this.mOffsetBrightenRatioLeft > 0.0f) {
            float f = this.mOffsetBrightenAlphaLeft;
            if (f >= 0.0f && f <= 1.0f) {
                float f2 = this.mOffsetBrightenAlphaRight;
                if (f2 < 0.0f || f2 > 1.0f) {
                    loadDefaultConfig();
                    Slog.e(TAG, "LoadXML false, mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
                    return false;
                }
                float f3 = this.mOffsetDarkenAlphaLeft;
                if (f3 >= 0.0f && f3 <= 1.0f) {
                    return true;
                }
                loadDefaultConfig();
                Slog.e(TAG, "LoadXML false, mOffsetDarkenAlphaLeft=" + this.mOffsetDarkenAlphaLeft);
                return false;
            }
        }
        loadDefaultConfig();
        Slog.e(TAG, "LoadXML false, mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
        return false;
    }

    private boolean isOminLevelParaFromXmlValid() {
        if (!this.mIsOminLevelModeEnable) {
            return true;
        }
        if (((float) this.mOminLevelCountValidLuxTh) < 0.0f) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOminLevelCountValidLuxTh=" + this.mOminLevelCountValidLuxTh);
            return false;
        } else if (this.mOminLevelCountValidTimeTh < 0) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false, mOminLevelCountValidTimeTh=" + this.mOminLevelCountValidTimeTh);
            return false;
        } else if (isBrightnessPointsListValid(this.mOminLevelCountLevelPointsList)) {
            return true;
        } else {
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mOminLevelPointsList is wrong, LoadDefaultConfig!");
            return false;
        }
    }

    private boolean isLineListParaFromXmlValid() {
        if (!isBrightnessPointsListValid(this.mDefaultBrightnessLinePointsList)) {
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mDefaultBrightnessLinePointsList is wrong, LoadDefaultConfig!");
            return false;
        }
        List<PointF> list = this.mDarkAdaptingBrightnessPointsList;
        if (list == null || isBrightnessPointsListValid(list)) {
            List<PointF> list2 = this.mDarkAdaptedBrightnessPointsList;
            if (list2 == null || isBrightnessPointsListValid(list2)) {
                return true;
            }
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mDarkAdaptedBrightnessPointsList is wrong, LoadDefaultConfig!");
            return false;
        }
        loadDefaultConfig();
        Slog.e(TAG, "checkPointsList mDarkAdaptingBrightnessPointsList is wrong, LoadDefaultConfig!");
        return false;
    }

    private boolean isVehicleModeParaFromXmlValid() {
        float f = this.mVehicleModeBrightness;
        if (f >= MIN_DEFAULT_BRIGHTNESS && f <= MAX_DEFAULT_BRIGHTNESS && this.mVehicleModeLuxThreshold >= 0.0f) {
            return true;
        }
        loadDefaultConfig();
        Slog.e(TAG, "VeBrightMode LoadDefaultConfig! mVeModeBrightness=" + this.mVehicleModeBrightness + ",mVeModeLuxThreshold=" + this.mVehicleModeLuxThreshold);
        return false;
    }

    private boolean isDayModeParaFromXmlValid() {
        float f = this.mDayModeMinimumBrightness;
        if (f < MIN_DEFAULT_BRIGHTNESS || f > MAX_DEFAULT_BRIGHTNESS) {
            loadDefaultConfig();
            Slog.e(TAG, "DayModeMinimumBrightness LoadDefaultConfig! mDayModeMinimumBrightness=" + this.mDayModeMinimumBrightness);
            return false;
        } else if (this.mDayModeModifyNumPoint > 0) {
            return true;
        } else {
            loadDefaultConfig();
            Slog.e(TAG, "mDayModeModifyNumPoint LoadDefaultConfig! mDayModeModifyNumPoint=" + this.mDayModeModifyNumPoint);
            return false;
        }
    }

    private void initLinePointsList() {
        List<PointF> list = this.mDefaultBrightnessLinePointsList;
        if (list == null) {
            Slog.w(TAG, "initLinePointsList false for mDefaultBrightnessLinePointsList == null");
            return;
        }
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            PointF curPoint = new PointF();
            curPoint.x = this.mDefaultBrightnessLinePointsList.get(i).x;
            curPoint.y = this.mDefaultBrightnessLinePointsList.get(i).y;
            if (this.mDefaultBrightnessLinePointsListCaliBefore == null) {
                this.mDefaultBrightnessLinePointsListCaliBefore = new ArrayList();
            }
            this.mDefaultBrightnessLinePointsListCaliBefore.add(curPoint);
        }
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver != null) {
            Settings.System.putIntForUser(contentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        }
        if (HWFLOW) {
            Slog.i(TAG, "init list_DefaultBrightnessLinePointsBeforeCali");
        }
    }

    private void updateBrightnessCalibration(List<PointF> linePointsListIn, int actualBrightnessNit, int standardBrightnessNit) {
        if (linePointsListIn == null) {
            this.mCalibrationRatio = 1.0f;
            Slog.w(TAG, "updateBrightnessCalibration false for linePointsList == null");
            return;
        }
        if (actualBrightnessNit < VALID_BRIGHTNESS_MIN_NIT || actualBrightnessNit > 1000 || standardBrightnessNit > 1000 || standardBrightnessNit <= 0) {
            this.mCalibrationRatio = 1.0f;
            Slog.e(TAG, "error input brightnessNit,standardBrightnessNit=" + standardBrightnessNit + ",actualBrightnessNit=" + actualBrightnessNit);
        } else {
            this.mCalibrationRatio = ((float) standardBrightnessNit) / ((float) actualBrightnessNit);
            if (HWFLOW) {
                Slog.i(TAG, "mCalibrationRatio=" + this.mCalibrationRatio + ",standardBrightnessNit=" + standardBrightnessNit + ",actualBrightnessNit=" + actualBrightnessNit);
            }
        }
        int listSize = linePointsListIn.size();
        for (int i = 1; i < listSize; i++) {
            PointF curPoint = linePointsListIn.get(i);
            if (curPoint.y > MIN_DEFAULT_BRIGHTNESS && curPoint.y < MAX_DEFAULT_BRIGHTNESS) {
                curPoint.y *= this.mCalibrationRatio;
                if (curPoint.y <= MIN_DEFAULT_BRIGHTNESS) {
                    curPoint.y = MIN_DEFAULT_BRIGHTNESS;
                }
                if (curPoint.y >= MAX_DEFAULT_BRIGHTNESS) {
                    curPoint.y = MAX_DEFAULT_BRIGHTNESS;
                }
            }
        }
        for (PointF currentPoint : linePointsListIn) {
            if (HWFLOW) {
                Slog.i(TAG, "LoadXMLConfig_NewCalibrationBrightnessLinePoints x=" + currentPoint.x + ",y=" + currentPoint.y);
            }
        }
    }

    private void updateLinePointsListForCalibration() {
        List<PointF> list;
        List<PointF> list2;
        List<PointF> list3;
        if (this.mIsBrightnessCalibrationEnabled && Math.abs(this.mCalibrationRatio - 1.0f) >= SMALL_VALUE) {
            if (this.mIsPowerSavingBrightnessLineEnable && (list2 = this.mPowerSavingBrightnessLinePointsList) != null) {
                updateNewLinePointsListForCalibration(list2);
                Slog.i(TAG, "update PowerSavingBrightnessLinePointsList for calibration");
                if (HWFLOW && (list3 = this.mPowerSavingBrightnessLinePointsList) != null) {
                    for (PointF curPoint : list3) {
                        Slog.i(TAG, "LoadXMLConfig_NewCalibrationPowerSavingPointsList x=" + curPoint.x + ", y=" + curPoint.y);
                    }
                }
            }
            List<PointF> list4 = this.mCameraBrightnessLinePointsList;
            if (list4 != null) {
                updateNewLinePointsListForCalibration(list4);
                if (HWFLOW) {
                    Slog.i(TAG, "update mCameraBrightnessLinePointsList for calibration");
                }
            }
            List<PointF> list5 = this.mGameModeBrightnessLinePointsList;
            if (list5 != null) {
                updateNewLinePointsListForCalibration(list5);
                if (HWFLOW) {
                    Slog.i(TAG, "update mGameModeBrightnessLinePointsList for calibration");
                }
                if (HWFLOW && (list = this.mGameModeBrightnessLinePointsList) != null) {
                    for (PointF curPoint2 : list) {
                        Slog.i(TAG, "LoadXMLConfig_GameModeBrightnessLinePointsList x=" + curPoint2.x + ", y=" + curPoint2.y);
                    }
                }
            }
            List<PointF> list6 = this.mReadingBrightnessLinePointsList;
            if (list6 != null) {
                updateNewLinePointsListForCalibration(list6);
                if (HWFLOW) {
                    Slog.i(TAG, "update mReadingBrightnessLinePointsList for calibration");
                    for (PointF curPoint3 : this.mReadingBrightnessLinePointsList) {
                        Slog.i(TAG, "LoadXMLConfig_ReadingModeBrightnessLinePointsList x=" + curPoint3.x + ",y=" + curPoint3.y);
                    }
                }
            }
        }
    }

    private void updateNewLinePointsListForCalibration(List<PointF> linePointsListIn) {
        if (linePointsListIn == null) {
            Slog.e(TAG, "updateNewLinePointsListForCalibration false for linePointsList == null");
            return;
        }
        int listSize = linePointsListIn.size();
        for (int i = 1; i < listSize; i++) {
            PointF curPoint = linePointsListIn.get(i);
            if (curPoint.y > MIN_DEFAULT_BRIGHTNESS && curPoint.y < MAX_DEFAULT_BRIGHTNESS) {
                curPoint.y *= this.mCalibrationRatio;
                if (curPoint.y <= MIN_DEFAULT_BRIGHTNESS) {
                    curPoint.y = MIN_DEFAULT_BRIGHTNESS;
                }
                if (curPoint.y >= MAX_DEFAULT_BRIGHTNESS) {
                    curPoint.y = MAX_DEFAULT_BRIGHTNESS;
                }
            }
        }
    }

    private boolean isBrightnessPointsListValid(List<PointF> linePointsListIn) {
        if (linePointsListIn == null) {
            Slog.e(TAG, "LoadXML false for linePointsList == null");
            return false;
        }
        int listSize = linePointsListIn.size();
        if (listSize <= 2 || listSize >= 100) {
            Slog.e(TAG, "LoadXML false for linePointsList number is wrong,number=" + listSize);
            return false;
        }
        PointF lastPoint = null;
        for (PointF curPoint : linePointsListIn) {
            if (lastPoint == null) {
                lastPoint = curPoint;
            } else if (lastPoint.x >= curPoint.x) {
                loadDefaultConfig();
                Slog.e(TAG, "LoadXML false for linePointsList is wrong");
                return false;
            } else {
                lastPoint = curPoint;
            }
        }
        return true;
    }

    private boolean isDayModeBrightnessValid() {
        List<PointF> list = this.mDefaultBrightnessLinePointsList;
        if (list == null) {
            Slog.e(TAG, "mDefaultBrightnessLinePointsList==null");
            return false;
        }
        int size = list.size();
        int i = this.mDayModeModifyNumPoint;
        if (size < i) {
            Slog.e(TAG, "mDefaultBrightnessLinePointsList.size < mDayModeModifyNumPoint");
            return false;
        } else if (i <= 0) {
            Slog.e(TAG, "mDayModeModifyNumPoint<=0");
            return false;
        } else if (this.mDefaultBrightnessLinePointsList.get(i - 1).y >= ((float) this.mDayModeModifyMinBrightness)) {
            return true;
        } else {
            Slog.e(TAG, "curPoint.y < mDayModeModifyMinBrightness");
            return false;
        }
    }

    private void getDayBrightnessLinePoints() {
        if (this.mDefaultBrightnessLinePointsList != null) {
            float brightParaAlpha = 1.0f;
            float brightParaBeta = 0.0f;
            if (isDayModeBrightnessValid()) {
                PointF curPoint = this.mDefaultBrightnessLinePointsList.get(this.mDayModeModifyNumPoint - 1);
                if (Math.abs((curPoint.y * 1.0f) - MIN_DEFAULT_BRIGHTNESS) < SMALL_VALUE) {
                    brightParaAlpha = 1.0f;
                    brightParaBeta = 0.0f;
                    Slog.e(TAG, "error DayBrightnessLinePoints input! brightness=4.0");
                } else {
                    brightParaAlpha = ((curPoint.y * 1.0f) - (((float) this.mDayModeModifyMinBrightness) * 1.0f)) / ((curPoint.y * 1.0f) - MIN_DEFAULT_BRIGHTNESS);
                    brightParaBeta = (curPoint.y * ((((float) this.mDayModeModifyMinBrightness) * 1.0f) - MIN_DEFAULT_BRIGHTNESS)) / ((curPoint.y * 1.0f) - MIN_DEFAULT_BRIGHTNESS);
                    Slog.i(TAG, "DayMode:brightParaAlpha=" + brightParaAlpha + ",brightParaBeta=" + brightParaBeta);
                }
            }
            List<PointF> list = this.mDayBrightnessLinePointsList;
            if (list == null) {
                this.mDayBrightnessLinePointsList = new ArrayList();
            } else {
                list.clear();
            }
            int cntPoint = 0;
            for (PointF curPoint2 : this.mDefaultBrightnessLinePointsList) {
                cntPoint++;
                if (cntPoint > this.mDayModeModifyNumPoint) {
                    this.mDayBrightnessLinePointsList.add(curPoint2);
                } else {
                    this.mDayBrightnessLinePointsList.add(new PointF(curPoint2.x, (curPoint2.y * brightParaAlpha) + brightParaBeta));
                }
            }
            for (PointF curPoint3 : this.mDayBrightnessLinePointsList) {
                if (HWFLOW) {
                    Slog.i(TAG, "DayMode:DayBrightnessLine,x=" + curPoint3.x + ",y=" + curPoint3.y);
                }
            }
        }
    }

    private void getOminLevelBrightnessLinePoints() {
        List<PointF> list = this.mOminLevelBrightnessLinePointsList;
        if (list == null) {
            this.mOminLevelBrightnessLinePointsList = new ArrayList();
        } else {
            list.clear();
        }
        List<PointF> list2 = this.mDayBrightnessLinePointsList;
        if (list2 != null) {
            for (PointF curPoint : list2) {
                this.mOminLevelBrightnessLinePointsList.add(curPoint);
                if (HWFLOW) {
                    Slog.i(TAG, "mOminLevelMode:LinePointsList,x=" + curPoint.x + ",y=" + curPoint.y);
                }
            }
            updateOminLevelBrightnessLinePoints();
            return;
        }
        Slog.w(TAG, "mOminLevelMode getLineFailed, mDayBrightnessLinePointsList==null");
    }

    private void updateOminLevelBrightnessLinePoints() {
        List<PointF> list = this.mOminLevelBrightnessLinePointsList;
        if (list == null) {
            Slog.w(TAG, "mOminLevelMode mOminLevelBrightnessLinePointsList==null,return");
            return;
        }
        int listSize = list.size();
        int countThMin = getOminLevelCountThMin(this.mOminLevelCountLevelPointsList);
        int countThMax = getOminLevelCountThMax(this.mOminLevelCountLevelPointsList);
        if (listSize >= 2) {
            PointF prePoint = this.mOminLevelBrightnessLinePointsList.get(0);
            PointF nexPoint = this.mOminLevelBrightnessLinePointsList.get(1);
            int i = this.mOminLevelCount;
            if (i >= countThMin) {
                prePoint.y = getOminLevelFromCount(i);
                this.mIsOminLevelCountEnable = true;
            } else {
                prePoint.y = getOminLevelThMin(this.mOminLevelCountLevelPointsList);
                this.mIsOminLevelCountEnable = false;
            }
            if (prePoint.y > nexPoint.y) {
                prePoint.y = nexPoint.y;
                Slog.w(TAG, "mOminLevelMode updateMinLevel x(0)=" + prePoint.x + ",y(0)=" + prePoint.y + ",y(0)==y(1)");
            }
            if (HWFLOW && this.mIsOminLevelCountEnable && this.mOminLevelCount < countThMax) {
                Slog.i(TAG, "mOminLevelMode updateMinLevel x(0)=" + prePoint.x + ",y(0)=" + prePoint.y);
                return;
            }
            return;
        }
        Slog.w(TAG, "mOminLevelMode mOminLevelBrightnessLinePointsList==null");
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
        float brightnessLevel = MIN_DEFAULT_BRIGHTNESS;
        if (linePointsList == null) {
            Slog.i(TAG, "mOminLevelMode linePointsListIn==null,return MIN_DEFAULT_BRIGHTNESS");
            return MIN_DEFAULT_BRIGHTNESS;
        }
        PointF prePoint = null;
        for (PointF curPoint : linePointsList) {
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (levelCount < curPoint.x) {
                brightnessLevel = prePoint.y;
            } else {
                prePoint = curPoint;
            }
        }
        return brightnessLevel;
    }

    private void loadDefaultConfig() {
        this.mDefaultBrightness = 100.0f;
        this.mIsBrightnessCalibrationEnabled = false;
        List<PointF> list = this.mDefaultBrightnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mDefaultBrightnessLinePointsList = new ArrayList();
        }
        this.mDefaultBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
        this.mDefaultBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mDefaultBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mDefaultBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
        this.mDefaultBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
        loadCameraDefaultBrightnessLine();
        loadPowerSavingDefaultBrightnessLine();
        loadOminLevelCountLevelPointsList();
        this.mIsOminLevelModeEnable = false;
        this.mDarkAdaptingBrightnessPointsList = null;
        this.mDarkAdaptingBrightness0LuxLevel = 0;
        this.mDarkAdaptedBrightnessPointsList = null;
        this.mDarkAdaptedBrightness0LuxLevel = 0;
        this.mIsDarkAdaptEnable = false;
        this.mDayModeModifyNumPoint = 3;
        this.mDayModeMinimumBrightness = MIN_DEFAULT_BRIGHTNESS;
        this.mIsPersonalizedBrightnessEnable = false;
        this.mIsPersonalizedBrightnessCurveLoadEnable = false;
        if (HWFLOW) {
            printConfigFromXml();
        }
    }

    private void loadCameraDefaultBrightnessLine() {
        List<PointF> list = this.mCameraBrightnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mCameraBrightnessLinePointsList = new ArrayList();
        }
        this.mCameraBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
        this.mCameraBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mCameraBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mCameraBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
        this.mCameraBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
    }

    private void loadReadingDefaultBrightnessLine() {
        List<PointF> list = this.mReadingBrightnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mReadingBrightnessLinePointsList = new ArrayList();
        }
        this.mReadingBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
        this.mReadingBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mReadingBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mReadingBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
        this.mReadingBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
    }

    private void loadGameModeDefaultBrightnessLine() {
        List<PointF> list = this.mGameModeBrightnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mGameModeBrightnessLinePointsList = new ArrayList();
        }
        this.mGameModeBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
        this.mGameModeBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mGameModeBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mGameModeBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
        this.mGameModeBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
    }

    private void loadPowerSavingDefaultBrightnessLine() {
        List<PointF> list = this.mPowerSavingBrightnessLinePointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mPowerSavingBrightnessLinePointsList = new ArrayList();
        }
        this.mPowerSavingBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
        this.mPowerSavingBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
        this.mPowerSavingBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
        this.mPowerSavingBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
        this.mPowerSavingBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
    }

    private void loadOminLevelCountLevelPointsList() {
        List<PointF> list = this.mOminLevelCountLevelPointsList;
        if (list != null) {
            list.clear();
        } else {
            this.mOminLevelCountLevelPointsList = new ArrayList();
        }
        this.mOminLevelCountLevelPointsList.add(new PointF(5.0f, 6.0f));
        this.mOminLevelCountLevelPointsList.add(new PointF(GAIN_FACTOR, 7.0f));
        this.mOminLevelCountLevelPointsList.add(new PointF(20.0f, 8.0f));
    }

    private void printConfigFromXml() {
        List<PointF> list;
        Slog.i(TAG, "LoadXMLConfig_DefaultBrightness=" + this.mDefaultBrightness);
        Slog.i(TAG, "LoadXMLConfig_mIsBrightnessCalibrationEnabled=" + this.mIsBrightnessCalibrationEnabled + ",mIsPowerSavingBrightnessLineEnable=" + this.mIsPowerSavingBrightnessLineEnable + ",mOffsetBrightenRatioLeft=" + this.mOffsetBrightenRatioLeft + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight + ",mOffsetDarkenAlphaLeft=" + this.mOffsetDarkenAlphaLeft + ",mIsManualMode=" + this.mIsManualMode + ",mManualBrightnessMaxLimit=" + this.mManualBrightnessMaxLimit + ",mIsPersonalizedBrightnessEnable=" + this.mIsPersonalizedBrightnessEnable + ",mIsVeModeEnable=" + this.mIsVehicleModeEnable + ",mVeModeBrightness=" + this.mVehicleModeBrightness + ",mVeLuxThreshold=" + this.mVehicleModeLuxThreshold + ",mIsGameModeEnable=" + this.mIsGameModeEnable + ",mIsPersonalizedBrightnessCurveLoadEnable=" + this.mIsPersonalizedBrightnessCurveLoadEnable);
        StringBuilder sb = new StringBuilder();
        sb.append("LoadXMLConfig_mIsGameModeBrightnessLimitationEnable=");
        sb.append(this.mIsGameModeBrightnessLimitationEnable);
        sb.append(",mGameModeBrightnessFloor=");
        sb.append(this.mGameModeBrightnessFloor);
        Slog.i(TAG, sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("LoadXMLConfig_mOminLevelMode=");
        sb2.append(this.mIsOminLevelModeEnable);
        sb2.append(",mCountEnable=");
        sb2.append(this.mIsOminLevelOffsetCountEnable);
        sb2.append(",mDayEn=");
        sb2.append(this.mIsOminLevelDayModeEnable);
        sb2.append(",ValidLux=");
        sb2.append(this.mOminLevelCountValidLuxTh);
        sb2.append(",ValidTime=");
        sb2.append(this.mOminLevelCountValidTimeTh);
        sb2.append(",mLongTime=");
        sb2.append(this.mOminLevelCountResetLongTimeTh);
        sb2.append(",EyeEn=");
        sb2.append(this.mIsEyeProtectionSplineEnable);
        Slog.i(TAG, sb2.toString());
        for (PointF curPoint : this.mDefaultBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_DefaultBrightnessLinePoints x=" + curPoint.x + ",y=" + curPoint.y);
        }
        for (PointF curPoint2 : this.mCameraBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_CameraBrightnessLinePointsList x=" + curPoint2.x + ",y=" + curPoint2.y);
        }
        for (PointF curPoint3 : this.mReadingBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_ReadingBrightnessLinePointsList x=" + curPoint3.x + ",y=" + curPoint3.y);
        }
        for (PointF curPoint4 : this.mGameModeBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_mGameModeBrightnessLinePointsList x=" + curPoint4.x + ",y=" + curPoint4.y);
        }
        for (PointF curPoint5 : this.mPowerSavingBrightnessLinePointsList) {
            Slog.i(TAG, "LoadXMLConfig_mPowerSavingBrightnessLinePointsList x=" + curPoint5.x + ",y=" + curPoint5.y);
        }
        if (this.mIsOminLevelModeEnable && (list = this.mOminLevelCountLevelPointsList) != null) {
            for (PointF curPoint6 : list) {
                Slog.i(TAG, "LoadXMLConfig_mOminLevelCountLevelPointsList x=" + curPoint6.x + ",y=" + curPoint6.y);
            }
        }
        if (this.mIsDarkAdaptEnable) {
            Slog.i(TAG, "LoadXMLConfig_DarkAdaptingBrightness0LuxLevel=" + this.mDarkAdaptingBrightness0LuxLevel + ",Adapted=" + this.mDarkAdaptedBrightness0LuxLevel);
        }
        Slog.i(TAG, "LoadXMLConfig_DayModeMinimumBrightness=" + this.mDayModeMinimumBrightness);
    }

    /* JADX WARNING: Removed duplicated region for block: B:298:0x0813 A[LOOP:0: B:6:0x003a->B:298:0x0813, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x08aa A[EDGE_INSN: B:331:0x08aa->B:312:0x08aa ?: BREAK  , SYNTHETIC] */
    private boolean isConfigedParametersFromXmlValid(InputStream inStream) {
        String name;
        if (HWFLOW) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean isCameraBrightnessLinePointsListsLoadStarted = false;
        boolean isCameraBrightnessLinePointsListLoaded = false;
        String name2 = null;
        boolean isReadingBrightnessLinePointsListLoaded = false;
        boolean isGameModeBrightnessLinePointsListsLoadStarted = false;
        boolean isGameModeBrightnessLinePointsListLoaded = false;
        boolean isPowerSavingBrightnessLinePointsListsLoadStarted = false;
        boolean isPowerSavingBrightnessLinePointsListLoaded = false;
        boolean isOminLevelCountLevelLinePointsListsLoadStarted = false;
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        boolean isDefaultBrightnessLoaded = false;
        XmlPullParser parser = Xml.newPullParser();
        boolean isDefaultBrightnessLinePointsListsLoadStarted = false;
        boolean isDefaultBrightnessLinePointsListLoaded = false;
        try {
            parser.setInput(inStream, "UTF-8");
            int eventType = parser.getEventType();
            while (true) {
                if (eventType == 1) {
                    break;
                }
                if (eventType != 2) {
                    if (eventType != 3) {
                        isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                        isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                        isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                        isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                        isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                        isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                        isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                        name2 = name2;
                    } else {
                        try {
                            name = parser.getName();
                            try {
                                if (XML_NAME_NOEXT.equals(name) && configGroupLoadStarted) {
                                    loadFinished = true;
                                    isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                    isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                    isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                    isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                    isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                    isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                    isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                    name2 = name2;
                                } else if (configGroupLoadStarted) {
                                    if ("DefaultBrightnessPoints".equals(name)) {
                                        try {
                                            if (this.mDefaultBrightnessLinePointsList != null) {
                                                isDefaultBrightnessLinePointsListsLoadStarted = false;
                                                isDefaultBrightnessLinePointsListLoaded = true;
                                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no DefaultBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e2) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e3) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if ("CameraBrightnessPoints".equals(name)) {
                                        isCameraBrightnessLinePointsListsLoadStarted = false;
                                        if (this.mCameraBrightnessLinePointsList != null) {
                                            isCameraBrightnessLinePointsListLoaded = true;
                                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                            name2 = name2;
                                        } else {
                                            Slog.e(TAG, "no CameraBrightnessPoints loaded!");
                                            return false;
                                        }
                                    } else if ("ReadingBrightnessPoints".equals(name)) {
                                        try {
                                            if (this.mReadingBrightnessLinePointsList != null) {
                                                isReadingBrightnessLinePointsListLoaded = true;
                                                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = null;
                                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                            } else {
                                                Slog.e(TAG, "no ReadingBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e4) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e5) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e6) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if ("GameModeBrightnessPoints".equals(name)) {
                                        isGameModeBrightnessLinePointsListsLoadStarted = false;
                                        try {
                                            if (this.mGameModeBrightnessLinePointsList != null) {
                                                isGameModeBrightnessLinePointsListLoaded = true;
                                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no GameModeBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e7) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e8) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e9) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if ("PowerSavingBrightnessPoints".equals(name)) {
                                        isPowerSavingBrightnessLinePointsListsLoadStarted = false;
                                        try {
                                            if (this.mPowerSavingBrightnessLinePointsList != null) {
                                                isPowerSavingBrightnessLinePointsListLoaded = true;
                                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no PowerSavingBrightnessPoints loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e10) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e11) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e12) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    } else if ("OminLevelCountLevelLinePoints".equals(name)) {
                                        isOminLevelCountLevelLinePointsListsLoadStarted = false;
                                        try {
                                            if (this.mOminLevelCountLevelPointsList != null) {
                                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                                name2 = name2;
                                            } else {
                                                Slog.e(TAG, "no OminLevelCountLevelPointsList loaded!");
                                                return false;
                                            }
                                        } catch (XmlPullParserException e13) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (IOException e14) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        } catch (NumberFormatException e15) {
                                            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
                                            Slog.e(TAG, "getConfigFromeXML false!");
                                            return false;
                                        }
                                    }
                                }
                            } catch (XmlPullParserException e16) {
                                Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (IOException e17) {
                                Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            } catch (NumberFormatException e18) {
                                Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
                                Slog.e(TAG, "getConfigFromeXML false!");
                                return false;
                            }
                        } catch (XmlPullParserException e19) {
                            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        } catch (IOException e20) {
                            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
                            Slog.e(TAG, "getConfigFromeXML false!");
                            return false;
                        } catch (NumberFormatException e21) {
                            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
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
                    if (XML_NAME_NOEXT.equals(name)) {
                        if (this.mDeviceActualBrightnessLevel == 0) {
                            if (HWFLOW) {
                                Slog.i(TAG, "actualDeviceLevel = 0, load started");
                            }
                            configGroupLoadStarted = true;
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else {
                            String deviceLevelString = parser.getAttributeValue(null, "level");
                            if (deviceLevelString == null || deviceLevelString.length() == 0) {
                                if (HWFLOW) {
                                    Slog.i(TAG, "actualDeviceLevel=" + this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                                }
                                configGroupLoadStarted = true;
                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                name2 = name2;
                            } else {
                                if (Integer.parseInt(deviceLevelString) == this.mDeviceActualBrightnessLevel) {
                                    if (HWFLOW) {
                                        Slog.i(TAG, "actualDeviceLevel=" + this.mDeviceActualBrightnessLevel + ", find matched level in XML, load start");
                                    }
                                    configGroupLoadStarted = true;
                                }
                                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                                name2 = name2;
                            }
                        }
                    } else if (configGroupLoadStarted) {
                        if ("DefaultBrightness".equals(name)) {
                            this.mDefaultBrightness = Float.parseFloat(parser.nextText());
                            isDefaultBrightnessLoaded = true;
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if ("BrightnessCalibrationEnabled".equals(name)) {
                            this.mIsBrightnessCalibrationEnabled = Boolean.parseBoolean(parser.nextText());
                        } else if ("OffsetBrightenRatioLeft".equals(name)) {
                            this.mOffsetBrightenRatioLeft = Float.parseFloat(parser.nextText());
                        } else if ("OffsetBrightenAlphaLeft".equals(name)) {
                            this.mOffsetBrightenAlphaLeft = Float.parseFloat(parser.nextText());
                        } else if ("OffsetBrightenAlphaRight".equals(name)) {
                            this.mOffsetBrightenAlphaRight = Float.parseFloat(parser.nextText());
                        } else if ("OffsetDarkenAlphaLeft".equals(name)) {
                            this.mOffsetDarkenAlphaLeft = Float.parseFloat(parser.nextText());
                        } else if ("PersonalizedBrightnessCurveEnable".equals(name)) {
                            this.mIsPersonalizedBrightnessEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("PersonalizedBrightnessCurveLoadEnable".equals(name)) {
                            this.mIsPersonalizedBrightnessCurveLoadEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("DayModeMinimumBrightness".equals(name)) {
                            this.mDayModeMinimumBrightness = Float.parseFloat(parser.nextText());
                        } else if ("VehicleModeEnable".equals(name)) {
                            this.mIsVehicleModeEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("VehicleModeBrighntess".equals(name)) {
                            this.mVehicleModeBrightness = Float.parseFloat(parser.nextText());
                        } else if ("VehicleModeLuxThreshold".equals(name)) {
                            this.mVehicleModeLuxThreshold = Float.parseFloat(parser.nextText());
                        } else if ("GameModeEnable".equals(name)) {
                            this.mIsGameModeEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("GameModeBrightnessLimitationEnable".equals(name)) {
                            this.mIsGameModeBrightnessLimitationEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("GameModeBrightnessFloor".equals(name)) {
                            this.mGameModeBrightnessFloor = Float.parseFloat(parser.nextText());
                        } else if ("DefaultBrightnessPoints".equals(name)) {
                            isDefaultBrightnessLinePointsListsLoadStarted = true;
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if ("Point".equals(name) && isDefaultBrightnessLinePointsListsLoadStarted) {
                            PointF currentPoint = new PointF();
                            String brightnessPoint = parser.nextText();
                            currentPoint.x = Float.parseFloat(brightnessPoint.split(",")[0]);
                            currentPoint.y = Float.parseFloat(brightnessPoint.split(",")[1]);
                            if (this.mDefaultBrightnessLinePointsList == null) {
                                this.mDefaultBrightnessLinePointsList = new ArrayList();
                            }
                            this.mDefaultBrightnessLinePointsList.add(currentPoint);
                        } else if ("CameraBrightnessPoints".equals(name)) {
                            isCameraBrightnessLinePointsListsLoadStarted = true;
                            if (this.mCameraBrightnessLinePointsList != null) {
                                this.mCameraBrightnessLinePointsList.clear();
                            }
                        } else if ("Point".equals(name) && isCameraBrightnessLinePointsListsLoadStarted) {
                            PointF currentPoint2 = new PointF();
                            String brightnessPoint2 = parser.nextText();
                            currentPoint2.x = Float.parseFloat(brightnessPoint2.split(",")[0]);
                            currentPoint2.y = Float.parseFloat(brightnessPoint2.split(",")[1]);
                            if (this.mCameraBrightnessLinePointsList == null) {
                                this.mCameraBrightnessLinePointsList = new ArrayList();
                            }
                            this.mCameraBrightnessLinePointsList.add(currentPoint2);
                        } else if ("ReadingBrightnessPoints".equals(name)) {
                            if (this.mReadingBrightnessLinePointsList != null) {
                                this.mReadingBrightnessLinePointsList.clear();
                            }
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                            name2 = 1;
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                        } else if ("Point".equals(name) && name2 != null) {
                            PointF currentPoint3 = new PointF();
                            String brightnessPoint3 = parser.nextText();
                            currentPoint3.x = Float.parseFloat(brightnessPoint3.split(",")[0]);
                            currentPoint3.y = Float.parseFloat(brightnessPoint3.split(",")[1]);
                            if (this.mReadingBrightnessLinePointsList == null) {
                                this.mReadingBrightnessLinePointsList = new ArrayList();
                            }
                            this.mReadingBrightnessLinePointsList.add(currentPoint3);
                        } else if ("GameModeBrightnessPoints".equals(name)) {
                            isGameModeBrightnessLinePointsListsLoadStarted = true;
                            if (this.mGameModeBrightnessLinePointsList != null) {
                                this.mGameModeBrightnessLinePointsList.clear();
                            }
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if ("Point".equals(name) && isGameModeBrightnessLinePointsListsLoadStarted) {
                            PointF currentPoint4 = new PointF();
                            String brightnessPoint4 = parser.nextText();
                            currentPoint4.x = Float.parseFloat(brightnessPoint4.split(",")[0]);
                            currentPoint4.y = Float.parseFloat(brightnessPoint4.split(",")[1]);
                            if (this.mGameModeBrightnessLinePointsList == null) {
                                this.mGameModeBrightnessLinePointsList = new ArrayList();
                            }
                            this.mGameModeBrightnessLinePointsList.add(currentPoint4);
                        } else if ("PowerSavingBrightnessPoints".equals(name)) {
                            isPowerSavingBrightnessLinePointsListsLoadStarted = true;
                            if (this.mPowerSavingBrightnessLinePointsList != null) {
                                this.mPowerSavingBrightnessLinePointsList.clear();
                            }
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if ("Point".equals(name) && isPowerSavingBrightnessLinePointsListsLoadStarted) {
                            PointF currentPoint5 = new PointF();
                            String brightnessPoint5 = parser.nextText();
                            currentPoint5.x = Float.parseFloat(brightnessPoint5.split(",")[0]);
                            currentPoint5.y = Float.parseFloat(brightnessPoint5.split(",")[1]);
                            if (this.mPowerSavingBrightnessLinePointsList == null) {
                                this.mPowerSavingBrightnessLinePointsList = new ArrayList();
                            }
                            this.mPowerSavingBrightnessLinePointsList.add(currentPoint5);
                        } else if ("PowerSavingBrighnessLineEnable".equals(name)) {
                            this.mIsPowerSavingBrightnessLineEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("ManualMode".equals(name)) {
                            if (Integer.parseInt(parser.nextText()) == 1) {
                                this.mIsManualMode = true;
                            }
                        } else if ("ManualBrightnessMaxLimit".equals(name)) {
                            if (this.mIsManualMode) {
                                this.mManualBrightnessMaxLimit = Integer.parseInt(parser.nextText());
                            }
                        } else if ("DayModeAlgoEnable".equals(name)) {
                            this.mIsDayModeAlgoEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("DayModeModifyNumPoint".equals(name)) {
                            this.mDayModeModifyNumPoint = Integer.parseInt(parser.nextText());
                        } else if ("DayModeModifyMinBrightness".equals(name)) {
                            this.mDayModeModifyMinBrightness = Integer.parseInt(parser.nextText());
                        } else if ("OminLevelModeEnable".equals(name)) {
                            this.mIsOminLevelModeEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("OminLevelOffsetCountEnable".equals(name)) {
                            this.mIsOminLevelOffsetCountEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("OminLevelDayModeEnable".equals(name)) {
                            this.mIsOminLevelDayModeEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("OminLevelCountValidLuxTh".equals(name)) {
                            this.mOminLevelCountValidLuxTh = Integer.parseInt(parser.nextText());
                        } else if ("OminLevelCountValidTimeTh".equals(name)) {
                            this.mOminLevelCountValidTimeTh = Integer.parseInt(parser.nextText());
                        } else if ("OminLevelCountResetLongTimeTh".equals(name)) {
                            this.mOminLevelCountResetLongTimeTh = Integer.parseInt(parser.nextText());
                        } else if ("EyeProtectionSplineEnable".equals(name)) {
                            this.mIsEyeProtectionSplineEnable = Boolean.parseBoolean(parser.nextText());
                        } else if ("OminLevelCountLevelLinePoints".equals(name)) {
                            isOminLevelCountLevelLinePointsListsLoadStarted = true;
                            if (this.mOminLevelCountLevelPointsList != null) {
                                this.mOminLevelCountLevelPointsList.clear();
                            }
                            isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                            isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                            isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                            isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                            isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                            name2 = name2;
                        } else if ("Point".equals(name) && isOminLevelCountLevelLinePointsListsLoadStarted) {
                            PointF currentPoint6 = new PointF();
                            String brightnessPoint6 = parser.nextText();
                            currentPoint6.x = Float.parseFloat(brightnessPoint6.split(",")[0]);
                            currentPoint6.y = Float.parseFloat(brightnessPoint6.split(",")[1]);
                            if (this.mOminLevelCountLevelPointsList == null) {
                                this.mOminLevelCountLevelPointsList = new ArrayList();
                            }
                            this.mOminLevelCountLevelPointsList.add(currentPoint6);
                        } else if ("AdaptingBrightness0LuxLevel".equals(name)) {
                            this.mDarkAdaptingBrightness0LuxLevel = Integer.parseInt(parser.nextText());
                        } else if ("AdaptedBrightness0LuxLevel".equals(name)) {
                            this.mDarkAdaptedBrightness0LuxLevel = Integer.parseInt(parser.nextText());
                        }
                    }
                    if (loadFinished) {
                    }
                }
                isCameraBrightnessLinePointsListLoaded = isCameraBrightnessLinePointsListLoaded;
                isReadingBrightnessLinePointsListLoaded = isReadingBrightnessLinePointsListLoaded;
                isGameModeBrightnessLinePointsListLoaded = isGameModeBrightnessLinePointsListLoaded;
                isPowerSavingBrightnessLinePointsListLoaded = isPowerSavingBrightnessLinePointsListLoaded;
                isOminLevelCountLevelLinePointsListsLoadStarted = isOminLevelCountLevelLinePointsListsLoadStarted;
                isPowerSavingBrightnessLinePointsListsLoadStarted = isPowerSavingBrightnessLinePointsListsLoadStarted;
                isGameModeBrightnessLinePointsListsLoadStarted = isGameModeBrightnessLinePointsListsLoadStarted;
                name2 = name2;
                if (loadFinished) {
                }
            }
            if (!isDefaultBrightnessLoaded || !isDefaultBrightnessLinePointsListLoaded) {
                if (!configGroupLoadStarted) {
                    Slog.e(TAG, "actualDeviceLevel=" + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                    return false;
                }
                Slog.e(TAG, "getConfigFromeXML false!");
                return false;
            } else if (!HWFLOW) {
                return true;
            } else {
                Slog.i(TAG, "getConfigFromeXML success!");
                return true;
            }
        } catch (XmlPullParserException e22) {
            Slog.e(TAG, "isConfigedParametersFromXmlValid : XmlPullParserException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        } catch (IOException e23) {
            Slog.e(TAG, "isConfigedParametersFromXmlValid : IOException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        } catch (NumberFormatException e24) {
            Slog.e(TAG, "isConfigedParametersFromXmlValid : NumberFormatException");
            Slog.e(TAG, "getConfigFromeXML false!");
            return false;
        }
    }

    public void updateCurrentUserId(int userId) {
        if (HWFLOW) {
            Slog.i(TAG, "save old user's paras and load new user's paras when user change ");
        }
        saveOffsetParas();
        this.mCurrentUserId = userId;
        loadOffsetParas();
        unlockDarkAdaptLine();
    }

    private void loadOffsetParas() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "loadOffsetParas failed, mContentResolver==null");
            return;
        }
        this.mPosBrightnessSaved = Settings.System.getFloatForUser(contentResolver, "hw_screen_auto_brightness_adj", 0.0f, this.mCurrentUserId) * MAX_DEFAULT_BRIGHTNESS;
        this.mPosBrightness = this.mPosBrightnessSaved;
        this.mDeltaSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_delta", 0.0f, this.mCurrentUserId);
        this.mDeltaNew = this.mDeltaSaved;
        boolean z = true;
        if (Settings.System.getIntForUser(this.mContentResolver, "spline_is_user_change", 0, this.mCurrentUserId) != 1) {
            z = false;
        }
        this.mIsUserChangeSaved = z;
        this.mIsUserChange = this.mIsUserChangeSaved;
        this.mOffsetBrightnessLastSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_offset_brightness_last", MIN_DEFAULT_BRIGHTNESS, this.mCurrentUserId);
        this.mOffsetBrightnessLast = this.mOffsetBrightnessLastSaved;
        this.mLastLuxDefaultBrightnessSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", MIN_DEFAULT_BRIGHTNESS, this.mCurrentUserId);
        this.mLastLuxDefaultBrightness = this.mLastLuxDefaultBrightnessSaved;
        this.mStartLuxDefaultBrightnessSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", MIN_DEFAULT_BRIGHTNESS, this.mCurrentUserId);
        this.mStartLuxDefaultBrightness = this.mStartLuxDefaultBrightnessSaved;
        this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        this.mAmLuxOffset = Settings.System.getFloatForUser(this.mContentResolver, "spline_ambient_lux_offset", DEFAULT_NO_OFFSET_LUX, this.mCurrentUserId);
        loadOminLevelParas();
        if (this.mIsManualMode) {
            float f = this.mStartLuxDefaultBrightness;
            int i = this.mManualBrightnessMaxLimit;
            if (f >= ((float) i) && this.mPosBrightness == ((float) i)) {
                this.mDelta = 0.0f;
                this.mDeltaNew = 0.0f;
                Slog.i(TAG, "updateLevel outdoor no offset set mDelta=0");
            }
        }
        loadTwoPointOffsetParameters();
        if (HWFLOW) {
            Slog.i(TAG, "Read:userId=" + this.mCurrentUserId + ",mPosBrightness=" + this.mPosBrightness + ",mOffsetBrightnessLast=" + this.mOffsetBrightnessLast + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mDelta=" + this.mDelta + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset);
        }
    }

    private void loadOminLevelParas() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "loadOminLevelParas failed, mContentResolver==null");
        } else if (this.mIsOminLevelModeEnable) {
            this.mOminLevelCountSaved = Settings.System.getIntForUser(contentResolver, "spline_ominlevel_count", 0, this.mCurrentUserId);
            this.mOminLevelCount = this.mOminLevelCountSaved;
            this.mOminLevelCountResetLongSetTimeSaved = Settings.System.getIntForUser(this.mContentResolver, "spline_ominlevel_time", 0, this.mCurrentUserId);
            this.mOminLevelCountResetLongSetTime = this.mOminLevelCountResetLongSetTimeSaved;
            Slog.i(TAG, "mOminLevelMode read mOminLevelCount=" + this.mOminLevelCount + ",mOminLevelCountResetLongSetTime=" + this.mOminLevelCountResetLongSetTime);
        }
    }

    private void saveOffsetParas() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "saveOffsetParas failed, mContentResolver==null");
            return;
        }
        float f = this.mPosBrightness;
        if (((int) (f * GAIN_FACTOR)) != ((int) (this.mPosBrightnessSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(contentResolver, "hw_screen_auto_brightness_adj", f / MAX_DEFAULT_BRIGHTNESS, this.mCurrentUserId);
            this.mPosBrightnessSaved = this.mPosBrightness;
        }
        float f2 = this.mDeltaNew;
        if (((int) (f2 * GAIN_FACTOR)) != ((int) (this.mDeltaSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_delta", f2, this.mCurrentUserId);
            this.mDeltaSaved = this.mDeltaNew;
        }
        boolean z = this.mIsUserChange;
        if (z != this.mIsUserChangeSaved) {
            Settings.System.putIntForUser(this.mContentResolver, "spline_is_user_change", z ? 1 : 0, this.mCurrentUserId);
            this.mIsUserChangeSaved = this.mIsUserChange;
        }
        float f3 = this.mOffsetBrightnessLast;
        if (((int) (f3 * GAIN_FACTOR)) != ((int) (this.mOffsetBrightnessLastSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_offset_brightness_last", f3, this.mCurrentUserId);
            this.mOffsetBrightnessLastSaved = this.mOffsetBrightnessLast;
        }
        float f4 = this.mLastLuxDefaultBrightness;
        if (((int) (f4 * GAIN_FACTOR)) != ((int) (this.mLastLuxDefaultBrightnessSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_last_lux_default_brightness", f4, this.mCurrentUserId);
            this.mLastLuxDefaultBrightnessSaved = this.mLastLuxDefaultBrightness;
        }
        float f5 = this.mStartLuxDefaultBrightness;
        if (((int) (f5 * GAIN_FACTOR)) != ((int) (this.mStartLuxDefaultBrightnessSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_start_lux_default_brightness", f5, this.mCurrentUserId);
            this.mStartLuxDefaultBrightnessSaved = this.mStartLuxDefaultBrightness;
        }
        saveAmbientLuxOffsetParameters();
        saveOminLevelOffsetParameters();
        saveTwoPointOffsetParameters();
        if (HWDEBUG) {
            Slog.d(TAG, "write:userId=" + this.mCurrentUserId + ",mPosBrightness =" + this.mPosBrightness + ",mOffsetBrightnessLast=" + this.mOffsetBrightnessLast + ",mIsUserChange=" + this.mIsUserChange + ",mDeltaNew=" + this.mDeltaNew + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness + ",mAmLux=" + this.mAmLux + ",mAmLuxOffset=" + this.mAmLuxOffset);
        }
    }

    private void saveAmbientLuxOffsetParameters() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "saveAmbientLuxOffsetParameters failed, mContentResolver==null");
            return;
        }
        float f = this.mAmLux;
        if (((int) (f * GAIN_FACTOR)) != ((int) (this.mAmLuxSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(contentResolver, "spline_ambient_lux", f, this.mCurrentUserId);
            this.mAmLuxSaved = this.mAmLux;
        }
        float f2 = this.mAmLuxOffset;
        if (((int) (f2 * GAIN_FACTOR)) != ((int) (this.mAmLuxOffsetSaved * GAIN_FACTOR))) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_ambient_lux_offset", f2, this.mCurrentUserId);
            this.mAmLuxOffsetSaved = this.mAmLuxOffset;
        }
    }

    private void saveOminLevelOffsetParameters() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "saveOminLevelOffsetParameters failed, mContentResolver==null");
            return;
        }
        int i = this.mOminLevelCount;
        if (i != this.mOminLevelCountSaved) {
            Settings.System.putIntForUser(contentResolver, "spline_ominlevel_count", i, this.mCurrentUserId);
            this.mOminLevelCountSaved = this.mOminLevelCount;
            Slog.i(TAG, "mOminLevelMode saved mOminLevelCount=" + this.mOminLevelCount);
        }
        int i2 = this.mOminLevelCountResetLongSetTime;
        if (i2 != this.mOminLevelCountResetLongSetTimeSaved) {
            Settings.System.putIntForUser(this.mContentResolver, "spline_ominlevel_time", i2, this.mCurrentUserId);
            this.mOminLevelCountResetLongSetTimeSaved = this.mOminLevelCountResetLongSetTime;
            Slog.i(TAG, "mOminLevelMode saved mOminLevelCountResetLongSetTime=" + this.mOminLevelCountResetLongSetTime);
        }
    }

    public static HwNormalizedSpline createHwNormalizedSpline(Context context, int deviceActualBrightnessLevel, int deviceActualBrightnessNit, int deviceStandardBrightnessNit) {
        return new HwNormalizedSpline(context, deviceActualBrightnessLevel, deviceActualBrightnessNit, deviceStandardBrightnessNit);
    }

    public String toString() {
        return new StringBuilder().toString();
    }

    public float interpolate(float lux) {
        this.mAmLux = lux;
        this.mIsReboot = Math.abs(this.mPosBrightness) < SMALL_VALUE;
        if (HWDEBUG) {
            Slog.d(TAG, "interpolate:mPosBrightness=" + this.mPosBrightness + ",lux=" + lux + ",mIsReboot=" + this.mIsReboot + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta);
        }
        float valueInterp = getInterpolatedValue(this.mPosBrightness, lux) / MAX_DEFAULT_BRIGHTNESS;
        saveOffsetParas();
        return valueInterp;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0073  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00bf  */
    public void updateLevelWithLux(float posBrightness, float lux) {
        if (lux < 0.0f) {
            Slog.e(TAG, "updateLevelWithLux, no update, error input lux, lux=" + lux);
            return;
        }
        this.mIsUserChange = !this.mIsReboot;
        this.mAmLuxOffset = lux;
        updateStartLuxDefaultBrightness(posBrightness, lux);
        this.mPosBrightness = posBrightness;
        if (this.mIsManualMode) {
            float f = this.mStartLuxDefaultBrightness;
            int i = this.mManualBrightnessMaxLimit;
            if (f >= ((float) i) && Math.abs(this.mPosBrightness - ((float) i)) < SMALL_VALUE) {
                this.mAmLuxOffset = DEFAULT_NO_OFFSET_LUX;
                this.mDelta = 0.0f;
                this.mDeltaNew = 0.0f;
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel outdoor no offset mDelta=0");
                }
                if (this.mIsTwoPointOffsetEnable) {
                    clearHighOffset();
                }
                if (Math.abs(this.mPosBrightness) < SMALL_VALUE) {
                    initDefaultOffsetPara();
                }
                if (!this.mIsOminLevelModeEnable) {
                    updateOminLevelCount(lux);
                } else {
                    this.mOminLevelCountResetLongSetTime = 0;
                    this.mOminLevelCount = 0;
                }
                if (this.mIsVehicleModeEnable && this.mIsVehicleModeBrightnessEnable && lux < this.mVehicleModeLuxThreshold) {
                    this.mIsVehicleModeClearOffsetEnable = true;
                    Slog.i(TAG, "VeBrightMode updateLevel lux=" + lux + ",luxTh=" + this.mVehicleModeLuxThreshold);
                }
                checkErrorCorrectionOffset();
                updateTwoPointOffset(this.mPosBrightness, this.mAmLuxOffset);
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel:mDelta=" + this.mDelta + ",mDeltaNew=" + this.mDeltaNew + ",mPosBrightness=" + this.mPosBrightness + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",lux=" + lux);
                }
                saveOffsetParas();
            }
        }
        float f2 = this.mPosBrightness;
        float f3 = this.mStartLuxDefaultBrightness;
        this.mDelta = f2 - f3;
        this.mDeltaNew = f2 - f3;
        if (Math.abs(this.mPosBrightness) < SMALL_VALUE) {
        }
        if (!this.mIsOminLevelModeEnable) {
        }
        this.mIsVehicleModeClearOffsetEnable = true;
        Slog.i(TAG, "VeBrightMode updateLevel lux=" + lux + ",luxTh=" + this.mVehicleModeLuxThreshold);
        checkErrorCorrectionOffset();
        updateTwoPointOffset(this.mPosBrightness, this.mAmLuxOffset);
        if (HWFLOW) {
        }
        saveOffsetParas();
    }

    private void updateStartLuxDefaultBrightness(float posBrightness, float lux) {
        if (this.mIsPersonalizedBrightnessEnable) {
            float defaultBrightness = getCurrentBrightness(lux);
            if (!this.mIsDayModeAlgoEnable || !this.mIsDayModeEnable || getBrightnessMode() != BrightnessModeState.NEWCURVE_MODE) {
                this.mStartLuxDefaultBrightness = defaultBrightness;
                return;
            }
            float oldBrightness = this.mStartLuxDefaultBrightness;
            float f = this.mDayModeMinimumBrightness;
            if (defaultBrightness > f) {
                f = defaultBrightness;
            }
            this.mStartLuxDefaultBrightness = f;
            if (oldBrightness != this.mStartLuxDefaultBrightness) {
                Slog.i(TAG, "updateLevel DayMode: defaultBrightness=" + defaultBrightness + ", mDayModeMinimumBrightness=" + this.mDayModeMinimumBrightness);
            }
        } else if (this.mIsDarkAdaptEnable) {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(getCurrentDarkAdaptLine(), lux);
            if (posBrightness == 0.0f || posBrightness >= this.mStartLuxDefaultBrightness) {
                this.mIsDarkAdaptLineLocked = false;
            } else {
                this.mIsDarkAdaptLineLocked = true;
            }
            if (HWFLOW) {
                Slog.i(TAG, "updateLevel DarkAdapt: mDefaultBrightness=" + this.mStartLuxDefaultBrightness + ", locked=" + this.mIsDarkAdaptLineLocked);
            }
        } else if (!this.mIsOminLevelCountEnable || !this.mIsOminLevelModeEnable) {
            if (!this.mIsDayModeAlgoEnable || !this.mIsDayModeEnable) {
                this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrightnessLinePointsList, lux);
                return;
            }
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDayBrightnessLinePointsList, lux);
            if (HWFLOW) {
                Slog.i(TAG, "updateLevel DayMode: mDefaultBrightnessFromLux=" + this.mStartLuxDefaultBrightness);
            }
        } else if ((!this.mIsDayModeAlgoEnable || !this.mIsDayModeEnable) && !this.mIsOminLevelDayModeEnable) {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mDefaultBrightnessLinePointsList, lux);
        } else {
            this.mStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mOminLevelBrightnessLinePointsList, lux);
            if (HWFLOW) {
                Slog.i(TAG, "updateLevel mOminLevelMode:mDefaultBrightness=" + this.mStartLuxDefaultBrightness);
            }
        }
    }

    private void initDefaultOffsetPara() {
        this.mAmLuxOffset = DEFAULT_NO_OFFSET_LUX;
        this.mDelta = 0.0f;
        this.mDeltaNew = 0.0f;
        this.mOffsetBrightnessLast = 0.0f;
        this.mLastLuxDefaultBrightness = 0.0f;
        this.mStartLuxDefaultBrightness = 0.0f;
        this.mIsDarkAdaptLineLocked = false;
        clearGameOffsetDelta();
    }

    public void updateLevelGameWithLux(float posBrightness, float lux) {
        this.mGameModePosBrightness = posBrightness;
        if (posBrightness != 0.0f) {
            this.mGameModeStartLuxDefaultBrightness = getDefaultBrightnessLevelNew(this.mGameModeBrightnessLinePointsList, lux);
            this.mDeltaTmp = posBrightness - this.mGameModeStartLuxDefaultBrightness;
            this.mGameModeOffsetLux = lux;
        } else {
            clearGameOffsetDelta();
        }
        if (HWFLOW) {
            Slog.i(TAG, "GameBrightMode updateLevelTmp:mDeltaTmp=" + this.mDeltaTmp + ",mGameModePosBrightness=" + this.mGameModePosBrightness + ",mGameModeStartLuxDefaultBrightness=" + this.mGameModeStartLuxDefaultBrightness + ",lux=" + lux);
        }
    }

    public void setGameCurveLevel(int curveLevel) {
        setGameModeEnable(curveLevel == 21);
    }

    public void setGameModeEnable(boolean isGameModeBrightnessEnable) {
        this.mIsGameModeBrightnessEnable = isGameModeBrightnessEnable;
    }

    public void clearGameOffsetDelta() {
        if (this.mDeltaTmp != 0.0f) {
            if (HWFLOW) {
                Slog.i(TAG, "GameBrightMode updateLevelTmp clearGameOffsetDelta,mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux);
            }
            this.mDeltaTmp = 0.0f;
            this.mGameModeOffsetLux = DEFAULT_NO_OFFSET_LUX;
            this.mGameModePosBrightness = 0.0f;
        }
    }

    private boolean isUpdateOminLevelValid() {
        BrightnessModeState currentMode = getBrightnessMode();
        if (currentMode == BrightnessModeState.GAME_MODE || currentMode == BrightnessModeState.VIDEO_FULLSCREEN_MODE || currentMode == BrightnessModeState.CAMERA_MODE) {
            return false;
        }
        if (!this.mIsVehicleModeEnable || !this.mIsVehicleModeBrightnessEnable) {
            return true;
        }
        return false;
    }

    private void updateOminLevelCount(float lux) {
        if (isUpdateOminLevelValid()) {
            int currentMinuteTime = (int) (System.currentTimeMillis() / 60000);
            int deltaMinuteTime = currentMinuteTime - this.mOminLevelCountResetLongSetTime;
            if (deltaMinuteTime >= this.mOminLevelCountResetLongTimeTh || deltaMinuteTime < 0) {
                this.mOminLevelCount = resetOminLevelCount(this.mOminLevelCountLevelPointsList, (float) this.mOminLevelCount);
                this.mOminLevelCountResetLongSetTime = currentMinuteTime;
                if (HWFLOW) {
                    Slog.i(TAG, "mOminLevelMode reset mOminLevelCount=" + this.mOminLevelCount + ",deltaMinuteTime=" + deltaMinuteTime + ",currenTime=" + currentMinuteTime);
                }
            }
            if (lux >= 0.0f && lux <= ((float) this.mOminLevelCountValidLuxTh)) {
                long currentTime = SystemClock.uptimeMillis();
                float brightenDefaultBrightness = getDefaultBrightnessLevelNew(this.mOminLevelBrightnessLinePointsList, lux);
                long deltaTime = currentTime - this.mOminLevelCountSetTime;
                if (deltaTime / 1000 >= ((long) this.mOminLevelCountValidTimeTh)) {
                    if (HWFLOW) {
                        Slog.i(TAG, "mOminLevelMode deltaTime=" + deltaTime + ",ValidTime=" + this.mOminLevelCountValidTimeTh);
                    }
                    updateOminLevelCountWithValidTime(currentTime, brightenDefaultBrightness);
                } else {
                    if (HWFLOW) {
                        Slog.i(TAG, "mOminLevelMode deltaTime=" + deltaTime);
                    }
                    updateOminLevelCountWithNoValidTime(currentTime, brightenDefaultBrightness);
                }
                updateOminLevelBrightnessLinePoints();
            }
        }
    }

    private void updateOminLevelCountWithValidTime(long currentTime, float brightenDefaultBrightness) {
        int i;
        if (Math.abs(this.mPosBrightness) < SMALL_VALUE) {
            int i2 = this.mOminLevelCount;
            if (i2 > 0 && this.mIsOminLevelOffsetCountEnable) {
                this.mOminLevelCount = i2 - 1;
                this.mOminLevelValidCount = 0;
                this.mOminLevelCountSetTime = currentTime;
                Slog.i(TAG, "mOminLevelMode resetoffset-- count=" + this.mOminLevelCount);
                return;
            }
            return;
        }
        float f = this.mPosBrightness;
        if (f - brightenDefaultBrightness > 0.0f) {
            if (this.mOminLevelCount < getOminLevelCountThMax(this.mOminLevelCountLevelPointsList)) {
                this.mOminLevelCount++;
                this.mOminLevelValidCount = 1;
                this.mOminLevelCountSetTime = currentTime;
                Slog.i(TAG, "mOminLevelMode brighten++ count=" + this.mOminLevelCount);
            }
        } else if (f - brightenDefaultBrightness < 0.0f && (i = this.mOminLevelCount) > 0) {
            this.mOminLevelCount = i - 1;
            this.mOminLevelValidCount = -1;
            this.mOminLevelCountSetTime = currentTime;
            Slog.i(TAG, "mOminLevelMode darken-- count=" + this.mOminLevelCount);
        }
    }

    private void updateOminLevelCountWithNoValidTime(long currentTime, float brightenDefaultBrightness) {
        int i;
        int i2;
        int i3;
        int i4;
        if (Math.abs(this.mPosBrightness) < SMALL_VALUE) {
            int i5 = this.mOminLevelCount;
            if (i5 > 0 && (i4 = this.mOminLevelValidCount) >= 0 && this.mIsOminLevelOffsetCountEnable) {
                this.mOminLevelCount = i5 - 1;
                this.mOminLevelValidCount = i4 - 1;
                this.mOminLevelCountSetTime = currentTime;
                Slog.i(TAG, "mOminLevelMode resetoffset-- count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
                return;
            }
            return;
        }
        float f = this.mPosBrightness;
        if (f - brightenDefaultBrightness > 0.0f) {
            if (this.mOminLevelCount < getOminLevelCountThMax(this.mOminLevelCountLevelPointsList) && (i3 = this.mOminLevelValidCount) <= 0) {
                this.mOminLevelCount++;
                this.mOminLevelValidCount = i3 + 1;
                this.mOminLevelCountSetTime = currentTime;
                Slog.i(TAG, "mOminLevelMode brighten++ count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
            }
        } else if (f - brightenDefaultBrightness < 0.0f && (i = this.mOminLevelCount) > 0 && (i2 = this.mOminLevelValidCount) >= 0) {
            this.mOminLevelCount = i - 1;
            this.mOminLevelValidCount = i2 - 1;
            this.mOminLevelCountSetTime = currentTime;
            Slog.i(TAG, "mOminLevelMode darken-- count=" + this.mOminLevelCount + ",ValidCount=" + this.mOminLevelValidCount);
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
            PointF prePoint = null;
            for (PointF curPoint : linePointsList) {
                if (prePoint == null) {
                    prePoint = curPoint;
                }
                if (levelCount < curPoint.x) {
                    ominLevelCount = (int) prePoint.x;
                } else {
                    prePoint = curPoint;
                }
            }
            return ominLevelCount;
        }
    }

    private int getOminLevelCountThMin(List<PointF> linePointsList) {
        if (linePointsList != null && linePointsList.size() > 0) {
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
        if (linePointsList != null && linePointsList.size() > 0) {
            return linePointsList.get(0).y;
        }
        return MIN_DEFAULT_BRIGHTNESS;
    }

    private float getOminLevelThMax(List<PointF> linePointsList) {
        int listSize;
        if (linePointsList != null && (listSize = linePointsList.size()) > 0) {
            return linePointsList.get(listSize - 1).y;
        }
        return MIN_DEFAULT_BRIGHTNESS;
    }

    public boolean isPowerSavingModeBrightnessChangeEnable(float lux, boolean isUsePowerSavingModeCurveEnable) {
        boolean isPowerSavingModeBrightnessChangeEnable = false;
        if (this.mIsPowerSavingBrightnessLineEnable && lux > this.mPowerSavingAmluxThreshold && this.mIsUsePowerSavingModeCurveEnable != isUsePowerSavingModeCurveEnable) {
            float mPowerSavingDefaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mPowerSavingBrightnessLinePointsList, lux);
            float defaultBrightnessFromLux = getDefaultBrightnessLevelNew(this.mDefaultBrightnessLinePointsList, lux);
            if (((int) mPowerSavingDefaultBrightnessFromLux) != ((int) defaultBrightnessFromLux)) {
                isPowerSavingModeBrightnessChangeEnable = true;
                if (HWFLOW) {
                    Slog.i(TAG, "PowerSavingMode lux=" + lux + ",usePgEnable=" + isUsePowerSavingModeCurveEnable + ",pgBrightness=" + mPowerSavingDefaultBrightnessFromLux + ",defaultBrightnessFromLux=" + defaultBrightnessFromLux);
                }
            }
        }
        this.mIsUsePowerSavingModeCurveEnable = isUsePowerSavingModeCurveEnable;
        return isPowerSavingModeBrightnessChangeEnable;
    }

    public void updateNewBrightnessCurve() {
        if (this.mIsPersonalizedBrightnessCurveLoadEnable) {
            this.mIsNewCurveEnable = false;
            if (isNewBrigtenssCurveValid()) {
                if (this.mIsRebootNewCurveEnable) {
                    this.mIsRebootNewCurveEnable = false;
                    this.mIsNewCurveEnableTmp = false;
                    this.mIsNewCurveEnable = true;
                    Slog.i(TAG, "NewCurveMode reboot first updateNewBrightnessCurve success!");
                }
                if (this.mIsNewCurveEnableTmp) {
                    this.mIsNewCurveEnableTmp = false;
                    this.mIsNewCurveEnable = true;
                    clearBrightnessOffset();
                    Slog.i(TAG, "NewCurveMode updateNewBrightnessCurve success!");
                }
            }
        } else if (HWFLOW) {
            Slog.i(TAG, "not updateNewBrightnessCurve,mIsPersonalizedBrightnessCurveLoadEnable=" + this.mIsPersonalizedBrightnessCurveLoadEnable);
        }
    }

    private boolean isNewBrigtenssCurveValid() {
        if (this.mBrightnessCurveLow.size() > 0) {
            this.mBrightnessCurveLow.clear();
        }
        this.mBrightnessCurveLow = getBrightnessListFromDb("BrightnessCurveLow");
        if (!isBrightnessListValid(this.mBrightnessCurveLow)) {
            this.mBrightnessCurveLow.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveLow");
            return false;
        }
        if (this.mBrightnessCurveMiddle.size() > 0) {
            this.mBrightnessCurveMiddle.clear();
        }
        this.mBrightnessCurveMiddle = getBrightnessListFromDb("BrightnessCurveMiddle");
        if (!isBrightnessListValid(this.mBrightnessCurveMiddle)) {
            this.mBrightnessCurveMiddle.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveMiddle");
            return false;
        }
        if (this.mBrightnessCurveHigh.size() > 0) {
            this.mBrightnessCurveHigh.clear();
        }
        this.mBrightnessCurveHigh = getBrightnessListFromDb("BrightnessCurveHigh");
        if (!isBrightnessListValid(this.mBrightnessCurveHigh)) {
            this.mBrightnessCurveHigh.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveHigh");
            return false;
        }
        if (this.mBrightnessCurveDefault.size() > 0) {
            this.mBrightnessCurveDefault.clear();
        }
        this.mBrightnessCurveDefault = getBrightnessListFromDb("BrightnessCurveDefault");
        if (isBrightnessListValid(this.mBrightnessCurveDefault)) {
            return true;
        }
        this.mBrightnessCurveDefault.clear();
        Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveDefault");
        return false;
    }

    private List<PointF> getBrightnessListFromDb(String brightnessCurveTag) {
        List<PointF> brightnessList = new ArrayList<>();
        DisplayEngineManager displayEngineManager = this.mManager;
        if (displayEngineManager == null || brightnessCurveTag == null) {
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

    private boolean isBrightnessListValid(List<PointF> linePointsList) {
        if (linePointsList == null) {
            Slog.e(TAG, "linePointsListin == null");
            return false;
        } else if (linePointsList.size() <= 2 || linePointsList.size() >= 100) {
            Slog.e(TAG, "linePointsListin number is wrong");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF curPoint : linePointsList) {
                if (lastPoint == null) {
                    lastPoint = curPoint;
                } else if (lastPoint.x - curPoint.x > -1.0E-6f) {
                    Slog.e(TAG, "linePointsListin list.y is false, lastPoint.x=" + lastPoint.x + ", curPoint.x =" + curPoint.x);
                    return false;
                } else if (((int) lastPoint.y) > ((int) curPoint.y)) {
                    Slog.e(TAG, "linePointsListin check list.y false, lastPoint.y=" + lastPoint.y + ", curPoint.y=" + curPoint.y);
                    return false;
                } else {
                    lastPoint = curPoint;
                }
            }
            return true;
        }
    }

    public void updateNewBrightnessCurveFromTmp() {
        synchronized (LOCK) {
            if (this.mIsNewCurveEnableTmp) {
                this.mIsNewCurveEnable = false;
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
                if (this.mIsNewCurveEnableTmp) {
                    this.mIsNewCurveEnableTmp = false;
                    this.mIsNewCurveEnable = true;
                    clearBrightnessOffset();
                    Slog.i(TAG, "NewCurveMode updateNewBrightnessCurve from tmp, success!");
                }
            }
        }
    }

    private List<PointF> cloneList(List<PointF> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        List<PointF> newList = new ArrayList<>();
        for (PointF point : list) {
            newList.add(new PointF(point.x, point.y));
        }
        return newList;
    }

    public void updateNewBrightnessCurveTmp() {
        this.mIsNewCurveEnableTmp = false;
        if (!this.mIsPersonalizedBrightnessEnable || !this.mIsPersonalizedBrightnessCurveLoadEnable) {
            if (HWFLOW) {
                Slog.d(TAG, "not updateNewBrightnessCurveTmp,mIsPersonalizedBrightnessEnable=" + this.mIsPersonalizedBrightnessEnable + ",mIsPersonalizedBrightnessCurveLoadEnable=" + this.mIsPersonalizedBrightnessCurveLoadEnable);
            }
        } else if (isTmpBrightnessCurveValid()) {
            this.mIsNewCurveEnableTmp = true;
            if (HWFLOW) {
                Slog.i(TAG, "NewCurveMode updateNewBrightnessCurveTmp success!");
            }
            if (!this.mIsPowerOnEnable) {
                updateNewBrightnessCurveFromTmp();
            }
        }
    }

    private boolean isTmpBrightnessCurveValid() {
        if (this.mBrightnessCurveLowTmp.size() > 0) {
            this.mBrightnessCurveLowTmp.clear();
        }
        this.mBrightnessCurveLowTmp = getBrightnessListFromDb("BrightnessCurveLow");
        if (!isBrightnessListValid(this.mBrightnessCurveLowTmp)) {
            this.mBrightnessCurveLowTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveLow");
            return false;
        }
        if (this.mBrightnessCurveMiddleTmp.size() > 0) {
            this.mBrightnessCurveMiddleTmp.clear();
        }
        this.mBrightnessCurveMiddleTmp = getBrightnessListFromDb("BrightnessCurveMiddle");
        if (!isBrightnessListValid(this.mBrightnessCurveMiddleTmp)) {
            this.mBrightnessCurveMiddleTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveMiddle");
            return false;
        }
        if (this.mBrightnessCurveHighTmp.size() > 0) {
            this.mBrightnessCurveHighTmp.clear();
        }
        this.mBrightnessCurveHighTmp = getBrightnessListFromDb("BrightnessCurveHigh");
        if (!isBrightnessListValid(this.mBrightnessCurveHighTmp)) {
            this.mBrightnessCurveHighTmp.clear();
            Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveHigh");
            return false;
        }
        if (this.mBrightnessCurveDefaultTmp.size() > 0) {
            this.mBrightnessCurveDefaultTmp.clear();
        }
        this.mBrightnessCurveDefaultTmp = getBrightnessListFromDb("BrightnessCurveDefault");
        if (isBrightnessListValid(this.mBrightnessCurveDefaultTmp)) {
            return true;
        }
        this.mBrightnessCurveDefaultTmp.clear();
        Slog.w(TAG, "NewCurveMode checkPointsList brightnessList is wrong,tag=BrightnessCurveDefault");
        return false;
    }

    public List<Short> getPersonalizedDefaultCurve() {
        if (this.mBrightnessCurveDefaultTmp.isEmpty()) {
            return Collections.emptyList();
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
        DisplayEngineManager displayEngineManager = this.mManager;
        if (displayEngineManager == null) {
            return Collections.emptyList();
        }
        List<Bundle> records = displayEngineManager.getAllRecords("AlgorithmESCW", new Bundle());
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
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
        if (this.mIsVehicleModeEnable) {
            boolean z = true;
            if (curveLevel == 19) {
                this.mIsVehicleModeBrightnessEnable = true;
            }
            if (curveLevel != 18) {
                z = false;
            }
            this.mIsVehicleModeQuitForPowerOnEnable = z;
            if (HWFLOW && this.mSceneLevel != curveLevel && (curveLevel == 19 || curveLevel == 18)) {
                Slog.i(TAG, "VeBrightMode curveLevel=" + curveLevel + ",VEnable=" + this.mIsVehicleModeBrightnessEnable);
            }
            this.mSceneLevel = curveLevel;
        }
    }

    public boolean isVehicleModeQuitForPowerOnEnable() {
        return this.mIsVehicleModeQuitForPowerOnEnable;
    }

    public boolean isVehicleModeBrightnessEnable() {
        return this.mIsVehicleModeBrightnessEnable;
    }

    public void updateVehicleModeQuitEnable() {
        if (this.mIsVehicleModeBrightnessEnable) {
            this.mIsVehicleModeBrightnessEnable = false;
            this.mIsVehicleModeQuitForPowerOnEnable = false;
            if (this.mIsVehicleModeClearOffsetEnable) {
                this.mIsVehicleModeClearOffsetEnable = false;
                clearBrightnessOffset();
                if (this.mIsTwoPointOffsetEnable) {
                    clearLowOffset();
                    clearTmpOffset();
                    Slog.i(TAG, "VeBrightMode clear low and tmp brightnessOffset");
                }
                Slog.i(TAG, "VeBrightMode clear brightnessOffset");
            }
            if (HWFLOW) {
                Slog.i(TAG, "VeBrightMode set mIsVeModeBrightnessEnable=" + this.mIsVehicleModeBrightnessEnable);
            }
        }
    }

    public boolean isPersonalizedNewCurveEnableTmp() {
        return this.mIsNewCurveEnableTmp && this.mIsPersonalizedBrightnessCurveLoadEnable;
    }

    public boolean isPersonalizedNewCurveEnable() {
        return this.mIsNewCurveEnable;
    }

    public void setPowerStatus(boolean isPoweronEnable) {
        if (this.mIsPowerOnEnable != isPoweronEnable) {
            this.mIsPowerOnEnable = isPoweronEnable;
        }
    }

    public void setNewCurveEnable(boolean isNewCurveEnable) {
        if (isNewCurveEnable && this.mIsPersonalizedBrightnessCurveLoadEnable) {
            Slog.i(TAG, "NewCurveMode updateNewBrightnessCurveReal starting..,mIsNewCurveEnable=" + this.mIsNewCurveEnable + ",mIsNewCurveEnableTmp=" + this.mIsNewCurveEnableTmp);
            updateNewBrightnessCurveFromTmp();
        }
    }

    public void clearBrightnessOffset() {
        if (Math.abs(this.mPosBrightness) > SMALL_VALUE) {
            this.mPosBrightness = 0.0f;
            this.mDelta = 0.0f;
            this.mDeltaNew = 0.0f;
            this.mIsUserChange = false;
            this.mAmLuxOffset = DEFAULT_NO_OFFSET_LUX;
            saveOffsetParas();
            if (HWFLOW) {
                Slog.i(TAG, "NewCurveMode clear tmp brightness offset");
            }
        }
    }

    private float getLimitedGameModeBrightness(float brightnessIn) {
        float brightnessOut;
        if (!this.mIsGameModeEnable || !this.mIsGameModeBrightnessLimitationEnable || !this.mIsGameModeBrightnessEnable) {
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
                    Slog.i(TAG, "getLimitedGameModeBrightness, brightnessOut=" + brightnessOut);
                }
                this.mLastGameModeBrightness = brightnessOut;
                return brightnessOut;
            }
        }
        this.mLastGameModeBrightness = brightnessIn;
        return brightnessIn;
    }

    /* access modifiers changed from: private */
    public class CameraModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private CameraModeCurveList() {
            this.mode = BrightnessModeState.CAMERA_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mCameraBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class GameModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private GameModeCurveList() {
            this.mode = BrightnessModeState.GAME_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mGameModeBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class HdrModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private HdrModeCurveList() {
            this.mode = BrightnessModeState.HDR_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mHdrModeBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class VideoFullScreenModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private VideoFullScreenModeCurveList() {
            this.mode = BrightnessModeState.VIDEO_FULLSCREEN_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mVideoFullScreenModeBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class ReadingModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private ReadingModeCurveList() {
            this.mode = BrightnessModeState.READING_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mReadingBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class NewCurveModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private NewCurveModeCurveList() {
            this.mode = BrightnessModeState.NEWCURVE_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.getCurrentNewCurveLinePointsList();
        }
    }

    /* access modifiers changed from: private */
    public class PowerSavingModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private PowerSavingModeCurveList() {
            this.mode = BrightnessModeState.POWERSAVING_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mPowerSavingBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class EyeProtectionModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private EyeProtectionModeCurveList() {
            this.mode = BrightnessModeState.EYE_PROTECTION_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mDefaultBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class CalibrationModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private CalibrationModeCurveList() {
            this.mode = BrightnessModeState.CALIBRATION_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mDefaultBrightnessLinePointsListCaliBefore;
        }
    }

    /* access modifiers changed from: private */
    public class DarkAdaptModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private DarkAdaptModeCurveList() {
            this.mode = BrightnessModeState.DARKADAPT_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.getCurrentDarkAdaptLine();
        }
    }

    /* access modifiers changed from: private */
    public class OminLevelModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private OminLevelModeCurveList() {
            this.mode = BrightnessModeState.OMINLEVEL_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mOminLevelBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class DarkModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private DarkModeCurveList() {
            this.mode = BrightnessModeState.DARK_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            if (HwNormalizedSpline.this.mIsDarkModeCurveEnable) {
                return HwNormalizedSpline.this.mDarkModeBrightnessLinePointsList;
            }
            return HwNormalizedSpline.this.mDayBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class DayModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private DayModeCurveList() {
            this.mode = BrightnessModeState.DAY_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mDayBrightnessLinePointsList;
        }
    }

    /* access modifiers changed from: private */
    public class DefaultModeCurveList implements BrightnessCurveList {
        private BrightnessModeState mode;

        private DefaultModeCurveList() {
            this.mode = BrightnessModeState.DEFAULT_MODE;
        }

        @Override // android.util.HwNormalizedSpline.BrightnessCurveList
        public List<PointF> getBrightnessCurveList() {
            return HwNormalizedSpline.this.mDefaultBrightnessLinePointsList;
        }
    }

    private float getCurrentBrightness(float lux) {
        float brightness;
        HwEyeProtectionSpline hwEyeProtectionSpline;
        BrightnessModeState currentMode = getBrightnessMode();
        BrightnessCurveList brightnessCurveList = this.mBrightnessCurveMap.get(currentMode);
        List<PointF> brightnessList = new ArrayList<>();
        if (brightnessCurveList != null) {
            brightnessList = brightnessCurveList.getBrightnessCurveList();
        }
        if (brightnessList == null || brightnessList.size() == 0) {
            brightnessList = this.mDefaultBrightnessLinePointsList;
            Slog.i(TAG, "NewCurveMode brightnessList null,set mDefaultBrightnessLinePointsList");
        }
        if (currentMode != BrightnessModeState.EYE_PROTECTION_MODE || (hwEyeProtectionSpline = this.mEyeProtectionSpline) == null) {
            brightness = getDefaultBrightnessLevelNew(brightnessList, lux);
        } else {
            brightness = hwEyeProtectionSpline.getEyeProtectionBrightnessLevel(lux);
        }
        if (this.mIsVehicleModeEnable && this.mIsVehicleModeBrightnessEnable && lux < this.mVehicleModeLuxThreshold) {
            float f = this.mVehicleModeBrightness;
            if (brightness > f) {
                f = brightness;
            }
            brightness = f;
        }
        float brightness2 = getLimitedGameModeBrightness(brightness);
        if (((int) this.mBrightnessForLog) != ((int) brightness2) && HWDEBUG) {
            Slog.i(TAG, "NewCurveMode mode=" + getBrightnessMode() + ",brightness=" + brightness2 + ",lux=" + lux + ",mPis=" + this.mPosBrightness + ",eyeProtectionMode=" + isEyeProtectionModeEnable() + ",mIsVeModeBrightnessEnable=" + this.mIsVehicleModeBrightnessEnable);
        }
        this.mBrightnessForLog = brightness2;
        return brightness2;
    }

    private boolean isEyeProtectionModeEnable() {
        HwEyeProtectionSpline hwEyeProtectionSpline = this.mEyeProtectionSpline;
        if (hwEyeProtectionSpline == null) {
            return false;
        }
        return hwEyeProtectionSpline.isEyeProtectionMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<PointF> getCurrentNewCurveLinePointsList() {
        if (!this.mIsNewCurveEnable) {
            Slog.i(TAG, "NewCurveMode NewCurveMode=false,return mDefaultBrightnessLinePointsList");
            return this.mDefaultBrightnessLinePointsList;
        }
        int currentCurveLevel = getCurrentCurveLevel();
        if (currentCurveLevel != this.mCurrentCurveLevel) {
            if (currentCurveLevel == 0) {
                if (HWFLOW) {
                    Slog.i(TAG, "NewCurveMode mBrightnessCurveLow NewCurveMode=" + this.mCurveLevel);
                }
            } else if (currentCurveLevel == 1) {
                if (HWFLOW) {
                    Slog.i(TAG, "NewCurveMode mBrightnessCurveMiddle NewCurveMode=" + this.mCurveLevel);
                }
            } else if (currentCurveLevel == 2) {
                if (HWFLOW) {
                    Slog.i(TAG, "NewCurveMode mBrightnessCurveHigh NewCurveMode=" + this.mCurveLevel);
                }
            } else if (HWFLOW) {
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
                if (this.mIsDayModeAlgoEnable) {
                    brightness = getDefaultBrightnessLevelNew(this.mDayBrightnessLinePointsList, fArr[i]);
                } else {
                    brightness = getDefaultBrightnessLevelNew(this.mDefaultBrightnessLinePointsList, fArr[i]);
                }
                brightnessList.add(new PointF(this.mLuxPonits[i], brightness));
                i++;
            } else {
                Slog.i(TAG, "NewCurveMode getCurrentDefaultNewCurveLine,mIsDayModeAlgoEnable=" + this.mIsDayModeAlgoEnable);
                return brightnessList;
            }
        }
    }

    public boolean getPersonalizedBrightnessCurveEnable() {
        if (!this.mIsNewCurveEnable || !this.mIsPersonalizedBrightnessEnable || !this.mIsPersonalizedBrightnessCurveLoadEnable) {
            return false;
        }
        return true;
    }

    public float getDefaultBrightness(float lux) {
        return getDefaultBrightnessLevelNew(this.mDefaultBrightnessLinePointsList, lux);
    }

    public float getNewDefaultBrightness(float lux) {
        List<PointF> brightnessList;
        new ArrayList();
        if (getBrightnessMode() == BrightnessModeState.NEWCURVE_MODE) {
            brightnessList = this.mBrightnessCurveDefault;
        } else {
            brightnessList = this.mDefaultBrightnessLinePointsList;
        }
        return getDefaultBrightnessLevelNew(brightnessList, lux);
    }

    public float getNewCurrentBrightness(float lux) {
        List<PointF> brightnessList;
        new ArrayList();
        if (getBrightnessMode() == BrightnessModeState.NEWCURVE_MODE) {
            brightnessList = getCurrentNewCurveLinePointsList();
        } else {
            brightnessList = this.mDefaultBrightnessLinePointsList;
        }
        return getDefaultBrightnessLevelNew(brightnessList, lux);
    }

    private BrightnessModeState getBrightnessMode() {
        if (this.mIsCameraModeEnable) {
            return BrightnessModeState.CAMERA_MODE;
        }
        if (this.mIsGameModeEnable && this.mIsGameModeBrightnessEnable) {
            return BrightnessModeState.GAME_MODE;
        }
        if (this.mIsHdrModeEnable && this.mIsHdrModeCurveEnable) {
            return BrightnessModeState.HDR_MODE;
        }
        if (this.mIsVideoFullScreenModeEnable && this.mIsVideoFullScreenModeBrightnessEnable) {
            return BrightnessModeState.VIDEO_FULLSCREEN_MODE;
        }
        if (this.mIsReadingModeEnable) {
            return BrightnessModeState.READING_MODE;
        }
        if (this.mIsNewCurveEnable) {
            return BrightnessModeState.NEWCURVE_MODE;
        }
        if (!this.mIsPowerSavingModeEnable || !this.mIsPowerSavingBrightnessLineEnable || this.mAmLux <= this.mPowerSavingAmluxThreshold) {
            return getBrightnessModeExt();
        }
        return BrightnessModeState.POWERSAVING_MODE;
    }

    private BrightnessModeState getBrightnessModeExt() {
        if (isEyeProtectionModeEnable() && this.mIsEyeProtectionSplineEnable) {
            return BrightnessModeState.EYE_PROTECTION_MODE;
        }
        if (this.mIsCalibrationModeBeforeEnable) {
            return BrightnessModeState.CALIBRATION_MODE;
        }
        if (this.mIsDarkAdaptEnable) {
            return BrightnessModeState.DARKADAPT_MODE;
        }
        if (this.mIsOminLevelCountEnable && this.mIsOminLevelModeEnable) {
            return BrightnessModeState.OMINLEVEL_MODE;
        }
        if (this.mIsDarkModeBrightnessEnable) {
            return BrightnessModeState.DARK_MODE;
        }
        if (!this.mIsDayModeAlgoEnable || !this.mIsDayModeEnable) {
            return BrightnessModeState.DEFAULT_MODE;
        }
        return BrightnessModeState.DAY_MODE;
    }

    public boolean isPowerSavingBrightnessLineEnable() {
        return this.mIsPowerSavingBrightnessLineEnable;
    }

    private float getInterpolatedValue(float positionBrightness, float lux) {
        float offsetBrightness;
        float posBrightness = positionBrightness;
        boolean isInDarkAdaptMode = false;
        updateDefaultBrightness(lux);
        if (this.mIsDarkAdaptEnable) {
            isInDarkAdaptMode = true;
            updateDarkAdaptState();
        }
        if (this.mIsReboot) {
            updateParaFromReboot();
        }
        if (this.mLastLuxDefaultBrightness <= 0.0f && this.mPosBrightness != 0.0f) {
            posBrightness = 0.0f;
            resetOffsetPara();
        }
        float f = this.mDefaultBrightnessFromLux;
        if ((!this.mIsGameModeEnable || !this.mIsGameModeBrightnessEnable || this.mDeltaTmp != 0.0f) && ((this.mIsGameModeBrightnessEnable || posBrightness != 0.0f) && !this.mIsCoverModeClearOffsetEnable)) {
            offsetBrightness = (!this.mIsGameModeEnable || !this.mIsGameModeBrightnessEnable) ? isInDarkAdaptMode ? getDarkAdaptOffset(posBrightness, lux) : this.mIsTwoPointOffsetEnable ? getInterpolatedValueFromTwoPointOffset(posBrightness, lux) : getOffsetBrightnessLevelNew(this.mStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, posBrightness) : getOffsetBrightnessLevelWithDelta(this.mGameModeStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, this.mGameModePosBrightness, this.mDeltaTmp);
        } else {
            offsetBrightness = this.mDefaultBrightnessFromLux;
            if (this.mIsCoverModeClearOffsetEnable) {
                this.mIsCoverModeClearOffsetEnable = false;
                if (HWFLOW) {
                    Slog.i(TAG, "set mIsCoverModeClearOffsetEnable=" + this.mIsCoverModeClearOffsetEnable);
                }
            }
        }
        printBrightnessParameters(lux, offsetBrightness);
        this.mLastLuxDefaultBrightness = this.mDefaultBrightnessFromLux;
        this.mOffsetBrightnessLast = offsetBrightness;
        return offsetBrightness;
    }

    private void printBrightnessParameters(float lux, float offsetBrightness) {
        if (HWFLOW && ((int) offsetBrightness) != ((int) this.mOffsetBrightnessLast)) {
            Slog.i(TAG, "offsetBrightness=" + offsetBrightness + ",mOffsetBrightnessLast=" + this.mOffsetBrightnessLast + ",lux=" + lux + ",mPosBrightness=" + this.mPosBrightness + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",mDefaultBrightnessFromLux=" + this.mDefaultBrightnessFromLux + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + ",mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
        if (HWFLOW && this.mIsGameModeBrightnessEnable && ((int) offsetBrightness) != ((int) this.mOffsetBrightnessLast)) {
            Slog.i(TAG, "GameBrightMode mGameModeStartLuxDefaultBrightness=" + this.mGameModeStartLuxDefaultBrightness + ",offsetBrightness=" + offsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModePosBrightness=" + this.mGameModePosBrightness + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux);
        }
    }

    private void updateDefaultBrightness(float lux) {
        float defaultBrightness = getCurrentBrightness(lux);
        if (((int) this.mDefaultBrightnessFromLux) != ((int) defaultBrightness) && HWFLOW) {
            Slog.i(TAG, "BrightenssCurve mode=" + getBrightnessMode() + ",lux=" + lux + ",defaultBrightness=" + defaultBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mPis=" + this.mPosBrightness + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mVeEnable=" + this.mIsVehicleModeBrightnessEnable + ",OffsetNeedClear=" + this.mIsErrorCorrectionOffsetNeedClear);
        }
        if (!this.mIsDayModeAlgoEnable || !this.mIsDayModeEnable || getBrightnessMode() != BrightnessModeState.NEWCURVE_MODE) {
            this.mDefaultBrightnessFromLux = defaultBrightness;
            return;
        }
        float oldBrightness = this.mDefaultBrightnessFromLux;
        float f = this.mDayModeMinimumBrightness;
        if (defaultBrightness > f) {
            f = defaultBrightness;
        }
        this.mDefaultBrightnessFromLux = f;
        if (HWFLOW && oldBrightness != this.mDefaultBrightnessFromLux) {
            Slog.i(TAG, "getInterpolatedValue DayMode: defaultBrightness =" + defaultBrightness + ", mDayModeMinimumBrightness =" + this.mDayModeMinimumBrightness);
        }
    }

    private void updateParaFromReboot() {
        float f = this.mDefaultBrightnessFromLux;
        this.mLastLuxDefaultBrightness = f;
        this.mStartLuxDefaultBrightness = f;
        this.mOffsetBrightnessLast = f;
        this.mIsReboot = false;
        this.mIsUserChange = false;
    }

    private void resetOffsetPara() {
        this.mPosBrightness = 0.0f;
        this.mDelta = 0.0f;
        this.mDeltaNew = 0.0f;
        this.mOffsetBrightnessLast = 0.0f;
        this.mLastLuxDefaultBrightness = 0.0f;
        this.mStartLuxDefaultBrightness = 0.0f;
        this.mIsUserChange = false;
        saveOffsetParas();
        if (HWFLOW) {
            Slog.i(TAG, "error state for default state");
        }
    }

    public float getDefaultBrightnessLevelNew(List<PointF> linePointsList, float lux) {
        if (linePointsList == null) {
            Slog.e(TAG, "getDefaultBrightnessLevelNew minBrightness,linePointsList==null");
            return MIN_DEFAULT_BRIGHTNESS;
        }
        float brightnessLevel = this.mDefaultBrightness;
        PointF prePoint = null;
        for (PointF curPoint : linePointsList) {
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (lux >= curPoint.x) {
                prePoint = curPoint;
                brightnessLevel = prePoint.y;
            } else if (curPoint.x > prePoint.x) {
                return (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (lux - prePoint.x)) + prePoint.y;
            } else {
                float brightnessLevel2 = this.mDefaultBrightness;
                Slog.w(TAG, "DefaultBrightness_prePoint.x <= nexPoint.x,x=" + curPoint.x + ",y=" + curPoint.y);
                return brightnessLevel2;
            }
        }
        return brightnessLevel;
    }

    /* access modifiers changed from: package-private */
    public float getOffsetBrightnessLevelNew(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        return getOffsetBrightnessLevelWithDelta(brightnessStartOrig, brightnessEndOrig, brightnessStartNew, this.mDelta);
    }

    /* access modifiers changed from: package-private */
    public float getOffsetBrightnessLevelWithDelta(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float delta) {
        if (this.mIsUserChange) {
            this.mIsUserChange = false;
        }
        float brightenRatio = 1.0f;
        float darkenRatio = 1.0f;
        if (brightnessStartOrig < brightnessEndOrig) {
            if (delta > 0.0f) {
                darkenRatio = getDefaultBrightenOffsetBrightenRaio(brightnessStartOrig, brightnessEndOrig, brightnessStartNew, delta);
            }
            if (delta < 0.0f) {
                brightenRatio = getDefaultDarkenOffsetBrightenRatio(brightnessStartOrig, brightnessEndOrig, brightnessStartNew, delta);
            }
        }
        if (brightnessStartOrig > brightnessEndOrig) {
            if (delta < 0.0f) {
                darkenRatio = getDefaultDarkenOffsetDarkenRatio(brightnessStartOrig, brightnessEndOrig, brightnessStartNew, delta);
            }
            if (delta > 0.0f) {
                brightenRatio = getDefaultBrightenOffsetDarkenRatio(brightnessStartOrig, brightnessEndOrig, brightnessStartNew);
            }
        }
        this.mDeltaNew = delta * brightenRatio * darkenRatio;
        if (this.mLastBrightnessEndOrig != brightnessEndOrig && HWFLOW) {
            Slog.i(TAG, "mDeltaNew=" + this.mDeltaNew + ",deltaStart=" + delta + ",darkenRatio=" + darkenRatio + ",brightenRatio=" + brightenRatio);
        }
        this.mLastBrightnessEndOrig = brightnessEndOrig;
        float brightnessAndDelta = this.mDeltaNew + brightnessEndOrig;
        float offsetBrightnessTemp = MIN_DEFAULT_BRIGHTNESS;
        if (brightnessAndDelta > MIN_DEFAULT_BRIGHTNESS) {
            offsetBrightnessTemp = brightnessAndDelta;
        }
        if (offsetBrightnessTemp < MAX_DEFAULT_BRIGHTNESS) {
            return offsetBrightnessTemp;
        }
        return MAX_DEFAULT_BRIGHTNESS;
    }

    private float getDefaultBrightenOffsetBrightenRaio(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float deltaStart) {
        float brightenRatio;
        float brightenRatio2;
        if (Math.abs(MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig) < SMALL_VALUE) {
            brightenRatio = 1.0f;
        } else {
            brightenRatio = (MAX_DEFAULT_BRIGHTNESS - brightnessEndOrig) / (MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig);
        }
        if (((int) this.mLastBrightnessEndOrig) != ((int) brightnessEndOrig) && HWFLOW) {
            Slog.i(TAG, "Orig_brightenRatio=" + brightenRatio + ",mOffsetBrightenAlphaRight=" + this.mOffsetBrightenAlphaRight);
        }
        float offsetBrightnessTmp = ((1.0f - this.mOffsetBrightenAlphaRight) * Math.max(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetBrightenAlphaRight * ((deltaStart * brightenRatio) + brightnessEndOrig));
        if (Math.abs(deltaStart) < SMALL_VALUE) {
            brightenRatio2 = 1.0f;
        } else {
            brightenRatio2 = (offsetBrightnessTmp - brightnessEndOrig) / deltaStart;
        }
        float brightenRatio3 = getBrightenOffsetBrightenRaio(brightenRatio2, brightnessStartOrig, brightnessEndOrig, brightnessStartNew, deltaStart);
        if (brightenRatio3 < 0.0f) {
            return 0.0f;
        }
        return brightenRatio3;
    }

    private float getDefaultDarkenOffsetBrightenRatio(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float deltaStart) {
        float brightenRatio;
        if (Math.abs(MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig) < SMALL_VALUE) {
            brightenRatio = 1.0f;
        } else {
            brightenRatio = (MAX_DEFAULT_BRIGHTNESS - brightnessEndOrig) / (MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig);
        }
        float brightenRatio2 = getDarkenOffsetBrightenRatio(brightenRatio, brightnessStartOrig, brightnessEndOrig, brightnessStartNew);
        if (brightenRatio2 < 0.0f) {
            return 0.0f;
        }
        return brightenRatio2;
    }

    private float getDefaultDarkenOffsetDarkenRatio(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float deltaStart) {
        float darkenRatio;
        float darkenRatio2;
        if (Math.abs(brightnessStartOrig - MIN_DEFAULT_BRIGHTNESS) < SMALL_VALUE) {
            darkenRatio = 1.0f;
        } else {
            darkenRatio = (brightnessEndOrig - MIN_DEFAULT_BRIGHTNESS) / (brightnessStartOrig - MIN_DEFAULT_BRIGHTNESS);
        }
        float offsetBrightnessTmp = ((1.0f - this.mOffsetDarkenAlphaLeft) * Math.min(brightnessEndOrig, brightnessStartNew)) + (this.mOffsetDarkenAlphaLeft * ((deltaStart * darkenRatio) + brightnessEndOrig));
        if (Math.abs(deltaStart) < SMALL_VALUE) {
            darkenRatio2 = 1.0f;
        } else {
            darkenRatio2 = (offsetBrightnessTmp - brightnessEndOrig) / deltaStart;
        }
        if (darkenRatio2 < 0.0f) {
            return 0.0f;
        }
        return darkenRatio2;
    }

    private float getDefaultBrightenOffsetDarkenRatio(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        float darkenRatio;
        float darkenRatioTmp;
        if (Math.abs(brightnessStartOrig) < SMALL_VALUE) {
            darkenRatioTmp = 1.0f;
            darkenRatio = 1.0f;
        } else {
            darkenRatioTmp = (float) Math.pow((double) (brightnessEndOrig / brightnessStartOrig), (double) this.mOffsetBrightenRatioLeft);
            float f = this.mOffsetBrightenAlphaLeft;
            darkenRatio = ((f * brightnessEndOrig) / brightnessStartOrig) + ((1.0f - f) * darkenRatioTmp);
        }
        float darkenRatio2 = getBrightenOffsetDarkenRatio(darkenRatio, brightnessStartOrig, brightnessEndOrig, brightnessStartNew);
        if (((int) this.mLastBrightnessEndOrig) != ((int) brightnessEndOrig) && HWFLOW) {
            Slog.i(TAG, "darkenRatio=" + darkenRatio2 + ",darkenRatioTmp=" + darkenRatioTmp + ",mOffsetBrightenAlphaLeft=" + this.mOffsetBrightenAlphaLeft);
        }
        return darkenRatio2;
    }

    public float getAmbientValueFromDb() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "getAmbientValueFromDb failed, mContentResolver==null");
            return 0.0f;
        }
        float ambientValue = Settings.System.getFloatForUser(contentResolver, "spline_ambient_lux", 0.0f, this.mCurrentUserId);
        if (((int) ambientValue) < 0) {
            Slog.e(TAG, "error inputValue<min,ambientValue=" + ambientValue);
            ambientValue = 0.0f;
        }
        if (((int) ambientValue) <= 40000) {
            return ambientValue;
        }
        Slog.e(TAG, "error inputValue>max,ambientValue=" + ambientValue);
        return AMBIENT_VALID_MAX_LUX;
    }

    public boolean isCalibrationTestEnable() {
        ContentResolver contentResolver = this.mContentResolver;
        boolean isCalibrationTestEnable = false;
        if (contentResolver == null) {
            Slog.w(TAG, "isCalibrationTestEnable failed, mContentResolver==null");
            return false;
        }
        int calibrationTest = Settings.System.getIntForUser(contentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
        if (calibrationTest == 0) {
            this.mIsCalibrationModeBeforeEnable = false;
            return false;
        }
        int calibrationTestLow = calibrationTest & 65535;
        int calibrationTestHigh = 65535 & (calibrationTest >> 16);
        if (calibrationTestLow != calibrationTestHigh) {
            Slog.e(TAG, "error db, clear DB,calibrationTestLow=" + calibrationTestLow + ",calibrationTestHigh=" + calibrationTestHigh);
            Settings.System.putIntForUser(this.mContentResolver, "spline_calibration_test", 0, this.mCurrentUserId);
            this.mIsCalibrationModeBeforeEnable = false;
            return false;
        }
        int calibrationModeBeforeEnableInt = (calibrationTestLow >> 1) & 1;
        int calibrationTestEnableInt = calibrationTestLow & 1;
        this.mIsCalibrationModeBeforeEnable = calibrationModeBeforeEnableInt == 1;
        if (calibrationTestEnableInt == 1) {
            isCalibrationTestEnable = true;
        }
        if (calibrationTest != this.mCalibrationTest) {
            this.mCalibrationTest = calibrationTest;
            if (HWFLOW) {
                Slog.d(TAG, "mCalibrationTest=" + this.mCalibrationTest + ",calibrationTestEnableInt=" + calibrationTestEnableInt + ",calibrationModeBeforeEnableInt=" + calibrationModeBeforeEnableInt);
            }
        }
        return isCalibrationTestEnable;
    }

    public void setEyeProtectionControlFlag(boolean isInControlTime) {
        HwEyeProtectionSpline hwEyeProtectionSpline = this.mEyeProtectionSpline;
        if (hwEyeProtectionSpline != null) {
            hwEyeProtectionSpline.setEyeProtectionControlFlag(isInControlTime);
        }
    }

    public void setReadingModeEnable(boolean isReadingModeEnable) {
        this.mIsReadingModeEnable = isReadingModeEnable;
    }

    public void setNoOffsetEnable(boolean isCoverModeClearOffsetEnable) {
        this.mIsCoverModeClearOffsetEnable = isCoverModeClearOffsetEnable;
        if (HWFLOW) {
            Slog.i(TAG, "LabcCoverMode CoverModeNoOffsetEnable=" + this.mIsCoverModeClearOffsetEnable);
        }
    }

    public void setCameraModeEnable(boolean isCameraModeEnable) {
        this.mIsCameraModeEnable = isCameraModeEnable;
    }

    public void setPowerSavingModeEnable(boolean isPowerSavingModeEnable) {
        this.mIsPowerSavingModeEnable = isPowerSavingModeEnable;
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

    public void setDayModeEnable(boolean isDayModeEnable) {
        this.mIsDayModeEnable = isDayModeEnable;
    }

    public void reSetOffsetFromHumanFactor(boolean isOffsetResetEnable, int minOffsetBrightness, int maxOffsetBrightness) {
        if (this.mIsBrightnessOffsetTmpValidEnable && this.mIsErrorCorrectionOffsetNeedClear && isOffsetResetEnable) {
            this.mIsErrorCorrectionOffsetNeedClear = false;
            clearBrightnessOffset();
        } else if (this.mIsDarkModeBrightnessEnable && isOffsetResetEnable && Math.abs(this.mPosBrightness) < ((float) this.mDarkModeMinOffsetBrightness) && Math.abs(this.mPosBrightness) > SMALL_VALUE) {
            Slog.i(TAG, "updateLevel: clear offset on DarkBrightMode, mPosBrightness=" + this.mPosBrightness);
            clearBrightnessOffset();
        } else if (isOffsetResetEnable && Math.abs(this.mPosBrightness) > SMALL_VALUE) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                if (((float) minOffsetBrightness) > MIN_DEFAULT_BRIGHTNESS) {
                    float f = this.mCalibrationRatio;
                    if (((float) minOffsetBrightness) * f > MIN_DEFAULT_BRIGHTNESS) {
                        minOffsetBrightness = (int) (((float) minOffsetBrightness) * f);
                    }
                }
                if (((float) maxOffsetBrightness) < MAX_DEFAULT_BRIGHTNESS) {
                    float f2 = this.mCalibrationRatio;
                    if (((float) maxOffsetBrightness) * f2 < MAX_DEFAULT_BRIGHTNESS) {
                        maxOffsetBrightness = (int) (((float) maxOffsetBrightness) * f2);
                    }
                }
            }
            if (this.mPosBrightness < ((float) minOffsetBrightness)) {
                this.mPosBrightness = (float) minOffsetBrightness;
                this.mOffsetBrightnessLast = (float) minOffsetBrightness;
                this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
                this.mIsReset = true;
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel:resetMin mPosBrightness=" + this.mPosBrightness + ",min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDelta=" + this.mDelta + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
            if (this.mPosBrightness > ((float) maxOffsetBrightness)) {
                this.mPosBrightness = (float) maxOffsetBrightness;
                this.mOffsetBrightnessLast = (float) maxOffsetBrightness;
                this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
                this.mIsReset = true;
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel:resetMax mPosBrightness=" + this.mPosBrightness + ",min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDelta=" + this.mDelta + ",mAmLuxOffset=" + this.mAmLuxOffset + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
        }
    }

    public void resetGameModeOffsetFromHumanFactor(int minOffsetBrightness, int maxOffsetBrightness) {
        if (Math.abs(this.mDeltaTmp) > SMALL_VALUE) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                if (((float) minOffsetBrightness) > MIN_DEFAULT_BRIGHTNESS) {
                    float f = this.mCalibrationRatio;
                    if (((float) minOffsetBrightness) * f > MIN_DEFAULT_BRIGHTNESS) {
                        minOffsetBrightness = (int) (((float) minOffsetBrightness) * f);
                    }
                }
                if (((float) maxOffsetBrightness) < MAX_DEFAULT_BRIGHTNESS) {
                    float f2 = this.mCalibrationRatio;
                    if (((float) maxOffsetBrightness) * f2 < MAX_DEFAULT_BRIGHTNESS) {
                        maxOffsetBrightness = (int) (((float) maxOffsetBrightness) * f2);
                    }
                }
            }
            float f3 = this.mGameModeStartLuxDefaultBrightness;
            float positionBrightness = this.mDeltaTmp + f3;
            if (positionBrightness < ((float) minOffsetBrightness)) {
                this.mGameModePosBrightness = (float) minOffsetBrightness;
                this.mDeltaTmp = ((float) minOffsetBrightness) - f3;
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel GameMode:resetMin, min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
            if (positionBrightness > ((float) maxOffsetBrightness)) {
                this.mGameModePosBrightness = (float) maxOffsetBrightness;
                this.mDeltaTmp = ((float) maxOffsetBrightness) - this.mGameModeStartLuxDefaultBrightness;
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel GameMode:resetMax, min=" + minOffsetBrightness + ",max=" + maxOffsetBrightness + ",mDeltaTmp=" + this.mDeltaTmp + ",mGameModeOffsetLux=" + this.mGameModeOffsetLux + ",mCalibrationRatio=" + this.mCalibrationRatio);
                }
            }
        }
    }

    public void resetGameBrightnessLimitation() {
        this.mLastGameModeBrightness = DEFAULT_NO_OFFSET_LUX;
        if (HWFLOW) {
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
            List<PointF> list = this.mDefaultBrightnessLinePointsList;
            if (list != null) {
                float defaultBrightness0LuxLevel = list.get(0).y;
                int i3 = this.mDarkAdaptingBrightness0LuxLevel;
                if (((float) i3) > defaultBrightness0LuxLevel) {
                    Slog.w(TAG, "fillDarkAdaptPointsList() error adapting=" + this.mDarkAdaptingBrightness0LuxLevel + " is larger than default=" + defaultBrightness0LuxLevel);
                    return;
                }
                this.mDarkAdaptingBrightnessPointsList = cloneListAndReplaceFirstElement(this.mDefaultBrightnessLinePointsList, new PointF(0.0f, (float) i3));
                this.mDarkAdaptedBrightnessPointsList = cloneListAndReplaceFirstElement(this.mDefaultBrightnessLinePointsList, new PointF(0.0f, (float) this.mDarkAdaptedBrightness0LuxLevel));
                this.mIsDarkAdaptEnable = true;
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
        if (!this.mIsDarkAdaptLineLocked && this.mDarkAdaptState != this.mDarkAdaptStateDetected) {
            if (HWFLOW) {
                Slog.i(TAG, "updateDarkAdaptState() " + this.mDarkAdaptState + " -> " + this.mDarkAdaptStateDetected);
            }
            this.mDarkAdaptState = this.mDarkAdaptStateDetected;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.util.HwNormalizedSpline$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
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
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<PointF> getCurrentDarkAdaptLine() {
        int i = AnonymousClass4.$SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[this.mDarkAdaptState.ordinal()];
        if (i == 1) {
            return this.mDefaultBrightnessLinePointsList;
        }
        if (i == 2) {
            return this.mDarkAdaptingBrightnessPointsList;
        }
        if (i != 3) {
            return this.mDefaultBrightnessLinePointsList;
        }
        return this.mDarkAdaptedBrightnessPointsList;
    }

    private float getDarkAdaptOffset(float positionBrightness, float lux) {
        float offsetMinLimit;
        float currentOffset = getOffsetBrightnessLevelNew(this.mStartLuxDefaultBrightness, this.mDefaultBrightnessFromLux, positionBrightness);
        if (this.mDelta >= 0.0f) {
            if (HWDEBUG) {
                Slog.d(TAG, String.format(Locale.ENGLISH, "getDarkAdaptOffset() mDelta=%.1f, current=%.1f", Float.valueOf(this.mDelta), Float.valueOf(currentOffset)));
            }
            return currentOffset;
        } else if (this.mIsDarkAdaptLineLocked) {
            if (HWDEBUG) {
                Slog.d(TAG, String.format(Locale.ENGLISH, "getDarkAdaptOffset() locked, current=%.1f", Float.valueOf(currentOffset)));
            }
            return currentOffset;
        } else {
            int i = AnonymousClass4.$SwitchMap$android$util$HwNormalizedSpline$DarkAdaptState[this.mDarkAdaptState.ordinal()];
            if (i == 1) {
                offsetMinLimit = getDefaultBrightnessLevelNew(this.mDarkAdaptingBrightnessPointsList, lux);
            } else if (i == 2) {
                offsetMinLimit = (getDefaultBrightnessLevelNew(this.mDarkAdaptingBrightnessPointsList, lux) + getDefaultBrightnessLevelNew(this.mDarkAdaptedBrightnessPointsList, lux)) / 2.0f;
            } else if (i != 3) {
                offsetMinLimit = MIN_DEFAULT_BRIGHTNESS;
            } else {
                offsetMinLimit = getDefaultBrightnessLevelNew(this.mDarkAdaptedBrightnessPointsList, lux);
            }
            if (HWDEBUG) {
                Slog.d(TAG, String.format(Locale.ENGLISH, "getDarkAdaptOffset() %s, current = %.1f, minLimit = %.1f", this.mDarkAdaptState, Float.valueOf(currentOffset), Float.valueOf(offsetMinLimit)));
            }
            return currentOffset > offsetMinLimit ? currentOffset : offsetMinLimit;
        }
    }

    public void setDarkAdaptState(DarkAdaptState state) {
        if (this.mIsDarkAdaptEnable && state != null) {
            this.mDarkAdaptStateDetected = state;
        }
    }

    public void unlockDarkAdaptLine() {
        if (this.mIsDarkAdaptEnable && this.mIsDarkAdaptLineLocked) {
            this.mIsDarkAdaptLineLocked = false;
            if (HWFLOW) {
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

    public void setVideoFullScreenModeBrightnessEnable(boolean isVideoFullScreenModeBrightnessEnable) {
        if (this.mIsVideoFullScreenModeBrightnessEnable != isVideoFullScreenModeBrightnessEnable) {
            this.mIsVideoFullScreenModeBrightnessEnable = isVideoFullScreenModeBrightnessEnable;
            if (HWFLOW) {
                Slog.i(TAG, "VideoBrightMode mIsVideoFullScreenModeBrightnessEnable=" + this.mIsVideoFullScreenModeBrightnessEnable);
            }
        }
    }

    public void initVideoFullScreenModeBrightnessPara(boolean isVideoFullScreenModeEnable, List<PointF> videoFullScreenModeList) {
        Slog.i(TAG, "initVideoFullScreenModeBrightnessPara isVideoFullScreenModeEnable=" + isVideoFullScreenModeEnable);
        if (isVideoFullScreenModeEnable && videoFullScreenModeList != null) {
            this.mIsVideoFullScreenModeEnable = isVideoFullScreenModeEnable;
            initVideoFullScreenModeBrightnessLinePointsList(videoFullScreenModeList);
        }
    }

    private void initVideoFullScreenModeBrightnessLinePointsList(List<PointF> videoFullScreenModeList) {
        List<PointF> list = this.mVideoFullScreenModeBrightnessLinePointsList;
        if (!(list == null || videoFullScreenModeList == null)) {
            list.clear();
            this.mVideoFullScreenModeBrightnessLinePointsList.addAll(videoFullScreenModeList);
            if (isBrightnessListValid(this.mVideoFullScreenModeBrightnessLinePointsList)) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                    updateNewLinePointsListForCalibration(this.mVideoFullScreenModeBrightnessLinePointsList);
                    if (HWFLOW) {
                        Slog.i(TAG, "update mVideoFullScreenModeBrightnessLinePointsList for calibration");
                    }
                }
                for (PointF point : this.mVideoFullScreenModeBrightnessLinePointsList) {
                    if (HWFLOW) {
                        Slog.i(TAG, "LoadXMLConfig_mVideoFullScreenModeBrightnessLinePointsList x=" + point.x + ",y=" + point.y);
                    }
                }
                return;
            }
            loadVideoFullScreenModeDefaultBrightnessLine();
            Slog.w(TAG, "loadVideoFullScreenModeDefaultBrightnessLine");
        }
    }

    private void loadVideoFullScreenModeDefaultBrightnessLine() {
        List<PointF> list = this.mVideoFullScreenModeBrightnessLinePointsList;
        if (list != null) {
            list.clear();
            this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
            this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
            this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
            this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
            this.mVideoFullScreenModeBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
        }
    }

    public void initDarkModeBrightnessPara(boolean isDarkModeCurveEnable, List<PointF> darkModeList) {
        Slog.i(TAG, "initDarkModeBrightnessPara isDarkModeCurveEnable=" + isDarkModeCurveEnable);
        if (isDarkModeCurveEnable && darkModeList != null) {
            this.mIsDarkModeCurveEnable = isDarkModeCurveEnable;
            initDarkModeBrightnessLinePointsList(darkModeList);
        }
    }

    private void initDarkModeBrightnessLinePointsList(List<PointF> darkModeList) {
        List<PointF> list = this.mDarkModeBrightnessLinePointsList;
        if (!(list == null || darkModeList == null)) {
            list.clear();
            this.mDarkModeBrightnessLinePointsList.addAll(darkModeList);
            if (isBrightnessListValid(this.mDarkModeBrightnessLinePointsList)) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                    updateNewLinePointsListForCalibration(this.mDarkModeBrightnessLinePointsList);
                    if (HWFLOW) {
                        Slog.i(TAG, "update mDarkModeBrightnessLinePointsList for calibration");
                    }
                }
                for (PointF point : this.mDarkModeBrightnessLinePointsList) {
                    if (HWFLOW) {
                        Slog.i(TAG, "LoadXMLConfig_mDarkModeBrightnessLinePointsList x=" + point.x + ",y=" + point.y);
                    }
                }
                return;
            }
            loadDarkModeDefaultBrightnessLine();
            Slog.w(TAG, "loadDarkModeDefaultBrightnessLine");
        }
    }

    private void loadDarkModeDefaultBrightnessLine() {
        List<PointF> list = this.mDarkModeBrightnessLinePointsList;
        if (list != null) {
            list.clear();
            this.mDarkModeBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
            this.mDarkModeBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
            this.mDarkModeBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
            this.mDarkModeBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
            this.mDarkModeBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
        }
    }

    public void initDayModeBrightnessPara(boolean isDayModeNewCurveEnable, List<PointF> dayModeBrightnessLinePoints) {
        Slog.i(TAG, "initDayModeBrightnessPara NewDayModeBrightnessEnable=" + isDayModeNewCurveEnable);
        if (isDayModeNewCurveEnable && dayModeBrightnessLinePoints != null) {
            this.mIsDayModeAlgoEnable = isDayModeNewCurveEnable;
            initDayModeBrightnessLinePointsList(dayModeBrightnessLinePoints);
        }
    }

    public void initHdrModeBrightnessPara(boolean isHdrModeEnable, List<PointF> hdrModeBrightnessLinePoints) {
        Slog.i(TAG, "initHdrModeBrightnessPara hdrModeBrightnessLinePoints,enable=" + isHdrModeEnable);
        if (isHdrModeEnable && hdrModeBrightnessLinePoints != null) {
            this.mIsHdrModeEnable = isHdrModeEnable;
            initHdrModeBrightnessLinePointsList(hdrModeBrightnessLinePoints);
        }
    }

    public void updateHdrModeCurveStatus(boolean isHdrCurveEnable) {
        this.mIsHdrModeCurveEnable = isHdrCurveEnable;
        Slog.i(TAG, "updateHdrModeCurveStatus,mIsHdrModeCurveEnable=" + this.mIsHdrModeCurveEnable);
    }

    private void initHdrModeBrightnessLinePointsList(List<PointF> hdrModeBrightnessLinePointsList) {
        List<PointF> list = this.mHdrModeBrightnessLinePointsList;
        if (!(list == null || hdrModeBrightnessLinePointsList == null)) {
            list.clear();
            this.mHdrModeBrightnessLinePointsList.addAll(hdrModeBrightnessLinePointsList);
            if (isBrightnessListValid(this.mHdrModeBrightnessLinePointsList)) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                    updateNewLinePointsListForCalibration(this.mHdrModeBrightnessLinePointsList);
                    if (HWFLOW) {
                        Slog.i(TAG, "update mHdrModeBrightnessLinePointsList for calibration");
                    }
                }
                for (PointF point : this.mHdrModeBrightnessLinePointsList) {
                    if (HWFLOW) {
                        Slog.i(TAG, "LoadXMLConfig_mHdrModeBrightnessLinePointsList x=" + point.x + ", y = " + point.y);
                    }
                }
                return;
            }
            loadHdrModeDefaultBrightnessLine();
            Slog.w(TAG, "loadHdrModeDefaultBrightnessLine");
        }
    }

    private void loadHdrModeDefaultBrightnessLine() {
        List<PointF> list = this.mHdrModeBrightnessLinePointsList;
        if (list != null) {
            list.clear();
            this.mHdrModeBrightnessLinePointsList.add(new PointF(0.0f, MIN_DEFAULT_BRIGHTNESS));
            this.mHdrModeBrightnessLinePointsList.add(new PointF(25.0f, 46.5f));
            this.mHdrModeBrightnessLinePointsList.add(new PointF(1995.0f, 140.7f));
            this.mHdrModeBrightnessLinePointsList.add(new PointF(4000.0f, MAX_DEFAULT_BRIGHTNESS));
            this.mHdrModeBrightnessLinePointsList.add(new PointF(AMBIENT_VALID_MAX_LUX, MAX_DEFAULT_BRIGHTNESS));
        }
    }

    private void initDayModeBrightnessLinePointsList(List<PointF> dayModeBrightnessList) {
        List<PointF> list = this.mDayBrightnessLinePointsList;
        if (!(list == null || dayModeBrightnessList == null)) {
            list.clear();
            this.mDayBrightnessLinePointsList.addAll(dayModeBrightnessList);
            if (isBrightnessListValid(this.mDayBrightnessLinePointsList)) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                    updateNewLinePointsListForCalibration(this.mDayBrightnessLinePointsList);
                    if (HWFLOW) {
                        Slog.i(TAG, "update NewDayBrightnessLinePointsList for calibration,mCalibrationRatio=" + this.mCalibrationRatio);
                    }
                }
                for (PointF point : this.mDayBrightnessLinePointsList) {
                    if (HWFLOW) {
                        Slog.i(TAG, "LoadXMLConfig_NewDayBrightnessLinePointsList x=" + point.x + ",y=" + point.y);
                    }
                }
                if (this.mIsOminLevelModeEnable) {
                    getOminLevelBrightnessLinePoints();
                    return;
                }
                return;
            }
            loadDefaultConfig();
            Slog.w(TAG, "loadDefaultConfig");
        }
    }

    public void initBrightenOffsetLux(boolean isBrightnessOffsetLuxModeEnable, float brightenOffsetLuxTh1, float brightenOffsetLuxTh2, float brightenOffsetLuxTh3) {
        this.mIsBrightnessOffsetLuxModeEnable = isBrightnessOffsetLuxModeEnable;
        this.mBrightenOffsetLuxTh1 = brightenOffsetLuxTh1;
        this.mBrightenOffsetLuxTh2 = brightenOffsetLuxTh2;
        this.mBrightenOffsetLuxTh3 = brightenOffsetLuxTh3;
        if (HWFLOW) {
            Slog.i(TAG, "initBrightnessOffsetPara,OffsetLuxModeEnable=" + isBrightnessOffsetLuxModeEnable);
        }
    }

    public void initBrightenOffsetNoValidDarkenLux(boolean isBrightenOffsetEffectMinLuxEnable, float brightenOffsetNoValidDarkenLuxTh1, float brightenOffsetNoValidDarkenLuxTh2, float brightenOffsetNoValidDarkenLuxTh3, float brightenOffsetNoValidDarkenLuxTh4) {
        this.mIsBrightenOffsetEffectMinLuxEnable = isBrightenOffsetEffectMinLuxEnable;
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

    public void initBrightnessOffsetTmpValidPara(boolean isBrightnessOffsetTmpValidEnable, float brightenOffsetNoValidSavedLuxTh1, float brightenOffsetNoValidSavedLuxTh2) {
        this.mIsBrightnessOffsetTmpValidEnable = isBrightnessOffsetTmpValidEnable;
        this.mBrightenOffsetNoValidSavedLuxTh1 = brightenOffsetNoValidSavedLuxTh1;
        this.mBrightenOffsetNoValidSavedLuxTh2 = brightenOffsetNoValidSavedLuxTh2;
        if (HWFLOW) {
            Slog.i(TAG, "initBrightnessOffsetPara,OffsetTmpValidEnable=" + this.mIsBrightnessOffsetTmpValidEnable);
        }
    }

    private float getBrightenOffsetBrightenRaio(float ratio, float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew, float delta) {
        float brightenRatio = ratio;
        float ambientLuxOffset = (float) ((int) this.mAmLuxOffset);
        if (getBrightnessMode() == BrightnessModeState.GAME_MODE) {
            ambientLuxOffset = (float) ((int) this.mGameModeOffsetLux);
        }
        if (this.mIsBrightnessOffsetLuxModeEnable && ambientLuxOffset >= 0.0f && Math.abs(this.mPosBrightness) > SMALL_VALUE) {
            float noValidBrightenLuxTh = getBrightenOffsetNoValidBrightenLux(ambientLuxOffset);
            float brightenOffsetDelta = 0.0f;
            if (noValidBrightenLuxTh >= 0.0f) {
                brightenOffsetDelta = getCurrentBrightness(noValidBrightenLuxTh) - MAX_DEFAULT_BRIGHTNESS;
            }
            if (this.mPosBrightness - MAX_DEFAULT_BRIGHTNESS > brightenOffsetDelta) {
                if (delta < 1.0f) {
                    brightenRatio = 1.0f;
                } else {
                    brightenRatio = ((brightnessEndOrig > brightnessStartNew ? brightnessEndOrig : brightnessStartNew) - brightnessEndOrig) / (SMALL_VALUE + delta);
                }
            } else if ((MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig) + brightenOffsetDelta < 1.0f) {
                brightenRatio = 1.0f;
            } else {
                brightenRatio = ((MAX_DEFAULT_BRIGHTNESS - brightnessEndOrig) + brightenOffsetDelta) / (((MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig) + SMALL_VALUE) + brightenOffsetDelta);
            }
            if (brightenRatio < 0.0f || brightenRatio > 1.0f) {
                brightenRatio = 0.0f;
            }
            if (((int) this.mLastBrightnessEndOrigIn) != ((int) brightnessEndOrig)) {
                this.mLastBrightnessEndOrigIn = brightnessEndOrig;
                if (HWFLOW) {
                    Slog.i(TAG, "BrightenOffset origRatio=" + ratio + "-->brightenRatio=" + brightenRatio + ",mode=" + getBrightnessMode() + ",noValidBrightenLuxTh=" + noValidBrightenLuxTh + ",mPosBrightness=" + this.mPosBrightness + ",BrightenOffsetBrightnessMax=" + (MAX_DEFAULT_BRIGHTNESS + brightenOffsetDelta) + ",ambientLuxOffset=" + ambientLuxOffset);
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
        if (this.mIsBrightnessOffsetLuxModeEnable && ambientLuxOffset >= 0.0f && Math.abs(this.mPosBrightness) > SMALL_VALUE) {
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
                darkenOffsetDelta = getCurrentBrightness(noValidBrightenLuxTh) - MAX_DEFAULT_BRIGHTNESS;
            }
            if ((MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig) + darkenOffsetDelta < 1.0f) {
                brightenRatio = 1.0f;
            } else {
                brightenRatio = ((MAX_DEFAULT_BRIGHTNESS - brightnessEndOrig) + darkenOffsetDelta) / (((MAX_DEFAULT_BRIGHTNESS - brightnessStartOrig) + darkenOffsetDelta) + SMALL_VALUE);
            }
            if (brightenRatio < 0.0f || brightenRatio > 1.0f) {
                brightenRatio = 0.0f;
            }
            if (((int) this.mLastBrightnessEndOrigIn) != ((int) brightnessEndOrig)) {
                this.mLastBrightnessEndOrigIn = brightnessEndOrig;
                if (HWFLOW) {
                    Slog.i(TAG, "DarkenOffset origRatio=" + ratio + "-->brightenRatio=" + brightenRatio + ",mode=" + getBrightnessMode() + ",noValidBrightenLuxTh=" + noValidBrightenLuxTh + ",DarkenOffsetBrightnessMin=" + (MAX_DEFAULT_BRIGHTNESS + darkenOffsetDelta) + ",ambientLuxOffset=" + ambientLuxOffset);
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
        if (this.mIsBrightnessOffsetLuxModeEnable && ambientLuxOffset >= 0.0f && Math.abs(this.mPosBrightness) > SMALL_VALUE) {
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
            if (noValidBrightenLuxTh > 0.0f || (noValidBrightenLuxTh == 0.0f && !this.mIsBrightenOffsetEffectMinLuxEnable)) {
                brightenOffsetBrightnessMin = getCurrentBrightness(noValidBrightenLuxTh);
            }
            if (brightnessStartOrig < SMALL_VALUE) {
                darkenRatio = 1.0f;
            } else {
                darkenRatio = (brightnessEndOrig - brightenOffsetBrightnessMin) / brightnessStartOrig;
            }
            if (darkenRatio < 0.0f || darkenRatio > 1.0f) {
                darkenRatio = 0.0f;
            }
            if (((int) this.mLastBrightnessEndOrigIn) != ((int) brightnessEndOrig)) {
                this.mLastBrightnessEndOrigIn = brightnessEndOrig;
                if (HWFLOW) {
                    Slog.i(TAG, "BrightenOffset OrigRatio=" + ratio + "-->darkenRatio=" + darkenRatio + ",mode=" + getBrightnessMode() + ",noValidBrightenLuxTh=" + noValidBrightenLuxTh + ",BrightenOffsetBrightnessMin=" + brightenOffsetBrightnessMin + ",mEffectMinLuxEnable=" + this.mIsBrightenOffsetEffectMinLuxEnable + ",ambientLuxOffset=" + ambientLuxOffset);
                }
            }
        }
        return darkenRatio;
    }

    private void checkErrorCorrectionOffset() {
        this.mIsErrorCorrectionOffsetNeedClear = false;
        if (this.mIsBrightnessOffsetTmpValidEnable) {
            float ambientLuxOffset = (float) ((int) this.mAmLuxOffset);
            if (Math.abs(this.mPosBrightness) > SMALL_VALUE && this.mDelta > 0.0f && ambientLuxOffset >= this.mBrightenOffsetNoValidSavedLuxTh1 && ambientLuxOffset < this.mBrightenOffsetNoValidSavedLuxTh2) {
                this.mIsErrorCorrectionOffsetNeedClear = true;
                if (HWFLOW) {
                    Slog.i(TAG, "updateLevel ErrorCorrectOffset, OffsetNeedClear=" + this.mIsErrorCorrectionOffsetNeedClear);
                }
            }
        }
    }

    public void updateDarkModeBrightness(boolean isDarkModeBrightnessEnable, int minOffsetBrightness) {
        if (HWFLOW) {
            Slog.i(TAG, "DarkBrightMode mIsDarkModeBrightnessEnable=" + this.mIsDarkModeBrightnessEnable + "->enable=" + isDarkModeBrightnessEnable + ",minOffsetBrightness=" + minOffsetBrightness);
        }
        this.mIsDarkModeBrightnessEnable = isDarkModeBrightnessEnable;
        this.mDarkModeMinOffsetBrightness = minOffsetBrightness;
    }

    private void loadTwoPointOffsetParameters() {
        ContentResolver contentResolver = this.mContentResolver;
        if (contentResolver == null) {
            Slog.w(TAG, "mContentResolver==null, no need loadTwoPointOffsetParameters");
            return;
        }
        this.mAmLuxOffsetLowSaved = Settings.System.getFloatForUser(contentResolver, "spline_two_point_offset_lowlux", DEFAULT_NO_OFFSET_LUX, this.mCurrentUserId);
        this.mAmLuxOffsetLow = this.mAmLuxOffsetLowSaved;
        this.mPosBrightnessLowSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_two_point_offset_lowlux_level", 0.0f, this.mCurrentUserId);
        this.mPosBrightnessLow = this.mPosBrightnessLowSaved;
        this.mAmLuxOffsetHighSaved = Settings.System.getFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux", DEFAULT_NO_OFFSET_LUX, this.mCurrentUserId);
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
            if (HWFLOW) {
                Slog.i(TAG, "twoPointOffset saved mAmLuxOffsetLow=" + this.mAmLuxOffsetLow);
            }
            this.mAmLuxOffsetLowSaved = this.mAmLuxOffsetLow;
        }
        float f2 = this.mPosBrightnessLow;
        if (f2 != this.mPosBrightnessLowSaved) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_two_point_offset_lowlux_level", f2, this.mCurrentUserId);
            if (HWFLOW) {
                Slog.i(TAG, "twoPointOffset saved mPosBrightnessLow=" + this.mPosBrightnessLow);
            }
            this.mPosBrightnessLowSaved = this.mPosBrightnessLow;
        }
        float f3 = this.mAmLuxOffsetHigh;
        if (f3 != this.mAmLuxOffsetHighSaved) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux", f3, this.mCurrentUserId);
            if (HWFLOW) {
                Slog.i(TAG, "twoPointOffset saved mAmLuxOffsetHigh=" + this.mAmLuxOffsetHigh);
            }
            this.mAmLuxOffsetHighSaved = this.mAmLuxOffsetHigh;
        }
        float f4 = this.mPosBrightnessHigh;
        if (f4 != this.mPosBrightnessHighSaved) {
            Settings.System.putFloatForUser(this.mContentResolver, "spline_two_point_offset_highlux_level", f4, this.mCurrentUserId);
            if (HWFLOW) {
                Slog.i(TAG, "twoPointOffset saved mPosBrightnessHigh=" + this.mPosBrightnessHigh);
            }
            this.mPosBrightnessHighSaved = this.mPosBrightnessHigh;
        }
    }

    private void updateTwoPointOffset(float posBrightness, float offsetLux) {
        if (this.mIsTwoPointOffsetEnable) {
            boolean z = false;
            if (Math.abs(posBrightness) < SMALL_VALUE) {
                this.mIsVehicleModeKeepMinBrightnessEnable = false;
                clearTwoPointOffset();
            } else if (offsetLux < 0.0f) {
                Slog.w(TAG, "no updateTwoPointOffset,offsetLux < 0, offsetLux=" + offsetLux);
            } else if (posBrightness > 0.0f && offsetLux >= 0.0f) {
                float defaultBrightness = getCurrentBrightness(offsetLux);
                float brightnessDelta = posBrightness - defaultBrightness;
                if (offsetLux < this.mAmLuxOffsetTh) {
                    if (this.mDelta == 0.0f) {
                        Slog.i(TAG, "updateLevel mIsTwoPointOffsetLowResetEnable=" + this.mIsTwoPointOffsetLowResetEnable);
                        this.mIsTwoPointOffsetLowResetEnable = true;
                    }
                    if (this.mIsVehicleModeEnable) {
                        if (!this.mIsVehicleModeBrightnessEnable && posBrightness < this.mVehicleModeBrightness) {
                            z = true;
                        }
                        this.mIsVehicleModeKeepMinBrightnessEnable = z;
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
                Slog.i(TAG, "updateLevel mPosBrightness=" + this.mPosBrightness + ",mAmLuxOffset=" + this.mAmLuxOffset + ",defaultBrightness=" + defaultBrightness + ",brightnessDelta=" + (posBrightness - defaultBrightness) + ",mIsVeModeKeepMinBrightnessEnable=" + this.mIsVehicleModeKeepMinBrightnessEnable + getTwoPointOffsetStrings());
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
                } else if (HWFLOW) {
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
                } else if (HWFLOW) {
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
        if (HWFLOW) {
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
        this.mAmLuxOffsetTmp = DEFAULT_NO_OFFSET_LUX;
    }

    private void clearLowOffset() {
        this.mPosBrightnessLow = 0.0f;
        this.mAmLuxOffsetLow = DEFAULT_NO_OFFSET_LUX;
    }

    private void clearHighOffset() {
        this.mPosBrightnessHigh = 0.0f;
        this.mAmLuxOffsetHigh = DEFAULT_NO_OFFSET_LUX;
    }

    private float getInterpolatedValueFromTwoPointOffset(float positionBrightness, float lux) {
        float noValidBrightnessLowDarkHighBright;
        boolean isNeedUpdateLowDarkHighBright;
        float noValidBrightnessLowBrightHighDark;
        boolean isNeedUpdateLowBrightHighDark;
        float defaultBrightness = getCurrentBrightness(lux);
        if (this.mAmLuxOffsetLow == DEFAULT_NO_OFFSET_LUX && this.mAmLuxOffsetHigh == DEFAULT_NO_OFFSET_LUX) {
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
                    if (HWFLOW && this.mLastLuxForTwoPointOffset != lux) {
                        Slog.i(TAG, "offsetBrightness=" + offsetBrightness + ",defaultBrightness=" + defaultBrightness + ",lux=" + lux + ",offsetLow=" + offsetBrightnessLow + ",offsetHigh=" + offsetBrightnessHigh + ",mIsVeModeKeepMinBrightnessEnable=" + this.mIsVehicleModeKeepMinBrightnessEnable + getTwoPointOffsetStrings());
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
        Slog.i(TAG, "offsetBrightness=" + offsetBrightness2 + ",defaultBrightness=" + defaultBrightness + ",lux=" + lux + ",offsetLow=" + offsetBrightnessLow2 + ",offsetHigh=" + offsetBrightnessHigh2 + ",mIsVeModeKeepMinBrightnessEnable=" + this.mIsVehicleModeKeepMinBrightnessEnable + getTwoPointOffsetStrings());
        this.mLastLuxForTwoPointOffset = lux;
        return offsetBrightness2;
    }

    private String getTwoPointOffsetStrings() {
        return "[luxLow=" + this.mAmLuxOffsetLow + ",Level=" + this.mPosBrightnessLow + "][luxHigh=" + this.mAmLuxOffsetHigh + ",Level=" + this.mPosBrightnessHigh + "][luxTmp=" + this.mAmLuxOffsetTmp + ",Level=" + this.mPosBrightnessTmp + "]";
    }

    private float getLowLuxOffsetBrightness(float lux, boolean isNeedUpdateLowBrightHighDark, float noValidBrightnessLowBrightHighDark, boolean isNeedUpdateLowDarkHighBright, float noValidBrightnessLowDarkHighBright) {
        float offsetBrightnessLow = getCurrentBrightness(lux);
        if (this.mIsVehicleModeEnable && this.mIsVehicleModeBrightnessEnable && this.mIsVehicleModeKeepMinBrightnessEnable) {
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
                noOffsetBrightness = MAX_DEFAULT_BRIGHTNESS;
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
        if (!HWFLOW || this.mLastLuxForTwoPointOffset == lux) {
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
                noOffsetBrightness = MAX_DEFAULT_BRIGHTNESS;
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
        if (HWFLOW && this.mLastLuxForTwoPointOffset != lux) {
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
            noOffsetBrightness = getCurrentBrightness(this.mLowBrightenOffsetNoValidDarkenLuxTh) - MIN_DEFAULT_BRIGHTNESS;
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
        if (HWFLOW && this.mLastLuxForTwoPointOffset != lux) {
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
        if (!HWFLOW || this.mLastLuxForTwoPointOffset == lux) {
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
            noOffsetBrightness = MIN_DEFAULT_BRIGHTNESS;
        } else {
            noOffsetBrightness = (-brightnessDelta) / brightnessStartOrig > this.mLowDarkenOffsetDarkenBrightnessRatio ? 4.0f : getCurrentBrightness(this.mLowDarkenOffsetNoValidDarkenLuxTh);
        }
        if (posBrightness <= getCurrentBrightness(this.mLowDarkenOffsetNoValidDarkenLuxTh)) {
            noOffsetBrightness = getCurrentBrightness(this.mLowDarkenOffsetNoValidDarkenLuxTh);
        }
        if (posBrightness <= MIN_DEFAULT_BRIGHTNESS) {
            return MIN_DEFAULT_BRIGHTNESS;
        }
        return noOffsetBrightness;
    }

    private float getTwoPointOffsetBrightness(float defaultBrightness, float lowOffsetBrightness, float highOffsetBrightnss, float noValidBrightnessLowDarkHighBright, float noValidBrightnessLowBrightHighDark) {
        if (this.mAmLuxOffsetLow >= 0.0f && this.mPosBrightnessLow != 0.0f && this.mAmLuxOffsetHigh >= 0.0f && this.mPosBrightnessHigh != 0.0f) {
            return getLowHightBrightness(defaultBrightness, lowOffsetBrightness, highOffsetBrightnss, noValidBrightnessLowDarkHighBright, noValidBrightnessLowBrightHighDark);
        }
        if (this.mAmLuxOffsetLow < 0.0f || this.mPosBrightnessLow == 0.0f || this.mAmLuxOffsetHigh != DEFAULT_NO_OFFSET_LUX) {
            return (this.mAmLuxOffsetHigh < 0.0f || this.mPosBrightnessHigh == 0.0f || this.mAmLuxOffsetLow != DEFAULT_NO_OFFSET_LUX) ? defaultBrightness : highOffsetBrightnss;
        }
        return lowOffsetBrightness;
    }

    private float getLowHightBrightness(float defaultBrightness, float lowOffsetBrightness, float highOffsetBrightnss, float noValidBrightnessLowDarkHighBright, float noValidBrightnessLowBrightHighDark) {
        float dLow = this.mPosBrightnessLow - getCurrentBrightness(this.mAmLuxOffsetLow);
        float dHigh = this.mPosBrightnessHigh - getCurrentBrightness(this.mAmLuxOffsetHigh);
        if (this.mIsVehicleModeEnable && this.mIsVehicleModeBrightnessEnable && this.mIsVehicleModeKeepMinBrightnessEnable) {
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

    public void initTwoPointOffsetPara(boolean isTwoPointOffsetEnable, float twoPointOffsetLuxTh, float twoPointOffsetAdjionLuxTh, float twoPointOffsetNoValidLuxTh) {
        if (isTwoPointOffsetEnable) {
            this.mIsTwoPointOffsetEnable = isTwoPointOffsetEnable;
            this.mAmLuxOffsetTh = twoPointOffsetLuxTh;
            this.mAmLuxOffsetLowHighDelta = twoPointOffsetAdjionLuxTh;
            this.mTwoPointOffsetNoValidLuxTh = twoPointOffsetNoValidLuxTh;
            updateTwoPointOffsetPara();
            Slog.i(TAG, "init TwoPointOffsetPara mIsTwoPointOffsetEnable=" + this.mIsTwoPointOffsetEnable + ",mAmLuxOffsetTh=" + this.mAmLuxOffsetTh + ",mAmLuxOffsetLowHighDelta=" + this.mAmLuxOffsetLowHighDelta + ",mTwoPointOffsetNoValidLuxTh=" + this.mTwoPointOffsetNoValidLuxTh);
        }
    }

    public void initTwoPointOffsetLowLuxPara(float lowBrightenOffsetNoValidBrightenLuxTh, float lowDarkenOffsetNoValidBrightenLuxTh, float lowBrightenOffsetNoValidDarkenLuxTh, float lowDarkenOffsetNoValidDarkenLuxTh, float lowDarkenOffsetDarkenBrightnessRatio) {
        if (this.mIsTwoPointOffsetEnable) {
            this.mLowBrightenOffsetNoValidBrightenLuxTh = lowBrightenOffsetNoValidBrightenLuxTh;
            this.mLowDarkenOffsetNoValidBrightenLuxTh = lowDarkenOffsetNoValidBrightenLuxTh;
            this.mLowBrightenOffsetNoValidDarkenLuxTh = lowBrightenOffsetNoValidDarkenLuxTh;
            this.mLowDarkenOffsetNoValidDarkenLuxTh = lowDarkenOffsetNoValidDarkenLuxTh;
            this.mLowDarkenOffsetDarkenBrightnessRatio = lowDarkenOffsetDarkenBrightnessRatio;
            Slog.i(TAG, "init TwoPointOffsetPara mLowBrightenOffsetNoValidBrightenLuxTh=" + this.mLowBrightenOffsetNoValidBrightenLuxTh + ",mLowDarkenOffsetNoValidBrightenLuxTh=" + this.mLowDarkenOffsetNoValidBrightenLuxTh + ",mLowBrightenOffsetNoValidDarkenLuxTh=" + this.mLowBrightenOffsetNoValidDarkenLuxTh + ",mLowDarkenOffsetNoValidDarkenLuxTh=" + this.mLowDarkenOffsetNoValidDarkenLuxTh + ",mLowDarkenOffsetDarkenBrightnessRatio=" + this.mLowDarkenOffsetDarkenBrightnessRatio);
        }
    }

    public void initTwoPointOffsetHighLuxPara(float highBrightenOffsetNoValidBrightenLuxTh, float highDarkenOffsetNoValidBrightenLuxTh, float highBrightenOffsetNoValidDarkenLuxTh, float highDarkenOffsetNoValidDarkenLuxTh) {
        if (this.mIsTwoPointOffsetEnable) {
            this.mHighBrightenOffsetNoValidBrightenLuxTh = highBrightenOffsetNoValidBrightenLuxTh;
            this.mHighDarkenOffsetNoValidBrightenLuxTh = highDarkenOffsetNoValidBrightenLuxTh;
            this.mHighBrightenOffsetNoValidDarkenLuxTh = highBrightenOffsetNoValidDarkenLuxTh;
            this.mHighDarkenOffsetNoValidDarkenLuxTh = highDarkenOffsetNoValidDarkenLuxTh;
            Slog.i(TAG, "init TwoPointOffsetPara mHighBrightenOffsetNoValidBrightenLuxTh=" + this.mHighBrightenOffsetNoValidBrightenLuxTh + ",mHighDarkenOffsetNoValidBrightenLuxTh=" + this.mHighDarkenOffsetNoValidBrightenLuxTh + ",mHighBrightenOffsetNoValidDarkenLuxTh=" + this.mHighBrightenOffsetNoValidDarkenLuxTh + ",mHighDarkenOffsetNoValidDarkenLuxTh=" + this.mHighDarkenOffsetNoValidDarkenLuxTh);
        }
    }

    public float getCurrentLowAmbientLuxForTwoPointOffset() {
        if (!this.mIsTwoPointOffsetEnable) {
            return DEFAULT_NO_OFFSET_LUX;
        }
        return this.mAmLuxOffsetLow;
    }

    public float getCurrentHighAmbientLuxForTwoPointOffset() {
        if (!this.mIsTwoPointOffsetEnable) {
            return DEFAULT_NO_OFFSET_LUX;
        }
        return this.mAmLuxOffsetHigh;
    }

    public float getCurrentTmpAmbientLuxForTwoPointOffset() {
        if (!this.mIsTwoPointOffsetEnable) {
            return DEFAULT_NO_OFFSET_LUX;
        }
        return this.mAmLuxOffsetTmp;
    }

    public void resetTwoPointOffsetLowFromHumanFactor(boolean isResetOffsetEnable, int minBrightness, int maxBrightness) {
        if (!isResetOffsetEnable || !this.mIsTwoPointOffsetEnable || !this.mIsTwoPointOffsetLowResetEnable) {
            float minOffsetBrightness = (float) minBrightness;
            float maxOffsetBrightness = (float) maxBrightness;
            if (isResetOffsetEnable && this.mIsTwoPointOffsetEnable && this.mPosBrightnessLow > 0.0f && this.mAmLuxOffsetLow >= 0.0f) {
                if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                    if (minOffsetBrightness > ((float) minBrightness)) {
                        float f = this.mCalibrationRatio;
                        if (minOffsetBrightness * f > ((float) minBrightness)) {
                            minOffsetBrightness = (float) ((int) (f * minOffsetBrightness));
                        }
                    }
                    if (maxOffsetBrightness < ((float) maxBrightness)) {
                        float f2 = this.mCalibrationRatio;
                        if (maxOffsetBrightness * f2 < ((float) maxBrightness)) {
                            maxOffsetBrightness = (float) ((int) (f2 * maxOffsetBrightness));
                        }
                    }
                }
                if (this.mPosBrightnessLow < minOffsetBrightness) {
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetLowFromHumanFactor mPosLow=" + this.mPosBrightnessLow + "-->minOffsetBrightness=" + minOffsetBrightness + ",mAmLuxOffsetLow=" + this.mAmLuxOffsetLow);
                    this.mPosBrightnessLow = (float) minBrightness;
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
        this.mIsTwoPointOffsetLowResetEnable = false;
        clearLowOffset();
    }

    public void resetTwoPointOffsetHighFromHumanFactor(boolean isResetOffsetEnable, int minBrightness, int maxBrightness) {
        float minOffsetBrightness = (float) minBrightness;
        float maxOffsetBrightness = (float) maxBrightness;
        if (isResetOffsetEnable && this.mIsTwoPointOffsetEnable && this.mPosBrightnessHigh > 0.0f && this.mAmLuxOffsetHigh >= 0.0f) {
            if (Math.abs(this.mCalibrationRatio - 1.0f) > SMALL_VALUE) {
                if (minOffsetBrightness > ((float) minBrightness)) {
                    float f = this.mCalibrationRatio;
                    if (minOffsetBrightness * f > ((float) minBrightness)) {
                        minOffsetBrightness = (float) ((int) (f * minOffsetBrightness));
                    }
                }
                if (maxOffsetBrightness < ((float) maxBrightness)) {
                    float f2 = this.mCalibrationRatio;
                    if (maxOffsetBrightness * f2 < ((float) maxBrightness)) {
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

    public void resetTwoPointOffsetTmpFromHumanFactor(boolean isResetOffsetEnable, int minBrightness, int maxBrightness) {
        if (isResetOffsetEnable && this.mIsTwoPointOffsetEnable) {
            float f = this.mPosBrightnessTmp;
            if (f > 0.0f && this.mAmLuxOffsetTmp >= 0.0f) {
                if (f < ((float) minBrightness)) {
                    clearTmpOffset();
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetTmpFromHumanFactor clearTmp min");
                    this.mPosBrightnessTmp = (float) minBrightness;
                }
                if (this.mPosBrightnessTmp > ((float) maxBrightness)) {
                    clearTmpOffset();
                    Slog.i(TAG, "updateLevel resetTwoPointOffsetTmpFromHumanFactor clearTmp max");
                    this.mPosBrightnessTmp = (float) maxBrightness;
                }
            }
        }
    }

    private void updateTwoPointOffsetPara() {
        float f = this.mPosBrightnessLow;
        if (f > 0.0f) {
            this.mIsVehicleModeKeepMinBrightnessEnable = f < this.mVehicleModeBrightness;
        }
        if (HWFLOW) {
            Slog.i(TAG, "Init mIsVeModeKeepMinBrightnessEnable=" + this.mIsVehicleModeKeepMinBrightnessEnable);
        }
    }
}
