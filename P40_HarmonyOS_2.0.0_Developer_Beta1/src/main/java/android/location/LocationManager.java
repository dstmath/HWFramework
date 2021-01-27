package android.location;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsMeasurementsEvent;
import android.location.GpsNavigationMessageEvent;
import android.location.GpsStatus;
import android.location.IGnssStatusListener;
import android.location.ILocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.location.ProviderProperties;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocationManager {
    public static final String EXTRA_PROVIDER_NAME = "android.location.extra.PROVIDER_NAME";
    public static final String FUSED_PROVIDER = "fused";
    public static final String GPS_PROVIDER = "gps";
    public static final String HIGH_POWER_REQUEST_CHANGE_ACTION = "android.location.HIGH_POWER_REQUEST_CHANGE";
    public static final String KEY_LOCATION_CHANGED = "location";
    public static final String KEY_PROVIDER_ENABLED = "providerEnabled";
    public static final String KEY_PROXIMITY_ENTERING = "entering";
    @Deprecated
    public static final String KEY_STATUS_CHANGED = "status";
    public static final String METADATA_SETTINGS_FOOTER_STRING = "com.android.settings.location.FOOTER_STRING";
    public static final String MODE_CHANGED_ACTION = "android.location.MODE_CHANGED";
    @Deprecated
    public static final String MODE_CHANGING_ACTION = "com.android.settings.location.MODE_CHANGING";
    public static final String NETWORK_PROVIDER = "network";
    public static final String PASSIVE_PROVIDER = "passive";
    public static final String PROVIDERS_CHANGED_ACTION = "android.location.PROVIDERS_CHANGED";
    public static final String SETTINGS_FOOTER_DISPLAYED_ACTION = "com.android.settings.location.DISPLAYED_FOOTER";
    public static final String SETTINGS_FOOTER_REMOVED_ACTION = "com.android.settings.location.REMOVED_FOOTER";
    private static final String TAG = "LocationManager";
    private final BatchedLocationCallbackTransport mBatchedLocationCallbackTransport;
    private final Context mContext;
    private final GnssMeasurementCallbackTransport mGnssMeasurementCallbackTransport;
    private final GnssNavigationMessageCallbackTransport mGnssNavigationMessageCallbackTransport;
    private final ArrayMap<OnNmeaMessageListener, GnssStatusListenerTransport> mGnssNmeaListeners = new ArrayMap<>();
    private volatile GnssStatus mGnssStatus;
    private final ArrayMap<GnssStatus.Callback, GnssStatusListenerTransport> mGnssStatusListeners = new ArrayMap<>();
    private final ArrayMap<GpsStatus.Listener, GnssStatusListenerTransport> mGpsStatusListeners = new ArrayMap<>();
    private final ArrayMap<LocationListener, ListenerTransport> mListeners = new ArrayMap<>();
    @UnsupportedAppUsage
    private final ILocationManager mService;
    private int mTimeToFirstFix;

    /* access modifiers changed from: private */
    public class ListenerTransport extends ILocationListener.Stub {
        private static final int TYPE_LOCATION_CHANGED = 1;
        private static final int TYPE_PROVIDER_DISABLED = 4;
        private static final int TYPE_PROVIDER_ENABLED = 3;
        private static final int TYPE_STATUS_CHANGED = 2;
        private LocationListener mListener;
        private final Handler mListenerHandler;

        ListenerTransport(LocationListener listener, Looper looper) {
            this.mListener = listener;
            if (looper == null) {
                this.mListenerHandler = new Handler(LocationManager.this) {
                    /* class android.location.LocationManager.ListenerTransport.AnonymousClass1 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        ListenerTransport.this._handleMessage(msg);
                    }
                };
            } else {
                this.mListenerHandler = new Handler(looper, LocationManager.this) {
                    /* class android.location.LocationManager.ListenerTransport.AnonymousClass2 */

                    @Override // android.os.Handler
                    public void handleMessage(Message msg) {
                        ListenerTransport.this._handleMessage(msg);
                    }
                };
            }
        }

        private boolean isThreadCanHandleMessage() {
            Thread.State state = this.mListenerHandler.getLooper().getThread().getState();
            if (state != Thread.State.TERMINATED && state != Thread.State.NEW && state != Thread.State.TIMED_WAITING && state != Thread.State.WAITING) {
                return true;
            }
            Log.e(LocationManager.TAG, "thread not runable, ignore msg, state:" + state + ", pkg:" + LocationManager.this.mContext.getPackageName());
            locationCallbackFinished();
            return false;
        }

        @Override // android.location.ILocationListener
        public void onLocationChanged(Location location) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = location;
            sendCallbackMessage(msg);
        }

        @Override // android.location.ILocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Message msg = Message.obtain();
            msg.what = 2;
            Bundle b = new Bundle();
            b.putString("provider", provider);
            b.putInt("status", status);
            if (extras != null) {
                b.putBundle("extras", extras);
            }
            msg.obj = b;
            sendCallbackMessage(msg);
        }

        @Override // android.location.ILocationListener
        public void onProviderEnabled(String provider) {
            Message msg = Message.obtain();
            msg.what = 3;
            msg.obj = provider;
            sendCallbackMessage(msg);
        }

        @Override // android.location.ILocationListener
        public void onProviderDisabled(String provider) {
            Message msg = Message.obtain();
            msg.what = 4;
            msg.obj = provider;
            sendCallbackMessage(msg);
        }

        private void sendCallbackMessage(Message msg) {
            if (isThreadCanHandleMessage() && !this.mListenerHandler.sendMessage(msg)) {
                locationCallbackFinished();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void _handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                this.mListener.onLocationChanged(new Location((Location) msg.obj));
            } else if (i == 2) {
                Bundle b = (Bundle) msg.obj;
                this.mListener.onStatusChanged(b.getString("provider"), b.getInt("status"), b.getBundle("extras"));
            } else if (i == 3) {
                this.mListener.onProviderEnabled((String) msg.obj);
            } else if (i == 4) {
                this.mListener.onProviderDisabled((String) msg.obj);
            }
            locationCallbackFinished();
        }

        private void locationCallbackFinished() {
            try {
                LocationManager.this.mService.locationCallbackFinished(this);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String[] getBackgroundThrottlingWhitelist() {
        try {
            return this.mService.getBackgroundThrottlingWhitelist();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String[] getIgnoreSettingsWhitelist() {
        try {
            return this.mService.getIgnoreSettingsWhitelist();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public LocationManager(Context context, ILocationManager service) {
        this.mService = service;
        this.mContext = context;
        this.mGnssMeasurementCallbackTransport = new GnssMeasurementCallbackTransport(this.mContext, this.mService);
        this.mGnssNavigationMessageCallbackTransport = new GnssNavigationMessageCallbackTransport(this.mContext, this.mService);
        this.mBatchedLocationCallbackTransport = new BatchedLocationCallbackTransport(this.mContext, this.mService);
    }

    private LocationProvider createProvider(String name, ProviderProperties properties) {
        return new LocationProvider(name, properties);
    }

    public List<String> getAllProviders() {
        try {
            return this.mService.getAllProviders();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getProviders(boolean enabledOnly) {
        try {
            return this.mService.getProviders(null, enabledOnly);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public LocationProvider getProvider(String name) {
        checkProvider(name);
        try {
            ProviderProperties properties = this.mService.getProviderProperties(name);
            if (properties == null) {
                return null;
            }
            return createProvider(name, properties);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        checkCriteria(criteria);
        try {
            return this.mService.getProviders(criteria, enabledOnly);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        checkCriteria(criteria);
        try {
            return this.mService.getBestProvider(criteria, enabledOnly);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
        checkProvider(provider);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, minTime, minDistance, false), listener, (Looper) null, (PendingIntent) null);
    }

    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper) {
        checkProvider(provider);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, minTime, minDistance, false), listener, looper, (PendingIntent) null);
    }

    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper) {
        checkCriteria(criteria);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, minTime, minDistance, false), listener, looper, (PendingIntent) null);
    }

    public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent) {
        checkProvider(provider);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, minTime, minDistance, false), (LocationListener) null, (Looper) null, intent);
    }

    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent) {
        checkCriteria(criteria);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, minTime, minDistance, false), (LocationListener) null, (Looper) null, intent);
    }

    public void requestSingleUpdate(String provider, LocationListener listener, Looper looper) {
        checkProvider(provider);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, 0, 0.0f, true), listener, looper, (PendingIntent) null);
    }

    public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper) {
        checkCriteria(criteria);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, 0, 0.0f, true), listener, looper, (PendingIntent) null);
    }

    public void requestSingleUpdate(String provider, PendingIntent intent) {
        checkProvider(provider);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, 0, 0.0f, true), (LocationListener) null, (Looper) null, intent);
    }

    public void requestSingleUpdate(Criteria criteria, PendingIntent intent) {
        checkCriteria(criteria);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, 0, 0.0f, true), (LocationListener) null, (Looper) null, intent);
    }

    @SystemApi
    public void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper) {
        checkListener(listener);
        requestLocationUpdates(request, listener, looper, (PendingIntent) null);
    }

    @SystemApi
    public void requestLocationUpdates(LocationRequest request, PendingIntent intent) {
        checkPendingIntent(intent);
        requestLocationUpdates(request, (LocationListener) null, (Looper) null, intent);
    }

    public boolean injectLocation(Location newLocation) {
        try {
            return this.mService.injectLocation(newLocation);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private ListenerTransport wrapListener(LocationListener listener, Looper looper) {
        ListenerTransport transport;
        if (listener == null) {
            return null;
        }
        synchronized (this.mListeners) {
            transport = this.mListeners.get(listener);
            if (transport == null) {
                transport = new ListenerTransport(listener, looper);
            }
            this.mListeners.put(listener, transport);
        }
        return transport;
    }

    @UnsupportedAppUsage
    private void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper, PendingIntent intent) {
        String packageName = this.mContext.getPackageName();
        try {
            this.mService.requestLocationUpdates(request, wrapListener(listener, looper), intent, packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeUpdates(LocationListener listener) {
        ListenerTransport transport;
        checkListener(listener);
        String packageName = this.mContext.getPackageName();
        synchronized (this.mListeners) {
            transport = this.mListeners.remove(listener);
        }
        if (transport != null) {
            try {
                this.mService.removeUpdates(transport, null, packageName);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void removeUpdates(PendingIntent intent) {
        checkPendingIntent(intent);
        try {
            this.mService.removeUpdates(null, intent, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent) {
        checkPendingIntent(intent);
        if (expiration < 0) {
            expiration = Long.MAX_VALUE;
        }
        Geofence fence = Geofence.createCircle(latitude, longitude, radius);
        try {
            this.mService.requestGeofence(new LocationRequest().setExpireIn(expiration), fence, intent, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addGeofence(LocationRequest request, Geofence fence, PendingIntent intent) {
        checkPendingIntent(intent);
        checkGeofence(fence);
        try {
            this.mService.requestGeofence(request, fence, intent, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeProximityAlert(PendingIntent intent) {
        checkPendingIntent(intent);
        try {
            this.mService.removeGeofence(null, intent, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeGeofence(Geofence fence, PendingIntent intent) {
        checkPendingIntent(intent);
        checkGeofence(fence);
        try {
            this.mService.removeGeofence(fence, intent, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeAllGeofences(PendingIntent intent) {
        checkPendingIntent(intent);
        try {
            this.mService.removeGeofence(null, intent, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isLocationEnabled() {
        return isLocationEnabledForUser(Process.myUserHandle());
    }

    @SystemApi
    public boolean isLocationEnabledForUser(UserHandle userHandle) {
        try {
            return this.mService.isLocationEnabledForUser(userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setLocationEnabledForUser(boolean enabled, UserHandle userHandle) {
        int i;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (enabled) {
            i = 3;
        } else {
            i = 0;
        }
        Settings.Secure.putIntForUser(contentResolver, Settings.Secure.LOCATION_MODE, i, userHandle.getIdentifier());
    }

    public boolean isProviderEnabled(String provider) {
        return isProviderEnabledForUser(provider, Process.myUserHandle());
    }

    @SystemApi
    public boolean isProviderEnabledForUser(String provider, UserHandle userHandle) {
        checkProvider(provider);
        try {
            return this.mService.isProviderEnabledForUser(provider, userHandle.getIdentifier());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @Deprecated
    public boolean setProviderEnabledForUser(String provider, boolean enabled, UserHandle userHandle) {
        checkProvider(provider);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        StringBuilder sb = new StringBuilder();
        sb.append(enabled ? HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX : NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
        sb.append(provider);
        return Settings.Secure.putStringForUser(contentResolver, "location_providers_allowed", sb.toString(), userHandle.getIdentifier());
    }

    public Location getLastLocation() {
        try {
            return this.mService.getLastLocation(null, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Location getLastKnownLocation(String provider) {
        checkProvider(provider);
        String packageName = this.mContext.getPackageName();
        try {
            return this.mService.getLastLocation(LocationRequest.createFromDeprecatedProvider(provider, 0, 0.0f, true), packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addTestProvider(String name, boolean requiresNetwork, boolean requiresSatellite, boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude, boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy) {
        ProviderProperties properties = new ProviderProperties(requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, supportsAltitude, supportsSpeed, supportsBearing, powerRequirement, accuracy);
        if (!name.matches(LocationProvider.BAD_CHARS_REGEX)) {
            try {
                this.mService.addTestProvider(name, properties, this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("provider name contains illegal character: " + name);
        }
    }

    public void removeTestProvider(String provider) {
        try {
            this.mService.removeTestProvider(provider, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTestProviderLocation(String provider, Location loc) {
        if (!loc.isComplete()) {
            IllegalArgumentException e = new IllegalArgumentException("Incomplete location object, missing timestamp or accuracy? " + loc);
            if (this.mContext.getApplicationInfo().targetSdkVersion <= 16) {
                Log.w(TAG, e);
                loc.makeComplete();
            } else {
                throw e;
            }
        }
        try {
            this.mService.setTestProviderLocation(provider, loc, this.mContext.getOpPackageName());
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void clearTestProviderLocation(String provider) {
    }

    public void setTestProviderEnabled(String provider, boolean enabled) {
        try {
            this.mService.setTestProviderEnabled(provider, enabled, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void clearTestProviderEnabled(String provider) {
        setTestProviderEnabled(provider, false);
    }

    @Deprecated
    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime) {
        try {
            this.mService.setTestProviderStatus(provider, status, extras, updateTime, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void clearTestProviderStatus(String provider) {
        setTestProviderStatus(provider, 2, null, 0);
    }

    public List<LocationRequest> getTestProviderCurrentRequests(String providerName) {
        checkProvider(providerName);
        try {
            return this.mService.getTestProviderCurrentRequests(providerName, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public class GnssStatusListenerTransport extends IGnssStatusListener.Stub {
        private static final int GNSS_EVENT_FIRST_FIX = 4;
        private static final int GNSS_EVENT_SATELLITE_STATUS = 5;
        private static final int GNSS_EVENT_STARTED = 2;
        private static final int GNSS_EVENT_STOPPED = 3;
        private static final int NMEA_RECEIVED = 1;
        private final GnssStatus.Callback mGnssCallback;
        private final Handler mGnssHandler;
        private final OnNmeaMessageListener mGnssNmeaListener;
        private final ArrayList<Nmea> mNmeaBuffer;

        private class GnssHandler extends Handler {
            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            GnssHandler(Handler handler) {
                super(handler != null ? handler.getLooper() : Looper.myLooper());
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    synchronized (GnssStatusListenerTransport.this.mNmeaBuffer) {
                        Iterator it = GnssStatusListenerTransport.this.mNmeaBuffer.iterator();
                        while (it.hasNext()) {
                            Nmea nmea = (Nmea) it.next();
                            GnssStatusListenerTransport.this.mGnssNmeaListener.onNmeaMessage(nmea.mNmea, nmea.mTimestamp);
                        }
                        GnssStatusListenerTransport.this.mNmeaBuffer.clear();
                    }
                } else if (i == 2) {
                    GnssStatusListenerTransport.this.mGnssCallback.onStarted();
                } else if (i == 3) {
                    GnssStatusListenerTransport.this.mGnssCallback.onStopped();
                } else if (i == 4) {
                    GnssStatusListenerTransport.this.mGnssCallback.onFirstFix(LocationManager.this.mTimeToFirstFix);
                } else if (i == 5) {
                    GnssStatusListenerTransport.this.mGnssCallback.onSatelliteStatusChanged(LocationManager.this.mGnssStatus);
                }
            }
        }

        /* access modifiers changed from: private */
        public class Nmea {
            String mNmea;
            long mTimestamp;

            Nmea(long timestamp, String nmea) {
                this.mTimestamp = timestamp;
                this.mNmea = nmea;
            }
        }

        GnssStatusListenerTransport(GnssStatus.Callback callback, Handler handler) {
            this.mGnssCallback = callback;
            this.mGnssHandler = new GnssHandler(handler);
            this.mGnssNmeaListener = null;
            this.mNmeaBuffer = null;
        }

        GnssStatusListenerTransport(OnNmeaMessageListener listener, Handler handler) {
            this.mGnssCallback = null;
            this.mGnssHandler = new GnssHandler(handler);
            this.mGnssNmeaListener = listener;
            this.mNmeaBuffer = new ArrayList<>();
        }

        GnssStatusListenerTransport(final GpsStatus.Listener listener, Handler handler) {
            this.mGnssHandler = new GnssHandler(handler);
            this.mNmeaBuffer = null;
            this.mGnssCallback = listener != null ? new GnssStatus.Callback(LocationManager.this) {
                /* class android.location.LocationManager.GnssStatusListenerTransport.AnonymousClass1 */

                @Override // android.location.GnssStatus.Callback
                public void onStarted() {
                    listener.onGpsStatusChanged(1);
                }

                @Override // android.location.GnssStatus.Callback
                public void onStopped() {
                    listener.onGpsStatusChanged(2);
                }

                @Override // android.location.GnssStatus.Callback
                public void onFirstFix(int ttff) {
                    listener.onGpsStatusChanged(3);
                }

                @Override // android.location.GnssStatus.Callback
                public void onSatelliteStatusChanged(GnssStatus status) {
                    listener.onGpsStatusChanged(4);
                }
            } : null;
            this.mGnssNmeaListener = null;
        }

        @Override // android.location.IGnssStatusListener
        public void onGnssStarted() {
            if (this.mGnssCallback != null) {
                this.mGnssHandler.obtainMessage(2).sendToTarget();
            }
        }

        @Override // android.location.IGnssStatusListener
        public void onGnssStopped() {
            if (this.mGnssCallback != null) {
                this.mGnssHandler.obtainMessage(3).sendToTarget();
            }
        }

        @Override // android.location.IGnssStatusListener
        public void onFirstFix(int ttff) {
            if (this.mGnssCallback != null) {
                LocationManager.this.mTimeToFirstFix = ttff;
                this.mGnssHandler.obtainMessage(4).sendToTarget();
            }
        }

        @Override // android.location.IGnssStatusListener
        public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths, float[] carrierFreqs) {
            if (this.mGnssCallback != null) {
                LocationManager.this.mGnssStatus = new GnssStatus(svCount, prnWithFlags, cn0s, elevations, azimuths, carrierFreqs);
                this.mGnssHandler.removeMessages(5);
                this.mGnssHandler.obtainMessage(5).sendToTarget();
            }
        }

        @Override // android.location.IGnssStatusListener
        public void onNmeaReceived(long timestamp, String nmea) {
            if (this.mGnssNmeaListener != null) {
                synchronized (this.mNmeaBuffer) {
                    this.mNmeaBuffer.add(new Nmea(timestamp, nmea));
                }
                this.mGnssHandler.removeMessages(1);
                this.mGnssHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    @Deprecated
    public boolean addGpsStatusListener(GpsStatus.Listener listener) {
        if (this.mGpsStatusListeners.get(listener) != null) {
            return true;
        }
        try {
            GnssStatusListenerTransport transport = new GnssStatusListenerTransport(listener, (Handler) null);
            boolean result = this.mService.registerGnssStatusCallback(transport, this.mContext.getPackageName());
            if (result) {
                this.mGpsStatusListeners.put(listener, transport);
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void removeGpsStatusListener(GpsStatus.Listener listener) {
        try {
            GnssStatusListenerTransport transport = this.mGpsStatusListeners.remove(listener);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean registerGnssStatusCallback(GnssStatus.Callback callback) {
        return registerGnssStatusCallback(callback, null);
    }

    public boolean registerGnssStatusCallback(GnssStatus.Callback callback, Handler handler) {
        synchronized (this.mGnssStatusListeners) {
            if (this.mGnssStatusListeners.get(callback) != null) {
                return true;
            }
            try {
                GnssStatusListenerTransport transport = new GnssStatusListenerTransport(callback, handler);
                boolean result = this.mService.registerGnssStatusCallback(transport, this.mContext.getPackageName());
                if (result) {
                    this.mGnssStatusListeners.put(callback, transport);
                }
                return result;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void unregisterGnssStatusCallback(GnssStatus.Callback callback) {
        synchronized (this.mGnssStatusListeners) {
            try {
                GnssStatusListenerTransport transport = this.mGnssStatusListeners.remove(callback);
                if (transport != null) {
                    this.mService.unregisterGnssStatusCallback(transport);
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    @Deprecated
    public boolean addNmeaListener(GpsStatus.NmeaListener listener) {
        return false;
    }

    @Deprecated
    public void removeNmeaListener(GpsStatus.NmeaListener listener) {
    }

    public boolean addNmeaListener(OnNmeaMessageListener listener) {
        return addNmeaListener(listener, null);
    }

    public boolean addNmeaListener(OnNmeaMessageListener listener, Handler handler) {
        if (this.mGnssNmeaListeners.get(listener) != null) {
            return true;
        }
        try {
            GnssStatusListenerTransport transport = new GnssStatusListenerTransport(listener, handler);
            boolean result = this.mService.registerGnssStatusCallback(transport, this.mContext.getPackageName());
            if (result) {
                this.mGnssNmeaListeners.put(listener, transport);
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeNmeaListener(OnNmeaMessageListener listener) {
        try {
            GnssStatusListenerTransport transport = this.mGnssNmeaListeners.remove(listener);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    @Deprecated
    public boolean addGpsMeasurementListener(GpsMeasurementsEvent.Listener listener) {
        return false;
    }

    public boolean registerGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback) {
        return registerGnssMeasurementsCallback(callback, null);
    }

    public boolean registerGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback, Handler handler) {
        return this.mGnssMeasurementCallbackTransport.add(callback, handler);
    }

    @SystemApi
    public void injectGnssMeasurementCorrections(GnssMeasurementCorrections measurementCorrections) {
        try {
            this.mGnssMeasurementCallbackTransport.injectGnssMeasurementCorrections(measurementCorrections);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public GnssCapabilities getGnssCapabilities() {
        try {
            long gnssCapabilities = this.mGnssMeasurementCallbackTransport.getGnssCapabilities();
            if (gnssCapabilities == -1) {
                gnssCapabilities = 0;
            }
            return GnssCapabilities.of(gnssCapabilities);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    @Deprecated
    public void removeGpsMeasurementListener(GpsMeasurementsEvent.Listener listener) {
    }

    public void unregisterGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback) {
        this.mGnssMeasurementCallbackTransport.remove(callback);
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    @Deprecated
    public boolean addGpsNavigationMessageListener(GpsNavigationMessageEvent.Listener listener) {
        return false;
    }

    @SystemApi
    @SuppressLint({"Doclava125"})
    @Deprecated
    public void removeGpsNavigationMessageListener(GpsNavigationMessageEvent.Listener listener) {
    }

    public boolean registerGnssNavigationMessageCallback(GnssNavigationMessage.Callback callback) {
        return registerGnssNavigationMessageCallback(callback, null);
    }

    public boolean registerGnssNavigationMessageCallback(GnssNavigationMessage.Callback callback, Handler handler) {
        return this.mGnssNavigationMessageCallbackTransport.add(callback, handler);
    }

    public void unregisterGnssNavigationMessageCallback(GnssNavigationMessage.Callback callback) {
        this.mGnssNavigationMessageCallbackTransport.remove(callback);
    }

    @Deprecated
    public GpsStatus getGpsStatus(GpsStatus status) {
        if (status == null) {
            status = new GpsStatus();
        }
        if (this.mGnssStatus != null) {
            status.setStatus(this.mGnssStatus, this.mTimeToFirstFix);
        }
        return status;
    }

    public int getGnssYearOfHardware() {
        try {
            return this.mService.getGnssYearOfHardware();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getGnssHardwareModelName() {
        try {
            return this.mService.getGnssHardwareModelName();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getGnssBatchSize() {
        try {
            return this.mService.getGnssBatchSize(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean registerGnssBatchedLocationCallback(long periodNanos, boolean wakeOnFifoFull, BatchedLocationCallback callback, Handler handler) {
        this.mBatchedLocationCallbackTransport.add(callback, handler);
        try {
            return this.mService.startGnssBatch(periodNanos, wakeOnFifoFull, this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void flushGnssBatch() {
        try {
            this.mService.flushGnssBatch(this.mContext.getPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean unregisterGnssBatchedLocationCallback(BatchedLocationCallback callback) {
        this.mBatchedLocationCallbackTransport.remove(callback);
        try {
            return this.mService.stopGnssBatch();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        try {
            return this.mService.sendExtraCommand(provider, command, extras);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public boolean sendNiResponse(int notifId, int userResponse) {
        try {
            return this.mService.sendNiResponse(notifId, userResponse);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static void checkProvider(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("invalid provider: " + provider);
        }
    }

    private static void checkCriteria(Criteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("invalid criteria: " + criteria);
        }
    }

    private static void checkListener(LocationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("invalid listener: " + listener);
        }
    }

    private void checkPendingIntent(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("invalid pending intent: " + intent);
        } else if (!intent.isTargetedToPackage()) {
            IllegalArgumentException e = new IllegalArgumentException("pending intent must be targeted to package");
            if (this.mContext.getApplicationInfo().targetSdkVersion <= 16) {
                Log.w(TAG, e);
                return;
            }
            throw e;
        }
    }

    private static void checkGeofence(Geofence fence) {
        if (fence == null) {
            throw new IllegalArgumentException("invalid geofence: " + fence);
        }
    }

    @SystemApi
    public boolean isProviderPackage(String packageName) {
        try {
            return this.mService.isProviderPackage(packageName);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return false;
        }
    }

    @SystemApi
    public void setExtraLocationControllerPackage(String packageName) {
        try {
            this.mService.setExtraLocationControllerPackage(packageName);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    @Deprecated
    public void setLocationControllerExtraPackage(String packageName) {
        try {
            this.mService.setExtraLocationControllerPackage(packageName);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public String getExtraLocationControllerPackage() {
        try {
            return this.mService.getExtraLocationControllerPackage();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return null;
        }
    }

    @SystemApi
    @Deprecated
    public void setLocationControllerExtraPackageEnabled(boolean enabled) {
        try {
            this.mService.setExtraLocationControllerPackageEnabled(enabled);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setExtraLocationControllerPackageEnabled(boolean enabled) {
        try {
            this.mService.setExtraLocationControllerPackageEnabled(enabled);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean isExtraLocationControllerPackageEnabled() {
        try {
            return this.mService.isExtraLocationControllerPackageEnabled();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return false;
        }
    }
}
