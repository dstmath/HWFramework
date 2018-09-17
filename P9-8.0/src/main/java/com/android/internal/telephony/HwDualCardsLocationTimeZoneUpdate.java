package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.util.TimeZone;

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
    private static HwDualCardsLocationTimeZoneUpdate mInstance = null;
    private boolean hasRegTzLocUpdater = false;
    private boolean hasUpdateTzByLoc = false;
    private Context mContext;
    private ContentResolver mCr = null;
    private Location mCurrentLocation = null;
    private HwLocationUpdateManager mHwLocationUpdateManager;
    private long mLastGetLocTime;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                    int slotId = intent.getIntExtra("subscription", -1);
                    HwDualCardsLocationTimeZoneUpdate.this.log("ACTION_SERVICE_STATE_CHANGED." + slotId);
                    Phone phone = PhoneFactory.getPhone(slotId);
                    ServiceState serviceState = phone != null ? phone.getServiceState() : null;
                    if (serviceState != null) {
                        String numeric = serviceState.getOperatorNumeric();
                        HwDualCardsLocationTimeZoneUpdate.this.log("numeric:" + numeric);
                        HwDualCardsLocationTimeZoneUpdate.this.updatePlmn(numeric, slotId);
                    }
                    if (HwDualCardsLocationTimeZoneUpdate.this.isDualCardsIsoNotEquals()) {
                        HwDualCardsLocationTimeZoneUpdate.this.registerTimeZoneLocationUpdater();
                    } else {
                        HwDualCardsLocationTimeZoneUpdate.this.unregisterTimeZoneLocationUpdater();
                    }
                }
            }
        }
    };
    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    long lastUpdateTimeZoneSpace = SystemClock.elapsedRealtime() - HwDualCardsLocationTimeZoneUpdate.this.mLastGetLocTime;
                    if (HwDualCardsLocationTimeZoneUpdate.this.mCurrentLocation == null || lastUpdateTimeZoneSpace > HwDualCardsLocationTimeZoneUpdate.REQUEST_CURRENT_LOCATION) {
                        if (HwDualCardsLocationTimeZoneUpdate.this.mCurrentLocation == null) {
                            HwDualCardsLocationTimeZoneUpdate.this.log("request location: mCurrentLocation is null");
                        } else {
                            HwDualCardsLocationTimeZoneUpdate.this.log("request location: last get location space is over 0.5H");
                        }
                        HwDualCardsLocationTimeZoneUpdate.this.mHwLocationUpdateManager.requestLocationUpdate(true);
                    } else {
                        HwDualCardsLocationTimeZoneUpdate.this.log("There is no need to request location.");
                    }
                }
            }
        }
    };
    private String numericSub1 = "";
    private String numericSub2 = "";

    private HwDualCardsLocationTimeZoneUpdate(Context context) {
        this.mContext = context;
        if (PHONE_NUM == 1) {
            log("this is a single card cell phone.");
            return;
        }
        this.mHwLocationUpdateManager = new HwLocationUpdateManager(this.mContext, this);
        for (int index = 0; index < PHONE_NUM; index++) {
            if (PhoneFactory.getPhone(index) != null) {
                PhoneFactory.getPhone(index).mCi.registerForRplmnsStateChanged(this, 1, Integer.valueOf(index));
            }
        }
        sendMessage(obtainMessage(1));
        if (this.mContext != null) {
            this.mCr = this.mContext.getContentResolver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SERVICE_STATE");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
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

    public boolean isNeedLocationTimeZoneUpdate() {
        if (PHONE_NUM != 1) {
            return this.hasUpdateTzByLoc;
        }
        log("this is a single card cell phone.");
        return false;
    }

    private boolean isDualCardsIsoNotEquals() {
        boolean z = false;
        log("numericSub1:" + this.numericSub1 + "  numericSub2: " + this.numericSub2);
        if (isInvalidOperatorNumeric(this.numericSub1) || isInvalidOperatorNumeric(this.numericSub2)) {
            return false;
        }
        if (!this.numericSub1.substring(0, 3).equals(this.numericSub2.substring(0, 3))) {
            z = true;
        }
        return z;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                int index = getCiIndex(msg).intValue();
                log("EVENT_RPLMNS_STATE_CHANGED" + index);
                Phone phone = PhoneFactory.getPhone(index);
                ServiceState serviceState = phone != null ? phone.getServiceState() : null;
                if (serviceState == null) {
                    return;
                }
                if (serviceState.getVoiceRegState() != 0 || serviceState.getDataRegState() != 0) {
                    updatePlmn(SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, ""), index);
                    return;
                }
                return;
            case 100:
                log("EVENT_LOCATION_CHANGED");
                updateTimeZoneByLocation((Location) msg.obj);
                return;
            default:
                log("unknow msg:" + msg.what);
                return;
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = Integer.valueOf(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    private void updateTimeZoneByLocation(Location location) {
        this.mCurrentLocation = location;
        if (this.mCurrentLocation != null) {
            this.mLastGetLocTime = SystemClock.elapsedRealtime();
            if (getAutoTimeZone()) {
                String newZoneId = TimezoneMapper.latLngToTimezoneString(this.mCurrentLocation.getLatitude(), this.mCurrentLocation.getLongitude());
                String currentZoneId = TimeZone.getDefault().getID();
                log("updateTimeZoneByLocation, current time zone: " + currentZoneId + "  new zone: " + newZoneId);
                if (isVaildZoneId(newZoneId) && (newZoneId.equals(currentZoneId) ^ 1) != 0) {
                    setTimeZone(newZoneId);
                    this.hasUpdateTzByLoc = true;
                    return;
                }
                return;
            }
            loge("Auto time zone disabled!");
            return;
        }
        loge("current loction is null!");
    }

    private void setTimeZone(String zoneId) {
        if (this.mContext != null) {
            AlarmManager alarm = (AlarmManager) this.mContext.getSystemService("alarm");
            if (alarm != null) {
                alarm.setTimeZone(zoneId);
            }
        }
    }

    private boolean isVaildZoneId(String zoneId) {
        if (TextUtils.isEmpty(zoneId) || "unknown".equals(zoneId) || "unusedtimezone".equals(zoneId)) {
            return false;
        }
        return true;
    }

    private void registerTimeZoneLocationUpdater() {
        if (!this.hasRegTzLocUpdater) {
            log("registerTimeZoneLocationUpdater!");
            this.mHwLocationUpdateManager.registerPassiveLocationUpdate();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            this.mContext.registerReceiver(this.mScreenOnReceiver, intentFilter);
            this.hasRegTzLocUpdater = true;
        }
    }

    private void unregisterTimeZoneLocationUpdater() {
        if (this.hasRegTzLocUpdater) {
            log("unregisterTimeZoneLocationUpdater!");
            this.mCurrentLocation = null;
            this.mHwLocationUpdateManager.unregisterPassiveLocationUpdate();
            this.mContext.unregisterReceiver(this.mScreenOnReceiver);
            this.hasRegTzLocUpdater = false;
            this.hasUpdateTzByLoc = false;
        }
    }

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
        return Global.getInt(this.mCr, "auto_time_zone", 0) != 0;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
