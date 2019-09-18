package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Country;
import android.location.CountryListener;
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
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.HwLocationBasedTimeZoneUpdater;
import com.android.internal.telephony.TimeServiceHelper;
import com.android.internal.telephony.latlongtotimezone.TimezoneMapper;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.util.List;
import java.util.TimeZone;
import libcore.util.TimeZoneFinder;

public class HwDualCardsLocationTimeZoneUpdate extends Handler {
    private static final int AUTO_TIME_ZONE_OFF = 0;
    private static final int EVENT_LOCATION_CHANGED = 100;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 1;
    private static final int INVAILD_SUBID = -1;
    private static final String LOG_TAG = "HwDualCardsLocTZUpdate";
    private static final int MCC_LEN = 3;
    private static final int NUMERIC_MIN_LEN = 5;
    private static final int PHONE_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final long REQUEST_CURRENT_LOCATION = 1800000;
    private static final int SINGLE_CARD_PHONE = 1;
    private static final int SINGLE_TIME_ZONE_COUNTRY = 1;
    /* access modifiers changed from: private */
    public static boolean isAllowUpdateOnce = false;
    private static HwDualCardsLocationTimeZoneUpdate mInstance = null;
    private boolean hasRegTzLocUpdater;
    /* access modifiers changed from: private */
    public boolean hasUpdateTzByLoc;
    private ConnectivityManager mCM;
    private Context mContext;
    private HwLocationBasedTimeZoneUpdater.LocationBasedCountryDetector mCountryDetector;
    private CountryListener mCountryListener;
    private ContentResolver mCr = null;
    /* access modifiers changed from: private */
    public Location mCurrentLocation = null;
    /* access modifiers changed from: private */
    public HwLocationUpdateManager mHwLocationUpdateManager;
    /* access modifiers changed from: private */
    public long mLastGetLocTime;
    /* access modifiers changed from: private */
    public HwLocationBasedTimeZoneUpdater mLocationBasedTimeZoneUpdater;
    private NetworkStateUpdateCallback mNetworkStateUpdateCallback;
    /* access modifiers changed from: private */
    public NitzStateMachine mNitzState;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mScreenOnReceiver;
    private TimeServiceHelper mTimeServiceHelper;
    private String numericSub1 = "";
    private String numericSub2 = "";

    private class NetworkStateUpdateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkStateUpdateCallback() {
        }

        public void onAvailable(Network network) {
            if (HwDualCardsLocationTimeZoneUpdate.this.mCurrentLocation == null) {
                HwDualCardsLocationTimeZoneUpdate.this.log("Get location when ps attach is have not got location before.");
                HwDualCardsLocationTimeZoneUpdate.this.mHwLocationUpdateManager.requestLocationUpdate(true);
            }
        }
    }

    private HwDualCardsLocationTimeZoneUpdate(Context context) {
        ServiceStateTracker serviceStateTracker = null;
        this.hasUpdateTzByLoc = false;
        this.hasRegTzLocUpdater = false;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                    int slotId = intent.getIntExtra("subscription", -1);
                    HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate = HwDualCardsLocationTimeZoneUpdate.this;
                    hwDualCardsLocationTimeZoneUpdate.log("ACTION_SERVICE_STATE_CHANGED." + slotId);
                    Phone phone = PhoneFactory.getPhone(slotId);
                    ServiceState serviceState = phone != null ? phone.getServiceState() : null;
                    if (serviceState != null) {
                        String numeric = serviceState.getOperatorNumeric();
                        HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate2 = HwDualCardsLocationTimeZoneUpdate.this;
                        hwDualCardsLocationTimeZoneUpdate2.log("numeric:" + numeric);
                        HwDualCardsLocationTimeZoneUpdate.this.updatePlmn(numeric, slotId);
                    }
                    if (HwDualCardsLocationTimeZoneUpdate.this.isDualCardsIsoNotEquals()) {
                        HwDualCardsLocationTimeZoneUpdate.this.registerTimeZoneLocationUpdater();
                    } else {
                        HwDualCardsLocationTimeZoneUpdate.this.unregisterTimeZoneLocationUpdater();
                    }
                }
            }
        };
        this.mScreenOnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && "android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    long lastUpdateTimeZoneSpace = SystemClock.elapsedRealtime() - HwDualCardsLocationTimeZoneUpdate.this.mLastGetLocTime;
                    if (HwDualCardsLocationTimeZoneUpdate.this.mCurrentLocation == null || lastUpdateTimeZoneSpace > HwDualCardsLocationTimeZoneUpdate.REQUEST_CURRENT_LOCATION) {
                        if (HwDualCardsLocationTimeZoneUpdate.this.mCurrentLocation == null) {
                            HwDualCardsLocationTimeZoneUpdate.this.log("request location: mCurrentLocation is null");
                        } else {
                            HwDualCardsLocationTimeZoneUpdate.this.log("request location: last get location space is over 0.5H");
                        }
                        HwDualCardsLocationTimeZoneUpdate.this.mHwLocationUpdateManager.requestLocationUpdate(false);
                    } else {
                        HwDualCardsLocationTimeZoneUpdate.this.log("There is no need to request location.");
                    }
                }
            }
        };
        this.mCountryListener = new CountryListener() {
            public void onCountryDetected(Country country) {
                HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate = HwDualCardsLocationTimeZoneUpdate.this;
                hwDualCardsLocationTimeZoneUpdate.log("Country detected " + country);
                if (country != null) {
                    String newZoneId = HwDualCardsLocationTimeZoneUpdate.this.mLocationBasedTimeZoneUpdater.getTimezoneIdByCountry(country);
                    String currentZoneId = TimeZone.getDefault().getID();
                    HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate2 = HwDualCardsLocationTimeZoneUpdate.this;
                    hwDualCardsLocationTimeZoneUpdate2.log("onCountryDetected, current time zone: " + currentZoneId + "  new zone: " + newZoneId);
                    if (HwDualCardsLocationTimeZoneUpdate.this.isVaildZoneId(newZoneId)) {
                        if (!newZoneId.equals(currentZoneId)) {
                            HwDualCardsLocationTimeZoneUpdate.this.setTimeZone(newZoneId, HwLocationBasedTimeZoneUpdater.SOURCE_COUNTRY_DETECTOR);
                        }
                        boolean unused = HwDualCardsLocationTimeZoneUpdate.this.hasUpdateTzByLoc = true;
                    }
                }
            }
        };
        this.mContext = context;
        if (PHONE_NUM == 1) {
            log("this is a single card cell phone.");
            return;
        }
        this.mHwLocationUpdateManager = new HwLocationUpdateManager(this.mContext, this);
        this.mLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.getInstance();
        if (this.mLocationBasedTimeZoneUpdater != null) {
            this.mCountryDetector = this.mLocationBasedTimeZoneUpdater.getCountryDetector();
        }
        for (int index = 0; index < PHONE_NUM; index++) {
            if (PhoneFactory.getPhone(index) != null) {
                PhoneFactory.getPhone(index).mCi.registerForRplmnsStateChanged(this, 1, Integer.valueOf(index));
            }
        }
        Phone phone = PhoneFactory.getDefaultPhone();
        serviceStateTracker = phone != null ? phone.getServiceStateTracker() : serviceStateTracker;
        if (serviceStateTracker != null) {
            this.mNitzState = serviceStateTracker.getNitzState();
        }
        sendMessage(obtainMessage(1));
        if (this.mContext != null) {
            this.mCr = this.mContext.getContentResolver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SERVICE_STATE");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            this.mTimeServiceHelper = new TimeServiceHelper(this.mContext);
            this.mTimeServiceHelper.setListener(new TimeServiceHelper.Listener() {
                public void onTimeDetectionChange(boolean enabled) {
                }

                public void onTimeZoneDetectionChange(boolean enabled) {
                    HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate = HwDualCardsLocationTimeZoneUpdate.this;
                    hwDualCardsLocationTimeZoneUpdate.log("auto update time zone key change to " + enabled);
                    if (enabled) {
                        boolean unused = HwDualCardsLocationTimeZoneUpdate.isAllowUpdateOnce = true;
                        if (HwDualCardsLocationTimeZoneUpdate.this.mNitzState != null) {
                            HwDualCardsLocationTimeZoneUpdate.this.mNitzState.handleAutoTimeZoneEnabled();
                            return;
                        }
                        return;
                    }
                    boolean unused2 = HwDualCardsLocationTimeZoneUpdate.this.hasUpdateTzByLoc = false;
                }
            });
        } else {
            log("mContext is null");
        }
        log("HwDualCardsLocationTimeZoneUpdate init ");
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new HwDualCardsLocationTimeZoneUpdate(context);
        }
    }

    public static HwDualCardsLocationTimeZoneUpdate getDefault() {
        if (mInstance == null) {
            Rlog.e(LOG_TAG, "mInstance null");
        }
        return mInstance;
    }

    public boolean isNeedLocationTimeZoneUpdate(Phone phone, String zoneId) {
        if (PHONE_NUM == 1) {
            log("this is a single card cell phone.");
            return false;
        } else if (!this.hasUpdateTzByLoc) {
            return isForbiddenUpdateTimeZoneByRegState(phone, zoneId);
        } else {
            log("update time zone by location.");
            return true;
        }
    }

    private boolean isForbiddenUpdateTimeZoneByRegState(Phone phone, String zoneId) {
        Phone vSimPhone = VSimUtilsInner.getVSimPhone();
        if (HwVSimUtils.isVSimEnabled() && isRegInService(vSimPhone) && !getRoamingState(vSimPhone)) {
            String vSimZoneId = getTimeZoneId(getMcc(vSimPhone));
            log("vsim zoneId=" + vSimZoneId);
            if (!TextUtils.isEmpty(vSimZoneId)) {
                return true ^ vSimZoneId.equals(zoneId);
            }
            return false;
        } else if (!getRoamingState(phone)) {
            return false;
        } else {
            int otherSubId = getOtherSubId(phone);
            if (!isCardPresent(otherSubId)) {
                log("only one card present,allow update time zone.");
                return false;
            }
            Phone otherPhone = PhoneFactory.getPhone(otherSubId);
            if (!isRegInService(otherPhone)) {
                log("the card is first registration, update time zone.");
                return false;
            } else if (!isDualCardsIsoNotEquals()) {
                log("dual cards register in the same contury.");
                return false;
            } else {
                if (getRoamingState(otherPhone)) {
                    String currentZoneId = TimeZone.getDefault().getID();
                    if (isVaildZoneId(zoneId) && zoneId.equals(currentZoneId)) {
                        log("dual cards present ,allow update time zone.");
                        return false;
                    }
                }
                if (!isAllowUpdateOnce) {
                    return true;
                }
                isAllowUpdateOnce = false;
                if (this.hasUpdateTzByLoc) {
                    log("dual cards registration roaming, requst location updata time zone.");
                    this.mHwLocationUpdateManager.requestLocationUpdate(false);
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isDualCardsIsoNotEquals() {
        log("numericSub1:" + this.numericSub1 + "  numericSub2: " + this.numericSub2);
        if (isInvalidOperatorNumeric(this.numericSub1) || isInvalidOperatorNumeric(this.numericSub2)) {
            return false;
        }
        return !this.numericSub1.substring(0, 3).equals(this.numericSub2.substring(0, 3));
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            int index = getCiIndex(msg).intValue();
            log("EVENT_RPLMNS_STATE_CHANGED" + index);
            Phone phone = PhoneFactory.getPhone(index);
            ServiceState serviceState = phone != null ? phone.getServiceState() : null;
            if (serviceState == null) {
                return;
            }
            if (serviceState.getVoiceRegState() != 0 || serviceState.getDataRegState() != 0) {
                updatePlmn(SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, ""), index);
            }
        } else if (i != 100) {
            log("unknow msg:" + msg.what);
        } else {
            log("EVENT_LOCATION_CHANGED");
            updateTimeZoneByLocation((Location) msg.obj);
        }
    }

    private Integer getCiIndex(Message msg) {
        if (msg == null) {
            return 0;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return (Integer) msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return 0;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return 0;
        }
        return (Integer) ar.userObj;
    }

    private void updateTimeZoneByLocation(Location location) {
        this.mCurrentLocation = location;
        if (this.mCurrentLocation != null) {
            this.mLastGetLocTime = SystemClock.elapsedRealtime();
            if (getAutoTimeZone()) {
                String newZoneId = TimezoneMapper.latLngToTimezoneString(this.mCurrentLocation.getLatitude(), this.mCurrentLocation.getLongitude());
                if (isVaildZoneId(newZoneId)) {
                    String currentZoneId = TimeZone.getDefault().getID();
                    log("updateTimeZoneByLocation, current time zone: " + currentZoneId + "  new zone: " + newZoneId);
                    if (!newZoneId.equals(currentZoneId)) {
                        setTimeZone(newZoneId, HwLocationBasedTimeZoneUpdater.SOURCE_LOCATION);
                    }
                    this.hasUpdateTzByLoc = true;
                } else if (this.mCountryDetector != null) {
                    this.mCountryDetector.queryCountryCode(this.mCurrentLocation, this.mCountryListener);
                }
            } else {
                loge("Auto time zone disabled!");
            }
        } else {
            loge("current loction is null!");
        }
    }

    /* access modifiers changed from: private */
    public void setTimeZone(String zoneId, String source) {
        if (this.mLocationBasedTimeZoneUpdater != null) {
            this.mLocationBasedTimeZoneUpdater.setTimeZone(zoneId, source);
        }
    }

    /* access modifiers changed from: private */
    public boolean isVaildZoneId(String zoneId) {
        if (TextUtils.isEmpty(zoneId) || "unknown".equals(zoneId) || "unusedtimezone".equals(zoneId)) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: android.os.PowerManager} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    public void registerTimeZoneLocationUpdater() {
        if (!this.hasRegTzLocUpdater) {
            log("registerTimeZoneLocationUpdater!");
            this.mHwLocationUpdateManager.registerPassiveLocationUpdate();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mContext.registerReceiver(this.mScreenOnReceiver, intentFilter);
            this.hasRegTzLocUpdater = true;
            this.mNetworkStateUpdateCallback = new NetworkStateUpdateCallback();
            this.mCM.registerDefaultNetworkCallback(this.mNetworkStateUpdateCallback);
            Phone phone = PhoneFactory.getDefaultPhone();
            PowerManager powerManager = null;
            if (!(phone == null || phone.getContext() == null)) {
                powerManager = phone.getContext().getSystemService("power");
            }
            if (powerManager != null && powerManager.isScreenOn() && this.mCurrentLocation == null) {
                log("screen is on, request location.");
                this.mHwLocationUpdateManager.requestLocationUpdate(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unregisterTimeZoneLocationUpdater() {
        if (this.hasRegTzLocUpdater) {
            log("unregisterTimeZoneLocationUpdater!");
            this.mCurrentLocation = null;
            this.mHwLocationUpdateManager.unregisterPassiveLocationUpdate();
            this.mContext.unregisterReceiver(this.mScreenOnReceiver);
            this.mCM.unregisterNetworkCallback(this.mNetworkStateUpdateCallback);
            this.hasRegTzLocUpdater = false;
        }
        this.hasUpdateTzByLoc = false;
    }

    /* access modifiers changed from: private */
    public void updatePlmn(String plmn, int subId) {
        if (subId == 0) {
            this.numericSub1 = plmn;
        } else if (subId == 1) {
            this.numericSub2 = plmn;
        }
    }

    private boolean isInvalidOperatorNumeric(String operatorNumeric) {
        return TextUtils.isEmpty(operatorNumeric) || operatorNumeric.length() < 5;
    }

    private boolean getAutoTimeZone() {
        return Settings.Global.getInt(this.mCr, "auto_time_zone", 0) != 0;
    }

    private int getOtherSubId(Phone phone) {
        int ownSubId = phone.getSubId();
        if (ownSubId == 0) {
            return 1;
        }
        if (ownSubId == 1) {
            return 0;
        }
        return -1;
    }

    private String getMcc(Phone phone) {
        ServiceState serviceState = phone != null ? phone.getServiceState() : null;
        if (serviceState == null) {
            log("serviceState is null.");
            return "";
        }
        String numeric = serviceState.getOperatorNumeric();
        if (!isInvalidOperatorNumeric(numeric)) {
            return numeric.substring(0, 3);
        }
        log("numeric is not valid.");
        return "";
    }

    private String getTimeZoneId(String mcc) {
        if (TextUtils.isEmpty(mcc)) {
            return "";
        }
        String zoneId = this.mNitzState != null ? this.mNitzState.getTimeZoneFromMcc(mcc) : null;
        if (zoneId != null) {
            return zoneId;
        }
        List<android.icu.util.TimeZone> timeZones = TimeZoneFinder.getInstance().lookupTimeZonesByCountry(MccTable.countryCodeForMcc(Integer.parseInt(mcc)));
        if (timeZones == null || timeZones.size() != 1) {
            return "";
        }
        return timeZones.get(0).getID();
    }

    private boolean getRoamingState(Phone phone) {
        ServiceState serviceState = phone != null ? phone.getServiceState() : null;
        if (serviceState != null) {
            return serviceState.getRoaming();
        }
        log("serviceState is null retrun true.");
        return false;
    }

    private boolean isRegInService(Phone phone) {
        ServiceState serviceState = phone != null ? phone.getServiceState() : null;
        boolean z = false;
        if (serviceState == null) {
            return false;
        }
        if (serviceState.getVoiceRegState() == 0 || serviceState.getDataRegState() == 0) {
            z = true;
        }
        return z;
    }

    public boolean isCardPresent(int slotId) {
        UiccController uiccController = UiccController.getInstance();
        boolean z = false;
        if (uiccController.getUiccCard(slotId) == null) {
            return false;
        }
        if (uiccController.getUiccCard(slotId).getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
