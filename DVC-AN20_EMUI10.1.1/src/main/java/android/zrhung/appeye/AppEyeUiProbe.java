package android.zrhung.appeye;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.BlockMonitor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.DataContract;
import android.util.Log;
import android.util.ZRHung;
import android.view.ViewRootImpl;
import android.zrhung.IAppEyeUiProbe;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;
import com.huawei.uikit.effect.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.Optional;

public final class AppEyeUiProbe extends ZrHungImpl implements IAppEyeUiProbe {
    private static boolean IS_BETA_VERSION = false;
    private static final String TAG = "ZrHung.AppEyeUiProbe";
    private static long msgDelayWatermarkDelay = 3000;
    private static long msgDelayWatermarkWarning = 1000;
    private static AppEyeUiProbe singleton;
    private BlockMonitor mBlockMonitor;
    private final AppEyeUIPChecker mChecker = new AppEyeUIPChecker(this);
    private HandlerThread mCheckerThread = null;
    private Handler mHandler;
    private boolean mIsLifeCycleListenerRegisted = false;
    private int mTriedTimes = 0;
    private Optional<AppEyeUiScroll> mUiScroll = Optional.empty();

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z = true;
        }
        IS_BETA_VERSION = z;
    }

    private AppEyeUiProbe(String wpName) {
        super(wpName);
    }

    /* access modifiers changed from: private */
    public final class AppEyeUIPChecker implements Runnable {
        private static final int FREEZE_HAPPENED = 2;
        private static final int INPUT_TIMEOUT_HAPPENED = 3;
        private static final int LENGTH_OF_ARRAY = 10;
        private static final int NO_EVENT_HAPPENED = 0;
        private static final int UPPER_BOUND_OF_ARRAY = 9;
        private static final long VSYNC_CHECK_OFFSET = 1000;
        private static final int WARINIG_HAPPENED = 1;
        private long mBlockFreezeTimeout = (this.mBlockWarningTimeout * 2);
        private long mBlockInputTimeout = 4000;
        private long mBlockWarningTimeout = 3000;
        private int mCheckStateForDispatchTime = 0;
        private WeakReference<Activity> mCurrActivity = null;
        private long mDefaultCheckInterval = VSYNC_CHECK_OFFSET;
        private long mDispatchTime;
        private long mFirstFdEventTime = 0;
        private boolean mIsConfigReady = false;
        private boolean mIsDispatching = false;
        private boolean mIsEnabled = false;
        private boolean mIsFunctionEnabled = true;
        private MessageQueue mMainQueue;
        private int mPid = Process.myPid();
        private AppEyeUiProbe mProbe;
        private long msgDelayFreeze = (this.msgDelayWarning * 2);
        private long msgDelayWarning = 3000;

        AppEyeUIPChecker(AppEyeUiProbe probe) {
            this.mProbe = probe;
        }

        public void run() {
            getHungConfig();
            if (!this.mIsFunctionEnabled) {
                Log.d(AppEyeUiProbe.TAG, "apppuip fuction is not enabled");
                return;
            }
            if (AppEyeUiProbe.IS_BETA_VERSION) {
                AppEyeUiProbe.this.registeLifeCycleListener(this.mProbe);
            }
            synchronized (this) {
                if (!this.mIsEnabled) {
                    this.mCheckStateForDispatchTime = 0;
                    if (!isCurrentActivityResume()) {
                        try {
                            Log.d(AppEyeUiProbe.TAG, "not watching, wait.");
                            wait();
                            Log.d(AppEyeUiProbe.TAG, "restart watching");
                        } catch (InterruptedException e) {
                            Log.w(AppEyeUiProbe.TAG, "Interrupted");
                        }
                    } else {
                        this.mIsEnabled = true;
                    }
                }
            }
            AppEyeUiProbe.this.mHandler.postDelayed(this, checkDispatchTimeout());
        }

        private long checkDispatchTimeout() {
            if (this.mIsDispatching) {
                long curTime = SystemClock.uptimeMillis();
                long elapsed = curTime - this.mDispatchTime;
                if (elapsed >= this.mBlockFreezeTimeout) {
                    if (this.mCheckStateForDispatchTime == 0 && isLastVsyncTimeout(this.mBlockWarningTimeout)) {
                        this.mCheckStateForDispatchTime = 1;
                        sendAppEyeEvents(ZRHung.APPEYE_UIP_WARNING, elapsed);
                    }
                    if (this.mCheckStateForDispatchTime == 1) {
                        sendAppEyeEvents(ZRHung.APPEYE_UIP_FREEZE, elapsed);
                        this.mCheckStateForDispatchTime = 2;
                        checkDispatchTimeout(curTime, elapsed);
                    }
                    if (this.mCheckStateForDispatchTime == 2) {
                        checkDispatchTimeout(curTime, elapsed);
                    }
                    return this.mDefaultCheckInterval;
                }
                long j = this.mBlockWarningTimeout;
                if (elapsed >= j) {
                    if (this.mCheckStateForDispatchTime == 0 && isLastVsyncTimeout(j)) {
                        sendAppEyeEvents(ZRHung.APPEYE_UIP_WARNING, elapsed);
                        this.mCheckStateForDispatchTime = 1;
                        this.mFirstFdEventTime = peekEvent() ? SystemClock.uptimeMillis() : this.mFirstFdEventTime;
                    }
                    if (this.mFirstFdEventTime == 0 && peekEvent()) {
                        this.mFirstFdEventTime = SystemClock.uptimeMillis();
                    }
                    return this.mDefaultCheckInterval;
                } else if (this.mBlockInputTimeout < elapsed && elapsed < j) {
                    return j - elapsed;
                }
            }
            if (this.mCheckStateForDispatchTime == 1 && this.mFirstFdEventTime != 0) {
                sendAppEyeEvents(ZRHung.APPEYE_UIP_SLOW, 0);
            }
            int i = this.mCheckStateForDispatchTime;
            if (i == 2 || i == 3) {
                Log.d(AppEyeUiProbe.TAG, "sendAppEye recover Events.");
                sendAppEyeEvents(ZRHung.APPEYE_UIP_RECOVER, 0);
            }
            this.mCheckStateForDispatchTime = 0;
            this.mFirstFdEventTime = 0;
            return this.mDefaultCheckInterval;
        }

        private void checkDispatchTimeout(long curTime, long elapsed) {
            long j = this.mFirstFdEventTime;
            if (curTime - j > this.mBlockInputTimeout && j != 0 && isLastVsyncTimeout(this.mBlockFreezeTimeout)) {
                sendAppEyeEvents(ZRHung.APPEYE_UIP_INPUT, elapsed);
                this.mCheckStateForDispatchTime = 3;
            } else if (this.mFirstFdEventTime == 0 && peekEvent()) {
                this.mFirstFdEventTime = SystemClock.uptimeMillis();
            }
        }

        private void getHungConfig() {
            if (!this.mIsConfigReady) {
                ZRHung.HungConfig cfg = ZRHung.getHungConfig(AppEyeUiProbe.this.mWpId);
                if (cfg == null) {
                    Log.w(AppEyeUiProbe.TAG, "Failed to get config from zrhung");
                    this.mIsConfigReady = true;
                } else if (cfg.status == 0) {
                    if (cfg.value == null) {
                        Log.w(AppEyeUiProbe.TAG, "Failed to get config from zrhung");
                        this.mIsConfigReady = true;
                        return;
                    }
                    String[] configs = cfg.value.split(",");
                    this.mIsFunctionEnabled = "1".equals(configs[0]);
                    for (int i = 1; i < configs.length; i++) {
                        try {
                            setTime(Long.parseLong(configs[i]), i);
                        } catch (NumberFormatException e) {
                            Log.w(AppEyeUiProbe.TAG, "the config string cannot be parsed as long");
                        }
                    }
                    this.msgDelayWarning = this.mBlockWarningTimeout;
                    this.msgDelayFreeze = this.mBlockFreezeTimeout;
                    this.mIsConfigReady = true;
                } else if (cfg.status == -1 || cfg.status == -2) {
                    Log.w(AppEyeUiProbe.TAG, "config is not support or there is no config");
                    this.mIsConfigReady = true;
                    this.mIsFunctionEnabled = false;
                } else {
                    Log.w(AppEyeUiProbe.TAG, "error config type");
                }
            }
        }

        private void setTime(long time, int index) {
            if (time != 0) {
                switch (index) {
                    case 1:
                        this.mBlockWarningTimeout = time;
                        return;
                    case 2:
                        this.mBlockFreezeTimeout = time;
                        return;
                    case 3:
                        this.mBlockInputTimeout = time;
                        return;
                    case 4:
                        long unused = AppEyeUiProbe.msgDelayWatermarkWarning = time;
                        return;
                    case 5:
                        long unused2 = AppEyeUiProbe.msgDelayWatermarkDelay = time;
                        return;
                    case 6:
                        this.mDefaultCheckInterval = time;
                        return;
                    default:
                        return;
                }
            }
        }

        private boolean peekEvent() {
            ViewRootImpl viewRootImpl;
            Activity tempActivity = null;
            WeakReference<Activity> weakReference = this.mCurrActivity;
            if (weakReference != null) {
                tempActivity = weakReference.get();
            }
            if (tempActivity == null || (viewRootImpl = tempActivity.getWindow().getDecorView().getViewRootImpl()) == null || !viewRootImpl.peekEvent()) {
                return false;
            }
            return true;
        }

        private boolean isLastVsyncTimeout(long timeout) {
            MessageQueue mainQueue = getMainMessageQueue();
            if (mainQueue != null && SystemClock.uptimeMillis() - mainQueue.mLastVsyncEnd > timeout - VSYNC_CHECK_OFFSET) {
                return true;
            }
            Log.w(AppEyeUiProbe.TAG, "UIP freezing Happened but we still have vsync signal, exclude this case");
            this.mIsFunctionEnabled = false;
            return false;
        }

        private MessageQueue getMainMessageQueue() {
            Looper mainLooper;
            if (this.mMainQueue == null && (mainLooper = Looper.getMainLooper()) != null) {
                this.mMainQueue = mainLooper.getQueue();
            }
            return this.mMainQueue;
        }

        private String getPackageName() {
            return ActivityThread.currentPackageName();
        }

        private String getActivityName() {
            return ActivityThread.currentActivityName();
        }

        private String getProcessName() {
            return ActivityThread.currentProcessName();
        }

        private int getUid() {
            try {
                if (ActivityThread.currentApplication() != null) {
                    return ActivityThread.currentApplication().getApplicationContext().getPackageManager().getApplicationInfo(getPackageName(), 0).uid;
                }
                return 0;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(AppEyeUiProbe.TAG, "name not found exception in AppEyeUiProbe");
                return 0;
            }
        }

        private boolean isCurrentActivityResume() {
            Activity tempActivity = null;
            WeakReference<Activity> weakReference = this.mCurrActivity;
            if (weakReference != null) {
                tempActivity = weakReference.get();
            }
            if (tempActivity != null) {
                return tempActivity.isResumed();
            }
            return false;
        }

        private void sendAppEyeEvents(short wpId, long elapsedTime) {
            try {
                ZrHungData data = new ZrHungData();
                data.putInt(DataContract.BaseProperty.UID, getUid());
                data.putInt("pid", this.mPid);
                data.putString(DataContract.BaseProperty.PACKAGE_NAME, getPackageName());
                data.putString("processName", getProcessName());
                StringBuilder eventBuffer = new StringBuilder();
                String cmd = null;
                if (wpId == 257) {
                    eventBuffer.append("APPEYE_UIP_WARNING" + System.lineSeparator());
                    eventBuffer.append("activityName = ");
                    eventBuffer.append(getActivityName() + System.lineSeparator());
                    eventBuffer.append("versionName = ");
                    eventBuffer.append(ZRHung.getVersionName(getPackageName()));
                    eventBuffer.append(System.lineSeparator());
                    eventBuffer.append(AppEyeUiProbe.this.dumpMessage());
                    eventBuffer.append(", has taken ");
                    eventBuffer.append(String.valueOf(elapsedTime) + "ms");
                    cmd = "p=" + this.mPid;
                } else if (wpId == 258) {
                    eventBuffer.append("APPEYE_UIP_FREEZE" + System.lineSeparator());
                    eventBuffer.append(AppEyeUiProbe.this.dumpMessage());
                    eventBuffer.append(", has taken ");
                    eventBuffer.append(String.valueOf(elapsedTime) + "ms");
                    cmd = "B";
                } else if (wpId == 260) {
                    eventBuffer.append("APPEYE_UIP_INPUT\n");
                }
                if (!Log.HWINFO) {
                    cmd = BuildConfig.FLAVOR;
                }
                AppEyeUiProbe.this.sendAppEyeEvent(wpId, data, cmd, eventBuffer.toString());
            } catch (Exception e) {
                Log.d(AppEyeUiProbe.TAG, "sendAppEyeEvent exception");
            }
        }
    }

    public static synchronized AppEyeUiProbe get() {
        AppEyeUiProbe appEyeUiProbe;
        synchronized (AppEyeUiProbe.class) {
            if (singleton == null) {
                singleton = new AppEyeUiProbe("appeye_uiprobe");
            }
            appEyeUiProbe = singleton;
        }
        return appEyeUiProbe;
    }

    public void setBlockMonitor(BlockMonitor monitor) {
        this.mBlockMonitor = monitor;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean start(ZrHungData zrHungData) {
        synchronized (this) {
            if (this.mCheckerThread == null) {
                this.mCheckerThread = new HandlerThread("AppEyeUiProbeThread");
                this.mCheckerThread.start();
                this.mHandler = new Handler(this.mCheckerThread.getLooper());
                this.mHandler.post(this.mChecker);
            }
            if (IS_BETA_VERSION && !this.mUiScroll.isPresent()) {
                this.mUiScroll = AppEyeUiScroll.createInstance(this.mHandler);
            }
        }
        synchronized (this.mChecker) {
            this.mChecker.mIsEnabled = true;
            this.mChecker.notifyAll();
        }
        return true;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean stop(ZrHungData zrHungData) {
        this.mChecker.mIsEnabled = false;
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
        if (r2.equals("onFlingStart") != false) goto L_0x0045;
     */
    @Override // android.zrhung.ZrHungImpl
    public boolean check(ZrHungData zrHungData) {
        boolean z = false;
        if (zrHungData == null) {
            Log.e(TAG, "args is null");
            return false;
        }
        String method = zrHungData.getString("method");
        switch (method.hashCode()) {
            case -1273815131:
                break;
            case 3540994:
                if (method.equals("stop")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 109757538:
                if (method.equals("start")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 231233763:
                if (method.equals("setCurrActivity")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            Object obj = zrHungData.get("obj");
            if (this.mUiScroll.isPresent()) {
                this.mUiScroll.get().onFlingStart(obj);
            }
        } else if (z) {
            Object receiveActivity = zrHungData.get("activity");
            if (receiveActivity instanceof Activity) {
                setCurrActivity((Activity) receiveActivity);
            }
        } else if (z) {
            start(null);
        } else if (!z) {
            Log.d(TAG, "unexpected method which should be handled.");
        } else {
            stop(null);
        }
        return true;
    }

    private void setCurrActivity(Activity activity) {
        this.mChecker.mCurrActivity = new WeakReference(activity);
    }

    public void beginDispatching(Message msg, Handler target, Runnable callback) {
        this.mChecker.mDispatchTime = SystemClock.uptimeMillis();
        this.mChecker.mIsDispatching = true;
    }

    public void beginDispatching(long dispatchTime) {
        this.mChecker.mDispatchTime = dispatchTime;
        this.mChecker.mIsDispatching = true;
    }

    /* access modifiers changed from: package-private */
    public class AppEyeActivityLifeCycleListener implements Application.ActivityLifecycleCallbacks {
        AppEyeUiProbe mProbe;

        AppEyeActivityLifeCycleListener(AppEyeUiProbe probe) {
            this.mProbe = probe;
        }

        public void onActivityPaused(Activity activity) {
            AppEyeUiProbe appEyeUiProbe = this.mProbe;
            if (appEyeUiProbe != null && appEyeUiProbe.mUiScroll.isPresent()) {
                ((AppEyeUiScroll) this.mProbe.mUiScroll.get()).stop(activity);
            }
        }

        public void onActivityResumed(Activity activity) {
            AppEyeUiProbe appEyeUiProbe = this.mProbe;
            if (appEyeUiProbe != null && appEyeUiProbe.mUiScroll.isPresent()) {
                ((AppEyeUiScroll) this.mProbe.mUiScroll.get()).setCurrActivity(activity);
            }
        }

        public void onActivityStopped(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityDestroyed(Activity activity) {
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        public void onActivityStarted(Activity activity) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registeLifeCycleListener(AppEyeUiProbe probe) {
        int i;
        if (!this.mIsLifeCycleListenerRegisted) {
            Application app = ActivityThread.currentApplication();
            if (app != null) {
                app.registerActivityLifecycleCallbacks(new AppEyeActivityLifeCycleListener(probe));
                this.mIsLifeCycleListenerRegisted = true;
                Log.d(TAG, "mMainLooper registeLifeCycleListener success");
            } else if (!this.mIsLifeCycleListenerRegisted && (i = this.mTriedTimes) < 3) {
                this.mTriedTimes = i + 1;
            } else if (probe != null) {
                probe.stop(null);
            }
        }
    }

    public void endDispatching() {
        this.mChecker.mIsDispatching = false;
    }

    private void addDelayedMsgList(long msgDelayed) {
        if (Log.HWINFO) {
            Log.i(TAG, "mMainLooper addDelayedMsgList");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String dumpMessage() {
        BlockMonitor blockMonitor = this.mBlockMonitor;
        if (blockMonitor != null) {
            return blockMonitor.dumpMainMessageQueue();
        }
        return "Erro No BlockMonitor Installed";
    }
}
