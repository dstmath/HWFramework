package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class HwQualcommCsgSearch extends CsgSearch {
    private static final String LOG_TAG = "HwQualcommCsgSearch";

    public HwQualcommCsgSearch(GsmCdmaPhone phone) {
        super(phone);
    }

    @Override // com.android.internal.telephony.CsgSearch
    public void handleMessage(Message msg) {
        Rlog.d(LOG_TAG, "msg id is " + msg.what);
        int i = msg.what;
        if (i == 1) {
            Rlog.d(LOG_TAG, "=csg=  Receved EVENT_SELECT_CSG_NETWORK_DONE.");
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar == null) {
                Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                return;
            }
            Message onComplete = (Message) ar.userObj;
            if (onComplete != null) {
                CSGNetworkList csgNetworklist = (CSGNetworkList) ((AsyncResult) onComplete.obj).result;
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
        } else if (i != 5) {
            super.handleMessage(msg);
        } else {
            Rlog.i(LOG_TAG, "=csg= Receved EVENT_CSG_PERIODIC_SCAN_DONE.");
            AsyncResult ar2 = (AsyncResult) msg.obj;
            if (ar2 == null) {
                Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
            } else if (ar2.exception != null || ar2.result == null) {
                Rlog.e(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list failed! " + ar2.exception);
            } else {
                Rlog.d(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list success -> select Csg! ");
                if (((CSGNetworkList) ar2.result).isToBeSearchedCsgListsEmpty()) {
                    Rlog.i(LOG_TAG, "=csg= Periodic Search: no avaiable CSG-ID -> cancel periodic search! ");
                    cancelCsgPeriodicSearchTimer();
                    return;
                }
                selectCSGNetwork(obtainMessage(6, ar2));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.CsgSearch
    public void getAvailableCSGNetworks(Message response) {
        byte[] requestData = new byte[7];
        try {
            ByteBuffer buf = ByteBuffer.wrap(requestData);
            buf.order(ByteOrder.nativeOrder());
            buf.put((byte) 16);
            buf.putShort(1);
            buf.put((byte) 6);
            buf.put((byte) 17);
            buf.putShort(0);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "getAvailableCSGNeworks exception");
        }
        this.mPhone.mCi.getAvailableCSGNetworks(requestData, obtainMessage(0, response));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.CsgSearch
    public void selectCSGNetwork(Message response) {
        AsyncResult ar = (AsyncResult) response.obj;
        if (ar == null || ar.result == null) {
            Rlog.e(LOG_TAG, "=csg= parsed CSG list is null, return exception");
            AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            response.sendToTarget();
            return;
        }
        CSGNetworkList csgNetworklist = (CSGNetworkList) ar.result;
        if (csgNetworklist.mCSGNetworks.size() > 0) {
            HwQualcommCsgNetworkInfo curSelCsgNetwork = csgNetworklist.getToBeRegsiteredCSGNetwork();
            Rlog.d(LOG_TAG, "to be registered CSG info is " + curSelCsgNetwork);
            if (curSelCsgNetwork != null && !curSelCsgNetwork.isEmpty()) {
                byte[] requestData = new byte[13];
                try {
                    ByteBuffer buf = ByteBuffer.wrap(requestData);
                    buf.order(ByteOrder.nativeOrder());
                    buf.put((byte) 32);
                    buf.putShort(10);
                    buf.putShort(curSelCsgNetwork.mcc);
                    buf.putShort(curSelCsgNetwork.mnc);
                    buf.put(curSelCsgNetwork.bIncludePcsDigit);
                    buf.putInt(curSelCsgNetwork.iCSGId);
                    buf.put((byte) 5);
                    this.mPhone.mCi.setCSGNetworkSelectionModeManual(requestData, obtainMessage(1, response));
                } catch (Exception e) {
                    Rlog.e(LOG_TAG, "selectCSGNetwork exception");
                    AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                    response.sendToTarget();
                }
            } else if (curSelCsgNetwork == null || !curSelCsgNetwork.isEmpty()) {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, Select CSG fail!");
                AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                response.sendToTarget();
            } else {
                Rlog.e(LOG_TAG, "=csg= not find suitable CSG-ID, so finish search! ");
                AsyncResult.forMessage(response, (Object) null, (Throwable) null);
                response.sendToTarget();
            }
        } else {
            Rlog.e(LOG_TAG, "=csg= mCSGNetworks is not initailized, return with exception");
            AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            response.sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.CsgSearch
    public void handleCsgNetworkQueryResult(AsyncResult ar) {
        if (ar == null || ar.userObj == null) {
            Rlog.e(LOG_TAG, "=csg=  ar or userObj is null, the code should never come here!!");
        } else if (ar.exception != null) {
            Rlog.e(LOG_TAG, "=csg=  exception happen: " + ar.exception);
            AsyncResult.forMessage((Message) ar.userObj, (Object) null, ar.exception);
            ((Message) ar.userObj).sendToTarget();
        } else {
            CSGNetworkList csgNetworklist = new CSGNetworkList();
            if (csgNetworklist.parseCsgResponseData((byte[]) ar.result)) {
                AsyncResult.forMessage((Message) ar.userObj, csgNetworklist, (Throwable) null);
                ((Message) ar.userObj).sendToTarget();
                return;
            }
            AsyncResult.forMessage((Message) ar.userObj, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
            ((Message) ar.userObj).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public class CSGNetworkList {
        private static final byte CSG_INFO_TAG = 20;
        public static final int CSG_LIST_CAT_ALLOWED = 1;
        public static final int CSG_LIST_CAT_OPERATOR = 2;
        public static final int CSG_LIST_CAT_UNKNOWN = 0;
        private static final byte CSG_SCAN_RESULT_TAG = 19;
        private static final byte CSG_SIG_INFO_TAG = 21;
        public static final byte GSM_ONLY = 1;
        public static final byte LTE_ONLY = 4;
        public static final byte MNC_DIGIT_IS_THREE = 1;
        public static final byte MNC_DIGIT_IS_TWO = 0;
        public static final int NAS_SCAN_AS_ABORT = 1;
        public static final int NAS_SCAN_REJ_IN_RLF = 2;
        public static final int NAS_SCAN_SUCCESS = 0;
        public static final int RADIO_IF_GSM = 4;
        public static final int RADIO_IF_LTE = 8;
        public static final int RADIO_IF_TDSCDMA = 9;
        public static final int RADIO_IF_UMTS = 5;
        public static final int SCAN_RESULT_LEN_FAIL = 0;
        public static final int SCAN_RESULT_LEN_SUCC = 4;
        public static final byte TDSCDMA_ONLY = 8;
        public static final byte UMTS_ONLY = 2;
        public ArrayList<HwQualcommCsgNetworkInfo> mCSGNetworks;
        private HwQualcommCsgNetworkInfo mCurSelectingCsgNetwork;

        private CSGNetworkList() {
            this.mCSGNetworks = new ArrayList<>();
            this.mCurSelectingCsgNetwork = null;
        }

        public HwQualcommCsgNetworkInfo getCurrentSelectingCsgNetwork() {
            return this.mCurSelectingCsgNetwork;
        }

        public boolean parseCsgResponseData(byte[] data) {
            boolean isParseSucc;
            boolean isParseSucc2 = false;
            if (data == null) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= response data is null");
                return false;
            }
            try {
                ByteBuffer resultBuffer = ByteBuffer.wrap(data);
                resultBuffer.order(ByteOrder.nativeOrder());
                byte byteVar = resultBuffer.get();
                if (19 != byteVar) {
                    try {
                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResult  tag is an unexpected value: " + ((int) byteVar));
                        return false;
                    } catch (Exception e) {
                        isParseSucc = false;
                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "parseCSGResponseData exception occurrs");
                        return isParseSucc;
                    }
                } else {
                    short scanResultLen = resultBuffer.getShort();
                    if (scanResultLen == 0) {
                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResultLen is 0x00, scan failed");
                        return false;
                    } else if (4 == scanResultLen) {
                        int intVar = resultBuffer.getInt();
                        if (intVar != 0) {
                            Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResult is not success with the value: " + intVar + ", break");
                            return false;
                        }
                        Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResult is success, go on with the parsing");
                        byte byteVar2 = resultBuffer.get();
                        if (20 != byteVar2) {
                            Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= CSG_INFO_TAG is not corrcet with the value: " + ((int) byteVar2) + ", break");
                            return false;
                        } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == 0) {
                            Rlog.e(HwQualcommCsgSearch.LOG_TAG, "csg_info_total_len is 0x00, break");
                            return false;
                        } else {
                            byte numOfCsgInfoEntries = resultBuffer.get();
                            Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= numOfEntries for CSG info = " + ((int) numOfCsgInfoEntries));
                            if (numOfCsgInfoEntries > 0) {
                                for (int i = 0; i < numOfCsgInfoEntries; i++) {
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
                                byte byteVar3 = resultBuffer.get();
                                if (21 != byteVar3) {
                                    Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= CSG_SIG_INFO_TAG is not corrcet with the value: " + ((int) byteVar3) + ", break");
                                    return false;
                                } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == 0) {
                                    Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= csg_sig_info_total_len is 0x00, break");
                                    return false;
                                } else {
                                    byte numOfCsgSigInfoEntries = resultBuffer.get();
                                    Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg= numOfCsgSigInfoEntries for CSG sig info = " + ((int) numOfCsgSigInfoEntries));
                                    if (numOfCsgSigInfoEntries > 0) {
                                        int i2 = 0;
                                        while (i2 < numOfCsgSigInfoEntries) {
                                            short mcc = resultBuffer.getShort();
                                            short mnc = resultBuffer.getShort();
                                            byte bIncludePcsDigit = resultBuffer.get();
                                            int iCSGId = resultBuffer.getInt();
                                            int iCSGSignalStrength = resultBuffer.getInt();
                                            int s = this.mCSGNetworks.size();
                                            isParseSucc = isParseSucc2;
                                            int j = 0;
                                            while (true) {
                                                if (j >= s) {
                                                    break;
                                                }
                                                try {
                                                    if (mcc == this.mCSGNetworks.get(j).mcc && mnc == this.mCSGNetworks.get(j).mnc && bIncludePcsDigit == this.mCSGNetworks.get(j).bIncludePcsDigit && iCSGId == this.mCSGNetworks.get(j).iCSGId) {
                                                        this.mCSGNetworks.get(j).iSignalStrength = iCSGSignalStrength;
                                                        break;
                                                    }
                                                    j++;
                                                    s = s;
                                                } catch (Exception e2) {
                                                    Rlog.e(HwQualcommCsgSearch.LOG_TAG, "parseCSGResponseData exception occurrs");
                                                    return isParseSucc;
                                                }
                                            }
                                            i2++;
                                            resultBuffer = resultBuffer;
                                            isParseSucc2 = isParseSucc;
                                        }
                                        Rlog.i(HwQualcommCsgSearch.LOG_TAG, "=csg= parse csg response data successfull");
                                        return true;
                                    }
                                    Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= num Of Csg Sig Info Entries is not corrcet break");
                                    return false;
                                }
                            } else {
                                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= numOfCsgInfoEntries is not correct break");
                                return false;
                            }
                        }
                    } else {
                        Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= scanResultLen is invalid, scan failed");
                        return false;
                    }
                }
            } catch (Exception e3) {
                isParseSucc = false;
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "parseCSGResponseData exception occurrs");
                return isParseSucc;
            }
        }

        public HwQualcommCsgNetworkInfo getToBeRegsiteredCSGNetwork() {
            this.mCurSelectingCsgNetwork = null;
            if (this.mCSGNetworks == null) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return this.mCurSelectingCsgNetwork;
            }
            try {
                boolean uiccIsCsgAware = HwQualcommCsgSearch.this.isCsgAwareUicc();
                StringBuilder sb = new StringBuilder();
                sb.append("=csg= only search ");
                sb.append(uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown");
                sb.append(" CSG lists");
                Rlog.d(HwQualcommCsgSearch.LOG_TAG, sb.toString());
                int list_size = this.mCSGNetworks.size();
                for (int i = 0; i < list_size; i++) {
                    HwQualcommCsgNetworkInfo csgInfo = this.mCSGNetworks.get(i);
                    if (csgInfo.isSelectedFail) {
                        Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg=  had selected and failed, so not reselect again!");
                    } else if (!uiccIsCsgAware) {
                        if ((1 == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                            this.mCurSelectingCsgNetwork = csgInfo;
                        }
                    } else if (2 == csgInfo.iCSGListCat && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                        this.mCurSelectingCsgNetwork = csgInfo;
                    }
                }
                Rlog.i(HwQualcommCsgSearch.LOG_TAG, "=csg=  get the strongest CSG network: " + this.mCurSelectingCsgNetwork);
            } catch (Exception e) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "getToBeRegisteredCSGNetwork exception occurrs");
            }
            return this.mCurSelectingCsgNetwork;
        }

        public boolean isToBeSearchedCsgListsEmpty() {
            boolean uiccIsCsgAware = HwQualcommCsgSearch.this.isCsgAwareUicc();
            StringBuilder sb = new StringBuilder();
            sb.append("=csg= only search ");
            sb.append(uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown");
            sb.append(" CSG lists");
            Rlog.d(HwQualcommCsgSearch.LOG_TAG, sb.toString());
            ArrayList<HwQualcommCsgNetworkInfo> arrayList = this.mCSGNetworks;
            if (arrayList == null) {
                Rlog.e(HwQualcommCsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return true;
            }
            int list_size = arrayList.size();
            for (int i = 0; i < list_size; i++) {
                HwQualcommCsgNetworkInfo csgInfo = this.mCSGNetworks.get(i);
                if (!uiccIsCsgAware) {
                    if (1 == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) {
                        Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                        return false;
                    }
                } else if (2 == csgInfo.iCSGListCat) {
                    Rlog.d(HwQualcommCsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                    return false;
                }
            }
            return true;
        }
    }
}
