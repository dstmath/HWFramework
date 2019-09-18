package com.android.server.display;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.display.BackLightCommonData;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.plug.PGSdk;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DisplayEffectMonitor implements HwServiceFactory.IDisplayEffectMonitor {
    private static final String ACTION_MONITOR_TIMER = "com.android.server.display.action.MONITOR_TIMER";
    private static final long HOUR = 3600000;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final long MINUTE = 60000;
    private static final String TAG = "DisplayEffectMonitor";
    private static final String TYPE_BOOT_COMPLETED = "bootCompleted";
    private static volatile DisplayEffectMonitor mMonitor;
    private static final Object mMonitorLock = new Object();
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public final BackLightMonitorManager mBackLightMonitorManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(DisplayEffectMonitor.TAG, "onReceive() intent is NULL!");
                return;
            }
            if (DisplayEffectMonitor.ACTION_MONITOR_TIMER.equals(intent.getAction())) {
                if (DisplayEffectMonitor.HWFLOW) {
                    Slog.i(DisplayEffectMonitor.TAG, "hour task time up");
                }
                DisplayEffectMonitor.this.mBackLightMonitorManager.triggerUploadTimer();
                DisplayEffectMonitor.this.mSceneRecognition.tryInit();
            }
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public final HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public final SceneRecognition mSceneRecognition;

    public interface MonitorModule {
        public static final String PARAM_TYPE = "paramType";

        boolean isParamOwner(String str);

        void sendMonitorParam(ArrayMap<String, Object> arrayMap);

        void triggerUploadTimer();
    }

    public class ParamLogPrinter {
        private static final int LOG_PRINTER_MSG = 1;
        private static final int mMessageDelayInMs = 2000;
        /* access modifiers changed from: private */
        public boolean mFirstTime = true;
        /* access modifiers changed from: private */
        public long mLastTime;
        /* access modifiers changed from: private */
        public int mLastValue;
        /* access modifiers changed from: private */
        public final Object mLock = new Object();
        /* access modifiers changed from: private */
        public String mPackageName;
        /* access modifiers changed from: private */
        public boolean mParamDecrease;
        /* access modifiers changed from: private */
        public boolean mParamIncrease;
        private final String mParamName;
        /* access modifiers changed from: private */
        public long mStartTime;
        /* access modifiers changed from: private */
        public int mStartValue;
        private final String mTAG;
        private final SimpleDateFormat mTimeFormater = new SimpleDateFormat("HH:mm:ss.SSS");
        private final Handler mTimeHandler = new Handler(DisplayEffectMonitor.this.mHandlerThread.getLooper()) {
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x0070, code lost:
                return;
             */
            public void handleMessage(Message msg) {
                synchronized (ParamLogPrinter.this.mLock) {
                    if (!ParamLogPrinter.this.mFirstTime) {
                        if (ParamLogPrinter.this.mLastValue == msg.arg1) {
                            if (ParamLogPrinter.this.mParamIncrease || ParamLogPrinter.this.mParamDecrease) {
                                ParamLogPrinter.this.printValueChange(ParamLogPrinter.this.mStartValue, ParamLogPrinter.this.mStartTime, ParamLogPrinter.this.mLastValue, ParamLogPrinter.this.mLastTime, ParamLogPrinter.this.mPackageName);
                            } else {
                                ParamLogPrinter.this.printSingleValue(ParamLogPrinter.this.mLastValue, ParamLogPrinter.this.mLastTime, ParamLogPrinter.this.mPackageName);
                            }
                            removeMessages(1);
                            boolean unused = ParamLogPrinter.this.mFirstTime = true;
                        }
                    }
                }
            }
        };

        public ParamLogPrinter(String paramName, String tag) {
            this.mParamName = paramName;
            this.mTAG = tag;
        }

        public void updateParam(int param, String packageName) {
            int i = param;
            synchronized (this.mLock) {
                try {
                    if (this.mFirstTime) {
                        this.mStartValue = i;
                        this.mLastValue = this.mStartValue;
                        this.mStartTime = System.currentTimeMillis();
                        this.mLastTime = this.mStartTime;
                        this.mPackageName = packageName;
                        this.mParamIncrease = false;
                        this.mParamDecrease = false;
                        this.mTimeHandler.removeMessages(1);
                        this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(1, i, 0), 2000);
                        this.mFirstTime = false;
                        return;
                    }
                    String str = packageName;
                    if (this.mLastValue != i) {
                        if ((!this.mParamIncrease || i >= this.mLastValue) && (!this.mParamDecrease || i <= this.mLastValue)) {
                            if (!this.mParamIncrease && !this.mParamDecrease) {
                                this.mParamIncrease = i > this.mLastValue;
                                this.mParamDecrease = i < this.mLastValue;
                            }
                            this.mLastValue = i;
                            this.mLastTime = System.currentTimeMillis();
                            this.mTimeHandler.removeMessages(1);
                            this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(1, i, 0), 2000);
                            return;
                        }
                        printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
                        this.mStartValue = this.mLastValue;
                        this.mLastValue = i;
                        this.mStartTime = this.mLastTime;
                        this.mLastTime = System.currentTimeMillis();
                        this.mParamIncrease = this.mLastValue > this.mStartValue;
                        this.mParamDecrease = this.mLastValue < this.mStartValue;
                        this.mTimeHandler.removeMessages(1);
                        this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(1, i, 0), 2000);
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }

        public void resetParam(int param, String packageName) {
            synchronized (this.mLock) {
                if (this.mFirstTime) {
                    if (this.mLastValue == param) {
                        return;
                    }
                } else if (this.mParamIncrease || this.mParamDecrease) {
                    printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
                } else {
                    printSingleValue(this.mLastValue, this.mLastTime, this.mPackageName);
                }
                printResetValue(param, packageName);
                this.mLastValue = param;
                this.mLastTime = System.currentTimeMillis();
                this.mTimeHandler.removeMessages(1);
                this.mFirstTime = true;
            }
        }

        public void changeName(int param, String packageName) {
            synchronized (this.mLock) {
                if (!this.mFirstTime) {
                    if (this.mParamIncrease || this.mParamDecrease) {
                        printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
                    } else {
                        printSingleValue(this.mLastValue, this.mLastTime, this.mPackageName);
                    }
                }
                printNameChange(this.mLastValue, this.mPackageName, param, packageName);
                this.mStartValue = param;
                this.mLastValue = this.mStartValue;
                this.mStartTime = System.currentTimeMillis();
                this.mLastTime = this.mStartTime;
                this.mPackageName = packageName;
                this.mParamIncrease = false;
                this.mParamDecrease = false;
                this.mTimeHandler.removeMessages(1);
                this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(1, param, 0), 2000);
                this.mFirstTime = false;
            }
        }

        /* access modifiers changed from: private */
        public void printSingleValue(int value, long time, String packageName) {
            String str;
            if (DisplayEffectMonitor.HWFLOW) {
                String str2 = this.mTAG;
                StringBuilder sb = new StringBuilder();
                sb.append(this.mParamName);
                sb.append(" ");
                sb.append(value);
                sb.append(" @");
                sb.append(this.mTimeFormater.format(Long.valueOf(time)));
                if (packageName != null) {
                    str = " by " + packageName;
                } else {
                    str = "";
                }
                sb.append(str);
                Slog.i(str2, sb.toString());
            }
        }

        /* access modifiers changed from: private */
        public void printValueChange(int startValue, long startTime, int endValue, long endTime, String packageName) {
            String str;
            if (DisplayEffectMonitor.HWFLOW) {
                String str2 = this.mTAG;
                StringBuilder sb = new StringBuilder();
                sb.append(this.mParamName);
                sb.append(" ");
                sb.append(startValue);
                sb.append(" -> ");
                sb.append(endValue);
                sb.append(" @");
                sb.append(this.mTimeFormater.format(Long.valueOf(endTime)));
                sb.append(" ");
                sb.append(endTime - startTime);
                sb.append("ms");
                if (packageName != null) {
                    str = " by " + packageName;
                } else {
                    str = "";
                }
                sb.append(str);
                Slog.i(str2, sb.toString());
            }
        }

        private void printResetValue(int value, String packageName) {
            String str;
            if (DisplayEffectMonitor.HWFLOW) {
                String str2 = this.mTAG;
                StringBuilder sb = new StringBuilder();
                sb.append(this.mParamName);
                sb.append(" reset ");
                sb.append(value);
                if (packageName != null) {
                    str = " by " + packageName;
                } else {
                    str = "";
                }
                sb.append(str);
                Slog.i(str2, sb.toString());
            }
        }

        private void printNameChange(int value1, String packageName1, int value2, String packageName2) {
            String str;
            String str2;
            if (DisplayEffectMonitor.HWFLOW) {
                String str3 = this.mTAG;
                StringBuilder sb = new StringBuilder();
                sb.append(this.mParamName);
                sb.append(" ");
                sb.append(value1);
                if (packageName1 != null) {
                    str = " by " + packageName1;
                } else {
                    str = "";
                }
                sb.append(str);
                sb.append(" -> ");
                sb.append(value2);
                if (packageName2 != null) {
                    str2 = " by " + packageName2;
                } else {
                    str2 = "";
                }
                sb.append(str2);
                Slog.i(str3, sb.toString());
            }
        }
    }

    private class SceneRecognition {
        private static final int MAX_TRY_INIT_TIMES = 5;
        private PGSdk.Sink mListener;
        private PGSdk mPGSdk;
        private BackLightCommonData.Scene mScene;
        private boolean needTryInitAgain;
        private int tryInitTimes;

        private SceneRecognition() {
        }

        /* access modifiers changed from: private */
        public void init() {
            this.mPGSdk = PGSdk.getInstance();
            if (this.mPGSdk == null) {
                Slog.e(DisplayEffectMonitor.TAG, "SceneRecognition init() PGSdk.getInstance() failed!");
                this.needTryInitAgain = true;
                return;
            }
            if (DisplayEffectMonitor.HWFLOW) {
                Slog.i(DisplayEffectMonitor.TAG, "SceneRecognition init() PGSdk.getInstance() success!");
            }
            this.needTryInitAgain = false;
            this.mListener = new PGSdk.Sink() {
                public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                    SceneRecognition.this.sceneChanged(stateType, eventType);
                }
            };
            try {
                this.mPGSdk.enableStateEvent(this.mListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
                this.mPGSdk.enableStateEvent(this.mListener, IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
                this.mPGSdk.enableStateEvent(this.mListener, IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                this.mPGSdk.enableStateEvent(this.mListener, IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                this.mPGSdk.enableStateEvent(this.mListener, IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
            } catch (RemoteException e) {
                Slog.e(DisplayEffectMonitor.TAG, "SceneRecognition init() enableStateEvent failed! RemoteException:" + e);
            }
        }

        /* access modifiers changed from: private */
        public void tryInit() {
            if (!this.needTryInitAgain) {
                return;
            }
            if (this.tryInitTimes < 5) {
                this.tryInitTimes++;
                if (DisplayEffectMonitor.HWFLOW) {
                    Slog.i(DisplayEffectMonitor.TAG, "SceneRecognition tryInit() for " + this.tryInitTimes + " times");
                }
                init();
                return;
            }
            Slog.e(DisplayEffectMonitor.TAG, "SceneRecognition tryInit() had tried " + this.tryInitTimes + " times, give up!");
            this.needTryInitAgain = false;
        }

        /* access modifiers changed from: private */
        public void sceneChanged(int stateType, int eventType) {
            switch (stateType) {
                case IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT /*10002*/:
                case IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT /*10011*/:
                    if (eventType == 1) {
                        setScene(BackLightCommonData.Scene.GAME);
                        return;
                    } else {
                        setScene(BackLightCommonData.Scene.OTHERS);
                        return;
                    }
                case IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT /*10010*/:
                case IDisplayEngineService.DE_ACTION_PG_VIDEO_END /*10016*/:
                    setScene(BackLightCommonData.Scene.OTHERS);
                    return;
                case IDisplayEngineService.DE_ACTION_PG_VIDEO_START /*10015*/:
                    setScene(BackLightCommonData.Scene.VIDEO);
                    return;
                default:
                    return;
            }
        }

        private void setScene(BackLightCommonData.Scene scene) {
            if (this.mScene != scene) {
                this.mScene = scene;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(MonitorModule.PARAM_TYPE, "sceneRecognition");
                params.put(MemoryConstant.MEM_POLICY_SCENE, scene.toString());
                DisplayEffectMonitor.this.sendMonitorParam(params);
            }
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public static DisplayEffectMonitor getInstance(Context context) {
        if (mMonitor == null) {
            synchronized (mMonitorLock) {
                if (mMonitor == null) {
                    if (context != null) {
                        try {
                            mMonitor = new DisplayEffectMonitor(context);
                        } catch (Exception e) {
                            Slog.e(TAG, "getInstance() failed! " + e);
                        }
                    } else {
                        Slog.w(TAG, "getInstance() failed! input context and instance is both null");
                    }
                }
            }
        }
        return mMonitor;
    }

    private DisplayEffectMonitor(Context context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mSceneRecognition = new SceneRecognition();
        this.mBackLightMonitorManager = new BackLightMonitorManager(this);
        if (HWFLOW) {
            Slog.i(TAG, "new instance success");
        }
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(MonitorModule.PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "sendMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(MonitorModule.PARAM_TYPE);
        if (HWDEBUG) {
            Slog.d(TAG, "sendMonitorParam() paramType: " + paramType);
        }
        if (paramType != null) {
            if (paramType.equals(TYPE_BOOT_COMPLETED)) {
                bootCompletedInit();
            } else if (this.mBackLightMonitorManager.isParamOwner(paramType)) {
                this.mBackLightMonitorManager.sendMonitorParam(params);
            } else {
                Slog.e(TAG, "sendMonitorParam() undefine paramType: " + paramType);
            }
        }
    }

    public String getCurrentTopAppName() {
        try {
            List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (runningTasks != null) {
                if (!runningTasks.isEmpty()) {
                    return runningTasks.get(0).topActivity.getPackageName();
                }
            }
            return null;
        } catch (SecurityException e) {
            Slog.e(TAG, "getCurrentTopAppName() getRunningTasks SecurityException :" + e);
            return null;
        }
    }

    private void bootCompletedInit() {
        if (this.mBackLightMonitorManager.needHourTimer()) {
            try {
                initHourTimer();
            } catch (NullPointerException e) {
                Slog.e(TAG, "initHourTimer() NullPointerException " + e);
            }
        }
        if (this.mBackLightMonitorManager.needSceneRecognition()) {
            this.mSceneRecognition.init();
        }
        if (HWFLOW) {
            Slog.i(TAG, "bootCompletedInit() done");
        }
    }

    private void initHourTimer() {
        boolean debugMode = SystemProperties.getBoolean("persist.display.monitor.debug", false);
        Calendar scheduleTime = Calendar.getInstance();
        scheduleTime.setTime(new Date());
        if (debugMode) {
            scheduleTime.set(12, scheduleTime.get(12) + 1);
        } else {
            scheduleTime.set(10, scheduleTime.get(10) + 1);
            scheduleTime.set(12, 0);
        }
        scheduleTime.set(13, 0);
        scheduleTime.set(14, 0);
        long waiTime = scheduleTime.getTimeInMillis() - System.currentTimeMillis();
        long j = 3600000;
        if (waiTime <= 0) {
            waiTime = debugMode ? 60000 : 3600000;
        }
        long triggerTime = SystemClock.elapsedRealtime() + waiTime;
        if (debugMode) {
            j = 60000;
        }
        long intervalTime = j;
        if (HWFLOW) {
            Slog.i(TAG, "initHourTimer() debugMode=" + debugMode + ", waiTime=" + waiTime + ", intervalTime=" + intervalTime);
        }
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Slog.e(TAG, "initHourTimer() getSystemService(ALARM_SERVICE) return null");
            return;
        }
        PendingIntent sender = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_MONITOR_TIMER), 0);
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter(ACTION_MONITOR_TIMER));
        this.mAlarmManager.setRepeating(2, triggerTime, intervalTime, sender);
        if (HWFLOW) {
            Slog.i(TAG, "initHourTimer() done");
        }
    }
}
