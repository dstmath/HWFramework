package com.android.server.location;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.location.IGeocodeProvider.Stub;
import android.os.Handler;
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
        this.mServiceWatcher = new ServiceWatcher(this.mContext, TAG, SERVICE_ACTION, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, null, handler);
    }

    protected GeocoderProxy(Context context) {
        this.mContext = context;
        this.mServiceWatcher = null;
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    private IGeocodeProvider getService() {
        return Stub.asInterface(this.mServiceWatcher.getBinder());
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getBestPackageName();
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocation(latitude, longitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider = getService();
        if (provider != null) {
            try {
                return provider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        }
        return "Service not Available";
    }
}
