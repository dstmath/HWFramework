package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;

public class DeviceFirewallManagerImpl extends DevicePolicyPlugin {
    private static final String ATTR_EXCLUSION_LIST = "exclusionList";
    private static final String ATTR_HOST_NAME = "hostName";
    private static final String ATTR_PAC_FILE_URL = "pacFileUrl";
    private static final String ATTR_PROXY_PORT = "proxyPort";
    private static final String GLOBAL_PROXY = "global-proxy";
    private static final String MDM_FIREWALL_PERMISSION = "com.huawei.permission.sec.MDM_FIREWALL";
    private static final String PAC = "PAC";
    private static final String TAG = "DeviceFirewallManagerImpl";

    public DeviceFirewallManagerImpl(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getFirewallPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(GLOBAL_PROXY, PolicyStruct.PolicyType.CONFIGURATION, new String[]{ATTR_HOST_NAME, ATTR_PROXY_PORT, ATTR_EXCLUSION_LIST});
        struct.addStruct(PAC, PolicyStruct.PolicyType.CONFIGURATION, new String[]{ATTR_PAC_FILE_URL});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission(MDM_FIREWALL_PERMISSION, "NEED MDM_FIREWALL PERMISSION");
        return true;
    }

    /* JADX INFO: finally extract failed */
    private boolean setGlobalProxyPolicy(Bundle policyData) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        String hostName = policyData.getString(ATTR_HOST_NAME);
        int proxyPort = 0;
        if (!(hostName == null || policyData.getString(ATTR_PROXY_PORT) == null)) {
            try {
                proxyPort = Integer.parseInt(policyData.getString(ATTR_PROXY_PORT));
            } catch (NumberFormatException e) {
                HwLog.e(TAG, "proxyPort : NumberFormatException");
            }
        }
        ProxyInfo proxyInfo = new ProxyInfo(hostName, proxyPort, policyData.getString(ATTR_EXCLUSION_LIST));
        long ident = Binder.clearCallingIdentity();
        try {
            connectivityManager.setGlobalProxy(proxyInfo);
            Binder.restoreCallingIdentity(ident);
            if (connectivityManager.getGlobalProxy() == null && hostName == null) {
                HwLog.i(TAG, "Set null proxy.");
                return true;
            } else if (proxyInfo.equals(connectivityManager.getGlobalProxy())) {
                return true;
            } else {
                HwLog.i(TAG, "Invalid or repeated proxy properties, ignoring.");
                return false;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private boolean setPacPolicy(Bundle policyData) {
        HwLog.i(TAG, "Set pac start.");
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        String pacFileUrl = policyData.getString(ATTR_PAC_FILE_URL);
        ProxyInfo proxyInfo = new ProxyInfo(pacFileUrl);
        long ident = Binder.clearCallingIdentity();
        try {
            if (TextUtils.isEmpty(pacFileUrl)) {
                connectivityManager.setGlobalProxy(null);
            } else {
                connectivityManager.setGlobalProxy(proxyInfo);
            }
            Binder.restoreCallingIdentity(ident);
            if (connectivityManager.getGlobalProxy() == null && TextUtils.isEmpty(pacFileUrl)) {
                HwLog.i(TAG, "Set null proxy.");
                return true;
            } else if (proxyInfo.equals(connectivityManager.getGlobalProxy())) {
                return true;
            } else {
                HwLog.i(TAG, "Invalid or repeated proxy properties, ignoring.");
                return false;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0032, code lost:
        if (r8.equals(com.android.server.devicepolicy.plugins.DeviceFirewallManagerImpl.GLOBAL_PROXY) != false) goto L_0x0036;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0040  */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onSetPolicy");
        boolean z = false;
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        }
        int hashCode = policyName.hashCode();
        if (hashCode != -1751360572) {
            if (hashCode == 78962 && policyName.equals(PAC)) {
                z = true;
                if (z) {
                    return setGlobalProxyPolicy(policyData);
                }
                if (!z) {
                    return true;
                }
                return setPacPolicy(policyData);
            }
        }
        z = true;
        if (z) {
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        HwDevicePolicyManagerEx policyManager = new HwDevicePolicyManagerEx();
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        Bundle bundle = policyManager.getPolicy((ComponentName) null, GLOBAL_PROXY);
        if (bundle == null || bundle.getString(ATTR_HOST_NAME) == null || bundle.getString(ATTR_PROXY_PORT) == null) {
            Bundle bundlePac = policyManager.getPolicy((ComponentName) null, PAC);
            if (bundlePac == null || bundlePac.getString(ATTR_PAC_FILE_URL) == null) {
                long ident = Binder.clearCallingIdentity();
                try {
                    connectivityManager.setGlobalProxy(null);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                ProxyInfo proxyInfo = new ProxyInfo(bundlePac.getString(ATTR_PAC_FILE_URL));
                long ident2 = Binder.clearCallingIdentity();
                try {
                    connectivityManager.setGlobalProxy(proxyInfo);
                } finally {
                    Binder.restoreCallingIdentity(ident2);
                }
            }
        } else {
            try {
                ProxyInfo proxyInfo2 = new ProxyInfo(bundle.getString(ATTR_HOST_NAME), Integer.parseInt(bundle.getString(ATTR_PROXY_PORT)), bundle.getString(ATTR_EXCLUSION_LIST));
                long ident3 = Binder.clearCallingIdentity();
                try {
                    connectivityManager.setGlobalProxy(proxyInfo2);
                } finally {
                    Binder.restoreCallingIdentity(ident3);
                }
            } catch (NumberFormatException e) {
                HwLog.e(TAG, "proxyPort : NumberFormatException");
            }
        }
    }
}
