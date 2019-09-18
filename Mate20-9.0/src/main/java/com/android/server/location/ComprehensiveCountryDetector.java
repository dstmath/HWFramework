package com.android.server.location;

import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.location.Geocoder;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComprehensiveCountryDetector extends CountryDetectorBase {
    static final boolean DEBUG = false;
    private static final long LOCATION_REFRESH_INTERVAL = 86400000;
    private static final int MAX_LENGTH_DEBUG_LOGS = 20;
    private static final String TAG = "CountryDetector";
    /* access modifiers changed from: private */
    public int mCountServiceStateChanges;
    private Country mCountry;
    /* access modifiers changed from: private */
    public Country mCountryFromLocation;
    private final ConcurrentLinkedQueue<Country> mDebugLogs = new ConcurrentLinkedQueue<>();
    private Country mLastCountryAddedToLogs;
    private CountryListener mLocationBasedCountryDetectionListener = new CountryListener() {
        public void onCountryDetected(Country country) {
            Country unused = ComprehensiveCountryDetector.this.mCountryFromLocation = country;
            Country unused2 = ComprehensiveCountryDetector.this.detectCountry(true, false);
            ComprehensiveCountryDetector.this.stopLocationBasedDetector();
        }
    };
    protected CountryDetectorBase mLocationBasedCountryDetector;
    protected Timer mLocationRefreshTimer;
    private final Object mObject = new Object();
    private PhoneStateListener mPhoneStateListener;
    private long mStartTime;
    private long mStopTime;
    private boolean mStopped = false;
    private final TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public int mTotalCountServiceStateChanges;
    private long mTotalTime;

    public ComprehensiveCountryDetector(Context context) {
        super(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public Country detectCountry() {
        return detectCountry(false, !this.mStopped);
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
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private Country getCountry() {
        Country result = getNetworkBasedCountry();
        if (result == null) {
            result = getLastKnownLocationBasedCountry();
        }
        if (result == null || ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS)) && !isValidCountryIso(result.mCountryIso))) {
            result = getSimBasedCountry();
        }
        if (result == null) {
            result = getLocaleCountry();
        }
        addToLogs(result);
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        if (r2.mDebugLogs.size() < 20) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        r2.mDebugLogs.poll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        r2.mDebugLogs.add(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002b, code lost:
        return;
     */
    private void addToLogs(Country country) {
        if (country != null) {
            synchronized (this.mObject) {
                if (this.mLastCountryAddedToLogs == null || !this.mLastCountryAddedToLogs.equals(country)) {
                    this.mLastCountryAddedToLogs = country;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isNetworkCountryCodeAvailable() {
        boolean z = true;
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS))) {
            return true;
        }
        if (this.mTelephonyManager.getPhoneType() != 1) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public Country getNetworkBasedCountry() {
        if (isNetworkCountryCodeAvailable()) {
            String countryIso = this.mTelephonyManager.getNetworkCountryIso();
            if (!TextUtils.isEmpty(countryIso)) {
                return new Country(countryIso, 0);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Country getLastKnownLocationBasedCountry() {
        return this.mCountryFromLocation;
    }

    /* access modifiers changed from: protected */
    public Country getSimBasedCountry() {
        String countryIso = this.mTelephonyManager.getSimCountryIso();
        if (!TextUtils.isEmpty(countryIso)) {
            return new Country(countryIso, 2);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public Country getLocaleCountry() {
        Locale defaultLocale = Locale.getDefault();
        if (defaultLocale != null) {
            return new Country(defaultLocale.getCountry(), 3);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public Country detectCountry(boolean notifyChange, boolean startLocationBasedDetection) {
        Country country = getCountry();
        runAfterDetectionAsync(this.mCountry != null ? new Country(this.mCountry) : this.mCountry, country, notifyChange, startLocationBasedDetection);
        this.mCountry = country;
        return this.mCountry;
    }

    /* access modifiers changed from: protected */
    public void runAfterDetectionAsync(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
        Handler handler = this.mHandler;
        final Country country2 = country;
        final Country country3 = detectedCountry;
        final boolean z = notifyChange;
        final boolean z2 = startLocationBasedDetection;
        AnonymousClass2 r1 = new Runnable() {
            public void run() {
                ComprehensiveCountryDetector.this.runAfterDetection(country2, country3, z, z2);
            }
        };
        handler.post(r1);
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
            detectCountry(false, true);
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mStopTime = 0;
            this.mCountServiceStateChanges = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void runAfterDetection(Country country, Country detectedCountry, boolean notifyChange, boolean startLocationBasedDetection) {
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

    /* access modifiers changed from: private */
    public synchronized void stopLocationBasedDetector() {
        if (this.mLocationBasedCountryDetector != null) {
            this.mLocationBasedCountryDetector.stop();
            this.mLocationBasedCountryDetector = null;
        }
    }

    /* access modifiers changed from: protected */
    public CountryDetectorBase createLocationBasedCountryDetector() {
        return new LocationBasedCountryDetector(this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean isAirplaneModeOff() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0;
    }

    private void notifyIfCountryChanged(Country country, Country detectedCountry) {
        if (detectedCountry != null && this.mListener != null) {
            if (country == null || !country.equals(detectedCountry)) {
                Slog.d(TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + country + " --> " + detectedCountry);
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
                    Country unused = ComprehensiveCountryDetector.this.detectCountry(false, true);
                }
            }, 86400000);
        }
    }

    private synchronized void cancelLocationRefresh() {
        if (this.mLocationRefreshTimer != null) {
            this.mLocationRefreshTimer.cancel();
            this.mLocationRefreshTimer = null;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void addPhoneStateListener() {
        if (this.mPhoneStateListener == null) {
            this.mPhoneStateListener = new PhoneStateListener() {
                public void onServiceStateChanged(ServiceState serviceState) {
                    int unused = ComprehensiveCountryDetector.this.mCountServiceStateChanges = ComprehensiveCountryDetector.this.mCountServiceStateChanges + 1;
                    int unused2 = ComprehensiveCountryDetector.this.mTotalCountServiceStateChanges = ComprehensiveCountryDetector.this.mTotalCountServiceStateChanges + 1;
                    if (ComprehensiveCountryDetector.this.isNetworkCountryCodeAvailable()) {
                        Country unused3 = ComprehensiveCountryDetector.this.detectCountry(true, true);
                    }
                }
            };
            this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void removePhoneStateListener() {
        if (this.mPhoneStateListener != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mPhoneStateListener = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isGeoCoderImplemented() {
        return Geocoder.isPresent();
    }

    public String toString() {
        long currentTime = SystemClock.elapsedRealtime();
        long currentSessionLength = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("ComprehensiveCountryDetector{");
        if (this.mStopTime == 0) {
            currentSessionLength = currentTime - this.mStartTime;
            sb.append("timeRunning=" + currentSessionLength + ", ");
        } else {
            sb.append("lastRunTimeLength=" + (this.mStopTime - this.mStartTime) + ", ");
        }
        sb.append("totalCountServiceStateChanges=" + this.mTotalCountServiceStateChanges + ", ");
        sb.append("currentCountServiceStateChanges=" + this.mCountServiceStateChanges + ", ");
        sb.append("totalTime=" + (this.mTotalTime + currentSessionLength) + ", ");
        sb.append("currentTime=" + currentTime + ", ");
        sb.append("countries=");
        Iterator<Country> it = this.mDebugLogs.iterator();
        while (it.hasNext()) {
            sb.append("\n   " + it.next().toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
