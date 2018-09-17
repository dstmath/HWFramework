package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException.Error;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class HwQualcommCsgSearch extends CsgSearch {
    private static final String LOG_TAG = "HwQualcommCsgSearch";

    private class CSGNetworkList {
        private static final byte CSG_INFO_TAG = (byte) 20;
        public static final int CSG_LIST_CAT_ALLOWED = 1;
        public static final int CSG_LIST_CAT_OPERATOR = 2;
        public static final int CSG_LIST_CAT_UNKNOWN = 0;
        private static final byte CSG_SCAN_RESULT_TAG = (byte) 19;
        private static final byte CSG_SIG_INFO_TAG = (byte) 21;
        public static final byte GSM_ONLY = (byte) 1;
        public static final byte LTE_ONLY = (byte) 4;
        public static final byte MNC_DIGIT_IS_THREE = (byte) 1;
        public static final byte MNC_DIGIT_IS_TWO = (byte) 0;
        public static final int NAS_SCAN_AS_ABORT = 1;
        public static final int NAS_SCAN_REJ_IN_RLF = 2;
        public static final int NAS_SCAN_SUCCESS = 0;
        public static final int RADIO_IF_GSM = 4;
        public static final int RADIO_IF_LTE = 8;
        public static final int RADIO_IF_TDSCDMA = 9;
        public static final int RADIO_IF_UMTS = 5;
        public static final int SCAN_RESULT_LEN_FAIL = 0;
        public static final int SCAN_RESULT_LEN_SUCC = 4;
        public static final byte TDSCDMA_ONLY = (byte) 8;
        public static final byte UMTS_ONLY = (byte) 2;
        public ArrayList<HwQualcommCsgNetworkInfo> mCSGNetworks;
        private HwQualcommCsgNetworkInfo mCurSelectingCsgNetwork;

        /* synthetic */ CSGNetworkList(HwQualcommCsgSearch this$0, CSGNetworkList -this1) {
            this();
        }

        private CSGNetworkList() {
            this.mCSGNetworks = new ArrayList();
            this.mCurSelectingCsgNetwork = null;
        }

        public HwQualcommCsgNetworkInfo getCurrentSelectingCsgNetwork() {
            return this.mCurSelectingCsgNetwork;
        }

        public boolean parseCsgResponseData(byte[] data) {
            boolean isParseSucc = false;
            if (data == null) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= response data is null");
                return false;
            }
            try {
                ByteBuffer resultBuffer = ByteBuffer.wrap(data);
                resultBuffer.order(ByteOrder.nativeOrder());
                byte byteVar = resultBuffer.get();
                if ((byte) 19 != byteVar) {
                    Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResult  tag is an unexpected value: " + byteVar);
                } else {
                    short scanResultLen = resultBuffer.getShort();
                    if (scanResultLen == (short) 0) {
                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResultLen is 0x00, scan failed");
                    } else if ((short) 4 == scanResultLen) {
                        int intVar = resultBuffer.getInt();
                        if (intVar != 0) {
                            Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResult is not success with the value: " + intVar + ", break");
                        } else {
                            Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResult is success, go on with the parsing");
                            byteVar = resultBuffer.get();
                            if ((byte) 20 != byteVar) {
                                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= CSG_INFO_TAG is not corrcet with the value: " + byteVar + ", break");
                            } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == (short) 0) {
                                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "csg_info_total_len is 0x00, break");
                            } else {
                                byte numOfCsgInfoEntries = resultBuffer.get();
                                Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= numOfEntries for CSG info = " + numOfCsgInfoEntries);
                                if (numOfCsgInfoEntries > (byte) 0) {
                                    byte i;
                                    for (i = (byte) 0; i < numOfCsgInfoEntries; i++) {
                                        HwQualcommCsgNetworkInfo csgNetworkInfo = new HwQualcommCsgNetworkInfo();
                                        csgNetworkInfo.mcc = resultBuffer.getShort();
                                        csgNetworkInfo.mnc = resultBuffer.getShort();
                                        csgNetworkInfo.bIncludePcsDigit = resultBuffer.get();
                                        csgNetworkInfo.iCSGListCat = resultBuffer.getInt();
                                        csgNetworkInfo.iCSGId = resultBuffer.getInt();
                                        byte[] nameBuffer = new byte[(resultBuffer.get() * 2)];
                                        resultBuffer.get(nameBuffer);
                                        csgNetworkInfo.sCSGName = new String(nameBuffer, "UTF-16");
                                        this.mCSGNetworks.add(csgNetworkInfo);
                                    }
                                    byteVar = resultBuffer.get();
                                    if ((byte) 21 != byteVar) {
                                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= CSG_SIG_INFO_TAG is not corrcet with the value: " + byteVar + ", break");
                                    } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == (short) 0) {
                                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= csg_sig_info_total_len is 0x00, break");
                                    } else {
                                        byte numOfCsgSigInfoEntries = resultBuffer.get();
                                        Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= numOfCsgSigInfoEntries for CSG sig info = " + numOfCsgSigInfoEntries);
                                        if (numOfCsgSigInfoEntries > (byte) 0) {
                                            for (i = (byte) 0; i < numOfCsgSigInfoEntries; i++) {
                                                short mcc = resultBuffer.getShort();
                                                short mnc = resultBuffer.getShort();
                                                byte bIncludePcsDigit = resultBuffer.get();
                                                int iCSGId = resultBuffer.getInt();
                                                int iCSGSignalStrength = resultBuffer.getInt();
                                                int j = 0;
                                                int s = this.mCSGNetworks.size();
                                                while (j < s) {
                                                    if (mcc == ((HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(j)).mcc && mnc == ((HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(j)).mnc && bIncludePcsDigit == ((HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(j)).bIncludePcsDigit && iCSGId == ((HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(j)).iCSGId) {
                                                        ((HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(j)).iSignalStrength = iCSGSignalStrength;
                                                        break;
                                                    }
                                                    j++;
                                                }
                                            }
                                            Rlog.i(HwQualcommCsgSearch.LOG_TAG, "=csg= parse csg response data successfull");
                                            isParseSucc = true;
                                        } else {
                                            Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= num Of Csg Sig Info Entries is not corrcet break");
                                        }
                                    }
                                } else {
                                    Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= numOfCsgInfoEntries is not correct break");
                                }
                            }
                        }
                    } else {
                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResultLen is invalid, scan failed");
                    }
                }
            } catch (Exception e) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= exception occurrs: " + e);
            }
            return isParseSucc;
        }

        public HwQualcommCsgNetworkInfo getToBeRegsiteredCSGNetwork() {
            this.mCurSelectingCsgNetwork = null;
            if (this.mCSGNetworks == null) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return this.mCurSelectingCsgNetwork;
            }
            try {
                boolean uiccIsCsgAware = HwQualcommCsgSearch.this.isCsgAwareUicc();
                Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= only search " + (uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown") + " CSG lists");
                int list_size = this.mCSGNetworks.size();
                for (int i = 0; i < list_size; i++) {
                    HwQualcommCsgNetworkInfo csgInfo = (HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(i);
                    if (csgInfo.isSelectedFail) {
                        Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg=  had selected and failed, so not reselect again!");
                    } else if (uiccIsCsgAware) {
                        if (2 == csgInfo.iCSGListCat && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                            this.mCurSelectingCsgNetwork = csgInfo;
                        }
                    } else if ((1 == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                        this.mCurSelectingCsgNetwork = csgInfo;
                    }
                }
                Rlog.i(HwQualcommCsgSearch.LOG_TAG, "=csg=  get the strongest CSG network: " + this.mCurSelectingCsgNetwork);
            } catch (Exception e) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg=  exception occurrs: " + e);
            }
            return this.mCurSelectingCsgNetwork;
        }

        public boolean isToBeSearchedCsgListsEmpty() {
            boolean isEmpty = true;
            boolean uiccIsCsgAware = HwQualcommCsgSearch.this.isCsgAwareUicc();
            Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= only search " + (uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown") + " CSG lists");
            if (this.mCSGNetworks == null) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return true;
            }
            int list_size = this.mCSGNetworks.size();
            for (int i = 0; i < list_size; i++) {
                HwQualcommCsgNetworkInfo csgInfo = (HwQualcommCsgNetworkInfo) this.mCSGNetworks.get(i);
                if (uiccIsCsgAware) {
                    if (2 == csgInfo.iCSGListCat) {
                        Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                        isEmpty = false;
                        break;
                    }
                } else if (1 == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) {
                    Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }

    public HwQualcommCsgSearch(GsmCdmaPhone phone) {
        super(phone);
    }

    public void handleMessage(Message msg) {
        Rlog.d(LOG_TAG, "msg id is " + msg.what);
        AsyncResult ar;
        CSGNetworkList csgNetworklist;
        switch (msg.what) {
            case 1:
                Rlog.d(LOG_TAG, "=csg=  Receved EVENT_SELECT_CSG_NETWORK_DONE.");
                ar = msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                    return;
                }
                Message onComplete = ar.userObj;
                if (onComplete != null) {
                    csgNetworklist = onComplete.obj.result;
                    if (ar.exception != null) {
                        Rlog.e(LOG_TAG, "=csg= select CSG failed! " + ar.exception);
                        HwQualcommCsgNetworkInfo curSelectingCsgNetwork = csgNetworklist.getCurrentSelectingCsgNetwork();
                        if (curSelectingCsgNetwork == null) {
                            Rlog.i(LOG_TAG, "=csg= current select CSG is null->maybe loop end. response result.");
                            AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                            onComplete.sendToTarget();
                            return;
                        }
                        curSelectingCsgNetwork.isSelectedFail = true;
                        Rlog.e(LOG_TAG, "=csg= mark  current CSG-ID item Failed!" + csgNetworklist.mCurSelectingCsgNetwork);
                        Rlog.i(LOG_TAG, "=csg= select next strongest CSG-ID->start select");
                        selectCSGNetwork(onComplete);
                        return;
                    }
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                Rlog.e(LOG_TAG, "=csg=  ar.userObj is null, the code should never come here!!");
                return;
            case CSGNetworkList.RADIO_IF_UMTS /*5*/:
                Rlog.i(LOG_TAG, "=csg= Receved EVENT_CSG_PERIODIC_SCAN_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                } else if (ar.exception != null || ar.result == null) {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list failed! " + ar.exception);
                    return;
                } else {
                    csgNetworklist = (CSGNetworkList) ar.result;
                    Rlog.d(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list success -> select Csg! ");
                    if (csgNetworklist.isToBeSearchedCsgListsEmpty()) {
                        Rlog.i(LOG_TAG, "=csg= Periodic Search: no avaiable CSG-ID -> cancel periodic search! ");
                        cancelCsgPeriodicSearchTimer();
                        return;
                    }
                    selectCSGNetwork(obtainMessage(6, ar));
                    return;
                }
            default:
                super.handleMessage(msg);
                return;
        }
    }

    void getAvailableCSGNetworks(Message response) {
        byte[] requestData = new byte[7];
        try {
            ByteBuffer buf = ByteBuffer.wrap(requestData);
            buf.order(ByteOrder.nativeOrder());
            buf.put((byte) 16);
            buf.putShort((short) 1);
            buf.put((byte) 6);
            buf.put((byte) 17);
            buf.putShort((short) 0);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "exception occurrs: " + e);
        }
        this.mPhone.mCi.getAvailableCSGNetworks(requestData, obtainMessage(0, response));
    }

    void selectCSGNetwork(Message response) {
        AsyncResult ar = response.obj;
        if (ar == null || ar.result == null) {
            Rlog.e(LOG_TAG, "=csg= parsed CSG list is null, return exception");
            AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
            response.sendToTarget();
            return;
        }
        CSGNetworkList csgNetworklist = ar.result;
        if (csgNetworklist.mCSGNetworks.size() > 0) {
            HwQualcommCsgNetworkInfo curSelCsgNetwork = csgNetworklist.getToBeRegsiteredCSGNetwork();
            Rlog.d(LOG_TAG, "to be registered CSG info is " + curSelCsgNetwork);
            if (curSelCsgNetwork != null && (curSelCsgNetwork.isEmpty() ^ 1) != 0) {
                byte[] requestData = new byte[13];
                try {
                    ByteBuffer buf = ByteBuffer.wrap(requestData);
                    buf.order(ByteOrder.nativeOrder());
                    buf.put((byte) 32);
                    buf.putShort((short) 10);
                    buf.putShort(curSelCsgNetwork.mcc);
                    buf.putShort(curSelCsgNetwork.mnc);
                    buf.put(curSelCsgNetwork.bIncludePcsDigit);
                    buf.putInt(curSelCsgNetwork.iCSGId);
                    buf.put((byte) 5);
                    this.mPhone.mCi.setCSGNetworkSelectionModeManual(requestData, obtainMessage(1, response));
                    return;
                } catch (Exception e) {
                    Rlog.e(LOG_TAG, "=csg= exception occurrs: " + e);
                    AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                    response.sendToTarget();
                    return;
                }
            } else if (curSelCsgNetwork == null || !curSelCsgNetwork.isEmpty()) {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, Select CSG fail!");
                AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                response.sendToTarget();
                return;
            } else {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, so finish search! ");
                AsyncResult.forMessage(response, null, null);
                response.sendToTarget();
                return;
            }
        }
        Rlog.e(LOG_TAG, "=csg= mCSGNetworks is not initailized, return with exception");
        AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
        response.sendToTarget();
    }

    void handleCsgNetworkQueryResult(AsyncResult ar) {
        if (ar == null || ar.userObj == null) {
            Rlog.e(LOG_TAG, "=csg=  ar or userObj is null, the code should never come here!!");
        } else if (ar.exception != null) {
            Rlog.e(LOG_TAG, "=csg=  exception happen: " + ar.exception);
            AsyncResult.forMessage((Message) ar.userObj, null, ar.exception);
            ((Message) ar.userObj).sendToTarget();
        } else {
            CSGNetworkList csgNetworklist = new CSGNetworkList(this, null);
            if (csgNetworklist.parseCsgResponseData((byte[]) ar.result)) {
                AsyncResult.forMessage((Message) ar.userObj, csgNetworklist, null);
                ((Message) ar.userObj).sendToTarget();
            } else {
                AsyncResult.forMessage((Message) ar.userObj, null, new CommandException(Error.GENERIC_FAILURE));
                ((Message) ar.userObj).sendToTarget();
            }
        }
    }
}
