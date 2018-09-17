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
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    static final String LOG_TAG = "RuimRecords";
    private static boolean PLUS_TRANFER_IN_AP = (HwModemCapability.isCapabilitySupport(2) ^ 1);
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
        /* synthetic */ EfCsimCdmaHomeLoaded(RuimRecords this$0, EfCsimCdmaHomeLoaded -this1) {
            this();
        }

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
                    if (data.length == 5) {
                        int nid = ((data[3] & 255) << 8) | (data[2] & 255);
                        sidBuf.append(((data[1] & 255) << 8) | (data[0] & 255)).append(',');
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
        /* synthetic */ EfCsimEprlLoaded(RuimRecords this$0, EfCsimEprlLoaded -this1) {
            this();
        }

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
        /* synthetic */ EfCsimImsimLoaded(RuimRecords this$0, EfCsimImsimLoaded -this1) {
            this();
        }

        private EfCsimImsimLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            boolean provisioned = (data[7] & 128) == 128;
            if (HwModemCapability.isCapabilitySupport(18)) {
                try {
                    RuimRecords.this.mImsi = RuimRecords.this.decodeCdmaImsi(data);
                    RuimRecords.this.mImsiReadyRegistrants.notifyRegistrants();
                    RuimRecords.this.log("IMSI: " + RuimRecords.this.mImsi.substring(0, 5) + "xxxxxxxxx");
                    RuimRecords.this.updateMccMncConfigWithCplmn(RuimRecords.this.getRUIMOperatorNumeric());
                    if (!(RuimRecords.this.mParentApp == null || RuimRecords.this.mParentApp.getUiccCard() == null)) {
                        UiccCardApplication simApp = RuimRecords.this.mParentApp.getUiccCard().getApplication(1);
                        if (simApp != null) {
                            RuimRecords.this.mCi.getIMSIForApp(simApp.getAid(), RuimRecords.this.obtainMessage(38));
                        }
                    }
                } catch (RuntimeException e) {
                    RuimRecords.this.loge("Illegal IMSI from CSIM_IMSIM=" + IccUtils.bytesToHexString(data));
                }
            }
            if (provisioned) {
                int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
                int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
                int digit7 = (data[4] >> 2) & 15;
                if (digit7 > 9) {
                    digit7 = 0;
                }
                int last3digits = ((data[4] & 3) << 8) | (data[3] & 255);
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
        /* synthetic */ EfCsimLiLoaded(RuimRecords this$0, EfCsimLiLoaded -this1) {
            this();
        }

        private EfCsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFli = (byte[]) ar.result;
            for (int i = 0; i < RuimRecords.this.mEFli.length; i += 2) {
                switch (RuimRecords.this.mEFli[i + 1]) {
                    case (byte) 1:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 110;
                        break;
                    case (byte) 2:
                        RuimRecords.this.mEFli[i] = (byte) 102;
                        RuimRecords.this.mEFli[i + 1] = (byte) 114;
                        break;
                    case (byte) 3:
                        RuimRecords.this.mEFli[i] = (byte) 101;
                        RuimRecords.this.mEFli[i + 1] = (byte) 115;
                        break;
                    case (byte) 4:
                        RuimRecords.this.mEFli[i] = (byte) 106;
                        RuimRecords.this.mEFli[i + 1] = (byte) 97;
                        break;
                    case (byte) 5:
                        RuimRecords.this.mEFli[i] = (byte) 107;
                        RuimRecords.this.mEFli[i + 1] = (byte) 111;
                        break;
                    case (byte) 6:
                        RuimRecords.this.mEFli[i] = (byte) 122;
                        RuimRecords.this.mEFli[i + 1] = (byte) 104;
                        break;
                    case (byte) 7:
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
        /* synthetic */ EfCsimMdnLoaded(RuimRecords this$0, EfCsimMdnLoaded -this1) {
            this();
        }

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
        /* synthetic */ EfCsimMipUppLoaded(RuimRecords this$0, EfCsimMipUppLoaded -this1) {
            this();
        }

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
            return false;
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = ar.result;
            if (data.length < 1) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read error");
                return;
            }
            BitwiseInputStream bitStream = new BitwiseInputStream(data);
            try {
                int mipUppLength = bitStream.read(8) << 3;
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
                    if (checkLengthLegal(mipUppLength, 4)) {
                        int numNai = bitStream.read(4);
                        mipUppLength -= 4;
                        int index = 0;
                        while (index < numNai && checkLengthLegal(mipUppLength, 4)) {
                            int naiEntryIndex = bitStream.read(4);
                            mipUppLength -= 4;
                            if (checkLengthLegal(mipUppLength, 8)) {
                                int naiLength = bitStream.read(8);
                                mipUppLength -= 8;
                                if (naiEntryIndex == 0) {
                                    if (checkLengthLegal(mipUppLength, naiLength << 3)) {
                                        char[] naiCharArray = new char[naiLength];
                                        for (int index1 = 0; index1 < naiLength; index1++) {
                                            naiCharArray[index1] = (char) (bitStream.read(8) & 255);
                                        }
                                        RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                        }
                                        return;
                                    }
                                    return;
                                }
                                if (checkLengthLegal(mipUppLength, (naiLength << 3) + 102)) {
                                    bitStream.skip((naiLength << 3) + 101);
                                    mipUppLength -= (naiLength << 3) + 102;
                                    if (bitStream.read(1) == 1) {
                                        if (checkLengthLegal(mipUppLength, 32)) {
                                            bitStream.skip(32);
                                            mipUppLength -= 32;
                                        } else {
                                            return;
                                        }
                                    }
                                    if (checkLengthLegal(mipUppLength, 5)) {
                                        bitStream.skip(4);
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
                                }
                                return;
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
        /* synthetic */ EfCsimSpnLoaded(RuimRecords this$0, EfCsimSpnLoaded -this1) {
            this();
        }

        private EfCsimSpnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onRecordLoaded(AsyncResult ar) {
            boolean z;
            byte[] data = ar.result;
            RuimRecords.this.log("CSIM_SPN=" + IccUtils.bytesToHexString(data));
            RuimRecords ruimRecords = RuimRecords.this;
            if ((data[0] & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            ruimRecords.mCsimSpnDisplayCondition = z;
            int encoding = data[1];
            int language = data[2];
            byte[] spnData = new byte[32];
            System.arraycopy(data, 3, spnData, 0, data.length + -3 < 32 ? data.length - 3 : 32);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & 255) != 255) {
                numBytes++;
            }
            if (numBytes == 0) {
                RuimRecords.this.setServiceProviderName("");
                return;
            }
            switch (encoding) {
                case 0:
                case 8:
                    RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
                    break;
                case 2:
                    String spn = new String(spnData, 0, numBytes, "US-ASCII");
                    if (!TextUtils.isPrintableAsciiOnly(spn)) {
                        RuimRecords.this.log("Some corruption in SPN decoding = " + spn);
                        RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                        RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                        break;
                    }
                    RuimRecords.this.setServiceProviderName(spn);
                    break;
                case 3:
                case 9:
                    RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                    break;
                case 4:
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
        /* synthetic */ EfPlLoaded(RuimRecords this$0, EfPlLoaded -this1) {
            this();
        }

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

    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=" + "xxxx" + " mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=" + this.mMdn + " mMin=" + this.mMin + " mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    public String getCdmaGsmImsi() {
        return this.mCdmaGsmImsi;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mOtaCommited = false;
        this.mRecordsRequired = true;
        this.mEFpl = null;
        this.mEFli = null;
        this.mCsimSpnDisplayCondition = false;
        this.mAdnCache = HwTelephonyFactory.getHwUiccManager().createHwAdnRecordCache(this.mFh);
        this.mRecordsRequested = false;
        this.mRecordsToLoad = 0;
        this.mCi.registerForIccRefresh(this, 31, null);
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
        this.mRecordsRequested = false;
        this.mImsiLoad = false;
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
        digits += 111;
        if (digits % 10 == 0) {
            digits -= 10;
        }
        if ((digits / 10) % 10 == 0) {
            digits -= 100;
        }
        if ((digits / 100) % 10 == 0) {
            return digits - 1000;
        }
        return digits;
    }

    public String getRUIMOperatorNumeric() {
        if (this.mImsi == null) {
            return null;
        }
        if (this.mMncLength != -1 && this.mMncLength != 0) {
            return this.mImsi.substring(0, this.mMncLength + 3);
        }
        try {
            return this.mImsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3))) + 3);
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
            return this.mImsi.substring(0, this.mMncLength + 3);
        } else {
            try {
                return this.mImsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3))) + 3);
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
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            AsyncResult ar;
            switch (msg.what) {
                case 1:
                    onReady();
                    break;
                case 3:
                    isRecordLoadResponse = true;
                    onGetImsiDone(msg);
                    break;
                case 4:
                    log("Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
                    break;
                case 5:
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
                case 10:
                    ar = (AsyncResult) msg.obj;
                    String[] localTemp = ar.result;
                    if (ar.exception == null) {
                        this.mMyMobileNumber = localTemp[0];
                        this.mMin2Min1 = localTemp[3];
                        this.mPrlVersion = localTemp[4];
                        log("MDN: " + this.mMyMobileNumber + " MIN: " + this.mMin2Min1);
                        break;
                    }
                    break;
                case 14:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        Rlog.i(LOG_TAG, "RuimRecords update failed", ar.exception);
                        break;
                    }
                    break;
                case 17:
                    log("Event EVENT_GET_SST_DONE Received");
                    break;
                case 18:
                case 19:
                case 21:
                case 22:
                    Rlog.w(LOG_TAG, "Event not supported: " + msg.what);
                    break;
                case 31:
                    isRecordLoadResponse = false;
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        handleRuimRefresh((IccRefreshResponse) ar.result);
                        break;
                    }
                    break;
                case 38:
                    log("get SIM_APP_IMSI");
                    ar = msg.obj;
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
            SystemProperties.set("ril.radio.cdma.icc_mcc", this.mImsi.substring(0, 3));
        }
        if (Resources.getSystem().getBoolean(17957043)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        if (!TextUtils.isEmpty(this.mMdn)) {
            int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getUiccCard().getPhoneId());
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                SubscriptionManager.from(this.mContext).setDisplayNumber(this.mMdn, subId);
            } else {
                log("Cannot call setDisplayNumber: invalid subId");
            }
        }
    }

    public void onReady() {
        fetchRuimRecords();
        this.mCi.getCDMASubscription(obtainMessage(10));
    }

    public void onGetImsiDone(Message msg) {
        if (msg != null) {
            AsyncResult ar = msg.obj;
            if (ar.exception != null) {
                loge("Exception querying IMSI, Exception:" + ar.exception);
                return;
            }
            this.mImsi = (String) ar.result;
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
            String operatorNumeric = getRUIMOperatorNumeric();
            log("NO update mccmnc=" + operatorNumeric);
            updateMccMncConfigWithCplmn(operatorNumeric);
            this.mImsiLoad = true;
            this.mImsiReadyRegistrants.notifyRegistrants();
            this.mCi.getCdmaGsmImsi(obtainMessage(37));
        }
    }

    void recordsRequired() {
        log("recordsRequired");
        this.mRecordsRequired = true;
        fetchRuimRecords();
    }

    private void fetchRuimRecords() {
        if (this.mParentApp != null) {
            if (!this.mRecordsRequested && (this.mRecordsRequired ^ 1) == 0 && AppState.APPSTATE_READY == this.mParentApp.getState()) {
                this.mRecordsRequested = true;
                log("fetchRuimRecords " + this.mRecordsToLoad);
                if (!HwModemCapability.isCapabilitySupport(18)) {
                    this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
                    this.mRecordsToLoad++;
                }
                if (!getIccidSwitch()) {
                    if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                        this.mCi.getICCID(obtainMessage(5));
                    } else {
                        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
                    }
                    this.mRecordsToLoad++;
                }
                getPbrRecordSize();
                this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(28474, obtainMessage(100, new EfCsimLiLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(28481, obtainMessage(100, new EfCsimSpnLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFLinearFixed(IccConstants.EF_CSIM_MDN, 1, obtainMessage(100, new EfCsimMdnLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_IMSIM, obtainMessage(100, new EfCsimImsimLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFLinearFixedAll(IccConstants.EF_CSIM_CDMAHOME, obtainMessage(100, new EfCsimCdmaHomeLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_EPRL, 4, obtainMessage(100, new EfCsimEprlLoaded(this, null)));
                this.mRecordsToLoad++;
                this.mFh.loadEFTransparent(IccConstants.EF_CSIM_MIPUPP, obtainMessage(100, new EfCsimMipUppLoaded(this, null)));
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
        if (SystemProperties.getBoolean("persist.radio.test-csim", false)) {
            return true;
        }
        if (this.mParentApp == null) {
            return false;
        }
        return (this.mParentApp.getType() == AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? false : true;
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
        } else if ((TextUtils.isEmpty(refreshResponse.aid) || (refreshResponse.aid.equals(this.mParentApp.getAid()) ^ 1) == 0) && !beforeHandleRuimRefresh(refreshResponse)) {
            switch (refreshResponse.refreshResult) {
                case 0:
                    log("handleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                    this.mAdnCache.reset();
                    fetchRuimRecords();
                    break;
                case 1:
                    log("handleRuimRefresh with SIM_REFRESH_INIT");
                    onIccRefreshInit();
                    break;
                case 2:
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
        if (operatorNumeric != null && operatorNumeric.length() >= 5) {
            setSystemProperty("gsm.sim.operator.numeric", operatorNumeric);
            MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
        }
    }
}
