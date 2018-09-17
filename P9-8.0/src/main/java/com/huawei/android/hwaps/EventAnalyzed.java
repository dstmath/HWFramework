package com.huawei.android.hwaps;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.hwaps.FpsRequest.SceneTypeE;
import java.util.ArrayList;
import java.util.HashMap;

public class EventAnalyzed implements IEventAnalyzed {
    public static final int ACTION_CANCEL = 3;
    public static final int ACTION_DEFAULT = -1;
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MASK = 255;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_OUTSIDE = 4;
    public static final int ACTION_POINTER_DOWN = 5;
    public static final int ACTION_POINTER_UP = 6;
    public static final int ACTION_SETFPS_NONPLAY = -2;
    public static final int ACTION_UP = 1;
    private static final int ACTION_UP_TO_DEFAULT_TIMR = 2000;
    public static final long ANALYZE_TIME = 60000;
    private static final int APS_ANALYZE_ENABLE = 2;
    private static final int APS_CONTROLL_ENABLE = 1;
    private static final int APS_DO_NOTHING = 0;
    private static final String APS_LOW_FPS_GAME = "huawei.intent.action.APS_LOW_FPS_GAME";
    private static final String APS_NOT_LOW_FPS_GAME = "huawei.intent.action.APS_NOT_LOW_FPS_GAME";
    private static final String APS_TOUCH_ACTION_DOWN = "huawei.intent.action.APS_TOUCH_ACTION_DOWN";
    private static final String APS_TOUCH_ACTION_UP = "huawei.intent.action.APS_TOUCH_ACTION_UP";
    private static final String APS_TOUCH_KEYCODE_VOLUME = "huawei.intent.action.APS_TOUCH_KEYCODE_VOLUME";
    private static final int CUST_APP_TYPE = 9998;
    private static final int CUST_GAME_TYPE = 9999;
    private static final int DEFAULT_BASE_Y_DPI = 244;
    public static final int DEFAULT_BATTERY = 20;
    public static final int DEFAULT_GAME = 0;
    public static final int DEFAULT_MAX_DALTA = 200;
    public static final int DEFAULT_MAX_FPS = 55;
    public static final int DEFAULT_MIDDLE_DALTA = 150;
    public static final int DEFAULT_MIN_FPS = 30;
    public static final int DEFAULT_SHORT_DALTA = 15;
    public static final int DEFAULT_STUDY_BATTERY = 60;
    private static final int DEFAULT_TARGETFPS = -1;
    private static final int FULL_SCREEN_REGION_COUNT = 4;
    public static final int HIGH_POWER_MODE = 0;
    public static final int KEYCODE_VOLUME_DOWN = 25;
    public static final int KEYCODE_VOLUME_UP = 24;
    public static final int LANDSCAPE_SCROLL_GAME = 6;
    public static final long LONG_PRESS_TIME = 1000;
    public static final int LOW_POWER_MODE = 1;
    public static final int LOW_POWER_MODE_30HZ = 1;
    public static final float MAX_DALTA_RATE = 0.2f;
    private static final int MAX_FLIP_TIME = 500;
    public static final int MAX_POWERKIT_FPS = 60;
    private static final int MAX_SCREEN_REGION_INDEX = 2;
    private static final int MAX_SCREEN_REGION_ROW = 3;
    private static final int MAX_SCREEN_REGION_SIZE = 9;
    public static final float MIDDLE_DALTA_RATE = 0.15f;
    public static final int MIN_POWERKIT_FPS = 15;
    private static String NODE_PATH = "/sys/class/graphics/fb0/lcd_fps_scence";
    public static final int NOT_DRAW_WITHOUT_DRAG = 10;
    public static final int NOT_LOW_POWER_MODE_30HZ = 0;
    private static final int ONE = 1;
    private static int ON_PAUSE = 2999;
    public static final int OTHERS_DEFAULT_FPS = 45;
    public static final int OTHER_FULL_SCREEN_GAME = 5;
    public static final int OTHER_GAME = 3;
    public static final int PLR_LDF_GAME = 8;
    public static final int PLR_LTR_GAME = 9;
    public static final int REALTIME_DEFAULT_FPS = 55;
    public static final int REALTIME_GAME = 0;
    public static final int RUN_GAME = 11;
    private static final int SCREEN_REGION_RATE = 3;
    public static final int SCREEN_WIDTH = 1080;
    public static final int SCROLL_DEFAULT_FPS = 55;
    public static final int SCROLL_GAME = 2;
    public static final int SCROLL_STATIC_GAME = 7;
    public static final float SHORT_DALTA_RATE = 0.02f;
    public static final int SINGLE_REGION_GAME = 4;
    private static final int STOP_EMERGENCY_FPS_TIME = 30000;
    private static final int STOP_TOUCH_MAX_FPS_TIME = 3000;
    public static final int STRATEGY_GAME = 1;
    public static final long STRATEGY_TIME = 3000;
    private static final String TAG = "Hwaps";
    public static final int THREE_DIMS_GAME = 4;
    public static final int TWO_DIMS_GAME = 1;
    public static final int TWO_P_FIVE_DIMS_HIGH_GAME = 3;
    public static final int TWO_P_FIVE_DIMS_LOW_GAME = 2;
    private static final int UNINITED_DEFAULT_VALUE = -1;
    private static final String VERSION = "6.0.0.24-1";
    private static final int ZERO = 0;
    private static final String[] mIdentifyGameType = new String[]{"REALTIME_GAME", "STRATEGY_GAME", "SCROLL_GAME", "OTHER_GAME", "SINGLE_REGION_GAME", "OTHER_FULL_SCREEN_GAME", "LANDSCAPE_SCROLL_GAME", "SCROLL_STATIC_GAME", "PLR_LDF_GAME", "PLR_LTR_GAME", "NOT_DRAW_WITHOUT_DRAG", "RUN_GAME"};
    private static boolean mStopSetFps = false;
    private boolean isXScroll = false;
    private boolean isYScroll = false;
    private int mAction;
    private ActivityManager mActivityManager;
    private AdCheck mAdCheck = null;
    private ApsLdfGameIdentify mApsLdfGameIdentify = null;
    private ApsLtrGameIdentify mApsLtrGameIdentify = null;
    private IApsManager mApsManager = null;
    private ApsTest mApsTest = null;
    private ApsThermal mApsThermal = null;
    private ApsUserFeedback mApsUserFeedback = null;
    private long mAvageDownUpLongTime = 0;
    private int mAverageFps = 0;
    private int mBatteryLimitStudyValue = -1;
    private int mBatteryLimitValue = -1;
    private long mBeginAnalyzeTime;
    private boolean mCanUseDebug = false;
    private int mClickCount = 0;
    private int mConsecutiveShortDownUpCount = 0;
    private Context mContext;
    private boolean mControlByAppSelf = false;
    private long mCurrentDownTime;
    private int mDesignatedFps = 60;
    private float mDesignatedSdrRatio = 1.0f;
    private long mDownMoveInterval;
    private long mDownUpInterval;
    private long mDownUpLongSum = 0;
    private long mDownUpShortSum = 0;
    private FpsRequest mEmergencyFpsRequest;
    private FpsRequest mExactlyFpsRequest;
    private boolean mFirstTimeResumeFps = true;
    private FpsRequest mFpsRequest;
    private GameState mGameState;
    private int mGameType = 0;
    private Handler mHandler = null;
    private boolean mHasIdentifyProcess = false;
    private boolean mHasIncreaseHighFPS = false;
    private boolean mHasIncreaseLowFPS = false;
    private boolean mHasOnPaused = false;
    private boolean mHasReleaseAPS = false;
    private boolean mHasSetFPS = false;
    private boolean mHasSetPid = false;
    private HashMap<String, Integer> mHashCtrlBattery = new HashMap();
    private HashMap<String, Integer> mHashFixdFps = new HashMap();
    private HashMap<String, Float> mHashFixdRatio = new HashMap();
    private HashMap<String, Boolean> mHashSceneFixed = new HashMap();
    private HashMap<String, ArrayList<Integer>> mHashSceneFps = new HashMap();
    private HashMap<String, ArrayList<Float>> mHashSceneRatio = new HashMap();
    private HwapsFactoryImpl mHwapsFactoryImpl = null;
    private Intent mIntent = null;
    private boolean mIsAllScroll = false;
    private boolean mIsAnalyzedEnable = false;
    private boolean mIsControllAllFpsEnable = false;
    private boolean mIsEnterEmergencyFps = false;
    private boolean mIsFirstStartAnalyze = true;
    private boolean mIsFullScreenTouchGame = false;
    private boolean mIsGameInfoExist = false;
    private boolean mIsGameProcess = false;
    private boolean mIsLongMove;
    private boolean mIsLowFps = false;
    private boolean mIsLowFpsGame = false;
    private boolean mIsSceneFixed = false;
    private boolean mIsScreenLandScape = false;
    private boolean mIsSingleRegionAvailalbeGame = false;
    private int mLastDownX;
    private int mLastDownY;
    private long mLastEventTime = 0;
    private int mLastGameType = -2;
    private long mLastUpTime = -1;
    private int mLevel = 0;
    private int mLongDownUpCount = 0;
    private int mLongPressCount = 0;
    private int mMaxDelta = 0;
    private int mMaxFps = 55;
    private int mMiddleDelta = 0;
    private int mMinFps = 30;
    private int mMinNonplayFps;
    private long mMoveTime;
    private int mMultiTouchCount = 0;
    private int mMyPid = -1;
    private OperateExperienceLib mOperateExLib;
    private boolean mPerformanceDebug = false;
    private String mPkgName;
    private FpsRequest mPowerKitFpsRequest = null;
    private ResumeFpsByTouch mResumeFpsByTouch = null;
    private ResumePowerKit mResumePowerKit = null;
    private int[] mSceenRegion = new int[9];
    private int mScreenHeightRegion = 0;
    private int mScreenOrientation = -2;
    private int mScreenWidth = 0;
    private int mScreenWidthRegion = 0;
    private SdrController mSdrController = null;
    private float mSdrRatio = 1.0f;
    private SendBroadcast mSendBroadcast = null;
    private setTouchStateForNonplay mSetTouchStateForNonplay = null;
    private int mShortDelta = 0;
    private int mShortDownUpCount = 0;
    private int mSlowScrollCount = 0;
    private StartSdr mStartSdr = null;
    private StartSdrForTest mStartSdrForTest = null;
    private StatisFps mStatisFps = null;
    private int mStatisFpsCount = 0;
    private StopEmergency mStopEmergency = null;
    private StopSdrForTest mStopSdrForTest = null;
    private int mStopTouchMaxFpsTime = 0;
    private FpsRequest mTouchFpsRequest;
    private int mTouchMaxFps = -1;
    private boolean mTouchResumeEnable = false;
    private long mUpTime = -1;
    private int mXScrollCount = 0;
    private int mYScrollCount = 0;
    private float mYdpi = 0.0f;
    private boolean mhasFirstSetFPS = false;

    private class QueryThread extends Thread {
        /* synthetic */ QueryThread(EventAnalyzed this$0, QueryThread -this1) {
            this();
        }

        private QueryThread() {
        }

        public void run() {
            ApsCommon.logD(EventAnalyzed.TAG, "QueryThread run!!!!");
            EventAnalyzed.this.query();
            EventAnalyzed.this.setFirstEnterFPS(EventAnalyzed.this.mGameType);
            EventAnalyzed.this.setAdaptSdr(EventAnalyzed.this.mGameType, -1);
        }
    }

    private class ResumeFpsByTouch implements Runnable {
        /* synthetic */ ResumeFpsByTouch(EventAnalyzed this$0, ResumeFpsByTouch -this1) {
            this();
        }

        private ResumeFpsByTouch() {
        }

        public void run() {
            if (EventAnalyzed.this.mResumeFpsByTouch != null) {
                EventAnalyzed.this.mTouchFpsRequest.stop();
                EventAnalyzed.this.mTouchResumeEnable = false;
                ApsCommon.logD(EventAnalyzed.TAG, "stop ResumeFpsByTouch");
            }
        }
    }

    private class ResumePowerKit implements Runnable {
        /* synthetic */ ResumePowerKit(EventAnalyzed this$0, ResumePowerKit -this1) {
            this();
        }

        private ResumePowerKit() {
        }

        public void run() {
            if (EventAnalyzed.this.mResumePowerKit != null) {
                EventAnalyzed.this.setPowerkitFrame(60);
                EventAnalyzed.this.mControlByAppSelf = false;
                ApsCommon.logD(EventAnalyzed.TAG, "stop Powerkit frame");
            }
        }
    }

    private class SendBroadcast implements Runnable {
        Intent mSendBroadcastIntent;

        /* synthetic */ SendBroadcast(EventAnalyzed this$0, SendBroadcast -this1) {
            this();
        }

        private SendBroadcast() {
            this.mSendBroadcastIntent = null;
        }

        public void setActionIntent(Intent intent) {
            this.mSendBroadcastIntent = intent;
        }

        private Intent getActionIntent() {
            return this.mSendBroadcastIntent;
        }

        public void run() {
            EventAnalyzed.this.mContext.sendBroadcast(getActionIntent());
        }
    }

    private class StartSdr implements Runnable {
        /* synthetic */ StartSdr(EventAnalyzed this$0, StartSdr -this1) {
            this();
        }

        private StartSdr() {
        }

        public void run() {
            if (EventAnalyzed.this.mSdrController != null && EventAnalyzed.this.mSdrController.IsSdrCase()) {
                EventAnalyzed.this.mSdrController.setSdrRatio(EventAnalyzed.this.mSdrRatio);
                EventAnalyzed.this.mSdrController.startSdr();
            }
        }
    }

    private class StartSdrForTest implements Runnable {
        /* synthetic */ StartSdrForTest(EventAnalyzed this$0, StartSdrForTest -this1) {
            this();
        }

        private StartSdrForTest() {
        }

        public void run() {
            float ratio;
            int r = SystemProperties.getInt("sys.aps.pressratio", 0);
            if (r == 0) {
                ratio = (float) Math.random();
            } else {
                ratio = ((float) r) / 100.0f;
            }
            ApsCommon.logD(EventAnalyzed.TAG, "start SdrForTest----ratio = " + ratio);
            EventAnalyzed.this.mSdrController.setSdrRatio(ratio);
            EventAnalyzed.this.mSdrController.startSdr();
            int startTime = SystemProperties.getInt("sys.aps.starttime", 0);
            if (startTime == 0) {
                startTime = 10000;
            }
            if (EventAnalyzed.this.mStopSdrForTest != null) {
                EventAnalyzed.this.mHandler.removeCallbacks(EventAnalyzed.this.mStopSdrForTest);
                EventAnalyzed.this.mHandler.postDelayed(EventAnalyzed.this.mStopSdrForTest, (long) startTime);
            }
            ApsCommon.logD(EventAnalyzed.TAG, "start SdrForTest");
        }
    }

    private class StatisFps implements Runnable {
        /* synthetic */ StatisFps(EventAnalyzed this$0, StatisFps -this1) {
            this();
        }

        private StatisFps() {
        }

        public void run() {
            if (EventAnalyzed.this.mStatisFpsCount <= 50) {
                int currentFps = EventAnalyzed.this.getCurrentFPS();
                if (currentFps == 0) {
                    EventAnalyzed.this.mIsLowFps = true;
                }
                EventAnalyzed.this.mAverageFps = ((EventAnalyzed.this.mAverageFps * EventAnalyzed.this.mStatisFpsCount) + currentFps) / (EventAnalyzed.this.mStatisFpsCount + 1);
                EventAnalyzed eventAnalyzed = EventAnalyzed.this;
                eventAnalyzed.mStatisFpsCount = eventAnalyzed.mStatisFpsCount + 1;
                ApsCommon.logI(EventAnalyzed.TAG, "SDR: StatisFps : currentFps = " + currentFps + "; mIsLowFps = " + EventAnalyzed.this.mIsLowFps + "; mAverageFps = " + EventAnalyzed.this.mAverageFps + "; mStatisFpsCount = " + EventAnalyzed.this.mStatisFpsCount);
                EventAnalyzed.this.mHandler.postDelayed(EventAnalyzed.this.mStatisFps, 300);
            }
        }
    }

    private class StopEmergency implements Runnable {
        /* synthetic */ StopEmergency(EventAnalyzed this$0, StopEmergency -this1) {
            this();
        }

        private StopEmergency() {
        }

        public void run() {
            if (EventAnalyzed.this.mEmergencyFpsRequest != null) {
                EventAnalyzed.this.mEmergencyFpsRequest.stop();
                EventAnalyzed.this.mHasIncreaseLowFPS = false;
                EventAnalyzed.this.mHasIncreaseHighFPS = false;
                ApsCommon.logD(EventAnalyzed.TAG, "stop EmergencyFpsRequest");
            }
        }
    }

    private class StopSdrForTest implements Runnable {
        /* synthetic */ StopSdrForTest(EventAnalyzed this$0, StopSdrForTest -this1) {
            this();
        }

        private StopSdrForTest() {
        }

        public void run() {
            EventAnalyzed.this.mSdrController.stopSdr();
            int stopTime = SystemProperties.getInt("sys.aps.stoptime", 0);
            if (stopTime == 0) {
                stopTime = 5000;
            }
            if (EventAnalyzed.this.mStartSdrForTest != null) {
                EventAnalyzed.this.mHandler.removeCallbacks(EventAnalyzed.this.mStartSdrForTest);
                EventAnalyzed.this.mHandler.postDelayed(EventAnalyzed.this.mStartSdrForTest, (long) stopTime);
            }
            ApsCommon.logD(EventAnalyzed.TAG, "stop SdrForTest");
        }
    }

    private class setTouchStateForNonplay implements Runnable {
        /* synthetic */ setTouchStateForNonplay(EventAnalyzed this$0, setTouchStateForNonplay -this1) {
            this();
        }

        private setTouchStateForNonplay() {
        }

        public void run() {
            if (EventAnalyzed.this.mGameState != null) {
                EventAnalyzed.this.mGameState.setTouchState(-2);
            }
        }
    }

    public EventAnalyzed() {
        if (AdCheck.isSupportAdCheck()) {
            this.mAdCheck = AdCheck.getInstance();
        }
    }

    public void initAPS(Context context, int screenWidth, int myPid) {
        boolean z;
        boolean z2 = false;
        Log.i(TAG, "APS: version is 6.0.0.24-1");
        this.mContext = context;
        this.mMyPid = myPid;
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mPkgName = context.getPackageName();
        this.mGameState = new GameState();
        this.mGameState.setApsVersion(VERSION);
        this.mGameState.setGamePid(this.mMyPid);
        this.mOperateExLib = new OperateExperienceLib();
        this.mOperateExLib.initApsLibrary(context);
        this.mFpsRequest = new FpsRequest(SceneTypeE.TOUCH_IDENTY);
        this.mEmergencyFpsRequest = new FpsRequest(SceneTypeE.TOUCH_EMERGENCY);
        this.mTouchFpsRequest = new FpsRequest(SceneTypeE.TOUCH_EMERGENCY);
        this.mExactlyFpsRequest = new FpsRequest(SceneTypeE.EXACTLY_IDENTIFY);
        queryGameInfo();
        this.mCanUseDebug = SystemProperties.getInt("sys.aps.test", 0) > 0;
        if (SystemProperties.getInt("sys.aps.performance", 0) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mPerformanceDebug = z;
        this.mStopTouchMaxFpsTime = SystemProperties.getInt("debug.aps.stoptouch.time", STOP_TOUCH_MAX_FPS_TIME);
        if (!(CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) || getCurrentBattery() > getCtrlBattery(this.mPkgName)) {
            if (ApsUserFeedback.isSupportApsUserFeedback()) {
                this.mApsUserFeedback = ApsUserFeedback.getInstance();
                this.mApsUserFeedback.setOperateExperienceLib(this.mOperateExLib);
            }
            if (ApsLdfGameIdentify.isSupportApsLdfGameIdentify()) {
                this.mApsLdfGameIdentify = ApsLdfGameIdentify.getInstance();
            }
            if (ApsLtrGameIdentify.isSupportApsLtrGameIdentify()) {
                this.mApsLtrGameIdentify = ApsLtrGameIdentify.getInstance();
            }
            if (isSupportEventAnalysis()) {
                int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                this.mScreenOrientation = context.getResources().getConfiguration().orientation;
                if (1 != this.mScreenOrientation) {
                    z2 = true;
                }
                this.mIsScreenLandScape = z2;
                initEventAnalysis(screenWidth, screenHeight);
            }
            if (ApsThermal.isSupportAPSThermal()) {
                this.mApsThermal = ApsThermal.getInstance();
            }
        }
        this.mYdpi = context.getResources().getDisplayMetrics().ydpi;
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        if (SdrController.isSupportApsSdr()) {
            this.mSdrController = SdrController.getInstance();
            ApsCommon.logI(TAG, "SDR: initial: SdrController init,mCanUseDebug : " + this.mCanUseDebug);
        }
        this.mStopSdrForTest = new StopSdrForTest(this, null);
        this.mStartSdrForTest = new StartSdrForTest(this, null);
        this.mStartSdr = new StartSdr(this, null);
        this.mStatisFps = new StatisFps(this, null);
        this.mHandler.postDelayed(this.mStatisFps, 10000);
        this.mStopEmergency = new StopEmergency(this, null);
        this.mResumeFpsByTouch = new ResumeFpsByTouch(this, null);
        this.mSetTouchStateForNonplay = new setTouchStateForNonplay(this, null);
        this.mBatteryLimitValue = SystemProperties.getInt("debug.aps.battery_limit", 20);
        this.mBatteryLimitStudyValue = SystemProperties.getInt("debug.aps.battery_limit_study", 60);
    }

    public boolean isAPSReady() {
        if (isAPSEnable()) {
            return true;
        }
        if (this.mApsThermal != null) {
            this.mApsThermal.stop();
        }
        if (this.mApsUserFeedback != null) {
            this.mApsUserFeedback.stop();
        }
        if (this.mApsLdfGameIdentify != null) {
            this.mApsLdfGameIdentify.resetApsLdfGameIdentify();
        }
        if (this.mApsLtrGameIdentify != null) {
            this.mApsLtrGameIdentify.resetApsLtrGameIdentify();
        }
        stopSdr();
        return false;
    }

    private boolean isAPSEnable() {
        int apsEnable = SystemProperties.getInt("debug.aps.enable", 0);
        if (2 == apsEnable) {
            this.mIsAnalyzedEnable = true;
        } else if (1 == apsEnable) {
            this.mIsAnalyzedEnable = true;
            this.mIsControllAllFpsEnable = true;
        } else if (CUST_GAME_TYPE == apsEnable || CUST_APP_TYPE == apsEnable) {
            this.mIsControllAllFpsEnable = true;
        } else {
            this.mIsAnalyzedEnable = false;
            this.mIsControllAllFpsEnable = false;
        }
        if (apsEnable > 0) {
            return true;
        }
        return false;
    }

    private boolean isSdrEnable() {
        if (SystemProperties.getInt("debug.aps.sdr", 0) > 0) {
            return true;
        }
        return false;
    }

    private boolean isIdentifyProcess(String strPkgName) {
        if (!this.mHasIdentifyProcess) {
            if (SystemProperties.get("debug.aps.process.name", "").equals(strPkgName)) {
                this.mIsGameProcess = true;
            } else {
                this.mIsGameProcess = false;
            }
            this.mHasIdentifyProcess = true;
        }
        return this.mIsGameProcess;
    }

    public boolean isGameProcess(String pkgName) {
        if (this.mHasIdentifyProcess && (this.mIsGameProcess ^ 1) != 0) {
            return this.mIsGameProcess;
        }
        if (isAPSReady()) {
            return isIdentifyProcess(pkgName);
        }
        return false;
    }

    public void queryGameInfo() {
        new QueryThread(this, null).start();
    }

    public boolean query() {
        loadSceneParaByGameType();
        loadSceneParaByPackageName();
        if (this.mOperateExLib != null) {
            this.mIsGameInfoExist = this.mOperateExLib.query();
        } else {
            this.mIsGameInfoExist = false;
        }
        if (this.mIsGameInfoExist) {
            this.mGameType = this.mOperateExLib.getGameType();
            this.mMaxFps = this.mOperateExLib.getMaxFps();
            this.mMinFps = this.mOperateExLib.getMinFps();
            this.mMinNonplayFps = this.mOperateExLib.getMinNonplayFps();
            this.mDesignatedFps = this.mMinFps;
            this.mDesignatedSdrRatio = this.mOperateExLib.getMinSdrRatio();
        } else {
            this.mGameType = 0;
            this.mMaxFps = 55;
            this.mMinFps = 30;
        }
        if (this.mMaxFps <= 60 || this.mMaxFps > 120) {
            this.mTouchMaxFps = 0;
        } else {
            this.mTouchMaxFps = this.mMaxFps - 60;
        }
        if (this.mGameState != null) {
            this.mGameState.setTouchGameType(this.mGameType);
        }
        if (55 == this.mMinFps && 55 == this.mMinNonplayFps) {
            if (this.mApsUserFeedback != null) {
                this.mApsUserFeedback.setNeedUserFeedBack(false);
                ApsCommon.logI(TAG, "this game do not need gpu analyze!");
            }
        } else if (this.mGameState != null) {
            this.mGameState.setNonplayFrame(this.mMinNonplayFps);
        }
        ApsCommon.logD(TAG, "mIsGameInfoExist : " + this.mIsGameInfoExist + "  , mMaxFps : " + this.mMaxFps + "  , mMinFps : " + this.mMinFps + "  , mGameType" + this.mGameType + " , mDesignatedFps:" + this.mDesignatedFps + " , mDesignatedSdrRatio:" + this.mDesignatedSdrRatio);
        return this.mIsGameInfoExist;
    }

    public void loadSceneParaByGameType() {
        for (String queryByName : mIdentifyGameType) {
            queryByName(queryByName);
        }
    }

    public void loadSceneParaByPackageName() {
        queryByName(this.mPkgName);
    }

    private void queryByName(String name) {
        if (this.mOperateExLib != null && this.mOperateExLib.queryByName(name)) {
            ArrayList<Integer> sceneFps = (ArrayList) this.mOperateExLib.getSceneFps().clone();
            ArrayList<Float> sceneRatio = (ArrayList) this.mOperateExLib.getSceneRatio().clone();
            if (sceneFps != null && sceneRatio != null) {
                int fixedFps = this.mOperateExLib.getMinFps();
                float fixedRatio = this.mOperateExLib.getMinSdrRatio();
                int ctrlBattery = this.mOperateExLib.getCtrlBattery();
                if (fixedFps > 60) {
                    this.mIsSceneFixed = false;
                } else {
                    this.mIsSceneFixed = true;
                }
                this.mHashSceneFps.put(name, sceneFps);
                this.mHashSceneRatio.put(name, sceneRatio);
                this.mHashCtrlBattery.put(name, Integer.valueOf(ctrlBattery));
                this.mHashFixdFps.put(name, Integer.valueOf(fixedFps));
                this.mHashFixdRatio.put(name, Float.valueOf(fixedRatio));
                this.mHashSceneFixed.put(name, Boolean.valueOf(this.mIsSceneFixed));
                int i = 0;
                while (i < sceneFps.size()) {
                    if (!(this.mGameState == null || sceneFps.get(i) == null)) {
                        this.mGameState.setSceneFps(name, i + 1, ((Integer) sceneFps.get(i)).intValue());
                    }
                    i++;
                }
                i = 0;
                while (i < sceneRatio.size()) {
                    if (!(this.mGameState == null || sceneRatio.get(i) == null)) {
                        this.mGameState.setSceneRatio(name, i + 1, (double) ((Float) sceneRatio.get(i)).floatValue());
                    }
                    i++;
                }
                if (this.mGameState != null) {
                    this.mGameState.setCtrlBattery(name, ctrlBattery);
                    this.mGameState.setSceneFixed(name, this.mIsSceneFixed);
                }
            }
        }
    }

    private int getFpsByGameType(int gameTypeId) {
        String name = mIdentifyGameType[gameTypeId];
        if (getCurrentBattery() > getCtrlBattery(name)) {
            return 0;
        }
        if (isSceneFixed(name)) {
            return getFixdFps(name);
        }
        return getSceneFps(name);
    }

    private float getRatioByGameType(int gameTypeId) {
        String name = mIdentifyGameType[gameTypeId];
        if (getCurrentBattery() > getCtrlBattery(name)) {
            return 0.0f;
        }
        if (isSceneFixed(name)) {
            return getFixdRatio(name);
        }
        return getSceneRatio(name);
    }

    private int getSceneFps(String name) {
        if (this.mHashSceneFps.containsKey(name)) {
            return ((Integer) ((ArrayList) this.mHashSceneFps.get(name)).get(0)).intValue();
        }
        return 0;
    }

    private float getSceneRatio(String name) {
        if (this.mHashSceneRatio.containsKey(name)) {
            return ((Float) ((ArrayList) this.mHashSceneRatio.get(name)).get(0)).floatValue();
        }
        return 0.0f;
    }

    private int getCtrlBattery(String name) {
        if (this.mHashCtrlBattery.containsKey(name)) {
            return ((Integer) this.mHashCtrlBattery.get(name)).intValue();
        }
        if (!name.equals("LR_LDF_GAME") && !name.equals("PLR_LTR_GAME") && !name.equals("NOT_DRAW_WITHOUT_DRAG") && !name.equals("RUN_GAME")) {
            return this.mBatteryLimitValue;
        }
        return this.mBatteryLimitValue > this.mBatteryLimitStudyValue ? this.mBatteryLimitValue : this.mBatteryLimitStudyValue;
    }

    private boolean isSceneFixed(String name) {
        if (this.mHashSceneFixed.containsKey(name)) {
            return ((Boolean) this.mHashSceneFixed.get(name)).booleanValue();
        }
        return false;
    }

    private int getFixdFps(String name) {
        if (this.mHashFixdFps.containsKey(name)) {
            return ((Integer) this.mHashFixdFps.get(name)).intValue();
        }
        return 0;
    }

    private float getFixdRatio(String name) {
        if (this.mHashFixdRatio.containsKey(name)) {
            return ((Float) this.mHashFixdRatio.get(name)).floatValue();
        }
        return 0.0f;
    }

    private int getCurrentBattery() {
        return SystemProperties.getInt("debug.aps.current_battery", 100);
    }

    private void increaseFPS(int fps) {
        int i = 1;
        if (!this.mIsControllAllFpsEnable) {
            if (!this.mIsAnalyzedEnable) {
                i = 0;
            } else if (!(this.mGameType == 8 || this.mGameType == 9 || this.mGameType == 10 || this.mGameType == 11)) {
                i = 0;
            }
            if ((i ^ 1) != 0) {
                return;
            }
        }
        if (55 == fps) {
            if (!this.mHasIncreaseHighFPS) {
                this.mHasIncreaseHighFPS = increaseEmergencyFPS(fps);
            }
        } else if (!(this.mHasIncreaseLowFPS || (this.mHasIncreaseHighFPS ^ 1) == 0)) {
            this.mHasIncreaseLowFPS = increaseEmergencyFPS(fps);
        }
        if (this.mIsEnterEmergencyFps) {
            this.mIsEnterEmergencyFps = false;
            this.mHandler.removeCallbacks(this.mStopEmergency);
            this.mHandler.postDelayed(this.mStopEmergency, 30000);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0007, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setGamePid(int pid) {
        if (!(this.mGameState == null || -1 == pid || this.mHasSetPid)) {
            this.mGameState.setGamePid(pid);
            this.mHasSetPid = true;
        }
    }

    private void analyzeDownAction(int x, int y, long downTime) {
        this.mCurrentDownTime = downTime;
        this.mLastDownX = x;
        this.mLastDownY = y;
        analyzeScreenRegion(x, y);
        if (-1 != this.mLastUpTime) {
            this.mDownUpInterval = this.mCurrentDownTime - this.mLastUpTime;
            if (this.mDownUpInterval >= 1000) {
                this.mDownUpLongSum += this.mDownUpInterval;
                this.mConsecutiveShortDownUpCount = 0;
                this.mLongDownUpCount++;
                return;
            }
            this.mDownUpShortSum += this.mDownUpInterval;
            this.mShortDownUpCount++;
            this.mConsecutiveShortDownUpCount++;
            if (this.mConsecutiveShortDownUpCount > 5) {
                if (this.mCanUseDebug && this.mSdrController != null && this.mSdrController.IsSdrCase()) {
                    int pressure = SystemProperties.getInt("sys.aps.pressure", 0);
                    if (this.mAverageFps < 10 || (this.mIsLowFps ^ 1) == 0 || this.mStatisFpsCount < 50) {
                        Log.e(TAG, "APS: SDR: the fps is too small and mAverageFps = " + this.mAverageFps + "; mIsLowFps = " + this.mIsLowFps);
                    } else if (pressure == 0) {
                        int r = SystemProperties.getInt("sys.aps.ratio", 0);
                        float ratio = 0.5f;
                        if (r != 0) {
                            ratio = ((float) r) / 100.0f;
                        }
                        this.mSdrController.setSdrRatio(ratio);
                        this.mSdrController.startSdr();
                    } else {
                        this.mHandler.postDelayed(this.mStartSdrForTest, 0);
                    }
                }
                increaseFPS(45);
            }
        }
    }

    private void analyzeMoveAction(long eventTime, long downTime) {
        this.mMoveTime = eventTime - downTime;
        if (this.mMoveTime >= STRATEGY_TIME) {
            this.mIsLongMove = true;
            if (this.mCanUseDebug) {
                stopSdr();
            }
            increaseFPS(55);
        }
    }

    private void analyzeUpAction(long eventTime, long downTime, int x, int y) {
        this.mLastUpTime = eventTime;
        this.isXScroll = false;
        this.isYScroll = false;
        int UpAndDownDeltX = Math.abs(x - this.mLastDownX);
        int UpAndDownDeltY = Math.abs(y - this.mLastDownY);
        this.mDownMoveInterval = eventTime - downTime;
        if (this.mDownMoveInterval > 500) {
            this.mSlowScrollCount++;
        }
        if (this.mCurrentDownTime != downTime) {
            UpAndDownDeltX = 0;
            UpAndDownDeltY = 0;
            ApsCommon.logD(TAG, "MultiTouch for up Action");
        }
        analyzeTouchDistance(UpAndDownDeltX, UpAndDownDeltY);
        if ((this.mXScrollCount >= 3 && this.mYScrollCount > 0) || ((this.mYScrollCount >= 3 && this.mXScrollCount > 0) || this.mXScrollCount >= 4 || this.mYScrollCount >= 4)) {
            this.mXScrollCount = 0;
            this.mYScrollCount = 0;
            increaseFPS(55);
            this.mIsAllScroll = true;
        }
        if (this.mDownMoveInterval >= 1000 && (this.isXScroll ^ 1) != 0 && (this.isYScroll ^ 1) != 0) {
            this.mLongPressCount++;
            if (this.mLongPressCount > 3) {
                increaseFPS(45);
            }
        }
    }

    public void collectInputEvent(int action, int x, int y, long downTime, long eventTime) {
        if (this.mApsLtrGameIdentify != null && this.mIsScreenLandScape) {
            this.mApsLtrGameIdentify.collectInputEvent(action, x, y);
        }
        if (!this.mIsScreenLandScape && this.mApsLdfGameIdentify != null) {
            this.mApsLdfGameIdentify.collectInputEvent(action, x, y, eventTime, downTime);
        }
    }

    public void analyzeInputEvent(int action, long eventTime, int x, int y, int pointCount, long downTime) {
        collectInputEvent(action, x, y, downTime, eventTime);
        this.mAction = action;
        switch (action) {
            case 0:
                analyzeDownAction(x, y, downTime);
                return;
            case 1:
                analyzeUpAction(eventTime, downTime, x, y);
                return;
            case 2:
                analyzeMoveAction(eventTime, downTime);
                return;
            case 3:
                return;
            default:
                if (pointCount > 1) {
                    this.mMultiTouchCount++;
                    if (this.mMultiTouchCount > 2) {
                        increaseFPS(45);
                        return;
                    }
                    return;
                }
                return;
        }
    }

    public void setHasOnPaused(boolean hasOnPaused) {
        boolean haspause = this.mHasOnPaused;
        this.mHasOnPaused = hasOnPaused;
        if (this.mApsThermal != null) {
            this.mApsThermal.stop();
        } else {
            ApsCommon.logI(TAG, "setHasOnPaused-ApsThermal is null");
        }
        if (this.mApsUserFeedback != null) {
            this.mApsUserFeedback.stop();
        } else {
            ApsCommon.logI(TAG, "setHasOnPaused-ApsUserFeedback is null");
        }
        if (this.mApsLdfGameIdentify != null) {
            this.mApsLdfGameIdentify.resetApsLdfGameIdentify();
        } else {
            ApsCommon.logI(TAG, "setHasOnPaused-mApsLdfGameIdentify is null");
        }
        if (this.mApsLtrGameIdentify != null) {
            this.mApsLtrGameIdentify.resetApsLtrGameIdentify();
        } else {
            ApsCommon.logI(TAG, "setHasOnPaused-mApsLtrGameIdentify is null");
        }
        ApsCommon.logD(TAG, "setHasOnPaused   lastValue  : " + haspause + "  ,  newValue :  " + this.mHasOnPaused);
    }

    private boolean isLtrGame() {
        String apsPkgName = SystemProperties.get("debug.aps.process.name", "");
        if (this.mGameState == null || (this.mGameState.isDepthGame() ^ 1) == 0 || this.mApsLtrGameIdentify == null) {
            return false;
        }
        return this.mApsLtrGameIdentify.isLtrGame(apsPkgName);
    }

    private boolean isLdfGame() {
        if (this.mGameState == null || (this.mGameState.isDepthGame() ^ 1) == 0 || this.mApsLdfGameIdentify == null || (this.mIsAllScroll ^ 1) == 0) {
            return false;
        }
        return this.mApsLdfGameIdentify.isLdfGame();
    }

    private int analyzeGameType(int shortAndLongTouchProportion, int totalTouchCount) {
        if (this.mIsLongMove) {
            ApsCommon.logD(TAG, "is RealTime Game");
            return 0;
        } else if (isLtrGame()) {
            ApsCommon.logD(TAG, "is PLR_LTR_GAME Game");
            return 9;
        } else if (!this.mIsAllScroll || this.mMultiTouchCount >= 3) {
            if (isLdfGame()) {
                ApsCommon.logD(TAG, "is PLR_LDF_GAME Game ");
                return 8;
            } else if (!this.mIsAllScroll && this.mAvageDownUpLongTime > STRATEGY_TIME && this.mMultiTouchCount < 2 && shortAndLongTouchProportion < 5 && this.mLongPressCount < 3 && totalTouchCount > 5 && this.mConsecutiveShortDownUpCount <= 7) {
                ApsCommon.logD(TAG, "is Strategy Game");
                return 1;
            } else if (this.mIsSingleRegionAvailalbeGame) {
                ApsCommon.logD(TAG, "is SingleRegionAvailalbeGame");
                return 4;
            } else if (this.mIsFullScreenTouchGame) {
                ApsCommon.logD(TAG, "is FullScreenTouchGame");
                return 4;
            } else {
                ApsCommon.logD(TAG, "is Other Game");
                return 3;
            }
        } else if (this.mClickCount < 3 && (this.mIsScreenLandScape ^ 1) != 0 && this.mSlowScrollCount < 3) {
            ApsCommon.logD(TAG, "is Scroll Static Game ");
            return 7;
        } else if (this.mIsScreenLandScape) {
            ApsCommon.logD(TAG, "is LandScape Scroll Game ");
            return 6;
        } else {
            ApsCommon.logD(TAG, "is Scroll Game ");
            return 2;
        }
    }

    public int collectData(long currentTime) {
        int shortAndLongTouchProportion = -1;
        computeAvailableRegion();
        if (0 == this.mBeginAnalyzeTime) {
            resetAnalyzePara();
            return -1;
        }
        if (2 == this.mAction) {
            analyzeMoveAction(currentTime, this.mCurrentDownTime);
        }
        if (this.mLongDownUpCount != 0) {
            this.mAvageDownUpLongTime = this.mDownUpLongSum / ((long) this.mLongDownUpCount);
            shortAndLongTouchProportion = this.mShortDownUpCount / this.mLongDownUpCount;
        }
        this.mMultiTouchCount /= 2;
        int totalTouchCount = this.mLongDownUpCount + this.mShortDownUpCount;
        ApsCommon.logD(TAG, "mIsLongMove = " + this.mIsLongMove + " , mIsAllScroll : " + this.mIsAllScroll + " , mAvageDownUpLongTime = " + this.mAvageDownUpLongTime + " , mMultiTouchCount = " + this.mMultiTouchCount + " , shortAndLongTouchProportion = " + shortAndLongTouchProportion + " , mLongPressCount  = " + this.mLongPressCount + " , totalTouchCount = " + totalTouchCount + " , mClickCount : " + this.mClickCount + " , mSlowScrollCount : " + this.mSlowScrollCount);
        int type = analyzeGameType(shortAndLongTouchProportion, totalTouchCount);
        this.mGameType = type;
        resetAnalyzePara();
        return type;
    }

    private void setFirstEnterFPS(int gameType) {
        if (!this.mhasFirstSetFPS) {
            int fps = 60;
            if (CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) {
                if (getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                    fps = this.mIsSceneFixed ? this.mDesignatedFps : ((Integer) ((ArrayList) this.mHashSceneFps.get(this.mPkgName)).get(0)).intValue();
                }
            } else if (getCurrentBattery() <= getCtrlBattery(mIdentifyGameType[gameType])) {
                fps = this.mMaxFps;
            }
            setFPS(fps, gameType);
            this.mhasFirstSetFPS = true;
            ApsCommon.logD(TAG, "first enter  setFPS : " + fps);
        }
    }

    public void setFPS(int fps, int gameType) {
        if (!mStopSetFps || 8 == this.mGameType || 9 == this.mGameType || 10 == this.mGameType || 11 == this.mGameType) {
            if (this.mExactlyFpsRequest == null || !(gameType == 8 || gameType == 9 || ((CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) && getCurrentBattery() <= getCtrlBattery(this.mPkgName)))) {
                if (!(this.mFpsRequest == null || this.mPerformanceDebug)) {
                    this.mFpsRequest.start(fps);
                }
            } else if (!this.mPerformanceDebug) {
                this.mExactlyFpsRequest.start(fps);
            }
            sendBroadcastBaseOnGame(fps <= 30 ? 1 : 0);
        }
    }

    public void setAdaptFPS(int gameType, int openGLType, int level) {
        if (-1 != gameType) {
            ApsCommon.logD(TAG, "mGameType = " + gameType + " , mLastGameType = " + this.mLastGameType);
            if (this.mLastGameType != gameType) {
                this.mLastGameType = gameType;
                this.mHasSetFPS = false;
            } else if (!this.mHasSetFPS) {
                this.mGameType = gameType;
                int recommendFPS = computeRecommendFPS(gameType, openGLType, level);
                if (gameType == 8 || gameType == 9) {
                    this.mOperateExLib.saveAPSResult(gameType, recommendFPS, recommendFPS);
                } else {
                    this.mOperateExLib.saveAPSResult(gameType, recommendFPS, this.mMinFps);
                }
                if (getCurrentBattery() <= getCtrlBattery(mIdentifyGameType[gameType])) {
                    this.mHasSetFPS = true;
                    setFPS(recommendFPS, gameType);
                    Log.d(TAG, "APS: action type is " + gameType + ", display type is " + openGLType);
                }
            }
        }
    }

    public int computeRecommendFPS(int gameType, int openGLType, int level) {
        if (getCurrentBattery() > getCtrlBattery(mIdentifyGameType[gameType])) {
            return 60;
        }
        int recommendFPS = 60;
        switch (gameType) {
            case 0:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 45;
                    break;
                }
                break;
            case 1:
                recommendFPS = 30;
                if (1 == level) {
                    recommendFPS = 20;
                    break;
                }
                break;
            case 2:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
            case 3:
                recommendFPS = 45;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
            case 4:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 50;
                    break;
                }
                break;
            case 5:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 45;
                    break;
                }
                break;
            case 6:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
            case SCROLL_STATIC_GAME /*7*/:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
            case PLR_LDF_GAME /*8*/:
                recommendFPS = 20;
                if (1 == level) {
                    recommendFPS = 20;
                    break;
                }
                break;
            case 9:
                recommendFPS = 30;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
            case NOT_DRAW_WITHOUT_DRAG /*10*/:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
            case 11:
                recommendFPS = 55;
                if (1 == level) {
                    recommendFPS = 30;
                    break;
                }
                break;
        }
        if (8 != gameType && recommendFPS < this.mMinFps) {
            recommendFPS = this.mMinFps;
        }
        int fps = getFpsByGameType(gameType);
        if (fps == 0) {
            return recommendFPS;
        }
        return fps;
    }

    public void resetAnalyzePara() {
        int i;
        this.mMultiTouchCount = 0;
        this.mXScrollCount = 0;
        this.mYScrollCount = 0;
        this.mLongPressCount = 0;
        this.mIsAllScroll = false;
        this.mIsFirstStartAnalyze = true;
        this.mBeginAnalyzeTime = 0;
        this.mLastUpTime = -1;
        this.mIsLongMove = false;
        this.mShortDownUpCount = 0;
        this.mLongDownUpCount = 0;
        this.mDownUpLongSum = 0;
        this.mDownUpShortSum = 0;
        this.mHasIncreaseHighFPS = false;
        this.mHasIncreaseLowFPS = false;
        this.mConsecutiveShortDownUpCount = 0;
        this.mIsEnterEmergencyFps = false;
        this.mIsSingleRegionAvailalbeGame = false;
        this.mIsFullScreenTouchGame = false;
        resetScreenRegion();
        this.mClickCount = 0;
        this.mSlowScrollCount = 0;
        ApsCommon.logD(TAG, "resetAnalyzePara");
        if (!this.mCanUseDebug && CUST_GAME_TYPE == this.mGameType && getCurrentBattery() <= getCtrlBattery(this.mPkgName) && this.mSdrController != null && this.mSdrController.IsSdrCase()) {
            if (this.mIsSceneFixed) {
                this.mSdrController.setSdrRatio(this.mDesignatedSdrRatio);
            } else {
                this.mSdrController.setSdrRatio(((Float) ((ArrayList) this.mHashSceneRatio.get(this.mPkgName)).get(0)).floatValue());
            }
            this.mSdrController.startSdr();
        }
        if (this.mIsLowFpsGame) {
            i = 1;
        } else {
            i = 0;
        }
        sendBroadcastBaseOnGame(i);
    }

    public void releaseAPS() {
        if (!this.mHasReleaseAPS && (this.mFpsRequest != null || this.mExactlyFpsRequest != null)) {
            if (this.mFpsRequest != null) {
                this.mFpsRequest.stop();
            }
            if (this.mExactlyFpsRequest != null) {
                this.mExactlyFpsRequest.stop();
            }
            resetAnalyzePara();
            this.mHasReleaseAPS = true;
            this.mHasSetPid = false;
            ApsCommon.logD(TAG, "releaseAPS");
        }
    }

    private int getLcdFpsScence() {
        return SystemProperties.getInt("debug.aps.lcd_fps_scence", 60);
    }

    private void sendBroadcastBaseOnAction(int action, long eventTime) {
        if (this.mSendBroadcast != null && this.mHandler != null && this.mContext != null) {
            if (action == 0) {
                if (eventTime - this.mUpTime < 5000) {
                    this.mHandler.removeCallbacks(this.mSendBroadcast);
                } else {
                    this.mSendBroadcast.setActionIntent(new Intent(APS_TOUCH_ACTION_DOWN));
                    this.mHandler.post(this.mSendBroadcast);
                }
            } else if (action == 1) {
                this.mSendBroadcast.setActionIntent(new Intent(APS_TOUCH_ACTION_UP));
                this.mHandler.postDelayed(this.mSendBroadcast, 5000);
                this.mUpTime = eventTime;
            }
        }
    }

    private void sendBroadcastBaseOnGame(int mode) {
        if (this.mSendBroadcast != null && this.mHandler != null && this.mContext != null) {
            int fpsValue = getLcdFpsScence();
            if (60 == fpsValue && 1 == mode) {
                this.mSendBroadcast.setActionIntent(new Intent(APS_LOW_FPS_GAME));
                this.mHandler.post(this.mSendBroadcast);
                this.mIsLowFpsGame = true;
            } else if (30 == fpsValue && mode == 0) {
                this.mSendBroadcast.setActionIntent(new Intent(APS_NOT_LOW_FPS_GAME));
                this.mHandler.post(this.mSendBroadcast);
                this.mIsLowFpsGame = false;
            }
        }
    }

    private void setPowerkitFrame(int frame) {
        if (this.mGameState == null) {
            this.mGameState = new GameState();
        }
        this.mGameState.setPowerKitFrame(frame);
    }

    public void processAnalyze(Context context, int action, long eventTime, int x, int y, int pointCount, long downTime) {
        if (action == 0 || action == 1) {
            setTouchState(action);
        }
        if (SmartLowpowerBrowser.needTouchState()) {
            if (this.mHwapsFactoryImpl == null) {
                this.mHwapsFactoryImpl = new HwapsFactoryImpl();
            }
            this.mHwapsFactoryImpl.getSmartLowpowerBrowser().setTouchState(action);
        }
        if (this.mContext == null) {
            this.mContext = context;
        }
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        if (9000 == SystemProperties.getInt("debug.aps.enable", 0)) {
            if (this.mSendBroadcast == null) {
                this.mSendBroadcast = new SendBroadcast(this, null);
            }
            sendBroadcastBaseOnAction(action, eventTime);
        } else if (ApsTest.isSupportAPSTset()) {
            if (this.mApsTest == null) {
                this.mApsTest = new ApsTest();
                this.mApsTest.start();
            }
        } else {
            int apsEnable = SystemProperties.getInt("debug.aps.enable", 0);
            if (action == 1 && eventTime - this.mLastEventTime > 10000 && this.mTouchMaxFps == 0) {
                if (this.mApsManager == null) {
                    this.mApsManager = HwFrameworkFactory.getApsManager();
                }
                if (this.mPowerKitFpsRequest == null) {
                    this.mPowerKitFpsRequest = new FpsRequest(SceneTypeE.OPENGL_SETTING);
                }
                this.mLastEventTime = eventTime;
                int fps = this.mApsManager.getFps(context.getPackageName());
                if (fps >= 15 && fps < 60) {
                    this.mPowerKitFpsRequest.start(fps);
                    setPowerkitFrame(fps);
                    this.mControlByAppSelf = true;
                    return;
                } else if (fps >= 60 && this.mControlByAppSelf) {
                    if (this.mResumePowerKit == null) {
                        this.mResumePowerKit = new ResumePowerKit(this, null);
                    }
                    setPowerkitFrame(59);
                    this.mPowerKitFpsRequest.stop();
                    this.mHandler.removeCallbacks(this.mResumePowerKit);
                    this.mHandler.postDelayed(this.mResumePowerKit, 200);
                } else if (fps == -1) {
                    this.mPowerKitFpsRequest.stop();
                    setPowerkitFrame(60);
                    this.mControlByAppSelf = true;
                }
            }
            if (this.mIsGameProcess) {
                if (this.mHasOnPaused) {
                    resetAnalyzePara();
                    this.mHasOnPaused = false;
                    if (this.mApsThermal != null) {
                        this.mApsThermal.resume();
                    }
                    if (this.mApsUserFeedback != null) {
                        this.mApsUserFeedback.resume();
                    }
                }
                if ((action == 0 || this.mFirstTimeResumeFps) && this.mTouchMaxFps > 0) {
                    this.mFirstTimeResumeFps = false;
                    this.mHandler.removeCallbacks(this.mResumeFpsByTouch);
                    this.mTouchFpsRequest.start(this.mTouchMaxFps);
                } else if (action == 1) {
                    this.mHandler.postDelayed(this.mResumeFpsByTouch, (long) this.mStopTouchMaxFpsTime);
                }
                if (CUST_GAME_TYPE == this.mGameType) {
                    if (getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                        if (!this.mCanUseDebug && this.mSdrController != null && this.mSdrController.IsSdrCase() && action == 0) {
                            if (this.mIsSceneFixed) {
                                this.mSdrController.setSdrRatio(this.mDesignatedSdrRatio);
                            } else {
                                this.mSdrController.setSdrRatio(((Float) ((ArrayList) this.mHashSceneRatio.get(this.mPkgName)).get(0)).floatValue());
                            }
                            this.mSdrController.startSdr();
                        }
                        return;
                    }
                }
                if (CUST_APP_TYPE == this.mGameType) {
                    if (getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                        return;
                    }
                }
                if (this.mIsFirstStartAnalyze) {
                    this.mBeginAnalyzeTime = eventTime;
                    this.mIsFirstStartAnalyze = false;
                }
                long currentTime = eventTime;
                if (eventTime - this.mBeginAnalyzeTime <= ANALYZE_TIME) {
                    analyzeInputEvent(action, eventTime, x, y, pointCount, downTime);
                } else {
                    int gametype = collectData(eventTime);
                    int openGLType = 0;
                    if (this.mGameState != null) {
                        openGLType = this.mGameState.getOpenglGameType();
                    }
                    if (openGLType == 10 || (openGLType == 11 && (this.mIsScreenLandScape ^ 1) != 0)) {
                        gametype = openGLType;
                    }
                    setTouchGameType(gametype);
                    isCertainGameType(gametype, openGLType);
                    setAdaptFPS(gametype, openGLType, this.mLevel);
                    setAdaptSdr(gametype, openGLType);
                }
            }
        }
    }

    public void setFPSLevel(int level) {
        this.mLevel = level;
    }

    public int getCurrentFPS() {
        if (this.mFpsRequest != null) {
            return this.mFpsRequest.getCurFPS();
        }
        return 55;
    }

    private void initEventAnalysis(int screenWidth, int screenHeight) {
        this.mScreenWidth = screenWidth;
        this.mScreenWidthRegion = screenWidth / 3;
        this.mScreenHeightRegion = screenHeight / 3;
        this.mMaxDelta = (int) (((float) this.mScreenWidth) * 0.2f);
        this.mMiddleDelta = (int) (((float) this.mScreenWidth) * 0.15f);
        this.mShortDelta = (int) (((float) this.mScreenWidth) * 0.02f);
        setParaForGameIdentify(screenWidth, screenHeight);
        if (this.mMaxDelta == 0) {
            this.mMaxDelta = 200;
        }
        if (this.mMiddleDelta == 0) {
            this.mMiddleDelta = DEFAULT_MIDDLE_DALTA;
        }
        if (this.mShortDelta == 0) {
            this.mShortDelta = 15;
        }
        this.mHasReleaseAPS = false;
        ApsCommon.logI(TAG, "initEventAnalysis");
    }

    private void setParaForGameIdentify(int screenWidth, int screenHeight) {
        if (this.mApsLtrGameIdentify != null) {
            this.mApsLtrGameIdentify.setParaForLtrGameIdentify(screenWidth, screenHeight);
        }
        if (this.mApsLdfGameIdentify != null) {
            this.mApsLdfGameIdentify.setParaForLdfGameIdentify(screenWidth);
        }
    }

    private void analyzeScreenRegion(int x, int y) {
        if (this.mScreenWidthRegion != 0 && this.mScreenHeightRegion != 0) {
            int xIndex = x / this.mScreenWidthRegion;
            int yIndex = y / this.mScreenHeightRegion;
            if (xIndex > 2) {
                xIndex = 2;
            }
            if (yIndex > 2) {
                yIndex = 2;
            }
            int screenRegionIndex = xIndex + (yIndex * 3);
            if (screenRegionIndex < this.mSceenRegion.length && screenRegionIndex >= 0) {
                int[] iArr = this.mSceenRegion;
                iArr[screenRegionIndex] = iArr[screenRegionIndex] + 1;
            }
        }
    }

    private void computeAvailableRegion() {
        int maxPointcount = 0;
        int sceondPointCount = 0;
        int usedRegionCount = 0;
        for (int i = 0; i < this.mSceenRegion.length; i++) {
            if (this.mSceenRegion[i] > maxPointcount) {
                sceondPointCount = maxPointcount;
                maxPointcount = this.mSceenRegion[i];
            } else if (this.mSceenRegion[i] > sceondPointCount) {
                sceondPointCount = this.mSceenRegion[i];
            }
            if (this.mSceenRegion[i] > 1) {
                usedRegionCount++;
            }
        }
        if (sceondPointCount != 0 && maxPointcount / sceondPointCount > 2) {
            this.mIsSingleRegionAvailalbeGame = true;
        }
        if (usedRegionCount > 4) {
            this.mIsFullScreenTouchGame = true;
        }
    }

    private void resetScreenRegion() {
        for (int i = 0; i < this.mSceenRegion.length; i++) {
            this.mSceenRegion[i] = 0;
        }
    }

    private boolean isSupportEventAnalysis() {
        if (1 == (SystemProperties.getInt("sys.aps.support", 0) & 1)) {
            return true;
        }
        return false;
    }

    private void setTouchState(int state) {
        if (this.mGameState == null) {
            this.mGameState = new GameState();
        }
        this.mGameState.setTouchState(state);
        if (this.mIsGameProcess && 1 == state) {
            this.mHandler.removeCallbacks(this.mSetTouchStateForNonplay);
            this.mHandler.postDelayed(this.mSetTouchStateForNonplay, 2000);
        }
    }

    private void setTouchGameType(int type) {
        if (this.mGameState != null) {
            if (type == 10) {
                type = 7;
            }
            this.mGameState.setTouchGameType(type);
        }
    }

    private void setEmergencyFPS(int fps) {
        if (!mStopSetFps || 8 == this.mGameType || 9 == this.mGameType || 10 == this.mGameType || 11 == this.mGameType) {
            if (fps < this.mMinFps) {
                fps = this.mMinFps;
            }
            if ((CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) && getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                if (this.mIsSceneFixed) {
                    fps = this.mDesignatedFps;
                } else {
                    fps = ((Integer) ((ArrayList) this.mHashSceneFps.get(this.mPkgName)).get(2)).intValue();
                    if (fps == 60) {
                        fps = 55;
                    }
                }
            }
            if (!(this.mEmergencyFpsRequest == null || this.mPerformanceDebug)) {
                this.mEmergencyFpsRequest.start(fps);
                sendBroadcastBaseOnGame(fps <= 30 ? 1 : 0);
            }
        }
    }

    private boolean isCertainGameType(int gameType, int openGLType) {
        ApsCommon.logD(TAG, "isCertainGameType  gameType : " + gameType + ", openGLType : " + openGLType);
        if (2 != gameType && gameType != 0 && (3 != gameType || (1 != openGLType && 2 != openGLType))) {
            return false;
        }
        ApsCommon.logD(TAG, "this game is CertainGame : ");
        return true;
    }

    private void analyzeTouchDistance(int xDistance, int yDistance) {
        if (xDistance > this.mMaxDelta) {
            this.mXScrollCount++;
            this.isXScroll = true;
        }
        if (yDistance > this.mMaxDelta) {
            this.mYScrollCount++;
            this.isYScroll = true;
        }
        if (xDistance < this.mShortDelta && yDistance < this.mShortDelta) {
            this.mClickCount++;
        }
    }

    private int getTargetFPS() {
        if (this.mFpsRequest != null) {
            return this.mFpsRequest.getTargetFPS();
        }
        return 55;
    }

    private boolean increaseEmergencyFPS(int increaseFps) {
        int targetFps = getTargetFPS();
        if (-1 == targetFps) {
            ApsCommon.logD(TAG, "target FPS is default, do not increase FPS!");
            return false;
        } else if (increaseFps - targetFps < 3) {
            return false;
        } else {
            setEmergencyFPS(increaseFps);
            this.mIsEnterEmergencyFps = true;
            ApsCommon.logD(TAG, "increase FPS to : " + increaseFps + " , target FPS is : " + targetFps);
            return true;
        }
    }

    public static void stopStudyAndControl(boolean isStop) {
        mStopSetFps = isStop;
    }

    public boolean isAdCheckEnable(String pkgName) {
        if (this.mAdCheck == null) {
            return false;
        }
        return this.mAdCheck.isAdCheckEnable(pkgName);
    }

    public int checkAd(String clsName) {
        if (this.mAdCheck == null) {
            return -1;
        }
        return this.mAdCheck.checkAd(clsName);
    }

    public boolean StopSdrForSpecial(String Info, int keyCode) {
        return SdrController.StopSdrForSpecial(Info, keyCode);
    }

    public float computeRecommendSdrRatio(int gameType) {
        if (getCurrentBattery() > getCtrlBattery(mIdentifyGameType[gameType])) {
            return 1.0f;
        }
        float sdrRatio = 1.0f;
        float dpi = 1.0f / this.mYdpi;
        switch (gameType) {
            case PLR_LDF_GAME /*8*/:
                sdrRatio = 244.0f * dpi;
                break;
            case 9:
                sdrRatio = 244.0f * dpi;
                break;
            case NOT_DRAW_WITHOUT_DRAG /*10*/:
                sdrRatio = 244.0f * dpi;
                break;
        }
        float ratio = getRatioByGameType(gameType);
        if (((double) ratio) > 1.0d || ((double) ratio) <= 0.0d) {
            return sdrRatio;
        }
        return ratio;
    }

    public void setAdaptSdr(int gameType, int openGLType) {
        if ((-1 == gameType && -1 != openGLType) || this.mSdrController == null) {
            return;
        }
        if (!this.mCanUseDebug && CUST_GAME_TYPE == this.mGameType && getCurrentBattery() <= getCtrlBattery(this.mPkgName) && this.mSdrController.IsSdrCase()) {
            if (this.mIsSceneFixed) {
                this.mSdrController.setSdrRatio(this.mDesignatedSdrRatio);
            } else {
                this.mSdrController.setSdrRatio(((Float) ((ArrayList) this.mHashSceneRatio.get(this.mPkgName)).get(0)).floatValue());
            }
            this.mSdrController.startSdr();
        } else if (CUST_APP_TYPE != this.mGameType || getCurrentBattery() > getCtrlBattery(this.mPkgName)) {
            if (gameType == 7 && openGLType == 10) {
                gameType = 10;
            }
            if ((gameType == 10 || gameType == 8 || gameType == 9) && this.mIsAnalyzedEnable && !this.mCanUseDebug && this.mAverageFps >= 10 && (this.mIsLowFps ^ 1) != 0 && this.mStatisFpsCount >= 50) {
                this.mHandler.removeCallbacks(this.mStartSdr);
                this.mHandler.postDelayed(this.mStartSdr, 2000);
            }
        }
    }

    private void stopSdr() {
        if (this.mSdrController != null) {
            this.mSdrController.stopSdr();
        }
    }

    private void resumeSdr() {
        if (this.mIsGameInfoExist) {
            this.mGameType = this.mOperateExLib.getGameType();
        }
        setAdaptSdr(this.mGameType, -1);
    }

    public boolean isBackground() {
        if (this.mActivityManager == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : this.mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(this.mPkgName) && appProcess.importance != 100) {
                ApsCommon.logD(TAG, "SDR: The pkg is in Background and pkg: " + this.mPkgName);
                return true;
            }
        }
        ApsCommon.logD(TAG, "SDR: The pkg is not in Background and pkg: " + this.mPkgName);
        return false;
    }

    private void checkBackground() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (EventAnalyzed.this.isBackground() && EventAnalyzed.this.mSdrController != null) {
                    ApsCommon.logI(EventAnalyzed.TAG, "SDR: stop SDR because the package is in background");
                    EventAnalyzed.this.mSdrController.stopSdrImmediately();
                }
            }
        }, 0);
    }

    public String[] getCustAppList(Context context, int type) {
        return OperateExperienceLib.getCustAppList(context, type);
    }

    public String[] getQueryResultGameList(Context context, int type) {
        return OperateExperienceLib.getQueryResultGameList(context, type);
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        int maxDimRatio = Integer.parseInt(SystemProperties.get("sys.aps.maxDimRatio", "-1"));
        int minBrightDuration = Integer.parseInt(SystemProperties.get("sys.aps.minBrightDuration", "-1"));
        if (maxDimRatio == -1 || minBrightDuration == -1 || screenOffTimeout <= minBrightDuration) {
            return -1;
        }
        return (screenOffTimeout * maxDimRatio) / 100;
    }
}
