package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.net.IConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class DeviceVpnManagerImpl extends DevicePolicyPlugin {
    private static final String DISABLE_SHOW_VPN_PASSWORD = "disable_show_vpn_password";
    private static final String DISABLE_VPN = "disable-vpn";
    private static final String MDM_VPN_PERMISSION = "com.huawei.permission.sec.MDM_VPN";
    private static final String NODE_VALUE = "value";
    private static final String SECURE_VPN = "secure-vpn";
    private static final String TAG = "DeviceVpnManagerImpl";

    public DeviceVpnManagerImpl(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getVpnPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISABLE_VPN, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(SECURE_VPN, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_SHOW_VPN_PASSWORD, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "enforceCallingOrSelfPermission");
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        return true;
    }

    private static IConnectivityManager getService() {
        return IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onSetPolicy");
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        } else if ((!DISABLE_VPN.equals(policyName) && !SECURE_VPN.equals(policyName)) || !policyData.getBoolean("value") || !isEffective) {
            return true;
        } else {
            try {
                getService().turnOffVpn("[Legacy VPN]", UserHandle.getCallingUserId());
                HwLog.i(TAG, "turnOff vpn success!");
                return true;
            } catch (RemoteException e) {
                HwLog.i(TAG, "turnOff vpn fail!");
                return false;
            }
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }
}
