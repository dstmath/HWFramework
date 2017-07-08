package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.module.network.TrafficEntity;
import tmsdkobf.jg;

/* compiled from: Unknown */
public final class NetworkManager extends BaseManagerB {
    public static final int INTERVAL_CUSTOM = 3;
    public static final int INTERVAL_FOR_BACKGROUND = 0;
    public static final int INTERVAL_FOR_REALTIME = 2;
    public static final int INTERVAL_FOR_UI = 1;
    private g yt;
    private h yu;

    public boolean addDefaultMobileMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        return !jg.cl() ? this.yt.addDefaultMobileMonitor(str, iNetworkInfoDao) : false;
    }

    public boolean addDefaultWifiMonitor(String str, INetworkInfoDao iNetworkInfoDao) {
        return !jg.cl() ? this.yt.addDefaultWifiMonitor(str, iNetworkInfoDao) : false;
    }

    public boolean addMonitor(String str, NetDataEntityFactory netDataEntityFactory, INetworkInfoDao iNetworkInfoDao) {
        return !jg.cl() ? this.yt.addMonitor(str, netDataEntityFactory, iNetworkInfoDao) : false;
    }

    public void clearTrafficInfo(String[] strArr) {
        if (!jg.cl()) {
            this.yt.clearTrafficInfo(strArr);
        }
    }

    public INetworkMonitor findMonitor(String str) {
        if (!jg.cl()) {
            return this.yt.findMonitor(str);
        }
        if (this.yu == null) {
            this.yu = new h();
        }
        return this.yu;
    }

    public long getInterval() {
        return !jg.cl() ? this.yt.getInterval() : -1;
    }

    public int getIntervalType() {
        return !jg.cl() ? this.yt.getIntervalType() : -1;
    }

    public long getMobileRxBytes(String str) {
        return !jg.cl() ? this.yt.getMobileRxBytes(str) : 0;
    }

    public long getMobileTxBytes(String str) {
        return !jg.cl() ? this.yt.getMobileTxBytes(str) : 0;
    }

    public TrafficEntity getTrafficEntity(String str) {
        return !jg.cl() ? this.yt.getTrafficEntity(str) : new TrafficEntity();
    }

    public long getWIFIRxBytes(String str) {
        return !jg.cl() ? this.yt.getWIFIRxBytes(str) : 0;
    }

    public long getWIFITxBytes(String str) {
        return !jg.cl() ? this.yt.getWIFITxBytes(str) : 0;
    }

    public boolean isEnable() {
        return !jg.cl() ? this.yt.isEnable() : false;
    }

    public boolean isSupportTrafficState() {
        return !jg.cl() ? this.yt.isSupportTrafficState() : false;
    }

    public void networkConnectivityChangeNotify() {
        if (!jg.cl()) {
            this.yt.networkConnectivityChangeNotify();
        }
    }

    public void notifyConfigChange() {
        if (!jg.cl()) {
            this.yt.notifyConfigChange();
        }
    }

    public void onCreate(Context context) {
        this.yt = new g();
        this.yt.onCreate(context);
        a(this.yt);
    }

    public ArrayList<TrafficEntity> refreshTrafficInfo(String[] strArr, boolean z) {
        return !jg.cl() ? this.yt.refreshTrafficInfo(strArr, z) : null;
    }

    public void refreshTrafficInfo(ArrayList<TrafficEntity> arrayList) {
        if (!jg.cl()) {
            this.yt.refreshTrafficInfo((ArrayList) arrayList);
        }
    }

    public void refreshTrafficInfo(TrafficEntity trafficEntity) {
        if (!jg.cl()) {
            this.yt.refreshTrafficInfo(trafficEntity);
        }
    }

    public boolean removeMonitor(String str) {
        return !jg.cl() ? this.yt.removeMonitor(str) : false;
    }

    public void setEnable(boolean z) {
        if (!jg.cl()) {
            this.yt.setEnable(z);
        }
    }

    public void setInterval(long j) {
        if (!jg.cl()) {
            this.yt.setInterval((long) ((int) j));
        }
    }

    public void setIntervalType(int i) {
        if (!jg.cl()) {
            this.yt.setIntervalType(i);
        }
    }
}
