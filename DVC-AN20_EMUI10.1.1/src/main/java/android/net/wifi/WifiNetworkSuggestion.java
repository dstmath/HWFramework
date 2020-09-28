package android.net.wifi;

import android.app.ActivityThread;
import android.net.MacAddress;
import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class WifiNetworkSuggestion implements Parcelable {
    public static final Parcelable.Creator<WifiNetworkSuggestion> CREATOR = new Parcelable.Creator<WifiNetworkSuggestion>() {
        /* class android.net.wifi.WifiNetworkSuggestion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiNetworkSuggestion createFromParcel(Parcel in) {
            return new WifiNetworkSuggestion((WifiConfiguration) in.readParcelable(null), in.readBoolean(), in.readBoolean(), in.readInt(), in.readString());
        }

        @Override // android.os.Parcelable.Creator
        public WifiNetworkSuggestion[] newArray(int size) {
            return new WifiNetworkSuggestion[size];
        }
    };
    public final boolean isAppInteractionRequired;
    public final boolean isUserInteractionRequired;
    public final String suggestorPackageName;
    public final int suggestorUid;
    public final WifiConfiguration wifiConfiguration;

    public static final class Builder {
        private static final int UNASSIGNED_PRIORITY = -1;
        private MacAddress mBssid = null;
        private boolean mIsAppInteractionRequired = false;
        private boolean mIsEnhancedOpen = false;
        private boolean mIsHiddenSSID = false;
        private boolean mIsMetered = false;
        private boolean mIsUserInteractionRequired = false;
        private int mPriority = -1;
        private String mSsid = null;
        private WifiEnterpriseConfig mWpa2EnterpriseConfig = null;
        private String mWpa2PskPassphrase = null;
        private WifiEnterpriseConfig mWpa3EnterpriseConfig = null;
        private String mWpa3SaePassphrase = null;

        public Builder setSsid(String ssid) {
            Preconditions.checkNotNull(ssid);
            if (StandardCharsets.UTF_8.newEncoder().canEncode(ssid)) {
                this.mSsid = new String(ssid);
                return this;
            }
            throw new IllegalArgumentException("SSID is not a valid unicode string");
        }

        public Builder setBssid(MacAddress bssid) {
            Preconditions.checkNotNull(bssid);
            this.mBssid = MacAddress.fromBytes(bssid.toByteArray());
            return this;
        }

        public Builder setIsEnhancedOpen(boolean isEnhancedOpen) {
            this.mIsEnhancedOpen = isEnhancedOpen;
            return this;
        }

        public Builder setWpa2Passphrase(String passphrase) {
            Preconditions.checkNotNull(passphrase);
            if (StandardCharsets.US_ASCII.newEncoder().canEncode(passphrase)) {
                this.mWpa2PskPassphrase = passphrase;
                return this;
            }
            throw new IllegalArgumentException("passphrase not ASCII encodable");
        }

        public Builder setWpa3Passphrase(String passphrase) {
            Preconditions.checkNotNull(passphrase);
            if (StandardCharsets.US_ASCII.newEncoder().canEncode(passphrase)) {
                this.mWpa3SaePassphrase = passphrase;
                return this;
            }
            throw new IllegalArgumentException("passphrase not ASCII encodable");
        }

        public Builder setWpa2EnterpriseConfig(WifiEnterpriseConfig enterpriseConfig) {
            Preconditions.checkNotNull(enterpriseConfig);
            this.mWpa2EnterpriseConfig = new WifiEnterpriseConfig(enterpriseConfig);
            return this;
        }

        public Builder setWpa3EnterpriseConfig(WifiEnterpriseConfig enterpriseConfig) {
            Preconditions.checkNotNull(enterpriseConfig);
            this.mWpa3EnterpriseConfig = new WifiEnterpriseConfig(enterpriseConfig);
            return this;
        }

        public Builder setIsHiddenSsid(boolean isHiddenSsid) {
            this.mIsHiddenSSID = isHiddenSsid;
            return this;
        }

        public Builder setIsAppInteractionRequired(boolean isAppInteractionRequired) {
            this.mIsAppInteractionRequired = isAppInteractionRequired;
            return this;
        }

        public Builder setIsUserInteractionRequired(boolean isUserInteractionRequired) {
            this.mIsUserInteractionRequired = isUserInteractionRequired;
            return this;
        }

        public Builder setPriority(int priority) {
            if (priority >= 0) {
                this.mPriority = priority;
                return this;
            }
            throw new IllegalArgumentException("Invalid priority value " + priority);
        }

        public Builder setIsMetered(boolean isMetered) {
            this.mIsMetered = isMetered;
            return this;
        }

        private void setSecurityParamsInWifiConfiguration(WifiConfiguration configuration) {
            if (!TextUtils.isEmpty(this.mWpa2PskPassphrase)) {
                configuration.setSecurityParams(2);
                configuration.preSharedKey = "\"" + this.mWpa2PskPassphrase + "\"";
            } else if (!TextUtils.isEmpty(this.mWpa3SaePassphrase)) {
                configuration.setSecurityParams(4);
                configuration.preSharedKey = "\"" + this.mWpa3SaePassphrase + "\"";
            } else if (this.mWpa2EnterpriseConfig != null) {
                configuration.setSecurityParams(3);
                configuration.enterpriseConfig = this.mWpa2EnterpriseConfig;
            } else if (this.mWpa3EnterpriseConfig != null) {
                configuration.setSecurityParams(5);
                configuration.enterpriseConfig = this.mWpa3EnterpriseConfig;
            } else if (this.mIsEnhancedOpen) {
                configuration.setSecurityParams(6);
            } else {
                configuration.setSecurityParams(0);
            }
        }

        private WifiConfiguration buildWifiConfiguration() {
            int i;
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "\"" + this.mSsid + "\"";
            MacAddress macAddress = this.mBssid;
            if (macAddress != null) {
                wifiConfiguration.BSSID = macAddress.toString();
            }
            setSecurityParamsInWifiConfiguration(wifiConfiguration);
            wifiConfiguration.hiddenSSID = this.mIsHiddenSSID;
            wifiConfiguration.priority = this.mPriority;
            if (this.mIsMetered) {
                i = 1;
            } else {
                i = 0;
            }
            wifiConfiguration.meteredOverride = i;
            return wifiConfiguration;
        }

        private void validateSecurityParams() {
            int i = 0;
            int numSecurityTypes = 0 + (this.mIsEnhancedOpen ? 1 : 0) + (!TextUtils.isEmpty(this.mWpa2PskPassphrase) ? 1 : 0) + (!TextUtils.isEmpty(this.mWpa3SaePassphrase) ? 1 : 0) + (this.mWpa2EnterpriseConfig != null ? 1 : 0);
            if (this.mWpa3EnterpriseConfig != null) {
                i = 1;
            }
            if (numSecurityTypes + i > 1) {
                throw new IllegalStateException("only one of setIsEnhancedOpen, setWpa2Passphrase,setWpa3Passphrase, setWpa2EnterpriseConfig or setWpa3EnterpriseConfig can be invoked for network specifier");
            }
        }

        public WifiNetworkSuggestion build() {
            String str = this.mSsid;
            if (str == null) {
                throw new IllegalStateException("setSsid should be invoked for suggestion");
            } else if (!TextUtils.isEmpty(str)) {
                MacAddress macAddress = this.mBssid;
                if (macAddress == null || (!macAddress.equals(MacAddress.BROADCAST_ADDRESS) && !this.mBssid.equals(MacAddress.ALL_ZEROS_ADDRESS))) {
                    validateSecurityParams();
                    return new WifiNetworkSuggestion(buildWifiConfiguration(), this.mIsAppInteractionRequired, this.mIsUserInteractionRequired, Process.myUid(), ActivityThread.currentApplication().getApplicationContext().getOpPackageName());
                }
                throw new IllegalStateException("invalid bssid for suggestion");
            } else {
                throw new IllegalStateException("invalid ssid for suggestion");
            }
        }
    }

    public WifiNetworkSuggestion() {
        this.wifiConfiguration = null;
        this.isAppInteractionRequired = false;
        this.isUserInteractionRequired = false;
        this.suggestorUid = -1;
        this.suggestorPackageName = null;
    }

    public WifiNetworkSuggestion(WifiConfiguration wifiConfiguration2, boolean isAppInteractionRequired2, boolean isUserInteractionRequired2, int suggestorUid2, String suggestorPackageName2) {
        Preconditions.checkNotNull(wifiConfiguration2);
        Preconditions.checkNotNull(suggestorPackageName2);
        this.wifiConfiguration = wifiConfiguration2;
        this.isAppInteractionRequired = isAppInteractionRequired2;
        this.isUserInteractionRequired = isUserInteractionRequired2;
        this.suggestorUid = suggestorUid2;
        this.suggestorPackageName = suggestorPackageName2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.wifiConfiguration, flags);
        dest.writeBoolean(this.isAppInteractionRequired);
        dest.writeBoolean(this.isUserInteractionRequired);
        dest.writeInt(this.suggestorUid);
        dest.writeString(this.suggestorPackageName);
    }

    public int hashCode() {
        return Objects.hash(this.wifiConfiguration.SSID, this.wifiConfiguration.BSSID, this.wifiConfiguration.allowedKeyManagement, Integer.valueOf(this.suggestorUid), this.suggestorPackageName);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WifiNetworkSuggestion)) {
            return false;
        }
        WifiNetworkSuggestion lhs = (WifiNetworkSuggestion) obj;
        if (!Objects.equals(this.wifiConfiguration.SSID, lhs.wifiConfiguration.SSID) || !Objects.equals(this.wifiConfiguration.BSSID, lhs.wifiConfiguration.BSSID) || !Objects.equals(this.wifiConfiguration.allowedKeyManagement, lhs.wifiConfiguration.allowedKeyManagement) || this.suggestorUid != lhs.suggestorUid || !TextUtils.equals(this.suggestorPackageName, lhs.suggestorPackageName)) {
            return false;
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("WifiNetworkSuggestion [");
        sb.append(", SSID=");
        sb.append(SafeDisplayUtil.safeDisplaySsid(this.wifiConfiguration.SSID));
        sb.append(", BSSID=");
        sb.append(SafeDisplayUtil.safeDisplayBssid(this.wifiConfiguration.BSSID));
        sb.append(", isAppInteractionRequired=");
        sb.append(this.isAppInteractionRequired);
        sb.append(", isUserInteractionRequired=");
        sb.append(this.isUserInteractionRequired);
        sb.append(", suggestorUid=");
        sb.append(this.suggestorUid);
        sb.append(", suggestorPackageName=");
        sb.append(this.suggestorPackageName);
        return sb.append("]").toString();
    }
}
