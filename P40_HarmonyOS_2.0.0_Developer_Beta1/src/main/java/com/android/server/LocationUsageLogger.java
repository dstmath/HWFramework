package com.android.server;

import android.location.Geofence;
import android.location.LocationRequest;
import android.os.SystemClock;
import android.util.Log;
import android.util.StatsLog;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usage.AppStandbyController;
import java.time.Instant;

/* access modifiers changed from: package-private */
public class LocationUsageLogger {
    private static final int API_USAGE_LOG_HOURLY_CAP = 60;
    private static final boolean D = Log.isLoggable(TAG, 3);
    private static final int ONE_HOUR_IN_MILLIS = 3600000;
    private static final int ONE_MINUTE_IN_MILLIS = 60000;
    private static final int ONE_SEC_IN_MILLIS = 1000;
    private static final String TAG = "LocationUsageLogger";
    private int mApiUsageLogHourlyCount = 0;
    private long mLastApiUsageLogHour = 0;

    LocationUsageLogger() {
    }

    private static int providerNameToStatsdEnum(String provider) {
        if ("network".equals(provider)) {
            return 1;
        }
        if ("gps".equals(provider)) {
            return 2;
        }
        if ("passive".equals(provider)) {
            return 3;
        }
        if ("fused".equals(provider)) {
            return 4;
        }
        return 0;
    }

    private static int bucketizeIntervalToStatsdEnum(long interval) {
        if (interval < 1000) {
            return 1;
        }
        if (interval < 5000) {
            return 2;
        }
        if (interval < 60000) {
            return 3;
        }
        if (interval < 600000) {
            return 4;
        }
        if (interval < AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT) {
            return 5;
        }
        return 6;
    }

    private static int bucketizeSmallestDisplacementToStatsdEnum(float smallestDisplacement) {
        if (smallestDisplacement == 0.0f) {
            return 1;
        }
        if (smallestDisplacement <= 0.0f || smallestDisplacement > 100.0f) {
            return 3;
        }
        return 2;
    }

    private static int bucketizeRadiusToStatsdEnum(float radius) {
        if (radius < 0.0f) {
            return 7;
        }
        if (radius < 100.0f) {
            return 1;
        }
        if (radius < 200.0f) {
            return 2;
        }
        if (radius < 300.0f) {
            return 3;
        }
        if (radius < 1000.0f) {
            return 4;
        }
        if (radius < 10000.0f) {
            return 5;
        }
        return 6;
    }

    private static int getBucketizedExpireIn(long expireAt) {
        if (expireAt == JobStatus.NO_LATEST_RUNTIME) {
            return 6;
        }
        long expireIn = Math.max(0L, expireAt - SystemClock.elapsedRealtime());
        if (expireIn < 20000) {
            return 1;
        }
        if (expireIn < 60000) {
            return 2;
        }
        if (expireIn < 600000) {
            return 3;
        }
        if (expireIn < AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT) {
            return 4;
        }
        return 5;
    }

    private static int categorizeActivityImportance(int importance) {
        if (importance == 100) {
            return 1;
        }
        if (importance == 125) {
            return 2;
        }
        return 3;
    }

    private static int getCallbackType(int apiType, boolean hasListener, boolean hasIntent) {
        if (apiType == 5) {
            return 1;
        }
        if (hasIntent) {
            return 3;
        }
        if (hasListener) {
            return 2;
        }
        return 0;
    }

    private boolean checkApiUsageLogCap() {
        if (D) {
            Log.d(TAG, "checking APIUsage log cap.");
        }
        long currentHour = Instant.now().toEpochMilli() / AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
        if (currentHour > this.mLastApiUsageLogHour) {
            this.mLastApiUsageLogHour = currentHour;
            this.mApiUsageLogHourlyCount = 0;
            return true;
        }
        this.mApiUsageLogHourlyCount = Math.min(this.mApiUsageLogHourlyCount + 1, (int) API_USAGE_LOG_HOURLY_CAP);
        return this.mApiUsageLogHourlyCount < API_USAGE_LOG_HOURLY_CAP;
    }

    public void logLocationApiUsage(int usageType, int apiInUse, String packageName, LocationRequest locationRequest, boolean hasListener, boolean hasIntent, Geofence geofence, int activityImportance) {
        String str;
        Exception e;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        try {
            if (checkApiUsageLogCap()) {
                boolean isLocationRequestNull = locationRequest == null;
                boolean isGeofenceNull = geofence == null;
                if (D) {
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("log API Usage to statsd. usageType: ");
                        sb.append(usageType);
                        sb.append(", apiInUse: ");
                        sb.append(apiInUse);
                        sb.append(", packageName: ");
                        String str2 = "";
                        sb.append(packageName == null ? str2 : packageName);
                        sb.append(", locationRequest: ");
                        sb.append(isLocationRequestNull ? str2 : locationRequest.toString());
                        sb.append(", hasListener: ");
                        sb.append(hasListener);
                        sb.append(", hasIntent: ");
                        sb.append(hasIntent);
                        sb.append(", geofence: ");
                        if (!isGeofenceNull) {
                            str2 = geofence.toString();
                        }
                        sb.append(str2);
                        sb.append(", importance: ");
                        sb.append(activityImportance);
                        Log.d(TAG, sb.toString());
                    } catch (Exception e2) {
                        e = e2;
                        str = TAG;
                        Log.w(str, "Failed to log API usage to statsd.", e);
                    }
                }
                if (isLocationRequestNull) {
                    i = 0;
                } else {
                    i = providerNameToStatsdEnum(locationRequest.getProvider());
                }
                if (isLocationRequestNull) {
                    i2 = 0;
                } else {
                    i2 = locationRequest.getQuality();
                }
                if (isLocationRequestNull) {
                    i3 = 0;
                } else {
                    i3 = bucketizeIntervalToStatsdEnum(locationRequest.getInterval());
                }
                if (isLocationRequestNull) {
                    i4 = 0;
                } else {
                    i4 = bucketizeSmallestDisplacementToStatsdEnum(locationRequest.getSmallestDisplacement());
                }
                long numUpdates = isLocationRequestNull ? 0 : (long) locationRequest.getNumUpdates();
                if (isLocationRequestNull || usageType == 1) {
                    i5 = 0;
                } else {
                    i5 = getBucketizedExpireIn(locationRequest.getExpireAt());
                }
                int callbackType = getCallbackType(apiInUse, hasListener, hasIntent);
                if (isGeofenceNull) {
                    i6 = 0;
                } else {
                    i6 = bucketizeRadiusToStatsdEnum(geofence.getRadius());
                }
                int categorizeActivityImportance = categorizeActivityImportance(activityImportance);
                str = TAG;
                try {
                    StatsLog.write(210, usageType, apiInUse, packageName, i, i2, i3, i4, numUpdates, i5, callbackType, i6, categorizeActivityImportance);
                } catch (Exception e3) {
                    e = e3;
                }
            }
        } catch (Exception e4) {
            e = e4;
            str = TAG;
            Log.w(str, "Failed to log API usage to statsd.", e);
        }
    }

    public void logLocationApiUsage(int usageType, int apiInUse, String providerName) {
        String str;
        Exception e;
        StringBuilder sb;
        try {
            if (checkApiUsageLogCap()) {
                if (D) {
                    try {
                        sb = new StringBuilder();
                        sb.append("log API Usage to statsd. usageType: ");
                    } catch (Exception e2) {
                        e = e2;
                        str = TAG;
                        Log.w(str, "Failed to log API usage to statsd.", e);
                    }
                    try {
                        sb.append(usageType);
                        sb.append(", apiInUse: ");
                        sb.append(apiInUse);
                        sb.append(", providerName: ");
                        sb.append(providerName);
                        Log.d(TAG, sb.toString());
                    } catch (Exception e3) {
                        e = e3;
                        str = TAG;
                        Log.w(str, "Failed to log API usage to statsd.", e);
                    }
                }
                int providerNameToStatsdEnum = providerNameToStatsdEnum(providerName);
                int callbackType = getCallbackType(apiInUse, true, true);
                str = TAG;
                try {
                    StatsLog.write(210, usageType, apiInUse, null, providerNameToStatsdEnum, 0, 0, 0, 0, 0, callbackType, 0, 0);
                } catch (Exception e4) {
                    e = e4;
                }
            }
        } catch (Exception e5) {
            e = e5;
            str = TAG;
            Log.w(str, "Failed to log API usage to statsd.", e);
        }
    }
}
