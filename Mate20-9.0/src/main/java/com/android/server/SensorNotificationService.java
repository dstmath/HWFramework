package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorAdditionalInfo;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;

public class SensorNotificationService extends SystemService implements SensorEventListener, LocationListener {
    private static final boolean DBG = false;
    private static final long KM_IN_M = 1000;
    private static final long LOCATION_MIN_DISTANCE = 100000;
    private static final long LOCATION_MIN_TIME = 1800000;
    private static final long MILLIS_2010_1_1 = 1262358000000L;
    private static final long MINUTE_IN_MS = 60000;
    private static final String PROPERTY_USE_MOCKED_LOCATION = "sensor.notification.use_mocked";
    private static final String TAG = "SensorNotificationService";
    private Context mContext;
    private long mLocalGeomagneticFieldUpdateTime = -1800000;
    private LocationManager mLocationManager;
    private Sensor mMetaSensor;
    private SensorManager mSensorManager;

    public SensorNotificationService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        LocalServices.addService(SensorNotificationService.class, this);
    }

    public void onBootPhase(int phase) {
        if (phase == 600) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            this.mMetaSensor = this.mSensorManager.getDefaultSensor(32);
            if (this.mMetaSensor != null) {
                this.mSensorManager.registerListener(this, this.mMetaSensor, 0);
            }
        }
        if (phase == 1000) {
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            if (this.mLocationManager != null) {
                this.mLocationManager.requestLocationUpdates("passive", 1800000, 100000.0f, this);
            }
        }
    }

    private void broadcastDynamicSensorChanged() {
        Intent i = new Intent("android.intent.action.DYNAMIC_SENSOR_CHANGED");
        i.setFlags(1073741824);
        this.mContext.sendBroadcastAsUser(i, UserHandle.ALL);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == this.mMetaSensor) {
            broadcastDynamicSensorChanged();
        }
    }

    public void onLocationChanged(Location location) {
        if (!(location.getLatitude() == 0.0d && location.getLongitude() == 0.0d) && SystemClock.elapsedRealtime() - this.mLocalGeomagneticFieldUpdateTime >= 600000) {
            long time = System.currentTimeMillis();
            if (useMockedLocation() != location.isFromMockProvider() && time >= MILLIS_2010_1_1) {
                GeomagneticField geomagneticField = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(), (float) location.getAltitude(), time);
                try {
                    SensorAdditionalInfo info = SensorAdditionalInfo.createLocalGeomagneticField(geomagneticField.getFieldStrength() / 1000.0f, (float) ((((double) geomagneticField.getDeclination()) * 3.141592653589793d) / 180.0d), (float) ((((double) geomagneticField.getInclination()) * 3.141592653589793d) / 180.0d));
                    if (info != null) {
                        this.mSensorManager.setOperationParameter(info);
                        this.mLocalGeomagneticFieldUpdateTime = SystemClock.elapsedRealtime();
                    }
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Invalid local geomagnetic field, ignore.");
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    private boolean useMockedLocation() {
        return "false".equals(System.getProperty(PROPERTY_USE_MOCKED_LOCATION, "false"));
    }
}
