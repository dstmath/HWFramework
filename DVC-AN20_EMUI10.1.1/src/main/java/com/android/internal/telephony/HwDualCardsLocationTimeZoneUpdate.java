package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.HwLocationBasedTimeZoneUpdater;
import com.android.internal.telephony.latlongtotimezone.TimezoneMapper;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.location.CountryExt;
import com.huawei.android.location.CountryListenerExt;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.MccTableExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.internal.telephony.TimeServiceHelperEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import com.huawei.libcore.timezone.TimeZoneFinderEx;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class HwDualCardsLocationTimeZoneUpdate extends Handler {
    private static final int AUTO_TIME_ZONE_OFF = 0;
    private static final int EVENT_LOCATION_CHANGED = 100;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 1;
    private static final boolean HW_DEBUGGABLE;
    private static final int INVAILD_SUBID = -1;
    private static final String LOG_TAG = "HwDualCardsLocTZUpdate";
    private static final int MCC_LEN = 3;
    private static final int NUMERIC_MIN_LEN = 5;
    private static final int PHONE_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final long REQUEST_CURRENT_LOCATION = 1800000;
    private static final int SINGLE_CARD_PHONE = 1;
    private static final int SINGLE_TIME_ZONE_COUNTRY = 1;
    private static boolean isAllowUpdateOnce = false;
    private static HwDualCardsLocationTimeZoneUpdate mInstance = null;
    private boolean hasRegTzLocUpdater = false;
    private boolean hasUpdateTzByLoc = false;
    private ConnectivityManager mCM;
    private Context mContext;
    private HwLocationBasedTimeZoneUpdater.LocationBasedCountryDetector mCountryDetector;
    private CountryListenerExt mCountryListener = new CountryListenerExt() {
        /* class com.android.internal.telephony.HwDualCardsLocationTimeZoneUpdate.AnonymousClass4 */

        @Override // com.huawei.android.location.CountryListenerExt
        public void onCountryDetected(CountryExt country) {
            if (HwDualCardsLocationTimeZoneUpdate.HW_DEBUGGABLE) {
                HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate = HwDualCardsLocationTimeZoneUpdate.this;
                hwDualCardsLocationTimeZoneUpdate.log("Country detected " + country);
            }
            if (country != null) {
                String newZoneId = HwDualCardsLocationTimeZoneUpdate.this.mLocationBasedTimeZoneUpdater.getTimezoneIdByCountry(country);
                String currentZoneId = TimeZone.getDefault().getID();
                if (HwDualCardsLocationTimeZoneUpdate.HW_DEBUGGABLE) {
                    HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate2 = HwDualCardsLocationTimeZoneUpdate.this;
                    hwDualCardsLocationTimeZoneUpdate2.log("onCountryDetected, current time zone: " + currentZoneId + "  new zone: " + newZoneId);
                }
                if (HwDualCardsLocationTimeZoneUpdate.this.isVaildZoneId(newZoneId)) {
                    if (!newZoneId.equals(currentZoneId)) {
                        HwDualCardsLocationTimeZoneUpdate.this.setTimeZone(newZoneId, HwLocationBasedTimeZoneUpdater.SOURCE_COUNTRY_DETECTOR);
                    }
                    HwDualCardsLocationTimeZoneUpdate.this.hasUpdateTzByLoc = true;
                }
            }
        }
    };
    private ContentResolver mCr = null;
    private Location mCurrentLocation = null;
    private HwLocationUpdateManager mHwLocationUpdateManager;
    private long mLastGetLocTime;
    private HwLocationBasedTimeZoneUpdater mLocationBasedTimeZoneUpdater;
    private NetworkStateUpdateCallback mNetworkStateUpdateCallback;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwDualCardsLocationTimeZoneUpdate.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot", -1);
                HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate = HwDualCardsLocationTimeZoneUpdate.this;
                hwDualCardsLocationTimeZoneUpdate.log("ACTION_SERVICE_STATE_CHANGED." + slotId);
                PhoneExt phone = PhoneFactoryExt.getPhone(slotId);
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
    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwDualCardsLocationTimeZoneUpdate.AnonymousClass3 */

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
                    return;
                }
                HwDualCardsLocationTimeZoneUpdate.this.log("There is no need to request location.");
            }
        }
    };
    private ServiceStateTrackerEx mServiceStateTracker;
    private TimeServiceHelperEx mTimeServiceHelper;
    private String numericSub1 = "";
    private String numericSub2 = "";

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.debuggable", 0) == 1) {
            z = true;
        }
        HW_DEBUGGABLE = z;
    }

    private HwDualCardsLocationTimeZoneUpdate(Context context) {
        this.mContext = context;
        if (PHONE_NUM == 1) {
            log("this is a single card cell phone.");
            return;
        }
        this.mHwLocationUpdateManager = new HwLocationUpdateManager(this.mContext, this);
        this.mLocationBasedTimeZoneUpdater = HwLocationBasedTimeZoneUpdater.getInstance();
        HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = this.mLocationBasedTimeZoneUpdater;
        if (hwLocationBasedTimeZoneUpdater != null) {
            this.mCountryDetector = hwLocationBasedTimeZoneUpdater.getCountryDetector();
        }
        for (int index = 0; index < PHONE_NUM; index++) {
            if (PhoneFactoryExt.getPhone(index) != null) {
                PhoneFactoryExt.getPhone(index).getCi().registerForRplmnsStateChanged(this, 1, Integer.valueOf(index));
            }
        }
        this.mServiceStateTracker = PhoneFactoryExt.getPhone(0).getServiceStateTracker();
        sendMessage(obtainMessage(1));
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mCr = context2.getContentResolver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SERVICE_STATE");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            this.mTimeServiceHelper = new TimeServiceHelperEx(this.mContext);
            TimeServiceHelperEx timeServiceHelperEx = this.mTimeServiceHelper;
            Objects.requireNonNull(timeServiceHelperEx);
            this.mTimeServiceHelper.setListener(new TimeServiceHelperEx.ListenerEx(timeServiceHelperEx) {
                /* class com.android.internal.telephony.HwDualCardsLocationTimeZoneUpdate.AnonymousClass1 */

                {
                    Objects.requireNonNull(x0);
                }

                @Override // com.huawei.internal.telephony.TimeServiceHelperEx.ListenerEx
                public void onTimeDetectionChange(boolean enabled) {
                }

                @Override // com.huawei.internal.telephony.TimeServiceHelperEx.ListenerEx
                public void onTimeZoneDetectionChange(boolean enabled) {
                    HwDualCardsLocationTimeZoneUpdate hwDualCardsLocationTimeZoneUpdate = HwDualCardsLocationTimeZoneUpdate.this;
                    hwDualCardsLocationTimeZoneUpdate.log("auto update time zone key change to " + enabled);
                    if (enabled) {
                        boolean unused = HwDualCardsLocationTimeZoneUpdate.isAllowUpdateOnce = true;
                        if (HwDualCardsLocationTimeZoneUpdate.this.mServiceStateTracker != null) {
                            HwDualCardsLocationTimeZoneUpdate.this.mServiceStateTracker.handleAutoTimeZoneEnabledHw();
                            return;
                        }
                        return;
                    }
                    HwDualCardsLocationTimeZoneUpdate.this.hasUpdateTzByLoc = false;
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
            RlogEx.e(LOG_TAG, "mInstance null");
        }
        return mInstance;
    }

    public boolean isNeedLocationTimeZoneUpdate(PhoneExt phone, String zoneId) {
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

    private boolean isForbiddenUpdateTimeZoneByRegState(PhoneExt phone, String zoneId) {
        PhoneExt vSimPhone = HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyVSimFactoryImpl").createHwInnerVSimManager().getVSimPhone();
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimEnabled() && isRegInService(vSimPhone) && !getRoamingState(vSimPhone)) {
            String vSimZoneId = getTimeZoneId(getMcc(vSimPhone));
            if (HW_DEBUGGABLE) {
                log("vsim zoneId=" + vSimZoneId);
            }
            if (!TextUtils.isEmpty(vSimZoneId)) {
                return true ^ vSimZoneId.equals(zoneId);
            }
            return false;
        } else if (!getRoamingState(phone)) {
            return false;
        } else {
            int otherSlotId = getOtherSlotId(phone);
            if (!isCardPresent(otherSlotId)) {
                log("only one card present,allow update time zone.");
                return false;
            }
            PhoneExt otherPhone = PhoneFactoryExt.getPhone(otherSlotId);
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
    /* access modifiers changed from: public */
    private boolean isDualCardsIsoNotEquals() {
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
            PhoneExt phone = PhoneFactoryExt.getPhone(index);
            ServiceState serviceState = phone != null ? phone.getServiceState() : null;
            if (serviceState == null) {
                return;
            }
            if (ServiceStateEx.getVoiceRegState(serviceState) != 0 || ServiceStateEx.getDataState(serviceState) != 0) {
                updatePlmn(SystemPropertiesEx.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, ""), index);
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
        if (AsyncResultEx.from(msg.obj) != null) {
            AsyncResultEx arEx = AsyncResultEx.from(msg.obj);
            if (arEx.getUserObj() == null || !(arEx.getUserObj() instanceof Integer)) {
                return 0;
            }
            return (Integer) arEx.getUserObj();
        }
        RlogEx.i(LOG_TAG, "invalid index, use default");
        return 0;
    }

    private void updateTimeZoneByLocation(Location location) {
        this.mCurrentLocation = location;
        if (this.mCurrentLocation != null) {
            this.mLastGetLocTime = SystemClock.elapsedRealtime();
            if (getAutoTimeZone()) {
                String newZoneId = TimezoneMapper.latLngToTimezoneString(this.mCurrentLocation.getLatitude(), this.mCurrentLocation.getLongitude());
                if (isVaildZoneId(newZoneId)) {
                    String currentZoneId = TimeZone.getDefault().getID();
                    if (HW_DEBUGGABLE) {
                        log("updateTimeZoneByLocation, current time zone: " + currentZoneId + "  new zone: " + newZoneId);
                    }
                    if (!newZoneId.equals(currentZoneId)) {
                        setTimeZone(newZoneId, HwLocationBasedTimeZoneUpdater.SOURCE_LOCATION);
                    }
                    this.hasUpdateTzByLoc = true;
                    return;
                }
                HwLocationBasedTimeZoneUpdater.LocationBasedCountryDetector locationBasedCountryDetector = this.mCountryDetector;
                if (locationBasedCountryDetector != null) {
                    locationBasedCountryDetector.queryCountryCode(this.mCurrentLocation, this.mCountryListener);
                    return;
                }
                return;
            }
            loge("Auto time zone disabled!");
            return;
        }
        loge("current loction is null!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTimeZone(String zoneId, String source) {
        HwLocationBasedTimeZoneUpdater hwLocationBasedTimeZoneUpdater = this.mLocationBasedTimeZoneUpdater;
        if (hwLocationBasedTimeZoneUpdater != null) {
            hwLocationBasedTimeZoneUpdater.setTimeZone(zoneId, source);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isVaildZoneId(String zoneId) {
        if (TextUtils.isEmpty(zoneId) || "unknown".equals(zoneId) || "unusedtimezone".equals(zoneId)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerTimeZoneLocationUpdater() {
        if (!this.hasRegTzLocUpdater) {
            log("registerTimeZoneLocationUpdater!");
            this.mHwLocationUpdateManager.registerPassiveLocationUpdate();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mContext.registerReceiver(this.mScreenOnReceiver, intentFilter);
            this.hasRegTzLocUpdater = true;
            this.mNetworkStateUpdateCallback = new NetworkStateUpdateCallback();
            this.mCM.registerDefaultNetworkCallback(this.mNetworkStateUpdateCallback);
            PhoneExt phone = PhoneFactoryExt.getPhone(0);
            PowerManager powerManager = null;
            if (!(phone == null || phone.getContext() == null)) {
                powerManager = (PowerManager) phone.getContext().getSystemService("power");
            }
            if (powerManager != null && powerManager.isScreenOn() && this.mCurrentLocation == null) {
                log("screen is on, request location.");
                this.mHwLocationUpdateManager.requestLocationUpdate(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterTimeZoneLocationUpdater() {
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
    public class NetworkStateUpdateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkStateUpdateCallback() {
        }

        public void onAvailable(Network network) {
            if (HwDualCardsLocationTimeZoneUpdate.this.mCurrentLocation == null) {
                HwDualCardsLocationTimeZoneUpdate.this.log("Get location when ps attach is have not got location before.");
                HwDualCardsLocationTimeZoneUpdate.this.mHwLocationUpdateManager.requestLocationUpdate(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePlmn(String plmn, int subId) {
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

    private int getOtherSlotId(PhoneExt phone) {
        int ownSubId = phone.getPhoneId();
        if (ownSubId == 0) {
            return 1;
        }
        if (ownSubId == 1) {
            return 0;
        }
        return -1;
    }

    private String getMcc(PhoneExt phone) {
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
        ServiceStateTrackerEx serviceStateTrackerEx = this.mServiceStateTracker;
        String zoneId = serviceStateTrackerEx != null ? serviceStateTrackerEx.getTimeZoneFromMcc(mcc) : null;
        if (zoneId != null) {
            return zoneId;
        }
        try {
            List<android.icu.util.TimeZone> timeZones = TimeZoneFinderEx.lookupTimeZonesByCountry(MccTableExt.countryCodeForMcc(Integer.parseInt(mcc)));
            if (timeZones != null && timeZones.size() == 1) {
                return timeZones.get(0).getID();
            }
        } catch (NumberFormatException e) {
            log("numeric is not valid NumberFormatException.");
        }
        return "";
    }

    private boolean getRoamingState(PhoneExt phone) {
        ServiceState serviceState = phone != null ? phone.getServiceState() : null;
        if (serviceState != null) {
            return serviceState.getRoaming();
        }
        log("serviceState is null retrun true.");
        return false;
    }

    private boolean isRegInService(PhoneExt phone) {
        ServiceState serviceState = phone != null ? phone.getServiceState() : null;
        if (serviceState == null) {
            return false;
        }
        if (ServiceStateEx.getVoiceRegState(serviceState) == 0 || ServiceStateEx.getDataState(serviceState) == 0) {
            return true;
        }
        return false;
    }

    public boolean isCardPresent(int slotId) {
        UiccControllerExt uiccController = UiccControllerExt.getInstance();
        if (uiccController.getUiccCard(slotId) == null || uiccController.getUiccCard(slotId).getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        RlogEx.i(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        RlogEx.e(LOG_TAG, s);
    }
}
