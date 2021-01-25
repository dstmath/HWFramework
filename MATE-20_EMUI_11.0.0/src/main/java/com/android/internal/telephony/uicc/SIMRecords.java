package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
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
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.gsm.SimTlv;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import com.huawei.internal.telephony.uicc.UsimServiceTableEx;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import huawei.cust.HwGetCfgFileConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    protected static final int EVENT_GET_EHPLMN_DONE_FOR_APNCURE = 43;
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
    private static final int EVENT_SMS_ON_SIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    protected static final boolean HW_IS_SUPPORT_FAST_FETCH_SIMINFO = SystemProperties.getBoolean("ro.odm.radio.nvcfg_normalization", false);
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    protected static final String LOG_TAG = "SIMRecords";
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
    @UnsupportedAppUsage
    byte[] mEfCPHS_MWI = null;
    @UnsupportedAppUsage
    byte[] mEfCff = null;
    @UnsupportedAppUsage
    byte[] mEfCfis = null;
    @UnsupportedAppUsage
    byte[] mEfLi = null;
    @UnsupportedAppUsage
    byte[] mEfMWIS = null;
    @UnsupportedAppUsage
    byte[] mEfPl = null;
    private String mFirstImsi;
    private HwCustSIMRecords mHwCustSIMRecords = null;
    private String mOriginVmImsi;
    private String mSecondImsi;
    private GetSpnFsmState mSpnState;
    @UnsupportedAppUsage
    UsimServiceTable mUsimServiceTable;
    @UnsupportedAppUsage
    VoiceMailConstants mVmConfig;
    private HashMap<String, Integer> sEventIdMap = new HashMap<>();

    /* access modifiers changed from: private */
    public enum GetSpnFsmState {
        IDLE,
        INIT,
        READ_SPN_3GPP,
        READ_SPN_CPHS,
        READ_SPN_SHORT_CPHS
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, android.os.Handler, java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SimRecords: ");
        sb.append(super.toString());
        sb.append(" mVmConfig");
        sb.append(this.mVmConfig);
        sb.append(" callForwardingEnabled=");
        sb.append(this.mCallForwardingStatus);
        sb.append(" spnState=");
        sb.append(this.mSpnState);
        sb.append(" mCphsInfo=");
        sb.append(this.mCphsInfo);
        sb.append(" mCspPlmnEnabled=");
        sb.append(this.mCspPlmnEnabled);
        sb.append(" efMWIS=");
        sb.append(this.mEfMWIS);
        sb.append(" efCPHS_MWI=");
        sb.append(this.mEfCPHS_MWI);
        sb.append(" mEfCff=");
        sb.append(this.mEfCff);
        sb.append(" mEfCfis=");
        sb.append(this.mEfCfis);
        sb.append(" getOperatorNumeric=");
        sb.append(Log.HWINFO ? getOperatorNumeric() : "***");
        return sb.toString();
    }

    public SIMRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = new AdnRecordCache(this.mFh);
        this.mVmConfig = new VoiceMailConstants(c, getSlotId());
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(app);
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(ci);
        this.mHwIccRecordsEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwSIMRecordsEx(this, uiccCardApplicationEx, c, commandsInterfaceEx);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        this.mCi.setOnSmsOnSim(this, 21, null);
        resetRecords();
        this.mHwIccRecordsEx.resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, EVENT_APP_LOCKED, null);
        this.mParentApp.registerForNetworkLocked(this, EVENT_APP_NETWORK_LOCKED, null);
        log("SIMRecords X ctor this=" + this);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dispose() {
        log("Disposing SIMRecords this=" + this);
        this.mCi.unSetOnSmsOnSim(this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mParentApp.unregisterForNetworkLocked(this);
        resetRecords();
        this.mHwIccRecordsEx.resetRecords();
        this.mHwIccRecordsEx.dispose();
        super.dispose();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
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
        this.mCarrierNameDisplayCondition = 0;
        this.mEfMWIS = null;
        this.mEfCPHS_MWI = null;
        this.mSpdi = null;
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
        log("update icc_operator_numeric=" + ((Object) null));
        this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), PhoneConfigurationManager.SSSS);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mLoaded.set(false);
        this.mHwIccRecordsEx.setImsiReady(false);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public String getMsisdnNumber() {
        UsimServiceTable usimServiceTable;
        if (!NEED_CHECK_MSISDN_BIT || (usimServiceTable = this.mUsimServiceTable) == null || usimServiceTable.isAvailable(UsimServiceTable.UsimService.MSISDN)) {
            return this.mMsisdn;
        }
        log("EF_MSISDN not available");
        return null;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public UsimServiceTable getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    @UnsupportedAppUsage
    private int getExtFromEf(int ef) {
        if (ef == 28480 && this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
            return IccConstants.EF_EXT5;
        }
        return IccConstants.EF_EXT1;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setMsisdnNumber(String alphaTag, String number, Message onComplete) {
        this.mNewMsisdn = number;
        this.mNewMsisdnTag = alphaTag;
        log("Set MSISDN: " + this.mNewMsisdnTag + " " + Rlog.pii(LOG_TAG, this.mNewMsisdn));
        new AdnRecordLoader(this.mFh).updateEF(new AdnRecord(this.mNewMsisdnTag, this.mNewMsisdn), IccConstants.EF_MSISDN, getExtFromEf(IccConstants.EF_MSISDN), 1, null, obtainMessage(30, onComplete));
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getMsisdnAlphaTag() {
        UsimServiceTable usimServiceTable;
        if (!NEED_CHECK_MSISDN_BIT || (usimServiceTable = this.mUsimServiceTable) == null || usimServiceTable.isAvailable(UsimServiceTable.UsimService.MSISDN)) {
            return this.mMsisdnTag;
        }
        log("EF_MSISDN not available");
        return null;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getVoiceMailNumber() {
        this.mHwIccRecordsEx.beforeGetVoiceMailNumber();
        HwCustSIMRecords hwCustSIMRecords = this.mHwCustSIMRecords;
        if (hwCustSIMRecords == null || !hwCustSIMRecords.isOpenRoamingVoiceMail()) {
            return this.mVoiceMailNum;
        }
        return this.mHwCustSIMRecords.getRoamingVoicemail();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        if (this.mIsVoiceMailFixed) {
            AsyncResult.forMessage(onComplete).exception = new IccVmFixedException("Voicemail number is fixed by operator");
            onComplete.sendToTarget();
            return;
        }
        this.mNewVoiceMailNum = voiceNumber;
        this.mNewVoiceMailTag = alphaTag;
        boolean custvmNotToSim = false;
        Boolean editvmnottosim = (Boolean) HwCfgFilePolicy.getValue("vm_edit_not_to_sim_bool", this.mParentApp.getPhoneId(), Boolean.class);
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
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MBDN, IccConstants.EF_EXT6, this.mMailboxIndex, null, obtainMessage(20, onComplete));
        } else if (isCphsMailboxEnabled()) {
            new AdnRecordLoader(this.mFh).updateEF(adn, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onComplete));
        } else {
            AsyncResult.forMessage(onComplete).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
            onComplete.sendToTarget();
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getVoiceMailAlphaTag() {
        return this.mVoiceMailTag;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (line == 1) {
            try {
                if (this.mEfMWIS != null) {
                    this.mEfMWIS[0] = (byte) ((this.mEfMWIS[0] & 254) | (countWaiting == 0 ? 0 : 1));
                    if (countWaiting < 0) {
                        this.mEfMWIS[1] = 0;
                    } else {
                        this.mEfMWIS[1] = (byte) countWaiting;
                    }
                    this.mFh.updateEFLinearFixed(IccConstants.EF_MWIS, 1, this.mEfMWIS, null, obtainMessage(14, IccConstants.EF_MWIS, 0));
                }
                if (this.mEfCPHS_MWI != null) {
                    this.mEfCPHS_MWI[0] = (byte) ((this.mEfCPHS_MWI[0] & 240) | (countWaiting == 0 ? 5 : 10));
                    this.mFh.updateEFTransparent(IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS, this.mEfCPHS_MWI, obtainMessage(14, Integer.valueOf((int) IccConstants.EF_VOICE_MAIL_INDICATOR_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving voice mail state to SIM. Probably malformed SIM record", ex);
            }
        }
    }

    private boolean validEfCfis(byte[] data) {
        if (data != null) {
            if (data[0] < 1 || data[0] > 4) {
                logw("MSP byte: " + ((int) data[0]) + " is not between 1 and 4", null);
            }
            for (byte b : data) {
                if (b != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceMessageCount() {
        int countVoiceMessages = -2;
        byte[] bArr = this.mEfMWIS;
        boolean voiceMailWaiting = false;
        if (bArr != null) {
            if ((bArr[0] & 1) != 0) {
                voiceMailWaiting = true;
            }
            countVoiceMessages = this.mEfMWIS[1] & 255;
            if (voiceMailWaiting && (countVoiceMessages == 0 || countVoiceMessages == 255)) {
                countVoiceMessages = -1;
            }
            log(" VoiceMessageCount from SIM MWIS = " + countVoiceMessages);
        } else {
            byte[] bArr2 = this.mEfCPHS_MWI;
            if (bArr2 != null) {
                int indicator = bArr2[0] & 15;
                if (indicator == 10) {
                    countVoiceMessages = -1;
                } else if (indicator == 5) {
                    countVoiceMessages = 0;
                }
                log(" VoiceMessageCount from SIM CPHS = " + countVoiceMessages);
            }
        }
        return countVoiceMessages;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceCallForwardingFlag() {
        return this.mCallForwardingStatus;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void setVoiceCallForwardingFlag(int line, boolean enable, String dialNumber) {
        int i;
        if (line == 1) {
            if (enable) {
                i = 1;
            } else {
                i = 0;
            }
            this.mCallForwardingStatus = i;
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
                    this.mFh.updateEFLinearFixed(IccConstants.EF_CFIS, 1, this.mEfCfis, null, obtainMessage(14, Integer.valueOf((int) IccConstants.EF_CFIS)));
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
                    this.mFh.updateEFTransparent(IccConstants.EF_CFF_CPHS, this.mEfCff, obtainMessage(14, Integer.valueOf((int) IccConstants.EF_CFF_CPHS)));
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                logw("Error saving call forwarding flag to SIM. Probably malformed SIM record", ex);
            } catch (RuntimeException e) {
                loge("Error saving call forwarding flag to SIM. Probably malformed dialNumber");
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchSimRecords();
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            log("getOperatorNumeric: IMSI == null");
            return null;
        } else if (this.mMncLength == -1 || this.mMncLength == 0) {
            log("getSIMOperatorNumeric: bad mncLength");
            if (this.mImsi.length() >= 5) {
                String mcc = this.mImsi.substring(0, 3);
                if (mcc.equals("404") || mcc.equals("405") || mcc.equals("232")) {
                    String mccmncCode = this.mImsi.substring(0, 5);
                    for (String mccmnc : IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC) {
                        if (mccmnc.equals(mccmncCode)) {
                            this.mMncLength = 2;
                            return this.mImsi.substring(0, this.mMncLength + 3);
                        }
                    }
                }
            }
            return null;
        } else if (imsi.length() >= this.mMncLength + 3) {
            return imsi.substring(0, this.mMncLength + 3);
        } else {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:675:0x10d0  */
    /* JADX WARNING: Removed duplicated region for block: B:695:0x1130  */
    /* JADX WARNING: Removed duplicated region for block: B:706:0x115b A[SYNTHETIC, Splitter:B:706:0x115b] */
    /* JADX WARNING: Removed duplicated region for block: B:716:0x119c  */
    /* JADX WARNING: Removed duplicated region for block: B:725:0x11c4  */
    /* JADX WARNING: Removed duplicated region for block: B:726:0x11c9  */
    /* JADX WARNING: Removed duplicated region for block: B:729:0x11f4  */
    /* JADX WARNING: Removed duplicated region for block: B:830:0x1536  */
    /* JADX WARNING: Removed duplicated region for block: B:857:0x10f3 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:859:0x1141 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:862:? A[RETURN, SYNTHETIC] */
    @Override // com.android.internal.telephony.uicc.IccRecords, android.os.Handler
    public void handleMessage(Message msg) {
        Throwable th;
        RuntimeException exc;
        Throwable th2;
        int length;
        int i;
        int length2;
        int i2;
        boolean isRecordLoadResponse;
        boolean isRecordLoadResponse2 = false;
        if (this.mDestroyed.get()) {
            loge("Received message[" + msg.what + "], Ignoring.");
            return;
        }
        try {
            int i3 = msg.what;
            if (i3 == 1) {
                onReady();
                this.mHwIccRecordsEx.onReady();
            } else if (i3 != 30) {
                if (i3 != EVENT_APP_LOCKED && i3 != EVENT_APP_NETWORK_LOCKED) {
                    String str = "***";
                    int i4 = 0;
                    switch (i3) {
                        case 3:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar = (AsyncResult) msg.obj;
                            if (ar.exception == null) {
                                if (!HwPartTelephonyFactory.IS_USING_HW_DESIGN) {
                                    setImsi((String) ar.result);
                                    break;
                                } else {
                                    onGetImsiDone(msg);
                                    break;
                                }
                            } else {
                                loge("Exception querying IMSI, Exception:" + ar.exception);
                                break;
                            }
                        case 4:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar2 = (AsyncResult) msg.obj;
                            byte[] data = (byte[]) ar2.result;
                            if (ar2.exception == null) {
                                this.mIccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                                this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                                this.mHwIccRecordsEx.onIccIdLoadedHw();
                                log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                                this.mHwIccRecordsEx.notifyRegisterLoadIccID(ar2.userObj, ar2.result, ar2.exception);
                                break;
                            } else {
                                this.mHwIccRecordsEx.notifyRegisterLoadIccID(ar2.userObj, ar2.result, ar2.exception);
                                break;
                            }
                        case 5:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar3 = (AsyncResult) msg.obj;
                            byte[] data2 = (byte[]) ar3.result;
                            boolean isValidMbdn = false;
                            if (ar3.exception == null) {
                                log("EF_MBI: " + IccUtils.bytesToHexString(data2));
                                this.mMailboxIndex = data2[0] & 255;
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
                            this.mVmConfig.setVoicemailOnSIM(null, null);
                            this.mVoiceMailNum = null;
                            this.mVoiceMailTag = null;
                            isRecordLoadResponse2 = true;
                            AsyncResult ar4 = (AsyncResult) msg.obj;
                            if (ar4.exception == null) {
                                AdnRecord adn = (AdnRecord) ar4.result;
                                StringBuilder sb = new StringBuilder();
                                sb.append("VM: ");
                                sb.append(adn);
                                sb.append(msg.what == 11 ? " EF[MAILBOX]" : " EF[MBDN]");
                                log(sb.toString());
                                if (adn.isEmpty() && msg.what == 6) {
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
                                sb2.append(msg.what == 11 ? "[MAILBOX]" : "[MBDN]");
                                log(sb2.toString());
                                if (msg.what == 6) {
                                    this.mRecordsToLoad++;
                                    new AdnRecordLoader(this.mFh).loadFromEF(IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, obtainMessage(11));
                                    break;
                                }
                            }
                            break;
                        case 7:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar5 = (AsyncResult) msg.obj;
                            byte[] data3 = (byte[]) ar5.result;
                            log("EF_MWIS : " + IccUtils.bytesToHexString(data3));
                            if (ar5.exception == null) {
                                if ((data3[0] & 255) != 255) {
                                    if (SystemProperties.getBoolean("ro.config.hw_eeVoiceMsgCount", false) && (data3[0] & 255) == 0) {
                                        this.mEfMWIS = null;
                                        Rlog.d(LOG_TAG, "SIMRecords EE VoiceMessageCount from SIM CPHS");
                                        break;
                                    } else {
                                        this.mEfMWIS = data3;
                                        break;
                                    }
                                } else {
                                    log("SIMRecords: Uninitialized record MWIS");
                                    break;
                                }
                            } else {
                                log("EVENT_GET_MWIS_DONE exception = " + ar5.exception);
                                break;
                            }
                            break;
                        case 8:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar6 = (AsyncResult) msg.obj;
                            byte[] data4 = (byte[]) ar6.result;
                            log("EF_CPHS_MWI: " + IccUtils.bytesToHexString(data4));
                            if (ar6.exception == null) {
                                this.mEfCPHS_MWI = data4;
                                break;
                            } else {
                                log("EVENT_GET_VOICE_MAIL_INDICATOR_CPHS_DONE exception = " + ar6.exception);
                                break;
                            }
                        case 9:
                            isRecordLoadResponse2 = true;
                            try {
                                this.mMncLength = 0;
                                try {
                                    if (!this.mCarrierTestOverride.isInTestMode()) {
                                        AsyncResult ar7 = (AsyncResult) msg.obj;
                                        IccIoResult result = (IccIoResult) ar7.result;
                                        result.getException();
                                        if (ar7.exception != null) {
                                            if (this.mMncLength != -1 && this.mMncLength != 0 && this.mMncLength != 2) {
                                                isRecordLoadResponse = true;
                                            } else if (this.mImsi == null) {
                                                isRecordLoadResponse = true;
                                            } else if (this.mImsi.length() >= 6) {
                                                String mccmncCode = this.mImsi.substring(0, 6);
                                                String[] strArr = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                int length3 = strArr.length;
                                                isRecordLoadResponse = true;
                                                int i5 = 0;
                                                while (true) {
                                                    if (i5 < length3) {
                                                        try {
                                                            if (strArr[i5].equals(mccmncCode)) {
                                                                this.mMncLength = 3;
                                                                log("setting6 mMncLength=" + this.mMncLength);
                                                            } else {
                                                                i5++;
                                                                ar7 = ar7;
                                                            }
                                                        } catch (RuntimeException e) {
                                                            exc = e;
                                                            isRecordLoadResponse2 = true;
                                                            try {
                                                                logw("Exception parsing SIM record", exc);
                                                                if (!isRecordLoadResponse2) {
                                                                }
                                                                onRecordLoaded();
                                                            } catch (Throwable th3) {
                                                                th = th3;
                                                                if (isRecordLoadResponse2) {
                                                                    onRecordLoaded();
                                                                }
                                                                throw th;
                                                            }
                                                        } catch (Throwable th4) {
                                                            th = th4;
                                                            isRecordLoadResponse2 = true;
                                                            if (isRecordLoadResponse2) {
                                                            }
                                                            throw th;
                                                        }
                                                    }
                                                }
                                            } else {
                                                isRecordLoadResponse = true;
                                            }
                                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                String mcc = this.mImsi.substring(0, 3);
                                                if (mcc.equals("404") || mcc.equals("405")) {
                                                    String mccmncCode2 = this.mImsi.substring(0, 5);
                                                    String[] strArr2 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                    int length4 = strArr2.length;
                                                    int i6 = 0;
                                                    while (true) {
                                                        if (i6 < length4) {
                                                            if (strArr2[i6].equals(mccmncCode2)) {
                                                                this.mMncLength = 2;
                                                            } else {
                                                                i6++;
                                                            }
                                                        }
                                                    }
                                                }
                                                this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                            }
                                            if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                if (this.mImsi != null) {
                                                    try {
                                                        String mccStr = this.mImsi.substring(0, 3);
                                                        if (!mccStr.equals("404")) {
                                                            if (!mccStr.equals("405")) {
                                                                int mcc2 = Integer.parseInt(mccStr);
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
                                            if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                                StringBuilder sb3 = new StringBuilder();
                                                sb3.append("GET_AD_DONE setSystemProperty simOperator=");
                                                sb3.append(Log.HWINFO ? getOperatorNumeric() : str);
                                                log(sb3.toString());
                                                setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                StringBuilder sb4 = new StringBuilder();
                                                sb4.append("update mccmnc=");
                                                if (Log.HWINFO) {
                                                    str = this.mImsi.substring(0, this.mMncLength + 3);
                                                }
                                                sb4.append(str);
                                                log(sb4.toString());
                                                this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                            }
                                            if (((this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true) && !this.mHwIccRecordsEx.getImsiReady()) {
                                                this.mHwIccRecordsEx.setImsiReady(true);
                                                this.mParentApp.notifyGetAdDone(null);
                                                this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                            }
                                            this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                        } else {
                                            isRecordLoadResponse = true;
                                            try {
                                                if (result.payload != null) {
                                                    byte[] data5 = result.payload;
                                                    log("EF_AD: " + IccUtils.bytesToHexString(data5));
                                                    if (data5.length < 3) {
                                                        log("Corrupt AD data on SIM");
                                                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                            String mccmncCode3 = this.mImsi.substring(0, 6);
                                                            String[] strArr3 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                            int length5 = strArr3.length;
                                                            int i7 = 0;
                                                            while (true) {
                                                                if (i7 < length5) {
                                                                    if (strArr3[i7].equals(mccmncCode3)) {
                                                                        this.mMncLength = 3;
                                                                        log("setting6 mMncLength=" + this.mMncLength);
                                                                    } else {
                                                                        i7++;
                                                                        strArr3 = strArr3;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                            String mcc3 = this.mImsi.substring(0, 3);
                                                            if (mcc3.equals("404") || mcc3.equals("405")) {
                                                                String mccmncCode4 = this.mImsi.substring(0, 5);
                                                                String[] strArr4 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                                int length6 = strArr4.length;
                                                                int i8 = 0;
                                                                while (true) {
                                                                    if (i8 < length6) {
                                                                        if (strArr4[i8].equals(mccmncCode4)) {
                                                                            this.mMncLength = 2;
                                                                        } else {
                                                                            i8++;
                                                                            mcc3 = mcc3;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                                        }
                                                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                            if (this.mImsi != null) {
                                                                try {
                                                                    String mccStr2 = this.mImsi.substring(0, 3);
                                                                    if (!mccStr2.equals("404")) {
                                                                        if (!mccStr2.equals("405")) {
                                                                            int mcc4 = Integer.parseInt(mccStr2);
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
                                                            StringBuilder sb5 = new StringBuilder();
                                                            sb5.append("GET_AD_DONE setSystemProperty simOperator=");
                                                            sb5.append(Log.HWINFO ? getOperatorNumeric() : str);
                                                            log(sb5.toString());
                                                            setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                            setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                            StringBuilder sb6 = new StringBuilder();
                                                            sb6.append("update mccmnc=");
                                                            if (Log.HWINFO) {
                                                                str = this.mImsi.substring(0, this.mMncLength + 3);
                                                            }
                                                            sb6.append(str);
                                                            log(sb6.toString());
                                                            this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                                        }
                                                        if (((this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true) && !this.mHwIccRecordsEx.getImsiReady()) {
                                                            this.mHwIccRecordsEx.setImsiReady(true);
                                                            this.mParentApp.notifyGetAdDone(null);
                                                            this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                                        }
                                                        this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                                    } else if (data5.length == 3) {
                                                        log("MNC length not present in EF_AD");
                                                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                            String mccmncCode5 = this.mImsi.substring(0, 6);
                                                            String[] strArr5 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                            int length7 = strArr5.length;
                                                            int i9 = 0;
                                                            while (true) {
                                                                if (i9 < length7) {
                                                                    if (strArr5[i9].equals(mccmncCode5)) {
                                                                        this.mMncLength = 3;
                                                                        log("setting6 mMncLength=" + this.mMncLength);
                                                                    } else {
                                                                        i9++;
                                                                        strArr5 = strArr5;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                            String mcc5 = this.mImsi.substring(0, 3);
                                                            if (mcc5.equals("404") || mcc5.equals("405")) {
                                                                String mccmncCode6 = this.mImsi.substring(0, 5);
                                                                String[] strArr6 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                                int length8 = strArr6.length;
                                                                int i10 = 0;
                                                                while (true) {
                                                                    if (i10 < length8) {
                                                                        if (strArr6[i10].equals(mccmncCode6)) {
                                                                            this.mMncLength = 2;
                                                                        } else {
                                                                            i10++;
                                                                            mcc5 = mcc5;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                                        }
                                                        if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                            if (this.mImsi != null) {
                                                                try {
                                                                    String mccStr3 = this.mImsi.substring(0, 3);
                                                                    if (!mccStr3.equals("404")) {
                                                                        if (!mccStr3.equals("405")) {
                                                                            int mcc6 = Integer.parseInt(mccStr3);
                                                                            Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc6);
                                                                            this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc6);
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
                                                            StringBuilder sb7 = new StringBuilder();
                                                            sb7.append("GET_AD_DONE setSystemProperty simOperator=");
                                                            sb7.append(Log.HWINFO ? getOperatorNumeric() : str);
                                                            log(sb7.toString());
                                                            setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                            setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                            StringBuilder sb8 = new StringBuilder();
                                                            sb8.append("update mccmnc=");
                                                            if (Log.HWINFO) {
                                                                str = this.mImsi.substring(0, this.mMncLength + 3);
                                                            }
                                                            sb8.append(str);
                                                            log(sb8.toString());
                                                            this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                                        }
                                                        if (((this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true) && !this.mHwIccRecordsEx.getImsiReady()) {
                                                            this.mHwIccRecordsEx.setImsiReady(true);
                                                            this.mParentApp.notifyGetAdDone(null);
                                                            this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                                        }
                                                        this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                                    } else {
                                                        int len = data5[3] & 15;
                                                        if (len == 2 || len == 3) {
                                                            this.mMncLength = len;
                                                        } else {
                                                            log("Received invalid or unset MNC Length=" + len);
                                                        }
                                                    }
                                                    updateOperatorPlmn();
                                                    isRecordLoadResponse2 = isRecordLoadResponse;
                                                    break;
                                                } else {
                                                    log("result.payload is null");
                                                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                        String mccmncCode7 = this.mImsi.substring(0, 6);
                                                        String[] strArr7 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                        int length9 = strArr7.length;
                                                        int i11 = 0;
                                                        while (true) {
                                                            if (i11 < length9) {
                                                                if (strArr7[i11].equals(mccmncCode7)) {
                                                                    this.mMncLength = 3;
                                                                    log("setting6 mMncLength=" + this.mMncLength);
                                                                } else {
                                                                    i11++;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                        String mcc7 = this.mImsi.substring(0, 3);
                                                        if (mcc7.equals("404") || mcc7.equals("405")) {
                                                            String mccmncCode8 = this.mImsi.substring(0, 5);
                                                            String[] strArr8 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                            int length10 = strArr8.length;
                                                            int i12 = 0;
                                                            while (true) {
                                                                if (i12 < length10) {
                                                                    if (strArr8[i12].equals(mccmncCode8)) {
                                                                        this.mMncLength = 2;
                                                                    } else {
                                                                        i12++;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                                    }
                                                    if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                        if (this.mImsi != null) {
                                                            try {
                                                                String mccStr4 = this.mImsi.substring(0, 3);
                                                                if (!mccStr4.equals("404")) {
                                                                    if (!mccStr4.equals("405")) {
                                                                        int mcc8 = Integer.parseInt(mccStr4);
                                                                        Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc8);
                                                                        this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc8);
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
                                                    if (!(this.mImsi == null || this.mMncLength == 0 || this.mImsi.length() < this.mMncLength + 3)) {
                                                        StringBuilder sb9 = new StringBuilder();
                                                        sb9.append("GET_AD_DONE setSystemProperty simOperator=");
                                                        sb9.append(Log.HWINFO ? getOperatorNumeric() : str);
                                                        log(sb9.toString());
                                                        setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                        setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                        StringBuilder sb10 = new StringBuilder();
                                                        sb10.append("update mccmnc=");
                                                        if (Log.HWINFO) {
                                                            str = this.mImsi.substring(0, this.mMncLength + 3);
                                                        }
                                                        sb10.append(str);
                                                        log(sb10.toString());
                                                        this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                                    }
                                                    if (((this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true) && !this.mHwIccRecordsEx.getImsiReady()) {
                                                        this.mHwIccRecordsEx.setImsiReady(true);
                                                        this.mParentApp.notifyGetAdDone(null);
                                                        this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                                    }
                                                    this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                                }
                                            } catch (Throwable th5) {
                                                th2 = th5;
                                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                                    String mccmncCode9 = this.mImsi.substring(0, 6);
                                                    String[] strArr9 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                                    length2 = strArr9.length;
                                                    i2 = 0;
                                                    while (true) {
                                                        if (i2 < length2) {
                                                            if (strArr9[i2].equals(mccmncCode9)) {
                                                                this.mMncLength = 3;
                                                                log("setting6 mMncLength=" + this.mMncLength);
                                                            } else {
                                                                i2++;
                                                            }
                                                        }
                                                    }
                                                }
                                                if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                                    String mcc9 = this.mImsi.substring(0, 3);
                                                    if (mcc9.equals("404") || mcc9.equals("405")) {
                                                        String mccmncCode10 = this.mImsi.substring(0, 5);
                                                        String[] strArr10 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                                        length = strArr10.length;
                                                        i = 0;
                                                        while (true) {
                                                            if (i < length) {
                                                                if (strArr10[i].equals(mccmncCode10)) {
                                                                    this.mMncLength = 2;
                                                                } else {
                                                                    i++;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                                }
                                                if (this.mMncLength == 0 || this.mMncLength == -1) {
                                                    if (this.mImsi == null) {
                                                        try {
                                                            String mccStr5 = this.mImsi.substring(0, 3);
                                                            if (!mccStr5.equals("404")) {
                                                                if (!mccStr5.equals("405")) {
                                                                    int mcc10 = Integer.parseInt(mccStr5);
                                                                    Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc10);
                                                                    this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc10);
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
                                                    StringBuilder sb11 = new StringBuilder();
                                                    sb11.append("GET_AD_DONE setSystemProperty simOperator=");
                                                    sb11.append(!Log.HWINFO ? getOperatorNumeric() : str);
                                                    log(sb11.toString());
                                                    setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                                    setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                                    StringBuilder sb12 = new StringBuilder();
                                                    sb12.append("update mccmnc=");
                                                    if (Log.HWINFO) {
                                                        str = this.mImsi.substring(0, this.mMncLength + 3);
                                                    }
                                                    sb12.append(str);
                                                    log(sb12.toString());
                                                    this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                                }
                                                if (((this.mImsi != null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true) && !this.mHwIccRecordsEx.getImsiReady()) {
                                                    this.mHwIccRecordsEx.setImsiReady(true);
                                                    this.mParentApp.notifyGetAdDone(null);
                                                    this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                                }
                                                this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                                updateOperatorPlmn();
                                                throw th2;
                                            }
                                        }
                                        updateOperatorPlmn();
                                        isRecordLoadResponse2 = isRecordLoadResponse;
                                    } else {
                                        isRecordLoadResponse = true;
                                    }
                                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 2) && this.mImsi != null && this.mImsi.length() >= 6) {
                                        String mccmncCode11 = this.mImsi.substring(0, 6);
                                        String[] strArr11 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                        int length11 = strArr11.length;
                                        int i13 = 0;
                                        while (true) {
                                            if (i13 < length11) {
                                                if (strArr11[i13].equals(mccmncCode11)) {
                                                    this.mMncLength = 3;
                                                    log("setting6 mMncLength=" + this.mMncLength);
                                                } else {
                                                    i13++;
                                                }
                                            }
                                        }
                                    }
                                    if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                        String mcc11 = this.mImsi.substring(0, 3);
                                        if (mcc11.equals("404") || mcc11.equals("405")) {
                                            String mccmncCode12 = this.mImsi.substring(0, 5);
                                            String[] strArr12 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                            int length12 = strArr12.length;
                                            int i14 = 0;
                                            while (true) {
                                                if (i14 < length12) {
                                                    if (strArr12[i14].equals(mccmncCode12)) {
                                                        this.mMncLength = 2;
                                                    } else {
                                                        i14++;
                                                    }
                                                }
                                            }
                                        }
                                        this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                    }
                                    if (this.mMncLength == 0 || this.mMncLength == -1) {
                                        if (this.mImsi != null) {
                                            try {
                                                String mccStr6 = this.mImsi.substring(0, 3);
                                                if (!mccStr6.equals("404")) {
                                                    if (!mccStr6.equals("405")) {
                                                        int mcc12 = Integer.parseInt(mccStr6);
                                                        Rlog.d(LOG_TAG, "SIMRecords: AD err, mcc is determing mnc length in error case::" + mcc12);
                                                        this.mMncLength = MccTable.smallestDigitsMccForMnc(mcc12);
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
                                        StringBuilder sb13 = new StringBuilder();
                                        sb13.append("GET_AD_DONE setSystemProperty simOperator=");
                                        sb13.append(Log.HWINFO ? getOperatorNumeric() : str);
                                        log(sb13.toString());
                                        setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                        setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                        StringBuilder sb14 = new StringBuilder();
                                        sb14.append("update mccmnc=");
                                        if (Log.HWINFO) {
                                            str = this.mImsi.substring(0, this.mMncLength + 3);
                                        }
                                        sb14.append(str);
                                        log(sb14.toString());
                                        this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                    }
                                    if (((this.mImsi == null || this.mMncLength == 0 || this.mMncLength == -1) ? false : true) && !this.mHwIccRecordsEx.getImsiReady()) {
                                        this.mHwIccRecordsEx.setImsiReady(true);
                                        this.mParentApp.notifyGetAdDone(null);
                                        this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                    }
                                    this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                    updateOperatorPlmn();
                                    isRecordLoadResponse2 = isRecordLoadResponse;
                                } catch (Throwable th6) {
                                    th2 = th6;
                                    String mccmncCode92 = this.mImsi.substring(0, 6);
                                    String[] strArr92 = MCCMNC_CODES_HAVING_3DIGITS_MNC;
                                    length2 = strArr92.length;
                                    i2 = 0;
                                    while (true) {
                                        if (i2 < length2) {
                                        }
                                        i2++;
                                    }
                                    String mcc92 = this.mImsi.substring(0, 3);
                                    String mccmncCode102 = this.mImsi.substring(0, 5);
                                    String[] strArr102 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
                                    length = strArr102.length;
                                    i = 0;
                                    while (true) {
                                        if (i < length) {
                                        }
                                        i++;
                                    }
                                    this.mHwIccRecordsEx.custMncLength(this.mImsi.substring(0, 3));
                                    if (this.mImsi == null) {
                                    }
                                    StringBuilder sb112 = new StringBuilder();
                                    sb112.append("GET_AD_DONE setSystemProperty simOperator=");
                                    sb112.append(!Log.HWINFO ? getOperatorNumeric() : str);
                                    log(sb112.toString());
                                    setSystemProperty("gsm.sim.operator.numeric", getOperatorNumeric());
                                    setSystemProperty(IccRecords.PROPERTY_MCC_MATCHING_FYROM, getOperatorNumeric());
                                    StringBuilder sb122 = new StringBuilder();
                                    sb122.append("update mccmnc=");
                                    if (Log.HWINFO) {
                                    }
                                    sb122.append(str);
                                    log(sb122.toString());
                                    this.mHwIccRecordsEx.updateMccMncConfigWithGplmn(this.mImsi.substring(0, this.mMncLength + 3));
                                    this.mHwIccRecordsEx.setImsiReady(true);
                                    this.mParentApp.notifyGetAdDone(null);
                                    this.mHwIccRecordsEx.onOperatorNumericLoadedHw();
                                    this.mHwIccRecordsEx.initFdnPsStatus(getSlotId());
                                    updateOperatorPlmn();
                                    throw th2;
                                }
                            } catch (RuntimeException e8) {
                                exc = e8;
                                logw("Exception parsing SIM record", exc);
                                if (!isRecordLoadResponse2) {
                                }
                                onRecordLoaded();
                            } catch (Throwable th7) {
                                th = th7;
                                if (isRecordLoadResponse2) {
                                }
                                throw th;
                            }
                            break;
                        case 10:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar8 = (AsyncResult) msg.obj;
                            if (ar8.exception == null) {
                                AdnRecord adn2 = (AdnRecord) ar8.result;
                                this.mMsisdn = adn2.getNumber();
                                this.mMsisdnTag = adn2.getAlphaTag();
                                log("MSISDN isempty:" + TextUtils.isEmpty(this.mMsisdn));
                                break;
                            } else {
                                log("Invalid or missing EF[MSISDN]");
                                break;
                            }
                        case 12:
                            isRecordLoadResponse2 = true;
                            getSpnFsm(false, (AsyncResult) msg.obj);
                            if (GetSpnFsmState.IDLE == this.mSpnState) {
                                this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 7, getServiceProviderName());
                            }
                            if ((this.mMncLength == -1 || this.mMncLength == 0 || this.mMncLength == 3) && this.mImsi != null && this.mImsi.length() >= 5) {
                                String mcc13 = this.mImsi.substring(0, 3);
                                if (mcc13.equals("404") || mcc13.equals("405")) {
                                    String mccmncCode13 = this.mImsi.substring(0, 5);
                                    if (!TextUtils.isEmpty(getServiceProviderName()) && getServiceProviderName().toLowerCase(Locale.US).contains("reliance")) {
                                        String[] strArr13 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC_ZERO_PREFIX_RELIANCE;
                                        int length13 = strArr13.length;
                                        while (true) {
                                            if (i4 >= length13) {
                                                break;
                                            } else if (strArr13[i4].equals(mccmncCode13)) {
                                                this.mMncLength = 2;
                                                break;
                                            } else {
                                                i4++;
                                            }
                                        }
                                    }
                                }
                            }
                        case 13:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar9 = (AsyncResult) msg.obj;
                            byte[] data6 = (byte[]) ar9.result;
                            if (ar9.exception == null) {
                                parseEfSpdi(data6);
                                break;
                            }
                            break;
                        case 14:
                            AsyncResult ar10 = (AsyncResult) msg.obj;
                            if (ar10.exception != null) {
                                logw("update failed. ", ar10.exception);
                                break;
                            }
                            break;
                        case 15:
                            isRecordLoadResponse2 = true;
                            AsyncResult ar11 = (AsyncResult) msg.obj;
                            byte[] data7 = (byte[]) ar11.result;
                            if (ar11.exception == null) {
                                SimTlv tlv = new SimTlv(data7, 0, data7.length);
                                while (true) {
                                    if (!tlv.isValidObject()) {
                                        break;
                                    } else if (tlv.getTag() == 67) {
                                        this.mPnnHomeName = IccUtils.networkNameToString(tlv.getData(), 0, tlv.getData().length);
                                        StringBuilder sb15 = new StringBuilder();
                                        sb15.append("PNN: ");
                                        if (Log.HWINFO) {
                                            str = this.mPnnHomeName;
                                        }
                                        sb15.append(str);
                                        log(sb15.toString());
                                        break;
                                    } else {
                                        tlv.nextObject();
                                    }
                                }
                            }
                            break;
                        default:
                            switch (i3) {
                                case 17:
                                    isRecordLoadResponse2 = true;
                                    AsyncResult ar12 = (AsyncResult) msg.obj;
                                    byte[] data8 = (byte[]) ar12.result;
                                    if (ar12.exception == null) {
                                        this.mUsimServiceTable = new UsimServiceTable(data8);
                                        log("SST: " + this.mUsimServiceTable);
                                        UsimServiceTableEx usimServiceTableEx = new UsimServiceTableEx();
                                        usimServiceTableEx.setUsimServiceTable(this.mUsimServiceTable);
                                        if (this.mHwIccRecordsEx.checkFileInServiceTable(IccConstants.EF_SPN, usimServiceTableEx, data8)) {
                                            getSpnFsm(true, null);
                                        } else {
                                            this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 7, null);
                                        }
                                        this.mHwIccRecordsEx.checkFileInServiceTable(IccConstants.EF_PNN, usimServiceTableEx, data8);
                                        break;
                                    } else {
                                        this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 7, null);
                                        break;
                                    }
                                case 18:
                                    isRecordLoadResponse2 = true;
                                    AsyncResult ar13 = (AsyncResult) msg.obj;
                                    if (ar13.exception == null) {
                                        handleSmses((ArrayList) ar13.result);
                                        break;
                                    }
                                    break;
                                case 19:
                                    Rlog.i("ENF", "marked read: sms " + msg.arg1);
                                    break;
                                case 20:
                                    isRecordLoadResponse2 = false;
                                    AsyncResult ar14 = (AsyncResult) msg.obj;
                                    log("EVENT_SET_MBDN_DONE ex:" + ar14.exception);
                                    if (ar14.exception == null) {
                                        this.mVoiceMailNum = this.mNewVoiceMailNum;
                                        this.mVoiceMailTag = this.mNewVoiceMailTag;
                                    }
                                    if (!isCphsMailboxEnabled()) {
                                        if (ar14.userObj != null) {
                                            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
                                            if (ar14.exception == null || configManager == null) {
                                                AsyncResult.forMessage((Message) ar14.userObj).exception = ar14.exception;
                                            } else {
                                                PersistableBundle b = configManager.getConfigForSubId(SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getPhoneId()));
                                                if (b == null || !b.getBoolean("editable_voicemail_number_bool")) {
                                                    AsyncResult.forMessage((Message) ar14.userObj).exception = ar14.exception;
                                                } else {
                                                    AsyncResult.forMessage((Message) ar14.userObj).exception = new IccVmNotSupportedException("Update SIM voice mailbox error");
                                                }
                                            }
                                            ((Message) ar14.userObj).sendToTarget();
                                            break;
                                        }
                                    } else {
                                        AdnRecord adn3 = new AdnRecord(this.mVoiceMailTag, this.mVoiceMailNum);
                                        Message onCphsCompleted = (Message) ar14.userObj;
                                        if (ar14.exception == null && ar14.userObj != null) {
                                            AsyncResult.forMessage((Message) ar14.userObj).exception = null;
                                            ((Message) ar14.userObj).sendToTarget();
                                            log("Callback with MBDN successful.");
                                            onCphsCompleted = null;
                                        }
                                        new AdnRecordLoader(this.mFh).updateEF(adn3, IccConstants.EF_MAILBOX_CPHS, IccConstants.EF_EXT1, 1, null, obtainMessage(25, onCphsCompleted));
                                        break;
                                    }
                                    break;
                                case 21:
                                    isRecordLoadResponse2 = false;
                                    AsyncResult ar15 = (AsyncResult) msg.obj;
                                    Integer index = (Integer) ar15.result;
                                    if (ar15.exception == null && index != null) {
                                        log("READ EF_SMS RECORD index=" + index);
                                        this.mFh.loadEFLinearFixed(IccConstants.EF_SMS, index.intValue(), obtainMessage(22));
                                        break;
                                    } else {
                                        loge("Error on SMS_ON_SIM with exp " + ar15.exception + " index " + index);
                                        break;
                                    }
                                case 22:
                                    isRecordLoadResponse2 = false;
                                    AsyncResult ar16 = (AsyncResult) msg.obj;
                                    if (ar16.exception != null) {
                                        loge("Error on GET_SMS with exp " + ar16.exception);
                                        break;
                                    } else {
                                        handleSms((byte[]) ar16.result);
                                        break;
                                    }
                                default:
                                    switch (i3) {
                                        case 24:
                                            isRecordLoadResponse2 = true;
                                            AsyncResult ar17 = (AsyncResult) msg.obj;
                                            byte[] data9 = (byte[]) ar17.result;
                                            if (ar17.exception == null) {
                                                log("EF_CFF_CPHS: " + IccUtils.bytesToHexString(data9));
                                                this.mEfCff = data9;
                                                break;
                                            } else {
                                                this.mEfCff = null;
                                                break;
                                            }
                                        case 25:
                                            isRecordLoadResponse2 = false;
                                            AsyncResult ar18 = (AsyncResult) msg.obj;
                                            if (ar18.exception == null) {
                                                this.mVoiceMailNum = this.mNewVoiceMailNum;
                                                this.mVoiceMailTag = this.mNewVoiceMailTag;
                                            } else {
                                                log("Set CPHS MailBox with exception: " + ar18.exception);
                                            }
                                            if (ar18.userObj != null) {
                                                log("Callback with CPHS MB successful.");
                                                AsyncResult.forMessage((Message) ar18.userObj).exception = ar18.exception;
                                                ((Message) ar18.userObj).sendToTarget();
                                                break;
                                            }
                                            break;
                                        case 26:
                                            isRecordLoadResponse2 = true;
                                            AsyncResult ar19 = (AsyncResult) msg.obj;
                                            if (ar19.exception == null) {
                                                this.mCphsInfo = (byte[]) ar19.result;
                                                log("iCPHS: " + IccUtils.bytesToHexString(this.mCphsInfo));
                                                break;
                                            }
                                            break;
                                        default:
                                            switch (i3) {
                                                case 32:
                                                    isRecordLoadResponse2 = true;
                                                    AsyncResult ar20 = (AsyncResult) msg.obj;
                                                    byte[] data10 = (byte[]) ar20.result;
                                                    if (ar20.exception == null) {
                                                        log("EF_CFIS: " + IccUtils.bytesToHexString(data10));
                                                        if (!validEfCfis(data10)) {
                                                            log("EF_CFIS: " + IccUtils.bytesToHexString(data10));
                                                            break;
                                                        } else {
                                                            this.mEfCfis = data10;
                                                            if ((data10[1] & 1) != 0) {
                                                                i4 = 1;
                                                            }
                                                            this.mCallForwardingStatus = i4;
                                                            log("EF_CFIS: callForwardingEnabled=" + this.mCallForwardingStatus);
                                                            this.mRecordsEventsRegistrants.notifyResult(1);
                                                            break;
                                                        }
                                                    } else {
                                                        this.mEfCfis = null;
                                                        String imsiOld = this.mHwIccRecordsEx.getVmSimImsi();
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
                                                    isRecordLoadResponse2 = true;
                                                    AsyncResult ar21 = (AsyncResult) msg.obj;
                                                    if (ar21.exception == null) {
                                                        byte[] data11 = (byte[]) ar21.result;
                                                        log("EF_CSP: " + IccUtils.bytesToHexString(data11));
                                                        boolean oldCspPlmnEnabled = this.mCspPlmnEnabled;
                                                        handleEfCspData(data11);
                                                        this.mHwIccRecordsEx.sendCspChangedBroadcast(oldCspPlmnEnabled, this.mCspPlmnEnabled);
                                                        break;
                                                    } else {
                                                        loge("Exception in fetching EF_CSP data " + ar21.exception);
                                                        break;
                                                    }
                                                case 34:
                                                    isRecordLoadResponse2 = true;
                                                    AsyncResult ar22 = (AsyncResult) msg.obj;
                                                    byte[] data12 = (byte[]) ar22.result;
                                                    if (ar22.exception == null) {
                                                        this.mGid1 = IccUtils.bytesToHexString(data12);
                                                        log("GID1: " + this.mGid1);
                                                        this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 5, this.mGid1);
                                                        break;
                                                    } else {
                                                        loge("Exception in get GID1 " + ar22.exception);
                                                        this.mGid1 = null;
                                                        this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 5, null);
                                                        break;
                                                    }
                                                default:
                                                    switch (i3) {
                                                        case 36:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar23 = (AsyncResult) msg.obj;
                                                            byte[] data13 = (byte[]) ar23.result;
                                                            if (ar23.exception == null) {
                                                                this.mGid2 = IccUtils.bytesToHexString(data13);
                                                                log("GID2: " + this.mGid2);
                                                                this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 6, this.mGid2);
                                                                break;
                                                            } else {
                                                                loge("Exception in get GID2 " + ar23.exception);
                                                                this.mGid2 = null;
                                                                this.mHwIccRecordsEx.updateCarrierFile(getSlotId(), 6, null);
                                                                break;
                                                            }
                                                        case 37:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar24 = (AsyncResult) msg.obj;
                                                            byte[] data14 = (byte[]) ar24.result;
                                                            if (ar24.exception == null && data14 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data14));
                                                                this.mPlmnActRecords = PlmnActRecord.getRecords(data14);
                                                                break;
                                                            } else {
                                                                loge("Failed getting User PLMN with Access Tech Records: " + ar24.exception);
                                                                break;
                                                            }
                                                        case 38:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar25 = (AsyncResult) msg.obj;
                                                            byte[] data15 = (byte[]) ar25.result;
                                                            if (ar25.exception == null && data15 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data15));
                                                                this.mOplmnActRecords = PlmnActRecord.getRecords(data15);
                                                                break;
                                                            } else {
                                                                loge("Failed getting Operator PLMN with Access Tech Records: " + ar25.exception);
                                                                break;
                                                            }
                                                        case 39:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar26 = (AsyncResult) msg.obj;
                                                            byte[] data16 = (byte[]) ar26.result;
                                                            if (ar26.exception == null && data16 != null) {
                                                                log("Received a PlmnActRecord, raw=" + IccUtils.bytesToHexString(data16));
                                                                this.mHplmnActRecords = PlmnActRecord.getRecords(data16);
                                                                log("HplmnActRecord[]=" + Arrays.toString(this.mHplmnActRecords));
                                                                break;
                                                            } else {
                                                                loge("Failed getting Home PLMN with Access Tech Records: " + ar26.exception);
                                                                break;
                                                            }
                                                        case 40:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar27 = (AsyncResult) msg.obj;
                                                            byte[] data17 = (byte[]) ar27.result;
                                                            if (ar27.exception == null && data17 != null) {
                                                                this.mEhplmns = parseBcdPlmnList(data17, "Equivalent Home");
                                                                break;
                                                            } else {
                                                                loge("Failed getting Equivalent Home PLMNs: " + ar27.exception);
                                                                break;
                                                            }
                                                        case 41:
                                                            isRecordLoadResponse2 = true;
                                                            AsyncResult ar28 = (AsyncResult) msg.obj;
                                                            byte[] data18 = (byte[]) ar28.result;
                                                            if (ar28.exception == null && data18 != null) {
                                                                this.mFplmns = parseBcdPlmnList(data18, "Forbidden");
                                                                if (msg.arg1 == 1238273) {
                                                                    isRecordLoadResponse2 = false;
                                                                    Message response = retrievePendingResponseMessage(Integer.valueOf(msg.arg2));
                                                                    if (response == null) {
                                                                        loge("Failed to retrieve a response message for FPLMN");
                                                                        break;
                                                                    } else {
                                                                        AsyncResult.forMessage(response, Arrays.copyOf(this.mFplmns, this.mFplmns.length), (Throwable) null);
                                                                        response.sendToTarget();
                                                                        break;
                                                                    }
                                                                }
                                                            } else {
                                                                loge("Failed getting Forbidden PLMNs: " + ar28.exception);
                                                                break;
                                                            }
                                                            break;
                                                        default:
                                                            super.handleMessage(msg);
                                                            break;
                                                    }
                                            }
                                    }
                            }
                    }
                } else {
                    onLocked(msg.what);
                }
            } else {
                isRecordLoadResponse2 = false;
                AsyncResult ar29 = (AsyncResult) msg.obj;
                if (ar29.exception == null) {
                    this.mMsisdn = this.mNewMsisdn;
                    this.mMsisdnTag = this.mNewMsisdnTag;
                    log("slot[" + getSlotId() + "]Success to update EF[MSISDN]");
                }
                if (ar29.userObj != null) {
                    AsyncResult.forMessage((Message) ar29.userObj).exception = ar29.exception;
                    ((Message) ar29.userObj).sendToTarget();
                }
            }
            if (!isRecordLoadResponse2) {
                return;
            }
        } catch (RuntimeException e9) {
            exc = e9;
            logw("Exception parsing SIM record", exc);
            if (!isRecordLoadResponse2) {
                return;
            }
            onRecordLoaded();
        }
        onRecordLoaded();
    }

    /* access modifiers changed from: private */
    public class EfPlLoaded implements IccRecords.IccRecordLoaded {
        private EfPlLoaded() {
        }

        /* synthetic */ EfPlLoaded(SIMRecords x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_PL";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfPl = (byte[]) ar.result;
            SIMRecords sIMRecords = SIMRecords.this;
            sIMRecords.log("EF_PL=" + IccUtils.bytesToHexString(SIMRecords.this.mEfPl));
        }
    }

    /* access modifiers changed from: private */
    public class EfUsimLiLoaded implements IccRecords.IccRecordLoaded {
        private EfUsimLiLoaded() {
        }

        /* synthetic */ EfUsimLiLoaded(SIMRecords x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_LI";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            SIMRecords.this.mEfLi = (byte[]) ar.result;
            SIMRecords sIMRecords = SIMRecords.this;
            sIMRecords.log("EF_LI=" + IccUtils.bytesToHexString(SIMRecords.this.mEfLi));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
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
            Rlog.d("ENF", "status : " + ((int) ba[0]));
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
                Rlog.i("ENF", "status " + i + ": " + ((int) ba[0]));
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
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRecordLoaded() {
        this.mRecordsToLoad--;
        if (getRecordsLoaded()) {
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
            return;
        }
        byte[] bArr = this.mEfCff;
        if (bArr != null) {
            if ((bArr[0] & 15) != 10) {
                i = 0;
            }
            this.mCallForwardingStatus = i;
            log("EF_CFF: callForwardingEnabled=" + this.mCallForwardingStatus);
            return;
        }
        this.mCallForwardingStatus = -1;
        log("EF_CFIS and EF_CFF not valid. callForwardingEnabled=" + this.mCallForwardingStatus);
    }

    private void setSimLanguageFromEF() {
        if (Resources.getSystem().getBoolean(17891567)) {
            setSimLanguage(this.mEfLi, this.mEfPl);
        } else {
            log("Not using EF LI/EF PL");
        }
    }

    private void onLockedAllRecordsLoaded() {
        setSimLanguageFromEF();
        setVoiceCallForwardingFlagFromSimRecords();
        if (this.mLockedRecordsReqReason == 1) {
            this.mLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else if (this.mLockedRecordsReqReason == 2) {
            this.mNetworkLockedRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        } else {
            loge("onLockedAllRecordsLoaded: unexpected mLockedRecordsReqReason " + this.mLockedRecordsReqReason);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onAllRecordsLoaded() {
        log("record load complete" + getSlotId());
        setSimLanguageFromEF();
        setVoiceCallForwardingFlagFromSimRecords();
        String operator = getOperatorNumeric();
        if (!TextUtils.isEmpty(operator)) {
            StringBuilder sb = new StringBuilder();
            sb.append("onAllRecordsLoaded set 'gsm.sim.operator.numeric' to operator='");
            sb.append(Log.HWINFO ? operator : "***");
            sb.append("'");
            log(sb.toString());
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mParentApp.getPhoneId(), operator);
        } else {
            log("onAllRecordsLoaded empty 'gsm.sim.operator.numeric' skipping");
        }
        String imsi = getIMSI();
        if (TextUtils.isEmpty(imsi) || imsi.length() < 3) {
            log("onAllRecordsLoaded empty imsi skipping setting mcc");
        } else {
            try {
                this.mTelephonyManager.setSimCountryIsoForPhone(this.mParentApp.getPhoneId(), MccTable.countryCodeForMcc(imsi.substring(0, 3)));
            } catch (RuntimeException exc) {
                logw("onAllRecordsLoaded: invalid IMSI with the exception ", exc);
            }
        }
        if (!VSimUtilsInner.isVSimSub(getSlotId())) {
            this.mHwIccRecordsEx.onAllRecordsLoadedHw();
        }
        VSimUtilsInner.setMarkForCardReload(getSlotId(), false);
        HwGetCfgFileConfig.readCfgFileConfig("xml/telephony-various.xml", getSlotId());
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    private void setVoiceMailByCountry(String spn) {
        if (this.mVmConfig.containsCarrierHw(spn)) {
            this.mIsVoiceMailFixed = this.mVmConfig.getVoiceMailFixed(spn);
            this.mVoiceMailNum = this.mVmConfig.getVoiceMailNumberHw(spn);
            this.mVoiceMailTag = this.mVmConfig.getVoiceMailTagHw(spn);
            this.mHwCustSIMRecords = (HwCustSIMRecords) HwCustUtils.createObj(HwCustSIMRecords.class, new Object[]{this.mContext});
            HwCustSIMRecords hwCustSIMRecords = this.mHwCustSIMRecords;
            if (hwCustSIMRecords != null && hwCustSIMRecords.isOpenRoamingVoiceMail()) {
                this.mHwCustSIMRecords.registerRoamingState(this.mVoiceMailNum);
            }
        }
    }

    public void getForbiddenPlmns(Message response) {
        this.mFh.loadEFTransparent(IccConstants.EF_FPLMN, obtainMessage(41, 1238273, storePendingResponseMessage(response)));
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onReady() {
        fetchSimRecords();
    }

    private void onLocked(int msg) {
        int i;
        log("only fetch EF_LI, EF_PL and EF_ICCID in locked state");
        if (msg == EVENT_APP_LOCKED) {
            i = 1;
        } else {
            i = 2;
        }
        this.mLockedRecordsReqReason = i;
        loadEfLiAndEfPl();
        if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
            this.mCi.getICCID(obtainMessage(4));
        } else {
            this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
        }
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
            this.mHwIccRecordsEx.refreshCardType();
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
            log("IMSI: mMncLength=" + this.mMncLength + ", mImsiLoad: " + this.mHwIccRecordsEx.getImsiReady());
            if (this.mImsi != null && this.mImsi.length() >= 6) {
                log("IMSI: " + this.mImsi.substring(0, 6) + Rlog.pii(LOG_TAG, this.mImsi.substring(6)));
            }
            String imsi = getIMSI();
            this.mHwIccRecordsEx.onImsiLoadedHw();
            this.mHwIccRecordsEx.updateSarMnc(imsi);
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
                    String[] strArr2 = IHwIccRecordsEx.MCCMNC_CODES_HAVING_2DIGITS_MNC;
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
            this.mHwIccRecordsEx.adapterForDoubleRilChannelAfterImsiReady();
            this.mImsiReadyRegistrants.notifyRegistrants();
        }
    }

    private void loadEfLiAndEfPl() {
        if (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_USIM) {
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

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void fetchSimRecords() {
        this.mRecordsRequested = true;
        log("fetchSimRecords " + this.mRecordsToLoad);
        this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
        this.mRecordsToLoad = this.mRecordsToLoad + 1;
        if (!this.mHwIccRecordsEx.getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(4));
            } else {
                this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(4));
            }
            this.mRecordsToLoad++;
        }
        if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
            log("fetchSimRecords: support fast fetch SIM records.");
            this.mHwIccRecordsEx.loadSimMatchedFileFromRilCache();
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
        this.mHwIccRecordsEx.getPbrRecordSize();
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
        this.mHwIccRecordsEx.loadEons();
        this.mHwIccRecordsEx.loadGID1();
        loadEfLiAndEfPl();
        this.mHwIccRecordsEx.loadCardSpecialFile(IccConstants.EF_HPLMN);
        this.mHwIccRecordsEx.loadCardSpecialFile(IccConstants.EF_OCSGL);
        log("fetchSimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getCarrierNameDisplayCondition() {
        return this.mCarrierNameDisplayCondition;
    }

    @UnsupportedAppUsage
    private void getSpnFsm(boolean start, AsyncResult ar) {
        if (start) {
            if (this.mSpnState == GetSpnFsmState.READ_SPN_3GPP || this.mSpnState == GetSpnFsmState.READ_SPN_CPHS || this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS || this.mSpnState == GetSpnFsmState.INIT) {
                this.mSpnState = GetSpnFsmState.INIT;
                return;
            }
            this.mSpnState = GetSpnFsmState.INIT;
        }
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[this.mSpnState.ordinal()];
        if (i != 1) {
            String str = "***";
            if (i == 2) {
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                } else {
                    byte[] data = (byte[]) ar.result;
                    this.mCarrierNameDisplayCondition = convertSpnDisplayConditionToBitmask(data[0] & 255);
                    setServiceProviderName(IccUtils.adnStringFieldToString(data, 1, data.length - 1));
                    String spn = getServiceProviderName();
                    if (spn == null || spn.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_CPHS;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Load EF_SPN: ");
                        if (Log.HWINFO) {
                            str = spn;
                        }
                        sb.append(str);
                        sb.append(" carrierNameDisplayCondition: ");
                        sb.append(this.mCarrierNameDisplayCondition);
                        log(sb.toString());
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_CPHS) {
                    if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
                        this.mHwIccRecordsEx.loadSimMatchedFileFromRilCacheByEfid(IccConstants.EF_SPN_CPHS);
                    } else {
                        this.mFh.loadEFTransparent(IccConstants.EF_SPN_CPHS, obtainMessage(12));
                    }
                    this.mRecordsToLoad++;
                    this.mCarrierNameDisplayCondition = 0;
                }
            } else if (i == 3) {
                if (ar == null || ar.exception != null) {
                    this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                } else {
                    byte[] data2 = (byte[]) ar.result;
                    setServiceProviderName(IccUtils.adnStringFieldToString(data2, 0, data2.length));
                    String spn2 = getServiceProviderName();
                    if (spn2 == null || spn2.length() == 0) {
                        this.mSpnState = GetSpnFsmState.READ_SPN_SHORT_CPHS;
                    } else {
                        this.mCarrierNameDisplayCondition = 0;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Load EF_SPN_CPHS: ");
                        if (Log.HWINFO) {
                            str = spn2;
                        }
                        sb2.append(str);
                        log(sb2.toString());
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn2);
                        this.mSpnState = GetSpnFsmState.IDLE;
                    }
                }
                if (this.mSpnState == GetSpnFsmState.READ_SPN_SHORT_CPHS) {
                    if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
                        this.mHwIccRecordsEx.loadSimMatchedFileFromRilCacheByEfid(IccConstants.EF_SPN_SHORT_CPHS);
                    } else {
                        this.mFh.loadEFTransparent(IccConstants.EF_SPN_SHORT_CPHS, obtainMessage(12));
                    }
                    this.mRecordsToLoad++;
                }
            } else if (i != 4) {
                this.mSpnState = GetSpnFsmState.IDLE;
            } else {
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
                        this.mCarrierNameDisplayCondition = 0;
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("Load EF_SPN_SHORT_CPHS: ");
                        if (Log.HWINFO) {
                            str = spn3;
                        }
                        sb3.append(str);
                        log(sb3.toString());
                        this.mTelephonyManager.setSimOperatorNameForPhone(this.mParentApp.getPhoneId(), spn3);
                    }
                }
                this.mSpnState = GetSpnFsmState.IDLE;
            }
        } else {
            setServiceProviderName(null);
            if (HW_IS_SUPPORT_FAST_FETCH_SIMINFO) {
                this.mHwIccRecordsEx.loadSimMatchedFileFromRilCacheByEfid(IccConstants.EF_SPN);
            } else {
                this.mFh.loadEFTransparent(IccConstants.EF_SPN, obtainMessage(12));
            }
            this.mRecordsToLoad++;
            this.mSpnState = GetSpnFsmState.READ_SPN_3GPP;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.uicc.SIMRecords$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState = new int[GetSpnFsmState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.INIT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.READ_SPN_3GPP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.READ_SPN_CPHS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$SIMRecords$GetSpnFsmState[GetSpnFsmState.READ_SPN_SHORT_CPHS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
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
            List<String> tmpSpdi = new ArrayList<>(plmnEntries.length / 3);
            for (int i = 0; i + 2 < plmnEntries.length; i += 3) {
                String plmnCode = IccUtils.bcdPlmnToString(plmnEntries, i);
                if (!TextUtils.isEmpty(plmnCode)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("EF_SPDI PLMN: ");
                    sb.append(Log.HWINFO ? plmnCode : "***");
                    log(sb.toString());
                    tmpSpdi.add(plmnCode);
                }
            }
            this.mSpdi = (String[]) tmpSpdi.toArray(new String[tmpSpdi.size()]);
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

    @UnsupportedAppUsage
    private boolean isCphsMailboxEnabled() {
        byte[] bArr = this.mCphsInfo;
        if (bArr != null && (bArr[1] & 48) == 48) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.i(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }

    /* access modifiers changed from: protected */
    public void logw(String s, Throwable tr) {
        Rlog.w(LOG_TAG, s, tr);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void logv(String s) {
        Rlog.v(LOG_TAG, s);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.IIccRecordsInner
    public boolean isCspPlmnEnabled() {
        return this.mCspPlmnEnabled;
    }

    private void handleEfCspData(byte[] data) {
        int usedCspGroups = data.length / 2;
        this.mCspPlmnEnabled = true;
        for (int i = 0; i < usedCspGroups; i++) {
            if (data[i * 2] == -64) {
                log("[CSP] found ValueAddedServicesGroup, value " + ((int) data[(i * 2) + 1]));
                if ((data[(i * 2) + 1] & 128) == 128) {
                    this.mCspPlmnEnabled = true;
                    return;
                }
                this.mCspPlmnEnabled = false;
                log("[CSP] Set Automatic Network Selection");
                this.mNetworkSelectionModeAutomaticRegistrants.notifyRegistrants();
                return;
            }
        }
        log("[CSP] Value Added Service Group (0xC0), not found!");
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
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
        pw.println(" mCarrierNameDisplayCondition=" + this.mCarrierNameDisplayCondition);
        pw.println(" mSpdi[]=" + this.mSpdi);
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
            this.mHwIccRecordsEx.setVmSimImsi(this.mImsi);
        }
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void initEventIdMap() {
        this.sEventIdMap.put("EVENT_GET_EHPLMN_DONE_FOR_APNCURE", 43);
        this.sEventIdMap.put("EVENT_GET_ICCID_DONE", 4);
        this.sEventIdMap.put("EVENT_GET_AD_DONE", 9);
        this.sEventIdMap.put("EVENT_GET_SST_DONE", 17);
        this.sEventIdMap.put("EVENT_GET_SPN_DONE", 12);
        this.sEventIdMap.put("EVENT_GET_GID1_DONE", 34);
        this.sEventIdMap.put("EVENT_GET_GID2_DONE", 36);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public int getEventIdFromMap(String event) {
        if (this.sEventIdMap.containsKey(event)) {
            return this.sEventIdMap.get(event).intValue();
        }
        log("Event Id not in the map.");
        return -1;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setVoiceMailByCountryHw(String spn) {
        setVoiceMailByCountry(spn);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void handleMessageEx(Message msg) {
        if (msg.what != 43) {
            handleMessage(msg);
            return;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        byte[] data = (byte[]) ar.result;
        if (ar.exception != null || data == null) {
            loge("Failed getting Equivalent Home PLMNs");
        } else {
            this.mEhplmns = parseBcdPlmnList(data, "Equivalent Home");
        }
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public VoiceMailConstants getVmConfig() {
        return this.mVmConfig;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void fetchSimRecordsHw() {
        fetchSimRecords();
    }
}
