package com.android.server.twilight;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.impl.CalendarAstronomer;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.SystemService;
import java.util.Objects;

public final class TwilightService extends SystemService implements AlarmManager.OnAlarmListener, Handler.Callback, LocationListener {
    private static final boolean DEBUG = false;
    private static final int MSG_START_LISTENING = 1;
    private static final int MSG_STOP_LISTENING = 2;
    private static final String TAG = "TwilightService";
    protected AlarmManager mAlarmManager;
    private boolean mBootCompleted;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper(), this);
    private boolean mHasListeners;
    protected Location mLastLocation;
    @GuardedBy("mListeners")
    protected TwilightState mLastTwilightState;
    /* access modifiers changed from: private */
    @GuardedBy("mListeners")
    public final ArrayMap<TwilightListener, Handler> mListeners = new ArrayMap<>();
    private LocationManager mLocationManager;
    private BroadcastReceiver mTimeChangedReceiver;

    public TwilightService(Context context) {
        super(context);
    }

    public void onStart() {
        publishLocalService(TwilightManager.class, new TwilightManager() {
            public void registerListener(TwilightListener listener, Handler handler) {
                synchronized (TwilightService.this.mListeners) {
                    boolean wasEmpty = TwilightService.this.mListeners.isEmpty();
                    TwilightService.this.mListeners.put(listener, handler);
                    if (wasEmpty && !TwilightService.this.mListeners.isEmpty()) {
                        TwilightService.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }

            public void unregisterListener(TwilightListener listener) {
                synchronized (TwilightService.this.mListeners) {
                    boolean wasEmpty = TwilightService.this.mListeners.isEmpty();
                    TwilightService.this.mListeners.remove(listener);
                    if (!wasEmpty && TwilightService.this.mListeners.isEmpty()) {
                        TwilightService.this.mHandler.sendEmptyMessage(2);
                    }
                }
            }

            public TwilightState getLastTwilightState() {
                TwilightState twilightState;
                synchronized (TwilightService.this.mListeners) {
                    twilightState = TwilightService.this.mLastTwilightState;
                }
                return twilightState;
            }
        });
    }

    public void onBootPhase(int phase) {
        if (phase == 1000) {
            Context c = getContext();
            this.mAlarmManager = (AlarmManager) c.getSystemService("alarm");
            this.mLocationManager = (LocationManager) c.getSystemService("location");
            this.mBootCompleted = true;
            if (this.mHasListeners) {
                startListening();
            }
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (!this.mHasListeners) {
                    this.mHasListeners = true;
                    if (this.mBootCompleted) {
                        startListening();
                    }
                }
                return true;
            case 2:
                if (this.mHasListeners) {
                    this.mHasListeners = false;
                    if (this.mBootCompleted) {
                        stopListening();
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private void startListening() {
        Slog.d(TAG, "startListening");
        this.mLocationManager.requestLocationUpdates(null, this, Looper.getMainLooper());
        if (this.mLocationManager.getLastLocation() == null) {
            if (this.mLocationManager.isProviderEnabled("network")) {
                this.mLocationManager.requestSingleUpdate("network", this, Looper.getMainLooper());
            } else if (this.mLocationManager.isProviderEnabled("gps")) {
                this.mLocationManager.requestSingleUpdate("gps", this, Looper.getMainLooper());
            }
        }
        if (this.mTimeChangedReceiver == null) {
            this.mTimeChangedReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Slog.d(TwilightService.TAG, "onReceive: " + intent);
                    TwilightService.this.updateTwilightState();
                }
            };
            IntentFilter intentFilter = new IntentFilter("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiver(this.mTimeChangedReceiver, intentFilter);
        }
        updateTwilightState();
    }

    private void stopListening() {
        Slog.d(TAG, "stopListening");
        if (this.mTimeChangedReceiver != null) {
            getContext().unregisterReceiver(this.mTimeChangedReceiver);
            this.mTimeChangedReceiver = null;
        }
        if (this.mLastTwilightState != null) {
            this.mAlarmManager.cancel(this);
        }
        this.mLocationManager.removeUpdates(this);
        this.mLastLocation = null;
    }

    /* access modifiers changed from: private */
    public void updateTwilightState() {
        Location location;
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mLastLocation != null) {
            location = this.mLastLocation;
        } else {
            location = this.mLocationManager.getLastLocation();
        }
        final TwilightState state = calculateTwilightState(location, currentTimeMillis);
        synchronized (this.mListeners) {
            if (!Objects.equals(this.mLastTwilightState, state)) {
                this.mLastTwilightState = state;
                for (int i = this.mListeners.size() - 1; i >= 0; i--) {
                    final TwilightListener listener = this.mListeners.keyAt(i);
                    this.mListeners.valueAt(i).post(new Runnable() {
                        public void run() {
                            listener.onTwilightStateChanged(state);
                        }
                    });
                }
            }
        }
        if (state != null) {
            this.mAlarmManager.setExact(1, state.isNight() ? state.sunriseTimeMillis() : state.sunsetTimeMillis(), TAG, this, this.mHandler);
        }
    }

    public void onAlarm() {
        Slog.d(TAG, "onAlarm");
        updateTwilightState();
    }

    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        if (location.getLongitude() != 0.0d || location.getLatitude() != 0.0d) {
            Slog.d(TAG, "onLocationChanged: provider=" + location.getProvider() + " accuracy=" + location.getAccuracy() + " time=" + location.getTime());
            this.mLastLocation = location;
            updateTwilightState();
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    private static TwilightState calculateTwilightState(Location location, long timeMillis) {
        if (location == null) {
            return null;
        }
        CalendarAstronomer ca = new CalendarAstronomer(location.getLongitude(), location.getLatitude());
        Calendar noon = Calendar.getInstance();
        noon.setTimeInMillis(timeMillis);
        noon.set(11, 12);
        noon.set(12, 0);
        noon.set(13, 0);
        noon.set(14, 0);
        ca.setTime(noon.getTimeInMillis());
        long sunriseTimeMillis = ca.getSunRiseSet(true);
        long sunsetTimeMillis = ca.getSunRiseSet(false);
        if (sunsetTimeMillis < timeMillis) {
            noon.add(5, 1);
            ca.setTime(noon.getTimeInMillis());
            sunriseTimeMillis = ca.getSunRiseSet(true);
        } else if (sunriseTimeMillis > timeMillis) {
            noon.add(5, -1);
            ca.setTime(noon.getTimeInMillis());
            sunsetTimeMillis = ca.getSunRiseSet(false);
        }
        return new TwilightState(sunriseTimeMillis, sunsetTimeMillis);
    }
}
