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
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
    private GsmCdmaPhone mGsmCdmaPhone;
    private Map<Integer, String[]> mMap = new HashMap();
    int mPhoneId;
    private String subTag;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = true;
        }
        HW_INFO = z;
    }

    public HwPhoneReferenceBase(GsmCdmaPhone phone) {
        this.mGsmCdmaPhone = phone;
        this.mPhoneId = this.mGsmCdmaPhone.getPhoneId();
        this.subTag = LOG_TAG + "[" + this.mGsmCdmaPhone.getPhoneId() + "]";
        HwHiCureDetection.createHwHiCureDetection(phone.getContext()).put(phone.getSubId(), phone);
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

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }

    private void setEccNumbers(String value) {
        try {
            if (!needSetEccNumbers()) {
                value = "";
            }
            if (this.mGsmCdmaPhone.getSubId() <= 0) {
                SystemProperties.set("ril.ecclist", value);
            } else {
                SystemProperties.set("ril.ecclist1", value);
            }
        } catch (RuntimeException e) {
            loge("setEccNumbers RuntimeException: " + e);
        } catch (Exception e2) {
            loge("setEccNumbers Exception: " + e2);
        }
    }

    private boolean needSetEccNumbers() {
        int slotId;
        boolean z = true;
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
        logd("needSetEccNumbers  slotId = " + slotId + " hasPresentCard = " + hasPresentCard);
        if (hasPresentCard && TelephonyManager.getDefault().getSimState(slotId) == 1) {
            z = false;
        }
        return z;
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
        Context context = this.mGsmCdmaPhone.getContext();
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

    public boolean virtualNetEccFormCarrier(int mPhoneId2) {
        try {
            Boolean supportVmEccState = (Boolean) HwCfgFilePolicy.getValue("support_vn_ecc", SubscriptionManager.getSlotIndex(mPhoneId2), Boolean.class);
            if (supportVmEccState != null) {
                return supportVmEccState.booleanValue();
            }
            return false;
        } catch (Exception e) {
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
        } else {
            int nvcfgResult = ((Integer) ar.result).intValue();
            if (nvcfgResult != 0 && HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == this.mPhoneId) {
                logd("handleUnsolSimNvcfgChange: nvcfgResult=" + nvcfgResult + ", get NVCFG for mainSlot:" + this.mPhoneId);
                this.mGsmCdmaPhone.mCi.getNvcfgMatchedResult(this.mGsmCdmaPhone.obtainMessage(115));
            }
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
        } else {
            String nvcfgResultInfo = (String) ar.result;
            logd("handleGetNvcfgResultDone: nvcfgResultInfo=" + nvcfgResultInfo);
            if (!TextUtils.isEmpty(nvcfgResultInfo)) {
                try {
                    SystemProperties.set(PROP_NVCFG_RESULT_FILE, nvcfgResultInfo);
                } catch (RuntimeException e) {
                    loge("handleGetNvcfgResultDone: RuntimeException e=" + e);
                }
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
        intent.putExtra("subid", this.mGsmCdmaPhone.getSubId());
        Context context = this.mGsmCdmaPhone.getContext();
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
                    if (10 == this.mMap.size()) {
                        this.mMap.clear();
                    }
                    String processName2 = getProcessName(pid);
                    String callingPackageName2 = getPackageNameForPid(pid);
                    try {
                        Context context = this.mGsmCdmaPhone.getContext();
                        appName = context.getPackageManager().getPackageInfo(callingPackageName2, 0).applicationInfo.loadLabel(context.getPackageManager()).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        loge("get appname wrong");
                    }
                    this.mMap.put(Integer.valueOf(pid), new String[]{appName, processName2, callingPackageName2});
                    processName = processName2;
                    callingPackageName = callingPackageName2;
                }
            }
            Rlog.i("ctaifs <" + appName + ">[" + callingPackageName + "][" + processName + "]", "[" + operationName + "] " + content);
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
        List<ActivityManager.RunningAppProcessInfo> l = ((ActivityManager) this.mGsmCdmaPhone.getContext().getSystemService("activity")).getRunningAppProcesses();
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
}
