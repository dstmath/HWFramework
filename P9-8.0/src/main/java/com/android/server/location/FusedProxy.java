package com.android.server.location;

import android.content.Context;
import android.hardware.location.IFusedLocationHardware;
import android.location.IFusedProvider;
import android.location.IFusedProvider.Stub;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.ServiceWatcher;

public final class FusedProxy {
    private final String TAG = "FusedProxy";
    private final FusedLocationHardwareSecure mLocationHardware;
    private final ServiceWatcher mServiceWatcher;

    private FusedProxy(Context context, Handler handler, IFusedLocationHardware locationHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        this.mLocationHardware = new FusedLocationHardwareSecure(locationHardware, context, "android.permission.LOCATION_HARDWARE");
        Context context2 = context;
        this.mServiceWatcher = new ServiceWatcher(context2, "FusedProxy", "com.android.location.service.FusedProvider", overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId, new Runnable() {
            public void run() {
                FusedProxy.this.bindProvider(FusedProxy.this.mLocationHardware);
            }
        }, handler);
    }

    public static FusedProxy createAndBind(Context context, Handler handler, IFusedLocationHardware locationHardware, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNameResId) {
        FusedProxy fusedProxy = new FusedProxy(context, handler, locationHardware, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNameResId);
        if (fusedProxy.mServiceWatcher.start()) {
            return fusedProxy;
        }
        return null;
    }

    private void bindProvider(IFusedLocationHardware locationHardware) {
        IFusedProvider provider = Stub.asInterface(this.mServiceWatcher.getBinder());
        if (provider == null) {
            Log.e("FusedProxy", "No instance of FusedProvider found on FusedLocationHardware connected.");
            return;
        }
        try {
            provider.onFusedLocationHardwareChange(locationHardware);
        } catch (RemoteException e) {
            Log.e("FusedProxy", e.toString());
        }
    }
}
