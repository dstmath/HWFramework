package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.module.network.TrafficEntity;
import tmsdkobf.ic;

public final class NetworkManager extends BaseManagerB {
    public static final int INTERVAL_CUSTOM = 3;
    public static final int INTERVAL_FOR_BACKGROUND = 0;
    public static final int INTERVAL_FOR_REALTIME = 2;
    public static final int INTERVAL_FOR_UI = 1;
    private g vx;
    private h vy;

    public boolean addDefaultMobileMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        return !ic.bE() ? this.vx.addDefaultMobileMonitor(str, iNetworkInfoDao) : false;
    }

    public boolean addDefaultWifiMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        return !ic.bE() ? this.vx.addDefaultWifiMonitor(str, iNetworkInfoDao) : false;
    }

    public boolean addMonitor(String str, NetDataEntityFactory netDataEntityFactory, INetworkInfoDao iNetworkInfoDao) {
        return !ic.bE() ? this.vx.addMonitor(str, netDataEntityFactory, iNetworkInfoDao) : false;
    }

    public void clearTrafficInfo(String[] strArr) {
        if (!ic.bE()) {
            this.vx.clearTrafficInfo(strArr);
        }
    }

    public INetworkMonitor findMonitor(String str) {
        if (!ic.bE()) {
            return this.vx.findMonitor(str);
        }
        if (this.vy == null) {
            this.vy = new h();
        }
        return this.vy;
    }

    public long getInterval() {
        return !ic.bE() ? this.vx.getInterval() : -1;
    }

    public int getIntervalType() {
        return !ic.bE() ? this.vx.getIntervalType() : -1;
    }

    public long getMobileRxBytes(String str) {
        return !ic.bE() ? this.vx.getMobileRxBytes(str) : 0;
    }

    public long getMobileTxBytes(String str) {
        return !ic.bE() ? this.vx.getMobileTxBytes(str) : 0;
    }

    public TrafficEntity getTrafficEntity(String str) {
        return !ic.bE() ? this.vx.getTrafficEntity(str) : new TrafficEntity();
    }

    public long getWIFIRxBytes(String str) {
        return !ic.bE() ? this.vx.getWIFIRxBytes(str) : 0;
    }

    public long getWIFITxBytes(String str) {
        return !ic.bE() ? this.vx.getWIFITxBytes(str) : 0;
    }

    public boolean isEnable() {
        return !ic.bE() ? this.vx.isEnable() : false;
    }

    public boolean isSupportTrafficState() {
        return !ic.bE() ? this.vx.isSupportTrafficState() : false;
    }

    public void networkConnectivityChangeNotify() {
        if (!ic.bE()) {
            this.vx.networkConnectivityChangeNotify();
        }
    }

    public void notifyConfigChange() {
        if (!ic.bE()) {
            this.vx.notifyConfigChange();
        }
    }

    public void onCreate(Context context) {
        this.vx = new g();
        this.vx.onCreate(context);
        a(this.vx);
    }

    public ArrayList<TrafficEntity> refreshTrafficInfo(String[] strArr, boolean z) {
        return !ic.bE() ? this.vx.refreshTrafficInfo(strArr, z) : null;
    }

    public void refreshTrafficInfo(ArrayList<TrafficEntity> arrayList) {
        if (!ic.bE()) {
            this.vx.refreshTrafficInfo((ArrayList) arrayList);
        }
    }

    public void refreshTrafficInfo(TrafficEntity trafficEntity) {
        if (!ic.bE()) {
            this.vx.refreshTrafficInfo(trafficEntity);
        }
    }

    public boolean removeMonitor(String str) {
        return !ic.bE() ? this.vx.removeMonitor(str) : false;
    }

    public void setEnable(boolean z) {
        if (!ic.bE()) {
            this.vx.setEnable(z);
        }
    }

    public void setInterval(long j) {
        if (!ic.bE()) {
            this.vx.setInterval((long) ((int) j));
        }
    }

    public void setIntervalType(int i) {
        if (!ic.bE()) {
            this.vx.setIntervalType(i);
        }
    }
}
