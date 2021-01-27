package com.android.server.location;

import android.content.Context;
import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider;
import android.os.IBinder;
import com.android.server.FgThread;
import com.android.server.ServiceWatcher;
import java.util.List;

public class GeocoderProxy {
    private static final String SERVICE_ACTION = "com.android.location.service.GeocodeProvider";
    private static final String TAG = "GeocoderProxy";
    private final ServiceWatcher mServiceWatcher;

    public static GeocoderProxy createAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        GeocoderProxy proxy = new GeocoderProxy(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private GeocoderProxy(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        this.mServiceWatcher = new ServiceWatcher(context, TAG, SERVICE_ACTION, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, FgThread.getHandler());
    }

    protected GeocoderProxy(Context context) {
        this.mServiceWatcher = null;
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getCurrentPackageName();
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        return (String) this.mServiceWatcher.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(latitude, longitude, maxResults, params, addrs) {
            /* class com.android.server.location.$$Lambda$GeocoderProxy$jfLn3HL2BzwsKdoI6ZZeFfEe10k */
            private final /* synthetic */ double f$0;
            private final /* synthetic */ double f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ GeocoderParams f$3;
            private final /* synthetic */ List f$4;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
            }

            @Override // com.android.server.ServiceWatcher.BlockingBinderRunner
            public final Object run(IBinder iBinder) {
                return IGeocodeProvider.Stub.asInterface(iBinder).getFromLocation(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, "Service not Available");
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        return (String) this.mServiceWatcher.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs) {
            /* class com.android.server.location.$$Lambda$GeocoderProxy$l4GRjTzjcqxZJILrVLX5qayXBE0 */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ double f$1;
            private final /* synthetic */ double f$2;
            private final /* synthetic */ double f$3;
            private final /* synthetic */ double f$4;
            private final /* synthetic */ int f$5;
            private final /* synthetic */ GeocoderParams f$6;
            private final /* synthetic */ List f$7;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r4;
                this.f$3 = r6;
                this.f$4 = r8;
                this.f$5 = r10;
                this.f$6 = r11;
                this.f$7 = r12;
            }

            @Override // com.android.server.ServiceWatcher.BlockingBinderRunner
            public final Object run(IBinder iBinder) {
                return IGeocodeProvider.Stub.asInterface(iBinder).getFromLocationName(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        }, "Service not Available");
    }
}
