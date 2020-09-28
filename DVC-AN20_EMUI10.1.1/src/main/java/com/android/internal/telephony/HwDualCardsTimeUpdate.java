package com.android.internal.telephony;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NtpTrustedTimeEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HwDualCardsTimeUpdate {
    private static final int AUTO_TIME_OFF = 0;
    private static final int COMPARE_ANOTHER_NITZ_TIME = 2;
    private static final int COMPARE_NTP_TIME = 0;
    private static final long DELAY_OBTAIN_NTP_TIME = 1800000;
    private static final int EVENT_DELAY_OBTAIN_NTP_TIME = 1;
    private static final int INVALID_NITZ_SAVE_TIME = 0;
    private static final String LOG_TAG = "HwDualCardsTimeUpdate";
    private static final int NITZ_CARD_ABSENT = 1;
    private static final int ONE_CARD_PRESENT = 1;
    private static final int PHONE_NUM = TelephonyManagerEx.getDefault().getPhoneCount();
    private static final int SINGLE_CARD_PHONE = 1;
    private static final int TIME_UPDATE_THRESHOLD = 5000;
    private static HwDualCardsTimeUpdate mInstance;
    private ConnectivityManager mCM;
    private Context mContext;
    private MyHandler mHandler;
    private boolean mHasNtpTime = false;
    private boolean mNetowrkConnected = false;
    private NetworkStateUpdateCallback mNetworkStateUpdateCallback;
    private NtpTrustedTimeEx mNtpTime;
    private PowerManager.WakeLock mWakeLock = null;

    private HwDualCardsTimeUpdate(Context context) {
        this.mContext = context;
        this.mNtpTime = NtpTrustedTimeEx.getInstance(this.mContext);
        PowerManager pm = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        if (pm != null) {
            this.mWakeLock = pm.newWakeLock(1, LOG_TAG);
        }
        HandlerThread thread = new HandlerThread(LOG_TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        this.mNetworkStateUpdateCallback = new NetworkStateUpdateCallback();
        this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        ConnectivityManager connectivityManager = this.mCM;
        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(this.mNetworkStateUpdateCallback, this.mHandler);
        }
        log("HwDualCardsTimeUpdate construct");
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new HwDualCardsTimeUpdate(context);
        }
    }

    public static HwDualCardsTimeUpdate getDefault() {
        if (mInstance == null) {
            RlogEx.e(LOG_TAG, "mInstance null");
        }
        return mInstance;
    }

    private class NetworkStateUpdateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkStateUpdateCallback() {
        }

        public void onAvailable(Network network) {
            HwDualCardsTimeUpdate.this.log("network available.");
            HwDualCardsTimeUpdate.this.mNetowrkConnected = true;
            HwDualCardsTimeUpdate.this.updateCacheNtpTime();
        }

        public void onLost(Network network) {
            HwDualCardsTimeUpdate.this.log("network not available.");
            HwDualCardsTimeUpdate.this.mNetowrkConnected = false;
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                HwDualCardsTimeUpdate.this.log("unknown message, ignore");
                return;
            }
            HwDualCardsTimeUpdate.this.log("EVENT_DELAY_OBTAIN_NTP_TIME");
            HwDualCardsTimeUpdate.this.updateCacheNtpTime();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCacheNtpTime() {
        PowerManager.WakeLock wakeLock;
        log("mNetowrkConnected: " + this.mNetowrkConnected + "  hasCache: " + this.mNtpTime.hasCache());
        if (!this.mNetowrkConnected || this.mNtpTime.hasCache() || (wakeLock = this.mWakeLock) == null) {
            this.mHasNtpTime = this.mNtpTime.hasCache();
            return;
        }
        wakeLock.acquire();
        try {
            this.mHasNtpTime = this.mNtpTime.forceRefresh();
            this.mWakeLock.release();
            if (this.mHasNtpTime) {
                if (this.mHandler.hasMessages(1)) {
                    this.mHandler.removeMessages(1);
                }
                log("update system clock.");
                if (isAutomaticTimeRequested()) {
                    log("update system clock.");
                    updateSystemClock();
                }
            }
            if (!this.mHasNtpTime && !this.mHandler.hasMessages(1)) {
                log("delay obtain ntp time.");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), DELAY_OBTAIN_NTP_TIME);
            }
        } catch (Throwable th) {
            this.mWakeLock.release();
            throw th;
        }
    }

    public boolean allowUpdateTimeFromNitz(PhoneExt phone, long nitzTime) {
        if (phone == null) {
            return true;
        }
        log("allowUpdateTimeFromNitz. mHasNtpTime :" + this.mHasNtpTime + "  phoneId: " + phone.getPhoneId());
        if (this.mHasNtpTime) {
            log("Ntp time threshold:" + Math.abs(nitzTime - this.mNtpTime.currentTimeMillis()));
            if (Math.abs(nitzTime - this.mNtpTime.currentTimeMillis()) <= 5000) {
                return true;
            }
            HwReportManagerImpl.getDefaultImpl().reportInvalidNitzTime(phone.getPhoneId(), timeStamptoDate(nitzTime), 0, timeStamptoDate(this.mNtpTime.currentTimeMillis()));
            return false;
        } else if (PHONE_NUM == 1) {
            return true;
        } else {
            return dualCardNitzTimeStrategy(phone, nitzTime);
        }
    }

    private boolean dualCardNitzTimeStrategy(PhoneExt phone, long nitzTime) {
        if (getCardPresentNum() != 1) {
            long[] saveNitzTime = updateSavedNitzTime();
            int otherPhoneId = 0;
            if (phone.getPhoneId() == 0) {
                otherPhoneId = 1;
            } else if (phone.getPhoneId() == 1) {
                otherPhoneId = 0;
            }
            if (saveNitzTime[otherPhoneId] == 0) {
                log("only one card has nitz time!");
                return true;
            }
            long nitzTimeInterval = Math.abs(nitzTime - saveNitzTime[otherPhoneId]);
            log("nitzTimeInterval: " + nitzTimeInterval);
            if (nitzTimeInterval <= 5000) {
                return true;
            }
            HwReportManagerImpl.getDefaultImpl().reportInvalidNitzTime(phone.getPhoneId(), timeStamptoDate(nitzTime), 2, timeStamptoDate(saveNitzTime[otherPhoneId]));
            return false;
        } else if (isCardPresent(phone.getPhoneId())) {
            return true;
        } else {
            HwReportManagerImpl.getDefaultImpl().reportInvalidNitzTime(phone.getPhoneId(), timeStamptoDate(nitzTime), 1, null);
            return false;
        }
    }

    private boolean isCardPresent(int phoneId) {
        UiccControllerExt uiccController = UiccControllerExt.getInstance();
        if (uiccController == null || uiccController.getUiccCard(phoneId) == null || uiccController.getUiccCard(phoneId).getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
            return false;
        }
        return true;
    }

    private void updateSystemClock() {
        if (Math.abs(this.mNtpTime.currentTimeMillis() - System.currentTimeMillis()) <= 5000) {
            log("Ignoring NTP update due to low skew");
        } else {
            SystemClock.setCurrentTimeMillis(this.mNtpTime.currentTimeMillis());
        }
    }

    private long[] updateSavedNitzTime() {
        long[] saveNitzTime = new long[PHONE_NUM];
        for (int i = 0; i < PHONE_NUM; i++) {
            PhoneExt phone = PhoneFactoryExt.getPhone(i);
            if (phone != null) {
                long savedTime = phone.getServiceStateTracker().getSavedNitzTime();
                if (savedTime != 0) {
                    saveNitzTime[i] = savedTime;
                }
            }
        }
        return saveNitzTime;
    }

    private int getCardPresentNum() {
        int cardPresentNum = 0;
        for (int i = 0; i < PHONE_NUM; i++) {
            if (isCardPresent(i)) {
                log("isCardPresent: " + i);
                cardPresentNum++;
            }
        }
        log("cardPresentNum: " + cardPresentNum);
        return cardPresentNum;
    }

    private boolean isAutomaticTimeRequested() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "auto_time", 0) != 0;
    }

    private String timeStamptoDate(long millSeconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date = new Date();
        date.setTime(millSeconds);
        return simpleDateFormat.format(date);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        RlogEx.i(LOG_TAG, s);
    }
}
