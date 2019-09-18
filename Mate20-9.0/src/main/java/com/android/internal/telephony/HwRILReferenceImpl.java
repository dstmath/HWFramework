package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaSmsSubaddress;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.data.DataCallResponse;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.AbstractRIL;
import com.android.internal.telephony.fullnetwork.HwFullNetworkChipCommon;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.IccUtils;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vendor.huawei.hardware.radio.V2_0.IRadio;
import vendor.huawei.hardware.radio.deprecated.V1_0.IOemHook;

public class HwRILReferenceImpl extends Handler implements AbstractRIL.HwRILReference {
    private static final String ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS = "android.intent.action.HW_EXIST_NETWORK_INFO";
    private static final String ACTION_IMS_SWITCH_STATE_CHANGE = "com.huawei.ACTION_IMS_SWITCH_STATE_CHANGE";
    private static final int BYTE_SIZE = 1;
    private static final String CARRIER_CONFIG_CHANGE_STATE = "carrierConfigChangeState";
    private static final int CARRIER_CONFIG_STATE_LOAD = 1;
    private static final int DEFAULT_SUBID = -1;
    private static final int DEFAULT_SUB_ID_RESET = -1;
    private static final int EVENT_GET_IMS_SWITCH_RESULT = 1;
    private static final int EVENT_SET_IMS_SWITCH_RESULT = 2;
    private static final int EVENT_UNSOL_SIM_NVCFG_FINISHED = 3;
    /* access modifiers changed from: private */
    public static final boolean FEATURE_HW_VOLTE_ON = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    private static final boolean FEATURE_HW_VOWIFI_ON = SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false);
    private static final boolean FEATURE_SHOW_VOLTE_SWITCH = SystemProperties.getBoolean("ro.config.hw_volte_show_switch", true);
    /* access modifiers changed from: private */
    public static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    private static final int HW_ANTENNA_STATE_TYPE = 2;
    private static final int HW_BAND_CLASS_TYPE = 1;
    private static final int HW_MAX_TX_POWER_TYPE = 4;
    private static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = {"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final int HW_VOLTE_USER_SWITCH_OFF = 0;
    private static final int HW_VOLTE_USER_SWITCH_ON = 1;
    private static final String IMS_SERVICE_STATE_CHANGED_ACTION = "huawei.intent.action.IMS_SERVICE_STATE_CHANGED";
    private static final String IMS_STATE = "state";
    private static final String IMS_STATE_CHANGE_SUBID = "subId";
    private static final String IMS_STATE_REGISTERED = "REGISTERED";
    private static final int INT_SIZE = 4;
    private static final int NVCFG_RESULT_FINISHED = 1;
    private static final String PROP_LTE_ENABLED = "persist.radio.lte_enabled";
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwRILReferenceImpl";
    private static final int RIL_MAX_COMMAND_BYTES = 8192;
    private static String mMcc = null;
    private Context existNetworkContext;
    protected Registrant mAntStateRegistrant;
    private final BroadcastReceiver mCarrierConfigListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwRILReferenceImpl.this.mHwRilReferenceInstanceId != null) {
                String action = intent.getAction();
                HwRILReferenceImpl.this.riljLog("receive event: action=" + action + ", mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
                int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (HwImsManagerInner.isDualImsAvailable() || HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue() == subId) {
                    if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                        boolean z = false;
                        int curSubForCarrier = intent.getIntExtra("subscription", 0);
                        if ((HwImsManagerInner.isDualImsAvailable() || subId == curSubForCarrier) && curSubForCarrier == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            HwRILReferenceImpl hwRILReferenceImpl = HwRILReferenceImpl.this;
                            if (intent.getIntExtra(HwRILReferenceImpl.CARRIER_CONFIG_CHANGE_STATE, 1) == 1) {
                                z = true;
                            }
                            boolean unused = hwRILReferenceImpl.mIsCarrierConfigLoaded = z;
                            HwRILReferenceImpl.this.riljLog("handle event: CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.");
                            if (HwRILReferenceImpl.this.mRil == null || !HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                                HwRILReferenceImpl.this.riljLog("mRil is null or carrier config is cleared.");
                            } else {
                                HwRILReferenceImpl.this.handleUnsolicitedRadioStateChanged(HwRILReferenceImpl.this.mRil.getRadioState().isOn(), HwRILReferenceImpl.this.mContext);
                            }
                        } else {
                            HwRILReferenceImpl.this.riljLog("getDefault4GSlotId do not match subId from intent.");
                            return;
                        }
                    } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                        if (!HwImsManagerInner.isDualImsAvailable() && HwRILReferenceImpl.this.mRil != null) {
                            HwRILReferenceImpl.this.handleUnsolicitedRadioStateChanged(HwRILReferenceImpl.this.mRil.getRadioState().isOn(), HwRILReferenceImpl.this.mContext);
                        }
                    } else if (HwRILReferenceImpl.IMS_SERVICE_STATE_CHANGED_ACTION.equals(action)) {
                        int curSubId = intent.getIntExtra("subId", -1);
                        HwRILReferenceImpl.this.riljLog("IMS_SERVICE_STATE_CHANGED_ACTION curSubId is : " + curSubId);
                        if (curSubId == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            boolean unused2 = HwRILReferenceImpl.this.mIsImsRegistered = HwRILReferenceImpl.IMS_STATE_REGISTERED.equals(intent.getStringExtra(HwRILReferenceImpl.IMS_STATE));
                            boolean isSupportVolte = HwRILReferenceImpl.FEATURE_HW_VOLTE_ON;
                            if (HwRILReferenceImpl.FEATURE_VOLTE_DYN && HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                                isSupportVolte = HwImsManagerInner.isVolteEnabledByPlatform(context, HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue());
                            } else if (HwRILReferenceImpl.FEATURE_VOLTE_DYN && !HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                                return;
                            }
                            HwRILReferenceImpl.this.riljLog("mIsImsRegistered is : " + HwRILReferenceImpl.this.mIsImsRegistered + " and isSupportVolte is : " + isSupportVolte);
                            if ((!HwRILReferenceImpl.FEATURE_VOLTE_DYN || HwRILReferenceImpl.this.mIsCarrierConfigLoaded) && HwRILReferenceImpl.this.mIsImsRegistered && !isSupportVolte) {
                                HwRILReferenceImpl.this.handleImsSwitch(HwRILReferenceImpl.this.mIsImsRegistered);
                            }
                        }
                    } else if ("com.huawei.ACTION_NETWORK_FACTORY_RESET".equals(action)) {
                        HwRILReferenceImpl.this.riljLog("receive action of reset ims");
                        if (intent.getIntExtra("subId", -1) == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            HwRILReferenceImpl.this.riljLog("reset ims state");
                            HwRILReferenceImpl.this.handleUnsolicitedRadioStateChanged(HwRILReferenceImpl.this.mRil.getRadioState().isOn(), HwRILReferenceImpl.this.mContext);
                        }
                    }
                    return;
                }
                HwRILReferenceImpl.this.riljLog("getDefault4GSlotId do not match mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    protected Registrant mCurBandClassRegistrant;
    private HwCommonRadioIndication mHwCommonRadioIndication;
    private volatile IRadio mHwCommonRadioProxy = null;
    private HwCommonRadioResponse mHwCommonRadioResponse;
    private HwCustRILReference mHwCustRILReference;
    /* access modifiers changed from: private */
    public Integer mHwRilReferenceInstanceId;
    protected RegistrantList mIccUimLockRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public boolean mIsCarrierConfigLoaded = false;
    /* access modifiers changed from: private */
    public boolean mIsImsRegistered = false;
    protected Registrant mMaxTxPowerRegistrant;
    private HwOemHookIndication mOemHookIndication;
    private volatile IOemHook mOemHookProxy = null;
    private HwOemHookResponse mOemHookResponse;
    private WorkSource mRILDefaultWorkSource;
    /* access modifiers changed from: private */
    public RIL mRil;
    private String mcc_operator = null;
    private boolean shouldReportRoamingPlusInfo = true;

    private interface RILCommand {
        void excute(IRadio iRadio, int i) throws RemoteException, RuntimeException;
    }

    public HwRILReferenceImpl(RIL ril) {
        this.mRil = ril;
        if (this.mRil != null) {
            if (!(this.mRil.getContext() == null || this.mRil.getContext().getApplicationInfo() == null)) {
                this.mRILDefaultWorkSource = new WorkSource(this.mRil.getContext().getApplicationInfo().uid, this.mRil.getContext().getPackageName());
            }
            this.mHwCustRILReference = (HwCustRILReference) HwCustUtils.createObj(HwCustRILReference.class, new Object[0]);
            this.mOemHookResponse = new HwOemHookResponse(this.mRil);
            this.mOemHookIndication = new HwOemHookIndication(this.mRil);
            this.mHwCommonRadioResponse = new HwCommonRadioResponse(this.mRil);
            this.mHwCommonRadioIndication = new HwCommonRadioIndication(this.mRil);
            if (FEATURE_VOLTE_DYN && HuaweiTelephonyConfigs.isHisiPlatform()) {
                IntentFilter filter = new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED");
                filter.addAction(IMS_SERVICE_STATE_CHANGED_ACTION);
                filter.addAction("com.huawei.ACTION_NETWORK_FACTORY_RESET");
                filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
                if (this.mRil.getContext() != null) {
                    riljLog("register receiver CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.");
                    this.mRil.getContext().registerReceiver(this.mCarrierConfigListener, filter);
                }
                this.mRil.registerForUnsolNvCfgFinished(this, 3, null);
            }
        }
    }

    public void handleMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case 1:
                    Rlog.d(RILJ_LOG_TAG, "Event EVENT_GET_IMS_SWITCH_RESULT Received");
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        boolean z = false;
                        if (1 == ((int[]) ar.result)[0]) {
                            z = true;
                        }
                        handleImsSwitch(z);
                        break;
                    } else {
                        Rlog.d(RILJ_LOG_TAG, "Get IMS switch failed!");
                        break;
                    }
                case 2:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    Rlog.d(RILJ_LOG_TAG, "Event EVENT_SET_IMS_SWITCH_RESULT Received, AsyncResult.userObj = " + ar2.userObj);
                    if (ar2.exception != null && (ar2.userObj instanceof Boolean) && ((Boolean) ar2.userObj).booleanValue()) {
                        saveSwitchStatusToDB(!getImsSwitch());
                        break;
                    }
                case 3:
                    handleUnsolSimNvCfgFinished(msg);
                    break;
                default:
                    Rlog.d(RILJ_LOG_TAG, "Invalid Message id:[" + msg.what + "]");
                    break;
            }
        }
    }

    private void saveSwitchStatusToDB(boolean on) {
        Rlog.d(RILJ_LOG_TAG, "ims switch in DB: " + on);
        if (this.mContext != null) {
            try {
                Settings.System.putInt(this.mContext.getContentResolver(), HwImsManagerInner.isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[this.mHwRilReferenceInstanceId.intValue()] : HW_VOLTE_USER_SWITCH, on ? 1 : 0);
            } catch (NullPointerException e) {
                Rlog.e(RILJ_LOG_TAG, "saveSwitchStatusToDB NullPointerException");
            } catch (Exception e2) {
                Rlog.e(RILJ_LOG_TAG, "saveSwitchStatusToDB Exception");
            }
        }
    }

    private void handleUnsolSimNvCfgFinished(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            Rlog.e(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: ar exception");
            return;
        }
        Object obj = ar.result;
        if (obj == null || !(obj instanceof Integer)) {
            Rlog.e(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: obj is null or not number");
            return;
        }
        int result = ((Integer) obj).intValue();
        Rlog.d(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: result=" + result);
        boolean needSetVolteSwitchToModem = true;
        boolean singleIms = HwImsManagerInner.isDualImsAvailable() ^ true;
        if (1 != result) {
            needSetVolteSwitchToModem = false;
        }
        if (singleIms) {
            int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            Rlog.d(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: subId=" + subId + ", currentId=" + this.mHwRilReferenceInstanceId);
            if (subId != this.mHwRilReferenceInstanceId.intValue()) {
                needSetVolteSwitchToModem = false;
            }
        }
        if (needSetVolteSwitchToModem) {
            handleUnsolicitedRadioStateChanged(this.mRil.getRadioState().isOn(), this.mContext);
        }
    }

    private String requestToStringEx(int request) {
        return HwTelephonyBaseManagerImpl.getDefault().requestToStringEx(request);
    }

    public Object processSolicitedEx(int rilRequest, Parcel p) {
        if (rilRequest == 518) {
            return responseVoid(p);
        }
        if (rilRequest == 524) {
            return responseVoid(p);
        }
        if (rilRequest == 532) {
            return responseInts(p);
        }
        if (rilRequest == 2017) {
            return responseVoid(p);
        }
        if (rilRequest == 2121) {
            return responseVoid(p);
        }
        Rlog.d(RILJ_LOG_TAG, "The Message is not processed in HwRILReferenceImpl");
        return null;
    }

    /* access modifiers changed from: private */
    public void riljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mHwRilReferenceInstanceId != null) {
            str = " [SUB" + this.mHwRilReferenceInstanceId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.d(RILJ_LOG_TAG, sb.toString());
    }

    private void loge(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mHwRilReferenceInstanceId != null) {
            str = " [SUB" + this.mHwRilReferenceInstanceId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(RILJ_LOG_TAG, sb.toString());
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = p.readInt();
        }
        return response;
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    public void handleImsSwitch(boolean modemImsStatus) {
        if (this.mContext == null) {
            riljLog("handleImsSwitch, mContext is null.nothing to do");
            return;
        }
        int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (this.mHwRilReferenceInstanceId == null || (!HwImsManagerInner.isDualImsAvailable() && this.mHwRilReferenceInstanceId.intValue() != subId)) {
            riljLog("getDefault4GSlotId do not match mHwRilReferenceInstanceId=" + this.mHwRilReferenceInstanceId);
            return;
        }
        boolean apImsStatus = getImsSwitch();
        riljLog("handleImsSwitch and apImsStatus is : " + apImsStatus + " and modemImsStatus is : " + modemImsStatus);
        if (apImsStatus != modemImsStatus) {
            setImsSwitch(apImsStatus, false);
        }
        sendBroadCastToIms(apImsStatus);
    }

    public void setImsSwitch(boolean on) {
        setImsSwitch(on, true);
    }

    private void setImsSwitch(final boolean on, boolean isSaveDB) {
        if (this.mHwRilReferenceInstanceId == null || (!HwImsManagerInner.isDualImsAvailable() && this.mHwRilReferenceInstanceId.intValue() != HwTelephonyManagerInner.getDefault().getDefault4GSlotId())) {
            riljLog("current slot not support volte");
            return;
        }
        if (this.mContext != null && isSaveDB) {
            try {
                Settings.System.putInt(this.mContext.getContentResolver(), HwImsManagerInner.isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[this.mHwRilReferenceInstanceId.intValue()] : HW_VOLTE_USER_SWITCH, on ? 1 : 0);
            } catch (Exception e) {
                Rlog.e(RILJ_LOG_TAG, "setImsSwitch get an exception");
            }
        }
        riljLog("setImsSwitch -> imsstatte : " + on);
        invokeIRadio(2114, obtainMessage(2, Boolean.valueOf(isSaveDB)), new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setImsSwitch(serial, on ? 1 : 0);
            }
        });
    }

    public boolean getImsSwitch() {
        return HwImsManagerInner.isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mHwRilReferenceInstanceId.intValue());
    }

    public void handleUnsolicitedRadioStateChanged(boolean on, Context context) {
        Rlog.d(RILJ_LOG_TAG, "handleUnsolicitedRadioStateChanged: state on =  " + on);
        this.mContext = context;
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            Rlog.d(RILJ_LOG_TAG, "hand radio state change and volte on is " + FEATURE_HW_VOLTE_ON);
            if ((!on || !FEATURE_HW_VOLTE_ON) && !FEATURE_HW_VOWIFI_ON) {
                Rlog.d(RILJ_LOG_TAG, "not to do, radio state is off");
            } else if (!FEATURE_VOLTE_DYN || this.mIsCarrierConfigLoaded) {
                boolean isSupportVolte = HwImsManagerInner.isVolteEnabledByPlatform(context, this.mHwRilReferenceInstanceId.intValue());
                if (!FEATURE_VOLTE_DYN || isSupportVolte || (this.mIsImsRegistered && !isSupportVolte)) {
                    getModemImsSwitch(obtainMessage(1));
                }
            } else {
                Rlog.d(RILJ_LOG_TAG, "CarrierConfig is not loaded completely");
            }
        }
    }

    public void getModemImsSwitch(Message result) {
        riljLog("getModemImsSwitch");
        invokeIRadio(2115, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getImsSwitch(serial);
            }
        });
    }

    private void sendBroadCastToIms(boolean imsSwitchOn) {
        Rlog.d(RILJ_LOG_TAG, "sendBroadCastToIms, imsSwitchOn is: " + imsSwitchOn);
        Intent intent = new Intent();
        intent.setAction(ACTION_IMS_SWITCH_STATE_CHANGE);
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent);
        }
    }

    public void iccGetATR(Message result) {
        invokeIRadio(2032, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimATR(serial);
            }
        });
    }

    public void getPOLCapabilty(Message response) {
        invokeIRadio(2064, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPolCapability(serial);
            }
        });
    }

    public void getCurrentPOLList(Message response) {
        invokeIRadio(2065, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPolList(serial);
            }
        });
    }

    public void setPOLEntry(final int index, final String numeric, final int nAct, Message response) {
        invokeIRadio(2066, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (numeric == null || numeric.length() == 0) {
                    radio.setPolEntry(serial, Integer.toString(index), "", Integer.toString(nAct));
                } else {
                    radio.setPolEntry(serial, Integer.toString(index), numeric, Integer.toString(nAct));
                }
            }
        });
    }

    public void writeContent(CdmaSmsMessage msg, String pdu) {
        try {
            byte[] pduBytes = pdu.getBytes("ISO-8859-1");
            for (byte content : pduBytes) {
                Rlog.e(RILJ_LOG_TAG, "writeSmsToRuim pdu is" + content);
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pduBytes));
            msg.teleserviceId = dis.readInt();
            boolean z = true;
            msg.isServicePresent = ((byte) dis.read()) == 1;
            msg.serviceCategory = dis.readInt();
            msg.address.digitMode = dis.readInt();
            msg.address.numberMode = dis.readInt();
            msg.address.numberType = dis.readInt();
            msg.address.numberPlan = dis.readInt();
            int addrNbrOfDigits = (byte) dis.read();
            for (int i = 0; i < addrNbrOfDigits; i++) {
                msg.address.digits.add(Byte.valueOf((byte) dis.read()));
            }
            msg.subAddress.subaddressType = dis.readInt();
            CdmaSmsSubaddress cdmaSmsSubaddress = msg.subAddress;
            if (((byte) dis.read()) != 1) {
                z = false;
            }
            cdmaSmsSubaddress.odd = z;
            int subaddrNbrOfDigits = (byte) dis.read();
            for (int i2 = 0; i2 < subaddrNbrOfDigits; i2++) {
                msg.subAddress.digits.add(Byte.valueOf((byte) dis.read()));
            }
            int bearerDataLength = dis.readInt();
            for (int i3 = 0; i3 < bearerDataLength; i3++) {
                msg.bearerData.add(Byte.valueOf((byte) dis.read()));
            }
        } catch (UnsupportedEncodingException e) {
            riljLog("writeSmsToRuim: UnsupportedEncodingException");
        } catch (IOException e2) {
            riljLog("writeSmsToRuim: conversion from input stream to object failed");
        }
    }

    public void setShouldReportRoamingPlusInfo(boolean on) {
        if (on) {
            riljLog("shouldReportRoamingPlusInfo will be set true");
            this.shouldReportRoamingPlusInfo = true;
        }
    }

    public void handleRequestGetImsiMessage(RILRequest rr, Object ret, Context context) {
        if (rr.mRequest == 11) {
            riljLog(rr.serialString() + "<      RIL_REQUEST_GET_IMSI xxxxx ");
            if (ret != null && !((String) ret).equals("")) {
                String temp_mcc = ((String) ret).substring(0, 3);
                if (mMcc == null && temp_mcc != null && this.mcc_operator != null && !this.mcc_operator.equals(temp_mcc) && temp_mcc.equals(HwFullNetworkChipCommon.PREFIX_LOCAL_MCC) && this.shouldReportRoamingPlusInfo) {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
                    intent.putExtra("current_mcc", this.mcc_operator);
                    context.sendBroadcast(intent);
                    Rlog.d(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mcc=" + this.mcc_operator + "when handleRequestGetImsiMessage");
                    this.shouldReportRoamingPlusInfo = false;
                }
                mMcc = temp_mcc;
                riljLog(" mMcc = " + mMcc);
            }
        }
    }

    private void unsljLog(int response) {
        riljLog("[UNSL]< " + unsolResponseToString(response));
    }

    private String unsolResponseToString(int request) {
        if (request == 3001) {
            return "UNSOL_HW_RESIDENT_NETWORK_CHANGED";
        }
        if (request == 3003) {
            return "UNSOL_HW_CS_CHANNEL_INFO_IND";
        }
        if (request == 3005) {
            return "UNSOL_HW_ECCNUM";
        }
        if (request == 3031) {
            return "UNSOL_HW_XPASS_RESELECT_INFO";
        }
        if (request == 3034) {
            return "UNSOL_HW_EXIST_NETWORK_INFO";
        }
        return "<unknown response>:" + request;
    }

    private void invokeIRadio(int requestId, Message result, RILCommand cmd) {
        IRadio radioProxy = getHuaweiCommonRadioProxy(null);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(requestId, result, this.mRILDefaultWorkSource);
            this.mRil.addRequestEx(rr);
            riljLog(rr.serialString() + "> " + RIL.requestToString(requestId));
            try {
                cmd.excute(radioProxy, rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                this.mRil.handleRadioProxyExceptionForRREx(RIL.requestToString(requestId), e, rr);
            }
        }
    }

    public IRadio getHuaweiCommonRadioProxy(Message result) {
        if (!this.mRil.mIsMobileNetworkSupported) {
            riljLog("getRadioProxy: Not calling getService(): wifi-only");
            if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mHwCommonRadioProxy != null) {
            return this.mHwCommonRadioProxy;
        } else {
            try {
                this.mHwCommonRadioProxy = IRadio.getService(RIL.HIDL_SERVICE_NAME[this.mRil.mPhoneId == null ? 0 : this.mRil.mPhoneId.intValue()]);
            } catch (RemoteException | RuntimeException e) {
                try {
                    loge("getRadioProxy: huaweiradioProxy got 1_0 RemoteException | RuntimeException");
                } catch (RemoteException | RuntimeException e2) {
                    this.mHwCommonRadioProxy = null;
                    loge("RadioProxy getService/setResponseFunctions got RemoteException | RuntimeException");
                }
            }
            if (this.mHwCommonRadioProxy != null) {
                this.mHwCommonRadioProxy.setResponseFunctionsHuawei(this.mHwCommonRadioResponse, this.mHwCommonRadioIndication);
            } else {
                loge("getRadioProxy: huawei radioProxy == null");
            }
            if (this.mHwCommonRadioProxy == null && result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return this.mHwCommonRadioProxy;
        }
    }

    public void clearHuaweiCommonRadioProxy() {
        riljLog("clearHuaweiCommonRadioProxy");
        this.mHwCommonRadioProxy = null;
    }

    public IOemHook getHwOemHookProxy(Message result) {
        if (!this.mRil.mIsMobileNetworkSupported) {
            riljLog("getHwOemHookProxy: Not calling getService(): wifi-only");
            if (result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mOemHookProxy != null) {
            return this.mOemHookProxy;
        } else {
            try {
                this.mOemHookProxy = IOemHook.getService(RIL.HIDL_SERVICE_NAME[this.mRil.mPhoneId == null ? 0 : this.mRil.mPhoneId.intValue()]);
                if (this.mOemHookProxy != null) {
                    this.mOemHookProxy.setResponseFunctions(this.mOemHookResponse, this.mOemHookIndication);
                } else {
                    loge("getHwOemHookProxy: mOemHookProxy == null");
                }
            } catch (RemoteException | RuntimeException e) {
                this.mOemHookProxy = null;
                loge("OemHookProxy getService/setResponseFunctions got RemoteException | RuntimeException");
            }
            if (this.mOemHookProxy == null && result != null) {
                AsyncResult.forMessage(result, null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return this.mOemHookProxy;
        }
    }

    public void clearHwOemHookProxy() {
        riljLog("clearOemHookProxy");
        this.mOemHookProxy = null;
    }

    public void setPowerGrade(final int powerGrade, Message response) {
        invokeIRadio(518, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setPowerGrade(serial, Integer.toString(powerGrade));
            }
        });
    }

    public void setWifiTxPowerGrade(final int powerGrade, Message response) {
        invokeIRadio(535, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setWifiPowerGrade(serial, Integer.toString(powerGrade));
            }
        });
    }

    public void supplyDepersonalization(final String netpin, final int type, Message result) {
        invokeIRadio(8, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio != null) {
                    radio.supplyDepersonalization(serial, HwRILReferenceImpl.this.convertNullToEmptyString(netpin), type);
                } else {
                    HwRILReferenceImpl.this.riljLog("not support below radio 2.0");
                }
            }
        });
    }

    public void registerForUimLockcard(Handler h, int what, Object obj) {
        this.mIccUimLockRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForUimLockcard(Handler h) {
        this.mIccUimLockRegistrants.remove(h);
    }

    public void notifyIccUimLockRegistrants() {
        if (this.mIccUimLockRegistrants != null) {
            this.mIccUimLockRegistrants.notifyRegistrants();
        }
    }

    public void sendSMSSetLong(final int flag, Message result) {
        invokeIRadio(2015, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setLongMessage(serial, flag);
            }
        });
    }

    public void dataConnectionDetach(final int mode, Message response) {
        invokeIRadio(2011, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.dataConnectionDeatch(serial, mode);
            }
        });
    }

    public void dataConnectionAttach(final int mode, Message response) {
        invokeIRadio(2012, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.dataConnectionAttach(serial, mode);
            }
        });
    }

    public void restartRild(Message result) {
        invokeIRadio(HwFullNetworkConstants.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.restartRILD(serial);
            }
        });
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    public void requestSetEmergencyNumbers(final String ecclist_withcard, final String ecclist_nocard) {
        riljLog("setEmergencyNumbers()");
        invokeIRadio(HwFullNetworkConstants.EVENT_RADIO_ON_PROCESS_SIM_STATE, null, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setEccNum(serial, ecclist_withcard, ecclist_nocard);
            }
        });
    }

    public void queryEmergencyNumbers() {
        riljLog("queryEmergencyNumbers()");
        invokeIRadio(522, null, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getEccNum(serial);
            }
        });
    }

    public void getCdmaGsmImsi(Message result) {
        invokeIRadio(529, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCdmaGsmImsi(serial);
            }
        });
    }

    public void getCdmaModeSide(Message result) {
        invokeIRadio(2127, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCdmaModeSide(serial);
            }
        });
    }

    public void setCdmaModeSide(final int modemID, Message result) {
        invokeIRadio(2118, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setCdmaModeSide(serial, modemID);
            }
        });
    }

    public void setVpMask(final int vpMask, Message result) {
        invokeIRadio(2099, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVoicePreferStatus(serial, vpMask);
            }
        });
    }

    public void resetAllConnections() {
        invokeIRadio(2017, null, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.resetAllConnections(serial);
            }
        });
    }

    public Map<String, String> correctApnAuth(String username, int authType, String password) {
        if (this.mHwCustRILReference != null && this.mHwCustRILReference.isCustCorrectApnAuthOn()) {
            return this.mHwCustRILReference.custCorrectApnAuth(username, authType, password);
        }
        Map<String, String> map = new HashMap<>();
        if (authType == 1) {
            if (TextUtils.isEmpty(username)) {
                authType = 0;
                password = "";
                riljLog("authType is pap but username is null, clear all");
            }
        } else if (authType == 2) {
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                authType = 0;
                username = "";
                password = "";
                riljLog("authType is chap but username or password is null, clear all");
            }
        } else if (authType == 3) {
            if (TextUtils.isEmpty(username)) {
                authType = 0;
                password = "";
                riljLog("authType is pap_chap but username is null, clear all");
            } else if (TextUtils.isEmpty(password)) {
                authType = 1;
                riljLog("authType is pap_chap but password is null, tune authType to pap");
            }
        }
        map.put("userName", username);
        map.put("authType", String.valueOf(authType));
        map.put("password", password);
        return map;
    }

    public void setHwRILReferenceInstanceId(int instanceId) {
        this.mHwRilReferenceInstanceId = Integer.valueOf(instanceId);
        riljLog("set HwRILReference InstanceId: " + instanceId);
    }

    public void notifyCModemStatus(final int state, Message result) {
        invokeIRadio(2121, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.notifyCModemStatus(serial, state);
            }
        });
    }

    public boolean unregisterSarRegistrant(int type, Message result) {
        Registrant removedRegistrant = null;
        riljLog("unregisterSarRegistrant start");
        if (type != 4) {
            switch (type) {
                case 1:
                    removedRegistrant = this.mCurBandClassRegistrant;
                    break;
                case 2:
                    removedRegistrant = this.mAntStateRegistrant;
                    break;
            }
        } else {
            removedRegistrant = this.mMaxTxPowerRegistrant;
        }
        if (removedRegistrant == null || result == null || removedRegistrant.getHandler() != result.getTarget()) {
            return false;
        }
        removedRegistrant.clear();
        return true;
    }

    public boolean registerSarRegistrant(int type, Message result) {
        boolean isSuccess = false;
        boolean z = false;
        if (result == null) {
            riljLog("registerSarRegistrant the param result is null");
            return false;
        }
        if (type != 4) {
            switch (type) {
                case 1:
                    this.mCurBandClassRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                    isSuccess = true;
                    break;
                case 2:
                    this.mAntStateRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                    isSuccess = true;
                    break;
            }
        } else {
            this.mMaxTxPowerRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
            isSuccess = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("registerSarRegistrant type = ");
        sb.append(type);
        sb.append(",isSuccess = ");
        if (!isSuccess) {
            z = true;
        }
        sb.append(z);
        riljLog(sb.toString());
        return isSuccess;
    }

    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
        ByteBuffer payload = ByteBuffer.wrap(data);
        payload.order(ByteOrder.nativeOrder());
        int type_id = payload.get();
        riljLog("type_id in notifyAntOrMaxTxPowerInfo is " + type_id);
        int response_size = payload.getShort();
        if (response_size < 0 || response_size > 8192) {
            riljLog("Response Size is Invalid " + response_size);
            return;
        }
        int result = payload.getInt();
        riljLog("notifyAntOrMaxTxPowerInfo result=" + result);
        ByteBuffer resultData = ByteBuffer.allocate(4);
        resultData.order(ByteOrder.nativeOrder());
        resultData.putInt(result);
        notifyResultByType(type_id, resultData);
    }

    public void notifyBandClassInfo(byte[] data) {
        ByteBuffer payload = ByteBuffer.wrap(data);
        payload.order(ByteOrder.nativeOrder());
        int activeBand = payload.getInt();
        riljLog("notifyBandClassInfo activeBand=" + activeBand);
        ByteBuffer resultData = ByteBuffer.allocate(4);
        resultData.order(ByteOrder.nativeOrder());
        resultData.putInt(activeBand);
        if (this.mCurBandClassRegistrant != null) {
            this.mCurBandClassRegistrant.notifyResult(resultData.array());
        }
    }

    private void notifyResultByType(int type, ByteBuffer resultData) {
        boolean isSuccess = false;
        riljLog("notifyResultByType start");
        if (type != 2) {
            if (type == 4 && this.mMaxTxPowerRegistrant != null) {
                this.mMaxTxPowerRegistrant.notifyResult(resultData.array());
                isSuccess = true;
            }
        } else if (this.mAntStateRegistrant != null) {
            this.mAntStateRegistrant.notifyResult(resultData.array());
            isSuccess = true;
        }
        if (!isSuccess) {
            riljLog("notifyResultByType type = " + type + " notifyResult failed");
        }
    }

    public void sendRacChangeBroadcast(byte[] data) {
        if (data != null) {
            ByteBuffer payload = ByteBuffer.wrap(data);
            payload.order(ByteOrder.nativeOrder());
            int rat = payload.get();
            int rac = payload.get();
            Rlog.d(RILJ_LOG_TAG, "rat: " + rat + " rac: " + rac);
            Intent intent = new Intent("com.huawei.android.intent.action.RAC_CHANGED");
            intent.putExtra("rat", rat);
            intent.putExtra("rac", rac);
            if (this.mContext != null) {
                this.mContext.sendBroadcast(intent);
            }
        }
    }

    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
        Rlog.v(RILJ_LOG_TAG, "sendHWBufferSolicited, event:" + event + ", reqdata:" + IccUtils.bytesToHexString(reqData));
        int length = reqData == null ? 0 : reqData.length;
        RIL ril = this.mRil;
        int dataSize = 5 + length;
        ByteBuffer buf = ByteBuffer.wrap(new byte[("QOEMHOOK".length() + 8 + dataSize)]);
        try {
            buf.order(ByteOrder.nativeOrder());
            RIL ril2 = this.mRil;
            buf.put("QOEMHOOK".getBytes("UTF-8"));
            RIL ril3 = this.mRil;
            buf.putInt(598043);
            buf.putInt(dataSize);
            buf.putInt(event);
            buf.put((byte) length);
            if (length > 0) {
                RIL ril4 = this.mRil;
                if (120 >= length) {
                    buf.put(reqData);
                }
            }
            this.mRil.invokeOemRilRequestRaw(buf.array(), result);
        } catch (UnsupportedEncodingException e) {
            Rlog.d(RILJ_LOG_TAG, "sendHWBufferSolicited failed, UnsupportedEncodingException");
        }
    }

    public void processHWBufferUnsolicited(byte[] respData) {
        if (respData == null || 5 > respData.length) {
            Rlog.d(RILJ_LOG_TAG, "response data is null or unavailable, it from Qcril !!!");
        } else {
            this.mRil.mHWBufferRegistrants.notifyRegistrants(new AsyncResult(null, respData, null));
        }
    }

    public void notifyDeviceState(final String device, final String state, final String extra, Message result) {
        invokeIRadio(537, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.impactAntDevstate(serial, device, state, extra);
            }
        });
    }

    public void existNetworkInfo(String state) {
        unsljLog(3034);
        if (state == null || state.length() < 3) {
            Rlog.d(RILJ_LOG_TAG, "plmn para error! break");
            return;
        }
        this.mcc_operator = state.substring(0, 3);
        Rlog.d(RILJ_LOG_TAG, "recieved RIL_UNSOL_HW_EXIST_NETWORK_INFO with mcc_operator =" + this.mcc_operator + "and mMcc =" + mMcc);
        if ((this.mcc_operator == null || !this.mcc_operator.equals(mMcc)) && ((mMcc == null || mMcc.equals(HwFullNetworkChipCommon.PREFIX_LOCAL_MCC)) && mMcc != null && this.shouldReportRoamingPlusInfo)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
            intent.putExtra("current_mcc", this.mcc_operator);
            this.existNetworkContext.sendBroadcast(intent);
            Rlog.d(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mcc_operator=" + this.mcc_operator);
            this.shouldReportRoamingPlusInfo = false;
        }
    }

    /* access modifiers changed from: private */
    public String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }

    public void sendSimMatchedOperatorInfo(String opKey, String opName, int state, String reserveField, Message response) {
        riljLog("sendSimMatchedOperatorInfo");
        final String str = opKey;
        final String str2 = opName;
        final int i = state;
        final String str3 = reserveField;
        AnonymousClass24 r1 = new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio != null) {
                    radio.sendSimMatchedOperatorInfo(serial, str, str2, i, str3);
                    return;
                }
                HwRILReferenceImpl.this.riljLog("sendSimMatchedOperatorInfo: not support by radio 2.0");
            }
        };
        invokeIRadio(2177, response, r1);
    }

    public void getSignalStrength(Message result) {
        invokeIRadio(331, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getHwSignalStrength(serial);
            }
        });
    }

    public void responseDataCallList(RadioResponseInfo responseInfo, ArrayList<SetupDataCallResult> dataCallResultList) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        ArrayList<DataCallResponse> dcResponseList = new ArrayList<>();
        int resultSize = dataCallResultList.size();
        for (int i = 0; i < resultSize; i++) {
            dcResponseList.add(convertDataCallResult(dataCallResultList.get(i)));
        }
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, dcResponseList);
            }
            this.mRil.processResponseDone(rr, responseInfo, dcResponseList);
        }
    }

    private DataCallResponse convertDataCallResult(SetupDataCallResult dcResult) {
        LinkAddress la;
        SetupDataCallResult setupDataCallResult = dcResult;
        if (setupDataCallResult == null) {
            return null;
        }
        String[] addresses = null;
        if (!TextUtils.isEmpty(setupDataCallResult.addresses)) {
            addresses = setupDataCallResult.addresses.split("\\s+");
        }
        String[] addresses2 = addresses;
        List<InetAddress> gatewayList = new ArrayList<>();
        if (addresses2 != null) {
            for (String address : addresses2) {
                String address2 = address.trim();
                if (!address2.isEmpty()) {
                    try {
                        if (address2.split("/").length == 2) {
                            la = new LinkAddress(address2);
                        } else {
                            InetAddress ia = NetworkUtils.numericToInetAddress(address2);
                            la = new LinkAddress(ia, ia instanceof Inet4Address ? 32 : 64);
                        }
                        gatewayList.add(la);
                    } catch (IllegalArgumentException e) {
                        riljLog("IllegalArgumentException: Unknown address: " + address2);
                    }
                }
            }
        }
        String[] dnses = null;
        if (!TextUtils.isEmpty(setupDataCallResult.dnses)) {
            dnses = setupDataCallResult.dnses.split("\\s+");
        }
        String[] dnses2 = dnses;
        ArrayList arrayList = new ArrayList();
        if (dnses2 != null) {
            for (String dns : dnses2) {
                try {
                    arrayList.add(NetworkUtils.numericToInetAddress(dns.trim()));
                } catch (IllegalArgumentException e2) {
                    riljLog("IllegalArgumentException: Unknown dns: " + dns);
                }
            }
        }
        String[] gateways = null;
        if (!TextUtils.isEmpty(setupDataCallResult.gateways)) {
            gateways = setupDataCallResult.gateways.split("\\s+");
        }
        String[] gateways2 = gateways;
        List<InetAddress> gatewayList2 = new ArrayList<>();
        if (gateways2 != null) {
            for (String gateway : gateways2) {
                try {
                    gatewayList2.add(NetworkUtils.numericToInetAddress(gateway.trim()));
                } catch (IllegalArgumentException e3) {
                    riljLog("IllegalArgumentException: Unknown gateway: " + gateway);
                }
            }
        }
        String[] strArr = addresses2;
        String[] strArr2 = gateways2;
        ArrayList arrayList2 = arrayList;
        String[] strArr3 = dnses2;
        List<InetAddress> list = gatewayList;
        DataCallResponse dataCallResponse = new DataCallResponse(setupDataCallResult.status, setupDataCallResult.suggestedRetryTime, setupDataCallResult.cid, setupDataCallResult.active, setupDataCallResult.type, setupDataCallResult.ifname, gatewayList, arrayList, gatewayList2, new ArrayList(Arrays.asList(setupDataCallResult.pcscf.trim().split("\\s+"))), setupDataCallResult.mtu);
        return dataCallResponse;
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, null);
            msg.sendToTarget();
        }
    }

    public void getSimMatchedFileFromRilCache(final int fileId, Message result) {
        invokeIRadio(2179, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimMatchedFileFromRilCache(serial, fileId);
            }
        });
    }

    public void custSetModemProperties() {
        int isSlotsSwitched = Settings.System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots", 0);
        if ((isSlotsSwitched != 1 || (this.mRil.mPhoneId != null && this.mRil.mPhoneId.intValue() != 0)) && (isSlotsSwitched != 0 || this.mRil.mPhoneId.intValue() != 1)) {
            boolean lteEnabled = HwNetworkTypeUtils.isLteServiceOn(this.mRil.mPreferredNetworkType);
            riljLog("mPhoneId = " + this.mRil.mPhoneId + ", setprop lte_enabled = " + lteEnabled);
            SystemProperties.set(PROP_LTE_ENABLED, String.valueOf(lteEnabled));
        }
    }
}
