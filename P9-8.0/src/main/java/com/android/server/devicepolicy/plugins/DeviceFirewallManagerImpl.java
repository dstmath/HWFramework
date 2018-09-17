package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;

public class DeviceFirewallManagerImpl extends DevicePolicyPlugin {
    private static final String GLOBAL_PROXY = "global-proxy";
    private static final String MDM_FIREWALL_PERMISSION = "com.huawei.permission.sec.MDM_FIREWALL";
    public static final String TAG = "DeviceFirewallManagerImpl";

    public DeviceFirewallManagerImpl(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getFirewallPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(GLOBAL_PROXY, PolicyType.CONFIGURATION, new String[]{"hostName", "proxyPort", "exclusionList"});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        HwLog.i(TAG, "onInit");
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(MDM_FIREWALL_PERMISSION, "NEED MDM_FIREWALL PERMISSION");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onSetPolicy");
        if (GLOBAL_PROXY.equals(policyName)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            String hostName = policyData.getString("hostName");
            int proxyPort = 0;
            if (!(hostName == null || policyData.getString("proxyPort") == null)) {
                proxyPort = Integer.parseInt(policyData.getString("proxyPort"));
            }
            ProxyInfo proxyInfo = new ProxyInfo(hostName, proxyPort, policyData.getString("exclusionList"));
            long ident = Binder.clearCallingIdentity();
            try {
                connectivityManager.setGlobalProxy(proxyInfo);
                if (connectivityManager.getGlobalProxy() == null && hostName == null) {
                    HwLog.i(TAG, "Set null proxy.");
                    return true;
                } else if (!proxyInfo.equals(connectivityManager.getGlobalProxy())) {
                    HwLog.i(TAG, "Invalid or repeated proxy properties, ignoring.");
                    return false;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyItem> arrayList) {
        long ident;
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, GLOBAL_PROXY);
        if (bundle == null || bundle.getString("hostName") == null || bundle.getString("proxyPort") == null) {
            ident = Binder.clearCallingIdentity();
            try {
                connectivityManager.setGlobalProxy(null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            ProxyInfo proxyInfo = new ProxyInfo(bundle.getString("hostName"), Integer.parseInt(bundle.getString("proxyPort")), bundle.getString("exclusionList"));
            ident = Binder.clearCallingIdentity();
            try {
                connectivityManager.setGlobalProxy(proxyInfo);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }
}
