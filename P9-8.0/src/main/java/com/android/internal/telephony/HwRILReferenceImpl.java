package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.AbstractRIL.HwRILReference;
import com.android.internal.telephony.AbstractRIL.RILCommand;
import com.android.internal.telephony.HwCallManagerReference.HWBuffer;
import com.android.internal.telephony.uicc.IccUtils;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import vendor.huawei.hardware.radio.V1_0.IRadio;

public class HwRILReferenceImpl extends Handler implements HwRILReference {
    private static final String ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS = "android.intent.action.HW_EXIST_NETWORK_INFO";
    private static final String ACTION_IMS_SWITCH_STATE_CHANGE = "com.huawei.ACTION_IMS_SWITCH_STATE_CHANGE";
    private static final int BYTE_SIZE = 1;
    private static final String CARRIER_CONFIG_CHANGE_STATE = "carrierConfigChangeState";
    private static final int CARRIER_CONFIG_STATE_LOAD = 1;
    private static final int DEFAULT_SUBID = -1;
    private static final int DEFAULT_SUB_ID_RESET = -1;
    protected static final int EVENT_GET_IMS_SWITCH_RESULT = 1;
    protected static final int EVENT_SET_IMS_SWITCH_FAIL = 2;
    private static final boolean FEATURE_HW_VOLTE_ON = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    private static final boolean FEATURE_HW_VOWIFI_ON = SystemProperties.getBoolean("ro.config.hw_vowifi", false);
    private static final boolean FEATURE_SHOW_VOLTE_SWITCH = SystemProperties.getBoolean("ro.config.hw_volte_show_switch", true);
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    private static final int HW_ANTENNA_STATE_TYPE = 2;
    private static final int HW_BAND_CLASS_TYPE = 1;
    private static final int HW_MAX_TX_POWER_TYPE = 4;
    private static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = new String[]{"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final int HW_VOLTE_USER_SWITCH_OFF = 0;
    private static final int HW_VOLTE_USER_SWITCH_ON = 1;
    private static final String IMS_SERVICE_STATE_CHANGED_ACTION = "huawei.intent.action.IMS_SERVICE_STATE_CHANGED";
    private static final String IMS_STATE = "state";
    private static final String IMS_STATE_CHANGE_SUBID = "subId";
    private static final String IMS_STATE_REGISTERED = "REGISTERED";
    private static final int INT_SIZE = 4;
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwRILReferenceImpl";
    private static final int RIL_MAX_COMMAND_BYTES = 8192;
    private static String mMcc = null;
    private Context existNetworkContext;
    protected Registrant mAntStateRegistrant;
    private final BroadcastReceiver mCarrierConfigListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            if (intent != null && HwRILReferenceImpl.this.mHwRilReferenceInstanceId != null) {
                String action = intent.getAction();
                HwRILReferenceImpl.this.riljLog("receive event: action=" + action + ", mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
                int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (HwImsManagerInner.isDualImsAvailable() || HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue() == subId) {
                    if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                        int curSubForCarrier = intent.getIntExtra("subscription", 0);
                        if ((HwImsManagerInner.isDualImsAvailable() || subId == curSubForCarrier) && curSubForCarrier == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            HwRILReferenceImpl hwRILReferenceImpl = HwRILReferenceImpl.this;
                            if (intent.getIntExtra(HwRILReferenceImpl.CARRIER_CONFIG_CHANGE_STATE, 1) != 1) {
                                z = false;
                            }
                            hwRILReferenceImpl.mIsCarrierConfigLoaded = z;
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
                        if (!(HwImsManagerInner.isDualImsAvailable() || HwRILReferenceImpl.this.mRil == null)) {
                            HwRILReferenceImpl.this.handleUnsolicitedRadioStateChanged(HwRILReferenceImpl.this.mRil.getRadioState().isOn(), HwRILReferenceImpl.this.mContext);
                        }
                    } else if (HwRILReferenceImpl.IMS_SERVICE_STATE_CHANGED_ACTION.equals(action)) {
                        int curSubId = intent.getIntExtra("subId", -1);
                        HwRILReferenceImpl.this.riljLog("IMS_SERVICE_STATE_CHANGED_ACTION curSubId is : " + curSubId);
                        if (curSubId == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            HwRILReferenceImpl.this.mIsImsRegistered = HwRILReferenceImpl.IMS_STATE_REGISTERED.equals(intent.getStringExtra(HwRILReferenceImpl.IMS_STATE));
                            boolean isSupportVolte = HwRILReferenceImpl.FEATURE_HW_VOLTE_ON;
                            if (HwRILReferenceImpl.FEATURE_VOLTE_DYN && HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                                isSupportVolte = HwImsManagerInner.isVolteEnabledByPlatform(context, HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue());
                            } else if (HwRILReferenceImpl.FEATURE_VOLTE_DYN && (HwRILReferenceImpl.this.mIsCarrierConfigLoaded ^ 1) != 0) {
                                return;
                            }
                            HwRILReferenceImpl.this.riljLog("mIsImsRegistered is : " + HwRILReferenceImpl.this.mIsImsRegistered + " and isSupportVolte is : " + isSupportVolte);
                            if ((!HwRILReferenceImpl.FEATURE_VOLTE_DYN || HwRILReferenceImpl.this.mIsCarrierConfigLoaded) && HwRILReferenceImpl.this.mIsImsRegistered && (isSupportVolte ^ 1) != 0) {
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
    private Context mContext;
    protected Registrant mCurBandClassRegistrant;
    private HwCustRILReference mHwCustRILReference;
    private Integer mHwRilReferenceInstanceId;
    protected RegistrantList mIccUimLockRegistrants = new RegistrantList();
    private boolean mIsCarrierConfigLoaded = false;
    private boolean mIsImsRegistered = false;
    protected Registrant mMaxTxPowerRegistrant;
    private WorkSource mRILDefaultWorkSource;
    private RIL mRil;
    private String mcc_operator = null;
    private boolean shouldReportRoamingPlusInfo = true;

    public HwRILReferenceImpl(RIL ril) {
        this.mRil = ril;
        if (!(this.mRil == null || this.mRil.getContext() == null || this.mRil.getContext().getApplicationInfo() == null)) {
            this.mRILDefaultWorkSource = new WorkSource(this.mRil.getContext().getApplicationInfo().uid, this.mRil.getContext().getPackageName());
        }
        this.mHwCustRILReference = (HwCustRILReference) HwCustUtils.createObj(HwCustRILReference.class, new Object[0]);
        if (FEATURE_VOLTE_DYN && !HwModemCapability.isCapabilitySupport(9)) {
            IntentFilter filter = new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED");
            filter.addAction(IMS_SERVICE_STATE_CHANGED_ACTION);
            filter.addAction("com.huawei.ACTION_NETWORK_FACTORY_RESET");
            filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
            if (!(this.mRil == null || this.mRil.getContext() == null)) {
                riljLog("register receiver CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.");
                this.mRil.getContext().registerReceiver(this.mCarrierConfigListener, filter);
            }
        }
    }

    public void handleMessage(Message msg) {
        boolean z = true;
        if (msg != null) {
            switch (msg.what) {
                case 1:
                    Rlog.d(RILJ_LOG_TAG, "Event EVENT_GET_IMS_SWITCH_RESULT Received");
                    AsyncResult ar = msg.obj;
                    if (ar.exception == null) {
                        if (1 != ar.result[0]) {
                            z = false;
                        }
                        handleImsSwitch(z);
                        break;
                    }
                    Rlog.d(RILJ_LOG_TAG, "Get IMS switch failed!");
                    break;
                case 2:
                    Rlog.d(RILJ_LOG_TAG, "Event EVENT_SET_IMS_SWITCH_FAIL Received");
                    if (((AsyncResult) msg.obj).exception != null) {
                        saveSwitchStatusToDB(getImsSwitch() ^ 1);
                        break;
                    }
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
                System.putInt(this.mContext.getContentResolver(), HwImsManagerInner.isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[this.mHwRilReferenceInstanceId.intValue()] : HW_VOLTE_USER_SWITCH, on ? 1 : 0);
            } catch (NullPointerException e) {
                Rlog.e(RILJ_LOG_TAG, "saveSwitchStatusToDB NullPointerException");
            } catch (Exception e2) {
                Rlog.e(RILJ_LOG_TAG, "saveSwitchStatusToDB Exception");
            }
        }
    }

    private String requestToStringEx(int request) {
        return HwTelephonyBaseManagerImpl.getDefault().requestToStringEx(request);
    }

    public Object processSolicitedEx(int rilRequest, Parcel p) {
        switch (rilRequest) {
            case 518:
                return responseVoid(p);
            case 524:
                return responseVoid(p);
            case 531:
                return responseVoid(p);
            case 532:
                return responseInts(p);
            case 2017:
                return responseVoid(p);
            case 2072:
                return responseVoid(p);
            case 2073:
                return responseInts(p);
            case 2107:
                return responseVoid(p);
            case 2121:
                return responseVoid(p);
            default:
                Rlog.d(RILJ_LOG_TAG, "The Message is not processed in HwRILReferenceImpl");
                return null;
        }
    }

    public void iccGetATR(Message result) {
        invokeIRadio(136, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimATR(serial);
            }
        });
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mHwRilReferenceInstanceId != null ? " [SUB" + this.mHwRilReferenceInstanceId + "]" : ""));
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
        if (this.mHwRilReferenceInstanceId == null || !(HwImsManagerInner.isDualImsAvailable() || this.mHwRilReferenceInstanceId.intValue() == subId)) {
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
        if (this.mHwRilReferenceInstanceId == null || !(HwImsManagerInner.isDualImsAvailable() || this.mHwRilReferenceInstanceId.intValue() == HwTelephonyManagerInner.getDefault().getDefault4GSlotId())) {
            riljLog("current slot not support volte");
            return;
        }
        if (this.mContext != null && isSaveDB) {
            try {
                System.putInt(this.mContext.getContentResolver(), HwImsManagerInner.isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[this.mHwRilReferenceInstanceId.intValue()] : HW_VOLTE_USER_SWITCH, on ? 1 : 0);
            } catch (NullPointerException e) {
                Rlog.e(RILJ_LOG_TAG, "e = " + e);
            } catch (Exception ex) {
                Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
            }
        }
        riljLog("setImsSwitch -> imsstatte : " + on);
        invokeIRadio(2114, obtainMessage(2), new RILCommand() {
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
        if (!HwModemCapability.isCapabilitySupport(9)) {
            Rlog.d(RILJ_LOG_TAG, "hand radio state change and volte on is " + FEATURE_HW_VOLTE_ON);
            if ((!on || !FEATURE_HW_VOLTE_ON) && !FEATURE_HW_VOWIFI_ON) {
                Rlog.d(RILJ_LOG_TAG, "not to do, radio state is off");
            } else if (!FEATURE_VOLTE_DYN || (this.mIsCarrierConfigLoaded ^ 1) == 0) {
                boolean isSupportVolte = HwImsManagerInner.isVolteEnabledByPlatform(context, this.mHwRilReferenceInstanceId.intValue());
                if (!FEATURE_VOLTE_DYN || isSupportVolte || (this.mIsImsRegistered && (isSupportVolte ^ 1) != 0)) {
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
            int i;
            byte[] pduBytes = pdu.getBytes("ISO-8859-1");
            for (byte content : pduBytes) {
                Rlog.e(RILJ_LOG_TAG, "writeSmsToRuim pdu is" + content);
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pduBytes));
            msg.teleserviceId = dis.readInt();
            msg.isServicePresent = ((byte) dis.read()) == (byte) 1;
            msg.serviceCategory = dis.readInt();
            msg.address.digitMode = dis.readInt();
            msg.address.numberMode = dis.readInt();
            msg.address.numberType = dis.readInt();
            msg.address.numberPlan = dis.readInt();
            int addrNbrOfDigits = (byte) dis.read();
            for (i = 0; i < addrNbrOfDigits; i++) {
                msg.address.digits.add(Byte.valueOf((byte) dis.read()));
            }
            msg.subAddress.subaddressType = dis.readInt();
            msg.subAddress.odd = ((byte) dis.read()) == (byte) 1;
            int subaddrNbrOfDigits = (byte) dis.read();
            for (i = 0; i < subaddrNbrOfDigits; i++) {
                msg.subAddress.digits.add(Byte.valueOf((byte) dis.read()));
            }
            int bearerDataLength = dis.readInt();
            for (i = 0; i < bearerDataLength; i++) {
                msg.bearerData.add(Byte.valueOf((byte) dis.read()));
            }
        } catch (UnsupportedEncodingException ex) {
            riljLog("writeSmsToRuim: UnsupportedEncodingException: " + ex);
        } catch (IOException ex2) {
            riljLog("writeSmsToRuim: conversion from input stream to object failed: " + ex2);
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
            riljLog(rr.serialString() + "< " + "     RIL_REQUEST_GET_IMSI" + " xxxxx ");
            if (ret != null && (((String) ret).equals("") ^ 1) != 0) {
                String temp_mcc = ((String) ret).substring(0, 3);
                if (mMcc == null && temp_mcc != null && this.mcc_operator != null && (this.mcc_operator.equals(temp_mcc) ^ 1) != 0 && temp_mcc.equals("460") && this.shouldReportRoamingPlusInfo) {
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
        switch (request) {
            case 3001:
                return "UNSOL_HW_RESIDENT_NETWORK_CHANGED";
            case 3003:
                return "UNSOL_HW_CS_CHANNEL_INFO_IND";
            case 3005:
                return "UNSOL_HW_ECCNUM";
            case 3031:
                return "UNSOL_HW_XPASS_RESELECT_INFO";
            case 3034:
                return "UNSOL_HW_EXIST_NETWORK_INFO";
            default:
                return "<unknown response>:" + request;
        }
    }

    public IRadio getHuaweiRadioProxy() {
        if (this.mRil.mRadioProxy instanceof IRadio) {
            return (IRadio) this.mRil.mRadioProxy;
        }
        return null;
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

    public void riseCdmaCutoffFreq(final boolean on, Message response) {
        invokeIRadio(524, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setRiseCdmaCutoffFreq(serial, on);
            }
        });
    }

    public void supplyDepersonalization(final String netpin, final int type, Message result) {
        invokeIRadio(8, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio instanceof vendor.huawei.hardware.radio.V1_1.IRadio) {
                    ((vendor.huawei.hardware.radio.V1_1.IRadio) radio).supplyDepersonalization(serial, HwRILReferenceImpl.this.convertNullToEmptyString(netpin), type);
                } else if (radio instanceof IRadio) {
                    IRadio radio1_0 = radio;
                    radio.supplyNetworkDepersonalization(serial, HwRILReferenceImpl.this.convertNullToEmptyString(netpin));
                } else {
                    HwRILReferenceImpl.this.riljLog("not support below radio 1.0 and 1.1");
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

    public void getCdmaChrInfo(Message result) {
        invokeIRadio(532, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCdmaChrInfo(serial);
            }
        });
    }

    public void restartRild(Message result) {
        invokeIRadio(2005, result, new RILCommand() {
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
        invokeIRadio(2001, null, new RILCommand() {
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

    public void testVoiceLoopBack(final int mode) {
        invokeIRadio(531, null, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.testVoiceLoopback(serial, mode);
            }
        });
    }

    public void setHwRatCombineMode(final int combineMode, Message result) {
        invokeIRadio(2072, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setRatCombinePrio(serial, combineMode);
            }
        });
    }

    public void getHwRatCombineMode(Message result) {
        invokeIRadio(2073, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getRatCombinePrio(serial);
            }
        });
    }

    public void setHwRFChannelSwitch(final int rfChannel, Message result) {
        invokeIRadio(2107, result, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setHwRFChannelSwitch(serial, rfChannel);
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
        Map<String, String> map = new HashMap();
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
        switch (type) {
            case 1:
                removedRegistrant = this.mCurBandClassRegistrant;
                break;
            case 2:
                removedRegistrant = this.mAntStateRegistrant;
                break;
            case 4:
                removedRegistrant = this.mMaxTxPowerRegistrant;
                break;
        }
        if (removedRegistrant == null || result == null || removedRegistrant.getHandler() != result.getTarget()) {
            return false;
        }
        removedRegistrant.clear();
        return true;
    }

    public boolean registerSarRegistrant(int type, Message result) {
        boolean isSuccess = false;
        if (result == null) {
            riljLog("registerSarRegistrant the param result is null");
            return false;
        }
        switch (type) {
            case 1:
                this.mCurBandClassRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                isSuccess = true;
                break;
            case 2:
                this.mAntStateRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                isSuccess = true;
                break;
            case 4:
                this.mMaxTxPowerRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                isSuccess = true;
                break;
        }
        riljLog("registerSarRegistrant type = " + type + ",isSuccess = " + (isSuccess ^ 1));
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
        switch (type) {
            case 2:
                if (this.mAntStateRegistrant != null) {
                    this.mAntStateRegistrant.notifyResult(resultData.array());
                    isSuccess = true;
                    break;
                }
                break;
            case 4:
                if (this.mMaxTxPowerRegistrant != null) {
                    this.mMaxTxPowerRegistrant.notifyResult(resultData.array());
                    isSuccess = true;
                    break;
                }
                break;
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
        int dataSize = length + 5;
        ByteBuffer buf = ByteBuffer.wrap(new byte[(("QOEMHOOK".length() + 8) + dataSize)]);
        try {
            buf.order(ByteOrder.nativeOrder());
            buf.put("QOEMHOOK".getBytes("UTF-8"));
            buf.putInt(598043);
            buf.putInt(dataSize);
            buf.putInt(event);
            buf.put((byte) length);
            if (length > 0 && HWBuffer.BUFFER_SIZE >= length) {
                buf.put(reqData);
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

    public void iccOpenLogicalChannel(final String AID, final byte p2, Message response) {
        invokeIRadio(536, response, new RILCommand() {
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.openChannelWithP2(serial, AID, "" + p2);
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x003d A:{Splitter: B:3:0x0037, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x003d, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x003e, code:
            r3 = r6.mRil;
            r4 = r6.mRil;
            r3.handleRadioProxyExceptionForRREx(com.android.internal.telephony.RIL.requestToString(r7), r0, r2);
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void invokeIRadio(int requestId, Message result, RILCommand cmd) {
        RILRequest rr = RILRequest.obtain(requestId, result, this.mRILDefaultWorkSource);
        StringBuilder append = new StringBuilder().append(rr.serialString()).append("> ");
        RIL ril = this.mRil;
        riljLog(append.append(RIL.requestToString(requestId)).toString());
        IRadio radioProxy = getHuaweiRadioProxy();
        if (radioProxy != null) {
            this.mRil.addRequestEx(rr);
            try {
                cmd.excute(radioProxy, rr.mSerial);
            } catch (Exception e) {
            }
        } else {
            rr.onError(1, null);
            rr.release();
        }
    }

    /* JADX WARNING: Missing block: B:16:0x006b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void existNetworkInfo(String state) {
        unsljLog(3034);
        if (state == null || state.length() < 3) {
            Rlog.d(RILJ_LOG_TAG, "plmn para error! break");
            return;
        }
        this.mcc_operator = state.substring(0, 3);
        Rlog.d(RILJ_LOG_TAG, "recieved RIL_UNSOL_HW_EXIST_NETWORK_INFO with mcc_operator =" + this.mcc_operator + "and mMcc =" + mMcc);
        if ((this.mcc_operator == null || !this.mcc_operator.equals(mMcc)) && ((mMcc == null || (mMcc.equals("460") ^ 1) == 0) && mMcc != null && this.shouldReportRoamingPlusInfo)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
            intent.putExtra("current_mcc", this.mcc_operator);
            this.existNetworkContext.sendBroadcast(intent);
            Rlog.d(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mcc_operator=" + this.mcc_operator);
            this.shouldReportRoamingPlusInfo = false;
        }
    }

    private String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }
}
