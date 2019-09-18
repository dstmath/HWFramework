package com.android.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwCarrierConfigCardManager;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.util.Locale;

public class HwRuimRecords extends RuimRecords {
    private static final int CDMA_GSM_IMSI_ARRAY_LENGTH = 2;
    private static final int DELAY_GET_CDMA_GSM_IMSI_TIME = 2000;
    private static final int EVENT_DELAY_GET_CDMA_GSM_IMSI = 40;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_PBR_DONE = 501;
    private static final int EVENT_GET_SIM_APP_IMSI_DONE = 38;
    private static final int EVENT_HW_BASE = 500;
    private static final int EVENT_SET_MDN_DONE = 39;
    private static final int GSM_CDMA_IMSI_SPLIT_ARRAY_LEN = 2;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    private static final int MAX_CG_IMSI_RETRIES = 10;
    private static final int MAX_MDN_BYTES = 11;
    private static final int MAX_MDN_NUMBERS = 15;
    protected boolean bNeedSendRefreshBC = false;
    private int mCGImsiRetryNum = 0;
    HwCarrierConfigCardManager mHwCarrierCardManager;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                HwRuimRecords hwRuimRecords = HwRuimRecords.this;
                hwRuimRecords.log("Receives ACTION_SET_RADIO_CAPABILITY_DONE on slot " + HwRuimRecords.this.getSlotId());
                if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT && HwRuimRecords.this.mIsSimPowerDown && HwRuimRecords.this.mParentApp != null && IccCardApplicationStatus.AppState.APPSTATE_READY == HwRuimRecords.this.mParentApp.getState()) {
                    HwRuimRecords.this.log("fetchRuimRecords again.");
                    boolean unused = HwRuimRecords.this.mIsSimPowerDown = false;
                    HwRuimRecords.this.mRecordsRequested = false;
                    HwRuimRecords.this.recordsRequired();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsSimPowerDown = false;
    protected String mNewMdnNumber = null;

    public HwRuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
        this.mHwCarrierCardManager = HwCarrierConfigCardManager.getDefault(c);
        this.mHwCarrierCardManager.reportIccRecordInstance(getSlotId(), this);
        if (getIccidSwitch()) {
            if (IS_MODEM_CAPABILITY_GET_ICCID_AT) {
                this.mCi.getICCID(obtainMessage(5));
            } else {
                this.mFh.loadEFTransparent(12258, obtainMessage(5));
            }
            this.mRecordsToLoad++;
        }
        addIntentFilter(c);
    }

    private void addIntentFilter(Context c) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        c.registerReceiver(this.mIntentReceiver, filter);
    }

    public boolean beforeHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        switch (refreshResponse.refreshResult) {
            case 0:
                log("beforeHandleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                this.mRecordsRequested = false;
                break;
            case 1:
                log("beforeHandleRuimRefresh with SIM_REFRESH_INIT");
                this.mRecordsRequested = false;
                break;
            case 2:
                log("beforeHandleRuimRefresh with SIM_REFRESH_RESET");
                this.mAdnCache.reset();
                break;
            default:
                log("beforeHandleRuimRefresh with unknown operation");
                break;
        }
        return false;
    }

    public boolean afterHandleRuimRefresh(IccRefreshResponse refreshResponse) {
        switch (refreshResponse.refreshResult) {
            case 0:
                log("afterHandleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                synchronized (this) {
                    this.mIccRefreshRegistrants.notifyRegistrants();
                }
                break;
            case 1:
                log("afterHandleRuimRefresh with SIM_REFRESH_INIT");
                if (HW_SIM_REFRESH) {
                    this.bNeedSendRefreshBC = true;
                    break;
                }
                break;
            case 2:
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
        return false;
    }

    public void onReady() {
        HwRuimRecords.super.onReady();
        if (this.bNeedSendRefreshBC && HW_SIM_REFRESH) {
            this.bNeedSendRefreshBC = false;
            synchronized (this) {
                this.mIccRefreshRegistrants.notifyRegistrants();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetRecords() {
        HwRuimRecords.super.resetRecords();
        this.mIs3Gphonebook = false;
        this.mIsGetPBRDone = false;
        this.mIsSimPowerDown = false;
    }

    /* access modifiers changed from: protected */
    public void onIccIdLoadedHw() {
        if (getIccidSwitch()) {
            sendIccidDoneBroadcast(this.mIccId);
        }
        if (HwHotplugController.IS_HOTSWAP_SUPPORT) {
            HwHotplugController.getInstance().onHotplugIccIdChanged(this.mIccId, getSlotId());
        }
        updateCarrierFile(getSlotId(), 1, this.mIccId);
    }

    /* access modifiers changed from: protected */
    public void updateCarrierFile(int slotId, int fileType, String fileValue) {
        this.mHwCarrierCardManager.updateCarrierFile(slotId, fileType, fileValue);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01c0, code lost:
        if (r0 != false) goto L_0x01c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x01c2, code lost:
        onRecordLoaded();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01d0, code lost:
        if (0 == 0) goto L_0x01d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01d3, code lost:
        return;
     */
    public void handleMessage(Message msg) {
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            int i = msg.what;
            if (i != EVENT_GET_PBR_DONE) {
                switch (i) {
                    case 37:
                        log("get CDMA_GSM_IMSI");
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mCdmaGsmImsi = (String) ar.result;
                            if (!isValidCdmaGsmImsi() && this.mCGImsiRetryNum < 10) {
                                log("CDMA_GSM_IMSI not get, retry");
                                this.mCGImsiRetryNum++;
                                delayGetCdmaGsmImsi();
                                break;
                            } else {
                                log("get CDMA_GSM_IMSI mImsiLoad is " + this.mImsiLoad);
                                if (isValidCdmaGsmImsi() || !this.mImsiLoad) {
                                    getCdmaGsmImsiDone(this.mCdmaGsmImsi);
                                } else {
                                    this.mHwCarrierCardManager.setSingleModeCdmaCard(getSlotId(), true);
                                    updateCarrierFile(getSlotId(), 4, this.mImsi);
                                    log("getCImsiFromCdmaGsmImsi done");
                                }
                                this.mCGImsiRetryNum = 0;
                                break;
                            }
                        } else {
                            Rlog.e("RuimRecords", "Exception querying CDMAGSM IMSI, Exception:" + ar.exception);
                            if (this.mCGImsiRetryNum >= 10) {
                                log("get CDMA_GSM_IMSI mImsiLoad is " + this.mImsiLoad);
                                if (!this.mImsiLoad) {
                                    getCdmaGsmImsiDone(null);
                                    break;
                                } else {
                                    this.mHwCarrierCardManager.setSingleModeCdmaCard(getSlotId(), true);
                                    updateCarrierFile(getSlotId(), 4, this.mImsi);
                                    log("getCImsiFromCdmaGsmImsi done");
                                    break;
                                }
                            }
                        }
                        break;
                    case EVENT_GET_SIM_APP_IMSI_DONE /*38*/:
                        log("get SIM_APP_IMSI");
                        if (((AsyncResult) msg.obj).exception == null) {
                            this.mCdmaGsmImsi = this.mImsi + "," + ((String) ar.result);
                            break;
                        } else {
                            Rlog.e("RuimRecords", "Exception querying SIM APP IMSI");
                            break;
                        }
                    case EVENT_SET_MDN_DONE /*39*/:
                        AsyncResult ar2 = (AsyncResult) msg.obj;
                        if (ar2.exception == null) {
                            this.mMdn = this.mNewMdnNumber;
                        }
                        if (ar2.userObj != null) {
                            AsyncResult.forMessage((Message) ar2.userObj).exception = ar2.exception;
                            ((Message) ar2.userObj).sendToTarget();
                        }
                        log("Success to update EF_MDN");
                        break;
                    case 40:
                        log("EVENT_DELAY_GET_CDMA_GSM_IMSI");
                        this.mCi.getCdmaGsmImsi(obtainMessage(37));
                        break;
                    default:
                        HwRuimRecords.super.handleMessage(msg);
                        break;
                }
            } else {
                isRecordLoadResponse = true;
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3.exception == null) {
                    this.mIs3Gphonebook = true;
                } else if ((ar3.exception instanceof CommandException) && CommandException.Error.SIM_ABSENT == ar3.exception.getCommandError()) {
                    this.mIsSimPowerDown = true;
                    log("Get PBR Done,mIsSimPowerDown: " + this.mIsSimPowerDown);
                }
                this.mIsGetPBRDone = true;
                log("Get PBR Done,mIs3Gphonebook: " + this.mIs3Gphonebook);
            }
        } catch (RuntimeException exc) {
            Rlog.w("RuimRecords", "Exception parsing RUIM record", exc);
        } catch (Throwable th) {
            if (0 != 0) {
                onRecordLoaded();
            }
            throw th;
        }
    }

    public int getSlotId() {
        if (this.mParentApp != null && this.mParentApp.getUiccCard() != null) {
            return this.mParentApp.getUiccCard().getPhoneId();
        }
        log("error , mParentApp.getUiccCard  is null");
        return 0;
    }

    /* access modifiers changed from: protected */
    public void getPbrRecordSize() {
        this.mFh.loadEFLinearFixedAll(20272, obtainMessage(EVENT_GET_PBR_DONE));
        this.mRecordsToLoad++;
    }

    private int decodeImsiDigits(int digits, int length) {
        int denominator;
        int constant = 0;
        int i = 0;
        while (true) {
            denominator = 1;
            if (i >= length) {
                break;
            }
            constant = (constant * 10) + 1;
            i++;
        }
        int digits2 = digits + constant;
        for (int i2 = 0; i2 < length; i2++) {
            digits2 = (digits2 / denominator) % 10 == 0 ? digits2 - (10 * denominator) : digits2;
            denominator *= 10;
        }
        return digits2;
    }

    public String decodeCdmaImsi(byte[] data) {
        int mcc = decodeImsiDigits(((data[9] & 3) << 8) | (data[8] & HwSubscriptionManager.SUB_INIT_STATE), 3);
        int digits_11_12 = decodeImsiDigits(data[6] & 127, 2);
        int first3digits = ((data[2] & 3) << 8) + (data[1] & 255);
        int second3digits = (((data[5] & 255) << 8) | (data[4] & 255)) >> 6;
        int digit7 = (data[4] >> 2) & 15;
        if (digit7 > 9) {
            digit7 = 0;
        }
        int last3digits = ((data[4] & 3) << 8) | (data[3] & HwSubscriptionManager.SUB_INIT_STATE);
        int first3digits2 = decodeImsiDigits(first3digits, 3);
        int second3digits2 = decodeImsiDigits(second3digits, 3);
        int last3digits2 = decodeImsiDigits(last3digits, 3);
        return String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(mcc)}) + String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(digits_11_12)}) + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(first3digits2)}) + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(second3digits2)}) + String.format(Locale.US, "%d", new Object[]{Integer.valueOf(digit7)}) + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(last3digits2)});
    }

    public void setMdnNumber(String alphaTag, String number, Message onComplete) {
        byte[] mMdn = new byte[11];
        if (number == null || number.length() == 0) {
            log("setMdnNumber, invalid number");
            this.mNewMdnNumber = null;
            for (int i = 0; i < 11; i++) {
                mMdn[i] = 0;
            }
            this.mFh.updateEFLinearFixed(28484, 1, mMdn, null, obtainMessage(EVENT_SET_MDN_DONE, onComplete));
            return;
        }
        int length = number.length();
        if (length > 15) {
            length = 15;
        }
        int length2 = length;
        log("setMdnNumber, validNumber input ");
        StringBuilder validNumber = new StringBuilder();
        for (int i2 = 0; i2 < length2; i2++) {
            char c = number.charAt(i2);
            if ((c >= '0' && c <= '9') || c == '*' || c == '#') {
                validNumber.append(c);
            } else {
                log("setMdnNumber, invalide char " + c + ", at index " + i2);
            }
        }
        byte[] convertByte = HwIccUtils.stringToCdmaDTMF(validNumber.toString());
        if (convertByte.length == 0 || convertByte.length > 11) {
            log("setMdnNumber, invalide convertByte");
            return;
        }
        for (int i3 = 0; i3 < 11; i3++) {
            mMdn[i3] = -1;
        }
        mMdn[0] = (byte) validNumber.length();
        System.arraycopy(convertByte, 0, mMdn, 1, convertByte.length < 11 ? convertByte.length : 10);
        if ("+".equals(number.substring(0, 1))) {
            mMdn[9] = 9;
            this.mNewMdnNumber = "+" + validNumber.toString();
        } else {
            mMdn[9] = 10;
            this.mNewMdnNumber = validNumber.toString();
        }
        mMdn[10] = 0;
        this.mFh.updateEFLinearFixed(28484, 1, mMdn, null, obtainMessage(EVENT_SET_MDN_DONE, onComplete));
    }

    private void delayGetCdmaGsmImsi() {
        log("delayGetCdmaGsmImsi");
        sendMessageDelayed(obtainMessage(40), HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
    }

    private boolean isValidCdmaGsmImsi() {
        int i = 0;
        if (this.mCdmaGsmImsi == null) {
            return false;
        }
        boolean isValid = true;
        String[] imsiArray = this.mCdmaGsmImsi.split(",");
        if (2 == imsiArray.length) {
            while (true) {
                if (i >= imsiArray.length) {
                    break;
                } else if (TextUtils.isEmpty(imsiArray[i].trim())) {
                    isValid = false;
                    break;
                } else {
                    i++;
                }
            }
        } else {
            isValid = false;
        }
        return isValid;
    }

    private void getCdmaGsmImsiDone(String cdmaGsmImsi) {
        String cdmaImsi = cdmaGsmImsi;
        if (cdmaGsmImsi != null) {
            String[] imsiArray = cdmaGsmImsi.split(",");
            if (imsiArray.length >= 2) {
                cdmaImsi = imsiArray[0];
            }
        }
        updateCarrierFile(getSlotId(), 4, cdmaImsi);
        log("getCImsiFromCdmaGsmImsi done");
    }

    public void dispose() {
        log("Disposing HwRuimRecords " + this);
        removeMessages(40);
        this.mHwCarrierCardManager.destory(getSlotId(), this);
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        HwRuimRecords.super.dispose();
    }

    public String getOperatorNumeric() {
        if (this.mImsi == null) {
            String tempOperatorNumeric = SystemProperties.get("ro.cdma.home.operator.numeric");
            log("imsi is null tempOperatorNumeric = " + tempOperatorNumeric);
            return tempOperatorNumeric;
        } else if (this.mMncLength != -1 && this.mMncLength != 0) {
            return this.mImsi.substring(0, 3 + this.mMncLength);
        } else {
            try {
                return this.mImsi.substring(0, 3 + MccTable.smallestDigitsMccForMnc(Integer.parseInt(this.mImsi.substring(0, 3))));
            } catch (RuntimeException e) {
                String tempOperatorNumeric2 = SystemProperties.get("ro.cdma.home.operator.numeric");
                log("mImsi is not avalible,parseInt error," + e.getMessage() + ",so return tempOperatorNumeric !");
                return tempOperatorNumeric2;
            }
        }
    }

    public void updateCsimImsi(byte[] data) {
        if (true == HwModemCapability.isCapabilitySupport(18)) {
            try {
                this.mImsi = decodeCdmaImsi(data);
                this.mImsiReadyRegistrants.notifyRegistrants();
                log("IMSI: " + this.mImsi.substring(0, 5) + "xxxxxxxxx");
                updateCarrierFile(getSlotId(), 4, this.mImsi);
                updateMccMncConfigWithCplmn(getRUIMOperatorNumeric());
                if (this.mParentApp != null && this.mParentApp.getUiccCard() != null) {
                    UiccCardApplication simApp = this.mParentApp.getUiccCard().getApplication(1);
                    if (simApp != null) {
                        this.mCi.getIMSIForApp(simApp.getAid(), obtainMessage(EVENT_GET_SIM_APP_IMSI_DONE));
                    }
                }
            } catch (RuntimeException e) {
                loge("Illegal IMSI from CSIM_IMSIM=" + IccUtils.bytesToHexString(data));
            }
        }
    }
}
