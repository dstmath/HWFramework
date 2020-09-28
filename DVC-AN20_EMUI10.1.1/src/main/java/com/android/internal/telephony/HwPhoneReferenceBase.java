package com.android.internal.telephony;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.HwImsManagerInner;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.ImsExceptionExt;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCfgFilePolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HwPhoneReferenceBase {
    private static final String ACTION_CSCON_MODE_CHANGED = "com.huawei.intent.action.CSCON_MODE";
    private static final int APPNAME_INDEX = 0;
    private static final int CALLINGPACKAGENAME_INDEX = 2;
    private static final String CSCON_MODE = "cscon_mode";
    private static final String EXTRA_LAA_STATE = "laa_state";
    private static final boolean HW_INFO;
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
    protected Context mContext;
    protected IHwGsmCdmaPhoneInner mHwGsmCdmaPhoneInner;
    private Map<Integer, String[]> mMap = new HashMap();
    protected int mNetworkRegState = 0;
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.HwPhoneReferenceBase.AnonymousClass1 */

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
        if (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getBoolean("persist.sys.huawei.debug.on", false)) {
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
        int newSubId = SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
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
                    msgHandled = false;
                    if (msg.what >= 100) {
                        msgHandled = true;
                    }
                    if (!msgHandled) {
                        logd("unhandle event");
                        break;
                    }
                    break;
            }
        } else {
            AsyncResult ar = (AsyncResult) msg.obj;
            setEccNumbers((String) ar.result);
            logd("Handle EVENT_ECC_NUM:" + ((String) ar.result));
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
                value = "";
            }
            if (this.mPhoneExt.getPhoneId() <= 0) {
                SystemProperties.set(STRING_HW_ECCLIST0, value);
            } else {
                SystemProperties.set(STRING_HW_ECCLIST1, value);
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
        if (!TelephonyManager.getDefault().isMultiSimEnabled() || !SystemProperties.getBoolean("ro.config.hw_ecc_with_sim_card", false)) {
            return true;
        }
        boolean hasPresentCard = false;
        int simCount = TelephonyManager.getDefault().getSimCount();
        int i = 0;
        while (true) {
            if (i >= simCount) {
                break;
            } else if (TelephonyManager.getDefault().getSimState(i) != 1) {
                hasPresentCard = true;
                break;
            } else {
                i++;
            }
        }
        int slotId = SubscriptionController.getInstance().getSlotIndex(this.mPhoneExt.getSubId());
        logd("needSetEccNumbers  slotId = " + slotId + " hasPresentCard = " + hasPresentCard);
        return !hasPresentCard || TelephonyManager.getDefault().getSimState(slotId) != 1;
    }

    /* access modifiers changed from: protected */
    public void onLaaStageChanged(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            logd("onLaaStageChanged:don't sendBroadcast LAA_STATE_CHANGE_ACTION");
            return;
        }
        int[] result = (int[]) ar.result;
        Intent intent = new Intent(LAA_STATE_CHANGE_ACTION);
        intent.putExtra(EXTRA_LAA_STATE, result[0]);
        logd("sendBroadcast com.huawei.laa.action.STATE_CHANGE_ACTION Laa_state=" + result[0]);
        Context context = this.mPhoneExt.getContext();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void handleUnsolCallAltSrv(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        logd("handleUnsolCallAltSrv");
        if (ar == null || ar.exception != null) {
            logd("handleUnsolCallAltSrv: ar or ar.exception is null");
            return;
        }
        IPhoneCallback callback = (IPhoneCallback) ar.userObj;
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
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            loge("handleUnsolSimNvcfgChange: ar is null or exception occurs.");
            return;
        }
        int nvcfgResult = ((Integer) ar.result).intValue();
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
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || ar.exception != null) {
            loge("handleGetNvcfgResultDone: ar is null or exception occurs.");
            return;
        }
        String nvcfgResultInfo = (String) ar.result;
        logd("handleGetNvcfgResultDone: nvcfgResultInfo=" + nvcfgResultInfo);
        if (!TextUtils.isEmpty(nvcfgResultInfo)) {
            try {
                SystemProperties.set(PROP_NVCFG_RESULT_FILE, nvcfgResultInfo);
            } catch (IllegalArgumentException e) {
                loge("handleGetNvcfgResultDone: IllegalArgumentException ");
            } catch (RuntimeException e2) {
                loge("handleGetNvcfgResultDone: RuntimeException");
            }
        }
    }

    public void handleUnsolCsconInfo(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        logd("handleUnsolCsconInfo");
        if (ar == null || ar.exception != null) {
            logd("handleUnsolCsconInfo: ar or ar.exception is null");
            return;
        }
        logd("handle EVENT_CSCON_MODE_INFO : " + ((Integer) ar.result).intValue());
        Intent intent = new Intent(ACTION_CSCON_MODE_CHANGED);
        intent.setPackage("com.huawei.systemserver");
        intent.putExtra(CSCON_MODE, ((Integer) ar.result).intValue());
        intent.putExtra("subid", this.mPhoneExt.getSubId());
        Context context = this.mPhoneExt.getContext();
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }

    public void logForTest(String operationName, String content) {
        String processName;
        String callingPackageName;
        if (HW_INFO) {
            int pid = Binder.getCallingPid();
            String appName = "";
            synchronized (this) {
                String[] name = this.mMap.get(Integer.valueOf(pid));
                if (name != null) {
                    appName = name[0];
                    processName = name[1];
                    callingPackageName = name[2];
                } else {
                    loge("pid is not exist in map");
                    if (MAX_MAP_SIZE == this.mMap.size()) {
                        this.mMap.clear();
                    }
                    String processName2 = getProcessName(pid);
                    String callingPackageName2 = getPackageNameForPid(pid);
                    try {
                        Context context = this.mPhoneExt.getContext();
                        appName = context.getPackageManager().getPackageInfo(callingPackageName2, 0).applicationInfo.loadLabel(context.getPackageManager()).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        loge("get appname wrong");
                    }
                    this.mMap.put(Integer.valueOf(pid), new String[]{appName, processName2, callingPackageName2});
                    processName = processName2;
                    callingPackageName = callingPackageName2;
                }
            }
            RlogEx.i("ctaifs <" + appName + ">[" + callingPackageName + "][" + processName + "]", "[" + operationName + "] " + content);
        }
    }

    private String getPackageNameForPid(int pid) {
        String res = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(504, data, reply, 0);
            reply.readException();
            res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (RuntimeException e) {
            logd("RuntimeException");
            return res;
        } catch (Exception e2) {
            logd("getPackageNameForPid exception");
            return res;
        }
    }

    private String getProcessName(int pid) {
        String processName = "";
        List<ActivityManager.RunningAppProcessInfo> l = ((ActivityManager) this.mPhoneExt.getContext().getSystemService("activity")).getRunningAppProcesses();
        if (l == null) {
            return processName;
        }
        for (ActivityManager.RunningAppProcessInfo info : l) {
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                }
            } catch (RuntimeException e) {
                logd("RuntimeException");
            } catch (Exception e2) {
                logd("Get The appName is wrong");
            }
        }
        return processName;
    }

    public void logForImei(String phoneType, String imei) {
        if (imei == null) {
            logd(phoneType + " imei is null");
        } else if (6 > imei.length()) {
            logd(phoneType + " imei is in wrong format:" + imei);
        } else {
            logd(phoneType + " imei:****" + imei.substring(imei.length() - 6, imei.length()));
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
        AsyncResult ar = (AsyncResult) msg.obj;
        if (!(ar != null && ar.exception == null) || !(ar.result instanceof String)) {
            loge("handleRplmnsStateChanged: EVENT_RPLMNS_STATE_CHANGED, ar exception occurs.");
            return;
        }
        this.mRplmn = (String) ar.result;
        logd("handleRplmnsStateChanged: EVENT_RPLMNS_STATE_CHANGED, mRplmn=" + this.mRplmn);
    }

    public boolean isRegisteredHomeNetworkForTransportType(int phoneId, int transportType) {
        return isRegisteredHomeNetworkForServiceState(TelephonyManager.getDefault().getServiceStateForSubscriber(SubscriptionController.getInstance().getSubIdUsingPhoneId(phoneId)), transportType);
    }

    public boolean isRegisteredHomeNetworkForServiceState(ServiceState ss, int transportType) {
        boolean isRegHomeState = true;
        if (getCombinedNetworkRegStateFromServicestate(ss, transportType) != 1) {
            isRegHomeState = false;
        }
        return isRegHomeState;
    }

    public int getCombinedNetworkRegState(int phoneId) {
        return getCombinedNetworkRegStateFromServicestate(TelephonyManager.getDefault().getServiceStateForSubscriber(SubscriptionController.getInstance().getSubIdUsingPhoneId(phoneId)), 1);
    }

    public int getCombinedNetworkRegStateFromServicestate(ServiceState ss, int transportType) {
        if (ss == null) {
            loge("getCombinedNetworkRegStateFromServicestate: ServiceState is null");
            return 0;
        }
        List<NetworkRegistrationInfo> netRegStateList = ss.getNetworkRegistrationInfoListForTransportType(transportType);
        if (netRegStateList == null || netRegStateList.size() == 0) {
            loge("getCombinedNetworkRegStateFromServicestate: netRegStateList is null or empty.");
            return 0;
        }
        int regState = 0;
        for (NetworkRegistrationInfo tmpNetRegState : netRegStateList) {
            if (tmpNetRegState.getRegistrationState() == 1) {
                logd("getCombinedNetworkRegStateFromServicestate: transportType=" + tmpNetRegState.getTransportType() + ", regState=" + tmpNetRegState.getRegistrationState());
                return 1;
            }
            regState = tmpNetRegState.getRegistrationState();
        }
        return regState;
    }

    /* access modifiers changed from: private */
    public class ServiceStateListener extends PhoneStateListener {
        ServiceStateListener(int subId) {
            super(Integer.valueOf(subId));
            HwPhoneReferenceBase.this.logd("ServiceStateListener create subId:" + subId);
        }

        /* access modifiers changed from: package-private */
        public void listen() {
            HwPhoneReferenceBase.this.mTelephonyManager.listen(this, 1);
        }

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
                    if (SystemProperties.getBoolean("ro.config.hw_eccNumUseRplmn", false)) {
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

    public void updateWfcMode(Context context, boolean roaming, int subId) throws ImsExceptionExt {
        try {
            HwImsManagerInner.updateWfcMode(context, roaming, subId);
        } catch (Exception e) {
            throw new ImsExceptionExt(e.getMessage());
        }
    }
}
