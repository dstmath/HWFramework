package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.IccRecords;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public final class CsgSearch extends Handler {
    private static final int EVENT_CSG_MANUAL_SCAN_DONE = 2;
    private static final int EVENT_CSG_MANUAL_SELECT_DONE = 3;
    private static final int EVENT_CSG_OCSGL_LOADED = 7;
    private static final int EVENT_CSG_PERIODIC_SCAN_DONE = 5;
    private static final int EVENT_CSG_PERIODIC_SEARCH_TIMEOUT = 4;
    private static final int EVENT_CSG_PERIODIC_SELECT_DONE = 6;
    private static final int EVENT_GET_AVAILABLE_CSG_NETWORK_DONE = 0;
    private static final int EVENT_SELECT_CSG_NETWORK_DONE = 1;
    private static final String LOG_TAG = "CsgSearch";
    private static final String OPERATOR_NAME_ATT_MICROCELL = "AT&T MicroCell";
    private static final int TIMER_CSG_PERIODIC_SEARCH = 7200000;
    private static boolean mIsSupportCsgSearch;
    private GsmCdmaPhone mPhone;

    private static class CSGNetworkInfo {
        public byte bIncludePcsDigit;
        public int iCSGId;
        public int iCSGListCat;
        public int iSignalStrength;
        public boolean isSelectedFail;
        public short mcc;
        public short mnc;
        public String sCSGName;

        private CSGNetworkInfo() {
            this.isSelectedFail = false;
        }

        public boolean isEmpty() {
            if (this.mcc == (short) 0 && this.mnc == (short) 0 && this.bIncludePcsDigit == null && this.iCSGListCat == 0 && this.iCSGId == 0) {
                return (this.sCSGName == null || this.sCSGName.isEmpty()) && this.iSignalStrength == 0;
            } else {
                return false;
            }
        }

        public void set(CSGNetworkInfo csgNeworkInfo) {
            this.mcc = csgNeworkInfo.mcc;
            this.mnc = csgNeworkInfo.mnc;
            this.bIncludePcsDigit = csgNeworkInfo.bIncludePcsDigit;
            this.iCSGListCat = csgNeworkInfo.iCSGListCat;
            this.iCSGId = csgNeworkInfo.iCSGId;
            if (csgNeworkInfo.sCSGName != null) {
                this.sCSGName = csgNeworkInfo.sCSGName;
            }
            this.iSignalStrength = csgNeworkInfo.iSignalStrength;
            this.isSelectedFail = csgNeworkInfo.isSelectedFail;
        }

        public String toString() {
            return "CSGNetworkInfo: mcc: " + this.mcc + ", mnc: " + this.mnc + ", bIncludePcsDigit: " + this.bIncludePcsDigit + ", iCSGListCat: " + this.iCSGListCat + ", iCSGId: " + this.iCSGId + ", sCSGName: " + this.sCSGName + ", iSignalStrength: " + this.iSignalStrength + " ,isSelectedFail:" + this.isSelectedFail;
        }
    }

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
        public ArrayList<CSGNetworkInfo> mCSGNetworks;
        private CSGNetworkInfo mCurSelectingCsgNetwork;

        private CSGNetworkList() {
            this.mCSGNetworks = new ArrayList();
            this.mCurSelectingCsgNetwork = null;
        }

        public CSGNetworkInfo getCurrentSelectingCsgNetwork() {
            return this.mCurSelectingCsgNetwork;
        }

        public boolean parseCsgResponseData(byte[] data) {
            boolean isParseSucc = false;
            if (data == null) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= response data is null");
                return false;
            }
            try {
                ByteBuffer resultBuffer = ByteBuffer.wrap(data);
                resultBuffer.order(ByteOrder.nativeOrder());
                byte byteVar = resultBuffer.get();
                if (19 != byteVar) {
                    Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResult  tag is an unexpected value: " + byteVar);
                } else {
                    short scanResultLen = resultBuffer.getShort();
                    if (scanResultLen == (short) 0) {
                        Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResultLen is 0x00, scan failed");
                    } else if (SCAN_RESULT_LEN_SUCC == scanResultLen) {
                        int intVar = resultBuffer.getInt();
                        if (intVar != 0) {
                            Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResult is not success with the value: " + intVar + ", break");
                        } else {
                            Rlog.d(CsgSearch.LOG_TAG, "=csg= scanResult is success, go on with the parsing");
                            byteVar = resultBuffer.get();
                            if (20 != byteVar) {
                                Rlog.e(CsgSearch.LOG_TAG, "=csg= CSG_INFO_TAG is not corrcet with the value: " + byteVar + ", break");
                            } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == (short) 0) {
                                Rlog.e(CsgSearch.LOG_TAG, "csg_info_total_len is 0x00, break");
                            } else {
                                byte numOfCsgInfoEntries = resultBuffer.get();
                                Rlog.d(CsgSearch.LOG_TAG, "=csg= numOfEntries for CSG info = " + numOfCsgInfoEntries);
                                if (numOfCsgInfoEntries > null) {
                                    byte i;
                                    for (i = MNC_DIGIT_IS_TWO; i < numOfCsgInfoEntries; i += NAS_SCAN_AS_ABORT) {
                                        CSGNetworkInfo csgNetworkInfo = new CSGNetworkInfo();
                                        csgNetworkInfo.mcc = resultBuffer.getShort();
                                        csgNetworkInfo.mnc = resultBuffer.getShort();
                                        csgNetworkInfo.bIncludePcsDigit = resultBuffer.get();
                                        csgNetworkInfo.iCSGListCat = resultBuffer.getInt();
                                        csgNetworkInfo.iCSGId = resultBuffer.getInt();
                                        byte[] nameBuffer = new byte[(resultBuffer.get() * NAS_SCAN_REJ_IN_RLF)];
                                        resultBuffer.get(nameBuffer);
                                        csgNetworkInfo.sCSGName = new String(nameBuffer, "UTF-16");
                                        this.mCSGNetworks.add(csgNetworkInfo);
                                    }
                                    byteVar = resultBuffer.get();
                                    if (21 != byteVar) {
                                        Rlog.e(CsgSearch.LOG_TAG, "=csg= CSG_SIG_INFO_TAG is not corrcet with the value: " + byteVar + ", break");
                                    } else if (Short.valueOf(resultBuffer.getShort()).shortValue() == (short) 0) {
                                        Rlog.e(CsgSearch.LOG_TAG, "=csg= csg_sig_info_total_len is 0x00, break");
                                    } else {
                                        byte numOfCsgSigInfoEntries = resultBuffer.get();
                                        Rlog.d(CsgSearch.LOG_TAG, "=csg= numOfCsgSigInfoEntries for CSG sig info = " + numOfCsgSigInfoEntries);
                                        if (numOfCsgSigInfoEntries > null) {
                                            for (i = MNC_DIGIT_IS_TWO; i < numOfCsgSigInfoEntries; i += NAS_SCAN_AS_ABORT) {
                                                short mcc = resultBuffer.getShort();
                                                short mnc = resultBuffer.getShort();
                                                byte bIncludePcsDigit = resultBuffer.get();
                                                int iCSGId = resultBuffer.getInt();
                                                int iCSGSignalStrength = resultBuffer.getInt();
                                                int s = this.mCSGNetworks.size();
                                                for (int j = SCAN_RESULT_LEN_FAIL; j < s; j += NAS_SCAN_AS_ABORT) {
                                                    short s2 = ((CSGNetworkInfo) this.mCSGNetworks.get(j)).mcc;
                                                    if (mcc == r0) {
                                                        s2 = ((CSGNetworkInfo) this.mCSGNetworks.get(j)).mnc;
                                                        if (mnc == r0) {
                                                            byte b = ((CSGNetworkInfo) this.mCSGNetworks.get(j)).bIncludePcsDigit;
                                                            if (bIncludePcsDigit == r0) {
                                                                int i2 = ((CSGNetworkInfo) this.mCSGNetworks.get(j)).iCSGId;
                                                                if (iCSGId == r0) {
                                                                    ((CSGNetworkInfo) this.mCSGNetworks.get(j)).iSignalStrength = iCSGSignalStrength;
                                                                    break;
                                                                }
                                                            } else {
                                                                continue;
                                                            }
                                                        } else {
                                                            continue;
                                                        }
                                                    }
                                                }
                                            }
                                            Rlog.i(CsgSearch.LOG_TAG, "=csg= parse csg response data successfull");
                                            isParseSucc = true;
                                        } else {
                                            Rlog.e(CsgSearch.LOG_TAG, "=csg= num Of Csg Sig Info Entries is not corrcet break");
                                        }
                                    }
                                } else {
                                    Rlog.e(CsgSearch.LOG_TAG, "=csg= numOfCsgInfoEntries is not correct break");
                                }
                            }
                        }
                    } else {
                        Rlog.e(CsgSearch.LOG_TAG, "=csg= scanResultLen is invalid, scan failed");
                    }
                }
            } catch (Exception e) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= exception occurrs: " + e);
            }
            return isParseSucc;
        }

        public CSGNetworkInfo getToBeRegsiteredCSGNetwork() {
            this.mCurSelectingCsgNetwork = null;
            if (this.mCSGNetworks == null) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return this.mCurSelectingCsgNetwork;
            }
            try {
                boolean uiccIsCsgAware = CsgSearch.this.isCsgAwareUicc();
                Rlog.d(CsgSearch.LOG_TAG, "=csg= only search " + (uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown") + " CSG lists");
                for (CSGNetworkInfo csgInfo : this.mCSGNetworks) {
                    if (csgInfo.isSelectedFail) {
                        Rlog.d(CsgSearch.LOG_TAG, "=csg=  had selected and failed, so not reselect again!");
                    } else if (uiccIsCsgAware) {
                        if (NAS_SCAN_REJ_IN_RLF == csgInfo.iCSGListCat && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                            this.mCurSelectingCsgNetwork = csgInfo;
                        }
                    } else if ((NAS_SCAN_AS_ABORT == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) && (this.mCurSelectingCsgNetwork == null || csgInfo.iSignalStrength < this.mCurSelectingCsgNetwork.iSignalStrength)) {
                        this.mCurSelectingCsgNetwork = csgInfo;
                    }
                }
                Rlog.i(CsgSearch.LOG_TAG, "=csg=  get the strongest CSG network: " + this.mCurSelectingCsgNetwork);
            } catch (Exception e) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg=  exception occurrs: " + e);
            }
            return this.mCurSelectingCsgNetwork;
        }

        public boolean isToBeSearchedCsgListsEmpty() {
            boolean isEmpty = true;
            boolean uiccIsCsgAware = CsgSearch.this.isCsgAwareUicc();
            Rlog.d(CsgSearch.LOG_TAG, "=csg= only search " + (uiccIsCsgAware ? "EF-Operator" : "UE Allowed or unknown") + " CSG lists");
            if (this.mCSGNetworks == null) {
                Rlog.e(CsgSearch.LOG_TAG, "=csg= input param is null, not should be here!");
                return true;
            }
            for (CSGNetworkInfo csgInfo : this.mCSGNetworks) {
                if (uiccIsCsgAware) {
                    if (NAS_SCAN_REJ_IN_RLF == csgInfo.iCSGListCat) {
                        Rlog.d(CsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                        isEmpty = false;
                        break;
                    }
                } else if (NAS_SCAN_AS_ABORT == csgInfo.iCSGListCat || csgInfo.iCSGListCat == 0) {
                    Rlog.d(CsgSearch.LOG_TAG, "=csg=  have one valid CSG item " + csgInfo);
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.CsgSearch.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.CsgSearch.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.CsgSearch.<clinit>():void");
    }

    public static boolean isSupportCsgSearch() {
        return mIsSupportCsgSearch;
    }

    public CsgSearch(GsmCdmaPhone phone) {
        this.mPhone = phone;
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        CSGNetworkList csgNetworklist;
        switch (msg.what) {
            case EVENT_GET_AVAILABLE_CSG_NETWORK_DONE /*0*/:
                Rlog.d(LOG_TAG, "=csg= Receved EVENT_GET_AVAILABLE_CSG_NETWORK_DONE.");
                ar = msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                } else {
                    handleCsgNetworkQueryResult(ar);
                }
            case EVENT_SELECT_CSG_NETWORK_DONE /*1*/:
                Rlog.d(LOG_TAG, "=csg=  Receved EVENT_SELECT_CSG_NETWORK_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg=  ar is null, the code should never come here!!");
                    return;
                }
                Message onComplete = ar.userObj;
                if (onComplete != null) {
                    csgNetworklist = onComplete.obj.result;
                    if (ar.exception != null) {
                        Rlog.e(LOG_TAG, "=csg= select CSG failed! " + ar.exception);
                        CSGNetworkInfo curSelectingCsgNetwork = csgNetworklist.getCurrentSelectingCsgNetwork();
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
            case EVENT_CSG_MANUAL_SCAN_DONE /*2*/:
                Rlog.d(LOG_TAG, "=csg= Receved EVENT_CSG_MANUAL_SCAN_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                } else if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "=csg= Manual Search: get avaiable CSG list failed! -> response " + ar.exception);
                    AsyncResult.forMessage((Message) ar.userObj, null, ar.exception);
                    ((Message) ar.userObj).sendToTarget();
                } else {
                    Rlog.i(LOG_TAG, "=csg= Manual Search: get avaiable CSG list success -> select Csg! ");
                    selectCSGNetwork(obtainMessage(EVENT_CSG_MANUAL_SELECT_DONE, ar));
                }
            case EVENT_CSG_MANUAL_SELECT_DONE /*3*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_MANUAL_SELECT_DONE!");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                }
                if (ar.exception != null) {
                    Rlog.i(LOG_TAG, "=csg= Manual Search: CSG-ID selection is failed! " + ar.exception);
                } else {
                    Rlog.i(LOG_TAG, "=csg= Manual Search: CSG-ID selection is success! ");
                }
                AsyncResult arUsrObj = (AsyncResult) ar.userObj;
                if (arUsrObj == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                    return;
                }
                AsyncResult.forMessage((Message) arUsrObj.userObj, null, ar.exception);
                ((Message) arUsrObj.userObj).sendToTarget();
            case EVENT_CSG_PERIODIC_SEARCH_TIMEOUT /*4*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_PERIODIC_SEARCH_TIMEOUT!");
                trigerPeriodicCsgSearch();
                Rlog.d(LOG_TAG, "=csg=  launch next Csg Periodic search timer!");
                judgeToLaunchCsgPeriodicSearchTimer();
            case EVENT_CSG_PERIODIC_SCAN_DONE /*5*/:
                Rlog.i(LOG_TAG, "=csg= Receved EVENT_CSG_PERIODIC_SCAN_DONE.");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                } else if (ar.exception != null || ar.result == null) {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list failed! " + ar.exception);
                } else {
                    csgNetworklist = (CSGNetworkList) ar.result;
                    Rlog.d(LOG_TAG, "=csg= Periodic Search: get avaiable CSG list success -> select Csg! ");
                    if (csgNetworklist.isToBeSearchedCsgListsEmpty()) {
                        Rlog.i(LOG_TAG, "=csg= Periodic Search: no avaiable CSG-ID -> cancel periodic search! ");
                        cancelCsgPeriodicSearchTimer();
                        return;
                    }
                    selectCSGNetwork(obtainMessage(EVENT_CSG_PERIODIC_SELECT_DONE, ar));
                }
            case EVENT_CSG_PERIODIC_SELECT_DONE /*6*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_PERIODIC_SELECT_DONE!");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "=csg= ar is null, the code should never come here!!");
                } else if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: CSG-ID selection is failed! " + ar.exception);
                } else {
                    Rlog.e(LOG_TAG, "=csg= Periodic Search: CSG-ID selection is success! ");
                }
            case EVENT_CSG_OCSGL_LOADED /*7*/:
                Rlog.d(LOG_TAG, "=csg= EVENT_CSG_OCSGL_LOADED!");
                judgeToLaunchCsgPeriodicSearchTimer();
            default:
                Rlog.e(LOG_TAG, "unexpected event not handled: " + msg.what);
        }
    }

    public void selectCsgNetworkManually(Message response) {
        Rlog.i(LOG_TAG, "start manual select CSG network...");
        getAvailableCSGNetworks(obtainMessage(EVENT_CSG_MANUAL_SCAN_DONE, response));
    }

    public void registerForCsgRecordsLoadedEvent() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            r.registerForCsgRecordsLoaded(this, EVENT_CSG_OCSGL_LOADED, null);
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            r.unregisterForCsgRecordsLoaded(this);
        }
    }

    private void getAvailableCSGNetworks(Message response) {
        byte[] requestData = new byte[EVENT_CSG_OCSGL_LOADED];
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
        this.mPhone.mCi.getAvailableCSGNetworks(requestData, obtainMessage(EVENT_GET_AVAILABLE_CSG_NETWORK_DONE, response));
    }

    private void selectCSGNetwork(Message response) {
        AsyncResult ar = response.obj;
        if (ar == null || ar.result == null) {
            Rlog.e(LOG_TAG, "=csg= parsed CSG list is null, return exception");
            AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
            response.sendToTarget();
            return;
        }
        CSGNetworkList csgNetworklist = ar.result;
        if (csgNetworklist.mCSGNetworks.size() > 0) {
            CSGNetworkInfo curSelCsgNetwork = csgNetworklist.getToBeRegsiteredCSGNetwork();
            Rlog.d(LOG_TAG, "to be registered CSG info is " + curSelCsgNetwork);
            if (curSelCsgNetwork != null && !curSelCsgNetwork.isEmpty()) {
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
                    this.mPhone.mCi.setCSGNetworkSelectionModeManual(requestData, obtainMessage(EVENT_SELECT_CSG_NETWORK_DONE, response));
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

    private void handleCsgNetworkQueryResult(AsyncResult ar) {
        if (ar == null || ar.userObj == null) {
            Rlog.e(LOG_TAG, "=csg=  ar or userObj is null, the code should never come here!!");
        } else if (ar.exception != null) {
            Rlog.e(LOG_TAG, "=csg=  exception happen: " + ar.exception);
            AsyncResult.forMessage((Message) ar.userObj, null, ar.exception);
            ((Message) ar.userObj).sendToTarget();
        } else {
            CSGNetworkList csgNetworklist = new CSGNetworkList();
            if (csgNetworklist.parseCsgResponseData((byte[]) ar.result)) {
                AsyncResult.forMessage((Message) ar.userObj, csgNetworklist, null);
                ((Message) ar.userObj).sendToTarget();
            } else {
                AsyncResult.forMessage((Message) ar.userObj, null, new CommandException(Error.GENERIC_FAILURE));
                ((Message) ar.userObj).sendToTarget();
            }
        }
    }

    private boolean isCsgAwareUicc() {
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r == null || r.getOcsgl().length > 0 || r.getCsglexist()) {
            return true;
        }
        Rlog.d(LOG_TAG, "=csg=  EF-Operator not present =>CSG not Aware UICC");
        return false;
    }

    private void trigerPeriodicCsgSearch() {
        getAvailableCSGNetworks(obtainMessage(EVENT_CSG_PERIODIC_SCAN_DONE));
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        boolean isLaunchTimer = false;
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        ServiceState ss = this.mPhone.getServiceState();
        String operatorAlpha = SystemProperties.get("gsm.operator.alpha", "");
        if (!(ss == null || ((ss.getVoiceRegState() != 0 && ss.getDataRegState() != 0) || ss.getRoaming() || operatorAlpha == null || OPERATOR_NAME_ATT_MICROCELL.equals(operatorAlpha)))) {
            isLaunchTimer = true;
        }
        if (isLaunchTimer && r != null) {
            byte[] csgLists = r.getOcsgl();
            if (r.getCsglexist() && csgLists.length == 0) {
                Rlog.d(LOG_TAG, "=csg= EFOCSGL is empty, not trigger periodic search!");
                isLaunchTimer = false;
            }
        }
        if (isLaunchTimer) {
            launchCsgPeriodicSearchTimer();
        } else {
            cancelCsgPeriodicSearchTimer();
        }
    }

    private void launchCsgPeriodicSearchTimer() {
        if (!hasMessages(EVENT_CSG_PERIODIC_SEARCH_TIMEOUT)) {
            Rlog.d(LOG_TAG, "=csg= lauch periodic search timer!");
            sendEmptyMessageDelayed(EVENT_CSG_PERIODIC_SEARCH_TIMEOUT, 7200000);
        }
    }

    private void cancelCsgPeriodicSearchTimer() {
        if (hasMessages(EVENT_CSG_PERIODIC_SEARCH_TIMEOUT)) {
            Rlog.d(LOG_TAG, "=csg= cancel periodic search timer!");
            removeMessages(EVENT_CSG_PERIODIC_SEARCH_TIMEOUT);
        }
    }
}
