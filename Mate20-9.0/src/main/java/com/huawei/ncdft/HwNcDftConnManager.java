package com.huawei.ncdft;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.ncdft.INcDft;
import java.util.List;

public class HwNcDftConnManager {
    private static final int BIND_FAIL = 1;
    private static final int BIND_SUCC = 0;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "HwNcDftConnManager";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    protected INcDft iNcDft = null;
    /* access modifiers changed from: private */
    public CallBack mCallerCallback = null;
    private Context mContext;
    protected ServiceConnection sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(HwNcDftConnManager.TAG, "service is connected");
            HwNcDftConnManager.this.iNcDft = INcDft.Stub.asInterface(service);
            if (HwNcDftConnManager.this.mCallerCallback != null) {
                HwNcDftConnManager.this.mCallerCallback.serviceConnected();
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(HwNcDftConnManager.TAG, "service is disconnceted");
            HwNcDftConnManager.this.iNcDft = null;
        }
    };

    public interface CallBack {
        void serviceConnected();
    }

    public HwNcDftConnManager(Context context) {
        this.mContext = context;
        bindToService(this.mContext);
    }

    public HwNcDftConnManager(Context context, CallBack callBack) {
        this.mCallerCallback = callBack;
        this.mContext = context.getApplicationContext();
        bindToService(this.mContext);
    }

    public boolean bindToService(Context context) {
        Log.i(TAG, "start to bind to NcDftService");
        if (context == null) {
            Log.e(TAG, "mContext == null");
            return false;
        }
        Intent serviceIntent = new Intent("com.huawei.ncdft.iNcDft");
        serviceIntent.setClassName("com.huawei.hiview", "com.huawei.ncdft.NcDftService");
        try {
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "startService is exception");
        }
        Intent tent = new Intent("com.huawei.ncdft.INcDft");
        tent.setClassName("com.huawei.hiview", "com.huawei.ncdft.NcDftService");
        try {
            return context.bindService(tent, this.sConn, 1);
        } catch (Exception e2) {
            return false;
        }
    }

    private void unbindToService() {
        if (this.mContext == null || this.sConn == null) {
            Log.e(TAG, "mContext == null || sConn == null");
        } else {
            this.mContext.unbindService(this.sConn);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        unbindToService();
        super.finalize();
    }

    public boolean triggerUpload(int domain, int event, int errorCode) {
        try {
            if (this.iNcDft == null && !bindToService(this.mContext)) {
                Log.d(TAG, "triggerUpload, fail to bind service");
                return false;
            } else if (this.iNcDft != null) {
                return this.iNcDft.triggerUpload(domain, event, errorCode);
            } else {
                Log.e(TAG, "triggerUpload, iNcdfT is null !");
                return false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "triggerUpload, connect failed! exception : " + e);
            return false;
        }
    }

    public int reportToDft(int domain, int ncEventID, List<String> list) {
        try {
            if (this.iNcDft == null && !bindToService(this.mContext)) {
                Log.d(TAG, "reportToDft, fail to bind service");
                return 1;
            } else if (this.iNcDft != null) {
                return this.iNcDft.notifyNcDftEvent(domain, ncEventID, list);
            } else {
                Log.e(TAG, "reportToDft, iNcdfT is null !");
                return 1;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "reportToDft, connect failed! exception : " + e);
            return 1;
        }
    }

    public int reportToDft(int domain, int ncEventID, Bundle data) {
        try {
            if (this.iNcDft == null && !bindToService(this.mContext)) {
                Log.d(TAG, "reportToDft, fail to bind service");
                return 1;
            } else if (this.iNcDft != null) {
                return this.iNcDft.notifyNcDftBundleEvent(domain, ncEventID, data);
            } else {
                Log.e(TAG, "reportToDft, iNcdfT is null !");
                return 1;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "reportToDft, connect failed! exception : " + e);
            return 1;
        }
    }

    public int reportNetworkInfo(int domain, int event, NetworkInfo info) {
        try {
            if (this.iNcDft == null && !bindToService(this.mContext)) {
                Log.d(TAG, "reportNetworkInfo, fail to bind service");
                return 1;
            } else if (this.iNcDft != null) {
                return this.iNcDft.reportNetworkInfo(domain, event, info);
            } else {
                Log.e(TAG, "reportNetworkInfo, iNcdfT is null !");
                return 1;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "reportGnssApkName, connect failed! exception : " + e);
            return 1;
        }
    }

    public int reportGnssLocation(int domain, int event, Location info, long time, String provider) {
        try {
            if (this.iNcDft == null && !bindToService(this.mContext)) {
                Log.d(TAG, "reportGnssLocation, fail to bind service");
                return 1;
            } else if (this.iNcDft != null) {
                return this.iNcDft.reportGnssLocation(domain, event, info, time, provider);
            } else {
                Log.e(TAG, "reportGnssLocation, iNcdfT is null !");
                return 1;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "reportGnssLocation, connect failed! exception : " + e);
            return 1;
        }
    }

    public int reportGnssSvStatus(int domain, int event, int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        try {
            if (this.iNcDft == null && !bindToService(this.mContext)) {
                Log.d(TAG, "reportGnssSvStatus, fail to bind service");
                return 1;
            } else if (this.iNcDft != null) {
                return this.iNcDft.reportGnssSvStatus(domain, event, svCount, svs, snrs, svElevations, svAzimuths);
            } else {
                Log.e(TAG, "reportGnssSvStatus, iNcdfT is null !");
                return 1;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "reportGnssSvStatus, connect failed! exception : " + e);
            return 1;
        }
    }

    public static boolean isCommercialUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 1);
        if (3 == userType || 4 == userType) {
            return false;
        }
        return true;
    }

    public boolean isOverseaCommercialUser() {
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 6) {
            return true;
        }
        return false;
    }
}
