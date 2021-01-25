package com.android.server.location;

import android.os.Handler;
import com.huawei.android.server.FgThreadEx;

public class LocationHandlerEx {
    private static final String GEOCODE_NAME = "GeoCodeHander";
    private static final String LOCATION_NAME = "LocationHander";
    private static final Object THREAD_LOCK = new Object();
    private static volatile Handler sGeoInstance = null;
    private static volatile Handler sInstance = null;

    private LocationHandlerEx() {
    }

    public static Handler getInstance() {
        if (sInstance == null) {
            synchronized (THREAD_LOCK) {
                if (sInstance == null) {
                    sInstance = new Handler(FgThreadEx.createFgThread(LOCATION_NAME).getLooper());
                }
            }
        }
        return sInstance;
    }

    public static Handler getGeoInstance() {
        if (sGeoInstance == null) {
            synchronized (THREAD_LOCK) {
                if (sGeoInstance == null) {
                    sGeoInstance = new Handler(FgThreadEx.createFgThread(GEOCODE_NAME).getLooper());
                }
            }
        }
        return sGeoInstance;
    }
}
