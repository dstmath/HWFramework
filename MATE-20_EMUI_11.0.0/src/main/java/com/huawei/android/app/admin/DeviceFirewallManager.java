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

    public boolean setGlobalProxy(ComponentName who, String hostName, int proxyPort, List<String> exclusionList) {
        Bundle bundle = new Bundle();
        if (TextUtils.isEmpty(hostName)) {
            bundle.putString("hostName", null);
            bundle.putString("proxyPort", null);
            bundle.putString("exclusionList", null);
            return this.mDpm.setPolicy(who, GLOBAL_PROXY, bundle);
        }
        bundle.putString("hostName", hostName);
        bundle.putString("proxyPort", Integer.toString(proxyPort));
        if (exclusionList != null) {
            bundle.putString("exclusionList", listToString(exclusionList));
        } else {
            bundle.putString("exclusionList", null);
        }
        boolean result = this.mDpm.setPolicy(who, GLOBAL_PROXY, bundle);
        Log.d(TAG, "setGlobalProxy and the result is: " + result);
        return result;
    }

    public List<String> getGlobalProxy(ComponentName who) {
        Bundle bundle = this.mDpm.getPolicy(who, GLOBAL_PROXY);
        List<String> result = new ArrayList<>();
        if (bundle != null) {
            return getGlobalProxyPara(bundle);
        }
        Log.d(TAG, "has not set the GlobalProxy, return empty list.");
        return result;
    }

    public boolean setPAC(ComponentName who, Uri mPacFileUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("pacFileUrl", (mPacFileUrl == null || Uri.EMPTY.equals(mPacFileUrl)) ? DeviceSettingsManager.EMPTY_STRING : mPacFileUrl.toString());
        boolean result = this.mDpm.setPolicy(who, PAC, bundle);
        Log.d(TAG, "setPAC and the result is: " + result);
        return result;
    }

    public String getPAC(ComponentName who) {
        Bundle bundle = this.mDpm.getPolicy(who, PAC);
        if (bundle != null) {
            return bundle.getString("pacFileUrl");
        }
        Log.d(TAG, "has not set the PAC, return null.");
        return null;
    }

    private String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            if (i == listSize - 1) {
                sb.append(list.get(i));
            } else {
                sb.append(list.get(i));
                sb.append(',');
            }
        }
        return sb.toString();
    }

    private List<String> getGlobalProxyPara(Bundle bundle) {
        String[] exclusionArray;
        List<String> result = new ArrayList<>();
        String hostName = bundle.getString("hostName");
        String proxyPort = bundle.getString("proxyPort");
        if (hostName == null && proxyPort == null) {
            Log.d(TAG, "hostName and proxyPort is null, return empty list.");
            return result;
        }
        String exclusionList = bundle.getString("exclusionList");
        result.add(hostName + ':' + proxyPort);
        if (exclusionList != null) {
            for (String str : exclusionList.split(",")) {
                result.add(str);
            }
        }
        return result;
    }
}
