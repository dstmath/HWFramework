package com.android.server.rms.iaware.dev;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.rms.iaware.dev.DevSchedFeatureBase;
import com.android.server.rms.iaware.dev.FeatureXmlConfigParserRt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BleSchedFeatureRt extends DevSchedFeatureBase {
    private static final int BLE_DEVICE_ID = 1;
    private static final int BLE_MODE_FIRST = -1;
    private static final int BLE_MODE_LAST = 3;
    private static final int BLE_MODE_NORAML = 2;
    private static final String DEFAULT_TAG = "Default";
    private static final String IGNORE_TAG = "IgnoreApp";
    private static final String IS_HOME = "IsHome";
    private static final String TAG = "BLESchedFeatureRT";
    private int mBleDefaultValue = 1;
    private AtomicBoolean mBleEnable = new AtomicBoolean(false);
    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        /* class com.android.server.rms.iaware.dev.BleSchedFeatureRt.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                AwareLog.e(BleSchedFeatureRt.TAG, "intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                AwareLog.e(BleSchedFeatureRt.TAG, "action is null");
            } else if ("android.bluetooth.adapter.action.BLE_STATE_CHANGED".equals(action)) {
                int newState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
                AwareLog.d(BleSchedFeatureRt.TAG, "newState: " + newState);
                if (newState == 10) {
                    BleSchedFeatureRt.this.mBleEnable.set(false);
                    BleSchedFeatureRt.this.removeBleControl();
                    AwareLog.d(BleSchedFeatureRt.TAG, "mBleEnable: " + BleSchedFeatureRt.this.mBleEnable.get());
                } else if (newState == 12) {
                    BleSchedFeatureRt.this.mBleEnable.set(true);
                    AwareLog.d(BleSchedFeatureRt.TAG, "mBleEnable: " + BleSchedFeatureRt.this.mBleEnable.get());
                }
            }
        }
    };
    private Context mContext;
    private boolean mEnable = false;
    private List<String> mExceptAppList = new ArrayList();
    private int mHomeMode = 1;
    private boolean mIsHomeCheck = false;
    private Map<Integer, Integer> mSceneMaps = new ArrayMap();

    public BleSchedFeatureRt(Context context, String name) {
        super(context);
        this.mContext = context;
        this.deviceId = 1;
        AwareLog.d(TAG, "create " + name + "BleSchedFeatureRt success");
    }

    private void init() {
        Context context = this.mContext;
        if (context == null) {
            AwareLog.e(TAG, "mContext is null, error!");
            return;
        }
        context.registerReceiver(this.mBleReceiver, new IntentFilter("android.bluetooth.adapter.action.BLE_STATE_CHANGED"));
        Object manager = this.mContext.getSystemService("bluetooth");
        if (!(manager instanceof BluetoothManager)) {
            AwareLog.e(TAG, "manager is not instance of BluetoothManager, error!");
            return;
        }
        BluetoothAdapter bleAdapter = ((BluetoothManager) manager).getAdapter();
        if (bleAdapter == null) {
            AwareLog.e(TAG, "bleAdapter is null, error!");
        } else {
            this.mBleEnable.set(bleAdapter.isEnabled());
        }
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public void handleTopAppChange(int pid, int uid, String pkg) {
        if (!this.mEnable || !this.mBleEnable.get()) {
            AwareLog.d(TAG, "ble off, return!");
        } else if (sScreenState == DevSchedFeatureBase.ScreenState.ScreenOff) {
            AwareLog.d(TAG, "screen off, return!");
        } else {
            handleTopApp(pkg, uid, pid);
        }
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public boolean handleScreenStateChange(DevSchedFeatureBase.ScreenState state) {
        if (!this.mEnable) {
            return true;
        }
        AwareLog.d(TAG, "sScreenState : " + sScreenState);
        if (state == DevSchedFeatureBase.ScreenState.ScreenOff) {
            removeBleControl();
        }
        if (state == DevSchedFeatureBase.ScreenState.ScreenOn) {
            sendCurrentDeviceMode();
        }
        return true;
    }

    private void addBleScene(int mode, Set<Integer> sceneIds) {
        if (!(sceneIds == null || sceneIds.size() == 0)) {
            for (Integer id : sceneIds) {
                this.mSceneMaps.put(id, Integer.valueOf(mode));
            }
        }
    }

    private void setBleOneRule(FeatureXmlConfigParserRt.ConfigRule rule) {
        if (DEFAULT_TAG.equals(rule.ruleName)) {
            if (rule.ruleMode != -1) {
                this.mBleDefaultValue = rule.ruleMode;
            }
        } else if (IGNORE_TAG.equals(rule.ruleName)) {
            if (rule.ruleScenePkgSets != null) {
                this.mExceptAppList.addAll(rule.ruleScenePkgSets);
            }
        } else if (IS_HOME.equals(rule.ruleName)) {
            this.mIsHomeCheck = true;
            this.mHomeMode = rule.ruleMode;
        } else {
            addBleScene(rule.ruleMode, rule.ruleSceneIdSets);
        }
    }

    private void setBleRule(List<FeatureXmlConfigParserRt.ConfigRule> ruleList) {
        for (FeatureXmlConfigParserRt.ConfigRule rule : ruleList) {
            if (rule != null) {
                setBleOneRule(rule);
            }
        }
    }

    private void initXml(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        List<FeatureXmlConfigParserRt.ConfigRule> ruleList = FeatureXmlConfigParserRt.getFirstRuleList(config);
        if (ruleList != null && ruleList.size() > 0) {
            this.mEnable = config.subSwitch;
            if (this.mEnable) {
                setBleRule(ruleList);
            }
        }
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public void readFeatureConfig(FeatureXmlConfigParserRt.FeatureXmlConfig config) {
        initXml(config);
        if (this.mEnable) {
            init();
        }
        AwareLog.d(TAG, "BLE SubSwitch " + this.mEnable);
    }

    private void handleTopApp(String processName, int uid, int pid) {
        HwActivityManagerService hwAms;
        if (processName == null || pid < 0) {
            AwareLog.i(TAG, "invalid appinfo, processName :" + processName + ", uid :" + uid + ", pid :" + pid);
        } else if (this.mExceptAppList.contains(processName)) {
            AwareLog.i(TAG, processName + " in exceptList, out of control.");
        } else {
            int mode = this.mBleDefaultValue;
            int appRecoType = AppTypeRecoManager.getInstance().getAppType(processName);
            Integer modeInt = this.mSceneMaps.get(Integer.valueOf(appRecoType));
            if (modeInt != null) {
                mode = modeInt.intValue();
            }
            if (this.mIsHomeCheck && (hwAms = HwActivityManagerService.self()) != null) {
                mode = hwAms.isLauncher(processName) ? this.mHomeMode : mode;
            }
            if (mode <= -1 || mode >= 3) {
                AwareLog.e(TAG, "getMode error! mode:" + mode);
                return;
            }
            DevSchedCallbackManager.getInstance().sendDeviceMode(this.deviceId, processName, uid, mode, null);
            AwareLog.d(TAG, "sendDeviceMode, packageName:" + processName + " type " + appRecoType + ", uid:" + uid + ", mode:" + mode);
        }
    }

    @Override // com.android.server.rms.iaware.dev.DevSchedFeatureBase
    public void sendCurrentDeviceMode() {
        if (!this.mEnable || !this.mBleEnable.get()) {
            AwareLog.d(TAG, "ble off, return!");
        } else if (sScreenState == DevSchedFeatureBase.ScreenState.ScreenOff) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeBleControl() {
        DevSchedCallbackManager.getInstance().sendDeviceMode(this.deviceId, null, 0, 2, null);
        AwareLog.d(TAG, "removeBleControl, deviceId:" + this.deviceId);
    }
}
