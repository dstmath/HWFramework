package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;

public class DeviceP2PPlugin extends DevicePolicyPlugin {
    private static final String DEVICE_P2P_POLICY_ITEM_NAME = "wifi_p2p_item_policy_name";
    private static final String DEVICE_P2P_POLICY_ITEM_VALUE = "wifi_p2p_policy_item_value";
    private static final String LOCATION_CONTROL_PERMSSION = "com.huawei.permission.sec.MDM_WIFI";
    public static final String TAG = DeviceP2PPlugin.class.getSimpleName();
    private boolean isWiFiDirectConnected = false;
    private WifiP2pManager.Channel mChannel = null;
    WifiP2pManager.ConnectionInfoListener mConnectionInfoListener = null;
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;

    public DeviceP2PPlugin(Context context) {
        super(context);
        this.mContext = context;
        this.mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            /* class com.android.server.devicepolicy.plugins.DeviceP2PPlugin.AnonymousClass1 */

            @Override // android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
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
        struct.addStruct(DEVICE_P2P_POLICY_ITEM_NAME, PolicyStruct.PolicyType.STATE, new String[]{DEVICE_P2P_POLICY_ITEM_VALUE});
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

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        if (policyName == null) {
            return false;
        }
        if (!DEVICE_P2P_POLICY_ITEM_NAME.equals(policyName)) {
            return true;
        }
        disConnectWifiDirect();
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        if (policyName == null) {
            return false;
        }
        if (!DEVICE_P2P_POLICY_ITEM_NAME.equals(policyName)) {
            return true;
        }
        disConnectWifiDirect();
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> arrayList) {
        return true;
    }

    private void disConnectWifiDirect() {
        this.mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        WifiP2pManager wifiP2pManager = this.mWifiP2pManager;
        if (wifiP2pManager != null) {
            if (this.mChannel == null) {
                Context context = this.mContext;
                this.mChannel = wifiP2pManager.initialize(context, context.getMainLooper(), null);
            }
            this.mWifiP2pManager.requestConnectionInfo(this.mChannel, this.mConnectionInfoListener);
            WifiP2pManager.Channel channel = this.mChannel;
            if (channel != null && this.isWiFiDirectConnected) {
                this.mWifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                    /* class com.android.server.devicepolicy.plugins.DeviceP2PPlugin.AnonymousClass2 */

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onSuccess() {
                    }

                    @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
                    public void onFailure(int reason) {
                    }
                });
            }
        }
    }
}
