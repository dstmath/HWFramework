package com.android.server.wifi;

import android.content.Context;
import android.net.DhcpResults;
import android.net.NetworkInfo;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.StateMachine;
import java.net.Inet4Address;
import java.util.List;

public abstract class AbsWifiStateMachine extends StateMachine {
    public static final int NETWORK_STATUS_UNWANTED_PORTAL = 3;

    public String getWpaSuppConfig() {
        return "";
    }

    protected AbsWifiStateMachine(String name, Looper looper) {
        super(name, looper);
    }

    protected AbsWifiStateMachine(String name, Handler handler) {
        super(name, handler);
    }

    protected AbsWifiStateMachine(String name) {
        super(name);
    }

    protected boolean processSupplicantStartingSetMode(Message message) {
        return false;
    }

    protected boolean processScanModeSetMode(Message message, int mLastOperationMode) {
        return false;
    }

    protected boolean processConnectModeSetMode(Message message) {
        return false;
    }

    protected boolean processL2ConnectedSetMode(Message message) {
        return false;
    }

    protected boolean processDisconnectedSetMode(Message message) {
        return false;
    }

    protected void enterConnectedStateByMode() {
    }

    protected boolean enterDriverStartedStateByMode() {
        return false;
    }

    protected void enableAllNetworksByMode() {
    }

    protected void handleNetworkDisconnect() {
    }

    protected void loadAndEnableAllNetworksByMode() {
    }

    protected boolean isScanAndManualConnectMode() {
        return false;
    }

    protected boolean processConnectModeAutoConnectByMode() {
        return false;
    }

    protected void recordAssociationRejectStatusCode(int statusCode) {
    }

    protected void startScreenOffScan() {
    }

    protected boolean processScreenOffScan(Message message) {
        return false;
    }

    protected void makeHwDefaultIPTable(DhcpResults dhcpResults) {
    }

    protected boolean handleHwDefaultIPConfiguration() {
        return false;
    }

    public DhcpResults getCachedDhcpResultsForCurrentConfig() {
        return null;
    }

    protected boolean hasMeteredHintForWi(Inet4Address ip) {
        return false;
    }

    protected void processSetVoWifiDetectMode(Message msg) {
    }

    protected void processSetVoWifiDetectPeriod(Message msg) {
    }

    protected void processIsSupportVoWifiDetect(Message msg) {
    }

    protected void processStatistics(int event) {
    }

    public int resetScoreByInetAccess(int score) {
        return score;
    }

    public boolean isWifiProEnabled() {
        return false;
    }

    public void startWifi2WifiRequest() {
    }

    public void getConfiguredNetworks(Message message) {
    }

    public void saveConnectingNetwork(WifiConfiguration config) {
    }

    public void reportPortalNetworkStatus() {
    }

    public boolean ignoreEnterConnectedState() {
        return false;
    }

    public void wifiNetworkExplicitlyUnselected() {
    }

    public void wifiNetworkExplicitlySelected() {
    }

    public void handleConnectedInWifiPro() {
    }

    public void handleDisconnectedInWifiPro() {
    }

    public void handleUnwantedNetworkInWifiPro(WifiConfiguration config, int unwantedType) {
    }

    public void handleValidNetworkInWifiPro(WifiConfiguration config) {
    }

    public void saveWpsNetIdInWifiPro(int netId) {
    }

    public void handleHandoverConnectFailed(int netId, int disableReason) {
    }

    public void sendWifiHandoverCompletedBroadcast(int statusCode, String bssid, String ssid) {
    }

    public void handleRxGoodInWifiPro(String cmd, String value, RssiPacketCountInfo info) {
    }

    public void updateWifiproWifiConfiguration(Message message) {
    }

    public void setWiFiProScanResultList(List<ScanResult> list) {
    }

    public boolean isWifiProEvaluatingAP() {
        return false;
    }

    public void updateScanDetailByWifiPro(ScanDetail scanDetail) {
    }

    public void tryUseStaticIpForFastConnecting(int lastNid) {
    }

    public void updateNetworkConcurrently() {
    }

    public void handleConnectFailedInWifiPro(int netId, int disableReason) {
    }

    public void notifyWifiConnectedBackgroundReady() {
    }

    public void resetWifiproEvaluateConfig(WifiInfo mWifiInfo, int netId) {
    }

    public boolean ignoreNetworkStateChange(NetworkInfo networkInfo) {
        return false;
    }

    public boolean ignoreSupplicantStateChange(SupplicantState state) {
        return false;
    }

    public void triggerRoamingNetworkMonitor(boolean autoRoaming) {
    }

    public void setWifiBackgroundReason(int status) {
    }

    public boolean isDualbandScanning() {
        return false;
    }

    public List<String> syncGetApLinkedStaList(AsyncChannel channel) {
        return null;
    }

    public void setSoftapMacFilter(String macFilter) {
    }

    public void setSoftapDisassociateSta(String mac) {
    }

    public List<String> handleGetApLinkedStaList() {
        return null;
    }

    public void handleSetSoftapMacFilter(String macFilter) {
    }

    public void handleSetSoftapDisassociateSta(String mac) {
    }

    public void handleSetWifiApConfigurationHw() {
    }

    public boolean handleWapiFailureEvent(Message message, SupplicantStateTracker mSupplicantStateTracker) {
        return false;
    }

    public int[] syncGetApChannelListFor5G(AsyncChannel channel) {
        return null;
    }

    public void setLocalMacAddressFromMacfile() {
    }

    public String getWifiCountryCode(Context context, String countryCode) {
        return countryCode;
    }

    public void handleStopWifiRepeater(AsyncChannel wifiP2pChannel) {
    }

    public boolean isWifiRepeaterStarted() {
        return false;
    }

    public void setWifiRepeaterStoped() {
    }

    public void sendStaFrequency(int frequency) {
    }

    public boolean isHiLinkActive() {
        return false;
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
    }

    public void sendWpsOkcStartedBroadcast() {
    }

    public void saveWpsOkcConfiguration(int connectionNetId, String connectionBssid) {
    }
}
