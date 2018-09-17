package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractRIL.RILCommand;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import vendor.huawei.hardware.radio.V1_0.CsgNetworkInfo;
import vendor.huawei.hardware.radio.V1_0.IRadio;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTH;

public class HwHisiRIL extends RIL {
    private static final int EVENT_RIL_CONNECTED = 100;
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwHisiRIL";
    private static final boolean SHOW_4G_PLUS_ICON = SystemProperties.getBoolean("ro.config.hw_show_4G_Plus_icon", false);
    private static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    private static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    protected int mApDsFlowConfig = 0;
    protected int mApDsFlowOper = 0;
    protected int mApDsFlowThreshold = 0;
    protected int mApDsFlowTotalThreshold = 0;
    private int mBalongSimSlot = 0;
    protected int mDsFlowNvEnable = 0;
    protected int mDsFlowNvInterval = 0;
    private Handler mHisiRilHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HwHisiRIL.this.riljLog("EVENT_RIL_CONNECTED, set AP time to CP.");
                    HwHisiRIL.this.setApTimeToCp();
                    return;
                default:
                    return;
            }
        }
    };
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                HwHisiRIL.this.riljLog("mIntentReceiver onReceive " + intent.getAction());
                HwHisiRIL.this.setApTimeToCp();
            }
        }
    };
    private Integer mRilInstanceId = null;

    private void setApTimeToCp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(14, -(calendar.get(15) + calendar.get(16)));
        Date utc = calendar.getTime();
        setTime(new SimpleDateFormat("yyyy/MM/dd").format(utc), new SimpleDateFormat("HH:mm:ss").format(utc), String.valueOf(TimeZone.getDefault().getRawOffset() / 3600000), null);
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
        registerIntentReceiver();
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRilInstanceId = instanceId;
        registerIntentReceiver();
        registerForRilConnected(this.mHisiRilHandler, 100, null);
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    protected Object processSolicitedEx(int rilRequest, Parcel p) {
        Object ret = super.processSolicitedEx(rilRequest, p);
        if (ret != null) {
            return ret;
        }
        switch (rilRequest) {
            case 2093:
                ret = responseInts(p);
                break;
            case 2108:
                ret = responseVoid(p);
                break;
            case 2132:
                ret = responseInts(p);
                break;
            default:
                return ret;
        }
        return ret;
    }

    static String requestToString(int request) {
        Rlog.d(RILJ_LOG_TAG, "Enter HwHisiRIL requestToString,");
        switch (request) {
            case 528:
                return "RIL_REQUEST_HW_QUERY_CARDTYPE";
            case 2019:
                return "RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE";
            case 2022:
                return "RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG";
            case 2028:
                return "RIL_REQUEST_HW_SET_SIM_SLOT_CFG";
            case 2029:
                return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
            case 2032:
                return "RIL_REQUEST_HW_SIM_GET_ATR";
            case 2037:
                return "RIL_REQUEST_HW_VSIM_SET_SIM_STATE";
            case 2038:
                return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
            case 2042:
                return "RIL_REQUEST_HW_GET_PLMN_INFO";
            case 2068:
                return "RIL_REQUEST_HW_SET_ISMCOEX";
            case 2075:
                return "RIL_REQUEST_HW_GET_ICCID";
            case 2087:
                return "RIL_REQUEST_HW_GET_LTE_FREQ_WITH_WLAN_COEX";
            case 2088:
                return "RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE";
            case 2093:
                return "RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG";
            case 2094:
                return "RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD";
            case 2108:
                return "RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION";
            case 2109:
                return "RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION";
            case 2119:
                return "RIL_REQUEST_HW_SET_UE_OPERATION_MODE";
            case 2120:
                return "RIL_REQUEST_HW_VSIM_POWER";
            case 2129:
                return "RIL_REQUEST_HW_QUERY_SERVICE_CELL_BAND";
            case 2130:
                return "RIL_REQUEST_HW_SET_TIME";
            case 2131:
                return "RIL_REQUEST_HW_GET_VSIM_BASEBAND_VERSION";
            case 2132:
                return "RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY";
            default:
                return "<unknown request>";
        }
    }

    public void rejectCallForCause(final int gsmIndex, final int cause, Message result) {
        invokeIRadio(2171, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.rejectCallWithReason(serial, gsmIndex, cause);
            }
        });
    }

    public void queryCardType(Message result) {
        invokeIRadio(528, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCardType(serial);
            }
        });
    }

    public void getSignalStrength(Message result) {
        invokeIRadio(331, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getHwSignalStrength(serial);
            }
        });
    }

    public void getBalongSim(Message result) {
        invokeIRadio(2029, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimSlot(serial);
            }
        });
    }

    public void setActiveModemMode(final int mode, Message result) {
        invokeIRadio(2088, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setActiveModemMode(serial, mode);
            }
        });
    }

    public void switchBalongSim(int modem1ToSlot, int modem2ToSlot, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2028, result, this.mRILDefaultWorkSource);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + ", modem1ToSlot: " + modem1ToSlot + " modem2ToSlot: " + modem2ToSlot + "currentSimSlot: " + this.mBalongSimSlot);
        send(rr);
    }

    public void switchBalongSim(final int modem1ToSlot, final int modem2ToSlot, final int modem3ToSlot, Message result) {
        invokeIRadio(2028, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                HwHisiRIL.this.riljLog("modem1ToSlot: " + modem1ToSlot + " modem2ToSlot: " + modem2ToSlot + " modem3ToSlot: " + modem3ToSlot);
                radio.setSimSlot(serial, modem1ToSlot, modem2ToSlot, modem3ToSlot);
            }
        });
    }

    public void iccGetATR(Message result) {
        invokeIRadio(2032, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimATR(serial);
            }
        });
    }

    public void getICCID(Message result) {
        invokeIRadio(2075, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getIccid(serial);
            }
        });
    }

    public void setLTEReleaseVersion(final int state, Message result) {
        invokeIRadio(2108, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setLteReleaseVersion(serial, state);
            }
        });
    }

    public void getLteReleaseVersion(Message result) {
        invokeIRadio(2109, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLteReleaseVersion(serial);
            }
        });
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        return response;
    }

    protected void notifyVpStatus(byte[] data) {
        int len = data.length;
        Rlog.d(RILJ_LOG_TAG, "notifyVpStatus: len = " + len);
        if (1 == len) {
            this.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
        }
    }

    void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mRilInstanceId != null ? " [SUB" + this.mRilInstanceId + "]" : ""));
    }

    public void switchVoiceCallBackgroundState(final int state, Message result) {
        invokeIRadio(2019, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVoicecallBackGroundState(serial, state);
            }
        });
    }

    public void getLocationInfo(Message result) {
        invokeIRadio(534, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLocationInfo(serial);
            }
        });
    }

    public void queryServiceCellBand(Message result) {
        invokeIRadio(2129, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.queryServiceCellBand(serial);
            }
        });
    }

    public void getSimState(Message result) {
        invokeIRadio(2038, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getVsimSimState(serial);
            }
        });
    }

    public void setSimState(final int index, final int enable, Message result) {
        invokeIRadio(2037, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVsimSimState(serial, index, enable, -1);
            }
        });
    }

    public void setTEEDataReady(final int apn, final int dh, final int sim, Message result) {
        invokeIRadio(2093, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVsimTEEDataReady(serial, apn, dh, sim);
            }
        });
    }

    public void hotSwitchSimSlot(final int modem0, final int modem1, final int modem2, Message result) {
        invokeIRadio(2094, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setSciChgCfg(serial, modem0, modem1, modem2);
            }
        });
    }

    public void getSimHotPlugState(Message result) {
        invokeIRadio(533, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimHotplugState(serial);
            }
        });
    }

    public void setUEOperationMode(final int mode, Message result) {
        invokeIRadio(2119, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setUeOperationMode(serial, mode);
            }
        });
    }

    public String getHwPrlVersion() {
        return SystemProperties.get("persist.radio.hwprlversion", "0");
    }

    public String getHwUimid() {
        return SystemProperties.get("persist.radio.hwuimid", "0");
    }

    public void setNetworkRatAndSrvDomainCfg(final int rat, final int srvDomain, Message result) {
        invokeIRadio(2022, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setNetworkRatAndSrvDomain(serial, rat, srvDomain);
            }
        });
    }

    public void setHwVSimPower(int power, Message result) {
        invokeIRadio(2120, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimPower(serial);
            }
        });
    }

    public void setISMCOEX(final String ISMCoexContent, Message result) {
        invokeIRadio(2068, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setIsmcoex(serial, ISMCoexContent);
            }
        });
    }

    public void sendCloudMessageToModem(int event_id) {
        String OEM_IDENTIFIER = "00000000";
        int mEventId = event_id;
        try {
            byte[] request = new byte[21];
            ByteBuffer buf = ByteBuffer.wrap(request);
            buf.order(ByteOrder.nativeOrder());
            buf.put(OEM_IDENTIFIER.getBytes("utf-8"));
            buf.putInt(210);
            buf.putInt(5);
            buf.putInt(event_id);
            buf.put((byte) 0);
            invokeOemRilRequestRaw(request, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            riljLog("HwCloudOTAService UnsupportedEncodingException");
        }
    }

    public void getRegPlmn(Message result) {
        invokeIRadio(2042, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPlmnInfo(serial);
            }
        });
    }

    public void getModemSupportVSimVersion(Message result) {
        invokeIRadio(2131, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vsimBasebandVersion(serial);
            }
        });
    }

    public void setApDsFlowCfg(int config, int threshold, int total_threshold, int oper, Message result) {
        this.mApDsFlowConfig = config;
        this.mApDsFlowThreshold = threshold;
        this.mApDsFlowTotalThreshold = total_threshold;
        this.mApDsFlowOper = oper;
        final int i = config;
        final int i2 = threshold;
        final int i3 = total_threshold;
        final int i4 = oper;
        invokeIRadio(2110, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setApDsFlowReportConfig(serial, i, i2, i3, i4);
            }
        });
    }

    public void setDsFlowNvCfg(final int enable, final int interval, Message result) {
        this.mDsFlowNvEnable = enable;
        this.mDsFlowNvInterval = interval;
        invokeIRadio(2112, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setDsFlowNvWriteConfigPara(serial, enable, interval);
            }
        });
    }

    public void setImsDomainConfig(final int selectDomain, Message result) {
        riljLog("setImsDomainConfig: " + selectDomain);
        invokeIRadio(2124, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setImsDomain(serial, selectDomain);
            }
        });
    }

    public void getImsDomain(Message result) {
        riljLog("getImsDomain");
        invokeIRadio(2126, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getImsDomain(serial);
            }
        });
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message result) {
        riljLog("handleUiccAuth");
        final RILUICCAUTH uiccAuth = new RILUICCAUTH();
        uiccAuth.authType = auth_type;
        String rand_str = IccUtils.bytesToHexString(rand);
        String auth_str = IccUtils.bytesToHexString(auth);
        if (rand_str != null) {
            uiccAuth.authParams.randLen = rand_str.length();
        }
        uiccAuth.authParams.rand = rand_str;
        if (auth_str != null) {
            uiccAuth.authParams.authLen = auth_str.length();
        }
        uiccAuth.authParams.auth = auth_str;
        invokeIRadio(2128, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.uiccAuth(serial, uiccAuth);
            }
        });
    }

    public void handleMapconImsaReq(byte[] Msg, Message result) {
        riljLog("handleMapconImsaReq: Msg = 0x" + IccUtils.bytesToHexString(Msg));
        final ArrayList<Byte> arrList = new ArrayList();
        for (byte valueOf : Msg) {
            arrList.add(Byte.valueOf(valueOf));
        }
        invokeIRadio(2125, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.vowifiToImsaMsg(serial, arrList);
            }
        });
    }

    public void setTime(final String date, final String time, final String timezone, Message result) {
        if (date == null || time == null || timezone == null) {
            Rlog.e(RILJ_LOG_TAG, "setTime check");
        }
        invokeIRadio(2130, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setTime(serial, date, time, timezone);
            }
        });
    }

    private void registerIntentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
    }

    public void notifyCellularCommParaReady(final int paratype, final int pathtype, Message result) {
        riljLog("notifyCellularCommParaReady: paratype = " + paratype + ", pathtype = " + pathtype);
        if (1 == paratype) {
            invokeIRadio(2132, result, new RILCommand() {
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwHisiRIL.this.riljLog("RIL_REQUEST_HW_SET_BASIC_COMM_PARA_READY");
                    radio.notifyCellularCommParaReady(serial, paratype, pathtype);
                }
            });
        }
        if (2 == paratype) {
            invokeIRadio(2133, result, new RILCommand() {
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwHisiRIL.this.riljLog("RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY");
                    radio.notifyCellularCloudParaReady(serial, paratype, pathtype);
                }
            });
        }
    }

    public void send(RILRequestReference rr) {
        Rlog.d(RILJ_LOG_TAG, "not use socket send");
    }

    public void getLteFreqWithWlanCoex(Message result) {
        invokeIRadio(2087, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLwclash(serial);
            }
        });
    }

    public void sendPseudocellCellInfo(int infoType, int lac, int cid, int radiotech, String plmn, Message result) {
        final int i = infoType;
        final int i2 = lac;
        final int i3 = cid;
        final int i4 = radiotech;
        final String str = plmn;
        invokeIRadio(2154, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendPseudocellCellInfo(serial, i, i2, i3, i4, str);
            }
        });
    }

    public void getAvailableCSGNetworks(Message result) {
        invokeIRadio(2155, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getAvailableCsgIds(serial);
            }
        });
    }

    public void setCSGNetworkSelectionModeManual(Object csgInfo, Message result) {
        final CsgNetworkInfo hisiCsg = new CsgNetworkInfo();
        HwHisiCsgNetworkInfo tempCsg = (HwHisiCsgNetworkInfo) csgInfo;
        hisiCsg.csgId = tempCsg.getCSGId();
        hisiCsg.plmn = tempCsg.getOper();
        hisiCsg.networkRat = tempCsg.getRat();
        invokeIRadio(2156, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.manualSelectionCsgId(serial, hisiCsg);
            }
        });
    }

    public void setDmRcsConfig(final int rcsCapability, final int devConfig, Message response) {
        invokeIRadio(2163, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio instanceof vendor.huawei.hardware.radio.V1_1.IRadio) {
                    ((vendor.huawei.hardware.radio.V1_1.IRadio) radio).setDmRcsConfig(serial, rcsCapability, devConfig);
                } else {
                    HwHisiRIL.this.riljLog("not support below radio 1.1");
                }
            }
        });
    }

    public void setRcsSwitch(final int switchState, Message response) {
        invokeIRadio(2161, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio instanceof vendor.huawei.hardware.radio.V1_1.IRadio) {
                    ((vendor.huawei.hardware.radio.V1_1.IRadio) radio).setRcsSwitch(serial, switchState);
                } else {
                    HwHisiRIL.this.riljLog("not support below radio 1.1");
                }
            }
        });
    }

    public void getRcsSwitchState(Message response) {
        invokeIRadio(2162, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio instanceof vendor.huawei.hardware.radio.V1_1.IRadio) {
                    ((vendor.huawei.hardware.radio.V1_1.IRadio) radio).getRcsSwitchState(serial);
                } else {
                    HwHisiRIL.this.riljLog("not support below radio 1.1");
                }
            }
        });
    }

    public void setDmPcscf(final String data, Message response) {
        invokeIRadio(2164, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio instanceof vendor.huawei.hardware.radio.V1_1.IRadio) {
                    ((vendor.huawei.hardware.radio.V1_1.IRadio) radio).setDmPcscf(serial, data);
                } else {
                    HwHisiRIL.this.riljLog("not support below radio 1.1");
                }
            }
        });
    }

    public void sendLaaCmd(final int cmd, final String reserved, Message result) {
        invokeIRadio(2157, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.sendLaaCmd(serial, cmd, reserved);
            }
        });
    }

    public void getLaaDetailedState(final String reserved, Message result) {
        invokeIRadio(2158, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getLaaDetailedState(serial, reserved);
            }
        });
    }

    public void setupEIMEDataCall(Message result) {
        riljLog("setupEIMEDataCall");
        invokeIRadio(2173, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setupDataCallEmergency(serial);
            }
        });
    }

    public void deactivateEIMEDataCall(Message result) {
        riljLog("deactivateEIMEDataCall");
        invokeIRadio(2174, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.deactivateDataCallEmergency(serial);
            }
        });
    }

    public void getEnhancedCellInfoList(Message result, WorkSource workSource) {
        riljLog("getEnhancedCellInfoList");
        invokeIRadio(2172, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCellInfoListOtdoa(serial);
            }
        });
    }

    public void writeSimLockNwData(final int field, final String data, Message result) {
        riljLog("writeSimLockNwData");
        invokeIRadio(2177, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.simlockNwDataWrite(serial, field, data);
            }
        });
    }
}
