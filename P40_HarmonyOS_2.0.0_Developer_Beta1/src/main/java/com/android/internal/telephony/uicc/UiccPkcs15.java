package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.UiccCarrierPrivilegeRules;
import com.google.android.mms.pdu.PduHeaders;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class UiccPkcs15 extends Handler {
    private static final String CARRIER_RULE_AID = "FFFFFFFFFFFF";
    private static final boolean DBG = true;
    private static final int EVENT_CLOSE_LOGICAL_CHANNEL_DONE = 7;
    private static final int EVENT_LOAD_ACCF_DONE = 6;
    private static final int EVENT_LOAD_ACMF_DONE = 4;
    private static final int EVENT_LOAD_ACRF_DONE = 5;
    private static final int EVENT_LOAD_DODF_DONE = 3;
    private static final int EVENT_LOAD_ODF_DONE = 2;
    private static final int EVENT_SELECT_PKCS15_DONE = 1;
    private static final String ID_ACRF = "4300";
    private static final String LOG_TAG = "UiccPkcs15";
    private static final String TAG_ASN_OCTET_STRING = "04";
    private static final String TAG_ASN_SEQUENCE = "30";
    private static final String TAG_TARGET_AID = "A0";
    private int mChannelId = -1;
    private FileHandler mFh;
    private Message mLoadedCallback;
    private Pkcs15Selector mPkcs15Selector;
    private List<String> mRules = null;
    private UiccProfile mUiccProfile;

    private class FileHandler extends Handler {
        protected static final int EVENT_READ_BINARY_DONE = 102;
        protected static final int EVENT_SELECT_FILE_DONE = 101;
        private Message mCallback;
        private String mFileId;
        private final String mPkcs15Path;

        public FileHandler(String pkcs15Path) {
            UiccPkcs15.log("Creating FileHandler, pkcs15Path: " + pkcs15Path);
            this.mPkcs15Path = pkcs15Path;
        }

        public boolean loadFile(String fileId, Message callBack) {
            UiccPkcs15.log("loadFile: " + fileId);
            if (fileId == null || callBack == null) {
                return false;
            }
            this.mFileId = fileId;
            this.mCallback = callBack;
            selectFile();
            return true;
        }

        private void selectFile() {
            if (UiccPkcs15.this.mChannelId > 0) {
                UiccPkcs15.this.mUiccProfile.iccTransmitApduLogicalChannel(UiccPkcs15.this.mChannelId, 0, PduHeaders.MM_FLAGS, 0, 4, 2, this.mFileId, obtainMessage(101));
            } else {
                UiccPkcs15.log("EF based");
            }
        }

        private void readBinary() {
            if (UiccPkcs15.this.mChannelId > 0) {
                UiccPkcs15.this.mUiccProfile.iccTransmitApduLogicalChannel(UiccPkcs15.this.mChannelId, 0, 176, 0, 0, 0, PhoneConfigurationManager.SSSS, obtainMessage(102));
            } else {
                UiccPkcs15.log("EF based");
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            UiccPkcs15.log("handleMessage: " + msg.what);
            AsyncResult ar = (AsyncResult) msg.obj;
            IccException iccException = null;
            if (ar.exception != null || ar.result == null) {
                UiccPkcs15.log("Error: " + ar.exception);
                AsyncResult.forMessage(this.mCallback, (Object) null, ar.exception);
                this.mCallback.sendToTarget();
                return;
            }
            int i = msg.what;
            if (i == 101) {
                readBinary();
            } else if (i != 102) {
                UiccPkcs15.log("Unknown event" + msg.what);
            } else {
                IccIoResult response = (IccIoResult) ar.result;
                String result = IccUtils.bytesToHexString(response.payload).toUpperCase(Locale.US);
                UiccPkcs15.log("IccIoResult: " + response + " payload: " + result);
                Message message = this.mCallback;
                if (result == null) {
                    iccException = new IccException("Error: null response for " + this.mFileId);
                }
                AsyncResult.forMessage(message, result, iccException);
                this.mCallback.sendToTarget();
            }
        }
    }

    private class Pkcs15Selector extends Handler {
        private static final int EVENT_OPEN_LOGICAL_CHANNEL_DONE = 201;
        private static final String PKCS15_AID = "A000000063504B43532D3135";
        private Message mCallback;

        public Pkcs15Selector(Message callBack) {
            this.mCallback = callBack;
            UiccPkcs15.this.mUiccProfile.iccOpenLogicalChannel(PKCS15_AID, 4, obtainMessage(EVENT_OPEN_LOGICAL_CHANNEL_DONE));
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            UiccPkcs15.log("handleMessage: " + msg.what);
            if (msg.what != EVENT_OPEN_LOGICAL_CHANNEL_DONE) {
                UiccPkcs15.log("Unknown event" + msg.what);
                return;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null || ar.result == null || !(ar.result instanceof int[])) {
                UiccPkcs15.log("error: " + ar.exception);
                AsyncResult.forMessage(this.mCallback, (Object) null, ar.exception);
            } else {
                UiccPkcs15.this.mChannelId = ((int[]) ar.result)[0];
                UiccPkcs15.log("mChannelId: " + UiccPkcs15.this.mChannelId);
                AsyncResult.forMessage(this.mCallback, (Object) null, (Throwable) null);
            }
            this.mCallback.sendToTarget();
        }
    }

    public UiccPkcs15(UiccProfile uiccProfile, Message loadedCallback) {
        log("Creating UiccPkcs15");
        this.mUiccProfile = uiccProfile;
        this.mLoadedCallback = loadedCallback;
        this.mPkcs15Selector = new Pkcs15Selector(obtainMessage(1));
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        log("handleMessage: " + msg.what);
        AsyncResult ar = (AsyncResult) msg.obj;
        int i = msg.what;
        if (i != 1) {
            if (i != 5) {
                if (i == 6) {
                    if (ar.exception == null && ar.result != null && (ar.result instanceof String)) {
                        parseAccf((String) ar.result);
                    }
                    cleanUp();
                } else if (i != 7) {
                    Rlog.e(LOG_TAG, "Unknown event " + msg.what);
                }
            } else if (ar.exception != null || ar.result == null || !(ar.result instanceof String)) {
                cleanUp();
            } else {
                this.mRules = new ArrayList();
                if (!this.mFh.loadFile(parseAcrf((String) ar.result), obtainMessage(6))) {
                    cleanUp();
                }
            }
        } else if (ar.exception == null) {
            this.mFh = new FileHandler((String) ar.result);
            if (!this.mFh.loadFile(ID_ACRF, obtainMessage(5))) {
                cleanUp();
            }
        } else {
            log("select pkcs15 failed: " + ar.exception);
            Message message = this.mLoadedCallback;
            if (message != null && message.getTarget() != null) {
                this.mLoadedCallback.sendToTarget();
            }
        }
    }

    private void cleanUp() {
        log("cleanUp");
        int i = this.mChannelId;
        if (i >= 0) {
            this.mUiccProfile.iccCloseLogicalChannel(i, obtainMessage(7));
            this.mChannelId = -1;
        }
        this.mLoadedCallback.sendToTarget();
    }

    private String parseAcrf(String data) {
        String ret = null;
        String acRules = data;
        while (!acRules.isEmpty()) {
            UiccCarrierPrivilegeRules.TLV tlvRule = new UiccCarrierPrivilegeRules.TLV(TAG_ASN_SEQUENCE);
            try {
                acRules = tlvRule.parse(acRules, false);
                String ruleString = tlvRule.getValue();
                if (ruleString.startsWith(TAG_TARGET_AID)) {
                    UiccCarrierPrivilegeRules.TLV tlvTarget = new UiccCarrierPrivilegeRules.TLV(TAG_TARGET_AID);
                    UiccCarrierPrivilegeRules.TLV tlvAid = new UiccCarrierPrivilegeRules.TLV(TAG_ASN_OCTET_STRING);
                    UiccCarrierPrivilegeRules.TLV tlvAsnPath = new UiccCarrierPrivilegeRules.TLV(TAG_ASN_SEQUENCE);
                    UiccCarrierPrivilegeRules.TLV tlvPath = new UiccCarrierPrivilegeRules.TLV(TAG_ASN_OCTET_STRING);
                    String ruleString2 = tlvTarget.parse(ruleString, false);
                    tlvAid.parse(tlvTarget.getValue(), true);
                    if (CARRIER_RULE_AID.equals(tlvAid.getValue())) {
                        tlvAsnPath.parse(ruleString2, true);
                        tlvPath.parse(tlvAsnPath.getValue(), true);
                        ret = tlvPath.getValue();
                    }
                }
            } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
                log("Error: " + ex);
            }
        }
        return ret;
    }

    private void parseAccf(String data) {
        String acCondition = data;
        while (!acCondition.isEmpty()) {
            UiccCarrierPrivilegeRules.TLV tlvCondition = new UiccCarrierPrivilegeRules.TLV(TAG_ASN_SEQUENCE);
            UiccCarrierPrivilegeRules.TLV tlvCert = new UiccCarrierPrivilegeRules.TLV(TAG_ASN_OCTET_STRING);
            try {
                acCondition = tlvCondition.parse(acCondition, false);
                tlvCert.parse(tlvCondition.getValue(), true);
                if (!tlvCert.getValue().isEmpty()) {
                    this.mRules.add(tlvCert.getValue());
                }
            } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
                log("Error: " + ex);
                return;
            }
        }
    }

    public List<String> getRules() {
        return this.mRules;
    }

    /* access modifiers changed from: private */
    public static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mRules != null) {
            pw.println(" mRules:");
            Iterator<String> it = this.mRules.iterator();
            while (it.hasNext()) {
                pw.println("  " + it.next());
            }
        }
    }
}
