package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.provider.Settings.System;
import android.telephony.Rlog;
import com.android.ims.ImsManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.vsim.HwVSimEventReport;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    private static final int OEM_HOOK_RAW_REQUEST_HEADERSIZE = 0;
    private static final int OPEN = 1;
    private static final int READ_EFS_FAIL = -1;
    private static final String RILJ_LOG_TAG = "RILJ-HwQualcommRIL";
    private static final int UNLOCK = 0;
    private static final int VALUE_SIZE = 1;
    private static final int WRITE_FAIL = 0;
    private static final int WRITE_SUCCESS = 1;
    private Integer mRilInstanceId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwQualcommRIL.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwQualcommRIL.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwQualcommRIL.<clinit>():void");
    }

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

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        super(context, preferredNetworkType, cdmaSubscription, null);
        this.mRilInstanceId = null;
    }

    public HwQualcommRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRilInstanceId = null;
        this.mRilInstanceId = instanceId;
    }

    public String getNVESN() {
        Class hwnv = getHWNV();
        if (hwnv == null) {
            return null;
        }
        try {
            return (String) hwnv.getMethod("getNVESN", new Class[WRITE_FAIL]).invoke(null, new Object[WRITE_FAIL]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return null;
        }
    }

    public Object processSolicitedEx(int rilRequest, Parcel p) {
        Object ret = super.processSolicitedEx(rilRequest, p);
        Rlog.d(RILJ_LOG_TAG, "Enter HwQualcommRIL processSolicitedEx,just for test");
        if (ret != null) {
            return ret;
        }
        switch (rilRequest) {
            case 136:
                ret = null;
                break;
            case 528:
                ret = Integer.valueOf(responsecardType(p));
                break;
            default:
                return ret;
        }
        return ret;
    }

    public void queryServiceCellBand(Message result) {
        Rlog.d(RILJ_LOG_TAG, "sending response as NULL");
        sendResponseToTarget(result, 2);
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        riljLog("openSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        boolean z = false;
        Class hwnv = getHWNV();
        if (hwnv == null) {
            return false;
        }
        try {
            Class[] clsArr = new Class[WRITE_SUCCESS];
            clsArr[WRITE_FAIL] = Integer.TYPE;
            Method registModem = hwnv.getMethod("registerModemGenericIndication", clsArr);
            Object[] objArr = new Object[WRITE_SUCCESS];
            objArr[WRITE_FAIL] = Integer.valueOf(mask);
            z = ((Boolean) registModem.invoke(null, objArr)).booleanValue();
            riljLog("registModem.invoke() result is " + z + ",mask = " + mask);
            return z;
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
            return z;
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
            return z;
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
            return z;
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
            return z;
        } catch (Exception e5) {
            riljLog("occur Exception!");
            return z;
        }
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        riljLog("closeSwitchOfUploadAntOrMaxTxPower, mask = " + mask);
        Class hwnv = getHWNV();
        boolean result = false;
        if (hwnv == null) {
            return false;
        }
        try {
            Class[] clsArr = new Class[WRITE_SUCCESS];
            clsArr[WRITE_FAIL] = Integer.TYPE;
            Method unregistModem = hwnv.getMethod("unregisterModemGenericIndication", clsArr);
            Object[] objArr = new Object[WRITE_SUCCESS];
            objArr[WRITE_FAIL] = Integer.valueOf(mask);
            return ((Boolean) unregistModem.invoke(null, objArr)).booleanValue();
        } catch (NoSuchMethodException e) {
            riljLog("occur NoSuchMethodException!");
            return result;
        } catch (RuntimeException e2) {
            riljLog("occur RuntimeException!");
            return result;
        } catch (IllegalAccessException e3) {
            riljLog("occur IllegalAccessException!");
            return result;
        } catch (InvocationTargetException e4) {
            riljLog("occur InvocationTargetException!");
            return result;
        } catch (Exception e5) {
            riljLog("occur Exception!");
            return result;
        }
    }

    protected Object handleUnsolicitedDefaultMessagePara(int response, Parcel p) {
        Object ret = super.handleUnsolicitedDefaultMessagePara(response, p);
        if (ret != null) {
            return ret;
        }
        switch (response) {
            case 3031:
                return null;
            default:
                return ret;
        }
    }

    public void handleUnsolicitedDefaultMessage(int response, Object ret, Context context) {
        super.handleUnsolicitedDefaultMessage(response, ret, context);
        switch (response) {
            case 136:
            default:
        }
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

    private int responsecardType(Parcel p) {
        int cardmode = WRITE_FAIL;
        int has_c = WRITE_FAIL;
        int has_g = WRITE_FAIL;
        IccCardStatus cardStatus = new IccCardStatus();
        cardStatus.setCardState(p.readInt());
        cardStatus.setUniversalPinState(p.readInt());
        cardStatus.mGsmUmtsSubscriptionAppIndex = p.readInt();
        cardStatus.mCdmaSubscriptionAppIndex = p.readInt();
        cardStatus.mImsSubscriptionAppIndex = p.readInt();
        int numApplications = p.readInt();
        if (numApplications > 8) {
            numApplications = 8;
        }
        cardStatus.mApplications = new IccCardApplicationStatus[numApplications];
        for (int i = WRITE_FAIL; i < numApplications; i += WRITE_SUCCESS) {
            IccCardApplicationStatus appStatus = new IccCardApplicationStatus();
            appStatus.app_type = appStatus.AppTypeFromRILInt(p.readInt());
            if (AppType.APPTYPE_RUIM == appStatus.app_type || AppType.APPTYPE_CSIM == appStatus.app_type) {
                has_c = WRITE_SUCCESS;
            } else if (AppType.APPTYPE_USIM == appStatus.app_type || AppType.APPTYPE_SIM == appStatus.app_type) {
                has_g = WRITE_SUCCESS;
            }
            if (cardmode == 0) {
                if (AppType.APPTYPE_CSIM == appStatus.app_type || AppType.APPTYPE_USIM == appStatus.app_type) {
                    cardmode = 2;
                } else if (AppType.APPTYPE_RUIM == appStatus.app_type || AppType.APPTYPE_SIM == appStatus.app_type) {
                    cardmode = WRITE_SUCCESS;
                }
            }
        }
        return ((has_c << WRITE_SUCCESS) | has_g) | (cardmode << INT_SIZE);
    }

    public void queryCardType(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(528, result);
        Rlog.d(RILJ_LOG_TAG, rr.serialString() + "> " + requestToString(rr.getRequest()));
        send(rr);
    }

    public void resetProfile(Message response) {
        Rlog.d(RILJ_LOG_TAG, "resetProfile");
        sendOemRilRequestRaw(524588, WRITE_FAIL, null, response);
    }

    public void sendCloudMessageToModem(int event_id) {
        Rlog.d(RILJ_LOG_TAG, "sendCloudMessageToModem event :" + event_id);
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = Integer.TYPE;
                Method sendCloudOtaCmd = hwnv.getMethod("sendCloudOtaCmd", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = Integer.valueOf(event_id);
                sendCloudOtaCmd.invoke(null, objArr);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (RuntimeException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
    }

    public void registerForModemCapEvent(Handler h, int what, Object obj) {
        this.mModemCapRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForModemCapEvent(Handler h) {
        this.mModemCapRegistrants.remove(h);
    }

    public void getModemCapability(Message response) {
        Rlog.d(RILJ_LOG_TAG, "GetModemCapability");
        sendOemRilRequestRaw(524323, WRITE_FAIL, null, response);
    }

    public void updateStackBinding(int stack, int enable, Message response) {
        byte[] payload = new byte[]{(byte) stack, (byte) enable};
        Rlog.d(RILJ_LOG_TAG, "UpdateStackBinding: on Stack: " + stack + ", enable/disable: " + enable);
        sendOemRilRequestRaw(524324, 2, payload, response);
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mRilInstanceId != null ? " [SUB" + this.mRilInstanceId + "]" : ""));
    }

    public void switchVoiceCallBackgroundState(int state, Message result) {
        byte[] payload = new byte[WRITE_SUCCESS];
        payload[WRITE_FAIL] = (byte) (state & 127);
        Rlog.d(RILJ_LOG_TAG, "switchVoiceCallBackgroundState: state is " + state);
        sendOemRilRequestRaw(524301, WRITE_SUCCESS, payload, null);
    }

    private void sendOemRilRequestRaw(int requestId, int numPayload, byte[] payload, Message response) {
        int i;
        int i2 = WRITE_FAIL;
        byte[] request = new byte[(this.mHeaderSize + (numPayload * WRITE_SUCCESS))];
        ByteBuffer buf = ByteBuffer.wrap(request);
        buf.order(ByteOrder.nativeOrder());
        char[] toCharArray = "QOEMHOOK".toCharArray();
        int length = toCharArray.length;
        for (i = WRITE_FAIL; i < length; i += WRITE_SUCCESS) {
            buf.put((byte) toCharArray[i]);
        }
        buf.putInt(requestId);
        if (numPayload > 0 && payload != null) {
            buf.putInt(numPayload * WRITE_SUCCESS);
            i = payload.length;
            while (i2 < i) {
                buf.put(payload[i2]);
                i2 += WRITE_SUCCESS;
            }
        }
        invokeOemRilRequestRaw(request, response);
    }

    public String getHwPrlVersion() {
        String prl = "0";
        Class hwnvCls = getHWNV();
        if (hwnvCls != null) {
            try {
                prl = (String) hwnvCls.getMethod("getCDMAPrlVersion", new Class[WRITE_FAIL]).invoke(null, new Object[WRITE_FAIL]);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
        return prl == null ? "0" : prl;
    }

    public String getHwUimid() {
        String esn = getNVESN();
        return esn == null ? "0" : esn;
    }

    public void closeRrc() {
        closeRrc("");
    }

    public void closeRrc(String interfaceName) {
        if (WRITE_SUCCESS == this.mPhoneType) {
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
            Class hwnv = getHWNV();
            if (hwnv != null) {
                try {
                    hwnv.getMethod("closeRrc", new Class[WRITE_FAIL]).invoke(null, new Object[WRITE_FAIL]);
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (RuntimeException e3) {
                    e3.printStackTrace();
                } catch (IllegalAccessException e4) {
                    e4.printStackTrace();
                } catch (InvocationTargetException e5) {
                    e5.printStackTrace();
                }
            }
        } else {
            Rlog.d(RILJ_LOG_TAG, "not CDMA or GSM phone, not support fast dormancy");
        }
    }

    public boolean setEhrpdByQMI(boolean enable) {
        Class hwnv = getHWNV();
        if (hwnv == null) {
            return false;
        }
        try {
            Class[] clsArr = new Class[WRITE_SUCCESS];
            clsArr[WRITE_FAIL] = Boolean.TYPE;
            Method setEhrpdByQMI = hwnv.getMethod("setEhrpdByQMI", clsArr);
            Object[] objArr = new Object[WRITE_SUCCESS];
            objArr[WRITE_FAIL] = Boolean.valueOf(enable);
            return ((Boolean) setEhrpdByQMI.invoke(null, objArr)).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            return false;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return false;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return false;
        }
    }

    public void setImsSwitch(boolean on) {
        try {
            ImsManager.getInstance(this.mContext, this.mRilInstanceId.intValue());
            ImsManager.setEnhanced4gLteModeSetting(this.mContext, on);
            System.putInt(this.mContext.getContentResolver(), "hw_volte_user_switch", on ? WRITE_SUCCESS : WRITE_FAIL);
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
        boolean z = true;
        try {
            if (System.getInt(this.mContext.getContentResolver(), "hw_volte_user_switch", WRITE_FAIL) != WRITE_SUCCESS) {
                z = false;
            }
            return z;
        } catch (NullPointerException e) {
            Rlog.e(RILJ_LOG_TAG, "e = " + e);
            return false;
        } catch (Exception ex) {
            Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
            return false;
        }
    }

    public void setLTEReleaseVersion(boolean state, Message response) {
        Class hwnv = getHWNV();
        Integer ret = Integer.valueOf(WRITE_FAIL);
        byte[] value = new byte[WRITE_SUCCESS];
        Throwable tr = null;
        if (state) {
            value[WRITE_FAIL] = (byte) 0;
        } else {
            value[WRITE_FAIL] = (byte) 1;
        }
        if (hwnv != null) {
            try {
                ret = (Integer) hwnv.getMethod("writeEFSItemFile", new Class[]{String.class, byte[].class, Integer.TYPE}).invoke(null, new Object[]{ITEM_FILE_CA_DISABLE, value, Integer.valueOf(WRITE_SUCCESS)});
                Rlog.e(RILJ_LOG_TAG, "write ca_disable value=" + value[WRITE_FAIL] + " ret=" + ret);
            } catch (Throwable nsme) {
                nsme.printStackTrace();
                tr = nsme;
            } catch (Throwable re) {
                re.printStackTrace();
                tr = re;
            } catch (Throwable iae) {
                iae.printStackTrace();
                tr = iae;
            } catch (Throwable ite) {
                ite.printStackTrace();
                tr = ite;
            }
        } else {
            Rlog.e(RILJ_LOG_TAG, "getHWNV() return null !!");
        }
        if (response != null) {
            AsyncResult.forMessage(response, ret, tr);
            response.sendToTarget();
        }
    }

    public void getLteReleaseVersion(Message response) {
        Class hwnv = getHWNV();
        byte[] value = new byte[WRITE_SUCCESS];
        Throwable tr = null;
        Integer ret = Integer.valueOf(READ_EFS_FAIL);
        int[] result = new int[WRITE_SUCCESS];
        result[WRITE_FAIL] = READ_EFS_FAIL;
        if (hwnv != null) {
            try {
                ret = (Integer) hwnv.getMethod("readEFSItemFile", new Class[]{String.class, byte[].class, Integer.TYPE}).invoke(null, new Object[]{ITEM_FILE_CA_DISABLE, value, Integer.valueOf(WRITE_SUCCESS)});
                Rlog.e(RILJ_LOG_TAG, "read ca_disable value=" + value[WRITE_FAIL] + " ret=" + ret);
            } catch (Throwable nsme) {
                nsme.printStackTrace();
                tr = nsme;
            } catch (Throwable re) {
                re.printStackTrace();
                tr = re;
            } catch (Throwable iae) {
                iae.printStackTrace();
                tr = iae;
            } catch (Throwable ite) {
                ite.printStackTrace();
                tr = ite;
            }
        } else {
            Rlog.e(RILJ_LOG_TAG, "getHWNV() return null !!");
        }
        if (response != null) {
            if (ret.intValue() == WRITE_SUCCESS) {
                if (value[WRITE_FAIL] == WRITE_SUCCESS) {
                    result[WRITE_FAIL] = WRITE_FAIL;
                } else if (value[WRITE_FAIL] == null) {
                    result[WRITE_FAIL] = WRITE_SUCCESS;
                }
            } else if (ret.intValue() == FILE_NOT_EXIST) {
                result[WRITE_FAIL] = WRITE_SUCCESS;
            }
            AsyncResult.forMessage(response, result, tr);
            response.sendToTarget();
        }
    }

    public void setPowerGrade(int powerGrade, Message response) {
        byte[] payload = new byte[WRITE_SUCCESS];
        payload[WRITE_FAIL] = (byte) (powerGrade & 127);
        Rlog.d(RILJ_LOG_TAG, "setPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(524489, WRITE_SUCCESS, payload, response);
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        byte[] payload = new byte[WRITE_SUCCESS];
        payload[WRITE_FAIL] = (byte) (powerGrade & 127);
        Rlog.d(RILJ_LOG_TAG, "setWifiTXPowerGrade: state is " + powerGrade);
        sendOemRilRequestRaw(598042, WRITE_SUCCESS, payload, response);
    }

    public void openSwitchOfUploadBandClass(Message result) {
        riljLog("Rilj, openSwitchOfUploadBandClass");
        byte[] bArr = new byte[WRITE_SUCCESS];
        bArr[WRITE_FAIL] = (byte) 1;
        sendOemRilRequestRaw(598041, WRITE_SUCCESS, bArr, result);
    }

    public void closeSwitchOfUploadBandClass(Message result) {
        riljLog("Rilj, closeSwitchOfUploadBandClass");
        byte[] bArr = new byte[WRITE_SUCCESS];
        bArr[WRITE_FAIL] = (byte) 0;
        sendOemRilRequestRaw(598041, WRITE_SUCCESS, bArr, result);
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
        char[] toCharArray = "QOEMHOOK".toCharArray();
        int length = toCharArray.length;
        for (int i = WRITE_FAIL; i < length; i += WRITE_SUCCESS) {
            buf.put((byte) toCharArray[i]);
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
            case HwVSimUtilsInner.STATE_EB /*2*/:
                if (action != WRITE_SUCCESS) {
                    z = false;
                }
                return setEcTestControl(z);
            case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                if (getNVInterface(action, buf) != null) {
                    z = false;
                }
                return z;
            case INT_SIZE /*4*/:
                return setKmcPubKey(buf);
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SWITCH_SLOT /*5*/:
                return getKmcPubKey(buf);
            case HwVSimEventReport.VSIM_CAUSE_TYPE_SET_TEE_DATA /*6*/:
                return getRandomInterface(buf);
            case HwVSimEventReport.VSIM_CAUSE_TYPE_CARD_POWER_ON /*7*/:
                if (action == 0) {
                    if (getEcCdmaCallInfo() != 0) {
                        z = false;
                    }
                    return z;
                } else if (action == WRITE_SUCCESS) {
                    return setEcCdmaCallInfo(action);
                }
                break;
            case HwVSimEventReport.VSIM_CAUSE_TYPE_ACTIVE_MODEM_MODE /*8*/:
                break;
            default:
                Rlog.w(RILJ_LOG_TAG, "error event, return false");
                return false;
        }
        if (action != WRITE_SUCCESS) {
            z = false;
        }
        return setEcCdmaCallVersion(z);
    }

    public byte getNVInterface(int nvItem, byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Byte) hwnv.getMethod("getNVInterface", new Class[]{Integer.TYPE, byte[].class}).invoke(null, new Object[]{Integer.valueOf(nvItem), buf})).byteValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getNVInterface InvocationTargetException " + e4);
            }
        }
        return ERROR;
    }

    public boolean setEcTestControl(boolean Control_flag) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = Boolean.TYPE;
                Method setEcTestControl = hwnv.getMethod("setEcTestControl", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = Boolean.valueOf(Control_flag);
                return ((Boolean) setEcTestControl.invoke(null, objArr)).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setEcTestControl InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean setKmcPubKey(byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = byte[].class;
                Method setKmcPubKey = hwnv.getMethod("setKmcPubKey", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = buf;
                return ((Boolean) setKmcPubKey.invoke(null, objArr)).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setKmcPubKey InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean getKmcPubKey(byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = byte[].class;
                Method getKmcPubKey = hwnv.getMethod("getKmcPubKey", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = buf;
                return ((Boolean) getKmcPubKey.invoke(null, objArr)).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getKmcPubKey InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean getRandomInterface(byte[] buf) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = byte[].class;
                Method getRandomInterface = hwnv.getMethod("getRandomInterface", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = buf;
                return ((Boolean) getRandomInterface.invoke(null, objArr)).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getRandomInterface InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean getEcCdmaCallVersion() {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Boolean) hwnv.getMethod("getEcCdmaCallVersion", new Class[WRITE_FAIL]).invoke(null, new Object[WRITE_FAIL])).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallVersion InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public boolean setEcCdmaCallVersion(boolean version) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = Boolean.TYPE;
                Method setEcCdmaCallVersion = hwnv.getMethod("setEcCdmaCallVersion", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = Boolean.valueOf(version);
                return ((Boolean) setEcCdmaCallVersion.invoke(null, objArr)).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallVersion InvocationTargetException " + e4);
            }
        }
        return false;
    }

    public int getEcCdmaCallInfo() {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                return ((Integer) hwnv.getMethod("getEcCdmaCallInfo", new Class[WRITE_FAIL]).invoke(null, new Object[WRITE_FAIL])).intValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "getEcCdmaCallInfo InvocationTargetException " + e4);
            }
        }
        return WRITE_FAIL;
    }

    public boolean setEcCdmaCallInfo(int info) {
        Class hwnv = getHWNV();
        if (hwnv != null) {
            try {
                Class[] clsArr = new Class[WRITE_SUCCESS];
                clsArr[WRITE_FAIL] = Integer.TYPE;
                Method setEcCdmaCallInfo = hwnv.getMethod("setEcCdmaCallInfo", clsArr);
                Object[] objArr = new Object[WRITE_SUCCESS];
                objArr[WRITE_FAIL] = Integer.valueOf(info);
                return ((Boolean) setEcCdmaCallInfo.invoke(null, objArr)).booleanValue();
            } catch (NoSuchMethodException e) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo NoSuchMethodException " + e);
            } catch (RuntimeException e2) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo RuntimeException " + e2);
            } catch (IllegalAccessException e3) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo IllegalAccessException " + e3);
            } catch (InvocationTargetException e4) {
                Rlog.e(RILJ_LOG_TAG, "setEcCdmaCallInfo InvocationTargetException " + e4);
            }
        }
        return false;
    }
}
