package android.app;

import android.app.ActivityThread;
import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.MemoryLeakMonitorManager;
import android.util.Log;
import android.view.autofill.AutofillManager;
import android.view.autofill.Helper;
import java.util.ArrayList;

public class Application extends ContextWrapper implements ComponentCallbacks2 {
    private static final String TAG = "Application";
    private ArrayList<ActivityLifecycleCallbacks> mActivityLifecycleCallbacks = new ArrayList<>();
    private ArrayList<OnProvideAssistDataListener> mAssistCallbacks = null;
    private ArrayList<ComponentCallbacks> mComponentCallbacks = new ArrayList<>();
    public LoadedApk mLoadedApk;

    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle bundle);

        void onActivityDestroyed(Activity activity);

        void onActivityPaused(Activity activity);

        void onActivityResumed(Activity activity);

        void onActivitySaveInstanceState(Activity activity, Bundle bundle);

        void onActivityStarted(Activity activity);

        void onActivityStopped(Activity activity);
    }

    public interface OnProvideAssistDataListener {
        void onProvideAssistData(Activity activity, Bundle bundle);
    }

    public Application() {
        super(null);
    }

    public void onCreate() {
    }

    public void installMemoryLeakMonitor() {
        MemoryLeakMonitorManager.installMemoryLeakMonitor(this);
    }

    public void onTerminate() {
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Object[] callbacks = collectComponentCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ComponentCallbacks) obj).onConfigurationChanged(newConfig);
            }
        }
    }

    public void onLowMemory() {
        Object[] callbacks = collectComponentCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ComponentCallbacks) obj).onLowMemory();
            }
        }
    }

    public void onTrimMemory(int level) {
        Object[] callbacks = collectComponentCallbacks();
        if (callbacks != null) {
            for (Object c : callbacks) {
                if (c instanceof ComponentCallbacks2) {
                    ((ComponentCallbacks2) c).onTrimMemory(level);
                }
            }
        }
    }

    public void registerComponentCallbacks(ComponentCallbacks callback) {
        synchronized (this.mComponentCallbacks) {
            this.mComponentCallbacks.add(callback);
        }
    }

    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        synchronized (this.mComponentCallbacks) {
            this.mComponentCallbacks.remove(callback);
        }
    }

    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        synchronized (this.mActivityLifecycleCallbacks) {
            this.mActivityLifecycleCallbacks.add(callback);
        }
    }

    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        synchronized (this.mActivityLifecycleCallbacks) {
            this.mActivityLifecycleCallbacks.remove(callback);
        }
    }

    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        synchronized (this) {
            if (this.mAssistCallbacks == null) {
                this.mAssistCallbacks = new ArrayList<>();
            }
            this.mAssistCallbacks.add(callback);
        }
    }

    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        synchronized (this) {
            if (this.mAssistCallbacks != null) {
                this.mAssistCallbacks.remove(callback);
            }
        }
    }

    public static String getProcessName() {
        return ActivityThread.currentProcessName();
    }

    /* access modifiers changed from: package-private */
    public final void attach(Context context) {
        attachBaseContext(context);
        this.mLoadedApk = ContextImpl.getImpl(context).mPackageInfo;
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityCreated(Activity activity, Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityCreated(activity, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityStarted(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityStarted(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityResumed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityResumed(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityPaused(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPaused(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityStopped(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityStopped(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivitySaveInstanceState(Activity activity, Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivitySaveInstanceState(activity, outState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchActivityDestroyed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityDestroyed(activity);
            }
        }
    }

    private Object[] collectComponentCallbacks() {
        Object[] callbacks = null;
        synchronized (this.mComponentCallbacks) {
            if (this.mComponentCallbacks.size() > 0) {
                callbacks = this.mComponentCallbacks.toArray();
            }
        }
        return callbacks;
    }

    private Object[] collectActivityLifecycleCallbacks() {
        Object[] callbacks = null;
        synchronized (this.mActivityLifecycleCallbacks) {
            if (this.mActivityLifecycleCallbacks.size() > 0) {
                callbacks = this.mActivityLifecycleCallbacks.toArray();
            }
        }
        return callbacks;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        if (r1 >= r0.length) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0014, code lost:
        ((android.app.Application.OnProvideAssistDataListener) r0[r1]).onProvideAssistData(r4, r5);
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000e, code lost:
        if (r0 == null) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        r1 = 0;
     */
    public void dispatchOnProvideAssistData(Activity activity, Bundle data) {
        synchronized (this) {
            if (this.mAssistCallbacks != null) {
                Object[] callbacks = this.mAssistCallbacks.toArray();
            }
        }
    }

    public AutofillManager.AutofillClient getAutofillClient() {
        AutofillManager.AutofillClient client = super.getAutofillClient();
        if (client != null) {
            return client;
        }
        if (Helper.sVerbose) {
            Log.v(TAG, "getAutofillClient(): null on super, trying to find activity thread");
        }
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        if (activityThread == null) {
            return null;
        }
        int activityCount = activityThread.mActivities.size();
        for (int i = 0; i < activityCount; i++) {
            ActivityThread.ActivityClientRecord record = activityThread.mActivities.valueAt(i);
            if (record != null) {
                Activity activity = record.activity;
                if (activity != null && activity.getWindow().getDecorView().hasFocus()) {
                    if (Helper.sVerbose) {
                        Log.v(TAG, "getAutofillClient(): found activity for " + this + ": " + activity);
                    }
                    return activity;
                }
            }
        }
        if (Helper.sVerbose != 0) {
            Log.v(TAG, "getAutofillClient(): none of the " + activityCount + " activities on " + this + " have focus");
        }
        return null;
    }
}
