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
import android.graphics.drawable.Icon;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.latlongtotimezone.TimezoneMapper;
import com.huawei.android.app.NotificationEx;
import com.huawei.android.location.CountryExt;
import com.huawei.android.location.CountryListenerExt;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonytimezone.BuildConfig;
import com.huawei.internal.telephony.MccTableExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.util.StateEx;
import com.huawei.internal.util.StateMachineEx;
import com.huawei.libcore.timezone.TimeZoneFinderEx;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.TimeZone;

public class HwLocationBasedTimeZoneUpdater extends StateMachineEx {
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
    private static final int HIGH_ACCURACY_CONFIRMED_IN_STARTUP = 1;
    private static final int HIGH_ACCURACY_DENIED_IN_STARTUP = 0;
    private static final boolean HW_DEBUGGABLE;
    private static final int INVALID_LAC = -1;
    private static final String LOCATION_SETTINGS_ACTIVITY = "android.settings.LOCATION_SOURCE_SETTINGS";
    private static final String LOG_TAG = "HwLocationBasedTzUpdater";
    private static final int MCC_LEN = 3;
    private static final String MULTI_TIMEZONE_COUNTRY = "au,br,ca,cd,cl,cy,ec,es,fm,gl,id,ki,kz,mn,mx,nz,pf,pg,pt,ru,um,us";
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_NO_COVER_COUNTRY = "gs";
    private static final int NOTIFICATION_ID_LOCATION_ACCESS = 1;
    private static final String NOTIFICATION_TAG = "HwLocationBasedTzUpdater";
    private static final int PHONE_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final long RESET_NOTIFY_AFTER_LOCATION_TZ_MS = 3600000;
    private static final long SEND_NOTIFY_WHEN_NO_NITZ_MS = 600000;
    public static final String SOURCE_COUNTRY_DETECTOR = "CountryDetector";
    public static final String SOURCE_LOCATION = "Location";
    private static final String TIME_UPDATE_NOTIFY_CHANNEL = "time_update_notify_channel";
    private static final String UPDATE_TIME_ZONE_BY_NITZ = "hw_update_time_zone_by_nitz";
    private static HwLocationBasedTimeZoneUpdater sInstance = null;
    private ContentObserver mAutoTimeZoneObserver = null;
    private ConnectivityManager mCM;
    private Context mContext;
    private LocationBasedCountryDetector mCountryDetector;
    private CountryListenerExt mCountryListener = new CountryListenerExt() {
        /* class com.android.internal.telephony.HwLocationBasedTimeZoneUpdater.AnonymousClass4 */

        public void onCountryDetected(CountryExt country) {
            if (HwLocationBasedTimeZoneUpdater.HW_DEBUGGABLE) {
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater.log("Country detected " + country);
            }
            String zoneId = HwLocationBasedTimeZoneUpdater.this.getTimezoneIdByCountry(country);
            String currentZoneId = TimeZone.getDefault().getID();
            if (HwLocationBasedTimeZoneUpdater.HW_DEBUGGABLE) {
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater2.log("onCountryChanged, new zoneId:" + zoneId + ", currentZoneId:" + currentZoneId);
            }
            if (HwLocationBasedTimeZoneUpdater.this.isValidTimeZone(zoneId)) {
                if (!zoneId.equals(currentZoneId)) {
                    if (HwLocationBasedTimeZoneUpdater.this.isNetworkNoCoverCountry(country) || HwLocationBasedTimeZoneUpdater.this.isZoneIdMatchIso(zoneId)) {
                        HwLocationBasedTimeZoneUpdater.this.setTimeZone(zoneId, HwLocationBasedTimeZoneUpdater.SOURCE_COUNTRY_DETECTOR);
                    } else {
                        HwLocationBasedTimeZoneUpdater.this.log("location convert time zone not match iso.");
                        return;
                    }
                }
                HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater3 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater3.mLastTzUpdateLocation = hwLocationBasedTimeZoneUpdater3.mCurrentLocation;
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater4 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater4.log("LocationTzUpdatedState, mTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime + "ms");
            }
        }
    };
    private ContentResolver mCr;
    private Location mCurrentLocation = null;
    private HwLocationUpdateManager mHwLocationUpdateManager = null;
    private final IdleState mIdleState = new IdleState();
    private boolean mIsPassiveLocationRegistered = false;
    private boolean mIsRoaming = false;
    private int mLastLocationMode = 0;
    private int mLastNitzLac = INVALID_LAC;
    private Location mLastTzUpdateLocation = null;
    private String mLastValidNetIso = BuildConfig.FLAVOR;
    private BroadcastReceiver mLocationModeReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwLocationBasedTimeZoneUpdater.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.location.MODE_CHANGED".equals(intent.getAction())) {
                int currentMode = Settings.Secure.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "location_mode", 0);
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater.log("MODE_CHANGED_ACTION, current mode: " + currentMode);
                HwLocationBasedTimeZoneUpdater.this.onLocationModeChanged();
            }
        }
    };
    private final LocationTzUpdatedState mLocationTzUpdatedState = new LocationTzUpdatedState();
    private BroadcastReceiver mMultiTimeZoneStateReceiver = null;
    private Handler mMyHandler = new Handler() {
        /* class com.android.internal.telephony.HwLocationBasedTimeZoneUpdater.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == HwLocationBasedTimeZoneUpdater.EVENT_RPLMNS_STATE_CHANGED) {
                AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                Integer slotId = (Integer) ar.getUserObj();
                for (int i2 = 0; i2 < HwLocationBasedTimeZoneUpdater.PHONE_NUM; i2++) {
                    PhoneExt phone = PhoneFactoryExt.getPhone(i2);
                    ServiceState ss = phone != null ? phone.getServiceState() : null;
                    if (ss != null && ((ServiceStateEx.getVoiceRegState(ss) == 0 || ServiceStateEx.getDataState(ss) == 0) && !TextUtils.isEmpty(HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso))) {
                        HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                        hwLocationBasedTimeZoneUpdater.log("[SLOT" + i2 + "] is in service, ignore EVENT_RPLMNS_STATE_CHANGED");
                        return;
                    }
                }
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater2.oldRplmn = hwLocationBasedTimeZoneUpdater2.rplmn;
                if (ar.getResult() == null || !(ar.getResult() instanceof String)) {
                    HwLocationBasedTimeZoneUpdater.this.rplmn = SystemPropertiesEx.get(HwLocationBasedTimeZoneUpdater.PROPERTY_GLOBAL_OPERATOR_NUMERIC, BuildConfig.FLAVOR);
                } else {
                    HwLocationBasedTimeZoneUpdater.this.rplmn = (String) ar.getResult();
                }
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater3 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater3.log("[SLOT" + slotId + "]EVENT_RPLMNS_STATE_CHANGED, rplmn" + HwLocationBasedTimeZoneUpdater.this.rplmn);
                String mcc = BuildConfig.FLAVOR;
                String oldMcc = BuildConfig.FLAVOR;
                if (HwLocationBasedTimeZoneUpdater.this.rplmn != null && HwLocationBasedTimeZoneUpdater.this.rplmn.length() > 3) {
                    mcc = HwLocationBasedTimeZoneUpdater.this.rplmn.substring(0, 3);
                }
                if (HwLocationBasedTimeZoneUpdater.this.oldRplmn != null && HwLocationBasedTimeZoneUpdater.this.oldRplmn.length() > 3) {
                    oldMcc = HwLocationBasedTimeZoneUpdater.this.oldRplmn.substring(0, 3);
                }
                if (!(BuildConfig.FLAVOR.equals(mcc) || mcc.equals(oldMcc))) {
                    HwLocationBasedTimeZoneUpdater.this.log("rplmn mcc changed.");
                    String netIso = BuildConfig.FLAVOR;
                    try {
                        netIso = MccTableExt.countryCodeForMcc(Integer.parseInt(mcc));
                    } catch (NumberFormatException e) {
                        HwLocationBasedTimeZoneUpdater.this.loge("countryCodeForMcc NumberFormatException");
                    }
                    if (!TextUtils.isEmpty(netIso)) {
                        HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso = netIso;
                        if (HwLocationBasedTimeZoneUpdater.HW_DEBUGGABLE) {
                            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater4 = HwLocationBasedTimeZoneUpdater.this;
                            hwLocationBasedTimeZoneUpdater4.log("[SLOT" + slotId + "]EVENT_RPLMNS_STATE_CHANGED, mLastValidNetIso:" + HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso);
                        }
                        HwLocationBasedTimeZoneUpdater.this.getHandler().sendMessage(obtainMessage(3, HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso));
                        HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater5 = HwLocationBasedTimeZoneUpdater.this;
                        if (hwLocationBasedTimeZoneUpdater5.isMultiTimeZoneCountry(hwLocationBasedTimeZoneUpdater5.mLastValidNetIso)) {
                            HwReportManagerImpl.getDefault().reportMultiTZRegistered();
                        }
                    }
                }
            } else if (i == HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE) {
                boolean useNetOnly = ((Boolean) msg.obj).booleanValue();
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater6 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater6.log("EVENT_REQUEST_LOCATION_UPDATE, useNetOnly: " + useNetOnly);
                HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.requestLocationUpdate(useNetOnly);
                if (useNetOnly) {
                    HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                }
            } else if (i == HwLocationBasedTimeZoneUpdater.EVENT_STOP_LOCATION_UPDATE) {
                HwLocationBasedTimeZoneUpdater.this.log("EVENT_STOP_LOCATION_UPDATE.");
                HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.stopLocationUpdate();
            } else if (i == HwLocationBasedTimeZoneUpdater.EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ) {
                HwLocationBasedTimeZoneUpdater.this.log("EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ, check whether to show notification.");
                if (!HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isLocationEnabled()) {
                    HwLocationBasedTimeZoneUpdater.this.showLocationAccessNotification();
                }
                HwReportManagerImpl.getDefault().reportMultiTZNoNitz();
            }
        }
    };
    private NetworkConnectionStateCallback mNetworkConnectionStateCallback = null;
    private long mNitzTimeZoneUpdateTime = 0;
    private final NitzTzUpdatedState mNitzTzUpdatedState = new NitzTzUpdatedState();
    private NotificationManager mNotificationManager;
    private BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwLocationBasedTimeZoneUpdater.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                String currentNetIso = null;
                int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                PhoneExt phone = PhoneFactoryExt.getPhone(mainSlot);
                ServiceState ss = null;
                ServiceState ss2 = phone != null ? phone.getServiceState() : null;
                if (ss2 != null && (ServiceStateEx.getVoiceRegState(ss2) == 0 || ServiceStateEx.getDataState(ss2) == 0)) {
                    currentNetIso = TelephonyManagerEx.getNetworkCountryIsoForPhone(mainSlot);
                }
                if (TextUtils.isEmpty(currentNetIso)) {
                    int otherSlot = 1;
                    if (HwLocationBasedTimeZoneUpdater.PHONE_NUM > 1) {
                        if (mainSlot != 0) {
                            otherSlot = 0;
                        }
                        PhoneExt phone2 = PhoneFactoryExt.getPhone(otherSlot);
                        if (phone2 != null) {
                            ss = phone2.getServiceState();
                        }
                        if (ss != null && (ServiceStateEx.getVoiceRegState(ss) == 0 || ServiceStateEx.getDataState(ss) == 0)) {
                            currentNetIso = TelephonyManagerEx.getNetworkCountryIsoForPhone(otherSlot);
                        }
                    }
                }
                HwLocationBasedTimeZoneUpdater.this.setNetworkRoamingState();
                boolean netIsoChanged = false;
                if (!TextUtils.isEmpty(currentNetIso) && !currentNetIso.equals(HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso)) {
                    netIsoChanged = true;
                    if (HwLocationBasedTimeZoneUpdater.HW_DEBUGGABLE) {
                        HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                        hwLocationBasedTimeZoneUpdater.log("ACTION_SERVICE_STATE_CHANGED, network country iso changed from " + HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso + " to " + currentNetIso);
                    }
                }
                if (!TextUtils.isEmpty(currentNetIso)) {
                    HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso = currentNetIso;
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
    private long mTimeZoneUpdateTime = 0;
    private String oldRplmn = BuildConfig.FLAVOR;
    private String rplmn = BuildConfig.FLAVOR;

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.debuggable", 0) == 1) {
            z = true;
        }
        HW_DEBUGGABLE = z;
    }

    private HwLocationBasedTimeZoneUpdater(Context context) {
        super("HwLocationBasedTzUpdater");
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mCr = context2.getContentResolver();
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        addState(this.mIdleState);
        addState(this.mNitzTzUpdatedState, this.mIdleState);
        addState(this.mLocationTzUpdatedState, this.mIdleState);
        setInitialState(this.mIdleState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SERVICE_STATE");
        Context context3 = this.mContext;
        if (context3 != null) {
            context3.registerReceiver(this.mServiceStateReceiver, filter);
        }
        registerRplmnChange();
        this.mHwLocationUpdateManager = new HwLocationUpdateManager(context, getHandler());
        if (this.mHwLocationUpdateManager.isLocationEnabled()) {
            this.mHwLocationUpdateManager.registerPassiveLocationUpdate();
            this.mIsPassiveLocationRegistered = true;
        }
        IntentFilter locationIntentFilter = new IntentFilter("android.location.MODE_CHANGED");
        Context context4 = this.mContext;
        if (context4 != null) {
            context4.registerReceiver(this.mLocationModeReceiver, locationIntentFilter);
        }
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
    /* access modifiers changed from: public */
    private boolean isZoneIdMatchIso(String zoneId) {
        List<android.icu.util.TimeZone> timeZones = TimeZoneFinderEx.lookupTimeZonesByCountry(this.mLastValidNetIso);
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
    /* access modifiers changed from: public */
    private void onLocationModeChanged() {
        int startupSwitch = Settings.Secure.getInt(this.mCr, "high_accuracy_startup_comfirm", 0);
        log("onLocationModeChanged, startupSwitch: " + startupSwitch);
        if (!this.mHwLocationUpdateManager.isLocationEnabled() || startupSwitch != 1) {
            if (this.mIsPassiveLocationRegistered) {
                this.mHwLocationUpdateManager.unregisterPassiveLocationUpdate();
                this.mIsPassiveLocationRegistered = false;
            }
        } else if (!this.mIsPassiveLocationRegistered) {
            this.mHwLocationUpdateManager.registerPassiveLocationUpdate();
            this.mIsPassiveLocationRegistered = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidTimeZone(String zoneId) {
        return !TextUtils.isEmpty(zoneId) && !"unknown".equals(zoneId) && !"unusedtimezone".equals(zoneId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNetworkRoamingState() {
        int i = 0;
        while (true) {
            if (i >= PHONE_NUM) {
                break;
            } else if (TelephonyManagerEx.isNetworkRoaming(SubscriptionManagerEx.getSubIdUsingSlotId(i))) {
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
    /* access modifiers changed from: public */
    private long getCheckPeriodByRoamingState() {
        return this.mIsRoaming ? 1800000 : 3600000;
    }

    private void registerRplmnChange() {
        for (int i = 0; i < PHONE_NUM; i++) {
            PhoneExt phone = PhoneFactoryExt.getPhone(i);
            if (!(phone == null || phone.getCi() == null)) {
                phone.getCi().registerForRplmnsStateChanged(this.mMyHandler, (int) EVENT_RPLMNS_STATE_CHANGED, Integer.valueOf(i));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMultiTimeZoneCountry(String iso) {
        if (HW_DEBUGGABLE) {
            log("isMultiTimeZoneCountry iso: " + iso);
        }
        if (TextUtils.isEmpty(iso)) {
            return false;
        }
        for (String s : MULTI_TIMEZONE_COUNTRY.split(",")) {
            if (iso.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerEventsForMultiTimeZoneCountry() {
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
        if (this.mMyHandler.hasMessages(EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ)) {
            log("remove EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ");
            this.mMyHandler.removeMessages(EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ);
        }
        Handler handler = this.mMyHandler;
        handler.sendMessageDelayed(handler.obtainMessage(EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ), SEND_NOTIFY_WHEN_NO_NITZ_MS);
        log("delay send EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ");
        this.mCurrentLocation = null;
        if (isScreenOn()) {
            log("request network and gps location once first enter multi tz country");
            Handler handler2 = this.mMyHandler;
            handler2.sendMessage(handler2.obtainMessage(EVENT_REQUEST_LOCATION_UPDATE, false));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterEventsForMultiTimeZoneCountry() {
        Context context;
        ContentResolver contentResolver;
        log("unregisterEventsForMultiTimeZoneCountry");
        ContentObserver contentObserver = this.mAutoTimeZoneObserver;
        if (!(contentObserver == null || (contentResolver = this.mCr) == null)) {
            contentResolver.unregisterContentObserver(contentObserver);
            this.mAutoTimeZoneObserver = null;
        }
        BroadcastReceiver broadcastReceiver = this.mMultiTimeZoneStateReceiver;
        if (!(broadcastReceiver == null || (context = this.mContext) == null)) {
            context.unregisterReceiver(broadcastReceiver);
            this.mMultiTimeZoneStateReceiver = null;
        }
        NetworkConnectionStateCallback networkConnectionStateCallback = this.mNetworkConnectionStateCallback;
        if (networkConnectionStateCallback != null) {
            this.mCM.unregisterNetworkCallback(networkConnectionStateCallback);
            this.mNetworkConnectionStateCallback = null;
        }
        if (this.mMyHandler.hasMessages(EVENT_REQUEST_LOCATION_UPDATE)) {
            log("remove EVENT_REQUEST_LOCATION_UPDATE");
            this.mMyHandler.removeMessages(EVENT_REQUEST_LOCATION_UPDATE);
        }
        if (this.mMyHandler.hasMessages(EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ)) {
            log("remove EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ");
            this.mMyHandler.removeMessages(EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ);
        }
        Handler handler = this.mMyHandler;
        handler.sendMessage(handler.obtainMessage(EVENT_STOP_LOCATION_UPDATE));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldUpateTimeZoneInCheckingState(Location location) {
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
    /* access modifiers changed from: public */
    private boolean shouldUpateTimeZoneInNitzState(Location location) {
        boolean ret = false;
        int newLac = INVALID_LAC;
        long nitzTimeDiff = SystemClock.elapsedRealtime() - this.mNitzTimeZoneUpdateTime;
        log("shouldUpateTimeZoneInNitzState, nitzTimeDiff: " + nitzTimeDiff + "ms, mNitzTimeZoneUpdateTime: " + this.mNitzTimeZoneUpdateTime + "ms");
        if (!this.mIsRoaming || this.mCurrentLocation != null) {
            int i = 0;
            while (true) {
                if (i < PHONE_NUM) {
                    CellLocation cellLoc = HwTelephonyManagerInner.getDefault().getCellLocation(i);
                    if (cellLoc != null && (cellLoc instanceof GsmCellLocation) && INVALID_LAC != ((GsmCellLocation) cellLoc).getLac()) {
                        newLac = ((GsmCellLocation) cellLoc).getLac();
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            }
            if (nitzTimeDiff > getCheckPeriodByRoamingState() && (newLac != this.mLastNitzLac || (!hasIccCard() && this.mLastNitzLac == INVALID_LAC && newLac == INVALID_LAC))) {
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
            if (TelephonyManagerEx.hasIccCard(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isScreenOn() {
        PowerManager pm;
        Context context = this.mContext;
        if (context == null || (pm = (PowerManager) context.getSystemService("power")) == null) {
            return false;
        }
        return pm.isScreenOn();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getAutoTimeZone() {
        return Settings.Global.getInt(this.mCr, "auto_time_zone", 0) != 0;
    }

    /* access modifiers changed from: protected */
    public void setTimeZone(String zoneId, String source) {
        AlarmManager alarm;
        if (HW_DEBUGGABLE) {
            log("setTimeZone, zoneId: " + zoneId + ", source: " + source);
        }
        Context context = this.mContext;
        if (context != null && (alarm = (AlarmManager) context.getSystemService("alarm")) != null) {
            alarm.setTimeZone(zoneId);
            if (SOURCE_LOCATION.equals(source)) {
                HwReportManagerImpl.getDefault().reportSetTimeZoneByLocation(zoneId);
            } else if (SOURCE_COUNTRY_DETECTOR.equals(source)) {
                HwReportManagerImpl.getDefault().reportSetTimeZoneByIso(PhoneFactoryExt.getPhone(0), zoneId, false, source);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showLocationAccessNotification() {
        log("showLocationAccessNotification");
        if (this.mContext != null) {
            Intent resultIntent = new Intent(LOCATION_SETTINGS_ACTIVITY);
            resultIntent.setFlags(335544320);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this.mContext, 0, resultIntent, 134217728);
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(TIME_UPDATE_NOTIFY_CHANNEL, this.mContext.getString(33686045), 3));
            Notification.Builder builder = new Notification.Builder(this.mContext).setSmallIcon(33751759).setWhen(System.currentTimeMillis()).setShowWhen(true).setAutoCancel(true).setDefaults(INVALID_LAC).setContentTitle(this.mContext.getString(33686046)).setContentText(this.mContext.getString(33686047)).setContentIntent(resultPendingIntent).setStyle(new Notification.BigTextStyle()).setChannelId(TIME_UPDATE_NOTIFY_CHANNEL).addAction(getEnableLocationAction());
            NotificationEx.Builder.setAppName(builder, this.mContext.getString(33686045));
            this.mNotificationManager.notify("HwLocationBasedTzUpdater", 1, builder.build());
        }
    }

    private Notification.Action getEnableLocationAction() {
        if (this.mContext == null) {
            return null;
        }
        Intent intent = new Intent(ACTION_ENABLE_LOCATION);
        intent.setPackage(this.mContext.getPackageName());
        return new Notification.Action.Builder((Icon) null, this.mContext.getString(33686048), PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissLocationAccessNotification() {
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
    public String getTimezoneIdByCountry(CountryExt country) {
        String countryIso = BuildConfig.FLAVOR;
        if (country != null) {
            countryIso = country.getCountryIso();
        }
        if (TextUtils.isEmpty(countryIso)) {
            return BuildConfig.FLAVOR;
        }
        if (countryIso.equalsIgnoreCase("cn") || countryIso.equalsIgnoreCase("ua")) {
            return TimeZoneFinderEx.lookupDefaultTimeZoneIdByCountry(countryIso);
        }
        List<android.icu.util.TimeZone> timeZones = TimeZoneFinderEx.lookupTimeZonesByCountry(countryIso);
        if (timeZones == null || timeZones.size() != 1) {
            return getDefaultTzForSigleTzCountry(countryIso);
        }
        return timeZones.get(0).getID();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNetworkNoCoverCountry(CountryExt country) {
        if (country == null) {
            return false;
        }
        String iso = country.getCountryIso();
        if (TextUtils.isEmpty(iso)) {
            return false;
        }
        for (String s : NETWORK_NO_COVER_COUNTRY.split(",")) {
            if (iso.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public String getDefaultTzForSigleTzCountry(String countryIso) {
        if (!getInstance().isMultiTimeZoneCountry(countryIso)) {
            String defaultZoneId = TimeZoneFinderEx.lookupDefaultTimeZoneIdByCountry(countryIso);
            if (HW_DEBUGGABLE) {
                log("defaultZoneId is : " + defaultZoneId);
            }
            return defaultZoneId;
        }
        log("timeZones more than one.");
        return BuildConfig.FLAVOR;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        RlogEx.i(getName(), s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        RlogEx.e(getName(), s);
    }

    private class IdleState extends StateEx {
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
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater.mTimeZoneUpdateTime = hwLocationBasedTimeZoneUpdater.mNitzTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                HwLocationBasedTimeZoneUpdater.this.mLastNitzLac = ((Integer) msg.obj).intValue();
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater2.log("EVENT_NITZ_TIMEZONE_UPDATE, mNitzTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mNitzTimeZoneUpdateTime + "ms");
                if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(HwLocationBasedTimeZoneUpdater.EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ)) {
                    HwLocationBasedTimeZoneUpdater.this.log("IdleState, remove EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ.");
                    HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(HwLocationBasedTimeZoneUpdater.EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ);
                }
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater3 = HwLocationBasedTimeZoneUpdater.this;
                if (!hwLocationBasedTimeZoneUpdater3.isInSpecificState(hwLocationBasedTimeZoneUpdater3.mNitzTzUpdatedState)) {
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater4 = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater4.transitionTo(hwLocationBasedTimeZoneUpdater4.mNitzTzUpdatedState);
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
                HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater5 = HwLocationBasedTimeZoneUpdater.this;
                hwLocationBasedTimeZoneUpdater5.log("IdleState.processMessage default:" + msg.what);
                return false;
            } else {
                HwLocationBasedTimeZoneUpdater.this.log("IdleState.processMessage EVENT_LOCATION_CHANGED");
                if (HwLocationBasedTimeZoneUpdater.this.shouldUpateTimeZoneInCheckingState((Location) msg.obj)) {
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater6 = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater6.transitionTo(hwLocationBasedTimeZoneUpdater6.mLocationTzUpdatedState);
                }
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class NitzTzUpdatedState extends StateEx {
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
            if (!HwLocationBasedTimeZoneUpdater.this.shouldUpateTimeZoneInNitzState((Location) msg.obj)) {
                return true;
            }
            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
            hwLocationBasedTimeZoneUpdater2.transitionTo(hwLocationBasedTimeZoneUpdater2.mLocationTzUpdatedState);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class LocationTzUpdatedState extends StateEx {
        private LocationTzUpdatedState() {
        }

        public void enter() {
            HwLocationBasedTimeZoneUpdater.this.log("entering LocationTzUpdatedState");
            if (!HwLocationBasedTimeZoneUpdater.this.getAutoTimeZone()) {
                HwLocationBasedTimeZoneUpdater.this.loge("Auto time zone disabled!");
            } else if (HwLocationBasedTimeZoneUpdater.this.mCurrentLocation != null) {
                String newZoneId = TimezoneMapper.latLngToTimezoneString(HwLocationBasedTimeZoneUpdater.this.mCurrentLocation.getLatitude(), HwLocationBasedTimeZoneUpdater.this.mCurrentLocation.getLongitude());
                if (HwLocationBasedTimeZoneUpdater.HW_DEBUGGABLE) {
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater.log("LocationTzUpdatedState, time zone after conversion: " + newZoneId);
                }
                boolean isValidZoneId = HwLocationBasedTimeZoneUpdater.this.isValidTimeZone(newZoneId);
                String currentZoneId = TimeZone.getDefault().getID();
                if (HwLocationBasedTimeZoneUpdater.HW_DEBUGGABLE) {
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater2 = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater2.log("LocationTzUpdatedState, current time zone: " + currentZoneId);
                }
                if (isValidZoneId) {
                    if (!HwLocationBasedTimeZoneUpdater.this.needUpdateTimeZoneByNitz()) {
                        HwLocationBasedTimeZoneUpdater.this.log("timezone should update by nitz.");
                        return;
                    }
                    if (!newZoneId.equals(currentZoneId)) {
                        List<String> affiliatedIslands = TimezoneMapper.getAffiliatedIslands();
                        if (affiliatedIslands == null || affiliatedIslands.contains(newZoneId) || HwLocationBasedTimeZoneUpdater.this.isZoneIdMatchIso(newZoneId)) {
                            HwLocationBasedTimeZoneUpdater.this.setTimeZone(newZoneId, HwLocationBasedTimeZoneUpdater.SOURCE_LOCATION);
                            if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(HwLocationBasedTimeZoneUpdater.EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ)) {
                                HwLocationBasedTimeZoneUpdater.this.log("LocationTzUpdatedState, reset EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ.");
                                HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(HwLocationBasedTimeZoneUpdater.EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ);
                                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessageDelayed(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_SHOW_NOTIFICATION_WHEN_NO_NITZ), 3600000);
                            }
                        } else {
                            HwLocationBasedTimeZoneUpdater.this.log("location convert time zone not match iso.");
                            return;
                        }
                    }
                    HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime = SystemClock.elapsedRealtime();
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater3 = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater3.mLastTzUpdateLocation = hwLocationBasedTimeZoneUpdater3.mCurrentLocation;
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater4 = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater4.log("LocationTzUpdatedState, mTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime + "ms");
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

    /* access modifiers changed from: private */
    public class AutoTimeZoneObserver extends ContentObserver {
        public AutoTimeZoneObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            if (Settings.Global.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "auto_time_zone", 0) == 0) {
                HwLocationBasedTimeZoneUpdater.this.log("Auto time zone disabled, send EVENT_STOP_LOCATION_UPDATE immediately.");
                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_STOP_LOCATION_UPDATE));
            } else if (HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isLocationEnabled()) {
                HwLocationBasedTimeZoneUpdater.this.log("Auto time zone enabled, send EVENT_REQUEST_LOCATION_UPDATE immediately.");
                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE, false));
                HwLocationBasedTimeZoneUpdater.this.mCurrentLocation = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public class MultiTimeZoneBroadcastReceiver extends BroadcastReceiver {
        private MultiTimeZoneBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(HwLocationBasedTimeZoneUpdater.EVENT_STOP_LOCATION_UPDATE)) {
                        HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_ON, remove EVENT_STOP_LOCATION_UPDATE.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(HwLocationBasedTimeZoneUpdater.EVENT_STOP_LOCATION_UPDATE);
                    }
                    long tzUpdateDiff = SystemClock.elapsedRealtime() - HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime;
                    HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
                    hwLocationBasedTimeZoneUpdater.log("ACTION_SCREEN_ON, tzUpdateDiff: " + tzUpdateDiff + "ms, mTimeZoneUpdateTime: " + HwLocationBasedTimeZoneUpdater.this.mTimeZoneUpdateTime + "ms");
                    if (HwLocationBasedTimeZoneUpdater.this.getAutoTimeZone() && HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isNetworkLocationAvailable()) {
                        if (HwLocationBasedTimeZoneUpdater.this.mCurrentLocation == null || tzUpdateDiff > HwLocationBasedTimeZoneUpdater.this.getCheckPeriodByRoamingState()) {
                            HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_ON, send EVENT_REQUEST_LOCATION_UPDATE.");
                            HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE, true));
                        }
                    }
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (HwLocationBasedTimeZoneUpdater.this.mMyHandler.hasMessages(HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE)) {
                        HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_OFF, remove EVENT_REQUEST_LOCATION_UPDATE.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.removeMessages(HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE);
                    }
                    HwLocationBasedTimeZoneUpdater.this.log("ACTION_SCREEN_OFF, delay send EVENT_STOP_LOCATION_UPDATE.");
                    HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessageDelayed(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_STOP_LOCATION_UPDATE), 5000);
                } else if ("android.location.MODE_CHANGED".equals(action)) {
                    int currentLocationMode = Settings.Secure.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "location_mode", 0);
                    if (Settings.Secure.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "location_mode", 0) == 0) {
                        HwLocationBasedTimeZoneUpdater.this.log("Location service disabled, send EVENT_STOP_LOCATION_UPDATE immediately.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_STOP_LOCATION_UPDATE));
                    } else if (HwLocationBasedTimeZoneUpdater.this.getAutoTimeZone() && HwLocationBasedTimeZoneUpdater.this.mLastLocationMode == 0) {
                        HwLocationBasedTimeZoneUpdater.this.log("Location service enabled, send EVENT_REQUEST_LOCATION_UPDATE immediately.");
                        HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE, false));
                        HwLocationBasedTimeZoneUpdater.this.dismissLocationAccessNotification();
                        HwLocationBasedTimeZoneUpdater.this.mCurrentLocation = null;
                    }
                    HwLocationBasedTimeZoneUpdater.this.mLastLocationMode = currentLocationMode;
                } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    if (Settings.Global.getInt(HwLocationBasedTimeZoneUpdater.this.mCr, "airplane_mode_on", 0) > 0) {
                        HwLocationBasedTimeZoneUpdater.this.log("airplane mode on, reset mCurrentLocation and net iso.");
                        HwLocationBasedTimeZoneUpdater.this.mCurrentLocation = null;
                        HwLocationBasedTimeZoneUpdater.this.mLastValidNetIso = BuildConfig.FLAVOR;
                    }
                } else if (HwLocationBasedTimeZoneUpdater.ACTION_ENABLE_LOCATION.equals(action)) {
                    HwLocationBasedTimeZoneUpdater.this.log("receives ACTION_ENABLE_LOCATION, dismiss notification and enable location access.");
                    HwLocationBasedTimeZoneUpdater.this.dismissLocationAccessNotification();
                    HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.setLocationEnabled(true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class NetworkConnectionStateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkConnectionStateCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.this;
            hwLocationBasedTimeZoneUpdater.log("Default network available: " + network);
            if (HwLocationBasedTimeZoneUpdater.this.mCurrentLocation == null && HwLocationBasedTimeZoneUpdater.this.mHwLocationUpdateManager.isNetworkLocationAvailable()) {
                HwLocationBasedTimeZoneUpdater.this.log("request network location when mobile/wifi network available");
                HwLocationBasedTimeZoneUpdater.this.mMyHandler.sendMessage(HwLocationBasedTimeZoneUpdater.this.mMyHandler.obtainMessage(HwLocationBasedTimeZoneUpdater.EVENT_REQUEST_LOCATION_UPDATE, true));
            }
        }
    }

    public class LocationBasedCountryDetector {
        private final Context mContext;
        private CountryExt mDetectedCountry;
        private CountryListenerExt mListener;
        private Thread mQueryThread;

        public LocationBasedCountryDetector(Context ctx) {
            this.mContext = ctx;
        }

        private void setCountryListener(CountryListenerExt listener) {
            this.mListener = listener;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyListener(CountryExt country) {
            CountryListenerExt countryListenerExt = this.mListener;
            if (countryListenerExt != null) {
                countryListenerExt.onCountryDetected(country);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getCountryFromLocation(Location location) {
            try {
                List<Address> addresses = new Geocoder(this.mContext).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses == null || addresses.size() <= 0) {
                    return null;
                }
                return addresses.get(0).getCountryCode();
            } catch (IOException e) {
                HwLocationBasedTimeZoneUpdater.this.loge("Exception occurs when getting country from location");
                return null;
            } catch (IllegalArgumentException e2) {
                HwLocationBasedTimeZoneUpdater.this.loge("illegal argument exception");
                return null;
            }
        }

        public synchronized void queryCountryCode(final Location location, CountryListenerExt listener) {
            if (location == null) {
                notifyListener(null);
            } else if (this.mQueryThread == null) {
                setCountryListener(listener);
                this.mQueryThread = new Thread(new Runnable() {
                    /* class com.android.internal.telephony.HwLocationBasedTimeZoneUpdater.LocationBasedCountryDetector.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        String countryIso = null;
                        Location location = location;
                        if (location != null) {
                            countryIso = LocationBasedCountryDetector.this.getCountryFromLocation(location);
                        }
                        if (countryIso != null) {
                            LocationBasedCountryDetector.this.mDetectedCountry = CountryExt.from(countryIso, 1);
                        } else {
                            LocationBasedCountryDetector.this.mDetectedCountry = null;
                        }
                        LocationBasedCountryDetector locationBasedCountryDetector = LocationBasedCountryDetector.this;
                        locationBasedCountryDetector.notifyListener(locationBasedCountryDetector.mDetectedCountry);
                        LocationBasedCountryDetector.this.mQueryThread = null;
                        LocationBasedCountryDetector.this.mListener = null;
                    }
                });
                this.mQueryThread.start();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needUpdateTimeZoneByNitz() {
        String[] specialZone;
        PhoneExt phoneExt = PhoneFactoryExt.getDefaultPhone();
        if (phoneExt == null) {
            log("phoneExt is null");
            return true;
        }
        ServiceStateTrackerEx serviceStateTrackerEx = phoneExt.getServiceStateTracker();
        if (serviceStateTrackerEx == null) {
            log("serviceStateTrackerEx is null");
            return true;
        }
        String savedZoneId = BuildConfig.FLAVOR;
        try {
            savedZoneId = (String) serviceStateTrackerEx.getClass().getMethod("getSavedTimeZoneId", new Class[0]).invoke(serviceStateTrackerEx, new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            loge("getSavedTimeZoneId: fail to execute");
        }
        String countyIso = TelephonyManagerEx.getNetworkCountryIsoForPhone(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
        if ("Asia/Novosibirsk".equalsIgnoreCase(savedZoneId) && "RU".equalsIgnoreCase(countyIso)) {
            return false;
        }
        String countryZoneId = SystemPropertiesEx.get(UPDATE_TIME_ZONE_BY_NITZ, BuildConfig.FLAVOR);
        if (!TextUtils.isEmpty(countryZoneId)) {
            String[] info = countryZoneId.split(",");
            int i = 0;
            while (info != null && i < info.length) {
                if (!(info[i] == null || (specialZone = info[i].split(":")) == null || specialZone.length != 2)) {
                    if (HW_DEBUGGABLE) {
                        log("zoneId = " + specialZone[0] + "  country = " + specialZone[1]);
                    }
                    if (specialZone[0].equalsIgnoreCase(savedZoneId) && specialZone[1].equalsIgnoreCase(countyIso)) {
                        return false;
                    }
                }
                i++;
            }
        }
        return true;
    }
}
