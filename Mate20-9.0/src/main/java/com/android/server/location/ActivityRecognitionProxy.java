package com.android.server.location;

import android.content.Context;
import android.hardware.location.ActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareClient;
import android.hardware.location.IActivityRecognitionHardwareWatcher;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.ServiceWatcher;

public class ActivityRecognitionProxy {
    private static final String TAG = "ActivityRecognitionProxy";
    /* access modifiers changed from: private */
    public final ActivityRecognitionHardware mInstance;
    /* access modifiers changed from: private */
    public final boolean mIsSupported;
    private final ServiceWatcher mServiceWatcher;

    private ActivityRecognitionProxy(Context context, Handler handler, boolean activityRecognitionHardwareIsSupported, ActivityRecognitionHardware activityRecognitionHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        this.mIsSupported = activityRecognitionHardwareIsSupported;
        this.mInstance = activityRecognitionHardware;
        ServiceWatcher serviceWatcher = new ServiceWatcher(context, TAG, "com.android.location.service.ActivityRecognitionProvider", overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId, new Runnable() {
            public void run() {
                ActivityRecognitionProxy.this.bindProvider();
            }
        }, handler);
        this.mServiceWatcher = serviceWatcher;
    }

    public static ActivityRecognitionProxy createAndBind(Context context, Handler handler, boolean activityRecognitionHardwareIsSupported, ActivityRecognitionHardware activityRecognitionHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        ActivityRecognitionProxy activityRecognitionProxy = new ActivityRecognitionProxy(context, handler, activityRecognitionHardwareIsSupported, activityRecognitionHardware, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId);
        if (activityRecognitionProxy.mServiceWatcher.start()) {
            return activityRecognitionProxy;
        }
        Log.e(TAG, "ServiceWatcher could not start.");
        return null;
    }

    /* access modifiers changed from: private */
    public void bindProvider() {
        if (!this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    String descriptor = binder.getInterfaceDescriptor();
                    if (IActivityRecognitionHardwareWatcher.class.getCanonicalName().equals(descriptor)) {
                        IActivityRecognitionHardwareWatcher watcher = IActivityRecognitionHardwareWatcher.Stub.asInterface(binder);
                        if (watcher == null) {
                            Log.e(ActivityRecognitionProxy.TAG, "No watcher found on connection.");
                        } else if (ActivityRecognitionProxy.this.mInstance == null) {
                            Log.d(ActivityRecognitionProxy.TAG, "AR HW instance not available, binding will be a no-op.");
                        } else {
                            try {
                                watcher.onInstanceChanged(ActivityRecognitionProxy.this.mInstance);
                            } catch (RemoteException e) {
                                Log.e(ActivityRecognitionProxy.TAG, "Error delivering hardware interface to watcher.", e);
                            }
                        }
                    } else if (IActivityRecognitionHardwareClient.class.getCanonicalName().equals(descriptor)) {
                        IActivityRecognitionHardwareClient client = IActivityRecognitionHardwareClient.Stub.asInterface(binder);
                        if (client == null) {
                            Log.e(ActivityRecognitionProxy.TAG, "No client found on connection.");
                            return;
                        }
                        try {
                            client.onAvailabilityChanged(ActivityRecognitionProxy.this.mIsSupported, ActivityRecognitionProxy.this.mInstance);
                        } catch (RemoteException e2) {
                            Log.e(ActivityRecognitionProxy.TAG, "Error delivering hardware interface to client.", e2);
                        }
                    } else {
                        Log.e(ActivityRecognitionProxy.TAG, "Invalid descriptor found on connection: " + descriptor);
                    }
                } catch (RemoteException e3) {
                    Log.e(ActivityRecognitionProxy.TAG, "Unable to get interface descriptor.", e3);
                }
            }
        })) {
            Log.e(TAG, "Null binder found on connection.");
        }
    }
}
