package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.PerformanceCollector;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.TestLooperManager;
import android.os.Trace;
import android.os.UserHandle;
import android.util.AndroidRuntimeException;
import android.util.Jlog;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.ViewConfiguration;
import android.view.WindowManagerGlobal;
import com.android.internal.content.ReferrerIntent;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class Instrumentation {
    public static final String REPORT_KEY_IDENTIFIER = "id";
    public static final String REPORT_KEY_STREAMRESULT = "stream";
    private static final String TAG = "Instrumentation";
    private List<ActivityMonitor> mActivityMonitors;
    private final Object mAnimationCompleteLock = new Object();
    private Context mAppContext;
    private boolean mAutomaticPerformanceSnapshots = false;
    private ComponentName mComponent;
    private Context mInstrContext;
    private MessageQueue mMessageQueue = null;
    private Bundle mPerfMetrics = new Bundle();
    private PerformanceCollector mPerformanceCollector;
    private Thread mRunner;
    private final Object mSync = new Object();
    private ActivityThread mThread = null;
    private UiAutomation mUiAutomation;
    private IUiAutomationConnection mUiAutomationConnection;
    private List<ActivityWaiter> mWaitingActivities;
    private IInstrumentationWatcher mWatcher;

    @Retention(RetentionPolicy.SOURCE)
    public @interface UiAutomationFlags {
    }

    public void setActivityThread(Object thread) {
        this.mThread = (ActivityThread) thread;
    }

    private void checkInstrumenting(String method) {
        if (this.mInstrContext == null) {
            throw new RuntimeException(method + " cannot be called outside of instrumented processes");
        }
    }

    public void onCreate(Bundle arguments) {
    }

    public void start() {
        if (this.mRunner == null) {
            this.mRunner = new InstrumentationThread("Instr: " + getClass().getName());
            this.mRunner.start();
            return;
        }
        throw new RuntimeException("Instrumentation already started");
    }

    public void onStart() {
    }

    public boolean onException(Object obj, Throwable e) {
        return false;
    }

    public void sendStatus(int resultCode, Bundle results) {
        IInstrumentationWatcher iInstrumentationWatcher = this.mWatcher;
        if (iInstrumentationWatcher != null) {
            try {
                iInstrumentationWatcher.instrumentationStatus(this.mComponent, resultCode, results);
            } catch (RemoteException e) {
                this.mWatcher = null;
            }
        }
    }

    public void addResults(Bundle results) {
        try {
            ActivityManager.getService().addInstrumentationResults(this.mThread.getApplicationThread(), results);
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
    }

    public void finish(int resultCode, Bundle results) {
        if (this.mAutomaticPerformanceSnapshots) {
            endPerformanceSnapshot();
        }
        if (this.mPerfMetrics != null) {
            if (results == null) {
                results = new Bundle();
            }
            results.putAll(this.mPerfMetrics);
        }
        UiAutomation uiAutomation = this.mUiAutomation;
        if (uiAutomation != null && !uiAutomation.isDestroyed()) {
            this.mUiAutomation.disconnect();
            this.mUiAutomation = null;
        }
        this.mThread.finishInstrumentation(resultCode, results);
    }

    public void setAutomaticPerformanceSnapshots() {
        this.mAutomaticPerformanceSnapshots = true;
        this.mPerformanceCollector = new PerformanceCollector();
    }

    public void startPerformanceSnapshot() {
        if (!isProfiling()) {
            this.mPerformanceCollector.beginSnapshot(null);
        }
    }

    public void endPerformanceSnapshot() {
        if (!isProfiling()) {
            this.mPerfMetrics = this.mPerformanceCollector.endSnapshot();
        }
    }

    public void onDestroy() {
    }

    public Context getContext() {
        return this.mInstrContext;
    }

    public ComponentName getComponentName() {
        return this.mComponent;
    }

    public Context getTargetContext() {
        return this.mAppContext;
    }

    public String getProcessName() {
        return this.mThread.getProcessName();
    }

    public boolean isProfiling() {
        return this.mThread.isProfiling();
    }

    public void startProfiling() {
        if (this.mThread.isProfiling()) {
            File file = new File(this.mThread.getProfileFilePath());
            file.getParentFile().mkdirs();
            Debug.startMethodTracing(file.toString(), 8388608);
        }
    }

    public void stopProfiling() {
        if (this.mThread.isProfiling()) {
            Debug.stopMethodTracing();
        }
    }

    public void setInTouchMode(boolean inTouch) {
        try {
            IWindowManager.Stub.asInterface(ServiceManager.getService("window")).setInTouchMode(inTouch);
        } catch (RemoteException e) {
        }
    }

    public void waitForIdle(Runnable recipient) {
        this.mMessageQueue.addIdleHandler(new Idler(recipient));
        this.mThread.getHandler().post(new EmptyRunnable());
    }

    public void waitForIdleSync() {
        validateNotAppThread();
        Idler idler = new Idler(null);
        this.mMessageQueue.addIdleHandler(idler);
        this.mThread.getHandler().post(new EmptyRunnable());
        idler.waitForIdle();
    }

    private void waitForEnterAnimationComplete(Activity activity) {
        synchronized (this.mAnimationCompleteLock) {
            long timeout = 5000;
            while (timeout > 0) {
                try {
                    if (!activity.mEnterAnimationComplete) {
                        long startTime = System.currentTimeMillis();
                        this.mAnimationCompleteLock.wait(timeout);
                        timeout -= System.currentTimeMillis() - startTime;
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void onEnterAnimationComplete() {
        synchronized (this.mAnimationCompleteLock) {
            this.mAnimationCompleteLock.notifyAll();
        }
    }

    public void runOnMainSync(Runnable runner) {
        validateNotAppThread();
        SyncRunnable sr = new SyncRunnable(runner);
        this.mThread.getHandler().post(sr);
        sr.waitForComplete();
    }

    public Activity startActivitySync(Intent intent) {
        return startActivitySync(intent, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007f, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0080, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0083, code lost:
        throw r6;
     */
    public Activity startActivitySync(Intent intent, Bundle options) {
        Activity activity;
        validateNotAppThread();
        synchronized (this.mSync) {
            Intent intent2 = new Intent(intent);
            ActivityInfo ai = intent2.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
            if (ai != null) {
                String myProc = this.mThread.getProcessName();
                if (ai.processName.equals(myProc)) {
                    intent2.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
                    ActivityWaiter aw = new ActivityWaiter(intent2);
                    if (this.mWaitingActivities == null) {
                        this.mWaitingActivities = new ArrayList();
                    }
                    this.mWaitingActivities.add(aw);
                    getTargetContext().startActivity(intent2, options);
                    do {
                        try {
                            this.mSync.wait();
                        } catch (InterruptedException e) {
                        }
                    } while (this.mWaitingActivities.contains(aw));
                    waitForEnterAnimationComplete(aw.activity);
                    SurfaceControl.Transaction t = new SurfaceControl.Transaction();
                    t.apply(true);
                    t.close();
                    activity = aw.activity;
                } else {
                    throw new RuntimeException("Intent in process " + myProc + " resolved to different process " + ai.processName + ": " + intent2);
                }
            } else {
                throw new RuntimeException("Unable to resolve activity for: " + intent2);
            }
        }
        return activity;
    }

    public static class ActivityMonitor {
        private final boolean mBlock;
        private final String mClass;
        int mHits;
        private final boolean mIgnoreMatchingSpecificIntents;
        Activity mLastActivity;
        private final ActivityResult mResult;
        private final IntentFilter mWhich;

        public ActivityMonitor(IntentFilter which, ActivityResult result, boolean block) {
            this.mHits = 0;
            this.mLastActivity = null;
            this.mWhich = which;
            this.mClass = null;
            this.mResult = result;
            this.mBlock = block;
            this.mIgnoreMatchingSpecificIntents = false;
        }

        public ActivityMonitor(String cls, ActivityResult result, boolean block) {
            this.mHits = 0;
            this.mLastActivity = null;
            this.mWhich = null;
            this.mClass = cls;
            this.mResult = result;
            this.mBlock = block;
            this.mIgnoreMatchingSpecificIntents = false;
        }

        public ActivityMonitor() {
            this.mHits = 0;
            this.mLastActivity = null;
            this.mWhich = null;
            this.mClass = null;
            this.mResult = null;
            this.mBlock = false;
            this.mIgnoreMatchingSpecificIntents = true;
        }

        /* access modifiers changed from: package-private */
        public final boolean ignoreMatchingSpecificIntents() {
            return this.mIgnoreMatchingSpecificIntents;
        }

        public final IntentFilter getFilter() {
            return this.mWhich;
        }

        public final ActivityResult getResult() {
            return this.mResult;
        }

        public final boolean isBlocking() {
            return this.mBlock;
        }

        public final int getHits() {
            return this.mHits;
        }

        public final Activity getLastActivity() {
            return this.mLastActivity;
        }

        public final Activity waitForActivity() {
            Activity res;
            synchronized (this) {
                while (this.mLastActivity == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                res = this.mLastActivity;
                this.mLastActivity = null;
            }
            return res;
        }

        public final Activity waitForActivityWithTimeout(long timeOut) {
            synchronized (this) {
                if (this.mLastActivity == null) {
                    try {
                        wait(timeOut);
                    } catch (InterruptedException e) {
                    }
                }
                if (this.mLastActivity == null) {
                    return null;
                }
                Activity res = this.mLastActivity;
                this.mLastActivity = null;
                return res;
            }
        }

        public ActivityResult onStartActivity(Intent intent) {
            return null;
        }

        /* access modifiers changed from: package-private */
        public final boolean match(Context who, Activity activity, Intent intent) {
            if (this.mIgnoreMatchingSpecificIntents) {
                return false;
            }
            synchronized (this) {
                if (this.mWhich != null && this.mWhich.match(who.getContentResolver(), intent, true, Instrumentation.TAG) < 0) {
                    return false;
                }
                if (this.mClass != null) {
                    String cls = null;
                    if (activity != null) {
                        cls = activity.getClass().getName();
                    } else if (intent.getComponent() != null) {
                        cls = intent.getComponent().getClassName();
                    }
                    if (cls == null || !this.mClass.equals(cls)) {
                        return false;
                    }
                }
                if (activity != null) {
                    this.mLastActivity = activity;
                    notifyAll();
                }
                return true;
            }
        }
    }

    public void addMonitor(ActivityMonitor monitor) {
        synchronized (this.mSync) {
            if (this.mActivityMonitors == null) {
                this.mActivityMonitors = new ArrayList();
            }
            this.mActivityMonitors.add(monitor);
        }
    }

    public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        ActivityMonitor am = new ActivityMonitor(filter, result, block);
        addMonitor(am);
        return am;
    }

    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        ActivityMonitor am = new ActivityMonitor(cls, result, block);
        addMonitor(am);
        return am;
    }

    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        waitForIdleSync();
        synchronized (this.mSync) {
            if (monitor.getHits() < minHits) {
                return false;
            }
            this.mActivityMonitors.remove(monitor);
            return true;
        }
    }

    public Activity waitForMonitor(ActivityMonitor monitor) {
        Activity activity = monitor.waitForActivity();
        synchronized (this.mSync) {
            this.mActivityMonitors.remove(monitor);
        }
        return activity;
    }

    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        Activity activity = monitor.waitForActivityWithTimeout(timeOut);
        synchronized (this.mSync) {
            this.mActivityMonitors.remove(monitor);
        }
        return activity;
    }

    public void removeMonitor(ActivityMonitor monitor) {
        synchronized (this.mSync) {
            this.mActivityMonitors.remove(monitor);
        }
    }

    public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
        AnonymousClass1MenuRunnable mr = new Runnable(targetActivity, id, flag) {
            /* class android.app.Instrumentation.AnonymousClass1MenuRunnable */
            private final Activity activity;
            private final int flags;
            private final int identifier;
            boolean returnValue;

            {
                this.activity = _activity;
                this.identifier = _identifier;
                this.flags = _flags;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.returnValue = this.activity.getWindow().performPanelIdentifierAction(0, this.identifier, this.flags);
            }
        };
        runOnMainSync(mr);
        return mr.returnValue;
    }

    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        validateNotAppThread();
        sendKeySync(new KeyEvent(0, 23));
        waitForIdleSync();
        try {
            Thread.sleep((long) ViewConfiguration.getLongPressTimeout());
            sendKeySync(new KeyEvent(1, 23));
            waitForIdleSync();
            AnonymousClass1ContextMenuRunnable cmr = new Runnable(targetActivity, id, flag) {
                /* class android.app.Instrumentation.AnonymousClass1ContextMenuRunnable */
                private final Activity activity;
                private final int flags;
                private final int identifier;
                boolean returnValue;

                {
                    this.activity = _activity;
                    this.identifier = _identifier;
                    this.flags = _flags;
                }

                @Override // java.lang.Runnable
                public void run() {
                    this.returnValue = this.activity.getWindow().performContextMenuIdentifierAction(this.identifier, this.flags);
                }
            };
            runOnMainSync(cmr);
            return cmr.returnValue;
        } catch (InterruptedException e) {
            Log.e(TAG, "Could not sleep for long press timeout", e);
            return false;
        }
    }

    public void sendStringSync(String text) {
        KeyEvent[] events;
        if (!(text == null || (events = KeyCharacterMap.load(-1).getEvents(text.toCharArray())) == null)) {
            for (KeyEvent keyEvent : events) {
                sendKeySync(KeyEvent.changeTimeRepeat(keyEvent, SystemClock.uptimeMillis(), 0));
            }
        }
    }

    public void sendKeySync(KeyEvent event) {
        validateNotAppThread();
        long downTime = event.getDownTime();
        long eventTime = event.getEventTime();
        int source = event.getSource();
        if (source == 0) {
            source = 257;
        }
        if (eventTime == 0) {
            eventTime = SystemClock.uptimeMillis();
        }
        if (downTime == 0) {
            downTime = eventTime;
        }
        KeyEvent newEvent = new KeyEvent(event);
        newEvent.setTime(downTime, eventTime);
        newEvent.setSource(source);
        newEvent.setFlags(event.getFlags() | 8);
        InputManager.getInstance().injectInputEvent(newEvent, 2);
    }

    public void sendKeyDownUpSync(int key) {
        sendKeySync(new KeyEvent(0, key));
        sendKeySync(new KeyEvent(1, key));
    }

    public void sendCharacterSync(int keyCode) {
        sendKeySync(new KeyEvent(0, keyCode));
        sendKeySync(new KeyEvent(1, keyCode));
    }

    public void sendPointerSync(MotionEvent event) {
        validateNotAppThread();
        if ((event.getSource() & 2) == 0) {
            event.setSource(4098);
        }
        try {
            WindowManagerGlobal.getWindowManagerService().injectInputAfterTransactionsApplied(event, 2);
        } catch (RemoteException e) {
        }
    }

    public void sendTrackballEventSync(MotionEvent event) {
        validateNotAppThread();
        if ((event.getSource() & 4) == 0) {
            event.setSource(65540);
        }
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Application app = getFactory(context.getPackageName()).instantiateApplication(cl, className);
        app.attach(context);
        return app;
    }

    public static Application newApplication(Class<?> clazz, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Application app = (Application) clazz.newInstance();
        app.attach(context);
        return app;
    }

    public void callApplicationOnCreate(Application app) {
        if (Log.HWINFO) {
            Trace.traceBegin(64, "app.onCreate");
        }
        app.onCreate();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
    }

    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        Application application2;
        Activity activity = (Activity) clazz.newInstance();
        if (application == null) {
            application2 = new Application();
        } else {
            application2 = application;
        }
        activity.attach(context, null, this, token, 0, application2, intent, info, title, parent, id, (Activity.NonConfigurationInstances) lastNonConfigurationInstance, new Configuration(), null, null, null, null, null);
        return activity;
    }

    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return getFactory((intent == null || intent.getComponent() == null) ? null : intent.getComponent().getPackageName()).instantiateActivity(cl, className, intent);
    }

    private AppComponentFactory getFactory(String pkg) {
        if (pkg == null) {
            Log.e(TAG, "No pkg specified, disabling AppComponentFactory");
            return AppComponentFactory.DEFAULT;
        }
        ActivityThread activityThread = this.mThread;
        if (activityThread == null) {
            Log.e(TAG, "Uninitialized ActivityThread, likely app-created Instrumentation, disabling AppComponentFactory", new Throwable());
            return AppComponentFactory.DEFAULT;
        }
        LoadedApk apk = activityThread.peekPackageInfo(pkg, true);
        if (apk == null) {
            apk = this.mThread.getSystemContext().mPackageInfo;
        }
        return apk.getAppFactory();
    }

    private void prePerformCreate(Activity activity) {
        if (this.mWaitingActivities != null) {
            synchronized (this.mSync) {
                int N = this.mWaitingActivities.size();
                for (int i = 0; i < N; i++) {
                    ActivityWaiter aw = this.mWaitingActivities.get(i);
                    if (aw.intent.filterEquals(activity.getIntent())) {
                        aw.activity = activity;
                        this.mMessageQueue.addIdleHandler(new ActivityGoing(aw));
                    }
                }
            }
        }
    }

    private void postPerformCreate(Activity activity) {
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                for (int i = 0; i < N; i++) {
                    this.mActivityMonitors.get(i).match(activity, activity, activity.getIntent());
                }
            }
        }
    }

    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        prePerformCreate(activity);
        activity.performCreate(icicle);
        postPerformCreate(activity);
    }

    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        prePerformCreate(activity);
        activity.performCreate(icicle, persistentState);
        postPerformCreate(activity);
    }

    public void callActivityOnDestroy(Activity activity) {
        activity.performDestroy();
    }

    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        activity.performRestoreInstanceState(savedInstanceState);
    }

    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        activity.performRestoreInstanceState(savedInstanceState, persistentState);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle savedInstanceState) {
        activity.onPostCreate(savedInstanceState);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        activity.onPostCreate(savedInstanceState, persistentState);
    }

    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        activity.performNewIntent(intent);
    }

    @UnsupportedAppUsage
    public void callActivityOnNewIntent(Activity activity, ReferrerIntent intent) {
        String oldReferrer = activity.mReferrer;
        if (intent != null) {
            try {
                activity.mReferrer = intent.mReferrer;
            } catch (Throwable th) {
                activity.mReferrer = oldReferrer;
                throw th;
            }
        }
        callActivityOnNewIntent(activity, intent != null ? new Intent(intent) : null);
        activity.mReferrer = oldReferrer;
    }

    public void callActivityOnStart(Activity activity) {
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onStart");
        }
        activity.onStart();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
    }

    public void callActivityOnRestart(Activity activity) {
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onRestart");
        }
        activity.onRestart();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
    }

    public void callActivityOnResume(Activity activity) {
        activity.mResumed = true;
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onResume");
        }
        activity.onResume();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                for (int i = 0; i < N; i++) {
                    this.mActivityMonitors.get(i).match(activity, activity, activity.getIntent());
                }
            }
        }
    }

    public void callActivityOnStop(Activity activity) {
        if (Log.HWINFO) {
            Trace.traceBegin(64, "onStop");
        }
        activity.onStop();
        if (Log.HWINFO) {
            Trace.traceEnd(64);
        }
    }

    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        activity.performSaveInstanceState(outState);
    }

    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState, PersistableBundle outPersistentState) {
        activity.performSaveInstanceState(outState, outPersistentState);
    }

    public void callActivityOnPause(Activity activity) {
        activity.performPause();
    }

    public void callActivityOnUserLeaving(Activity activity) {
        activity.performUserLeaving();
    }

    @Deprecated
    public void startAllocCounting() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        Debug.resetAllCounts();
        Debug.startAllocCounting();
    }

    @Deprecated
    public void stopAllocCounting() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        Debug.stopAllocCounting();
    }

    private void addValue(String key, int value, Bundle results) {
        if (results.containsKey(key)) {
            List<Integer> list = results.getIntegerArrayList(key);
            if (list != null) {
                list.add(Integer.valueOf(value));
                return;
            }
            return;
        }
        ArrayList<Integer> list2 = new ArrayList<>();
        list2.add(Integer.valueOf(value));
        results.putIntegerArrayList(key, list2);
    }

    public Bundle getAllocCounts() {
        Bundle results = new Bundle();
        results.putLong(PerformanceCollector.METRIC_KEY_GLOBAL_ALLOC_COUNT, (long) Debug.getGlobalAllocCount());
        results.putLong(PerformanceCollector.METRIC_KEY_GLOBAL_ALLOC_SIZE, (long) Debug.getGlobalAllocSize());
        results.putLong(PerformanceCollector.METRIC_KEY_GLOBAL_FREED_COUNT, (long) Debug.getGlobalFreedCount());
        results.putLong(PerformanceCollector.METRIC_KEY_GLOBAL_FREED_SIZE, (long) Debug.getGlobalFreedSize());
        results.putLong(PerformanceCollector.METRIC_KEY_GC_INVOCATION_COUNT, (long) Debug.getGlobalGcInvocationCount());
        return results;
    }

    public Bundle getBinderCounts() {
        Bundle results = new Bundle();
        results.putLong(PerformanceCollector.METRIC_KEY_SENT_TRANSACTIONS, (long) Debug.getBinderSentTransactions());
        results.putLong(PerformanceCollector.METRIC_KEY_RECEIVED_TRANSACTIONS, (long) Debug.getBinderReceivedTransactions());
        return results;
    }

    public static final class ActivityResult {
        private final int mResultCode;
        private final Intent mResultData;

        public ActivityResult(int resultCode, Intent resultData) {
            this.mResultCode = resultCode;
            this.mResultData = resultData;
        }

        public int getResultCode() {
            return this.mResultCode;
        }

        public Intent getResultData() {
            return this.mResultData;
        }
    }

    @UnsupportedAppUsage
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        RemoteException e;
        String str;
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        ActivityResult activityResult = null;
        Uri referrer = target != null ? target.onProvideReferrer() : null;
        if (referrer != null) {
            intent.putExtra(Intent.EXTRA_REFERRER, referrer);
        }
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    ActivityMonitor am = this.mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return result;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            if (requestCode >= 0) {
                                activityResult = am.getResult();
                            }
                            return activityResult;
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            long start = SystemClock.uptimeMillis();
            IActivityTaskManager service = ActivityTaskManager.getService();
            String basePackageName = who.getBasePackageName();
            String resolveTypeIfNeeded = intent.resolveTypeIfNeeded(who.getContentResolver());
            if (target != null) {
                try {
                    str = target.mEmbeddedID;
                } catch (RemoteException e2) {
                    e = e2;
                    throw new RuntimeException("Failure from system", e);
                }
            } else {
                str = null;
            }
            try {
                int result2 = service.startActivity(whoThread, basePackageName, intent, resolveTypeIfNeeded, token, str, requestCode, 0, null, options);
                Jlog.printStartActivityInfo(intent, start, result2);
                checkStartActivityResult(result2, intent);
                return null;
            } catch (RemoteException e3) {
                e = e3;
                throw new RuntimeException("Failure from system", e);
            }
        } catch (RemoteException e4) {
            e = e4;
            throw new RuntimeException("Failure from system", e);
        }
    }

    @UnsupportedAppUsage
    public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options) {
        execStartActivitiesAsUser(who, contextThread, token, target, intents, options, who.getUserId());
    }

    @UnsupportedAppUsage
    public int execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options, int userId) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    ActivityMonitor am = this.mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intents[0]);
                    }
                    if (result != null) {
                        am.mHits++;
                        return -96;
                    } else if (am.match(who, null, intents[0])) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            return -96;
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
        try {
            String[] resolvedTypes = new String[intents.length];
            for (int i2 = 0; i2 < intents.length; i2++) {
                intents[i2].migrateExtraStreamToClipData();
                intents[i2].prepareToLeaveProcess(who);
                resolvedTypes[i2] = intents[i2].resolveTypeIfNeeded(who.getContentResolver());
            }
            int result2 = ActivityTaskManager.getService().startActivities(whoThread, who.getBasePackageName(), intents, resolvedTypes, token, options, userId);
            checkStartActivityResult(result2, intents[0]);
            return result2;
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    @UnsupportedAppUsage
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        ActivityResult activityResult = null;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    ActivityMonitor am = this.mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return result;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            if (requestCode >= 0) {
                                activityResult = am.getResult();
                            }
                            return activityResult;
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            long start = SystemClock.uptimeMillis();
            int result2 = ActivityTaskManager.getService().startActivity(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, target, requestCode, 0, null, options);
            Jlog.printStartActivityInfo(intent, start, result2);
            checkStartActivityResult(result2, intent);
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    @UnsupportedAppUsage
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String resultWho, Intent intent, int requestCode, Bundle options, UserHandle user) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        ActivityResult activityResult = null;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    ActivityMonitor am = this.mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return result;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            if (requestCode >= 0) {
                                activityResult = am.getResult();
                            }
                            return activityResult;
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            long start = SystemClock.uptimeMillis();
            int result2 = ActivityTaskManager.getService().startActivityAsUser(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, resultWho, requestCode, 0, null, options, user.getIdentifier());
            Jlog.printStartActivityInfo(intent, start, result2);
            checkStartActivityResult(result2, intent);
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    @UnsupportedAppUsage
    public ActivityResult execStartActivityAsCaller(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options, IBinder permissionToken, boolean ignoreTargetSecurity, int userId) {
        RemoteException e;
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        ActivityResult activityResult = null;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    ActivityMonitor am = this.mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return result;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            if (requestCode >= 0) {
                                activityResult = am.getResult();
                            }
                            return activityResult;
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            long start = SystemClock.uptimeMillis();
            try {
                int result2 = ActivityTaskManager.getService().startActivityAsCaller(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, target != null ? target.mEmbeddedID : null, requestCode, 0, null, options, permissionToken, ignoreTargetSecurity, userId);
                try {
                    Jlog.printStartActivityInfo(intent, start, result2);
                    checkStartActivityResult(result2, intent);
                    return null;
                } catch (RemoteException e2) {
                    e = e2;
                }
            } catch (RemoteException e3) {
                e = e3;
                throw new RuntimeException("Failure from system", e);
            }
        } catch (RemoteException e4) {
            e = e4;
            throw new RuntimeException("Failure from system", e);
        }
    }

    @UnsupportedAppUsage
    public void execStartActivityFromAppTask(Context who, IBinder contextThread, IAppTask appTask, Intent intent, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (true) {
                    if (i >= N) {
                        break;
                    }
                    ActivityMonitor am = this.mActivityMonitors.get(i);
                    ActivityResult result = null;
                    if (am.ignoreMatchingSpecificIntents()) {
                        result = am.onStartActivity(intent);
                    }
                    if (result != null) {
                        am.mHits++;
                        return;
                    } else if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            return;
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
        try {
            intent.migrateExtraStreamToClipData();
            intent.prepareToLeaveProcess(who);
            long start = SystemClock.uptimeMillis();
            int result2 = appTask.startActivity(whoThread.asBinder(), who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), options);
            Jlog.printStartActivityInfo(intent, start, result2);
            checkStartActivityResult(result2, intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    /* access modifiers changed from: package-private */
    public final void init(ActivityThread thread, Context instrContext, Context appContext, ComponentName component, IInstrumentationWatcher watcher, IUiAutomationConnection uiAutomationConnection) {
        this.mThread = thread;
        this.mThread.getLooper();
        this.mMessageQueue = Looper.myQueue();
        this.mInstrContext = instrContext;
        this.mAppContext = appContext;
        this.mComponent = component;
        this.mWatcher = watcher;
        this.mUiAutomationConnection = uiAutomationConnection;
    }

    /* access modifiers changed from: package-private */
    public final void basicInit(ActivityThread thread) {
        this.mThread = thread;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static void checkStartActivityResult(int res, Object intent) {
        if (ActivityManager.isStartResultFatalError(res)) {
            switch (res) {
                case -100:
                    throw new IllegalStateException("Cannot start voice activity on a hidden session");
                case ActivityManager.START_VOICE_NOT_ACTIVE_SESSION /* -99 */:
                    throw new IllegalStateException("Session calling startVoiceActivity does not match active session");
                case ActivityManager.START_NOT_CURRENT_USER_ACTIVITY /* -98 */:
                default:
                    throw new AndroidRuntimeException("Unknown error code " + res + " when starting " + intent);
                case ActivityManager.START_NOT_VOICE_COMPATIBLE /* -97 */:
                    throw new SecurityException("Starting under voice control not allowed for: " + intent);
                case ActivityManager.START_CANCELED /* -96 */:
                    throw new AndroidRuntimeException("Activity could not be started for " + intent);
                case ActivityManager.START_NOT_ACTIVITY /* -95 */:
                    throw new IllegalArgumentException("PendingIntent is not an activity");
                case ActivityManager.START_PERMISSION_DENIED /* -94 */:
                    throw new SecurityException("Not allowed to start activity " + intent);
                case ActivityManager.START_FORWARD_AND_REQUEST_CONFLICT /* -93 */:
                    throw new AndroidRuntimeException("FORWARD_RESULT_FLAG used while also requesting a result");
                case ActivityManager.START_CLASS_NOT_FOUND /* -92 */:
                case ActivityManager.START_INTENT_NOT_RESOLVED /* -91 */:
                    if (!(intent instanceof Intent) || ((Intent) intent).getComponent() == null) {
                        throw new ActivityNotFoundException("No Activity found to handle " + intent);
                    }
                    throw new ActivityNotFoundException("Unable to find explicit activity class " + ((Intent) intent).getComponent().toShortString() + "; have you declared this activity in your AndroidManifest.xml?");
                case ActivityManager.START_ASSISTANT_HIDDEN_SESSION /* -90 */:
                    throw new IllegalStateException("Cannot start assistant activity on a hidden session");
                case ActivityManager.START_ASSISTANT_NOT_ACTIVE_SESSION /* -89 */:
                    throw new IllegalStateException("Session calling startAssistantActivity does not match active session");
            }
        }
    }

    private final void validateNotAppThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("This method can not be called from the main application thread");
        }
    }

    public UiAutomation getUiAutomation() {
        return getUiAutomation(0);
    }

    public UiAutomation getUiAutomation(int flags) {
        UiAutomation uiAutomation = this.mUiAutomation;
        boolean mustCreateNewAutomation = uiAutomation == null || uiAutomation.isDestroyed();
        if (this.mUiAutomationConnection == null) {
            return null;
        }
        if (!mustCreateNewAutomation && this.mUiAutomation.getFlags() == flags) {
            return this.mUiAutomation;
        }
        if (mustCreateNewAutomation) {
            this.mUiAutomation = new UiAutomation(getTargetContext().getMainLooper(), this.mUiAutomationConnection);
        } else {
            this.mUiAutomation.disconnect();
        }
        this.mUiAutomation.connect(flags);
        return this.mUiAutomation;
    }

    public TestLooperManager acquireLooperManager(Looper looper) {
        checkInstrumenting("acquireLooperManager");
        return new TestLooperManager(looper);
    }

    private final class InstrumentationThread extends Thread {
        public InstrumentationThread(String name) {
            super(name);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            try {
                Process.setThreadPriority(-8);
            } catch (RuntimeException e) {
                Log.w(Instrumentation.TAG, "Exception setting priority of instrumentation thread " + Process.myTid(), e);
            }
            if (Instrumentation.this.mAutomaticPerformanceSnapshots) {
                Instrumentation.this.startPerformanceSnapshot();
            }
            Instrumentation.this.onStart();
        }
    }

    /* access modifiers changed from: private */
    public static final class EmptyRunnable implements Runnable {
        private EmptyRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
        }
    }

    /* access modifiers changed from: private */
    public static final class SyncRunnable implements Runnable {
        private boolean mComplete;
        private final Runnable mTarget;

        public SyncRunnable(Runnable target) {
            this.mTarget = target;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mTarget.run();
            synchronized (this) {
                this.mComplete = true;
                notifyAll();
            }
        }

        public void waitForComplete() {
            synchronized (this) {
                while (!this.mComplete) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class ActivityWaiter {
        public Activity activity;
        public final Intent intent;

        public ActivityWaiter(Intent _intent) {
            this.intent = _intent;
        }
    }

    /* access modifiers changed from: private */
    public final class ActivityGoing implements MessageQueue.IdleHandler {
        private final ActivityWaiter mWaiter;

        public ActivityGoing(ActivityWaiter waiter) {
            this.mWaiter = waiter;
        }

        @Override // android.os.MessageQueue.IdleHandler
        public final boolean queueIdle() {
            synchronized (Instrumentation.this.mSync) {
                Instrumentation.this.mWaitingActivities.remove(this.mWaiter);
                Instrumentation.this.mSync.notifyAll();
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static final class Idler implements MessageQueue.IdleHandler {
        private final Runnable mCallback;
        private boolean mIdle = false;

        public Idler(Runnable callback) {
            this.mCallback = callback;
        }

        @Override // android.os.MessageQueue.IdleHandler
        public final boolean queueIdle() {
            Runnable runnable = this.mCallback;
            if (runnable != null) {
                runnable.run();
            }
            synchronized (this) {
                this.mIdle = true;
                notifyAll();
            }
            return false;
        }

        public void waitForIdle() {
            synchronized (this) {
                while (!this.mIdle) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
