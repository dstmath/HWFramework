package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.IMonitor;
import com.android.internal.telephony.vsim.HwVSimUtils;
import java.util.TimeZone;

public class HwReportManagerImpl implements HwReportManager {
    private static final int AUTO_TIME_ZONE_OFF = 0;
    private static final int EVENT_MULTI_TZ_REG = 907047005;
    private static final int EVENT_NETWORK_INFO = 907047001;
    private static final int EVENT_NITZ_IGNORE = 907047004;
    private static final int EVENT_NULTI_TZ_NO_NITZ = 907047006;
    private static final int EVENT_SET_TIME_ZONE_BY_ISO = 907047003;
    private static final int EVENT_SET_TIME_ZONE_BY_NITZ = 907047002;
    private static final int EVENT_SET_TIMZ_ZONE_BY_LOCATION = 907047007;
    private static final int INVAILUE_TZ_OFFSET = -1;
    private static final String LOG_TAG = "HwReportManagerImpl";
    private static final int MAX_REPORT_TIME = 5;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final long RESET_REPORT_TIMES_INTERVAL = 86400000;
    private static long mInitTime;
    private static HwReportManager mInstance = new HwReportManagerImpl();
    private static int mReportTimes;
    private String mMccMnc = "";
    private String mMccMncOtherSlot = "";
    private String mPreForidden = "";
    private long mPreOffset;
    private String mPreZoneId = "";
    private String mSimOperator = "";
    private String mSimOperatorOtherSlot = "";

    private HwReportManagerImpl() {
        mInitTime = SystemClock.elapsedRealtime();
    }

    public static HwReportManager getDefault() {
        if (SystemClock.elapsedRealtime() - mInitTime >= RESET_REPORT_TIMES_INTERVAL) {
            mReportTimes = 0;
        }
        return mInstance;
    }

    private void getRegisteredInfo() {
        this.mMccMnc = TelephonyManager.getDefault().getNetworkOperator(0);
        this.mSimOperator = TelephonyManager.getDefault().getSimOperator(0);
        this.mMccMncOtherSlot = TelephonyManager.getDefault().getNetworkOperator(1);
        this.mSimOperatorOtherSlot = TelephonyManager.getDefault().getSimOperator(1);
    }

    private void setRegisteredInfoParam(IMonitor.EventStream eventStream) {
        synchronized (eventStream) {
            eventStream.setParam(0, this.mMccMnc);
            eventStream.setParam(1, this.mSimOperator);
            eventStream.setParam(2, this.mMccMncOtherSlot);
            eventStream.setParam(3, this.mSimOperatorOtherSlot);
        }
    }

    public void reportSetTimeZoneByNitz(Phone phone, String zoneId, int tzOffset, String source) {
        log("reportSetTimeZoneByNitz  tzOffset:" + tzOffset);
        int subId = phone.getPhoneId();
        getRegisteredInfo();
        if (tzOffset != -1) {
            if (((long) tzOffset) == this.mPreOffset || mReportTimes > 5) {
                log("tzOffset is not chaneged or report time is " + mReportTimes);
                return;
            }
            this.mPreOffset = (long) tzOffset;
        } else if ((zoneId == null || !zoneId.equals(this.mPreZoneId)) && mReportTimes <= 5) {
            this.mPreZoneId = zoneId;
        } else {
            log("zoneId is not chaneged.");
            return;
        }
        IMonitor.EventStream regInfoStream = IMonitor.openEventStream(EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(EVENT_SET_TIME_ZONE_BY_NITZ);
        if (eventStream != null) {
            eventStream.setParam(0, regInfoStream);
            eventStream.setParam(1, zoneId);
            eventStream.setParam(2, getAutoTimeZone());
            eventStream.setParam(3, subId);
            eventStream.setParam(4, source);
            IMonitor.sendEvent(eventStream);
            mReportTimes++;
            log("Send infomation this report time is :" + mReportTimes);
        }
        IMonitor.closeEventStream(regInfoStream);
        IMonitor.closeEventStream(eventStream);
    }

    public void reportSetTimeZoneByIso(Phone phone, String zoneId, boolean mNitzUpdatedTime, String source) {
        log("reportSetTimeZoneByIso  nitzUpdatedTime:" + mNitzUpdatedTime + " source: " + source);
        getRegisteredInfo();
        if (!isAllowedReport(zoneId) || mReportTimes > 5) {
            log("zoneId is not chaneged or report time is " + mReportTimes);
            return;
        }
        IMonitor.EventStream regInfoStream = IMonitor.openEventStream(EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(EVENT_SET_TIME_ZONE_BY_ISO);
        if (eventStream != null) {
            eventStream.setParam(0, regInfoStream);
            eventStream.setParam(1, Boolean.valueOf(mNitzUpdatedTime));
            eventStream.setParam(2, getAutoTimeZone());
            eventStream.setParam(3, SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, ""));
            eventStream.setParam(4, source);
            IMonitor.sendEvent(eventStream);
            mReportTimes++;
            log("Send infomation this report time is :" + mReportTimes);
        }
        IMonitor.closeEventStream(regInfoStream);
        IMonitor.closeEventStream(eventStream);
    }

    public void reportNitzIgnore(int phoneId, String forbidden) {
        log("reportNitzIgnore  phoneId : " + phoneId + "forbidden:" + forbidden);
        getRegisteredInfo();
        if (this.mPreForidden.equals(forbidden) || mReportTimes > 5) {
            log("forbidden condition is not chaneged or report time is " + mReportTimes);
            return;
        }
        this.mPreForidden = forbidden;
        IMonitor.EventStream regInfoStream = IMonitor.openEventStream(EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(EVENT_NITZ_IGNORE);
        if (eventStream != null) {
            eventStream.setParam(0, regInfoStream);
            eventStream.setParam(1, phoneId);
            eventStream.setParam(2, Boolean.valueOf(HwVSimUtils.isVSimEnabled()));
            eventStream.setParam(4, forbidden);
            IMonitor.sendEvent(eventStream);
            mReportTimes++;
            log("Send infomation this report time is :" + mReportTimes);
        }
        IMonitor.closeEventStream(regInfoStream);
        IMonitor.closeEventStream(eventStream);
    }

    public void reportMultiTZRegistered() {
        log("reportMultiTZRegistered");
        getRegisteredInfo();
        IMonitor.EventStream regInfoStream = IMonitor.openEventStream(EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(EVENT_MULTI_TZ_REG);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(0, regInfoStream);
            if (getLocationMode() != 0) {
                z = true;
            }
            eventStream.setParam(1, Boolean.valueOf(z));
            eventStream.setParam(2, getLocationMode());
            eventStream.setParam(3, getAutoTimeZone());
            eventStream.setParam(4, Boolean.valueOf(isNetworkAvailable()));
            IMonitor.sendEvent(eventStream);
        }
        IMonitor.closeEventStream(regInfoStream);
        IMonitor.closeEventStream(eventStream);
    }

    public void reportMultiTZNoNitz() {
        log("reportMultiTZNoNitz");
        getRegisteredInfo();
        IMonitor.EventStream regInfoStream = IMonitor.openEventStream(EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(EVENT_NULTI_TZ_NO_NITZ);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(0, regInfoStream);
            eventStream.setParam(1, TimeZone.getDefault().getID());
            if (getLocationMode() != 0) {
                z = true;
            }
            eventStream.setParam(2, Boolean.valueOf(z));
            eventStream.setParam(3, getLocationMode());
            eventStream.setParam(4, getAutoTimeZone());
            eventStream.setParam(5, Boolean.valueOf(isNetworkAvailable()));
            IMonitor.sendEvent(eventStream);
        }
        IMonitor.closeEventStream(regInfoStream);
        IMonitor.closeEventStream(eventStream);
    }

    public void reportSetTimeZoneByLocation(String zoneId) {
        log("reportSetTimeZoneByLocation");
        getRegisteredInfo();
        if (!isAllowedReport(zoneId) || mReportTimes > 5) {
            log("zoneId is not chaneged or report time is " + mReportTimes);
            return;
        }
        IMonitor.EventStream regInfoStream = IMonitor.openEventStream(EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(EVENT_SET_TIMZ_ZONE_BY_LOCATION);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(0, regInfoStream);
            eventStream.setParam(1, zoneId);
            if (getLocationMode() != 0) {
                z = true;
            }
            eventStream.setParam(2, Boolean.valueOf(z));
            eventStream.setParam(3, getLocationMode());
            eventStream.setParam(4, getAutoTimeZone());
            eventStream.setParam(5, Boolean.valueOf(isNetworkAvailable()));
            IMonitor.sendEvent(eventStream);
            mReportTimes++;
            log("Send infomation this report time is :" + mReportTimes);
        }
        IMonitor.closeEventStream(regInfoStream);
        IMonitor.closeEventStream(eventStream);
    }

    private int getAutoTimeZone() {
        Context context = null;
        if (PhoneFactory.getDefaultPhone() != null) {
            context = PhoneFactory.getDefaultPhone().getContext();
        }
        if (context != null) {
            ContentResolver cr = context.getContentResolver();
            if (cr != null) {
                return Settings.Global.getInt(cr, "auto_time_zone", 0);
            }
            log(" cr is null.");
            return 0;
        }
        log(" context is null, can not get cr.");
        return 0;
    }

    private boolean isAllowedReport(String zoneId) {
        String str = TimeZone.getDefault().getID();
        return str == null || !str.equalsIgnoreCase(zoneId);
    }

    private int getLocationMode() {
        Context context = null;
        if (PhoneFactory.getDefaultPhone() != null) {
            context = PhoneFactory.getDefaultPhone().getContext();
        }
        if (context != null) {
            ContentResolver cr = context.getContentResolver();
            if (cr != null) {
                return Settings.Secure.getInt(cr, "location_mode", 0);
            }
            log(" cr is null.");
            return 0;
        }
        log(" context is null, can not get cr.");
        return 0;
    }

    private boolean isNetworkAvailable() {
        Context context = null;
        if (PhoneFactory.getDefaultPhone() != null) {
            context = PhoneFactory.getDefaultPhone().getContext();
        }
        boolean z = false;
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo ni = cm == null ? null : cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
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
