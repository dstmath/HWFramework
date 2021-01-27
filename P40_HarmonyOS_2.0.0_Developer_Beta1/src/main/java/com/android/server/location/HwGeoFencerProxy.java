package com.android.server.location;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.GeoFenceParams;
import android.location.IGeoFenceListener;
import android.location.IGeoFencer;
import android.os.IBinder;
import android.os.RemoteException;

public class HwGeoFencerProxy extends HwGeoFencerBase {
    private static final int CONNECT_TIMEOUT = 60000;
    private static final boolean LOGV_ENABLED = false;
    private static final String TAG = "HwGeoFencerProxy";
    private static HwGeoFencerProxy sGeoFencerProxy;
    private final Context mContext;
    private IGeoFencer mGeoFencer;
    private final Intent mIntent;
    private final IGeoFenceListener.Stub mListener = new IGeoFenceListener.Stub() {
        /* class com.android.server.location.HwGeoFencerProxy.AnonymousClass2 */

        public void geoFenceExpired(PendingIntent intent) throws RemoteException {
            LBSLog.d(HwGeoFencerProxy.TAG, false, "geoFenceExpired - %{private}s", intent);
            HwGeoFencerProxy.this.remove(intent, true);
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.server.location.HwGeoFencerProxy.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (this) {
                HwGeoFencerProxy.this.mGeoFencer = IGeoFencer.Stub.asInterface(service);
                notifyAll();
            }
            LBSLog.i(HwGeoFencerProxy.TAG, false, "onServiceConnected: mGeoFencer - %{public}s", HwGeoFencerProxy.this.mGeoFencer);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            synchronized (this) {
                HwGeoFencerProxy.this.mGeoFencer = null;
            }
            LBSLog.i(HwGeoFencerProxy.TAG, false, "onServiceDisconnected", new Object[0]);
        }
    };

    public static HwGeoFencerProxy getGeoFencerProxy(Context context, String serviceName) {
        if (sGeoFencerProxy == null) {
            sGeoFencerProxy = new HwGeoFencerProxy(context, serviceName);
        }
        return sGeoFencerProxy;
    }

    private HwGeoFencerProxy(Context context, String serviceName) {
        this.mContext = context;
        this.mIntent = new Intent();
        this.mIntent.setPackage(serviceName);
        this.mContext.bindService(this.mIntent, this.mServiceConnection, 21);
    }

    @Override // com.android.server.location.HwGeoFencerBase
    public void removeCaller(int uid) {
        super.removeCaller(uid);
        IGeoFencer iGeoFencer = this.mGeoFencer;
        if (iGeoFencer != null) {
            try {
                iGeoFencer.clearGeoFenceUser(uid);
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "removeCaller catch RemoteException", new Object[0]);
            }
        } else {
            LBSLog.e(TAG, false, "removeCaller - mGeoFencer is null", new Object[0]);
        }
    }

    private boolean ensureGeoFencer() {
        if (this.mGeoFencer != null) {
            return true;
        }
        try {
            synchronized (this.mServiceConnection) {
                logv("waiting...");
                this.mServiceConnection.wait(60000);
                logv("woke up!!!");
            }
            if (this.mGeoFencer != null) {
                return true;
            }
            LBSLog.w(TAG, false, "Timed out. No GeoFencer connection", new Object[0]);
            return false;
        } catch (InterruptedException e) {
            LBSLog.w(TAG, false, "Interrupted while waiting for GeoFencer", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.HwGeoFencerBase
    public boolean start(GeoFenceParams geofence) {
        if (ensureGeoFencer()) {
            try {
                return this.mGeoFencer.setGeoFence(this.mListener, geofence);
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "start catch RemoteException", new Object[0]);
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.location.HwGeoFencerBase
    public boolean stop(PendingIntent intent) {
        if (ensureGeoFencer()) {
            try {
                this.mGeoFencer.clearGeoFence(this.mListener, intent);
                return true;
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "stop catch RemoteException", new Object[0]);
            }
        }
        return false;
    }

    private void logv(String s) {
    }
}
