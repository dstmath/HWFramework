package com.android.server.location;

import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.location.Geocoder;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComprehensiveCountryDetector extends CountryDetectorBase {
    static final boolean DEBUG = false;
    private static final long LOCATION_REFRESH_INTERVAL = 86400000;
    private static final int MAX_LENGTH_DEBUG_LOGS = 20;
    private static final String TAG = "CountryDetector";
    private int mCountServiceStateChanges;
    private Country mCountry;
    private Country mCountryFromLocation;
    private final ConcurrentLinkedQueue<Country> mDebugLogs;
    private Country mLastCountryAddedToLogs;
    private CountryListener mLocationBasedCountryDetectionListener;
    protected CountryDetectorBase mLocationBasedCountryDetector;
    protected Timer mLocationRefreshTimer;
    private final Object mObject;
    private PhoneStateListener mPhoneStateListener;
    private long mStartTime;
    private long mStopTime;
    private boolean mStopped;
    private final TelephonyManager mTelephonyManager;
    private int mTotalCountServiceStateChanges;
    private long mTotalTime;

    /* renamed from: com.android.server.location.ComprehensiveCountryDetector.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Country val$country;
        final /* synthetic */ Country val$detectedCountry;
        final /* synthetic */ boolean val$notifyChange;
        final /* synthetic */ boolean val$startLocationBasedDetection;

        AnonymousClass2(Country val$country, Country val$detectedCountry, boolean val$notifyChange, boolean val$startLocationBasedDetection) {
            this.val$country = val$country;
            this.val$detectedCountry = val$detectedCountry;
            this.val$notifyChange = val$notifyChange;
            this.val$startLocationBasedDetection = val$startLocationBasedDetection;
        }

        public void run() {
            ComprehensiveCountryDetector.this.runAfterDetection(this.val$country, this.val$detectedCountry, this.val$notifyChange, this.val$startLocationBasedDetection);
        }
    }

    public ComprehensiveCountryDetector(Context context) {
        super(context);
        this.mStopped = DEBUG;
        this.mDebugLogs = new ConcurrentLinkedQueue();
        this.mObject = new Object();
        this.mLocationBasedCountryDetectionListener = new CountryListener() {
            public void onCountryDetected(Country country) {
                ComprehensiveCountryDetector.this.mCountryFromLocation = country;
                ComprehensiveCountryDetector.this.detectCountry(true, ComprehensiveCountryDetector.DEBUG);
                ComprehensiveCountryDetector.this.stopLocationBasedDetector();
            }
        };
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public Country detectCountry() {
        return detectCountry(DEBUG, this.mStopped ? DEBUG : true);
    }

    public void stop() {
        Slog.i(TAG, "Stop the detector.");
        cancelLocationRefresh();
        removePhoneStateListener();
        stopLocationBasedDetector();
        this.mListener = null;
        this.mStopped = true;
    }

    private boolean isValidCountryIso(String countryIso) {
        Slog.d(TAG, "isValidCountryIso: " + countryIso);
        if (!TextUtils.isEmpty(countryIso)) {
            int len = countryIso.length();
            if (2 == len) {
                for (int i = 0; i < len; i++) {
                    char ch = countryIso.charAt(i);
                    if (('a' > ch || ch > 'z') && ('A' > ch || ch > 'Z')) {
                        return DEBUG;
                    }
                }
                return true;
            }
        }
        return DEBUG;
    }

    private Country getCountry() {
        Country result = getNetworkBasedCountry();
        if (result == null) {
            result = getLastKnownLocationBasedCountry();
        }
        if (result == null || ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", "")) && !isValidCountryIso(result.mCountryIso))) {
            result = getSimBasedCountry();
        }
        if (result == null) {
            result = getLocaleCountry();
        }
        addToLogs(result);
        return result;
    }

    private void addToLogs(Country country) {
        if (country != null) {
            synchronized (this.mObject) {
                if (this.mLastCountryAddedToLogs == null || !this.mLastCountryAddedToLogs.equals(country)) {
                    this.mLastCountryAddedToLogs = country;
                    if (this.mDebugLogs.size() >= MAX_LENGTH_DEBUG_LOGS) {
                        this.mDebugLogs.poll();
                    }
                    this.mDebugLogs.add(country);
                    return;
                }
            }
        }
    }

    private boolean isNetworkCountryCodeAvailable() {
        boolean z = true;
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            return true;
        }
        if (this.mTelephonyManager.getPhoneType() != 1) {
            z = DEBUG;
        }
        return z;
    }

    protected Country getNetworkBasedCountry() {
        if (isNetworkCountryCodeAvailable()) {
            String countryIso = this.mTelephonyManager.getNetworkCountryIso();
            if (!TextUtils.isEmpty(countryIso)) {
                return new Country(countryIso, 0);
            }
        }
        return null;
    }

    protected Country getLastKnownLocationBasedCountry() {
        return this.mCountryFromLocation;
    }

    protected Country getSimBasedCountry() {
        String countryIso = this.mTelephonyManager.getSimCountryIso();
        if (TextUtils.isEmpty(countryIso)) {
            return null;
        }
        return new Country(countryIso, 2);
    }

    protected Country getLocaleCountry() {
        Locale defaultLocale = Locale.getDefault();
        if (defaultLocale != null) {
            return new Country(defaultLocale.getCountry(), 3);
        }
        return null;
    }

    private Country detectCountry(boolean notifyChange, boolean startLocationBasedDetection) {
        Country country = getCountry();
        runAfterDetectionAsync(this.mCountry != null ? new Country(this.mCountry) : this.mCountry, country, notifyChange, startLocationBasedDetection);
        this.mCountry = country;
        return this.mCountry;
    }

    protected void runAfterDetectionAsync(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
        this.mHandler.post(new AnonymousClass2(country, detectedCountry, notifyChange, startLocationBasedDetection));
    }

    public void setCountryListener(CountryListener listener) {
        CountryListener prevListener = this.mListener;
        this.mListener = listener;
        if (this.mListener == null) {
            removePhoneStateListener();
            stopLocationBasedDetector();
            cancelLocationRefresh();
            this.mStopTime = SystemClock.elapsedRealtime();
            this.mTotalTime += this.mStopTime;
        } else if (prevListener == null) {
            addPhoneStateListener();
            detectCountry(DEBUG, true);
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mStopTime = 0;
            this.mCountServiceStateChanges = 0;
        }
    }

    void runAfterDetection(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
        if (notifyChange) {
            notifyIfCountryChanged(country, detectedCountry);
        }
        if (startLocationBasedDetection && ((detectedCountry == null || detectedCountry.getSource() > 1) && isAirplaneModeOff() && this.mListener != null && isGeoCoderImplemented())) {
            startLocationBasedDetector(this.mLocationBasedCountryDetectionListener);
        }
        if (detectedCountry == null || detectedCountry.getSource() >= 1) {
            scheduleLocationRefresh();
            return;
        }
        cancelLocationRefresh();
        stopLocationBasedDetector();
    }

    private synchronized void startLocationBasedDetector(CountryListener listener) {
        if (this.mLocationBasedCountryDetector == null) {
            this.mLocationBasedCountryDetector = createLocationBasedCountryDetector();
            this.mLocationBasedCountryDetector.setCountryListener(listener);
            this.mLocationBasedCountryDetector.detectCountry();
        }
    }

    private synchronized void stopLocationBasedDetector() {
        if (this.mLocationBasedCountryDetector != null) {
            this.mLocationBasedCountryDetector.stop();
            this.mLocationBasedCountryDetector = null;
        }
    }

    protected CountryDetectorBase createLocationBasedCountryDetector() {
        return new LocationBasedCountryDetector(this.mContext);
    }

    protected boolean isAirplaneModeOff() {
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
            return true;
        }
        return DEBUG;
    }

    private void notifyIfCountryChanged(Country country, Country detectedCountry) {
        if (detectedCountry != null && this.mListener != null) {
            if (country == null || !country.equals(detectedCountry)) {
                Slog.d(TAG, "" + country + " --> " + detectedCountry);
                notifyListener(detectedCountry);
            }
        }
    }

    private synchronized void scheduleLocationRefresh() {
        if (this.mLocationRefreshTimer == null) {
            this.mLocationRefreshTimer = new Timer();
            this.mLocationRefreshTimer.schedule(new TimerTask() {
                public void run() {
                    ComprehensiveCountryDetector.this.mLocationRefreshTimer = null;
                    ComprehensiveCountryDetector.this.detectCountry(ComprehensiveCountryDetector.DEBUG, true);
                }
            }, LOCATION_REFRESH_INTERVAL);
        }
    }

    private synchronized void cancelLocationRefresh() {
        if (this.mLocationRefreshTimer != null) {
            this.mLocationRefreshTimer.cancel();
            this.mLocationRefreshTimer = null;
        }
    }

    protected synchronized void addPhoneStateListener() {
        if (this.mPhoneStateListener == null) {
            this.mPhoneStateListener = new PhoneStateListener() {
                public void onServiceStateChanged(ServiceState serviceState) {
                    ComprehensiveCountryDetector comprehensiveCountryDetector = ComprehensiveCountryDetector.this;
                    comprehensiveCountryDetector.mCountServiceStateChanges = comprehensiveCountryDetector.mCountServiceStateChanges + 1;
                    comprehensiveCountryDetector = ComprehensiveCountryDetector.this;
                    comprehensiveCountryDetector.mTotalCountServiceStateChanges = comprehensiveCountryDetector.mTotalCountServiceStateChanges + 1;
                    if (ComprehensiveCountryDetector.this.isNetworkCountryCodeAvailable()) {
                        ComprehensiveCountryDetector.this.detectCountry(true, true);
                    }
                }
            };
            this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
        }
    }

    protected synchronized void removePhoneStateListener() {
        if (this.mPhoneStateListener != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mPhoneStateListener = null;
        }
    }

    protected boolean isGeoCoderImplemented() {
        return Geocoder.isPresent();
    }

    public String toString() {
        long currentTime = SystemClock.elapsedRealtime();
        long currentSessionLength = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("ComprehensiveCountryDetector{");
        if (this.mStopTime == 0) {
            currentSessionLength = currentTime - this.mStartTime;
            sb.append("timeRunning=").append(currentSessionLength).append(", ");
        } else {
            sb.append("lastRunTimeLength=").append(this.mStopTime - this.mStartTime).append(", ");
        }
        sb.append("totalCountServiceStateChanges=").append(this.mTotalCountServiceStateChanges).append(", ");
        sb.append("currentCountServiceStateChanges=").append(this.mCountServiceStateChanges).append(", ");
        sb.append("totalTime=").append(this.mTotalTime + currentSessionLength).append(", ");
        sb.append("currentTime=").append(currentTime).append(", ");
        sb.append("countries=");
        for (Country country : this.mDebugLogs) {
            sb.append("\n   ").append(country.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
