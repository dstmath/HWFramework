package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import com.android.internal.telephony.HwSignalStrength;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccCardStatusUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;

public abstract class HwBaseSignalStrengthManager extends Handler {
    protected static final int CDMA_LEVEL = 8;
    protected static final int DELAYED_ECC_TO_NOSERVICE_VALUE = SystemPropertiesEx.getInt("ro.ecc_to_noservice.timer", 0);
    protected static final int DELAYED_TIME_DEFAULT_VALUE = 20;
    protected static final int DELAY_DURING_TIME = SystemPropertiesEx.getInt("ro.signalsmooth.delaytimer", (int) VALUE_DELAY_DURING_TIME);
    protected static final int EVDO_LEVEL = 16;
    protected static final int EVENT_BASE = 1000;
    protected static final int EVENT_DELAY_UPDATE_REGISTER_STATE_DONE = 1001;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 1002;
    protected static final int GSM_LEVEL = 1;
    protected static final int INVALID = -1;
    protected static final boolean IS_FEATURE_SIGNAL_DUALPARAM = SystemProperties.getBoolean("signal.dualparam", false);
    private static final String LOG_TAG = "HwBaseSignalStrengthManager";
    protected static final int LTE_LEVEL = 4;
    protected static final int LTE_RSSNR_POOR_STD = SystemPropertiesEx.getInt("ro.lte.rssnrpoorstd", -5);
    protected static final int LTE_RSSNR_UNKOUWN_STD = 99;
    protected static final int LTE_STRENGTH_POOR_STD = SystemPropertiesEx.getInt("ro.lte.poorstd", -125);
    protected static final int LTE_STRENGTH_UNKOUWN_STD = -44;
    protected static final int L_RSSNR_POOR_STD = -5;
    protected static final int L_STRENGTH_POOR_STD = -125;
    protected static final int MAX_SIGNAL_VALUE = -1;
    protected static final int NR_LEVEL = 32;
    protected static final int NR_RSSNR_POOR_STD = SystemPropertiesEx.getInt("ro.nr.rssnrpoorstd", -5);
    protected static final int NR_RSSNR_UNKOUWN_STD = 99;
    protected static final int NR_STRENGTH_POOR_STD = SystemPropertiesEx.getInt("ro.nr.poorstd", -125);
    protected static final int NR_STRENGTH_UNKOUWN_STD = -44;
    protected static final String NR_TECHNOLOGY_CONFIGA = "ConfigA";
    protected static final String NR_TECHNOLOGY_CONFIGAD = "ConfigAD";
    protected static final String NR_TECHNOLOGY_CONFIGB = "ConfigB";
    protected static final String NR_TECHNOLOGY_CONFIGC = "ConfigC";
    protected static final String NR_TECHNOLOGY_CONFIGD = "ConfigD";
    protected static final int NSA_INVALID_STATE = 0;
    protected static final int NSA_STATE1 = 1;
    protected static final int NSA_STATE2 = 2;
    protected static final int NSA_STATE3 = 3;
    protected static final int NSA_STATE4 = 4;
    protected static final int NSA_STATE5 = 5;
    protected static final int N_RSSNR_POOR_STD = -5;
    protected static final int N_STRENGTH_POOR_STD = -125;
    protected static final int ONE_SEC_DURING_VALUE = 1000;
    protected static final double SIGNAL_INTERVAL = -1.0d;
    protected static final int TIME_NOT_SET = 0;
    protected static final int UMTS_LEVEL = 2;
    protected static final int VALUE_DELAY_DURING_TIME = 6000;
    protected static final double VALUE_NEW_COEF_QUA_DES_SS = 0.15d;
    protected static final int VALUE_NEW_COEF_STR_DES_SS = 5;
    protected static final double VALUE_OLD_COEF_QUA_DES_SS = 0.85d;
    protected static final int VALUE_OLD_COEF_STR_DES_SS = 7;
    protected int delayedTimeDefaultValue = SystemPropertiesEx.getInt("ro.lostnetwork.default_timer", (int) DELAYED_TIME_DEFAULT_VALUE);
    protected int delayedTimeNetworkstatusCs2G = (SystemPropertiesEx.getInt("ro.lostnetwork.delaytimer_cs2G", this.delayedTimeDefaultValue) * 1000);
    protected int delayedTimeNetworkstatusCs3G = (SystemPropertiesEx.getInt("ro.lostnetwork.delaytimer_cs3G", this.delayedTimeDefaultValue) * 1000);
    protected int delayedTimeNetworkstatusCs4G = (SystemPropertiesEx.getInt("ro.lostnetwork.delaytimer_cs4G", this.delayedTimeDefaultValue) * 1000);
    protected int delayedTimeNetworkstatusPs2G = (SystemPropertiesEx.getInt("ro.lostnetwork.delaytimer_ps2G", this.delayedTimeDefaultValue) * 1000);
    protected int delayedTimeNetworkstatusPs3G = (SystemPropertiesEx.getInt("ro.lostnetwork.delaytimer_ps3G", this.delayedTimeDefaultValue) * 1000);
    protected int delayedTimeNetworkstatusPs4G = (SystemPropertiesEx.getInt("ro.lostnetwork.delaytimer_ps4G", this.delayedTimeDefaultValue) * 1000);
    protected Context mContext;
    protected ContentResolver mCr;
    protected HwServiceStateTrackerEx mHwServiceStateTrackerEx;
    protected boolean mIsRefreshState = false;
    protected boolean mIsRefreshStateEcc = false;
    protected int mMainSlot;
    protected int mMainSlotEcc;
    protected PhoneExt mPhone;
    protected int mPhoneId;
    private PreferNetworkDb mPreferNetworkDb = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwBaseSignalStrengthManager.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.huawei.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                if (HwBaseSignalStrengthManager.this.hasMessages(HwBaseSignalStrengthManager.EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
                    HwBaseSignalStrengthManager.this.removeMessages(HwBaseSignalStrengthManager.EVENT_DELAY_UPDATE_REGISTER_STATE_DONE);
                }
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    int phoneKey = bundle.getInt("slot");
                    int phoneId = HwBaseSignalStrengthManager.this.mPhone.getPhoneId();
                    int state = bundle.getInt("state");
                    HwBaseSignalStrengthManager hwBaseSignalStrengthManager = HwBaseSignalStrengthManager.this;
                    hwBaseSignalStrengthManager.logd("onReceive action phoneKey = " + phoneKey + ",state = " + state);
                    if (1 == state && phoneKey == phoneId) {
                        HwBaseSignalStrengthManager hwBaseSignalStrengthManager2 = HwBaseSignalStrengthManager.this;
                        hwBaseSignalStrengthManager2.logd("HW_ACTION_CARRIER_CONFIG_CHANGED phoneId: " + phoneId);
                        HwBaseSignalStrengthManager.this.mPhone.getCi().getSignalStrength(HwBaseSignalStrengthManager.this.obtainMessage(HwBaseSignalStrengthManager.EVENT_GET_SIGNAL_STRENGTH));
                    }
                }
            } else if ("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action)) {
                HwBaseSignalStrengthManager hwBaseSignalStrengthManager3 = HwBaseSignalStrengthManager.this;
                StringBuilder sb = new StringBuilder();
                sb.append("[SLOT");
                sb.append(HwBaseSignalStrengthManager.this.mPhone.getPhoneId());
                sb.append("]CardState: ");
                sb.append(intent.getIntExtra("newSubState", -1));
                sb.append("IsMphone: ");
                sb.append(HwBaseSignalStrengthManager.this.mPhone.getPhoneId() == intent.getIntExtra("phone", 0));
                hwBaseSignalStrengthManager3.logd(sb.toString());
                if (intent.getIntExtra("operationResult", 1) == 0 && HwBaseSignalStrengthManager.this.mPhone.getPhoneId() == intent.getIntExtra("phone", 0) && HwBaseSignalStrengthManager.this.hasMessages(HwBaseSignalStrengthManager.EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) && intent.getIntExtra("newSubState", -1) == 0) {
                    HwBaseSignalStrengthManager.this.cancelDeregisterStateDelayTimer();
                    HwBaseSignalStrengthManager.this.mServiceStateTracker.pollState();
                }
            } else {
                HwBaseSignalStrengthManager hwBaseSignalStrengthManager4 = HwBaseSignalStrengthManager.this;
                hwBaseSignalStrengthManager4.logd("unknowns msg: " + action);
            }
        }
    };
    protected IServiceStateTrackerInner mServiceStateTracker;
    protected String mTag;

    /* access modifiers changed from: protected */
    public abstract boolean isNetworkTypeChanged(SignalStrength signalStrength, SignalStrength signalStrength2);

    protected HwBaseSignalStrengthManager(IServiceStateTrackerInner serviceStateTracker, PhoneExt phoneExt, HwServiceStateTrackerEx hwServiceStateTrackerEx) {
        this.mServiceStateTracker = serviceStateTracker;
        this.mPhone = phoneExt;
        this.mPhoneId = this.mPhone.getPhoneId();
        this.mTag = "HwBaseSignalStrengthManager[" + this.mPhoneId + "]";
        this.mHwServiceStateTrackerEx = hwServiceStateTrackerEx;
        this.mContext = this.mPhone.getContext();
        this.mCr = this.mContext.getContentResolver();
        this.mMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        this.mMainSlotEcc = this.mMainSlot;
        registerBroadcastReceiver();
        if (HwTelephonyManager.getDefault().isNrSupported()) {
            registerSetPreferDb();
        }
    }

    public void handleMessage(Message msg) {
        logd("handleMessage, msg.what = " + msg.what);
        int i = msg.what;
        if (i == EVENT_DELAY_UPDATE_REGISTER_STATE_DONE) {
            handleDelayUpdateRegisterStateDone();
        } else if (i == EVENT_GET_SIGNAL_STRENGTH) {
            this.mServiceStateTracker.onSignalStrengthResultHw(msg);
        }
        super.handleMessage(msg);
    }

    private void handleDelayUpdateRegisterStateDone() {
        logd("[Phone" + this.mPhoneId + "]Delay Timer expired, begin get register state");
        this.mIsRefreshState = true;
        this.mPhone.getCi().getSignalStrength(obtainMessage(EVENT_GET_SIGNAL_STRENGTH));
        this.mServiceStateTracker.pollState();
    }

    /* access modifiers changed from: protected */
    public boolean isCardInvalid(boolean isSubDeactivated, int slotId) {
        IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
        UiccCard newCard = UiccController.getInstance().getUiccCard(slotId);
        if (newCard != null) {
            newState = newCard.getCardState();
        }
        boolean isCardPresent = IccCardStatusUtils.isCardPresent(newState);
        logd("isCardPresent: " + isCardPresent + " slotId : " + slotId);
        return !isCardPresent || isSubDeactivated;
    }

    /* access modifiers changed from: protected */
    public int getNetworkType(ServiceState ss) {
        if (ss.getDataNetworkType() != 0) {
            return ss.getDataNetworkType();
        }
        return ss.getVoiceNetworkType();
    }

    /* access modifiers changed from: protected */
    public void cancelDeregisterStateDelayTimer() {
        if (hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            logd("cancelDeregisterStateDelayTimer");
            removeMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE);
            this.mPhone.getCi().getSignalStrength(obtainMessage(EVENT_GET_SIGNAL_STRENGTH));
        }
    }

    /* access modifiers changed from: protected */
    public void delaySendDeregisterStateChange(int delayedTime) {
        if (!hasMessages(EVENT_DELAY_UPDATE_REGISTER_STATE_DONE)) {
            Message msg = obtainMessage();
            msg.what = EVENT_DELAY_UPDATE_REGISTER_STATE_DONE;
            sendMessageDelayed(msg, (long) delayedTime);
            logd("RegisterStateChange timer is running,do nothing");
        }
    }

    /* access modifiers changed from: protected */
    public void signalStrengthResultHw(SignalStrength newSignalStrength) {
        this.mHwServiceStateTrackerEx.updateHwnff(this.mPhoneId, newSignalStrength);
    }

    /* access modifiers changed from: protected */
    public void clearNrSignalStrength(boolean isDelaySsUpdate, SignalStrength ss) {
        if (HwModemCapability.isCapabilitySupport(29)) {
            int nsaState = this.mServiceStateTracker.getmSSHw().getNsaState();
            boolean isNrSsValid = false;
            boolean isNeedClear = (nsaState == 0 || nsaState == 1) && this.mServiceStateTracker.getmSSHw().getHwNetworkType() != DELAYED_TIME_DEFAULT_VALUE;
            if (nsaState == 2) {
                isNeedClear = true;
            }
            if (ss.getNrRsrp() != Integer.MAX_VALUE) {
                isNrSsValid = true;
            }
            if (isDelaySsUpdate && isNeedClear && isNrSsValid) {
                logd("need clear nr signal strength.");
                ss.clearNrSiganlStrength();
            }
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private synchronized void registerSetPreferDb() {
        ContentResolver cr = this.mContext.getContentResolver();
        if (this.mPreferNetworkDb == null && cr != null) {
            this.mPreferNetworkDb = new PreferNetworkDb(new Handler());
            cr.registerContentObserver(Settings.Global.getUriFor("preferred_network_mode"), true, this.mPreferNetworkDb);
        }
    }

    public synchronized void unRegisterSetPreferDb() {
        ContentResolver cr = null;
        if (this.mContext != null) {
            cr = this.mContext.getContentResolver();
        }
        if (!(cr == null || this.mPreferNetworkDb == null)) {
            cr.unregisterContentObserver(this.mPreferNetworkDb);
        }
    }

    public void dispose() {
        unRegisterSetPreferDb();
        try {
            if (this.mReceiver != null) {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            loge("broadcast had been unregistered.");
        }
    }

    /* access modifiers changed from: protected */
    public void logd(String str) {
        RlogEx.i(this.mTag, str);
    }

    /* access modifiers changed from: protected */
    public void logi(String str) {
        RlogEx.i(this.mTag, str);
    }

    /* access modifiers changed from: protected */
    public void loge(String str) {
        RlogEx.e(this.mTag, str);
    }

    protected abstract class HwBaseDoubleSignalStrength {
        protected int mDelayTime;
        protected double mDoubleLteRsrp;
        protected double mDoubleLteRssnr;
        protected double mDoubleNrRsrp;
        protected double mDoubleNrRssnr;
        protected double mOldDoubleLteRsrp;
        protected double mOldDoubleLteRssnr;
        protected double mOldDoubleNrRsrp;
        protected double mOldDoubleNrRssnr;
        protected int mTechState;

        protected HwBaseDoubleSignalStrength() {
        }

        public HwBaseDoubleSignalStrength(SignalStrength ss) {
            this.mDoubleNrRsrp = (double) ss.getNrRsrp();
            this.mDoubleNrRssnr = (double) ss.getNrRssnr();
            this.mDoubleLteRsrp = (double) ss.getLteRsrp();
            this.mDoubleLteRssnr = (double) ss.getLteRssnr();
            this.mOldDoubleNrRsrp = this.mDoubleNrRsrp;
            this.mOldDoubleNrRssnr = this.mDoubleNrRssnr;
            this.mOldDoubleLteRsrp = this.mDoubleLteRsrp;
            this.mOldDoubleLteRssnr = this.mDoubleLteRssnr;
            this.mDelayTime = 0;
        }

        /* access modifiers changed from: protected */
        public boolean processNrRsrpAlaphFilter(HwBaseDoubleSignalStrength oldDoubleSignalStrength, SignalStrength newSignalStrength, SignalStrength modemSignalStrength, boolean isNeedProcessDescend) {
            double oldRsrp = oldDoubleSignalStrength.getDoubleNrRsrp();
            double modemNrRsrp = (double) modemSignalStrength.getNrRsrp();
            this.mOldDoubleNrRsrp = oldRsrp;
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager.logd("NR--old : " + oldRsrp + "; instant new : " + modemNrRsrp);
            if (modemNrRsrp >= HwBaseSignalStrengthManager.SIGNAL_INTERVAL) {
                modemNrRsrp = (double) HwBaseSignalStrengthManager.NR_STRENGTH_POOR_STD;
            }
            if (oldRsrp >= HwBaseSignalStrengthManager.SIGNAL_INTERVAL || oldRsrp <= modemNrRsrp) {
                this.mDoubleNrRsrp = modemNrRsrp;
            } else if (isNeedProcessDescend) {
                this.mDoubleNrRsrp = ((7.0d * oldRsrp) + (5.0d * modemNrRsrp)) / 12.0d;
            } else {
                this.mDoubleNrRsrp = oldRsrp;
            }
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager2 = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager2.logd("NR modem : " + modemNrRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleNrRsrp);
            double d = this.mDoubleNrRsrp;
            if (d - modemNrRsrp <= HwBaseSignalStrengthManager.SIGNAL_INTERVAL || d - modemNrRsrp >= 1.0d) {
                newSignalStrength.setNrRsrp((int) this.mDoubleNrRsrp);
                return true;
            }
            this.mDoubleNrRsrp = modemNrRsrp;
            newSignalStrength.setNrRsrp((int) this.mDoubleNrRsrp);
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean processNrRssnrAlaphFilter(HwBaseDoubleSignalStrength oldDoubleSignalStrength, SignalStrength newSignalStrength, SignalStrength modemSignalStrength, boolean isNeedProcessDescend) {
            double oldRssnr = oldDoubleSignalStrength.getDoubleNrRssnr();
            double modemNrRssnr = (double) modemSignalStrength.getNrRssnr();
            this.mOldDoubleNrRssnr = oldRssnr;
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager.logd("Before processNrRssnrAlaphFilter -- old : " + oldRssnr + "; instant new : " + modemNrRssnr);
            if (modemNrRssnr == 99.0d) {
                modemNrRssnr = (double) HwBaseSignalStrengthManager.NR_RSSNR_POOR_STD;
            }
            if (oldRssnr <= modemNrRssnr) {
                this.mDoubleNrRssnr = modemNrRssnr;
            } else if (isNeedProcessDescend) {
                this.mDoubleNrRssnr = (HwBaseSignalStrengthManager.VALUE_OLD_COEF_QUA_DES_SS * oldRssnr) + (HwBaseSignalStrengthManager.VALUE_NEW_COEF_QUA_DES_SS * modemNrRssnr);
            } else {
                this.mDoubleNrRssnr = oldRssnr;
            }
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager2 = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager2.logd("NrRssnrAlaphFilter modem : " + modemNrRssnr + "; old : " + oldRssnr + "; new : " + this.mDoubleNrRssnr);
            if (this.mDoubleNrRssnr - modemNrRssnr <= HwBaseSignalStrengthManager.SIGNAL_INTERVAL || this.mDoubleLteRssnr - modemNrRssnr >= 1.0d) {
                newSignalStrength.setNrRssnr((int) this.mDoubleNrRssnr);
                return true;
            }
            this.mDoubleNrRssnr = modemNrRssnr;
            newSignalStrength.setNrRssnr((int) this.mDoubleNrRssnr);
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean processLteRsrpAlaphFilter(HwBaseDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldRsrp = oldDoubleSs.getDoubleLteRsrp();
            double modemLteRsrp = (double) modemSs.getLteRsrp();
            this.mOldDoubleLteRsrp = oldRsrp;
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager.logd("LTE--old : " + oldRsrp + "; instant new : " + modemLteRsrp);
            if (modemLteRsrp >= HwBaseSignalStrengthManager.SIGNAL_INTERVAL) {
                modemLteRsrp = (double) HwBaseSignalStrengthManager.LTE_STRENGTH_POOR_STD;
            }
            if (oldRsrp <= modemLteRsrp) {
                this.mDoubleLteRsrp = modemLteRsrp;
            } else if (isNeedProcessDescend) {
                this.mDoubleLteRsrp = ((7.0d * oldRsrp) + (5.0d * modemLteRsrp)) / 12.0d;
            } else {
                this.mDoubleLteRsrp = oldRsrp;
            }
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager2 = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager2.logd("LTE modem : " + modemLteRsrp + "; old : " + oldRsrp + "; new : " + this.mDoubleLteRsrp);
            double d = this.mDoubleLteRsrp;
            if (d - modemLteRsrp <= HwBaseSignalStrengthManager.SIGNAL_INTERVAL || d - modemLteRsrp >= 1.0d) {
                newSs.setLteRsrp((int) this.mDoubleLteRsrp);
                return true;
            }
            this.mDoubleLteRsrp = modemLteRsrp;
            newSs.setLteRsrp((int) this.mDoubleLteRsrp);
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean processLteRssnrAlaphFilter(HwBaseDoubleSignalStrength oldDoubleSs, SignalStrength newSs, SignalStrength modemSs, boolean isNeedProcessDescend) {
            double oldRssnr = oldDoubleSs.getDoubleLteRssnr();
            double modemLteRssnr = (double) modemSs.getLteRssnr();
            this.mOldDoubleLteRssnr = oldRssnr;
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager.logd("Before processLteRssnrAlaphFilter -- old : " + oldRssnr + "; instant new : " + modemLteRssnr);
            if (modemLteRssnr == 99.0d) {
                modemLteRssnr = (double) HwBaseSignalStrengthManager.LTE_RSSNR_POOR_STD;
            }
            if (oldRssnr <= modemLteRssnr) {
                this.mDoubleLteRssnr = modemLteRssnr;
            } else if (isNeedProcessDescend) {
                this.mDoubleLteRssnr = (HwBaseSignalStrengthManager.VALUE_OLD_COEF_QUA_DES_SS * oldRssnr) + (HwBaseSignalStrengthManager.VALUE_NEW_COEF_QUA_DES_SS * modemLteRssnr);
            } else {
                this.mDoubleLteRssnr = oldRssnr;
            }
            HwBaseSignalStrengthManager hwBaseSignalStrengthManager2 = HwBaseSignalStrengthManager.this;
            hwBaseSignalStrengthManager2.logd("LteRssnrAlaphFilter modem : " + modemLteRssnr + "; old : " + oldRssnr + "; new : " + this.mDoubleLteRssnr);
            double d = this.mDoubleLteRssnr;
            if (d - modemLteRssnr <= HwBaseSignalStrengthManager.SIGNAL_INTERVAL || d - modemLteRssnr >= 1.0d) {
                newSs.setLteRssnr((int) this.mDoubleLteRssnr);
                return true;
            }
            this.mDoubleLteRssnr = modemLteRssnr;
            newSs.setLteRssnr((int) this.mDoubleLteRssnr);
            return false;
        }

        /* access modifiers changed from: protected */
        public void processNrFakeSignalStrengthForSlowDescend(HwSignalStrength hwSigStr, HwBaseDoubleSignalStrength oldDoubleSignalStrength, SignalStrength newSignalStrength) {
            HwSignalStrength.SignalThreshold signalThreshold;
            int nrRssnr;
            if ((this.mTechState & 32) != 0) {
                int oldLevel = hwSigStr.getLevel(HwSignalStrength.SignalType.NR, (int) oldDoubleSignalStrength.getOldDoubleNrRsrp(), (int) oldDoubleSignalStrength.getOldDoubleNrRssnr());
                int newLevel = hwSigStr.getLevel(HwSignalStrength.SignalType.NR, newSignalStrength.getNrRsrp(), newSignalStrength.getNrRssnr());
                int diffLevel = oldLevel - newLevel;
                HwBaseSignalStrengthManager.this.logd("NR oldLevel: " + oldLevel + ", newLevel: " + newLevel);
                if (diffLevel > 1 && (signalThreshold = hwSigStr.getSignalThreshold(HwSignalStrength.SignalType.NR)) != null) {
                    int lowerLevel = oldLevel - 1;
                    int nrRsrp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                    if (nrRsrp != -1) {
                        this.mDoubleNrRsrp = (double) nrRsrp;
                        newSignalStrength.setNrRsrp(nrRsrp);
                        this.mDelayTime = HwBaseSignalStrengthManager.VALUE_DELAY_DURING_TIME / diffLevel;
                    }
                    HwBaseSignalStrengthManager.this.logd("NR lowerLevel: " + lowerLevel + ", nrRsrp: " + nrRsrp);
                    if (HwBaseSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM && (nrRssnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true)) != -1) {
                        this.mDoubleNrRssnr = (double) nrRssnr;
                        newSignalStrength.setNrRssnr(nrRssnr);
                        this.mDelayTime = HwBaseSignalStrengthManager.VALUE_DELAY_DURING_TIME / diffLevel;
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void processLteFakeSignalStrengthForSlowDescend(HwSignalStrength hwSigStr, HwBaseDoubleSignalStrength oldDoubleSignalStrength, SignalStrength newSignalStrength, boolean isGsm) {
            HwSignalStrength.SignalThreshold signalThreshold;
            int lteRssnr;
            if ((this.mTechState & 4) != 0) {
                HwSignalStrength.SignalType signalType = isGsm ? HwSignalStrength.SignalType.LTE : HwSignalStrength.SignalType.CDMALTE;
                int oldLevel = hwSigStr.getLevel(signalType, (int) oldDoubleSignalStrength.getOldDoubleLteRsrp(), (int) oldDoubleSignalStrength.getOldDoubleLteRssnr());
                int newLevel = hwSigStr.getLevel(signalType, newSignalStrength.getLteRsrp(), newSignalStrength.getLteRssnr());
                int diffLevel = oldLevel - newLevel;
                HwBaseSignalStrengthManager hwBaseSignalStrengthManager = HwBaseSignalStrengthManager.this;
                StringBuilder sb = new StringBuilder();
                String str = "LTE";
                sb.append(isGsm ? str : "CDMALTE");
                sb.append("oldLevel: ");
                sb.append(oldLevel);
                sb.append(", newLevel: ");
                sb.append(newLevel);
                hwBaseSignalStrengthManager.logd(sb.toString());
                if (diffLevel > 1 && (signalThreshold = hwSigStr.getSignalThreshold(signalType)) != null) {
                    int lowerLevel = oldLevel - 1;
                    int lteRsrp = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, false);
                    if (lteRsrp != -1) {
                        this.mDoubleLteRsrp = (double) lteRsrp;
                        newSignalStrength.setLteRsrp(lteRsrp);
                        this.mDelayTime = HwBaseSignalStrengthManager.VALUE_DELAY_DURING_TIME / diffLevel;
                    }
                    HwBaseSignalStrengthManager hwBaseSignalStrengthManager2 = HwBaseSignalStrengthManager.this;
                    StringBuilder sb2 = new StringBuilder();
                    if (!isGsm) {
                        str = "CDMALTE";
                    }
                    sb2.append(str);
                    sb2.append("lowerLevel: ");
                    sb2.append(lowerLevel);
                    sb2.append(", lteRsrp: ");
                    sb2.append(lteRsrp);
                    hwBaseSignalStrengthManager2.logd(sb2.toString());
                    if (HwBaseSignalStrengthManager.IS_FEATURE_SIGNAL_DUALPARAM && (lteRssnr = signalThreshold.getHighThresholdBySignalLevel(lowerLevel, true)) != -1) {
                        this.mDoubleLteRssnr = (double) lteRssnr;
                        newSignalStrength.setLteRssnr(lteRssnr);
                        this.mDelayTime = HwBaseSignalStrengthManager.VALUE_DELAY_DURING_TIME / diffLevel;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public double getDoubleNrRsrp() {
            return this.mDoubleNrRsrp;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleNrRssnr() {
            return this.mDoubleNrRssnr;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleLteRsrp() {
            return this.mDoubleLteRsrp;
        }

        /* access modifiers changed from: package-private */
        public double getDoubleLteRssnr() {
            return this.mDoubleLteRssnr;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleNrRsrp() {
            return this.mOldDoubleNrRsrp;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleNrRssnr() {
            return this.mOldDoubleNrRssnr;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleLteRsrp() {
            return this.mOldDoubleLteRsrp;
        }

        /* access modifiers changed from: package-private */
        public double getOldDoubleLteRssnr() {
            return this.mOldDoubleLteRssnr;
        }
    }

    /* access modifiers changed from: private */
    public class PreferNetworkDb extends ContentObserver {
        PreferNetworkDb(Handler handler) {
            super(handler);
        }

        public void onChange(boolean isSelfChange) {
            HwBaseSignalStrengthManager.this.logd("Receive PREFERRED_NETWORK_MODE changed. ");
            if (!HwBaseSignalStrengthManager.this.mHwServiceStateTrackerEx.isNrSwitchOn()) {
                HwBaseSignalStrengthManager.this.mHwServiceStateTrackerEx.removeRatChangedDelyaMessage();
                HwBaseSignalStrengthManager.this.cancelDeregisterStateDelayTimer();
            }
            HwBaseSignalStrengthManager.this.mServiceStateTracker.pollState();
        }
    }

    public boolean needCancelDelay() {
        boolean isSubDeactivated = SubscriptionController.getInstance().getSubState(this.mPhone.getPhoneId()) == 0;
        IccCardConstants.State externalState = IccCardConstants.State.UNKNOWN;
        IccCard iccCard = this.mPhone.getPhone().getIccCard();
        if (iccCard != null) {
            externalState = iccCard.getState();
        }
        PhoneConstants.State callState = PhoneFactory.getPhone(this.mPhone.getPhoneId()).getState();
        int newMainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        logd("desiredPowerState : " + this.mServiceStateTracker.getDesiredPowerState() + ", radiostate : " + this.mPhone.getCi().getRadioState() + ", mRadioOffByDoRecovery : " + this.mServiceStateTracker.getDoRecoveryTriggerState() + ", isSubDeactivated : " + isSubDeactivated + ", phoneOOS : " + this.mPhone.getOOSFlag() + ", isUserPref4GSlot : " + HwFullNetworkManager.getInstance().isUserPref4GSlot(newMainSlot) + ", externalState : " + externalState + ", callState : " + callState);
        return !this.mServiceStateTracker.getDesiredPowerState() || this.mPhone.getCi().getRadioState() == 0 || this.mServiceStateTracker.getDoRecoveryTriggerState() || isCardInvalid(isSubDeactivated, this.mPhone.getPhoneId()) || !HwFullNetworkManager.getInstance().isUserPref4GSlot(newMainSlot) || this.mPhone.getOOSFlag() || externalState == IccCardConstants.State.PUK_REQUIRED || callState != PhoneConstants.State.IDLE;
    }
}
