package com.android.server.wifi.p2p;

import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.os.Looper;
import android.os.Message;
import com.android.internal.util.State;
import com.android.server.wifi.WifiRepeater;

public abstract class AbsWifiP2pService extends IWifiP2pManager.Stub {
    /* access modifiers changed from: protected */
    public boolean autoAcceptConnection() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void clearValidDeivceList() {
    }

    /* access modifiers changed from: protected */
    public Object getHwP2pStateMachine(String name, Looper looper, boolean p2pSupported) {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean handleDefaultStateMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleP2pNotSupportedStateMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleInactiveStateMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleClientHwMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleP2pEnabledStateExMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setmMagicLinkDeviceFlag(boolean magicLinkDeviceFlag) {
    }

    /* access modifiers changed from: protected */
    public boolean getMagicLinkDeviceFlag() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGroupNegotiationStateExMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGroupCreatedStateExMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleOngoingGroupRemovalStateExMessage(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleTetheringDhcpRange(String[] tetheringDhcpRanges) {
    }

    /* access modifiers changed from: protected */
    public void sendGroupConfigInfo(WifiP2pGroup mGroup) {
    }

    /* access modifiers changed from: protected */
    public void sendP2pConnectingStateBroadcast() {
    }

    /* access modifiers changed from: protected */
    public void sendP2pFailStateBroadcast() {
    }

    /* access modifiers changed from: protected */
    public void sendP2pConnectedStateBroadcast() {
    }

    /* access modifiers changed from: protected */
    public void updateGroupCapability(WifiP2pDeviceList device, String deviceAddress, int groupCapab) {
        device.updateGroupCapability(deviceAddress, groupCapab);
    }

    /* access modifiers changed from: protected */
    public int getNetworkId(WifiP2pGroupList mGroups, String deviceAddress) {
        return mGroups.getNetworkId(deviceAddress);
    }

    /* access modifiers changed from: protected */
    public String getOwnerAddr(WifiP2pGroupList mGroups, int netId) {
        return mGroups.getOwnerAddr(netId);
    }

    /* access modifiers changed from: protected */
    public int getNetworkId(WifiP2pGroupList mGroups, String deviceAddress, String ssid) {
        return mGroups.getNetworkId(deviceAddress, ssid);
    }

    /* access modifiers changed from: protected */
    public void updateStatus(WifiP2pDeviceList mPeers, String deviceAddress, int invited) {
        mPeers.updateStatus(deviceAddress, invited);
    }

    /* access modifiers changed from: protected */
    public boolean getWifiRepeaterEnabled() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean startWifiRepeater(WifiP2pGroup group) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void stopWifiRepeater(WifiP2pGroup group) {
    }

    /* access modifiers changed from: protected */
    public void initWifiRepeaterConfig() {
    }

    /* access modifiers changed from: protected */
    public boolean processMessageForP2pCollision(Message msg, State state) {
        return false;
    }

    /* access modifiers changed from: protected */
    public String getWifiRepeaterServerAddress() {
        return null;
    }

    public WifiRepeater getWifiRepeater() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void notifyP2pChannelNumber(int channel) {
    }

    /* access modifiers changed from: protected */
    public void notifyP2pState(String state) {
    }

    /* access modifiers changed from: protected */
    public int addScanChannelInTimeout(int channelID, int timeout) {
        return timeout;
    }
}
