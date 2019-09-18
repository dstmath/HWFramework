package com.android.server.location;

import android.content.Context;
import android.location.Address;
import android.location.Country;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.util.Slog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationBasedCountryDetector extends CountryDetectorBase {
    private static final long QUERY_LOCATION_TIMEOUT = 300000;
    private static final String TAG = "LocationBasedCountryDetector";
    private List<String> mEnabledProviders;
    protected List<LocationListener> mLocationListeners;
    private LocationManager mLocationManager;
    protected Thread mQueryThread;
    protected Timer mTimer;

    public LocationBasedCountryDetector(Context ctx) {
        super(ctx);
        this.mLocationManager = (LocationManager) ctx.getSystemService("location");
    }

    /* access modifiers changed from: protected */
    public String getCountryFromLocation(Location location) {
        try {
            List<Address> addresses = new Geocoder(this.mContext).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses == null || addresses.size() <= 0) {
                return null;
            }
            return addresses.get(0).getCountryCode();
        } catch (IOException e) {
            Slog.w(TAG, "Exception occurs when getting country from location");
            return null;
        } catch (IllegalArgumentException e2) {
            Slog.w(TAG, "illegal argument exception");
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAcceptableProvider(String provider) {
        return "passive".equals(provider);
    }

    /* access modifiers changed from: protected */
    public void registerListener(String provider, LocationListener listener) {
        long bid = Binder.clearCallingIdentity();
        try {
            this.mLocationManager.requestLocationUpdates(provider, 0, 0.0f, listener);
        } finally {
            Binder.restoreCallingIdentity(bid);
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterListener(LocationListener listener) {
        long bid = Binder.clearCallingIdentity();
        try {
            this.mLocationManager.removeUpdates(listener);
        } finally {
            Binder.restoreCallingIdentity(bid);
        }
    }

    /* access modifiers changed from: protected */
    public Location getLastKnownLocation() {
        long bid = Binder.clearCallingIdentity();
        try {
            Location bestLocation = null;
            for (String provider : this.mLocationManager.getAllProviders()) {
                Location lastKnownLocation = this.mLocationManager.getLastKnownLocation(provider);
                if (lastKnownLocation != null && (bestLocation == null || bestLocation.getElapsedRealtimeNanos() < lastKnownLocation.getElapsedRealtimeNanos())) {
                    bestLocation = lastKnownLocation;
                }
            }
            return bestLocation;
        } finally {
            Binder.restoreCallingIdentity(bid);
        }
    }

    /* access modifiers changed from: protected */
    public long getQueryLocationTimeout() {
        return 300000;
    }

    /* access modifiers changed from: protected */
    public List<String> getEnabledProviders() {
        if (this.mEnabledProviders == null) {
            this.mEnabledProviders = this.mLocationManager.getProviders(true);
        }
        return this.mEnabledProviders;
    }

    public synchronized Country detectCountry() {
        if (this.mLocationListeners == null) {
            List<String> enabledProviders = getEnabledProviders();
            int totalProviders = enabledProviders.size();
            if (totalProviders > 0) {
                this.mLocationListeners = new ArrayList(totalProviders);
                for (int i = 0; i < totalProviders; i++) {
                    String provider = enabledProviders.get(i);
                    if (isAcceptableProvider(provider)) {
                        LocationListener listener = new LocationListener() {
                            public void onLocationChanged(Location location) {
                                if (location != null) {
                                    LocationBasedCountryDetector.this.stop();
                                    LocationBasedCountryDetector.this.queryCountryCode(location);
                                }
                            }

                            public void onProviderDisabled(String provider) {
                            }

                            public void onProviderEnabled(String provider) {
                            }

                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }
                        };
                        this.mLocationListeners.add(listener);
                        registerListener(provider, listener);
                    }
                }
                this.mTimer = new Timer();
                this.mTimer.schedule(new TimerTask() {
                    public void run() {
                        LocationBasedCountryDetector.this.mTimer = null;
                        LocationBasedCountryDetector.this.stop();
                        LocationBasedCountryDetector.this.queryCountryCode(LocationBasedCountryDetector.this.getLastKnownLocation());
                    }
                }, getQueryLocationTimeout());
            } else {
                queryCountryCode(getLastKnownLocation());
            }
        } else {
            throw new IllegalStateException();
        }
        return this.mDetectedCountry;
    }

    public synchronized void stop() {
        if (this.mLocationListeners != null) {
            for (LocationListener listener : this.mLocationListeners) {
                unregisterListener(listener);
            }
            this.mLocationListeners = null;
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    /* access modifiers changed from: private */
    public synchronized void queryCountryCode(final Location location) {
        if (location == null) {
            notifyListener(null);
        } else if (this.mQueryThread == null) {
            this.mQueryThread = new Thread(new Runnable() {
                public void run() {
                    String countryIso = null;
                    if (location != null) {
                        countryIso = LocationBasedCountryDetector.this.getCountryFromLocation(location);
                    }
                    if (countryIso != null) {
                        LocationBasedCountryDetector.this.mDetectedCountry = new Country(countryIso, 1);
                    } else {
                        LocationBasedCountryDetector.this.mDetectedCountry = null;
                    }
                    LocationBasedCountryDetector.this.notifyListener(LocationBasedCountryDetector.this.mDetectedCountry);
                    LocationBasedCountryDetector.this.mQueryThread = null;
                }
            });
            this.mQueryThread.start();
        }
    }
}
