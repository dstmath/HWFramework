package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.AbstractUiccController;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.vsim.HwVSimUtils;

public class HwUiccControllerReference implements AbstractUiccController.UiccControllerReference {
    protected static final String CHINA_PLMN_MCC = "460";
    protected static final String CHINA_TELECOM_PLMN = "46003";
    protected static final String CHINA_TELECOM_PLMN_FULL_READ = "46099";
    protected static final String CHINA_UNICOM_PLMN = "46001";
    private static final String CHINA_UNICOM_PLMN_SECOND = "46009";
    private static final long DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS = ((long) SystemProperties.getInt("persist.sys.time_sim2airplane", 10000));
    private static final int EVENT_SET_RADIOPOWERDOWN_IFNOCARD = 1;
    private static final boolean IS_SIM2AIRPLANE_ENABLED;
    private static final String LOG_TAG = "HwUiccControllerReference";
    private static final Object mLock = new Object();
    UiccController mController;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Rlog.d(HwUiccControllerReference.LOG_TAG, "EVENT_SET_RADIOPOWERDOWN_IFNOCARD");
                HwUiccControllerReference.this.powerDownIfNoCardimmediately();
            }
        }
    };
    private RegistrantList mIccFdnStatusChangedRegistrants = new RegistrantList();
    private PowerManager.WakeLock mWakeLock = null;

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
        if (SystemProperties.getBoolean("ro.config.hw_sim2airplane", false) && SystemProperties.getBoolean("persist.sys.hw_sim2airplane", true) && TelephonyManager.getDefault().isMultiSimEnabled()) {
            z = true;
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

    /* access modifiers changed from: private */
    public void powerDownIfNoCardimmediately() {
        if (SystemProperties.getBoolean("gsm.dualcards.switch", false)) {
            Rlog.d(LOG_TAG, "Dual cards slots switch is doing operations, pls keep waiting!");
            setRadioPowerDownIfNoCard();
            return;
        }
        for (UiccCard uc : this.mController.getUiccCards()) {
            if (uc == null) {
                Rlog.d(LOG_TAG, "sorry but pls wait for get all uicc cards state done");
                releaseWakeLock();
                return;
            }
        }
        int[] sub1Ids = SubscriptionManager.getSubId(0);
        int sub1Id = sub1Ids == null ? 0 : sub1Ids[0];
        int phone1Id = SubscriptionController.getInstance().getPhoneId(sub1Id);
        int powerOffSub = 1;
        int[] sub2Ids = SubscriptionManager.getSubId(1);
        int phone2Id = SubscriptionController.getInstance().getPhoneId(sub2Ids == null ? 1 : sub2Ids[0]);
        boolean isCard1Present = HwTelephonyManagerInner.getDefault().getCardType(0) != -1 || this.mController.getUiccCards()[0].getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT;
        boolean isCard2Present = HwTelephonyManagerInner.getDefault().getCardType(1) != -1 || this.mController.getUiccCards()[1].getCardState() == IccCardStatus.CardState.CARDSTATE_PRESENT;
        boolean isRadio1Off = PhoneFactory.getPhone(phone1Id).getServiceState().getState() == 3;
        boolean isRadio2Off = PhoneFactory.getPhone(phone2Id).getServiceState().getState() == 3;
        boolean isRadio1Off2 = powerUpRadioIfhasCard(phone1Id, isRadio1Off, isCard1Present);
        boolean isRadio2Off2 = powerUpRadioIfhasCard(phone2Id, isRadio2Off, isCard2Present);
        if (isRadio1Off2) {
            int i = sub1Id;
        } else if (isRadio2Off2) {
            int[] iArr = sub1Ids;
            int i2 = sub1Id;
        } else {
            if (isCard1Present && isCard2Present) {
                Rlog.d(LOG_TAG, "Both cards are present, no need to power off any modem");
                releaseWakeLock();
            } else if (isCard2Present) {
                Rlog.d(LOG_TAG, "Only card 2 is present, judge whether need to power off the 1st modem");
                if (PhoneFactory.getPhone(0).getState() != PhoneConstants.State.IDLE) {
                    Rlog.i(LOG_TAG, "should try to power off the 1st modem later");
                    setRadioPowerDownIfNoCard();
                } else if (!isRadio1Off2) {
                    Rlog.i(LOG_TAG, "try to power off the 1st modem immediately");
                    PhoneFactory.getPhone(0).setRadioPower(false);
                    releaseWakeLock();
                } else {
                    Rlog.d(LOG_TAG, "the 1st modem was already powered off before");
                    releaseWakeLock();
                }
            } else {
                int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                int slaveSlot = mainSlot == 0 ? 1 : 0;
                if (!isCard1Present) {
                    powerOffSub = slaveSlot;
                }
                boolean isSlaveModemOff = powerOffSub == 0 ? isRadio1Off2 : isRadio2Off2;
                int[] iArr2 = sub1Ids;
                Rlog.d(LOG_TAG, "Only card 1 is present or both cards are absent, judge whether need to power off the 2st modem");
                StringBuilder sb = new StringBuilder();
                int i3 = sub1Id;
                sb.append("isCard1Present: ");
                sb.append(isCard1Present);
                sb.append(", mainSlot: ");
                sb.append(mainSlot);
                sb.append(", isSlaveModemOff: ");
                sb.append(isSlaveModemOff);
                Rlog.d(LOG_TAG, sb.toString());
                if (PhoneFactory.getPhone(powerOffSub).getState() != PhoneConstants.State.IDLE) {
                    Rlog.i(LOG_TAG, "should try to power off the 2st modem later");
                    setRadioPowerDownIfNoCard();
                } else if (!isSlaveModemOff) {
                    Rlog.i(LOG_TAG, "try to power off the 2st modem immediately");
                    PhoneFactory.getPhone(powerOffSub).setRadioPower(false);
                    releaseWakeLock();
                } else {
                    Rlog.d(LOG_TAG, "the 2st modem was already powered off before");
                    releaseWakeLock();
                }
                return;
            }
            return;
        }
        Rlog.d(LOG_TAG, "there is at least one modem powered down already");
        releaseWakeLock();
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
        }
        if (TelephonyManager.MultiSimVariants.DSDS != TelephonyManager.getDefault().getMultiSimConfiguration()) {
            boolean isAirplaneModeOn = false;
            if (!SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
                if (HwVSimUtils.isVSimInProcess()) {
                    Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: vsim in process");
                    return isRadioOff;
                }
                Context context = PhoneFactory.getPhone(phoneId).getContext();
                if (context == null) {
                    Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: no context");
                    return isRadioOff;
                }
                if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
                    isAirplaneModeOn = true;
                }
                Rlog.d(LOG_TAG, "Airplane mode on : " + isAirplaneModeOn + ", Radio off :" + isRadioOff + ", Card present : " + isCardPresent);
                if (!isCardPresent || !isRadioOff || isAirplaneModeOn) {
                    Rlog.d(LOG_TAG, "don't try to power on phone" + phoneId);
                } else {
                    Rlog.d(LOG_TAG, "Modem is radio off, try to power on the phone" + phoneId + " modem immediately");
                    PhoneFactory.getPhone(phoneId).setRadioPower(true);
                    isRadioOff = false;
                }
                return isRadioOff;
            }
        }
        Rlog.d(LOG_TAG, "powerUpRadioIfhasCard: multisim variants is DSDS");
        return isRadioOff;
    }
}
