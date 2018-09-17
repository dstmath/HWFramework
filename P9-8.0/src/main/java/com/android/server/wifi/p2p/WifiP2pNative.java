package com.android.server.wifi.p2p;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;

public class WifiP2pNative {
    private final String mInterfaceName;
    private final SupplicantP2pIfaceHal mSupplicantP2pIfaceHal;
    private final String mTAG;

    public WifiP2pNative(String interfaceName, SupplicantP2pIfaceHal p2pIfaceHal) {
        this.mTAG = "WifiP2pNative-" + interfaceName;
        this.mInterfaceName = interfaceName;
        this.mSupplicantP2pIfaceHal = p2pIfaceHal;
    }

    public String getInterfaceName() {
        return this.mInterfaceName;
    }

    public void enableVerboseLogging(int verbose) {
    }

    public boolean connectToSupplicant() {
        if (this.mSupplicantP2pIfaceHal.isInitializationStarted() || (this.mSupplicantP2pIfaceHal.initialize() ^ 1) == 0) {
            return this.mSupplicantP2pIfaceHal.isInitializationComplete();
        }
        return false;
    }

    public void closeSupplicantConnection() {
    }

    public boolean setDeviceName(String name) {
        return this.mSupplicantP2pIfaceHal.setWpsDeviceName(name);
    }

    public boolean p2pListNetworks(WifiP2pGroupList groups) {
        return this.mSupplicantP2pIfaceHal.loadGroups(groups);
    }

    public boolean startWpsPbc(String iface, String bssid) {
        return this.mSupplicantP2pIfaceHal.startWpsPbc(iface, bssid);
    }

    public boolean startWpsPinKeypad(String iface, String pin) {
        return this.mSupplicantP2pIfaceHal.startWpsPinKeypad(iface, pin);
    }

    public String startWpsPinDisplay(String iface, String bssid) {
        return this.mSupplicantP2pIfaceHal.startWpsPinDisplay(iface, bssid);
    }

    public boolean removeP2pNetwork(int netId) {
        return this.mSupplicantP2pIfaceHal.removeNetwork(netId);
    }

    public boolean setP2pDeviceName(String name) {
        return this.mSupplicantP2pIfaceHal.setWpsDeviceName(name);
    }

    public boolean setP2pDeviceType(String type) {
        return this.mSupplicantP2pIfaceHal.setWpsDeviceType(type);
    }

    public boolean setConfigMethods(String cfg) {
        return this.mSupplicantP2pIfaceHal.setWpsConfigMethods(cfg);
    }

    public boolean setP2pSsidPostfix(String postfix) {
        return this.mSupplicantP2pIfaceHal.setSsidPostfix(postfix);
    }

    public boolean setP2pGroupIdle(String iface, int time) {
        return this.mSupplicantP2pIfaceHal.setGroupIdle(iface, time);
    }

    public boolean setP2pPowerSave(String iface, boolean enabled) {
        return this.mSupplicantP2pIfaceHal.setPowerSave(iface, enabled);
    }

    public boolean setWfdEnable(boolean enable) {
        return this.mSupplicantP2pIfaceHal.enableWfd(enable);
    }

    public boolean setWfdDeviceInfo(String hex) {
        return this.mSupplicantP2pIfaceHal.setWfdDeviceInfo(hex);
    }

    public boolean p2pFind() {
        return p2pFind(0);
    }

    public boolean p2pFind(int timeout) {
        return this.mSupplicantP2pIfaceHal.find(timeout);
    }

    public boolean p2pStopFind() {
        return this.mSupplicantP2pIfaceHal.stopFind();
    }

    public boolean p2pExtListen(boolean enable, int period, int interval) {
        return this.mSupplicantP2pIfaceHal.configureExtListen(enable, period, interval);
    }

    public boolean p2pSetChannel(int lc, int oc) {
        return this.mSupplicantP2pIfaceHal.setListenChannel(lc, oc);
    }

    public boolean p2pFlush() {
        return this.mSupplicantP2pIfaceHal.flush();
    }

    public String p2pConnect(WifiP2pConfig config, boolean joinExistingGroup) {
        return this.mSupplicantP2pIfaceHal.connect(config, joinExistingGroup);
    }

    public boolean p2pCancelConnect() {
        return this.mSupplicantP2pIfaceHal.cancelConnect();
    }

    public boolean p2pProvisionDiscovery(WifiP2pConfig config) {
        return this.mSupplicantP2pIfaceHal.provisionDiscovery(config);
    }

    public boolean p2pGroupAdd(boolean persistent) {
        return this.mSupplicantP2pIfaceHal.groupAdd(persistent);
    }

    public boolean p2pGroupAdd(int netId) {
        return this.mSupplicantP2pIfaceHal.groupAdd(netId, true);
    }

    public boolean magiclinkGroupAdd(boolean persistent, String freq) {
        return this.mSupplicantP2pIfaceHal.groupAddWithFreq(-1, persistent, freq);
    }

    public boolean magiclinkGroupAdd(int netId, String freq) {
        return this.mSupplicantP2pIfaceHal.groupAddWithFreq(netId, true, freq);
    }

    public boolean magiclinkConnect(String cmd) {
        return this.mSupplicantP2pIfaceHal.magiclinkConnect(cmd);
    }

    public boolean p2pGroupRemove(String iface) {
        return this.mSupplicantP2pIfaceHal.groupRemove(iface);
    }

    public boolean p2pReject(String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.reject(deviceAddress);
    }

    public boolean p2pInvite(WifiP2pGroup group, String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.invite(group, deviceAddress);
    }

    public boolean p2pReinvoke(int netId, String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.reinvoke(netId, deviceAddress);
    }

    public String p2pGetSsid(String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.getSsid(deviceAddress);
    }

    public String p2pGetDeviceAddress() {
        return this.mSupplicantP2pIfaceHal.getDeviceAddress();
    }

    public int getGroupCapability(String deviceAddress) {
        return this.mSupplicantP2pIfaceHal.getGroupCapability(deviceAddress);
    }

    public boolean p2pServiceAdd(WifiP2pServiceInfo servInfo) {
        return this.mSupplicantP2pIfaceHal.serviceAdd(servInfo);
    }

    public boolean p2pServiceDel(WifiP2pServiceInfo servInfo) {
        return this.mSupplicantP2pIfaceHal.serviceRemove(servInfo);
    }

    public boolean p2pServiceFlush() {
        return this.mSupplicantP2pIfaceHal.serviceFlush();
    }

    public String p2pServDiscReq(String addr, String query) {
        return this.mSupplicantP2pIfaceHal.requestServiceDiscovery(addr, query);
    }

    public boolean p2pServDiscCancelReq(String id) {
        return this.mSupplicantP2pIfaceHal.cancelServiceDiscovery(id);
    }

    public void setMiracastMode(int mode) {
        this.mSupplicantP2pIfaceHal.setMiracastMode(mode);
    }

    public String getNfcHandoverRequest() {
        return this.mSupplicantP2pIfaceHal.getNfcHandoverRequest();
    }

    public String getNfcHandoverSelect() {
        return this.mSupplicantP2pIfaceHal.getNfcHandoverSelect();
    }

    public boolean initiatorReportNfcHandover(String selectMessage) {
        return this.mSupplicantP2pIfaceHal.initiatorReportNfcHandover(selectMessage);
    }

    public boolean responderReportNfcHandover(String requestMessage) {
        return this.mSupplicantP2pIfaceHal.responderReportNfcHandover(requestMessage);
    }

    public String getP2pClientList(int netId) {
        return this.mSupplicantP2pIfaceHal.getClientList(netId);
    }

    public boolean setP2pClientList(int netId, String list) {
        return this.mSupplicantP2pIfaceHal.setClientList(netId, list);
    }

    public boolean saveConfig() {
        return this.mSupplicantP2pIfaceHal.saveConfig();
    }

    public boolean enableP2p(int p2pFlag) {
        return true;
    }

    public boolean addP2pRptGroup(String config) {
        return this.mSupplicantP2pIfaceHal.addP2pRptGroup(config);
    }
}
