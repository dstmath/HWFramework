package com.huawei.android.hwaps;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
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
    private static final int MAX_SCREEN_REGION_INDEX = 2;
    private static final int MAX_SCREEN_REGION_ROW = 3;
    private static final int MAX_SCREEN_REGION_SIZE = 9;
    public static final float MIDDLE_DALTA_RATE = 0.15f;
    private static String NODE_PATH = null;
    public static final int NOT_DRAW_WITHOUT_DRAG = 10;
    public static final int NOT_LOW_POWER_MODE_30HZ = 0;
    private static final int ONE = 1;
    private static int ON_PAUSE = 0;
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
    private static final String VERSION = "3.3.11";
    private static final int ZERO = 0;
    private static final String[] mIdentifyGameType = null;
    private static boolean mStopSetFps;
    private boolean isXScroll;
    private boolean isYScroll;
    private int mAction;
    private ActivityManager mActivityManager;
    private AdCheck mAdCheck;
    private ApsLdfGameIdentify mApsLdfGameIdentify;
    private ApsLtrGameIdentify mApsLtrGameIdentify;
    private ApsTest mApsTest;
    private ApsThermal mApsThermal;
    private ApsUserFeedback mApsUserFeedback;
    private long mAvageDownUpLongTime;
    private int mAverageFps;
    private int mBatteryLimitStudyValue;
    private int mBatteryLimitValue;
    private long mBeginAnalyzeTime;
    private boolean mCanUseDebug;
    private int mClickCount;
    private int mConsecutiveShortDownUpCount;
    private Context mContext;
    private long mCurrentDownTime;
    private int mDesignatedFps;
    private float mDesignatedSdrRatio;
    private long mDownMoveInterval;
    private long mDownUpInterval;
    private long mDownUpLongSum;
    private long mDownUpShortSum;
    private FpsRequest mEmergencyFpsRequest;
    private FpsRequest mExactlyFpsRequest;
    private FpsRequest mFpsRequest;
    private GameState mGameState;
    private int mGameType;
    private Handler mHandler;
    private boolean mHasIdentifyProcess;
    private boolean mHasIncreaseHighFPS;
    private boolean mHasIncreaseLowFPS;
    private boolean mHasOnPaused;
    private boolean mHasReleaseAPS;
    private boolean mHasSetFPS;
    private boolean mHasSetPid;
    private HashMap<String, Integer> mHashCtrlBattery;
    private HashMap<String, Integer> mHashFixdFps;
    private HashMap<String, Float> mHashFixdRatio;
    private HashMap<String, Boolean> mHashSceneFixed;
    private HashMap<String, ArrayList<Integer>> mHashSceneFps;
    private HashMap<String, ArrayList<Float>> mHashSceneRatio;
    private boolean mIsAllScroll;
    private boolean mIsAnalyzedEnable;
    private boolean mIsControllAllFpsEnable;
    private boolean mIsEnterEmergencyFps;
    private boolean mIsFirstStartAnalyze;
    private boolean mIsFullScreenTouchGame;
    private boolean mIsGameInfoExist;
    private boolean mIsGameProcess;
    private boolean mIsLongMove;
    private boolean mIsLowFps;
    private boolean mIsLowFpsGame;
    private boolean mIsSceneFixed;
    private boolean mIsScreenLandScape;
    private boolean mIsSingleRegionAvailalbeGame;
    private int mLastDownX;
    private int mLastDownY;
    private int mLastGameType;
    private long mLastUpTime;
    private int mLevel;
    private int mLongDownUpCount;
    private int mLongPressCount;
    private int mMaxDelta;
    private int mMaxFps;
    private int mMiddleDelta;
    private int mMinFps;
    private int mMinNonplayFps;
    private long mMoveTime;
    private int mMultiTouchCount;
    private int mMyPid;
    private OperateExperienceLib mOperateExLib;
    private boolean mPerformanceDebug;
    private String mPkgName;
    private ResumeFpsByTouch mResumeFpsByTouch;
    private int[] mSceenRegion;
    private int mScreenHeightRegion;
    private int mScreenOrientation;
    private int mScreenWidth;
    private int mScreenWidthRegion;
    private SdrController mSdrController;
    private float mSdrRatio;
    private setTouchStateForNonplay mSetTouchStateForNonplay;
    private int mShortDelta;
    private int mShortDownUpCount;
    private int mSlowScrollCount;
    private StartSdr mStartSdr;
    private StartSdrForTest mStartSdrForTest;
    private StatisFps mStatisFps;
    private int mStatisFpsCount;
    private StopEmergency mStopEmergency;
    private StopSdrForTest mStopSdrForTest;
    private int mStopTouchMaxFpsTime;
    private FpsRequest mTouchFpsRequest;
    private int mTouchMaxFps;
    private boolean mTouchResumeEnable;
    private int mXScrollCount;
    private int mYScrollCount;
    private float mYdpi;
    private boolean mhasFirstSetFPS;

    /* renamed from: com.huawei.android.hwaps.EventAnalyzed.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ int val$action;
        final /* synthetic */ Context val$context;

        AnonymousClass1(int val$action, Context val$context) {
            this.val$action = val$action;
            this.val$context = val$context;
        }

        public void run() {
            if (this.val$action == 0) {
                ApsCommon.logD(EventAnalyzed.TAG, "APK: APS: 30Hz: ACTION_DOWN");
                this.val$context.sendBroadcast(new Intent(EventAnalyzed.APS_TOUCH_ACTION_DOWN));
            } else if (this.val$action == EventAnalyzed.TWO_DIMS_GAME) {
                ApsCommon.logD(EventAnalyzed.TAG, "APK: APS: 30Hz: ACTION_UP");
                this.val$context.sendBroadcast(new Intent(EventAnalyzed.APS_TOUCH_ACTION_UP));
            }
        }
    }

    /* renamed from: com.huawei.android.hwaps.EventAnalyzed.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ int val$mode;

        AnonymousClass2(int val$mode) {
            this.val$mode = val$mode;
        }

        public void run() {
            int fpsValue = EventAnalyzed.this.getLcdFpsScence();
            if (EventAnalyzed.DEFAULT_STUDY_BATTERY == fpsValue && EventAnalyzed.TWO_DIMS_GAME == this.val$mode) {
                EventAnalyzed.this.mContext.sendBroadcast(new Intent(EventAnalyzed.APS_LOW_FPS_GAME));
                EventAnalyzed.this.mIsLowFpsGame = true;
            } else if (EventAnalyzed.DEFAULT_MIN_FPS == fpsValue && this.val$mode == 0) {
                EventAnalyzed.this.mContext.sendBroadcast(new Intent(EventAnalyzed.APS_NOT_LOW_FPS_GAME));
                EventAnalyzed.this.mIsLowFpsGame = false;
            }
        }
    }

    private class QueryThread extends Thread {
        private QueryThread() {
        }

        public void run() {
            ApsCommon.logD(EventAnalyzed.TAG, "QueryThread run!!!!");
            EventAnalyzed.this.query();
            EventAnalyzed.this.setFirstEnterFPS(EventAnalyzed.this.mGameType);
            EventAnalyzed.this.setAdaptSdr(EventAnalyzed.this.mGameType, EventAnalyzed.UNINITED_DEFAULT_VALUE);
        }
    }

    private class ResumeFpsByTouch implements Runnable {
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

    private class StartSdr implements Runnable {
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
        private StartSdrForTest() {
        }

        public void run() {
            float ratio;
            int r = SystemProperties.getInt("sys.aps.pressratio", EventAnalyzed.REALTIME_GAME);
            if (r == 0) {
                ratio = (float) Math.random();
            } else {
                ratio = ((float) r) / 100.0f;
            }
            ApsCommon.logD(EventAnalyzed.TAG, "start SdrForTest----ratio = " + ratio);
            EventAnalyzed.this.mSdrController.setSdrRatio(ratio);
            EventAnalyzed.this.mSdrController.startSdr();
            int startTime = SystemProperties.getInt("sys.aps.starttime", EventAnalyzed.REALTIME_GAME);
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
        private StatisFps() {
        }

        public void run() {
            if (EventAnalyzed.this.mStatisFpsCount <= 50) {
                int currentFps = EventAnalyzed.this.getCurrentFPS();
                if (currentFps == 0) {
                    EventAnalyzed.this.mIsLowFps = true;
                }
                EventAnalyzed.this.mAverageFps = ((EventAnalyzed.this.mAverageFps * EventAnalyzed.this.mStatisFpsCount) + currentFps) / (EventAnalyzed.this.mStatisFpsCount + EventAnalyzed.TWO_DIMS_GAME);
                EventAnalyzed eventAnalyzed = EventAnalyzed.this;
                eventAnalyzed.mStatisFpsCount = eventAnalyzed.mStatisFpsCount + EventAnalyzed.TWO_DIMS_GAME;
                ApsCommon.logI(EventAnalyzed.TAG, "SDR: StatisFps : currentFps = " + currentFps + "; mIsLowFps = " + EventAnalyzed.this.mIsLowFps + "; mAverageFps = " + EventAnalyzed.this.mAverageFps + "; mStatisFpsCount = " + EventAnalyzed.this.mStatisFpsCount);
                EventAnalyzed.this.mHandler.postDelayed(EventAnalyzed.this.mStatisFps, 300);
            }
        }
    }

    private class StopEmergency implements Runnable {
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
        private StopSdrForTest() {
        }

        public void run() {
            EventAnalyzed.this.mSdrController.stopSdr();
            int stopTime = SystemProperties.getInt("sys.aps.stoptime", EventAnalyzed.REALTIME_GAME);
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
        private setTouchStateForNonplay() {
        }

        public void run() {
            if (EventAnalyzed.this.mGameState != null) {
                EventAnalyzed.this.mGameState.setTouchState(EventAnalyzed.ACTION_SETFPS_NONPLAY);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.EventAnalyzed.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.EventAnalyzed.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.EventAnalyzed.<clinit>():void");
    }

    public EventAnalyzed() {
        this.mLastUpTime = -1;
        this.mDownUpLongSum = 0;
        this.mDownUpShortSum = 0;
        this.mAvageDownUpLongTime = 0;
        this.mGameType = REALTIME_GAME;
        this.mMaxFps = SCROLL_DEFAULT_FPS;
        this.mMinFps = DEFAULT_MIN_FPS;
        this.mTouchMaxFps = REALTIME_GAME;
        this.mBatteryLimitValue = UNINITED_DEFAULT_VALUE;
        this.mBatteryLimitStudyValue = UNINITED_DEFAULT_VALUE;
        this.mMyPid = UNINITED_DEFAULT_VALUE;
        this.mConsecutiveShortDownUpCount = REALTIME_GAME;
        this.mMultiTouchCount = REALTIME_GAME;
        this.mShortDownUpCount = REALTIME_GAME;
        this.mLongDownUpCount = REALTIME_GAME;
        this.mScreenWidth = REALTIME_GAME;
        this.mLastGameType = ACTION_SETFPS_NONPLAY;
        this.mXScrollCount = REALTIME_GAME;
        this.mYScrollCount = REALTIME_GAME;
        this.mLongPressCount = REALTIME_GAME;
        this.mMaxDelta = REALTIME_GAME;
        this.mLevel = REALTIME_GAME;
        this.mIsAllScroll = false;
        this.mHasSetFPS = false;
        this.mhasFirstSetFPS = false;
        this.mHasIncreaseHighFPS = false;
        this.mHasIncreaseLowFPS = false;
        this.mIsFirstStartAnalyze = true;
        this.mHasReleaseAPS = false;
        this.mHasSetPid = false;
        this.mIsGameInfoExist = false;
        this.mHasOnPaused = false;
        this.mHasIdentifyProcess = false;
        this.mIsGameProcess = false;
        this.mMiddleDelta = REALTIME_GAME;
        this.mShortDelta = REALTIME_GAME;
        this.mSceenRegion = new int[PLR_LTR_GAME];
        this.mScreenWidthRegion = REALTIME_GAME;
        this.mScreenHeightRegion = REALTIME_GAME;
        this.mScreenOrientation = ACTION_SETFPS_NONPLAY;
        this.mClickCount = REALTIME_GAME;
        this.mSlowScrollCount = REALTIME_GAME;
        this.mIsSingleRegionAvailalbeGame = false;
        this.mIsFullScreenTouchGame = false;
        this.mIsScreenLandScape = false;
        this.mIsAnalyzedEnable = false;
        this.mIsControllAllFpsEnable = false;
        this.mIsEnterEmergencyFps = false;
        this.isXScroll = false;
        this.isYScroll = false;
        this.mApsThermal = null;
        this.mApsUserFeedback = null;
        this.mAdCheck = null;
        this.mStopEmergency = null;
        this.mResumeFpsByTouch = null;
        this.mHandler = null;
        this.mApsLdfGameIdentify = null;
        this.mApsLtrGameIdentify = null;
        this.mSetTouchStateForNonplay = null;
        this.mSdrController = null;
        this.mCanUseDebug = false;
        this.mSdrRatio = 1.0f;
        this.mStopSdrForTest = null;
        this.mStartSdrForTest = null;
        this.mStartSdr = null;
        this.mPerformanceDebug = false;
        this.mStatisFpsCount = REALTIME_GAME;
        this.mAverageFps = REALTIME_GAME;
        this.mIsLowFps = false;
        this.mStatisFps = null;
        this.mDesignatedFps = DEFAULT_STUDY_BATTERY;
        this.mDesignatedSdrRatio = 1.0f;
        this.mYdpi = 0.0f;
        this.mHashSceneFps = new HashMap();
        this.mHashSceneRatio = new HashMap();
        this.mHashCtrlBattery = new HashMap();
        this.mHashSceneFixed = new HashMap();
        this.mHashFixdFps = new HashMap();
        this.mHashFixdRatio = new HashMap();
        this.mIsSceneFixed = false;
        this.mIsLowFpsGame = false;
        this.mTouchResumeEnable = false;
        this.mStopTouchMaxFpsTime = REALTIME_GAME;
        this.mApsTest = null;
        if (AdCheck.isSupportAdCheck()) {
            this.mAdCheck = AdCheck.getInstance();
        }
    }

    public void initAPS(Context context, int screenWidth, int myPid) {
        boolean z;
        boolean z2 = false;
        Log.i(TAG, "APS: version is 3.3.11");
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
        this.mExactlyFpsRequest = new FpsRequest(SceneTypeE.EXACTLY_IDENTIFY);
        this.mTouchFpsRequest = new FpsRequest(SceneTypeE.TOUCH_EMERGENCY);
        queryGameInfo();
        this.mCanUseDebug = SystemProperties.getInt("sys.aps.test", REALTIME_GAME) > 0;
        if (SystemProperties.getInt("sys.aps.performance", REALTIME_GAME) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mPerformanceDebug = z;
        this.mStopTouchMaxFpsTime = SystemProperties.getInt("debug.aps.stoptouch.time", STOP_TOUCH_MAX_FPS_TIME);
        if (CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) {
            if (getCurrentBattery() > getCtrlBattery(this.mPkgName)) {
            }
            this.mYdpi = context.getResources().getDisplayMetrics().ydpi;
            this.mHandler = new Handler();
            if (SdrController.isSupportApsSdr()) {
                this.mSdrController = SdrController.getInstance();
                ApsCommon.logI(TAG, "SDR: initial: SdrController init,mCanUseDebug : " + this.mCanUseDebug);
            }
            this.mStopSdrForTest = new StopSdrForTest();
            this.mStartSdrForTest = new StartSdrForTest();
            this.mStartSdr = new StartSdr();
            this.mStatisFps = new StatisFps();
            this.mHandler.postDelayed(this.mStatisFps, 10000);
            this.mStopEmergency = new StopEmergency();
            this.mResumeFpsByTouch = new ResumeFpsByTouch();
            this.mSetTouchStateForNonplay = new setTouchStateForNonplay();
            this.mBatteryLimitValue = SystemProperties.getInt("debug.aps.battery_limit", DEFAULT_BATTERY);
            this.mBatteryLimitStudyValue = SystemProperties.getInt("debug.aps.battery_limit_study", DEFAULT_STUDY_BATTERY);
        }
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
            if (TWO_DIMS_GAME != this.mScreenOrientation) {
                z2 = true;
            }
            this.mIsScreenLandScape = z2;
            initEventAnalysis(screenWidth, screenHeight);
        }
        if (ApsThermal.isSupportAPSThermal()) {
            this.mApsThermal = ApsThermal.getInstance();
        }
        this.mYdpi = context.getResources().getDisplayMetrics().ydpi;
        this.mHandler = new Handler();
        if (SdrController.isSupportApsSdr()) {
            this.mSdrController = SdrController.getInstance();
            ApsCommon.logI(TAG, "SDR: initial: SdrController init,mCanUseDebug : " + this.mCanUseDebug);
        }
        this.mStopSdrForTest = new StopSdrForTest();
        this.mStartSdrForTest = new StartSdrForTest();
        this.mStartSdr = new StartSdr();
        this.mStatisFps = new StatisFps();
        this.mHandler.postDelayed(this.mStatisFps, 10000);
        this.mStopEmergency = new StopEmergency();
        this.mResumeFpsByTouch = new ResumeFpsByTouch();
        this.mSetTouchStateForNonplay = new setTouchStateForNonplay();
        this.mBatteryLimitValue = SystemProperties.getInt("debug.aps.battery_limit", DEFAULT_BATTERY);
        this.mBatteryLimitStudyValue = SystemProperties.getInt("debug.aps.battery_limit_study", DEFAULT_STUDY_BATTERY);
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
        int apsEnable = SystemProperties.getInt("debug.aps.enable", REALTIME_GAME);
        if (TWO_P_FIVE_DIMS_LOW_GAME == apsEnable) {
            this.mIsAnalyzedEnable = true;
        } else if (TWO_DIMS_GAME == apsEnable) {
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
        if (SystemProperties.getInt("debug.aps.sdr", REALTIME_GAME) > 0) {
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
        if (this.mHasIdentifyProcess && !this.mIsGameProcess) {
            return this.mIsGameProcess;
        }
        if (isAPSReady()) {
            return isIdentifyProcess(pkgName);
        }
        return false;
    }

    public void queryGameInfo() {
        new QueryThread().start();
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
            this.mGameType = REALTIME_GAME;
            this.mMaxFps = SCROLL_DEFAULT_FPS;
            this.mMinFps = DEFAULT_MIN_FPS;
        }
        if (this.mMaxFps > DEFAULT_STUDY_BATTERY && this.mMaxFps <= 120) {
            this.mTouchMaxFps = this.mMaxFps - 60;
        }
        if (this.mGameState != null) {
            this.mGameState.setTouchGameType(this.mGameType);
        }
        if (SCROLL_DEFAULT_FPS == this.mMinFps && SCROLL_DEFAULT_FPS == this.mMinNonplayFps) {
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
        int length = mIdentifyGameType.length;
        for (int i = REALTIME_GAME; i < length; i += TWO_DIMS_GAME) {
            queryByName(mIdentifyGameType[i]);
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
                if (fixedFps >= DEFAULT_STUDY_BATTERY) {
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
                int i = REALTIME_GAME;
                while (i < sceneFps.size()) {
                    if (!(this.mGameState == null || sceneFps.get(i) == null)) {
                        this.mGameState.setSceneFps(name, i + TWO_DIMS_GAME, ((Integer) sceneFps.get(i)).intValue());
                    }
                    i += TWO_DIMS_GAME;
                }
                i = REALTIME_GAME;
                while (i < sceneRatio.size()) {
                    if (!(this.mGameState == null || sceneRatio.get(i) == null)) {
                        this.mGameState.setSceneRatio(name, i + TWO_DIMS_GAME, (double) ((Float) sceneRatio.get(i)).floatValue());
                    }
                    i += TWO_DIMS_GAME;
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
            return REALTIME_GAME;
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
            return ((Integer) ((ArrayList) this.mHashSceneFps.get(name)).get(REALTIME_GAME)).intValue();
        }
        return REALTIME_GAME;
    }

    private float getSceneRatio(String name) {
        if (this.mHashSceneRatio.containsKey(name)) {
            return ((Float) ((ArrayList) this.mHashSceneRatio.get(name)).get(REALTIME_GAME)).floatValue();
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
        return REALTIME_GAME;
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
        if (this.mIsControllAllFpsEnable || (this.mIsAnalyzedEnable && (this.mGameType == PLR_LDF_GAME || this.mGameType == PLR_LTR_GAME || this.mGameType == NOT_DRAW_WITHOUT_DRAG || this.mGameType == RUN_GAME))) {
            if (SCROLL_DEFAULT_FPS == fps) {
                if (!this.mHasIncreaseHighFPS) {
                    this.mHasIncreaseHighFPS = increaseEmergencyFPS(fps);
                }
            } else if (!(this.mHasIncreaseLowFPS || this.mHasIncreaseHighFPS)) {
                this.mHasIncreaseLowFPS = increaseEmergencyFPS(fps);
            }
            if (this.mIsEnterEmergencyFps) {
                this.mIsEnterEmergencyFps = false;
                this.mHandler.removeCallbacks(this.mStopEmergency);
                this.mHandler.postDelayed(this.mStopEmergency, 30000);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setGamePid(int pid) {
        if (!(this.mGameState == null || UNINITED_DEFAULT_VALUE == pid || this.mHasSetPid)) {
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
            if (this.mDownUpInterval >= LONG_PRESS_TIME) {
                this.mDownUpLongSum += this.mDownUpInterval;
                this.mConsecutiveShortDownUpCount = REALTIME_GAME;
                this.mLongDownUpCount += TWO_DIMS_GAME;
                return;
            }
            this.mDownUpShortSum += this.mDownUpInterval;
            this.mShortDownUpCount += TWO_DIMS_GAME;
            this.mConsecutiveShortDownUpCount += TWO_DIMS_GAME;
            if (this.mConsecutiveShortDownUpCount > OTHER_FULL_SCREEN_GAME) {
                if (this.mCanUseDebug && this.mSdrController != null && this.mSdrController.IsSdrCase()) {
                    int pressure = SystemProperties.getInt("sys.aps.pressure", REALTIME_GAME);
                    if (this.mAverageFps < NOT_DRAW_WITHOUT_DRAG || this.mIsLowFps || this.mStatisFpsCount < 50) {
                        Log.e(TAG, "APS: SDR: the fps is too small and mAverageFps = " + this.mAverageFps + "; mIsLowFps = " + this.mIsLowFps);
                    } else if (pressure == 0) {
                        int r = SystemProperties.getInt("sys.aps.ratio", REALTIME_GAME);
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
                increaseFPS(OTHERS_DEFAULT_FPS);
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
            increaseFPS(SCROLL_DEFAULT_FPS);
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
            this.mSlowScrollCount += TWO_DIMS_GAME;
        }
        if (this.mCurrentDownTime != downTime) {
            UpAndDownDeltX = REALTIME_GAME;
            UpAndDownDeltY = REALTIME_GAME;
            ApsCommon.logD(TAG, "MultiTouch for up Action");
        }
        analyzeTouchDistance(UpAndDownDeltX, UpAndDownDeltY);
        if ((this.mXScrollCount < TWO_P_FIVE_DIMS_HIGH_GAME || this.mYScrollCount <= 0) && ((this.mYScrollCount < TWO_P_FIVE_DIMS_HIGH_GAME || this.mXScrollCount <= 0) && this.mXScrollCount < THREE_DIMS_GAME)) {
            if (this.mYScrollCount >= THREE_DIMS_GAME) {
            }
            if (this.mDownMoveInterval >= LONG_PRESS_TIME && !this.isXScroll && !this.isYScroll) {
                this.mLongPressCount += TWO_DIMS_GAME;
                if (this.mLongPressCount > TWO_P_FIVE_DIMS_HIGH_GAME) {
                    increaseFPS(OTHERS_DEFAULT_FPS);
                    return;
                }
                return;
            }
            return;
        }
        this.mXScrollCount = REALTIME_GAME;
        this.mYScrollCount = REALTIME_GAME;
        increaseFPS(SCROLL_DEFAULT_FPS);
        this.mIsAllScroll = true;
        if (this.mDownMoveInterval >= LONG_PRESS_TIME) {
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
            case REALTIME_GAME /*0*/:
                analyzeDownAction(x, y, downTime);
            case TWO_DIMS_GAME /*1*/:
                analyzeUpAction(eventTime, downTime, x, y);
            case TWO_P_FIVE_DIMS_LOW_GAME /*2*/:
                analyzeMoveAction(eventTime, downTime);
            case TWO_P_FIVE_DIMS_HIGH_GAME /*3*/:
            default:
                if (pointCount > TWO_DIMS_GAME) {
                    this.mMultiTouchCount += TWO_DIMS_GAME;
                    if (this.mMultiTouchCount > TWO_P_FIVE_DIMS_LOW_GAME) {
                        increaseFPS(OTHERS_DEFAULT_FPS);
                    }
                }
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
        if (this.mGameState == null || this.mGameState.isDepthGame() || this.mApsLtrGameIdentify == null) {
            return false;
        }
        return this.mApsLtrGameIdentify.isLtrGame(apsPkgName);
    }

    private boolean isLdfGame() {
        if (this.mGameState == null || this.mGameState.isDepthGame() || this.mApsLdfGameIdentify == null || this.mIsAllScroll) {
            return false;
        }
        return this.mApsLdfGameIdentify.isLdfGame();
    }

    private int analyzeGameType(int shortAndLongTouchProportion, int totalTouchCount) {
        if (this.mIsLongMove) {
            ApsCommon.logD(TAG, "is RealTime Game");
            return REALTIME_GAME;
        } else if (isLtrGame()) {
            ApsCommon.logD(TAG, "is PLR_LTR_GAME Game");
            return PLR_LTR_GAME;
        } else if (!this.mIsAllScroll || this.mMultiTouchCount >= TWO_P_FIVE_DIMS_HIGH_GAME) {
            if (isLdfGame()) {
                ApsCommon.logD(TAG, "is PLR_LDF_GAME Game ");
                return PLR_LDF_GAME;
            } else if (!this.mIsAllScroll && this.mAvageDownUpLongTime > STRATEGY_TIME && this.mMultiTouchCount < TWO_P_FIVE_DIMS_LOW_GAME && shortAndLongTouchProportion < OTHER_FULL_SCREEN_GAME && this.mLongPressCount < TWO_P_FIVE_DIMS_HIGH_GAME && totalTouchCount > OTHER_FULL_SCREEN_GAME && this.mConsecutiveShortDownUpCount <= SCROLL_STATIC_GAME) {
                ApsCommon.logD(TAG, "is Strategy Game");
                return TWO_DIMS_GAME;
            } else if (this.mIsSingleRegionAvailalbeGame) {
                ApsCommon.logD(TAG, "is SingleRegionAvailalbeGame");
                return THREE_DIMS_GAME;
            } else if (this.mIsFullScreenTouchGame) {
                ApsCommon.logD(TAG, "is FullScreenTouchGame");
                return THREE_DIMS_GAME;
            } else {
                ApsCommon.logD(TAG, "is Other Game");
                return TWO_P_FIVE_DIMS_HIGH_GAME;
            }
        } else if (this.mClickCount < TWO_P_FIVE_DIMS_HIGH_GAME && !this.mIsScreenLandScape && this.mSlowScrollCount < TWO_P_FIVE_DIMS_HIGH_GAME) {
            ApsCommon.logD(TAG, "is Scroll Static Game ");
            return SCROLL_STATIC_GAME;
        } else if (this.mIsScreenLandScape) {
            ApsCommon.logD(TAG, "is LandScape Scroll Game ");
            return LANDSCAPE_SCROLL_GAME;
        } else {
            ApsCommon.logD(TAG, "is Scroll Game ");
            return TWO_P_FIVE_DIMS_LOW_GAME;
        }
    }

    public int collectData(long currentTime) {
        int shortAndLongTouchProportion = UNINITED_DEFAULT_VALUE;
        computeAvailableRegion();
        if (0 == this.mBeginAnalyzeTime) {
            resetAnalyzePara();
            return UNINITED_DEFAULT_VALUE;
        }
        if (TWO_P_FIVE_DIMS_LOW_GAME == this.mAction) {
            analyzeMoveAction(currentTime, this.mCurrentDownTime);
        }
        if (this.mLongDownUpCount != 0) {
            this.mAvageDownUpLongTime = this.mDownUpLongSum / ((long) this.mLongDownUpCount);
            shortAndLongTouchProportion = this.mShortDownUpCount / this.mLongDownUpCount;
        }
        this.mMultiTouchCount /= TWO_P_FIVE_DIMS_LOW_GAME;
        int totalTouchCount = this.mLongDownUpCount + this.mShortDownUpCount;
        ApsCommon.logD(TAG, "mIsLongMove = " + this.mIsLongMove + " , mIsAllScroll : " + this.mIsAllScroll + " , mAvageDownUpLongTime = " + this.mAvageDownUpLongTime + " , mMultiTouchCount = " + this.mMultiTouchCount + " , shortAndLongTouchProportion = " + shortAndLongTouchProportion + " , mLongPressCount  = " + this.mLongPressCount + " , totalTouchCount = " + totalTouchCount + " , mClickCount : " + this.mClickCount + " , mSlowScrollCount : " + this.mSlowScrollCount);
        int type = analyzeGameType(shortAndLongTouchProportion, totalTouchCount);
        this.mGameType = type;
        resetAnalyzePara();
        return type;
    }

    private void setFirstEnterFPS(int gameType) {
        if (!this.mhasFirstSetFPS) {
            int fps = DEFAULT_STUDY_BATTERY;
            if (CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) {
                if (getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                    fps = this.mIsSceneFixed ? this.mDesignatedFps : ((Integer) ((ArrayList) this.mHashSceneFps.get(this.mPkgName)).get(REALTIME_GAME)).intValue();
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
        if (!mStopSetFps || PLR_LDF_GAME == this.mGameType || PLR_LTR_GAME == this.mGameType || NOT_DRAW_WITHOUT_DRAG == this.mGameType || RUN_GAME == this.mGameType) {
            int i;
            if (this.mExactlyFpsRequest == null || !(gameType == PLR_LDF_GAME || gameType == PLR_LTR_GAME || ((CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) && getCurrentBattery() <= getCtrlBattery(this.mPkgName)))) {
                if (!(this.mFpsRequest == null || this.mPerformanceDebug)) {
                    this.mFpsRequest.start(fps);
                }
            } else if (!this.mPerformanceDebug) {
                this.mExactlyFpsRequest.start(fps);
            }
            if (fps <= DEFAULT_MIN_FPS) {
                i = TWO_DIMS_GAME;
            } else {
                i = REALTIME_GAME;
            }
            sendBroadcastBaseOnGame(i);
        }
    }

    public void setAdaptFPS(int gameType, int openGLType, int level) {
        if (UNINITED_DEFAULT_VALUE != gameType) {
            ApsCommon.logD(TAG, "mGameType = " + gameType + " , mLastGameType = " + this.mLastGameType);
            if (this.mLastGameType != gameType) {
                this.mLastGameType = gameType;
                this.mHasSetFPS = false;
            } else if (!this.mHasSetFPS) {
                this.mGameType = gameType;
                int recommendFPS = computeRecommendFPS(gameType, openGLType, level);
                if (gameType == PLR_LDF_GAME || gameType == PLR_LTR_GAME) {
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
            return DEFAULT_STUDY_BATTERY;
        }
        int recommendFPS = DEFAULT_STUDY_BATTERY;
        switch (gameType) {
            case REALTIME_GAME /*0*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = OTHERS_DEFAULT_FPS;
                    break;
                }
                break;
            case TWO_DIMS_GAME /*1*/:
                recommendFPS = DEFAULT_MIN_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_BATTERY;
                    break;
                }
                break;
            case TWO_P_FIVE_DIMS_LOW_GAME /*2*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
            case TWO_P_FIVE_DIMS_HIGH_GAME /*3*/:
                recommendFPS = OTHERS_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
            case THREE_DIMS_GAME /*4*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = 50;
                    break;
                }
                break;
            case OTHER_FULL_SCREEN_GAME /*5*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = OTHERS_DEFAULT_FPS;
                    break;
                }
                break;
            case LANDSCAPE_SCROLL_GAME /*6*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
            case SCROLL_STATIC_GAME /*7*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
            case PLR_LDF_GAME /*8*/:
                recommendFPS = DEFAULT_BATTERY;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_BATTERY;
                    break;
                }
                break;
            case PLR_LTR_GAME /*9*/:
                recommendFPS = DEFAULT_MIN_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
            case NOT_DRAW_WITHOUT_DRAG /*10*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
            case RUN_GAME /*11*/:
                recommendFPS = SCROLL_DEFAULT_FPS;
                if (TWO_DIMS_GAME == level) {
                    recommendFPS = DEFAULT_MIN_FPS;
                    break;
                }
                break;
        }
        if (PLR_LDF_GAME != gameType && recommendFPS < this.mMinFps) {
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
        this.mMultiTouchCount = REALTIME_GAME;
        this.mXScrollCount = REALTIME_GAME;
        this.mYScrollCount = REALTIME_GAME;
        this.mLongPressCount = REALTIME_GAME;
        this.mIsAllScroll = false;
        this.mIsFirstStartAnalyze = true;
        this.mBeginAnalyzeTime = 0;
        this.mLastUpTime = -1;
        this.mIsLongMove = false;
        this.mShortDownUpCount = REALTIME_GAME;
        this.mLongDownUpCount = REALTIME_GAME;
        this.mDownUpLongSum = 0;
        this.mDownUpShortSum = 0;
        this.mHasIncreaseHighFPS = false;
        this.mHasIncreaseLowFPS = false;
        this.mConsecutiveShortDownUpCount = REALTIME_GAME;
        this.mIsEnterEmergencyFps = false;
        this.mIsSingleRegionAvailalbeGame = false;
        this.mIsFullScreenTouchGame = false;
        resetScreenRegion();
        this.mClickCount = REALTIME_GAME;
        this.mSlowScrollCount = REALTIME_GAME;
        ApsCommon.logD(TAG, "resetAnalyzePara");
        if (!this.mCanUseDebug && CUST_GAME_TYPE == this.mGameType && getCurrentBattery() <= getCtrlBattery(this.mPkgName) && this.mSdrController != null && this.mSdrController.IsSdrCase()) {
            if (this.mIsSceneFixed) {
                this.mSdrController.setSdrRatio(this.mDesignatedSdrRatio);
            } else {
                this.mSdrController.setSdrRatio(((Float) ((ArrayList) this.mHashSceneRatio.get(this.mPkgName)).get(REALTIME_GAME)).floatValue());
            }
            this.mSdrController.startSdr();
        }
        if (this.mIsLowFpsGame) {
            i = TWO_DIMS_GAME;
        } else {
            i = REALTIME_GAME;
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
        return SystemProperties.getInt("debug.aps.lcd_fps_scence", DEFAULT_STUDY_BATTERY);
    }

    private void sendBroadcastBaseOnAction(Context context, int action) {
        new Thread(new AnonymousClass1(action, context)).start();
    }

    private void sendBroadcastBaseOnGame(int mode) {
        new Thread(new AnonymousClass2(mode)).start();
    }

    public void processAnalyze(Context context, int action, long eventTime, int x, int y, int pointCount, long downTime) {
        if (action == 0 || action == TWO_DIMS_GAME) {
            setTouchState(action);
        }
        if (9000 == SystemProperties.getInt("debug.aps.enable", REALTIME_GAME)) {
            sendBroadcastBaseOnAction(context, action);
        } else if (ApsTest.isSupportAPSTset()) {
            if (this.mApsTest == null) {
                this.mApsTest = new ApsTest();
                this.mApsTest.start();
            }
        } else if (this.mIsGameProcess) {
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
            if (action != 0 || this.mTouchResumeEnable) {
                if (action == TWO_DIMS_GAME && this.mTouchResumeEnable) {
                    this.mHandler.removeCallbacks(this.mResumeFpsByTouch);
                    this.mHandler.postDelayed(this.mResumeFpsByTouch, (long) this.mStopTouchMaxFpsTime);
                }
            } else if (this.mTouchMaxFps != 0) {
                this.mTouchFpsRequest.start(this.mTouchMaxFps);
                this.mTouchResumeEnable = true;
            }
            if (CUST_GAME_TYPE == this.mGameType) {
                if (getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                    if (!this.mCanUseDebug && this.mSdrController != null && this.mSdrController.IsSdrCase() && action == 0) {
                        if (this.mIsSceneFixed) {
                            this.mSdrController.setSdrRatio(this.mDesignatedSdrRatio);
                        } else {
                            this.mSdrController.setSdrRatio(((Float) ((ArrayList) this.mHashSceneRatio.get(this.mPkgName)).get(REALTIME_GAME)).floatValue());
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
                int openGLType = REALTIME_GAME;
                if (this.mGameState != null) {
                    openGLType = this.mGameState.getOpenglGameType();
                }
                if (openGLType == NOT_DRAW_WITHOUT_DRAG || (openGLType == RUN_GAME && !this.mIsScreenLandScape)) {
                    gametype = openGLType;
                }
                setTouchGameType(gametype);
                isCertainGameType(gametype, openGLType);
                setAdaptFPS(gametype, openGLType, this.mLevel);
                setAdaptSdr(gametype, openGLType);
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
        return SCROLL_DEFAULT_FPS;
    }

    private void initEventAnalysis(int screenWidth, int screenHeight) {
        this.mScreenWidth = screenWidth;
        this.mScreenWidthRegion = screenWidth / TWO_P_FIVE_DIMS_HIGH_GAME;
        this.mScreenHeightRegion = screenHeight / TWO_P_FIVE_DIMS_HIGH_GAME;
        this.mMaxDelta = (int) (((float) this.mScreenWidth) * MAX_DALTA_RATE);
        this.mMiddleDelta = (int) (((float) this.mScreenWidth) * MIDDLE_DALTA_RATE);
        this.mShortDelta = (int) (((float) this.mScreenWidth) * SHORT_DALTA_RATE);
        setParaForGameIdentify(screenWidth, screenHeight);
        if (this.mMaxDelta == 0) {
            this.mMaxDelta = DEFAULT_MAX_DALTA;
        }
        if (this.mMiddleDelta == 0) {
            this.mMiddleDelta = DEFAULT_MIDDLE_DALTA;
        }
        if (this.mShortDelta == 0) {
            this.mShortDelta = DEFAULT_SHORT_DALTA;
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
            if (xIndex > TWO_P_FIVE_DIMS_LOW_GAME) {
                xIndex = TWO_P_FIVE_DIMS_LOW_GAME;
            }
            if (yIndex > TWO_P_FIVE_DIMS_LOW_GAME) {
                yIndex = TWO_P_FIVE_DIMS_LOW_GAME;
            }
            int screenRegionIndex = xIndex + (yIndex * TWO_P_FIVE_DIMS_HIGH_GAME);
            if (screenRegionIndex < this.mSceenRegion.length && screenRegionIndex >= 0) {
                int[] iArr = this.mSceenRegion;
                iArr[screenRegionIndex] = iArr[screenRegionIndex] + TWO_DIMS_GAME;
            }
        }
    }

    private void computeAvailableRegion() {
        int maxPointcount = REALTIME_GAME;
        int sceondPointCount = REALTIME_GAME;
        int usedRegionCount = REALTIME_GAME;
        for (int i = REALTIME_GAME; i < this.mSceenRegion.length; i += TWO_DIMS_GAME) {
            if (this.mSceenRegion[i] > maxPointcount) {
                sceondPointCount = maxPointcount;
                maxPointcount = this.mSceenRegion[i];
            } else if (this.mSceenRegion[i] > sceondPointCount) {
                sceondPointCount = this.mSceenRegion[i];
            }
            if (this.mSceenRegion[i] > TWO_DIMS_GAME) {
                usedRegionCount += TWO_DIMS_GAME;
            }
        }
        if (sceondPointCount != 0 && maxPointcount / sceondPointCount > TWO_P_FIVE_DIMS_LOW_GAME) {
            this.mIsSingleRegionAvailalbeGame = true;
        }
        if (usedRegionCount > THREE_DIMS_GAME) {
            this.mIsFullScreenTouchGame = true;
        }
    }

    private void resetScreenRegion() {
        for (int i = REALTIME_GAME; i < this.mSceenRegion.length; i += TWO_DIMS_GAME) {
            this.mSceenRegion[i] = REALTIME_GAME;
        }
    }

    private boolean isSupportEventAnalysis() {
        if (TWO_DIMS_GAME == (SystemProperties.getInt("sys.aps.support", REALTIME_GAME) & TWO_DIMS_GAME)) {
            return true;
        }
        return false;
    }

    private void setTouchState(int state) {
        if (this.mGameState == null) {
            this.mGameState = new GameState();
        }
        this.mGameState.setTouchState(state);
        if (this.mIsGameProcess && TWO_DIMS_GAME == state) {
            this.mHandler.removeCallbacks(this.mSetTouchStateForNonplay);
            this.mHandler.postDelayed(this.mSetTouchStateForNonplay, 2000);
        }
    }

    private void setTouchGameType(int type) {
        if (this.mGameState != null) {
            if (type == NOT_DRAW_WITHOUT_DRAG) {
                type = SCROLL_STATIC_GAME;
            }
            this.mGameState.setTouchGameType(type);
        }
    }

    private void setEmergencyFPS(int fps) {
        if (!mStopSetFps || PLR_LDF_GAME == this.mGameType || PLR_LTR_GAME == this.mGameType || NOT_DRAW_WITHOUT_DRAG == this.mGameType || RUN_GAME == this.mGameType) {
            if (fps < this.mMinFps) {
                fps = this.mMinFps;
            }
            if ((CUST_GAME_TYPE == this.mGameType || CUST_APP_TYPE == this.mGameType) && getCurrentBattery() <= getCtrlBattery(this.mPkgName)) {
                if (this.mIsSceneFixed) {
                    fps = this.mDesignatedFps;
                } else {
                    fps = ((Integer) ((ArrayList) this.mHashSceneFps.get(this.mPkgName)).get(TWO_P_FIVE_DIMS_LOW_GAME)).intValue();
                    if (fps == DEFAULT_STUDY_BATTERY) {
                        fps = SCROLL_DEFAULT_FPS;
                    }
                }
            }
            if (!(this.mEmergencyFpsRequest == null || this.mPerformanceDebug)) {
                int i;
                this.mEmergencyFpsRequest.start(fps);
                if (fps <= DEFAULT_MIN_FPS) {
                    i = TWO_DIMS_GAME;
                } else {
                    i = REALTIME_GAME;
                }
                sendBroadcastBaseOnGame(i);
            }
        }
    }

    private boolean isCertainGameType(int gameType, int openGLType) {
        ApsCommon.logD(TAG, "isCertainGameType  gameType : " + gameType + ", openGLType : " + openGLType);
        if (TWO_P_FIVE_DIMS_LOW_GAME != gameType && gameType != 0 && (TWO_P_FIVE_DIMS_HIGH_GAME != gameType || (TWO_DIMS_GAME != openGLType && TWO_P_FIVE_DIMS_LOW_GAME != openGLType))) {
            return false;
        }
        ApsCommon.logD(TAG, "this game is CertainGame : ");
        return true;
    }

    private void analyzeTouchDistance(int xDistance, int yDistance) {
        if (xDistance > this.mMaxDelta) {
            this.mXScrollCount += TWO_DIMS_GAME;
            this.isXScroll = true;
        }
        if (yDistance > this.mMaxDelta) {
            this.mYScrollCount += TWO_DIMS_GAME;
            this.isYScroll = true;
        }
        if (xDistance < this.mShortDelta && yDistance < this.mShortDelta) {
            this.mClickCount += TWO_DIMS_GAME;
        }
    }

    private int getTargetFPS() {
        if (this.mFpsRequest != null) {
            return this.mFpsRequest.getTargetFPS();
        }
        return SCROLL_DEFAULT_FPS;
    }

    private boolean increaseEmergencyFPS(int increaseFps) {
        int targetFps = getTargetFPS();
        if (UNINITED_DEFAULT_VALUE == targetFps) {
            ApsCommon.logD(TAG, "target FPS is default, do not increase FPS!");
            return false;
        } else if (increaseFps - targetFps < TWO_P_FIVE_DIMS_HIGH_GAME) {
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
            return UNINITED_DEFAULT_VALUE;
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
            case PLR_LTR_GAME /*9*/:
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
        if ((UNINITED_DEFAULT_VALUE == gameType && UNINITED_DEFAULT_VALUE != openGLType) || this.mSdrController == null) {
            return;
        }
        if (!this.mCanUseDebug && CUST_GAME_TYPE == this.mGameType && getCurrentBattery() <= getCtrlBattery(this.mPkgName) && this.mSdrController.IsSdrCase()) {
            if (this.mIsSceneFixed) {
                this.mSdrController.setSdrRatio(this.mDesignatedSdrRatio);
            } else {
                this.mSdrController.setSdrRatio(((Float) ((ArrayList) this.mHashSceneRatio.get(this.mPkgName)).get(REALTIME_GAME)).floatValue());
            }
            this.mSdrController.startSdr();
        } else if (CUST_APP_TYPE != this.mGameType || getCurrentBattery() > getCtrlBattery(this.mPkgName)) {
            if (gameType == SCROLL_STATIC_GAME && openGLType == NOT_DRAW_WITHOUT_DRAG) {
                gameType = NOT_DRAW_WITHOUT_DRAG;
            }
            if ((gameType == NOT_DRAW_WITHOUT_DRAG || gameType == PLR_LDF_GAME || gameType == PLR_LTR_GAME) && this.mIsAnalyzedEnable && !this.mCanUseDebug && this.mAverageFps >= NOT_DRAW_WITHOUT_DRAG && !this.mIsLowFps && this.mStatisFpsCount >= 50) {
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
        setAdaptSdr(this.mGameType, UNINITED_DEFAULT_VALUE);
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
        if (maxDimRatio == UNINITED_DEFAULT_VALUE || minBrightDuration == UNINITED_DEFAULT_VALUE || screenOffTimeout <= minBrightDuration) {
            return UNINITED_DEFAULT_VALUE;
        }
        return (screenOffTimeout * maxDimRatio) / 100;
    }
}
