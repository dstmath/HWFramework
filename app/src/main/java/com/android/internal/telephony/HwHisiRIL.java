package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.UiccAuthResponse;
import android.telephony.UiccAuthResponse.UiccAuthResponseData;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class HwHisiRIL extends RIL {
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwHisiRIL";
    private static final boolean SHOW_4G_PLUS_ICON = false;
    private Object crrConnRet;
    private int mBalongSimSlot;
    private final RegistrantList mHwCrrConnIndRegistrants;
    private final BroadcastReceiver mIntentReceiver;
    private final RegistrantList mRegPLMNSelInfoRegistrants;
    private Integer mRilInstanceId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwHisiRIL.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwHisiRIL.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwHisiRIL.<clinit>():void");
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
        this.mBalongSimSlot = 0;
        this.mRilInstanceId = null;
        this.crrConnRet = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                    HwHisiRIL.this.riljLog("mIntentReceiver onReceive " + intent.getAction());
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(14, -(calendar.get(15) + calendar.get(16)));
                    Date utc = calendar.getTime();
                    HwHisiRIL.this.setTime(new SimpleDateFormat("yyyy/MM/dd").format(utc), new SimpleDateFormat("HH:mm:ss").format(utc), String.valueOf(TimeZone.getDefault().getRawOffset() / 3600000), null);
                }
            }
        };
        this.mHwCrrConnIndRegistrants = new RegistrantList();
        this.mRegPLMNSelInfoRegistrants = new RegistrantList();
        registerIntentReceiver();
    }

    public HwHisiRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mBalongSimSlot = 0;
        this.mRilInstanceId = null;
        this.crrConnRet = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.TIME_SET".equals(intent.getAction()) || "android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                    HwHisiRIL.this.riljLog("mIntentReceiver onReceive " + intent.getAction());
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(14, -(calendar.get(15) + calendar.get(16)));
                    Date utc = calendar.getTime();
                    HwHisiRIL.this.setTime(new SimpleDateFormat("yyyy/MM/dd").format(utc), new SimpleDateFormat("HH:mm:ss").format(utc), String.valueOf(TimeZone.getDefault().getRawOffset() / 3600000), null);
                }
            }
        };
        this.mHwCrrConnIndRegistrants = new RegistrantList();
        this.mRegPLMNSelInfoRegistrants = new RegistrantList();
        this.mRilInstanceId = instanceId;
        registerIntentReceiver();
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
            case 534:
                ret = responseStrings(p);
                break;
            case 2006:
                ret = responseICC_IO(p);
                break;
            case 2007:
                ret = responseInts(p);
                break;
            case 2008:
                ret = responseVoid(p);
                break;
            case 2009:
                ret = responseICC_IO(p);
                break;
            case 2019:
                ret = responseVoid(p);
                break;
            case 2022:
                ret = responseVoid(p);
                break;
            case 2029:
                ret = responseSimSlot(p);
                break;
            case 2032:
                ret = responseString(p);
                break;
            case 2037:
                ret = responseInts(p);
                break;
            case 2038:
                ret = responseInts(p);
                break;
            case 2042:
                ret = responseString(p);
                break;
            case 2075:
                ret = responseICCID(p);
                break;
            case 2087:
                ret = responseInts(p);
                break;
            case 2088:
                ret = responseInts(p);
                break;
            case 2093:
                ret = responseInts(p);
                break;
            case 2094:
                ret = responseVoid(p);
                if (isPlatformTwoModems()) {
                    this.shouldBreakRilSocket = RILJ_LOGV;
                    break;
                }
                break;
            case 2108:
                ret = responseVoid(p);
                break;
            case 2109:
                ret = responseInts(p);
                break;
            case 2119:
                ret = responseVoid(p);
                break;
            case 2120:
                ret = responseVoid(p);
                break;
            case 2124:
                ret = responseVoid(p);
                break;
            case 2125:
                ret = responseVoid(p);
                break;
            case 2126:
                ret = responseInts(p);
                break;
            case 2128:
                ret = responseUiccAuth(p);
                break;
            case 2129:
                ret = responseStrings(p);
                break;
            case 2130:
                ret = responseVoid(p);
                break;
            case 2131:
                ret = responseInts(p);
                break;
            default:
                return ret;
        }
        return ret;
    }

    protected Object handleUnsolicitedDefaultMessagePara(int response, Parcel p) {
        Object ret = super.handleUnsolicitedDefaultMessagePara(response, p);
        if (ret != null) {
            return ret;
        }
        switch (response) {
            case 1520:
                ret = responseInts(p);
                break;
            case 1521:
                ret = responseString(p);
                break;
            case 3006:
                ret = responseStrings(p);
                break;
            case 3010:
                ret = responseInts(p);
                break;
            case 3031:
                ret = null;
                break;
            case 3032:
                ret = responseRaw(p);
                break;
            case 3037:
                ret = responseInts(p);
                break;
            case 3041:
                ret = responseRaw(p);
                break;
            case 3047:
                ret = responseInts(p);
                break;
            default:
                return ret;
        }
        return ret;
    }

    public void handleUnsolicitedDefaultMessage(int response, Object ret, Context context) {
        super.handleUnsolicitedDefaultMessage(response, ret, context);
        switch (response) {
            case 136:
                break;
            case 1520:
                unsljLog(response);
                if (this.mSimHotPlugRegistrants != null) {
                    this.mSimHotPlugRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 1521:
                unsljLog(response);
                if (this.mIccidChangedRegistrants != null) {
                    this.mIccidChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3006:
                unsljLog(response);
                if (this.mNetRejectRegistrant != null) {
                    this.mNetRejectRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3010:
                riljLog("deal RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND");
                unsljLog(response);
                if (this.mRegPLMNSelInfoRegistrants != null) {
                    this.mRegPLMNSelInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3032:
                unsljLog(response);
                notifyVpStatus((byte[]) ret);
                break;
            case 3037:
                unsljLog(response);
                if (SHOW_4G_PLUS_ICON) {
                    this.mCaStateChangedRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3041:
                unsljLog(response);
                riljLog("RIL_UNSOL_HW_IMSA_VOWIFI_MSG " + IccUtils.bytesToHexString((byte[]) ret));
                if (this.mCommonImsaToMapconInfoRegistrant != null) {
                    this.mCommonImsaToMapconInfoRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3047:
                unsljLog(response);
                if (this.mHwCrrConnIndRegistrants != null) {
                    this.mHwCrrConnIndRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                }
                this.crrConnRet = ret;
                break;
            default:
        }
    }

    private void unsljLog(int response) {
        riljLog("[UNSL]< " + unsolResponseToString(response));
    }

    private String unsolResponseToString(int request) {
        switch (request) {
            case 1520:
                return "UNSOL_HW_SIM_HOTPLUG";
            case 1521:
                return "UNSOL_HW_SIM_ICCID_CHANGED";
            case 3006:
                return "RIL_UNSOL_HW_NETWORK_REJECT_CASE";
            case 3010:
                return "RIL_UNSOL_HW_PLMN_SEARCH_INFO_IND";
            case 3032:
                return "UNSOL_HOOK_HW_VP_STATUS";
            case 3037:
                return "UNSOL_HW_CA_STATE_CHANGED";
            case 3047:
                return "UNSOL_HW_CRR_CONN_IND";
            default:
                return "<unknown response>:" + request;
        }
    }

    static String requestToString(int request) {
        Rlog.d(RILJ_LOG_TAG, "Enter HwHisiRIL requestToString,");
        switch (request) {
            case 528:
                return "RIL_REQUEST_HW_QUERY_CARDTYPE";
            case 2006:
                return "SIM_TRANSMIT_BASIC";
            case 2007:
                return "SIM_OPEN_CHANNEL";
            case 2008:
                return "SIM_CLOSE_CHANNEL";
            case 2009:
                return "SIM_TRANSMIT_CHANNEL";
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
            default:
                return "<unknown request>";
        }
    }

    public void queryCardType(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(528, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getBalongSim(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2029, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setActiveModemMode(int mode, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2088, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void switchBalongSim(int modem1ToSlot, int modem2ToSlot, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2028, result);
        rr.getParcel().writeInt(2);
        rr.getParcel().writeInt(modem1ToSlot);
        rr.getParcel().writeInt(modem2ToSlot);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + ", modem1ToSlot: " + modem1ToSlot + " modem2ToSlot: " + modem2ToSlot + "currentSimSlot: " + this.mBalongSimSlot);
        send(rr);
    }

    public void switchBalongSim(int modem1ToSlot, int modem2ToSlot, int modem3ToSlot, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2028, result);
        rr.getParcel().writeInt(3);
        rr.getParcel().writeInt(modem1ToSlot);
        rr.getParcel().writeInt(modem2ToSlot);
        rr.getParcel().writeInt(modem3ToSlot);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + ", modem1ToSlot: " + modem1ToSlot + " modem2ToSlot: " + modem2ToSlot + " modem3ToSlot: " + modem3ToSlot);
        send(rr);
    }

    public Object responseSimSlot(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        this.mBalongSimSlot = response[0];
        return response;
    }

    public void iccGetATR(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2032, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void iccExchangeAPDU(int cla, int command, int channel, int p1, int p2, int p3, String data, Message result) {
        RILRequestReference rr;
        if (channel == 0) {
            rr = RILRequestReference.obtain(2006, result);
        } else {
            rr = RILRequestReference.obtain(2009, result);
        }
        rr.getParcel().writeInt(cla);
        rr.getParcel().writeInt(command);
        rr.getParcel().writeInt(channel);
        rr.getParcel().writeString(null);
        rr.getParcel().writeInt(p1);
        rr.getParcel().writeInt(p2);
        rr.getParcel().writeInt(p3);
        rr.getParcel().writeString(data);
        rr.getParcel().writeString(null);
        riljLog(rr.serialString() + "> iccExchangeAPDU: " + requestToString(rr.getRequest()) + " 0x" + Integer.toHexString(cla) + " 0x" + Integer.toHexString(command) + " 0x" + Integer.toHexString(channel) + " " + p1 + "," + p2 + "," + p3);
        send(rr);
    }

    public void iccOpenChannel(String AID, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2007, result);
        rr.getParcel().writeString(AID);
        riljLog(rr.serialString() + "> iccOpenChannel: " + requestToString(rr.getRequest()) + " " + AID);
        send(rr);
    }

    public void iccCloseChannel(int channel, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2008, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(channel);
        riljLog(rr.serialString() + "> iccCloseChannel: " + requestToString(rr.getRequest()) + " " + channel);
        send(rr);
    }

    private Object responseICC_IO(Parcel p) {
        int sw1 = p.readInt();
        int sw2 = p.readInt();
        String s = p.readString();
        Rlog.d(RILJ_LOG_TAG, "< iccIO:  0x" + Integer.toHexString(sw1) + " 0x" + Integer.toHexString(sw2) + " " + s);
        return new IccIoResult(sw1, sw2, s);
    }

    public void getICCID(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2075, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    private Object responseICCID(Parcel p) {
        return hexStringToBcd(p.readString());
    }

    public static byte[] hexStringToBcd(String s) {
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

    static int hexCharToInt(char c) {
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

    public void setLTEReleaseVersion(boolean state, Message result) {
        int i = 1;
        RILRequestReference rr = RILRequestReference.obtain(2108, result);
        rr.getParcel().writeInt(1);
        Parcel parcel = rr.getParcel();
        if (!state) {
            i = 0;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + (state ? " CA function on" : " CA function off"));
        send(rr);
    }

    public void getLteReleaseVersion(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2109, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    private Object responseString(Parcel p) {
        return p.readString();
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        return response;
    }

    private Object responseRaw(Parcel p) {
        return p.createByteArray();
    }

    protected void notifyVpStatus(byte[] data) {
        int len = data.length;
        Rlog.d(RILJ_LOG_TAG, "notifyVpStatus: len = " + len);
        if (1 == len) {
            this.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult(null, data, null));
        }
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mRilInstanceId != null ? " [SUB" + this.mRilInstanceId + "]" : ""));
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2019, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(state);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getLocationInfo(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(534, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void queryServiceCellBand(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2129, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    private Object responseStrings(Parcel p) {
        int numStrings = p.readInt();
        String[] response = new String[numStrings];
        for (int i = 0; i < numStrings; i++) {
            response[i] = p.readString();
        }
        return response;
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
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[0]=" + this.mRilSocketMaps[0] + " mRilSocketMaps[1]=" + this.mRilSocketMaps[1] + " mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
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
            this.mRilSocketMapEnable = RILJ_LOGV;
        } else {
            this.mRilSocketMapEnable = false;
        }
        if (this.mSocket == null) {
            notifyPendingRilSocket();
            riljLog("[2Cards]hotSwitchSimSlotFor2Modem notify mPendingRilSocketLock!");
            this.mResultMessage = result;
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
            this.mRilSocketMapEnable = RILJ_LOGV;
            riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMapEnable true!");
        } else {
            this.mRilSocketMapEnable = false;
            riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMapEnable false!");
        }
        return RILJ_LOGV;
    }

    public void getSimHotPlugState(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(533, result);
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

    public String getHwPrlVersion() {
        return SystemProperties.get("persist.radio.hwprlversion", "0");
    }

    public String getHwUimid() {
        return SystemProperties.get("persist.radio.hwuimid", "0");
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
        Rlog.d(RILJ_LOG_TAG, rr.serialString() + "> " + requestToString(rr.getRequest()) + " power: " + power);
        send(rr);
    }

    public void setISMCOEX(String ISMCoexContent, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2068, result);
        riljLog("ISMCoexContent: " + ISMCoexContent);
        rr.getParcel().writeString(ISMCoexContent);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
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

    public void getRegPlmn(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2042, response);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getModemSupportVSimVersion(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2131, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void setImsDomainConfig(int selectDomain, Message result) {
        riljLog("setImsDomainConfig: " + selectDomain);
        RILRequestReference rr = RILRequestReference.obtain(2124, result);
        rr.getParcel().writeInt(1);
        rr.getParcel().writeInt(selectDomain);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void getImsDomain(Message result) {
        riljLog("getImsDomain");
        RILRequestReference rr = RILRequestReference.obtain(2126, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void handleUiccAuth(int auth_type, byte[] rand, byte[] auth, Message result) {
        riljLog("handleUiccAuth");
        RILRequestReference rr = RILRequestReference.obtain(2128, result);
        rr.getParcel().writeInt(auth_type);
        rr.getParcel().writeByteArray(rand);
        rr.getParcel().writeByteArray(auth);
        send(rr);
    }

    public void handleMapconImsaReq(byte[] Msg, Message result) {
        riljLog("handleMapconImsaReq:");
        RILRequestReference rr = RILRequestReference.obtain(2125, result);
        rr.getParcel().writeByteArray(Msg);
        send(rr);
    }

    private Object responseUiccAuth(Parcel in) {
        UiccAuthResponse uiccResponse = new UiccAuthResponse();
        uiccResponse.mResult = in.readInt();
        if (uiccResponse.mResult == 0) {
            uiccResponse.mUiccAuthChallenge.mResData = new UiccAuthResponseData();
            uiccResponse.mUiccAuthChallenge.mResData.present = in.readInt();
            uiccResponse.mUiccAuthChallenge.mResData.data = in.createByteArray();
            uiccResponse.mUiccAuthChallenge.mResData.len = uiccResponse.mUiccAuthChallenge.mResData.data.length;
            uiccResponse.mUiccAuthChallenge.mIkData = new UiccAuthResponseData();
            uiccResponse.mUiccAuthChallenge.mIkData.present = in.readInt();
            uiccResponse.mUiccAuthChallenge.mIkData.data = in.createByteArray();
            uiccResponse.mUiccAuthChallenge.mIkData.len = uiccResponse.mUiccAuthChallenge.mIkData.data.length;
            uiccResponse.mUiccAuthChallenge.mCkData = new UiccAuthResponseData();
            uiccResponse.mUiccAuthChallenge.mCkData.present = in.readInt();
            uiccResponse.mUiccAuthChallenge.mCkData.data = in.createByteArray();
            uiccResponse.mUiccAuthChallenge.mCkData.len = uiccResponse.mUiccAuthChallenge.mCkData.data.length;
        } else {
            uiccResponse.mUiccAuthSyncFail.present = in.readInt();
            uiccResponse.mUiccAuthSyncFail.data = in.createByteArray();
            uiccResponse.mUiccAuthSyncFail.len = uiccResponse.mUiccAuthSyncFail.data.length;
        }
        if (uiccResponse.mResult == 0) {
            riljLog("responseUiccAuth,res len:" + uiccResponse.mUiccAuthChallenge.mResData.len + " ik len:" + uiccResponse.mUiccAuthChallenge.mIkData.len + " ck len:" + uiccResponse.mUiccAuthChallenge.mCkData.len);
        } else {
            riljLog("responseUiccAuth,mUiccAuthSyncFail len:" + uiccResponse.mUiccAuthSyncFail.len);
        }
        return uiccResponse;
    }

    public void setTime(String date, String time, String timezone, Message result) {
        RILRequestReference rr;
        if (!(date == null || time == null)) {
            if (timezone == null) {
            }
            rr = RILRequestReference.obtain(2130, result);
            rr.getParcel().writeInt(3);
            rr.getParcel().writeString(date);
            rr.getParcel().writeString(time);
            rr.getParcel().writeString(timezone);
            riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + ": " + date + " " + time + " " + timezone);
            send(rr);
        }
        Rlog.e(RILJ_LOG_TAG, "setTime check");
        rr = RILRequestReference.obtain(2130, result);
        rr.getParcel().writeInt(3);
        rr.getParcel().writeString(date);
        rr.getParcel().writeString(time);
        rr.getParcel().writeString(timezone);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()) + ": " + date + " " + time + " " + timezone);
        send(rr);
    }

    private void registerIntentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
    }

    public void registerForCrrConn(Handler h, int what, Object obj) {
        this.mHwCrrConnIndRegistrants.add(new Registrant(h, what, obj));
        riljLog("registerForCrrConn: " + h);
        if (this.crrConnRet != null) {
            this.mHwCrrConnIndRegistrants.notifyRegistrants(new AsyncResult(null, this.crrConnRet, null));
        }
    }

    public void unregisterForCrrConn(Handler h) {
        this.mHwCrrConnIndRegistrants.remove(h);
        riljLog("unregisterForCrrConn: " + h);
    }

    public void setOnRegPLMNSelInfo(Handler h, int what, Object obj) {
        this.mRegPLMNSelInfoRegistrants.add(new Registrant(h, what, obj));
        riljLog("setOnRegPLMNSelInfo: " + h);
    }

    public void unSetOnRegPLMNSelInfo(Handler h) {
        this.mRegPLMNSelInfoRegistrants.remove(h);
        riljLog("unSetOnRegPLMNSelInfo: " + h);
    }

    public void getLteFreqWithWlanCoex(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2087, result);
        riljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }
}
