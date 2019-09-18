package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Address;
import android.location.Country;
import android.location.CountryListener;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.latlongtotimezone.TimezoneMapper;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import libcore.util.TimeZoneFinder;

public class HwLocationBasedTimeZoneUpdater extends StateMachine {
    private static final String ACTION_ENABLE_LOCATION = "huawei.intent.action.ENABLE_LOCATION";
    private static final double DEFAULT_LOCATION_CHECK_DISTANCE = 100000.0d;
    private static final long DEFAULT_NITZ_OR_LOCATION_CHECK_PERIOD_MS = 3600000;
    private static final int DELAY_STOP_LOCATION_AFTER_SCREEN_OFF = 5000;
    public static final int EVENT_LOCATION_CHANGED = 100;
    private static final int EVENT_NET_ISO_CHANGED = 3;
    public static final int EVENT_NITZ_TIMEZONE_UPDATE = 1;
    private static final int EVENT_REQUEST_LOCATION_UPDATE = 5;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 4;
    private static final int EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ = 7;
    private static final int EVENT_STOP_LOCATION_UPDATE = 6;
    private static final int INVALID_LAC = -1;
    private static final String LOCATION_SETTINGS_ACTIVITY = "android.settings.LOCATION_SOURCE_SETTINGS";
    private static final String LOG_TAG = "HwLocationBasedTzUpdater";
    private static final int MCC_LEN = 3;
    private static final String MULTI_TIMEZONE_COUNTRY = "au,br,ca,cd,cl,cy,ec,es,fm,gl,id,ki,kz,mn,mx,nz,pf,pg,pt,ru,um,us";
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_NO_COVER_COUNTRY = "gs";
    private static final int NOTIFICATION_ID_LOCATION_ACCESS = 1;
    private static final String NOTIFICATION_TAG = "HwLocationBasedTzUpdater";
    /* access modifiers changed from: private */
    public static final int PHONE_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final long RESET_NOTIFY_AFTER_LOCATION_TZ_MS = 3600000;
    private static final long SEND_NOTIFY_WHEN_NO_NITZ_MS = 600000;
    public static final String SOURCE_COUNTRY_DETECTOR = "CountryDetector";
    public static final String SOURCE_LOCATION = "Location";
    private static final String TIME_UPDATE_NOTIFY_CHANNEL = "time_update_notify_channel";
    private static HwLocationBasedTimeZoneUpdater sInstance = null;
    private ContentObserver mAutoTimeZoneObserver = null;
    private ConnectivityManager mCM;
    private Context mContext;
    /* access modifiers changed from: private */
    public LocationBasedCountryDetector mCountryDetector;
    /* access modifiers changed from: private */
    public CountryListener mCountryListener = new CountryListener() {
        public void onCountryDetected(Country country) {
            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
            hwLocationBasedTimeZoneUpdater.log("Country detected " + country);
            String zoneId = HwLocationBasedTimeZoneUpdater.this.getTimezoneIdByCountry(country);
            String currentZoneId = TimeZone.getDefault().getID();
            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
            hwLocationBasedTimeZoneUpdater2.log("onCountryChanged, new zoneId:" + zoneId + ", currentZoneId:" + currentZoneId);
            if (HwLocationBasedTimeZoneUpdater.this.isValidTimeZone(zoneId)) {
                if (!zoneId.equals(currentZoneId)) {
                    if (HwLocationBasedTimeZoneUpdater.this.isNetworkNoCoverCountry(country) || HwLocationBasedTimeZoneUpdater.this.isZoneIdMatchIso(zoneId)) {
                        HwLocationBasedTimeZoneUpdater.this.setTimeZone(zoneId, HwLocationBasedTimeZoneUpdater.SOURCE_COUNTRY_DETECTOR);
                    } else {
                        HwLocationBasedTimeZoneUpdater.this.log("location convert time zone not match iso.");
                        return;
                    }
                }
                long unused = HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                Location unused2 = HwLocationBasedTimeZoneUpdater.this.mLastTzUpdateLocation = HwLocationBasedTimeZoneUpdater.this.mCurrentLocation;
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater3 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater3.log("LocationTzUpdatedState, mTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime + "ms");
            }
        }
    };
    /* access modifiers changed from: private */
    public ContentResolver mCr;
    /* access modifiers changed from: private */
    public Location mCurrentLocation = null;
    /* access modifiers changed from: private */
    public HwLocationUpdateManager mHwLocationUpdateManager = null;
    private final IdleState mIdleState = new IdleState();
    private boolean mIsRoaming = false;
    /* access modifiers changed from: private */
    public int mLastLocationMode = 0;
    /* access modifiers changed from: private */
    public int mLastNitzLac = -1;
    /* access modifiers changed from: private */
    public Location mLastTzUpdateLocation = null;
    /* access modifiers changed from: private */
    public String mLastValidNetIso = "";
    /* access modifiers changed from: private */
    public final LocationTzUpdatedState mLocationTzUpdatedState = new LocationTzUpdatedState();
    private BroadcastReceiver mMultiTimeZoneStateReceiver = null;
    /* access modifiers changed from: private */
    public Handler mMyHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    Integer slotId = (Integer) ar.userObj;
                    int i = 0;
                    while (i < HwLocationBasedTimeZoneUpdater.PHONE_NUM) {
                        Phone phone = PhoneFactory.getPhone(i);
                        ServiceState ss = phone != null ? phone.getServiceState() : null;
                        if (ss == null || (!(ss.getVoiceRegState() == 0 || ss.getDataRegState() == 0) || TextUtils.isEmpty(HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso))) {
                            i++;
                        } else {
                            HwLocationBasedTimeZoneUpdater.this.log("[SLOT" + i + "] is in service, ignore EVENT_RPLMNS_STATE_CHANGED");
                            return;
                        }
                    }
                    String unused = HwLocationBasedTimeZoneUpdater.this.oldRplmn = HwLocationBasedTimeZoneUpdater.this.rplmn;
                    if (ar.result == null || !(ar.result instanceof String)) {
                        String unused2 = HwLocationBasedTimeZoneUpdater.this.rplmn = SystemProperties.get(HwLocationBasedTimeZoneUpdater.PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
                    } else {
                        String unused3 = HwLocationBasedTimeZoneUpdater.this.rplmn = (String) ar.result;
                    }
                    HwLocationBasedTimeZoneUpdater.this.log("[SLOT" + slotId + "]EVENT_RPLMNS_STATE_CHANGED, rplmn" + HwLocationBasedTimeZoneUpdater.this.rplmn);
                    String mcc = "";
                    String oldMcc = "";
                    if (HwLocationBasedTimeZoneUpdater.this.rplmn != null && HwLocationBasedTimeZoneUpdater.this.rplmn.length() > 3) {
                        mcc = HwLocationBasedTimeZoneUpdater.this.rplmn.substring(0, 3);
                    }
                    if (HwLocationBasedTimeZoneUpdater.this.oldRplmn != null && HwLocationBasedTimeZoneUpdater.this.oldRplmn.length() > 3) {
                        oldMcc = HwLocationBasedTimeZoneUpdater.this.oldRplmn.substring(0, 3);
                    }
                    if (!"".equals(mcc) && !mcc.equals(oldMcc)) {
                        HwLocationBasedTimeZoneUpdater.this.log("rplmn mcc changed.");
                        String netIso = "";
                        try {
                            netIso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                        } catch (NumberFormatException e) {
                            HwLocationBasedTimeZoneUpdater.this.loge("countryCodeForMcc NumberFormatException");
                        }
                        if (!TextUtils.isEmpty(netIso)) {
                            String unused4 = HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso = netIso;
                            HwLocationBasedTimeZoneUpdater.this.log("[SLOT" + slotId + "]EVENT_RPLMNS_STATE_CHANGED, mLastValidNetIso:" + HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso);
                            HwLocationBasedTimeZoneUpdater.this.getHandler().sendMessage(obtainMessage(3, HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso));
                            if (HwLocationBasedTimeZoneUpdater.this.isMultiTimeZoneCountry(HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso)) {
                                HwReportManagerImpl.getDefault().reportMultiTZRegistered();
                                break;
                            }
                        }
                    }
                    break;
                case 5:
                    boolean useNetOnly = ((Boolean) msg.obj).booleanValue();
                    HwLocationBasedTimeZoneUpdater.this.log("EVENT_REQUEST_LOCATION_UPDATE, useNetOnly: " + useNetOnly);
                    HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.requestLocationUpdate(useNetOnly);
                    if (useNetOnly) {
                        long unused5 = HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                        break;
                    }
                    break;
                case 6:
                    HwLocationBasedTimeZoneUpdater.this.log("EVENT_STOP_LOCATION_UPDATE.");
                    HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.stopLocationUpdate();
                    break;
                case 7:
                    HwLocationBasedTimeZoneUpdater.this.log("EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ, check whether to show notification.");
                    if (!HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isLocationEnabled()) {
                        HwLocationBasedTimeZoneUpdater.this.showLocationAccessNotification();
                    }
                    HwReportManagerImpl.getDefault().reportMultiTZNoNitz();
                    break;
            }
        }
    };
    private NetworkConnectionStateCallback mNetworkConnectionStateCallback = null;
    /* access modifiers changed from: private */
    public long mNitzTimeZoneUpdateTime = 0;
    /* access modifiers changed from: private */
    public final NitzTzUpdatedState mNitzTzUpdatedState = new NitzTzUpdatedState();
    private NotificationManager mNotificationManager;
    private BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                String currentNetIso = null;
                int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                Phone phone = PhoneFactory.getPhone(mainSlot);
                ServiceState serviceState = null;
                ServiceState ss = phone != null ? phone.getServiceState() : null;
                if (ss != null && (ss.getVoiceRegState() == 0 || ss.getDataRegState() == 0)) {
                    currentNetIso = TelephonyManager.getDefault().getNetworkCountryIso(mainSlot);
                }
                if (TextUtils.isEmpty(currentNetIso)) {
                    int i = 1;
                    if (HwLocationBasedTimeZoneUpdater.PHONE_NUM > 1) {
                        if (mainSlot != 0) {
                            i = 0;
                        }
                        int otherSlot = i;
                        Phone phone2 = PhoneFactory.getPhone(otherSlot);
                        if (phone2 != null) {
                            serviceState = phone2.getServiceState();
                        }
                        ServiceState ss2 = serviceState;
                        if (ss2 != null && (ss2.getVoiceRegState() == 0 || ss2.getDataRegState() == 0)) {
                            currentNetIso = TelephonyManager.getDefault().getNetworkCountryIso(otherSlot);
                        }
                    }
                }
                HwLocationBasedTimeZoneUpdater.this.setNetworkRoamingState();
                boolean netIsoChanged = false;
                if (!TextUtils.isEmpty(currentNetIso) && !currentNetIso.equals(HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso)) {
                    netIsoChanged = true;
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater.log("ACTION_SERVICE_STATE_CHANGED, network country iso changed from " + HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso + " to " + currentNetIso);
                }
                if (!TextUtils.isEmpty(currentNetIso)) {
                    String unused = HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso = currentNetIso;
                }
                if (netIsoChanged) {
                    HwLocationBasedTimeZoneUpdater.this.getHandler().sendMessage(HwLocationBasedTimeZoneUpdater.this.obtainMessage(3, currentNetIso));
                    if (!TextUtils.isEmpty(currentNetIso) && HwLocationBasedTimeZoneUpdater.this.isMultiTimeZoneCountry(currentNetIso)) {
                        HwReportManagerImpl.getDefault().reportMultiTZRegistered();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public long mTimeZoneUpdateTime = 0;
    /* access modifiers changed from: private */
    public String oldRplmn = "";
    /* access modifiers changed from: private */
    public String rplmn = "";

    private class AutoTimeZoneObserver extends ContentObserver {
        public AutoTimeZoneObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (Settings.Global.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "auto_time_zone", 0) == 0) {
                HwLocationBasedTimeZoneUpdater.this.log("Auto time zone disabled, send EVENT_STOP_LOCATION_UPDATE immediately.");
                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(6));
            } else if (HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isLocationEnabled()) {
                HwLocationBasedTimeZoneUpdater.this.log("Auto time zone enabled, send EVENT_REQUEST_LOCATION_UPDATE immediately.");
                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(5, false));
                Location unused = HwLocationBasedTimeZoneUpdater.this.mCurrentLocation = null;
            }
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            HwLocationBasedTimeZoneUpdater.this.log("entering IdleState");
        }

        public void exit() {
            HwLocationBasedTimeZoneUpdater.this.log("leaving IdleState");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwLocationBasedTimeZoneUpdater.this.log("IdleState.processMessage EVENT_NITZ_TIMEZONE_UPDATE");
                long unused = HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = HwLocationBasedTimeZoneUpdater.this.mNitzTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                int unused2 = HwLocationBasedTimeZoneUpdater.this.mLastNitzLac = ((Integer) msg.obj).intValue();
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater.log("EVENT_NITZ_TIMEZONE_UPDATE, mNitzTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mNitzTimeZoneUpdateTime + "ms");
                if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(7)) {
                    HwLocationBasedTimeZoneUpdater.this.log("IdleState, remove EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ.");
                    HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(7);
                }
                if (!HwLocationBasedTimeZoneUpdater.this.getCurrentState().equals(HwLocationBasedTimeZoneUpdater.this.mNitzTzUpdatedState)) {
                    HwLocationBasedTimeZoneUpdater.this.transitionTo(HwLocationBasedTimeZoneUpdater.this.mNitzTzUpdatedState);
                }
                return true;
            } else if (i == 3) {
                HwLocationBasedTimeZoneUpdater.this.log("IdleState.processMessage EVENT_NET_ISO_CHANGED");
                if (HwLocationBasedTimeZoneUpdater.this.isMultiTimeZoneCountry((String) msg.obj)) {
                    HwLocationBasedTimeZoneUpdater.this.registerEventsForMultiTimeZoneCountry();
                } else {
                    HwLocationBasedTimeZoneUpdater.this.unregisterEventsForMultiTimeZoneCountry();
                }
                return true;
            } else if (i != 100) {
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater2.log("IdleState.processMessage default:" + msg.what);
                return false;
            } else {
                HwLocationBasedTimeZoneUpdater.this.log("IdleState.processMessage EVENT_LOCATION_CHANGED");
                if (HwLocationBasedTimeZoneUpdater.this.shouldUpateTimeZoneInCheckingState((Location) msg.obj)) {
                    HwLocationBasedTimeZoneUpdater.this.transitionTo(HwLocationBasedTimeZoneUpdater.this.mLocationTzUpdatedState);
                }
                return true;
            }
        }
    }

    public class LocationBasedCountryDetector {
        private final Context mContext;
        /* access modifiers changed from: private */
        public Country mDetectedCountry;
        /* access modifiers changed from: private */
        public CountryListener mListener;
        /* access modifiers changed from: private */
        public Thread mQueryThread;

        public LocationBasedCountryDetector(Context ctx) {
            this.mContext = ctx;
        }

        private void setCountryListener(CountryListener listener) {
            this.mListener = listener;
        }

        /* access modifiers changed from: private */
        public void notifyListener(Country country) {
            if (this.mListener != null) {
                this.mListener.onCountryDetected(country);
            }
        }

        /* access modifiers changed from: private */
        public synchronized String getCountryFromLocation(Location location) {
            String country;
            country = null;
            try {
                List<Address> addresses = new Geocoder(this.mContext).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    country = addresses.get(0).getCountryCode();
                }
            } catch (IOException e) {
                HwLocationBasedTimeZoneUpdater.this.loge("Exception occurs when getting country from location");
            } catch (IllegalArgumentException e2) {
                HwLocationBasedTimeZoneUpdater.this.loge("illegal argument exception");
            }
            return country;
        }

        public synchronized void queryCountryCode(final Location location, CountryListener listener) {
            if (location == null) {
                notifyListener(null);
            } else if (this.mQueryThread == null) {
                setCountryListener(listener);
                this.mQueryThread = new Thread(new Runnable() {
                    public void run() {
                        String countryIso = null;
                        if (location != null) {
                            countryIso = LocationBasedCountryDetector.this.getCountryFromLocation(location);
                        }
                        if (countryIso != null) {
                            Country unused = LocationBasedCountryDetector.this.mDetectedCountry = new Country(countryIso, 1);
                        } else {
                            Country unused2 = LocationBasedCountryDetector.this.mDetectedCountry = null;
                        }
                        LocationBasedCountryDetector.this.notifyListener(LocationBasedCountryDetector.this.mDetectedCountry);
                        Thread unused3 = LocationBasedCountryDetector.this.mQueryThread = null;
                        CountryListener unused4 = LocationBasedCountryDetector.this.mListener = null;
                    }
                });
                this.mQueryThread.start();
            }
        }
    }

    private class LocationTzUpdatedState extends State {
        private LocationTzUpdatedState() {
        }

        public void enter() {
            HwLocationBasedTimeZoneUpdater.this.log("entering LocationTzUpdatedState");
            if (!HwLocationBasedTimeZoneUpdater.this.getAutoTimeZone()) {
                HwLocationBasedTimeZoneUpdater.this.loge("Auto time zone disabled!");
            } else if (HwLocationBasedTimeZoneUpdater.this.mCurrentLocation != null) {
                String newZoneId = TimezoneMapper.latLngToTimezoneString(HwLocationBasedTimeZoneUpdater.this.mCurrentLocation.getLatitude(), HwLocationBasedTimeZoneUpdater.this.mCurrentLocation.getLongitude());
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater.log("LocationTzUpdatedState, time zone after conversion: " + newZoneId);
                boolean isValidZoneId = HwLocationBasedTimeZoneUpdater.this.isValidTimeZone(newZoneId);
                String currentZoneId = TimeZone.getDefault().getID();
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater2.log("LocationTzUpdatedState, current time zone: " + currentZoneId);
                if (isValidZoneId) {
                    if (!newZoneId.equals(currentZoneId)) {
                        List<String> affiliatedIslands = TimezoneMapper.getAffiliatedIslands();
                        if (affiliatedIslands == null || affiliatedIslands.contains(newZoneId) || HwLocationBasedTimeZoneUpdater.this.isZoneIdMatchIso(newZoneId)) {
                            HwLocationBasedTimeZoneUpdater.this.setTimeZone(newZoneId, HwLocationBasedTimeZoneUpdater.SOURCE_LOCATION);
                            if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(7)) {
                                HwLocationBasedTimeZoneUpdater.this.log("LocationTzUpdatedState, reset EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ.");
                                HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(7);
                                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessageDelayed(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(7), 3600000);
                            }
                        } else {
                            HwLocationBasedTimeZoneUpdater.this.log("location convert time zone not match iso.");
                            return;
                        }
                    }
                    long unused = HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                    Location unused2 = HwLocationBasedTimeZoneUpdater.this.mLastTzUpdateLocation = HwLocationBasedTimeZoneUpdater.this.mCurrentLocation;
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater3 = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater3.log("LocationTzUpdatedState, mTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime + "ms");
                } else if (HwLocationBasedTimeZoneUpdater.this.mCountryDetector != null) {
                    HwLocationBasedTimeZoneUpdater.this.mCountryDetector.queryCountryCode(HwLocationBasedTimeZoneUpdater.this.mCurrentLocation, HwLocationBasedTimeZoneUpdater.this.mCountryListener);
                }
            }
        }

        public void exit() {
            HwLocationBasedTimeZoneUpdater.this.log("leaving LocationTzUpdatedState");
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
            hwLocationBasedTimeZoneUpdater.log("LocationTzUpdatedState.processMessage default:" + msg.what);
            return false;
        }
    }

    private class MultiTimeZoneBroadcastReceiver extends BroadcastReceiver {
        private MultiTimeZoneBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(6)) {
                        HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_ON, remove EVENT_STOP_LOCATION_UPDATE.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(6);
                    }
                    long tzUpdateDiff = SystemClock.elapsedRealtime() - HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime;
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater.log("ACTION_SCREEN_ON, tzUpdateDiff: " + tzUpdateDiff + "ms, mTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime + "ms");
                    if (HwLocationBasedTimeZoneUpdater.this.getAutoTimeZone() && HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isNetworkLocationAvailable() && (HwLocationBasedTimeZoneUpdater.this.mCurrentLocation == null || tzUpdateDiff > HwLocationBasedTimeZoneUpdater.this.getCheckPeriodByRoamingState())) {
                        HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_ON, send EVENT_REQUEST_LOCATION_UPDATE.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(5, true));
                    }
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(5)) {
                        HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_OFF, remove EVENT_REQUEST_LOCATION_UPDATE.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(5);
                    }
                    HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_OFF, delay send EVENT_STOP_LOCATION_UPDATE.");
                    HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessageDelayed(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(6), HwVSimConstants.WAIT_FOR_SIM_STATUS_CHANGED_UNSOL_TIMEOUT);
                } else if ("android.location.MODE_CHANGED".equals(action)) {
                    int currentLocationMode = Settings.Secure.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "location_mode", 0);
                    if (Settings.Secure.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "location_mode", 0) == 0) {
                        HwLocationBasedTimeZoneUpdater.this.log("Location service disabled, send EVENT_STOP_LOCATION_UPDATE immediately.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(6));
                    } else if (HwLocationBasedTimeZoneUpdater.this.getAutoTimeZone() && HwLocationBasedTimeZoneUpdater.this.mLastLocationMode == 0) {
                        HwLocationBasedTimeZoneUpdater.this.log("Location service enabled, send EVENT_REQUEST_LOCATION_UPDATE immediately.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(5, false));
                        HwLocationBasedTimeZoneUpdater.this.dismissLocationAccessNotification();
                        Location unused = HwLocationBasedTimeZoneUpdater.this.mCurrentLocation = null;
                    }
                    int unused2 = HwLocationBasedTimeZoneUpdater.this.mLastLocationMode = currentLocationMode;
                } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    if (Settings.Global.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "airplane_mode_on", 0) > 0) {
                        HwLocationBasedTimeZoneUpdater.this.log("airplane mode on, reset mCurrentLocation and net iso.");
                        Location unused3 = HwLocationBasedTimeZoneUpdater.this.mCurrentLocation = null;
                        String unused4 = HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso = "";
                    }
                } else if (HwLocationBasedTimeZoneUpdater.ACTION_ENABLE_LOCATION.equals(action)) {
                    HwLocationBasedTimeZoneUpdater.this.log("receives ACTION_ENABLE_LOCATION, dismiss notification and enable location access.");
                    HwLocationBasedTimeZoneUpdater.this.dismissLocationAccessNotification();
                    HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.setLocationEnabled(true);
                }
            }
        }
    }

    private class NetworkConnectionStateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkConnectionStateCallback() {
        }

        public void onAvailable(Network network) {
            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
            hwLocationBasedTimeZoneUpdater.log("Default network available: " + network);
            if (HwLocationBasedTimeZoneUpdater.this.mCurrentLocation == null && HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isNetworkLocationAvailable()) {
                HwLocationBasedTimeZoneUpdater.this.log("request network location when mobile/wifi network available");
                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(5, true));
            }
        }
    }

    private class NitzTzUpdatedState extends State {
        private NitzTzUpdatedState() {
        }

        public void enter() {
            HwLocationBasedTimeZoneUpdater.this.log("entering NitzTzUpdatedState");
        }

        public void exit() {
            HwLocationBasedTimeZoneUpdater.this.log("leaving NitzTzUpdatedState");
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 100) {
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater.log("NitzTzUpdatedState.processMessage default:" + msg.what);
                return false;
            }
            HwLocationBasedTimeZoneUpdater.this.log("NitzTzUpdatedState.processMessage EVENT_LOCATION_CHANGED");
            if (HwLocationBasedTimeZoneUpdater.this.shouldUpateTimeZoneInNitzState((Location) msg.obj)) {
                HwLocationBasedTimeZoneUpdater.this.transitionTo(HwLocationBasedTimeZoneUpdater.this.mLocationTzUpdatedState);
            }
            return true;
        }
    }

    private HwLocationBasedTimeZoneUpdater(Context context) {
        super("HwLocationBasedTzUpdater");
        this.mContext = context;
        if (this.mContext != null) {
            this.mCr = this.mContext.getContentResolver();
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        addState(this.mIdleState);
        addState(this.mNitzTzUpdatedState, this.mIdleState);
        addState(this.mLocationTzUpdatedState, this.mIdleState);
        setInitialState(this.mIdleState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SERVICE_STATE");
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mServiceStateReceiver, filter);
        }
        registerRplmnChange();
        this.mHwLocationUpdateManager = new HwLocationUpdateManager(context, getHandler());
        this.mHwLocationUpdateManager.registerPassiveLocationUpdate();
        if (Geocoder.isPresent()) {
            this.mCountryDetector = new LocationBasedCountryDetector(context);
        }
    }

    public static HwLocationBasedTimeZoneUpdater init(Context context) {
        HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater;
        synchronized (HwLocationBasedTimeZoneUpdater.class) {
            if (sInstance == null) {
                sInstance = new HwLocationBasedTimeZoneUpdater(context);
            } else {
                Log.wtf("HwLocationBasedTzUpdater", "init() called multiple times! sInstance = " + sInstance);
            }
            hwLocationBasedTimeZoneUpdater = sInstance;
        }
        return hwLocationBasedTimeZoneUpdater;
    }

    public static HwLocationBasedTimeZoneUpdater getInstance() {
        if (sInstance == null) {
            Log.wtf("HwLocationBasedTzUpdater", "getInstance null");
        }
        return sInstance;
    }

    public void dispose() {
        quit();
    }

    /* access modifiers changed from: private */
    public boolean isZoneIdMatchIso(String zoneId) {
        List<android.icu.util.TimeZone> timeZones = TimeZoneFinder.getInstance().lookupTimeZonesByCountry(this.mLastValidNetIso);
        if (timeZones == null) {
            return true;
        }
        int len = timeZones.size();
        for (int i = 0; i < len; i++) {
            if (timeZones.get(i) != null && zoneId.equalsIgnoreCase(timeZones.get(i).getID())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isValidTimeZone(String zoneId) {
        return !TextUtils.isEmpty(zoneId) && !"unknown".equals(zoneId) && !"unusedtimezone".equals(zoneId);
    }

    /* access modifiers changed from: private */
    public void setNetworkRoamingState() {
        int i = 0;
        while (true) {
            if (i >= PHONE_NUM) {
                break;
            } else if (TelephonyManager.getDefault().isNetworkRoaming(i)) {
                this.mIsRoaming = true;
                break;
            } else {
                i++;
            }
        }
        if (PHONE_NUM == i) {
            this.mIsRoaming = false;
        }
    }

    /* access modifiers changed from: private */
    public long getCheckPeriodByRoamingState() {
        return this.mIsRoaming ? 1800000 : 3600000;
    }

    private void registerRplmnChange() {
        for (int i = 0; i < PHONE_NUM; i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (!(phone == null || phone.mCi == null)) {
                phone.mCi.registerForRplmnsStateChanged(this.mMyHandler, 4, Integer.valueOf(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isMultiTimeZoneCountry(String iso) {
        boolean ret = false;
        log("isMultiTimeZoneCountry iso: " + iso);
        int i = 0;
        if (TextUtils.isEmpty(iso)) {
            return false;
        }
        String[] multiTimeZoneIsoArray = MULTI_TIMEZONE_COUNTRY.split(",");
        int length = multiTimeZoneIsoArray.length;
        while (true) {
            if (i >= length) {
                break;
            } else if (iso.equalsIgnoreCase(multiTimeZoneIsoArray[i])) {
                ret = true;
                break;
            } else {
                i++;
            }
        }
        return ret;
    }

    /* access modifiers changed from: private */
    public void registerEventsForMultiTimeZoneCountry() {
        log("registerEventsForMultiTimeZoneCountry");
        if (this.mAutoTimeZoneObserver == null && this.mCr != null) {
            this.mAutoTimeZoneObserver = new AutoTimeZoneObserver(getHandler());
            this.mCr.registerContentObserver(Settings.Global.getUriFor("auto_time_zone"), true, this.mAutoTimeZoneObserver);
        }
        if (this.mMultiTimeZoneStateReceiver == null && this.mContext != null) {
            this.mMultiTimeZoneStateReceiver = new MultiTimeZoneBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.location.MODE_CHANGED");
            intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            intentFilter.addAction(ACTION_ENABLE_LOCATION);
            this.mContext.registerReceiver(this.mMultiTimeZoneStateReceiver, intentFilter);
        }
        if (this.mNetworkConnectionStateCallback == null) {
            this.mNetworkConnectionStateCallback = new NetworkConnectionStateCallback();
            this.mCM.registerDefaultNetworkCallback(this.mNetworkConnectionStateCallback, getHandler());
        }
        if (this.mMyHandler.hasMessages(7)) {
            log("remove EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ");
            this.mMyHandler.removeMessages(7);
        }
        this.mMyHandler.sendMessageDelayed(this.mMyHandler.obtainMessage(7), SEND_NOTIFY_WHEN_NO_NITZ_MS);
        log("delay send EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ");
        this.mCurrentLocation = null;
        if (isScreenOn()) {
            log("request network and gps location once first enter multi tz country");
            this.mMyHandler.sendMessage(this.mMyHandler.obtainMessage(5, false));
        }
    }

    /* access modifiers changed from: private */
    public void unregisterEventsForMultiTimeZoneCountry() {
        log("unregisterEventsForMultiTimeZoneCountry");
        if (!(this.mAutoTimeZoneObserver == null || this.mCr == null)) {
            this.mCr.unregisterContentObserver(this.mAutoTimeZoneObserver);
            this.mAutoTimeZoneObserver = null;
        }
        if (!(this.mMultiTimeZoneStateReceiver == null || this.mContext == null)) {
            this.mContext.unregisterReceiver(this.mMultiTimeZoneStateReceiver);
            this.mMultiTimeZoneStateReceiver = null;
        }
        if (this.mNetworkConnectionStateCallback != null) {
            this.mCM.unregisterNetworkCallback(this.mNetworkConnectionStateCallback);
            this.mNetworkConnectionStateCallback = null;
        }
        if (this.mMyHandler.hasMessages(5)) {
            log("remove EVENT_REQUEST_LOCATION_UPDATE");
            this.mMyHandler.removeMessages(5);
        }
        if (this.mMyHandler.hasMessages(7)) {
            log("remove EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ");
            this.mMyHandler.removeMessages(7);
        }
        this.mMyHandler.sendMessage(this.mMyHandler.obtainMessage(6));
    }

    /* access modifiers changed from: private */
    public boolean shouldUpateTimeZoneInCheckingState(Location location) {
        boolean ret = false;
        if (this.mCurrentLocation == null || this.mLastTzUpdateLocation == null) {
            log("shouldUpateTimeZoneInCheckingState, mCurrentLocation or mLastTzUpdateLocation is null.");
            ret = true;
        } else if (location != null) {
            double distance = HwLocationUpdateManager.getDistance(location.getLatitude(), location.getLongitude(), this.mLastTzUpdateLocation.getLatitude(), this.mLastTzUpdateLocation.getLongitude());
            long locTimeDiff = location.getElapsedRealtimeNanos() - this.mLastTzUpdateLocation.getElapsedRealtimeNanos();
            log("shouldUpateTimeZoneInCheckingState, distance: " + distance + "m, locTimeDiff: " + locTimeDiff + "ns");
            if (distance > DEFAULT_LOCATION_CHECK_DISTANCE || locTimeDiff > getCheckPeriodByRoamingState() * NANOS_PER_MILLI) {
                ret = true;
            }
        }
        this.mCurrentLocation = location;
        return ret;
    }

    /* access modifiers changed from: private */
    public boolean shouldUpateTimeZoneInNitzState(Location location) {
        boolean ret = false;
        int newLac = -1;
        long nitzTimeDiff = SystemClock.elapsedRealtime() - this.mNitzTimeZoneUpdateTime;
        log("shouldUpateTimeZoneInNitzState, nitzTimeDiff: " + nitzTimeDiff + "ms, mNitzTimeZoneUpdateTime: " + this.mNitzTimeZoneUpdateTime + "ms");
        if (!this.mIsRoaming || this.mCurrentLocation != null) {
            int i = 0;
            while (true) {
                if (i >= PHONE_NUM) {
                    break;
                }
                CellLocation cellLoc = HwTelephonyManagerInner.getDefault().getCellLocation(i);
                if (cellLoc != null && (cellLoc instanceof GsmCellLocation) && -1 != ((GsmCellLocation) cellLoc).getLac()) {
                    newLac = ((GsmCellLocation) cellLoc).getLac();
                    break;
                }
                i++;
            }
            if (nitzTimeDiff > getCheckPeriodByRoamingState() && (newLac != this.mLastNitzLac || (!hasIccCard() && this.mLastNitzLac == -1 && newLac == -1))) {
                log("shouldUpateTimeZoneInNitzState, lac changed or card absent.");
                ret = true;
            }
            this.mCurrentLocation = location;
            return ret;
        }
        log("shouldUpateTimeZoneInNitzState, first get location in roaming.");
        this.mCurrentLocation = location;
        return true;
    }

    private boolean hasIccCard() {
        for (int i = 0; i < PHONE_NUM; i++) {
            if (TelephonyManager.getDefault().hasIccCard(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isScreenOn() {
        if (this.mContext != null) {
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            if (pm != null) {
                return pm.isScreenOn();
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean getAutoTimeZone() {
        return Settings.Global.getInt(this.mCr, "auto_time_zone", 0) != 0;
    }

    /* access modifiers changed from: protected */
    public void setTimeZone(String zoneId, String source) {
        log("setTimeZone, zoneId: " + zoneId + ", source: " + source);
        if (this.mContext != null) {
            AlarmManager alarm = (AlarmManager) this.mContext.getSystemService("alarm");
            if (alarm != null) {
                alarm.setTimeZone(zoneId);
                if (SOURCE_LOCATION.equals(source)) {
                    HwReportManagerImpl.getDefault().reportSetTimeZoneByLocation(zoneId);
                } else if (SOURCE_COUNTRY_DETECTOR.equals(source)) {
                    HwReportManagerImpl.getDefault().reportSetTimeZoneByIso(PhoneFactory.getDefaultPhone(), zoneId, false, source);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void showLocationAccessNotification() {
        log("showLocationAccessNotification");
        if (this.mContext != null) {
            Intent resultIntent = new Intent(LOCATION_SETTINGS_ACTIVITY);
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(TIME_UPDATE_NOTIFY_CHANNEL, this.mContext.getString(33686045), 3));
            this.mNotificationManager.notify("HwLocationBasedTzUpdater", 1, new Notification.Builder(this.mContext).setSmallIcon(33751759).setAppName(this.mContext.getString(33686045)).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(-1).setContentTitle(this.mContext.getString(33686046)).setContentText(this.mContext.getString(33686047)).setContentIntent(resultPendingIntent).setStyle(new Notification.BigTextStyle()).setChannelId(TIME_UPDATE_NOTIFY_CHANNEL).addAction(getEnableLocationAction()).build());
        }
    }

    private Notification.Action getEnableLocationAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_ENABLE_LOCATION);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder(null, this.mContext.getString(33686048), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    /* access modifiers changed from: private */
    public void dismissLocationAccessNotification() {
        if (this.mNotificationManager != null) {
            log("dismissLocationAccessNotification");
            this.mNotificationManager.cancel("HwLocationBasedTzUpdater", 1);
            this.mNotificationManager = null;
        }
    }

    /* access modifiers changed from: protected */
    public LocationBasedCountryDetector getCountryDetector() {
        return this.mCountryDetector;
    }

    /* access modifiers changed from: protected */
    public String getTimezoneIdByCountry(Country country) {
        String countryIso = "";
        if (country != null) {
            countryIso = country.getCountryIso();
        }
        if (!TextUtils.isEmpty(countryIso)) {
            if (countryIso.equalsIgnoreCase("cn") || countryIso.equalsIgnoreCase("ua")) {
                return TimeZoneFinder.getInstance().lookupDefaultTimeZoneIdByCountry(countryIso);
            }
            List<android.icu.util.TimeZone> timeZones = TimeZoneFinder.getInstance().lookupTimeZonesByCountry(countryIso);
            if (timeZones != null && timeZones.size() == 1) {
                return timeZones.get(0).getID();
            }
            log("timeZones more than one.");
        }
        return "";
    }

    /* access modifiers changed from: private */
    public boolean isNetworkNoCoverCountry(Country country) {
        boolean ret = false;
        int i = 0;
        if (country == null) {
            return false;
        }
        String iso = country.getCountryIso();
        if (TextUtils.isEmpty(iso)) {
            return false;
        }
        String[] noNetworkConverCountry = NETWORK_NO_COVER_COUNTRY.split(",");
        int length = noNetworkConverCountry.length;
        while (true) {
            if (i >= length) {
                break;
            } else if (iso.equalsIgnoreCase(noNetworkConverCountry[i])) {
                ret = true;
                break;
            } else {
                i++;
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }
}
