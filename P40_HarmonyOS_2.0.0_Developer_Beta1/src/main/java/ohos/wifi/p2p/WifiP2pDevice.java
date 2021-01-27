package ohos.wifi.p2p;

import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.wifi.InnerUtils;

public class WifiP2pDevice implements Sequenceable {
    private static final int AVAILABLE = 3;
    private static final int CONNECTED = 0;
    private static final int FAILED = 2;
    private static final int GROUP_CAPAB_GROUP_OWNER = 1;
    private static final int INVITED = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pDevice");
    private static final int UNAVAILABLE = 4;
    private String deviceAddress = "";
    private String deviceName = "";
    private int groupCapability;
    private String primaryDeviceType = "";
    private int status = 4;

    public String getDeviceAddress() {
        return this.deviceAddress;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getPrimaryDeviceType() {
        return this.primaryDeviceType;
    }

    public int getConnState() {
        return this.status;
    }

    public boolean isGroupOwner() {
        return (this.groupCapability & 1) != 0;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (((parcel.writeString(this.deviceAddress) && parcel.writeString(this.deviceName)) && parcel.writeString(this.primaryDeviceType)) && parcel.writeInt(this.status)) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        String readString = parcel.readString();
        if (this.deviceName != null) {
            this.deviceName = readString;
            this.deviceAddress = parcel.readString();
            this.primaryDeviceType = parcel.readString();
            parcel.readString();
            parcel.readInt();
            parcel.readInt();
            this.groupCapability = parcel.readInt();
            this.status = parcel.readInt();
            if (parcel.readInt() == 1) {
                parcel.readInt();
                parcel.readInt();
                parcel.readInt();
                parcel.readInt();
            }
        }
        return true;
    }
}
