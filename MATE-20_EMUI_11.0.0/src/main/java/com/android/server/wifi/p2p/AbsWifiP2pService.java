package com.android.server.wifi.p2p;

import android.content.Context;
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
    public Object getHwP2pStateMachine(String name, Looper looper, boolean isP2pSupported) {
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
    public void setMagicLinkDeviceFlag(boolean isMagicLinkDevice) {
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
    public void sendGroupConfigInfo(WifiP2pGroup group) {
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
    public int getNetworkId(WifiP2pGroupList groups, String deviceAddress) {
        return groups.getNetworkId(deviceAddress);
    }

    /* access modifiers changed from: protected */
    public int getNetworkId(WifiP2pGroupList groups, String deviceAddress, String ssid) {
        return groups.getNetworkId(deviceAddress, ssid);
    }

    /* access modifiers changed from: protected */
    public String getOwnerAddr(WifiP2pGroupList groups, int netId) {
        return groups.getOwnerAddr(netId);
    }

    /* access modifiers changed from: protected */
    public void updateStatus(WifiP2pDeviceList peers, String deviceAddress, int invited) {
        peers.updateStatus(deviceAddress, invited);
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
    public int addScanChannelInTimeout(int channelId, int timeout) {
        return timeout;
    }

    public void handleClientConnect(WifiP2pGroup group) {
    }

    public void handleClientDisconnect(WifiP2pGroup group) {
    }

    public void notifyRptGroupRemoved() {
    }

    public void setWifiRepeaterState(int state) {
    }

    /* access modifiers changed from: protected */
    public boolean allowP2pFind(int uid) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleP2pStopFind(int uid) {
    }

    /* access modifiers changed from: protected */
    public void processStatistics(Context context, int eventId, int choice) {
    }

    /* access modifiers changed from: protected */
    public boolean isMiracastDevice(String deviceType) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean wifiIsConnected() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendReinvokePersisentGrouBroadcast(int netId) {
    }

    /* access modifiers changed from: protected */
    public String getSsidPostFix(String deviceName) {
        return deviceName;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiP2pForbidden(int msgWhat) {
        return false;
    }

    public synchronized void removeDisableP2pGcDhcp(boolean shouldRemoveAll) {
    }

    public boolean shouldDisableP2pGcDhcp() {
        return false;
    }
}
