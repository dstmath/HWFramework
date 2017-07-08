package android.location;

import android.app.PendingIntent;
import android.content.Context;
import android.location.GnssStatus.Callback;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.location.IGnssStatusListener.Stub;
import android.net.LinkQualityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;
import com.android.internal.location.ProviderProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationManager {
    public static final String EXTRA_GPS_ENABLED = "enabled";
    public static final String FUSED_PROVIDER = "fused";
    public static final String GPS_ENABLED_CHANGE_ACTION = "android.location.GPS_ENABLED_CHANGE";
    public static final String GPS_FIX_CHANGE_ACTION = "android.location.GPS_FIX_CHANGE";
    public static final String GPS_PROVIDER = "gps";
    public static final String HIGH_POWER_REQUEST_CHANGE_ACTION = "android.location.HIGH_POWER_REQUEST_CHANGE";
    public static final String KEY_LOCATION_CHANGED = "location";
    public static final String KEY_PROVIDER_ENABLED = "providerEnabled";
    public static final String KEY_PROXIMITY_ENTERING = "entering";
    public static final String KEY_STATUS_CHANGED = "status";
    public static final String MODE_CHANGED_ACTION = "android.location.MODE_CHANGED";
    public static final String NETWORK_PROVIDER = "network";
    public static final String PASSIVE_PROVIDER = "passive";
    public static final String PROVIDERS_CHANGED_ACTION = "android.location.PROVIDERS_CHANGED";
    private static final String TAG = "LocationManager";
    private final Context mContext;
    private final GnssMeasurementCallbackTransport mGnssMeasurementCallbackTransport;
    private final GnssNavigationMessageCallbackTransport mGnssNavigationMessageCallbackTransport;
    private final HashMap<OnNmeaMessageListener, GnssStatusListenerTransport> mGnssNmeaListeners;
    private GnssStatus mGnssStatus;
    private final HashMap<Callback, GnssStatusListenerTransport> mGnssStatusListeners;
    private final HashMap<NmeaListener, GnssStatusListenerTransport> mGpsNmeaListeners;
    private final HashMap<Listener, GnssStatusListenerTransport> mGpsStatusListeners;
    private HashMap<LocationListener, ListenerTransport> mListeners;
    private final HashMap<GnssNavigationMessageEvent.Callback, GnssNavigationMessage.Callback> mNavigationMessageBridge;
    private final HashMap<GnssNmeaListener, GnssStatusListenerTransport> mOldGnssNmeaListeners;
    private final HashMap<GnssStatusCallback, GnssStatusListenerTransport> mOldGnssStatusListeners;
    private final ILocationManager mService;
    private int mTimeToFirstFix;

    /* renamed from: android.location.LocationManager.1 */
    class AnonymousClass1 extends GnssNavigationMessage.Callback {
        final /* synthetic */ GnssNavigationMessageEvent.Callback val$callback;

        AnonymousClass1(GnssNavigationMessageEvent.Callback val$callback) {
            this.val$callback = val$callback;
        }

        public void onGnssNavigationMessageReceived(GnssNavigationMessage message) {
            this.val$callback.onGnssNavigationMessageReceived(new GnssNavigationMessageEvent(message));
        }

        public void onStatusChanged(int status) {
            this.val$callback.onStatusChanged(status);
        }
    }

    private class GnssStatusListenerTransport extends Stub {
        private static final int NMEA_RECEIVED = 1000;
        private final Callback mGnssCallback;
        private final Handler mGnssHandler;
        private final OnNmeaMessageListener mGnssNmeaListener;
        private final Listener mGpsListener;
        private final NmeaListener mGpsNmeaListener;
        private final ArrayList<Nmea> mNmeaBuffer;
        private final GnssStatusCallback mOldGnssCallback;
        private final GnssNmeaListener mOldGnssNmeaListener;

        private class GnssHandler extends Handler {
            public GnssHandler(Handler handler) {
                super(handler != null ? handler.getLooper() : Looper.myLooper());
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case AudioState.ROUTE_EARPIECE /*1*/:
                        GnssStatusListenerTransport.this.mGnssCallback.onStarted();
                    case AudioState.ROUTE_BLUETOOTH /*2*/:
                        GnssStatusListenerTransport.this.mGnssCallback.onStopped();
                    case Engine.DEFAULT_STREAM /*3*/:
                        GnssStatusListenerTransport.this.mGnssCallback.onFirstFix(LocationManager.this.mTimeToFirstFix);
                    case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                        GnssStatusListenerTransport.this.mGnssCallback.onSatelliteStatusChanged(LocationManager.this.mGnssStatus);
                    case GnssStatusListenerTransport.NMEA_RECEIVED /*1000*/:
                        ArrayList<Nmea> tempNmeaBuffer;
                        synchronized (GnssStatusListenerTransport.this.mNmeaBuffer) {
                            tempNmeaBuffer = (ArrayList) GnssStatusListenerTransport.this.mNmeaBuffer.clone();
                            GnssStatusListenerTransport.this.mNmeaBuffer.clear();
                            break;
                        }
                        if (tempNmeaBuffer != null) {
                            int length = tempNmeaBuffer.size();
                            for (int i = 0; i < length; i++) {
                                Nmea nmea = (Nmea) tempNmeaBuffer.get(i);
                                GnssStatusListenerTransport.this.mGnssNmeaListener.onNmeaMessage(nmea.mNmea, nmea.mTimestamp);
                            }
                        }
                    default:
                }
            }
        }

        private class Nmea {
            String mNmea;
            long mTimestamp;

            Nmea(long timestamp, String nmea) {
                this.mTimestamp = timestamp;
                this.mNmea = nmea;
            }
        }

        GnssStatusListenerTransport(LocationManager this$0, Listener listener) {
            this(listener, null);
        }

        GnssStatusListenerTransport(Listener listener, Handler handler) {
            this.mGpsListener = listener;
            this.mGnssHandler = new GnssHandler(handler);
            this.mGpsNmeaListener = null;
            this.mNmeaBuffer = null;
            this.mOldGnssCallback = null;
            this.mGnssCallback = new Callback() {
                public void onStarted() {
                    GnssStatusListenerTransport.this.mGpsListener.onGpsStatusChanged(1);
                }

                public void onStopped() {
                    GnssStatusListenerTransport.this.mGpsListener.onGpsStatusChanged(2);
                }

                public void onFirstFix(int ttff) {
                    GnssStatusListenerTransport.this.mGpsListener.onGpsStatusChanged(3);
                }

                public void onSatelliteStatusChanged(GnssStatus status) {
                    GnssStatusListenerTransport.this.mGpsListener.onGpsStatusChanged(4);
                }
            };
            this.mOldGnssNmeaListener = null;
            this.mGnssNmeaListener = null;
        }

        GnssStatusListenerTransport(LocationManager this$0, NmeaListener listener) {
            this(listener, null);
        }

        GnssStatusListenerTransport(NmeaListener listener, Handler handler) {
            this.mGpsListener = null;
            this.mGnssHandler = new GnssHandler(handler);
            this.mGpsNmeaListener = listener;
            this.mNmeaBuffer = new ArrayList();
            this.mOldGnssCallback = null;
            this.mGnssCallback = null;
            this.mOldGnssNmeaListener = null;
            this.mGnssNmeaListener = new OnNmeaMessageListener() {
                public void onNmeaMessage(String nmea, long timestamp) {
                    GnssStatusListenerTransport.this.mGpsNmeaListener.onNmeaReceived(timestamp, nmea);
                }
            };
        }

        GnssStatusListenerTransport(LocationManager this$0, GnssStatusCallback callback) {
            this(callback, null);
        }

        GnssStatusListenerTransport(GnssStatusCallback callback, Handler handler) {
            this.mOldGnssCallback = callback;
            this.mGnssCallback = new Callback() {
                public void onStarted() {
                    GnssStatusListenerTransport.this.mOldGnssCallback.onStarted();
                }

                public void onStopped() {
                    GnssStatusListenerTransport.this.mOldGnssCallback.onStopped();
                }

                public void onFirstFix(int ttff) {
                    GnssStatusListenerTransport.this.mOldGnssCallback.onFirstFix(ttff);
                }

                public void onSatelliteStatusChanged(GnssStatus status) {
                    GnssStatusListenerTransport.this.mOldGnssCallback.onSatelliteStatusChanged(status);
                }
            };
            this.mGnssHandler = new GnssHandler(handler);
            this.mOldGnssNmeaListener = null;
            this.mGnssNmeaListener = null;
            this.mNmeaBuffer = null;
            this.mGpsListener = null;
            this.mGpsNmeaListener = null;
        }

        GnssStatusListenerTransport(LocationManager this$0, Callback callback) {
            this(callback, null);
        }

        GnssStatusListenerTransport(Callback callback, Handler handler) {
            this.mOldGnssCallback = null;
            this.mGnssCallback = callback;
            this.mGnssHandler = new GnssHandler(handler);
            this.mOldGnssNmeaListener = null;
            this.mGnssNmeaListener = null;
            this.mNmeaBuffer = null;
            this.mGpsListener = null;
            this.mGpsNmeaListener = null;
        }

        GnssStatusListenerTransport(LocationManager this$0, GnssNmeaListener listener) {
            this(listener, null);
        }

        GnssStatusListenerTransport(GnssNmeaListener listener, Handler handler) {
            this.mGnssCallback = null;
            this.mOldGnssCallback = null;
            this.mGnssHandler = new GnssHandler(handler);
            this.mOldGnssNmeaListener = listener;
            this.mGnssNmeaListener = new OnNmeaMessageListener() {
                public void onNmeaMessage(String message, long timestamp) {
                    GnssStatusListenerTransport.this.mOldGnssNmeaListener.onNmeaReceived(timestamp, message);
                }
            };
            this.mGpsListener = null;
            this.mGpsNmeaListener = null;
            this.mNmeaBuffer = new ArrayList();
        }

        GnssStatusListenerTransport(LocationManager this$0, OnNmeaMessageListener listener) {
            this(listener, null);
        }

        GnssStatusListenerTransport(OnNmeaMessageListener listener, Handler handler) {
            this.mOldGnssCallback = null;
            this.mGnssCallback = null;
            this.mGnssHandler = new GnssHandler(handler);
            this.mOldGnssNmeaListener = null;
            this.mGnssNmeaListener = listener;
            this.mGpsListener = null;
            this.mGpsNmeaListener = null;
            this.mNmeaBuffer = new ArrayList();
        }

        public void onGnssStarted() {
            if (this.mGpsListener != null) {
                Message msg = Message.obtain();
                msg.what = 1;
                if (!this.mGnssHandler.sendMessage(msg)) {
                    Log.w(LocationManager.TAG, "looper is quiting when onGnssStarted called.");
                    removeListener();
                }
            }
        }

        public void onGnssStopped() {
            if (this.mGpsListener != null) {
                Message msg = Message.obtain();
                msg.what = 2;
                if (!this.mGnssHandler.sendMessage(msg)) {
                    Log.w(LocationManager.TAG, "looper is quiting when onGnssStopped called.");
                    removeListener();
                }
            }
        }

        public void onFirstFix(int ttff) {
            if (this.mGpsListener != null) {
                LocationManager.this.mTimeToFirstFix = ttff;
                Message msg = Message.obtain();
                msg.what = 3;
                if (!this.mGnssHandler.sendMessage(msg)) {
                    Log.w(LocationManager.TAG, "looper is quiting when onFirstFix called.");
                    removeListener();
                }
            }
        }

        public void onSvStatusChanged(int svCount, int[] prnWithFlags, float[] cn0s, float[] elevations, float[] azimuths) {
            if (this.mGnssCallback != null) {
                LocationManager.this.mGnssStatus = new GnssStatus(svCount, prnWithFlags, cn0s, elevations, azimuths);
                Message msg = Message.obtain();
                msg.what = 4;
                this.mGnssHandler.removeMessages(4);
                if (!this.mGnssHandler.sendMessage(msg)) {
                    Log.w(LocationManager.TAG, "looper is quiting when onSvStatusChanged called.");
                    removeListener();
                }
            }
        }

        public void onNmeaReceived(long timestamp, String nmea) {
            if (this.mGnssNmeaListener != null) {
                synchronized (this.mNmeaBuffer) {
                    this.mNmeaBuffer.add(new Nmea(timestamp, nmea));
                }
                Message msg = Message.obtain();
                msg.what = NMEA_RECEIVED;
                this.mGnssHandler.removeMessages(NMEA_RECEIVED);
                if (!this.mGnssHandler.sendMessage(msg)) {
                    Log.w(LocationManager.TAG, "looper is quiting when onNmeaReceived called.");
                    removeListener();
                }
            }
        }

        private void removeListener() {
            if (this.mGpsListener != null) {
                LocationManager.this.removeGpsStatusListener(this.mGpsListener);
            }
            if (this.mOldGnssNmeaListener != null) {
                LocationManager.this.removeNmeaListener(this.mOldGnssNmeaListener);
            }
            if (this.mGpsNmeaListener != null) {
                LocationManager.this.removeNmeaListener(this.mGpsNmeaListener);
            }
            if (this.mGnssNmeaListener != null) {
                LocationManager.this.removeNmeaListener(this.mGnssNmeaListener);
            }
            if (this.mGnssCallback != null) {
                LocationManager.this.unregisterGnssStatusCallback(this.mGnssCallback);
            }
            if (this.mOldGnssCallback != null) {
                LocationManager.this.unregisterGnssStatusCallback(this.mOldGnssCallback);
            }
        }
    }

    private class ListenerTransport extends ILocationListener.Stub {
        private static final int TYPE_LOCATION_CHANGED = 1;
        private static final int TYPE_PROVIDER_DISABLED = 4;
        private static final int TYPE_PROVIDER_ENABLED = 3;
        private static final int TYPE_STATUS_CHANGED = 2;
        private LocationListener mListener;
        private final Handler mListenerHandler;

        /* renamed from: android.location.LocationManager.ListenerTransport.2 */
        class AnonymousClass2 extends Handler {
            AnonymousClass2(Looper $anonymous0) {
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                ListenerTransport.this._handleMessage(msg);
            }
        }

        ListenerTransport(LocationListener listener, Looper looper) {
            this.mListener = listener;
            if (looper == null) {
                this.mListenerHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        ListenerTransport.this._handleMessage(msg);
                    }
                };
            } else {
                this.mListenerHandler = new AnonymousClass2(looper);
            }
        }

        public void onLocationChanged(Location location) {
            Message msg = Message.obtain();
            msg.what = TYPE_LOCATION_CHANGED;
            msg.obj = location;
            if (!this.mListenerHandler.sendMessage(msg)) {
                Log.e(LocationManager.TAG, "onLocationChanged: handler quitting,remove the listener. " + LocationManager.this.mContext.getPackageName());
                LocationManager.this.removeUpdates(this.mListener);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Message msg = Message.obtain();
            msg.what = TYPE_STATUS_CHANGED;
            Bundle b = new Bundle();
            b.putString(LaunchMode.PROVIDER, provider);
            b.putInt(LocationManager.KEY_STATUS_CHANGED, status);
            if (extras != null) {
                b.putBundle("extras", extras);
            }
            msg.obj = b;
            if (!this.mListenerHandler.sendMessage(msg)) {
                Log.e(LocationManager.TAG, "onStatusChanged: handler quitting,remove the listener. " + LocationManager.this.mContext.getPackageName());
                LocationManager.this.removeUpdates(this.mListener);
            }
        }

        public void onProviderEnabled(String provider) {
            Message msg = Message.obtain();
            msg.what = TYPE_PROVIDER_ENABLED;
            msg.obj = provider;
            if (!this.mListenerHandler.sendMessage(msg)) {
                Log.e(LocationManager.TAG, "onProviderEnabled: handler quitting,remove the listener. " + LocationManager.this.mContext.getPackageName());
                LocationManager.this.removeUpdates(this.mListener);
            }
        }

        public void onProviderDisabled(String provider) {
            Message msg = Message.obtain();
            msg.what = TYPE_PROVIDER_DISABLED;
            msg.obj = provider;
            if (!this.mListenerHandler.sendMessage(msg)) {
                Log.e(LocationManager.TAG, "onProviderDisabled: handler quitting,remove the listener. " + LocationManager.this.mContext.getPackageName());
                LocationManager.this.removeUpdates(this.mListener);
            }
        }

        private void _handleMessage(Message msg) {
            switch (msg.what) {
                case TYPE_LOCATION_CHANGED /*1*/:
                    this.mListener.onLocationChanged(new Location((Location) msg.obj));
                    break;
                case TYPE_STATUS_CHANGED /*2*/:
                    Bundle b = msg.obj;
                    this.mListener.onStatusChanged(b.getString(LaunchMode.PROVIDER), b.getInt(LocationManager.KEY_STATUS_CHANGED), b.getBundle("extras"));
                    break;
                case TYPE_PROVIDER_ENABLED /*3*/:
                    this.mListener.onProviderEnabled((String) msg.obj);
                    break;
                case TYPE_PROVIDER_DISABLED /*4*/:
                    this.mListener.onProviderDisabled((String) msg.obj);
                    break;
            }
            try {
                LocationManager.this.mService.locationCallbackFinished(this);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public LocationManager(Context context, ILocationManager service) {
        this.mGpsStatusListeners = new HashMap();
        this.mGpsNmeaListeners = new HashMap();
        this.mOldGnssStatusListeners = new HashMap();
        this.mGnssStatusListeners = new HashMap();
        this.mOldGnssNmeaListeners = new HashMap();
        this.mGnssNmeaListeners = new HashMap();
        this.mNavigationMessageBridge = new HashMap();
        this.mListeners = new HashMap();
        this.mService = service;
        this.mContext = context;
        this.mGnssMeasurementCallbackTransport = new GnssMeasurementCallbackTransport(this.mContext, this.mService);
        this.mGnssNavigationMessageCallbackTransport = new GnssNavigationMessageCallbackTransport(this.mContext, this.mService);
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
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, minTime, minDistance, false), listener, null, null);
    }

    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper) {
        checkProvider(provider);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, minTime, minDistance, false), listener, looper, null);
    }

    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper) {
        checkCriteria(criteria);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, minTime, minDistance, false), listener, looper, null);
    }

    public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent) {
        checkProvider(provider);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, minTime, minDistance, false), null, null, intent);
    }

    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent) {
        checkCriteria(criteria);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, minTime, minDistance, false), null, null, intent);
    }

    public void requestSingleUpdate(String provider, LocationListener listener, Looper looper) {
        checkProvider(provider);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, 0, 0.0f, true), listener, looper, null);
    }

    public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper) {
        checkCriteria(criteria);
        checkListener(listener);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, 0, 0.0f, true), listener, looper, null);
    }

    public void requestSingleUpdate(String provider, PendingIntent intent) {
        checkProvider(provider);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedProvider(provider, 0, 0.0f, true), null, null, intent);
    }

    public void requestSingleUpdate(Criteria criteria, PendingIntent intent) {
        checkCriteria(criteria);
        checkPendingIntent(intent);
        requestLocationUpdates(LocationRequest.createFromDeprecatedCriteria(criteria, 0, 0.0f, true), null, null, intent);
    }

    public void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper) {
        checkListener(listener);
        requestLocationUpdates(request, listener, looper, null);
    }

    public void requestLocationUpdates(LocationRequest request, PendingIntent intent) {
        checkPendingIntent(intent);
        requestLocationUpdates(request, null, null, intent);
    }

    private ListenerTransport wrapListener(LocationListener listener, Looper looper) {
        if (listener == null) {
            return null;
        }
        ListenerTransport transport;
        synchronized (this.mListeners) {
            transport = (ListenerTransport) this.mListeners.get(listener);
            if (transport == null) {
                transport = new ListenerTransport(listener, looper);
            }
            this.mListeners.put(listener, transport);
        }
        return transport;
    }

    private void requestLocationUpdates(LocationRequest request, LocationListener listener, Looper looper, PendingIntent intent) {
        String packageName = this.mContext.getPackageName();
        try {
            this.mService.requestLocationUpdates(request, wrapListener(listener, looper), intent, packageName);
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    public void removeUpdates(LocationListener listener) {
        checkListener(listener);
        String packageName = this.mContext.getPackageName();
        synchronized (this.mListeners) {
            ListenerTransport transport = (ListenerTransport) this.mListeners.remove(listener);
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
            expiration = LinkQualityInfo.UNKNOWN_LONG;
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

    public boolean isProviderEnabled(String provider) {
        checkProvider(provider);
        try {
            return this.mService.isProviderEnabled(provider);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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
        if (name.matches(LocationProvider.BAD_CHARS_REGEX)) {
            throw new IllegalArgumentException("provider name contains illegal character: " + name);
        }
        try {
            this.mService.addTestProvider(name, properties, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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

    public void clearTestProviderLocation(String provider) {
        try {
            this.mService.clearTestProviderLocation(provider, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTestProviderEnabled(String provider, boolean enabled) {
        try {
            this.mService.setTestProviderEnabled(provider, enabled, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearTestProviderEnabled(String provider) {
        try {
            this.mService.clearTestProviderEnabled(provider, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime) {
        try {
            this.mService.setTestProviderStatus(provider, status, extras, updateTime, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearTestProviderStatus(String provider) {
        try {
            this.mService.clearTestProviderStatus(provider, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean addGpsStatusListener(Listener listener) {
        if (this.mGpsStatusListeners.get(listener) != null) {
            return true;
        }
        try {
            GnssStatusListenerTransport transport = new GnssStatusListenerTransport(this, listener);
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
    public void removeGpsStatusListener(Listener listener) {
        try {
            GnssStatusListenerTransport transport = (GnssStatusListenerTransport) this.mGpsStatusListeners.remove(listener);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean registerGnssStatusCallback(GnssStatusCallback callback) {
        return registerGnssStatusCallback(callback, null);
    }

    public boolean registerGnssStatusCallback(GnssStatusCallback callback, Handler handler) {
        if (this.mOldGnssStatusListeners.get(callback) != null) {
            return true;
        }
        try {
            GnssStatusListenerTransport transport = new GnssStatusListenerTransport(callback, handler);
            boolean result = this.mService.registerGnssStatusCallback(transport, this.mContext.getPackageName());
            if (result) {
                this.mOldGnssStatusListeners.put(callback, transport);
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterGnssStatusCallback(GnssStatusCallback callback) {
        try {
            GnssStatusListenerTransport transport = (GnssStatusListenerTransport) this.mOldGnssStatusListeners.remove(callback);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean registerGnssStatusCallback(Callback callback) {
        return registerGnssStatusCallback(callback, null);
    }

    public boolean registerGnssStatusCallback(Callback callback, Handler handler) {
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

    public void unregisterGnssStatusCallback(Callback callback) {
        try {
            GnssStatusListenerTransport transport = (GnssStatusListenerTransport) this.mGnssStatusListeners.remove(callback);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean addNmeaListener(NmeaListener listener) {
        if (this.mGpsNmeaListeners.get(listener) != null) {
            return true;
        }
        try {
            GnssStatusListenerTransport transport = new GnssStatusListenerTransport(this, listener);
            boolean result = this.mService.registerGnssStatusCallback(transport, this.mContext.getPackageName());
            if (result) {
                this.mGpsNmeaListeners.put(listener, transport);
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public void removeNmeaListener(NmeaListener listener) {
        try {
            GnssStatusListenerTransport transport = (GnssStatusListenerTransport) this.mGpsNmeaListeners.remove(listener);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean addNmeaListener(GnssNmeaListener listener) {
        return addNmeaListener(listener, null);
    }

    public boolean addNmeaListener(GnssNmeaListener listener, Handler handler) {
        if (this.mGpsNmeaListeners.get(listener) != null) {
            return true;
        }
        try {
            GnssStatusListenerTransport transport = new GnssStatusListenerTransport(listener, handler);
            boolean result = this.mService.registerGnssStatusCallback(transport, this.mContext.getPackageName());
            if (result) {
                this.mOldGnssNmeaListeners.put(listener, transport);
            }
            return result;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeNmeaListener(GnssNmeaListener listener) {
        try {
            GnssStatusListenerTransport transport = (GnssStatusListenerTransport) this.mOldGnssNmeaListeners.remove(listener);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean addNmeaListener(OnNmeaMessageListener listener) {
        return addNmeaListener(listener, null);
    }

    public boolean addNmeaListener(OnNmeaMessageListener listener, Handler handler) {
        if (this.mGpsNmeaListeners.get(listener) != null) {
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
            GnssStatusListenerTransport transport = (GnssStatusListenerTransport) this.mGnssNmeaListeners.remove(listener);
            if (transport != null) {
                this.mService.unregisterGnssStatusCallback(transport);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    @Deprecated
    public void removeGpsMeasurementListener(GpsMeasurementsEvent.Listener listener) {
    }

    public void unregisterGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback) {
        this.mGnssMeasurementCallbackTransport.remove(callback);
    }

    @Deprecated
    public boolean addGpsNavigationMessageListener(GpsNavigationMessageEvent.Listener listener) {
        return false;
    }

    @Deprecated
    public void removeGpsNavigationMessageListener(GpsNavigationMessageEvent.Listener listener) {
    }

    public boolean registerGnssNavigationMessageCallback(GnssNavigationMessageEvent.Callback callback) {
        return registerGnssNavigationMessageCallback(callback, null);
    }

    public boolean registerGnssNavigationMessageCallback(GnssNavigationMessageEvent.Callback callback, Handler handler) {
        GnssNavigationMessage.Callback bridge = new AnonymousClass1(callback);
        this.mNavigationMessageBridge.put(callback, bridge);
        return this.mGnssNavigationMessageCallbackTransport.add(bridge, handler);
    }

    public void unregisterGnssNavigationMessageCallback(GnssNavigationMessageEvent.Callback callback) {
        this.mGnssNavigationMessageCallbackTransport.remove((GnssNavigationMessage.Callback) this.mNavigationMessageBridge.remove(callback));
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

    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        try {
            return this.mService.sendExtraCommand(provider, command, extras);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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
            if (this.mContext.getApplicationInfo().targetSdkVersion > 16) {
                throw e;
            }
            Log.w(TAG, e);
        }
    }

    private static void checkGeofence(Geofence fence) {
        if (fence == null) {
            throw new IllegalArgumentException("invalid geofence: " + fence);
        }
    }
}
