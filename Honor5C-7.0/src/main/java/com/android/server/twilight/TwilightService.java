package com.android.server.twilight;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.format.Time;
import com.android.server.SystemService;
import com.android.server.TwilightCalculator;
import com.android.server.am.ProcessList;
import com.android.server.audio.AudioService;
import java.util.ArrayList;
import libcore.util.Objects;

public final class TwilightService extends SystemService {
    private static final String ACTION_RESET_TWILIGHT_AUTO = "com.android.server.action.RESET_TWILIGHT_AUTO";
    public static final String ACTION_TWILIGHT_CHANGED = "android.intent.action.TWILIGHT_CHANGED";
    static final String ACTION_UPDATE_TWILIGHT_STATE = "com.android.server.action.UPDATE_TWILIGHT_STATE";
    static final boolean DEBUG = false;
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_IS_NIGHT = "isNight";
    private static final String EXTRA_RESET_USER = "user";
    private static final long RESET_TIME = 7200000;
    static final String TAG = "TwilightService";
    private static final long TWILIGHT_ADJUSTMENT_TIME = 7200000;
    AlarmManager mAlarmManager;
    private boolean mBootCompleted;
    private final ContentObserver mContentObserver;
    private int mCurrentUser;
    private final LocationListener mEmptyLocationListener;
    final ArrayList<TwilightListenerRecord> mListeners;
    LocationHandler mLocationHandler;
    private final LocationListener mLocationListener;
    LocationManager mLocationManager;
    final Object mLock;
    private boolean mLocked;
    private final BroadcastReceiver mReceiver;
    private final TwilightManager mService;
    TwilightState mTwilightState;

    /* renamed from: com.android.server.twilight.TwilightService.2 */
    class AnonymousClass2 extends ContentObserver {
        AnonymousClass2(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int value = Secure.getIntForUser(TwilightService.this.getContext().getContentResolver(), "twilight_mode", 0, TwilightService.this.mCurrentUser);
            if (value == 0) {
                TwilightService.this.setLockedState(new TwilightState(TwilightService.DEBUG, 0.0f));
            } else if (value == 1) {
                TwilightService.this.setLockedState(new TwilightState(true, 1.0f));
            } else if (value == 3) {
                TwilightService.this.setLockedState(new TwilightState(TwilightService.DEBUG, 0.0f));
                TwilightService.this.scheduleReset();
            } else if (value == 4) {
                TwilightService.this.setLockedState(new TwilightState(true, 1.0f));
                TwilightService.this.scheduleReset();
            } else {
                TwilightService.this.mLocked = TwilightService.DEBUG;
                TwilightService.this.mLocationHandler.requestTwilightUpdate();
            }
        }
    }

    private final class LocationHandler extends Handler {
        private static final double FACTOR_GMT_OFFSET_LONGITUDE = 0.004166666666666667d;
        private static final float LOCATION_UPDATE_DISTANCE_METER = 20000.0f;
        private static final long LOCATION_UPDATE_ENABLE_INTERVAL_MAX = 900000;
        private static final long LOCATION_UPDATE_ENABLE_INTERVAL_MIN = 5000;
        private static final long LOCATION_UPDATE_MS = 86400000;
        private static final long MIN_LOCATION_UPDATE_MS = 1800000;
        private static final int MSG_DISABLE_LOCATION_UPDATES = 5;
        private static final int MSG_DO_TWILIGHT_UPDATE = 4;
        private static final int MSG_ENABLE_LOCATION_UPDATES = 1;
        private static final int MSG_GET_NEW_LOCATION_UPDATE = 2;
        private static final int MSG_PROCESS_NEW_LOCATION = 3;
        private boolean mDidFirstInit;
        private long mLastNetworkRegisterTime;
        private long mLastUpdateInterval;
        private Location mLocation;
        private boolean mNetworkListenerEnabled;
        private boolean mPassiveListenerEnabled;
        private final TwilightCalculator mTwilightCalculator;

        private LocationHandler() {
            this.mLastNetworkRegisterTime = -1800000;
            this.mTwilightCalculator = new TwilightCalculator();
        }

        public void processNewLocation(Location location) {
            sendMessage(obtainMessage(MSG_PROCESS_NEW_LOCATION, location));
        }

        public void enableLocationUpdates() {
            sendEmptyMessage(MSG_ENABLE_LOCATION_UPDATES);
        }

        public void disableLocationUpdates() {
            sendEmptyMessage(MSG_DISABLE_LOCATION_UPDATES);
        }

        public void requestLocationUpdate() {
            sendEmptyMessage(MSG_GET_NEW_LOCATION_UPDATE);
        }

        public void requestTwilightUpdate() {
            sendEmptyMessage(MSG_DO_TWILIGHT_UPDATE);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_LOCATION_UPDATES /*1*/:
                    break;
                case MSG_GET_NEW_LOCATION_UPDATE /*2*/:
                    if (this.mNetworkListenerEnabled && this.mLastNetworkRegisterTime + MIN_LOCATION_UPDATE_MS < SystemClock.elapsedRealtime()) {
                        this.mNetworkListenerEnabled = TwilightService.DEBUG;
                        TwilightService.this.mLocationManager.removeUpdates(TwilightService.this.mEmptyLocationListener);
                        break;
                    }
                    return;
                    break;
                case MSG_PROCESS_NEW_LOCATION /*3*/:
                    Location location = msg.obj;
                    boolean hasMoved = TwilightService.hasMoved(this.mLocation, location);
                    boolean hasBetterAccuracy = this.mLocation != null ? location.getAccuracy() < this.mLocation.getAccuracy() ? true : TwilightService.DEBUG : true;
                    if (hasMoved || hasBetterAccuracy) {
                        setLocation(location);
                        break;
                    }
                case MSG_DO_TWILIGHT_UPDATE /*4*/:
                    updateTwilightState();
                    break;
                case MSG_DISABLE_LOCATION_UPDATES /*5*/:
                    TwilightService.this.mLocationManager.removeUpdates(TwilightService.this.mLocationListener);
                    removeMessages(MSG_ENABLE_LOCATION_UPDATES);
                    break;
            }
            boolean isProviderEnabled;
            try {
                isProviderEnabled = TwilightService.this.mLocationManager.isProviderEnabled("network");
            } catch (Exception e) {
                isProviderEnabled = TwilightService.DEBUG;
            }
            if (!this.mNetworkListenerEnabled && r10) {
                this.mNetworkListenerEnabled = true;
                this.mLastNetworkRegisterTime = SystemClock.elapsedRealtime();
                TwilightService.this.mLocationManager.requestLocationUpdates("network", LOCATION_UPDATE_MS, 0.0f, TwilightService.this.mEmptyLocationListener);
                if (!this.mDidFirstInit) {
                    this.mDidFirstInit = true;
                    if (this.mLocation == null) {
                        retrieveLocation();
                    }
                }
            }
            boolean isProviderEnabled2;
            try {
                isProviderEnabled2 = TwilightService.this.mLocationManager.isProviderEnabled("passive");
            } catch (Exception e2) {
                isProviderEnabled2 = TwilightService.DEBUG;
            }
            if (!this.mPassiveListenerEnabled && r11) {
                this.mPassiveListenerEnabled = true;
                TwilightService.this.mLocationManager.requestLocationUpdates("passive", 0, LOCATION_UPDATE_DISTANCE_METER, TwilightService.this.mLocationListener);
            }
            if (!(this.mNetworkListenerEnabled ? this.mPassiveListenerEnabled : TwilightService.DEBUG)) {
                this.mLastUpdateInterval = (long) (((double) this.mLastUpdateInterval) * 1.5d);
                if (this.mLastUpdateInterval == 0) {
                    this.mLastUpdateInterval = LOCATION_UPDATE_ENABLE_INTERVAL_MIN;
                } else if (this.mLastUpdateInterval > LOCATION_UPDATE_ENABLE_INTERVAL_MAX) {
                    this.mLastUpdateInterval = LOCATION_UPDATE_ENABLE_INTERVAL_MAX;
                }
                sendEmptyMessageDelayed(MSG_ENABLE_LOCATION_UPDATES, this.mLastUpdateInterval);
            }
        }

        private void retrieveLocation() {
            Location location = null;
            for (String lastKnownLocation : TwilightService.this.mLocationManager.getProviders(new Criteria(), true)) {
                Location lastKnownLocation2 = TwilightService.this.mLocationManager.getLastKnownLocation(lastKnownLocation);
                if (location == null || (lastKnownLocation2 != null && location.getElapsedRealtimeNanos() < lastKnownLocation2.getElapsedRealtimeNanos())) {
                    location = lastKnownLocation2;
                }
            }
            if (location == null) {
                int i;
                Time currentTime = new Time();
                currentTime.set(System.currentTimeMillis());
                long j = currentTime.gmtoff;
                if (currentTime.isDst > 0) {
                    i = 3600;
                } else {
                    i = 0;
                }
                double lngOffset = FACTOR_GMT_OFFSET_LONGITUDE * ((double) (j - ((long) i)));
                location = new Location("fake");
                location.setLongitude(lngOffset);
                location.setLatitude(0.0d);
                location.setAccuracy(417000.0f);
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            setLocation(location);
        }

        private void setLocation(Location location) {
            this.mLocation = location;
            updateTwilightState();
        }

        private void updateTwilightState() {
            if (this.mLocation == null) {
                TwilightService.this.setTwilightState(null);
                return;
            }
            long nextUpdate;
            long now = System.currentTimeMillis();
            this.mTwilightCalculator.calculateTwilight(now, this.mLocation.getLatitude(), this.mLocation.getLongitude());
            boolean isNight = this.mTwilightCalculator.mState == MSG_ENABLE_LOCATION_UPDATES ? true : TwilightService.DEBUG;
            long todaySunrise = this.mTwilightCalculator.mSunrise;
            long todaySunset = this.mTwilightCalculator.mSunset;
            this.mTwilightCalculator.calculateTwilight(LOCATION_UPDATE_MS + now, this.mLocation.getLatitude(), this.mLocation.getLongitude());
            long tomorrowSunrise = this.mTwilightCalculator.mSunrise;
            float amount = 0.0f;
            if (isNight) {
                if (todaySunrise == -1 || todaySunset == -1) {
                    amount = 1.0f;
                } else if (now > todaySunset) {
                    amount = Math.min(1.0f, ((float) (now - todaySunset)) / 7200000.0f);
                } else {
                    amount = Math.max(0.0f, 1.0f - (((float) (todaySunrise - now)) / 7200000.0f));
                }
            }
            TwilightService.this.setTwilightState(new TwilightState(isNight, amount));
            if (todaySunrise == -1 || todaySunset == -1) {
                nextUpdate = now + 43200000;
            } else if (amount != 1.0f && amount != 0.0f) {
                nextUpdate = 60000 + 540000;
            } else if (now > todaySunset) {
                nextUpdate = 60000 + tomorrowSunrise;
            } else if (now > todaySunrise) {
                nextUpdate = 60000 + todaySunset;
            } else {
                nextUpdate = 60000 + todaySunrise;
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(TwilightService.this.getContext(), 0, new Intent(TwilightService.ACTION_UPDATE_TWILIGHT_STATE), 0);
            TwilightService.this.mAlarmManager.cancel(pendingIntent);
            TwilightService.this.mAlarmManager.setExact(MSG_ENABLE_LOCATION_UPDATES, nextUpdate, pendingIntent);
        }
    }

    private static class TwilightListenerRecord implements Runnable {
        private final Handler mHandler;
        private final TwilightListener mListener;

        public TwilightListenerRecord(TwilightListener listener, Handler handler) {
            this.mListener = listener;
            this.mHandler = handler;
        }

        public void postUpdate() {
            this.mHandler.post(this);
        }

        public void run() {
            this.mListener.onTwilightStateChanged();
        }
    }

    public TwilightService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mListeners = new ArrayList();
        this.mService = new TwilightManager() {
            public TwilightState getCurrentState() {
                TwilightState twilightState;
                synchronized (TwilightService.this.mLock) {
                    twilightState = TwilightService.this.mTwilightState;
                }
                return twilightState;
            }

            public void registerListener(TwilightListener listener, Handler handler) {
                synchronized (TwilightService.this.mLock) {
                    TwilightService.this.mListeners.add(new TwilightListenerRecord(listener, handler));
                    if (TwilightService.this.mListeners.size() == 1) {
                        TwilightService.this.mLocationHandler.enableLocationUpdates();
                    }
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void unregisterListener(TwilightListener listener) {
                synchronized (TwilightService.this.mLock) {
                    int i = 0;
                    while (true) {
                        if (i >= TwilightService.this.mListeners.size()) {
                            break;
                        }
                        if (((TwilightListenerRecord) TwilightService.this.mListeners.get(i)).mListener == listener) {
                            TwilightService.this.mListeners.remove(i);
                        }
                        i++;
                    }
                    if (TwilightService.this.mListeners.size() == 0) {
                        TwilightService.this.mLocationHandler.disableLocationUpdates();
                    }
                }
            }
        };
        this.mContentObserver = new AnonymousClass2(new Handler());
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    TwilightService.this.mCurrentUser = ActivityManager.getCurrentUser();
                    TwilightService.this.reregisterSettingObserver();
                } else if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction()) && !intent.getBooleanExtra(AudioService.CONNECT_INTENT_KEY_STATE, TwilightService.DEBUG)) {
                    TwilightService.this.mLocationHandler.requestLocationUpdate();
                } else if (TwilightService.ACTION_RESET_TWILIGHT_AUTO.equals(intent.getAction())) {
                    Secure.putIntForUser(TwilightService.this.getContext().getContentResolver(), "twilight_mode", 2, intent.getIntExtra(TwilightService.EXTRA_RESET_USER, 0));
                } else {
                    TwilightService.this.mLocationHandler.requestTwilightUpdate();
                }
            }
        };
        this.mEmptyLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        this.mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                TwilightService.this.mLocationHandler.processNewLocation(location);
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
    }

    public void onStart() {
        this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
        this.mLocationManager = (LocationManager) getContext().getSystemService("location");
        this.mLocationHandler = new LocationHandler();
        this.mCurrentUser = ActivityManager.getCurrentUser();
        IntentFilter filter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction(ACTION_UPDATE_TWILIGHT_STATE);
        getContext().registerReceiver(this.mReceiver, filter);
        publishLocalService(TwilightManager.class, this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            getContext().getContentResolver().registerContentObserver(Secure.getUriFor("twilight_mode"), DEBUG, this.mContentObserver, this.mCurrentUser);
            this.mContentObserver.onChange(true);
            this.mBootCompleted = true;
            sendBroadcast();
        }
    }

    private void reregisterSettingObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.unregisterContentObserver(this.mContentObserver);
        contentResolver.registerContentObserver(Secure.getUriFor("twilight_mode"), DEBUG, this.mContentObserver, this.mCurrentUser);
        this.mContentObserver.onChange(true);
    }

    private void setLockedState(TwilightState state) {
        synchronized (this.mLock) {
            this.mLocked = DEBUG;
            setTwilightState(state);
            this.mLocked = true;
        }
    }

    private void setTwilightState(TwilightState state) {
        synchronized (this.mLock) {
            if (this.mLocked) {
                return;
            }
            if (!Objects.equal(this.mTwilightState, state)) {
                this.mTwilightState = state;
                int listenerLen = this.mListeners.size();
                for (int i = 0; i < listenerLen; i++) {
                    ((TwilightListenerRecord) this.mListeners.get(i)).postUpdate();
                }
            }
            sendBroadcast();
        }
    }

    private void sendBroadcast() {
        synchronized (this.mLock) {
            if (this.mTwilightState == null) {
                return;
            }
            if (this.mBootCompleted) {
                Intent intent = new Intent(ACTION_TWILIGHT_CHANGED);
                intent.putExtra(EXTRA_IS_NIGHT, this.mTwilightState.isNight());
                intent.putExtra(EXTRA_AMOUNT, this.mTwilightState.getAmount());
                intent.addFlags(1073741824);
                getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    private void scheduleReset() {
        long resetTime = System.currentTimeMillis() + TWILIGHT_ADJUSTMENT_TIME;
        Intent resetIntent = new Intent(ACTION_RESET_TWILIGHT_AUTO);
        resetIntent.putExtra(EXTRA_RESET_USER, this.mCurrentUser);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, resetIntent, 0);
        this.mAlarmManager.cancel(pendingIntent);
        this.mAlarmManager.setExact(1, resetTime, pendingIntent);
    }

    private static boolean hasMoved(Location from, Location to) {
        boolean z = true;
        if (to == null) {
            return DEBUG;
        }
        if (from == null) {
            return true;
        }
        if (to.getElapsedRealtimeNanos() < from.getElapsedRealtimeNanos()) {
            return DEBUG;
        }
        if (from.distanceTo(to) < from.getAccuracy() + to.getAccuracy()) {
            z = DEBUG;
        }
        return z;
    }
}
