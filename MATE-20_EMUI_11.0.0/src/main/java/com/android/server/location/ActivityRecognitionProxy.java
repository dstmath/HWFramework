package com.android.server.location;

import android.content.Context;
import android.hardware.location.ActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareClient;
import android.hardware.location.IActivityRecognitionHardwareWatcher;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.FgThread;
import com.android.server.ServiceWatcher;

public class ActivityRecognitionProxy {
    private static final String TAG = "ActivityRecognitionProxy";
    private final ActivityRecognitionHardware mInstance;
    private final boolean mIsSupported;
    private final ServiceWatcher mServiceWatcher;

    public static ActivityRecognitionProxy createAndBind(Context context, boolean activityRecognitionHardwareIsSupported, ActivityRecognitionHardware activityRecognitionHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        ActivityRecognitionProxy activityRecognitionProxy = new ActivityRecognitionProxy(context, activityRecognitionHardwareIsSupported, activityRecognitionHardware, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId);
        if (activityRecognitionProxy.mServiceWatcher.start()) {
            return activityRecognitionProxy;
        }
        return null;
    }

    private ActivityRecognitionProxy(Context context, boolean activityRecognitionHardwareIsSupported, ActivityRecognitionHardware activityRecognitionHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        this.mIsSupported = activityRecognitionHardwareIsSupported;
        this.mInstance = activityRecognitionHardware;
        this.mServiceWatcher = new ServiceWatcher(context, TAG, "com.android.location.service.ActivityRecognitionProvider", overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId, FgThread.getHandler()) {
            /* class com.android.server.location.ActivityRecognitionProxy.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // com.android.server.ServiceWatcher
            public void onBind() {
                runOnBinder(new ServiceWatcher.BinderRunner() {
                    /* class com.android.server.location.$$Lambda$ActivityRecognitionProxy$1$d2hvjpSk2zwb2N0mtEiubZ0jBE */

                    @Override // com.android.server.ServiceWatcher.BinderRunner
                    public final void run(IBinder iBinder) {
                        ActivityRecognitionProxy.this.initializeService(iBinder);
                    }
                });
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void initializeService(IBinder binder) {
        try {
            String descriptor = binder.getInterfaceDescriptor();
            if (IActivityRecognitionHardwareWatcher.class.getCanonicalName().equals(descriptor)) {
                IActivityRecognitionHardwareWatcher watcher = IActivityRecognitionHardwareWatcher.Stub.asInterface(binder);
                if (this.mInstance != null) {
                    watcher.onInstanceChanged(this.mInstance);
                }
            } else if (IActivityRecognitionHardwareClient.class.getCanonicalName().equals(descriptor)) {
                IActivityRecognitionHardwareClient.Stub.asInterface(binder).onAvailabilityChanged(this.mIsSupported, this.mInstance);
            } else {
                Log.e(TAG, "Invalid descriptor found on connection: " + descriptor);
            }
        } catch (RemoteException e) {
            Log.w(TAG, e);
        }
    }
}
