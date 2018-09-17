package com.android.server.location;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.GeoFenceParams;
import android.location.IGeoFenceListener.Stub;
import android.location.IGeoFencer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class HwGeoFencerProxy extends HwGeoFencerBase {
    private static final boolean LOGV_ENABLED = true;
    private static final String TAG = "HwGeoFencerProxy";
    private static HwGeoFencerProxy mGeoFencerProxy;
    private final Context mContext;
    private IGeoFencer mGeoFencer;
    private final Intent mIntent;
    private final Stub mListener = new Stub() {
        public void geoFenceExpired(PendingIntent intent) throws RemoteException {
            HwGeoFencerProxy.this.logv("geoFenceExpired - " + intent);
            HwGeoFencerProxy.this.remove(intent, true);
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (this) {
                HwGeoFencerProxy.this.mGeoFencer = IGeoFencer.Stub.asInterface(service);
                notifyAll();
            }
            Log.v(HwGeoFencerProxy.TAG, "onServiceConnected: mGeoFencer - " + HwGeoFencerProxy.this.mGeoFencer);
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (this) {
                HwGeoFencerProxy.this.mGeoFencer = null;
            }
            Log.v(HwGeoFencerProxy.TAG, "onServiceDisconnected");
        }
    };

    public static HwGeoFencerProxy getGeoFencerProxy(Context context, String serviceName) {
        if (mGeoFencerProxy == null) {
            mGeoFencerProxy = new HwGeoFencerProxy(context, serviceName);
        }
        return mGeoFencerProxy;
    }

    private HwGeoFencerProxy(Context context, String serviceName) {
        this.mContext = context;
        this.mIntent = new Intent();
        this.mIntent.setPackage(serviceName);
        this.mContext.bindService(this.mIntent, this.mServiceConnection, 21);
    }

    public void removeCaller(int uid) {
        super.removeCaller(uid);
        if (this.mGeoFencer != null) {
            try {
                this.mGeoFencer.clearGeoFenceUser(uid);
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        Log.e(TAG, "removeCaller - mGeoFencer is null");
    }

    private boolean ensureGeoFencer() {
        if (this.mGeoFencer == null) {
            try {
                synchronized (this.mServiceConnection) {
                    logv("waiting...");
                    this.mServiceConnection.wait(AppHibernateCst.DELAY_ONE_MINS);
                    logv("woke up!!!");
                }
                if (this.mGeoFencer == null) {
                    Log.w(TAG, "Timed out. No GeoFencer connection");
                    return false;
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while waiting for GeoFencer");
                return false;
            }
        }
        return true;
    }

    protected boolean start(GeoFenceParams geofence) {
        if (ensureGeoFencer()) {
            try {
                return this.mGeoFencer.setGeoFence(this.mListener, geofence);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    protected boolean stop(PendingIntent intent) {
        if (ensureGeoFencer()) {
            try {
                this.mGeoFencer.clearGeoFence(this.mListener, intent);
                return true;
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    private void logv(String s) {
        Log.v(TAG, s);
    }
}
