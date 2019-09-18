package com.android.server.rms.iaware.dev;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.rms.iaware.dev.DevSchedFeatureBase;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BLESchedFeatureRT extends DevSchedFeatureBase {
    private static final int BLE_MODE_FIRST = -1;
    private static final int BLE_MODE_LAST = 3;
    private static final int BLE_MODE_LOW = 1;
    private static final int BLE_MODE_NORAML = 2;
    private static final int BLE_MODE_STOP = 0;
    private static final String DEVICE_NAME = "ble_iconnect_nearby";
    private static final int INVALID_MODE = -2;
    private static final String ITEM_MODE = "mode";
    private static final String TAG = "BLESchedFeatureRT";
    private static final List<String> mExceptAppList = new ArrayList();
    private static final List<SceneInfo> mSceneList = new ArrayList();
    /* access modifiers changed from: private */
    public AtomicBoolean mBleEnable = new AtomicBoolean(false);
    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                AwareLog.e(BLESchedFeatureRT.TAG, "intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                AwareLog.e(BLESchedFeatureRT.TAG, "action is null");
                return;
            }
            if (action.equals("android.bluetooth.adapter.action.BLE_STATE_CHANGED")) {
                int newState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
                AwareLog.d(BLESchedFeatureRT.TAG, "newState: " + newState);
                if (10 == newState) {
                    BLESchedFeatureRT.this.mBleEnable.set(false);
                    BLESchedFeatureRT.this.removeBleControl();
                } else if (12 == newState) {
                    BLESchedFeatureRT.this.mBleEnable.set(true);
                }
                AwareLog.d(BLESchedFeatureRT.TAG, "mBleEnable: " + BLESchedFeatureRT.this.mBleEnable.get());
            }
        }
    };
    private Context mContext;
    private final DevXmlConfig mDevXmlConfig = new DevXmlConfig();

    public BLESchedFeatureRT(Context context, String name) {
        super(context);
        this.mContext = context;
        readCustConfig();
        init();
        AwareLog.d(TAG, "create " + name + "BLESchedFeatureRT success");
    }

    private void init() {
        if (this.mContext == null) {
            AwareLog.e(TAG, "mContext is null, error!");
            return;
        }
        this.mContext.registerReceiver(this.mBleReceiver, new IntentFilter("android.bluetooth.adapter.action.BLE_STATE_CHANGED"));
        BluetoothManager bleManager = (BluetoothManager) this.mContext.getSystemService("bluetooth");
        if (bleManager == null) {
            AwareLog.e(TAG, "bleManager is null, error!");
            return;
        }
        BluetoothAdapter bleAdapter = bleManager.getAdapter();
        if (bleAdapter == null) {
            AwareLog.e(TAG, "bleAdapter is null, error!");
        } else {
            this.mBleEnable.set(bleAdapter.isEnabled());
        }
    }

    private void readCustConfig() {
        this.mDevXmlConfig.readSceneInfos(DEVICE_NAME, mSceneList);
        this.mDevXmlConfig.readExceptApps(DEVICE_NAME, mExceptAppList);
        this.mDeviceId = this.mDevXmlConfig.readDeviceId(DEVICE_NAME);
    }

    public boolean handleResAppData(long timestamp, int event, AttrSegments attrSegments) {
        if (event != 15020) {
            return false;
        }
        if (!this.mBleEnable.get()) {
            AwareLog.d(TAG, "ble off, return!");
            return false;
        } else if (DevSchedFeatureBase.ScreenState.ScreenOff == mScreenState) {
            AwareLog.d(TAG, "screen off, return!");
            return false;
        } else {
            handleAppToTopEvent(attrSegments, event);
            return true;
        }
    }

    public boolean handScreenStateChange(DevSchedFeatureBase.ScreenState state) {
        AwareLog.d(TAG, "mScreenState : " + mScreenState);
        if (state == DevSchedFeatureBase.ScreenState.ScreenOff) {
            removeBleControl();
        } else if (state == DevSchedFeatureBase.ScreenState.ScreenOn) {
            sendCurrentDeviceMode();
        }
        return true;
    }

    private void handleAppToTopEvent(AttrSegments attrSegments, int event) {
        if (attrSegments == null || !attrSegments.isValid()) {
            AwareLog.e(TAG, "attrSegments is Illegal, attrSegments is " + attrSegments);
            return;
        }
        ArrayMap<String, String> appInfo = attrSegments.getSegment("calledApp");
        if (appInfo == null) {
            AwareLog.i(TAG, "appInfo is NULL");
            return;
        }
        try {
            handleTopApp(appInfo.get("processName"), Integer.parseInt(appInfo.get("uid")), Integer.parseInt(appInfo.get("pid")));
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "get uid or pid fail, happend NumberFormatException");
        }
    }

    private void handleTopApp(String processName, int uid, int pid) {
        if (processName == null || pid < 0) {
            AwareLog.i(TAG, "invalid appinfo, processName :" + processName + ", uid :" + uid + ", pid :" + pid);
        } else if (mExceptAppList.contains(processName)) {
            AwareLog.i(TAG, processName + " in exceptList, out of control.");
        } else {
            SceneInfo scene = DevSchedUtil.getSceneInfo(15020, mSceneList);
            if (scene == null) {
                AwareLog.i(TAG, "no EVENT_APP_TO_TOP object.");
                return;
            }
            int index = scene.isMatch(processName, Integer.valueOf(uid), Integer.valueOf(pid));
            if (index >= 0) {
                int mode = getMode(scene, index);
                if (mode <= -1 || mode >= 3) {
                    AwareLog.e(TAG, "getMode error! mode:" + mode);
                    return;
                }
                DevSchedCallbackManager.getInstance().sendDeviceMode(this.mDeviceId, processName, uid, mode, null);
                AwareLog.d(TAG, "sendDeviceMode,  packageName:" + processName + ", uid:" + uid + ", mode:" + mode);
            } else {
                removeBleControl();
            }
        }
    }

    private int getMode(SceneInfo scene, int index) {
        if (scene == null) {
            AwareLog.e(TAG, "scene is null, error!");
            return -2;
        }
        String modeOrg = scene.getRuleItemValue("mode", index);
        if (modeOrg == null) {
            AwareLog.e(TAG, "mode is null, error!");
            return -2;
        }
        try {
            return Integer.parseInt(modeOrg.trim());
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "mode is not Integer, error, mode:" + modeOrg);
            return -2;
        }
    }

    public void sendCurrentDeviceMode() {
        if (!this.mBleEnable.get()) {
            AwareLog.d(TAG, "ble off, return!");
        } else if (DevSchedFeatureBase.ScreenState.ScreenOff == mScreenState) {
            AwareLog.d(TAG, "screen off, return!");
        } else {
            String topApp = DevSchedUtil.getTopFrontApp(this.mContext);
            if (topApp == null) {
                AwareLog.i(TAG, "topApp is null.");
                return;
            }
            AwareLog.d(TAG, "sendCurrentDeviceMode, topApp:" + topApp);
            handleTopApp(topApp, DevSchedUtil.getUidByPkgName(topApp), 0);
        }
    }

    public boolean handleUpdateCustConfig() {
        removeBleControl();
        clearCacheInfo();
        readCustConfig();
        AwareLog.d(TAG, "ble cust config update completed, mDeviceId:" + this.mDeviceId + ", mSceneList :" + mSceneList + ", mExceptAppList :" + mExceptAppList);
        return true;
    }

    /* access modifiers changed from: private */
    public void removeBleControl() {
        DevSchedCallbackManager.getInstance().sendDeviceMode(this.mDeviceId, null, 0, 2, null);
        AwareLog.d(TAG, "removeBleControl,  mDeviceId:" + this.mDeviceId);
    }

    private void clearCacheInfo() {
        mSceneList.clear();
        mExceptAppList.clear();
    }

    public boolean handlerNaviStatus(boolean isInNavi) {
        return true;
    }
}
