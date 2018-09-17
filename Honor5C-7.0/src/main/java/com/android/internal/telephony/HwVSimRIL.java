package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class HwVSimRIL extends RIL {
    private static final boolean RILJ_LOGD = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwVSimRIL";
    private int mApDsFlowConfig;
    private int mApDsFlowOper;
    private int mApDsFlowThreshold;
    private int mApDsFlowTotalThreshold;
    private int mDsFlowNvEnable;
    private int mDsFlowNvInterval;
    private final RegistrantList mVsimRegPLMNSelInfoRegistrants;

    public HwVSimRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
        this.mApDsFlowConfig = 0;
        this.mApDsFlowThreshold = 0;
        this.mApDsFlowTotalThreshold = 0;
        this.mApDsFlowOper = 0;
        this.mDsFlowNvEnable = 0;
        this.mDsFlowNvInterval = 0;
        this.mVsimRegPLMNSelInfoRegistrants = new RegistrantList();
    }

    public HwVSimRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mApDsFlowConfig = 0;
        this.mApDsFlowThreshold = 0;
        this.mApDsFlowTotalThreshold = 0;
        this.mApDsFlowOper = 0;
        this.mDsFlowNvEnable = 0;
        this.mDsFlowNvInterval = 0;
        this.mVsimRegPLMNSelInfoRegistrants = new RegistrantList();
    }

    protected Object processSolicitedEx(int rilRequest, Parcel p) {
        Object ret = super.processSolicitedEx(rilRequest, p);
        if (ret != null) {
            return ret;
        }
        if (rilRequest == 2037) {
            ret = responseInts(p);
        } else if (rilRequest == 2038) {
            ret = responseInts(p);
        } else if (rilRequest == 2093) {
            ret = responseInts(p);
        } else if (rilRequest == 2042) {
            ret = responseString(p);
        } else if (rilRequest == 2041) {
            ret = responseInts(p);
        } else if (rilRequest == 2089) {
            ret = responseTrafficData(p);
        } else if (rilRequest == 2090) {
            ret = responseVoid(p);
        } else if (rilRequest == 2110) {
            ret = responseVoid(p);
        } else if (rilRequest == 2092) {
            ret = responseDeviceVersion(p);
        } else if (rilRequest == 2075) {
            ret = responseICCID(p);
        } else if (rilRequest == 2111) {
            ret = responseInts(p);
        } else if (rilRequest == 2112) {
            ret = responseVoid(p);
        } else if (rilRequest == 2088) {
            ret = responseInts(p);
        } else if (rilRequest == 2094) {
            ret = responseVoid(p);
            if (isPlatformTwoModems()) {
                this.shouldBreakRilSocket = RILJ_LOGD;
            }
        } else if (rilRequest == 2029) {
            ret = responseInts(p);
        } else if (rilRequest == 2072) {
            ret = responseVoid(p);
        } else if (rilRequest == 2107) {
            ret = responseVoid(p);
        } else if (rilRequest == 2011) {
            ret = responseVoid(p);
        } else if (rilRequest == 2012) {
            ret = responseVoid(p);
        } else if (rilRequest == 2119) {
            ret = responseVoid(p);
        } else if (rilRequest == 2022) {
            ret = responseVoid(p);
        } else if (rilRequest == 2120) {
            ret = responseVoid(p);
        } else if (rilRequest == 2073) {
            ret = responseInts(p);
        } else if (rilRequest == 2005) {
            ret = responseVoid(p);
        } else if (rilRequest == 2131) {
            ret = responseInts(p);
        }
        return ret;
    }

    protected Object handleUnsolicitedDefaultMessagePara(int response, Parcel p) {
        Object ret = super.handleUnsolicitedDefaultMessagePara(response, p);
        if (ret != null) {
            return ret;
        }
        if (response == 3007) {
            ret = responseVoid(p);
        } else if (response == 3010) {
            ret = responseInts(p);
        } else if (response == 3035) {
            ret = responseApDsFlowInfoReport(p);
        } else if (response == 3027) {
            ret = responseInts(p);
        } else if (response == 3001) {
            ret = responseString(p);
        } else if (response == 3006) {
            ret = responseStrings(p);
        }
        return ret;
    }

    public void handleUnsolicitedDefaultMessage(int response, Object ret, Context context) {
        super.handleUnsolicitedDefaultMessage(response, ret, context);
        switch (response) {
            case 3001:
                unsljLogRet(response, ret);
                if (this.mUnsolRplmnsStateRegistrant != null) {
                    this.mUnsolRplmnsStateRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3006:
                unsljLogRet(response, ret);
                if (this.mNetRejectRegistrant != null) {
                    this.mNetRejectRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3007:
                Rlog.d(RILJ_LOG_TAG, "deal RIL_UNSOL_HW_VSIM_RDH_REQUEST");
                unsljLogRet(response, ret);
                if (this.mVsimRDHRegistrant != null) {
                    this.mVsimRDHRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3010:
                Rlog.d(RILJ_LOG_TAG, "deal RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND");
                unsljLogRet(response, ret);
                if (this.mVsimRegPLMNSelInfoRegistrants != null) {
                    this.mVsimRegPLMNSelInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3027:
                Rlog.d(RILJ_LOG_TAG, "deal RIL_UNSOL_HW_TIMER_TASK_EXPIRED");
                unsljLogRet(response, ret);
                if (this.mVsimTimerTaskExpiredRegistrant != null) {
                    this.mVsimTimerTaskExpiredRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3035:
                unsljLogRet(response, ret);
                if (this.mVsimApDsFlowInfoRegistrant != null) {
                    this.mVsimApDsFlowInfoRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            default:
        }
    }

    static String responseToString(int request) {
        switch (request) {
            case 3001:
                return "UNSOL_RESIDENT_NETWORK_CHANGED";
            case 3006:
                return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
            case 3007:
                return "UNSOL_HW_VSIM_RDH_REQUEST";
            case 3010:
                return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
            case 3027:
                return "RIL_UNSOL_HW_TIMER_TASK_EXPIRED";
            case 3035:
                return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
            default:
                return "<unknown response>: " + request;
        }
    }

    static String requestToString(int request) {
        switch (request) {
            case 2005:
                return "RIL_REQUEST_HW_RESTRAT_RILD";
            case 2011:
                return "RIL_REQUEST_DATA_CONNECTION_DETACH";
            case 2012:
                return "RIL_REQUEST_DATA_CONNECTION_ATTACH";
            case 2022:
                return "RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG";
            case 2029:
                return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
            case 2037:
                return "RIL_REQUEST_HW_VSIM_SET_SIM_STATE";
            case 2038:
                return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
            case 2041:
                return "RIL_REQUEST_HW_GET_SYSTEM_INFO_EX";
            case 2042:
                return "RIL_REQUEST_HW_GET_PLMN_INFO";
            case 2072:
                return "RIL_REQUEST_HW_SET_RAT_COMBINE_PRIO";
            case 2073:
                return "RIL_REQUEST_HW_GET_RAT_COMBINE_PRIO";
            case 2075:
                return "RIL_REQUEST_HW_GET_ICCID";
            case 2088:
                return "RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE";
            case 2089:
                return "RIL_REQUEST_HW_GET_DS_FLOW_INFO";
            case 2090:
                return "RIL_REQUEST_HW_CLEAR_DS_FLOW_INFO";
            case 2092:
                return "RIL_REQUEST_HW_GET_DEVICE_VERSION";
            case 2093:
                return "RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG";
            case 2094:
                return "RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD";
            case 2107:
                return "RIL_REQUEST_HW_RAT_RFIC_CHANNEL_SWITCH";
            case 2110:
                return "RIL_REQUEST_HW_SET_AP_DS_FLOW_CONFIG";
            case 2111:
                return "RIL_REQUEST_HW_VSIM_CHECK_CARD";
            case 2112:
                return "RIL_REQUEST_SET_DS_FLOW_NV_WRITE_CFG_PARA";
            case 2119:
                return "RIL_REQUEST_HW_SET_UE_OPERATION_MODE";
            case 2120:
                return "RIL_REQUEST_HW_VSIM_POWER";
            case 2131:
                return "RIL_REQUEST_HW_GET_VSIM_BASEBAND_VERSION";
            default:
                return "<unknown request>: " + request;
        }
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        return response;
    }

    private Object responseString(Parcel p) {
        return p.readString();
    }

    private Object responseStrings(Parcel p) {
        return p.readStringArray();
    }

    private Object responseTrafficData(Parcel p) {
        String[] response = new String[6];
        for (int i = 0; i < 6; i++) {
            response[i] = p.readString();
        }
        return response;
    }

    private Object responseDeviceVersion(Parcel p) {
        String[] response = new String[11];
        for (int i = 0; i < 11; i++) {
            response[i] = p.readString();
        }
        return response;
    }

    private Object responseICCID(Parcel p) {
        return hexStringToBcd(p.readString());
    }

    private Object responseApDsFlowInfoReport(Parcel p) {
        return new String[]{p.readString(), p.readString(), p.readString(), p.readString(), p.readString(), p.readString(), p.readString()};
    }

    private static byte[] hexStringToBcd(String s) {
        if (s == null) {
            return new byte[0];
        }
        int sz = s.length();
        byte[] ret = new byte[(sz / 2)];
        for (int i = 0; i < sz; i += 2) {
            ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i + 1)) << 4) | hexCharToInt(s.charAt(i)));
        }
        return ret;
    }

    private static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 97) + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + " [VSIM]");
    }

    private void unsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + responseToString(response) + " " + retToString(response, ret));
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
    }

    public void setRadioPower(boolean on, Message result) {
        super.setRadioPower(on, result);
        if (on && this.mApDsFlowConfig == 1) {
            setApDsFlowCfg(this.mApDsFlowConfig, this.mApDsFlowThreshold, this.mApDsFlowTotalThreshold, this.mApDsFlowOper, null);
        }
        if (on && this.mDsFlowNvEnable == 1) {
            setDsFlowNvCfg(this.mDsFlowNvEnable, this.mDsFlowNvInterval, null);
        }
    }

    public void getSimState(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2038, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setSimState(int index, int enable, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2037, response);
        if (isPlatformTwoModems()) {
            rr.getParcel().writeInt(3);
            rr.getParcel().writeInt(index);
            rr.getParcel().writeInt(enable);
            rr.getParcel().writeInt(1);
        } else {
            rr.getParcel().writeInt(2);
            rr.getParcel().writeInt(index);
            rr.getParcel().writeInt(enable);
        }
        riljLog(rr.serialString() + "> setSimState: " + requestToString(rr.getRequest()) + " index= " + index + ", enable = " + enable);
        send(rr);
    }

    public void setTEEDataReady(int apn, int dh, int sim, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2093, response);
        rr.getParcel().writeInt(3);
        rr.getParcel().writeInt(apn);
        rr.getParcel().writeInt(dh);
        rr.getParcel().writeInt(sim);
        riljLog(rr.serialString() + "> setTEEDataReady: " + requestToString(rr.getRequest()) + " apn= " + apn + ", dh = " + dh + ", sim = " + sim);
        send(rr);
    }

    public void getRegPlmn(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2042, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void clearTrafficData(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2090, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getTrafficData(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2089, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getDevSubMode(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2092, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getSimStateViaSysinfoEx(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2041, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setApDsFlowCfg(int config, int threshold, int total_threshold, int oper, Message response) {
        this.mApDsFlowConfig = config;
        this.mApDsFlowThreshold = threshold;
        this.mApDsFlowTotalThreshold = total_threshold;
        this.mApDsFlowOper = oper;
        RILRequestReference rr = RILRequestReference.obtain(2110, response);
        rr.getParcel().writeInt(4);
        rr.getParcel().writeInt(config);
        rr.getParcel().writeInt(threshold);
        rr.getParcel().writeInt(total_threshold);
        rr.getParcel().writeInt(oper);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + " config: " + config + " threshold: " + threshold + " total_threshold: " + total_threshold + " oper: " + oper);
        send(rr);
    }

    public void getICCID(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2075, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void hvCheckCard(Message response) {
        send(RILRequestReference.obtain(2111, response));
    }

    public void setDsFlowNvCfg(int enable, int interval, Message response) {
        this.mDsFlowNvEnable = enable;
        this.mDsFlowNvInterval = interval;
        RILRequestReference rr = RILRequestReference.obtain(2112, response);
        rr.getParcel().writeInt(2);
        rr.getParcel().writeInt(enable);
        rr.getParcel().writeInt(interval);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + " enable: " + enable + " interval: " + interval);
        send(rr);
    }

    public void setActiveModemMode(int mode, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2088, response);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void hotSwitchSimSlot(int modem0, int modem1, int modem2, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2094, result);
        rr.getParcel().writeInt(3);
        rr.getParcel().writeInt(modem0);
        rr.getParcel().writeInt(modem1);
        rr.getParcel().writeInt(modem2);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + "modem0: " + modem0 + " modem1: " + modem1 + " modem2: " + modem2);
        send(rr);
    }

    public void hotSwitchSimSlotFor2Modem(int modem0, int modem1, int modem2, Message result) {
        int sendData1;
        int sendData2;
        riljLog("[2Cards]hotSwitchSimSlotFor2Modem modem0=" + modem0 + " modem1=" + modem1 + " modem2=" + modem2);
        if ((1 == modem1 && modem0 == 0 && 2 == modem2) || (2 == modem0 && modem2 == 0)) {
            sendData1 = 0;
            sendData2 = 1;
            if (modem0 == 0) {
                this.mRilSocketMaps[0] = 0;
                this.mRilSocketMaps[1] = 1;
                this.mRilSocketMaps[2] = 2;
            } else {
                this.mRilSocketMaps[0] = 2;
                this.mRilSocketMaps[1] = 1;
                this.mRilSocketMaps[2] = 0;
            }
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[0]=" + this.mRilSocketMaps[0]);
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[1]=" + this.mRilSocketMaps[1]);
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
        } else if ((modem1 == 0 && 1 == modem0 && 2 == modem2) || (2 == modem0 && 1 == modem2)) {
            sendData1 = 1;
            sendData2 = 0;
            if (1 == modem0) {
                this.mRilSocketMaps[0] = 1;
                this.mRilSocketMaps[1] = 0;
                this.mRilSocketMaps[2] = 2;
            } else {
                this.mRilSocketMaps[0] = 1;
                this.mRilSocketMaps[1] = 2;
                this.mRilSocketMaps[2] = 0;
            }
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[0]=" + this.mRilSocketMaps[0]);
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[1]=" + this.mRilSocketMaps[1]);
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
        } else {
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem error branch!");
            return;
        }
        if (this.mRilSocketMaps[0] == 2 || this.mRilSocketMaps[1] == 2) {
            this.mRilSocketMapEnable = RILJ_LOGD;
        } else {
            this.mRilSocketMapEnable = false;
        }
        if (this.mSocket == null) {
            notifyPendingRilSocket();
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem notify mPendingRilSocketLock!");
            this.mResultMessage = result;
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set shouldBreakRilSocket true!");
            return;
        }
        RILRequestReference rr = RILRequestReference.obtain(2094, null);
        rr.getParcel().writeInt(2);
        rr.getParcel().writeInt(sendData1);
        rr.getParcel().writeInt(sendData2);
        this.mResultMessage = result;
        send(rr);
        riljLog(rr.serialString() + "[2Cards]> " + requestToString(rr.getRequest()) + " sendData1: " + sendData1 + " sendData2: " + sendData2);
    }

    public boolean updateSocketMapForSlaveSub(int modem0, int modem1, int modem2) {
        riljLog("[2Cards]updateSocketMapForSlaveSub modem0=" + modem0 + " modem1=" + modem1 + " modem2=" + modem2);
        if ((1 == modem1 && modem0 == 0 && 2 == modem2) || (2 == modem0 && modem2 == 0)) {
            if (modem0 == 0) {
                this.mRilSocketMaps[0] = 0;
                this.mRilSocketMaps[1] = 1;
                this.mRilSocketMaps[2] = 2;
            } else {
                this.mRilSocketMaps[0] = 2;
                this.mRilSocketMaps[1] = 1;
                this.mRilSocketMaps[2] = 0;
            }
        } else if ((modem1 != 0 || 1 != modem0 || 2 != modem2) && (2 != modem0 || 1 != modem2)) {
            riljLog("[2Cards]updateSocketMapForSlaveSub error branch!");
            return false;
        } else if (1 == modem0) {
            this.mRilSocketMaps[0] = 1;
            this.mRilSocketMaps[1] = 0;
            this.mRilSocketMaps[2] = 2;
        } else {
            this.mRilSocketMaps[0] = 1;
            this.mRilSocketMaps[1] = 2;
            this.mRilSocketMaps[2] = 0;
        }
        riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMaps[0]=" + this.mRilSocketMaps[0]);
        riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMaps[1]=" + this.mRilSocketMaps[1]);
        riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
        if (this.mRilSocketMaps[0] == 2 || this.mRilSocketMaps[1] == 2) {
            this.mRilSocketMapEnable = RILJ_LOGD;
            riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMapEnable true!");
        } else {
            this.mRilSocketMapEnable = false;
            riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMapEnable false!");
        }
        return RILJ_LOGD;
    }

    public void getBalongSim(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2029, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setHwRatCombineMode(int combineMode, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2072, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(combineMode);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setHwRFChannelSwitch(int rfChannel, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2107, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(rfChannel);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void dataConnectionDetach(int mode, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2011, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void dataConnectionAttach(int mode, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2012, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setUEOperationMode(int mode, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2119, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setNetworkRatAndSrvDomainCfg(int rat, int srvDomain, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2022, result);
        rr.getParcel().writeInt(2);
        rr.getParcel().writeInt(rat);
        rr.getParcel().writeInt(srvDomain);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + " rat: " + rat + " srvDomain: " + srvDomain);
        send(rr);
    }

    public void setHwVSimPower(int power, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2120, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(power);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + " power: " + power);
        send(rr);
    }

    public void getHwRatCombineMode(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2073, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void restartRild(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2005, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getModemSupportVSimVersion(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2131, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setOnVsimRegPLMNSelInfo(Handler h, int what, Object obj) {
        this.mVsimRegPLMNSelInfoRegistrants.add(new Registrant(h, what, obj));
        Rlog.d(RILJ_LOG_TAG, "setOnVsimRegPLMNSelInfo: " + h);
    }

    public void unSetOnVsimRegPLMNSelInfo(Handler h) {
        this.mVsimRegPLMNSelInfoRegistrants.remove(h);
        Rlog.d(RILJ_LOG_TAG, "unSetOnVsimRegPLMNSelInfo: " + h);
    }
}
