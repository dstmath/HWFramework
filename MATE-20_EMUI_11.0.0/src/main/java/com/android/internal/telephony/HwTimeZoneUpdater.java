package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import android.text.TextUtils;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonytimezone.BuildConfig;
import com.huawei.internal.telephony.MccTableExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;
import com.huawei.libcore.timezone.TimeZoneFinderEx;
import java.util.List;
import java.util.TimeZone;

public class HwTimeZoneUpdater {
    private static final int EVENT_AUTO_TIME_ZONE_CHANGED = 1;
    private static final int EVENT_MCC_CHANGED = 2;
    private static final boolean HW_DEBUGGABLE;
    private static final int MCC_LEN = 3;
    private static final int PHONE_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final int SINGLE_TIME_ZONE = 1;
    private static final String TAG = "HwTimeZoneUpdater";
    private static final boolean USE_LOCATION_TIME_ZONE = SystemPropertiesEx.getBoolean("ro.config.location_time_zone", true);
    private Context mContext;
    private Handler mHandler;
    private HwLocationBasedTimeZoneUpdater mHwLocTzUpdater;
    private ServiceStateTrackerEx mServiceStateTracker;
    private SettingsObserver mSettingsObserver;

    static {
        boolean z = false;
        if (SystemPropertiesEx.getInt("ro.debuggable", (int) PHONE_NUM) == 1) {
            z = true;
        }
        HW_DEBUGGABLE = z;
    }

    public HwTimeZoneUpdater(Context context) {
        this.mContext = context;
        init();
        if (USE_LOCATION_TIME_ZONE) {
            this.mHwLocTzUpdater = HwLocationBasedTimeZoneUpdater.init(context);
            this.mHwLocTzUpdater.start();
        }
        HwDualCardsLocationTimeZoneUpdate.init(context);
        HwDualCardsTimeUpdate.init(context);
    }

    private void init() {
        log("init...");
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        this.mSettingsObserver = new SettingsObserver(this.mHandler, 1);
        this.mSettingsObserver.observe(this.mContext);
        for (int i = PHONE_NUM; i < PHONE_NUM; i++) {
            PhoneExt phone = PhoneFactoryExt.getPhone(i);
            if (phone != null) {
                phone.registerForMccChanged(this.mHandler, (int) EVENT_MCC_CHANGED, Integer.valueOf(i));
            }
        }
        PhoneExt phone2 = PhoneFactoryExt.getPhone((int) PHONE_NUM);
        this.mServiceStateTracker = phone2 != null ? phone2.getServiceStateTracker() : null;
    }

    private void setTimeZoneByRplmn(String zoneId) {
        if (HW_DEBUGGABLE) {
            log("setTimeZoneByRplmn: zoneId=" + zoneId);
        }
        ((AlarmManager) this.mContext.getSystemService("alarm")).setTimeZone(zoneId);
        HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").getHwReportManager().reportSetTimeZoneByIso(PhoneFactoryExt.getPhone((int) PHONE_NUM), zoneId, false, "RplmnIso");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAutomaticTimeZoneRequested() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "auto_time_zone", PHONE_NUM) != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onMccChanged(AsyncResultEx ar) {
        String mcc = (String) ar.getResult();
        log("[SLOT" + ((Integer) ar.getUserObj()) + "] rplmn mcc changed, mcc" + mcc);
        if (!checkAllCardsNotRegInService()) {
            log("onMccChanged, any card is in service, return.");
        } else if (!isAutomaticTimeZoneRequested()) {
            log("onMccChanged, auto timezone disabled.");
        } else {
            String currentZoneId = TimeZone.getDefault().getID();
            String zoneId = getTimeZoneId(mcc);
            if (HW_DEBUGGABLE) {
                log("onMccChanged, zoneId:" + zoneId + ", currentZoneId:" + currentZoneId);
            }
            if (!TextUtils.isEmpty(zoneId) && !zoneId.equals(currentZoneId)) {
                setTimeZoneByRplmn(zoneId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAutoTimeZoneForRplmn() {
        if (!checkAllCardsNotRegInService()) {
            log("checkAutoTimeZoneForRplmn, any card is in service, return.");
            return;
        }
        String rplmn = BuildConfig.FLAVOR;
        String mcc = BuildConfig.FLAVOR;
        int i = PHONE_NUM;
        while (true) {
            if (i >= PHONE_NUM) {
                break;
            }
            PhoneExt phone = PhoneFactoryExt.getPhone(i);
            ServiceStateTrackerEx sst = phone != null ? phone.getServiceStateTracker() : null;
            rplmn = sst != null ? sst.getRplmn() : BuildConfig.FLAVOR;
            if (!TextUtils.isEmpty(rplmn)) {
                log("checkAutoTimeZoneForRplmn, got valid rplmn: " + rplmn);
                break;
            }
            i++;
        }
        if (!TextUtils.isEmpty(rplmn) && rplmn.length() > MCC_LEN) {
            mcc = rplmn.substring(PHONE_NUM, MCC_LEN);
        }
        String currentZoneId = TimeZone.getDefault().getID();
        String zoneId = getTimeZoneId(mcc);
        if (HW_DEBUGGABLE) {
            log("checkAutoTimeZoneForRplmn, zoneId:" + zoneId + ", currentZoneId:" + currentZoneId);
        }
        if (!TextUtils.isEmpty(zoneId) && !zoneId.equals(currentZoneId)) {
            setTimeZoneByRplmn(zoneId);
        }
    }

    private boolean checkAllCardsNotRegInService() {
        if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isVSimEnabled() || !isRegInService(HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyVSimFactoryImpl").createHwInnerVSimManager().getVSimPhone())) {
            for (int i = PHONE_NUM; i < PHONE_NUM; i++) {
                if (isRegInService(PhoneFactoryExt.getPhone(i))) {
                    log("checkAllCardsNotRegInService, [SLOT" + i + "] is in service");
                    return false;
                }
            }
            return true;
        }
        log("checkAllCardsNotRegInService, vsim is in service");
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

    private String getTimeZoneId(String mcc) {
        if (TextUtils.isEmpty(mcc)) {
            return BuildConfig.FLAVOR;
        }
        ServiceStateTrackerEx serviceStateTrackerEx = this.mServiceStateTracker;
        String zoneId = serviceStateTrackerEx != null ? serviceStateTrackerEx.getTimeZoneFromMcc(mcc) : null;
        if (zoneId != null) {
            return zoneId;
        }
        String countryIso = BuildConfig.FLAVOR;
        try {
            countryIso = MccTableExt.countryCodeForMcc(Integer.parseInt(mcc));
        } catch (NumberFormatException e) {
            loge("countryCodeForMcc NumberFormatException");
        }
        if (TextUtils.isEmpty(countryIso)) {
            return BuildConfig.FLAVOR;
        }
        List<android.icu.util.TimeZone> timeZones = TimeZoneFinderEx.lookupTimeZonesByCountry(countryIso);
        if (timeZones == null || timeZones.size() != 1) {
            return HwLocationBasedTimeZoneUpdater.getInstance().getDefaultTzForSigleTzCountry(countryIso);
        }
        return timeZones.get(PHONE_NUM).getID();
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        RlogEx.i(TAG, s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        RlogEx.e(TAG, s);
    }

    /* access modifiers changed from: private */
    public static class SettingsObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        SettingsObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        /* access modifiers changed from: package-private */
        public void observe(Context context) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("auto_time_zone"), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwTimeZoneUpdater.this.log("EVENT_AUTO_TIME_ZONE_CHANGED");
                if (HwTimeZoneUpdater.this.isAutomaticTimeZoneRequested()) {
                    HwTimeZoneUpdater.this.checkAutoTimeZoneForRplmn();
                }
            } else if (i != HwTimeZoneUpdater.EVENT_MCC_CHANGED) {
                HwTimeZoneUpdater.this.log("unknown message, ignore");
            } else {
                HwTimeZoneUpdater.this.log("EVENT_MCC_CHANGED");
                HwTimeZoneUpdater.this.onMccChanged(AsyncResultEx.from(msg.obj));
            }
        }
    }
}
