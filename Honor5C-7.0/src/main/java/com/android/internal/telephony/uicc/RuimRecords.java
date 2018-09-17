package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CallFailCause;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded;
import com.android.internal.util.BitwiseInputStream;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class RuimRecords extends IccRecords {
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    protected static final int EVENT_GET_CDMA_GSM_IMSI_DONE = 37;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_SIM_APP_IMSI_DONE = 38;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_RUIM_REFRESH = 31;
    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = false;
    static final String LOG_TAG = "RuimRecords";
    private static boolean PLUS_TRANFER_IN_AP;
    protected String mCdmaGsmImsi;
    boolean mCsimSpnDisplayCondition;
    private byte[] mEFli;
    private byte[] mEFpl;
    private String mHomeNetworkId;
    private String mHomeSystemId;
    protected String mMdn;
    private String mMin;
    private String mMin2Min1;
    private String mMyMobileNumber;
    private String mNai;
    private boolean mOtaCommited;
    private String mPrlVersion;
    private boolean mRecordsRequired;

    private class EfCsimCdmaHomeLoaded implements IccRecordLoaded {
        private EfCsimCdmaHomeLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_CDMAHOME";
        }

        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> dataList = ar.result;
            RuimRecords.this.log("CSIM_CDMAHOME data size=" + dataList.size());
            if (!dataList.isEmpty()) {
                StringBuilder sidBuf = new StringBuilder();
                StringBuilder nidBuf = new StringBuilder();
                for (byte[] data : dataList) {
                    if (data.length == RuimRecords.EVENT_GET_ICCID_DONE) {
                        int nid = ((data[RuimRecords.EVENT_GET_IMSI_DONE] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[2] & PduHeaders.STORE_STATUS_ERROR_END);
                        sidBuf.append(((data[1] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[0] & PduHeaders.STORE_STATUS_ERROR_END)).append(',');
                        nidBuf.append(nid).append(',');
                    }
                }
                sidBuf.setLength(sidBuf.length() - 1);
                nidBuf.setLength(nidBuf.length() - 1);
                RuimRecords.this.mHomeSystemId = sidBuf.toString();
                RuimRecords.this.mHomeNetworkId = nidBuf.toString();
            }
        }
    }

    private class EfCsimEprlLoaded implements IccRecordLoaded {
        private EfCsimEprlLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.onGetCSimEprlDone(ar);
        }
    }

    private class EfCsimImsimLoaded implements IccRecordLoaded {
        private EfCsimImsimLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            boolean provisioned = (data[7] & PduPart.P_Q) == PduPart.P_Q ? true : RuimRecords.IS_MODEM_CAPABILITY_GET_ICCID_AT;
            if (HwModemCapability.isCapabilitySupport(RuimRecords.EVENT_GET_ALL_SMS_DONE)) {
                try {
                    RuimRecords.this.mImsi = RuimRecords.this.decodeCdmaImsi(data);
                    RuimRecords.this.mImsiReadyRegistrants.notifyRegistrants();
                    RuimRecords.this.log("IMSI: " + RuimRecords.this.mImsi.substring(0, RuimRecords.EVENT_GET_ICCID_DONE) + "xxxxxxxxx");
                    RuimRecords.this.updateMccMncConfigWithCplmn(RuimRecords.this.getRUIMOperatorNumeric());
                    if (!(RuimRecords.this.mParentApp == null || RuimRecords.this.mParentApp.getUiccCard() == null)) {
                        UiccCardApplication simApp = RuimRecords.this.mParentApp.getUiccCard().getApplication(1);
                        if (simApp != null) {
                            RuimRecords.this.mCi.getIMSIForApp(simApp.getAid(), RuimRecords.this.obtainMessage(RuimRecords.EVENT_GET_SIM_APP_IMSI_DONE));
                        }
                    }
                } catch (RuntimeException e) {
                    RuimRecords.this.loge("Illegal IMSI from CSIM_IMSIM=" + IccUtils.bytesToHexString(data));
                }
            }
            if (provisioned) {
                int first3digits = ((data[2] & RuimRecords.EVENT_GET_IMSI_DONE) << 8) + (data[1] & PduHeaders.STORE_STATUS_ERROR_END);
                int second3digits = (((data[RuimRecords.EVENT_GET_ICCID_DONE] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE] & PduHeaders.STORE_STATUS_ERROR_END)) >> 6;
                int digit7 = (data[RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE] >> 2) & 15;
                if (digit7 > 9) {
                    digit7 = 0;
                }
                int last3digits = ((data[RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE] & RuimRecords.EVENT_GET_IMSI_DONE) << 8) | (data[RuimRecords.EVENT_GET_IMSI_DONE] & PduHeaders.STORE_STATUS_ERROR_END);
                first3digits = RuimRecords.this.adjstMinDigits(first3digits);
                second3digits = RuimRecords.this.adjstMinDigits(second3digits);
                last3digits = RuimRecords.this.adjstMinDigits(last3digits);
                StringBuilder builder = new StringBuilder();
                builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(first3digits)}));
                builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(second3digits)}));
                builder.append(String.format(Locale.US, "%d", new Object[]{Integer.valueOf(digit7)}));
                builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(last3digits)}));
                RuimRecords.this.mMin = builder.toString();
                RuimRecords.this.log("min present=" + RuimRecords.this.mMin);
                return;
            }
            RuimRecords.this.log("min not present");
        }
    }

    private class EfCsimLiLoaded implements IccRecordLoaded {
        private EfCsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFli = (byte[]) ar.result;
            for (int i = 0; i < RuimRecords.this.mEFli.length; i += 2) {
                switch (RuimRecords.this.mEFli[i + 1]) {
                    case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 110;
                        break;
                    case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                        RuimRecords.this.mEFli[i] = (byte) 102;
                        RuimRecords.this.mEFli[i + 1] = (byte) 114;
                        break;
                    case RuimRecords.EVENT_GET_IMSI_DONE /*3*/:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 115;
                        break;
                    case RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE /*4*/:
                        RuimRecords.this.mEFli[i] = (byte) 106;
                        RuimRecords.this.mEFli[i + 1] = (byte) 97;
                        break;
                    case RuimRecords.EVENT_GET_ICCID_DONE /*5*/:
                        RuimRecords.this.mEFli[i] = (byte) 107;
                        RuimRecords.this.mEFli[i + 1] = (byte) 111;
                        break;
                    case CharacterSets.ISO_8859_3 /*6*/:
                        RuimRecords.this.mEFli[i] = (byte) 122;
                        RuimRecords.this.mEFli[i + 1] = (byte) 104;
                        break;
                    case CharacterSets.ISO_8859_4 /*7*/:
                        RuimRecords.this.mEFli[i] = (byte) 104;
                        RuimRecords.this.mEFli[i + 1] = (byte) 101;
                        break;
                    default:
                        RuimRecords.this.mEFli[i] = (byte) 32;
                        RuimRecords.this.mEFli[i + 1] = (byte) 32;
                        break;
                }
            }
            RuimRecords.this.log("EF_LI=" + IccUtils.bytesToHexString(RuimRecords.this.mEFli));
        }
    }

    private class EfCsimMdnLoaded implements IccRecordLoaded {
        private EfCsimMdnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MDN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_MDN=" + IccUtils.bytesToHexString(data));
            int mdnDigitsNum = data[0] & 15;
            RuimRecords.this.mMdn = IccUtils.cdmaBcdToString(data, 1, mdnDigitsNum);
            RuimRecords.this.log("CSIM MDN=" + RuimRecords.this.mMdn);
        }
    }

    private class EfCsimMipUppLoaded implements IccRecordLoaded {
        private EfCsimMipUppLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MIPUPP";
        }

        boolean checkLengthLegal(int length, int expectLength) {
            if (length >= expectLength) {
                return true;
            }
            Log.e(RuimRecords.LOG_TAG, "CSIM MIPUPP format error, length = " + length + "expected length at least =" + expectLength);
            return RuimRecords.IS_MODEM_CAPABILITY_GET_ICCID_AT;
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            if (data.length < 1) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read error");
                return;
            }
            BitwiseInputStream bitStream = new BitwiseInputStream(data);
            try {
                int mipUppLength = bitStream.read(8) << RuimRecords.EVENT_GET_IMSI_DONE;
                if (checkLengthLegal(mipUppLength, 1)) {
                    mipUppLength--;
                    if (bitStream.read(1) == 1) {
                        if (checkLengthLegal(mipUppLength, 11)) {
                            bitStream.skip(11);
                            mipUppLength -= 11;
                        } else {
                            return;
                        }
                    }
                    if (checkLengthLegal(mipUppLength, RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE)) {
                        int numNai = bitStream.read(RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE);
                        mipUppLength -= 4;
                        int index = 0;
                        while (index < numNai && checkLengthLegal(mipUppLength, RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE)) {
                            int naiEntryIndex = bitStream.read(RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE);
                            mipUppLength -= 4;
                            if (checkLengthLegal(mipUppLength, 8)) {
                                int naiLength = bitStream.read(8);
                                mipUppLength -= 8;
                                if (naiEntryIndex == 0) {
                                    if (checkLengthLegal(mipUppLength, naiLength << RuimRecords.EVENT_GET_IMSI_DONE)) {
                                        char[] naiCharArray = new char[naiLength];
                                        for (int index1 = 0; index1 < naiLength; index1++) {
                                            naiCharArray[index1] = (char) (bitStream.read(8) & PduHeaders.STORE_STATUS_ERROR_END);
                                        }
                                        RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                        }
                                        return;
                                    }
                                    return;
                                } else if (checkLengthLegal(mipUppLength, (naiLength << RuimRecords.EVENT_GET_IMSI_DONE) + CallFailCause.RECOVERY_ON_TIMER_EXPIRED)) {
                                    bitStream.skip((naiLength << RuimRecords.EVENT_GET_IMSI_DONE) + CallFailCause.MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE);
                                    mipUppLength -= (naiLength << RuimRecords.EVENT_GET_IMSI_DONE) + CallFailCause.RECOVERY_ON_TIMER_EXPIRED;
                                    if (bitStream.read(1) == 1) {
                                        if (checkLengthLegal(mipUppLength, 32)) {
                                            bitStream.skip(32);
                                            mipUppLength -= 32;
                                        } else {
                                            return;
                                        }
                                    }
                                    if (checkLengthLegal(mipUppLength, RuimRecords.EVENT_GET_ICCID_DONE)) {
                                        bitStream.skip(RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE);
                                        mipUppLength = (mipUppLength - 4) - 1;
                                        if (bitStream.read(1) == 1) {
                                            if (checkLengthLegal(mipUppLength, 32)) {
                                                bitStream.skip(32);
                                                mipUppLength -= 32;
                                            } else {
                                                return;
                                            }
                                        }
                                        index++;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read Exception error!");
            }
        }
    }

    private class EfCsimSpnLoaded implements IccRecordLoaded {
        private EfCsimSpnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRecordLoaded(AsyncResult ar) {
            boolean z;
            int len = 32;
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_SPN=" + IccUtils.bytesToHexString(data));
            RuimRecords ruimRecords = RuimRecords.this;
            if ((data[0] & 1) != 0) {
                z = true;
            } else {
                z = RuimRecords.IS_MODEM_CAPABILITY_GET_ICCID_AT;
            }
            ruimRecords.mCsimSpnDisplayCondition = z;
            int encoding = data[1];
            int language = data[2];
            byte[] spnData = new byte[32];
            if (data.length - 3 < 32) {
                len = data.length - 3;
            }
            System.arraycopy(data, RuimRecords.EVENT_GET_IMSI_DONE, spnData, 0, len);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & PduHeaders.STORE_STATUS_ERROR_END) != PduHeaders.STORE_STATUS_ERROR_END) {
                numBytes++;
            }
            if (numBytes == 0) {
                RuimRecords.this.setServiceProviderName("");
                return;
            }
            switch (encoding) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                case CharacterSets.ISO_8859_5 /*8*/:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
                    break;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    String spn = new String(spnData, 0, numBytes, "US-ASCII");
                    if (!TextUtils.isPrintableAsciiOnly(spn)) {
                        RuimRecords.this.log("Some corruption in SPN decoding = " + spn);
                        RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                        RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                        break;
                    }
                    RuimRecords.this.setServiceProviderName(spn);
                    break;
                case RuimRecords.EVENT_GET_IMSI_DONE /*3*/:
                case CharacterSets.ISO_8859_6 /*9*/:
                    RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                    break;
                case RuimRecords.EVENT_GET_DEVICE_IDENTITY_DONE /*4*/:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, CharacterSets.MIMENAME_UTF_16));
                    break;
                default:
                    try {
                        RuimRecords.this.log("SPN encoding not supported");
                        break;
                    } catch (Exception e) {
                        RuimRecords.this.log("spn decode error: " + e);
                        break;
                    }
            }
            RuimRecords.this.log("spn=" + RuimRecords.this.getServiceProviderName());
            RuimRecords.this.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
            RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
        }
    }

    private class EfPlLoaded implements IccRecordLoaded {
        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFpl = (byte[]) ar.result;
            RuimRecords.this.log("EF_PL=" + IccUtils.bytesToHexString(RuimRecords.this.mEFpl));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.RuimRecords.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.RuimRecords.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.RuimRecords.<clinit>():void");
    }

    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=" + "xxxx" + " mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=" + this.mMdn + " mMin=" + this.mMin + " mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    public String getCdmaGsmImsi() {
        return this.mCdmaGsmImsi;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mOtaCommited = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        this.mRecordsRequired = true;
        this.mEFpl = null;
        this.mEFli = null;
        this.mCsimSpnDisplayCondition = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        this.mAdnCache = HwTelephonyFactory.getHwUiccManager().createHwAdnRecordCache(this.mFh);
        this.mRecordsRequested = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        this.mRecordsToLoad = 0;
        this.mCi.registerForIccRefresh(this, EVENT_RUIM_REFRESH, null);
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        log("RuimRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing RuimRecords " + this);
        this.mCi.unregisterForIccRefresh(this);
        this.mParentApp.unregisterForReady(this);
        resetRecords();
        super.dispose();
    }

    protected void finalize() {
        log("RuimRecords finalized");
    }

    protected void resetRecords() {
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mAdnCache.reset();
        this.mRecordsRequested = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        this.mImsiLoad = IS_MODEM_CAPABILITY_GET_ICCID_AT;
    }

    public String getIMSI() {
        return this.mImsi;
    }

    public String getMdnNumber() {
        return this.mMyMobileNumber;
    }

    public String getCdmaMin() {
        return this.mMin2Min1;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    public String getNAI() {
        return this.mNai;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete).exception = new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        loge("method setVoiceMailNumber is not implemented");
    }

    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchRuimRecords();
        }
    }

    private int adjstMinDigits(int digits) {
        digits += CallFailCause.PROTOCOL_ERROR_UNSPECIFIED;
        if (digits % EVENT_GET_CDMA_SUBSCRIPTION_DONE == 0) {
            digits -= 10;
        }
        if ((digits / EVENT_GET_CDMA_SUBSCRIPTION_DONE) % EVENT_GET_CDMA_SUBSCRIPTION_DONE == 0) {
            digits -= 100;
        }
        if ((digits / 100) % EVENT_GET_CDMA_SUBSCRIPTION_DONE == 0) {
            return digits - 1000;
        }
        return digits;
    }

    public String getRUIMOperatorNumeric() {
        if (this.mImsi == null) {
            return null;
        }
        if (this.mMncLength != -1 && this.mMncLength != 0) {
            return this.mImsi.substring(0, this.mMncLength + EVENT_GET_IMSI_DONE);
        }
        try {
            return this.mImsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, EVENT_GET_IMSI_DONE))) + EVENT_GET_IMSI_DONE);
        } catch (RuntimeException e) {
            log("mImsi is not avalible,parseInt error," + e.getMessage() + ",so return null !");
            return null;
        }
    }

    public String getOperatorNumeric() {
        String tempOperatorNumeric;
        if (this.mImsi == null) {
            tempOperatorNumeric = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
            log("imsi is null tempOperatorNumeric = " + tempOperatorNumeric);
            return tempOperatorNumeric;
        } else if (this.mMncLength != -1 && this.mMncLength != 0) {
            return this.mImsi.substring(0, this.mMncLength + EVENT_GET_IMSI_DONE);
        } else {
            try {
                return this.mImsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, EVENT_GET_IMSI_DONE))) + EVENT_GET_IMSI_DONE);
            } catch (RuntimeException e) {
                tempOperatorNumeric = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
                log("mImsi is not avalible,parseInt error," + e.getMessage() + ",so return tempOperatorNumeric !");
                return tempOperatorNumeric;
            }
        }
    }

    private void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > EVENT_GET_IMSI_DONE) {
            this.mPrlVersion = Integer.toString(((data[2] & PduHeaders.STORE_STATUS_ERROR_END) << 8) | (data[EVENT_GET_IMSI_DONE] & PduHeaders.STORE_STATUS_ERROR_END));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            AsyncResult ar;
            switch (msg.what) {
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    onReady();
                    break;
                case EVENT_GET_IMSI_DONE /*3*/:
                    isRecordLoadResponse = true;
                    ar = msg.obj;
                    if (ar.exception == null) {
                        this.mImsi = (String) ar.result;
                        if (this.mImsi != null && (this.mImsi.length() < 6 || this.mImsi.length() > 15)) {
                            loge("invalid IMSI ");
                            this.mImsi = null;
                        }
                        String operatorNumeric = getRUIMOperatorNumeric();
                        log("NO update mccmnc=" + operatorNumeric);
                        updateMccMncConfigWithCplmn(operatorNumeric);
                        this.mImsiLoad = true;
                        this.mImsiReadyRegistrants.notifyRegistrants();
                        this.mCi.getCdmaGsmImsi(obtainMessage(EVENT_GET_CDMA_GSM_IMSI_DONE));
                        break;
                    }
                    loge("Exception querying IMSI, Exception:" + ar.exception);
                    break;
                case EVENT_GET_DEVICE_IDENTITY_DONE /*4*/:
                    log("Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
                    break;
                case EVENT_GET_ICCID_DONE /*5*/:
                    isRecordLoadResponse = true;
                    ar = (AsyncResult) msg.obj;
                    byte[] data = ar.result;
                    if (ar.exception == null) {
                        this.mIccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mIccId));
                        this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                        onIccIdLoadedHw();
                        this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                        break;
                    }
                    this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                    break;
                case EVENT_GET_CDMA_SUBSCRIPTION_DONE /*10*/:
                    ar = (AsyncResult) msg.obj;
                    String[] localTemp = ar.result;
                    if (ar.exception == null) {
                        this.mMyMobileNumber = localTemp[0];
                        this.mMin2Min1 = localTemp[EVENT_GET_IMSI_DONE];
                        this.mPrlVersion = localTemp[EVENT_GET_DEVICE_IDENTITY_DONE];
                        log("MDN: " + this.mMyMobileNumber + " MIN: " + this.mMin2Min1);
                        break;
                    }
                    break;
                case EVENT_UPDATE_DONE /*14*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Rlog.i(LOG_TAG, "RuimRecords update failed", ar.exception);
                        break;
                    }
                    break;
                case EVENT_GET_SST_DONE /*17*/:
                    log("Event EVENT_GET_SST_DONE Received");
                    break;
                case EVENT_GET_ALL_SMS_DONE /*18*/:
                case EVENT_MARK_SMS_READ_DONE /*19*/:
                case EVENT_SMS_ON_RUIM /*21*/:
                case EVENT_GET_SMS_DONE /*22*/:
                    Rlog.w(LOG_TAG, "Event not supported: " + msg.what);
                    break;
                case EVENT_RUIM_REFRESH /*31*/:
                    isRecordLoadResponse = IS_MODEM_CAPABILITY_GET_ICCID_AT;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        handleRuimRefresh((IccRefreshResponse) ar.result);
                        break;
                    }
                    break;
                case EVENT_GET_SIM_APP_IMSI_DONE /*38*/:
                    log("get SIM_APP_IMSI");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mCdmaGsmImsi = this.mImsi + "," + ((String) ar.result);
                        break;
                    } else {
                        Rlog.e(LOG_TAG, "Exception querying SIM APP IMSI, Exception:" + ar.exception);
                        break;
                    }
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        }
    }

    private static String[] getAssetLanguages(Context ctx) {
        String[] locales = ctx.getAssets().getLocales();
        String[] localeLangs = new String[locales.length];
        for (int i = 0; i < locales.length; i++) {
            String localeStr = locales[i];
            int separator = localeStr.indexOf(45);
            if (separator < 0) {
                localeLangs[i] = localeStr;
            } else {
                localeLangs[i] = localeStr.substring(0, separator);
            }
        }
        return localeLangs;
    }

    protected void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        if (this.mRecordsToLoad == 0 && this.mRecordsRequested) {
            onAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    protected void onAllRecordsLoaded() {
        log("record load complete");
        if (PLUS_TRANFER_IN_AP && this.mImsi != null) {
            SystemProperties.set("ril.radio.cdma.icc_mcc", this.mImsi.substring(0, EVENT_GET_IMSI_DONE));
        }
        if (Resources.getSystem().getBoolean(17957023)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        if (!TextUtils.isEmpty(this.mMdn)) {
            int[] subIds = SubscriptionController.getInstance().getSubId(this.mParentApp.getUiccCard().getPhoneId());
            if (subIds != null) {
                SubscriptionManager.from(this.mContext).setDisplayNumber(this.mMdn, subIds[0]);
            } else {
                log("Cannot call setDisplayNumber: invalid subId");
            }
        }
    }

    public void onReady() {
        fetchRuimRecords();
        this.mCi.getCDMASubscription(obtainMessage(EVENT_GET_CDMA_SUBSCRIPTION_DONE));
    }

    void recordsRequired() {
        log("recordsRequired");
        this.mRecordsRequired = true;
        fetchRuimRecords();
    }

    private void fetchRuimRecords() {
        if (this.mParentApp != null) {
            if (!this.mRecordsRequested && this.mRecordsRequired && AppState.APPSTATE_READY == this.mParentApp.getState()) {
                this.mRecordsRequested = true;
                log("fetchRuimRecords " + this.mRecordsToLoad);
                if (!HwModemCapability.isCapabilitySupport(EVENT_GET_ALL_SMS_DONE)) {
                    this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(EVENT_GET_IMSI_DONE));
                    this.mRecordsToLoad++;
                }
                if (!getIccidSwitch()) {
                    if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                        this.mCi.getICCID(obtainMessage(EVENT_GET_ICCID_DONE));
                    } else {
                        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(EVENT_GET_ICCID_DONE));
                    }
                    this.mRecordsToLoad++;
                }
                getPbrRecordSize();
                this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_LI, obtainMessage(100, new EfCsimLiLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_RUIM_SPN, obtainMessage(100, new EfCsimSpnLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFLinearFixed(IccConstants.EF_CSIM_MDN, 1, obtainMessage(100, new EfCsimMdnLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_IMSIM, obtainMessage(100, new EfCsimImsimLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_CSIM_CDMAHOME, obtainMessage(100, new EfCsimCdmaHomeLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_EPRL, EVENT_GET_DEVICE_IDENTITY_DONE, obtainMessage(100, new EfCsimEprlLoaded()));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_MIPUPP, obtainMessage(100, new EfCsimMipUppLoaded()));
                this.mRecordsToLoad++;
                log("fetchRuimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
                return;
            }
            log("fetchRuimRecords: Abort fetching records mRecordsRequested = " + this.mRecordsRequested + " state = " + this.mParentApp.getState() + " required = " + this.mRecordsRequired);
        }
    }

    public int getDisplayRule(String plmn) {
        return 0;
    }

    public boolean isProvisioned() {
        if (SystemProperties.getBoolean("persist.radio.test-csim", IS_MODEM_CAPABILITY_GET_ICCID_AT)) {
            return true;
        }
        if (this.mParentApp == null) {
            return IS_MODEM_CAPABILITY_GET_ICCID_AT;
        }
        return (this.mParentApp.getType() == AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? IS_MODEM_CAPABILITY_GET_ICCID_AT : true;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        log("RuimRecords:setVoiceMessageWaiting - NOP for CDMA");
    }

    public int getVoiceMessageCount() {
        log("RuimRecords:getVoiceMessageCount - NOP for CDMA");
        return 0;
    }

    private void handleRuimRefresh(IccRefreshResponse refreshResponse) {
        if (refreshResponse == null) {
            log("handleRuimRefresh received without input");
        } else if ((refreshResponse.aid == null || refreshResponse.aid.equals(this.mParentApp.getAid())) && !beforeHandleRuimRefresh(refreshResponse)) {
            switch (refreshResponse.refreshResult) {
                case PduPersister.LOAD_MODE_MMS_COMMON /*0*/:
                    log("handleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                    this.mAdnCache.reset();
                    fetchRuimRecords();
                    break;
                case PduPersister.PROC_STATUS_TRANSIENT_FAILURE /*1*/:
                    log("handleRuimRefresh with SIM_REFRESH_INIT");
                    onIccRefreshInit();
                    break;
                case PduPersister.PROC_STATUS_PERMANENTLY_FAILURE /*2*/:
                    log("handleRuimRefresh with SIM_REFRESH_RESET");
                    break;
                default:
                    log("handleRuimRefresh with unknown operation");
                    break;
            }
            if (!afterHandleRuimRefresh(refreshResponse)) {
            }
        }
    }

    public String getMdn() {
        return this.mMdn;
    }

    public String getMin() {
        return this.mMin;
    }

    public String getSid() {
        return this.mHomeSystemId;
    }

    public String getNid() {
        return this.mHomeNetworkId;
    }

    public boolean getCsimSpnDisplayCondition() {
        return this.mCsimSpnDisplayCondition;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, "[RuimRecords] " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "[RuimRecords] " + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RuimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mOtaCommited=" + this.mOtaCommited);
        pw.println(" mMyMobileNumber=" + this.mMyMobileNumber);
        pw.println(" mMin2Min1=" + this.mMin2Min1);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mEFpl[]=" + Arrays.toString(this.mEFpl));
        pw.println(" mEFli[]=" + Arrays.toString(this.mEFli));
        pw.println(" mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.flush();
    }

    private void updateMccMncConfigWithCplmn(String operatorNumeric) {
        log("updateMccMncConfigWithCplmn: " + operatorNumeric);
        if (operatorNumeric != null && operatorNumeric.length() >= EVENT_GET_ICCID_DONE) {
            setSystemProperty("gsm.sim.operator.numeric", operatorNumeric);
            MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, IS_MODEM_CAPABILITY_GET_ICCID_AT);
        }
    }
}
