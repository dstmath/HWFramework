package com.android.server.wifi.p2p;

import android.net.wifi.p2p.IWifiP2pManager.Stub;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.os.Looper;
import android.os.Message;
import com.android.internal.util.State;
import com.android.server.wifi.WifiRepeater;

public abstract class AbsWifiP2pService extends Stub {
    protected boolean autoAcceptConnection() {
        return false;
    }

    protected void clearValidDeivceList() {
    }

    protected Object getHwP2pStateMachine(String name, Looper looper, boolean p2pSupported) {
        return null;
    }

    protected boolean handleDefaultStateMessage(Message message) {
        return false;
    }

    protected boolean handleP2pNotSupportedStateMessage(Message message) {
        return false;
    }

    protected boolean handleInactiveStateMessage(Message message) {
        return false;
    }

    protected boolean handleClientHwMessage(Message message) {
        return false;
    }

    protected boolean handleP2pEnabledStateExMessage(Message message) {
        return false;
    }

    protected void setmMagicLinkDeviceFlag(boolean magicLinkDeviceFlag) {
    }

    protected boolean getMagicLinkDeviceFlag() {
        return false;
    }

    protected boolean handleGroupNegotiationStateExMessage(Message message) {
        return false;
    }

    protected boolean handleGroupCreatedStateExMessage(Message message) {
        return false;
    }

    protected boolean handleOngoingGroupRemovalStateExMessage(Message message) {
        return false;
    }

    protected void handleTetheringDhcpRange(String[] tetheringDhcpRanges) {
    }

    protected void sendGroupConfigInfo(WifiP2pGroup mGroup) {
    }

    protected void sendP2pConnectingStateBroadcast() {
    }

    protected void sendP2pFailStateBroadcast() {
    }

    protected void sendP2pConnectedStateBroadcast() {
    }

    protected void updateGroupCapability(WifiP2pDeviceList device, String deviceAddress, int groupCapab) {
        device.updateGroupCapability(deviceAddress, groupCapab);
    }

    protected int getNetworkId(WifiP2pGroupList mGroups, String deviceAddress) {
        return mGroups.getNetworkId(deviceAddress);
    }

    protected String getOwnerAddr(WifiP2pGroupList mGroups, int netId) {
        return mGroups.getOwnerAddr(netId);
    }

    protected int getNetworkId(WifiP2pGroupList mGroups, String deviceAddress, String ssid) {
        return mGroups.getNetworkId(deviceAddress, ssid);
    }

    protected void updateStatus(WifiP2pDeviceList mPeers, String deviceAddress, int invited) {
        mPeers.updateStatus(deviceAddress, invited);
    }

    protected boolean getWifiRepeaterEnabled() {
        return false;
    }

    protected boolean startWifiRepeater(WifiP2pGroup group) {
        return false;
    }

    protected void stopWifiRepeater(WifiP2pGroup group) {
    }

    protected void initWifiRepeaterConfig() {
    }

    protected boolean processMessageForP2pCollision(Message msg, State state) {
        return false;
    }

    protected String getWifiRepeaterServerAddress() {
        return null;
    }

    public WifiRepeater getWifiRepeater() {
        return null;
    }

    protected void notifyP2pChannelNumber(int channel) {
    }

    protected void notifyP2pState(String state) {
    }
}
