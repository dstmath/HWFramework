package com.android.server.wifi;

import android.net.NetworkInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Message;
import android.os.Process;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.cast.CastOptManager;

public class HwWifiStateMachineEx implements IHwWifiStateMachineEx {
    private static final String TAG = "WifiClientModeImpl";
    private static boolean isWifiPoorLink = false;
    private IHwWifiStateMachineInner mHwWifiStateMachineInner;

    public static HwWifiStateMachineEx createHwWifiStateMachineEx(IHwWifiStateMachineInner hwWifiStateMachineInner) {
        return new HwWifiStateMachineEx(hwWifiStateMachineInner);
    }

    HwWifiStateMachineEx(IHwWifiStateMachineInner hwWifiStateMachineInner) {
        this.mHwWifiStateMachineInner = hwWifiStateMachineInner;
    }

    public WifiInfo hwSyncRequestConnectionInfo(WifiInfo wifiInfo) {
        WifiInfo result = new WifiInfo(wifiInfo);
        result.setNetworkId(this.mHwWifiStateMachineInner.getSelfCureNetworkId());
        if (result.getRssi() <= -127) {
            result.setRssi(-70);
        }
        result.setSupplicantState(SupplicantState.COMPLETED);
        return result;
    }

    public boolean checkForceReconnect(NetworkInfo networkInfo, WifiConfiguration config, boolean isForceReconnect) {
        HwHiLog.d(TAG, false, "callingPid = " + config.callingPid + " myPid() =" + Process.myPid(), new Object[0]);
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting() || config.callingPid == Process.myPid() || !config.isTempCreated || !this.mHwWifiStateMachineInner.isWifiProEvaluatingAP()) {
            return isForceReconnect;
        }
        config.isTempCreated = false;
        return true;
    }

    public void handleWifiproPrivateStatus(int status) {
        if (status == 3) {
            this.mHwWifiStateMachineInner.reportPortalNetworkStatus();
        } else if (status == 4) {
            this.mHwWifiStateMachineInner.notifyWifiConnectedBackgroundReady();
        }
    }

    public boolean handleHwPrivateMsgInConnectedState(Message message) {
        int i = message.what;
        if (i == 131895) {
            this.mHwWifiStateMachineInner.startSelfCureReconnect();
            if (this.mHwWifiStateMachineInner.getIpClient() != null) {
                this.mHwWifiStateMachineInner.getIpClient().forceRemoveDhcpCache();
            }
            this.mHwWifiStateMachineInner.sendMessageDelayed(131145, 500);
            return true;
        } else if (i != 131898) {
            switch (i) {
                case 131873:
                    this.mHwWifiStateMachineInner.wifiNetworkExplicitlyUnselected();
                    this.mHwWifiStateMachineInner.hwSetNetworkDetailedState(NetworkInfo.DetailedState.VERIFYING_POOR_LINK);
                    IHwWifiStateMachineInner iHwWifiStateMachineInner = this.mHwWifiStateMachineInner;
                    iHwWifiStateMachineInner.hwSendNetworkStateChangeBroadcast(iHwWifiStateMachineInner.getLastBssid());
                    isWifiPoorLink = true;
                    return true;
                case 131874:
                    this.mHwWifiStateMachineInner.updateWifiBackgroudStatus(message.arg1);
                    this.mHwWifiStateMachineInner.wifiNetworkExplicitlySelected();
                    this.mHwWifiStateMachineInner.setWifiBackgroundStatus(false);
                    this.mHwWifiStateMachineInner.hwSendConnectedState();
                    isWifiPoorLink = false;
                    return true;
                case 131875:
                    this.mHwWifiStateMachineInner.triggerInvalidlinkNetworkMonitor();
                    return true;
                case 131876:
                    this.mHwWifiStateMachineInner.wifiNetworkExplicitlyUnselected();
                    this.mHwWifiStateMachineInner.hwSetNetworkDetailedState(NetworkInfo.DetailedState.VERIFYING_POOR_LINK);
                    isWifiPoorLink = true;
                    return true;
                default:
                    switch (i) {
                        case 131882:
                            IHwWifiStateMachineInner iHwWifiStateMachineInner2 = this.mHwWifiStateMachineInner;
                            iHwWifiStateMachineInner2.sendUpdateDnsServersRequest(message, iHwWifiStateMachineInner2.getLinkProperties());
                            return true;
                        case 131883:
                            ClientModeImpl clientModeImpl = this.mHwWifiStateMachineInner;
                            clientModeImpl.transitionTo(clientModeImpl.getObtainingIpState());
                            return true;
                        case 131884:
                            this.mHwWifiStateMachineInner.hwStopIpClient();
                            this.mHwWifiStateMachineInner.sendMessageDelayed(131885, message.obj, 1000);
                            return true;
                        case 131885:
                            IHwWifiStateMachineInner iHwWifiStateMachineInner3 = this.mHwWifiStateMachineInner;
                            iHwWifiStateMachineInner3.handleStaticIpConfig(iHwWifiStateMachineInner3.getIpClient(), this.mHwWifiStateMachineInner.getWifiNative(), (StaticIpConfiguration) message.obj);
                            return true;
                        case 131886:
                            this.mHwWifiStateMachineInner.startSelfCureWifiReassoc(message.arg1);
                            return true;
                        case 131887:
                            this.mHwWifiStateMachineInner.startSelfCureWifiReset();
                            return true;
                        default:
                            return false;
                    }
            }
        } else {
            if (this.mHwWifiStateMachineInner.getIpClient() != null) {
                this.mHwWifiStateMachineInner.getIpClient().forceRemoveDhcpCache();
            }
            return true;
        }
    }

    public boolean handleHwPrivateMsgInObtainingIpState(Message message) {
        switch (message.what) {
            case 131875:
                HwHiLog.d(TAG, false, "inObtainingIpState handle INVALID_LINK_DETECTED", new Object[0]);
                this.mHwWifiStateMachineInner.triggerInvalidlinkNetworkMonitor();
                return true;
            case 131884:
                this.mHwWifiStateMachineInner.hwStopIpClient();
                this.mHwWifiStateMachineInner.sendMessageDelayed(131885, message.obj, 1000);
                return true;
            case 131885:
                IHwWifiStateMachineInner iHwWifiStateMachineInner = this.mHwWifiStateMachineInner;
                iHwWifiStateMachineInner.handleStaticIpConfig(iHwWifiStateMachineInner.getIpClient(), this.mHwWifiStateMachineInner.getWifiNative(), (StaticIpConfiguration) message.obj);
                return true;
            default:
                return false;
        }
    }

    public boolean ignoreEnterConnectedStateByWifipro() {
        if (this.mHwWifiStateMachineInner.ignoreEnterConnectedState()) {
            return true;
        }
        if (!this.mHwWifiStateMachineInner.isWifiProEvaluatingAP()) {
            return false;
        }
        this.mHwWifiStateMachineInner.updateNetworkConcurrently();
        ClientModeImpl clientModeImpl = this.mHwWifiStateMachineInner;
        clientModeImpl.transitionTo(clientModeImpl.getConnectedState());
        return true;
    }

    public boolean isTempCreated(WifiConfiguration config) {
        return config.isTempCreated;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean handleHwPrivateMsgInDefaultState(Message message) {
        int i = message.what;
        if (i == 131672) {
            return true;
        }
        if (i != 131899) {
            switch (i) {
                case 131874:
                    this.mHwWifiStateMachineInner.setWifiBackgroundStatus(false);
                    return true;
                case 131875:
                    return true;
                default:
                    switch (i) {
                        case 131888:
                        case 131889:
                        case 131890:
                        case 131891:
                            break;
                        case 131892:
                            this.mHwWifiStateMachineInner.stopSelfCureWifi(message.arg1);
                            if (message.arg1 < 0) {
                                if (this.mHwWifiStateMachineInner.getCurrentState() != this.mHwWifiStateMachineInner.getDisconnectedState()) {
                                    this.mHwWifiStateMachineInner.getHandler().removeMessages(147459);
                                    this.mHwWifiStateMachineInner.sendMessage(131145);
                                    this.mHwWifiStateMachineInner.hwHandleNetworkDisconnect();
                                } else {
                                    this.mHwWifiStateMachineInner.hwSetNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
                                    this.mHwWifiStateMachineInner.hwSendNetworkStateChangeBroadcast((String) null);
                                }
                            }
                            return true;
                        case 131893:
                            if (this.mHwWifiStateMachineInner.getNetworkAgent() != null) {
                                this.mHwWifiStateMachineInner.getNetworkAgent().sendNetworkInfo(this.mHwWifiStateMachineInner.getNetworkInfo());
                            } else {
                                this.mHwWifiStateMachineInner.hwSetNetworkDetailedState(NetworkInfo.DetailedState.DISCONNECTED);
                                this.mHwWifiStateMachineInner.getNewWifiNetworkAgent().sendNetworkInfo(this.mHwWifiStateMachineInner.getNetworkInfo());
                            }
                            return true;
                        default:
                            switch (i) {
                            }
                    }
                    this.mHwWifiStateMachineInner.notifySelfCureComplete(false, message.arg1);
                    return true;
            }
        } else {
            HwHiLog.d(TAG, false, "reset wifi mode because of CMD_ASSOCIATE_ASSISTANTE_TIMEOUT", new Object[0]);
            ClientModeImpl clientModeImpl = this.mHwWifiStateMachineInner;
            if (clientModeImpl instanceof ClientModeImpl) {
                clientModeImpl.setWifiMode("android", 0);
            }
        }
        return false;
    }

    public boolean handleHwPrivateMsgInConnectModeState(Message message) {
        if (message.what != 131672) {
            return false;
        }
        this.mHwWifiStateMachineInner.updateWifiproWifiConfiguration(message);
        return true;
    }

    public static boolean isWifiPoorLink() {
        return isWifiPoorLink;
    }

    public static void resetWifiPoorLink() {
        HwHiLog.d(TAG, false, "isWifiPoorLink = " + isWifiPoorLink, new Object[0]);
        isWifiPoorLink = false;
    }

    public boolean isNeedIgnoreConnect() {
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (!(castOptManager != null && castOptManager.isCastOptWorking())) {
            return false;
        }
        HwHiLog.i(TAG, false, "Ignore this connect because cast is working", new Object[0]);
        return true;
    }
}
