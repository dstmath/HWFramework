package com.android.location.provider;

import android.location.Address;
import android.location.GeocoderParams;
import android.location.IGeocodeProvider.Stub;
import android.os.IBinder;
import java.util.List;

public abstract class GeocodeProvider {
    private Stub mProvider = new Stub() {
        public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
            return GeocodeProvider.this.onGetFromLocation(latitude, longitude, maxResults, params, addrs);
        }

        public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
            return GeocodeProvider.this.onGetFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
    };

    public abstract String onGetFromLocation(double d, double d2, int i, GeocoderParams geocoderParams, List<Address> list);

    public abstract String onGetFromLocationName(String str, double d, double d2, double d3, double d4, int i, GeocoderParams geocoderParams, List<Address> list);

    public IBinder getBinder() {
        return this.mProvider;
    }
}
