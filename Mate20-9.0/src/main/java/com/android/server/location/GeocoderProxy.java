package com.android.server.location;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.ServiceWatcher;
import java.util.List;

public class GeocoderProxy {
    private static final String SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String TAG = "GeocoderProxy";
    private final Context mContext;
    private final ServiceWatcher mServiceWatcher;

    public static GeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        GeocoderProxy proxy = new GeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private GeocoderProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        this.mContext = context;
        ServiceWatcher serviceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, null, handler);
        this.mServiceWatcher = serviceWatcher;
    }

    protected GeocoderProxy(Context context) {
        this.mContext = context;
        this.mServiceWatcher = null;
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getBestPackageName();
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        String[] result = {"Service not Available"};
        ServiceWatcher serviceWatcher = this.mServiceWatcher;
        final String[] strArr = result;
        final double d = latitude;
        final double d2 = longitude;
        final int i = maxResults;
        final GeocoderParams geocoderParams = params;
        final List<Address> list = addrs;
        AnonymousClass1 r1 = new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    strArr[0] = IGeocodeProvider.Stub.asInterface(binder).getFromLocation(d, d2, i, geocoderParams, list);
                } catch (RemoteException e) {
                    Log.w(GeocoderProxy.TAG, e);
                }
            }
        };
        serviceWatcher.runOnBinder(r1);
        return result[0];
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        String[] result = {"Service not Available"};
        final String[] strArr = result;
        final String str = locationName;
        final double d = lowerLeftLatitude;
        final double d2 = lowerLeftLongitude;
        final double d3 = upperRightLatitude;
        final double d4 = upperRightLongitude;
        String[] result2 = result;
        final int i = maxResults;
        AnonymousClass2 r17 = r1;
        ServiceWatcher serviceWatcher = this.mServiceWatcher;
        final GeocoderParams geocoderParams = params;
        final List<Address> list = addrs;
        AnonymousClass2 r1 = new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    strArr[0] = IGeocodeProvider.Stub.asInterface(binder).getFromLocationName(str, d, d2, d3, d4, i, geocoderParams, list);
                } catch (RemoteException e) {
                    Log.w(GeocoderProxy.TAG, e);
                }
            }
        };
        serviceWatcher.runOnBinder(r17);
        return result2[0];
    }
}
