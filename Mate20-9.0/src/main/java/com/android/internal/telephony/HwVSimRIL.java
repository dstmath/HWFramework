package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractRIL;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import vendor.huawei.hardware.hisiradio.V1_0.IHisiRadio;

public final class HwVSimRIL extends HwHisiRIL {
    private static final boolean RILJ_LOGD = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwVSimRIL";

    public HwVSimRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
    }

    public HwVSimRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
    }

    static String responseToString(int request) {
        if (request == 3001) {
            return "UNSOL_RESIDENT_NETWORK_CHANGED";
        }
        if (request == 3010) {
            return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
        }
        if (request == 3027) {
            return "RIL_UNSOL_HW_TIMER_TASK_EXPIRED";
        }
        if (request == 3035) {
            return "RIL_UNSOL_HW_AP_DS_FLOW_INFO_REPORT";
        }
        if (request == 3125) {
            return "RIL_UNSOL_HW_RESTRAT_RILD_NV_MATCH";
        }
        switch (request) {
            case 3006:
                return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
            case 3007:
                return "UNSOL_HW_VSIM_RDH_REQUEST";
            default:
                return "<unknown response>: " + request;
        }
    }

    static String requestToString(int request) {
        return HwHisiRIL.requestToString(request);
    }

    /* access modifiers changed from: package-private */
    public void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + " [VSIM]");
    }

    /* access modifiers changed from: package-private */
    public void unsljLogRet(int response, Object ret) {
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

    public void clearTrafficData(Message result) {
        invokeIRadio(2090, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.clearDsFlowInfo(serial);
            }
        });
    }

    public void getTrafficData(Message result) {
        invokeIRadio(2089, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getDsFlowInfo(serial);
            }
        });
    }

    public void getDevSubMode(Message result) {
        invokeIRadio(2092, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getDeviceVersion(serial);
            }
        });
    }

    public void getSimStateViaSysinfoEx(Message result) {
        invokeIRadio(2041, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSystemInfoEx(serial);
            }
        });
    }

    public void hvCheckCard(Message result) {
        invokeIRadio(2111, result, new AbstractRIL.HisiRILCommand() {
            public void excute(IHisiRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimCheckCard(serial);
            }
        });
    }

    public void setOnVsimRegPLMNSelInfo(Handler h, int what, Object obj) {
        setOnRegPLMNSelInfo(h, what, obj);
        Rlog.d(RILJ_LOG_TAG, "setOnVsimRegPLMNSelInfo: " + h);
    }

    public void unSetOnVsimRegPLMNSelInfo(Handler h) {
        unSetOnRegPLMNSelInfo(h);
        Rlog.d(RILJ_LOG_TAG, "unSetOnVsimRegPLMNSelInfo: " + h);
    }
}
