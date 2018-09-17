package com.android.server.location;

import android.content.Context;
import android.hardware.location.ActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareClient;
import android.hardware.location.IActivityRecognitionHardwareWatcher;
import android.hardware.location.IActivityRecognitionHardwareWatcher.Stub;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.ServiceWatcher;

public class ActivityRecognitionProxy {
    private static final String TAG = "ActivityRecognitionProxy";
    private final ActivityRecognitionHardware mInstance;
    private final boolean mIsSupported;
    private final ServiceWatcher mServiceWatcher;

    private ActivityRecognitionProxy(Context context, Handler handler, boolean activityRecognitionHardwareIsSupported, ActivityRecognitionHardware activityRecognitionHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        this.mIsSupported = activityRecognitionHardwareIsSupported;
        this.mInstance = activityRecognitionHardware;
        Context context2 = context;
        this.mServiceWatcher = new ServiceWatcher(context2, TAG, "com.android.location.service.ActivityRecognitionProvider", overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId, new Runnable() {
            public void run() {
                ActivityRecognitionProxy.this.bindProvider();
            }
        }, handler);
    }

    public static ActivityRecognitionProxy createAndBind(Context context, Handler handler, boolean activityRecognitionHardwareIsSupported, ActivityRecognitionHardware activityRecognitionHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        ActivityRecognitionProxy activityRecognitionProxy = new ActivityRecognitionProxy(context, handler, activityRecognitionHardwareIsSupported, activityRecognitionHardware, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId);
        if (activityRecognitionProxy.mServiceWatcher.start()) {
            return activityRecognitionProxy;
        }
        Log.e(TAG, "ServiceWatcher could not start.");
        return null;
    }

    private void bindProvider() {
        IBinder binder = this.mServiceWatcher.getBinder();
        if (binder == null) {
            Log.e(TAG, "Null binder found on connection.");
            return;
        }
        try {
            String descriptor = binder.getInterfaceDescriptor();
            if (IActivityRecognitionHardwareWatcher.class.getCanonicalName().equals(descriptor)) {
                IActivityRecognitionHardwareWatcher watcher = Stub.asInterface(binder);
                if (watcher == null) {
                    Log.e(TAG, "No watcher found on connection.");
                } else if (this.mInstance == null) {
                    Log.d(TAG, "AR HW instance not available, binding will be a no-op.");
                } else {
                    try {
                        watcher.onInstanceChanged(this.mInstance);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error delivering hardware interface to watcher.", e);
                    }
                }
            } else if (IActivityRecognitionHardwareClient.class.getCanonicalName().equals(descriptor)) {
                IActivityRecognitionHardwareClient client = IActivityRecognitionHardwareClient.Stub.asInterface(binder);
                if (client == null) {
                    Log.e(TAG, "No client found on connection.");
                    return;
                }
                try {
                    client.onAvailabilityChanged(this.mIsSupported, this.mInstance);
                } catch (RemoteException e2) {
                    Log.e(TAG, "Error delivering hardware interface to client.", e2);
                }
            } else {
                Log.e(TAG, "Invalid descriptor found on connection: " + descriptor);
            }
        } catch (RemoteException e22) {
            Log.e(TAG, "Unable to get interface descriptor.", e22);
        }
    }
}
