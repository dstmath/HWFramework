package com.android.server.emcom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.emcom.IEmcomManager.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.emcom.grabservice.AppInfo;
import com.android.server.emcom.grabservice.AppInfo.EventInfo;
import com.android.server.emcom.grabservice.AutoGrabService;
import com.android.server.emcom.grabservice.EmcomConfigParser;
import com.android.server.emcom.grabservice.NotificationListener;
import com.android.server.emcom.util.EMCOMConstants;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.Map.Entry;

public class EmcomManagerService extends SystemService implements EMCOMConstants {
    private static final String APP_NAMES = "applist";
    private static final String BASTET_ACC_APP_LIST_ACTION = "huawei.intent.action.ACC_APP_LIST_ACTION";
    private static final String EMCOM_CONFIG_UPDATE_ACTION = "huawei.intent.action.PUSH_HW_BASTET_CONFIG_ACTION";
    private static final long READ_CONFIG_DELAY = 10000;
    private static final String SYSTEM_PACKAGE_NAME = "android";
    static final String TAG = "EmcomManagerService";
    private final Context mContext;
    private ArrayList<String> mEmcomAppList;
    private EmcomConfigParser mEmcomConfigParser;
    private EmcomManagerReceiver mEmcomManagerReceiver;
    private Handler mHandler;

    private final class BinderService extends Stub {
        private BinderService() {
        }

        public void init() {
        }
    }

    private class ConfigUpdateRunable implements Runnable {
        private ConfigUpdateRunable() {
        }

        public void run() {
            EmcomManagerService.this.parseXmlConfig();
        }
    }

    private class EmcomHandle extends Handler {
        public EmcomHandle(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                    EmcomManagerService.this.doBroadcastMessage(msg);
                case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                    EmcomManagerService.this.updateEmcomConfig();
                default:
            }
        }
    }

    private class EmcomManagerReceiver extends BroadcastReceiver {
        private EmcomManagerReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    int actionId = 0;
                    if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                        actionId = 1;
                    } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                        actionId = 2;
                    } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        actionId = 3;
                    } else if (action.equals(EmcomManagerService.EMCOM_CONFIG_UPDATE_ACTION)) {
                        actionId = 4;
                    }
                    if (actionId != 0) {
                        Message msg = EmcomManagerService.this.mHandler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = actionId;
                        switch (actionId) {
                            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                                msg.obj = intent.getDataString().substring(8);
                                break;
                            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                                Log.d(EmcomManagerService.TAG, "receive update config broadcast.");
                                break;
                        }
                        EmcomManagerService.this.mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    public EmcomManagerService(Context context) {
        super(context);
        this.mEmcomAppList = new ArrayList();
        this.mContext = context;
    }

    public void onStart() {
        Log.d(TAG, "EmcomManagerService onstart");
        initBroadcastReceiver();
        this.mHandler = new EmcomHandle(EmcomThread.getInstanceLooper());
        this.mContext.startService(new Intent(this.mContext, AutoGrabService.class));
        this.mContext.startService(new Intent(this.mContext, NotificationListener.class));
        publishBinderService("EmcomManager", new BinderService());
    }

    public void onBootPhase(int phase) {
        Log.d(TAG, "EmcomManagerService phase = " + phase);
        if (phase == IOTController.TYPE_MASTER) {
            updateEmcomConfig();
        }
    }

    private void parseXmlConfig() {
        this.mEmcomConfigParser = EmcomConfigParser.getInstance();
        if (this.mEmcomConfigParser.parse()) {
            Log.d(TAG, "emcom config parser parse success.");
            onUpdateAppList(this.mEmcomConfigParser.getAppPackageNames());
            for (AppInfo appInfo : this.mEmcomConfigParser.getAppInfos()) {
                checkVersionAndSendConfig(appInfo);
            }
        }
    }

    private void checkVersionAndSendConfig(AppInfo appInfo) {
        String pkgName = appInfo.getPackageName();
        appInfo.effectiveEvents.clear();
        int uid = getAppUid(pkgName);
        if (uid > 0) {
            appInfo.setUid(uid);
            String version = getInstallVersion(pkgName);
            if (!TextUtils.isEmpty(version)) {
                addEffectiveEvents(appInfo, uid, version);
                setEffectiveAutograbParam(appInfo, version);
            }
            if (!appInfo.effectiveEvents.isEmpty() || !TextUtils.isEmpty(appInfo.effectiveAutograbParam)) {
                Log.d(TAG, "need to send config to grabService");
                onUpdateSendConfig(appInfo);
                return;
            }
            return;
        }
        onUpdateDeleteConfig(pkgName);
    }

    private void setEffectiveAutograbParam(AppInfo appInfo, String version) {
        appInfo.setEffectiveAutograbParam(null);
        for (Entry<String, String> entry : appInfo.autograbParams.entrySet()) {
            if (version.matches((String) entry.getKey())) {
                appInfo.setEffectiveAutograbParam((String) entry.getValue());
                return;
            }
        }
    }

    private void addEffectiveEvents(AppInfo appInfo, int uid, String version) {
        appInfo.effectiveEvents.clear();
        for (EventInfo event : appInfo.getEvents()) {
            if (version.matches(event.version)) {
                event.uid = uid;
                appInfo.effectiveEvents.add(event);
                Log.d(TAG, "addEffectiveEvents");
            }
        }
    }

    private String getInstallVersion(String packageName) {
        String versionName = null;
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.i(TAG, "app is not install");
            return versionName;
        }
    }

    private int getAppUid(String packageName) {
        int uid = -1;
        try {
            return this.mContext.getPackageManager().getApplicationInfo(packageName, 1).uid;
        } catch (NameNotFoundException e) {
            Log.i(TAG, "app is not install");
            return uid;
        }
    }

    private void initBroadcastReceiver() {
        this.mEmcomManagerReceiver = new EmcomManagerReceiver();
        initPackageReceiver();
        initConfigUpdateReceiver();
    }

    private void initPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
        this.mContext.registerReceiver(this.mEmcomManagerReceiver, filter);
    }

    private void initConfigUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(EMCOM_CONFIG_UPDATE_ACTION);
        this.mContext.registerReceiver(this.mEmcomManagerReceiver, filter);
    }

    private void doBroadcastMessage(Message msg) {
        int actionId = msg.arg1;
        String name = msg.obj;
        Log.d(TAG, "doBroadcastMessage  action id: " + actionId + "  name = " + name);
        switch (actionId) {
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                handlerPackageChanged(actionId, name);
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_TRANSITING /*4*/:
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), READ_CONFIG_DELAY);
                Log.d(TAG, "update config after 10 seconds.");
            default:
                Log.e(TAG, "Unknown action id: " + actionId);
        }
    }

    private void updateEmcomConfig() {
        Log.d(TAG, "update emcom config");
        this.mHandler.post(new ConfigUpdateRunable());
    }

    private void handlerPackageChanged(int type, String packageName) {
        Log.d(TAG, "handlerPackageChanged  type = " + type + " packageName = " + packageName);
        if (this.mEmcomAppList.contains(packageName)) {
            AppInfo appInfo = this.mEmcomConfigParser.getAppInfoByPackageName(packageName);
            if (appInfo != null) {
                checkVersionAndSendConfig(appInfo);
            }
        }
    }

    public void onUpdateSendConfig(AppInfo info) {
        sendToGrabService(6, info);
    }

    public void onUpdateDeleteConfig(String packageName) {
        sendToGrabService(5, packageName);
    }

    public void onUpdateAppList(ArrayList<String> applist) {
        if (applist == null || applist.isEmpty()) {
            Log.e(TAG, "app list empty.");
            return;
        }
        this.mEmcomAppList.clear();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < applist.size(); i++) {
            String str = (String) applist.get(i);
            Log.d(TAG, "  processReport onUpdateAppList str  = " + str);
            this.mEmcomAppList.add(str);
            buffer.append(str).append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
            sendToGrabService(4, buffer.toString());
            sendToBastetService(buffer.toString());
        }
    }

    private void sendToBastetService(String pkgNames) {
        Intent intent = new Intent(BASTET_ACC_APP_LIST_ACTION);
        Bundle bundle = new Bundle();
        bundle.putString(APP_NAMES, pkgNames);
        intent.putExtras(bundle);
        intent.setPackage(SYSTEM_PACKAGE_NAME);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendToGrabService(int what, Object obj) {
        Handler handler = AutoGrabService.getHandler();
        if (handler == null) {
            Log.w(TAG, "AutoGrabService handler is null.");
        } else {
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }
}
