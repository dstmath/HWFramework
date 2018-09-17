package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;

public class DeviceHwSystemManager {
    private static final String POLICY_DISABLE_WIFI = "HwSystemManager-disable-wifiset";
    private static final String POLICY_SUPER_WHITE_LIST = "super-whitelist-hwsystemmanager";
    private static final String TAG = DeviceHwSystemManager.class.getSimpleName();
    private HwDevicePolicyManagerEx mManager = new HwDevicePolicyManagerEx();

    public ArrayList<String> getSuperWhiteListForHwSystemManger(ComponentName who) {
        Bundle bundle = this.mManager.getPolicy(who, POLICY_SUPER_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        return bundle.getStringArrayList("value");
    }

    public boolean setSuperWhiteListForHwSystemManger(ComponentName who, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.setPolicy(who, POLICY_SUPER_WHITE_LIST, bundle);
    }

    public boolean removeSuperWhiteListForHwSystemManger(ComponentName who, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.removePolicy(who, POLICY_SUPER_WHITE_LIST, bundle);
    }

    public boolean setDataSaverMode(ComponentName who, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mManager.setPolicy(who, "data-saver-mode", bundle);
    }

    public boolean getDataSaverMode(ComponentName who, boolean disable) {
        Bundle bundle = this.mManager.getPolicy(who, "data-saver-mode");
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }
}
