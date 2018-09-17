package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.AbstractUiccController.UiccControllerReference;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.vsim.HwVSimUtils;

public class HwUiccControllerReference implements UiccControllerReference {
    protected static final String CHINA_PLMN_MCC = "460";
    protected static final String CHINA_TELECOM_PLMN = "46003";
    protected static final String CHINA_TELECOM_PLMN_FULL_READ = "46099";
    protected static final String CHINA_UNICOM_PLMN = "46001";
    private static final String CHINA_UNICOM_PLMN_SECOND = "46009";
    private static final long DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS = ((long) SystemProperties.getInt("persist.sys.time_sim2airplane", 30000));
    private static final int EVENT_SET_RADIOPOWERDOWN_IFNOCARD = 1;
    private static final boolean IS_SIM2AIRPLANE_ENABLED;
    private static final String LOG_TAG = "HwUiccControllerReference";
    private static final Object mLock = new Object();
    UiccController mController;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Rlog.d(HwUiccControllerReference.LOG_TAG, "EVENT_SET_RADIOPOWERDOWN_IFNOCARD");
                    HwUiccControllerReference.this.powerDownIfNoCardimmediately();
                    return;
                default:
                    return;
            }
        }
    };
    private RegistrantList mIccFdnStatusChangedRegistrants = new RegistrantList();
    private WakeLock mWakeLock = null;

    public HwUiccControllerReference(UiccController uiccController) {
        this.mController = uiccController;
    }

    public int getCardType(int slotId) {
        return -1;
    }

    public void getUiccCardStatus(Message result, int slotId) {
        CommandsInterface[] cis = this.mController.getmCis();
        if (cis != null && slotId < cis.length) {
            cis[slotId].getIccCardStatus(result);
        }
    }

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.hw_sim2airplane", false) && SystemProperties.getBoolean("persist.sys.hw_sim2airplane", true)) {
            z = TelephonyManager.getDefault().isMultiSimEnabled();
        }
        IS_SIM2AIRPLANE_ENABLED = z;
    }

    public void processRadioPowerDownIfNoCard(UiccCard[] uiccCards) {
        if (!IS_SIM2AIRPLANE_ENABLED || uiccCards.length < 2) {
            Rlog.d(LOG_TAG, "processRadioPowerDownIfNoCard error , pls open this feature");
            return;
        }
        for (UiccCard uc : this.mController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(LOG_TAG, "wait for get all uicc cards state done");
                return;
            }
        }
        setRadioPowerDownIfNoCard();
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            Rlog.d(LOG_TAG, "release wakelock");
        }
    }

    private void powerDownIfNoCardimmediately() {
        if (SystemProperties.getBoolean("gsm.dualcards.switch", false)) {
            Rlog.d(LOG_TAG, "Dual cards slots switch is doing operations, pls keep waiting!");
            setRadioPowerDownIfNoCard();
            return;
        }
        boolean isCard1Present;
        boolean isCard2Present;
        for (UiccCard uc : this.mController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(LOG_TAG, "sorry but pls wait for get all uicc cards state done");
                releaseWakeLock();
                return;
            }
        }
        int[] sub1Ids = SubscriptionManager.getSubId(0);
        int phone1Id = SubscriptionController.getInstance().getPhoneId(sub1Ids == null ? 0 : sub1Ids[0]);
        int[] sub2Ids = SubscriptionManager.getSubId(1);
        int phone2Id = SubscriptionController.getInstance().getPhoneId(sub2Ids == null ? 1 : sub2Ids[0]);
        if (HwTelephonyManagerInner.getDefault().getCardType(0) == -1) {
            isCard1Present = this.mController.getUiccCards()[0].getCardState() == CardState.CARDSTATE_PRESENT;
        } else {
            isCard1Present = true;
        }
        if (HwTelephonyManagerInner.getDefault().getCardType(1) == -1) {
            isCard2Present = this.mController.getUiccCards()[1].getCardState() == CardState.CARDSTATE_PRESENT;
        } else {
            isCard2Present = true;
        }
        boolean isRadio1Off = PhoneFactory.getPhone(phone1Id).getServiceState().getState() == 3;
        boolean isRadio2Off = PhoneFactory.getPhone(phone2Id).getServiceState().getState() == 3;
        isRadio1Off = powerUpRadioIfhasCard(phone1Id, isRadio1Off, isCard1Present);
        isRadio2Off = powerUpRadioIfhasCard(phone2Id, isRadio2Off, isCard2Present);
        if (isRadio1Off || isRadio2Off) {
            Rlog.d(LOG_TAG, "there is at least one modem powered down already");
            releaseWakeLock();
            return;
        }
        if (isCard1Present && isCard2Present) {
            Rlog.d(LOG_TAG, "Both cards are present, no need to power off any modem");
            releaseWakeLock();
        } else if (isCard2Present) {
            Rlog.d(LOG_TAG, "Only card 2 is present, judge whether need to power off the 1st modem");
            if (PhoneFactory.getPhone(0).getState() != State.IDLE) {
                Rlog.i(LOG_TAG, "should try to power off the 1st modem later");
                setRadioPowerDownIfNoCard();
            } else if (isRadio1Off) {
                Rlog.d(LOG_TAG, "the 1st modem was already powered off before");
                releaseWakeLock();
            } else {
                Rlog.i(LOG_TAG, "try to power off the 1st modem immediately");
                PhoneFactory.getPhone(0).setRadioPower(false);
                releaseWakeLock();
            }
        } else {
            int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            int powerOffSub = isCard1Present ? 1 : mainSlot == 0 ? 1 : 0;
            boolean isSlaveModemOff = powerOffSub == 0 ? isRadio1Off : isRadio2Off;
            Rlog.d(LOG_TAG, "Only card 1 is present or both cards are absent, judge whether need to power off the 2st modem");
            Rlog.d(LOG_TAG, "isCard1Present: " + isCard1Present + ", mainSlot: " + mainSlot + ", isSlaveModemOff: " + isSlaveModemOff);
            if (PhoneFactory.getPhone(powerOffSub).getState() != State.IDLE) {
                Rlog.i(LOG_TAG, "should try to power off the 2st modem later");
                setRadioPowerDownIfNoCard();
            } else if (isSlaveModemOff) {
                Rlog.d(LOG_TAG, "the 2st modem was already powered off before");
                releaseWakeLock();
            } else {
                Rlog.i(LOG_TAG, "try to power off the 2st modem immediately");
                PhoneFactory.getPhone(powerOffSub).setRadioPower(false);
                releaseWakeLock();
            }
        }
    }

    private void setRadioPowerDownIfNoCard() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS);
        PowerManager pm = (PowerManager) PhoneFactory.getPhone(0).getContext().getSystemService("power");
        if (this.mWakeLock == null) {
            this.mWakeLock = pm.newWakeLock(1, "RADIOPOWERDOWN_IFNOCARD_WAKELOCK");
        }
        this.mWakeLock.setReferenceCounted(false);
        Rlog.d(LOG_TAG, "start radiopowerdown ifnocard delay ,acquire wakelock");
        this.mWakeLock.acquire();
    }

    public void registerForFdnStatusChange(Handler h, int what, Object obj) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            synchronized (mLock) {
                Registrant r = new Registrant(h, what, obj);
                this.mIccFdnStatusChangedRegistrants.add(r);
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForFdnStatusChange(Handler h) {
        if (HuaweiTelephonyConfigs.isPsRestrictedByFdn()) {
            synchronized (mLock) {
                this.mIccFdnStatusChangedRegistrants.remove(h);
            }
        }
    }

    public void notifyFdnStatusChange() {
        this.mIccFdnStatusChangedRegistrants.notifyRegistrants();
    }

    private boolean powerUpRadioIfhasCard(int phoneId, boolean isRadioOff, boolean isCardPresent) {
        if (!HwHotplugController.IS_HOTSWAP_SUPPORT) {
            Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: hot swap not support");
            return isRadioOff;
        } else if (MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
            Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: multisim variants is DSDS");
            return isRadioOff;
        } else if (HwVSimUtils.isVSimInProcess()) {
            Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: vsim in process");
            return isRadioOff;
        } else {
            Context context = PhoneFactory.getPhone(phoneId).getContext();
            if (context == null) {
                Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: no context");
                return isRadioOff;
            }
            boolean isAirplaneModeOn = Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
            Rlog.d(LOG_TAG, "Airplane mode on : " + isAirplaneModeOn + ", Radio off :" + isRadioOff + ", Card present : " + isCardPresent);
            if (isCardPresent && isRadioOff && (isAirplaneModeOn ^ 1) != 0) {
                Rlog.d(LOG_TAG, "Modem is radio off, try to power on the phone" + phoneId + " modem immediately");
                PhoneFactory.getPhone(phoneId).setRadioPower(true);
                isRadioOff = false;
            } else {
                Rlog.d(LOG_TAG, "don't try to power on phone" + phoneId);
            }
            return isRadioOff;
        }
    }
}
