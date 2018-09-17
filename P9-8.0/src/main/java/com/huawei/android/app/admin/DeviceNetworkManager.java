package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceNetworkManager {
    private static final String POLICY_NETWORK_BLACK_LIST = "network-black-list";
    private static final String TAG = "DeviceNetworkManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

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

    public boolean addNetworkAccessBlackList(ComponentName admin, ArrayList<String> addDomainList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", addDomainList);
        return this.mDpm.setPolicy(admin, POLICY_NETWORK_BLACK_LIST, bundle);
    }

    public boolean removeNetworkAccessBlackList(ComponentName admin, ArrayList<String> removeDomainList) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("value", removeDomainList);
        return this.mDpm.removePolicy(admin, POLICY_NETWORK_BLACK_LIST, bundle);
    }

    public List<String> getNetworkAccessBlackList(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_NETWORK_BLACK_LIST);
        ArrayList<String> lists = new ArrayList();
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return lists;
    }

    public List<String> queryBrowsingHistory(ComponentName admin) {
        Bundle bundle = this.mDpm.getCustomPolicy(admin, "queryBrowsingHistory", null);
        ArrayList<String> lists = new ArrayList();
        if (bundle != null) {
            return bundle.getStringArrayList("value");
        }
        return lists;
    }
}
