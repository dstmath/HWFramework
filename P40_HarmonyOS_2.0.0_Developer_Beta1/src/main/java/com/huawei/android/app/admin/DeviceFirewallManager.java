package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;

public class DeviceFirewallManager {
    private static final String GLOBAL_PROXY = "global-proxy";
    private static final String PAC = "PAC";
    private static final String TAG = "DeviceFirewallManager";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setGlobalProxy(ComponentName admin, String hostName, int proxyPort, List<String> exclusionList) {
        Bundle bundle = new Bundle();
        if (TextUtils.isEmpty(hostName)) {
            bundle.putString("hostName", null);
            bundle.putString("proxyPort", null);
            bundle.putString("exclusionList", null);
            return this.mDpm.setPolicy(admin, GLOBAL_PROXY, bundle);
        }
        bundle.putString("hostName", hostName);
        bundle.putString("proxyPort", Integer.toString(proxyPort));
        if (exclusionList != null) {
            bundle.putString("exclusionList", listToString(exclusionList));
        } else {
            bundle.putString("exclusionList", null);
        }
        boolean isSuccess = this.mDpm.setPolicy(admin, GLOBAL_PROXY, bundle);
        Log.d(TAG, "setGlobalProxy and the result is: " + isSuccess);
        return isSuccess;
    }

    public List<String> getGlobalProxy(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, GLOBAL_PROXY);
        List<String> results = new ArrayList<>();
        if (bundle != null) {
            return getGlobalProxyPara(bundle);
        }
        Log.d(TAG, "has not set the GlobalProxy, return empty list.");
        return results;
    }

    public boolean setPAC(ComponentName admin, Uri pacFileUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("pacFileUrl", (pacFileUrl == null || Uri.EMPTY.equals(pacFileUrl)) ? "" : pacFileUrl.toString());
        boolean isSuccess = this.mDpm.setPolicy(admin, PAC, bundle);
        Log.d(TAG, "setPAC and the result is: " + isSuccess);
        return isSuccess;
    }

    public String getPAC(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, PAC);
        if (bundle != null) {
            return bundle.getString("pacFileUrl");
        }
        Log.d(TAG, "has not set the PAC, return null.");
        return null;
    }

    private String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        int listSize = list.size();
        int lastIndex = listSize - 1;
        for (int i = 0; i < listSize; i++) {
            if (i == lastIndex) {
                sb.append(list.get(i));
            } else {
                sb.append(list.get(i));
                sb.append(',');
            }
        }
        return sb.toString();
    }

    private List<String> getGlobalProxyPara(Bundle bundle) {
        List<String> paraLists = new ArrayList<>();
        String hostName = bundle.getString("hostName");
        String proxyPort = bundle.getString("proxyPort");
        if (hostName == null && proxyPort == null) {
            Log.d(TAG, "hostName and proxyPort is null, return empty list.");
            return paraLists;
        }
        paraLists.add(hostName + ':' + proxyPort);
        String exclusionStrs = bundle.getString("exclusionList");
        if (exclusionStrs != null) {
            for (String item : exclusionStrs.split(",")) {
                paraLists.add(item);
            }
        }
        return paraLists;
    }
}
