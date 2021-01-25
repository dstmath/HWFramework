package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.HwNormalizedSpline;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.HwBrightnessXmlLoader;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import java.util.Calendar;
import java.util.List;

public class HwBrightnessSceneController {
    private static final int DC_MODE_OFF_NUM = 0;
    private static final int DC_MODE_ON_NUM = 1;
    private static final int DEFAULT_INIT_SCENE_LEVEL = -1;
    private static final long DEFAULT_INIT_SYSTEM_TIME = -1;
    private static final float DEFAUL_OFFSET_LUX = -1.0f;
    private static final int GAME_IS_DISABLE_AUTO_BRIGHTNESS = 29;
    private static final int GAME_IS_DISABLE_AUTO_BRIGHTNESS_CLOSE = 0;
    private static final int GAME_IS_DISABLE_AUTO_BRIGHTNESS_OPEN = 1;
    private static final int GAME_IS_ENABLE_AUTO_BRIGHTNESS = 28;
    private static final int GAME_IS_FRONT_STATE = 27;
    private static final int GAME_MODE_ENTER = 21;
    private static final int GAME_MODE_QUIT = 20;
    private static final int GAME_NOT_FRONT_STATE = 26;
    private static final int HDR_ENTER = 33;
    private static final int HDR_EXIT = 32;
    private static final int HOME_MODE_ENTER = 23;
    private static final int HOME_MODE_QUIT = 22;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final String KEY_DC_BRIGHTNESS_DIMMING_SWITCH = "hw_dc_brightness_dimming_switch";
    private static final String KEY_READING_MODE_SWITCH = "hw_reading_mode_display_switch";
    private static final int MAX_DEFAULT_BRIGHTNESS = 255;
    private static final int MIN_DEFAULT_BRIGHTNESS = 4;
    private static final int MODE_DEFAULT = 0;
    private static final int MODE_TOP_GAME = 1;
    private static final int MSG_FULL_SCREEN_VIDEO = 3;
    private static final int MSG_GAME_IS_DISABLE_AUTO_BRIGHTNESS = 4;
    private static final int MSG_GAME_IS_DISABLE_AUTO_BRIGHTNESS_SET_MODE = 5;
    private static final int MSG_LANDSCAPE_GAME = 6;
    private static final int MSG_UPDATE_AUTO_BRIGHTNESS = 2;
    private static final int MSG_UPDATE_LANDSCAPE = 1;
    private static final int READING_MODE_OFF_NUM = 0;
    private static final int READING_MODE_ON_NUM = 1;
    private static final String TAG = "HwBrightnessSceneController";
    private static final int VEHICLE_MODE_ENTER = 19;
    private static final int VEHICLE_MODE_QUIT = 18;
    private static final int VIDEO_MODE_ENTER = 25;
    private static final int VIDEO_MODE_QUIT = 24;
    private static final int WALK_MODE_ENTER = 17;
    private static final int WALK_MODE_QUIT = 18;
    private Callbacks mCallbacks;
    private final Context mContext;
    private int mCurrentAutoBrightness = 0;
    private int mCurrentDarkMode = 16;
    int mCurrentDisplayMode = 0;
    private int mCurrentUserId = 0;
    private int mCurveLevel = -1;
    private final HwBrightnessXmlLoader.Data mData;
    private DcModeObserver mDcModeObserver = null;
    private DisplayEffectMonitor mDisplayEffectMonitor;
    private final HwFoldScreenManagerEx.FoldDisplayModeListener mFoldDisplayModeListener = new HwFoldScreenManagerEx.FoldDisplayModeListener() {
        /* class com.android.server.display.HwBrightnessSceneController.AnonymousClass2 */

        public void onScreenDisplayModeChange(int displayMode) {
            if (HwBrightnessSceneController.HWDEBUG) {
                Slog.i(HwBrightnessSceneController.TAG, "onScreenDisplayModeChange displayMode=" + displayMode);
            }
            if (HwBrightnessSceneController.this.mCurrentDisplayMode != displayMode) {
                if (HwBrightnessSceneController.HWFLOW) {
                    Slog.i(HwBrightnessSceneController.TAG, "mCurrentDisplayMode=" + HwBrightnessSceneController.this.mCurrentDisplayMode + "-->displayMode=" + displayMode);
                }
                HwBrightnessSceneController hwBrightnessSceneController = HwBrightnessSceneController.this;
                hwBrightnessSceneController.mCurrentDisplayMode = displayMode;
                hwBrightnessSceneController.sendDisplayModeToMonitor(displayMode);
            }
        }
    };
    private int mGameDisableAutoBrightnessModeStatus = 0;
    int mGameLevel = 20;
    private long mGameModeEnterTimestamp = 0;
    private long mGameModeQuitTimestamp = 0;
    private int mHdrCurveLevel = -1;
    private final HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private final HwBrightnessSceneControllerHandler mHwBrightnessSceneControllerHandler;
    private final HandlerThread mHwBrightnessSceneControllerThread;
    private HwHumanFactorBrightness mHwHumanFactorBrightness;
    private final HwNormalizedSpline mHwNormalizedSpline;
    boolean mIsAnimationGameChangeEnable = false;
    private boolean mIsBrightnessChangeUpStatus = false;
    private boolean mIsBrightnessModeSetAutoEnable = false;
    boolean mIsDarkenAmbientEnable = false;
    private boolean mIsDcModeBrightnessEnable = false;
    private boolean mIsDcModeObserverInitialize = true;
    private boolean mIsDisplayModeListenerEnabled = false;
    boolean mIsGameDisableAutoBrightnessModeEnable = false;
    boolean mIsGameDisableAutoBrightnessModeKeepOffsetEnable = false;
    private boolean mIsGameIsFrontEnable = false;
    boolean mIsGameModeBrightnessOffsetEnable = false;
    private boolean mIsHdrEnable = false;
    private boolean mIsLandscapeGameModeState = false;
    boolean mIsLandscapeModeState = false;
    boolean mIsLastGameDisableAutoBrightnessModeEnable = false;
    private boolean mIsLightSensorEnabled = false;
    boolean mIsNightUpTimeEnable = false;
    private boolean mIsNightUpTimeFirstCheckEnable = true;
    private boolean mIsPowerOnOffStatus = false;
    boolean mIsReadingModeChangeAnimationEnable = false;
    private boolean mIsReadingModeEnable = false;
    private boolean mIsSettingsObserverInitialize = true;
    private boolean mIsVehicleModeQuitEnable = false;
    boolean mIsVideoModeEnable = false;
    boolean mIsVideoPlay = false;
    private LandscapeStateReceiver mLandscapeStateReceiver = null;
    private int mLastBrightnessModeForGame = 1;
    private long mNightUpModePowerOffTimestamp = 0;
    private long mNightUpModePowerOnOffTimeDelta = 0;
    private long mNightUpModePowerOnTimestamp = 0;
    private long mPowerOffVehicleTimestamp = 0;
    private long mPowerOnVehicleTimestamp = 0;
    private int mReadingMode = 0;
    private ContentObserver mReadingModeObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.display.HwBrightnessSceneController.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            HwBrightnessSceneController hwBrightnessSceneController = HwBrightnessSceneController.this;
            hwBrightnessSceneController.mReadingMode = Settings.System.getIntForUser(hwBrightnessSceneController.mContext.getContentResolver(), HwBrightnessSceneController.KEY_READING_MODE_SWITCH, 0, HwBrightnessSceneController.this.mCurrentUserId);
            HwBrightnessSceneController hwBrightnessSceneController2 = HwBrightnessSceneController.this;
            boolean z = true;
            if (hwBrightnessSceneController2.mReadingMode != 1) {
                z = false;
            }
            hwBrightnessSceneController2.setReadingModeBrightnessLineEnable(z);
        }
    };
    private int mSceneLevel = -1;
    private long mSetCurrentAutoBrightnessTime = DEFAULT_INIT_SYSTEM_TIME;
    private SettingsObserver mSettingsObserver = null;

    public interface Callbacks {
        void setProximitySceneMode(boolean z);

        void updateCurrentAutoBrightness();
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public HwBrightnessSceneController(Callbacks callbacks, Context context, HwAmbientLuxFilterAlgo hwLuxFilterAlgo, HwNormalizedSpline hwSpline, HwHumanFactorBrightness hwHumanFactorBrightness) {
        this.mCallbacks = callbacks;
        this.mContext = context;
        this.mHwAmbientLuxFilterAlgo = hwLuxFilterAlgo;
        this.mHwNormalizedSpline = hwSpline;
        this.mData = HwBrightnessXmlLoader.getData();
        this.mHwHumanFactorBrightness = hwHumanFactorBrightness;
        this.mHwBrightnessSceneControllerThread = new HandlerThread(TAG);
        this.mHwBrightnessSceneControllerThread.start();
        this.mHwBrightnessSceneControllerHandler = new HwBrightnessSceneControllerHandler(this.mHwBrightnessSceneControllerThread.getLooper());
        if (this.mData.gameDisableAutoBrightnessModeEnable) {
            updateGameDisableAutoBrightnessMode();
            this.mIsLastGameDisableAutoBrightnessModeEnable = this.mIsGameDisableAutoBrightnessModeEnable;
            Slog.i(TAG, "Init updateGameDisableAutoBrightnessModeEnable");
        }
        if (this.mData.landscapeBrightnessModeEnable) {
            this.mHwBrightnessSceneControllerHandler.post(new Runnable() {
                /* class com.android.server.display.$$Lambda$HwBrightnessSceneController$8RVI0tkZFVw1Bm8STksSzLMIv20 */

                @Override // java.lang.Runnable
                public final void run() {
                    HwBrightnessSceneController.this.lambda$new$0$HwBrightnessSceneController();
                }
            });
        }
        initReadingMode();
        if (this.mData.luxlinePointsForBrightnessLevelEnable) {
            this.mSettingsObserver = new SettingsObserver(this.mHwBrightnessSceneControllerHandler);
        }
        if (this.mData.dcModeEnable) {
            this.mDcModeObserver = new DcModeObserver(this.mHwBrightnessSceneControllerHandler);
        }
        this.mDisplayEffectMonitor = DisplayEffectMonitor.getInstance(context);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "getDisplayEffectMonitor failed!");
        }
        Slog.i(TAG, "Init HwBrightnessSceneController");
    }

    public /* synthetic */ void lambda$new$0$HwBrightnessSceneController() {
        this.mLandscapeStateReceiver = new LandscapeStateReceiver();
        Slog.i(TAG, "Init registerReceiver LandscapeStateReceiver");
    }

    private void initReadingMode() {
        HwBrightnessSceneControllerHandler hwBrightnessSceneControllerHandler;
        if (this.mData.readingModeEnable && this.mContext != null && (hwBrightnessSceneControllerHandler = this.mHwBrightnessSceneControllerHandler) != null) {
            hwBrightnessSceneControllerHandler.post(new Runnable() {
                /* class com.android.server.display.$$Lambda$HwBrightnessSceneController$ZUqdKobKwTer2lrVZW_xRfGdWYI */

                @Override // java.lang.Runnable
                public final void run() {
                    HwBrightnessSceneController.this.lambda$initReadingMode$1$HwBrightnessSceneController();
                }
            });
        }
    }

    public /* synthetic */ void lambda$initReadingMode$1$HwBrightnessSceneController() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_READING_MODE_SWITCH), true, this.mReadingModeObserver, -1);
        this.mReadingMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_READING_MODE_SWITCH, 0, this.mCurrentUserId);
        if (this.mReadingMode == 1) {
            setReadingModeBrightnessLineEnable(true);
        }
        Slog.i(TAG, "Init readingMode");
    }

    /* access modifiers changed from: package-private */
    public void initBrightnessParaForSpline() {
        if (this.mData.brightnessOffsetLuxModeEnable) {
            initBrightnessOffsetPara();
        }
        if (this.mData.twoPointOffsetModeEnable) {
            initTwoPointBrightnessOffsetPara();
        }
        if (this.mData.videoFullScreenModeEnable) {
            initVideoFullScreenModeBrightnessPara(this.mData.videoFullScreenModeEnable, this.mData.brightnessLineForVideoFullScreenMode);
        }
        if (this.mData.dayModeNewCurveEnable && this.mData.dayModeAlgoEnable) {
            initDayModeBrightnessPara(this.mData.dayModeNewCurveEnable, this.mData.dayModeBrightnessLinePoints);
        }
        if (this.mData.hdrModeEnable) {
            initHdrModeBrightnessPara(this.mData.hdrModeEnable, this.mData.brightnessLineForHdrMode);
        }
        if (this.mData.darkModeEnable && this.mData.isDarkModeCurveEnable) {
            initDarkModeBrightnessPara(this.mData.isDarkModeCurveEnable, this.mData.brightnessLineForDarkMode);
        }
    }

    private void initVideoFullScreenModeBrightnessPara(boolean isVideoFullScreenModeEnable, List<PointF> list) {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline == null) {
            Slog.w(TAG, "initVideoFullScreenModeBrightnessPara fail, mHwNormalizedSpline=null");
        } else {
            hwNormalizedSpline.initVideoFullScreenModeBrightnessPara(isVideoFullScreenModeEnable, list);
        }
    }

    private void initDayModeBrightnessPara(boolean isDayModeNewCurveEnable, List<PointF> dayModeBrightnessLinePoints) {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline == null) {
            Slog.w(TAG, "initDayModeBrightnessPara fail, mHwNormalizedSpline=null");
        } else {
            hwNormalizedSpline.initDayModeBrightnessPara(isDayModeNewCurveEnable, dayModeBrightnessLinePoints);
        }
    }

    private void initHdrModeBrightnessPara(boolean isHdrModeEnable, List<PointF> hdrModeBrightnessLinePoints) {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline == null) {
            Slog.w(TAG, "initHdrModeBrightnessPara fail, mHwNormalizedSpline=null");
        } else {
            hwNormalizedSpline.initHdrModeBrightnessPara(isHdrModeEnable, hdrModeBrightnessLinePoints);
        }
    }

    private void initDarkModeBrightnessPara(boolean isDarkModeCurveEnable, List<PointF> darkModeBrightnessLinePoints) {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline == null) {
            Slog.w(TAG, "initDarkModeBrightnessPara fail, mHwNormalizedSpline=null");
        } else {
            hwNormalizedSpline.initDarkModeBrightnessPara(isDarkModeCurveEnable, darkModeBrightnessLinePoints);
        }
    }

    private void initBrightnessOffsetPara() {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.initBrightenOffsetLux(this.mData.brightnessOffsetLuxModeEnable, this.mData.brightenOffsetLuxTh1, this.mData.brightenOffsetLuxTh2, this.mData.brightenOffsetLuxTh3);
            this.mHwNormalizedSpline.initBrightenOffsetNoValidDarkenLux(this.mData.brightenOffsetEffectMinLuxEnable, this.mData.brightenOffsetNoValidDarkenLuxTh1, this.mData.brightenOffsetNoValidDarkenLuxTh2, this.mData.brightenOffsetNoValidDarkenLuxTh3, this.mData.brightenOffsetNoValidDarkenLuxTh4);
            this.mHwNormalizedSpline.initBrightenOffsetNoValidBrightenLux(this.mData.brightenOffsetNoValidBrightenLuxTh1, this.mData.brightenOffsetNoValidBrightenLuxTh2, this.mData.brightenOffsetNoValidBrightenLuxTh3, this.mData.brightenOffsetNoValidBrightenLuxTh4);
            this.mHwNormalizedSpline.initDarkenOffsetLux(this.mData.darkenOffsetLuxTh1, this.mData.darkenOffsetLuxTh2, this.mData.darkenOffsetLuxTh3);
            this.mHwNormalizedSpline.initDarkenOffsetNoValidBrightenLux(this.mData.darkenOffsetNoValidBrightenLuxTh1, this.mData.darkenOffsetNoValidBrightenLuxTh2, this.mData.darkenOffsetNoValidBrightenLuxTh3, this.mData.darkenOffsetNoValidBrightenLuxTh4);
            this.mHwNormalizedSpline.initBrightnessOffsetTmpValidPara(this.mData.brightnessOffsetTmpValidEnable, this.mData.brightenOffsetNoValidSavedLuxTh1, this.mData.brightenOffsetNoValidSavedLuxTh2);
        }
    }

    private void initTwoPointBrightnessOffsetPara() {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.initTwoPointOffsetPara(this.mData.twoPointOffsetModeEnable, this.mData.twoPointOffsetLuxTh, this.mData.twoPointOffsetAdjionLuxTh, this.mData.twoPointOffsetNoValidLuxTh);
            this.mHwNormalizedSpline.initTwoPointOffsetLowLuxPara(this.mData.lowBrightenOffsetNoValidBrightenLuxTh, this.mData.lowDarkenOffsetNoValidBrightenLuxTh, this.mData.lowBrightenOffsetNoValidDarkenLuxTh, this.mData.lowDarkenOffsetNoValidDarkenLuxTh, this.mData.lowDarkenOffsetDarkenBrightnessRatio);
            this.mHwNormalizedSpline.initTwoPointOffsetHighLuxPara(this.mData.highBrightenOffsetNoValidBrightenLuxTh, this.mData.highDarkenOffsetNoValidBrightenLuxTh, this.mData.highBrightenOffsetNoValidDarkenLuxTh, this.mData.highDarkenOffsetNoValidDarkenLuxTh);
        }
    }

    /* access modifiers changed from: package-private */
    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
        if (this.mHwNormalizedSpline == null) {
            Slog.i(TAG, "setPersonalizedBrightnessCurveLevel failed! curveLevel=" + curveLevel + ",mHwNormalizedSpline=null");
        } else if (curveLevel == 19 || curveLevel == 18 || curveLevel == 17 || curveLevel == 18) {
            updateArState(curveLevel);
        } else if (curveLevel == 23 || curveLevel == 22) {
            updateHomeModeState(curveLevel);
        } else if (curveLevel == 29 || curveLevel == 28) {
            updateGameDisableAutoBrightnessModeState(curveLevel);
        } else if (curveLevel == 33 || curveLevel == 32) {
            updateHdrStatus(curveLevel);
        } else {
            updateBrightnessCurveState(curveLevel);
        }
    }

    private void updateArState(int curveLevel) {
        if (this.mSceneLevel != curveLevel && HWFLOW) {
            Slog.i(TAG, "updateArState,curveLevel=" + curveLevel);
        }
        updateWalkState(curveLevel);
        updateVehicleState(curveLevel);
    }

    private void updateWalkState(int curveLevel) {
        if (this.mData.isWalkModeEnable) {
            if (curveLevel == 17 || curveLevel == 18) {
                HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
                if (hwAmbientLuxFilterAlgo == null) {
                    Slog.w(TAG, "updateWaState fail,mHwAmbientLuxFilterAlgo==null");
                    return;
                }
                hwAmbientLuxFilterAlgo.updateWalkStatus(curveLevel == 17);
                if (this.mSceneLevel != curveLevel) {
                    if (HWFLOW) {
                        Slog.i(TAG, "WaBrightMode set curveLevel=" + curveLevel);
                    }
                    this.mSceneLevel = curveLevel;
                }
            }
        }
    }

    private void updateHdrStatus(int curveLevel) {
        if (this.mData.hdrModeEnable) {
            if (curveLevel != 33 && curveLevel != 32) {
                return;
            }
            if (this.mHwNormalizedSpline == null) {
                Slog.w(TAG, "updateHdrStatus no update,mHwNormalizedSpline==null");
                return;
            }
            this.mIsHdrEnable = curveLevel == 33;
            if (this.mData.hdrModeCurveEnable) {
                this.mHwNormalizedSpline.updateHdrModeCurveStatus(this.mIsHdrEnable);
            }
            if (HWFLOW) {
                Slog.i(TAG, "updateHdrStatus set curveLevel=" + curveLevel + ",mIsHdrEnable=" + this.mIsHdrEnable);
            }
            if (this.mHdrCurveLevel != curveLevel) {
                updateCurrentAutoBrightness();
            }
            this.mHdrCurveLevel = curveLevel;
        }
    }

    private void updateBrightnessCurveState(int curveLevel) {
        if (HWDEBUG) {
            Slog.d(TAG, "updateBrightnessCurveState, curveLevel=" + curveLevel);
        }
        updateGameIsFrontState(curveLevel);
        updateGameModeCurveState(curveLevel);
        updateVideoModeCurveState(curveLevel);
        updatePersonalCurveModeCureState(curveLevel);
    }

    private void updateVehicleState(int curveLevel) {
        if (this.mData.vehicleModeEnable) {
            if (curveLevel == 19 || curveLevel == 18) {
                HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
                if (hwNormalizedSpline == null) {
                    Slog.w(TAG, "VeBrightMode updateVeState fail,HwNormalizedScreenAutoBrightnessSpline==null");
                    return;
                }
                hwNormalizedSpline.setSceneCurveLevel(curveLevel);
                if (curveLevel == 19) {
                    this.mIsVehicleModeQuitEnable = false;
                    long timDelta = SystemClock.elapsedRealtime() - this.mPowerOnVehicleTimestamp;
                    if (timDelta > this.mData.vehicleModeEnterTimeForPowerOn) {
                        updateCurrentAutoBrightness();
                        if (HWFLOW) {
                            Slog.i(TAG, "VeBrightMode updateAutoBrightness curveLevel=" + curveLevel + ",timDelta=" + timDelta);
                        }
                    }
                } else if (this.mIsVehicleModeQuitEnable) {
                    long timDelta2 = SystemClock.elapsedRealtime() - this.mPowerOnVehicleTimestamp;
                    boolean isVehicleModeBrightnessEnable = this.mHwNormalizedSpline.isVehicleModeBrightnessEnable();
                    if (timDelta2 < this.mData.vehicleModeQuitTimeForPowerOn && isVehicleModeBrightnessEnable) {
                        this.mHwNormalizedSpline.updateVehicleModeQuitEnable();
                        Slog.i(TAG, "VeBrightMode mIsVeModeQuitEnable timDelta=" + timDelta2);
                    }
                }
                if (this.mSceneLevel != curveLevel) {
                    if (HWFLOW) {
                        Slog.i(TAG, "VeBrightMode set curveLevel=" + curveLevel);
                    }
                    this.mSceneLevel = curveLevel;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateVehiclePara(boolean isPowerOn) {
        HwNormalizedSpline hwNormalizedSpline;
        if (!(this.mIsPowerOnOffStatus == isPowerOn || (hwNormalizedSpline = this.mHwNormalizedSpline) == null)) {
            if (!isPowerOn) {
                boolean isNewCurveEableTmp = hwNormalizedSpline.isPersonalizedNewCurveEnableTmp();
                this.mHwNormalizedSpline.setNewCurveEnable(isNewCurveEableTmp);
                if (isNewCurveEableTmp) {
                    Slog.i(TAG, "NewCurveMode poweroff updateNewCurve(tem--real),enableTmp=" + isNewCurveEableTmp + ",isPowerOn=" + isPowerOn);
                }
            }
            this.mHwNormalizedSpline.setPowerStatus(isPowerOn);
            updateVehicleQuitPara(isPowerOn);
        }
        this.mIsPowerOnOffStatus = isPowerOn;
    }

    private void updateVehicleQuitPara(boolean isPowerOn) {
        if (this.mData.vehicleModeEnable) {
            if (isPowerOn) {
                updateVehicleQuitEnable();
                return;
            }
            this.mIsVehicleModeQuitEnable = false;
            this.mPowerOffVehicleTimestamp = SystemClock.elapsedRealtime();
        }
    }

    private void updateVehicleQuitEnable() {
        HwNormalizedSpline hwNormalizedSpline;
        this.mPowerOnVehicleTimestamp = SystemClock.elapsedRealtime();
        if (this.mPowerOnVehicleTimestamp - this.mPowerOffVehicleTimestamp > this.mData.vehicleModeDisableTimeMillis && (hwNormalizedSpline = this.mHwNormalizedSpline) != null) {
            boolean isVehicleEnable = hwNormalizedSpline.isVehicleModeBrightnessEnable();
            boolean isVehicleQuitEnable = this.mHwNormalizedSpline.isVehicleModeQuitForPowerOnEnable();
            if (isVehicleEnable && isVehicleQuitEnable) {
                this.mHwNormalizedSpline.updateVehicleModeQuitEnable();
                if (HWFLOW) {
                    Slog.i(TAG, "VeBrightMode quit from lastOnScreen");
                }
            }
            this.mIsVehicleModeQuitEnable = true;
            if (HWFLOW) {
                Slog.i(TAG, "VeBrightMode mIsVeModeQuitEnable OnOfftime=" + (this.mPowerOnVehicleTimestamp - this.mPowerOffVehicleTimestamp));
            }
        }
    }

    private void updateGameModeCurveState(int curveLevel) {
        if (isGameModeCurveLevelValid(curveLevel)) {
            this.mHwNormalizedSpline.setGameCurveLevel(curveLevel);
            if (this.mGameLevel != curveLevel) {
                this.mIsAnimationGameChangeEnable = true;
            }
            if (curveLevel == 21) {
                this.mIsGameModeBrightnessOffsetEnable = true;
                this.mHwAmbientLuxFilterAlgo.setGameModeEnable(true);
                if (this.mIsLightSensorEnabled) {
                    setProximitySceneMode(true);
                }
                this.mGameModeEnterTimestamp = SystemClock.elapsedRealtime();
                long timeDelta = this.mGameModeEnterTimestamp - this.mGameModeQuitTimestamp;
                if (timeDelta > this.mData.gameModeClearOffsetTime) {
                    float ambientLuxOffset = this.mHwNormalizedSpline.getGameModeAmbientLuxForOffset();
                    if (!this.mData.gameModeOffsetValidAmbientLuxEnable || ambientLuxOffset == DEFAUL_OFFSET_LUX) {
                        this.mHwNormalizedSpline.clearGameOffsetDelta();
                    } else {
                        this.mHwNormalizedSpline.resetGameModeOffsetFromHumanFactor(this.mHwHumanFactorBrightness.calculateHumanFactorMinBrightness(ambientLuxOffset, 1), this.mHwHumanFactorBrightness.calculateHumanFactorMaxBrightness(ambientLuxOffset, 1));
                    }
                    this.mHwNormalizedSpline.resetGameBrightnessLimitation();
                    Slog.i(TAG, "GameBrightMode enterGame timeDelta=" + timeDelta);
                }
            } else {
                this.mHwAmbientLuxFilterAlgo.setGameModeEnable(false);
                if (this.mIsLightSensorEnabled) {
                    setProximitySceneMode(false);
                }
                this.mIsGameModeBrightnessOffsetEnable = false;
                this.mGameModeQuitTimestamp = SystemClock.elapsedRealtime();
            }
            Slog.i(TAG, "GameBrightMode updateAutoBrightness,curveLevel=" + curveLevel);
            updateCurrentAutoBrightness();
            this.mGameLevel = curveLevel;
        }
    }

    private boolean isGameModeCurveLevelValid(int curveLevel) {
        if (!this.mData.gameModeEnable) {
            return false;
        }
        if ((curveLevel != 21 && curveLevel != 20) || this.mHwNormalizedSpline == null || this.mGameLevel == curveLevel || this.mHwHumanFactorBrightness == null || this.mHwAmbientLuxFilterAlgo == null) {
            return false;
        }
        return true;
    }

    private void updateGameIsFrontState(int curveLevel) {
        if ((curveLevel == 27 || curveLevel == 26) && this.mData.gameDisableAutoBrightnessModeEnable) {
            this.mIsGameIsFrontEnable = curveLevel == 27;
            if (HWFLOW) {
                Slog.i(TAG, "GameDabMode mIsGameIsFrontEnable=" + this.mIsGameIsFrontEnable + ",mIsGameDisableAutoBrightnessModeEnable=" + this.mIsGameDisableAutoBrightnessModeEnable + ",mLastBrightnessModeForGame=" + this.mLastBrightnessModeForGame + ",curveLevel=" + curveLevel);
            }
            if (this.mData.isLandscapeGameModeEnable) {
                updateLandscapeGameModeStateDelayedMsg(this.mIsGameIsFrontEnable);
            }
            if (this.mIsGameDisableAutoBrightnessModeEnable) {
                int currentBrightnessMode = getCurrentBrightnessMode();
                if (this.mIsGameIsFrontEnable) {
                    backupCurrentBrightnessMode(currentBrightnessMode);
                    if (currentBrightnessMode == 1) {
                        setAutoBrightnessMode(false);
                    }
                } else if (this.mLastBrightnessModeForGame == 1 && currentBrightnessMode == 0) {
                    setAutoBrightnessMode(true);
                } else if (HWFLOW) {
                    Slog.i(TAG, "GameDabMode no need recoveryMode,LastBrightnessModeForGame=" + this.mLastBrightnessModeForGame + ",currentBrightnessMode=" + currentBrightnessMode);
                }
            }
        }
    }

    private void updateAutoBrightnessModeStatus() {
        int currentBrightnessMode = getCurrentBrightnessMode();
        if (this.mIsGameDisableAutoBrightnessModeEnable) {
            backupCurrentBrightnessMode(currentBrightnessMode);
            if (currentBrightnessMode == 1 && this.mIsGameIsFrontEnable) {
                this.mIsBrightnessModeSetAutoEnable = false;
                this.mHwBrightnessSceneControllerHandler.removeMessages(5);
                this.mHwBrightnessSceneControllerHandler.sendEmptyMessage(5);
            } else if (HWFLOW) {
                Slog.i(TAG, "GameDabMode no need process brightnessMode,currentBrightnessMode=" + currentBrightnessMode + ",mIsGameIsFrontEnable=" + this.mIsGameIsFrontEnable);
            }
        } else {
            int i = this.mLastBrightnessModeForGame;
            if (i != currentBrightnessMode && i == 1) {
                this.mIsBrightnessModeSetAutoEnable = true;
                this.mHwBrightnessSceneControllerHandler.removeMessages(5);
                this.mHwBrightnessSceneControllerHandler.sendEmptyMessage(5);
            } else if (HWFLOW) {
                Slog.i(TAG, "GameDabMode no need process,mLastBrightnessModeForGame=" + this.mLastBrightnessModeForGame + ",currentBrightnessMode=" + currentBrightnessMode);
            }
        }
        if (HWFLOW) {
            Slog.i(TAG, "GameDabMode updateAutoBrightnessModeStatus(),mIsGameDisableAutoBrightnessModeEnable=" + this.mIsGameDisableAutoBrightnessModeEnable + ",mLastBrightnessModeForGame=" + this.mLastBrightnessModeForGame + ",currentBrightnessMode =" + currentBrightnessMode);
        }
    }

    private void backupCurrentBrightnessMode(int brightnessMode) {
        if (this.mLastBrightnessModeForGame != brightnessMode) {
            if (HWFLOW) {
                Slog.i(TAG, "GameDabMode backupBrightnessMode,mLastBrightnessModeForGame=" + this.mLastBrightnessModeForGame + "-->brightnessMode=" + brightnessMode);
            }
            this.mLastBrightnessModeForGame = brightnessMode;
        } else if (HWFLOW) {
            Slog.i(TAG, "GameDabMode no need backupBrightnessMode, brightnessMode=" + brightnessMode);
        }
    }

    private int getCurrentBrightnessMode() {
        Context context = this.mContext;
        if (context == null) {
            return 0;
        }
        return Settings.System.getIntForUser(context.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAutoBrightnessMode(boolean isAutoMode) {
        if (this.mContext != null) {
            if (isAutoMode != (getCurrentBrightnessMode() == 1)) {
                if (isAutoMode) {
                    this.mIsGameDisableAutoBrightnessModeKeepOffsetEnable = true;
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 1, this.mCurrentUserId);
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "hw_screen_brightness_mode_value", 1, this.mCurrentUserId);
                } else {
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId);
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "hw_screen_brightness_mode_value", 0, this.mCurrentUserId);
                }
                Slog.i(TAG, "GameDabMode setAutoBrightnessMode isAutoMode=" + isAutoMode);
            }
        }
    }

    private void updateGameDisableAutoBrightnessModeState(int curveLevel) {
        if (curveLevel == 29 || curveLevel == 28) {
            this.mIsGameDisableAutoBrightnessModeEnable = curveLevel == 29;
            updateGameDisableAutoBrightnessModeEnableDb();
            updateAutoBrightnessModeStatus();
        }
    }

    private void updateGameDisableAutoBrightnessModeEnableDb() {
        int i;
        if (this.mContext != null) {
            if (this.mIsGameDisableAutoBrightnessModeEnable) {
                i = 1;
            } else {
                i = 0;
            }
            this.mGameDisableAutoBrightnessModeStatus = i;
            if (this.mData.proximitySceneModeEnable && this.mIsGameModeBrightnessOffsetEnable) {
                if (this.mIsLastGameDisableAutoBrightnessModeEnable && !this.mIsGameDisableAutoBrightnessModeEnable) {
                    Slog.i(TAG, "setProximitySceneMode true");
                    setProximitySceneMode(true);
                }
                if (!this.mIsLastGameDisableAutoBrightnessModeEnable && this.mIsGameDisableAutoBrightnessModeEnable) {
                    Slog.i(TAG, "setProximitySceneMode false");
                    setProximitySceneMode(false);
                }
            }
            this.mIsLastGameDisableAutoBrightnessModeEnable = this.mIsGameDisableAutoBrightnessModeEnable;
            this.mHwBrightnessSceneControllerHandler.removeMessages(4);
            this.mHwBrightnessSceneControllerHandler.sendEmptyMessage(4);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGameDisableAutoBrightnessModeEnableDbMsg() {
        if (HWFLOW) {
            Slog.i(TAG, "GameDabMode updateGameDisableAutoBrightnessModeEnableDbMsg,ModeStatus=" + this.mGameDisableAutoBrightnessModeStatus);
        }
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "game_disable_auto_brightness_mode", this.mGameDisableAutoBrightnessModeStatus, this.mCurrentUserId);
    }

    /* access modifiers changed from: package-private */
    public void updateGameDisableAutoBrightnessModeEnable() {
        updateGameDisableAutoBrightnessMode();
    }

    private void updateGameDisableAutoBrightnessMode() {
        int gameDisableAutoBrightnessMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), "game_disable_auto_brightness_mode", 0, this.mCurrentUserId);
        boolean z = true;
        if (gameDisableAutoBrightnessMode != 1) {
            z = false;
        }
        this.mIsGameDisableAutoBrightnessModeEnable = z;
        if (HWFLOW) {
            Slog.i(TAG, "GameDabMode gameDisableAutoBrightnessMode=" + gameDisableAutoBrightnessMode + ",mIsGameDisableAutoBrightnessModeEnable=" + this.mIsGameDisableAutoBrightnessModeEnable);
        }
    }

    private void updateVideoModeCurveState(int curveLevel) {
        if (this.mData.videoFullScreenModeEnable) {
            if (curveLevel != 25 && curveLevel != 24) {
                return;
            }
            if (this.mHwNormalizedSpline == null) {
                Slog.w(TAG, "VideoBrightMode no update,HwNormalizedScreenAutoBrightnessSpline==null");
                return;
            }
            this.mIsVideoModeEnable = curveLevel == 25;
            if (HWFLOW) {
                Slog.i(TAG, "VideoBrightMode set curveLevel=" + curveLevel + ",mIsVideoModeEnable=" + this.mIsVideoModeEnable);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVideoPlayStatus(boolean isVideoPlay) {
        this.mIsVideoPlay = isVideoPlay;
        Slog.i(TAG, "setVideoPlayStatus, mIsVideoPlay= " + this.mIsVideoPlay);
        if (this.mData.videoFullScreenModeEnable) {
            this.mHwBrightnessSceneControllerHandler.removeMessages(3);
            this.mHwBrightnessSceneControllerHandler.sendEmptyMessageDelayed(3, (long) this.mData.videoFullScreenModeDelayTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFullScreenVideoState() {
        boolean isFullScreenVideoState = false;
        if (this.mIsVideoPlay && this.mIsVideoModeEnable) {
            isFullScreenVideoState = true;
        }
        if (HWFLOW) {
            Slog.i(TAG, "updateFullScreenVideoState=" + isFullScreenVideoState + ",mIsVideoPlay=" + this.mIsVideoPlay + ",mIsVideoModeEnable=" + this.mIsVideoModeEnable);
        }
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.setVideoFullScreenModeBrightnessEnable(isFullScreenVideoState);
        } else {
            Slog.e(TAG, "setVideoFullScreenModeBrightnessEnable fail, mHwNormalizedSpline=null");
        }
    }

    private void quitVehicleMode() {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline == null) {
            Slog.w(TAG, "VeBrightMode quitVeMode fail,HwNormalizedScreenAutoBrightnessSpline==null");
        } else if (this.mHwAmbientLuxFilterAlgo == null) {
            Slog.w(TAG, "VeBrightMode quitVeMode fail,mHwAmbientLuxFilterAlgo==null");
        } else {
            hwNormalizedSpline.updateVehicleModeQuitEnable();
        }
    }

    private void updateHomeModeState(int curveLevel) {
        if (this.mData.homeModeEnable) {
            if (curveLevel != 23 && curveLevel != 22) {
                return;
            }
            if (this.mHwAmbientLuxFilterAlgo == null) {
                Slog.w(TAG, "HoBrightMode updateHoModeState fail,mHwAmbientLuxFilterAlgo==null");
            } else if (this.mHwNormalizedSpline == null) {
                Slog.w(TAG, "HoBrightMode setDayModeEnable fail,mHwNormalizedSpline==null");
            } else {
                boolean isHomeModeEnable = curveLevel == 23;
                if (HWFLOW) {
                    Slog.i(TAG, "HoBrightMode set curveLevel=" + curveLevel + ",isHoModeEnable=" + isHomeModeEnable);
                }
                this.mHwAmbientLuxFilterAlgo.setHomeModeEnable(isHomeModeEnable);
                if (this.mData.dayModeAlgoEnable) {
                    this.mHwNormalizedSpline.setDayModeEnable(this.mHwAmbientLuxFilterAlgo.getDayModeEnable());
                }
                if (isHomeModeEnable) {
                    quitVehicleMode();
                }
                updateCurrentAutoBrightness();
            }
        }
    }

    private void updatePersonalCurveModeCureState(int curveLevel) {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline == null) {
            Slog.w(TAG, "NewCurveMode updateCurveState fail,HwNormalizedScreenAutoBrightnessSpline==null");
            return;
        }
        if (curveLevel != this.mCurveLevel) {
            hwNormalizedSpline.setPersonalizedBrightnessCurveLevel(curveLevel);
            if (HWFLOW) {
                Slog.i(TAG, "NewCurveMode setPersonalizedBrightnessCurveLevel curveLevel=" + curveLevel);
            }
        }
        this.mCurveLevel = curveLevel;
    }

    /* access modifiers changed from: package-private */
    public void updateDarkMode() {
        int currentDarkMode = getCurrentDarkMode();
        if (HWFLOW) {
            Slog.i(TAG, "DarkBrightMode currentDarkMode=" + currentDarkMode);
        }
        if ((currentDarkMode == 32 && this.mCurrentDarkMode != 32) || (this.mCurrentDarkMode == 32 && currentDarkMode != 32)) {
            if (currentDarkMode == 32) {
                updateDarkModeBrightness(true, this.mData.darkModeOffsetMinBrightness);
            } else {
                updateDarkModeBrightness(false, 4);
            }
            Slog.i(TAG, "DarkBrightMode updateDarkMode, currentDarkMode=" + currentDarkMode);
            updateCurrentAutoBrightness();
        }
        if (currentDarkMode == 32) {
            this.mCurrentDarkMode = 32;
        } else {
            this.mCurrentDarkMode = 16;
        }
    }

    private int getCurrentDarkMode() {
        Context context = this.mContext;
        if (context == null) {
            return 16;
        }
        return context.getResources().getConfiguration().uiMode & 48;
    }

    private void updateDarkModeBrightness(boolean isDarkModeEnable, int minOffsetBrightness) {
        if (HWFLOW) {
            Slog.i(TAG, "DarkBrightMode, isDarkModeEnable=" + isDarkModeEnable + ",minOffsetBrightness=" + minOffsetBrightness);
        }
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo != null) {
            hwAmbientLuxFilterAlgo.updateDarkModeEnable(isDarkModeEnable);
        }
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline != null) {
            hwNormalizedSpline.updateDarkModeBrightness(isDarkModeEnable, minOffsetBrightness);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDarkenAmbientEnable(float ambientLux) {
        if (this.mData.nightUpModeEnable) {
            if (ambientLux < this.mData.nightUpModeLuxThreshold) {
                this.mIsDarkenAmbientEnable = true;
                return;
            }
            this.mIsDarkenAmbientEnable = false;
            if (this.mIsNightUpTimeEnable) {
                this.mIsNightUpTimeEnable = false;
                if (HWFLOW) {
                    Slog.i(TAG, "NightUpBrightMode mIsNightUpTimeEnable set false, ambientLux=" + ambientLux);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateNightUpModeTime(boolean isPowerOnEnable) {
        if (this.mData.nightUpModeEnable) {
            if (isPowerOnEnable) {
                this.mNightUpModePowerOnTimestamp = SystemClock.elapsedRealtime();
                this.mNightUpModePowerOnOffTimeDelta = this.mNightUpModePowerOnTimestamp - this.mNightUpModePowerOffTimestamp;
                if (HWFLOW) {
                    Slog.i(TAG, "NightUpBrightMode mPowerOnOffTimeDelta=" + this.mNightUpModePowerOnOffTimeDelta + ",nightUpModeSwitchTimeMillis=" + this.mData.nightUpModeSwitchTimeMillis);
                }
                if (this.mNightUpModePowerOnOffTimeDelta >= ((long) this.mData.nightUpModeSwitchTimeMillis) || this.mIsNightUpTimeFirstCheckEnable) {
                    this.mIsNightUpTimeFirstCheckEnable = false;
                    updateCurrentTimeForNightUpMode();
                    return;
                }
                return;
            }
            this.mNightUpModePowerOffTimestamp = SystemClock.elapsedRealtime();
        }
    }

    private void updateCurrentTimeForNightUpMode() {
        int currentHour = Calendar.getInstance().get(11);
        this.mIsNightUpTimeEnable = false;
        if (this.mData.nightUpModeBeginHourTime < this.mData.nightUpModeEndHourTime) {
            if (currentHour >= this.mData.nightUpModeBeginHourTime && currentHour < this.mData.nightUpModeEndHourTime) {
                this.mIsNightUpTimeEnable = true;
            }
        } else if (currentHour >= this.mData.nightUpModeBeginHourTime || currentHour < this.mData.nightUpModeEndHourTime) {
            this.mIsNightUpTimeEnable = true;
        }
        if (HWFLOW) {
            Slog.i(TAG, "NightUpBrightMode updateCurrentTimeForNightUpMode, mIsNightUpTimeEnable=" + this.mIsNightUpTimeEnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateOffsetPara() {
        if (this.mHwNormalizedSpline != null && this.mHwHumanFactorBrightness != null && this.mHwAmbientLuxFilterAlgo != null && this.mData.offsetResetEnable) {
            if (this.mData.twoPointOffsetModeEnable) {
                resetTwoPointOffsetFromHumanFactor(this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable());
            }
            float ambientLuxOffset = this.mHwNormalizedSpline.getCurrentAmbientLuxForOffset();
            if (ambientLuxOffset != DEFAUL_OFFSET_LUX) {
                int offsetScreenBrightnessMinByAmbientLux = this.mHwHumanFactorBrightness.calculateHumanFactorMinBrightness(ambientLuxOffset, 0);
                int offsetScreenBrightnessMaxByAmbientLux = this.mHwHumanFactorBrightness.calculateHumanFactorMaxBrightness(ambientLuxOffset, 0);
                if (this.mHwNormalizedSpline.getPersonalizedBrightnessCurveEnable()) {
                    float defaultBrightness = this.mHwNormalizedSpline.getDefaultBrightness(ambientLuxOffset);
                    float currentBrightness = this.mHwNormalizedSpline.getNewCurrentBrightness(ambientLuxOffset);
                    offsetScreenBrightnessMinByAmbientLux += ((int) currentBrightness) - ((int) defaultBrightness);
                    offsetScreenBrightnessMaxByAmbientLux += ((int) currentBrightness) - ((int) defaultBrightness);
                    Slog.i(TAG, "NewCurveMode new offset MinByAmbientLux=" + offsetScreenBrightnessMinByAmbientLux + ",maxByAmbientLux=" + offsetScreenBrightnessMaxByAmbientLux);
                }
                this.mHwNormalizedSpline.reSetOffsetFromHumanFactor(this.mHwAmbientLuxFilterAlgo.getOffsetResetEnable(), offsetScreenBrightnessMinByAmbientLux, offsetScreenBrightnessMaxByAmbientLux);
            }
        }
    }

    private void resetTwoPointOffsetFromHumanFactor(boolean isResetTwoPointOffset) {
        HwNormalizedSpline hwNormalizedSpline = this.mHwNormalizedSpline;
        if (hwNormalizedSpline != null && this.mHwHumanFactorBrightness != null) {
            float ambientLuxOffsetLow = hwNormalizedSpline.getCurrentLowAmbientLuxForTwoPointOffset();
            float ambientLuxOffsetHigh = this.mHwNormalizedSpline.getCurrentHighAmbientLuxForTwoPointOffset();
            float ambientLuxOffsetTmp = this.mHwNormalizedSpline.getCurrentTmpAmbientLuxForTwoPointOffset();
            if (ambientLuxOffsetLow != DEFAUL_OFFSET_LUX) {
                this.mHwNormalizedSpline.resetTwoPointOffsetLowFromHumanFactor(isResetTwoPointOffset, this.mHwHumanFactorBrightness.calculateHumanFactorMinBrightness(ambientLuxOffsetLow, 0), this.mHwHumanFactorBrightness.calculateHumanFactorMaxBrightness(ambientLuxOffsetLow, 0));
            }
            if (ambientLuxOffsetHigh != DEFAUL_OFFSET_LUX) {
                this.mHwNormalizedSpline.resetTwoPointOffsetHighFromHumanFactor(isResetTwoPointOffset, this.mHwHumanFactorBrightness.calculateHumanFactorMinBrightness(ambientLuxOffsetHigh, 0), this.mHwHumanFactorBrightness.calculateHumanFactorMaxBrightness(ambientLuxOffsetHigh, 0));
            }
            if (ambientLuxOffsetTmp != DEFAUL_OFFSET_LUX) {
                this.mHwNormalizedSpline.resetTwoPointOffsetTmpFromHumanFactor(isResetTwoPointOffset, this.mHwHumanFactorBrightness.calculateHumanFactorMinBrightness(ambientLuxOffsetHigh, 0), this.mHwHumanFactorBrightness.calculateHumanFactorMaxBrightness(ambientLuxOffsetHigh, 0));
            }
        }
    }

    private void updateCurrentAutoBrightness() {
        Callbacks callbacks = this.mCallbacks;
        if (callbacks == null) {
            Slog.w(TAG, "mCallbacks==null,no updateBrightness");
        } else {
            callbacks.updateCurrentAutoBrightness();
        }
    }

    private void setProximitySceneMode(boolean isProximitySceneMode) {
        Callbacks callbacks = this.mCallbacks;
        if (callbacks == null) {
            Slog.w(TAG, "mCallbacks==null,no updateBrightness");
        } else {
            callbacks.setProximitySceneMode(isProximitySceneMode);
        }
    }

    /* access modifiers changed from: private */
    public final class HwBrightnessSceneControllerHandler extends Handler {
        HwBrightnessSceneControllerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.w(HwBrightnessSceneController.TAG, "HwBrightnessSceneControllerHandler msg==null");
                return;
            }
            switch (msg.what) {
                case 1:
                    HwBrightnessSceneController hwBrightnessSceneController = HwBrightnessSceneController.this;
                    hwBrightnessSceneController.updateLandscapeMode(hwBrightnessSceneController.mIsLandscapeModeState);
                    return;
                case 2:
                    HwBrightnessSceneController hwBrightnessSceneController2 = HwBrightnessSceneController.this;
                    hwBrightnessSceneController2.updateCurrentAutoBrightness(hwBrightnessSceneController2.mCurrentAutoBrightness);
                    return;
                case 3:
                    HwBrightnessSceneController.this.updateFullScreenVideoState();
                    return;
                case 4:
                    HwBrightnessSceneController.this.updateGameDisableAutoBrightnessModeEnableDbMsg();
                    return;
                case 5:
                    HwBrightnessSceneController hwBrightnessSceneController3 = HwBrightnessSceneController.this;
                    hwBrightnessSceneController3.setAutoBrightnessMode(hwBrightnessSceneController3.mIsBrightnessModeSetAutoEnable);
                    return;
                case 6:
                    HwBrightnessSceneController.this.updateLandscapeGameModeState();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateCurrentUserId(int userId) {
        if (userId != this.mCurrentUserId) {
            if (HWFLOW) {
                Slog.i(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
            }
            this.mCurrentUserId = userId;
            updateDcMode();
        }
    }

    /* access modifiers changed from: private */
    public class LandscapeStateReceiver extends BroadcastReceiver {
        LandscapeStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            filter.setPriority(1000);
            HwBrightnessSceneController.this.mContext.registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.e(HwBrightnessSceneController.TAG, "LandscapeStateReceiver Invalid input parameter!");
            } else if (HwBrightnessSceneController.this.mContext == null) {
                Slog.e(HwBrightnessSceneController.TAG, "LandscapeStateReceiver onReceive failed, mContext==null!");
            } else if ("android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction())) {
                Configuration configuration = HwBrightnessSceneController.this.mContext.getResources().getConfiguration();
                if (configuration == null) {
                    Slog.e(HwBrightnessSceneController.TAG, "LandscapeStateReceiver onReceive failed, configuration==null!");
                    return;
                }
                int ori = configuration.orientation;
                if (ori == 2) {
                    HwBrightnessSceneController.this.mIsLandscapeModeState = true;
                } else if (ori == 1) {
                    HwBrightnessSceneController.this.mIsLandscapeModeState = false;
                } else if (HwBrightnessSceneController.HWDEBUG) {
                    Slog.d(HwBrightnessSceneController.TAG, "LandscapeBrightMode ORIENTATION no change");
                }
                if (HwBrightnessSceneController.HWFLOW) {
                    Slog.i(HwBrightnessSceneController.TAG, "LandscapeBrightMode MSG_UPDATE_LANDSCAPE mIsLandscapeModeState=" + HwBrightnessSceneController.this.mIsLandscapeModeState);
                }
                HwBrightnessSceneController hwBrightnessSceneController = HwBrightnessSceneController.this;
                hwBrightnessSceneController.sendLandscapeStateUpdate(hwBrightnessSceneController.mIsLandscapeModeState);
                if (HwBrightnessSceneController.this.mData.darkModeEnable) {
                    HwBrightnessSceneController.this.updateDarkMode();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendLandscapeStateUpdate(boolean isLandscapeEnable) {
        this.mHwBrightnessSceneControllerHandler.removeMessages(1);
        if (isLandscapeEnable) {
            this.mHwBrightnessSceneControllerHandler.sendEmptyMessageDelayed(1, (long) this.mData.landscapeModeEnterDelayTime);
        } else {
            this.mHwBrightnessSceneControllerHandler.sendEmptyMessageDelayed(1, (long) this.mData.landscapeModeQuitDelayTime);
        }
        if (HWDEBUG) {
            Slog.i(TAG, "LandscapeBrightMode MSG_UPDATE_LANDSCAPE mIsLandscapeModeState=" + this.mIsLandscapeModeState + ",bTime=" + this.mData.landscapeModeEnterDelayTime + ",dTime=" + this.mData.landscapeModeQuitDelayTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLandscapeMode(boolean isLandscapeMode) {
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo != null) {
            hwAmbientLuxFilterAlgo.updateLandscapeMode(isLandscapeMode);
            if (HWFLOW) {
                Slog.i(TAG, "LandscapeBrightMode real LandscapeState,ModeState=" + isLandscapeMode);
            }
        }
        if (this.mData.isLandscapeGameModeEnable && !isLandscapeMode && this.mIsLandscapeGameModeState) {
            if (HWFLOW) {
                Slog.i(TAG, "LandscapeBrightMode real LandscapeState quit, updateLandscapeGameModeState");
            }
            updateLandscapeGameModeState();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateContentObserver() {
        if (this.mData.luxlinePointsForBrightnessLevelEnable && this.mIsSettingsObserverInitialize) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_auto_brightness"), false, this.mSettingsObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_brightness"), false, this.mSettingsObserver, -1);
            this.mIsSettingsObserverInitialize = false;
            Slog.i(TAG, "SettingsObserver Initialize");
        }
        if (this.mData.dcModeEnable && this.mIsDcModeObserverInitialize) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(KEY_DC_BRIGHTNESS_DIMMING_SWITCH), true, this.mDcModeObserver, -1);
            this.mIsDcModeObserverInitialize = false;
            Slog.i(TAG, "DcModeObserver Initialize");
            updateDcMode();
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            HwBrightnessSceneController.this.handleBrightnessSettingsChange();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBrightnessSettingsChange() {
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, this.mCurrentUserId) == 1) {
            handleAutoBrightnessSettingsChange();
        } else {
            handleManualBrightnessSettingsChange();
        }
    }

    private void handleManualBrightnessSettingsChange() {
        int manualBrightnessDb = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", 0, this.mCurrentUserId);
        if (this.mCurrentAutoBrightness != manualBrightnessDb) {
            int autoBrightnessDb = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
            if (autoBrightnessDb != manualBrightnessDb && autoBrightnessDb > 0) {
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", manualBrightnessDb, this.mCurrentUserId);
            }
            updateCurrentAutoBrightness(manualBrightnessDb);
            if (HWDEBUG) {
                Slog.i(TAG, "updateCurrentAutoBrightness from manualBrightnessDb=" + manualBrightnessDb + ",autoBrightnessDb=" + autoBrightnessDb);
            }
            this.mCurrentAutoBrightness = manualBrightnessDb;
        }
    }

    private void handleAutoBrightnessSettingsChange() {
        int i;
        boolean isBrightnessChangeUpStatus = false;
        int autoBrightnessDb = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 0, this.mCurrentUserId);
        long time = SystemClock.uptimeMillis();
        if (this.mCurrentAutoBrightness != autoBrightnessDb) {
            this.mHwBrightnessSceneControllerHandler.removeMessages(2);
            if (autoBrightnessDb > this.mCurrentAutoBrightness) {
                isBrightnessChangeUpStatus = true;
            }
            if (autoBrightnessDb == 0 || (i = this.mCurrentAutoBrightness) == 0 || isBrightnessChangeUpStatus != this.mIsBrightnessChangeUpStatus || ((autoBrightnessDb > i && time - this.mSetCurrentAutoBrightnessTime > ((long) this.mData.brightnessChageUpDelayTime)) || (autoBrightnessDb < this.mCurrentAutoBrightness && time - this.mSetCurrentAutoBrightnessTime > ((long) this.mData.brightnessChageDownDelayTime)))) {
                this.mSetCurrentAutoBrightnessTime = time;
                this.mHwBrightnessSceneControllerHandler.sendEmptyMessage(2);
                if (HWFLOW && (autoBrightnessDb == 0 || this.mCurrentAutoBrightness == 0)) {
                    Slog.i(TAG, "updateCurrentAutoBrightness now,brightness=" + autoBrightnessDb);
                }
            } else {
                this.mHwBrightnessSceneControllerHandler.sendEmptyMessageDelayed(2, (long) this.mData.brightnessChageDefaultDelayTime);
            }
            this.mCurrentAutoBrightness = autoBrightnessDb;
            this.mIsBrightnessChangeUpStatus = isBrightnessChangeUpStatus;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurrentAutoBrightness(int brightness) {
        if (this.mHwAmbientLuxFilterAlgo != null) {
            if (HWDEBUG) {
                Slog.i(TAG, "updateCurrentAutoBrightness realBrightness=" + brightness);
            }
            this.mHwAmbientLuxFilterAlgo.setCurrentAutoBrightness(brightness);
        }
    }

    /* access modifiers changed from: private */
    public final class DcModeObserver extends ContentObserver {
        DcModeObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange, Uri uri) {
            HwBrightnessSceneController.this.handleDcModeSettingsChange();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDcModeSettingsChange() {
        updateDcMode();
    }

    private void updateDcMode() {
        int dcMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_DC_BRIGHTNESS_DIMMING_SWITCH, 0, this.mCurrentUserId);
        boolean z = true;
        if (dcMode != 1) {
            z = false;
        }
        this.mIsDcModeBrightnessEnable = z;
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo != null) {
            hwAmbientLuxFilterAlgo.setDcModeBrightnessEnable(this.mIsDcModeBrightnessEnable);
        }
        if (HWFLOW) {
            Slog.i(TAG, "DcModeBrightnessEnable=" + this.mIsDcModeBrightnessEnable + ",dcMode=" + dcMode);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setReadingModeBrightnessLineEnable(boolean isReadingMode) {
        if (this.mHwNormalizedSpline != null && this.mData.readingModeEnable) {
            this.mHwNormalizedSpline.setReadingModeEnable(isReadingMode);
            if ((isReadingMode || !this.mIsReadingModeEnable) && (!isReadingMode || this.mIsReadingModeEnable)) {
                this.mIsReadingModeChangeAnimationEnable = false;
            } else {
                this.mIsReadingModeChangeAnimationEnable = true;
                updateCurrentAutoBrightness();
            }
            if (HWFLOW) {
                Slog.i(TAG, "setReadingModeControlFlag, isReadingMode=" + isReadingMode + ", mIsReadingModeChangeAnimationEnable=" + this.mIsReadingModeChangeAnimationEnable);
            }
            this.mIsReadingModeEnable = isReadingMode;
        }
    }

    /* access modifiers changed from: package-private */
    public void setFoldDisplayModeEnable(boolean isDisplayModeListenerEnabled) {
        if (isDisplayModeListenerEnabled) {
            if (!this.mIsDisplayModeListenerEnabled) {
                if (HWFLOW) {
                    Slog.i(TAG, "open FoldDisplayModeListener start ...");
                }
                this.mIsDisplayModeListenerEnabled = true;
                HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayModeListener);
                this.mCurrentDisplayMode = HwFoldScreenManagerEx.getDisplayMode();
                if (HWFLOW) {
                    Slog.i(TAG, "open FoldDisplayModeListener,mCurrentDisplayMode=" + this.mCurrentDisplayMode);
                }
            }
        } else if (this.mIsDisplayModeListenerEnabled) {
            this.mIsDisplayModeListenerEnabled = false;
            HwFoldScreenManagerEx.unregisterFoldDisplayMode(this.mFoldDisplayModeListener);
            if (HWFLOW) {
                Slog.i(TAG, "close FoldDisplayModeListener");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDisplayModeToMonitor(int displayMode) {
        if (this.mDisplayEffectMonitor != null) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put(DisplayEffectMonitor.MonitorModule.PARAM_TYPE, "displayMode");
            params.put("displayMode", Integer.valueOf(displayMode));
            this.mDisplayEffectMonitor.sendMonitorParam(params);
            if (HWDEBUG) {
                Slog.i(TAG, "sendDisplayModeToMonitor, displayMode=" + displayMode);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getHdrModeEnable() {
        return this.mIsHdrEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateLightSensorEnabled(boolean isLightSensorEnabled) {
        this.mIsLightSensorEnabled = isLightSensorEnabled;
        Slog.i(TAG, "updateLightSensorEnabled LightSensorEnabled=" + isLightSensorEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getGameDisableAutoBrightnessModeEnable() {
        return this.mIsGameDisableAutoBrightnessModeEnable;
    }

    private void updateLandscapeGameModeStateDelayedMsg(boolean isGameModeEnable) {
        int landscapeGameModeStateDelayedTime;
        if (isGameModeEnable) {
            landscapeGameModeStateDelayedTime = this.mData.landscapeGameModeEnterDelayTime;
        } else {
            landscapeGameModeStateDelayedTime = this.mData.landscapeGameModeQuitDelayTime;
        }
        if (HWFLOW) {
            Slog.i(TAG, "updateLandscapeGameModeStateDelayedMsg, isGameModeEnable=" + isGameModeEnable + ",landscapeGameModeStateDelayedTime=" + landscapeGameModeStateDelayedTime);
        }
        this.mHwBrightnessSceneControllerHandler.removeMessages(6);
        this.mHwBrightnessSceneControllerHandler.sendEmptyMessageDelayed(6, (long) landscapeGameModeStateDelayedTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLandscapeGameModeState() {
        boolean isLandscapeGameModeState = false;
        if ((this.mIsGameIsFrontEnable && this.mIsLandscapeModeState) || (!this.mIsGameIsFrontEnable && this.mIsLandscapeModeState && this.mIsLandscapeGameModeState)) {
            isLandscapeGameModeState = true;
        }
        this.mIsLandscapeGameModeState = isLandscapeGameModeState;
        if (HWFLOW) {
            Slog.i(TAG, "updateLandscapeGameModeState,isLandscapeGameModeState=" + isLandscapeGameModeState + ",mIsLandscapeModeState=" + this.mIsLandscapeModeState + ",mIsGameIsFrontEnable=" + this.mIsGameIsFrontEnable);
        }
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo != null) {
            hwAmbientLuxFilterAlgo.setLandscapeGameModeState(isLandscapeGameModeState);
        } else {
            Slog.e(TAG, "updateLandscapeGameModeState fail, mHwAmbientLuxFilterAlgo=null");
        }
    }
}
