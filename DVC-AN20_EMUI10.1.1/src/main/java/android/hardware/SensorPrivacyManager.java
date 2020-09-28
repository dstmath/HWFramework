package android.hardware;

import android.content.Context;
import android.hardware.ISensorPrivacyListener;
import android.hardware.ISensorPrivacyManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import com.android.internal.annotations.GuardedBy;

public final class SensorPrivacyManager {
    @GuardedBy({"sInstanceLock"})
    private static SensorPrivacyManager sInstance;
    private static final Object sInstanceLock = new Object();
    private final Context mContext;
    private final ArrayMap<OnSensorPrivacyChangedListener, ISensorPrivacyListener> mListeners = new ArrayMap<>();
    private final ISensorPrivacyManager mService;

    public interface OnSensorPrivacyChangedListener {
        void onSensorPrivacyChanged(boolean z);
    }

    private SensorPrivacyManager(Context context, ISensorPrivacyManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public static SensorPrivacyManager getInstance(Context context) {
        SensorPrivacyManager sensorPrivacyManager;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                try {
                    sInstance = new SensorPrivacyManager(context, ISensorPrivacyManager.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.SENSOR_PRIVACY_SERVICE)));
                } catch (ServiceManager.ServiceNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
            sensorPrivacyManager = sInstance;
        }
        return sensorPrivacyManager;
    }

    public void setSensorPrivacy(boolean enable) {
        try {
            this.mService.setSensorPrivacy(enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addSensorPrivacyListener(final OnSensorPrivacyChangedListener listener) {
        synchronized (this.mListeners) {
            ISensorPrivacyListener iListener = this.mListeners.get(listener);
            if (iListener == null) {
                iListener = new ISensorPrivacyListener.Stub() {
                    /* class android.hardware.SensorPrivacyManager.AnonymousClass1 */

                    @Override // android.hardware.ISensorPrivacyListener
                    public void onSensorPrivacyChanged(boolean enabled) {
                        listener.onSensorPrivacyChanged(enabled);
                    }
                };
                this.mListeners.put(listener, iListener);
            }
            try {
                this.mService.addSensorPrivacyListener(iListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void removeSensorPrivacyListener(OnSensorPrivacyChangedListener listener) {
        synchronized (this.mListeners) {
            ISensorPrivacyListener iListener = this.mListeners.get(listener);
            if (iListener != null) {
                this.mListeners.remove(iListener);
                try {
                    this.mService.removeSensorPrivacyListener(iListener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }
    }

    public boolean isSensorPrivacyEnabled() {
        try {
            return this.mService.isSensorPrivacyEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
