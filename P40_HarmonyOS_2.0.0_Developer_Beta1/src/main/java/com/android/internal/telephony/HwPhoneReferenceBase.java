package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.NetworkRegistrationInfoEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneStateListenerEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import huawei.cust.HwCfgFilePolicy;
import java.util.List;

public abstract class HwPhoneReferenceBase {
    private static final String ACTION_CSCON_MODE_CHANGED = "com.huawei.intent.action.CSCON_MODE";
    private static final int APPNAME_INDEX = 0;
    private static final int CALLINGPACKAGENAME_INDEX = 2;
    private static final String CSCON_MODE = "cscon_mode";
    private static final String EXTRA_LAA_STATE = "laa_state";
    private static final boolean HW_INFO;
    protected static final int HW_SWITCH_SLOT_DONE = 1;
    protected static final String HW_SWITCH_SLOT_STEP = "HW_SWITCH_SLOT_STEP";
    private static final int IMEI_TEST_LEAST_LENGTH = 6;
    private static final String LAA_STATE_CHANGE_ACTION = "com.huawei.laa.action.STATE_CHANGE_ACTION";
    private static String LOG_TAG = "HwPhoneReferenceBase";
    private static final int MAX_MAP_SIZE = 10;
    private static final int NAME_ARRAY_SIZE = 3;
    protected static final int NVCFG_RESULT_FAILED = 3;
    protected static final int NVCFG_RESULT_FINISHED = 1;
    protected static final int NVCFG_RESULT_MODEM_RESET = 2;
    protected static final int NVCFG_RESULT_REFRESHED = 0;
    private static final int PROCESSNAME_INDEX = 1;
    protected static final String PROP_NVCFG_RESULT_FILE = "persist.radio.nvcfg_file";
    private static final String STRING_HW_ECCLIST0 = "ril.hw_ecclist";
    private static final String STRING_HW_ECCLIST1 = "ril.hw_ecclist1";
    public static final int TRANSPORT_TYPE_INVALID = -1;
    public static final int TRANSPORT_TYPE_WLAN = 2;
    public static final int TRANSPORT_TYPE_WWAN = 1;
    protected Context mContext;
    protected IHwGsmCdmaPhoneInner mHwGsmCdmaPhoneInner;
    protected int mNetworkRegState = 0;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.HwPhoneReferenceBase.AnonymousClass1 */

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            HwPhoneReferenceBase.this.logd("onSubscriptionsChanged");
            HwPhoneReferenceBase.this.registerListener();
        }
    };
    protected PhoneExt mPhoneExt;
    protected int mPhoneId;
    private ServiceStateListener mPhoneStateListener;
    private String mRplmn = null;
    protected int mSubId = 0;
    private TelephonyManager mTelephonyManager;
    private String subTag;

    /* access modifiers changed from: protected */
    public abstract void globalEccCustom(String str);

    /* access modifiers changed from: protected */
    public abstract boolean isCurrentPhoneType();

    static {
        boolean z = false;
        if (SystemPropertiesEx.getBoolean("ro.debuggable", false) || SystemPropertiesEx.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = true;
        }
        HW_INFO = z;
    }

    public HwPhoneReferenceBase(IHwGsmCdmaPhoneInner gsmCdmaPhoneInner, PhoneExt phoneExt) {
        this.mHwGsmCdmaPhoneInner = gsmCdmaPhoneInner;
        this.mPhoneExt = phoneExt;
        this.mPhoneId = this.mPhoneExt.getPhoneId();
        this.mContext = phoneExt.getContext();
        this.subTag = LOG_TAG + "[" + this.mPhoneId + "]";
        HwHiCureDetection.createHwHiCureDetection(this.mContext).put(this.mPhoneId, phoneExt);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        ((SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service")).addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        registerListener();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListener() {
        int newSubId = SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
        if (!SubscriptionManager.isValidSubscriptionId(newSubId)) {
            logd("sub id is invalid, cancel listen.");
            ServiceStateListener serviceStateListener = this.mPhoneStateListener;
            if (serviceStateListener != null) {
                serviceStateListener.cancelListen();
                this.mPhoneStateListener = null;
            }
            this.mSubId = -1;
            return;
        }
        if (newSubId != this.mSubId) {
            logd("sub id is different, cancel first and create a new one." + newSubId);
            ServiceStateListener serviceStateListener2 = this.mPhoneStateListener;
            if (serviceStateListener2 != null) {
                serviceStateListener2.cancelListen();
            }
            this.mPhoneStateListener = new ServiceStateListener(newSubId);
            this.mPhoneStateListener.listen();
        } else {
            logd("sub id is not change, do nothing." + this.mSubId);
        }
        this.mSubId = newSubId;
    }

    public boolean beforeHandleMessage(Message msg) {
        logd("beforeHandleMessage what = " + msg.what);
        boolean msgHandled = true;
        int i = msg.what;
        if (i != 104) {
            switch (i) {
                case 112:
                    logd("EVENT_HW_LAA_STATE_CHANGED");
                    onLaaStageChanged(msg);
                    break;
                case 113:
                    logd("EVENT_UNSOL_HW_CALL_ALT_SRV_DONE");
                    handleUnsolCallAltSrv(msg);
                    break;
                case 114:
                    logd("EVENT_UNSOL_SIM_NVCFG_FINISHED");
                    handleUnsolSimNvcfgChange(msg);
                    break;
                case 115:
                    logd("EVENT_GET_NVCFG_RESULT_INFO_DONE");
                    handleGetNvcfgResultInfoDone(msg);
                    break;
                case 116:
                    handleUnsolCsconInfo(msg);
                    break;
                case 117:
                    handleRplmnsStateChanged(msg);
                    break;
                default:
                    msgHandled = msg.what >= 100;
                    if (!msgHandled) {
                        logd("unhandle event");
                        break;
                    }
                    break;
            }
        } else {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            setEccNumbers((String) ar.getResult());
            logd("Handle EVENT_ECC_NUM:" + ar.getResult());
        }
        return msgHandled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
        RlogEx.i(this.subTag, msg);
    }

    private void loge(String msg) {
        RlogEx.e(this.subTag, msg);
    }

    private void setEccNumbers(String oriValue) {
        String value = oriValue;
        try {
            if (!needSetEccNumbers()) {
                value = BuildConfig.FLAVOR;
            }
            if (this.mPhoneExt.getPhoneId() <= 0) {
                SystemPropertiesEx.set(STRING_HW_ECCLIST0, value);
            } else {
                SystemPropertiesEx.set(STRING_HW_ECCLIST1, value);
            }
        } catch (IllegalArgumentException e) {
            loge("setEccNumbers IllegalArgumentException");
        } catch (RuntimeException e2) {
            loge("setEccNumbers RuntimeException");
        } catch (Exception e3) {
            loge("setEccNumbers Exception");
        }
    }

    private boolean needSetEccNumbers() {
        if (!TelephonyManagerEx.isMultiSimEnabled() || !SystemPropertiesEx.getBoolean("ro.config.hw_ecc_with_sim_card", false)) {
            return true;
        }
        boolean hasPresentCard = false;
        int simCount = TelephonyManagerEx.getSimCount();
        int i = 0;
        while (true) {
            if (i >= simCount) {
                break;
            } else if (TelephonyManagerEx.getSimState(i) != 1) {
                hasPresentCard = true;
                break;
            } else {
                i++;
            }
        }
        int slotId = SubscriptionControllerEx.getInstance().getSlotIndex(this.mPhoneExt.getSubId());
        logd("needSetEccNumbers  slotId = " + slotId + " hasPresentCard = " + hasPresentCard);
        return !hasPresentCard || TelephonyManagerEx.getSimState(slotId) != 1;
    }

    /* access modifiers changed from: protected */
    public void onLaaStageChanged(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            logd("onLaaStageChanged:don't sendBroadcast LAA_STATE_CHANGE_ACTION");
            return;
        }
        int[] result = (int[]) ar.getResult();
        Intent intent = new Intent(LAA_STATE_CHANGE_ACTION);
        intent.putExtra(EXTRA_LAA_STATE, result[0]);
        logd("sendBroadcast com.huawei.laa.action.STATE_CHANGE_ACTION Laa_state=" + result[0]);
        Context context = this.mPhoneExt.getContext();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void handleUnsolCallAltSrv(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        logd("handleUnsolCallAltSrv");
        if (ar == null || ar.getException() != null) {
            logd("handleUnsolCallAltSrv: ar or ar.exception is null");
            return;
        }
        IPhoneCallback callback = (IPhoneCallback) ar.getUserObj();
        if (callback != null) {
            try {
                callback.onCallback1(this.mPhoneId);
                logd("handleUnsolCallAltSrv,onCallback1 for subId=" + this.mPhoneId);
            } catch (RemoteException ex) {
                logd("handleUnsolCallAltSrv:onCallback1 RemoteException:" + ex);
            }
        } else {
            logd("handleUnsolCallAltSrv: callback is null");
        }
    }

    public boolean virtualNetEccFormCarrier(int slotId) {
        try {
            Boolean supportVmEccState = (Boolean) HwCfgFilePolicy.getValue("support_vn_ecc", slotId, Boolean.class);
            if (supportVmEccState != null) {
                return supportVmEccState.booleanValue();
            }
            return false;
        } catch (ClassCastException e) {
            loge("Failed to get support_vm_ecc in carrier ClassCastException");
            return false;
        } catch (Exception e2) {
            logd("Failed to get support_vm_ecc in carrier");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void handleUnsolSimNvcfgChange(Message msg) {
        if (msg == null) {
            loge("handleUnsolSimNvcfgChange: msg is null, return.");
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            loge("handleUnsolSimNvcfgChange: ar is null or exception occurs.");
            return;
        }
        int nvcfgResult = ((Integer) ar.getResult()).intValue();
        if (nvcfgResult != 0 && HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == this.mPhoneId) {
            logd("handleUnsolSimNvcfgChange: nvcfgResult=" + nvcfgResult + ", get NVCFG for mainSlot:" + this.mPhoneId);
            this.mPhoneExt.getCi().getNvcfgMatchedResult(this.mPhoneExt.obtainMessage(115));
        }
    }

    /* access modifiers changed from: protected */
    public void handleGetNvcfgResultInfoDone(Message msg) {
        if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() != this.mPhoneId || msg == null) {
            loge("handleGetNvcfgResultDone: mPhoneId=" + this.mPhoneId + " is not main Slot, or msg is null, return.");
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null || ar.getException() != null) {
            loge("handleGetNvcfgResultDone: ar is null or exception occurs.");
            return;
        }
        String nvcfgResultInfo = (String) ar.getResult();
        logd("handleGetNvcfgResultDone: nvcfgResultInfo=" + nvcfgResultInfo);
        if (!TextUtils.isEmpty(nvcfgResultInfo)) {
            try {
                SystemPropertiesEx.set(PROP_NVCFG_RESULT_FILE, nvcfgResultInfo);
            } catch (IllegalArgumentException e) {
                loge("handleGetNvcfgResultDone: IllegalArgumentException ");
            } catch (RuntimeException e2) {
                loge("handleGetNvcfgResultDone: RuntimeException");
            }
        }
    }

    public void handleUnsolCsconInfo(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        logd("handleUnsolCsconInfo");
        if (ar == null || ar.getException() != null) {
            logd("handleUnsolCsconInfo: ar or ar.exception is null");
            return;
        }
        logd("handle EVENT_CSCON_MODE_INFO : " + ((Integer) ar.getResult()).intValue());
        Intent intent = new Intent(ACTION_CSCON_MODE_CHANGED);
        intent.setPackage("com.huawei.systemserver");
        intent.putExtra(CSCON_MODE, ((Integer) ar.getResult()).intValue());
        intent.putExtra("subid", this.mPhoneExt.getSubId());
        Context context = this.mPhoneExt.getContext();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void logForImei(String phoneType, String imei) {
        if (imei == null) {
            logd(phoneType + " imei is null");
        } else if (6 > imei.length()) {
            logd(phoneType + " imei is in wrong format:" + imei);
        } else {
            logd(phoneType + " imei:****" + imei.substring(imei.length() - 6));
        }
    }

    public String getRplmn() {
        return this.mRplmn;
    }

    private void handleRplmnsStateChanged(Message msg) {
        if (msg == null) {
            loge("handleRplmnsStateChanged: EVENT_RPLMNS_STATE_CHANGED, msg is null, return;");
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (!(ar != null && ar.getException() == null) || !(ar.getResult() instanceof String)) {
            loge("handleRplmnsStateChanged: EVENT_RPLMNS_STATE_CHANGED, ar exception occurs.");
            return;
        }
        this.mRplmn = (String) ar.getResult();
        logd("handleRplmnsStateChanged: EVENT_RPLMNS_STATE_CHANGED, mRplmn=" + this.mRplmn);
    }

    public boolean isRegisteredHomeNetworkForTransportType(int phoneId, int transportType) {
        return isRegisteredHomeNetworkForServiceState(TelephonyManagerEx.getServiceStateForSubscriber(SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(phoneId)), transportType);
    }

    public boolean isRegisteredHomeNetworkForServiceState(ServiceState ss, int transportType) {
        boolean isRegHomeState = true;
        if (getCombinedNetworkRegStateFromServicestate(ss, transportType) != 1) {
            isRegHomeState = false;
        }
        return isRegHomeState;
    }

    public int getCombinedNetworkRegState(int phoneId) {
        return getCombinedNetworkRegStateFromServicestate(TelephonyManagerEx.getServiceStateForSubscriber(SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(phoneId)), 1);
    }

    public int getCombinedNetworkRegStateFromServicestate(ServiceState ss, int transportType) {
        if (ss == null) {
            loge("getCombinedNetworkRegStateFromServicestate: ServiceState is null");
            return 0;
        }
        List<NetworkRegistrationInfoEx> netRegStateList = ServiceStateEx.getNetworkRegistrationInfoExListForTransportType(transportType, ss);
        if (netRegStateList == null || netRegStateList.size() == 0) {
            loge("getCombinedNetworkRegStateFromServicestate: netRegStateList is null or empty.");
            return 0;
        }
        int regState = 0;
        for (NetworkRegistrationInfoEx tmpNetRegState : netRegStateList) {
            if (tmpNetRegState.getRegistrationState() == 1) {
                logd("getCombinedNetworkRegStateFromServicestate: transportType=" + tmpNetRegState.getTransportType() + ", regState=" + tmpNetRegState.getRegistrationState());
                return 1;
            }
            regState = tmpNetRegState.getRegistrationState();
        }
        return regState;
    }

    public void updateWfcMode(Context context, boolean roaming, int subId) throws ImsExceptionExt {
        try {
            HwImsManagerInner.updateWfcMode(context, roaming, subId);
        } catch (Exception e) {
            throw new ImsExceptionExt(e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public class ServiceStateListener extends PhoneStateListenerEx {
        ServiceStateListener(int subId) {
            super(subId);
            HwPhoneReferenceBase.this.logd("ServiceStateListener create subId:" + subId);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.HwPhoneReferenceBase$ServiceStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void listen() {
            HwPhoneReferenceBase.this.mTelephonyManager.listen(this, 1);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.HwPhoneReferenceBase$ServiceStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void cancelListen() {
            HwPhoneReferenceBase.this.mTelephonyManager.listen(this, 0);
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null) {
                String hplmn = HwPhoneReferenceBase.this.mPhoneExt.getIccRecords().getOperatorNumeric();
                HwPhoneReferenceBase.this.logd("onServiceStateChanged, state = " + state + ", hplmn = " + hplmn + ", isCurrentType: " + HwPhoneReferenceBase.this.isCurrentPhoneType());
                if (HwPhoneReferenceBase.this.isCurrentPhoneType()) {
                    boolean isUseRplmn = false;
                    if (SystemPropertiesEx.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
                        boolean isRegHomeState = HwPhoneReferenceBase.this.isRegisteredHomeNetworkForServiceState(state, 1);
                        if (!TextUtils.isEmpty(HwPhoneReferenceBase.this.getRplmn()) && !isRegHomeState) {
                            isUseRplmn = true;
                        }
                        if (isUseRplmn) {
                            HwPhoneReferenceBase.this.logd("onServiceStateChanged:isRegHomeState=" + isRegHomeState + ", rplmn =" + HwPhoneReferenceBase.this.getRplmn());
                            HwPhoneReferenceBase hwPhoneReferenceBase = HwPhoneReferenceBase.this;
                            hwPhoneReferenceBase.globalEccCustom(hwPhoneReferenceBase.getRplmn());
                        } else if (hplmn != null) {
                            HwPhoneReferenceBase.this.globalEccCustom(hplmn);
                        } else {
                            HwPhoneReferenceBase.this.logd("hplmn is null.");
                        }
                        HwPhoneReferenceBase hwPhoneReferenceBase2 = HwPhoneReferenceBase.this;
                        hwPhoneReferenceBase2.mNetworkRegState = hwPhoneReferenceBase2.getCombinedNetworkRegStateFromServicestate(state, 1);
                    }
                }
            }
        }
    }
}
