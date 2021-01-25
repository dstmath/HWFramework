package ohos.wifi.p2p;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.wifi.InnerUtils;
import ohos.wifi.WifiLinkedInfo;

public class WifiP2pNetworkInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pNetworkInfo");
    private WifiLinkedInfo.ConnState connState;

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return true;
    }

    public WifiLinkedInfo.ConnState getConnState() {
        return this.connState;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        parcel.readInt();
        parcel.readInt();
        parcel.readString();
        parcel.readString();
        parcel.readString();
        this.connState = convertState(parcel.readString());
        return true;
    }

    private WifiLinkedInfo.ConnState convertState(String str) {
        try {
            if ("IDLE".equals(str)) {
                return WifiLinkedInfo.ConnState.valueOf("DISCONNECTED");
            }
            if ("SUSPENDED".equals(str)) {
                return WifiLinkedInfo.ConnState.valueOf("CONNECTED");
            }
            if ("FAILED".equals(str)) {
                return WifiLinkedInfo.ConnState.valueOf("UNKNOWN");
            }
            return WifiLinkedInfo.ConnState.valueOf(str);
        } catch (IllegalArgumentException unused) {
            HiLog.error(LABEL, "convertState error! state is %{public}s", str);
            return WifiLinkedInfo.ConnState.valueOf("UNKNOWN");
        }
    }
}
