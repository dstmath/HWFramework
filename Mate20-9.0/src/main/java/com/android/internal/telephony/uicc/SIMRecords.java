package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import huawei.cust.HwGetCfgFileConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class SIMRecords extends IccRecords {
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
    private static final int EVENT_APP_NETWORK_LOCKED = 259;
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
    protected static final int EVENT_GET_SIM_MATCHED_FILE_DONE = 42;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SPDI_DONE = 13;
    private static final int EVENT_GET_SPN_DONE = 12;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE = 8;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SET_CPHS_MAILBOX_DONE = 25;
    private static final int EVENT_SET_MBDN_DONE = 20;
    private static final int EVENT_SET_MSISDN_DONE = 30;
    private static final int EVENT_SMS_ON_SIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    protected static final boolean HW_IS_SUPPORT_FAST_FETCH_SIMINFO = SystemProperties.getBoolean("ro.odm.radio.nvcfg_normalization", false);
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    protected static final String LOG_TAG = "SIMRecords";
    private static final String[] MCCMNC_CODES_HAVING_3DIGITS_MNC = {"302370", "302720", SimulatedCommands.FAKE_MCC_MNC, "405025", "405026", "405027", "405028", "405029", "405030", "405031", "405032", "405033", "405034", "405035", "405036", "405037", "405038", "405039", "405040", "405041", "405042", "405043", "405044", "405045", "405046", "405047", "405750", "405751", "405752", "405753", "405754", "405755", "405756", "405799", "405800", "405801", "405802", "405803", "405804", "405805", "405806", "405807", "405808", "405809", "405810", "405811", "405812", "405813", "405814", "405815", "405816", "405817", "405818", "405819", "405820", "405821", "405822", "405823", "405824", "405825", "405826", "405827", "405828", "405829", "405830", "405831", "405832", "405833", "405834", "405835", "405836", "405837", "405838", "405839", "405840", "405841", "405842", "405843", "405844", "405845", "405846", "405847", "405848", "405849", "405850", "405851", "405852", "405853", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874", "405875", "405876", "405877", "405878", "405879", "405880", "405881", "405882", "405883", "405884", "405885", "405886", "405908", "405909", "405910", "405911", "405912", "405913", "405914", "405915", "405916", "405917", "405918", "405919", "405920", "405921", "405922", "405923", "405924", "405925", "405926", "405927", "405928", "405929", "405930", "405931", "405932", "502142", "502143", "502145", "502146", "502147", "502148"};
    private static final boolean NEED_CHECK_MSISDN_BIT = SystemProperties.getBoolean("ro.config.hw_check_msisdn", false);
    private static final int SIM_RECORD_EVENT_BASE = 0;
    private static final int SYSTEM_EVENT_BASE = 256;
    static final int TAG_FULL_NETWORK_NAME = 67;
    static final int TAG_SHORT_NETWORK_NAME = 69;
    static final int TAG_SPDI = 163;
    static final int TAG_SPDI_PLMN_LIST = 128;
    private static final boolean VDBG = false;
    private int mCallForwardingStatus;
    private byte[] mCphsInfo = null;
    boolean mCspPlmnEnabled = true;
    byte[] mEfCPHS_MWI = null;
    byte[] mEfCff = null;
    byte[] mEfCfis = null;
    byte[] mEfLi = null;
    byte[] mEfMWIS = null;
    byte[] mEfPl = null;
    private String mFirstImsi;
    private HwCustSIMRecords mHwCustSIMRecords = null;
    private String mOriginVmImsi;
    private String mSecondImsi;
    ArrayList<String> mSpdiNetworks = null;
    int mSpnDisplayCondition;
    private GetSpnFsmState mSpnState;
    UsimServiceTable mUsimServiceTable;
    VoiceMailConstants mVmConfig;
    private HashMap<String, Integer> sEventIdMap = new HashMap<>();

    private class EfPlLoaded implements IccRecords.IccRecordLoaded {
        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords sIMRecords = SIMRecords.this;
            sIMRecords.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    private class EfUsimLiLoaded implements IccRecords.IccRecordLoaded {
        private EfUsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords sIMRecords = SIMRecords.this;
            sIMRecords.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    private enum GetSpnFsmState {
        IDLE,
        INIT,
        READ_SPN_3GPP,
        READ_SPN_CPHS,
        READ_SPN_SHORT_CPHS
    }

    public String toString() {
        return "SimRecords: " + super.toString() + " mVmConfig" + this.mVmConfig + " callForwardingEnabled=" + this.mCallForwardingStatus + " spnState=" + this.mSpnState + " mCphsInfo=" + this.mCphsInfo + " mCspPlmnEnabled=" + this.mCspPlmnEnabled + " efMWIS=" + this.mEfMWIS + " efCPHS_MWI=" + this.mEfCPHS_MWI + " mEfCff=" + this.mEfCff + " mEfCfis=" + this.mEfCfis + " getOperatorNumeric=" + getOperatorNumeric();
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = HwTelephonyFactory.getHwUiccManager().createHwAdnRecordCache(this.mFh);
        this.mVmConfig = (VoiceMailConstants) HwTelephonyFactory.getHwUiccManager().createHwVoiceMailConstants(c, getSlotId());
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        this.mCi.setOnSmsOnSim(this, 21, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 258, null);
        this.mParentApp.registerForNetworkLocked(this, 259, null);
        log("SIMRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mParentApp.unregisterForNetworkLocked(this);
        resetRecords();
        super.dispose();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        log("finalized");
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
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
        this.mLockedRecordsReqReason = 0;
        this.mLoaded.set(false);
        this.mImsiLoad = false;
    }

    public String getMsisdnNumber() {
        if (!NEED_CHECK_MSISDN_BIT || this.mUsimServiceTable == null || this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.MSISDN)) {
            return this.mMsisdn;
        }
        log("EF_MSISDN not available");
        return null;
    }

    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    private int getExtFromEf(int ef) {
        if (ef != 28480) {
            return IccConstants.EF_EXT1;
        }
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            return IccConstants.EF_EXT5;
        }
        return IccConstants.EF_EXT1;
    }

    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    public String getMsisdnAlphaTag() {
        if (!NEED_CHECK_MSISDN_BIT || this.mUsimServiceTable == null || this.mUsimServiceTable.isAvailable(UsimServiceTable.UsimService.MSISDN)) {
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
        Message message = onComplete;
        if (this.mIsVoiceMailFixed) {
            AsyncResult.forMessage(onComplete).exception = new IccVmFixedException("Voicemail number is fixed by operator");
            onComplete.sendToTarget();
            return;
        }
        this.mNewVoiceMailNum = voiceNumber;
        this.mNewVoiceMailTag = alphaTag;
        boolean custvmNotToSim = false;
        Boolean editvmnottosim = (Boolean) HwCfgFilePolicy.getValue("vm_edit_not_to_sim_bool", SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getPhoneId()), Boolean.class);
        if (editvmnottosim != null) {
            custvmNotToSim = editvmnottosim.booleanValue();
        }
        if (custvmNotToSim) {
            log("Don't need to edit to SIM");
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Voicemail number can't be edit to SIM");
            onComplete.sendToTarget();
            return;
        }
        AdnRecord adn = new AdnRecord(this.mNewVoiceMailTag, this.mNewVoiceMailNum);
        if (this.mMailboxIndex != 0 && this.mMailboxIndex != 255) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, null, obtainMessage(20, message));
        } else if (isCphsMailboxEnabled()) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, message));
        } else {
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
            onComplete.sendToTarget();
        }
    }

    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (line == 1) {
            try {
                if (this.mEfMWIS != null) {
                    this.mEfMWIS[0] = (byte) ((this.mEfMWIS[0] & 254) | (countWaiting == 0 ? (byte) 0 : 1));
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mEfCPHS_MWI != null) {
                    this.mEfCPHS_MWI[0] = (byte) ((this.mEfCPHS_MWI[0] & 240) | (countWaiting == 0 ? (byte) 5 : 10));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    private boolean validEfCfis(byte[] data) {
        if (data != null) {
            if (data[0] < 1 || data[0] > 4) {
                logw("MSP byte: " + data[0] + " is not between 1 and 4", null);
            }
            for (byte b : data) {
                if (b != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getVoiceMessageCount() {
        int countVoiceMessages = -2;
        boolean z = false;
        if (this.mEfMWIS != null) {
            if ((this.mEfMWIS[0] & 1) != 0) {
                z = true;
            }
            boolean voiceMailWaiting = z;
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (voiceMailWaiting && (countVoiceMessages == 0 || countVoiceMessages == 255)) {
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
        if (line == 1) {
            this.mCallForwardingStatus = enable ? 1 : 0;
            this.mRecordsEventsRegistrants.notifyResult(1);
            try {
                if (validEfCfis(this.mEfCfis)) {
                    if (enable) {
                        byte[] bArr = this.mEfCfis;
                        bArr[1] = (byte) (bArr[1] | 1);
                    } else {
                        byte[] bArr2 = this.mEfCfis;
                        bArr2[1] = (byte) (bArr2[1] & 254);
                    }
                    log("setVoiceCallForwardingFlag: enable=" + enable + " mEfCfis=" + IccUtils.bytesToHexString(this.mEfCfis));
                    if (enable && !TextUtils.isEmpty(dialNumber)) {
                        logv("EF_CFIS: updating cf number, " + Rlog.pii(LOG_TAG, dialNumber));
                        byte[] bcdNumber = PhoneNumberUtils.numberToCalledPartyBCD(dialNumber, 1);
                        System.arraycopy(bcdNumber, 0, this.mEfCfis, 3, bcdNumber.length);
                        this.mEfCfis[2] = (byte) bcdNumber.length;
                        this.mEfCfis[14] = -1;
                        this.mEfCfis[15] = -1;
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
            } catch (RuntimeException e) {
                loge("Error saving call forwarding flag to SIM. Probably malformed dialNumber");
            }
        }
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    public String getOperatorNumeric() {
        String imsi = getIMSI();
        if (this.mImsi == null) {
            log("IMSI == null");
            return null;
        } else if (this.mImsi.length() < 6 || this.mImsi.length() > 15) {
            Rlog.e(LOG_TAG, "invalid IMSI ");
            return null;
        } else if (this.mMncLength == -1 || this.mMncLength == 0) {
            log("getSIMOperatorNumeric: bad mncLength");
            if (this.mImsi.length() >= 5) {
                String mcc = this.mImsi.substring(0, 3);
                if (mcc.equals("404") || mcc.equals("405") || mcc.equals("232")) {
                    String mccmncCode = this.mImsi.substring(0, 5);
                    for (String mccmnc : MCCMNC_CODES_HAVING_2DIGITS_MNC) {
                        if (mccmnc.equals(mccmncCode)) {
                            this.mMncLength = 2;
                            return this.mImsi.substring(0, 3 + this.mMncLength);
                        }
                    }
                }
            }
            return null;
        } else if (imsi.length() >= this.mMncLength + 3) {
            return imsi.substring(0, 3 + this.mMncLength);
        } else {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:785:0x1392, code lost:
        if (r3 != false) goto L_0x1394;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:786:0x1394, code lost:
        onRecordLoaded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:792:0x13a0, code lost:
        if (r3 == false) goto L_0x13a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:793:0x13a3, code lost:
        return;
     */
    public void handleMessage(Message msg) {
        byte[] data;
        int mcc;
        int mcc2;
        int i;
        int mcc3;
        int slotId;
        int mcc4;
        int mcc5;
        int mcc6;
        byte[] data2;
        Message message = msg;
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message[" + message.what + "], Ignoring.");
            return;
        }
        try {
            int i2 = message.what;
            if (i2 == 1) {
                onReady();
            } else if (i2 != 30) {
                int i3 = 0;
                switch (i2) {
                    case 3:
                        isRecordLoadResponse = true;
                        onGetImsiDone(msg);
                        break;
                    case 4:
                        isRecordLoadResponse = true;
                        AsyncResult ar = (AsyncResult) message.obj;
                        byte[] data3 = (byte[]) ar.result;
                        if (ar.exception == null) {
                            this.mIccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data3, 0, data3.length);
                            this.mFullIccId = IccUtils.bchToString(data3, 0, data3.length);
                            onIccIdLoadedHw();
                            log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                            this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                            break;
                        } else {
                            this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                            break;
                        }
                    case 5:
                        isRecordLoadResponse = true;
                        AsyncResult ar2 = (AsyncResult) message.obj;
                        byte[] data4 = (byte[]) ar2.result;
                        boolean isValidMbdn = false;
                        if (ar2.exception == null) {
                            log("EF_MBI: " + IccUtils.bytesToHexString(data4));
                            this.mMailboxIndex = data4[0] & 255;
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
                        AsyncResult ar3 = (AsyncResult) message.obj;
                        if (ar3.exception == null) {
                            AdnRecord adn = (AdnRecord) ar3.result;
                            StringBuilder sb = new StringBuilder();
                            sb.append("VM: ");
                            sb.append(adn);
                            sb.append(message.what == 11 ? " EF[MAILBOX]" : " EF[MBDN]");
                            log(sb.toString());
                            if (adn.isEmpty() && message.what == 6) {
                                this.mRecordsToLoad++;
                                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                                break;
                            } else {
                                this.mVoiceMailNum = adn.getNumber();
                                this.mVoiceMailTag = adn.getAlphaTag();
                                this.mVmConfig.setVoicemailOnSIM(this.mVoiceMailNum, this.mVoiceMailTag);
                                break;
                            }
                        } else {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("Invalid or missing EF");
                            sb2.append(message.what == 11 ? "[MAILBOX]" : "[MBDN]");
                            log(sb2.toString());
                            if (message.what == 6) {
                                this.mRecordsToLoad++;
                                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                                break;
                            }
                        }
                        break;
                    case 7:
                        isRecordLoadResponse = true;
                        AsyncResult ar4 = (AsyncResult) message.obj;
                        byte[] data5 = (byte[]) ar4.result;
                        log("EF_MWIS : " + IccUtils.bytesToHexString(data5));
                        if (ar4.exception == null) {
                            if ((data5[0] & 255) != 255) {
                                if (SystemProperties.getBoolean("ro.config.hw_eeVoiceMsgCount", false) && (data5[0] & 255) == 0) {
                                    this.mEfMWIS = null;
                                    Rlog.d(LOG_TAG, "SIMRecords EE VoiceMessageCount from SIM CPHS");
                                    break;
                                } else {
                                    this.mEfMWIS = data5;
                                    break;
                                }
                            } else {
                                log("SIMRecords: Uninitialized record MWIS");
                                break;
                            }
                        } else {
                            log("EVENT_GET_MWIS_DONE exception = " + ar4.exception);
                            break;
                        }
                        break;
                    case 8:
                        isRecordLoadResponse = true;
                        AsyncResult ar5 = (AsyncResult) message.obj;
                        log("EF_CPHS_MWI: " + IccUtils.bytesToHexString((byte[]) ar5.result));
                        if (ar5.exception == null) {
                            this.mEfCPHS_MWI = data;
                            break;
                        } else {
                            log("EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = " + ar5.exception);
                            break;
                        }
                    case 9:
                        isRecordLoadResponse = true;
                        try {
                            if (!this.mCarrierTestOverride.isInTestMode() || getIMSI() == null) {
                                AsyncResult ar6 = (AsyncResult) message.obj;
                                IccIoResult result = (IccIoResult) ar6.result;
                                IccException iccException = result.getException();
                                if (ar6.exception == null) {
                                    if (iccException == null) {
                                        if (result.payload == null) {
                                            log("result.payload is null");
                                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                String mccmncCode = this.mImsi.substring(0, 6);
                                                String[] strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                int length = strArr.length;
                                                int i4 = 0;
                                                while (true) {
                                                    if (i4 < length) {
                                                        if (strArr[i4].equals(mccmncCode)) {
                                                            this.mMncLength = 3;
                                                            log("setting6 mMncLength=" + this.mMncLength);
                                                        } else {
                                                            i4++;
                                                        }
                                                    }
                                                }
                                            }
                                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                String mcc7 = this.mImsi.substring(0, 3);
                                                if (mcc7.equals("404") || mcc7.equals("405")) {
                                                    String mccmncCode2 = this.mImsi.substring(0, 5);
                                                    String[] strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                    int length2 = strArr2.length;
                                                    int i5 = 0;
                                                    while (true) {
                                                        if (i5 < length2) {
                                                            if (strArr2[i5].equals(mccmncCode2)) {
                                                                this.mMncLength = 2;
                                                            } else {
                                                                i5++;
                                                            }
                                                        }
                                                    }
                                                }
                                                custMncLength(this.mImsi.substring(0, 3));
                                            }
                                            if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                if (this.mImsi != null) {
                                                    try {
                                                        String mccStr = this.mImsi.substring(0, 3);
                                                        if (!mccStr.equals("404")) {
                                                            if (!mccStr.equals("405")) {
                                                                Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc6);
                                                                this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc6);
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
                                            if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                                log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                                setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                                updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
                                            }
                                            if (!(this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1)) {
                                                i3 = 1;
                                            }
                                            if (i3 != 0 && !this.mImsiLoad) {
                                                this.mImsiLoad = true;
                                                this.mParentApp.notifyGetAdDone(null);
                                                onOperatorNumericLoadedHw();
                                            }
                                            i = getSlotId();
                                            initFdnPsStatus(i);
                                            break;
                                        } else {
                                            byte[] data6 = result.payload;
                                            log("EF_AD: " + IccUtils.bytesToHexString(data6));
                                            if (data6.length < 3) {
                                                log("Corrupt AD data on SIM");
                                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                    String mccmncCode3 = this.mImsi.substring(0, 6);
                                                    String[] strArr3 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                    int length3 = strArr3.length;
                                                    int i6 = 0;
                                                    while (true) {
                                                        if (i6 < length3) {
                                                            if (strArr3[i6].equals(mccmncCode3)) {
                                                                this.mMncLength = 3;
                                                                log("setting6 mMncLength=" + this.mMncLength);
                                                            } else {
                                                                i6++;
                                                            }
                                                        }
                                                    }
                                                }
                                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                    String mcc8 = this.mImsi.substring(0, 3);
                                                    if (mcc8.equals("404") || mcc8.equals("405")) {
                                                        String mccmncCode4 = this.mImsi.substring(0, 5);
                                                        String[] strArr4 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                        int length4 = strArr4.length;
                                                        int i7 = 0;
                                                        while (true) {
                                                            if (i7 < length4) {
                                                                if (strArr4[i7].equals(mccmncCode4)) {
                                                                    this.mMncLength = 2;
                                                                } else {
                                                                    i7++;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    custMncLength(this.mImsi.substring(0, 3));
                                                }
                                                if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                    if (this.mImsi != null) {
                                                        try {
                                                            String mccStr2 = this.mImsi.substring(0, 3);
                                                            if (!mccStr2.equals("404")) {
                                                                if (!mccStr2.equals("405")) {
                                                                    Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc5);
                                                                    this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc5);
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
                                                if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                                    log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                                    setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                    setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                    log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                                    updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
                                                }
                                                if (!(this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1)) {
                                                    i3 = 1;
                                                }
                                                if (i3 != 0 && !this.mImsiLoad) {
                                                    this.mImsiLoad = true;
                                                    this.mParentApp.notifyGetAdDone(null);
                                                    onOperatorNumericLoadedHw();
                                                }
                                                slotId = getSlotId();
                                            } else if (data6.length == 3) {
                                                log("MNC length not present in EF_AD");
                                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                    String mccmncCode5 = this.mImsi.substring(0, 6);
                                                    String[] strArr5 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                    int length5 = strArr5.length;
                                                    int i8 = 0;
                                                    while (true) {
                                                        if (i8 < length5) {
                                                            if (strArr5[i8].equals(mccmncCode5)) {
                                                                this.mMncLength = 3;
                                                                log("setting6 mMncLength=" + this.mMncLength);
                                                            } else {
                                                                i8++;
                                                            }
                                                        }
                                                    }
                                                }
                                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                    String mcc9 = this.mImsi.substring(0, 3);
                                                    if (mcc9.equals("404") || mcc9.equals("405")) {
                                                        String mccmncCode6 = this.mImsi.substring(0, 5);
                                                        String[] strArr6 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                        int length6 = strArr6.length;
                                                        int i9 = 0;
                                                        while (true) {
                                                            if (i9 < length6) {
                                                                if (strArr6[i9].equals(mccmncCode6)) {
                                                                    this.mMncLength = 2;
                                                                } else {
                                                                    i9++;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    custMncLength(this.mImsi.substring(0, 3));
                                                }
                                                if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                    if (this.mImsi != null) {
                                                        try {
                                                            String mccStr3 = this.mImsi.substring(0, 3);
                                                            if (!mccStr3.equals("404")) {
                                                                if (!mccStr3.equals("405")) {
                                                                    Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc4);
                                                                    this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc4);
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
                                                if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                                    log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                                    setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                    setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                    log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                                    updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
                                                }
                                                if (!(this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1)) {
                                                    i3 = 1;
                                                }
                                                if (i3 != 0 && !this.mImsiLoad) {
                                                    this.mImsiLoad = true;
                                                    this.mParentApp.notifyGetAdDone(null);
                                                    onOperatorNumericLoadedHw();
                                                }
                                                slotId = getSlotId();
                                            } else {
                                                this.mMncLength = data6[3] & 15;
                                                log("setting4 mMncLength=" + this.mMncLength);
                                            }
                                            initFdnPsStatus(slotId);
                                        }
                                    }
                                }
                                log("read EF_AD exception occurs");
                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                    String mccmncCode7 = this.mImsi.substring(0, 6);
                                    String[] strArr7 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                    int length7 = strArr7.length;
                                    int i10 = 0;
                                    while (true) {
                                        if (i10 < length7) {
                                            if (strArr7[i10].equals(mccmncCode7)) {
                                                this.mMncLength = 3;
                                                log("setting6 mMncLength=" + this.mMncLength);
                                            } else {
                                                i10++;
                                            }
                                        }
                                    }
                                }
                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                    String mcc10 = this.mImsi.substring(0, 3);
                                    if (mcc10.equals("404") || mcc10.equals("405")) {
                                        String mccmncCode8 = this.mImsi.substring(0, 5);
                                        String[] strArr8 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                        int length8 = strArr8.length;
                                        int i11 = 0;
                                        while (true) {
                                            if (i11 < length8) {
                                                if (strArr8[i11].equals(mccmncCode8)) {
                                                    this.mMncLength = 2;
                                                } else {
                                                    i11++;
                                                }
                                            }
                                        }
                                    }
                                    custMncLength(this.mImsi.substring(0, 3));
                                }
                                if (this.mMncLength == 0 || this.mMncLength == -1) {
                                    if (this.mImsi != null) {
                                        try {
                                            String mccStr4 = this.mImsi.substring(0, 3);
                                            if (!mccStr4.equals("404")) {
                                                if (!mccStr4.equals("405")) {
                                                    Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc3);
                                                    this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc3);
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
                                if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                    log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                    setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                    setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                    log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                    updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
                                }
                                if (!(this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1)) {
                                    i3 = 1;
                                }
                                if (i3 != 0 && !this.mImsiLoad) {
                                    this.mImsiLoad = true;
                                    this.mParentApp.notifyGetAdDone(null);
                                    onOperatorNumericLoadedHw();
                                }
                                i = getSlotId();
                                initFdnPsStatus(i);
                            } else {
                                this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(getIMSI().substring(0, 3)));
                                log("[TestMode] mMncLength=" + this.mMncLength);
                            }
                        } catch (NumberFormatException e5) {
                            this.mMncLength = 0;
                            loge("[TestMode] Corrupt IMSI! mMncLength=" + this.mMncLength);
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                String mccmncCode9 = this.mImsi.substring(0, 6);
                                String[] strArr9 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                int length9 = strArr9.length;
                                int i12 = 0;
                                while (true) {
                                    if (i12 < length9) {
                                        if (strArr9[i12].equals(mccmncCode9)) {
                                            this.mMncLength = 3;
                                            log("setting6 mMncLength=" + this.mMncLength);
                                        } else {
                                            i12++;
                                        }
                                    }
                                }
                            }
                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                String mcc11 = this.mImsi.substring(0, 3);
                                if (mcc11.equals("404") || mcc11.equals("405")) {
                                    String mccmncCode10 = this.mImsi.substring(0, 5);
                                    String[] strArr10 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                    int length10 = strArr10.length;
                                    int i13 = 0;
                                    while (true) {
                                        if (i13 < length10) {
                                            if (strArr10[i13].equals(mccmncCode10)) {
                                                this.mMncLength = 2;
                                            } else {
                                                i13++;
                                            }
                                        }
                                    }
                                }
                                custMncLength(this.mImsi.substring(0, 3));
                            }
                            if (this.mMncLength == 0 || this.mMncLength == -1) {
                                if (this.mImsi != null) {
                                    try {
                                        String mccStr5 = this.mImsi.substring(0, 3);
                                        if (!mccStr5.equals("404")) {
                                            if (!mccStr5.equals("405")) {
                                                Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc);
                                                this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc);
                                            }
                                        }
                                        this.mMncLength = 3;
                                    } catch (NumberFormatException e6) {
                                        this.mMncLength = 0;
                                        loge("Corrupt IMSI!");
                                    }
                                } else {
                                    this.mMncLength = 0;
                                    log("MNC length not present in EF_AD");
                                }
                            }
                            if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                                setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                                updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
                            }
                            if (!(this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1)) {
                                i3 = 1;
                            }
                            if (i3 != 0 && !this.mImsiLoad) {
                                this.mImsiLoad = true;
                                this.mParentApp.notifyGetAdDone(null);
                                onOperatorNumericLoadedHw();
                            }
                            initFdnPsStatus(getSlotId());
                            throw th2;
                        }
                        if (this.mMncLength == 15) {
                            this.mMncLength = 0;
                            log("setting5 mMncLength=" + this.mMncLength);
                        } else if (this.mMncLength > 3) {
                            this.mMncLength = 2;
                        } else if (!(this.mMncLength == 2 || this.mMncLength == 3)) {
                            this.mMncLength = -1;
                            log("setting5 mMncLength=" + this.mMncLength);
                        }
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                            String mccmncCode11 = this.mImsi.substring(0, 6);
                            String[] strArr11 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                            int length11 = strArr11.length;
                            int i14 = 0;
                            while (true) {
                                if (i14 < length11) {
                                    if (strArr11[i14].equals(mccmncCode11)) {
                                        this.mMncLength = 3;
                                        log("setting6 mMncLength=" + this.mMncLength);
                                    } else {
                                        i14++;
                                    }
                                }
                            }
                        }
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                            String mcc12 = this.mImsi.substring(0, 3);
                            if (mcc12.equals("404") || mcc12.equals("405")) {
                                String mccmncCode12 = this.mImsi.substring(0, 5);
                                String[] strArr12 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                int length12 = strArr12.length;
                                int i15 = 0;
                                while (true) {
                                    if (i15 < length12) {
                                        if (strArr12[i15].equals(mccmncCode12)) {
                                            this.mMncLength = 2;
                                        } else {
                                            i15++;
                                        }
                                    }
                                }
                            }
                            custMncLength(this.mImsi.substring(0, 3));
                        }
                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                            if (this.mImsi != null) {
                                try {
                                    String mccStr6 = this.mImsi.substring(0, 3);
                                    if (!mccStr6.equals("404")) {
                                        if (!mccStr6.equals("405")) {
                                            Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc2);
                                            this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc2);
                                        }
                                    }
                                    this.mMncLength = 3;
                                } catch (NumberFormatException e7) {
                                    this.mMncLength = 0;
                                    loge("Corrupt IMSI!");
                                }
                            } else {
                                this.mMncLength = 0;
                                log("MNC length not present in EF_AD");
                            }
                        }
                        if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                            log("GET_AD_DONE setSystemProperty simOperator=" + getOperatorNumeric());
                            setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                            setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                            log("update mccmnc=" + this.mImsi.substring(0, this.mMncLength + 3));
                            updateMccMncConfigWithGplmn(this.mImsi.substring(0, 3 + this.mMncLength));
                        }
                        if (!(this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1)) {
                            i3 = 1;
                        }
                        if (i3 != 0 && !this.mImsiLoad) {
                            this.mImsiLoad = true;
                            this.mParentApp.notifyGetAdDone(null);
                            onOperatorNumericLoadedHw();
                        }
                        initFdnPsStatus(getSlotId());
                        break;
                    case 10:
                        isRecordLoadResponse = true;
                        AsyncResult ar7 = (AsyncResult) message.obj;
                        if (ar7.exception == null) {
                            AdnRecord adn2 = (AdnRecord) ar7.result;
                            this.mMsisdn = adn2.getNumber();
                            this.mMsisdnTag = adn2.getAlphaTag();
                            log("MSISDN isempty:" + TextUtils.isEmpty(this.mMsisdn));
                            break;
                        } else {
                            log("Invalid or missing EF[MSISDN]");
                            break;
                        }
                    case 12:
                        isRecordLoadResponse = true;
                        getSpnFsm(false, (AsyncResult) message.obj);
                        if (GetSpnFsmState.IDLE == this.mSpnState) {
                            updateCarrierFile(getSlotId(), 7, getServiceProviderName());
                        }
                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                            String mcc13 = this.mImsi.substring(0, 3);
                            if (mcc13.equals("404") || mcc13.equals("405")) {
                                String mccmncCode13 = this.mImsi.substring(0, 5);
                                if (!TextUtils.isEmpty(getServiceProviderName()) && getServiceProviderName().toLowerCase(Locale.US).contains("reliance")) {
                                    String[] strArr13 = MCCMNC_CODES_HAVING_2DIGITS_MNC_ZERO_PREFIX_RELIANCE;
                                    int length13 = strArr13.length;
                                    while (true) {
                                        if (i3 < length13) {
                                            if (strArr13[i3].equals(mccmncCode13)) {
                                                this.mMncLength = 2;
                                            } else {
                                                i3++;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    case 13:
                        isRecordLoadResponse = true;
                        AsyncResult ar8 = (AsyncResult) message.obj;
                        byte[] data7 = (byte[]) ar8.result;
                        if (ar8.exception == null) {
                            parseEfSpdi(data7);
                            break;
                        } else {
                            break;
                        }
                    case 14:
                        AsyncResult ar9 = (AsyncResult) message.obj;
                        if (ar9.exception != null) {
                            logw("update failed. ", ar9.exception);
                            break;
                        }
                        break;
                    case 15:
                        isRecordLoadResponse = true;
                        AsyncResult ar10 = (AsyncResult) message.obj;
                        byte[] data8 = (byte[]) ar10.result;
                        if (ar10.exception != null) {
                            break;
                        } else {
                            SimTlv tlv = new SimTlv(data8, 0, data8.length);
                            while (true) {
                                if (!tlv.isValidObject()) {
                                    break;
                                } else if (tlv.getTag() == 67) {
                                    this.mPnnHomeName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                                    log("PNN: " + this.mPnnHomeName);
                                    break;
                                } else {
                                    tlv.nextObject();
                                }
                            }
                        }
                    default:
                        switch (i2) {
                            case 17:
                                isRecordLoadResponse = true;
                                AsyncResult ar11 = (AsyncResult) message.obj;
                                byte[] data9 = (byte[]) ar11.result;
                                if (ar11.exception == null) {
                                    this.mUsimServiceTable = new UsimServiceTable(data9);
                                    log("SST: " + this.mUsimServiceTable);
                                    if (checkFileInServiceTable(IccConstants.EF_SPN, this.mUsimServiceTable, data9)) {
                                        getSpnFsm(true, null);
                                    } else {
                                        updateCarrierFile(getSlotId(), 7, null);
                                    }
                                    checkFileInServiceTable(IccConstants.EF_PNN, this.mUsimServiceTable, data9);
                                    break;
                                } else {
                                    updateCarrierFile(getSlotId(), 7, null);
                                    break;
                                }
                            case 18:
                                isRecordLoadResponse = true;
                                AsyncResult ar12 = (AsyncResult) message.obj;
                                if (ar12.exception == null) {
                                    handleSmses((ArrayList) ar12.result);
                                    break;
                                } else {
                                    break;
                                }
                            case 19:
                                Rlog.i("ENF", "marked read: sms " + message.arg1);
                                break;
                            case 20:
                                isRecordLoadResponse = false;
                                AsyncResult ar13 = (AsyncResult) message.obj;
                                log("EVENT_SET_MBDN_DONE ex:" + ar13.exception);
                                if (ar13.exception == null) {
                                    this.mVoiceMailNum = this.mNewVoiceMailNum;
                                    this.mVoiceMailTag = this.mNewVoiceMailTag;
                                }
                                if (!isCphsMailboxEnabled()) {
                                    if (ar13.userObj != null) {
                                        CarrierConfigManager configLoader = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                                        if (ar13.exception == null || configLoader == null || !configLoader.getConfig().getBoolean("editable_voicemail_number_bool")) {
                                            AsyncResult.forMessage((Message) ar13.userObj).exception = ar13.exception;
                                        } else {
                                            AsyncResult.forMessage((Message) ar13.userObj).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
                                        }
                                        ((Message) ar13.userObj).sendToTarget();
                                        break;
                                    }
                                } else {
                                    AdnRecord adn3 = new AdnRecord(this.mVoiceMailTag, this.mVoiceMailNum);
                                    Message onCphsCompleted = (Message) ar13.userObj;
                                    if (ar13.exception == null && ar13.userObj != null) {
                                        AsyncResult.forMessage((Message) ar13.userObj).exception = null;
                                        ((Message) ar13.userObj).sendToTarget();
                                        log("Callback with MBDN successful.");
                                        onCphsCompleted = null;
                                    }
                                    new AdnRecordLoader(this.mFh).updateEF(adn3, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onCphsCompleted));
                                    break;
                                }
                                break;
                            case 21:
                                isRecordLoadResponse = false;
                                AsyncResult ar14 = (AsyncResult) message.obj;
                                Integer index = (Integer) ar14.result;
                                if (ar14.exception == null) {
                                    if (index != null) {
                                        log("READ EF_SMS RECORD index=" + index);
                                        this.mFh.loadEFLinearFixed(IccConstants.EF_SMS, index.intValue(), obtainMessage(22));
                                        break;
                                    }
                                }
                                loge("Error on SMS_ON_SIM with exp " + ar14.exception + " index " + index);
                                break;
                            case 22:
                                isRecordLoadResponse = false;
                                AsyncResult ar15 = (AsyncResult) message.obj;
                                if (ar15.exception != null) {
                                    loge("Error on GET_SMS with exp " + ar15.exception);
                                    break;
                                } else {
                                    handleSms((byte[]) ar15.result);
                                    break;
                                }
                            default:
                                switch (i2) {
                                    case 24:
                                        isRecordLoadResponse = true;
                                        AsyncResult ar16 = (AsyncResult) message.obj;
                                        byte[] data10 = (byte[]) ar16.result;
                                        if (ar16.exception == null) {
                                            log("EF_CFF_CPHS: " + IccUtils.bytesToHexString(data10));
                                            this.mEfCff = data10;
                                            break;
                                        } else {
                                            this.mEfCff = null;
                                            break;
                                        }
                                    case 25:
                                        isRecordLoadResponse = false;
                                        AsyncResult ar17 = (AsyncResult) message.obj;
                                        if (ar17.exception == null) {
                                            this.mVoiceMailNum = this.mNewVoiceMailNum;
                                            this.mVoiceMailTag = this.mNewVoiceMailTag;
                                        } else {
                                            log("Set CPHS MailBox with exception: " + ar17.exception);
                                        }
                                        if (ar17.userObj != null) {
                                            log("Callback with CPHS MB successful.");
                                            AsyncResult.forMessage((Message) ar17.userObj).exception = ar17.exception;
                                            ((Message) ar17.userObj).sendToTarget();
                                            break;
                                        }
                                        break;
                                    case 26:
                                        isRecordLoadResponse = true;
                                        AsyncResult ar18 = (AsyncResult) message.obj;
                                        if (ar18.exception == null) {
                                            this.mCphsInfo = (byte[]) ar18.result;
                                            log("iCPHS: " + IccUtils.bytesToHexString(this.mCphsInfo));
                                            break;
                                        } else {
                                            break;
                                        }
                                    default:
                                        switch (i2) {
                                            case 32:
                                                isRecordLoadResponse = true;
                                                AsyncResult ar19 = (AsyncResult) message.obj;
                                                byte[] data11 = (byte[]) ar19.result;
                                                if (ar19.exception == null) {
                                                    log("EF_CFIS: " + IccUtils.bytesToHexString(data11));
                                                    if (!validEfCfis(data11)) {
                                                        log("EF_CFIS: " + IccUtils.bytesToHexString(data11));
                                                        break;
                                                    } else {
                                                        this.mEfCfis = data11;
                                                        if ((data11[1] & 1) != 0) {
                                                            i3 = 1;
                                                        }
                                                        this.mCallForwardingStatus = i3;
                                                        log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
                                                        this.mRecordsEventsRegistrants.notifyResult(1);
                                                        break;
                                                    }
                                                } else {
                                                    this.mEfCfis = null;
                                                    String imsiOld = getVmSimImsi();
                                                    if (this.mOriginVmImsi == null) {
                                                        this.mOriginVmImsi = imsiOld;
                                                    }
                                                    if (imsiOld != null && imsiOld.equals(this.mImsi)) {
                                                        if (getCallForwardingPreference()) {
                                                            this.mCallForwardingStatus = 1;
                                                            this.mRecordsEventsRegistrants.notifyResult(1);
                                                            break;
                                                        }
                                                    } else if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                                                        setCallForwardingPreference(false);
                                                        break;
                                                    } else {
                                                        if (this.mFirstImsi == null) {
                                                            this.mFirstImsi = this.mImsi;
                                                        } else if (!this.mFirstImsi.equals(this.mImsi) && this.mSecondImsi == null) {
                                                            this.mSecondImsi = this.mImsi;
                                                        }
                                                        if (!(this.mOriginVmImsi == null || this.mFirstImsi == null || this.mOriginVmImsi.equals(this.mFirstImsi) || this.mSecondImsi == null || this.mOriginVmImsi.equals(this.mSecondImsi))) {
                                                            setCallForwardingPreference(false);
                                                            break;
                                                        }
                                                    }
                                                }
                                                break;
                                            case 33:
                                                isRecordLoadResponse = true;
                                                AsyncResult ar20 = (AsyncResult) message.obj;
                                                if (ar20.exception == null) {
                                                    log("EF_CSP: " + IccUtils.bytesToHexString((byte[]) ar20.result));
                                                    boolean oldCspPlmnEnabled = this.mCspPlmnEnabled;
                                                    handleEfCspData(data2);
                                                    sendCspChangedBroadcast(oldCspPlmnEnabled, this.mCspPlmnEnabled);
                                                    break;
                                                } else {
                                                    loge("Exception in fetching EF_CSP data " + ar20.exception);
                                                    break;
                                                }
                                            case 34:
                                                isRecordLoadResponse = true;
                                                AsyncResult ar21 = (AsyncResult) message.obj;
                                                byte[] data12 = (byte[]) ar21.result;
                                                if (ar21.exception == null) {
                                                    this.mGid1 = IccUtils.bytesToHexString(data12);
                                                    log("GID1: " + this.mGid1);
                                                    updateCarrierFile(getSlotId(), 5, this.mGid1);
                                                    break;
                                                } else {
                                                    loge("Exception in get GID1 " + ar21.exception);
                                                    this.mGid1 = null;
                                                    updateCarrierFile(getSlotId(), 5, null);
                                                    break;
                                                }
                                            default:
                                                switch (i2) {
                                                    case 36:
                                                        isRecordLoadResponse = true;
                                                        AsyncResult ar22 = (AsyncResult) message.obj;
                                                        byte[] data13 = (byte[]) ar22.result;
                                                        if (ar22.exception == null) {
                                                            this.mGid2 = IccUtils.bytesToHexString(data13);
                                                            log("GID2: " + this.mGid2);
                                                            updateCarrierFile(getSlotId(), 6, this.mGid2);
                                                            break;
                                                        } else {
                                                            loge("Exception in get GID2 " + ar22.exception);
                                                            this.mGid2 = null;
                                                            updateCarrierFile(getSlotId(), 6, null);
                                                            break;
                                                        }
                                                    case 37:
                                                        isRecordLoadResponse = true;
                                                        AsyncResult ar23 = (AsyncResult) message.obj;
                                                        byte[] data14 = (byte[]) ar23.result;
                                                        if (ar23.exception == null) {
                                                            if (data14 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data14));
                                                                this.mPlmnActRecords = PlmnActRecord.getRecords(data14);
                                                                break;
                                                            }
                                                        }
                                                        loge("Failed getting User PLMN with Access Tech Records: " + ar23.exception);
                                                        break;
                                                    case 38:
                                                        isRecordLoadResponse = true;
                                                        AsyncResult ar24 = (AsyncResult) message.obj;
                                                        byte[] data15 = (byte[]) ar24.result;
                                                        if (ar24.exception == null) {
                                                            if (data15 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data15));
                                                                this.mOplmnActRecords = PlmnActRecord.getRecords(data15);
                                                                break;
                                                            }
                                                        }
                                                        loge("Failed getting Operator PLMN with Access Tech Records: " + ar24.exception);
                                                        break;
                                                    case 39:
                                                        isRecordLoadResponse = true;
                                                        AsyncResult ar25 = (AsyncResult) message.obj;
                                                        byte[] data16 = (byte[]) ar25.result;
                                                        if (ar25.exception == null) {
                                                            if (data16 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data16));
                                                                this.mHplmnActRecords = PlmnActRecord.getRecords(data16);
                                                                log("HplmnActRecord[]=" + Arrays.toString(this.mHplmnActRecords));
                                                                break;
                                                            }
                                                        }
                                                        loge("Failed getting Home PLMN with Access Tech Records: " + ar25.exception);
                                                        break;
                                                    case 40:
                                                        isRecordLoadResponse = true;
                                                        AsyncResult ar26 = (AsyncResult) message.obj;
                                                        byte[] data17 = (byte[]) ar26.result;
                                                        if (ar26.exception == null) {
                                                            if (data17 != null) {
                                                                this.mEhplmns = parseBcdPlmnList(data17, "Equivalent Home");
                                                                break;
                                                            }
                                                        }
                                                        loge("Failed getting Equivalent Home PLMNs: " + ar26.exception);
                                                        break;
                                                    case 41:
                                                        isRecordLoadResponse = true;
                                                        AsyncResult ar27 = (AsyncResult) message.obj;
                                                        byte[] data18 = (byte[]) ar27.result;
                                                        if (ar27.exception == null) {
                                                            if (data18 != null) {
                                                                this.mFplmns = parseBcdPlmnList(data18, "Forbidden");
                                                                if (message.arg1 == 1238273) {
                                                                    isRecordLoadResponse = false;
                                                                    Message response = retrievePendingResponseMessage(Integer.valueOf(message.arg2));
                                                                    if (response == null) {
                                                                        loge("Failed to retrieve a response message for FPLMN");
                                                                        break;
                                                                    } else {
                                                                        AsyncResult.forMessage(response, Arrays.copyOf(this.mFplmns, this.mFplmns.length), null);
                                                                        response.sendToTarget();
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        loge("Failed getting Forbidden PLMNs: " + ar27.exception);
                                                        break;
                                                    default:
                                                        switch (i2) {
                                                            case 258:
                                                            case 259:
                                                                onLocked(message.what);
                                                                break;
                                                            default:
                                                                super.handleMessage(msg);
                                                                break;
                                                        }
                                                }
                                        }
                                }
                        }
                }
            } else {
                isRecordLoadResponse = false;
                AsyncResult ar28 = (AsyncResult) message.obj;
                if (ar28.exception == null) {
                    this.mMsisdn = this.mNewMsisdn;
                    this.mMsisdnTag = this.mNewMsisdnTag;
                    log("Success to update EF[MSISDN]");
                }
                if (ar28.userObj != null) {
                    AsyncResult.forMessage((Message) ar28.userObj).exception = ar28.exception;
                    ((Message) ar28.userObj).sendToTarget();
                }
            }
        } catch (RuntimeException exc) {
            try {
                logw("Exception parsing SIM record", exc);
            } catch (Throwable th3) {
                if (isRecordLoadResponse) {
                    onRecordLoaded();
                }
                throw th3;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleFileUpdate(int efid) {
        if (efid != 28435) {
            if (efid == 28437) {
                this.mRecordsToLoad++;
                log("[CSP] SIM Refresh for EF_CSP_CPHS");
                this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
                return;
            } else if (efid == 28439) {
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                return;
            } else if (efid == 28475) {
                log("SIM Refresh called for EF_FDN");
                this.mParentApp.queryFdn();
                this.mAdnCache.reset();
                return;
            } else if (efid == 28480) {
                this.mRecordsToLoad++;
                log("SIM Refresh called for EF_MSISDN");
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, obtainMessage(10));
                return;
            } else if (efid == 28615) {
                this.mRecordsToLoad++;
                new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, obtainMessage(6));
                return;
            } else if (efid != 28619) {
                this.mAdnCache.reset();
                fetchSimRecords();
                return;
            }
        }
        log("SIM Refresh called for EF_CFIS or EF_CFF_CPHS");
        loadCallForwardingRecords();
    }

    private int dispatchGsmMessage(SmsMessage message) {
        this.mNewSmsRegistrants.notifyResult(message);
        return 0;
    }

    private void handleSms(byte[] ba) {
        if (ba[0] != 0) {
            Rlog.d("ENF", "status : " + ba[0]);
        }
        if (ba[0] == 3) {
            int n = ba.length;
            byte[] pdu = new byte[(n - 1)];
            System.arraycopy(ba, 1, pdu, 0, n - 1);
            dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
        }
    }

    private void handleSmses(ArrayList<byte[]> messages) {
        int count = messages.size();
        for (int i = 0; i < count; i++) {
            byte[] ba = messages.get(i);
            if (ba[0] != 0) {
                Rlog.i("ENF", "status " + i + ": " + ba[0]);
            }
            if (ba[0] == 3) {
                int n = ba.length;
                byte[] pdu = new byte[(n - 1)];
                System.arraycopy(ba, 1, pdu, 0, n - 1);
                dispatchGsmMessage(SmsMessage.createFromPdu(pdu, "3gpp"));
                ba[0] = 1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRecordLoaded() {
        this.mRecordsToLoad--;
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (getLockedRecordsLoaded() || getNetworkLockedRecordsLoaded()) {
            onLockedAllRecordsLoaded();
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

    private void setSimLanguageFromEF() {
        if (Resources.getSystem().getBoolean(17957064)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
    }

    private void onLockedAllRecordsLoaded() {
        setSimLanguageFromEF();
        if (this.mLockedRecordsReqReason == 1) {
            this.mLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else if (this.mLockedRecordsReqReason == 2) {
            this.mNetworkLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        } else {
            loge("onLockedAllRecordsLoaded: unexpected mLockedRecordsReqReason " + this.mLockedRecordsReqReason);
        }
    }

    /* access modifiers changed from: protected */
    public void onAllRecordsLoaded() {
        log("record load complete" + getSlotId());
        setSimLanguageFromEF();
        setVoiceCallForwardingFlagFromSimRecords();
        String operator = getOperatorNumeric();
        if (!TextUtils.isEmpty(operator)) {
            log("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='" + operator + "'");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
        } else {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        }
        String imsi = getIMSI();
        if (TextUtils.isEmpty(this.mImsi) || imsi.length() < 3) {
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
        HwGetCfgFileConfig.readCfgFileConfig("xml/telephony-various.xml", getSlotId());
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
    }

    /* access modifiers changed from: protected */
    public void setVoiceMailByCountry(String spn) {
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

    private void onLocked(int msg) {
        log("only fetch EF_LI, EF_PL and EF_ICCID in locked state");
        this.mLockedRecordsReqReason = msg == 258 ? 1 : 2;
        loadEfLiAndEfPl();
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
        this.mRecordsToLoad++;
    }

    public void onGetImsiDone(Message msg) {
        if (msg != null) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                loge("Exception querying IMSI, Exception:" + ar.exception);
                return;
            }
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
            if (this.mImsi != null && this.mImsi.length() >= 6) {
                log("IMSI: " + this.mImsi.substring(0, 6) + Rlog.pii(LOG_TAG, this.mImsi.substring(6)));
            }
            String imsi = getIMSI();
            onImsiLoadedHw();
            updateSarMnc(imsi);
            if ((this.mMncLength == 0 || this.mMncLength == 2) && imsi != null && imsi.length() >= 6) {
                String mccmncCode = imsi.substring(0, 6);
                String[] strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (strArr[i].equals(mccmncCode)) {
                        this.mMncLength = 3;
                        log("IMSI: setting1 mMncLength=" + this.mMncLength);
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if ((this.mMncLength == 0 || this.mMncLength == 3) && imsi != null && imsi.length() >= 5) {
                String mcc = imsi.substring(0, 3);
                if (mcc.equals("404") || mcc.equals("405")) {
                    String mccmncCode2 = imsi.substring(0, 5);
                    String[] strArr2 = MCCMNC_CODES_HAVING_2DIGITS_MNC;
                    int length2 = strArr2.length;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length2) {
                            break;
                        } else if (strArr2[i2].equals(mccmncCode2)) {
                            this.mMncLength = 2;
                            break;
                        } else {
                            i2++;
                        }
                    }
                }
            }
            if (this.mMncLength == 0 && imsi != null) {
                try {
                    String mccStr = imsi.substring(0, 3);
                    if (!mccStr.equals("404")) {
                        if (!mccStr.equals("405")) {
                            this.mMncLength = MccTable.smallestDigitsMccForMnc(Integer.parseInt(mccStr));
                        }
                    }
                    this.mMncLength = 3;
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
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            this.mFh.loadEFTransparent(IccConstants.EF_LI, obtainMessage(100, new EfUsimLiLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded()));
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

    /* access modifiers changed from: protected */
    public void fetchSimRecords() {
        this.mRecordsRequested = true;
        log("fetchSimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad = this.mRecordsToLoad + 1;
        if (!getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(4));
            } else {
                this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
            }
            this.mRecordsToLoad++;
        }
        if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
            log("fetchSimRecords: support fast fetch SIM records.");
            loadSimMatchedFileFromRilCache();
        } else {
            CommandsInterface commandsInterface = this.mCi;
            IccFileHandler iccFileHandler = this.mFh;
            commandsInterface.iccIOForApp(176, IccConstants.EF_AD, this.mFh.getEFPath(IccConstants.EF_AD), 0, 0, 4, null, null, this.mParentApp.getAid(), obtainMessage(9));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_SST, obtainMessage(17));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_GID1, obtainMessage(34));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_GID2, obtainMessage(36));
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
        this.mFh.loadEFTransparent(IccConstants.EF_INFO_CPHS, obtainMessage(26));
        this.mRecordsToLoad++;
        this.mFh.loadEFTransparent(IccConstants.EF_CSP_CPHS, obtainMessage(33));
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

    public int getDisplayRule(ServiceState serviceState) {
        if (this.mParentApp != null && this.mParentApp.getUiccProfile() != null && this.mParentApp.getUiccProfile().getOperatorBrandOverride() != null) {
            return 2;
        }
        if (TextUtils.isEmpty(getServiceProviderName()) || this.mSpnDisplayCondition == -1) {
            return 2;
        }
        if (!useRoamingFromServiceState() ? !isOnMatchingPlmn(serviceState.getOperatorNumeric()) : serviceState.getRoaming()) {
            if ((this.mSpnDisplayCondition & 2) == 0) {
                return 2 | 1;
            }
            return 2;
        } else if ((this.mSpnDisplayCondition & 1) == 1) {
            return 1 | 2;
        } else {
            return 1;
        }
    }

    private boolean useRoamingFromServiceState() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle b = configManager.getConfigForSubId(SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getPhoneId()));
            if (b != null && b.getBoolean("spn_display_rule_use_roaming_from_service_state_bool")) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnMatchingPlmn(String plmn) {
        if (plmn == null) {
            return false;
        }
        if (plmn.equals(getOperatorNumeric())) {
            return true;
        }
        if (this.mSpdiNetworks != null) {
            Iterator<String> it = this.mSpdiNetworks.iterator();
            while (it.hasNext()) {
                if (plmn.equals(it.next())) {
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
        switch (this.mSpnState) {
            case INIT:
                setServiceProviderName(null);
                if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
                    this.mCi.getSimMatchedFileFromRilCache(IccConstants.EF_SPN, obtainMessage(42));
                } else {
                    this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
                }
                this.mRecordsToLoad++;
                this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
                break;
            case READ_SPN_3GPP:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                } else {
                    byte[] data = (byte[]) ar.result;
                    this.mSpnDisplayCondition = 255 & data[0];
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                    String spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    } else {
                        log("Load EF_SPN: " + spn + " spnDisplayCondition: " + this.mSpnDisplayCondition);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                    if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
                        this.mCi.getSimMatchedFileFromRilCache(IccConstants.EF_SPN_CPHS, obtainMessage(42));
                    } else {
                        this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                    }
                    this.mRecordsToLoad++;
                    this.mSpnDisplayCondition = -1;
                    break;
                }
                break;
            case READ_SPN_CPHS:
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    byte[] data2 = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data2, 0, data2.length));
                    String spn2 = getServiceProviderName();
                    if (spn2 == null || spn2.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_CPHS: " + spn2);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn2);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                    if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
                        this.mCi.getSimMatchedFileFromRilCache(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(42));
                    } else {
                        this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                    }
                    this.mRecordsToLoad++;
                    break;
                }
                break;
            case READ_SPN_SHORT_CPHS:
                if (ar == null || ar.exception != null) {
                    setServiceProviderName(null);
                    log("No SPN loaded in either CHPS or 3GPP");
                } else {
                    byte[] data3 = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data3, 0, data3.length));
                    String spn3 = getServiceProviderName();
                    if (spn3 == null || spn3.length() == 0) {
                        log("No SPN loaded in either CHPS or 3GPP");
                    } else {
                        this.mSpnDisplayCondition = 2;
                        log("Load EF_SPN_SHORT_CPHS: " + spn3);
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn3);
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
        while (true) {
            if (!tlv.isValidObject()) {
                break;
            }
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
            this.mSpdiNetworks = new ArrayList<>(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.bcdPlmnToString(plmnEntries, i);
                if (plmnCode != null && plmnCode.length() >= 5) {
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
        int numValidPlmns = 0;
        String[] parsed = new String[numPlmns];
        for (int i = 0; i < numPlmns; i++) {
            parsed[numValidPlmns] = IccUtils.bcdPlmnToString(data, i * 3);
            if (!TextUtils.isEmpty(parsed[numValidPlmns])) {
                numValidPlmns++;
            }
        }
        return (String[]) Arrays.copyOf(parsed, numValidPlmns);
    }

    private boolean isCphsMailboxEnabled() {
        boolean z = false;
        if (this.mCphsInfo == null) {
            return false;
        }
        if ((this.mCphsInfo[1] & 48) == 48) {
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

    /* access modifiers changed from: protected */
    public void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, s, tr);
    }

    /* access modifiers changed from: protected */
    public void logv(String s) {
        Rlog.v(LOG_TAG, s);
    }

    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[2 * i] == -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + data[(2 * i) + 1]);
                if ((data[(2 * i) + 1] & 128) == 128) {
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
        pw.println(" mUsimServiceTable=" + this.mUsimServiceTable);
        pw.println(" mGid1=" + this.mGid1);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid1=" + this.mCarrierTestOverride.getFakeGid1());
        }
        pw.println(" mGid2=" + this.mGid2);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakeGid2=" + this.mCarrierTestOverride.getFakeGid2());
        }
        pw.println(" mPnnHomeName=" + this.mPnnHomeName);
        if (this.mCarrierTestOverride.isInTestMode()) {
            pw.println(" mFakePnnHomeName=" + this.mCarrierTestOverride.getFakePnnHomeName());
        }
        pw.println(" mPlmnActRecords[]=" + Arrays.toString(this.mPlmnActRecords));
        pw.println(" mOplmnActRecords[]=" + Arrays.toString(this.mOplmnActRecords));
        pw.println(" mHplmnActRecords[]=" + Arrays.toString(this.mHplmnActRecords));
        pw.println(" mFplmns[]=" + Arrays.toString(this.mFplmns));
        pw.println(" mEhplmns[]=" + Arrays.toString(this.mEhplmns));
        pw.flush();
    }

    private boolean getCallForwardingPreference() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        boolean cf = sp.getBoolean("cf_enabled_key" + getSlotId(), false);
        Rlog.d(LOG_TAG, "Get callforwarding info from perferences getSlotId()=" + getSlotId() + ",cf=" + cf);
        return cf;
    }

    private void setCallForwardingPreference(boolean enabled) {
        Rlog.d(LOG_TAG, "Set callforwarding info to perferences getSlotId()=" + getSlotId() + ",cf=" + enabled);
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        StringBuilder sb = new StringBuilder();
        sb.append("cf_enabled_key");
        sb.append(getSlotId());
        edit.putBoolean(sb.toString(), enabled);
        edit.commit();
        if (this.mImsi != null) {
            setVmSimImsi(this.mImsi);
        }
    }

    /* access modifiers changed from: protected */
    public void updateMccMncConfigWithGplmn(String operatorNumeric) {
        log("updateMccMncConfigWithGplmn: " + operatorNumeric);
        if (HwTelephonyFactory.getHwUiccManager().isCDMASimCard(this.mParentApp.getPhoneId())) {
            log("cdma card, ignore updateMccMncConfiguration");
        } else if (operatorNumeric != null && operatorNumeric.length() >= 5) {
            MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
        }
    }

    /* access modifiers changed from: protected */
    public void initEventIdMap() {
        this.sEventIdMap.put("EVENT_GET_MBDN_DONE", 6);
        this.sEventIdMap.put("EVENT_GET_ICCID_DONE", 4);
        this.sEventIdMap.put("EVENT_GET_AD_DONE", 9);
        this.sEventIdMap.put("EVENT_GET_SST_DONE", 17);
        this.sEventIdMap.put("EVENT_GET_SPN_DONE", 12);
        this.sEventIdMap.put("EVENT_GET_GID1_DONE", 34);
        this.sEventIdMap.put("EVENT_GET_GID2_DONE", 36);
    }

    /* access modifiers changed from: protected */
    public int getEventIdFromMap(String event) {
        if (this.sEventIdMap.containsKey(event)) {
            return this.sEventIdMap.get(event).intValue();
        }
        log("Event Id not in the map.");
        return -1;
    }
}
