package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import java.util.ArrayList;

public class DeviceP2PPlugin extends DevicePolicyPlugin {
    private static final String DEVICE_P2P_POLICY_ITEM_NAME = "wifi_p2p_item_policy_name";
    private static final String DEVICE_P2P_POLICY_ITEM_VALUE = "wifi_p2p_policy_item_value";
    private static final String LOCATION_CONTROL_PERMSSION = "com.huawei.permission.sec.MDM_WIFI";
    public static final String TAG = DeviceP2PPlugin.class.getSimpleName();
    private boolean isWiFiDirectConnected = false;
    private Channel mChannel = null;
    ConnectionInfoListener mConnectionInfoListener = null;
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;

    public DeviceP2PPlugin(Context context) {
        super(context);
        this.mContext = context;
        this.mConnectionInfoListener = new ConnectionInfoListener() {
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info != null && info.groupFormed) {
                    DeviceP2PPlugin.this.isWiFiDirectConnected = true;
                }
            }
        };
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct("wifi_p2p_item_policy_name", PolicyType.STATE, new String[]{DEVICE_P2P_POLICY_ITEM_VALUE});
        return struct;
    }

    public boolean onInit(PolicyStruct policyStruct) {
        if (policyStruct == null) {
            return false;
        }
        return true;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        this.mContext.enforceCallingOrSelfPermission(LOCATION_CONTROL_PERMSSION, "need permission com.huawei.permission.sec.MDM");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        if (policyName == null) {
            return false;
        }
        if ("wifi_p2p_item_policy_name".equals(policyName)) {
            disConnectWifiDirect();
        }
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean changed) {
        if (policyName == null) {
            return false;
        }
        if ("wifi_p2p_item_policy_name".equals(policyName)) {
            disConnectWifiDirect();
        }
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> arrayList) {
        return true;
    }

    private void disConnectWifiDirect() {
        if (this.mWifiP2pManager == null || this.mChannel == null) {
            this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
            this.mChannel = this.mWifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), null);
        }
        this.mWifiP2pManager.requestConnectionInfo(this.mChannel, this.mConnectionInfoListener);
        if (this.mWifiP2pManager != null && this.mChannel != null && this.isWiFiDirectConnected) {
            this.mWifiP2pManager.removeGroup(this.mChannel, new ActionListener() {
                public void onSuccess() {
                }

                public void onFailure(int reason) {
                }
            });
        }
    }
}
