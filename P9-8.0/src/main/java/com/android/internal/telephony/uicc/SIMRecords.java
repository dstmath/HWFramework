package com.android.internal.telephony.uicc;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.encrypt.PasswordUtil;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.android.internal.telephony.uicc.UsimServiceTable.UsimService;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SIMRecords extends IccRecords {
    private static final /* synthetic */ int[] -com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues = null;
    static final int CFF_LINE1_MASK = 15;
    static final int CFF_LINE1_RESET = 240;
    static final int CFF_UNCONDITIONAL_ACTIVE = 10;
    static final int CFF_UNCONDITIONAL_DEACTIVE = 5;
    private static final int CFIS_ADN_CAPABILITY_ID_OFFSET = 14;
    private static final int CFIS_ADN_EXTENSION_ID_OFFSET = 15;
    private static final int CFIS_BCD_NUMBER_LENGTH_OFFSET = 2;
    private static final int CFIS_TON_NPI_OFFSET = 3;
    public static final String CF_ENABLED = "cf_enabled_key";
    private static final int CPHS_SST_MBN_ENABLED = 48;
    private static final int CPHS_SST_MBN_MASK = 48;
    private static final boolean CRASH_RIL = false;
    private static final int EVENT_APP_LOCKED = 258;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 257;
    private static final int EVENT_GET_AD_DONE = 9;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CFF_DONE = 24;
    private static final int EVENT_GET_CFIS_DONE = 32;
    private static final int EVENT_GET_CPHS_MAILBOX_DONE = 11;
    private static final int EVENT_GET_CSP_CPHS_DONE = 33;
    private static final int EVENT_GET_EHPLMN_DONE = 40;
    private static final int EVENT_GET_FPLMN_DONE = 41;
    private static final int EVENT_GET_GID1_DONE = 34;
    private static final int EVENT_GET_GID2_DONE = 36;
    private static final int EVENT_GET_HPLMN_W_ACT_DONE = 39;
    private static final int EVENT_GET_ICCID_DONE = 4;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_INFO_CPHS_DONE = 26;
    private static final int EVENT_GET_MBDN_DONE = 6;
    private static final int EVENT_GET_MBI_DONE = 5;
    private static final int EVENT_GET_MSISDN_DONE = 10;
    private static final int EVENT_GET_MWIS_DONE = 7;
    private static final int EVENT_GET_OPLMN_W_ACT_DONE = 38;
    private static final int EVENT_GET_PLMN_W_ACT_DONE = 37;
    private static final int EVENT_GET_PNN_DONE = 15;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SPDI_DONE = 13;
    private static final int EVENT_GET_SPN_DONE = 12;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE = 8;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SET_CPHS_MAILBOX_DONE = 25;
    private static final int EVENT_SET_MBDN_DONE = 20;
    private static final int EVENT_SET_MSISDN_DONE = 30;
    private static final int EVENT_SIM_REFRESH = 259;
    private static final int EVENT_SMS_ON_SIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    protected static final String LOG_TAG = "SIMRecords";
    private static final String[] MCCMNC_CODES_HAVING_2DIGITS_MNC = new String[]{"40400", "40401", "40402", "40403", "40404", "40405", "40407", "40409", "40410", "40411", "40412", "40413", "40414", "40415", "40416", "40417", "40418", "40419", "40420", "40421", "40422", "40424", "40425", "40427", "40428", "40429", "40430", "40431", "40433", "40434", "40435", "40436", "40437", "40438", "40440", "40441", "40442", "40443", "40444", "40445", "40446", "40449", "40450", "40451", "40452", "40453", "40454", "40455", "40456", "40457", "40458", "40459", "40460", "40462", "40464", "40466", "40467", "40468", "40469", "40470", "40471", "40472", "40473", "40474", "40475", "40476", "40477", "40478", "40479", "40480", "40481", "40482", "40483", "40484", "40485", "40486", "40487", "40488", "40489", "40490", "40491", "40492", "40493", "40494", "40495", "40496", "40497", "40498", "40501", "40505", "40506", "40507", "40508", "40509", "40510", "40511", "40512", "40513", "40514", "40515", "40517", "40518", "40519", "40520", "40521", "40522", "40523", "40524", "40548", "40551", "40552", "40553", "40554", "40555", "40556", "40566", "40567", "40570", "23210"};
    private static final String[] MCCMNC_CODES_HAVING_2DIGITS_MNC_ZERO_PREFIX_RELIANCE = new String[]{"40503", "40504"};
    private static final String[] MCCMNC_CODES_HAVING_3DIGITS_MNC = new String[]{"302370", "302720", SimulatedCommands.FAKE_MCC_MNC, "405025", "405026", "405027", "405028", "405029", "405030", "405031", "405032", "405033", "405034", "405035", "405036", "405037", "405038", "405039", "405040", "405041", "405042", "405043", "405044", "405045", "405046", "405047", "405750", "405751", "405752", "405753", "405754", "405755", "405756", "405799", "405800", "405801", "405802", "405803", "405804", "405805", "405806", "405807", "405808", "405809", "405810", "405811", "405812", "405813", "405814", "405815", "405816", "405817", "405818", "405819", "405820", "405821", "405822", "405823", "405824", "405825", "405826", "405827", "405828", "405829", "405830", "405831", "405832", "405833", "405834", "405835", "405836", "405837", "405838", "405839", "405840", "405841", "405842", "405843", "405844", "405845", "405846", "405847", "405848", "405849", "405850", "405851", "405852", "405853", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874", "405875", "405876", "405877", "405878", "405879", "405880", "405881", "405882", "405883", "405884", "405885", "405886", "405908", "405909", "405910", "405911", "405912", "405913", "405914", "405915", "405916", "405917", "405918", "405919", "405920", "405921", "405922", "405923", "405924", "405925", "405926", "405927", "405928", "405929", "405930", "405931", "405932", "502142", "502143", "502145", "502146", "502147", "502148"};
    private static final boolean NEED_CHECK_MSISDN_BIT = SystemProperties.getBoolean("ro.config.hw_check_msisdn", false);
    public static final String SIM_IMSI = "sim_imsi_key";
    private static final int SIM_RECORD_EVENT_BASE = 0;
    private static final int SYSTEM_EVENT_BASE = 256;
    static final int TAG_FULL_NETWORK_NAME = 67;
    static final int TAG_SHORT_NETWORK_NAME = 69;
    static final int TAG_SPDI = 163;
    static final int TAG_SPDI_PLMN_LIST = 128;
    private static final boolean VDBG = false;
    public static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    private static PasswordUtil mPasswordUtil = HwFrameworkFactory.getPasswordUtil();
    private int mCallForwardingStatus;
    private byte[] mCphsInfo;
    boolean mCspPlmnEnabled;
    byte[] mEfCPHS_MWI;
    byte[] mEfCff;
    byte[] mEfCfis;
    byte[] mEfLi;
    byte[] mEfMWIS;
    byte[] mEfPl;
    private String mFirstImsi;
    private HwCustSIMRecords mHwCustSIMRecords;
    private String mOriginVmImsi;
    String mPnnHomeName;
    private final BroadcastReceiver mReceiver;
    private String mSecondImsi;
    ArrayList<String> mSpdiNetworks;
    int mSpnDisplayCondition;
    SpnOverride mSpnOverride;
    private GetSpnFsmState mSpnState;
    UsimServiceTable mUsimServiceTable;
    VoiceMailConstants mVmConfig;

    private class EfPlLoaded implements IccRecordLoaded {
        /* synthetic */ EfPlLoaded(SIMRecords this$0, EfPlLoaded -this1) {
            this();
        }

        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    private class EfUsimLiLoaded implements IccRecordLoaded {
        /* synthetic */ EfUsimLiLoaded(SIMRecords this$0, EfUsimLiLoaded -this1) {
            this();
        }

        private EfUsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    private enum GetSpnFsmState {
        IDLE,
        INIT,
        READ_SPN_3GPP,
        READ_SPN_CPHS,
        READ_SPN_SHORT_CPHS
    }

    private static /* synthetic */ int[] -getcom-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues() {
        if (-com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues != null) {
            return -com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues;
        }
        int[] iArr = new int[GetSpnFsmState.values().length];
        try {
            iArr[GetSpnFsmState.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GetSpnFsmState.INIT.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_3GPP.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_CPHS.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GetSpnFsmState.READ_SPN_SHORT_CPHS.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        -com-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues = iArr;
        return iArr;
    }

    public String toString() {
        return "SimRecords: " + super.toString() + " mVmConfig" + this.mVmConfig + " mSpnOverride=" + "mSpnOverride" + " callForwardingEnabled=" + this.mCallForwardingStatus + " spnState=" + this.mSpnState + " mCphsInfo=" + this.mCphsInfo + " mCspPlmnEnabled=" + this.mCspPlmnEnabled + " efMWIS=" + this.mEfMWIS + " efCPHS_MWI=" + this.mEfCPHS_MWI + " mEfCff=" + this.mEfCff + " mEfCfis=" + this.mEfCfis + " getOperatorNumeric=" + getOperatorNumeric();
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mCphsInfo = null;
        this.mCspPlmnEnabled = true;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mEfCff = null;
        this.mEfCfis = null;
        this.mEfLi = null;
        this.mEfPl = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.mHwCustSIMRecords = null;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    SIMRecords.this.sendMessage(SIMRecords.this.obtainMessage(257));
                }
            }
        };
        this.mAdnCache = HwTelephonyFactory.getHwUiccManager().createHwAdnRecordCache(this.mFh);
        this.mVmConfig = (VoiceMailConstants) HwTelephonyFactory.getHwUiccManager().createHwVoiceMailConstants(c, getSlotId());
        this.mSpnOverride = new SpnOverride();
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        this.mCi.setOnSmsOnSim(this, 21, null);
        this.mCi.registerForIccRefresh(this, 259, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 258, null);
        log("SIMRecords X ctor this=" + this);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        c.registerReceiver(this.mReceiver, intentfilter);
    }

    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.mCi.unregisterForIccRefresh(this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        resetRecords();
        super.dispose();
    }

    protected void finalize() {
        log("finalized");
    }

    protected void resetRecords() {
        this.mImsi = null;
        this.mMsisdn = null;
        this.mVoiceMailNum = null;
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mSpnDisplayCondition = -1;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mSpdiNetworks = null;
        this.mPnnHomeName = null;
        this.mGid1 = null;
        this.mGid2 = null;
        this.mPlmnActRecords = null;
        this.mOplmnActRecords = null;
        this.mHplmnActRecords = null;
        this.mFplmns = null;
        this.mEhplmns = null;
        this.mAdnCache.reset();
        log("SIMRecords: onRadioOffOrNotAvailable set 'gsm.sim.operator.numeric' to operator=null");
        log("update icc_operator_numeric=" + null);
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), "");
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), "");
        this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), "");
        this.mRecordsRequested = false;
        this.mImsiLoad = false;
    }

    public String getIMSI() {
        return this.mImsi;
    }

    public String getMsisdnNumber() {
        if (!NEED_CHECK_MSISDN_BIT || this.mUsimServiceTable == null || (this.mUsimServiceTable.isAvailable(UsimService.MSISDN) ^ 1) == 0) {
            return this.mMsisdn;
        }
        log("EF_MSISDN not available");
        return null;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    private int getExtFromEf(int ef) {
        switch (ef) {
            case IccConstants.EF_MSISDN /*28480*/:
                if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
                    return IccConstants.EF_EXT5;
                }
                return IccConstants.EF_EXT1;
            default:
                return IccConstants.EF_EXT1;
        }
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    public String getMsisdnAlphaTag() {
        if (!NEED_CHECK_MSISDN_BIT || this.mUsimServiceTable == null || (this.mUsimServiceTable.isAvailable(UsimService.MSISDN) ^ 1) == 0) {
            return this.mMsisdnTag;
        }
        log("EF_MSISDN not available");
        return null;
    }

    public String getVoiceMailNumber() {
        if (this.mHwCustSIMRecords == null || !this.mHwCustSIMRecords.isOpenRoamingVoiceMail()) {
            return this.mVoiceMailNum;
        }
        return this.mHwCustSIMRecords.getRoamingVoicemail();
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        if (this.mIsVoiceMailFixed) {
            AsyncResult.forMessage(onComplete).exception = new IccVmFixedException("Voicemail number is fixed by operator");
            onComplete.sendToTarget();
            return;
        }
        this.mNewVoiceMailNum = voiceNumber;
        this.mNewVoiceMailTag = alphaTag;
        AdnRecord adn = new AdnRecord(this.mNewVoiceMailTag, this.mNewVoiceMailNum);
        if (this.mMailboxIndex != 0 && this.mMailboxIndex != 255) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, null, obtainMessage(20, onComplete));
        } else if (isCphsMailboxEnabled()) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onComplete));
        } else {
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
            onComplete.sendToTarget();
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        int i = 0;
        if (line == 1) {
            try {
                if (this.mEfMWIS != null) {
                    byte[] bArr = this.mEfMWIS;
                    int i2 = this.mEfMWIS[0] & 254;
                    if (countWaiting != 0) {
                        i = 1;
                    }
                    bArr[0] = (byte) (i | i2);
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = (byte) 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mEfCPHS_MWI != null) {
                    this.mEfCPHS_MWI[0] = (byte) ((countWaiting == 0 ? 5 : 10) | (this.mEfCPHS_MWI[0] & 240));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    private boolean validEfCfis(byte[] data) {
        if (data == null) {
            return false;
        }
        if (data[0] < (byte) 1 || data[0] > (byte) 4) {
            logw("MSP byte: " + data[0] + " is not between 1 and 4", null);
        }
        return true;
    }

    public int getVoiceMessageCount() {
        int countVoiceMessages = -2;
        if (this.mEfMWIS != null) {
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (((this.mEfMWIS[0] & 1) != 0) && (countVoiceMessages == 0 || countVoiceMessages == 255)) {
                countVoiceMessages = -1;
            }
            log(" VoiceMessageCount from SIM MWIS = " + countVoiceMessages);
        } else if (this.mEfCPHS_MWI != null) {
            int indicator = this.mEfCPHS_MWI[0] & 15;
            if (indicator == 10) {
                countVoiceMessages = -1;
            } else if (indicator == 5) {
                countVoiceMessages = 0;
            }
            log(" VoiceMessageCount from SIM CPHS = " + countVoiceMessages);
        }
        return countVoiceMessages;
    }

    public int getVoiceCallForwardingFlag() {
        return this.mCallForwardingStatus;
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i = 0;
        if (line == 1) {
            if (enable) {
                i = 1;
            }
            this.mCallForwardingStatus = i;
            this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(1));
            try {
                if (validEfCfis(this.mEfCfis)) {
                    byte[] bArr;
                    if (enable) {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] & 254);
                    }
                    log("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && (TextUtils.isEmpty(dialNumber) ^ 1) != 0) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii(LOG_TAG, dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(dialNumber);
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = (byte) -1;
                        this.mEfCfis[15] = (byte) -1;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_CFIS, 1, this.mEfCfis, null, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFIS)));
                } else {
                    log("setVoiceCallForwardingFlag: ignoring enable=" + enable + " invalid mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    setCallForwardingPreference(enable);
                }
                if (this.mEfCff != null) {
                    if (enable) {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 10);
                    } else {
                        this.mEfCff[0] = (byte) ((this.mEfCff[0] & 240) | 5);
                    }
                    this.mFh.updateEFTransparent(IccConstants.EF_CFF_CPHS, this.mEfCff, obtainMessage(14, Integer.valueOf(IccConstants.EF_CFF_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    public String getOperatorNumeric() {
        if (this.mImsi == null) {
            log("IMSI == null");
            return null;
        } else if (this.mImsi.length() < 6 || this.mImsi.length() > 15) {
            Rlog.e(LOG_TAG, "invalid IMSI ");
            return null;
        } else if (this.mMncLength != -1 && this.mMncLength != 0) {
            return this.mImsi.substring(0, this.mMncLength + 3);
        } else {
            log("getSIMOperatorNumeric: bad mncLength");
            if (this.mImsi.length() >= 5) {
                String mcc = this.mImsi.substring(0, 3);
                if (mcc.equals("404") || mcc.equals("405") || mcc.equals("232")) {
                    String mccmncCode = this.mImsi.substring(0, 5);
                    for (String mccmnc : MCCMNC_CODES_HAVING_2DIGITS_MNC) {
                        if (mccmnc.equals(mccmncCode)) {
                            this.mMncLength = 2;
                            return this.mImsi.substring(0, this.mMncLength + 3);
                        }
                    }
                }
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:118:0x0456, code:
            if (r23.equals("405") != false) goto L_0x0458;
     */
    /* JADX WARNING: Missing block: B:199:0x067c, code:
            if (r23.equals("405") != false) goto L_0x067e;
     */
    /* JADX WARNING: Missing block: B:280:0x08c1, code:
            if (r23.equals("405") != false) goto L_0x08c3;
     */
    /* JADX WARNING: Missing block: B:361:0x0ae5, code:
            if (r23.equals("405") != false) goto L_0x0ae7;
     */
    /* JADX WARNING: Missing block: B:443:0x0d4d, code:
            if (r23.equals("405") != false) goto L_0x0d4f;
     */
    /* JADX WARNING: Missing block: B:510:0x0f0d, code:
            if (r23.equals("405") != false) goto L_0x0f0f;
     */
    /* JADX WARNING: Missing block: B:556:0x1060, code:
            if (r32.mMncLength == 2) goto L_0x0cc6;
     */
    /* JADX WARNING: Missing block: B:596:0x1173, code:
            if (r23.equals("405") != false) goto L_0x1175;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        String[] strArr;
        int i;
        int length;
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message[" + msg.what + "], Ignoring.");
            return;
        }
        String mccmncCode;
        String mcc;
        String mccStr;
        int mcc2;
        boolean isImsiAndAdDone;
        try {
            AsyncResult ar;
            byte[] data;
            AdnRecord adn;
            int i2;
            int length2;
            switch (msg.what) {
                case 1:
                    onReady();
                    break;
                case 3:
                    isRecordLoadResponse = true;
                    onGetImsiDone(msg);
                    break;
                case 4:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mIccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                        this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                        onIccIdLoadedHw();
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                        this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                        break;
                    }
                    this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                    break;
                case 5:
                    isRecordLoadResponse = true;
                    ar = msg.obj;
                    data = ar.result;
                    boolean isValidMbdn = false;
                    if (ar.exception == null) {
                        log("EF_MBI: " + IccUtils.bytesToHexString(data));
                        this.mMailboxIndex = data[0] & 255;
                        if (!(this.mMailboxIndex == 0 || this.mMailboxIndex == 255)) {
                            log("Got valid mailbox number for MBDN");
                            isValidMbdn = true;
                        }
                    }
                    this.mRecordsToLoad++;
                    if (!isValidMbdn) {
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                        break;
                    } else {
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                        break;
                    }
                case 6:
                case 11:
                    this.mVoiceMailNum = null;
                    this.mVoiceMailTag = null;
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        adn = ar.result;
                        log("VM: " + adn + (msg.what == 11 ? " EF[MAILBOX]" : " EF[MBDN]"));
                        if (!adn.isEmpty() || msg.what != 6) {
                            this.mVoiceMailNum = adn.getNumber();
                            this.mVoiceMailTag = adn.getAlphaTag();
                            this.mVmConfig.setVoicemailOnSIM(this.mVoiceMailNum, this.mVoiceMailTag);
                            break;
                        }
                        this.mRecordsToLoad++;
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                        break;
                    }
                    log("Invalid or missing EF" + (msg.what == 11 ? "[MAILBOX]" : "[MBDN]"));
                    if (msg.what == 6) {
                        this.mRecordsToLoad++;
                        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                        break;
                    }
                    break;
                case 7:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    log("EF_MWIS : " + IccUtils.bytesToHexString(data));
                    if (ar.exception == null) {
                        if ((data[0] & 255) != 255) {
                            if (!SystemProperties.getBoolean("ro.config.hw_eeVoiceMsgCount", false) || (data[0] & 255) != 0) {
                                this.mEfMWIS = data;
                                break;
                            }
                            this.mEfMWIS = null;
                            Rlog.d(LOG_TAG, "SIMRecords EE VoiceMessageCount from SIM CPHS");
                            break;
                        }
                        log("SIMRecords: Uninitialized record MWIS");
                        break;
                    }
                    log("EVENT_GET_MWIS_DONE exception = " + ar.exception);
                    break;
                    break;
                case 8:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    log("EF_CPHS_MWI: " + IccUtils.bytesToHexString(data));
                    if (ar.exception == null) {
                        this.mEfCPHS_MWI = data;
                        break;
                    } else {
                        log("EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = " + ar.exception);
                        break;
                    }
                case 9:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    IccIoResult result = ar.result;
                    IccException iccException = result.getException();
                    String[] strArr2;
                    if (ar.exception == null && iccException == null) {
                        if (result.payload != null) {
                            data = result.payload;
                            log("EF_AD: " + IccUtils.bytesToHexString(data));
                            if (data.length >= 3) {
                                if (data.length != 3) {
                                    this.mMncLength = data[3] & 15;
                                    log("setting4 mMncLength=" + this.mMncLength);
                                    if (this.mMncLength == 15) {
                                        this.mMncLength = 0;
                                        log("setting5 mMncLength=" + this.mMncLength);
                                    } else if (this.mMncLength > 3) {
                                        this.mMncLength = 2;
                                    } else if (!(this.mMncLength == 2 || this.mMncLength == 3)) {
                                        this.mMncLength = -1;
                                        log("setting5 mMncLength=" + this.mMncLength);
                                    }
                                    if (!(this.mMncLength == -1 || this.mMncLength == 0)) {
                                        break;
                                    }
                                    if (this.mImsi != null && this.mImsi.length() >= 6) {
                                        mccmncCode = this.mImsi.substring(0, 6);
                                        strArr2 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                        i2 = 0;
                                        length2 = strArr2.length;
                                        while (i2 < length2) {
                                            if (strArr2[i2].equals(mccmncCode)) {
                                                this.mMncLength = 3;
                                                log("setting6 mMncLength=" + this.mMncLength);
                                            } else {
                                                i2++;
                                            }
                                        }
                                    }
                                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                        mcc = this.mImsi.substring(0, 3);
                                        if (!mcc.equals("404")) {
                                            break;
                                        }
                                        mccmncCode = this.mImsi.substring(0, 5);
                                        strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                        i2 = 0;
                                        length2 = strArr2.length;
                                        while (i2 < length2) {
                                            if (strArr2[i2].equals(mccmncCode)) {
                                                this.mMncLength = 2;
                                                custMncLength(this.mImsi.substring(0, 3));
                                            } else {
                                                i2++;
                                            }
                                        }
                                        custMncLength(this.mImsi.substring(0, 3));
                                    }
                                    if (this.mMncLength == 0 || this.mMncLength == -1) {
                                        if (this.mImsi != null) {
                                            try {
                                                mccStr = this.mImsi.substring(0, 3);
                                                if (!mccStr.equals("404")) {
                                                    if (!mccStr.equals("405")) {
                                                        mcc2 = Integer.parseInt(mccStr);
                                                        Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc2);
                                                        this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc2);
                                                    }
                                                }
                                                this.mMncLength = 3;
                                            } catch (NumberFormatException e) {
                                                this.mMncLength = 0;
                                                loge("Corrupt IMSI!");
                                            }
                                        } else {
                                            this.mMncLength = 0;
                                            log("MNC length not present in EF_AD");
                                        }
                                    }
                                    if (!(this.mImsi == null || this.mMncLength == 0)) {
                                        log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                        setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                        setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                        log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                        updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                    }
                                    isImsiAndAdDone = (this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true;
                                    if (isImsiAndAdDone && (this.mImsiLoad ^ 1) != 0) {
                                        this.mImsiLoad = true;
                                        this.mParentApp.notifyGetAdDone(null);
                                        onOperatorNumericLoadedHw();
                                    }
                                    initFdnPsStatus(getSlotId());
                                    break;
                                }
                                log("MNC length not present in EF_AD");
                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                    mccmncCode = this.mImsi.substring(0, 6);
                                    strArr2 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                    i2 = 0;
                                    length2 = strArr2.length;
                                    while (i2 < length2) {
                                        if (strArr2[i2].equals(mccmncCode)) {
                                            this.mMncLength = 3;
                                            log("setting6 mMncLength=" + this.mMncLength);
                                        } else {
                                            i2++;
                                        }
                                    }
                                }
                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                    mcc = this.mImsi.substring(0, 3);
                                    if (!mcc.equals("404")) {
                                        break;
                                    }
                                    mccmncCode = this.mImsi.substring(0, 5);
                                    strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                    i2 = 0;
                                    length2 = strArr2.length;
                                    while (i2 < length2) {
                                        if (strArr2[i2].equals(mccmncCode)) {
                                            this.mMncLength = 2;
                                            custMncLength(this.mImsi.substring(0, 3));
                                        } else {
                                            i2++;
                                        }
                                    }
                                    custMncLength(this.mImsi.substring(0, 3));
                                }
                                if (this.mMncLength == 0 || this.mMncLength == -1) {
                                    if (this.mImsi != null) {
                                        try {
                                            mccStr = this.mImsi.substring(0, 3);
                                            if (!mccStr.equals("404")) {
                                                if (!mccStr.equals("405")) {
                                                    mcc2 = Integer.parseInt(mccStr);
                                                    Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc2);
                                                    this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc2);
                                                }
                                            }
                                            this.mMncLength = 3;
                                        } catch (NumberFormatException e2) {
                                            this.mMncLength = 0;
                                            loge("Corrupt IMSI!");
                                        }
                                    } else {
                                        this.mMncLength = 0;
                                        log("MNC length not present in EF_AD");
                                    }
                                }
                                if (!(this.mImsi == null || this.mMncLength == 0)) {
                                    log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                    setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                    setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                    log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                    updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                }
                                isImsiAndAdDone = (this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true;
                                if (isImsiAndAdDone && (this.mImsiLoad ^ 1) != 0) {
                                    this.mImsiLoad = true;
                                    this.mParentApp.notifyGetAdDone(null);
                                    onOperatorNumericLoadedHw();
                                }
                                initFdnPsStatus(getSlotId());
                                break;
                            }
                            log("Corrupt AD data on SIM");
                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                mccmncCode = this.mImsi.substring(0, 6);
                                strArr2 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                i2 = 0;
                                length2 = strArr2.length;
                                while (i2 < length2) {
                                    if (strArr2[i2].equals(mccmncCode)) {
                                        this.mMncLength = 3;
                                        log("setting6 mMncLength=" + this.mMncLength);
                                    } else {
                                        i2++;
                                    }
                                }
                            }
                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                mcc = this.mImsi.substring(0, 3);
                                if (!mcc.equals("404")) {
                                    break;
                                }
                                mccmncCode = this.mImsi.substring(0, 5);
                                strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                i2 = 0;
                                length2 = strArr2.length;
                                while (i2 < length2) {
                                    if (strArr2[i2].equals(mccmncCode)) {
                                        this.mMncLength = 2;
                                        custMncLength(this.mImsi.substring(0, 3));
                                    } else {
                                        i2++;
                                    }
                                }
                                custMncLength(this.mImsi.substring(0, 3));
                            }
                            if (this.mMncLength == 0 || this.mMncLength == -1) {
                                if (this.mImsi != null) {
                                    try {
                                        mccStr = this.mImsi.substring(0, 3);
                                        if (!mccStr.equals("404")) {
                                            if (!mccStr.equals("405")) {
                                                mcc2 = Integer.parseInt(mccStr);
                                                Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc2);
                                                this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc2);
                                            }
                                        }
                                        this.mMncLength = 3;
                                    } catch (NumberFormatException e3) {
                                        this.mMncLength = 0;
                                        loge("Corrupt IMSI!");
                                    }
                                } else {
                                    this.mMncLength = 0;
                                    log("MNC length not present in EF_AD");
                                }
                            }
                            if (!(this.mImsi == null || this.mMncLength == 0)) {
                                log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                            }
                            isImsiAndAdDone = (this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true;
                            if (isImsiAndAdDone && (this.mImsiLoad ^ 1) != 0) {
                                this.mImsiLoad = true;
                                this.mParentApp.notifyGetAdDone(null);
                                onOperatorNumericLoadedHw();
                            }
                            initFdnPsStatus(getSlotId());
                            break;
                        }
                        log("result.payload is null");
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                            mccmncCode = this.mImsi.substring(0, 6);
                            strArr2 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                            i2 = 0;
                            length2 = strArr2.length;
                            while (i2 < length2) {
                                if (strArr2[i2].equals(mccmncCode)) {
                                    this.mMncLength = 3;
                                    log("setting6 mMncLength=" + this.mMncLength);
                                } else {
                                    i2++;
                                }
                            }
                        }
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                            mcc = this.mImsi.substring(0, 3);
                            if (!mcc.equals("404")) {
                                break;
                            }
                            mccmncCode = this.mImsi.substring(0, 5);
                            strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                            i2 = 0;
                            length2 = strArr2.length;
                            while (i2 < length2) {
                                if (strArr2[i2].equals(mccmncCode)) {
                                    this.mMncLength = 2;
                                    custMncLength(this.mImsi.substring(0, 3));
                                } else {
                                    i2++;
                                }
                            }
                            custMncLength(this.mImsi.substring(0, 3));
                        }
                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                            if (this.mImsi != null) {
                                try {
                                    mccStr = this.mImsi.substring(0, 3);
                                    if (!mccStr.equals("404")) {
                                        if (!mccStr.equals("405")) {
                                            mcc2 = Integer.parseInt(mccStr);
                                            Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc2);
                                            this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc2);
                                        }
                                    }
                                    this.mMncLength = 3;
                                } catch (NumberFormatException e4) {
                                    this.mMncLength = 0;
                                    loge("Corrupt IMSI!");
                                }
                            } else {
                                this.mMncLength = 0;
                                log("MNC length not present in EF_AD");
                            }
                        }
                        if (!(this.mImsi == null || this.mMncLength == 0)) {
                            log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                            setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                            setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                            log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                            updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                        }
                        isImsiAndAdDone = (this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true;
                        if (isImsiAndAdDone && (this.mImsiLoad ^ 1) != 0) {
                            this.mImsiLoad = true;
                            this.mParentApp.notifyGetAdDone(null);
                            onOperatorNumericLoadedHw();
                        }
                        initFdnPsStatus(getSlotId());
                        break;
                    }
                    log("read EF_AD exception occurs");
                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                        mccmncCode = this.mImsi.substring(0, 6);
                        strArr2 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                        i2 = 0;
                        length2 = strArr2.length;
                        while (i2 < length2) {
                            if (strArr2[i2].equals(mccmncCode)) {
                                this.mMncLength = 3;
                                log("setting6 mMncLength=" + this.mMncLength);
                            } else {
                                i2++;
                            }
                        }
                    }
                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                        mcc = this.mImsi.substring(0, 3);
                        if (!mcc.equals("404")) {
                            break;
                        }
                        mccmncCode = this.mImsi.substring(0, 5);
                        strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                        i2 = 0;
                        length2 = strArr2.length;
                        while (i2 < length2) {
                            if (strArr2[i2].equals(mccmncCode)) {
                                this.mMncLength = 2;
                                custMncLength(this.mImsi.substring(0, 3));
                            } else {
                                i2++;
                            }
                        }
                        custMncLength(this.mImsi.substring(0, 3));
                    }
                    if (this.mMncLength == 0 || this.mMncLength == -1) {
                        if (this.mImsi != null) {
                            try {
                                mccStr = this.mImsi.substring(0, 3);
                                if (!mccStr.equals("404")) {
                                    if (!mccStr.equals("405")) {
                                        mcc2 = Integer.parseInt(mccStr);
                                        Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc2);
                                        this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc2);
                                    }
                                }
                                this.mMncLength = 3;
                            } catch (NumberFormatException e5) {
                                this.mMncLength = 0;
                                loge("Corrupt IMSI!");
                            }
                        } else {
                            this.mMncLength = 0;
                            log("MNC length not present in EF_AD");
                        }
                    }
                    if (!(this.mImsi == null || this.mMncLength == 0)) {
                        log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                        setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                        setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                        log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                        updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                    }
                    isImsiAndAdDone = (this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true;
                    if (isImsiAndAdDone && (this.mImsiLoad ^ 1) != 0) {
                        this.mImsiLoad = true;
                        this.mParentApp.notifyGetAdDone(null);
                        onOperatorNumericLoadedHw();
                    }
                    initFdnPsStatus(getSlotId());
                    break;
                    break;
                case 10:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        adn = (AdnRecord) ar.result;
                        this.mMsisdn = adn.getNumber();
                        this.mMsisdnTag = adn.getAlphaTag();
                        log("MSISDN isempty:" + TextUtils.isEmpty(this.mMsisdn));
                        break;
                    }
                    log("Invalid or missing EF[MSISDN]");
                    break;
                case 12:
                    isRecordLoadResponse = true;
                    getSpnFsm(false, (AsyncResult) msg.obj);
                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                        mcc = this.mImsi.substring(0, 3);
                        if (!mcc.equals("404")) {
                            break;
                        }
                        mccmncCode = this.mImsi.substring(0, 5);
                        if (!TextUtils.isEmpty(getServiceProviderName()) && getServiceProviderName().toLowerCase(Locale.US).contains("reliance")) {
                            for (String mccmnc : MCCMNC_CODES_HAVING_2DIGITS_MNC_ZERO_PREFIX_RELIANCE) {
                                if (mccmnc.equals(mccmncCode)) {
                                    this.mMncLength = 2;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                case 13:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        parseEfSpdi(data);
                        break;
                    }
                    break;
                case 14:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        logw("update failed. ", ar.exception);
                        break;
                    }
                    break;
                case 15:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        SimTlv simTlv = new SimTlv(data, 0, data.length);
                        while (simTlv.isValidObject()) {
                            if (simTlv.getTag() == 67) {
                                this.mPnnHomeName = IccUtils.networkNameToString(simTlv.getData(), 0, simTlv.getData().length);
                                break;
                            }
                            simTlv.nextObject();
                        }
                        break;
                    }
                    break;
                case 17:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mUsimServiceTable = new UsimServiceTable(data);
                        log("SST: " + this.mUsimServiceTable);
                        if (checkFileInServiceTable(IccConstants.EF_SPN, this.mUsimServiceTable, data)) {
                            getSpnFsm(true, null);
                        }
                        checkFileInServiceTable(IccConstants.EF_PNN, this.mUsimServiceTable, data);
                        break;
                    }
                    break;
                case 18:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        handleSmses((ArrayList) ar.result);
                        break;
                    }
                    break;
                case 19:
                    Rlog.i("ENF", "marked read: sms " + msg.arg1);
                    break;
                case 20:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    log("EVENT_SET_MBDN_DONE ex:" + ar.exception);
                    if (ar.exception == null) {
                        this.mVoiceMailNum = this.mNewVoiceMailNum;
                        this.mVoiceMailTag = this.mNewVoiceMailTag;
                    }
                    if (!isCphsMailboxEnabled()) {
                        if (ar.userObj != null) {
                            CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                            if (ar.exception == null || configLoader == null || !configLoader.getConfig().getBoolean("editable_voicemail_number_bool")) {
                                AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                            } else {
                                AsyncResult.forMessage((Message) ar.userObj).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
                            }
                            ((Message) ar.userObj).sendToTarget();
                            break;
                        }
                    }
                    adn = new AdnRecord(this.mVoiceMailTag, this.mVoiceMailNum);
                    Message onCphsCompleted = ar.userObj;
                    if (ar.exception == null && ar.userObj != null) {
                        AsyncResult.forMessage((Message) ar.userObj).exception = null;
                        ((Message) ar.userObj).sendToTarget();
                        log("Callback with MBDN successful.");
                        onCphsCompleted = null;
                    }
                    new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onCphsCompleted));
                    break;
                    break;
                case 21:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    Integer index = ar.result;
                    if (ar.exception == null && index != null) {
                        log("READ EF_SMS RECORD index=" + index);
                        this.mFh.loadEFLinearFixed(IccConstants.EF_SMS, index.intValue(), obtainMessage(22));
                        break;
                    }
                    loge("Error on SMS_ON_SIM with exp " + ar.exception + " index " + index);
                    break;
                    break;
                case 22:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        loge("Error on GET_SMS with exp " + ar.exception);
                        break;
                    } else {
                        handleSms((byte[]) ar.result);
                        break;
                    }
                case 24:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_CFF_CPHS: " + IccUtils.bytesToHexString(data));
                        this.mEfCff = data;
                        break;
                    }
                    this.mEfCff = null;
                    break;
                case 25:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mVoiceMailNum = this.mNewVoiceMailNum;
                        this.mVoiceMailTag = this.mNewVoiceMailTag;
                    } else {
                        log("Set CPHS MailBox with exception: " + ar.exception);
                    }
                    if (ar.userObj != null) {
                        log("Callback with CPHS MB successful.");
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    }
                    break;
                case 26:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mCphsInfo = (byte[]) ar.result;
                        log("iCPHS: " + IccUtils.bytesToHexString(this.mCphsInfo));
                        break;
                    }
                    break;
                case 30:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mMsisdn = this.mNewMsisdn;
                        this.mMsisdnTag = this.mNewMsisdnTag;
                        log("Success to update EF[MSISDN]");
                    }
                    if (ar.userObj != null) {
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    }
                    break;
                case 32:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        log("EF_CFIS: " + IccUtils.bytesToHexString(data));
                        if (!validEfCfis(data)) {
                            log("EF_CFIS: " + IccUtils.bytesToHexString(data));
                            break;
                        }
                        this.mEfCfis = data;
                        if ((data[1] & 1) != 0) {
                            i2 = 1;
                        } else {
                            i2 = 0;
                        }
                        this.mCallForwardingStatus = i2;
                        log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
                        this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(1));
                        break;
                    }
                    this.mEfCfis = null;
                    String imsiOld = getVmSimImsi();
                    if (this.mOriginVmImsi == null) {
                        this.mOriginVmImsi = imsiOld;
                    }
                    if (imsiOld != null) {
                        if (imsiOld.equals(this.mImsi)) {
                            if (getCallForwardingPreference()) {
                                this.mCallForwardingStatus = 1;
                                this.mRecordsEventsRegistrants.notifyResult(Integer.valueOf(1));
                                break;
                            }
                        }
                    }
                    if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                        setCallForwardingPreference(false);
                        break;
                    }
                    if (this.mFirstImsi == null) {
                        this.mFirstImsi = this.mImsi;
                    } else if (!this.mFirstImsi.equals(this.mImsi) && this.mSecondImsi == null) {
                        this.mSecondImsi = this.mImsi;
                    }
                    if (!(this.mOriginVmImsi == null || this.mFirstImsi == null || (this.mOriginVmImsi.equals(this.mFirstImsi) ^ 1) == 0 || this.mSecondImsi == null || (this.mOriginVmImsi.equals(this.mSecondImsi) ^ 1) == 0)) {
                        setCallForwardingPreference(false);
                        break;
                    }
                    break;
                case 33:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        data = (byte[]) ar.result;
                        log("EF_CSP: " + IccUtils.bytesToHexString(data));
                        boolean oldCspPlmnEnabled = this.mCspPlmnEnabled;
                        handleEfCspData(data);
                        checkSendBroadcast(oldCspPlmnEnabled, this.mCspPlmnEnabled);
                        break;
                    }
                    loge("Exception in fetching EF_CSP data " + ar.exception);
                    break;
                case 34:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mGid1 = IccUtils.bytesToHexString(data);
                        log("GID1: " + this.mGid1);
                        break;
                    }
                    loge("Exception in get GID1 " + ar.exception);
                    this.mGid1 = null;
                    break;
                case 36:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mGid2 = IccUtils.bytesToHexString(data);
                        log("GID2: " + this.mGid2);
                        break;
                    }
                    loge("Exception in get GID2 " + ar.exception);
                    this.mGid2 = null;
                    break;
                case 37:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null && data != null) {
                        log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data));
                        this.mPlmnActRecords = PlmnActRecord.getRecords(data);
                        break;
                    }
                    loge("Failed getting User PLMN with Access Tech Records: " + ar.exception);
                    break;
                    break;
                case 38:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null && data != null) {
                        log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data));
                        this.mOplmnActRecords = PlmnActRecord.getRecords(data);
                        break;
                    }
                    loge("Failed getting Operator PLMN with Access Tech Records: " + ar.exception);
                    break;
                    break;
                case 39:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null && data != null) {
                        log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data));
                        this.mHplmnActRecords = PlmnActRecord.getRecords(data);
                        log("HplmnActRecord[]=" + Arrays.toString(this.mHplmnActRecords));
                        break;
                    }
                    loge("Failed getting Home PLMN with Access Tech Records: " + ar.exception);
                    break;
                    break;
                case 40:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null && data != null) {
                        this.mEhplmns = parseBcdPlmnList(data, "Equivalent Home");
                        break;
                    } else {
                        loge("Failed getting Equivalent Home PLMNs: " + ar.exception);
                        break;
                    }
                    break;
                case 41:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null && data != null) {
                        this.mFplmns = parseBcdPlmnList(data, "Forbidden");
                        if (msg.arg1 == 1238273) {
                            isRecordLoadResponse = false;
                            Message response = retrievePendingResponseMessage(Integer.valueOf(msg.arg2));
                            if (response == null) {
                                loge("Failed to retrieve a response message for FPLMN");
                                break;
                            }
                            AsyncResult.forMessage(response, Arrays.copyOf(this.mFplmns, this.mFplmns.length), null);
                            response.sendToTarget();
                            break;
                        }
                    }
                    loge("Failed getting Forbidden PLMNs: " + ar.exception);
                    break;
                    break;
                case 257:
                    handleCarrierNameOverride();
                    break;
                case 258:
                    onLocked();
                    break;
                case 259:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    log("Sim REFRESH with exception: " + ar.exception);
                    if (ar.exception == null) {
                        handleSimRefresh((IccRefreshResponse) ar.result);
                        break;
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            logw("Exception parsing SIM record", exc);
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        }
    }

    protected void handleFileUpdate(int efid) {
        switch (efid) {
            case IccConstants.EF_CFF_CPHS /*28435*/:
            case IccConstants.EF_CFIS /*28619*/:
                log("SIM Refresh called for EF_CFIS or EF_CFF_CPHS");
                loadCallForwardingRecords();
                return;
            case IccConstants.EF_CSP_CPHS /*28437*/:
                this.mRecordsToLoad++;
                log("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
                return;
            case IccConstants.EF_MAILBOX_CPHS /*28439*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                return;
            case IccConstants.EF_FDN /*28475*/:
                log("SIM Refresh called for EF_FDN");
                this.mParentApp.queryFdn();
                this.mAdnCache.reset();
                return;
            case IccConstants.EF_MSISDN /*28480*/:
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
                return;
            case IccConstants.EF_MBDN /*28615*/:
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                return;
            default:
                this.mAdnCache.reset();
                fetchSimRecords();
                return;
        }
    }

    private void handleSimRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleSimRefresh received without input");
        } else if ((TextUtils.isEmpty(refreshResponse.aid) || (refreshResponse.aid.equals(this.mParentApp.getAid()) ^ 1) == 0) && !beforeHandleSimRefresh(refreshResponse)) {
            switch (refreshResponse.refreshResult) {
                case 0:
                    log("handleSimRefresh with SIM_FILE_UPDATED");
                    handleFileUpdate(refreshResponse.efId);
                    break;
                case 1:
                    log("handleSimRefresh with SIM_REFRESH_INIT");
                    onIccRefreshInit();
                    this.mParentApp.queryFdn();
                    break;
                case 2:
                    log("handleSimRefresh with SIM_REFRESH_RESET");
                    break;
                default:
                    log("handleSimRefresh with unknown operation");
                    break;
            }
            if (!afterHandleSimRefresh(refreshResponse)) {
            }
        }
    }

    private int dispatchGsmMessage(SmsMessage message) {
        this.mNewSmsRegistrants.notifyResult(message);
        return 0;
    }

    private void handleSms(byte[] ba) {
        if (ba[0] != (byte) 0) {
            Rlog.d("ENF", "status : " + ba[0]);
        }
        if (ba[0] == (byte) 3) {
            int n = ba.length;
            byte[] pdu = new byte[(n - 1)];
            System.arraycopy(ba, 1, pdu, 0, n - 1);
            dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
        }
    }

    private void handleSmses(ArrayList<byte[]> messages) {
        int count = messages.size();
        for (int i = 0; i < count; i++) {
            byte[] ba = (byte[]) messages.get(i);
            if (ba[0] != (byte) 0) {
                Rlog.i("ENF", "status " + i + ": " + ba[0]);
            }
            if (ba[0] == (byte) 3) {
                int n = ba.length;
                byte[] pdu = new byte[(n - 1)];
                System.arraycopy(ba, 1, pdu, 0, n - 1);
                dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
                ba[0] = (byte) 1;
            }
        }
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    private void setVoiceCallForwardingFlagFromSimRecords() {
        int i = 1;
        if (validEfCfis(this.mEfCfis)) {
            this.mCallForwardingStatus = this.mEfCfis[1] & 1;
            log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else if (this.mEfCff != null) {
            if ((this.mEfCff[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            log("EF_CFF: callForwardingEnabled=" + this.mCallForwardingStatus);
        } else {
            this.mCallForwardingStatus = -1;
            log("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
        }
    }

    protected void onAllRecordsLoaded() {
        log("record load complete");
        if (Resources.getSystem().getBoolean(17957043)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
        setVoiceCallForwardingFlagFromSimRecords();
        if (this.mParentApp.getState() == AppState.APPSTATE_PIN || this.mParentApp.getState() == AppState.APPSTATE_PUK) {
            this.mRecordsRequested = false;
            return;
        }
        String operator = getOperatorNumeric();
        if (TextUtils.isEmpty(operator)) {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        } else {
            log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
        }
        if (TextUtils.isEmpty(this.mImsi)) {
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        } else {
            try {
                this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(Integer.parseInt(this.mImsi.substring(0, 3))));
            } catch (RuntimeException exc) {
                logw("onAllRecordsLoaded: invalid IMSI with the exception ", exc);
            }
        }
        if (!VSimUtilsInner.isVSimSub(getSlotId())) {
            onAllRecordsLoadedHw();
        }
        VSimUtilsInner.setMarkForCardReload(getSlotId(), false);
        setVoiceMailByCountry(operator);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    private void handleCarrierNameOverride() {
        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configLoader == null || !configLoader.getConfig().getBoolean("carrier_name_override_bool")) {
            setSpnFromConfig(getOperatorNumeric());
            return;
        }
        String carrierName = configLoader.getConfig().getString("carrier_name_string");
        setServiceProviderName(carrierName);
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), carrierName);
    }

    private void setSpnFromConfig(String carrier) {
        if (this.mSpnOverride.containsCarrier(carrier)) {
            setServiceProviderName(this.mSpnOverride.getSpn(carrier));
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), getServiceProviderName());
        }
    }

    protected void setVoiceMailByCountry(String spn) {
        if (this.mVmConfig.containsCarrier(spn)) {
            this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn);
            this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumber(spn);
            this.mVoiceMailTag = this.mVmConfig.getVoiceMailTag(spn);
            this.mHwCustSIMRecords = (HwCustSIMRecords) HwCustUtils.createObj(HwCustSIMRecords.class, new Object[]{this.mContext});
            if (this.mHwCustSIMRecords != null && this.mHwCustSIMRecords.isOpenRoamingVoiceMail()) {
                this.mHwCustSIMRecords.registerRoamingState(this.mVoiceMailNum);
            }
        }
    }

    public void getForbiddenPlmns(Message response) {
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238273, storePendingResponseMessage(response)));
    }

    public void onReady() {
        fetchSimRecords();
    }

    private void onLocked() {
        log("only fetch EF_LI and EF_PL in lock state");
        loadEfLiAndEfPl();
    }

    public void onGetImsiDone(Message msg) {
        if (msg != null) {
            AsyncResult ar = msg.obj;
            if (ar.exception != null) {
                loge("Exception querying IMSI, Exception:" + ar.exception);
                return;
            }
            String mccmncCode;
            this.mImsi = (String) ar.result;
            refreshCardType();
            if (this.mImsi != null && (this.mImsi.length() < 6 || this.mImsi.length() > 15)) {
                loge("invalid IMSI ");
                this.mImsi = null;
            }
            if (this.mImsi != null) {
                try {
                    Integer.parseInt(this.mImsi.substring(0, 3));
                } catch (NumberFormatException e) {
                    loge("invalid numberic IMSI ");
                    this.mImsi = null;
                }
            }
            log("IMSI: mMncLength=" + this.mMncLength + ", mImsiLoad: " + this.mImsiLoad);
            if (this.mImsi != null) {
                log("IMSI: " + this.mImsi.substring(0, 6) + Rlog.pii(LOG_TAG, this.mImsi.substring(6)));
            }
            onImsiLoadedHw();
            updateSarMnc(this.mImsi);
            if ((this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                mccmncCode = this.mImsi.substring(0, 6);
                for (String mccmnc : MCCMNC_CODES_HAVING_3DIGITS_MNC) {
                    if (mccmnc.equals(mccmncCode)) {
                        this.mMncLength = 3;
                        log("IMSI: setting1 mMncLength=" + this.mMncLength);
                        break;
                    }
                }
            }
            if ((this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                String mcc = this.mImsi.substring(0, 3);
                if (mcc.equals("404") || mcc.equals("405")) {
                    mccmncCode = this.mImsi.substring(0, 5);
                    for (String mccmnc2 : MCCMNC_CODES_HAVING_2DIGITS_MNC) {
                        if (mccmnc2.equals(mccmncCode)) {
                            this.mMncLength = 2;
                            break;
                        }
                    }
                }
            }
            if (this.mMncLength == 0 && this.mImsi != null) {
                try {
                    String mccStr = this.mImsi.substring(0, 3);
                    if (mccStr.equals("404") || mccStr.equals("405")) {
                        this.mMncLength = 3;
                    } else {
                        this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(mccStr));
                    }
                } catch (NumberFormatException e2) {
                    this.mMncLength = 0;
                    Rlog.e(LOG_TAG, "SIMRecords: Corrupt IMSI!");
                }
            }
            adapterForDoubleRilChannelAfterImsiReady();
            this.mImsiReadyRegistrants.notifyRegistrants();
        }
    }

    private void loadEfLiAndEfPl() {
        if (this.mParentApp.getType() == AppType.APPTYPE_USIM) {
            this.mRecordsRequested = true;
            this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(100, new EfUsimLiLoaded(this, null)));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
            this.mRecordsToLoad++;
        }
    }

    private void loadCallForwardingRecords() {
        this.mRecordsRequested = true;
        this.mFh.loadEFLinearFixed(IccConstants.EF_CFIS, 1, obtainMessage(32));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CFF_CPHS, obtainMessage(24));
        this.mRecordsToLoad++;
    }

    protected void fetchSimRecords() {
        this.mRecordsRequested = true;
        log("fetchSimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad++;
        this.mCi.iccIOForApp(176, IccConstants.EF_AD, this.mFh.getEFPath(IccConstants.EF_AD), 0, 0, 4, null, null, this.mParentApp.getAid(), obtainMessage(9));
        this.mRecordsToLoad++;
        if (!getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(4));
            } else {
                this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
            }
            this.mRecordsToLoad++;
        }
        getPbrRecordSize();
        new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MBI, 1, obtainMessage(5));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_MWIS, 1, obtainMessage(7));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, obtainMessage(8));
        this.mRecordsToLoad++;
        loadCallForwardingRecords();
        this.mFh.loadEFTransparent(IccConstants.EF_SPDI, obtainMessage(13));
        this.mRecordsToLoad++;
        this.mFh.loadEFLinearFixed(IccConstants.EF_PNN, 1, obtainMessage(15));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_SST, obtainMessage(17));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_INFO_CPHS, obtainMessage(26));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID1, obtainMessage(34));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_GID2, obtainMessage(36));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_PLMN_W_ACT, obtainMessage(37));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_OPLMN_W_ACT, obtainMessage(38));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_HPLMN_W_ACT, obtainMessage(39));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_EHPLMN, obtainMessage(40));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238272, -1));
        this.mRecordsToLoad++;
        loadEons();
        loadGID1();
        loadEfLiAndEfPl();
        loadCardSpecialFile(IccConstants.EF_HPLMN);
        loadCardSpecialFile(IccConstants.EF_OCSGL);
        log("fetchSimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    public int getDisplayRule(String plmn) {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null && this.mParentApp.getUiccCard().getOperatorBrandOverride() != null) {
            return 2;
        }
        if (TextUtils.isEmpty(getServiceProviderName()) || this.mSpnDisplayCondition == -1) {
            return 2;
        }
        if (isOnMatchingPlmn(plmn)) {
            if ((this.mSpnDisplayCondition & 1) == 1) {
                return 3;
            }
            return 1;
        } else if ((this.mSpnDisplayCondition & 2) == 0) {
            return 3;
        } else {
            return 2;
        }
    }

    private boolean isOnMatchingPlmn(String plmn) {
        if (plmn == null) {
            return false;
        }
        if (plmn.equals(getOperatorNumeric())) {
            return true;
        }
        if (this.mSpdiNetworks != null) {
            for (String spdiNet : this.mSpdiNetworks) {
                if (plmn.equals(spdiNet)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getSpnFsm(boolean start, AsyncResult ar) {
        if (start) {
            if (this.mSpnState == GetSpnFsmState.READ_SPN_3GPP || this.mSpnState == GetSpnFsmState.READ_SPN_CPHS || this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS || this.mSpnState == GetSpnFsmState.INIT) {
                this.mSpnState = GetSpnFsmState.INIT;
                return;
            }
            this.mSpnState = GetSpnFsmState.INIT;
        }
        byte[] data;
        String spn;
        switch (-getcom-android-internal-telephony-uicc-SIMRecords$GetSpnFsmStateSwitchesValues()[this.mSpnState.ordinal()]) {
            case 1:
                setServiceProviderName(null);
                this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
                this.mRecordsToLoad++;
                this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
                break;
            case 2:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                } else {
                    data = ar.result;
                    this.mSpnDisplayCondition = data[0] & 255;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    } else {
                        log("Load EF_SPN: " + spn + " spnDisplayCondition: " + this.mSpnDisplayCondition);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    this.mSpnDisplayCondition = -1;
                    break;
                }
                break;
            case 3:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_CPHS: " + spn);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                    this.mRecordsToLoad++;
                    break;
                }
                break;
            case 4:
                if (ar == null || ar.exception != null) {
                    setServiceProviderName(null);
                    log("No SPN loaded in either CHPS or 3GPP");
                } else {
                    data = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 0, data.length));
                    spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        log("No SPN loaded in either CHPS or 3GPP");
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_SHORT_CPHS: " + spn);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                    }
                }
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
            default:
                this.mSpnState = GetSpnFsmState.IDLE;
                break;
        }
    }

    private void parseEfSpdi(byte[] data) {
        SimTlv tlv = new SimTlv(data, 0, data.length);
        byte[] plmnEntries = null;
        while (tlv.isValidObject()) {
            if (tlv.getTag() == 163) {
                tlv = new SimTlv(tlv.getData(), 0, tlv.getData().length);
            }
            if (tlv.getTag() == 128) {
                plmnEntries = tlv.getData();
                break;
            }
            tlv.nextObject();
        }
        if (plmnEntries != null) {
            this.mSpdiNetworks = new ArrayList(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.bcdToString(plmnEntries, i, 3);
                if (plmnCode.length() >= 5) {
                    log("EF_SPDI network: " + plmnCode);
                    this.mSpdiNetworks.add(plmnCode);
                }
            }
        }
    }

    private String[] parseBcdPlmnList(byte[] data, String description) {
        log("Received " + description + " PLMNs, raw=" + IccUtils.bytesToHexString(data));
        if (data.length == 0 || data.length % 3 != 0) {
            loge("Received invalid " + description + " PLMN list");
            return null;
        }
        int numPlmns = data.length / 3;
        String[] ret = new String[numPlmns];
        for (int i = 0; i < numPlmns; i++) {
            ret[i] = IccUtils.bcdPlmnToString(data, i * 3);
        }
        return ret;
    }

    private boolean isCphsMailboxEnabled() {
        boolean z = true;
        if (this.mCphsInfo == null) {
            return false;
        }
        if ((this.mCphsInfo[1] & 48) != 48) {
            z = false;
        }
        return z;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    protected void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, s, tr);
    }

    protected void logv(String s) {
        Rlog.v(LOG_TAG, s);
    }

    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[i * 2] == (byte) -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + data[(i * 2) + 1]);
                if ((data[(i * 2) + 1] & 128) == 128) {
                    this.mCspPlmnEnabled = true;
                } else {
                    this.mCspPlmnEnabled = false;
                    log("[CSP] Set Automatic Network Selection");
                    this.mNetworkSelectionModeAutomaticRegistrants.notifyRegistrants();
                }
                return;
            }
        }
        log("[CSP] Value Added Service Group (0xC0), not found!");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("SIMRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mVmConfig=" + this.mVmConfig);
        pw.println(" mSpnOverride=" + this.mSpnOverride);
        pw.println(" mCallForwardingStatus=" + this.mCallForwardingStatus);
        pw.println(" mSpnState=" + this.mSpnState);
        pw.println(" mCphsInfo=" + this.mCphsInfo);
        pw.println(" mCspPlmnEnabled=" + this.mCspPlmnEnabled);
        pw.println(" mEfMWIS[]=" + Arrays.toString(this.mEfMWIS));
        pw.println(" mEfCPHS_MWI[]=" + Arrays.toString(this.mEfCPHS_MWI));
        pw.println(" mEfCff[]=" + Arrays.toString(this.mEfCff));
        pw.println(" mEfCfis[]=" + Arrays.toString(this.mEfCfis));
        pw.println(" mSpnDisplayCondition=" + this.mSpnDisplayCondition);
        pw.println(" mSpdiNetworks[]=" + this.mSpdiNetworks);
        pw.println(" mPnnHomeName=" + this.mPnnHomeName);
        pw.println(" mUsimServiceTable=" + this.mUsimServiceTable);
        pw.println(" mGid1=" + this.mGid1);
        pw.println(" mGid2=" + this.mGid2);
        pw.println(" mPlmnActRecords[]=" + Arrays.toString(this.mPlmnActRecords));
        pw.println(" mOplmnActRecords[]=" + Arrays.toString(this.mOplmnActRecords));
        pw.println(" mHplmnActRecords[]=" + Arrays.toString(this.mHplmnActRecords));
        pw.println(" mFplmns[]=" + Arrays.toString(this.mFplmns));
        pw.println(" mEhplmns[]=" + Arrays.toString(this.mEhplmns));
        pw.flush();
    }

    private String getVmSimImsi() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        if (!sp.contains("sim_imsi_key" + getSlotId()) && sp.contains("vm_sim_imsi_key" + getSlotId())) {
            String imsi = sp.getString("vm_sim_imsi_key" + getSlotId(), null);
            if (!(imsi == null || mPasswordUtil == null)) {
                String oldDecodeVmSimImsi = mPasswordUtil.pswd2PlainText(imsi);
                try {
                    imsi = new String(Base64.decode(imsi, 0), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    Rlog.e(LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
                }
                if (imsi.equals(this.mImsi) || oldDecodeVmSimImsi.equals(this.mImsi)) {
                    imsi = this.mImsi;
                    Rlog.d(LOG_TAG, "getVmSimImsi: Old IMSI encryption is not supported, now setVmSimImsi again.");
                    setVmSimImsi(imsi);
                    Editor editor = sp.edit();
                    editor.remove("vm_sim_imsi_key" + getSlotId());
                    editor.commit();
                }
            }
        }
        String vmSimImsi = sp.getString("sim_imsi_key" + getSlotId(), null);
        if (vmSimImsi == null) {
            return vmSimImsi;
        }
        try {
            return new String(Base64.decode(vmSimImsi, 0), "utf-8");
        } catch (IllegalArgumentException e2) {
            Rlog.e(LOG_TAG, "getVmSimImsi IllegalArgumentException");
            return vmSimImsi;
        } catch (UnsupportedEncodingException e3) {
            Rlog.e(LOG_TAG, "getVmSimImsi UnsupportedEncodingException");
            return vmSimImsi;
        }
    }

    private void setVmSimImsi(String imsi) {
        try {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            editor.putString("sim_imsi_key" + getSlotId(), new String(Base64.encode(imsi.getBytes("utf-8"), 0), "utf-8"));
            editor.apply();
        } catch (UnsupportedEncodingException e) {
            Rlog.d(LOG_TAG, "setVmSimImsi UnsupportedEncodingException");
        }
    }

    private boolean getCallForwardingPreference() {
        boolean cf = PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean("cf_enabled_key" + getSlotId(), false);
        Rlog.d(LOG_TAG, "Get callforwarding info from perferences getSlotId()=" + getSlotId() + ",cf=" + cf);
        return cf;
    }

    private void setCallForwardingPreference(boolean enabled) {
        Rlog.d(LOG_TAG, "Set callforwarding info to perferences getSlotId()=" + getSlotId() + ",cf=" + enabled);
        Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        edit.putBoolean("cf_enabled_key" + getSlotId(), enabled);
        edit.commit();
        if (this.mImsi != null) {
            setVmSimImsi(this.mImsi);
        }
    }

    private void updateMccMncConfigWithGplmn(String operatorNumeric) {
        log("updateMccMncConfigWithGplmn: " + operatorNumeric);
        if (HwTelephonyFactory.getHwUiccManager().isCDMASimCard(this.mParentApp.getPhoneId())) {
            log("cdma card, ignore updateMccMncConfiguration");
        } else if (operatorNumeric != null && operatorNumeric.length() >= 5) {
            MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
        }
    }

    private void adapterForDoubleRilChannelAfterImsiReady() {
        if (this.mImsi != null && this.mMncLength != 0 && this.mMncLength != -1) {
            log("EVENT_GET_IMSI_DONE, update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
            updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
            if (!this.mImsiLoad) {
                log("EVENT_GET_IMSI_DONE, trigger notifyGetAdDone and onOperatorNumericLoadedHw.");
                setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                this.mImsiLoad = true;
                this.mParentApp.notifyGetAdDone(null);
                onOperatorNumericLoadedHw();
            }
        }
    }

    private void checkSendBroadcast(boolean oldCspPlmnEnabled, boolean CspPlmnEnabled) {
        if (SystemProperties.getBoolean("ro.config.csp_enable", false) && oldCspPlmnEnabled != CspPlmnEnabled) {
            Intent intent = new Intent("android.intent.action.ACTION_HW_CSP_PLMN_CHANGE");
            intent.addFlags(536870912);
            intent.putExtra("state", this.mCspPlmnEnabled);
            this.mContext.sendBroadcast(intent);
            log("Broadcast, CSP Plmn Enabled change to " + this.mCspPlmnEnabled);
        }
    }
}
