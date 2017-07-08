package android.app;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorAdditionalInfo;
import android.hardware.input.InputManager;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.net.wifi.AnqpInformationElement;
import android.os.BatteryStats.HistoryItem;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.Parcelable;
import android.os.PerformanceCollector;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.rms.AppAssociate;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech;
import android.util.AndroidRuntimeException;
import android.util.Jlog;
import android.util.Log;
import android.view.IWindowManager.Stub;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.internal.content.ReferrerIntent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Instrumentation {
    public static final String REPORT_KEY_IDENTIFIER = "id";
    public static final String REPORT_KEY_STREAMRESULT = "stream";
    private static final String TAG = "Instrumentation";
    private List<ActivityMonitor> mActivityMonitors;
    private Context mAppContext;
    private boolean mAutomaticPerformanceSnapshots;
    private ComponentName mComponent;
    private Context mInstrContext;
    private MessageQueue mMessageQueue;
    private Bundle mPerfMetrics;
    private PerformanceCollector mPerformanceCollector;
    private Thread mRunner;
    private final Object mSync;
    private ActivityThread mThread;
    private UiAutomation mUiAutomation;
    private IUiAutomationConnection mUiAutomationConnection;
    private List<ActivityWaiter> mWaitingActivities;
    private IInstrumentationWatcher mWatcher;

    /* renamed from: android.app.Instrumentation.1ContextMenuRunnable */
    class AnonymousClass1ContextMenuRunnable implements Runnable {
        private final Activity activity;
        private final int flags;
        private final int identifier;
        boolean returnValue;

        public AnonymousClass1ContextMenuRunnable(Activity _activity, int _identifier, int _flags) {
            this.activity = _activity;
            this.identifier = _identifier;
            this.flags = _flags;
        }

        public void run() {
            this.returnValue = this.activity.getWindow().performContextMenuIdentifierAction(this.identifier, this.flags);
        }
    }

    /* renamed from: android.app.Instrumentation.1MenuRunnable */
    class AnonymousClass1MenuRunnable implements Runnable {
        private final Activity activity;
        private final int flags;
        private final int identifier;
        boolean returnValue;

        public AnonymousClass1MenuRunnable(Activity _activity, int _identifier, int _flags) {
            this.activity = _activity;
            this.identifier = _identifier;
            this.flags = _flags;
        }

        public void run() {
            this.returnValue = this.activity.getWindow().performPanelIdentifierAction(0, this.identifier, this.flags);
        }
    }

    private final class ActivityGoing implements IdleHandler {
        private final ActivityWaiter mWaiter;

        public ActivityGoing(ActivityWaiter waiter) {
            this.mWaiter = waiter;
        }

        public final boolean queueIdle() {
            synchronized (Instrumentation.this.mSync) {
                Instrumentation.this.mWaitingActivities.remove(this.mWaiter);
                Instrumentation.this.mSync.notifyAll();
            }
            return false;
        }
    }

    public static class ActivityMonitor {
        private final boolean mBlock;
        private final String mClass;
        int mHits;
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
        }

        public ActivityMonitor(String cls, ActivityResult result, boolean block) {
            this.mHits = 0;
            this.mLastActivity = null;
            this.mWhich = null;
            this.mClass = cls;
            this.mResult = result;
            this.mBlock = block;
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final Activity waitForActivity() {
            Activity res;
            synchronized (this) {
                while (true) {
                    if (this.mLastActivity == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    } else {
                        res = this.mLastActivity;
                        this.mLastActivity = null;
                    }
                }
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

        final boolean match(Context who, Activity activity, Intent intent) {
            synchronized (this) {
                if (this.mWhich == null || this.mWhich.match(who.getContentResolver(), intent, true, Instrumentation.TAG) >= 0) {
                    if (this.mClass != null) {
                        Object cls = null;
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
                return false;
            }
        }
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

    private static final class ActivityWaiter {
        public Activity activity;
        public final Intent intent;

        public ActivityWaiter(Intent _intent) {
            this.intent = _intent;
        }
    }

    private static final class EmptyRunnable implements Runnable {
        private EmptyRunnable() {
        }

        public void run() {
        }
    }

    private static final class Idler implements IdleHandler {
        private final Runnable mCallback;
        private boolean mIdle;

        public Idler(Runnable callback) {
            this.mCallback = callback;
            this.mIdle = false;
        }

        public final boolean queueIdle() {
            if (this.mCallback != null) {
                this.mCallback.run();
            }
            synchronized (this) {
                this.mIdle = true;
                notifyAll();
            }
            return false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void waitForIdle() {
            synchronized (this) {
                while (true) {
                    if (this.mIdle) {
                    } else {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    private final class InstrumentationThread extends Thread {
        public InstrumentationThread(String name) {
            super(name);
        }

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

    private static final class SyncRunnable implements Runnable {
        private boolean mComplete;
        private final Runnable mTarget;

        public SyncRunnable(Runnable target) {
            this.mTarget = target;
        }

        public void run() {
            this.mTarget.run();
            synchronized (this) {
                this.mComplete = true;
                notifyAll();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void waitForComplete() {
            synchronized (this) {
                while (true) {
                    if (this.mComplete) {
                    } else {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    public Instrumentation() {
        this.mSync = new Object();
        this.mThread = null;
        this.mMessageQueue = null;
        this.mAutomaticPerformanceSnapshots = false;
        this.mPerfMetrics = new Bundle();
    }

    public void onCreate(Bundle arguments) {
    }

    public void start() {
        if (this.mRunner != null) {
            throw new RuntimeException("Instrumentation already started");
        }
        this.mRunner = new InstrumentationThread("Instr: " + getClass().getName());
        this.mRunner.start();
    }

    public void onStart() {
    }

    public boolean onException(Object obj, Throwable e) {
        return false;
    }

    public void sendStatus(int resultCode, Bundle results) {
        if (this.mWatcher != null) {
            try {
                this.mWatcher.instrumentationStatus(this.mComponent, resultCode, results);
            } catch (RemoteException e) {
                this.mWatcher = null;
            }
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
        if (!(this.mUiAutomation == null || this.mUiAutomation.isDestroyed())) {
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

    public boolean isProfiling() {
        return this.mThread.isProfiling();
    }

    public void startProfiling() {
        if (this.mThread.isProfiling()) {
            File file = new File(this.mThread.getProfileFilePath());
            file.getParentFile().mkdirs();
            Debug.startMethodTracing(file.toString(), HistoryItem.STATE_SENSOR_ON_FLAG);
        }
    }

    public void stopProfiling() {
        if (this.mThread.isProfiling()) {
            Debug.stopMethodTracing();
        }
    }

    public void setInTouchMode(boolean inTouch) {
        try {
            Stub.asInterface(ServiceManager.getService(AppAssociate.ASSOC_WINDOW)).setInTouchMode(inTouch);
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

    public void runOnMainSync(Runnable runner) {
        validateNotAppThread();
        SyncRunnable sr = new SyncRunnable(runner);
        this.mThread.getHandler().post(sr);
        sr.waitForComplete();
    }

    public Activity startActivitySync(Intent intent) {
        Throwable th;
        validateNotAppThread();
        synchronized (this.mSync) {
            try {
                Intent intent2 = new Intent(intent);
                try {
                    ActivityInfo ai = intent2.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
                    if (ai == null) {
                        throw new RuntimeException("Unable to resolve activity for: " + intent2);
                    }
                    String myProc = this.mThread.getProcessName();
                    if (ai.processName.equals(myProc)) {
                        intent2.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
                        ActivityWaiter aw = new ActivityWaiter(intent2);
                        if (this.mWaitingActivities == null) {
                            this.mWaitingActivities = new ArrayList();
                        }
                        this.mWaitingActivities.add(aw);
                        getTargetContext().startActivity(intent2);
                        do {
                            try {
                                this.mSync.wait();
                            } catch (InterruptedException e) {
                            }
                        } while (this.mWaitingActivities.contains(aw));
                        Activity activity = aw.activity;
                        return activity;
                    }
                    throw new RuntimeException("Intent in process " + myProc + " resolved to different process " + ai.processName + ": " + intent2);
                } catch (Throwable th2) {
                    th = th2;
                    intent = intent2;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
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
        AnonymousClass1MenuRunnable mr = new AnonymousClass1MenuRunnable(targetActivity, id, flag);
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
            AnonymousClass1ContextMenuRunnable cmr = new AnonymousClass1ContextMenuRunnable(targetActivity, id, flag);
            runOnMainSync(cmr);
            return cmr.returnValue;
        } catch (InterruptedException e) {
            Log.e(TAG, "Could not sleep for long press timeout", e);
            return false;
        }
    }

    public void sendStringSync(String text) {
        if (text != null) {
            KeyEvent[] events = KeyCharacterMap.load(-1).getEvents(text.toCharArray());
            if (events != null) {
                for (KeyEvent changeTimeRepeat : events) {
                    sendKeySync(KeyEvent.changeTimeRepeat(changeTimeRepeat, SystemClock.uptimeMillis(), 0));
                }
            }
        }
    }

    public void sendKeySync(KeyEvent event) {
        validateNotAppThread();
        long downTime = event.getDownTime();
        long eventTime = event.getEventTime();
        int action = event.getAction();
        int code = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int metaState = event.getMetaState();
        int deviceId = event.getDeviceId();
        int scancode = event.getScanCode();
        int source = event.getSource();
        int flags = event.getFlags();
        if (source == 0) {
            source = AnqpInformationElement.ANQP_CAPABILITY_LIST;
        }
        if (eventTime == 0) {
            eventTime = SystemClock.uptimeMillis();
        }
        if (downTime == 0) {
            downTime = eventTime;
        }
        InputManager.getInstance().injectInputEvent(new KeyEvent(downTime, eventTime, action, code, repeatCount, metaState, deviceId, scancode, flags | 8, source), 2);
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
            event.setSource(MtpConstants.OPERATION_OPEN_SESSION);
        }
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    public void sendTrackballEventSync(MotionEvent event) {
        validateNotAppThread();
        if ((event.getSource() & 4) == 0) {
            event.setSource(SensorAdditionalInfo.TYPE_SAMPLING);
        }
        InputManager.getInstance().injectInputEvent(event, 2);
    }

    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return newApplication(cl.loadClass(className), context);
    }

    public static Application newApplication(Class<?> clazz, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Application app = (Application) clazz.newInstance();
        app.attach(context);
        return app;
    }

    public void callApplicationOnCreate(Application app) {
        Trace.traceBegin(64, "app.onCreate");
        app.onCreate();
        Trace.traceEnd(64);
    }

    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        Activity activity = (Activity) clazz.newInstance();
        activity.attach(context, null, this, token, 0, application, intent, info, title, parent, id, (NonConfigurationInstances) lastNonConfigurationInstance, new Configuration(), null, null, null);
        return activity;
    }

    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (Activity) cl.loadClass(className).newInstance();
    }

    private void prePerformCreate(Activity activity) {
        if (this.mWaitingActivities != null) {
            synchronized (this.mSync) {
                int N = this.mWaitingActivities.size();
                for (int i = 0; i < N; i++) {
                    ActivityWaiter aw = (ActivityWaiter) this.mWaitingActivities.get(i);
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
                    ((ActivityMonitor) this.mActivityMonitors.get(i)).match(activity, activity, activity.getIntent());
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
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                for (int i = 0; i < N; i++) {
                    ((ActivityMonitor) this.mActivityMonitors.get(i)).match(activity, activity, activity.getIntent());
                }
            }
        }
    }

    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        activity.performRestoreInstanceState(savedInstanceState);
    }

    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        activity.performRestoreInstanceState(savedInstanceState, persistentState);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        activity.onPostCreate(icicle);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        activity.onPostCreate(icicle, persistentState);
    }

    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        activity.onNewIntent(intent);
    }

    public void callActivityOnNewIntent(Activity activity, ReferrerIntent intent) {
        Intent intent2 = null;
        String oldReferrer = activity.mReferrer;
        if (intent != null) {
            try {
                activity.mReferrer = intent.mReferrer;
            } catch (Throwable th) {
                activity.mReferrer = oldReferrer;
            }
        }
        if (intent != null) {
            intent2 = new Intent((Intent) intent);
        }
        callActivityOnNewIntent(activity, intent2);
        activity.mReferrer = oldReferrer;
    }

    public void callActivityOnStart(Activity activity) {
        Trace.traceBegin(64, "onStart");
        activity.onStart();
        Trace.traceEnd(64);
    }

    public void callActivityOnRestart(Activity activity) {
        Trace.traceBegin(64, "onRestart");
        activity.onRestart();
        Trace.traceEnd(64);
    }

    public void callActivityOnResume(Activity activity) {
        activity.mResumed = true;
        Trace.traceBegin(64, "onResume");
        activity.onResume();
        Trace.traceEnd(64);
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                for (int i = 0; i < N; i++) {
                    ((ActivityMonitor) this.mActivityMonitors.get(i)).match(activity, activity, activity.getIntent());
                }
            }
        }
    }

    public void callActivityOnStop(Activity activity) {
        Trace.traceBegin(64, "onStop");
        activity.onStop();
        Trace.traceEnd(64);
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
        ArrayList<Integer> list2 = new ArrayList();
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

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        Uri referrer = target != null ? target.onProvideReferrer() : null;
        if (referrer != null) {
            intent.putExtra(Intent.EXTRA_REFERRER, (Parcelable) referrer);
        }
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (i < N) {
                    ActivityMonitor am = (ActivityMonitor) this.mActivityMonitors.get(i);
                    if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            ActivityResult result;
                            if (requestCode >= 0) {
                                result = am.getResult();
                            } else {
                                result = null;
                            }
                            return result;
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
            if (Jlog.isPerfTest()) {
                Jlog.i(2022, "whopid=" + Process.myPid() + "&whopkg=" + (who != null ? who.getPackageName() : "unknow") + "&" + Intent.toPkgClsString(intent));
            }
            checkStartActivityResult(ActivityManagerNative.getDefault().startActivity(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, target != null ? target.mEmbeddedID : null, requestCode, 0, null, options), intent);
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options) {
        execStartActivitiesAsUser(who, contextThread, token, target, intents, options, UserHandle.myUserId());
    }

    public void execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target, Intent[] intents, Bundle options, int userId) {
        int i;
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                i = 0;
                while (i < N) {
                    ActivityMonitor am = (ActivityMonitor) this.mActivityMonitors.get(i);
                    if (am.match(who, null, intents[0])) {
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
            String[] resolvedTypes = new String[intents.length];
            for (i = 0; i < intents.length; i++) {
                intents[i].migrateExtraStreamToClipData();
                intents[i].prepareToLeaveProcess(who);
                resolvedTypes[i] = intents[i].resolveTypeIfNeeded(who.getContentResolver());
            }
            checkStartActivityResult(ActivityManagerNative.getDefault().startActivities(whoThread, who.getBasePackageName(), intents, resolvedTypes, token, options, userId), intents[0]);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (i < N) {
                    ActivityMonitor am = (ActivityMonitor) this.mActivityMonitors.get(i);
                    if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            ActivityResult result = requestCode >= 0 ? am.getResult() : null;
                            return result;
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
            if (Jlog.isPerfTest()) {
                Jlog.i(2022, "whopid=" + Process.myPid() + "&whopkg=" + (who != null ? who.getPackageName() : "unknow") + "&" + Intent.toPkgClsString(intent));
            }
            checkStartActivityResult(ActivityManagerNative.getDefault().startActivity(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, target, requestCode, 0, null, options), intent);
            return null;
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options, UserHandle user) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (i < N) {
                    ActivityMonitor am = (ActivityMonitor) this.mActivityMonitors.get(i);
                    if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            ActivityResult result = requestCode >= 0 ? am.getResult() : null;
                            return result;
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
            if (Jlog.isPerfTest()) {
                Jlog.i(2022, "whopid=" + Process.myPid() + "&whopkg=" + (who != null ? who.getPackageName() : "unknow") + "&" + Intent.toPkgClsString(intent));
            }
            checkStartActivityResult(ActivityManagerNative.getDefault().startActivityAsUser(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, target != null ? target.mEmbeddedID : null, requestCode, 0, null, options, user.getIdentifier()), intent);
            return null;
        } catch (Throwable e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    public ActivityResult execStartActivityAsCaller(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options, boolean ignoreTargetSecurity, int userId) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (i < N) {
                    ActivityMonitor am = (ActivityMonitor) this.mActivityMonitors.get(i);
                    if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            ActivityResult result = requestCode >= 0 ? am.getResult() : null;
                            return result;
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
            if (Jlog.isPerfTest()) {
                Jlog.i(2022, "whopid=" + Process.myPid() + "&whopkg=" + (who != null ? who.getPackageName() : "unknow") + "&" + Intent.toPkgClsString(intent));
            }
            checkStartActivityResult(ActivityManagerNative.getDefault().startActivityAsCaller(whoThread, who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), token, target != null ? target.mEmbeddedID : null, requestCode, 0, null, options, ignoreTargetSecurity, userId), intent);
            return null;
        } catch (Throwable e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    public void execStartActivityFromAppTask(Context who, IBinder contextThread, IAppTask appTask, Intent intent, Bundle options) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (this.mActivityMonitors != null) {
            synchronized (this.mSync) {
                int N = this.mActivityMonitors.size();
                int i = 0;
                while (i < N) {
                    ActivityMonitor am = (ActivityMonitor) this.mActivityMonitors.get(i);
                    if (am.match(who, null, intent)) {
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
            checkStartActivityResult(appTask.startActivity(whoThread.asBinder(), who.getBasePackageName(), intent, intent.resolveTypeIfNeeded(who.getContentResolver()), options), intent);
        } catch (RemoteException e) {
            throw new RuntimeException("Failure from system", e);
        }
    }

    final void init(ActivityThread thread, Context instrContext, Context appContext, ComponentName component, IInstrumentationWatcher watcher, IUiAutomationConnection uiAutomationConnection) {
        this.mThread = thread;
        this.mThread.getLooper();
        this.mMessageQueue = Looper.myQueue();
        this.mInstrContext = instrContext;
        this.mAppContext = appContext;
        this.mComponent = component;
        this.mWatcher = watcher;
        this.mUiAutomationConnection = uiAutomationConnection;
    }

    public static void checkStartActivityResult(int res, Object intent) {
        if (res < 0) {
            switch (res) {
                case KeymasterDefs.KM_ERROR_UNSUPPORTED_PADDING_MODE /*-10*/:
                    throw new IllegalStateException("Cannot start voice activity on a hidden session");
                case TextToSpeech.ERROR_NOT_INSTALLED_YET /*-9*/:
                    throw new IllegalStateException("Session calling startVoiceActivity does not match active session");
                case TextToSpeech.ERROR_NETWORK_TIMEOUT /*-7*/:
                    throw new SecurityException("Starting under voice control not allowed for: " + intent);
                case TextToSpeech.ERROR_NETWORK /*-6*/:
                    throw new AndroidRuntimeException("Activity could not be started for " + intent);
                case TextToSpeech.ERROR_OUTPUT /*-5*/:
                    throw new IllegalArgumentException("PendingIntent is not an activity");
                case TextToSpeech.ERROR_SERVICE /*-4*/:
                    throw new SecurityException("Not allowed to start activity " + intent);
                case TextToSpeech.ERROR_SYNTHESIS /*-3*/:
                    throw new AndroidRuntimeException("FORWARD_RESULT_FLAG used while also requesting a result");
                case TextToSpeech.STOPPED /*-2*/:
                case TextToSpeech.LANG_MISSING_DATA /*-1*/:
                    if (!(intent instanceof Intent) || ((Intent) intent).getComponent() == null) {
                        throw new ActivityNotFoundException("No Activity found to handle " + intent);
                    }
                    throw new ActivityNotFoundException("Unable to find explicit activity class " + ((Intent) intent).getComponent().toShortString() + "; have you declared this activity in your AndroidManifest.xml?");
                default:
                    throw new AndroidRuntimeException("Unknown error code " + res + " when starting " + intent);
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
        boolean mustCreateNewAutomation = this.mUiAutomation != null ? this.mUiAutomation.isDestroyed() : true;
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
}
