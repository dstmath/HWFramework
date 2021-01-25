package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.IMonitorExt;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import java.util.TimeZone;

public class HwReportManagerImpl extends DefaultHwReportManager {
    private static final int AUTO_TIME_ZONE_OFF = 0;
    private static final int EVENT_INVALID_NITZ_TIME = 907047008;
    private static final int EVENT_MULTI_TZ_REG = 907047005;
    private static final int EVENT_NETWORK_INFO = 907047001;
    private static final int EVENT_NITZ_IGNORE = 907047004;
    private static final int EVENT_NULTI_TZ_NO_NITZ = 907047006;
    private static final int EVENT_SET_TIME_ZONE_BY_ISO = 907047003;
    private static final int EVENT_SET_TIME_ZONE_BY_NITZ = 907047002;
    private static final int EVENT_SET_TIMZ_ZONE_BY_LOCATION = 907047007;
    private static final int INVAILUE_TZ_OFFSET = -1;
    private static final String LOG_TAG = "HwReportManagerImpl";
    private static final int MAX_NITZ_REPORT_TIME = 5;
    private static final int MAX_REPORT_TIME = 5;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final long RESET_REPORT_TIMES_INTERVAL = 86400000;
    private static HwReportManager mInstance;
    private long mInitTime = SystemClock.elapsedRealtime();
    private String mMccMnc = BuildConfig.FLAVOR;
    private String mMccMncOtherSlot = BuildConfig.FLAVOR;
    private int mNitzReportTimes;
    private String mPreForidden = BuildConfig.FLAVOR;
    private long mPreOffset;
    private String mPreZoneId = BuildConfig.FLAVOR;
    private int mReportTimes;
    private String mSimOperator = BuildConfig.FLAVOR;
    private String mSimOperatorOtherSlot = BuildConfig.FLAVOR;

    private HwReportManagerImpl() {
    }

    public static synchronized HwReportManager getDefault() {
        HwReportManager hwReportManager;
        synchronized (HwReportManagerImpl.class) {
            if (mInstance == null) {
                mInstance = new HwReportManagerImpl();
            }
            mInstance.initValue();
            hwReportManager = mInstance;
        }
        return hwReportManager;
    }

    public static HwReportManagerImpl getDefaultImpl() {
        return getDefault();
    }

    private void initValue() {
        log("initValue!");
        if (SystemClock.elapsedRealtime() - this.mInitTime >= RESET_REPORT_TIMES_INTERVAL) {
            this.mInitTime = SystemClock.elapsedRealtime();
            this.mReportTimes = 0;
            this.mNitzReportTimes = 0;
        }
    }

    private void getRegisteredInfo() {
        int subIdOfSlot1 = SubscriptionManagerEx.getSubIdUsingSlotId(0);
        int subIdOfSlot2 = SubscriptionManagerEx.getSubIdUsingSlotId(1);
        this.mMccMnc = TelephonyManagerEx.getNetworkOperator(TelephonyManagerEx.getDefault(), subIdOfSlot1);
        this.mSimOperator = TelephonyManagerEx.getSimOperatorNumericForPhone(0);
        this.mMccMncOtherSlot = TelephonyManagerEx.getNetworkOperator(TelephonyManagerEx.getDefault(), subIdOfSlot2);
        this.mSimOperatorOtherSlot = TelephonyManagerEx.getSimOperatorNumericForPhone(1);
    }

    private void setRegisteredInfoParam(IMonitorExt.EventStreamExt eventStream) {
        synchronized (eventStream) {
            eventStream.setParam(eventStream, 0, this.mMccMnc);
            eventStream.setParam(eventStream, 1, this.mSimOperator);
            eventStream.setParam(eventStream, 2, this.mMccMncOtherSlot);
            eventStream.setParam(eventStream, 3, this.mSimOperatorOtherSlot);
        }
    }

    public void reportSetTimeZoneByNitz(PhoneExt phone, String zoneId, int tzOffset, String source) {
        log("reportSetTimeZoneByNitz tzOffset:" + tzOffset);
        if (phone != null) {
            int subId = phone.getPhoneId();
            getRegisteredInfo();
            if (tzOffset != -1) {
                if (((long) tzOffset) == this.mPreOffset || this.mReportTimes > 5) {
                    log("tzOffset is not chaneged or report time is " + this.mReportTimes);
                    return;
                }
                this.mPreOffset = (long) tzOffset;
            } else if ((zoneId == null || !zoneId.equals(this.mPreZoneId)) && this.mReportTimes <= 5) {
                this.mPreZoneId = zoneId;
            } else {
                log("zoneId is not chaneged.");
                return;
            }
            IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
            if (regInfoStream != null) {
                setRegisteredInfoParam(regInfoStream);
            }
            IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_SET_TIME_ZONE_BY_NITZ);
            if (eventStream != null) {
                eventStream.setParam(eventStream, 0, regInfoStream);
                eventStream.setParam(eventStream, 1, zoneId);
                eventStream.setParam(eventStream, 2, getAutoTimeZone());
                eventStream.setParam(eventStream, 3, subId);
                eventStream.setParam(eventStream, 4, source);
                IMonitorExt.sendEvent(eventStream);
                this.mReportTimes++;
                log("reportSetTimeZoneByNitz Send infomation this report time is :" + this.mReportTimes);
            }
            IMonitorExt.closeEventStream(regInfoStream);
            IMonitorExt.closeEventStream(eventStream);
        }
    }

    public void reportSetTimeZoneByIso(PhoneExt phone, String zoneId, boolean isNitzUpdatedTime, String source) {
        log("reportSetTimeZoneByIso nitzUpdatedTime:" + isNitzUpdatedTime + " source: " + source);
        getRegisteredInfo();
        if (!isAllowedReport(zoneId) || this.mReportTimes > 5) {
            log("zoneId is not chaneged or report time is " + this.mReportTimes);
            return;
        }
        IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_SET_TIME_ZONE_BY_ISO);
        if (eventStream != null) {
            eventStream.setParam(eventStream, 0, regInfoStream);
            eventStream.setParam(eventStream, 1, isNitzUpdatedTime);
            eventStream.setParam(eventStream, 2, getAutoTimeZone());
            eventStream.setParam(eventStream, 3, SystemPropertiesEx.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, BuildConfig.FLAVOR));
            eventStream.setParam(eventStream, 4, source);
            IMonitorExt.sendEvent(eventStream);
            this.mReportTimes++;
            log("reportSetTimeZoneByIso Send infomation this report time is :" + this.mReportTimes);
        }
        IMonitorExt.closeEventStream(regInfoStream);
        IMonitorExt.closeEventStream(eventStream);
    }

    public void reportNitzIgnore(int phoneId, String forbidden) {
        log("reportNitzIgnore phoneId : " + phoneId + "forbidden:" + forbidden);
        getRegisteredInfo();
        if (this.mPreForidden.equals(forbidden) || this.mReportTimes > 5) {
            log("forbidden condition is not chaneged or report time is " + this.mReportTimes);
            return;
        }
        this.mPreForidden = forbidden;
        IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_NITZ_IGNORE);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(eventStream, 0, regInfoStream);
            eventStream.setParam(eventStream, 1, phoneId);
            if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimEnabled()) {
                z = true;
            }
            eventStream.setParam(eventStream, 2, z);
            eventStream.setParam(eventStream, 4, forbidden);
            IMonitorExt.sendEvent(eventStream);
            this.mReportTimes++;
            log("Send infomation this report time is :" + this.mReportTimes);
        }
        IMonitorExt.closeEventStream(regInfoStream);
        IMonitorExt.closeEventStream(eventStream);
    }

    public void reportMultiTZRegistered() {
        log("reportMultiTZRegistered");
        getRegisteredInfo();
        IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_MULTI_TZ_REG);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(eventStream, 0, regInfoStream);
            if (getLocationMode() != 0) {
                z = true;
            }
            eventStream.setParam(eventStream, 1, z);
            eventStream.setParam(eventStream, 2, getLocationMode());
            eventStream.setParam(eventStream, 3, getAutoTimeZone());
            eventStream.setParam(eventStream, 4, isNetworkAvailable());
            IMonitorExt.sendEvent(eventStream);
        }
        IMonitorExt.closeEventStream(regInfoStream);
        IMonitorExt.closeEventStream(eventStream);
    }

    public void reportMultiTZNoNitz() {
        log("reportMultiTZNoNitz");
        getRegisteredInfo();
        IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_NULTI_TZ_NO_NITZ);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(eventStream, 0, regInfoStream);
            eventStream.setParam(eventStream, 1, TimeZone.getDefault().getID());
            if (getLocationMode() != 0) {
                z = true;
            }
            eventStream.setParam(eventStream, 2, z);
            eventStream.setParam(eventStream, 3, getLocationMode());
            eventStream.setParam(eventStream, 4, getAutoTimeZone());
            eventStream.setParam(eventStream, 5, isNetworkAvailable());
            IMonitorExt.sendEvent(eventStream);
        }
        IMonitorExt.closeEventStream(regInfoStream);
        IMonitorExt.closeEventStream(eventStream);
    }

    public void reportSetTimeZoneByLocation(String zoneId) {
        log("reportSetTimeZoneByLocation");
        getRegisteredInfo();
        if (!isAllowedReport(zoneId) || this.mReportTimes > 5) {
            log("zoneId is not chaneged or report time is " + this.mReportTimes);
            return;
        }
        IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_SET_TIMZ_ZONE_BY_LOCATION);
        if (eventStream != null) {
            boolean z = false;
            eventStream.setParam(eventStream, 0, regInfoStream);
            eventStream.setParam(eventStream, 1, zoneId);
            if (getLocationMode() != 0) {
                z = true;
            }
            eventStream.setParam(eventStream, 2, z);
            eventStream.setParam(eventStream, 3, getLocationMode());
            eventStream.setParam(eventStream, 4, getAutoTimeZone());
            eventStream.setParam(eventStream, 5, isNetworkAvailable());
            IMonitorExt.sendEvent(eventStream);
            this.mReportTimes++;
            log("Send infomation this report time is :" + this.mReportTimes);
        }
        IMonitorExt.closeEventStream(regInfoStream);
        IMonitorExt.closeEventStream(eventStream);
    }

    public void reportInvalidNitzTime(int phoneId, String nitzTime, int reason, String trustedTime) {
        log("reportInvalidNitzTime phoneId:" + phoneId + " nitzTime: " + nitzTime + " reason: " + reason + " trusted time: " + trustedTime);
        if (this.mNitzReportTimes >= 5) {
            log("already reach the max NITZ report times!");
            return;
        }
        IMonitorExt.EventStreamExt regInfoStream = IMonitorExt.openEventStream((int) EVENT_NETWORK_INFO);
        if (regInfoStream != null) {
            setRegisteredInfoParam(regInfoStream);
        }
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) EVENT_INVALID_NITZ_TIME);
        if (eventStream != null) {
            eventStream.setParam(eventStream, 0, regInfoStream);
            eventStream.setParam(eventStream, 1, phoneId);
            eventStream.setParam(eventStream, 2, nitzTime);
            eventStream.setParam(eventStream, 3, reason);
            eventStream.setParam(eventStream, 4, trustedTime);
            IMonitorExt.sendEvent(eventStream);
            this.mNitzReportTimes++;
            log("Send Invalid NITZ infomation this report time is :" + this.mNitzReportTimes);
        }
        IMonitorExt.closeEventStream(regInfoStream);
        IMonitorExt.closeEventStream(eventStream);
    }

    private int getAutoTimeZone() {
        Context context = null;
        if (PhoneFactoryExt.getPhone(0) != null) {
            context = PhoneFactoryExt.getPhone(0).getContext();
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
        if (PhoneFactoryExt.getPhone(0) != null) {
            context = PhoneFactoryExt.getPhone(0).getContext();
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
        if (PhoneFactoryExt.getPhone(0) != null) {
            context = PhoneFactoryExt.getPhone(0).getContext();
        }
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo ni = cm == null ? null : cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
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
