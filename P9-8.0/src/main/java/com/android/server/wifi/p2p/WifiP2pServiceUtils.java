package com.android.server.wifi.p2p;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Message;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.InactiveState;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.OngoingGroupRemovalState;
import com.huawei.utils.reflect.EasyInvokeUtils;
import com.huawei.utils.reflect.FieldObject;
import com.huawei.utils.reflect.MethodObject;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;

public class WifiP2pServiceUtils extends EasyInvokeUtils {
    FieldObject<Integer> PEER_CONNECTION_USER_ACCEPT;
    MethodObject<Void> enableBTCoex;
    MethodObject<Void> handleGroupRemoved;
    FieldObject<Boolean> mAutonomousGroup;
    FieldObject<WifiP2pGroup> mGroup;
    FieldObject<InactiveState> mInactiveState;
    FieldObject<OngoingGroupRemovalState> mOngoingGroupRemovalState;
    FieldObject<WifiP2pDevice> mThisDevice;
    MethodObject<Void> replyToMessage;
    MethodObject<Void> sendP2pConnectionChangedBroadcast;

    @GetField(fieldObject = "PEER_CONNECTION_USER_ACCEPT")
    public int getPeerConnectionUserAccept(WifiP2pServiceImpl wifiP2pService) {
        return ((Integer) getField(this.PEER_CONNECTION_USER_ACCEPT, wifiP2pService)).intValue();
    }

    @GetField(fieldObject = "mGroup")
    public WifiP2pGroup getmGroup(P2pStateMachine p2pStateMachine) {
        return (WifiP2pGroup) getField(this.mGroup, p2pStateMachine);
    }

    @InvokeMethod(methodObject = "sendP2pConnectionChangedBroadcast")
    public void sendP2pConnectionChangedBroadcast(P2pStateMachine p2pStateMachine) {
        invokeMethod(this.sendP2pConnectionChangedBroadcast, p2pStateMachine, new Object[0]);
    }

    @GetField(fieldObject = "mInactiveState")
    public InactiveState getmInactiveState(P2pStateMachine p2pStateMachine) {
        return (InactiveState) getField(this.mInactiveState, p2pStateMachine);
    }

    @GetField(fieldObject = "mOngoingGroupRemovalState")
    public OngoingGroupRemovalState getmOngoingGroupRemovalState(P2pStateMachine p2pStateMachine) {
        return (OngoingGroupRemovalState) getField(this.mOngoingGroupRemovalState, p2pStateMachine);
    }

    @GetField(fieldObject = "mThisDevice")
    public WifiP2pDevice getmThisDevice(WifiP2pServiceImpl wifiP2pService) {
        return (WifiP2pDevice) getField(this.mThisDevice, wifiP2pService);
    }

    @InvokeMethod(methodObject = "enableBTCoex")
    public void enableBTCoex(P2pStateMachine p2pStateMachine) {
        invokeMethod(this.enableBTCoex, p2pStateMachine, new Object[0]);
    }

    @InvokeMethod(methodObject = "handleGroupRemoved")
    public void handleGroupRemoved(P2pStateMachine p2pStateMachine) {
        invokeMethod(this.handleGroupRemoved, p2pStateMachine, new Object[0]);
    }

    @SetField(fieldObject = "mAutonomousGroup")
    public void setAutonomousGroup(WifiP2pServiceImpl wifiP2pService, boolean value) {
        setField(this.mAutonomousGroup, wifiP2pService, Boolean.valueOf(value));
    }

    @InvokeMethod(methodObject = "replyToMessage")
    public void replyToMessage(P2pStateMachine p2pStateMachine, Message msg, int what, int arg1) {
        invokeMethod(this.replyToMessage, p2pStateMachine, new Object[]{msg, Integer.valueOf(what), Integer.valueOf(arg1)});
    }
}
