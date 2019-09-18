package com.android.server.security.securitydiagnose;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import com.android.server.pm.AntiMalComponentInfo;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.deviceusage.HwOEMInfoAdapter;
import com.android.server.security.securitydiagnose.RootDetectReport;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.security.IHwSecurityDiagnoseCallback;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwSecurityDiagnosePlugin extends IHwSecurityDiagnosePlugin.Stub implements IHwSecurityPlugin {
    private static final String BD_PACKAGE_NAME = "com.huawei.bd";
    private static final String BD_SERVICE_NAME = "com.huawei.bd.BDService";
    /* access modifiers changed from: private */
    public static final boolean CHINA_RELEASE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context contxt) {
            HwSecurityDiagnosePlugin access$100;
            synchronized (HwSecurityDiagnosePlugin.mLock) {
                if (HwSecurityDiagnosePlugin.mInstance == null) {
                    HwSecurityDiagnosePlugin unused = HwSecurityDiagnosePlugin.mInstance = new HwSecurityDiagnosePlugin(contxt);
                }
                access$100 = HwSecurityDiagnosePlugin.mInstance;
            }
            return access$100;
        }

        public String getPluginPermission() {
            return HwSecurityDiagnosePlugin.PERMISSION;
        }
    };
    private static final long DELAY_INTERVAL = 6000;
    private static final long DELAY_TRIGGER_ROOT_SCAN = 43200000;
    private static final int EVT_CHECK_BD_AGAIN = 1000;
    private static final int EVT_PROCESS_SECURE_DATA = 1001;
    private static final int EVT_REPORTER = 1002;
    private static final int EVT_START_ROOT_CHECK = 1003;
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (SystemProperties.get("ro.secure", "1").equals("0") || Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean HW_OPTB_CHINA = "156".equals(SystemProperties.get("ro.config.hw_optb", ""));
    private static final String JSON_FORMAT_ENCODING = "UTF-8";
    private static final int MAX_SEND_APKS = 5;
    private static final int MAX_TRY_COUNT = 10;
    private static final String PERMISSION = "com.huawei.permission.SECURITY_DIAGNOSE";
    private static final String ROOT_ALARM_ACTION = "huawei.intent.action.ROOT_DETECT_REPORTER";
    private static final String TAG = "HwSecurityDiagnosePlugin";
    private static List<IHwSecurityDiagnoseCallback> mCallBackList = new ArrayList();
    /* access modifiers changed from: private */
    public static HwSecurityDiagnosePlugin mInstance;
    /* access modifiers changed from: private */
    public static final Object mLock = new Object();
    private static final Object mLockCallBackList = new Object();
    private static volatile boolean mTriggered = false;
    private ArrayList<AntiMalComponentInfo> mAntiMalComponentList;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsBootCompleted;
    private boolean mIsDBNotExist;
    /* access modifiers changed from: private */
    public Handler mMyHandler;
    private ArrayList<QueueParams> mQueue;
    private BroadcastReceiver mReceiver;
    private RootDetectReport.Listener mRootReportListener;
    private int mTryCount;

    private static class QueueParams {
        public final Object mArg;
        public final int mReporter;

        private QueueParams(int reporter, Object arg) {
            this.mReporter = reporter;
            this.mArg = arg;
        }
    }

    private static class RootCheckTrigger extends Thread {
        private RootCheckTrigger() {
        }

        public void run() {
            if (HwSecurityDiagnosePlugin.HW_DEBUG) {
                Log.d(HwSecurityDiagnosePlugin.TAG, "rootCheckTask START!");
            }
            RootDetectReport.getInstance().triggerRootScan();
        }
    }

    private void setTriggerTime() {
        if (this.mContext == null) {
            Log.e(TAG, "Time trigger error");
            return;
        }
        PendingIntent sender = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ROOT_ALARM_ACTION), 268435456);
        ((AlarmManager) this.mContext.getSystemService("alarm")).setRepeating(3, DELAY_TRIGGER_ROOT_SCAN + SystemClock.elapsedRealtime(), DELAY_TRIGGER_ROOT_SCAN, sender);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.securitydiagnose.HwSecurityDiagnosePlugin, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Log.d(TAG, "onStart()");
        }
        IntentFilter intentFilter = new IntentFilter("android.intent.action.LOCKED_BOOT_COMPLETED");
        intentFilter.addAction(ROOT_ALARM_ACTION);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        RootDetectReport.init(this.mContext);
        RootDetectReport.getInstance().setListener(this.mRootReportListener);
        if (CHINA_RELEASE_VERSION || HW_OPTB_CHINA) {
            MalAppDetectReport.init(this.mContext);
        }
        AppLayerStpProxy.init(this.mContext);
    }

    public void onStop() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public int report(int reporterID, Bundle data) {
        if (!CHINA_RELEASE_VERSION) {
            return 0;
        }
        if (HW_DEBUG) {
            Log.d(TAG, "report reporterID = " + reporterID);
        }
        checkPermission(PERMISSION);
        this.mMyHandler.sendMessage(this.mMyHandler.obtainMessage(1002, reporterID, 0, data));
        return 0;
    }

    public void sendComponentInfo(Bundle data) {
        if (CHINA_RELEASE_VERSION) {
            if (data == null) {
                Log.e(TAG, "sendComponentInfo bundle is null!");
                return;
            }
            checkPermission(PERMISSION);
            ArrayList<AntiMalComponentInfo> componentList = data.getParcelableArrayList(HwSecDiagnoseConstant.COMPONENT_LIST);
            if (componentList == null || componentList.size() == 0) {
                if (HW_DEBUG) {
                    Log.d(TAG, "sendComponentInfo componentList IS null!");
                }
                return;
            }
            synchronized (this.mAntiMalComponentList) {
                Iterator<AntiMalComponentInfo> it = componentList.iterator();
                while (it.hasNext()) {
                    this.mAntiMalComponentList.add(it.next());
                }
            }
        }
    }

    public boolean componentValid(String componentName) {
        if (HW_DEBUG) {
            Log.d(TAG, "componentValid componentName = " + componentName);
        }
        checkPermission(PERMISSION);
        if (TextUtils.isEmpty(componentName)) {
            return true;
        }
        synchronized (this.mAntiMalComponentList) {
            Iterator<AntiMalComponentInfo> it = this.mAntiMalComponentList.iterator();
            while (it.hasNext()) {
                AntiMalComponentInfo acpi = it.next();
                if (componentName.equals(acpi.mName)) {
                    boolean isNormal = acpi.isNormal();
                    return isNormal;
                }
            }
            return true;
        }
    }

    public int getSystemStatus() {
        if (HW_DEBUG) {
            Log.d(TAG, "getSystemStatus");
        }
        checkPermission(PERMISSION);
        return 0;
    }

    private void checkPermission(String permission) {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    /* access modifiers changed from: private */
    public void reportInner(int reporterID, Object data) {
        if (HW_DEBUG) {
            Log.d(TAG, "reportInner reporterID = " + reporterID + " boot: " + this.mIsBootCompleted + " mIsDBNotExist = " + this.mIsDBNotExist);
        }
        if (!this.mIsBootCompleted) {
            cache(reporterID, data);
        } else if (this.mIsDBNotExist) {
            saveDataToOeminfo(reporterID, data);
        } else {
            cache(reporterID, data);
            checkHwBigDataExist();
        }
    }

    private HwSecurityDiagnosePlugin(Context contxt) {
        this.mTryCount = 0;
        this.mIsDBNotExist = false;
        this.mAntiMalComponentList = new ArrayList<>();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.e(HwSecurityDiagnosePlugin.TAG, "mReceiver intent is NULL!");
                    return;
                }
                String action = intent.getAction();
                if (HwSecurityDiagnosePlugin.HW_DEBUG) {
                    Log.d(HwSecurityDiagnosePlugin.TAG, "onReceive ACTION : " + action);
                }
                if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                    boolean unused = HwSecurityDiagnosePlugin.this.mIsBootCompleted = true;
                    if (HwSecurityDiagnosePlugin.CHINA_RELEASE_VERSION) {
                        HwSecurityDiagnosePlugin.this.mMyHandler.removeMessages(1000);
                        HwSecurityDiagnosePlugin.this.mMyHandler.sendEmptyMessage(1001);
                    }
                    HwSecurityDiagnosePlugin.this.mMyHandler.sendEmptyMessage(1003);
                } else if (HwSecurityDiagnosePlugin.ROOT_ALARM_ACTION.equals(action)) {
                    if (HwSecurityDiagnosePlugin.HW_DEBUG) {
                        Log.d(HwSecurityDiagnosePlugin.TAG, "alarmManager trigger");
                    }
                    RootDetectReport.getInstance().triggerRootScan();
                }
            }
        };
        this.mMyHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1000:
                    case 1001:
                        HwSecurityDiagnosePlugin.this.checkHwBigDataExist();
                        return;
                    case 1002:
                        HwSecurityDiagnosePlugin.this.reportInner(msg.arg1, (Bundle) msg.obj);
                        return;
                    case 1003:
                        HwSecurityDiagnosePlugin.this.triggerRootCheck();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mRootReportListener = new RootDetectReport.Listener() {
            public void onRootReport(JSONObject json, boolean needReport) {
                HwSecurityDiagnosePlugin.this.onRootStatus(json);
                if (HwSecurityDiagnosePlugin.CHINA_RELEASE_VERSION && needReport) {
                    HwSecurityDiagnosePlugin.this.reportInner(102, json);
                }
            }
        };
        this.mContext = contxt;
    }

    private ArrayList<QueueParams> getQueue() {
        synchronized (mLock) {
            if (this.mQueue == null) {
                this.mQueue = new ArrayList<>();
            }
        }
        return this.mQueue;
    }

    private void cache(int reporterID, Object data) {
        synchronized (mLock) {
            getQueue().add(new QueueParams(reporterID, data));
        }
    }

    /* access modifiers changed from: private */
    public void triggerRootCheck() {
        new RootCheckTrigger().start();
        setTriggerTime();
    }

    private void retryCheck() {
        int i = this.mTryCount + 1;
        this.mTryCount = i;
        boolean needTry = i <= 10;
        if (HW_DEBUG) {
            Log.d(TAG, "retryCheck mTryCount = " + this.mTryCount);
        }
        if (needTry) {
            this.mMyHandler.sendEmptyMessageDelayed(1000, DELAY_INTERVAL);
            return;
        }
        this.mTryCount = 0;
        this.mIsDBNotExist = true;
        processQueue();
    }

    /* access modifiers changed from: private */
    public void checkHwBigDataExist() {
        if (ServiceManager.getService(BD_SERVICE_NAME) == null) {
            retryCheck();
            return;
        }
        this.mTryCount = 0;
        processQueue();
    }

    private void processQueue() {
        synchronized (mLock) {
            if (this.mQueue != null) {
                if (HW_DEBUG) {
                    Log.d(TAG, "processQueue SIZE = " + this.mQueue.size() + " mIsDBNotExist " + this.mIsDBNotExist);
                }
                if (this.mIsDBNotExist) {
                    Iterator<QueueParams> it = this.mQueue.iterator();
                    while (it.hasNext()) {
                        QueueParams params = it.next();
                        saveDataToOeminfo(params.mReporter, params.mArg);
                    }
                } else {
                    Iterator<QueueParams> it2 = this.mQueue.iterator();
                    while (it2.hasNext()) {
                        QueueParams params2 = it2.next();
                        sendDataToBD(params2.mReporter, params2.mArg);
                    }
                }
                this.mQueue.clear();
            }
        }
    }

    private void saveDataToOeminfo(int reportId, Object data) {
        if (HW_DEBUG) {
            Log.d(TAG, "saveDataToOeminfo reportId = " + reportId + "\n data : " + data);
        }
        switch (reportId) {
            case 100:
                sendAntiMalDataToOEMInfo((Bundle) data);
                return;
            case 101:
                sendRenewDataToOEMInfo((Bundle) data);
                return;
            case 102:
                sendRootCheckDataToOEMInfo((JSONObject) data);
                return;
            default:
                Log.e(TAG, "saveDataToOeminfo The ID is invalid!");
                return;
        }
    }

    private void sendDataToBD(int reportId, Object data) {
        if (HW_DEBUG) {
            Log.d(TAG, "sendDataToBD reportId = " + reportId);
        }
        switch (reportId) {
            case 100:
                sendAntiMalDataToBD((Bundle) data);
                return;
            case 101:
                sendRenewDataToBD((Bundle) data);
                return;
            case 102:
                sendRootCheckDataToBD((JSONObject) data);
                return;
            default:
                Log.e(TAG, "saveDataToOeminfo The ID is invalid!");
                return;
        }
    }

    private JSONObject parcelAntiMalBaseData(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "sendAntiMalDataToBD bundle is NULL!");
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(HwSecDiagnoseConstant.ANTIMAL_TIME, bundle.getString(HwSecDiagnoseConstant.ANTIMAL_TIME));
            json.put(HwSecDiagnoseConstant.ANTIMAL_ROOT_STATE, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_ROOT_STATE));
            json.put(HwSecDiagnoseConstant.ANTIMAL_FASTBOOT_STATE, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_FASTBOOT_STATE));
            json.put(HwSecDiagnoseConstant.ANTIMAL_SYSTEM_STATE, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_SYSTEM_STATE));
            json.put(HwSecDiagnoseConstant.ANTIMAL_MAL_COUNT, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_MAL_COUNT));
            json.put(HwSecDiagnoseConstant.ANTIMAL_DELETE_COUNT, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_DELETE_COUNT));
            json.put(HwSecDiagnoseConstant.ANTIMAL_TAMPER_COUNT, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_TAMPER_COUNT));
            json.put(HwSecDiagnoseConstant.ANTIMAL_SELINUX_STATE, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_SELINUX_STATE));
            json.put("SecVer", bundle.getString("SecVer"));
            json.put(HwSecDiagnoseConstant.ANTIMAL_SYSTEM_CUST_STATE, bundle.getInt(HwSecDiagnoseConstant.ANTIMAL_SYSTEM_CUST_STATE));
            json.put(HwSecDiagnoseConstant.ANTIMAL_USED_TIME, bundle.getString(HwSecDiagnoseConstant.ANTIMAL_USED_TIME));
            json.put("SecVer", bundle.getString("SecVer"));
            return json;
        } catch (Exception e) {
            Log.e(TAG, "parcelAntiMalData E:" + e);
            if (HW_DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendAntiMalDataToOEMInfo(Bundle bundle) {
        JSONObject json = parcelAntiMalBaseData(bundle);
        if (json != null) {
            try {
                byte[] antiMalArry = json.toString().getBytes(JSON_FORMAT_ENCODING);
                HwOEMInfoAdapter.writeByteArrayToOeminfo(HwSecDiagnoseConstant.OEMINFO_ID_ANTIMAL, antiMalArry.length, antiMalArry);
                if (HW_DEBUG) {
                    Log.d(TAG, "sendAntiMalDataToOEMInfo STR:" + json.toString() + " LEN = " + antiMalArry.length);
                }
            } catch (Exception e) {
                Log.e(TAG, "sendRenewDataToOEMInfo e :" + e);
                if (HW_DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(TAG, "sendAntiMalDataToOEMInfo JSON IS NULL!");
        }
    }

    private JSONObject apkInfoToJson(AntiMalApkInfo apkInfo) {
        JSONObject apkJson = new JSONObject();
        try {
            apkJson.put(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, apkInfo.mType);
            apkJson.put("PackageName", apkInfo.mPackageName);
            apkJson.put(HwSecDiagnoseConstant.ANTIMAL_APK_NAME, apkInfo.mApkName);
            apkJson.put(HwSecDiagnoseConstant.ANTIMAL_APK_PATH, apkInfo.mPath.replaceAll("\\/", "-"));
            apkJson.put(HwSecDiagnoseConstant.ANTIMAL_APK_VERSION, apkInfo.mVersion);
            apkJson.put(HwSecDiagnoseConstant.ANTIMAL_APK_LAST_MODIFY, apkInfo.mLastModifyTime);
            if (HW_DEBUG) {
                Log.d(TAG, "sendAntiMalDataToBD apkInfoToJson PATH: " + apkInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, "apkInfoToJson EXCEPTION = " + e);
            if (HW_DEBUG) {
                e.printStackTrace();
            }
        }
        return apkJson;
    }

    private void sendAntiMalDataToBD(Bundle bundle) {
        JSONObject json = parcelAntiMalBaseData(bundle);
        if (bundle == null || json == null) {
            Log.e(TAG, "sendAntiMalDataToBD bundle is NULL!");
            return;
        }
        try {
            ArrayList<AntiMalApkInfo> apkInfoList = bundle.getParcelableArrayList(HwSecDiagnoseConstant.ANTIMAL_APK_LIST);
            if (apkInfoList == null || apkInfoList.size() <= 0) {
                if (HW_DEBUG) {
                    Log.d(TAG, "sendAntiMalDataToBD The list is empty!");
                }
                Flog.bdReport(this.mContext, 121, json, 27);
            } else {
                int size = apkInfoList.size();
                int sendCnt = apkInfoList.size() / 5;
                for (int j = 0; j < sendCnt; j++) {
                    JSONArray jsonArry = new JSONArray();
                    for (int i = 0; i < 5; i++) {
                        AntiMalApkInfo apkInfo = apkInfoList.get((j * 5) + i);
                        if (apkInfo != null) {
                            jsonArry.put(apkInfoToJson(apkInfo));
                        }
                    }
                    json.put(HwSecDiagnoseConstant.ANTIMAL_APK_LIST, jsonArry);
                    if (HW_DEBUG) {
                        Log.d(TAG, "sendAntiMalDataToBD LENGTH = " + json.toString().length() + "\n ANTIMAL data : " + json.toString());
                    }
                    Flog.bdReport(this.mContext, 121, json, 27);
                }
                int other = size % 5;
                if (other > 0) {
                    JSONArray jsons = new JSONArray();
                    for (int left = size - other; left < size; left++) {
                        AntiMalApkInfo ai = apkInfoList.get(left);
                        if (ai != null) {
                            jsons.put(apkInfoToJson(ai));
                        }
                    }
                    json.put(HwSecDiagnoseConstant.ANTIMAL_APK_LIST, jsons);
                    if (HW_DEBUG) {
                        Log.d(TAG, "sendAntiMalDataToBD LEFT LENGTH = " + json.toString().length() + "\n LEFT ANTIMAL data : " + json.toString());
                    }
                    Flog.bdReport(this.mContext, 121, json, 27);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendAntiMalDataToBD EXCEPTION = " + e);
            if (HW_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject parcleRenewData(Bundle bundle) {
        if (bundle == null) {
            Log.e(TAG, "parcleRenewData bundle is NULL!");
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(HwSecDiagnoseConstant.DEVICE_RENEW_SN_CODE, bundle.getString(HwSecDiagnoseConstant.DEVICE_RENEW_SN_CODE));
            json.put(HwSecDiagnoseConstant.DEVICE_RENEW_TIME, bundle.getString(HwSecDiagnoseConstant.DEVICE_RENEW_TIME));
            return json;
        } catch (Exception e) {
            Log.e(TAG, "parcleRenewData E: " + e);
            if (HW_DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendRenewDataToBD(Bundle bundle) {
        JSONObject json = parcleRenewData(bundle);
        Flog.bdReport(this.mContext, 122, json, 27);
        if (HW_DEBUG) {
            Log.d(TAG, "sendRenewDataToBD data: " + json.toString());
        }
    }

    private void sendRenewDataToOEMInfo(Bundle bundle) {
        JSONObject json = parcleRenewData(bundle);
        if (json != null) {
            try {
                byte[] renewArry = json.toString().getBytes(JSON_FORMAT_ENCODING);
                HwOEMInfoAdapter.writeByteArrayToOeminfo(HwSecDiagnoseConstant.OEMINFO_ID_DEVICE_RENEW, renewArry.length, renewArry);
                if (HW_DEBUG) {
                    Log.d(TAG, "sendRenewDataToOEMInfo STR:" + json.toString() + " LEN = " + renewArry.length);
                }
            } catch (Exception e) {
                Log.e(TAG, "sendRenewDataToOEMInfo e :" + e);
                if (HW_DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(TAG, "sendRenewDataToOEMInfo JSON IS NULL!");
        }
    }

    private void sendRootCheckDataToBD(JSONObject json) {
        if (json != null) {
            Flog.bdReport(this.mContext, 123, json, 27);
            if (HW_DEBUG) {
                Log.d(TAG, "sendRootCheckDataToBD data: " + json.toString());
            }
        }
    }

    private void sendRootCheckDataToOEMInfo(JSONObject json) {
        if (json == null) {
            Log.e(TAG, "sendRootCheckDataToOEMInfo json is NULL!");
            return;
        }
        try {
            byte[] bootArry = json.toString().getBytes(JSON_FORMAT_ENCODING);
            HwOEMInfoAdapter.writeByteArrayToOeminfo(HwSecDiagnoseConstant.OEMINFO_ID_ROOT_CHECK, bootArry.length, bootArry);
            if (HW_DEBUG) {
                Log.d(TAG, "sendRootCheckDataToOEMInfo STR:" + json.toString() + " LEN = " + bootArry.length);
            }
        } catch (Exception e) {
            Log.e(TAG, "sendRootCheckDataToOEMInfo e :" + e);
            if (HW_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onRootStatus(JSONObject rootStatus) {
        int i;
        int rootResult;
        List<IHwSecurityDiagnoseCallback> copyList = new ArrayList<>();
        synchronized (mLockCallBackList) {
            copyList.addAll(mCallBackList);
            mCallBackList.clear();
            mTriggered = false;
            if (HW_DEBUG) {
                Log.d(TAG, "lock in onRootStatus");
            }
        }
        try {
            rootResult = rootStatus.getInt(HwSecDiagnoseConstant.ROOT_STATUS);
        } catch (JSONException e) {
            Log.e(TAG, "json object error");
            rootResult = -1;
        }
        int copyListSize = copyList.size();
        for (i = 0; i < copyListSize; i++) {
            try {
                if (HW_DEBUG) {
                    Log.d(TAG, "onRootStatus callbackID:" + i);
                }
                copyList.get(i).onRootStatus(rootResult);
            } catch (RemoteException e2) {
                Log.e(TAG, "callback error");
            }
        }
        copyList.clear();
        if (HW_DEBUG) {
            Log.d(TAG, "root result is : " + rootResult);
        }
    }

    public void getRootStatus(IHwSecurityDiagnoseCallback callback) {
        checkPermission(PERMISSION);
        boolean needTrigger = false;
        synchronized (mLockCallBackList) {
            if (!mTriggered) {
                needTrigger = true;
                mTriggered = true;
                if (HW_DEBUG) {
                    Log.d(TAG, "need to trigger root status");
                }
            }
            if (!mCallBackList.contains(callback)) {
                mCallBackList.add(callback);
                if (HW_DEBUG) {
                    Log.d(TAG, "getRootStatus list size:" + mCallBackList.size());
                }
            } else if (HW_DEBUG) {
                Log.d(TAG, "callback has in list,do not trigger again");
            }
        }
        if (needTrigger) {
            if (HW_DEBUG) {
                Log.d(TAG, "trigger root status");
            }
            triggerRootCheck();
        }
    }

    public int getRootStatusSync() {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().getRootStatusSync();
    }

    public int getSystemStatusSync() {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().getSystemStatusSync();
    }

    public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition_info) {
        checkPermission(PERMISSION);
        return AppLayerStpProxy.getInstance().sendThreatenInfo(id, status, credible, version, name, addition_info);
    }
}
