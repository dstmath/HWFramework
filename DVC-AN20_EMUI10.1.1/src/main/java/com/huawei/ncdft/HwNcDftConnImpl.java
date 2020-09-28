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
import android.util.wifi.HwHiLog;
import com.huawei.ncdft.INcDft;
import com.huawei.uikit.effect.BuildConfig;
import java.util.List;

public class HwNcDftConnImpl {
    private static final String NCDFT_ACTION = "com.huawei.ncdft.INcDft";
    private static final String NCDFT_CLASS = "com.huawei.ncdft.NcDftService";
    private static final String NCDFT_PACKAGE = "com.huawei.hiview";
    private static final String TAG = "HwNcDftConnImpl";
    private static HwNcDftConnImpl sInstance;
    private INcDft mClient;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class com.huawei.ncdft.HwNcDftConnImpl.AnonymousClass1 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            HwHiLog.i(HwNcDftConnImpl.TAG, false, "service is connected", new Object[0]);
            HwNcDftConnImpl.this.updateINcDft(INcDft.Stub.asInterface(service));
        }

        public void onServiceDisconnected(ComponentName arg0) {
            HwHiLog.i(HwNcDftConnImpl.TAG, false, "service is disconnceted", new Object[0]);
            HwNcDftConnImpl.this.updateINcDft(null);
        }
    };
    private Context mContext;
    private boolean mIsBindingSuccess;

    private HwNcDftConnImpl(Context context) {
        this.mContext = context;
        this.mIsBindingSuccess = false;
        bindToService();
    }

    public static HwNcDftConnImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (HwNcDftConnImpl.class) {
                if (sInstance == null) {
                    sInstance = new HwNcDftConnImpl(context);
                }
            }
        }
        return sInstance;
    }

    private synchronized void bindToService() {
        if (this.mIsBindingSuccess) {
            HwHiLog.i(TAG, false, "bindToService ready", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "bindToService start", new Object[0]);
        Intent intent = new Intent(NCDFT_ACTION);
        intent.setClassName(NCDFT_PACKAGE, NCDFT_CLASS);
        try {
            this.mIsBindingSuccess = this.mContext.bindService(intent, this.mConnection, 1);
        } catch (SecurityException e) {
            HwHiLog.e(TAG, false, "bindService fail", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void updateINcDft(INcDft ncdft) {
        this.mClient = ncdft;
    }

    /* access modifiers changed from: package-private */
    public synchronized int reportToDft(int domain, int event, List<String> list) {
        int ret;
        ret = 0;
        if (this.mClient == null) {
            bindToService();
        }
        if (this.mClient != null) {
            try {
                ret = this.mClient.notifyNcDftEvent(domain, event, list);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "reportToDft fail", new Object[0]);
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public synchronized int reportToDft(int domain, int event, Bundle data) {
        int ret;
        ret = 0;
        if (this.mClient == null) {
            bindToService();
        }
        if (this.mClient != null) {
            try {
                ret = this.mClient.notifyNcDftBundleEvent(domain, event, data);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "reportToDft fail", new Object[0]);
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public synchronized String getFromDft(int domain, List<String> list) {
        String value;
        value = BuildConfig.FLAVOR;
        if (this.mClient == null) {
            bindToService();
        }
        if (this.mClient != null) {
            try {
                value = this.mClient.getNcDftParam(domain, list);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "reportToDft fail", new Object[0]);
            }
        }
        return value;
    }

    /* access modifiers changed from: package-private */
    public synchronized void reportNetworkInfo(int domain, int event, NetworkInfo info) {
        if (this.mClient == null) {
            bindToService();
        }
        if (this.mClient != null) {
            try {
                this.mClient.reportNetworkInfo(domain, event, info);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "reportToDft fail", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void reportGnssLocation(int domain, int event, Location location, long time, String provider) {
        if (this.mClient == null) {
            bindToService();
        }
        if (this.mClient != null) {
            try {
                this.mClient.reportGnssLocation(domain, event, location, time, provider);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "reportToDft fail", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void reportGnssSvStatus(int domain, int event, int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        if (this.mClient == null) {
            bindToService();
        }
        if (this.mClient != null) {
            try {
                this.mClient.reportGnssSvStatus(domain, event, svCount, svs, snrs, svElevations, svAzimuths);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "reportToDft fail", new Object[0]);
            }
        }
    }
}
