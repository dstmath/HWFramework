package com.huawei.android.app.admin;

import android.content.ComponentName;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.List;
import java.util.Map;

public class DeviceNetworkManager {
    private static final String TAG = "DeviceNetworkManager";
    private final HwDevicePolicyManagerEx mDpm;

    public DeviceNetworkManager() {
        this.mDpm = new HwDevicePolicyManagerEx();
    }

    public void addApn(ComponentName admin, Map<String, String> apnInfo) {
        this.mDpm.addApn(admin, apnInfo);
    }

    public void deleteApn(ComponentName admin, String apnId) {
        this.mDpm.deleteApn(admin, apnId);
    }

    public void updateApn(ComponentName admin, Map<String, String> apnInfo, String apnId) {
        this.mDpm.updateApn(admin, apnInfo, apnId);
    }

    public void setPreferApn(ComponentName admin, String apnId) {
        this.mDpm.setPreferApn(admin, apnId);
    }

    public List<String> queryApn(ComponentName admin, Map<String, String> apnInfo) {
        return this.mDpm.queryApn(admin, apnInfo);
    }

    public Map<String, String> getApnInfo(ComponentName admin, String apnId) {
        return this.mDpm.getApnInfo(admin, apnId);
    }

    public void addNetworkAccessWhitelist(ComponentName admin, List<String> addrList) {
        this.mDpm.addNetworkAccessWhitelist(admin, addrList);
    }

    public void removeNetworkAccessWhitelist(ComponentName admin, List<String> addrList) {
        this.mDpm.removeNetworkAccessWhitelist(admin, addrList);
    }

    public List<String> getNetworkAccessWhitelist(ComponentName admin) {
        return this.mDpm.getNetworkAccessWhitelist(admin);
    }
}
