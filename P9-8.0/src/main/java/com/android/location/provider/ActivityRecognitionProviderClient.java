package com.android.location.provider;

import android.hardware.location.IActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareClient.Stub;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public abstract class ActivityRecognitionProviderClient {
    private static final String TAG = "ArProviderClient";
    private Stub mClient = new Stub() {
        public void onAvailabilityChanged(boolean isSupported, IActivityRecognitionHardware instance) {
            int callingUid = Binder.getCallingUid();
            if (callingUid != 1000) {
                Log.d(ActivityRecognitionProviderClient.TAG, "Ignoring calls from non-system server. Uid: " + callingUid);
                return;
            }
            ActivityRecognitionProvider provider;
            if (isSupported) {
                try {
                    provider = new ActivityRecognitionProvider(instance);
                } catch (RemoteException e) {
                    Log.e(ActivityRecognitionProviderClient.TAG, "Error creating Hardware Activity-Recognition Provider.", e);
                    return;
                }
            }
            provider = null;
            ActivityRecognitionProviderClient.this.onProviderChanged(isSupported, provider);
        }
    };

    public abstract void onProviderChanged(boolean z, ActivityRecognitionProvider activityRecognitionProvider);

    protected ActivityRecognitionProviderClient() {
    }

    public IBinder getBinder() {
        return this.mClient;
    }
}
