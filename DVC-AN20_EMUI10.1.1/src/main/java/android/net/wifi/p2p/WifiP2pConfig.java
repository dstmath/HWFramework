package android.net.wifi.p2p;

import android.annotation.UnsupportedAppUsage;
import android.net.MacAddress;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WpsInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.PatternSyntaxException;

public class WifiP2pConfig implements Parcelable {
    public static final Parcelable.Creator<WifiP2pConfig> CREATOR = new Parcelable.Creator<WifiP2pConfig>() {
        /* class android.net.wifi.p2p.WifiP2pConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiP2pConfig createFromParcel(Parcel in) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = in.readString();
            config.wps = (WpsInfo) in.readParcelable(null);
            config.groupOwnerIntent = in.readInt();
            config.netId = in.readInt();
            config.networkName = in.readString();
            config.passphrase = in.readString();
            config.groupOwnerBand = in.readInt();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public WifiP2pConfig[] newArray(int size) {
            return new WifiP2pConfig[size];
        }
    };
    public static final int GROUP_OWNER_BAND_2GHZ = 1;
    public static final int GROUP_OWNER_BAND_5GHZ = 2;
    public static final int GROUP_OWNER_BAND_AUTO = 0;
    public static final int MAX_GROUP_OWNER_INTENT = 15;
    @UnsupportedAppUsage
    public static final int MIN_GROUP_OWNER_INTENT = 0;
    public String deviceAddress;
    public int groupOwnerBand;
    public int groupOwnerIntent;
    @UnsupportedAppUsage
    public int netId;
    public String networkName;
    public String passphrase;
    public WpsInfo wps;

    @Retention(RetentionPolicy.SOURCE)
    public @interface GroupOperatingBandType {
    }

    public WifiP2pConfig() {
        this.deviceAddress = "";
        this.networkName = "";
        this.passphrase = "";
        this.groupOwnerBand = 0;
        this.groupOwnerIntent = -1;
        this.netId = -2;
        this.wps = new WpsInfo();
        this.wps.setup = 0;
    }

    public void invalidate() {
        this.deviceAddress = "";
    }

    @UnsupportedAppUsage
    public WifiP2pConfig(String supplicantEvent) throws IllegalArgumentException {
        int devPasswdId;
        this.deviceAddress = "";
        this.networkName = "";
        this.passphrase = "";
        this.groupOwnerBand = 0;
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
            if (devPasswdId == 1) {
                this.wps.setup = 1;
            } else if (devPasswdId == 4) {
                this.wps.setup = 0;
            } else if (devPasswdId != 5) {
                this.wps.setup = 0;
            } else {
                this.wps.setup = 2;
            }
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\n wps: ");
        sbuf.append(this.wps);
        sbuf.append("\n groupOwnerIntent: ");
        sbuf.append(this.groupOwnerIntent);
        sbuf.append("\n persist: ");
        sbuf.append(this.netId);
        sbuf.append("\n networkName: ");
        sbuf.append(this.networkName);
        sbuf.append("\n passphrase: ");
        sbuf.append(TextUtils.isEmpty(this.passphrase) ? "<empty>" : "<non-empty>");
        sbuf.append("\n groupOwnerBand: ");
        sbuf.append(this.groupOwnerBand);
        return sbuf.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public WifiP2pConfig(WifiP2pConfig source) {
        this.deviceAddress = "";
        this.networkName = "";
        this.passphrase = "";
        this.groupOwnerBand = 0;
        this.groupOwnerIntent = -1;
        this.netId = -2;
        if (source != null) {
            this.deviceAddress = source.deviceAddress;
            this.wps = new WpsInfo(source.wps);
            this.groupOwnerIntent = source.groupOwnerIntent;
            this.netId = source.netId;
            this.networkName = source.networkName;
            this.passphrase = source.passphrase;
            this.groupOwnerBand = source.groupOwnerBand;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeParcelable(this.wps, flags);
        dest.writeInt(this.groupOwnerIntent);
        dest.writeInt(this.netId);
        dest.writeString(this.networkName);
        dest.writeString(this.passphrase);
        dest.writeInt(this.groupOwnerBand);
    }

    public static final class Builder {
        private static final MacAddress MAC_ANY_ADDRESS = MacAddress.fromString("02:00:00:00:00:00");
        private MacAddress mDeviceAddress = MAC_ANY_ADDRESS;
        private int mGroupOperatingBand = 0;
        private int mGroupOperatingFrequency = 0;
        private int mNetId = -1;
        private String mNetworkName = "";
        private String mPassphrase = "";

        public Builder setDeviceAddress(MacAddress deviceAddress) {
            if (deviceAddress == null) {
                this.mDeviceAddress = MAC_ANY_ADDRESS;
            } else {
                this.mDeviceAddress = deviceAddress;
            }
            return this;
        }

        public Builder setNetworkName(String networkName) {
            if (!TextUtils.isEmpty(networkName)) {
                try {
                    if (networkName.matches("^DIRECT-[a-zA-Z0-9]{2}.*")) {
                        this.mNetworkName = networkName;
                        return this;
                    }
                    throw new IllegalArgumentException("network name must starts with the prefix DIRECT-xy.");
                } catch (PatternSyntaxException e) {
                }
            } else {
                throw new IllegalArgumentException("network name must be non-empty.");
            }
        }

        public Builder setPassphrase(String passphrase) {
            if (TextUtils.isEmpty(passphrase)) {
                throw new IllegalArgumentException("passphrase must be non-empty.");
            } else if (passphrase.length() < 8 || passphrase.length() > 63) {
                throw new IllegalArgumentException("The length of a passphrase must be between 8 and 63.");
            } else {
                this.mPassphrase = passphrase;
                return this;
            }
        }

        public Builder setGroupOperatingBand(int band) {
            if (band == 0 || band == 1 || band == 2) {
                this.mGroupOperatingBand = band;
                return this;
            }
            throw new IllegalArgumentException("Invalid constant for the group operating band!");
        }

        public Builder setGroupOperatingFrequency(int frequency) {
            if (frequency >= 0) {
                this.mGroupOperatingFrequency = frequency;
                return this;
            }
            throw new IllegalArgumentException("Invalid group operating frequency!");
        }

        public Builder enablePersistentMode(boolean persistent) {
            if (persistent) {
                this.mNetId = -2;
            } else {
                this.mNetId = -1;
            }
            return this;
        }

        public WifiP2pConfig build() {
            if (TextUtils.isEmpty(this.mNetworkName)) {
                throw new IllegalStateException("network name must be non-empty.");
            } else if (TextUtils.isEmpty(this.mPassphrase)) {
                throw new IllegalStateException("passphrase must be non-empty.");
            } else if (this.mGroupOperatingFrequency <= 0 || this.mGroupOperatingBand <= 0) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = this.mDeviceAddress.toString();
                config.networkName = this.mNetworkName;
                config.passphrase = this.mPassphrase;
                config.groupOwnerBand = 0;
                int i = this.mGroupOperatingFrequency;
                if (i > 0) {
                    config.groupOwnerBand = i;
                } else {
                    int i2 = this.mGroupOperatingBand;
                    if (i2 > 0) {
                        config.groupOwnerBand = i2;
                    }
                }
                config.netId = this.mNetId;
                return config;
            } else {
                throw new IllegalStateException("Preferred frequency and band are mutually exclusive.");
            }
        }
    }
}
