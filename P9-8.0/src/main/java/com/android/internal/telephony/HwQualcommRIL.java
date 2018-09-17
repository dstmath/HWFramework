package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.telephony.Rlog;
import com.android.ims.HwImsManagerInner;
import com.android.ims.ImsManager;
import com.android.internal.telephony.AbstractRIL.RILCommand;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import vendor.huawei.hardware.radio.V1_0.IRadio;

public class HwQualcommRIL extends RIL {
    private static final int BUF_NOT_ENOUGH = -3;
    private static final int BYTE_SIZE = 1;
    private static final byte ERROR = (byte) -1;
    private static final int FILE_NOT_EXIST = -2;
    private static Class HWNV = null;
    private static final int INT_SIZE = 4;
    private static final String ITEM_FILE_CA_DISABLE = "/nv/item_files/modem/lte/common/ca_disable";
    private static final int LOCK = 1;
    private static final int OEMHOOK_BASE = 524288;
    private static final int OEMHOOK_EVT_HOOK_CSG_PERFORM_NW_SCAN = 524438;
    private static final int OEMHOOK_EVT_HOOK_CSG_SET_SYS_SEL_PREF = 524439;
    private static final int OEM_HOOK_RAW_REQUEST_HEADERSIZE = ("QOEMHOOK".length() + 8);
    private static final int OPEN = 1;
    public static final int QCRIL_EVT_HOOK_REGISTER_HUAWEI_GENERIC = 598045;
    public static final int QCRIL_EVT_HOOK_UNREGISTER_HUAWEI_GENERIC = 598046;
    private static final int READ_EFS_FAIL = -1;
    private static final String RILJ_LOG_TAG = "RILJ-HwQualcommRIL";
    public static final int RIL_HW_EVT_HOOK_BASE = 598016;
    private static final int UNLOCK = 0;
    private static final int VALUE_SIZE = 1;
    private static final int WRITE_FAIL = 0;
    private static final int WRITE_SUCCESS = 1;
    private static Object qcRilHook = null;
    private Integer mRilInstanceId = null;

    public static boolean checkExistinSys(String path) {
        if (new File(Environment.getRootDirectory().getPath() + "/framework", path).exists()) {
            return true;
        }
        return false;
    }

    public static synchronized Class getHWNV() {
        Class cls;
        synchronized (HwQualcommRIL.class) {
            Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL getClass");
            if (HWNV == null) {
                String realProvider = "com.huawei.android.hwnv.HWNVFuncation";
                String realProviderPath = "system/framework/hwnv.jar";
                String realProviderPath_vendor = "/system/vendor/framework/hwnv.jar";
                try {
                    PathClassLoader classLoader;
                    if (checkExistinSys("hwnv.jar")) {
                        classLoader = new PathClassLoader(realProviderPath, ClassLoader.getSystemClassLoader());
                    } else {
                        classLoader = new PathClassLoader(realProviderPath_vendor, ClassLoader.getSystemClassLoader());
                    }
                    HWNV = classLoader.loadClass(realProvider);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                }
            }
            cls = HWNV;
        }
        return cls;
    }

    private synchronized void getQcRilHook(Context context) {
        Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL getQcRilHook");
        if (qcRilHook == null) {
            try {
                Object[] params = new Object[]{context};
                setQcRilHook(new PathClassLoader("system/framework/qcrilhook.jar", null, ClassLoader.getSystemClassLoader()).loadClass("com.qualcomm.qcrilhook.QcRilHook").getConstructor(new Class[]{Context.class}).newInstance(params));
            } catch (ClassNotFoundException e) {
                riljLog("occur ClassNotFoundException!");
            } catch (RuntimeException e2) {
                riljLog("occur RuntimeException!");
            } catch (NoSuchMethodException e3) {
                riljLog("occur NoSuchMethodException!");
            } catch (IllegalAccessException e4) {
                riljLog("occur IllegalAccessException!");
            } catch (InvocationTargetException e5) {
                riljLog("occur InvocationTargetException!");
            } catch (Exception e6) {
                riljLog("occur Exception!");
            }
        }
        return;
    }

    public static Object getQcRilHook() {
        return qcRilHook;
    }

    public static void setQcRilHook(Object qcRilHook) {
        qcRilHook = qcRilHook;
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
        getQcRilHook(context);
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRilInstanceId = instanceId;
        getQcRilHook(context);
    }

    public String getNVESN() {
        String result = null;
        riljLog("getNVESN() entry");
        if (qcRilHook != null) {
            String obj = invokeQcRilHookMethod("qcRilGetESN", null, null);
            if (obj != null) {
                result = obj;
            }
        }
        riljLog("getNVESN() exit ");
        return result;
    }

    public void queryServiceCellBand(Message result) {
        Rlog.d(RILJ_LOG_TAG, "sending response as NULL");
        sendResponseToTarget(result, 2);
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        riljLog("openSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        boolean result = false;
        try {
            if (qcRilHook != null) {
                result = ((Boolean) qcRilHook.getClass().getMethod("qcRilSetSarConfigStatus", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(qcRilHook, new Object[]{Integer.valueOf(QCRIL_EVT_HOOK_REGISTER_HUAWEI_GENERIC), Integer.valueOf(mask)})).booleanValue();
            }
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
        } catch (Exception e5) {
            riljLog("occur Exception!");
        }
        riljLog("openSwitchOfUploadAntOrMaxTxPower, result = " + result);
        return result;
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        riljLog("closeSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        boolean result = false;
        try {
            if (qcRilHook != null) {
                result = ((Boolean) qcRilHook.getClass().getMethod("qcRilSetSarConfigStatus", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(qcRilHook, new Object[]{Integer.valueOf(QCRIL_EVT_HOOK_UNREGISTER_HUAWEI_GENERIC), Integer.valueOf(mask)})).booleanValue();
            }
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
        } catch (Exception e5) {
            riljLog("occur Exception!");
        }
        riljLog("closeSwitchOfUploadAntOrMaxTxPower, result = " + result);
        return result;
    }

    private String unsolResponseToString(int request) {
        switch (request) {
            case 3032:
                return "UNSOL_HOOK_HW_VP_STATUS";
            default:
                return "<unknown response>=" + request;
        }
    }

    static String requestToString(int request) {
        Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL requestToString,");
        switch (request) {
            case 528:
                return "RIL_REQUEST_HW_QUERY_CARDTYPE";
            default:
                return "<unknown request>";
        }
    }

    public void queryCardType(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(528, result, this.mRILDefaultWorkSource);
        Rlog.d(RILJ_LOG_TAG, rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void resetProfile(Message response) {
        Rlog.d(RILJ_LOG_TAG, "resetProfile");
        sendOemRilRequestRaw(524588, 0, null, response);
    }

    public void sendCloudMessageToModem(int event_id) {
        riljLog("sendCloudMessageToModem() entry: event_id = " + event_id);
        boolean result = false;
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSendCloudMessageToModem", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(event_id)});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("sendCloudMessageToModem() exit : result = " + result);
    }

    public void registerForModemCapEvent(Handler h, int what, Object obj) {
        this.mModemCapRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForModemCapEvent(Handler h) {
        this.mModemCapRegistrants.remove(h);
    }

    public void getModemCapability(Message response) {
        Rlog.d(RILJ_LOG_TAG, "GetModemCapability");
        sendOemRilRequestRaw(524323, 0, null, response);
    }

    public void updateStackBinding(int stack, int enable, Message response) {
        byte[] payload = new byte[]{(byte) stack, (byte) enable};
        Rlog.d(RILJ_LOG_TAG, "UpdateStackBinding: on Stack: " + stack + ", enable/disable: " + enable);
        sendOemRilRequestRaw(524324, 2, payload, response);
    }

    void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mRilInstanceId != null ? " [SUB" + this.mRilInstanceId + "]" : ""));
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
        byte[] payload = new byte[]{(byte) (state & 127)};
        Rlog.d(RILJ_LOG_TAG, "switchVoiceCallBackgroundState: state is " + state);
        sendOemRilRequestRaw(524301, 1, payload, null);
    }

    private void sendOemRilRequestRaw(int requestId, int numPayload, byte[] payload, Message response) {
        int length;
        int i = 0;
        byte[] request = new byte[(this.mHeaderSize + (numPayload * 1))];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        for (char c : "QOEMHOOK".toCharArray()) {
            buf.put((byte) c);
        }
        buf.putInt(requestId);
        if (numPayload > 0 && payload != null) {
            buf.putInt(numPayload * 1);
            length = payload.length;
            while (i < length) {
                buf.put(payload[i]);
                i++;
            }
        }
        invokeOemRilRequestRaw(request, response);
    }

    public String getHwPrlVersion() {
        String prl = "0";
        riljLog("getHwPrlVersion() entry");
        if (qcRilHook != null) {
            String obj = invokeQcRilHookMethod("qcRilGetCdmaPrlVersion", null, null);
            if (obj != null) {
                prl = obj;
            }
        }
        riljLog("getHwPrlVersion() exit : prl = " + prl);
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
        String MsplVersion = "0";
        riljLog("getHwCDMAMsplVersion() entry");
        if (qcRilHook != null) {
            String obj = invokeQcRilHookMethod("qcRilGetCdmaMsplVersion", null, null);
            if (obj != null) {
                MsplVersion = obj;
            }
        }
        riljLog("getHwCDMAMsplVersion() exit : MsplVersion = " + MsplVersion);
        return MsplVersion;
    }

    public String getHwCDMAMlplVersion() {
        String MlplVersion = "0";
        riljLog("getHwCDMAMsplVersion() entry");
        if (qcRilHook != null) {
            String obj = invokeQcRilHookMethod("qcRilGetCdmaMlplVersion", null, null);
            if (obj != null) {
                MlplVersion = obj;
            }
        }
        riljLog("getHwCDMAMsplVersion() exit : MlplVersion = " + MlplVersion);
        return MlplVersion;
    }

    public void closeRrc(String interfaceName) {
        if (1 == this.mPhoneType) {
            if (interfaceName == null) {
                Rlog.d(RILJ_LOG_TAG, "interfaceName is null");
                return;
            }
            Rlog.d(RILJ_LOG_TAG, "request interface " + interfaceName + " go dormant for GSMphone");
            try {
                sendOemRilRequestRaw(524291, interfaceName.length(), interfaceName.getBytes("UTF-8"), null);
            } catch (Exception e) {
                Rlog.d(RILJ_LOG_TAG, "Rilj, closeRrc");
            }
        } else if (2 == this.mPhoneType) {
            Rlog.d(RILJ_LOG_TAG, "request all interfaces go dormant for CDMAphone");
            boolean result = false;
            if (qcRilHook != null) {
                Object obj = invokeQcRilHookMethod("qcRilCloseRrc", null, null);
                if (obj != null) {
                    result = ((Boolean) obj).booleanValue();
                }
            }
            riljLog("closeRrc, result = " + result);
        } else {
            Rlog.d(RILJ_LOG_TAG, "not CDMA or GSM phone, not support fast dormancy");
        }
    }

    public boolean setEhrpdByQMI(boolean enable) {
        boolean result = false;
        riljLog("setEhrpdByQMI() entry:enable =" + enable);
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetEhrpdSwitch", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(enable)});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("setEhrpdByQMI() exit : result = " + result);
        return result;
    }

    public void setImsSwitch(boolean on) {
        try {
            ImsManager.getInstance(this.mContext, this.mRilInstanceId.intValue());
            ImsManager.setEnhanced4gLteModeSetting(this.mContext, on);
            System.putInt(this.mContext.getContentResolver(), "hw_volte_user_switch", on ? 1 : 0);
        } catch (NullPointerException e) {
            Rlog.e(RILJ_LOG_TAG, "e = " + e);
        } catch (Exception ex) {
            Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
        }
    }

    public void getImsDomain(Message result) {
        riljLog("getImsDomain");
        AsyncResult.forMessage(result, null, null);
        result.sendToTarget();
    }

    public boolean getImsSwitch() {
        try {
            return HwImsManagerInner.isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mRilInstanceId.intValue());
        } catch (NullPointerException e) {
            Rlog.e(RILJ_LOG_TAG, "e = " + e);
        } catch (Exception ex) {
            Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
        }
        return false;
    }

    public void setLTEReleaseVersion(int state, Message response) {
        int i = 1;
        Integer ret = Integer.valueOf(0);
        boolean isDisableCa = state <= 0;
        Throwable tr = null;
        riljLog("setLTEReleaseVersion() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetLteCaStatus", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(isDisableCa)});
            if (obj != null) {
                if (!((Boolean) obj).booleanValue()) {
                    i = 0;
                }
                ret = Integer.valueOf(i);
            } else {
                tr = new RuntimeException("exception when set ca");
            }
        }
        if (response != null) {
            AsyncResult.forMessage(response, ret, tr);
            response.sendToTarget();
        }
        riljLog("setLTEReleaseVersion() exit");
    }

    public void getLteReleaseVersion(Message response) {
        boolean isCaDisable = false;
        Throwable tr = null;
        int[] result = new int[]{-1};
        riljLog("getLteReleaseVersion() entry");
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
        riljLog("getLteReleaseVersion() exit");
    }

    public void setPowerGrade(int powerGrade, Message response) {
        byte[] payload = new byte[]{(byte) (powerGrade & 127)};
        Rlog.d(RILJ_LOG_TAG, "setPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(524489, 1, payload, response);
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        byte[] payload = new byte[]{(byte) (powerGrade & 127)};
        Rlog.d(RILJ_LOG_TAG, "setWifiTXPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(598042, 1, payload, response);
    }

    public void openSwitchOfUploadBandClass(Message result) {
        riljLog("Rilj, openSwitchOfUploadBandClass");
        sendOemRilRequestRaw(598041, 1, new byte[]{(byte) 1}, result);
    }

    public void closeSwitchOfUploadBandClass(Message result) {
        riljLog("Rilj, closeSwitchOfUploadBandClass");
        sendOemRilRequestRaw(598041, 1, new byte[]{(byte) 0}, result);
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
        boolean z = true;
        Rlog.i(RILJ_LOG_TAG, "cmd for get/set nv, event is : " + event);
        switch (event) {
            case 2:
                if (action != 1) {
                    z = false;
                }
                return setEcTestControl(z);
            case 3:
                if (getNVInterface(action, buf) != (byte) 0) {
                    z = false;
                }
                return z;
            case 4:
                return setKmcPubKey(buf);
            case 5:
                return getKmcPubKey(buf);
            case 6:
                return getRandomInterface(buf);
            case 7:
                if (action == 0) {
                    if (getEcCdmaCallInfo() != 0) {
                        z = false;
                    }
                    return z;
                } else if (action == 1) {
                    return setEcCdmaCallInfo(action);
                }
                break;
            case 8:
                break;
            default:
                Rlog.w(RILJ_LOG_TAG, "error event, return false");
                return false;
        }
        if (action != 1) {
            z = false;
        }
        return setEcCdmaCallVersion(z);
    }

    public byte getNVInterface(int nvItem, byte[] buf) {
        byte result = ERROR;
        riljLog("getNVInterface() entry:nvItem =" + nvItem);
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilReadNvByID", new Class[]{Integer.TYPE, byte[].class}, new Object[]{Integer.valueOf(nvItem), buf});
            if (obj != null) {
                result = ((Byte) obj).byteValue();
            }
        }
        riljLog("getNVInterface() exit : result = " + result);
        return result;
    }

    public boolean setEcTestControl(boolean Control_flag) {
        boolean result = false;
        riljLog("setEcTestControl() entry:Control_flag =" + Control_flag);
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetEcTestControl", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(Control_flag)});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("setEcTestControl() exit : result = " + result);
        return result;
    }

    public boolean setKmcPubKey(byte[] buf) {
        boolean result = false;
        riljLog("setKmcPubKey() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetEcKmcPubKey", new Class[]{byte[].class}, new Object[]{buf});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("setKmcPubKey() exit : result = " + result);
        return result;
    }

    public boolean getKmcPubKey(byte[] buf) {
        boolean result = false;
        riljLog("getKmcPubKey() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilGetEcKmcPubKey", new Class[]{byte[].class}, new Object[]{buf});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("getKmcPubKey() exit : result = " + result);
        return result;
    }

    public boolean getRandomInterface(byte[] buf) {
        boolean result = false;
        riljLog("getRandomInterface() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilGetEcRandomData", new Class[]{byte[].class}, new Object[]{buf});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("getRandomInterface() exit : result = " + result);
        return result;
    }

    public boolean getEcCdmaCallVersion() {
        boolean result = false;
        riljLog("getEcCdmaCallVersion() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilGetEcCdmaCallVersion", null, null);
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("getEcCdmaCallVersion() exit : result = " + result);
        return result;
    }

    public boolean setEcCdmaCallVersion(boolean version) {
        boolean result = false;
        riljLog("setEcCdmaCallVersion() entry:version =" + version);
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetEcCdmaCallVersion", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(version)});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("setEcCdmaCallVersion() exit : result = " + result);
        return result;
    }

    public int getEcCdmaCallInfo() {
        int result = 0;
        riljLog("getEcCdmaCallInfo() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilGetEcCdmaCallInfo", null, null);
            if (obj != null) {
                result = ((Integer) obj).intValue();
            }
        }
        riljLog("getEcCdmaCallInfo() exit : result = " + result);
        return 0;
    }

    public boolean setEcCdmaCallInfo(int info) {
        boolean result = false;
        riljLog("setEcCdmaCallInfo() entry:version =" + info);
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilSetEcCdmaCallInfo", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(info)});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("setEcCdmaCallInfo() exit : result = " + result);
        return result;
    }

    public boolean getMipActiveProfile(byte[] buf) {
        boolean result = false;
        riljLog("getMipActiveProfile() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilGetMipActiveProfile", new Class[]{byte[].class}, new Object[]{buf});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("getMipActiveProfile() exit : result = " + result);
        return result;
    }

    public boolean getMipGenUserProf(byte[] buf) {
        boolean result = false;
        riljLog("getMipGenUserProf() entry");
        if (qcRilHook != null) {
            Object obj = invokeQcRilHookMethod("qcRilGetMipGenUserProf", new Class[]{byte[].class}, new Object[]{buf});
            if (obj != null) {
                result = ((Boolean) obj).booleanValue();
            }
        }
        riljLog("getMipGenUserProf() exit : result = " + result);
        return result;
    }

    private Object invokeQcRilHookMethod(String methodName, Class[] cArgType, Object[] inParamList) {
        try {
            if (qcRilHook != null) {
                return qcRilHook.getClass().getMethod(methodName, cArgType).invoke(qcRilHook, inParamList);
            }
            riljLog("qchook does not init!");
            return null;
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
        } catch (Exception e5) {
            riljLog("occur Exception!");
        }
    }

    public void getSignalStrength(Message result) {
        invokeIRadio(331, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getHwSignalStrength(serial);
            }
        });
    }
}
