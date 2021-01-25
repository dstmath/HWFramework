package ohos.wifi.p2p;

import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class WifiP2pConfig implements Sequenceable {
    private static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final int GO_BAND_2G = 1;
    public static final int GO_BAND_5G = 2;
    public static final int GO_BAND_AUTO = 0;
    private static final int PERSISTENT_NET_ID = -2;
    private static final int TEMPORARY_NET_ID = -1;
    @SystemApi
    public static final int WPS_DISPLAY = 1;
    @SystemApi
    public static final int WPS_KEYPAD = 2;
    @SystemApi
    public static final int WPS_LABEL = 3;
    @SystemApi
    public static final int WPS_PBC = 0;
    private String deviceAddress;
    private String groupName;
    private int groupOwnerBand;
    private int netId;
    private String passphrase;
    private int wpsType;

    public WifiP2pConfig(String str, String str2) {
        this.deviceAddress = DEFAULT_MAC_ADDRESS;
        this.groupName = "";
        this.passphrase = "";
        this.groupOwnerBand = 0;
        this.wpsType = -1;
        this.netId = -1;
        this.groupName = str;
        this.passphrase = str2;
    }

    public WifiP2pConfig() {
        this(null, null);
    }

    public String getDeviceAddress() {
        return this.deviceAddress;
    }

    public void setDeviceAddress(String str) {
        this.deviceAddress = str;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String str) {
        this.groupName = str;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public void setPassphrase(String str) {
        this.passphrase = str;
    }

    public int getGroupOwnerBand() {
        return this.groupOwnerBand;
    }

    public void setGroupOwnerBand(int i) {
        this.groupOwnerBand = i;
    }

    @SystemApi
    public int getWpsType() {
        return this.wpsType;
    }

    @SystemApi
    public void setWpsType(int i) {
        this.wpsType = i;
    }

    @SystemApi
    public void setPersistentMode(boolean z) {
        if (z) {
            this.netId = -2;
        } else {
            this.netId = -1;
        }
    }

    @SystemApi
    public boolean isPersistentMode() {
        return this.netId == -2;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (((((((((((!parcel.writeString(this.groupName) || !parcel.writeString(this.passphrase)) ? null : 1) == null || !parcel.writeString(this.deviceAddress)) ? null : 1) == null || !parcel.writeInt(this.groupOwnerBand)) ? null : 1) == null || !parcel.writeInt(isPersistentMode() ? 1 : 0)) ? null : 1) == null || !parcel.writeInt(this.wpsType)) ? null : 1) != null) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.deviceAddress = parcel.readString();
        if (parcel.readString() != null) {
            this.wpsType = parcel.readInt();
            parcel.readString();
            parcel.readString();
        }
        parcel.readInt();
        this.netId = parcel.readInt();
        this.groupName = parcel.readString();
        this.passphrase = parcel.readString();
        this.groupOwnerBand = parcel.readInt();
        return true;
    }
}
