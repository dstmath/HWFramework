package com.android.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwAllInOneController;
import com.android.internal.telephony.HwHotplugController;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.util.Locale;

public class HwRuimRecords extends RuimRecords {
    private static final int CDMA_GSM_IMSI_ARRAY_LENGTH = 2;
    private static final int DELAY_GET_CDMA_GSM_IMSI_TIME = 2000;
    private static final int EVENT_DELAY_GET_CDMA_GSM_IMSI = 40;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_PBR_DONE = 33;
    private static final int EVENT_SET_MDN_DONE = 39;
    private static final boolean IS_MODEM_CAPABILITY_GET_ICCID_AT = HwModemCapability.isCapabilitySupport(19);
    private static final int MAX_CG_IMSI_RETRIES = 10;
    private static final int MAX_MDN_BYTES = 11;
    private static final int MAX_MDN_NUMBERS = 15;
    protected boolean bNeedSendRefreshBC = false;
    private int mCGImsiRetryNum = 0;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(intent.getAction())) {
                    HwRuimRecords.this.log("Receives ACTION_SET_RADIO_CAPABILITY_DONE on slot " + HwRuimRecords.this.getSlotId());
                    boolean bNeedFetchRecords = (HwAllInOneController.IS_FAST_SWITCH_SIMSLOT && HwRuimRecords.this.mIsSimPowerDown && HwRuimRecords.this.mParentApp != null) ? AppState.APPSTATE_READY == HwRuimRecords.this.mParentApp.getState() : false;
                    if (bNeedFetchRecords) {
                        HwRuimRecords.this.log("fetchRuimRecords again.");
                        HwRuimRecords.this.mIsSimPowerDown = false;
                        HwRuimRecords.this.mRecordsRequested = false;
                        HwRuimRecords.this.recordsRequired();
                    }
                }
            }
        }
    };
    private boolean mIsSimPowerDown = false;
    protected String mNewMdnNumber = null;

    public HwRuimRecords(UiccCardApplication app, Context c, CommandsInterface ci) {
        super(app, c, ci);
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
        super.onReady();
        if (this.bNeedSendRefreshBC && HW_SIM_REFRESH) {
            this.bNeedSendRefreshBC = false;
            synchronized (this) {
                this.mIccRefreshRegistrants.notifyRegistrants();
            }
        }
    }

    protected void resetRecords() {
        super.resetRecords();
        this.mIs3Gphonebook = false;
        this.mIsGetPBRDone = false;
        this.mIsSimPowerDown = false;
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
        boolean isRecordLoadResponse = false;
        if (this.mDestroyed.get()) {
            loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            return;
        }
        try {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_GET_PBR_DONE /*33*/:
                    isRecordLoadResponse = true;
                    ar = msg.obj;
                    if (ar.exception == null) {
                        this.mIs3Gphonebook = true;
                    } else if ((ar.exception instanceof CommandException) && Error.SIM_ABSENT == ((CommandException) ar.exception).getCommandError()) {
                        this.mIsSimPowerDown = true;
                        log("Get PBR Done,mIsSimPowerDown: " + this.mIsSimPowerDown);
                    }
                    this.mIsGetPBRDone = true;
                    log("Get PBR Done,mIs3Gphonebook: " + this.mIs3Gphonebook);
                    break;
                case 37:
                    log("get CDMA_GSM_IMSI");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mCdmaGsmImsi = (String) ar.result;
                        if (!isValidCdmaGsmImsi() && this.mCGImsiRetryNum < 10) {
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
                case 40:
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
            constant = (constant * 10) + 1;
        }
        digits += constant;
        int denominator = 1;
        for (i = 0; i < length; i++) {
            if ((digits / denominator) % 10 == 0) {
                digits -= denominator * 10;
            }
            denominator *= 10;
        }
        return digits;
    }

    public String decodeCdmaImsi(byte[] data) {
        int mcc = decodeImsiDigits(((data[9] & 3) << 8) | (data[8] & HwSubscriptionManager.SUB_INIT_STATE), 3);
        int digits_11_12 = decodeImsiDigits(data[6] & 127, 2);
        int first3digits = ((data[2] & 3) << 8) + (data[1] & HwSubscriptionManager.SUB_INIT_STATE);
        int second3digits = (((data[5] & HwSubscriptionManager.SUB_INIT_STATE) << 8) | (data[4] & HwSubscriptionManager.SUB_INIT_STATE)) >> 6;
        int digit7 = (data[4] >> 2) & 15;
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
        if (length > 15) {
            length = 15;
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
        if (convertByte.length == 0 || convertByte.length > 11) {
            log("setMdnNumber, invalide convertByte");
            return;
        }
        byte[] mMdn = new byte[11];
        for (i = 0; i < 11; i++) {
            mMdn[i] = (byte) -1;
        }
        mMdn[0] = (byte) validNumber.length();
        System.arraycopy(convertByte, 0, mMdn, 1, convertByte.length < 11 ? convertByte.length : 10);
        if ("+".equals(number.substring(0, 1))) {
            mMdn[9] = (byte) 9;
            this.mNewMdnNumber = "+" + validNumber.toString();
        } else {
            mMdn[9] = (byte) 10;
            this.mNewMdnNumber = validNumber.toString();
        }
        mMdn[10] = (byte) 0;
        this.mFh.updateEFLinearFixed(28484, 1, mMdn, null, obtainMessage(EVENT_SET_MDN_DONE, onComplete));
    }

    private void delayGetCdmaGsmImsi() {
        log("delayGetCdmaGsmImsi");
        sendMessageDelayed(obtainMessage(40), HwVSimConstants.GET_MODEM_SUPPORT_VERSION_INTERVAL);
    }

    private boolean isValidCdmaGsmImsi() {
        if (this.mCdmaGsmImsi == null) {
            return false;
        }
        boolean isValid = true;
        String[] imsiArray = this.mCdmaGsmImsi.split(",");
        if (2 == imsiArray.length) {
            for (String trim : imsiArray) {
                if (TextUtils.isEmpty(trim.trim())) {
                    isValid = false;
                    break;
                }
            }
        } else {
            isValid = false;
        }
        return isValid;
    }

    public void dispose() {
        log("Disposing HwRuimRecords " + this);
        removeMessages(40);
        super.dispose();
    }
}
