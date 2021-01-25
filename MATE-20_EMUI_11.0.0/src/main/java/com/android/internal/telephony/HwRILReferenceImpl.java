package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.radio.V1_0.CdmaSmsMessage;
import android.hardware.radio.V1_0.CdmaSmsSubaddress;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.data.DataCallResponse;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.AbstractRIL;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.android.telephony.SubscriptionManagerEx;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import vendor.huawei.hardware.radio.V2_0.IRadio;
import vendor.huawei.hardware.radio.V2_1.HwSignalStrength_2_1;
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
    private static final int EVENT_SET_NETWORK_AUTO_RESULT = 4;
    private static final int EVENT_UNSOL_SIM_NVCFG_FINISHED = 3;
    private static final boolean FEATURE_HW_VOLTE_ON = SystemProperties.getBoolean("ro.config.hw_volte_on", false);
    private static final boolean FEATURE_HW_VOWIFI_ON = SystemProperties.getBoolean("ro.vendor.config.hw_vowifi", false);
    private static final boolean FEATURE_SHOW_VOLTE_SWITCH = SystemProperties.getBoolean("ro.config.hw_volte_show_switch", true);
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    private static final int HW_ANTENNA_STATE_TYPE = 2;
    private static final int HW_BAND_CLASS_TYPE = 1;
    private static final int HW_MAX_TX_POWER_TYPE = 4;
    private static final int HW_VOLTE_SWITCH_NOT_SAVE_DB = 0;
    private static final int HW_VOLTE_SWITCH_SAVE_DB = 1;
    private static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = {"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final int HW_VOLTE_USER_SWITCH_OFF = 0;
    private static final int HW_VOLTE_USER_SWITCH_ON = 1;
    private static final String IMS_SERVICE_STATE_CHANGED_ACTION = "huawei.intent.action.IMS_SERVICE_STATE_CHANGED";
    private static final String IMS_STATE = "state";
    private static final String IMS_STATE_CHANGE_SUBID = "subId";
    private static final String IMS_STATE_REGISTERED = "REGISTERED";
    private static final int INT_SIZE = 4;
    private static final int MCC_LEN = 3;
    private static final int NR_MODE_NSA = 1;
    private static final int NR_MODE_SA = 2;
    private static final int NR_MODE_SA_NSA = 3;
    private static final int NR_MODE_UNKNOWN = 0;
    private static final String NR_OPTION_MODE = "nr_option_mode";
    private static final int NVCFG_RESULT_FINISHED = 1;
    private static final int PHONE_SUB2 = 1;
    private static final String PROP_LTE_ENABLED = "persist.radio.lte_enabled";
    private static final int QCRIL_EVT_HOOK_UNSOL_MODEM_CAPABILITY = 525308;
    private static final int RILHOOK_UNSOL_HW_BOOSTER_REPORT_BUFFER = 598046;
    private static final int RILHOOK_UNSOL_HW_REPORT_BUFFER = 598044;
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwRILReferenceImpl";
    private static final int RIL_MAX_COMMAND_BYTES = 8192;
    private static final int RIL_UNSOL_HOOK_HW_VP_STATUS = 598029;
    private static final int SWITCH_DUAL_CARD_SLOTS = 1;
    private static final int VP_STATUS_LEN = 1;
    private static String mMcc = null;
    private Context existNetworkContext;
    protected Registrant mAntStateRegistrant;
    private final BroadcastReceiver mCarrierConfigListener = new BroadcastReceiver() {
        /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && HwRILReferenceImpl.this.mHwRilReferenceInstanceId != null) {
                String action = intent.getAction();
                HwRILReferenceImpl.this.riljLog("receive event: action=" + action + ", mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
                int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (!HwImsManagerInner.isDualImsAvailable() && HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue() != subId) {
                    HwRILReferenceImpl.this.riljLog("getDefault4GSlotId do not match mHwRilReferenceInstanceId=" + HwRILReferenceImpl.this.mHwRilReferenceInstanceId);
                } else if ("android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                    handleCarrierConfigChanged(intent, subId);
                } else {
                    boolean z = false;
                    if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                        if (!HwImsManagerInner.isDualImsAvailable() && HwRILReferenceImpl.this.mRil != null) {
                            HwRILReferenceImpl hwRILReferenceImpl = HwRILReferenceImpl.this;
                            if (hwRILReferenceImpl.mRil.getRadioState() == 1) {
                                z = true;
                            }
                            hwRILReferenceImpl.handleUnsolicitedRadioStateChanged(z, HwRILReferenceImpl.this.mContext);
                        }
                    } else if (HwRILReferenceImpl.IMS_SERVICE_STATE_CHANGED_ACTION.equals(action)) {
                        int curSubId = intent.getIntExtra(HwRILReferenceImpl.IMS_STATE_CHANGE_SUBID, -1);
                        HwRILReferenceImpl.this.riljLog("IMS_SERVICE_STATE_CHANGED_ACTION curSubId is : " + curSubId);
                        if (curSubId == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            HwRILReferenceImpl.this.mIsImsRegistered = HwRILReferenceImpl.IMS_STATE_REGISTERED.equals(intent.getStringExtra(HwRILReferenceImpl.IMS_STATE));
                            boolean isSupportVolte = HwRILReferenceImpl.FEATURE_HW_VOLTE_ON;
                            if (HwRILReferenceImpl.FEATURE_VOLTE_DYN && HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                                isSupportVolte = HwImsManagerInner.isVolteEnabledByPlatform(context, HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue());
                            } else if (HwRILReferenceImpl.FEATURE_VOLTE_DYN && !HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                                return;
                            }
                            HwRILReferenceImpl.this.riljLog("mIsImsRegistered is : " + HwRILReferenceImpl.this.mIsImsRegistered + " and isSupportVolte is : " + isSupportVolte);
                            if ((!HwRILReferenceImpl.FEATURE_VOLTE_DYN || HwRILReferenceImpl.this.mIsCarrierConfigLoaded) && HwRILReferenceImpl.this.mIsImsRegistered && !isSupportVolte) {
                                HwRILReferenceImpl hwRILReferenceImpl2 = HwRILReferenceImpl.this;
                                hwRILReferenceImpl2.handleImsSwitch(hwRILReferenceImpl2.mIsImsRegistered);
                            }
                        }
                    } else if ("com.huawei.ACTION_NETWORK_FACTORY_RESET".equals(action)) {
                        HwRILReferenceImpl.this.riljLog("receive action of reset ims");
                        if (intent.getIntExtra(HwRILReferenceImpl.IMS_STATE_CHANGE_SUBID, -1) == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                            HwRILReferenceImpl.this.riljLog("reset ims state");
                            HwRILReferenceImpl hwRILReferenceImpl3 = HwRILReferenceImpl.this;
                            if (hwRILReferenceImpl3.mRil.getRadioState() == 1) {
                                z = true;
                            }
                            hwRILReferenceImpl3.handleUnsolicitedRadioStateChanged(z, HwRILReferenceImpl.this.mContext);
                        }
                    } else {
                        HwRILReferenceImpl.this.riljLog("receive action not match.");
                    }
                }
            }
        }

        private void handleCarrierConfigChanged(Intent intent, int subId) {
            boolean z = false;
            int curSubForCarrier = intent.getIntExtra("phone", 0);
            if ((HwImsManagerInner.isDualImsAvailable() || subId == curSubForCarrier) && curSubForCarrier == HwRILReferenceImpl.this.mHwRilReferenceInstanceId.intValue()) {
                HwRILReferenceImpl.this.mIsCarrierConfigLoaded = intent.getIntExtra(HwRILReferenceImpl.CARRIER_CONFIG_CHANGE_STATE, 1) == 1;
                HwRILReferenceImpl.this.riljLog("handle event: CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED.");
                if (HwRILReferenceImpl.this.mRil == null || !HwRILReferenceImpl.this.mIsCarrierConfigLoaded) {
                    HwRILReferenceImpl.this.riljLog("mRil is null or carrier config is cleared.");
                    return;
                }
                HwRILReferenceImpl hwRILReferenceImpl = HwRILReferenceImpl.this;
                if (hwRILReferenceImpl.mRil.getRadioState() == 1) {
                    z = true;
                }
                hwRILReferenceImpl.handleUnsolicitedRadioStateChanged(z, HwRILReferenceImpl.this.mContext);
                return;
            }
            HwRILReferenceImpl.this.riljLog("getDefault4GSlotId do not match subId from intent.");
        }
    };
    private Context mContext;
    protected Registrant mCurBandClassRegistrant;
    private HwCommonRadioIndication mHwCommonRadioIndication;
    private volatile IRadio mHwCommonRadioProxy = null;
    private HwCommonRadioResponse mHwCommonRadioResponse;
    private HwCustRILReference mHwCustRILReference;
    private Integer mHwRilReferenceInstanceId;
    protected RegistrantList mIccUimLockRegistrants = new RegistrantList();
    private boolean mIsCarrierConfigLoaded = false;
    private boolean mIsImsRegistered = false;
    private boolean mIsNrEnabled = false;
    protected Registrant mMaxTxPowerRegistrant;
    private HwOemHookIndication mOemHookIndication;
    private volatile IOemHook mOemHookProxy = null;
    private HwOemHookResponse mOemHookResponse;
    private WorkSource mRILDefaultWorkSource;
    private RIL mRil;
    private String mccOperator = null;
    private boolean shouldReportRoamingPlusInfo = true;

    /* access modifiers changed from: private */
    public interface RILCommand {
        void excute(IRadio iRadio, int i) throws RemoteException, RuntimeException;
    }

    public HwRILReferenceImpl(RIL ril) {
        this.mRil = ril;
        RIL ril2 = this.mRil;
        if (ril2 != null) {
            if (!(ril2.getContext() == null || this.mRil.getContext().getApplicationInfo() == null)) {
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
                this.mRil.registerForUnsolNvCfgFinished(this, 3, (Object) null);
            }
        }
    }

    private static boolean isNrSupported() {
        return HwTelephonyManager.getDefault().isNrSupported();
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg != null) {
            int i = msg.what;
            boolean z = true;
            if (i == 1) {
                Rlog.i(RILJ_LOG_TAG, "Event EVENT_GET_IMS_SWITCH_RESULT Received");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.i(RILJ_LOG_TAG, "Get IMS switch failed!");
                } else if (ar.result instanceof int[]) {
                    if (((int[]) ar.result)[0] != 1) {
                        z = false;
                    }
                    handleImsSwitch(z);
                }
            } else if (i == 2) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                Rlog.i(RILJ_LOG_TAG, "Event EVENT_SET_IMS_SWITCH_RESULT Received,  = " + msg.arg1);
                if (ar2.exception != null && msg.arg1 == 1) {
                    saveSwitchStatusToDB(!getImsSwitch());
                }
                if (ar2.userObj instanceof Message) {
                    ((Message) ar2.userObj).sendToTarget();
                }
            } else if (i == 3) {
                handleUnsolSimNvCfgFinished(msg);
            } else if (i != 4) {
                Rlog.i(RILJ_LOG_TAG, "Invalid Message id:[" + msg.what + "]");
            } else {
                Rlog.i(RILJ_LOG_TAG, "Event EVENT_SET_NETWORK_AUTO_RESULT Received");
                getModemImsSwitchIfNeed(true);
            }
        }
    }

    private void saveSwitchStatusToDB(boolean on) {
        String dbName;
        Rlog.i(RILJ_LOG_TAG, "ims switch in DB: " + on);
        if (this.mContext != null) {
            try {
                if (HwImsManagerInner.isDualImsAvailable()) {
                    dbName = HW_VOLTE_USER_SWITCH_DUALIMS[this.mHwRilReferenceInstanceId.intValue()];
                } else {
                    dbName = HW_VOLTE_USER_SWITCH;
                }
                Settings.System.putInt(this.mContext.getContentResolver(), dbName, on ? 1 : 0);
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
        if (obj instanceof Integer) {
            int result = ((Integer) obj).intValue();
            Rlog.i(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: result=" + result);
            boolean z = true;
            boolean singleIms = HwImsManagerInner.isDualImsAvailable() ^ true;
            boolean needSetVolteSwitchToModem = result == 1;
            if (singleIms) {
                int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                Rlog.i(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: subId=" + subId + ", currentId=" + this.mHwRilReferenceInstanceId);
                if (subId != this.mHwRilReferenceInstanceId.intValue()) {
                    needSetVolteSwitchToModem = false;
                }
            }
            if (needSetVolteSwitchToModem) {
                if (this.mRil.getRadioState() != 1) {
                    z = false;
                }
                handleUnsolicitedRadioStateChanged(z, this.mContext);
                return;
            }
            return;
        }
        Rlog.e(RILJ_LOG_TAG, "handleUnsolSimNvCfgFinished: obj is null or not number");
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
        Rlog.i(RILJ_LOG_TAG, "The Message is not processed in HwRILReferenceImpl");
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void riljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mHwRilReferenceInstanceId != null) {
            str = " [SUB" + this.mHwRilReferenceInstanceId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.i(RILJ_LOG_TAG, sb.toString());
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

    public void handleImsSwitch(boolean isModemImsSwitchOn) {
        if (this.mContext == null) {
            riljLog("handleImsSwitch, mContext is null.nothing to do");
            return;
        }
        int subId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        if (this.mHwRilReferenceInstanceId == null || (!HwImsManagerInner.isDualImsAvailable() && this.mHwRilReferenceInstanceId.intValue() != subId)) {
            riljLog("getDefault4GSlotId do not match mHwRilReferenceInstanceId=" + this.mHwRilReferenceInstanceId);
            return;
        }
        boolean isApImsSwitchOn = getImsSwitchByNr();
        riljLog("handleImsSwitch and apImsStatus is : " + isApImsSwitchOn + " and modemImsStatus is : " + isModemImsSwitchOn);
        if (isApImsSwitchOn != isModemImsSwitchOn && isNeedSetImsSwitch()) {
            setImsSwitch(isApImsSwitchOn, false, null);
        }
        sendBroadCastToIms(isApImsSwitchOn);
    }

    private boolean isNeedSetImsSwitch() {
        if (!isNrSupported() || !isNrOptionModeInSa(this.mContext)) {
            return true;
        }
        if (SubscriptionManagerEx.getSimStateForSlotIndex(this.mHwRilReferenceInstanceId.intValue()) != 10) {
            riljLog("isNeedSetImsSwitch: simState is not LOADED");
            return false;
        } else if (isConfigForIdentifiedCarrier(this.mHwRilReferenceInstanceId.intValue())) {
            return true;
        } else {
            riljLog("isNeedSetImsSwitch: isConfigForIdentifiedCarrier is false");
            return false;
        }
    }

    private boolean isNrOptionModeInSa(Context context) {
        int nrOptionMode = Settings.System.getInt(context.getContentResolver(), NR_OPTION_MODE, 0);
        if (nrOptionMode == 2 || nrOptionMode == 3) {
            return true;
        }
        return false;
    }

    private boolean isConfigForIdentifiedCarrier(int slotId) {
        int subId = SubscriptionManagerEx.getSubIdUsingSlotId(slotId);
        if (subId == -1) {
            return false;
        }
        CarrierConfigManager configMgr = null;
        if (this.mContext.getSystemService("carrier_config") instanceof CarrierConfigManager) {
            configMgr = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        }
        PersistableBundle carrierConfig = null;
        if (configMgr != null) {
            carrierConfig = configMgr.getConfigForSubId(subId);
        }
        return CarrierConfigManager.isConfigForIdentifiedCarrier(carrierConfig);
    }

    public void setNrSwitch(boolean on, Message onComplete) {
        riljLog("setNrSwitch nrstate : " + on);
        Context context = this.mContext;
        if (context == null) {
            sendMsgToTarget(onComplete);
        } else if (isShowVolteSwitchInNsa(context, this.mHwRilReferenceInstanceId.intValue())) {
            sendMsgToTarget(onComplete);
        } else {
            this.mIsNrEnabled = on;
            if (!this.mIsCarrierConfigLoaded) {
                sendMsgToTarget(onComplete);
            } else if (!HwImsManagerInner.isVolteEnabledByPlatform(this.mContext, this.mHwRilReferenceInstanceId.intValue())) {
                sendMsgToTarget(onComplete);
            } else {
                if (on) {
                    setImsSwitch(on, false, onComplete);
                } else {
                    setImsSwitch(getImsSwitch(), false, onComplete);
                }
                sendBroadCastToIms(on);
            }
        }
    }

    private void sendMsgToTarget(Message onComplete) {
        if (onComplete != null) {
            onComplete.sendToTarget();
        }
    }

    public boolean getImsSwitchByNr() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        if (!this.mIsNrEnabled || isShowVolteSwitchInNsa(context, this.mHwRilReferenceInstanceId.intValue()) || !HwImsManagerInner.isVolteEnabledByPlatform(this.mContext, this.mHwRilReferenceInstanceId.intValue())) {
            return getImsSwitch();
        }
        return true;
    }

    private boolean isShowVolteSwitchInNsa(Context context, int slotId) {
        Boolean isVolteSwitchInNsa;
        if (this.mContext == null || Settings.System.getInt(context.getContentResolver(), NR_OPTION_MODE, 0) != 1 || (isVolteSwitchInNsa = (Boolean) HwCfgFilePolicy.getValue("show_volte_switch_in_nsa", slotId, Boolean.class)) == null || !isVolteSwitchInNsa.booleanValue()) {
            return false;
        }
        riljLog("isShowVolteSwitchInNsa is true, slotId -> " + slotId);
        return true;
    }

    private void setImsSwitch(boolean on, boolean isSaveDB) {
        setImsSwitch(on, isSaveDB, null);
    }

    private void setImsSwitch(final boolean on, boolean isSaveDB, Message onComplete) {
        String dbName;
        if (this.mHwRilReferenceInstanceId == null || (!HwImsManagerInner.isDualImsAvailable() && this.mHwRilReferenceInstanceId.intValue() != HwTelephonyManagerInner.getDefault().getDefault4GSlotId())) {
            riljLog("current slot not support volte");
            if (onComplete != null) {
                onComplete.sendToTarget();
                return;
            }
            return;
        }
        if (this.mContext != null && isSaveDB) {
            try {
                if (HwImsManagerInner.isDualImsAvailable()) {
                    dbName = HW_VOLTE_USER_SWITCH_DUALIMS[this.mHwRilReferenceInstanceId.intValue()];
                } else {
                    dbName = HW_VOLTE_USER_SWITCH;
                }
                Settings.System.putInt(this.mContext.getContentResolver(), dbName, on ? 1 : 0);
            } catch (Exception e) {
                Rlog.e(RILJ_LOG_TAG, "setImsSwitch get an exception");
            }
        }
        riljLog("setImsSwitch -> imsstatte : " + on);
        Message msg = obtainMessage(2);
        if (isSaveDB) {
            msg.arg1 = 1;
        } else {
            msg.arg1 = 0;
        }
        if (onComplete != null) {
            msg.obj = onComplete;
        }
        invokeIRadio(2114, msg, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass2 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setImsSwitch(serial, on ? 1 : 0);
            }
        });
    }

    public boolean getImsSwitch() {
        return HwImsManagerInner.isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mHwRilReferenceInstanceId.intValue());
    }

    public void setImsSwitch(boolean on) {
        setImsSwitch(on, true, null);
    }

    public void handleUnsolicitedRadioStateChanged(boolean on, Context context) {
        HwCustRILReference hwCustRILReference;
        Rlog.i(RILJ_LOG_TAG, "handleUnsolicitedRadioStateChanged: state on =  " + on);
        this.mContext = context;
        if (HuaweiTelephonyConfigs.isHisiPlatform()) {
            int phoneId = this.mRil.mPhoneId == null ? 0 : this.mRil.mPhoneId.intValue();
            if (!on || (hwCustRILReference = this.mHwCustRILReference) == null || !hwCustRILReference.isSetNetwrokAutoAfterSwitch(phoneId)) {
                getModemImsSwitchIfNeed(on);
                return;
            }
            Phone phone = PhoneFactory.getPhone(phoneId);
            if (phone != null) {
                phone.setNetworkSelectionModeAutomatic(obtainMessage(4));
            }
        }
    }

    private void getModemImsSwitchIfNeed(boolean on) {
        Rlog.i(RILJ_LOG_TAG, "hand radio state change and volte on is " + FEATURE_HW_VOLTE_ON);
        if ((!on || !FEATURE_HW_VOLTE_ON) && !FEATURE_HW_VOWIFI_ON) {
            Rlog.i(RILJ_LOG_TAG, "not to do, radio state is off");
        } else if (!FEATURE_VOLTE_DYN || this.mIsCarrierConfigLoaded) {
            boolean isSupportVolte = HwImsManagerInner.isVolteEnabledByPlatform(this.mContext, this.mHwRilReferenceInstanceId.intValue());
            if (!FEATURE_VOLTE_DYN || isSupportVolte || (this.mIsImsRegistered && !isSupportVolte)) {
                getModemImsSwitch(obtainMessage(1));
            }
        } else {
            Rlog.i(RILJ_LOG_TAG, "CarrierConfig is not loaded completely");
        }
    }

    public void getModemImsSwitch(Message result) {
        riljLog("getModemImsSwitch");
        invokeIRadio(2115, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass3 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getImsSwitch(serial);
            }
        });
    }

    private void sendBroadCastToIms(boolean imsSwitchOn) {
        Rlog.i(RILJ_LOG_TAG, "sendBroadCastToIms, imsSwitchOn is: " + imsSwitchOn);
        Intent intent = new Intent();
        intent.setAction(ACTION_IMS_SWITCH_STATE_CHANGE);
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void iccGetATR(Message result) {
        invokeIRadio(2032, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass4 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimATR(serial);
            }
        });
    }

    public void getPOLCapabilty(Message response) {
        invokeIRadio(2064, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass5 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPolCapability(serial);
            }
        });
    }

    public void getCurrentPOLList(Message response) {
        invokeIRadio(2065, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass6 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getPolList(serial);
            }
        });
    }

    public void setPOLEntry(final int index, final String numeric, final int nAct, Message response) {
        invokeIRadio(2066, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass7 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                String str = numeric;
                if (str == null || str.length() == 0) {
                    radio.setPolEntry(serial, Integer.toString(index), "", Integer.toString(nAct));
                } else {
                    radio.setPolEntry(serial, Integer.toString(index), numeric, Integer.toString(nAct));
                }
            }
        });
    }

    public void writeContent(CdmaSmsMessage msg, String pdu) {
        ByteArrayInputStream bais = null;
        DataInputStream dis = null;
        try {
            byte[] pduBytes = pdu.getBytes(StandardCharsets.ISO_8859_1);
            boolean z = false;
            for (byte content : pduBytes) {
                Rlog.e(RILJ_LOG_TAG, "writeSmsToRuim pdu is" + ((int) content));
            }
            ByteArrayInputStream bais2 = new ByteArrayInputStream(pduBytes);
            DataInputStream dis2 = new DataInputStream(bais2);
            msg.teleserviceId = dis2.readInt();
            msg.isServicePresent = ((byte) dis2.read()) == 1;
            msg.serviceCategory = dis2.readInt();
            msg.address.digitMode = dis2.readInt();
            msg.address.numberMode = dis2.readInt();
            msg.address.numberType = dis2.readInt();
            msg.address.numberPlan = dis2.readInt();
            int addrNbrOfDigits = (byte) dis2.read();
            for (int i = 0; i < addrNbrOfDigits; i++) {
                msg.address.digits.add(Byte.valueOf((byte) dis2.read()));
            }
            msg.subAddress.subaddressType = dis2.readInt();
            CdmaSmsSubaddress cdmaSmsSubaddress = msg.subAddress;
            if (((byte) dis2.read()) == 1) {
                z = true;
            }
            cdmaSmsSubaddress.odd = z;
            int subaddrNbrOfDigits = (byte) dis2.read();
            for (int i2 = 0; i2 < subaddrNbrOfDigits; i2++) {
                msg.subAddress.digits.add(Byte.valueOf((byte) dis2.read()));
            }
            int bearerDataLength = dis2.readInt();
            for (int i3 = 0; i3 < bearerDataLength; i3++) {
                msg.bearerData.add(Byte.valueOf((byte) dis2.read()));
            }
            try {
                bais2.close();
                dis2.close();
            } catch (IOException e) {
                riljLog("close error:IOException");
            }
        } catch (UnsupportedEncodingException e2) {
            riljLog("writeSmsToRuim: UnsupportedEncodingException");
            if (0 != 0) {
                bais.close();
            }
            if (0 != 0) {
                dis.close();
            }
        } catch (IOException e3) {
            riljLog("writeSmsToRuim: conversion from input stream to object failed");
            if (0 != 0) {
                bais.close();
            }
            if (0 != 0) {
                dis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    bais.close();
                } catch (IOException e4) {
                    riljLog("close error:IOException");
                    throw th;
                }
            }
            if (0 != 0) {
                dis.close();
            }
            throw th;
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
            if (ret != null && !"".equals(ret) && (ret instanceof String)) {
                String tempMcc = ((String) ret).substring(0, 3);
                if (mMcc == null && tempMcc != null) {
                    sendRoamingPlusBroadcast(tempMcc, this.mccOperator, context);
                }
                mMcc = tempMcc;
                riljLog(" mMcc = " + mMcc);
            }
        }
    }

    private void sendRoamingPlusBroadcast(String tempMcc, String mccOperator2, Context context) {
        if (mccOperator2 != null && !mccOperator2.equals(tempMcc) && "460".equals(tempMcc) && this.shouldReportRoamingPlusInfo) {
            Intent intent = new Intent();
            intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
            intent.putExtra("current_mcc", mccOperator2);
            context.sendBroadcast(intent);
            Rlog.i(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mcc=" + mccOperator2 + "when handleRequestGetImsiMessage");
            this.shouldReportRoamingPlusInfo = false;
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
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
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
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
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
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
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
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
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
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass8 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setPowerGrade(serial, Integer.toString(powerGrade));
            }
        });
    }

    public void setWifiTxPowerGrade(final int powerGrade, Message response) {
        invokeIRadio(535, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass9 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setWifiPowerGrade(serial, Integer.toString(powerGrade));
            }
        });
    }

    public void supplyDepersonalization(final String netpin, final int type, Message result) {
        invokeIRadio(8, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass10 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio != null) {
                    radio.supplyDepersonalization(serial, HwRILReferenceImpl.this.convertNullToEmptyString(netpin), type);
                } else {
                    HwRILReferenceImpl.this.riljLog("not support below radio 2.0");
                }
            }
        });
    }

    public void registerForUimLockcard(Handler handler, int what, Object obj) {
        this.mIccUimLockRegistrants.add(new Registrant(handler, what, obj));
    }

    public void unregisterForUimLockcard(Handler handler) {
        this.mIccUimLockRegistrants.remove(handler);
    }

    public void notifyIccUimLockRegistrants() {
        RegistrantList registrantList = this.mIccUimLockRegistrants;
        if (registrantList != null) {
            registrantList.notifyRegistrants();
        }
    }

    public void sendSMSSetLong(final int flag, Message result) {
        invokeIRadio(2015, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass11 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setLongMessage(serial, flag);
            }
        });
    }

    public void dataConnectionDetach(final int mode, Message response) {
        invokeIRadio(2011, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass12 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.dataConnectionDeatch(serial, mode);
            }
        });
    }

    public void dataConnectionAttach(final int mode, Message response) {
        invokeIRadio(2012, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass13 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.dataConnectionAttach(serial, mode);
            }
        });
    }

    public void restartRild(Message result) {
        invokeIRadio(2005, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass14 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.restartRILD(serial);
            }
        });
    }

    public void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, (Object) null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    public void requestSetEmergencyNumbers(final String ecclist_withcard, final String ecclist_nocard) {
        riljLog("setEmergencyNumbers()");
        invokeIRadio(2001, null, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass15 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setEccNum(serial, ecclist_withcard, ecclist_nocard);
            }
        });
    }

    public void queryEmergencyNumbers() {
        riljLog("queryEmergencyNumbers()");
        invokeIRadio(522, null, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass16 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getEccNum(serial);
            }
        });
    }

    public void getCdmaGsmImsi(Message result) {
        invokeIRadio(529, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass17 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCdmaGsmImsi(serial);
            }
        });
    }

    public void getCdmaModeSide(Message result) {
        invokeIRadio(2127, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass18 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getCdmaModeSide(serial);
            }
        });
    }

    public void setCdmaModeSide(final int modemID, Message result) {
        invokeIRadio(2118, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass19 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setCdmaModeSide(serial, modemID);
            }
        });
    }

    public void setVpMask(final int vpMask, Message result) {
        invokeIRadio(2099, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass20 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.setVoicePreferStatus(serial, vpMask);
            }
        });
    }

    public void resetAllConnections() {
        invokeIRadio(2017, null, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass21 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.resetAllConnections(serial);
            }
        });
    }

    public void setHwRILReferenceInstanceId(int instanceId) {
        this.mHwRilReferenceInstanceId = Integer.valueOf(instanceId);
        riljLog("set HwRILReference InstanceId: " + instanceId);
    }

    public void notifyCModemStatus(final int state, Message result) {
        invokeIRadio(2121, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass22 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.notifyCModemStatus(serial, state);
            }
        });
    }

    public boolean unregisterSarRegistrant(int type, Message result) {
        Registrant removedRegistrant = null;
        riljLog("unregisterSarRegistrant start");
        if (type == 1) {
            removedRegistrant = this.mCurBandClassRegistrant;
        } else if (type == 2) {
            removedRegistrant = this.mAntStateRegistrant;
        } else if (type == 4) {
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
        if (type == 1) {
            this.mCurBandClassRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
            isSuccess = true;
        } else if (type == 2) {
            this.mAntStateRegistrant = new Registrant(result.getTarget(), result.what, result.obj);
            isSuccess = true;
        } else if (type == 4) {
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
        int typeId = payload.get();
        riljLog("typeId in notifyAntOrMaxTxPowerInfo is " + typeId);
        int responseSize = payload.getShort();
        if (responseSize < 0 || responseSize > RIL_MAX_COMMAND_BYTES) {
            riljLog("Response Size is Invalid " + responseSize);
            return;
        }
        int result = payload.getInt();
        riljLog("notifyAntOrMaxTxPowerInfo result=" + result);
        ByteBuffer resultData = ByteBuffer.allocate(4);
        resultData.order(ByteOrder.nativeOrder());
        resultData.putInt(result);
        notifyResultByType(typeId, resultData);
    }

    public void notifyBandClassInfo(byte[] data) {
        ByteBuffer payload = ByteBuffer.wrap(data);
        payload.order(ByteOrder.nativeOrder());
        int activeBand = payload.getInt();
        riljLog("notifyBandClassInfo activeBand=" + activeBand);
        ByteBuffer resultData = ByteBuffer.allocate(4);
        resultData.order(ByteOrder.nativeOrder());
        resultData.putInt(activeBand);
        Registrant registrant = this.mCurBandClassRegistrant;
        if (registrant != null) {
            registrant.notifyResult(resultData.array());
        }
    }

    private void notifyResultByType(int type, ByteBuffer resultData) {
        Registrant registrant;
        boolean isSuccess = false;
        riljLog("notifyResultByType start");
        if (type == 2) {
            Registrant registrant2 = this.mAntStateRegistrant;
            if (registrant2 != null) {
                registrant2.notifyResult(resultData.array());
                isSuccess = true;
            }
        } else if (type == 4 && (registrant = this.mMaxTxPowerRegistrant) != null) {
            registrant.notifyResult(resultData.array());
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
            Rlog.i(RILJ_LOG_TAG, "rat: " + rat + " rac: " + rac);
            Intent intent = new Intent("com.huawei.android.intent.action.RAC_CHANGED");
            intent.putExtra("rat", rat);
            intent.putExtra("rac", rac);
            Context context = this.mContext;
            if (context != null) {
                context.sendBroadcast(intent);
            }
        }
    }

    public void sendHWBufferSolicited(Message result, int event, byte[] reqData) {
        Rlog.v(RILJ_LOG_TAG, "sendHWBufferSolicited, event:" + event + ", reqdata:" + IccUtils.bytesToHexString(reqData));
        int length = reqData == null ? 0 : reqData.length;
        int dataSize = length + 5;
        ByteBuffer buf = ByteBuffer.wrap(new byte[("QOEMHOOK".length() + 8 + dataSize)]);
        buf.order(ByteOrder.nativeOrder());
        RIL ril = this.mRil;
        buf.put("QOEMHOOK".getBytes(StandardCharsets.UTF_8));
        buf.putInt(598043);
        buf.putInt(dataSize);
        buf.putInt(event);
        buf.put((byte) length);
        if (length > 0 && length <= 120) {
            buf.put(reqData);
        }
        this.mRil.invokeOemRilRequestRaw(buf.array(), result);
    }

    public void processHWBufferUnsolicited(byte[] respData) {
        if (respData == null || 5 > respData.length) {
            Rlog.i(RILJ_LOG_TAG, "response data is null or unavailable, it from Qcril !!!");
        } else {
            this.mRil.mHWBufferRegistrants.notifyRegistrants(new AsyncResult((Object) null, respData, (Throwable) null));
        }
    }

    public void notifyDeviceState(final String device, final String state, final String extra, Message result) {
        invokeIRadio(537, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass23 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.impactAntDevstate(serial, device, state, extra);
            }
        });
    }

    public void existNetworkInfo(String state) {
        unsljLog(3034);
        if (state == null || state.length() < 3) {
            Rlog.i(RILJ_LOG_TAG, "plmn para error! break");
            return;
        }
        this.mccOperator = state.substring(0, 3);
        Rlog.i(RILJ_LOG_TAG, "recieved RIL_UNSOL_HW_EXIST_NETWORK_INFO with mccOperator =" + this.mccOperator + "and mMcc =" + mMcc);
        String str = this.mccOperator;
        if (str == null || !str.equals(mMcc)) {
            String str2 = mMcc;
            if ((str2 == null || "460".equals(str2)) && mMcc != null && this.shouldReportRoamingPlusInfo) {
                Intent intent = new Intent();
                intent.setAction(ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS);
                intent.putExtra("current_mcc", this.mccOperator);
                this.existNetworkContext.sendBroadcast(intent);
                Rlog.i(RILJ_LOG_TAG, "sendBroadcast:ACTION_HW_EXIST_NETWORK_INFO_ROAMING_PLUS with extra: mccOperator=" + this.mccOperator);
                this.shouldReportRoamingPlusInfo = false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String convertNullToEmptyString(String string) {
        return string != null ? string : "";
    }

    public void sendSimMatchedOperatorInfo(final String opKey, final String opName, final int state, final String reserveField, Message response) {
        riljLog("sendSimMatchedOperatorInfo");
        invokeIRadio(2177, response, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass24 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                if (radio != null) {
                    radio.sendSimMatchedOperatorInfo(serial, opKey, opName, state, reserveField);
                } else {
                    HwRILReferenceImpl.this.riljLog("sendSimMatchedOperatorInfo: not support by radio 2.0");
                }
            }
        });
    }

    public void getSignalStrength(Message result) {
        invokeIRadio(331, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass25 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
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
            dcResponseList.add(RIL.convertDataCallResult(dataCallResultList.get(i)));
        }
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, dcResponseList);
            }
            this.mRil.processResponseDone(rr, responseInfo, dcResponseList);
        }
    }

    public void getSimMatchedFileFromRilCache(final int fileId, Message result) {
        invokeIRadio(2179, result, new RILCommand() {
            /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass26 */

            @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
            public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                radio.getSimMatchedFileFromRilCache(serial, fileId);
            }
        });
    }

    public void custSetModemProperties() {
        int isSlotsSwitched = Settings.System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots", 0);
        if (isSlotsSwitched != 1 || (this.mRil.mPhoneId != null && this.mRil.mPhoneId.intValue() != 0)) {
            if (isSlotsSwitched != 0 || this.mRil.mPhoneId.intValue() != 1) {
                boolean lteEnabled = HwNetworkTypeUtils.isLteServiceOn(this.mRil.mPreferredNetworkType);
                riljLog("mPhoneId = " + this.mRil.mPhoneId + ", setprop lte_enabled = " + lteEnabled);
                SystemProperties.set(PROP_LTE_ENABLED, String.valueOf(lteEnabled));
            }
        }
    }

    public void notifyUnsolOemHookResponse(byte[] ret) {
        ByteBuffer oemHookResponse = ByteBuffer.wrap(ret);
        oemHookResponse.order(ByteOrder.nativeOrder());
        if (isQcUnsolOemHookResp(oemHookResponse)) {
            Rlog.i(RILJ_LOG_TAG, "OEM ID check Passed");
            processUnsolOemhookResponse(oemHookResponse, ret);
        } else if (this.mRil.mUnsolOemHookRawRegistrant != null) {
            Rlog.i(RILJ_LOG_TAG, "External OEM message, to be notified");
            this.mRil.mUnsolOemHookRawRegistrant.notifyRegistrant(new AsyncResult((Object) null, ret, (Throwable) null));
        } else {
            Rlog.e(RILJ_LOG_TAG, "invalid OEM message, to be ignored");
        }
    }

    private boolean isQcUnsolOemHookResp(ByteBuffer oemHookResponse) {
        if (oemHookResponse.capacity() < this.mRil.mHeaderSize) {
            riljLog("RIL_UNSOL_OEM_HOOK_RAW data size is " + oemHookResponse.capacity());
            return false;
        }
        byte[] oemIdBytes = new byte["QOEMHOOK".length()];
        oemHookResponse.get(oemIdBytes);
        String oemIdString = new String(oemIdBytes, StandardCharsets.UTF_8);
        riljLog("Oem ID in RIL_UNSOL_OEM_HOOK_RAW is " + oemIdString);
        return "QOEMHOOK".equals(oemIdString);
    }

    private void processUnsolOemhookResponse(ByteBuffer oemHookResponse, byte[] ret) {
        int responseId = oemHookResponse.getInt();
        riljLog("Response ID in RIL_UNSOL_OEM_HOOK_RAW is " + responseId);
        int responseSize = oemHookResponse.getInt();
        if (responseSize < 0) {
            riljLog("Response Size is Invalid " + responseSize);
            return;
        }
        byte[] responseData = new byte[responseSize];
        if (oemHookResponse.remaining() == responseSize) {
            oemHookResponse.get(responseData, 0, responseSize);
            handleUnsolOemhookResponseByEventId(responseId, responseData, ret);
            return;
        }
        riljLog("Response Size(" + responseSize + ") doesnot match remaining bytes(" + oemHookResponse.remaining() + ") in the buffer. So, don't process further");
    }

    private void handleUnsolOemhookResponseByEventId(int responseId, byte[] responseData, byte[] ret) {
        switch (responseId) {
            case QCRIL_EVT_HOOK_UNSOL_MODEM_CAPABILITY /* 525308 */:
                notifyModemCap(responseData, this.mRil.mPhoneId);
                return;
            case 525341:
                sendRacChangeBroadcast(responseData);
                return;
            case RIL_UNSOL_HOOK_HW_VP_STATUS /* 598029 */:
                notifyVpStatus(responseData);
                return;
            case 598032:
                notifyAntOrMaxTxPowerInfo(responseData);
                return;
            case 598035:
                notifyBandClassInfo(responseData);
                return;
            case RILHOOK_UNSOL_HW_REPORT_BUFFER /* 598044 */:
                riljLog("received QCRILHOOK_UNSOL_HW_REPORT_BUFFER buffer is :" + IccUtils.bytesToHexString(responseData));
                processHWBufferUnsolicited(responseData);
                return;
            case 598046:
                if (this.mRil.mUnsolOemHookRawRegistrant != null) {
                    Rlog.i(RILJ_LOG_TAG, "External OEM booster message, to be notified");
                    this.mRil.mUnsolOemHookRawRegistrant.notifyRegistrant(new AsyncResult((Object) null, ret, (Throwable) null));
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void notifyVpStatus(byte[] data) {
        int len = data.length;
        riljLog("notifyVpStatus: len = " + len);
        if (len == 1) {
            this.mRil.mReportVpStatusRegistrants.notifyRegistrants(new AsyncResult((Object) null, data, (Throwable) null));
        }
    }

    private void notifyModemCap(byte[] data, Integer phoneId) {
        this.mRil.mModemCapRegistrants.notifyRegistrants(new AsyncResult((Object) null, new UnsolOemHookBuffer(phoneId.intValue(), data), (Throwable) null));
        Rlog.i(RILJ_LOG_TAG, "MODEM_CAPABILITY on phone=" + phoneId + " notified to registrants");
    }

    public SignalStrength convertHalSignalStrength_2_1(HwSignalStrength_2_1 signalStrength, int phoneId) {
        SignalStrength ss = new SignalStrength(new CellSignalStrengthCdma(signalStrength.cdma.dbm, signalStrength.cdma.ecio, signalStrength.evdo.dbm, signalStrength.evdo.ecio, signalStrength.evdo.signalNoiseRatio), new CellSignalStrengthGsm(signalStrength.gsm.signalStrength, signalStrength.gsm.bitErrorRate, signalStrength.gsm.timingAdvance), new CellSignalStrengthWcdma(signalStrength.wcdma.base.signalStrength, signalStrength.wcdma.base.bitErrorRate, signalStrength.wcdma.rscp, signalStrength.wcdma.ecno, Integer.MAX_VALUE), new CellSignalStrengthTdscdma(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), new CellSignalStrengthLte(signalStrength.lte.signalStrength, signalStrength.lte.rsrp, signalStrength.lte.rsrq, signalStrength.lte.rssnr, signalStrength.lte.cqi, signalStrength.lte.timingAdvance), new CellSignalStrengthNr(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, signalStrength.nr.rsrp, signalStrength.nr.rsrq, signalStrength.nr.rssnr));
        ss.setPhoneId(phoneId);
        return ss;
    }

    public void setNrSaState(final int on, Message response) {
        if (!isSupportIRadio21()) {
            sendFailMessage(response, 2186);
        } else {
            invokeIRadio(2186, response, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass27 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.setHwNrSaState(serial, on);
                    }
                }
            });
        }
    }

    public void getNrSaState(Message result) {
        if (!isSupportIRadio21()) {
            sendFailMessage(result, 2187);
        } else {
            invokeIRadio(2187, result, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass28 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.getHwNrSaState(serial);
                    }
                }
            });
        }
    }

    public void getNrCellSsbId(Message result) {
        if (!isSupportIRadio21()) {
            sendFailMessage(result, 2164);
        } else {
            invokeIRadio(2164, result, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass29 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.getHwNrSsbInfo(serial);
                    }
                }
            });
        }
    }

    public void getRrcConnectionState(Message result) {
        if (!isSupportIRadio21()) {
            sendFailMessage(result, 2199);
        } else {
            invokeIRadio(2199, result, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass30 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.getHwRrcConnectionState(serial);
                    }
                }
            });
        }
    }

    public void setNrOptionMode(final int mode, Message msg) {
        if (!isSupportIRadio21()) {
            sendFailMessage(msg, 2160);
        } else {
            invokeIRadio(2160, msg, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass31 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.setHwNrOptionMode(serial, mode);
                    }
                }
            });
        }
    }

    public void getNrOptionMode(Message result) {
        if (!isSupportIRadio21()) {
            sendFailMessage(result, 2161);
        } else {
            invokeIRadio(2161, result, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass32 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.getHwNrOptionMode(serial);
                    }
                }
            });
        }
    }

    public void setTemperatureControlToModem(final int level, final int type, Message result) {
        if (!isSupportIRadio21()) {
            sendFailMessage(result, 2159);
        } else {
            invokeIRadio(2159, result, new RILCommand() {
                /* class com.android.internal.telephony.HwRILReferenceImpl.AnonymousClass33 */

                @Override // com.android.internal.telephony.HwRILReferenceImpl.RILCommand
                public void excute(IRadio radio, int serial) throws RemoteException, RuntimeException {
                    vendor.huawei.hardware.radio.V2_1.IRadio radioProxy21 = vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio);
                    if (radioProxy21 != null) {
                        radioProxy21.setTemperatureControl(serial, level, type);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public static final class UnsolOemHookBuffer {
        private byte[] mData;
        private int mRilInstance;

        public UnsolOemHookBuffer(int rilInstance, byte[] data) {
            this.mRilInstance = rilInstance;
            if (data != null) {
                this.mData = new byte[data.length];
                System.arraycopy(data, 0, this.mData, 0, data.length);
                return;
            }
            this.mData = null;
        }

        public int getRilInstance() {
            return this.mRilInstance;
        }

        public byte[] getUnsolOemHookBuffer() {
            byte[] bArr = this.mData;
            if (bArr == null) {
                return null;
            }
            byte[] data = new byte[bArr.length];
            System.arraycopy(bArr, 0, data, 0, bArr.length);
            return data;
        }
    }

    private boolean isSupportIRadio21() {
        IRadio radio = getHuaweiCommonRadioProxy(null);
        if (radio == null || vendor.huawei.hardware.radio.V2_1.IRadio.castFrom(radio) == null) {
            return false;
        }
        return true;
    }

    private void sendFailMessage(Message result, int event) {
        StringBuilder sb = new StringBuilder();
        RIL ril = this.mRil;
        sb.append(RIL.requestToString(event));
        sb.append(" not support");
        riljLog(sb.toString());
        if (result != null) {
            AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(6));
            result.sendToTarget();
        }
    }
}
