package ohos.wifi;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class WifiMpConfig implements Sequenceable {
    public static final int CELL_NETWORK = 801;
    private int targetNetworkId;
    private int uid;

    public WifiMpConfig(int i, int i2) {
        this.uid = i;
        this.targetNetworkId = i2;
    }

    public WifiMpConfig() {
        this(-1, -1);
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int i) {
        this.uid = i;
    }

    public int getTargetNetworkId() {
        return this.targetNetworkId;
    }

    public void setTargetNetworkId(int i) {
        this.targetNetworkId = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel.writeInt(this.uid) && parcel.writeInt(this.targetNetworkId)) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.uid = parcel.readInt();
        this.targetNetworkId = parcel.readInt();
        return true;
    }
}
