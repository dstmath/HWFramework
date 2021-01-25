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
    private static final String SECURE_VPN = "secure-vpn";
    public static final String TAG = "DeviceVpnManagerImpl";

    public DeviceVpnManagerImpl(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getVpnPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISABLE_VPN, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(SECURE_VPN, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_SHOW_VPN_PASSWORD, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
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
        } else if ((!DISABLE_VPN.equals(policyName) && !SECURE_VPN.equals(policyName)) || !policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE) || !isEffective) {
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

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        return true;
    }
}
