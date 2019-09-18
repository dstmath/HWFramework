package android.location;

import android.content.Context;
import android.location.ILocationManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Geocoder {
    private static final String TAG = "Geocoder";
    private GeocoderParams mParams;
    private ILocationManager mService;

    public static boolean isPresent() {
        ILocationManager lm = ILocationManager.Stub.asInterface(ServiceManager.getService("location"));
        if (lm == null) {
            return false;
        }
        try {
            return lm.geocoderIsPresent();
        } catch (RemoteException e) {
            Log.e(TAG, "isPresent: got RemoteException", e);
            return false;
        }
    }

    public Geocoder(Context context, Locale locale) {
        if (locale != null) {
            this.mParams = new GeocoderParams(context, locale);
            this.mService = ILocationManager.Stub.asInterface(ServiceManager.getService("location"));
            return;
        }
        throw new NullPointerException("locale == null");
    }

    public Geocoder(Context context) {
        this(context, Locale.getDefault());
    }

    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < -90.0d || latitude > 90.0d) {
            throw new IllegalArgumentException("latitude == " + latitude);
        } else if (longitude < -180.0d || longitude > 180.0d) {
            throw new IllegalArgumentException("longitude == " + longitude);
        } else {
            try {
                ArrayList arrayList = new ArrayList();
                String ex = this.mService.getFromLocation(latitude, longitude, maxResults, this.mParams, arrayList);
                if (ex == null) {
                    return arrayList;
                }
                throw new IOException(ex);
            } catch (RemoteException e) {
                Log.e(TAG, "getFromLocation: got RemoteException", e);
                return null;
            }
        }
    }

    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        if (locationName != null) {
            try {
                ArrayList arrayList = new ArrayList();
                String ex = this.mService.getFromLocationName(locationName, 0.0d, 0.0d, 0.0d, 0.0d, maxResults, this.mParams, arrayList);
                if (ex == null) {
                    return arrayList;
                }
                throw new IOException(ex);
            } catch (RemoteException e) {
                Log.e(TAG, "getFromLocationName: got RemoteException", e);
                return null;
            }
        } else {
            throw new IllegalArgumentException("locationName == null");
        }
    }

    public List<Address> getFromLocationName(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude) throws IOException {
        double d = lowerLeftLatitude;
        double d2 = lowerLeftLongitude;
        double d3 = upperRightLatitude;
        double d4 = upperRightLongitude;
        if (locationName == null) {
            double d5 = d4;
            double d6 = d3;
            double d7 = d2;
            double d8 = d;
            throw new IllegalArgumentException("locationName == null");
        } else if (d < -90.0d || d > 90.0d) {
            double d9 = d4;
            double d10 = d3;
            double d11 = d2;
            throw new IllegalArgumentException("lowerLeftLatitude == " + lowerLeftLatitude);
        } else if (d2 < -180.0d || d2 > 180.0d) {
            double d12 = d4;
            double d13 = d3;
            throw new IllegalArgumentException("lowerLeftLongitude == " + lowerLeftLongitude);
        } else if (d3 < -90.0d || d3 > 90.0d) {
            double d14 = d4;
            throw new IllegalArgumentException("upperRightLatitude == " + upperRightLatitude);
        } else if (d4 < -180.0d || d4 > 180.0d) {
            throw new IllegalArgumentException("upperRightLongitude == " + upperRightLongitude);
        } else {
            try {
                ArrayList<Address> result = new ArrayList<>();
                String ex = this.mService.getFromLocationName(locationName, d, d2, d3, upperRightLongitude, maxResults, this.mParams, result);
                if (ex == null) {
                    return result;
                }
                throw new IOException(ex);
            } catch (RemoteException e) {
                Log.e(TAG, "getFromLocationName: got RemoteException", e);
                return null;
            }
        }
    }
}
