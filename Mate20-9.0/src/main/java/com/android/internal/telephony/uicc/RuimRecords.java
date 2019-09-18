package com.android.internal.telephony.uicc;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.util.BitwiseInputStream;
import com.google.android.mms.pdu.CharacterSets;
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
    protected static final int EVENT_GET_CDMA_GSM_IMSI_DONE = 37;
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
    /* access modifiers changed from: private */
    public byte[] mEFli = null;
    /* access modifiers changed from: private */
    public byte[] mEFpl = null;
    /* access modifiers changed from: private */
    public String mHomeNetworkId;
    /* access modifiers changed from: private */
    public String mHomeSystemId;
    /* access modifiers changed from: private */
    public String mMin;
    private String mMin2Min1;
    private String mMyMobileNumber;
    /* access modifiers changed from: private */
    public String mNai;
    private boolean mOtaCommited = false;
    private String mPrlVersion;
    private boolean mRecordsRequired = true;

    private class EfCsimCdmaHomeLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimCdmaHomeLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_CDMAHOME";
        }

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
                String unused = RuimRecords.this.mHomeSystemId = sidBuf.toString();
                String unused2 = RuimRecords.this.mHomeNetworkId = nidBuf.toString();
            }
        }
    }

    private class EfCsimEprlLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimEprlLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_EPRL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            RuimRecords.this.onGetCSimEprlDone(ar);
        }
    }

    private class EfCsimImsimLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimImsimLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_IMSIM";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            boolean provisioned = (data[7] & 128) == 128;
            RuimRecords.this.updateCsimImsi(data);
            if (provisioned) {
                int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
                int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
                int digit7 = (data[4] >> 2) & 15;
                if (digit7 > 9) {
                    digit7 = 0;
                }
                int i = data[3] & 255;
                int first3digits2 = RuimRecords.this.adjstMinDigits(first3digits);
                int second3digits2 = RuimRecords.this.adjstMinDigits(second3digits);
                int last3digits = RuimRecords.this.adjstMinDigits(i | ((data[4] & 3) << 8));
                String unused = RuimRecords.this.mMin = String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(first3digits2)}) + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(second3digits2)}) + String.format(Locale.US, "%d", new Object[]{Integer.valueOf(digit7)}) + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(last3digits)});
                RuimRecords ruimRecords = RuimRecords.this;
                StringBuilder sb = new StringBuilder();
                sb.append("min present=");
                sb.append(RuimRecords.this.mMin);
                ruimRecords.log(sb.toString());
                return;
            }
            RuimRecords.this.log("min not present");
        }
    }

    private class EfCsimLiLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimLiLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_LI";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] unused = RuimRecords.this.mEFli = (byte[]) ar.result;
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

    private class EfCsimMdnLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimMdnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_MDN";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            RuimRecords.this.mMdn = HwTelephonyFactory.getHwUiccManager().cdmaBcdToStringHw(data, 1, data[0] & 15);
            if (TextUtils.isEmpty(RuimRecords.this.mMdn)) {
                RuimRecords.this.loge("CSIM MDN = null");
            }
        }
    }

    private class EfCsimMipUppLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimMipUppLoaded() {
        }

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
                        int index1 = 0;
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
                                        while (true) {
                                            int index12 = index1;
                                            if (index12 >= naiLength) {
                                                break;
                                            }
                                            naiCharArray[index12] = (char) (bitStream.read(i) & 255);
                                            index1 = index12 + 1;
                                        }
                                        String unused = RuimRecords.this.mNai = new String(naiCharArray);
                                        if (Log.isLoggable(RuimRecords.LOG_TAG, 2)) {
                                            Log.v(RuimRecords.LOG_TAG, "MIPUPP Nai = " + RuimRecords.this.mNai);
                                        }
                                        return;
                                    }
                                    return;
                                } else if (checkLengthLegal(mipUppLength5, (naiLength << 3) + 102)) {
                                    bitStream.skip((naiLength << 3) + 101);
                                    int mipUppLength6 = mipUppLength5 - ((naiLength << 3) + 102);
                                    if (bitStream.read(1) == 1) {
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

    private class EfCsimSpnLoaded implements IccRecords.IccRecordLoaded {
        private EfCsimSpnLoaded() {
        }

        public String getEfName() {
            return "EF_CSIM_SPN";
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0078, code lost:
            r11.this$0.setServiceProviderName(com.android.internal.telephony.GsmAlphabet.gsm7BitPackedToString(r5, 0, (r6 * 8) / 7));
         */
        public void onRecordLoaded(AsyncResult ar) {
            byte[] data = (byte[]) ar.result;
            RuimRecords.this.log("CSIM_SPN=" + IccUtils.bytesToHexString(data));
            RuimRecords.this.mCsimSpnDisplayCondition = (data[0] & 1) != 0;
            byte encoding = data[1];
            byte b = data[2];
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
                RuimRecords.this.setServiceProviderName("");
                return;
            }
            if (encoding != 0) {
                switch (encoding) {
                    case 2:
                        String spn = new String(spnData, 0, numBytes, "US-ASCII");
                        if (!TextUtils.isPrintableAsciiOnly(spn)) {
                            RuimRecords.this.log("Some corruption in SPN decoding = " + spn);
                            RuimRecords.this.log("Using ENCODING_GSM_7BIT_ALPHABET scheme...");
                            RuimRecords.this.setServiceProviderName(GsmAlphabet.gsm7BitPackedToString(spnData, 0, (numBytes * 8) / 7));
                            break;
                        } else {
                            RuimRecords.this.setServiceProviderName(spn);
                            break;
                        }
                    case 3:
                        break;
                    case 4:
                        RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, CharacterSets.MIMENAME_UTF_16));
                        break;
                    default:
                        switch (encoding) {
                            case 8:
                                break;
                            case 9:
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
                }
            }
            RuimRecords.this.setServiceProviderName(new String(spnData, 0, numBytes, "ISO-8859-1"));
            RuimRecords.this.log("spn=" + RuimRecords.this.getServiceProviderName());
            RuimRecords.this.log("spnCondition=" + RuimRecords.this.mCsimSpnDisplayCondition);
            RuimRecords.this.mTelephonyManager.setSimOperatorNameForPhone(RuimRecords.this.mParentApp.getPhoneId(), RuimRecords.this.getServiceProviderName());
        }
    }

    private class EfPlLoaded implements IccRecords.IccRecordLoaded {
        private EfPlLoaded() {
        }

        public String getEfName() {
            return "EF_PL";
        }

        public void onRecordLoaded(AsyncResult ar) {
            byte[] unused = RuimRecords.this.mEFpl = (byte[]) ar.result;
            RuimRecords ruimRecords = RuimRecords.this;
            ruimRecords.log("EF_PL=" + IccUtils.bytesToHexString(RuimRecords.this.mEFpl));
        }
    }

    public String toString() {
        return "RuimRecords: " + super.toString() + " m_ota_commited" + this.mOtaCommited + " mMyMobileNumber=xxxx mMin2Min1=" + this.mMin2Min1 + " mPrlVersion=" + this.mPrlVersion + " mEFpl=" + this.mEFpl + " mEFli=" + this.mEFli + " mCsimSpnDisplayCondition=" + this.mCsimSpnDisplayCondition + " mMdn=xxxx mMin=xxxx mHomeSystemId=" + this.mHomeSystemId + " mHomeNetworkId=" + this.mHomeNetworkId;
    }

    public String getCdmaGsmImsi() {
        return this.mCdmaGsmImsi;
    }

    public RuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mAdnCache = HwTelephonyFactory.getHwUiccManager().createHwAdnRecordCache(this.mFh);
        this.mRecordsRequested = false;
        this.mLockedRecordsReqReason = 0;
        this.mRecordsToLoad = 0;
        resetRecords();
        this.mParentApp.registerForReady(this, 1, null);
        this.mParentApp.registerForLocked(this, 32, null);
        this.mParentApp.registerForNetworkLocked(this, 33, null);
        log("RuimRecords X ctor this=" + this);
    }

    public void dispose() {
        log("Disposing RuimRecords " + this);
        this.mParentApp.unregisterForReady(this);
        this.mParentApp.unregisterForLocked(this);
        this.mParentApp.unregisterForNetworkLocked(this);
        resetRecords();
        super.dispose();
    }

    /* access modifiers changed from: protected */
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
        this.mImsiLoad = false;
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

    /* access modifiers changed from: private */
    public int adjstMinDigits(int digits) {
        int digits2 = digits + 111;
        int digits3 = digits2 % 10 == 0 ? digits2 - 10 : digits2;
        int digits4 = (digits3 / 10) % 10 == 0 ? digits3 - 100 : digits3;
        return (digits4 / 100) % 10 == 0 ? digits4 - 1000 : digits4;
    }

    public String getRUIMOperatorNumeric() {
        String imsi = getIMSI();
        if (imsi == null) {
            return null;
        }
        if (this.mMncLength != -1 && this.mMncLength != 0) {
            return imsi.substring(0, 3 + this.mMncLength);
        }
        try {
            return this.mImsi.substring(0, 3 + MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3))));
        } catch (RuntimeException e) {
            log("mImsi is not avalible,parseInt error," + e.getMessage() + ",so return null !");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void onGetCSimEprlDone(AsyncResult ar) {
        byte[] data = (byte[]) ar.result;
        log("CSIM_EPRL=" + IccUtils.bytesToHexString(data));
        if (data.length > 3) {
            this.mPrlVersion = Integer.toString(((data[2] & 255) << 8) | (data[3] & 255));
        }
        log("CSIM PRL version=" + this.mPrlVersion);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0106, code lost:
        if (r0 != false) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0108, code lost:
        onRecordLoaded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0116, code lost:
        if (0 == 0) goto L_0x0119;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0119, code lost:
        return;
     */
    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
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
                    AsyncResult ar = (AsyncResult) msg.obj;
                    byte[] data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        this.mIccId = HwTelephonyFactory.getHwUiccManager().bcdIccidToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mIccId));
                        this.mFullIccId = IccUtils.bchToString(data, 0, data.length);
                        log("iccid: " + SubscriptionInfo.givePrintableIccid(this.mFullIccId));
                        onIccIdLoadedHw();
                        this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                        break;
                    } else {
                        this.mIccIDLoadRegistrants.notifyRegistrants(ar);
                        break;
                    }
                case 10:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    String[] localTemp = (String[]) ar2.result;
                    if (ar2.exception == null) {
                        this.mMyMobileNumber = localTemp[0];
                        this.mMin2Min1 = localTemp[3];
                        this.mPrlVersion = localTemp[4];
                        log("EVENT_GET_CDMA_SUBSCRIPTION_DONE");
                        break;
                    } else {
                        break;
                    }
                case 14:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3.exception != null) {
                        Rlog.i(LOG_TAG, "RuimRecords update failed", ar3.exception);
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
                case 32:
                case 33:
                    onLocked(msg.what);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        } catch (RuntimeException exc) {
            Rlog.w(LOG_TAG, "Exception parsing RUIM record", exc);
        } catch (Throwable th) {
            if (0 != 0) {
                onRecordLoaded();
            }
            throw th;
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

    /* access modifiers changed from: protected */
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
        if (PLUS_TRANFER_IN_AP && this.mImsi != null) {
            SystemProperties.set("ril.radio.cdma.icc_mcc", this.mImsi.substring(0, 3));
        }
        if (Resources.getSystem().getBoolean(17957064)) {
            setSimLanguage(this.mEFli, this.mEFpl);
        }
        this.mLoaded.set(true);
        this.mRecordsLoadedRegistrants.notifyRegistrants(new AsyncResult(null, null, null));
        if (!TextUtils.isEmpty(this.mMdn)) {
            int subId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mParentApp.getUiccProfile().getPhoneId());
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
            log("NO update mccmnc=" + operatorNumeric);
            updateMccMncConfigWithCplmn(operatorNumeric);
            this.mImsiLoad = true;
            this.mImsiReadyRegistrants.notifyRegistrants();
        }
    }

    private void onLocked(int msg) {
        log("only fetch EF_ICCID in locked state");
        this.mLockedRecordsReqReason = msg == 32 ? 1 : 2;
        this.mFh.loadEFTransparent(IccConstants.EF_ICCID, obtainMessage(5));
        this.mRecordsToLoad++;
    }

    private void fetchRuimRecords() {
        if (this.mParentApp != null) {
            if (this.mRecordsRequested || !this.mRecordsRequired || IccCardApplicationStatus.AppState.APPSTATE_READY != this.mParentApp.getState()) {
                log("fetchRuimRecords: Abort fetching records mRecordsRequested = " + this.mRecordsRequested + " state = " + this.mParentApp.getState() + " required = " + this.mRecordsRequired);
                return;
            }
            this.mRecordsRequested = true;
            log("fetchRuimRecords " + this.mRecordsToLoad);
            if (!HwModemCapability.isCapabilitySupport(18)) {
                this.mCi.getCdmaGsmImsi(obtainMessage(37));
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

    public int getDisplayRule(ServiceState serviceState) {
        return 0;
    }

    public boolean isProvisioned() {
        if (SystemProperties.getBoolean("persist.radio.test-csim", false)) {
            return true;
        }
        if (this.mParentApp == null) {
            return false;
        }
        return (this.mParentApp.getType() == IccCardApplicationStatus.AppType.APPTYPE_CSIM && (this.mMdn == null || this.mMin == null)) ? false : true;
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        log("RuimRecords:setVoiceMessageWaiting - NOP for CDMA");
    }

    public int getVoiceMessageCount() {
        log("RuimRecords:getVoiceMessageCount - NOP for CDMA");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void handleFileUpdate(int efid) {
        this.mAdnCache.reset();
        fetchRuimRecords();
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

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, "[RuimRecords] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[RuimRecords] " + s);
    }

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

    /* access modifiers changed from: protected */
    public void updateMccMncConfigWithCplmn(String operatorNumeric) {
        log("updateMccMncConfigWithCplmn: " + operatorNumeric);
        if (operatorNumeric != null && operatorNumeric.length() >= 5) {
            setSystemProperty("gsm.sim.operator.numeric", operatorNumeric);
            MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void recordsRequired() {
        log("recordsRequired");
        this.mRecordsRequired = true;
        fetchRuimRecords();
    }
}
