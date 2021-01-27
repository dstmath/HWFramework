package android.net.wifi;

import android.app.ActivityThread;
import android.net.MacAddress;
import android.net.MatchAllNetworkSpecifier;
import android.net.NetworkSpecifier;
import android.net.wifi.hwUtil.SafeDisplayUtil;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.os.Process;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.util.Preconditions;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class WifiNetworkSpecifier extends NetworkSpecifier implements Parcelable {
    public static final Parcelable.Creator<WifiNetworkSpecifier> CREATOR = new Parcelable.Creator<WifiNetworkSpecifier>() {
        /* class android.net.wifi.WifiNetworkSpecifier.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiNetworkSpecifier createFromParcel(Parcel in) {
            return new WifiNetworkSpecifier((PatternMatcher) in.readParcelable(null), Pair.create((MacAddress) in.readParcelable(null), (MacAddress) in.readParcelable(null)), (WifiConfiguration) in.readParcelable(null), in.readInt(), in.readString());
        }

        @Override // android.os.Parcelable.Creator
        public WifiNetworkSpecifier[] newArray(int size) {
            return new WifiNetworkSpecifier[size];
        }
    };
    public final Pair<MacAddress, MacAddress> bssidPatternMatcher;
    public final String requestorPackageName;
    public final int requestorUid;
    public final PatternMatcher ssidPatternMatcher;
    public final WifiConfiguration wifiConfiguration;

    public static final class Builder {
        private static final Pair<MacAddress, MacAddress> MATCH_ALL_BSSID_PATTERN = new Pair<>(MacAddress.ALL_ZEROS_ADDRESS, MacAddress.ALL_ZEROS_ADDRESS);
        private static final String MATCH_ALL_SSID_PATTERN_PATH = ".*";
        private static final String MATCH_EMPTY_SSID_PATTERN_PATH = "";
        private static final MacAddress MATCH_EXACT_BSSID_PATTERN_MASK = MacAddress.BROADCAST_ADDRESS;
        private static final Pair<MacAddress, MacAddress> MATCH_NO_BSSID_PATTERN1 = new Pair<>(MacAddress.BROADCAST_ADDRESS, MacAddress.BROADCAST_ADDRESS);
        private static final Pair<MacAddress, MacAddress> MATCH_NO_BSSID_PATTERN2 = new Pair<>(MacAddress.ALL_ZEROS_ADDRESS, MacAddress.BROADCAST_ADDRESS);
        private Pair<MacAddress, MacAddress> mBssidPatternMatcher = null;
        private boolean mIsEnhancedOpen = false;
        private boolean mIsHiddenSSID = false;
        private PatternMatcher mSsidPatternMatcher = null;
        private WifiEnterpriseConfig mWpa2EnterpriseConfig = null;
        private String mWpa2PskPassphrase = null;
        private WifiEnterpriseConfig mWpa3EnterpriseConfig = null;
        private String mWpa3SaePassphrase = null;

        public Builder setSsidPattern(PatternMatcher ssidPattern) {
            Preconditions.checkNotNull(ssidPattern);
            this.mSsidPatternMatcher = ssidPattern;
            return this;
        }

        public Builder setSsid(String ssid) {
            Preconditions.checkNotNull(ssid);
            if (StandardCharsets.UTF_8.newEncoder().canEncode(ssid)) {
                this.mSsidPatternMatcher = new PatternMatcher(ssid, 0);
                return this;
            }
            throw new IllegalArgumentException("SSID is not a valid unicode string");
        }

        public Builder setBssidPattern(MacAddress baseAddress, MacAddress mask) {
            Preconditions.checkNotNull(baseAddress, mask);
            this.mBssidPatternMatcher = Pair.create(baseAddress, mask);
            return this;
        }

        public Builder setBssid(MacAddress bssid) {
            Preconditions.checkNotNull(bssid);
            this.mBssidPatternMatcher = Pair.create(bssid, MATCH_EXACT_BSSID_PATTERN_MASK);
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
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            if (this.mSsidPatternMatcher.getType() == 0) {
                wifiConfiguration.SSID = "\"" + this.mSsidPatternMatcher.getPath() + "\"";
            }
            if (this.mBssidPatternMatcher.second == MATCH_EXACT_BSSID_PATTERN_MASK) {
                wifiConfiguration.BSSID = this.mBssidPatternMatcher.first.toString();
            }
            setSecurityParamsInWifiConfiguration(wifiConfiguration);
            wifiConfiguration.hiddenSSID = this.mIsHiddenSSID;
            return wifiConfiguration;
        }

        private boolean hasSetAnyPattern() {
            return (this.mSsidPatternMatcher == null && this.mBssidPatternMatcher == null) ? false : true;
        }

        private void setMatchAnyPatternIfUnset() {
            if (this.mSsidPatternMatcher == null) {
                this.mSsidPatternMatcher = new PatternMatcher(MATCH_ALL_SSID_PATTERN_PATH, 2);
            }
            if (this.mBssidPatternMatcher == null) {
                this.mBssidPatternMatcher = MATCH_ALL_BSSID_PATTERN;
            }
        }

        private boolean hasSetMatchNonePattern() {
            if ((this.mSsidPatternMatcher.getType() == 1 || !this.mSsidPatternMatcher.getPath().equals("")) && !this.mBssidPatternMatcher.equals(MATCH_NO_BSSID_PATTERN1) && !this.mBssidPatternMatcher.equals(MATCH_NO_BSSID_PATTERN2)) {
                return false;
            }
            return true;
        }

        private boolean hasSetMatchAllPattern() {
            if (!this.mSsidPatternMatcher.match("") || !this.mBssidPatternMatcher.equals(MATCH_ALL_BSSID_PATTERN)) {
                return false;
            }
            return true;
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

        public WifiNetworkSpecifier build() {
            if (hasSetAnyPattern()) {
                setMatchAnyPatternIfUnset();
                if (hasSetMatchNonePattern()) {
                    throw new IllegalStateException("cannot set match-none pattern for specifier");
                } else if (hasSetMatchAllPattern()) {
                    throw new IllegalStateException("cannot set match-all pattern for specifier");
                } else if (!this.mIsHiddenSSID || this.mSsidPatternMatcher.getType() == 0) {
                    validateSecurityParams();
                    return new WifiNetworkSpecifier(this.mSsidPatternMatcher, this.mBssidPatternMatcher, buildWifiConfiguration(), Process.myUid(), ActivityThread.currentApplication().getApplicationContext().getOpPackageName());
                } else {
                    throw new IllegalStateException("setSsid should also be invoked when setIsHiddenSsid is invoked for network specifier");
                }
            } else {
                throw new IllegalStateException("one of setSsidPattern/setSsid/setBssidPattern/setBssid should be invoked for specifier");
            }
        }
    }

    public WifiNetworkSpecifier() throws IllegalAccessException {
        throw new IllegalAccessException("Use the builder to create an instance");
    }

    public WifiNetworkSpecifier(PatternMatcher ssidPatternMatcher2, Pair<MacAddress, MacAddress> bssidPatternMatcher2, WifiConfiguration wifiConfiguration2, int requestorUid2, String requestorPackageName2) {
        Preconditions.checkNotNull(ssidPatternMatcher2);
        Preconditions.checkNotNull(bssidPatternMatcher2);
        Preconditions.checkNotNull(wifiConfiguration2);
        Preconditions.checkNotNull(requestorPackageName2);
        this.ssidPatternMatcher = ssidPatternMatcher2;
        this.bssidPatternMatcher = bssidPatternMatcher2;
        this.wifiConfiguration = wifiConfiguration2;
        this.requestorUid = requestorUid2;
        this.requestorPackageName = requestorPackageName2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.ssidPatternMatcher, flags);
        dest.writeParcelable(this.bssidPatternMatcher.first, flags);
        dest.writeParcelable(this.bssidPatternMatcher.second, flags);
        dest.writeParcelable(this.wifiConfiguration, flags);
        dest.writeInt(this.requestorUid);
        dest.writeString(this.requestorPackageName);
    }

    public int hashCode() {
        return Objects.hash(this.ssidPatternMatcher.getPath(), Integer.valueOf(this.ssidPatternMatcher.getType()), this.bssidPatternMatcher, this.wifiConfiguration.allowedKeyManagement, Integer.valueOf(this.requestorUid), this.requestorPackageName);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WifiNetworkSpecifier)) {
            return false;
        }
        WifiNetworkSpecifier lhs = (WifiNetworkSpecifier) obj;
        if (!Objects.equals(this.ssidPatternMatcher.getPath(), lhs.ssidPatternMatcher.getPath()) || !Objects.equals(Integer.valueOf(this.ssidPatternMatcher.getType()), Integer.valueOf(lhs.ssidPatternMatcher.getType())) || !Objects.equals(this.bssidPatternMatcher, lhs.bssidPatternMatcher) || !Objects.equals(this.wifiConfiguration.allowedKeyManagement, lhs.wifiConfiguration.allowedKeyManagement) || this.requestorUid != lhs.requestorUid || !TextUtils.equals(this.requestorPackageName, lhs.requestorPackageName)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "WifiNetworkSpecifier [, SSID Match pattern=" + this.ssidPatternMatcher + ", BSSID Match pattern=" + this.bssidPatternMatcher + ", SSID=" + SafeDisplayUtil.safeDisplaySsid(this.wifiConfiguration.SSID) + ", BSSID=" + SafeDisplayUtil.safeDisplayBssid(this.wifiConfiguration.BSSID) + ", requestorUid=" + this.requestorUid + ", requestorPackageName=" + this.requestorPackageName + "]";
    }

    @Override // android.net.NetworkSpecifier
    public boolean satisfiedBy(NetworkSpecifier other) {
        if (this == other || other == null || (other instanceof MatchAllNetworkSpecifier)) {
            return true;
        }
        if (other instanceof WifiNetworkAgentSpecifier) {
            return ((WifiNetworkAgentSpecifier) other).satisfiesNetworkSpecifier(this);
        }
        return equals(other);
    }

    @Override // android.net.NetworkSpecifier
    public void assertValidFromUid(int requestorUid2) {
        if (this.requestorUid != requestorUid2) {
            throw new SecurityException("mismatched UIDs");
        }
    }
}
