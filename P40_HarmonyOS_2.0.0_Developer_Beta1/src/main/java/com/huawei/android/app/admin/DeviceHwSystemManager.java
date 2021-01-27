package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;

public class DeviceHwSystemManager {
    private static final String POLICY_DISABLE_POWER_SAVER_MODE = "HwSystemManager-disable-powersavemode";
    private static final String POLICY_DISABLE_WIFI = "HwSystemManager-disable-wifiset";
    private static final String POLICY_ENTERPRISE_TRUST_LIST = "enterprise-trustlist-hwsystemmanager";
    private static final String POLICY_ENTERPRISE_WHITE_LIST = "enterprise-whitelist-hwsystemmanager";
    private static final String POLICY_SUPER_TRUST_LIST = "super-trustlist-hwsystemmanager";
    private static final String POLICY_SUPER_WHITE_LIST = "super-whitelist-hwsystemmanager";
    private static final String POLICY_VALUE = "value";
    private static final String TAG = DeviceHwSystemManager.class.getSimpleName();
    private HwDevicePolicyManagerEx mManager = new HwDevicePolicyManagerEx();

    @Deprecated
    public ArrayList<String> getSuperWhiteListForHwSystemManger(ComponentName admin) {
        return getSuperTrustListForHwSystemManger(admin);
    }

    @Deprecated
    public boolean setSuperWhiteListForHwSystemManger(ComponentName admin, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.setPolicy(admin, POLICY_SUPER_WHITE_LIST, bundle);
    }

    @Deprecated
    public boolean removeSuperWhiteListForHwSystemManger(ComponentName admin, ArrayList<String> list) {
        return removeSuperTrustListForHwSystemManger(admin, list);
    }

    public boolean removeSuperTrustListForHwSystemManger(ComponentName admin, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.removePolicy(admin, POLICY_SUPER_WHITE_LIST, bundle);
    }

    public boolean setSuperTrustListForHwSystemManger(ComponentName admin, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.setPolicy(admin, POLICY_SUPER_TRUST_LIST, bundle);
    }

    public ArrayList<String> getSuperTrustListForHwSystemManger(ComponentName admin) {
        Bundle bundle = this.mManager.getPolicy(admin, POLICY_SUPER_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getSuperTrustListForHwSystemManger exception.");
            return null;
        }
    }

    public boolean setDataSaverMode(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mManager.setPolicy(admin, "data-saver-mode", bundle);
    }

    public boolean getDataSaverMode(ComponentName admin, boolean isDisabled) {
        Bundle bundle = this.mManager.getPolicy(admin, "data-saver-mode");
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    @Deprecated
    public ArrayList<String> getEnterpriseWhiteList(ComponentName admin) {
        return getEnterpriseTrustList(admin);
    }

    @Deprecated
    public boolean addEnterpriseWhiteList(ComponentName admin, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.setPolicy(admin, POLICY_ENTERPRISE_WHITE_LIST, bundle);
    }

    @Deprecated
    public boolean removeEnterpriseWhiteList(ComponentName admin, ArrayList<String> list) {
        return removeEnterpriseTrustList(admin, list);
    }

    public boolean removeEnterpriseTrustList(ComponentName admin, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.removePolicy(admin, POLICY_ENTERPRISE_WHITE_LIST, bundle);
    }

    public boolean addEnterpriseTrustList(ComponentName admin, ArrayList<String> list) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", list);
        return this.mManager.setPolicy(admin, POLICY_ENTERPRISE_TRUST_LIST, bundle);
    }

    public ArrayList<String> getEnterpriseTrustList(ComponentName admin) {
        Bundle bundle = this.mManager.getPolicy(admin, POLICY_ENTERPRISE_WHITE_LIST);
        if (bundle == null) {
            return null;
        }
        try {
            return bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "getEnterpriseTrustList exception.");
            return null;
        }
    }

    public boolean setPowerSaveModeDisabled(ComponentName admin, Boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled.booleanValue());
        return this.mManager.setPolicy(admin, POLICY_DISABLE_POWER_SAVER_MODE, bundle);
    }

    public boolean isPowerSaveModeDisabled(ComponentName admin) {
        Bundle bundle = this.mManager.getPolicy(admin, POLICY_DISABLE_POWER_SAVER_MODE);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }
}
