package android.app;

import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.MemoryLeakMonitorManager;
import android.os.RemoteException;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class Service extends ContextWrapper implements ComponentCallbacks2 {
    public static final int START_CONTINUATION_MASK = 15;
    public static final int START_FLAG_REDELIVERY = 1;
    public static final int START_FLAG_RETRY = 2;
    public static final int START_NOT_STICKY = 2;
    public static final int START_REDELIVER_INTENT = 3;
    public static final int START_STICKY = 1;
    public static final int START_STICKY_COMPATIBILITY = 0;
    public static final int START_TASK_REMOVED_COMPLETE = 1000;
    public static final int STOP_FOREGROUND_DETACH = 2;
    public static final int STOP_FOREGROUND_REMOVE = 1;
    private static final String TAG = "Service";
    private IActivityManager mActivityManager = null;
    private Application mApplication = null;
    private String mClassName = null;
    private boolean mStartCompatibility = false;
    private ActivityThread mThread = null;
    private IBinder mToken = null;

    public abstract IBinder onBind(Intent intent);

    public Service() {
        super(null);
    }

    public final Application getApplication() {
        return this.mApplication;
    }

    public void onCreate() {
    }

    @Deprecated
    public void onStart(Intent intent, int startId) {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return this.mStartCompatibility ? 0 : 1;
    }

    public void onDestroy() {
        MemoryLeakMonitorManager.watchMemoryLeak(this);
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
    }

    public void onTrimMemory(int level) {
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void onRebind(Intent intent) {
    }

    public void onTaskRemoved(Intent rootIntent) {
    }

    public final void stopSelf() {
        stopSelf(-1);
    }

    public final void stopSelf(int startId) {
        if (this.mActivityManager != null) {
            try {
                this.mActivityManager.stopServiceToken(new ComponentName((Context) this, this.mClassName), this.mToken, startId);
            } catch (RemoteException e) {
            }
        }
    }

    public final boolean stopSelfResult(int startId) {
        if (this.mActivityManager == null) {
            return false;
        }
        try {
            return this.mActivityManager.stopServiceToken(new ComponentName((Context) this, this.mClassName), this.mToken, startId);
        } catch (RemoteException e) {
            return false;
        }
    }

    @Deprecated
    public final void setForeground(boolean isForeground) {
        Log.w(TAG, "setForeground: ignoring old API call on " + getClass().getName());
    }

    public final void startForeground(int id, Notification notification) {
        try {
            this.mActivityManager.setServiceForeground(new ComponentName((Context) this, this.mClassName), this.mToken, id, notification, 0);
        } catch (RemoteException e) {
        }
    }

    public final void stopForeground(boolean removeNotification) {
        stopForeground(removeNotification ? 1 : 0);
    }

    public final void stopForeground(int flags) {
        try {
            this.mActivityManager.setServiceForeground(new ComponentName((Context) this, this.mClassName), this.mToken, 0, null, flags);
        } catch (RemoteException e) {
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("nothing to dump");
    }

    public final void attach(Context context, ActivityThread thread, String className, IBinder token, Application application, Object activityManager) {
        attachBaseContext(context);
        this.mThread = thread;
        this.mClassName = className;
        this.mToken = token;
        this.mApplication = application;
        this.mActivityManager = (IActivityManager) activityManager;
        this.mStartCompatibility = getApplicationInfo().targetSdkVersion < 5;
    }

    public final void detachAndCleanUp() {
        this.mToken = null;
    }

    final String getClassName() {
        return this.mClassName;
    }
}
