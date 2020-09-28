package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class HwUiccControllerReference extends Handler implements IHwUiccControllerEx {
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
    IUiccControllerInner mController;
    private Handler mHandler = new Handler() {
        /* class com.android.internal.telephony.uicc.HwUiccControllerReference.AnonymousClass1 */

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwUiccControllerReference.log("EVENT_SET_RADIOPOWERDOWN_IFNOCARD");
                HwUiccControllerReference.this.powerDownIfNoCardimmediately();
            }
        }
    };
    private RegistrantList mIccFdnStatusChangedRegistrants = new RegistrantList();
    private PowerManager.WakeLock mWakeLock = null;

    public HwUiccControllerReference(IUiccControllerInner uiccController) {
        this.mController = uiccController;
    }

    public void getUiccCardStatus(Message result, int slotId) {
        CommandsInterfaceEx[] cis = this.mController.getCis();
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

    public void processRadioPowerDownIfNoCard() {
        if (!IS_SIM2AIRPLANE_ENABLED) {
            log("processRadioPowerDownIfNoCard error , pls open this feature");
            return;
        }
        UiccCardExt[] uiccCardExts = this.mController.getUiccCards();
        if (uiccCardExts == null || uiccCardExts.length < 2) {
            log("processRadioPowerDownIfNoCard getUiccCards error");
            return;
        }
        for (UiccCardExt uiccCardExt : uiccCardExts) {
            if (uiccCardExt == null) {
                log("wait for get all uicc cards state done");
                return;
            }
        }
        setRadioPowerDownIfNoCard();
    }

    private void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.mWakeLock.release();
            log("release wakelock");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void powerDownIfNoCardimmediately() {
        if (SystemProperties.getBoolean("gsm.dualcards.switch", false)) {
            log("Dual cards slots switch is doing operations, pls keep waiting!");
            setRadioPowerDownIfNoCard();
            return;
        }
        UiccCardExt[] uiccCardExts = this.mController.getUiccCards();
        if (uiccCardExts != null) {
            for (UiccCardExt uiccCardExt : uiccCardExts) {
                if (uiccCardExt == null) {
                    log("sorry but pls wait for get all uicc cards state done");
                    releaseWakeLock();
                    return;
                }
            }
            int[] sub1Ids = SubscriptionManager.getSubId(0);
            int phone1Id = SubscriptionController.getInstance().getPhoneId(sub1Ids == null ? 0 : sub1Ids[0]);
            int powerOffSub = 1;
            int[] sub2Ids = SubscriptionManager.getSubId(1);
            int phone2Id = SubscriptionController.getInstance().getPhoneId(sub2Ids == null ? 1 : sub2Ids[0]);
            boolean isCard1Present = HwTelephonyManagerInner.getDefault().getCardType(0) != -1 || uiccCardExts[0].getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT;
            boolean isCard2Present = HwTelephonyManagerInner.getDefault().getCardType(1) != -1 || uiccCardExts[1].getCardState() == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT;
            boolean isRadio1Off = PhoneFactory.getPhone(phone1Id).getServiceState().getState() == 3;
            boolean isRadio2Off = PhoneFactory.getPhone(phone2Id).getServiceState().getState() == 3;
            boolean isRadio1Off2 = powerUpRadioIfhasCard(phone1Id, isRadio1Off, isCard1Present);
            boolean isRadio2Off2 = powerUpRadioIfhasCard(phone2Id, isRadio2Off, isCard2Present);
            if (!isRadio1Off2) {
                if (!isRadio2Off2) {
                    if (isCard1Present && isCard2Present) {
                        log("Both cards are present, no need to power off any modem");
                        releaseWakeLock();
                        return;
                    } else if (isCard2Present) {
                        log("Only card 2 is present, judge whether need to power off the 1st modem");
                        if (PhoneFactory.getPhone(0).getState() != PhoneConstants.State.IDLE) {
                            Rlog.i(LOG_TAG, "should try to power off the 1st modem later");
                            setRadioPowerDownIfNoCard();
                            return;
                        } else if (!isRadio1Off2) {
                            Rlog.i(LOG_TAG, "try to power off the 1st modem immediately");
                            PhoneFactory.getPhone(0).setRadioPower(false);
                            releaseWakeLock();
                            return;
                        } else {
                            log("the 1st modem was already powered off before");
                            releaseWakeLock();
                            return;
                        }
                    } else {
                        int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        int slaveSlot = mainSlot == 0 ? 1 : 0;
                        if (!isCard1Present) {
                            powerOffSub = slaveSlot;
                        }
                        boolean isSlaveModemOff = powerOffSub == 0 ? isRadio1Off2 : isRadio2Off2;
                        log("Only card 1 is present or both cards are absent, judge whether need to power off the 2st modem");
                        log("isCard1Present: " + isCard1Present + ", mainSlot: " + mainSlot + ", isSlaveModemOff: " + isSlaveModemOff);
                        if (PhoneFactory.getPhone(powerOffSub).getState() != PhoneConstants.State.IDLE) {
                            Rlog.i(LOG_TAG, "should try to power off the 2st modem later");
                            setRadioPowerDownIfNoCard();
                            return;
                        } else if (!isSlaveModemOff) {
                            Rlog.i(LOG_TAG, "try to power off the 2st modem immediately");
                            PhoneFactory.getPhone(powerOffSub).setRadioPower(false);
                            releaseWakeLock();
                            return;
                        } else {
                            log("the 2st modem was already powered off before");
                            releaseWakeLock();
                            return;
                        }
                    }
                }
            }
            log("there is at least one modem powered down already");
            releaseWakeLock();
        }
    }

    private void setRadioPowerDownIfNoCard() {
        this.mHandler.removeMessages(1);
        long delayTime = DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS;
        if (HwTelephonyManager.getDefault().isVSimEnabled()) {
            delayTime += DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS;
            log("vsim is open, set delay time to " + delayTime);
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), delayTime);
        PowerManager pm = (PowerManager) PhoneFactory.getPhone(0).getContext().getSystemService("power");
        if (this.mWakeLock == null) {
            this.mWakeLock = pm.newWakeLock(1, "RADIOPOWERDOWN_IFNOCARD_WAKELOCK");
        }
        this.mWakeLock.setReferenceCounted(false);
        log("start radiopowerdown ifnocard delay ,acquire wakelock");
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
            log("powerUpRadioIfhasCard: hot swap not support");
            return isRadioOff;
        }
        if (TelephonyManager.MultiSimVariants.DSDS != TelephonyManager.getDefault().getMultiSimConfiguration()) {
            boolean isAirplaneModeOn = false;
            if (!SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
                if (!HwTelephonyManager.getDefault().isPlatformSupportVsim() || !HwVSimUtils.isVSimInProcess()) {
                    Context context = PhoneFactory.getPhone(phoneId).getContext();
                    if (context == null) {
                        log("powerUpRadioIfhasCard: no context");
                        return isRadioOff;
                    }
                    if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
                        isAirplaneModeOn = true;
                    }
                    log("Airplane mode on : " + isAirplaneModeOn + ", Radio off :" + isRadioOff + ", Card present : " + isCardPresent);
                    if (!isCardPresent || !isRadioOff || isAirplaneModeOn) {
                        log("don't try to power on phone" + phoneId);
                        return isRadioOff;
                    }
                    log("Modem is radio off, try to power on the phone" + phoneId + " modem immediately");
                    PhoneFactory.getPhone(phoneId).setRadioPower(true);
                    return false;
                }
                log("powerUpRadioIfhasCard: vsim in process");
                return isRadioOff;
            }
        }
        log("powerUpRadioIfhasCard: multisim variants is DSDS");
        return isRadioOff;
    }

    /* access modifiers changed from: private */
    public static void log(String string) {
        RlogEx.i(LOG_TAG, string);
    }
}
