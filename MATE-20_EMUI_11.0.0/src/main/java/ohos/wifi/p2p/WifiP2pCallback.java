package ohos.wifi.p2p;

import java.util.List;
import ohos.annotation.SystemApi;

public class WifiP2pCallback {
    public void eventExecFail(int i) {
    }

    public void eventExecOk() {
    }

    public void eventP2pControllerDisconnected() {
    }

    public void eventP2pDevice(WifiP2pDevice wifiP2pDevice) {
    }

    public void eventP2pDevicesList(List<WifiP2pDevice> list) {
    }

    public void eventP2pGroup(WifiP2pGroup wifiP2pGroup) {
    }

    @SystemApi
    public void eventP2pLinkedInfo(WifiP2pLinkedInfo wifiP2pLinkedInfo) {
    }

    public void eventP2pNetwork(WifiP2pNetworkInfo wifiP2pNetworkInfo) {
    }

    @SystemApi
    public void eventP2pPersistentGroup(List<WifiP2pGroup> list) {
    }
}
