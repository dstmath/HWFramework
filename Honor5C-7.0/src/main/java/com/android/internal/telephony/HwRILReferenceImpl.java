package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.AbstractRIL.HwRILReference;
import com.android.internal.telephony.HwCallManagerReference.HWBuffer;
import com.android.internal.telephony.uicc.IccIoResult;
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

public class HwRILReferenceImpl implements HwRILReference {
    private static final String ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS = "android.intent.action.HW_EXIST_NETWORK_INFO";
    private static final String ACTION_HW_XPASS_RESELECT_INFO = "android.intent.action.HW_XPASS_RESELECT_INFO";
    private static final String ACTION_IMS_SWITCH_STATE_CHANGE = "com.huawei.ACTION_IMS_SWITCH_STATE_CHANGE";
    private static final int BYTE_SIZE = 1;
    private static final boolean FEATURE_SHOW_VOLTE_SWITCH = false;
    private static final boolean FEATURE_VOLTE_DYN = false;
    private static final int HW_ANTENNA_STATE_TYPE = 2;
    private static final int HW_BAND_CLASS_TYPE = 1;
    private static final int HW_MAX_TX_POWER_TYPE = 4;
    private static final String HW_ONLY_REMEBER_VOLTE_USER_SWITCH = "hw_only_remeber_volte_user_switch";
    private static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final int INT_SIZE = 4;
    private static final int LAST_VOLTE_SWITCH_OFF = 0;
    private static final int MODEM_IMS_SWITCH_ON = 1;
    private static final int NO_NEED_REMBER_VOLTE_SWITCH = 1;
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwRILReferenceImpl";
    private static final int RIL_MAX_COMMAND_BYTES = 8192;
    static int countAfterBoot;
    protected Registrant mAntStateRegistrant;
    private final BroadcastReceiver mCarrierConfigListener;
    private Context mContext;
    protected Registrant mCurBandClassRegistrant;
    private HwCustRILReference mHwCustRILReference;
    private Integer mHwRilReferenceInstanceId;
    protected RegistrantList mIccUimLockRegistrants;
    private boolean mImsState;
    protected Registrant mMaxTxPowerRegistrant;
    private String mMcc;
    private RIL mRil;
    private String mcc_operator;
    private boolean shouldReportRoamingPlusInfo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwRILReferenceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwRILReferenceImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwRILReferenceImpl.<clinit>():void");
    }

    public HwRILReferenceImpl(RIL ril) {
        this.mcc_operator = null;
        this.mImsState = FEATURE_VOLTE_DYN;
        this.mMcc = null;
        this.shouldReportRoamingPlusInfo = RILJ_LOGV;
        this.mIccUimLockRegistrants = new RegistrantList();
        this.mCarrierConfigListener = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    HwRILReferenceImpl.this.riljLog("receive event: action=" + action + ", mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
                    int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    if (HwRILReferenceImpl.this.mHwRilReferenceInstanceId == null || HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue() == subId) {
                        if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                            if (subId != intent.getIntExtra("subscription", HwRILReferenceImpl.LAST_VOLTE_SWITCH_OFF)) {
                                HwRILReferenceImpl.this.riljLog("getDefault4GSlotId do not match subId from intent.");
                                return;
                            }
                            HwRILReferenceImpl.this.riljLog("handle event: CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.");
                            if (HwRILReferenceImpl.this.mRil != null) {
                                HwRILReferenceImpl.this.handleUnsolicitedRadioStateChanged(HwRILReferenceImpl.this.mRil.getRadioState().isOn(), HwRILReferenceImpl.this.mContext);
                            } else {
                                HwRILReferenceImpl.this.riljLog("mRil is null.");
                            }
                        }
                        return;
                    }
                    HwRILReferenceImpl.this.riljLog("getDefault4GSlotId do not match mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
                }
            }
        };
        this.mRil = ril;
        this.mHwCustRILReference = (HwCustRILReference) HwCustUtils.createObj(HwCustRILReference.class, new Object[LAST_VOLTE_SWITCH_OFF]);
        if (FEATURE_VOLTE_DYN && !HwModemCapability.isCapabilitySupport(9)) {
            IntentFilter filter = new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED");
            if (!(this.mRil == null || this.mRil.getContext() == null)) {
                riljLog("register receiver CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.");
                this.mRil.getContext().registerReceiver(this.mCarrierConfigListener, filter);
            }
        }
    }

    private String requestToStringEx(int request) {
        return HwTelephonyBaseManagerImpl.getDefault().requestToStringEx(request);
    }

    public Object processSolicitedEx(int rilRequest, Parcel p) {
        boolean z = RILJ_LOGV;
        switch (rilRequest) {
            case 115:
                return responseInts(p);
            case 116:
                return responseVoid(p);
            case 136:
                return responseString(p);
            case 501:
                return responseICC_IO(p);
            case 504:
                return responseICC_IO(p);
            case 518:
                return responseVoid(p);
            case 522:
                return responseVoid(p);
            case 524:
                return responseVoid(p);
            case 528:
                return responseInts(p);
            case 529:
                return responseString(p);
            case 531:
                return responseVoid(p);
            case 532:
                return responseInts(p);
            case 533:
                return responseInts(p);
            case 535:
                return responseVoid(p);
            case 2001:
                return responseVoid(p);
            case 2005:
                return responseVoid(p);
            case 2011:
                return responseVoid(p);
            case 2012:
                return responseVoid(p);
            case 2015:
                return responseInts(p);
            case 2017:
                return responseVoid(p);
            case 2028:
                return responseVoid(p);
            case 2064:
                return responseInts(p);
            case 2065:
                return responseNetworkInfoWithActs(p);
            case 2066:
                return responseVoid(p);
            case 2072:
                return responseVoid(p);
            case 2073:
                return responseInts(p);
            case 2099:
                return responseVoid(p);
            case 2107:
                return responseVoid(p);
            case 2114:
                return responseVoid(p);
            case 2115:
                boolean z2;
                Object ret = responseInts(p);
                int[] buf = (int[]) ret;
                StringBuilder append = new StringBuilder().append("RIL_REQUEST_HW_GET_IMS_SWITCH, ap.ImsState = ").append(this.mImsState).append("; modem.ImsState = ");
                if (buf[LAST_VOLTE_SWITCH_OFF] == NO_NEED_REMBER_VOLTE_SWITCH) {
                    z2 = RILJ_LOGV;
                } else {
                    z2 = FEATURE_VOLTE_DYN;
                }
                riljLog(append.append(z2).append("; FEATURE_VOLTE_DYN=").append(FEATURE_VOLTE_DYN).toString());
                z2 = this.mImsState;
                if (NO_NEED_REMBER_VOLTE_SWITCH != buf[LAST_VOLTE_SWITCH_OFF]) {
                    z = FEATURE_VOLTE_DYN;
                }
                handleImsSwitch(z2, z);
                return ret;
            case 2118:
                return responseVoid(p);
            case 2121:
                return responseVoid(p);
            case 2127:
                return responseInts(p);
            default:
                Rlog.d(RILJ_LOG_TAG, "The Message is not processed in HwRILReferenceImpl");
                return null;
        }
    }

    public void iccExchangeAPDU(int cla, int command, int channel, int p1, int p2, int p3, String data, Message result) {
        RILRequestReference rr;
        if (channel == 0) {
            rr = RILRequestReference.obtain(501, result);
        } else {
            rr = RILRequestReference.obtain(504, result);
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
        riljLog(rr.serialString() + "> iccExchangeAPDU: " + requestToStringEx(rr.getRequest()) + " 0x" + Integer.toHexString(cla) + " 0x" + Integer.toHexString(command) + " 0x" + Integer.toHexString(channel) + " " + p1 + "," + p2 + "," + p3);
        this.mRil.send(rr);
    }

    public void iccOpenChannel(String AID, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(115, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeString(AID);
        riljLog(rr.serialString() + "> iccOpenChannel: " + requestToStringEx(rr.getRequest()) + " " + AID);
        this.mRil.send(rr);
    }

    public void iccCloseChannel(int channel, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(116, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(channel);
        riljLog(rr.serialString() + "> iccCloseChannel: " + requestToStringEx(rr.getRequest()) + " " + channel);
        this.mRil.send(rr);
    }

    public void iccGetATR(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(136, result);
        int slotId = this.mHwRilReferenceInstanceId != null ? this.mHwRilReferenceInstanceId.intValue() : LAST_VOLTE_SWITCH_OFF;
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(slotId);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    private void riljLog(String msg) {
        Rlog.d(RILJ_LOG_TAG, msg + (this.mHwRilReferenceInstanceId != null ? " [SUB" + this.mHwRilReferenceInstanceId + "]" : ""));
    }

    private Object responseICC_IO(Parcel p) {
        int sw1 = p.readInt();
        int sw2 = p.readInt();
        String s = p.readString();
        riljLog("< iccIO:  0x" + Integer.toHexString(sw1) + " 0x" + Integer.toHexString(sw2) + " " + s);
        return new IccIoResult(sw1, sw2, s);
    }

    private Object responseInts(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[numInts];
        for (int i = LAST_VOLTE_SWITCH_OFF; i < numInts; i += NO_NEED_REMBER_VOLTE_SWITCH) {
            response[i] = p.readInt();
        }
        return response;
    }

    private Object responseVoid(Parcel p) {
        return null;
    }

    private Object responseString(Parcel p) {
        return p.readString();
    }

    private void handleImsSwitch(boolean apImsStatus, boolean modemImsStatus) {
        if (this.mContext == null) {
            riljLog("handleImsSwitch, mContext is null.nothing to do");
            return;
        }
        int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (this.mHwRilReferenceInstanceId == null || this.mHwRilReferenceInstanceId.intValue() == subId) {
            boolean needSyncImsSwitch = RILJ_LOGV;
            boolean isCardSupVolte = isCarrierSupportVolte(subId);
            if (FEATURE_VOLTE_DYN && !isCardSupVolte) {
                needSyncImsSwitch = FEATURE_VOLTE_DYN;
            }
            if (needSyncImsSwitch) {
                boolean switchShow = cardSwitchShow(subId, isCardSupVolte);
                riljLog("handleImsSwitch: switchShow " + switchShow + " apImsStatus " + apImsStatus + " modemImsStatus " + modemImsStatus);
                setImsBySwitchShown(apImsStatus, modemImsStatus, switchShow);
            }
            sendBroadCastToIms(this.mImsState);
            return;
        }
        riljLog("getDefault4GSlotId do not match mHwRilReferenceInstanceId=" + this.mHwRilReferenceInstanceId);
    }

    private void setImsBySwitchShown(boolean apImsStatus, boolean modemImsStatus, boolean switchShow) {
        boolean z = FEATURE_VOLTE_DYN;
        if (switchShow) {
            if (this.mContext != null) {
                boolean lastApImsStatusOff;
                if (System.getInt(this.mContext.getContentResolver(), HW_ONLY_REMEBER_VOLTE_USER_SWITCH, NO_NEED_REMBER_VOLTE_SWITCH) == 0) {
                    lastApImsStatusOff = RILJ_LOGV;
                } else {
                    lastApImsStatusOff = FEATURE_VOLTE_DYN;
                }
                if (lastApImsStatusOff) {
                    apImsStatus = FEATURE_VOLTE_DYN;
                    System.putInt(this.mContext.getContentResolver(), HW_ONLY_REMEBER_VOLTE_USER_SWITCH, NO_NEED_REMBER_VOLTE_SWITCH);
                }
            }
            if (apImsStatus != modemImsStatus) {
                setImsSwitch(apImsStatus);
            }
        } else if (!modemImsStatus) {
            if (!(this.mContext == null || apImsStatus)) {
                System.putInt(this.mContext.getContentResolver(), HW_ONLY_REMEBER_VOLTE_USER_SWITCH, LAST_VOLTE_SWITCH_OFF);
            }
            if (!modemImsStatus) {
                z = RILJ_LOGV;
            }
            setImsSwitch(z);
        }
    }

    private boolean isCarrierSupportVolte(int subId) {
        Boolean cardSupportVolte = getCarrierConfigValue(subId, "carrier_volte_available_bool");
        if (cardSupportVolte != null) {
            return cardSupportVolte.booleanValue();
        }
        return FEATURE_VOLTE_DYN;
    }

    private Boolean cardCarrierShowVolteSwitch(int subId) {
        return getCarrierConfigValue(subId, "carrier_volte_show_switch_bool");
    }

    private Boolean getCarrierConfigValue(int subId, String keyCarrierConfig) {
        CarrierConfigManager cfgMgr = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (cfgMgr == null) {
            return null;
        }
        PersistableBundle b = cfgMgr.getConfigForSubId(subId);
        if (b != null) {
            return (Boolean) b.get(keyCarrierConfig);
        }
        return null;
    }

    private boolean cardSwitchShow(int subId, boolean isCardSupVolte) {
        if (!FEATURE_VOLTE_DYN) {
            return FEATURE_SHOW_VOLTE_SWITCH;
        }
        if (!isCardSupVolte) {
            return FEATURE_VOLTE_DYN;
        }
        Boolean voSwitchShow = cardCarrierShowVolteSwitch(subId);
        if (voSwitchShow != null) {
            return voSwitchShow.booleanValue();
        }
        return FEATURE_SHOW_VOLTE_SWITCH;
    }

    public void setImsSwitch(boolean on) {
        int i = NO_NEED_REMBER_VOLTE_SWITCH;
        if (this.mHwRilReferenceInstanceId == null || this.mHwRilReferenceInstanceId.intValue() == HwTelephonyManagerInner.getDefault().getDefault4GSlotId()) {
            this.mImsState = on;
            if (this.mContext != null) {
                try {
                    int i2;
                    ContentResolver contentResolver = this.mContext.getContentResolver();
                    String str = HW_VOLTE_USER_SWITCH;
                    if (on) {
                        i2 = NO_NEED_REMBER_VOLTE_SWITCH;
                    } else {
                        i2 = LAST_VOLTE_SWITCH_OFF;
                    }
                    System.putInt(contentResolver, str, i2);
                } catch (NullPointerException e) {
                    Rlog.e(RILJ_LOG_TAG, "e = " + e);
                } catch (Exception ex) {
                    Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
                }
            }
            RILRequestReference rr = RILRequestReference.obtain(2114, null);
            rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
            Parcel parcel = rr.getParcel();
            if (!on) {
                i = LAST_VOLTE_SWITCH_OFF;
            }
            parcel.writeInt(i);
            riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + " : " + this.mImsState);
            this.mRil.send(rr);
            return;
        }
        riljLog("current slot not support volte");
    }

    public boolean getImsSwitch() {
        return this.mImsState;
    }

    public void handleUnsolicitedRadioStateChanged(boolean on, Context context) {
        boolean z = RILJ_LOGV;
        Rlog.d(RILJ_LOG_TAG, "handleUnsolicitedRadioStateChanged: state on =  " + on);
        this.mContext = context;
        try {
            if (System.getInt(this.mContext.getContentResolver(), HW_VOLTE_USER_SWITCH, LAST_VOLTE_SWITCH_OFF) != NO_NEED_REMBER_VOLTE_SWITCH) {
                z = FEATURE_VOLTE_DYN;
            }
            this.mImsState = z;
        } catch (NullPointerException e) {
            Rlog.e(RILJ_LOG_TAG, "e = " + e);
        } catch (Exception ex) {
            Rlog.e(RILJ_LOG_TAG, "ex = " + ex);
        }
        if (!HwModemCapability.isCapabilitySupport(9)) {
            boolean vowifiOn = SystemProperties.getBoolean("ro.config.hw_vowifi", FEATURE_VOLTE_DYN);
            if (on || vowifiOn) {
                getModemImsSwitch(null);
            } else {
                Rlog.d(RILJ_LOG_TAG, "not to do, radio state is off");
            }
        }
    }

    public void getModemImsSwitch(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2115, result);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    private void sendBroadCastToIms(boolean imsSwitchOn) {
        Rlog.d(RILJ_LOG_TAG, "sendBroadCastToIms, imsSwitchOn is: " + imsSwitchOn);
        Intent intent = new Intent();
        intent.setAction(ACTION_IMS_SWITCH_STATE_CHANGE);
        if (this.mContext != null) {
            this.mContext.sendBroadcast(intent);
        }
    }

    private Object responseNetworkInfoWithActs(Parcel p) {
        int numInts = p.readInt();
        int[] response = new int[(numInts * 6)];
        riljLog("dwj responseNetworkInfoWithActs  numInts:" + numInts);
        for (int i = LAST_VOLTE_SWITCH_OFF; i < numInts * 6; i += NO_NEED_REMBER_VOLTE_SWITCH) {
            response[i] = p.readInt();
            riljLog("dwj responseNetworkInfoWithActs  response[" + i + "]:" + response[i]);
        }
        return response;
    }

    public void getPOLCapabilty(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2064, response);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void getCurrentPOLList(Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2065, response);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void setPOLEntry(int index, String numeric, int nAct, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2066, response);
        if (numeric == null || numeric.length() == 0) {
            rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
            rr.getParcel().writeString(Integer.toString(index));
        } else {
            rr.getParcel().writeInt(3);
            rr.getParcel().writeString(Integer.toString(index));
            rr.getParcel().writeString(numeric);
            rr.getParcel().writeString(Integer.toString(nAct));
        }
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void writeContent(RILRequestReference rr, String pdu) {
        try {
            int i;
            byte[] pduBytes = pdu.getBytes("ISO-8859-1");
            int length = pduBytes.length;
            for (int i2 = LAST_VOLTE_SWITCH_OFF; i2 < length; i2 += NO_NEED_REMBER_VOLTE_SWITCH) {
                Rlog.e(RILJ_LOG_TAG, "writeSmsToRuim pdu is" + pduBytes[i2]);
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(pdu.getBytes("ISO-8859-1")));
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeByte((byte) dis.read());
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeByte((byte) dis.read());
            for (i = LAST_VOLTE_SWITCH_OFF; i < 36; i += NO_NEED_REMBER_VOLTE_SWITCH) {
                rr.getParcel().writeByte((byte) dis.read());
            }
            rr.getParcel().writeInt(dis.readInt());
            rr.getParcel().writeByte((byte) dis.read());
            rr.getParcel().writeByte((byte) dis.read());
            for (i = LAST_VOLTE_SWITCH_OFF; i < 36; i += NO_NEED_REMBER_VOLTE_SWITCH) {
                rr.getParcel().writeByte((byte) dis.read());
            }
            rr.getParcel().writeInt(dis.readInt());
            for (i = LAST_VOLTE_SWITCH_OFF; i < HwSubscriptionManager.SUB_INIT_STATE; i += NO_NEED_REMBER_VOLTE_SWITCH) {
                rr.getParcel().writeByte((byte) dis.read());
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
            this.shouldReportRoamingPlusInfo = RILJ_LOGV;
        }
    }

    public void handleRequestGetImsiMessage(RILRequest rr, Object ret, Context context) {
        if (rr.mRequest == 11) {
            riljLog(rr.serialString() + "< " + "     RIL_REQUEST_GET_IMSI" + " xxxxx ");
            if (ret != null && !((String) ret).equals("")) {
                String temp_mcc = ((String) ret).substring(LAST_VOLTE_SWITCH_OFF, 3);
                if (this.mMcc == null && temp_mcc != null && this.mcc_operator != null && !this.mcc_operator.equals(temp_mcc) && temp_mcc.equals("460") && this.shouldReportRoamingPlusInfo) {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
                    intent.putExtra("current_mcc", this.mcc_operator);
                    context.sendBroadcast(intent);
                    Rlog.d(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mcc=" + this.mcc_operator + "when handleRequestGetImsiMessage");
                    this.shouldReportRoamingPlusInfo = FEATURE_VOLTE_DYN;
                }
                this.mMcc = temp_mcc;
                riljLog(" mMcc = " + this.mMcc);
            }
        }
    }

    public Object handleUnsolicitedDefaultMessagePara(int response, Parcel p) {
        switch (response) {
            case 3001:
                return responseString(p);
            case 3003:
                return responseInts(p);
            case 3005:
                return responseString(p);
            case 3031:
                return responseInts(p);
            case 3034:
                return responseString(p);
            default:
                return null;
        }
    }

    public void handleUnsolicitedDefaultMessage(int response, Object ret, Context context) {
        Intent intent;
        switch (response) {
            case 3001:
                unsljLog(response);
                if (this.mRil.mUnsolRplmnsStateRegistrant != null) {
                    this.mRil.mUnsolRplmnsStateRegistrant.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3003:
                unsljLog(response);
                if (this.mRil.mSpeechInfoRegistrants != null) {
                    Rlog.d(RILJ_LOG_TAG, "RIL.java is ready for submitting SPEECHINFO");
                    this.mRil.mSpeechInfoRegistrants.notifyRegistrants(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3005:
                unsljLog(response);
                if (this.mRil.mECCNumRegistrant != null) {
                    this.mRil.mECCNumRegistrant.notifyRegistrant(new AsyncResult(null, ret, null));
                    break;
                }
                break;
            case 3031:
                unsljLog(response);
                int[] result_temp = (int[]) ret;
                Rlog.d(RILJ_LOG_TAG, "recieved RIL_UNSOL_HW_XPASS_RESELECT_INFO with result_temp");
                if (result_temp != null) {
                    Rlog.d(RILJ_LOG_TAG, "result_temp[0]=" + result_temp[LAST_VOLTE_SWITCH_OFF] + "   ,result_temp[1]=" + result_temp[NO_NEED_REMBER_VOLTE_SWITCH]);
                }
                if (countAfterBoot == 0) {
                    Rlog.d(RILJ_LOG_TAG, "countAfterBoot =" + countAfterBoot);
                    if (result_temp != null && (result_temp[LAST_VOLTE_SWITCH_OFF] == NO_NEED_REMBER_VOLTE_SWITCH || result_temp[NO_NEED_REMBER_VOLTE_SWITCH] == NO_NEED_REMBER_VOLTE_SWITCH)) {
                        intent = new Intent();
                        intent.setAction(ACTION_HW_XPASS_RESELECT_INFO);
                        context.sendBroadcast(intent);
                        Rlog.d(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_XPASS_RESELECT_INFO");
                        countAfterBoot = NO_NEED_REMBER_VOLTE_SWITCH;
                        break;
                    }
                }
                break;
            case 3034:
                unsljLog(response);
                if (ret != null && ((String) ret).length() >= 3) {
                    this.mcc_operator = ((String) ret).substring(LAST_VOLTE_SWITCH_OFF, 3);
                    Rlog.d(RILJ_LOG_TAG, "recieved RIL_UNSOL_HW_EXIST_NETWORK_INFO with mcc_operator =" + this.mcc_operator + "and mMcc =" + this.mMcc);
                    if ((this.mcc_operator == null || !this.mcc_operator.equals(this.mMcc)) && ((this.mMcc == null || this.mMcc.equals("460")) && this.mMcc != null && this.shouldReportRoamingPlusInfo)) {
                        intent = new Intent();
                        intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
                        intent.putExtra("current_mcc", this.mcc_operator);
                        context.sendBroadcast(intent);
                        Rlog.d(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mcc_operator=" + this.mcc_operator);
                        this.shouldReportRoamingPlusInfo = FEATURE_VOLTE_DYN;
                        break;
                    }
                }
                Rlog.d(RILJ_LOG_TAG, "plmn para error! break");
                break;
                break;
            default:
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

    public void setPowerGrade(int powerGrade, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(518, response);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeString(Integer.toString(powerGrade));
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + ": " + powerGrade);
        this.mRil.send(rr);
    }

    public void setWifiTxPowerGrade(int powerGrade, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(535, response);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeString(Integer.toString(powerGrade));
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + ": " + powerGrade);
        this.mRil.send(rr);
    }

    public void riseCdmaCutoffFreq(boolean on, Message response) {
        int i = NO_NEED_REMBER_VOLTE_SWITCH;
        RILRequestReference rr = RILRequestReference.obtain(524, response);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        Parcel parcel = rr.getParcel();
        if (!on) {
            i = LAST_VOLTE_SWITCH_OFF;
        }
        parcel.writeInt(i);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + ": " + on);
        this.mRil.send(rr);
    }

    public void supplyDepersonalization(String netpin, int type, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(8, result);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + " Type:" + type);
        rr.getParcel().writeInt(type);
        rr.getParcel().writeString(netpin);
        this.mRil.send(rr);
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

    public void sendSMSSetLong(int flag, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2015, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(flag);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + "flag " + flag);
        this.mRil.send(rr);
    }

    public void dataConnectionDetach(int mode, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2011, response);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void dataConnectionAttach(int mode, Message response) {
        RILRequestReference rr = RILRequestReference.obtain(2012, response);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void getCdmaChrInfo(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(532, result);
        riljLog(rr.serialString() + "> getCdmaChrInfo: " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void restartRild(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2005, result);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    public void requestSetEmergencyNumbers(String ecclist_withcard, String ecclist_nocard) {
        riljLog("setEmergencyNumbers()");
        RILRequestReference rr = RILRequestReference.obtain(2001, null);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        rr.getParcel().writeInt(HW_ANTENNA_STATE_TYPE);
        rr.getParcel().writeString(ecclist_withcard);
        rr.getParcel().writeString(ecclist_nocard);
        this.mRil.send(rr);
    }

    public void queryEmergencyNumbers() {
        riljLog("queryEmergencyNumbers()");
        RILRequestReference rr = RILRequestReference.obtain(522, null);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void getCdmaGsmImsi(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(529, result);
        riljLog(rr.serialString() + "> getCdmaGsmIMSI: " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void testVoiceLoopBack(int mode) {
        RILRequestReference rr = RILRequestReference.obtain(531, null);
        riljLog("testVoiceLoopBack: " + mode);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(mode);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void setHwRatCombineMode(int combineMode, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2072, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(combineMode);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void getHwRatCombineMode(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2073, result);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void setHwRFChannelSwitch(int rfChannel, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2107, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(rfChannel);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void getCdmaModeSide(Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2127, result);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void setCdmaModeSide(int modemID, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2118, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(modemID);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public void setVpMask(int vpMask, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2099, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(vpMask);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()) + " {" + vpMask + "}");
        this.mRil.send(rr);
    }

    public void resetAllConnections() {
        RILRequestReference rr = RILRequestReference.obtain(2017, null);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public Map<String, String> correctApnAuth(String username, int authType, String password) {
        if (this.mHwCustRILReference != null && this.mHwCustRILReference.isCustCorrectApnAuthOn()) {
            return this.mHwCustRILReference.custCorrectApnAuth(username, authType, password);
        }
        Map<String, String> map = new HashMap();
        if (authType == NO_NEED_REMBER_VOLTE_SWITCH) {
            if (TextUtils.isEmpty(username)) {
                authType = LAST_VOLTE_SWITCH_OFF;
                password = "";
                riljLog("authType is pap but username is null, clear all");
            }
        } else if (authType == HW_ANTENNA_STATE_TYPE) {
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                authType = LAST_VOLTE_SWITCH_OFF;
                username = "";
                password = "";
                riljLog("authType is chap but username or password is null, clear all");
            }
        } else if (authType == 3) {
            if (TextUtils.isEmpty(username)) {
                authType = LAST_VOLTE_SWITCH_OFF;
                password = "";
                riljLog("authType is pap_chap but username is null, clear all");
            }
            if (!TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
                authType = NO_NEED_REMBER_VOLTE_SWITCH;
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

    public void notifyCModemStatus(int state, Message result) {
        RILRequestReference rr = RILRequestReference.obtain(2121, result);
        rr.getParcel().writeInt(NO_NEED_REMBER_VOLTE_SWITCH);
        rr.getParcel().writeInt(state);
        riljLog(rr.serialString() + "> " + requestToStringEx(rr.getRequest()));
        this.mRil.send(rr);
    }

    public boolean unregisterSarRegistrant(int type, Message result) {
        Registrant removedRegistrant = null;
        riljLog("unregisterSarRegistrant start");
        switch (type) {
            case NO_NEED_REMBER_VOLTE_SWITCH /*1*/:
                removedRegistrant = this.mCurBandClassRegistrant;
                break;
            case HW_ANTENNA_STATE_TYPE /*2*/:
                removedRegistrant = this.mAntStateRegistrant;
                break;
            case INT_SIZE /*4*/:
                removedRegistrant = this.mMaxTxPowerRegistrant;
                break;
        }
        if (removedRegistrant == null || result == null || removedRegistrant.getHandler() != result.getTarget()) {
            return FEATURE_VOLTE_DYN;
        }
        removedRegistrant.clear();
        return RILJ_LOGV;
    }

    public boolean registerSarRegistrant(int type, Message result) {
        boolean z = FEATURE_VOLTE_DYN;
        boolean isSuccess = FEATURE_VOLTE_DYN;
        if (result == null) {
            riljLog("registerSarRegistrant the param result is null");
            return FEATURE_VOLTE_DYN;
        }
        switch (type) {
            case NO_NEED_REMBER_VOLTE_SWITCH /*1*/:
                this.mCurBandClassRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                isSuccess = RILJ_LOGV;
                break;
            case HW_ANTENNA_STATE_TYPE /*2*/:
                this.mAntStateRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                isSuccess = RILJ_LOGV;
                break;
            case INT_SIZE /*4*/:
                this.mMaxTxPowerRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
                isSuccess = RILJ_LOGV;
                break;
        }
        StringBuilder append = new StringBuilder().append("registerSarRegistrant type = ").append(type).append(",isSuccess = ");
        if (!isSuccess) {
            z = RILJ_LOGV;
        }
        riljLog(append.append(z).toString());
        return isSuccess;
    }

    public void notifyAntOrMaxTxPowerInfo(byte[] data) {
        ByteBuffer payload = ByteBuffer.wrap(data);
        payload.order(ByteOrder.nativeOrder());
        int type_id = payload.get();
        riljLog("type_id in notifyAntOrMaxTxPowerInfo is " + type_id);
        int response_size = payload.getShort();
        if (response_size < 0 || response_size > RIL_MAX_COMMAND_BYTES) {
            riljLog("Response Size is Invalid " + response_size);
            return;
        }
        int result = payload.getInt();
        riljLog("notifyAntOrMaxTxPowerInfo result=" + result);
        ByteBuffer resultData = ByteBuffer.allocate(INT_SIZE);
        resultData.order(ByteOrder.nativeOrder());
        resultData.putInt(result);
        notifyResultByType(type_id, resultData);
    }

    public void notifyBandClassInfo(byte[] data) {
        ByteBuffer payload = ByteBuffer.wrap(data);
        payload.order(ByteOrder.nativeOrder());
        int activeBand = payload.getInt();
        riljLog("notifyBandClassInfo activeBand=" + activeBand);
        ByteBuffer resultData = ByteBuffer.allocate(INT_SIZE);
        resultData.order(ByteOrder.nativeOrder());
        resultData.putInt(activeBand);
        if (this.mCurBandClassRegistrant != null) {
            this.mCurBandClassRegistrant.notifyResult(resultData.array());
        }
    }

    private void notifyResultByType(int type, ByteBuffer resultData) {
        boolean isSuccess = FEATURE_VOLTE_DYN;
        riljLog("notifyResultByType start");
        switch (type) {
            case HW_ANTENNA_STATE_TYPE /*2*/:
                if (this.mAntStateRegistrant != null) {
                    this.mAntStateRegistrant.notifyResult(resultData.array());
                    isSuccess = RILJ_LOGV;
                    break;
                }
                break;
            case INT_SIZE /*4*/:
                if (this.mMaxTxPowerRegistrant != null) {
                    this.mMaxTxPowerRegistrant.notifyResult(resultData.array());
                    isSuccess = RILJ_LOGV;
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
        int length = reqData == null ? LAST_VOLTE_SWITCH_OFF : reqData.length;
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
}
