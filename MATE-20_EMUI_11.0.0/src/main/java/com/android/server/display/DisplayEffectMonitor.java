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
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwServiceFactory;
import com.android.server.display.BackLightCommonData;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.displayengine.IDisplayEngineService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DisplayEffectMonitor implements HwServiceFactory.IDisplayEffectMonitor {
    private static final String ACTION_MONITOR_TIMER = "com.android.server.display.action.MONITOR_TIMER";
    private static final long HOUR = 3600000;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final boolean IS_DISPLAY_LOG_THREAD_DISABLED = SystemProperties.getBoolean("ro.config.displayeffectlogthread.disable", false);
    private static final long MINUTE = 60000;
    private static final String TAG = "DisplayEffectMonitor";
    private static final String TYPE_BOOT_COMPLETED = "bootCompleted";
    private static volatile DisplayEffectMonitor mMonitor;
    private static final Object mMonitorLock = new Object();
    private AlarmManager mAlarmManager;
    private final BackLightMonitorManager mBackLightMonitorManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.display.DisplayEffectMonitor.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(DisplayEffectMonitor.TAG, "onReceive() intent is NULL!");
            } else if (DisplayEffectMonitor.ACTION_MONITOR_TIMER.equals(intent.getAction())) {
                if (DisplayEffectMonitor.HWFLOW) {
                    Slog.i(DisplayEffectMonitor.TAG, "hour task time up");
                }
                DisplayEffectMonitor.this.mBackLightMonitorManager.triggerUploadTimer();
                DisplayEffectMonitor.this.mSceneRecognition.tryInit();
            }
        }
    };
    private final Context mContext;
    private HandlerThread mHandlerThread;
    private final SceneRecognition mSceneRecognition;

    public interface MonitorModule {
        public static final String PARAM_TYPE = "paramType";

        boolean isParamOwner(String str);

        void sendMonitorParam(String str, ArrayMap<String, Object> arrayMap);

        void triggerUploadTimer();
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
                            Slog.e(TAG, "getInstance() failed! catch Exception");
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
        if (IS_DISPLAY_LOG_THREAD_DISABLED) {
            Slog.w(TAG, "DisplayEffectMo thread is disabled.");
        } else {
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
        }
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
                this.mBackLightMonitorManager.sendMonitorParam(paramType, params);
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
            initHourTimer();
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
        int i = (waiTime > 0 ? 1 : (waiTime == 0 ? 0 : -1));
        long intervalTime = MINUTE;
        if (i <= 0) {
            waiTime = debugMode ? 60000 : 3600000;
        }
        long triggerTime = SystemClock.elapsedRealtime() + waiTime;
        if (!debugMode) {
            intervalTime = 3600000;
        }
        if (HWFLOW) {
            Slog.i(TAG, "initHourTimer() debugMode=" + debugMode + ", waiTime=" + waiTime + ", intervalTime=" + intervalTime);
        }
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (this.mAlarmManager == null) {
            Slog.e(TAG, "initHourTimer() getSystemService(ALARM_SERVICE) return null");
            return;
        }
        PendingIntent sender = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_MONITOR_TIMER), 0);
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter(ACTION_MONITOR_TIMER), "com.huawei.permission.CONFIG_BRIGHTNESS", null);
        this.mAlarmManager.setRepeating(2, triggerTime, intervalTime, sender);
        if (HWFLOW) {
            Slog.i(TAG, "initHourTimer() done");
        }
    }

    public boolean isUiNightModeOn() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "ui_night_mode", 0, -2) == 2;
    }

    /* access modifiers changed from: private */
    public class SceneRecognition {
        private static final int MAX_TRY_INIT_TIMES = 5;
        private PowerKit.Sink mListener;
        private PowerKit mPowerKit;
        private BackLightCommonData.Scene mScene;
        private boolean needTryInitAgain;
        private int tryInitTimes;

        private SceneRecognition() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void init() {
            this.mPowerKit = PowerKit.getInstance();
            if (this.mPowerKit == null) {
                Slog.e(DisplayEffectMonitor.TAG, "SceneRecognition init() PowerKit.getInstance() failed!");
                this.needTryInitAgain = true;
                return;
            }
            if (DisplayEffectMonitor.HWFLOW) {
                Slog.i(DisplayEffectMonitor.TAG, "SceneRecognition init() PowerKit.getInstance() success!");
            }
            this.needTryInitAgain = false;
            this.mListener = new PowerKit.Sink() {
                /* class com.android.server.display.DisplayEffectMonitor.SceneRecognition.AnonymousClass1 */

                public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
                    SceneRecognition.this.sceneChanged(stateType, eventType);
                }
            };
            try {
                this.mPowerKit.enableStateEvent(this.mListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_START);
                this.mPowerKit.enableStateEvent(this.mListener, (int) IDisplayEngineService.DE_ACTION_PG_VIDEO_END);
                this.mPowerKit.enableStateEvent(this.mListener, (int) IDisplayEngineService.DE_ACTION_PG_2DGAME_FRONT);
                this.mPowerKit.enableStateEvent(this.mListener, (int) IDisplayEngineService.DE_ACTION_PG_3DGAME_FRONT);
                this.mPowerKit.enableStateEvent(this.mListener, (int) IDisplayEngineService.DE_ACTION_PG_LAUNCHER_FRONT);
            } catch (RemoteException e) {
                Slog.e(DisplayEffectMonitor.TAG, "SceneRecognition init() enableStateEvent failed! RemoteException:" + e);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void tryInit() {
            if (this.needTryInitAgain) {
                int i = this.tryInitTimes;
                if (i < 5) {
                    this.tryInitTimes = i + 1;
                    if (DisplayEffectMonitor.HWFLOW) {
                        Slog.i(DisplayEffectMonitor.TAG, "SceneRecognition tryInit() for " + this.tryInitTimes + " times");
                    }
                    init();
                    return;
                }
                Slog.e(DisplayEffectMonitor.TAG, "SceneRecognition tryInit() had tried " + this.tryInitTimes + " times, give up!");
                this.needTryInitAgain = false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void sceneChanged(int stateType, int eventType) {
            if (stateType != 10002) {
                if (stateType != 10010) {
                    if (stateType != 10011) {
                        if (stateType == 10015) {
                            setScene(BackLightCommonData.Scene.VIDEO);
                            return;
                        } else if (stateType != 10016) {
                            return;
                        }
                    }
                }
                setScene(BackLightCommonData.Scene.OTHERS);
                return;
            }
            if (eventType == 1) {
                setScene(BackLightCommonData.Scene.GAME);
            } else {
                setScene(BackLightCommonData.Scene.OTHERS);
            }
        }

        private void setScene(BackLightCommonData.Scene scene) {
            if (this.mScene != scene) {
                this.mScene = scene;
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put(MonitorModule.PARAM_TYPE, "sceneRecognition");
                params.put("scene", scene.toString());
                DisplayEffectMonitor.this.sendMonitorParam(params);
            }
        }
    }

    public class ParamLogPrinter {
        private static final int LOG_PRINTER_MSG = 1;
        private static final int mMessageDelayInMs = 2000;
        private boolean mFirstTime = true;
        private long mLastTime;
        private int mLastValue;
        private final Object mLock = new Object();
        private String mPackageName;
        private boolean mParamDecrease;
        private boolean mParamIncrease;
        private final String mParamName;
        private long mStartTime;
        private int mStartValue;
        private final String mTAG;
        private final SimpleDateFormat mTimeFormater = new SimpleDateFormat("HH:mm:ss.SSS");
        private Handler mTimeHandler = null;

        public ParamLogPrinter(String paramName, String tag) {
            this.mParamName = paramName;
            this.mTAG = tag;
            if (DisplayEffectMonitor.this.mHandlerThread != null) {
                this.mTimeHandler = new Handler(DisplayEffectMonitor.this.mHandlerThread.getLooper(), DisplayEffectMonitor.this) {
                    /* class com.android.server.display.DisplayEffectMonitor.ParamLogPrinter.AnonymousClass1 */

                    @Override // android.os.Handler
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
                                    ParamLogPrinter.this.mFirstTime = true;
                                }
                            }
                        }
                    }
                };
            }
        }

        public void updateParam(int param, String packageName) {
            synchronized (this.mLock) {
                boolean z = false;
                if (this.mFirstTime) {
                    this.mStartValue = param;
                    this.mLastValue = this.mStartValue;
                    this.mStartTime = System.currentTimeMillis();
                    this.mLastTime = this.mStartTime;
                    this.mPackageName = packageName;
                    this.mParamIncrease = false;
                    this.mParamDecrease = false;
                    sendHandlerMessage(param);
                    this.mFirstTime = false;
                } else if (this.mLastValue != param) {
                    if ((!this.mParamIncrease || param >= this.mLastValue) && (!this.mParamDecrease || param <= this.mLastValue)) {
                        if (!this.mParamIncrease && !this.mParamDecrease) {
                            this.mParamIncrease = param > this.mLastValue;
                            if (param < this.mLastValue) {
                                z = true;
                            }
                            this.mParamDecrease = z;
                        }
                        this.mLastValue = param;
                        this.mLastTime = System.currentTimeMillis();
                        sendHandlerMessage(param);
                        return;
                    }
                    printValueChange(this.mStartValue, this.mStartTime, this.mLastValue, this.mLastTime, this.mPackageName);
                    this.mStartValue = this.mLastValue;
                    this.mLastValue = param;
                    this.mStartTime = this.mLastTime;
                    this.mLastTime = System.currentTimeMillis();
                    this.mParamIncrease = this.mLastValue > this.mStartValue;
                    if (this.mLastValue < this.mStartValue) {
                        z = true;
                    }
                    this.mParamDecrease = z;
                    sendHandlerMessage(param);
                }
            }
        }

        private void sendHandlerMessage(int param) {
            Handler handler = this.mTimeHandler;
            if (handler != null) {
                handler.removeMessages(1);
                this.mTimeHandler.sendMessageDelayed(this.mTimeHandler.obtainMessage(1, param, 0), 2000);
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
                this.mFirstTime = true;
                if (this.mTimeHandler != null) {
                    this.mTimeHandler.removeMessages(1);
                }
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
                sendHandlerMessage(param);
                this.mFirstTime = false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void printSingleValue(int value, long time, String packageName) {
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
        /* access modifiers changed from: public */
        private void printValueChange(int startValue, long startTime, int endValue, long endTime, String packageName) {
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
            if (DisplayEffectMonitor.HWFLOW) {
                String str2 = this.mTAG;
                StringBuilder sb = new StringBuilder();
                sb.append(this.mParamName);
                sb.append(" ");
                sb.append(value1);
                String str3 = "";
                if (packageName1 != null) {
                    str = " by " + packageName1;
                } else {
                    str = str3;
                }
                sb.append(str);
                sb.append(" -> ");
                sb.append(value2);
                if (packageName2 != null) {
                    str3 = " by " + packageName2;
                }
                sb.append(str3);
                Slog.i(str2, sb.toString());
            }
        }
    }
}
