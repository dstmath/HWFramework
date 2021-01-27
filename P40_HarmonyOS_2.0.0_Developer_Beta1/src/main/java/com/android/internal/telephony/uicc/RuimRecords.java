package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
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
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.util.BitwiseInputStream;
import com.google.android.mms.pdu.CharacterSets;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.UiccCardApplicationEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

public class RuimRecords extends IccRecords {
    private static final int EVENT_APP_LOCKED = 32;
    private static final int EVENT_APP_NETWORK_LOCKED = 33;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_IMSI_DONE = 3;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    static final String LOG_TAG = "RuimRecords";
    private static boolean PLUS_TRANFER_IN_AP = (!HwModemCapability.isCapabilitySupport(2));
    protected String mCdmaGsmImsi;
    boolean mCsimSpnDisplayCondition = false;
    @UnsupportedAppUsage
    private byte[] mEFli = null;
    @UnsupportedAppUsage
    private byte[] mEFpl = null;
    private String mHomeNetworkId;
    private String mHomeSystemId;
    private String mMdn;
    @UnsupportedAppUsage
    private String mMin;
    private String mMin2Min1;
    private String mMyMobileNumber;
    @UnsupportedAppUsage
    private String mNai;
    private boolean mOtaCommited = false;
    private String mPrlVersion;
    private boolean mRecordsRequired = true;

    @Override // com.android.internal.telephony.uicc.IccRecords, android.os.Handler, java.lang.Object
    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=xxxx mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=xxxx mMin=xxxx mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public String getCdmaGsmImsi() {
        return this.mCdmaGsmImsi;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = new AdnRecordCache(this.mFh);
        UiccCardApplicationEx uiccCardApplicationEx = new UiccCardApplicationEx();
        uiccCardApplicationEx.setUiccCardApplication(app);
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(ci);
        this.mHwIccRecordsEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwRUIMRecordsEx(this, uiccCardApplicationEx, c, commandsInterfaceEx);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mHwIccRecordsEx.resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 32, null);
        this.mParentApp.registerForNetworkLocked(this, 33, null);
        log("RuimRecords X ctor this=" + this);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dispose() {
        log("Disposing RuimRecords " + this);
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
        log("RuimRecords finalized");
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        this.mMncLength = -1;
        log("setting0 mMncLength" + this.mMncLength);
        this.mIccId = null;
        this.mFullIccId = null;
        this.mAdnCache.reset();
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mLoaded.set(false);
        this.mHwIccRecordsEx.setImsiReady(false);
    }

    @UnsupportedAppUsage
    public String getMdnNumber() {
        return this.mMyMobileNumber;
    }

    public String getCdmaMin() {
        return this.mMin2Min1;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public String getNAI() {
        return this.mNai;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete) {
        AsyncResult.forMessage(onComplete).exception = new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        loge("method setVoiceMailNumber is not implemented");
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchRuimRecords();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private int adjstMinDigits(int digits) {
        int digits2 = digits + 111;
        int digits3 = digits2 % 10 == 0 ? digits2 - 10 : digits2;
        int digits4 = (digits3 / 10) % 10 == 0 ? digits3 - 100 : digits3;
        return (digits4 / 100) % 10 == 0 ? digits4 - 1000 : digits4;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    @UnsupportedAppUsage
    public String getRUIMOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            return null;
        }
        if (this.mMncLength != -1 && this.mMncLength != 0) {
            return imsi.substring(0, this.mMncLength + 3);
        }
        try {
            return this.mImsi.substring(0, MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3))) + 3);
        } catch (RuntimeException e) {
            log("mImsi is not avalible,parseInt error," + e.getMessage() + ",so return null !");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class EfPlLoaded implements IccRecords.IccRecordLoaded {
        private EfPlLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_PL";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFpl = (byte[]) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("EF_PL=" + IccUtils.bytesToHexString(RuimRecords.this.mEFpl));
        }
    }

    /* access modifiers changed from: private */
    public class EfCsimLiLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimLiLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_LI";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.mEFli = (byte[]) ar.result;
            for (int i = 0; i < RuimRecords.this.mEFli.length; i += 2) {
                switch (RuimRecords.this.mEFli[i + 1]) {
                    case 1:
                        RuimRecords.this.mEFli[i] = 101;
                        RuimRecords.this.mEFli[i + 1] = 110;
                        break;
                    case 2:
                        RuimRecords.this.mEFli[i] = 102;
                        RuimRecords.this.mEFli[i + 1] = 114;
                        break;
                    case 3:
                        RuimRecords.this.mEFli[i] = 101;
                        RuimRecords.this.mEFli[i + 1] = 115;
                        break;
                    case 4:
                        RuimRecords.this.mEFli[i] = 106;
                        RuimRecords.this.mEFli[i + 1] = 97;
                        break;
                    case 5:
                        RuimRecords.this.mEFli[i] = 107;
                        RuimRecords.this.mEFli[i + 1] = 111;
                        break;
                    case 6:
                        RuimRecords.this.mEFli[i] = 122;
                        RuimRecords.this.mEFli[i + 1] = 104;
                        break;
                    case 7:
                        RuimRecords.this.mEFli[i] = 104;
                        RuimRecords.this.mEFli[i + 1] = 101;
                        break;
                    default:
                        RuimRecords.this.mEFli[i] = 32;
                        RuimRecords.this.mEFli[i + 1] = 32;
                        break;
                }
            }
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("EF_LI=" + IccUtils.bytesToHexString(RuimRecords.this.mEFli));
        }
    }

    /* access modifiers changed from: private */
    public class EfCsimSpnLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimSpnLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        /* JADX WARNING: Removed duplicated region for block: B:47:0x0102  */
        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            StringBuilder sb = new StringBuilder();
            sb.append("CSIM_SPN=");
            String str = "***";
            sb.append(Log.HWINFO ? IccUtils.bytesToHexString(data) : str);
            ruimRecords.log(sb.toString());
            RuimRecords.this.mCsimSpnDisplayCondition = (data[0] & 1) != 0;
            byte b = data[1];
            byte b2 = data[2];
            int len = 32;
            byte[] spnData = new byte[32];
            if (data.length - 3 < 32) {
                len = data.length - 3;
            }
            System.arraycopy(data, 3, spnData, 0, len);
            int numBytes = 0;
            while (numBytes < spnData.length && (spnData[numBytes] & 255) != 255) {
                numBytes++;
            }
            if (numBytes == 0) {
                RuimRecords.this.setServiceProviderName(PhoneConfigurationManager.SSSS);
                return;
            }
            if (b != 0) {
                if (b != 2) {
                    if (b != 3) {
                        if (b == 4) {
                            RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, CharacterSets.MIMENAME_UTF_16));
                        } else if (b != 8) {
                            if (b != 9) {
                                try {
                                    RuimRecords.this.log("SPN encoding not supported");
                                } catch (Exception e) {
                                    RuimRecords.this.log("spn decode error");
                                }
                            }
                        }
                    }
                    RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                } else {
                    String spn = new String(spnData, 0, numBytes, "US-ASCII");
                    if (TextUtils.isPrintableAsciiOnly(spn)) {
                        RuimRecords.this.setServiceProviderName(spn);
                    } else {
                        RuimRecords ruimRecords2 = RuimRecords.this;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Some corruption in SPN decoding = ");
                        sb2.append(Log.HWINFO ? spn : str);
                        ruimRecords2.log(sb2.toString());
                        RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                        RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                    }
                }
                RuimRecords ruimRecords3 = RuimRecords.this;
                StringBuilder sb3 = new StringBuilder();
                sb3.append("spn=");
                if (Log.HWINFO) {
                    str = RuimRecords.this.getServiceProviderName();
                }
                sb3.append(str);
                ruimRecords3.log(sb3.toString());
                RuimRecords ruimRecords4 = RuimRecords.this;
                ruimRecords4.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
                RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
            }
            RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
            RuimRecords ruimRecords32 = RuimRecords.this;
            StringBuilder sb32 = new StringBuilder();
            sb32.append("spn=");
            if (Log.HWINFO) {
            }
            sb32.append(str);
            ruimRecords32.log(sb32.toString());
            RuimRecords ruimRecords42 = RuimRecords.this;
            ruimRecords42.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
            RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
        }
    }

    /* access modifiers changed from: private */
    public class EfCsimMdnLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimMdnLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_MDN";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            RuimRecords.this.mMdn = HwTelephonyFactory.getHwUiccManager().cdmaBcdToStringHw(data, 1, data[0] & 15);
            if (TextUtils.isEmpty(RuimRecords.this.mMdn)) {
                RuimRecords ruimRecords = RuimRecords.this;
                ruimRecords.loge("slot[" + RuimRecords.this.getSlotId() + "] CSIM MDN = null");
            }
        }
    }

    /* access modifiers changed from: private */
    public class EfCsimImsimLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimImsimLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            boolean provisioned = (data[7] & 128) == 128;
            RuimRecords.this.mHwIccRecordsEx.updateCsimImsi(data);
            if (provisioned) {
                int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
                int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
                int digit7 = (data[4] >> 2) & 15;
                if (digit7 > 9) {
                    digit7 = 0;
                }
                int last3digits = (data[3] & 255) | ((data[4] & 3) << 8);
                int first3digits2 = RuimRecords.this.adjstMinDigits(first3digits);
                int second3digits2 = RuimRecords.this.adjstMinDigits(second3digits);
                int last3digits2 = RuimRecords.this.adjstMinDigits(last3digits);
                RuimRecords.this.mMin = String.format(Locale.US, "%03d", Integer.valueOf(first3digits2)) + String.format(Locale.US, "%03d", Integer.valueOf(second3digits2)) + String.format(Locale.US, "%d", Integer.valueOf(digit7)) + String.format(Locale.US, "%03d", Integer.valueOf(last3digits2));
                RuimRecords ruimRecords = RuimRecords.this;
                StringBuilder sb = new StringBuilder();
                sb.append("min present=");
                sb.append(Rlog.pii(RuimRecords.LOG_TAG, RuimRecords.this.mMin));
                ruimRecords.log(sb.toString());
                return;
            }
            RuimRecords.this.log("min not present");
        }
    }

    /* access modifiers changed from: private */
    public class EfCsimCdmaHomeLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimCdmaHomeLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_CDMAHOME";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            ArrayList<byte[]> dataList = (ArrayList) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("CSIM_CDMAHOME data size=" + dataList.size());
            if (!dataList.isEmpty()) {
                StringBuilder sidBuf = new StringBuilder();
                StringBuilder nidBuf = new StringBuilder();
                Iterator<byte[]> it = dataList.iterator();
                while (it.hasNext()) {
                    byte[] data = it.next();
                    if (data.length == 5) {
                        sidBuf.append(((data[1] & 255) << 8) | (data[0] & 255));
                        sidBuf.append(',');
                        nidBuf.append(((data[3] & 255) << 8) | (data[2] & 255));
                        nidBuf.append(',');
                    }
                }
                sidBuf.setLength(sidBuf.length() - 1);
                nidBuf.setLength(nidBuf.length() - 1);
                RuimRecords.this.mHomeSystemId = sidBuf.toString();
                RuimRecords.this.mHomeNetworkId = nidBuf.toString();
            }
        }
    }

    /* access modifiers changed from: private */
    public class EfCsimEprlLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimEprlLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.onGetCSimEprlDone(ar);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = (byte[]) ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    /* access modifiers changed from: private */
    public class EfCsimMipUppLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimMipUppLoaded() {
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public String getEfName() {
            return "EF_CSIM_MIPUPP";
        }

        /* access modifiers changed from: package-private */
        public boolean checkLengthLegal(int length, int expectLength) {
            if (length >= expectLength) {
                return true;
            }
            Log.e(RuimRecords.LOG_TAG, "CSIM MIPUPP format error, length = " + length + "expected length at least =" + expectLength);
            return false;
        }

        @Override // com.android.internal.telephony.uicc.IccRecords.IccRecordLoaded
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            if (data.length < 1) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read error");
                return;
            }
            BitwiseInputStream bitStream = new BitwiseInputStream(data);
            int i = 8;
            try {
                int mipUppLength = bitStream.read(8) << 3;
                if (checkLengthLegal(mipUppLength, 1)) {
                    int mipUppLength2 = mipUppLength - 1;
                    if (bitStream.read(1) == 1) {
                        if (checkLengthLegal(mipUppLength2, 11)) {
                            bitStream.skip(11);
                            mipUppLength2 -= 11;
                        } else {
                            return;
                        }
                    }
                    if (checkLengthLegal(mipUppLength2, 4)) {
                        int numNai = bitStream.read(4);
                        int mipUppLength3 = mipUppLength2 - 4;
                        int index = 0;
                        while (index < numNai && checkLengthLegal(mipUppLength3, 4)) {
                            int naiEntryIndex = bitStream.read(4);
                            int mipUppLength4 = mipUppLength3 - 4;
                            if (checkLengthLegal(mipUppLength4, i)) {
                                int naiLength = bitStream.read(i);
                                int mipUppLength5 = mipUppLength4 - 8;
                                if (naiEntryIndex == 0) {
                                    if (checkLengthLegal(mipUppLength5, naiLength << 3)) {
                                        char[] naiCharArray = new char[naiLength];
                                        for (int index1 = 0; index1 < naiLength; index1++) {
                                            naiCharArray[index1] = (char) (bitStream.read(i) & 255);
                                        }
                                        RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                            return;
                                        }
                                        return;
                                    }
                                    return;
                                } else if (checkLengthLegal(mipUppLength5, (naiLength << 3) + CallFailCause.RECOVERY_ON_TIMER_EXPIRY)) {
                                    bitStream.skip((naiLength << 3) + 101);
                                    int mnAaaSpiIndicator = bitStream.read(1);
                                    int mipUppLength6 = mipUppLength5 - ((naiLength << 3) + CallFailCause.RECOVERY_ON_TIMER_EXPIRY);
                                    if (mnAaaSpiIndicator == 1) {
                                        if (checkLengthLegal(mipUppLength6, 32)) {
                                            bitStream.skip(32);
                                            mipUppLength6 -= 32;
                                        } else {
                                            return;
                                        }
                                    }
                                    if (checkLengthLegal(mipUppLength6, 5)) {
                                        bitStream.skip(4);
                                        mipUppLength3 = (mipUppLength6 - 4) - 1;
                                        if (bitStream.read(1) == 1) {
                                            if (checkLengthLegal(mipUppLength3, 32)) {
                                                bitStream.skip(32);
                                                mipUppLength3 -= 32;
                                            } else {
                                                return;
                                            }
                                        }
                                        index++;
                                        i = 8;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(RuimRecords.LOG_TAG, "MIPUPP read Exception error!");
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, android.os.Handler
    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            int i = msg.what;
            if (i == 1) {
                onReady();
                this.mHwIccRecordsEx.onReady();
            } else if (i == 10) {
                AsyncResult ar = (AsyncResult) msg.obj;
                String[] localTemp = (String[]) ar.result;
                if (ar.exception == null) {
                    this.mMyMobileNumber = localTemp[0];
                    this.mMin2Min1 = localTemp[3];
                    this.mPrlVersion = localTemp[4];
                    log("EVENT_GET_CDMA_SUBSCRIPTION_DONE");
                }
            } else if (i == 14) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    Rlog.i(LOG_TAG, "RuimRecords update failed", ar2.exception);
                }
            } else if (i == 3) {
                isRecordLoadResponse = true;
                onGetImsiDone(msg);
            } else if (i == 4) {
                log("Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
            } else if (i != 5) {
                if (!(i == 21 || i == 22)) {
                    if (i != 32 && i != 33) {
                        switch (i) {
                            case 17:
                                log("Event EVENT_GET_SST_DONE Received");
                                break;
                            case 18:
                            case 19:
                                break;
                            default:
                                super.handleMessage(msg);
                                break;
                        }
                    } else {
                        onLocked(msg.what);
                    }
                }
                Rlog.w(LOG_TAG, "Event not supported: " + msg.what);
            } else {
                isRecordLoadResponse = true;
                AsyncResult ar3 = (AsyncResult) msg.obj;
                byte[] data = (byte[]) ar3.result;
                if (ar3.exception != null) {
                    this.mHwIccRecordsEx.notifyRegisterLoadIccID(ar3.userObj, ar3.result, ar3.exception);
                } else {
                    this.mIccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                    log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mIccId));
                    this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                    log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                    this.mHwIccRecordsEx.onIccIdLoadedHw();
                    this.mHwIccRecordsEx.notifyRegisterLoadIccID(ar3.userObj, ar3.result, ar3.exception);
                }
            }
            if (!isRecordLoadResponse) {
                return;
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                onRecordLoaded();
            }
            throw th;
        }
        onRecordLoaded();
    }

    @UnsupportedAppUsage
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

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onRecordLoaded() {
        this.mRecordsToLoad--;
        log("onRecordLoaded " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested + ", " + getSlotId());
        if (getRecordsLoaded()) {
            onAllRecordsLoaded();
        } else if (getLockedRecordsLoaded() || getNetworkLockedRecordsLoaded()) {
            onLockedAllRecordsLoaded();
        } else if (this.mRecordsToLoad < 0) {
            loge("recordsToLoad <0, programmer error suspected");
            this.mRecordsToLoad = 0;
        }
    }

    private void onLockedAllRecordsLoaded() {
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
        if (PLUS_TRANFER_IN_AP && this.mImsi != null) {
            SystemProperties.set("ril.radio.cdma.icc_mcc", this.mImsi.substring(0, 3));
        }
        if (Resources.getSystem().getBoolean(17891567)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult((Object) null, (Object) null, (Throwable) null));
        if (!TextUtils.isEmpty(this.mMdn)) {
            int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getUiccProfile().getPhoneId());
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                SubscriptionManager.from(this.mContext).setDisplayNumber(this.mMdn, subId);
            } else {
                log("Cannot call setDisplayNumber: invalid subId");
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void onReady() {
        fetchRuimRecords();
        this.mCi.getCDMASubscription(obtainMessage(10));
    }

    private void onLocked(int msg) {
        int i;
        log("only fetch EF_ICCID in locked state");
        if (msg == 32) {
            i = 1;
        } else {
            i = 2;
        }
        this.mLockedRecordsReqReason = i;
        if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
            this.mCi.getICCID(obtainMessage(5));
        } else {
            this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
        }
        this.mRecordsToLoad++;
    }

    @UnsupportedAppUsage
    private void fetchRuimRecords() {
        if (this.mParentApp != null) {
            if (this.mRecordsRequested || !this.mRecordsRequired || IccCardApplicationStatus.AppState.APPSTATE_READY != this.mParentApp.getState()) {
                log("fetchRuimRecords: Abort fetching records mRecordsRequested = " + this.mRecordsRequested + " state = " + this.mParentApp.getState() + " required = " + this.mRecordsRequired);
                return;
            }
            this.mRecordsRequested = true;
            log("fetchRuimRecords " + this.mRecordsToLoad);
            if (!HwModemCapability.isCapabilitySupport(18)) {
                this.mHwIccRecordsEx.getCdmaGsmImsiFromHwRil();
                this.mCi.getIMSIForApp(this.mParentApp.getAid(), obtainMessage(3));
                this.mRecordsToLoad++;
            }
            if (!this.mHwIccRecordsEx.getIccidSwitch()) {
                if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                    this.mCi.getICCID(obtainMessage(5));
                } else {
                    this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
                }
                this.mRecordsToLoad++;
            }
            this.mHwIccRecordsEx.getPbrRecordSize();
            this.mFh.loadEFTransparent(IccConstants.EF_PL, obtainMessage(100, new EfPlLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(28474, obtainMessage(100, new EfCsimLiLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(28481, obtainMessage(100, new EfCsimSpnLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFLinearFixed(IccConstants.EF_CSIM_MDN, 1, obtainMessage(100, new EfCsimMdnLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_CSIM_IMSIM, obtainMessage(100, new EfCsimImsimLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFLinearFixedAll(IccConstants.EF_CSIM_CDMAHOME, obtainMessage(100, new EfCsimCdmaHomeLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_CSIM_EPRL, 4, obtainMessage(100, new EfCsimEprlLoaded()));
            this.mRecordsToLoad++;
            this.mFh.loadEFTransparent(IccConstants.EF_CSIM_MIPUPP, obtainMessage(100, new EfCsimMipUppLoaded()));
            this.mRecordsToLoad++;
            log("fetchRuimRecords " + this.mRecordsToLoad + " requested: " + this.mRecordsRequested);
        }
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public boolean isProvisioned() {
        if (SystemProperties.getBoolean("persist.radio.test-csim", false)) {
            return true;
        }
        if (this.mParentApp == null) {
            return false;
        }
        return (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? false : true;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        log("RuimRecords:setVoiceMessageWaiting - NOP for CDMA");
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public int getVoiceMessageCount() {
        log("RuimRecords:getVoiceMessageCount - NOP for CDMA");
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    public void handleFileUpdate(int efid) {
        this.mAdnCache.reset();
        fetchRuimRecords();
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public boolean getCsimSpnDisplayCondition() {
        return this.mCsimSpnDisplayCondition;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.i(LOG_TAG, "[RuimRecords] " + s);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.uicc.IccRecords
    @UnsupportedAppUsage
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[RuimRecords] " + s);
    }

    @Override // com.android.internal.telephony.uicc.IccRecords
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("RuimRecords: " + this);
        pw.println(" extends:");
        super.dump(fd, pw, args);
        pw.println(" mOtaCommited=" + this.mOtaCommited);
        pw.println(" mMyMobileNumber=xxx");
        pw.println(" mMin2Min1=" + this.mMin2Min1);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mEFpl[]=" + Arrays.toString(this.mEFpl));
        pw.println(" mEFli[]=" + Arrays.toString(this.mEFli));
        pw.println(" mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition);
        pw.println(" mMdn=xxxx");
        pw.println(" mMin=xxxx");
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.flush();
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.IIccRecordsInner
    public void recordsRequired() {
        log("recordsRequired");
        this.mRecordsRequired = true;
        fetchRuimRecords();
    }

    public void onGetImsiDone(Message msg) {
        if (msg != null) {
            AsyncResult ar = (AsyncResult) msg.obj;
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
            StringBuilder sb = new StringBuilder();
            sb.append("NO update mccmnc=");
            sb.append(Log.HWINFO ? operatorNumeric : "***");
            log(sb.toString());
            this.mHwIccRecordsEx.updateMccMncConfigWithCplmn(operatorNumeric);
            this.mHwIccRecordsEx.setImsiReady(true);
            this.mImsiReadyRegistrants.notifyRegistrants();
        }
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setMdn(String mdn) {
        this.mMdn = mdn;
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void handleMessageEx(Message msg) {
        handleMessage(msg);
    }

    @Override // com.android.internal.telephony.uicc.IIccRecordsInner
    public void setCdmaGsmImsi(String imsiCdma) {
        this.mCdmaGsmImsi = imsiCdma;
    }

    @Override // com.android.internal.telephony.uicc.IccRecords, com.android.internal.telephony.uicc.IIccRecordsInner
    public String getOperatorNumeric() {
        return this.mHwIccRecordsEx.getOperatorNumericHw();
    }
}
