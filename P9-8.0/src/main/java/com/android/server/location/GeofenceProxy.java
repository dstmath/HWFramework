package com.android.server.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.location.GeofenceHardwareService;
import android.hardware.location.IGeofenceHardware;
import android.hardware.location.IGeofenceHardware.Stub;
import android.location.IFusedGeofenceHardware;
import android.location.IGeofenceProvider;
import android.location.IGpsGeofenceHardware;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.ServiceWatcher;

public final class GeofenceProxy {
    private static final int GEOFENCE_GPS_HARDWARE_CONNECTED = 4;
    private static final int GEOFENCE_GPS_HARDWARE_DISCONNECTED = 5;
    private static final int GEOFENCE_HARDWARE_CONNECTED = 2;
    private static final int GEOFENCE_HARDWARE_DISCONNECTED = 3;
    private static final int GEOFENCE_PROVIDER_CONNECTED = 1;
    private static final String SERVICE_ACTION = "com.android.location.service.GeofenceProvider";
    private static final String TAG = "GeofenceProxy";
    private final Context mContext;
    private final IFusedGeofenceHardware mFusedGeofenceHardware;
    private IGeofenceHardware mGeofenceHardware;
    private final IGpsGeofenceHardware mGpsGeofenceHardware;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Object -get2;
            switch (msg.what) {
                case 1:
                    -get2 = GeofenceProxy.this.mLock;
                    synchronized (-get2) {
                        if (GeofenceProxy.this.mGeofenceHardware != null) {
                            GeofenceProxy.this.setGeofenceHardwareInProviderLocked();
                            break;
                        }
                    }
                    break;
                case 2:
                    -get2 = GeofenceProxy.this.mLock;
                    synchronized (-get2) {
                        if (GeofenceProxy.this.mGeofenceHardware != null) {
                            GeofenceProxy.this.setGpsGeofenceLocked();
                            GeofenceProxy.this.setFusedGeofenceLocked();
                            GeofenceProxy.this.setGeofenceHardwareInProviderLocked();
                            break;
                        }
                    }
                    break;
                case 3:
                    -get2 = GeofenceProxy.this.mLock;
                    synchronized (-get2) {
                        if (GeofenceProxy.this.mGeofenceHardware == null) {
                            GeofenceProxy.this.setGeofenceHardwareInProviderLocked();
                            break;
                        }
                    }
                    break;
                default:
                    return;
            }
        }
    };
    private final Object mLock = new Object();
    private Runnable mRunnable = new Runnable() {
        public void run() {
            GeofenceProxy.this.mHandler.sendEmptyMessage(1);
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (GeofenceProxy.this.mLock) {
                GeofenceProxy.this.mGeofenceHardware = Stub.asInterface(service);
                GeofenceProxy.this.mHandler.sendEmptyMessage(2);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (GeofenceProxy.this.mLock) {
                GeofenceProxy.this.mGeofenceHardware = null;
                GeofenceProxy.this.mHandler.sendEmptyMessage(3);
            }
        }
    };
    private final ServiceWatcher mServiceWatcher;

    public static GeofenceProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler, IGpsGeofenceHardware gpsGeofence, IFusedGeofenceHardware fusedGeofenceHardware) {
        GeofenceProxy proxy = new GeofenceProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler, gpsGeofence, fusedGeofenceHardware);
        if (proxy.bindGeofenceProvider()) {
            return proxy;
        }
        return null;
    }

    private GeofenceProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler, IGpsGeofenceHardware gpsGeofence, IFusedGeofenceHardware fusedGeofenceHardware) {
        this.mContext = context;
        this.mServiceWatcher = new ServiceWatcher(context, TAG, SERVICE_ACTION, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, this.mRunnable, handler);
        this.mGpsGeofenceHardware = gpsGeofence;
        this.mFusedGeofenceHardware = fusedGeofenceHardware;
        bindHardwareGeofence();
    }

    private boolean bindGeofenceProvider() {
        return this.mServiceWatcher.start();
    }

    private void bindHardwareGeofence() {
        this.mContext.bindServiceAsUser(new Intent(this.mContext, GeofenceHardwareService.class), this.mServiceConnection, 1, UserHandle.SYSTEM);
    }

    private void setGeofenceHardwareInProviderLocked() {
        try {
            IGeofenceProvider provider = IGeofenceProvider.Stub.asInterface(this.mServiceWatcher.getBinder());
            if (provider != null) {
                provider.setGeofenceHardware(this.mGeofenceHardware);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Remote Exception: setGeofenceHardwareInProviderLocked: " + e);
        }
    }

    private void setGpsGeofenceLocked() {
        try {
            if (this.mGpsGeofenceHardware != null) {
                this.mGeofenceHardware.setGpsGeofenceHardware(this.mGpsGeofenceHardware);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error while connecting to GeofenceHardwareService");
        }
    }

    private void setFusedGeofenceLocked() {
        try {
            this.mGeofenceHardware.setFusedGeofenceHardware(this.mFusedGeofenceHardware);
        } catch (RemoteException e) {
            Log.e(TAG, "Error while connecting to GeofenceHardwareService");
        }
    }
}
