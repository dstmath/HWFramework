package ohos.wifi.p2p;

import java.util.ArrayList;
import java.util.List;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.wifi.InnerUtils;

public class WifiP2pGroup implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pGroup");
    private List<WifiP2pDevice> devices = new ArrayList();
    private int frequency;
    private String groupName;
    private String interfaceName;
    private boolean isGroupOwner;
    private int netId;
    private WifiP2pDevice owner;
    private String ownerIpAddress;
    private String passphrase;

    public int getFrequency() {
        return this.frequency;
    }

    public int getNetId() {
        return this.netId;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public String getOwnerIpAddress() {
        return this.ownerIpAddress;
    }

    public void setOwnerIpAddress(String str) {
        this.ownerIpAddress = str;
    }

    public boolean isGroupOwner() {
        return this.isGroupOwner;
    }

    public List<WifiP2pDevice> getClientList() {
        return this.devices;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        Object[] objArr = (((((((((((!parcel.writeInt(this.frequency) || !parcel.writeInt(this.netId)) ? null : 1) == null || !parcel.writeString(this.groupName)) ? null : 1) == null || !parcel.writeString(this.interfaceName)) ? null : 1) == null || !parcel.writeString(this.passphrase)) ? null : 1) == null || !parcel.writeString(this.ownerIpAddress)) ? null : 1) == null || !parcel.writeInt(this.isGroupOwner ? 1 : 0)) ? null : 1;
        parcel.writeSequenceable(this.owner);
        if (((objArr == null || !parcel.writeSequenceableList(this.devices)) ? null : 1) != null) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.groupName = parcel.readString();
        parcel.readString();
        this.owner = new WifiP2pDevice();
        if (!this.owner.unmarshalling(parcel)) {
            this.owner = null;
        }
        int i = 0;
        this.isGroupOwner = parcel.readByte() != 0;
        int readInt = parcel.readInt();
        ArrayList arrayList = new ArrayList();
        while (readInt <= 8 && readInt > 0 && i < readInt) {
            parcel.readString();
            WifiP2pDevice wifiP2pDevice = new WifiP2pDevice();
            if (wifiP2pDevice.unmarshalling(parcel)) {
                arrayList.add(wifiP2pDevice);
            }
            i++;
        }
        this.devices = arrayList;
        this.passphrase = parcel.readString();
        this.interfaceName = parcel.readString();
        this.netId = parcel.readInt();
        this.frequency = parcel.readInt();
        this.ownerIpAddress = parcel.readString();
        return true;
    }
}
