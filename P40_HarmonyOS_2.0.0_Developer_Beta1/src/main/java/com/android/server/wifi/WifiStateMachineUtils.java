package com.android.server.wifi;

import android.net.DhcpResults;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Message;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;

public class WifiStateMachineUtils extends EasyInvokeUtils {
    public static final int GOOD_LINK_DETECTED = 4;
    public static final int POOR_LINK_DETECTED = 3;
    static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    FieldObject<Integer> NETWORK_STATUS_UNWANTED_VALIDATION_FAILED;
    MethodObject<Void> getAdditionalWifiServiceInterfaces;
    MethodObject<Void> handleIPv4Success;
    FieldObject<DhcpResults> mDhcpResults;
    FieldObject<State> mDisconnectedState;
    FieldObject<String> mInterfaceName;
    FieldObject<Integer> mLastNetworkId;
    FieldObject<HwNetworkAgent> mNetworkAgent;
    FieldObject<NetworkInfo> mNetworkInfo;
    FieldObject<Integer> mOperationalMode;
    FieldObject<ScanRequestProxy> mScanRequestProxy;
    FieldObject<Boolean> mScreenOn;
    FieldObject<Long> mSupplicantScanIntervalMs;
    FieldObject<WifiConfigManager> mWifiConfigManager;
    FieldObject<WifiConnectivityManager> mWifiConnectivityManager;
    FieldObject<WifiInfo> mWifiInfo;
    FieldObject<WifiNative> mWifiNative;
    FieldObject<AsyncChannel> mWifiP2pChannel;
    MethodObject<Void> replyToMessage;
    MethodObject<Void> setWifiState;

    @GetField(fieldObject = "mWifiNative")
    public WifiNative getWifiNative(ClientModeImpl wifiStateMachine) {
        return (WifiNative) getField(this.mWifiNative, wifiStateMachine);
    }

    @GetField(fieldObject = "mWifiConfigManager")
    public WifiConfigManager getWifiConfigManager(ClientModeImpl wifiStateMachine) {
        return (WifiConfigManager) getField(this.mWifiConfigManager, wifiStateMachine);
    }

    @GetField(fieldObject = "mWifiConnectivityManager")
    public WifiConnectivityManager getWifiConnectivityManager(ClientModeImpl wifiStateMachine) {
        return (WifiConnectivityManager) getField(this.mWifiConnectivityManager, wifiStateMachine);
    }

    @GetField(fieldObject = "mLastNetworkId")
    public int getLastNetworkId(ClientModeImpl wifiStateMachine) {
        return ((Integer) getField(this.mLastNetworkId, wifiStateMachine)).intValue();
    }

    @GetField(fieldObject = "mOperationalMode")
    public int getOperationalMode(ClientModeImpl wifiStateMachine) {
        return ((Integer) getField(this.mOperationalMode, wifiStateMachine)).intValue();
    }

    @SetField(fieldObject = "mOperationalMode")
    public void setOperationalMode(ClientModeImpl wifiStateMachine, int value) {
        setField(this.mOperationalMode, wifiStateMachine, Integer.valueOf(value));
    }

    @GetField(fieldObject = "mNetworkInfo")
    public NetworkInfo getNetworkInfo(ClientModeImpl wifiStateMachine) {
        return (NetworkInfo) getField(this.mNetworkInfo, wifiStateMachine);
    }

    @GetField(fieldObject = "mWifiP2pChannel")
    public AsyncChannel getWifiP2pChannel(ClientModeImpl wifiStateMachine) {
        return (AsyncChannel) getField(this.mWifiP2pChannel, wifiStateMachine);
    }

    @GetField(fieldObject = "mDisconnectedState")
    public State getDisconnectedState(ClientModeImpl wifiStateMachine) {
        return (State) getField(this.mDisconnectedState, wifiStateMachine);
    }

    @GetField(fieldObject = "mScreenOn")
    public boolean getScreenOn(ClientModeImpl wifiStateMachine) {
        return ((Boolean) getField(this.mScreenOn, wifiStateMachine)).booleanValue();
    }

    @GetField(fieldObject = "mSupplicantScanIntervalMs")
    public long getSupplicantScanIntervalMs(ClientModeImpl wifiStateMachine) {
        return ((Long) getField(this.mSupplicantScanIntervalMs, wifiStateMachine)).longValue();
    }

    @GetField(fieldObject = "mInterfaceName")
    public String getInterfaceName(ClientModeImpl wifiStateMachine) {
        return (String) getField(this.mInterfaceName, wifiStateMachine);
    }

    @GetField(fieldObject = "mWifiInfo")
    public WifiInfo getWifiInfo(ClientModeImpl wifiStateMachine) {
        return (WifiInfo) getField(this.mWifiInfo, wifiStateMachine);
    }

    @InvokeMethod(methodObject = "setWifiState")
    public void setWifiState(ClientModeImpl wifiStateMachine, int wifiState) {
        invokeMethod(this.setWifiState, wifiStateMachine, new Object[]{Integer.valueOf(wifiState)});
    }

    @InvokeMethod(methodObject = "replyToMessage")
    public void replyToMessage(ClientModeImpl wifiStateMachine, Message msg, int what, int arg1) {
        invokeMethod(this.replyToMessage, wifiStateMachine, new Object[]{msg, Integer.valueOf(what), Integer.valueOf(arg1)});
    }

    @InvokeMethod(methodObject = "handleIPv4Success")
    public void handleIPv4Success(ClientModeImpl wifiStateMachine, DhcpResults dhcpResults) {
        invokeMethod(this.handleIPv4Success, wifiStateMachine, new Object[]{dhcpResults});
    }

    @GetField(fieldObject = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED")
    public int getUnwantedValidationFailed(ClientModeImpl wifiStateMachine) {
        return ((Integer) getField(this.NETWORK_STATUS_UNWANTED_VALIDATION_FAILED, wifiStateMachine)).intValue();
    }

    @GetField(fieldObject = "mNetworkAgent")
    public HwNetworkAgent getNetworkAgent(ClientModeImpl wifiStateMachine) {
        return (HwNetworkAgent) getField(this.mNetworkAgent, wifiStateMachine);
    }

    @GetField(fieldObject = "mDhcpResults")
    public DhcpResults getDhcpResults(ClientModeImpl wifiStateMachine) {
        return (DhcpResults) getField(this.mDhcpResults, wifiStateMachine);
    }

    @InvokeMethod(methodObject = "replyToMessage")
    public void replyToMessage(ClientModeImpl wifiStateMachine, Message msg, int what, Object obj) {
        invokeMethod(this.replyToMessage, wifiStateMachine, new Object[]{msg, Integer.valueOf(what), obj});
    }

    @InvokeMethod(methodObject = "getAdditionalWifiServiceInterfaces")
    public void getAdditionalWifiServiceInterfaces(ClientModeImpl wifiStateMachine) {
        invokeMethod(this.getAdditionalWifiServiceInterfaces, wifiStateMachine, new Object[]{null});
    }

    @GetField(fieldObject = "mScanRequestProxy")
    public ScanRequestProxy getScanRequestProxy(ClientModeImpl wifiStateMachine) {
        return (ScanRequestProxy) getField(this.mScanRequestProxy, wifiStateMachine);
    }
}
