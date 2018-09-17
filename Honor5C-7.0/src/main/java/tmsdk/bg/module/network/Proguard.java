package tmsdk.bg.module.network;

import tmsdk.bg.creator.ManagerCreatorB;

/* compiled from: Unknown */
public final class Proguard {
    public void callAllMethods() {
        NetworkManager networkManager = (NetworkManager) ManagerCreatorB.getManager(NetworkManager.class);
        networkManager.addDefaultMobileMonitor(null, null);
        networkManager.addDefaultWifiMonitor(null, null);
        networkManager.removeMonitor(null);
        networkManager.addMonitor(null, null, null);
        networkManager.clearTrafficInfo(null);
        networkManager.findMonitor(null);
        networkManager.getInterval();
        networkManager.getIntervalType();
        networkManager.getMobileRxBytes(null);
        networkManager.getMobileTxBytes(null);
        networkManager.getTrafficEntity(null);
        networkManager.getWIFIRxBytes(null);
        networkManager.getWIFITxBytes(null);
        networkManager.isEnable();
        networkManager.isSupportTrafficState();
        networkManager.notifyConfigChange();
        networkManager.refreshTrafficInfo(null, false);
        networkManager.setEnable(false);
        networkManager.setInterval(0);
    }
}
