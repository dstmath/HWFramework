package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.Locale;

public class HwRuimRecords extends RuimRecords {
    private static final int CDMA_GSM_IMSI_ARRAY_LENGTH = 2;
    private static final int DELAY_GET_CDMA_GSM_IMSI_TIME = 2000;
    private static final int EVENT_DELAY_GET_CDMA_GSM_IMSI = 40;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_PBR_DONE = 33;
    private static final int EVENT_SET_MDN_DONE = 39;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = false;
    private static final int MAX_CG_IMSI_RETRIES = 10;
    private static final int MAX_MDN_BYTES = 11;
    private static final int MAX_MDN_NUMBERS = 15;
    protected boolean bNeedSendRefreshBC;
    private int mCGImsiRetryNum;
    protected String mNewMdnNumber;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.uicc.HwRuimRecords.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.uicc.HwRuimRecords.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.HwRuimRecords.<clinit>():void");
    }

    public HwRuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.bNeedSendRefreshBC = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        this.mNewMdnNumber = null;
        this.mCGImsiRetryNum = 0;
        if (getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(EVENT_GET_ICCID_DONE));
            } else {
                this.mFh.loadEFTransparent(12258, obtainMessage(EVENT_GET_ICCID_DONE));
            }
            this.mRecordsToLoad++;
        }
    }

    public boolean beforeHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        switch (refreshResponse.refreshResult) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                log("beforeHandleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                this.mRecordsRequested = IS_MODEM_CAPABILITY_GET_ICCID_AT;
                break;
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                log("beforeHandleRuimRefresh with SIM_REFRESH_INIT");
                this.mRecordsRequested = IS_MODEM_CAPABILITY_GET_ICCID_AT;
                break;
            case CDMA_GSM_IMSI_ARRAY_LENGTH /*2*/:
                log("beforeHandleRuimRefresh with SIM_REFRESH_RESET");
                this.mAdnCache.reset();
                break;
            default:
                log("beforeHandleRuimRefresh with unknown operation");
                break;
        }
        return IS_MODEM_CAPABILITY_GET_ICCID_AT;
    }

    public boolean afterHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        switch (refreshResponse.refreshResult) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                log("afterHandleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                synchronized (this) {
                    this.mIccRefreshRegistrants.notifyRegistrants();
                    break;
                }
                break;
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                log("afterHandleRuimRefresh with SIM_REFRESH_INIT");
                if (HW_SIM_REFRESH) {
                    this.bNeedSendRefreshBC = true;
                    break;
                }
                break;
            case CDMA_GSM_IMSI_ARRAY_LENGTH /*2*/:
                log("afterHandleRuimRefresh with SIM_REFRESH_RESET");
                if (HW_SIM_REFRESH) {
                    this.bNeedSendRefreshBC = true;
                    break;
                }
                break;
            default:
                log("afterHandleRuimRefresh with unknown operation");
                break;
        }
        return IS_MODEM_CAPABILITY_GET_ICCID_AT;
    }

    public void onReady() {
        super.onReady();
        if (this.bNeedSendRefreshBC && HW_SIM_REFRESH) {
            this.bNeedSendRefreshBC = IS_MODEM_CAPABILITY_GET_ICCID_AT;
            synchronized (this) {
                this.mIccRefreshRegistrants.notifyRegistrants();
            }
        }
    }

    protected void resetRecords() {
        super.resetRecords();
        this.mIs3Gphonebook = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        this.mIsGetPBRDone = IS_MODEM_CAPABILITY_GET_ICCID_AT;
    }

    protected void onIccIdLoadedHw() {
        if (getIccidSwitch()) {
            sendIccidDoneBroadcast(this.mIccId);
        }
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().onHotplugIccIdChanged(this.mIccId, getSlotId());
        }
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
                case EVENT_GET_PBR_DONE /*33*/:
                    isRecordLoadResponse = true;
                    if (msg.obj.exception == null) {
                        this.mIs3Gphonebook = true;
                    }
                    this.mIsGetPBRDone = true;
                    log("Get PBR Done,mIs3Gphonebook: " + this.mIs3Gphonebook);
                    break;
                case 37:
                    log("get CDMA_GSM_IMSI");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mCdmaGsmImsi = (String) ar.result;
                        if (!isValidCdmaGsmImsi() && this.mCGImsiRetryNum < MAX_CG_IMSI_RETRIES) {
                            log("CDMA_GSM_IMSI not get, retry");
                            this.mCGImsiRetryNum++;
                            delayGetCdmaGsmImsi();
                            break;
                        }
                        this.mCGImsiRetryNum = 0;
                        break;
                    }
                    Rlog.e("RuimRecords", "Exception querying CDMAGSM IMSI, Exception:" + ar.exception);
                    break;
                    break;
                case EVENT_SET_MDN_DONE /*39*/:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mMdn = this.mNewMdnNumber;
                    }
                    if (ar.userObj != null) {
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                    }
                    log("Success to update EF_MDN");
                    break;
                case EVENT_DELAY_GET_CDMA_GSM_IMSI /*40*/:
                    log("EVENT_DELAY_GET_CDMA_GSM_IMSI");
                    this.mCi.getCdmaGsmImsi(obtainMessage(37));
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (RuntimeException exc) {
            Rlog.w("RuimRecords", "Exception parsing RUIM record", exc);
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        } catch (Throwable th) {
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        }
    }

    public int getSlotId() {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null) {
            return this.mParentApp.getUiccCard().getPhoneId();
        }
        log("error , mParentApp.getUiccCard  is null");
        return 0;
    }

    protected void getPbrRecordSize() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(EVENT_GET_PBR_DONE));
        this.mRecordsToLoad++;
    }

    private int decodeImsiDigits(int digits, int length) {
        int i;
        int constant = 0;
        for (i = 0; i < length; i++) {
            constant = (constant * MAX_CG_IMSI_RETRIES) + 1;
        }
        digits += constant;
        int denominator = 1;
        for (i = 0; i < length; i++) {
            if ((digits / denominator) % MAX_CG_IMSI_RETRIES == 0) {
                digits -= denominator * MAX_CG_IMSI_RETRIES;
            }
            denominator *= MAX_CG_IMSI_RETRIES;
        }
        return digits;
    }

    public String decodeCdmaImsi(byte[] data) {
        int mcc = decodeImsiDigits(((data[9] & 3) << 8) | (data[8] & HwSubscriptionManager.SUB_INIT_STATE), 3);
        int digits_11_12 = decodeImsiDigits(data[6] & 127, CDMA_GSM_IMSI_ARRAY_LENGTH);
        int first3digits = ((data[CDMA_GSM_IMSI_ARRAY_LENGTH] & 3) << 8) + (data[1] & HwSubscriptionManager.SUB_INIT_STATE);
        int second3digits = (((data[EVENT_GET_ICCID_DONE] & HwSubscriptionManager.SUB_INIT_STATE) << 8) | (data[4] & HwSubscriptionManager.SUB_INIT_STATE)) >> 6;
        int digit7 = (data[4] >> CDMA_GSM_IMSI_ARRAY_LENGTH) & MAX_MDN_NUMBERS;
        if (digit7 > 9) {
            digit7 = 0;
        }
        int last3digits = ((data[4] & 3) << 8) | (data[3] & HwSubscriptionManager.SUB_INIT_STATE);
        first3digits = decodeImsiDigits(first3digits, 3);
        second3digits = decodeImsiDigits(second3digits, 3);
        last3digits = decodeImsiDigits(last3digits, 3);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(mcc)}));
        builder.append(String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(digits_11_12)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(first3digits)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(second3digits)}));
        builder.append(String.format(Locale.US, "%d", new Object[]{Integer.valueOf(digit7)}));
        builder.append(String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(last3digits)}));
        return builder.toString();
    }

    public void setMdnNumber(String alphaTag, String number, Message onComplete) {
        if (number == null || number.length() == 0) {
            log("setMdnNumber, invalid number");
            return;
        }
        int i;
        int length = number.length();
        if (length > MAX_MDN_NUMBERS) {
            length = MAX_MDN_NUMBERS;
        }
        log("setMdnNumber, validNumber input ");
        StringBuilder validNumber = new StringBuilder();
        for (i = 0; i < length; i++) {
            char c = number.charAt(i);
            if ((c >= '0' && c <= '9') || c == '*' || c == '#') {
                validNumber.append(c);
            } else {
                log("setMdnNumber, invalide char " + c + ", at index " + i);
            }
        }
        byte[] convertByte = HwIccUtils.stringToCdmaDTMF(validNumber.toString());
        if (convertByte.length == 0 || convertByte.length > MAX_MDN_BYTES) {
            log("setMdnNumber, invalide convertByte");
            return;
        }
        byte[] mMdn = new byte[MAX_MDN_BYTES];
        for (i = 0; i < MAX_MDN_BYTES; i++) {
            mMdn[i] = (byte) -1;
        }
        mMdn[0] = (byte) validNumber.length();
        System.arraycopy(convertByte, 0, mMdn, 1, convertByte.length < MAX_MDN_BYTES ? convertByte.length : MAX_CG_IMSI_RETRIES);
        if ("+".equals(number.substring(0, 1))) {
            mMdn[9] = (byte) 9;
            this.mNewMdnNumber = "+" + validNumber.toString();
        } else {
            mMdn[9] = (byte) 10;
            this.mNewMdnNumber = validNumber.toString();
        }
        mMdn[MAX_CG_IMSI_RETRIES] = (byte) 0;
        this.mFh.updateEFLinearFixed(28484, 1, mMdn, null, obtainMessage(EVENT_SET_MDN_DONE, onComplete));
    }

    private void delayGetCdmaGsmImsi() {
        log("delayGetCdmaGsmImsi");
        sendMessageDelayed(obtainMessage(EVENT_DELAY_GET_CDMA_GSM_IMSI), 2000);
    }

    private boolean isValidCdmaGsmImsi() {
        if (this.mCdmaGsmImsi == null) {
            return IS_MODEM_CAPABILITY_GET_ICCID_AT;
        }
        boolean isValid = true;
        String[] imsiArray = this.mCdmaGsmImsi.split(",");
        if (CDMA_GSM_IMSI_ARRAY_LENGTH == imsiArray.length) {
            for (String trim : imsiArray) {
                if (TextUtils.isEmpty(trim.trim())) {
                    isValid = IS_MODEM_CAPABILITY_GET_ICCID_AT;
                    break;
                }
            }
        } else {
            isValid = IS_MODEM_CAPABILITY_GET_ICCID_AT;
        }
        return isValid;
    }

    public void dispose() {
        log("Disposing HwRuimRecords " + this);
        removeMessages(EVENT_DELAY_GET_CDMA_GSM_IMSI);
        super.dispose();
    }
}
