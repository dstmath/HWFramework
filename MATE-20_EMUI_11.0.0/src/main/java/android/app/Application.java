package android.app;

import android.annotation.UnsupportedAppUsage;
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
    @UnsupportedAppUsage
    private ArrayList<ActivityLifecycleCallbacks> mActivityLifecycleCallbacks = new ArrayList<>();
    @UnsupportedAppUsage
    private ArrayList<OnProvideAssistDataListener> mAssistCallbacks = null;
    @UnsupportedAppUsage
    private ArrayList<ComponentCallbacks> mComponentCallbacks = new ArrayList<>();
    @UnsupportedAppUsage
    public LoadedApk mLoadedApk;

    public interface OnProvideAssistDataListener {
        void onProvideAssistData(Activity activity, Bundle bundle);
    }

    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle bundle);

        void onActivityDestroyed(Activity activity);

        void onActivityPaused(Activity activity);

        void onActivityResumed(Activity activity);

        void onActivitySaveInstanceState(Activity activity, Bundle bundle);

        void onActivityStarted(Activity activity);

        void onActivityStopped(Activity activity);

        default void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
        }

        default void onActivityPostCreated(Activity activity, Bundle savedInstanceState) {
        }

        default void onActivityPreStarted(Activity activity) {
        }

        default void onActivityPostStarted(Activity activity) {
        }

        default void onActivityPreResumed(Activity activity) {
        }

        default void onActivityPostResumed(Activity activity) {
        }

        default void onActivityPrePaused(Activity activity) {
        }

        default void onActivityPostPaused(Activity activity) {
        }

        default void onActivityPreStopped(Activity activity) {
        }

        default void onActivityPostStopped(Activity activity) {
        }

        default void onActivityPreSaveInstanceState(Activity activity, Bundle outState) {
        }

        default void onActivityPostSaveInstanceState(Activity activity, Bundle outState) {
        }

        default void onActivityPreDestroyed(Activity activity) {
        }

        default void onActivityPostDestroyed(Activity activity) {
        }
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

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        Object[] callbacks = collectComponentCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ComponentCallbacks) obj).onConfigurationChanged(newConfig);
            }
        }
    }

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
        Object[] callbacks = collectComponentCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ComponentCallbacks) obj).onLowMemory();
            }
        }
    }

    @Override // android.content.ComponentCallbacks2
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

    @Override // android.content.Context
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        synchronized (this.mComponentCallbacks) {
            this.mComponentCallbacks.add(callback);
        }
    }

    @Override // android.content.Context
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
    @UnsupportedAppUsage
    public final void attach(Context context) {
        attachBaseContext(context);
        this.mLoadedApk = ContextImpl.getImpl(context).mPackageInfo;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPreCreated(Activity activity, Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPreCreated(activity, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityCreated(Activity activity, Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityCreated(activity, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostCreated(Activity activity, Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostCreated(activity, savedInstanceState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPreStarted(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPreStarted(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityStarted(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityStarted(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostStarted(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostStarted(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPreResumed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPreResumed(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityResumed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityResumed(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostResumed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostResumed(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPrePaused(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPrePaused(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPaused(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPaused(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostPaused(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostPaused(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPreStopped(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPreStopped(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityStopped(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityStopped(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostStopped(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostStopped(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPreSaveInstanceState(Activity activity, Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPreSaveInstanceState(activity, outState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivitySaveInstanceState(Activity activity, Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivitySaveInstanceState(activity, outState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostSaveInstanceState(Activity activity, Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostSaveInstanceState(activity, outState);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPreDestroyed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPreDestroyed(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityDestroyed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityDestroyed(activity);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void dispatchActivityPostDestroyed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((ActivityLifecycleCallbacks) obj).onActivityPostDestroyed(activity);
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

    @UnsupportedAppUsage
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
    public void dispatchOnProvideAssistData(Activity activity, Bundle data) {
        Object[] callbacks;
        synchronized (this) {
            if (this.mAssistCallbacks != null) {
                callbacks = this.mAssistCallbacks.toArray();
            } else {
                return;
            }
        }
        if (callbacks != null) {
            for (Object obj : callbacks) {
                ((OnProvideAssistDataListener) obj).onProvideAssistData(activity, data);
            }
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public AutofillManager.AutofillClient getAutofillClient() {
        Activity activity;
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
            if (record != null && (activity = record.activity) != null && activity.getWindow().getDecorView().hasFocus()) {
                if (Helper.sVerbose) {
                    Log.v(TAG, "getAutofillClient(): found activity for " + this + ": " + activity);
                }
                return activity;
            }
        }
        if (Helper.sVerbose) {
            Log.v(TAG, "getAutofillClient(): none of the " + activityCount + " activities on " + this + " have focus");
        }
        return null;
    }
}
