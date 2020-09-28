package com.android.internal.telephony;

import android.content.Context;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.config.V1_0.IRadioConfig;
import android.hardware.radio.config.V1_0.SimSlotStatus;
import android.hardware.radio.config.V1_1.ModemsConfig;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Registrant;
import android.os.RemoteException;
import android.os.WorkSource;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.uicc.IccSlotStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

public class RadioConfig extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_SERVICE_DEAD = 1;
    private static final HalVersion RADIO_CONFIG_HAL_VERSION_1_0 = new HalVersion(1, 0);
    private static final HalVersion RADIO_CONFIG_HAL_VERSION_1_1 = new HalVersion(1, 1);
    private static final HalVersion RADIO_CONFIG_HAL_VERSION_UNKNOWN = new HalVersion(-1, -1);
    private static final String TAG = "RadioConfig";
    private static final boolean VDBG = false;
    private static RadioConfig sRadioConfig;
    private final WorkSource mDefaultWorkSource;
    private final boolean mIsMobileNetworkSupported;
    private final RadioConfigIndication mRadioConfigIndication;
    private volatile IRadioConfig mRadioConfigProxy = null;
    private final AtomicLong mRadioConfigProxyCookie = new AtomicLong(0);
    private final RadioConfigResponse mRadioConfigResponse;
    private HalVersion mRadioConfigVersion = RADIO_CONFIG_HAL_VERSION_UNKNOWN;
    private final SparseArray<RILRequest> mRequestList = new SparseArray<>();
    private final ServiceDeathRecipient mServiceDeathRecipient;
    protected Registrant mSimSlotStatusRegistrant;

    /* access modifiers changed from: package-private */
    public final class ServiceDeathRecipient implements IHwBinder.DeathRecipient {
        ServiceDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            RadioConfig.logd("serviceDied");
            RadioConfig radioConfig = RadioConfig.this;
            radioConfig.sendMessage(radioConfig.obtainMessage(1, Long.valueOf(cookie)));
        }
    }

    private RadioConfig(Context context) {
        this.mIsMobileNetworkSupported = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        this.mRadioConfigResponse = new RadioConfigResponse(this);
        this.mRadioConfigIndication = new RadioConfigIndication(this);
        this.mServiceDeathRecipient = new ServiceDeathRecipient();
        this.mDefaultWorkSource = new WorkSource(context.getApplicationInfo().uid, context.getPackageName());
    }

    public static RadioConfig getInstance(Context context) {
        if (sRadioConfig == null) {
            sRadioConfig = new RadioConfig(context);
        }
        return sRadioConfig;
    }

    public void handleMessage(Message message) {
        if (message.what == 1) {
            logd("handleMessage: EVENT_SERVICE_DEAD cookie = " + message.obj + " mRadioConfigProxyCookie = " + this.mRadioConfigProxyCookie.get());
            if (((Long) message.obj).longValue() == this.mRadioConfigProxyCookie.get()) {
                resetProxyAndRequestList("EVENT_SERVICE_DEAD", null);
            }
        }
    }

    private void clearRequestList(int error, boolean loggable) {
        synchronized (this.mRequestList) {
            int count = this.mRequestList.size();
            if (loggable) {
                logd("clearRequestList: mRequestList=" + count);
            }
            for (int i = 0; i < count; i++) {
                RILRequest rr = this.mRequestList.valueAt(i);
                if (loggable) {
                    logd(i + ": [" + rr.mSerial + "] " + requestToString(rr.mRequest));
                }
                rr.onError(error, null);
                rr.release();
            }
            this.mRequestList.clear();
        }
    }

    private void resetProxyAndRequestList(String caller, Exception e) {
        loge(caller + ": " + e);
        this.mRadioConfigProxy = null;
        this.mRadioConfigProxyCookie.incrementAndGet();
        RILRequest.resetSerial();
        clearRequestList(1, false);
        getRadioConfigProxy(null);
    }

    public IRadioConfig getRadioConfigProxy(Message result) {
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mRadioConfigProxy != null) {
            return this.mRadioConfigProxy;
        } else {
            updateRadioConfigProxy();
            if (this.mRadioConfigProxy == null && result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return this.mRadioConfigProxy;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0042, code lost:
        r4.mRadioConfigProxy = null;
        loge("getRadioConfigProxy: RadioConfigProxy setResponseFunctions: " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x000c A[ExcHandler: RemoteException | RuntimeException (r0v8 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:5:0x000f] */
    private void updateRadioConfigProxy() {
        try {
            this.mRadioConfigProxy = android.hardware.radio.config.V1_1.IRadioConfig.getService(true);
            this.mRadioConfigVersion = RADIO_CONFIG_HAL_VERSION_1_1;
        } catch (NoSuchElementException e) {
        }
        try {
            if (this.mRadioConfigProxy == null) {
                try {
                    this.mRadioConfigProxy = IRadioConfig.getService(true);
                    this.mRadioConfigVersion = RADIO_CONFIG_HAL_VERSION_1_0;
                } catch (NoSuchElementException e2) {
                }
            }
            if (this.mRadioConfigProxy == null) {
                loge("getRadioConfigProxy: mRadioConfigProxy == null");
                return;
            }
            this.mRadioConfigProxy.linkToDeath(this.mServiceDeathRecipient, this.mRadioConfigProxyCookie.incrementAndGet());
            this.mRadioConfigProxy.setResponseFunctions(this.mRadioConfigResponse, this.mRadioConfigIndication);
        } catch (RemoteException | RuntimeException e3) {
        }
    }

    private RILRequest obtainRequest(int request, Message result, WorkSource workSource) {
        RILRequest rr = RILRequest.obtain(request, result, workSource);
        synchronized (this.mRequestList) {
            this.mRequestList.append(rr.mSerial, rr);
        }
        return rr;
    }

    private RILRequest findAndRemoveRequestFromList(int serial) {
        RILRequest rr;
        synchronized (this.mRequestList) {
            rr = this.mRequestList.get(serial);
            if (rr != null) {
                this.mRequestList.remove(serial);
            }
        }
        return rr;
    }

    public RILRequest processResponse(RadioResponseInfo responseInfo) {
        int serial = responseInfo.serial;
        int error = responseInfo.error;
        int type = responseInfo.type;
        if (type != 0) {
            loge("processResponse: Unexpected response type " + type);
        }
        RILRequest rr = findAndRemoveRequestFromList(serial);
        if (rr != null) {
            return rr;
        }
        loge("processResponse: Unexpected response! serial: " + serial + " error: " + error);
        return null;
    }

    public void getSimSlotsStatus(Message result) {
        IRadioConfig radioConfigProxy = getRadioConfigProxy(result);
        if (radioConfigProxy != null) {
            RILRequest rr = obtainRequest(200, result, this.mDefaultWorkSource);
            logd(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioConfigProxy.getSimSlotsStatus(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                resetProxyAndRequestList("getSimSlotsStatus", e);
            }
        }
    }

    public void setPreferredDataModem(int modemId, Message result) {
        if (isSetPreferredDataCommandSupported()) {
            RILRequest rr = obtainRequest(204, result, this.mDefaultWorkSource);
            logd(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                ((android.hardware.radio.config.V1_1.IRadioConfig) this.mRadioConfigProxy).setPreferredDataModem(rr.mSerial, (byte) modemId);
            } catch (RemoteException | RuntimeException e) {
                resetProxyAndRequestList("setPreferredDataModem", e);
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    public void getPhoneCapability(Message result) {
        if (getRadioConfigProxy(null) != null && !this.mRadioConfigVersion.less(RADIO_CONFIG_HAL_VERSION_1_1)) {
            RILRequest rr = obtainRequest(206, result, this.mDefaultWorkSource);
            logd(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                ((android.hardware.radio.config.V1_1.IRadioConfig) this.mRadioConfigProxy).getPhoneCapability(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                resetProxyAndRequestList("getPhoneCapability", e);
            }
        } else if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }

    public boolean isSetPreferredDataCommandSupported() {
        if (!HuaweiTelephonyConfigs.isMTKPlatform() || !HwModemCapability.isCapabilitySupport(29) || getRadioConfigProxy(null) == null || !this.mRadioConfigVersion.greaterOrEqual(RADIO_CONFIG_HAL_VERSION_1_1)) {
            return false;
        }
        return true;
    }

    public void setSimSlotsMapping(int[] physicalSlots, Message result) {
        IRadioConfig radioConfigProxy = getRadioConfigProxy(result);
        if (radioConfigProxy != null) {
            RILRequest rr = obtainRequest(201, result, this.mDefaultWorkSource);
            logd(rr.serialString() + "> " + requestToString(rr.mRequest) + " " + Arrays.toString(physicalSlots));
            try {
                radioConfigProxy.setSimSlotsMapping(rr.mSerial, primitiveArrayToArrayList(physicalSlots));
            } catch (RemoteException | RuntimeException e) {
                resetProxyAndRequestList("setSimSlotsMapping", e);
            }
        }
    }

    private static ArrayList<Integer> primitiveArrayToArrayList(int[] arr) {
        ArrayList<Integer> arrayList = new ArrayList<>(arr.length);
        for (int i : arr) {
            arrayList.add(Integer.valueOf(i));
        }
        return arrayList;
    }

    static String requestToString(int request) {
        if (request == 200) {
            return "GET_SLOT_STATUS";
        }
        if (request == 201) {
            return "SET_LOGICAL_TO_PHYSICAL_SLOT_MAPPING";
        }
        if (request == 204) {
            return "SET_PREFERRED_DATA_MODEM";
        }
        if (request == 206) {
            return "GET_PHONE_CAPABILITY";
        }
        if (request == 207) {
            return "SWITCH_DUAL_SIM_CONFIG";
        }
        return "<unknown request " + request + ">";
    }

    public void setModemsConfig(int numOfLiveModems, Message result) {
        IRadioConfig radioConfigProxy = getRadioConfigProxy(result);
        if (radioConfigProxy != null && this.mRadioConfigVersion.greaterOrEqual(RADIO_CONFIG_HAL_VERSION_1_1)) {
            android.hardware.radio.config.V1_1.IRadioConfig radioConfigProxy11 = (android.hardware.radio.config.V1_1.IRadioConfig) radioConfigProxy;
            RILRequest rr = obtainRequest(207, result, this.mDefaultWorkSource);
            logd(rr.serialString() + "> " + requestToString(rr.mRequest) + ", numOfLiveModems = " + numOfLiveModems);
            try {
                ModemsConfig modemsConfig = new ModemsConfig();
                modemsConfig.numOfLiveModems = (byte) numOfLiveModems;
                radioConfigProxy11.setModemsConfig(rr.mSerial, modemsConfig);
            } catch (RemoteException | RuntimeException e) {
                resetProxyAndRequestList("setModemsConfig", e);
            }
        }
    }

    public void registerForSimSlotStatusChanged(Handler h, int what, Object obj) {
        this.mSimSlotStatusRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterForSimSlotStatusChanged(Handler h) {
        Registrant registrant = this.mSimSlotStatusRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mSimSlotStatusRegistrant.clear();
            this.mSimSlotStatusRegistrant = null;
        }
    }

    static ArrayList<IccSlotStatus> convertHalSlotStatus(ArrayList<SimSlotStatus> halSlotStatusList) {
        ArrayList<IccSlotStatus> response = new ArrayList<>(halSlotStatusList.size());
        Iterator<SimSlotStatus> it = halSlotStatusList.iterator();
        while (it.hasNext()) {
            SimSlotStatus slotStatus = it.next();
            IccSlotStatus iccSlotStatus = new IccSlotStatus();
            iccSlotStatus.setCardState(slotStatus.cardState);
            iccSlotStatus.setSlotState(slotStatus.slotState);
            iccSlotStatus.logicalSlotIndex = slotStatus.logicalSlotId;
            iccSlotStatus.atr = slotStatus.atr;
            iccSlotStatus.iccid = slotStatus.iccid;
            response.add(iccSlotStatus);
        }
        return response;
    }

    static ArrayList<IccSlotStatus> convertHalSlotStatus_1_2(ArrayList<android.hardware.radio.config.V1_2.SimSlotStatus> halSlotStatusList) {
        ArrayList<IccSlotStatus> response = new ArrayList<>(halSlotStatusList.size());
        Iterator<android.hardware.radio.config.V1_2.SimSlotStatus> it = halSlotStatusList.iterator();
        while (it.hasNext()) {
            android.hardware.radio.config.V1_2.SimSlotStatus slotStatus = it.next();
            IccSlotStatus iccSlotStatus = new IccSlotStatus();
            iccSlotStatus.setCardState(slotStatus.base.cardState);
            iccSlotStatus.setSlotState(slotStatus.base.slotState);
            iccSlotStatus.logicalSlotIndex = slotStatus.base.logicalSlotId;
            iccSlotStatus.atr = slotStatus.base.atr;
            iccSlotStatus.iccid = slotStatus.base.iccid;
            iccSlotStatus.eid = slotStatus.eid;
            response.add(iccSlotStatus);
        }
        return response;
    }

    /* access modifiers changed from: private */
    public static void logd(String log) {
        Rlog.d(TAG, log);
    }

    private static void loge(String log) {
        Rlog.e(TAG, log);
    }
}
