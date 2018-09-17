package tmsdk.bg.module.network;

import java.util.ArrayList;
import tmsdk.common.module.network.NetworkInfoEntity;

final class h implements INetworkMonitor {
    h() {
    }

    public int addCallback(INetworkChangeCallBack iNetworkChangeCallBack) {
        return 0;
    }

    public void clearAllLogs() {
    }

    public ArrayList<NetworkInfoEntity> getAllLogs() {
        return new ArrayList();
    }

    public boolean getRefreshState() {
        return false;
    }

    public void notifyConfigChange() {
    }

    public boolean removeCallback(int i) {
        return false;
    }

    public boolean removeCallback(INetworkChangeCallBack iNetworkChangeCallBack) {
        return false;
    }

    public void setRefreshState(boolean z) {
    }
}
