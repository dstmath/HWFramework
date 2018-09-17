package com.android.server.location;

import android.content.Context;
import android.hardware.location.IFusedLocationHardware;
import android.location.IFusedGeofenceHardware;
import android.util.Log;

public class FlpHardwareProvider {
    private static final boolean DEBUG = true;
    public static final String LOCATION = "Location";
    private static final String TAG = "FlpHardwareProvider";
    private static FlpHardwareProvider sSingletonInstance = null;

    public static FlpHardwareProvider getInstance(Context context) {
        if (sSingletonInstance == null) {
            sSingletonInstance = new FlpHardwareProvider();
            Log.d(TAG, "getInstance() created empty provider");
        }
        return sSingletonInstance;
    }

    private FlpHardwareProvider() {
    }

    public static boolean isSupported() {
        Log.d(TAG, "isSupported() returning false");
        return false;
    }

    public IFusedLocationHardware getLocationHardware() {
        return null;
    }

    public IFusedGeofenceHardware getGeofenceHardware() {
        return null;
    }

    public void cleanup() {
        Log.d(TAG, "empty cleanup()");
    }
}
