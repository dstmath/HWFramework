package android.net.wifi.p2p;

import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WpsInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class WifiP2pConfig implements Parcelable {
    public static final Parcelable.Creator<WifiP2pConfig> CREATOR = new Parcelable.Creator<WifiP2pConfig>() {
        public WifiP2pConfig createFromParcel(Parcel in) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = in.readString();
            config.wps = (WpsInfo) in.readParcelable(null);
            config.groupOwnerIntent = in.readInt();
            config.netId = in.readInt();
            return config;
        }

        public WifiP2pConfig[] newArray(int size) {
            return new WifiP2pConfig[size];
        }
    };
    public static final int MAX_GROUP_OWNER_INTENT = 15;
    public static final int MIN_GROUP_OWNER_INTENT = 0;
    public String deviceAddress;
    public int groupOwnerIntent;
    public int netId;
    public WpsInfo wps;

    public WifiP2pConfig() {
        this.deviceAddress = "";
        this.groupOwnerIntent = -1;
        this.netId = -2;
        this.wps = new WpsInfo();
        this.wps.setup = 0;
    }

    public void invalidate() {
        this.deviceAddress = "";
    }

    public WifiP2pConfig(String supplicantEvent) throws IllegalArgumentException {
        int devPasswdId;
        this.deviceAddress = "";
        this.groupOwnerIntent = -1;
        this.netId = -2;
        String[] tokens = supplicantEvent.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (tokens.length < 2 || !tokens[0].equals("P2P-GO-NEG-REQUEST")) {
            throw new IllegalArgumentException("Malformed supplicant event");
        }
        this.deviceAddress = tokens[1];
        this.wps = new WpsInfo();
        if (tokens.length > 2) {
            try {
                devPasswdId = Integer.parseInt(tokens[2].split("=")[1]);
            } catch (NumberFormatException e) {
                devPasswdId = 0;
            }
            if (devPasswdId != 1) {
                switch (devPasswdId) {
                    case 4:
                        this.wps.setup = 0;
                        return;
                    case 5:
                        this.wps.setup = 2;
                        return;
                    default:
                        this.wps.setup = 0;
                        return;
                }
            } else {
                this.wps.setup = 1;
            }
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\n address: ");
        sbuf.append(this.deviceAddress);
        sbuf.append("\n wps: ");
        sbuf.append(this.wps);
        sbuf.append("\n groupOwnerIntent: ");
        sbuf.append(this.groupOwnerIntent);
        sbuf.append("\n persist: ");
        sbuf.append(this.netId);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pConfig(WifiP2pConfig source) {
        this.deviceAddress = "";
        this.groupOwnerIntent = -1;
        this.netId = -2;
        if (source != null) {
            this.deviceAddress = source.deviceAddress;
            this.wps = new WpsInfo(source.wps);
            this.groupOwnerIntent = source.groupOwnerIntent;
            this.netId = source.netId;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeParcelable(this.wps, flags);
        dest.writeInt(this.groupOwnerIntent);
        dest.writeInt(this.netId);
    }
}
