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
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import huawei.android.telephony.wrapper.OptWrapperFactory;

public class HwUiccControllerReference implements UiccControllerReference {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = null;
    protected static final String CHINA_PLMN_MCC = "460";
    protected static final String CHINA_TELECOM_PLMN = "46003";
    protected static final String CHINA_TELECOM_PLMN_FULL_READ = "46099";
    protected static final String CHINA_UNICOM_PLMN = "46001";
    private static final String CHINA_UNICOM_PLMN_SECOND = "46009";
    private static final long DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS = 0;
    private static final int EVENT_SET_RADIOPOWERDOWN_IFNOCARD = 1;
    private static final boolean IS_SIM2AIRPLANE_ENABLED = false;
    private static final String LOG_TAG = "HwUiccControllerReference";
    private static final Object mLock = null;
    UiccController mController;
    private Handler mHandler;
    private RegistrantList mIccFdnStatusChangedRegistrants;
    private WakeLock mWakeLock;

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues() {
        if (-com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = EVENT_SET_RADIOPOWERDOWN_IFNOCARD;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.HwUiccControllerReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.HwUiccControllerReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.HwUiccControllerReference.<clinit>():void");
    }

    public HwUiccControllerReference(UiccController uiccController) {
        this.mWakeLock = null;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwUiccControllerReference.EVENT_SET_RADIOPOWERDOWN_IFNOCARD /*1*/:
                        Rlog.d(HwUiccControllerReference.LOG_TAG, "EVENT_SET_RADIOPOWERDOWN_IFNOCARD");
                        HwUiccControllerReference.this.powerDownIfNoCardimmediately();
                    default:
                }
            }
        };
        this.mIccFdnStatusChangedRegistrants = new RegistrantList();
        this.mController = uiccController;
    }

    public int getCardType(int slotId) {
        Rlog.d(LOG_TAG, "getCardType slotId = " + slotId);
        UiccCard uicccard = OptWrapperFactory.getMSimUiccControllerWrapper().getUiccCard(this.mController, slotId);
        AppType[] appType = new AppType[]{AppType.APPTYPE_UNKNOWN, AppType.APPTYPE_UNKNOWN};
        int appCount = 0;
        int cardType = -1;
        String gsmImsi = null;
        String cdmaImsi = null;
        if (slotId < 0 || slotId > 2 || uicccard == null) {
            return -1;
        }
        int i;
        for (i = 0; i < 8; i += EVENT_SET_RADIOPOWERDOWN_IFNOCARD) {
            UiccCardApplication app = uicccard.getApplication(i);
            if (app != null) {
                if (appCount < 2) {
                    appType[appCount] = app.getType();
                    Rlog.d(LOG_TAG, "index " + appCount + " appType = " + appType[appCount]);
                    if (AppType.APPTYPE_RUIM == appType[appCount] || AppType.APPTYPE_CSIM == appType[appCount]) {
                        IccRecords records = app.getIccRecords();
                        if (records != null && gsmImsi == null && cdmaImsi == null) {
                            gsmImsi = records.getIMSI();
                            cdmaImsi = records.getIMSI();
                            Rlog.d(LOG_TAG, "cdmaImsi = xxxxxx");
                        }
                    }
                }
                appCount += EVENT_SET_RADIOPOWERDOWN_IFNOCARD;
            }
        }
        switch (appCount) {
            case EVENT_SET_RADIOPOWERDOWN_IFNOCARD /*1*/:
                switch (-getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues()[appType[0].ordinal()]) {
                    case EVENT_SET_RADIOPOWERDOWN_IFNOCARD /*1*/:
                    case HwVSimUtilsInner.STATE_EB /*2*/:
                        cardType = 30;
                        break;
                    case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                        cardType = 10;
                        break;
                    case HwVSimEventReport.VSIM_PROCESS_TYPE_ED /*4*/:
                        cardType = 20;
                        break;
                    default:
                        cardType = -1;
                        break;
                }
            case HwVSimUtilsInner.STATE_EB /*2*/:
                i = 0;
                while (i < appType.length) {
                    if (appType[i] == AppType.APPTYPE_RUIM && appType[1 - i] == AppType.APPTYPE_SIM) {
                        cardType = 40;
                        Rlog.d(LOG_TAG, "getCardType:cdmaImsi=xxxxxx ,gsmImsi=xxxxxx");
                        if (!(gsmImsi == null || cdmaImsi == null)) {
                            try {
                                if (CHINA_TELECOM_PLMN.equals(cdmaImsi.substring(0, 5))) {
                                    String plmn = gsmImsi.substring(0, 5);
                                    String mcc = gsmImsi.substring(0, 3);
                                    if (CHINA_UNICOM_PLMN.equals(plmn) || CHINA_UNICOM_PLMN_SECOND.equals(plmn)) {
                                        cardType = 42;
                                    }
                                    if (CHINA_TELECOM_PLMN.equals(plmn) || CHINA_TELECOM_PLMN_FULL_READ.equals(plmn)) {
                                        cardType = 30;
                                    }
                                    if (!CHINA_PLMN_MCC.equals(mcc)) {
                                        cardType = 41;
                                    }
                                }
                            } catch (RuntimeException e) {
                                cardType = -1;
                            }
                        }
                    }
                    if (appType[i] == AppType.APPTYPE_USIM && appType[1 - i] == AppType.APPTYPE_SIM) {
                        cardType = 50;
                    }
                    i += EVENT_SET_RADIOPOWERDOWN_IFNOCARD;
                }
                break;
            default:
                cardType = -1;
                break;
        }
        return cardType;
    }

    public void getUiccCardStatus(Message result, int slotId) {
        CommandsInterface[] cis = this.mController.getmCis();
        if (cis != null && slotId < cis.length) {
            cis[slotId].getIccCardStatus(result);
        }
    }

    public void processRadioPowerDownIfNoCard(UiccCard[] uiccCards) {
        if (!IS_SIM2AIRPLANE_ENABLED || uiccCards.length < 2) {
            Rlog.d(LOG_TAG, "processRadioPowerDownIfNoCard error , pls open this feature");
            return;
        }
        UiccCard[] uiccCards2 = this.mController.getUiccCards();
        int length = uiccCards2.length;
        for (int i = 0; i < length; i += EVENT_SET_RADIOPOWERDOWN_IFNOCARD) {
            if (uiccCards2[i] == null) {
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
        if (SystemProperties.getBoolean("gsm.dualcards.switch", IS_SIM2AIRPLANE_ENABLED)) {
            Rlog.d(LOG_TAG, "Dual cards slots switch is doing operations, pls keep waiting!");
            setRadioPowerDownIfNoCard();
            return;
        }
        boolean isCard1Present;
        boolean isCard2Present;
        UiccCard[] uiccCards = this.mController.getUiccCards();
        int length = uiccCards.length;
        for (int i = 0; i < length; i += EVENT_SET_RADIOPOWERDOWN_IFNOCARD) {
            if (uiccCards[i] == null) {
                Rlog.d(LOG_TAG, "sorry but pls wait for get all uicc cards state done");
                releaseWakeLock();
                return;
            }
        }
        int[] sub1Ids = SubscriptionManager.getSubId(0);
        int phone1Id = SubscriptionController.getInstance().getPhoneId(sub1Ids == null ? 0 : sub1Ids[0]);
        int[] sub2Ids = SubscriptionManager.getSubId(EVENT_SET_RADIOPOWERDOWN_IFNOCARD);
        int phone2Id = SubscriptionController.getInstance().getPhoneId(sub2Ids == null ? EVENT_SET_RADIOPOWERDOWN_IFNOCARD : sub2Ids[0]);
        if (HwTelephonyManagerInner.getDefault().getCardType(0) == -1) {
            isCard1Present = this.mController.getUiccCards()[0].getCardState() == CardState.CARDSTATE_PRESENT ? true : IS_SIM2AIRPLANE_ENABLED;
        } else {
            isCard1Present = true;
        }
        if (HwTelephonyManagerInner.getDefault().getCardType(EVENT_SET_RADIOPOWERDOWN_IFNOCARD) == -1) {
            isCard2Present = this.mController.getUiccCards()[EVENT_SET_RADIOPOWERDOWN_IFNOCARD].getCardState() == CardState.CARDSTATE_PRESENT ? true : IS_SIM2AIRPLANE_ENABLED;
        } else {
            isCard2Present = true;
        }
        boolean isRadio1Off = PhoneFactory.getPhone(phone1Id).getServiceState().getState() == 3 ? true : IS_SIM2AIRPLANE_ENABLED;
        boolean isRadio2Off = PhoneFactory.getPhone(phone2Id).getServiceState().getState() == 3 ? true : IS_SIM2AIRPLANE_ENABLED;
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
                PhoneFactory.getPhone(0).setRadioPower(IS_SIM2AIRPLANE_ENABLED);
                releaseWakeLock();
            }
        } else {
            int powerOffSub;
            int mainSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            int slaveSlot = mainSlot == 0 ? EVENT_SET_RADIOPOWERDOWN_IFNOCARD : 0;
            if (isCard1Present) {
                powerOffSub = EVENT_SET_RADIOPOWERDOWN_IFNOCARD;
            } else {
                powerOffSub = slaveSlot;
            }
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
                PhoneFactory.getPhone(powerOffSub).setRadioPower(IS_SIM2AIRPLANE_ENABLED);
                releaseWakeLock();
            }
        }
    }

    private void setRadioPowerDownIfNoCard() {
        this.mHandler.removeMessages(EVENT_SET_RADIOPOWERDOWN_IFNOCARD);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SET_RADIOPOWERDOWN_IFNOCARD), DELAY_SET_RADIOPOWERDOWN_IFNOCARD_MILLIS);
        PowerManager pm = (PowerManager) PhoneFactory.getPhone(0).getContext().getSystemService("power");
        if (this.mWakeLock == null) {
            this.mWakeLock = pm.newWakeLock(EVENT_SET_RADIOPOWERDOWN_IFNOCARD, "RADIOPOWERDOWN_IFNOCARD_WAKELOCK");
        }
        this.mWakeLock.setReferenceCounted(IS_SIM2AIRPLANE_ENABLED);
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
        } else if (MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", IS_SIM2AIRPLANE_ENABLED)) {
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
            boolean isAirplaneModeOn = Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0 ? true : IS_SIM2AIRPLANE_ENABLED;
            Rlog.d(LOG_TAG, "Airplane mode on : " + isAirplaneModeOn + ", Radio off :" + isRadioOff + ", Card present : " + isCardPresent);
            if (isCardPresent && isRadioOff && !isAirplaneModeOn) {
                Rlog.d(LOG_TAG, "Modem is radio off, try to power on the phone" + phoneId + " modem immediately");
                PhoneFactory.getPhone(phoneId).setRadioPower(true);
                isRadioOff = IS_SIM2AIRPLANE_ENABLED;
            } else {
                Rlog.d(LOG_TAG, "don't try to power on phone" + phoneId);
            }
            return isRadioOff;
        }
    }
}
