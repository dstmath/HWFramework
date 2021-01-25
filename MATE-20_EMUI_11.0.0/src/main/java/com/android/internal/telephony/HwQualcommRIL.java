package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import com.android.ims.HwImsManagerInner;
import com.android.ims.ImsManager;
import com.huawei.android.telephony.RlogEx;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import vendor.huawei.hardware.qcomradio.V1_0.IQcomRadio;

public class HwQualcommRIL extends RIL {
    private static final int BUF_NOT_ENOUGH = -3;
    private static final int BYTE_SIZE = 1;
    private static final byte ERROR = -1;
    private static final int FILE_NOT_EXIST = -2;
    private static final int INT_SIZE = 4;
    private static final String ITEM_FILE_CA_DISABLE = "/nv/item_files/modem/lte/common/ca_disable";
    private static final int LOCK = 1;
    private static final int OEMHOOK_BASE = 524288;
    private static final int OEMHOOK_EVT_HOOK_CSG_PERFORM_NW_SCAN = 524438;
    private static final int OEMHOOK_EVT_HOOK_CSG_SET_SYS_SEL_PREF = 524439;
    private static final int OEM_HOOK_RAW_REQUEST_HEADERSIZE = ("QOEMHOOK".length() + 8);
    private static final int OPEN = 1;
    private static final int PARATYPE_BASIC_COMM = 1;
    private static final int PARATYPE_CELLULAR_CLOUD = 2;
    private static final int PARA_PATHTYPE_COTA = 1;
    static final String[] QCOM_HIDL_SERVICE_NAME = {"slot1", "slot2", "slot3"};
    public static final int QCRIL_EVT_HOOK_REGISTER_HUAWEI_GENERIC = 598045;
    public static final int QCRIL_EVT_HOOK_UNREGISTER_HUAWEI_GENERIC = 598046;
    private static final int READ_EFS_FAIL = -1;
    private static final String RILJ_LOG_TAG = "RILJ-HwQualcommRIL";
    public static final int RIL_HW_EVT_HOOK_BASE = 598016;
    private static final int TYPEMASK_PARATYPE_BASIC_COMM = 0;
    private static final int TYPEMASK_PARATYPE_CELLULAR_CLOUD = 1;
    private static final int UNLOCK = 0;
    private static final int VALUE_SIZE = 1;
    private static final int WRITE_FAIL = 0;
    private static final int WRITE_SUCCESS = 1;
    private static Object qcRilHook = null;
    protected Context mQcomContext;
    HwQualcommRadioIndication mQcomRadioIndication;
    volatile IQcomRadio mQcomRadioProxy;
    HwQualcommRadioResponse mQcomRadioResponse;
    boolean mQcomRilJIntiDone;
    private Integer mRilInstanceId;

    public interface QcomRILCommand {
        void excute(IQcomRadio iQcomRadio, int i) throws RemoteException, RuntimeException;
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mQcomRadioProxy = null;
        this.mQcomRilJIntiDone = false;
        this.mRilInstanceId = null;
        this.mRilInstanceId = instanceId;
        getQcRilHook(context);
        this.mQcomContext = context;
        this.mQcomRadioIndication = new HwQualcommRadioIndication(this);
        this.mQcomRadioResponse = new HwQualcommRadioResponse(this);
        this.mQcomRilJIntiDone = true;
        getQcomRadioProxy(null);
    }

    public static Object getQcRilHook() {
        return qcRilHook;
    }

    public static void setQcRilHook(Object qcRilHook2) {
        qcRilHook = qcRilHook2;
    }

    private synchronized void getQcRilHook(Context context) {
        qcomRiljLog("Enter HwQualcommRIL getQcRilHook");
        if (qcRilHook == null) {
            try {
                setQcRilHook(new PathClassLoader("system/framework/qcrilhook.jar", null, ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qcrilhook.QcRilHook").getConstructor(Context.class).newInstance(context));
            } catch (ClassNotFoundException e) {
                qcomRiljLoge("occur ClassNotFoundException!");
            } catch (RuntimeException e2) {
                qcomRiljLoge("occur RuntimeException!");
            } catch (NoSuchMethodException e3) {
                qcomRiljLoge("occur NoSuchMethodException!");
            } catch (IllegalAccessException e4) {
                qcomRiljLoge("occur IllegalAccessException!");
            } catch (InvocationTargetException e5) {
                qcomRiljLoge("occur InvocationTargetException!");
            } catch (Exception e6) {
                qcomRiljLoge("occur Exception!");
            }
        }
    }

    public void resetQcomRadioProxy() {
        getQcomRadioProxy(null);
    }

    public void clearQcomRadioProxy() {
        this.mQcomRadioProxy = null;
    }

    public IQcomRadio getQcomRadioProxy(Message result) {
        if (!this.mQcomRilJIntiDone) {
            return null;
        }
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mQcomRadioProxy != null) {
            return this.mQcomRadioProxy;
        } else {
            try {
                this.mQcomRadioProxy = IQcomRadio.getService(QCOM_HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()], false);
                if (this.mQcomRadioProxy != null) {
                    this.mQcomRadioProxy.linkToDeath(this.mRadioProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                    this.mQcomRadioProxy.setResponseFunctionsHuawei(this.mQcomRadioResponse, this.mQcomRadioIndication);
                } else {
                    qcomRiljLoge("getQcomRadioProxy: mRadioProxy == null");
                }
            } catch (RemoteException | RuntimeException e) {
                this.mQcomRadioProxy = null;
                qcomRiljLoge("getQcomRadioProxy:Exception occurs when getService/setResponseFunctionsHuawei");
            }
            if (this.mQcomRadioProxy == null && result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return this.mQcomRadioProxy;
        }
    }

    private void invokeIRadio(int requestId, Message result, QcomRILCommand cmd) {
        IQcomRadio radioProxy = getQcomRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(requestId, result, getmRILDefaultWorkSourceHw());
            addRequestEx(rr);
            qcomRiljLog(rr.serialString() + "> " + requestToString(requestId));
            try {
                cmd.excute(radioProxy, rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(requestId), e, rr);
            }
        }
    }

    public void qcomRiljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        RlogEx.i(RILJ_LOG_TAG, sb.toString());
    }

    public void qcomRiljLoge(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        RlogEx.e(RILJ_LOG_TAG, sb.toString());
    }

    public String getNVESN() {
        Object obj;
        String result = null;
        qcomRiljLog("getNVESN() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetESN", null, null)) == null)) {
            result = (String) obj;
        }
        qcomRiljLog("getNVESN() exit ");
        return result;
    }

    public void queryServiceCellBand(Message result) {
        qcomRiljLog("sending response as NULL");
        sendResponseToTarget(result, 2);
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        qcomRiljLog("openSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        boolean result = false;
        try {
            if (qcRilHook != null) {
                result = ((Boolean) qcRilHook.getClass().getMethod("qcRilSetSarConfigStatus", Integer.TYPE, Integer.TYPE).invoke(qcRilHook, Integer.valueOf((int) QCRIL_EVT_HOOK_REGISTER_HUAWEI_GENERIC), Integer.valueOf(mask))).booleanValue();
            }
        } catch (NoSuchMethodException e) {
            qcomRiljLoge("occur NoSuchMethodException!");
        } catch (RuntimeException e2) {
            qcomRiljLoge("occur RuntimeException!");
        } catch (IllegalAccessException e3) {
            qcomRiljLoge("occur IllegalAccessException!");
        } catch (InvocationTargetException e4) {
            qcomRiljLoge("occur InvocationTargetException!");
        } catch (Exception e5) {
            qcomRiljLoge("occur Exception!");
        }
        qcomRiljLog("openSwitchOfUploadAntOrMaxTxPower, result = " + result);
        return result;
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        qcomRiljLog("closeSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        boolean result = false;
        try {
            if (qcRilHook != null) {
                result = ((Boolean) qcRilHook.getClass().getMethod("qcRilSetSarConfigStatus", Integer.TYPE, Integer.TYPE).invoke(qcRilHook, Integer.valueOf((int) QCRIL_EVT_HOOK_UNREGISTER_HUAWEI_GENERIC), Integer.valueOf(mask))).booleanValue();
            }
        } catch (NoSuchMethodException e) {
            qcomRiljLoge("occur NoSuchMethodException!");
        } catch (RuntimeException e2) {
            qcomRiljLoge("occur RuntimeException!");
        } catch (IllegalAccessException e3) {
            qcomRiljLoge("occur IllegalAccessException!");
        } catch (InvocationTargetException e4) {
            qcomRiljLoge("occur InvocationTargetException!");
        } catch (Exception e5) {
            qcomRiljLoge("occur Exception!");
        }
        qcomRiljLog("closeSwitchOfUploadAntOrMaxTxPower, result = " + result);
        return result;
    }

    private String unsolResponseToString(int request) {
        if (request == 3032) {
            return "UNSOL_HOOK_HW_VP_STATUS";
        }
        return "<unknown response>=" + request;
    }

    public void queryCardType(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(528, result, getmRILDefaultWorkSourceHw());
        qcomRiljLog(rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void resetProfile(Message response) {
        qcomRiljLog("resetProfile");
        sendOemRilRequestRaw(524588, 0, null, response);
    }

    public void sendCloudMessageToModem(int eventId) {
        Object obj;
        qcomRiljLog("sendCloudMessageToModem() entry: event_id = " + eventId);
        boolean result = false;
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilSendCloudMessageToModem", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(eventId)})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("sendCloudMessageToModem() exit : result = " + result);
    }

    public void registerForModemCapEvent(Handler handler, int what, Object obj) {
        this.mModemCapRegistrants.addUnique(handler, what, obj);
    }

    public void unregisterForModemCapEvent(Handler handler) {
        this.mModemCapRegistrants.remove(handler);
    }

    public void getSignalStrength(Message result) {
        if (getRILReference() != null) {
            getRILReference().getSignalStrength(result);
        }
    }

    public void getModemCapability(Message response) {
        qcomRiljLog("GetModemCapability");
        sendOemRilRequestRaw(524323, 0, null, response);
    }

    public void updateStackBinding(int stack, int enable, Message response) {
        qcomRiljLog("UpdateStackBinding: on Stack: " + stack + ", enable/disable: " + enable);
        sendOemRilRequestRaw(524324, 2, new byte[]{(byte) stack, (byte) enable}, response);
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
        qcomRiljLog("switchVoiceCallBackgroundState: state is " + state);
        sendOemRilRequestRaw(524301, 1, new byte[]{(byte) (state & 127)}, null);
    }

    private void sendOemRilRequestRaw(int requestId, int numPayload, byte[] payload, Message response) {
        byte[] request = new byte[(this.mHeaderSize + (numPayload * 1))];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        for (char c : "QOEMHOOK".toCharArray()) {
            buf.put((byte) c);
        }
        buf.putInt(requestId);
        if (numPayload > 0 && payload != null) {
            buf.putInt(numPayload * 1);
            for (byte b : payload) {
                buf.put(b);
            }
        }
        invokeOemRilRequestRaw(request, response);
    }

    public String getHwPrlVersion() {
        Object obj;
        String prl = "0";
        qcomRiljLog("getHwPrlVersion() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetCdmaPrlVersion", null, null)) == null)) {
            prl = (String) obj;
        }
        qcomRiljLog("getHwPrlVersion() exit : prl = " + prl);
        return prl;
    }

    public String getHwUimid() {
        String esn = getNVESN();
        return esn == null ? "0" : esn;
    }

    public void closeRrc() {
        closeRrc("");
    }

    public String getHwCDMAMsplVersion() {
        Object obj;
        String msplVersion = "0";
        qcomRiljLog("getHwCDMAMsplVersion() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetCdmaMsplVersion", null, null)) == null)) {
            msplVersion = (String) obj;
        }
        qcomRiljLog("getHwCDMAMsplVersion() exit : MsplVersion = " + msplVersion);
        return msplVersion;
    }

    public String getHwCDMAMlplVersion() {
        Object obj;
        String mlplVersion = "0";
        qcomRiljLog("getHwCDMAMsplVersion() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetCdmaMlplVersion", null, null)) == null)) {
            mlplVersion = (String) obj;
        }
        qcomRiljLog("getHwCDMAMsplVersion() exit : MlplVersion = " + mlplVersion);
        return mlplVersion;
    }

    public void closeRrc(String interfaceName) {
        Object obj;
        if (this.mPhoneType == 1) {
            if (interfaceName == null) {
                qcomRiljLog("interfaceName is null");
                return;
            }
            qcomRiljLog("request interface " + interfaceName + " go dormant for GSMphone");
            try {
                sendOemRilRequestRaw(524291, interfaceName.length(), interfaceName.getBytes(StandardCharsets.UTF_8), null);
            } catch (ReadOnlyBufferException e) {
                qcomRiljLog("Rilj, closeRrc ReadOnlyBufferException.");
            } catch (Exception e2) {
                qcomRiljLog("Rilj, closeRrc");
            }
        } else if (this.mPhoneType == 2) {
            qcomRiljLog("request all interfaces go dormant for CDMAphone");
            boolean result = false;
            if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilCloseRrc", null, null)) == null)) {
                result = ((Boolean) obj).booleanValue();
            }
            qcomRiljLog("closeRrc, result = " + result);
        } else {
            qcomRiljLog("not CDMA or GSM phone, not support fast dormancy");
        }
    }

    public boolean setEhrpdByQMI(boolean enable) {
        Object obj;
        boolean result = false;
        qcomRiljLog("setEhrpdByQMI() entry:enable =" + enable);
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilSetEhrpdSwitch", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(enable)})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("setEhrpdByQMI() exit : result = " + result);
        return result;
    }

    public void getImsDomain(Message result) {
        qcomRiljLog("getImsDomain");
        AsyncResult.forMessage(result, (Object) null, (Throwable) null);
        result.sendToTarget();
    }

    public boolean getImsSwitch() {
        try {
            return HwImsManagerInner.isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mRilInstanceId.intValue());
        } catch (NullPointerException e) {
            RlogEx.e(RILJ_LOG_TAG, "getImsSwitch error");
            return false;
        } catch (Exception e2) {
            RlogEx.e(RILJ_LOG_TAG, "getImsSwitch exception occurs");
            return false;
        }
    }

    public void setImsSwitch(boolean on) {
        try {
            ImsManager.getInstance(this.mContext, this.mRilInstanceId.intValue());
            ImsManager.setEnhanced4gLteModeSetting(this.mContext, on);
            Settings.System.putInt(this.mContext.getContentResolver(), "hw_volte_user_switch", on ? 1 : 0);
        } catch (NullPointerException e) {
            RlogEx.e(RILJ_LOG_TAG, "etImsSwitch on error ");
        } catch (Exception e2) {
            RlogEx.e(RILJ_LOG_TAG, "setImsSwitch exception occurs");
        }
    }

    public void setLTEReleaseVersion(int state, Message response) {
        int ret = 0;
        boolean isDisableCa = state <= 0;
        Throwable tr = null;
        qcomRiljLog("setLTEReleaseVersion() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetLteCaStatus", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(isDisableCa)});
            if (obj != null) {
                ret = Integer.valueOf(((Boolean) obj).booleanValue() ? 1 : 0);
            } else {
                tr = new RuntimeException("exception when set ca");
            }
        }
        if (response != null) {
            AsyncResult.forMessage(response, ret, tr);
            response.sendToTarget();
        }
        qcomRiljLog("setLTEReleaseVersion() exit");
    }

    public void getLteReleaseVersion(Message response) {
        boolean isCaDisable = false;
        Throwable tr = null;
        int[] result = {-1};
        qcomRiljLog("getLteReleaseVersion() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilIsLteCaDisable", null, null);
            if (obj != null) {
                isCaDisable = ((Boolean) obj).booleanValue();
            } else {
                tr = new RuntimeException("exception when set ca");
            }
        }
        if (response != null) {
            if (tr == null) {
                if (isCaDisable) {
                    result[0] = 0;
                } else {
                    result[0] = 1;
                }
            }
            AsyncResult.forMessage(response, result, tr);
            response.sendToTarget();
        }
        qcomRiljLog("getLteReleaseVersion() exit");
    }

    public void setPowerGrade(int powerGrade, Message response) {
        qcomRiljLog("setPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(524489, 1, new byte[]{(byte) (powerGrade & 127)}, response);
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        qcomRiljLog("setWifiTXPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(598042, 1, new byte[]{(byte) (powerGrade & 127)}, response);
    }

    public void openSwitchOfUploadBandClass(Message result) {
        qcomRiljLog("Rilj, openSwitchOfUploadBandClass");
        sendOemRilRequestRaw(598041, 1, new byte[]{1}, result);
    }

    public void closeSwitchOfUploadBandClass(Message result) {
        qcomRiljLog("Rilj, closeSwitchOfUploadBandClass");
        sendOemRilRequestRaw(598041, 1, new byte[]{0}, result);
    }

    public void getAvailableCSGNetworks(byte[] data, Message response) {
        sendOemRilRequestRawBytes(OEMHOOK_EVT_HOOK_CSG_PERFORM_NW_SCAN, data, response);
    }

    public void setCSGNetworkSelectionModeManual(byte[] data, Message response) {
        sendOemRilRequestRawBytes(OEMHOOK_EVT_HOOK_CSG_SET_SYS_SEL_PREF, data, response);
    }

    private void sendOemRilRequestRawBytes(int requestId, byte[] payload, Message response) {
        byte[] request = new byte[(this.mHeaderSize + payload.length)];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        for (char c : "QOEMHOOK".toCharArray()) {
            buf.put((byte) c);
        }
        buf.putInt(requestId);
        buf.putInt(payload.length);
        buf.put(payload);
        invokeOemRilRequestRaw(request, response);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        RlogEx.i(RILJ_LOG_TAG, "cmd for get/set nv, event is : " + event);
        boolean z = false;
        switch (event) {
            case 2:
                if (action == 1) {
                    z = true;
                }
                return setEcTestControl(z);
            case 3:
                if (getNVInterface(action, buf) == 0) {
                    return true;
                }
                return false;
            case INT_SIZE /* 4 */:
                return setKmcPubKey(buf);
            case 5:
                return getKmcPubKey(buf);
            case 6:
                return getRandomInterface(buf);
            case 7:
                if (action == 0) {
                    if (getEcCdmaCallInfo() == 0) {
                        return true;
                    }
                    return false;
                } else if (action == 1) {
                    return setEcCdmaCallInfo(action);
                } else {
                    return false;
                }
            case 8:
                if (action == 1) {
                    z = true;
                }
                return setEcCdmaCallVersion(z);
            default:
                RlogEx.w(RILJ_LOG_TAG, "error event, return false");
                return false;
        }
    }

    public byte getNVInterface(int nvItem, byte[] buf) {
        Object obj;
        byte result = ERROR;
        qcomRiljLog("getNVInterface() entry:nvItem =" + nvItem);
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilReadNvByID", new Class[]{Integer.TYPE, byte[].class}, new Object[]{Integer.valueOf(nvItem), buf})) == null)) {
            result = ((Byte) obj).byteValue();
        }
        qcomRiljLog("getNVInterface() exit : result = " + ((int) result));
        return result;
    }

    public boolean setEcTestControl(boolean ControlFlag) {
        Object obj;
        boolean result = false;
        qcomRiljLog("setEcTestControl() entry:ControlFlag =" + ControlFlag);
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilSetEcTestControl", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(ControlFlag)})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("setEcTestControl() exit : result = " + result);
        return result;
    }

    public boolean setKmcPubKey(byte[] buf) {
        Object obj;
        boolean result = false;
        qcomRiljLog("setKmcPubKey() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilSetEcKmcPubKey", new Class[]{byte[].class}, new Object[]{buf})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("setKmcPubKey() exit : result = " + result);
        return result;
    }

    public boolean getKmcPubKey(byte[] buf) {
        Object obj;
        boolean result = false;
        qcomRiljLog("getKmcPubKey() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetEcKmcPubKey", new Class[]{byte[].class}, new Object[]{buf})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("getKmcPubKey() exit : result = " + result);
        return result;
    }

    public boolean getRandomInterface(byte[] buf) {
        Object obj;
        boolean result = false;
        qcomRiljLog("getRandomInterface() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetEcRandomData", new Class[]{byte[].class}, new Object[]{buf})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("getRandomInterface() exit : result = " + result);
        return result;
    }

    public boolean getEcCdmaCallVersion() {
        Object obj;
        boolean result = false;
        qcomRiljLog("getEcCdmaCallVersion() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetEcCdmaCallVersion", null, null)) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("getEcCdmaCallVersion() exit : result = " + result);
        return result;
    }

    public boolean setEcCdmaCallVersion(boolean version) {
        Object obj;
        boolean result = false;
        qcomRiljLog("setEcCdmaCallVersion() entry:version =" + version);
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilSetEcCdmaCallVersion", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(version)})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("setEcCdmaCallVersion() exit : result = " + result);
        return result;
    }

    public int getEcCdmaCallInfo() {
        Object obj;
        int result = 0;
        qcomRiljLog("getEcCdmaCallInfo() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetEcCdmaCallInfo", null, null)) == null)) {
            result = ((Integer) obj).intValue();
        }
        qcomRiljLog("getEcCdmaCallInfo() exit : result = " + result);
        return 0;
    }

    public boolean setEcCdmaCallInfo(int info) {
        Object obj;
        boolean result = false;
        qcomRiljLog("setEcCdmaCallInfo() entry:version =" + info);
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilSetEcCdmaCallInfo", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(info)})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("setEcCdmaCallInfo() exit : result = " + result);
        return result;
    }

    public boolean getMipActiveProfile(byte[] buf) {
        Object obj;
        boolean result = false;
        qcomRiljLog("getMipActiveProfile() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetMipActiveProfile", new Class[]{byte[].class}, new Object[]{buf})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("getMipActiveProfile() exit : result = " + result);
        return result;
    }

    public boolean getMipGenUserProf(byte[] buf) {
        Object obj;
        boolean result = false;
        qcomRiljLog("getMipGenUserProf() entry");
        if (!(qcRilHook == null || (obj = invokeQcRilHookMethod("qcRilGetMipGenUserProf", new Class[]{byte[].class}, new Object[]{buf})) == null)) {
            result = ((Boolean) obj).booleanValue();
        }
        qcomRiljLog("getMipGenUserProf() exit : result = " + result);
        return result;
    }

    private Object invokeQcRilHookMethod(String methodName, Class[] cArgType, Object[] inParamList) {
        try {
            if (qcRilHook != null) {
                return qcRilHook.getClass().getMethod(methodName, cArgType).invoke(qcRilHook, inParamList);
            }
            qcomRiljLoge("qchook does not init!");
            return null;
        } catch (NoSuchMethodException e) {
            qcomRiljLoge("occur NoSuchMethodException!");
            return null;
        } catch (RuntimeException e2) {
            qcomRiljLoge("occur RuntimeException!");
            return null;
        } catch (IllegalAccessException e3) {
            qcomRiljLoge("occur IllegalAccessException!");
            return null;
        } catch (InvocationTargetException e4) {
            qcomRiljLoge("occur InvocationTargetException!");
            return null;
        } catch (Exception e5) {
            qcomRiljLoge("occur Exception!");
            return null;
        }
    }

    public void setDeepNoDisturbState(int state, Message response) {
        int ret = 0;
        Throwable tr = null;
        qcomRiljLog("setDeepNoDisturbState() entry:state =" + state);
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetDeepNoDisturbStatus", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(state)});
            if (obj != null) {
                ret = Integer.valueOf(((Boolean) obj).booleanValue() ? 1 : 0);
            } else {
                tr = new RuntimeException("exception when setDeepNoDisturbState");
            }
        }
        if (response != null) {
            AsyncResult.forMessage(response, ret, tr);
            response.sendToTarget();
        }
        qcomRiljLog("setDeepNoDisturbState() exit");
    }

    public void notifyCellularCommParaReady(final int paratype, final int pathtype, Message result) {
        qcomRiljLog("notifyCellularCommParaReady: paratype = " + paratype + ", pathtype = " + pathtype);
        if (paratype == 2) {
            invokeIRadio(2133, result, new QcomRILCommand() {
                /* class com.android.internal.telephony.HwQualcommRIL.AnonymousClass1 */

                @Override // com.android.internal.telephony.HwQualcommRIL.QcomRILCommand
                public void excute(IQcomRadio radio, int serial) throws RemoteException, RuntimeException {
                    HwQualcommRIL.this.qcomRiljLog("RIL_REQUEST_HW_SET_CELLULAR_CLOUD_PARA_READY");
                    radio.notifyCellularCloudParaReady(serial, paratype, pathtype);
                }
            });
        }
    }
}
