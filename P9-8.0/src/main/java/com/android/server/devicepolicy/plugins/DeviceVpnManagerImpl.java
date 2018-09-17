package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class DeviceVpnManagerImpl extends DevicePolicyPlugin {
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
        struct.addStruct(DISABLE_VPN, PolicyType.STATE, new String[]{"value"});
        struct.addStruct(SECURE_VPN, PolicyType.STATE, new String[]{"value"});
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
        return Stub.asInterface(ServiceManager.getService("connectivity"));
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onSetPolicy");
        if ((DISABLE_VPN.equals(policyName) || SECURE_VPN.equals(policyName)) && policyData.getBoolean("value") && effective) {
            try {
                getService().turnOffVpn("[Legacy VPN]", UserHandle.getCallingUserId());
                HwLog.i(TAG, "turnOff vpn success!");
            } catch (RemoteException e) {
                HwLog.i(TAG, "turnOff vpn fail!");
                return false;
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
}
