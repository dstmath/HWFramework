package android.net.shared;

import android.net.Network;
import android.net.ProvisioningConfigurationParcelable;
import android.net.StaticIpConfiguration;
import android.net.apf.ApfCapabilities;
import java.util.Objects;
import java.util.StringJoiner;

public class ProvisioningConfiguration {
    private static final int DEFAULT_TIMEOUT_MS = 36000;
    public ApfCapabilities mApfCapabilities;
    public String mDisplayName;
    public boolean mEnableIPv4;
    public boolean mEnableIPv6;
    public int mIPv6AddrGenMode;
    public InitialConfiguration mInitialConfig;
    public Network mNetwork;
    public int mProvisioningTimeoutMs;
    public int mRequestedPreDhcpActionMs;
    public StaticIpConfiguration mStaticIpConfig;
    public boolean mUsingIpReachabilityMonitor;
    public boolean mUsingMultinetworkPolicyTracker;

    public static class Builder {
        protected ProvisioningConfiguration mConfig = new ProvisioningConfiguration();

        public Builder withoutIPv4() {
            this.mConfig.mEnableIPv4 = false;
            return this;
        }

        public Builder withoutIPv6() {
            this.mConfig.mEnableIPv6 = false;
            return this;
        }

        public Builder withoutMultinetworkPolicyTracker() {
            this.mConfig.mUsingMultinetworkPolicyTracker = false;
            return this;
        }

        public Builder withoutIpReachabilityMonitor() {
            this.mConfig.mUsingIpReachabilityMonitor = false;
            return this;
        }

        public Builder withPreDhcpAction() {
            this.mConfig.mRequestedPreDhcpActionMs = ProvisioningConfiguration.DEFAULT_TIMEOUT_MS;
            return this;
        }

        public Builder withPreDhcpAction(int dhcpActionTimeoutMs) {
            this.mConfig.mRequestedPreDhcpActionMs = dhcpActionTimeoutMs;
            return this;
        }

        public Builder withInitialConfiguration(InitialConfiguration initialConfig) {
            this.mConfig.mInitialConfig = initialConfig;
            return this;
        }

        public Builder withStaticConfiguration(StaticIpConfiguration staticConfig) {
            this.mConfig.mStaticIpConfig = staticConfig;
            return this;
        }

        public Builder withApfCapabilities(ApfCapabilities apfCapabilities) {
            this.mConfig.mApfCapabilities = apfCapabilities;
            return this;
        }

        public Builder withProvisioningTimeoutMs(int timeoutMs) {
            this.mConfig.mProvisioningTimeoutMs = timeoutMs;
            return this;
        }

        public Builder withRandomMacAddress() {
            this.mConfig.mIPv6AddrGenMode = 0;
            return this;
        }

        public Builder withStableMacAddress() {
            this.mConfig.mIPv6AddrGenMode = 2;
            return this;
        }

        public Builder withNetwork(Network network) {
            this.mConfig.mNetwork = network;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.mConfig.mDisplayName = displayName;
            return this;
        }

        public ProvisioningConfiguration build() {
            return new ProvisioningConfiguration(this.mConfig);
        }
    }

    public ProvisioningConfiguration() {
        this.mEnableIPv4 = true;
        this.mEnableIPv6 = true;
        this.mUsingMultinetworkPolicyTracker = true;
        this.mUsingIpReachabilityMonitor = true;
        this.mProvisioningTimeoutMs = DEFAULT_TIMEOUT_MS;
        this.mIPv6AddrGenMode = 2;
        this.mNetwork = null;
        this.mDisplayName = null;
    }

    public ProvisioningConfiguration(ProvisioningConfiguration other) {
        this.mEnableIPv4 = true;
        this.mEnableIPv6 = true;
        this.mUsingMultinetworkPolicyTracker = true;
        this.mUsingIpReachabilityMonitor = true;
        this.mProvisioningTimeoutMs = DEFAULT_TIMEOUT_MS;
        this.mIPv6AddrGenMode = 2;
        StaticIpConfiguration staticIpConfiguration = null;
        this.mNetwork = null;
        this.mDisplayName = null;
        this.mEnableIPv4 = other.mEnableIPv4;
        this.mEnableIPv6 = other.mEnableIPv6;
        this.mUsingMultinetworkPolicyTracker = other.mUsingMultinetworkPolicyTracker;
        this.mUsingIpReachabilityMonitor = other.mUsingIpReachabilityMonitor;
        this.mRequestedPreDhcpActionMs = other.mRequestedPreDhcpActionMs;
        this.mInitialConfig = InitialConfiguration.copy(other.mInitialConfig);
        StaticIpConfiguration staticIpConfiguration2 = other.mStaticIpConfig;
        this.mStaticIpConfig = staticIpConfiguration2 != null ? new StaticIpConfiguration(staticIpConfiguration2) : staticIpConfiguration;
        this.mApfCapabilities = other.mApfCapabilities;
        this.mProvisioningTimeoutMs = other.mProvisioningTimeoutMs;
        this.mIPv6AddrGenMode = other.mIPv6AddrGenMode;
        this.mNetwork = other.mNetwork;
        this.mDisplayName = other.mDisplayName;
    }

    public ProvisioningConfigurationParcelable toStableParcelable() {
        ProvisioningConfigurationParcelable p = new ProvisioningConfigurationParcelable();
        p.enableIPv4 = this.mEnableIPv4;
        p.enableIPv6 = this.mEnableIPv6;
        p.usingMultinetworkPolicyTracker = this.mUsingMultinetworkPolicyTracker;
        p.usingIpReachabilityMonitor = this.mUsingIpReachabilityMonitor;
        p.requestedPreDhcpActionMs = this.mRequestedPreDhcpActionMs;
        InitialConfiguration initialConfiguration = this.mInitialConfig;
        StaticIpConfiguration staticIpConfiguration = null;
        p.initialConfig = initialConfiguration == null ? null : initialConfiguration.toStableParcelable();
        StaticIpConfiguration staticIpConfiguration2 = this.mStaticIpConfig;
        if (staticIpConfiguration2 != null) {
            staticIpConfiguration = new StaticIpConfiguration(staticIpConfiguration2);
        }
        p.staticIpConfig = staticIpConfiguration;
        p.apfCapabilities = this.mApfCapabilities;
        p.provisioningTimeoutMs = this.mProvisioningTimeoutMs;
        p.ipv6AddrGenMode = this.mIPv6AddrGenMode;
        p.network = this.mNetwork;
        p.displayName = this.mDisplayName;
        return p;
    }

    public static ProvisioningConfiguration fromStableParcelable(ProvisioningConfigurationParcelable p) {
        StaticIpConfiguration staticIpConfiguration = null;
        if (p == null) {
            return null;
        }
        ProvisioningConfiguration config = new ProvisioningConfiguration();
        config.mEnableIPv4 = p.enableIPv4;
        config.mEnableIPv6 = p.enableIPv6;
        config.mUsingMultinetworkPolicyTracker = p.usingMultinetworkPolicyTracker;
        config.mUsingIpReachabilityMonitor = p.usingIpReachabilityMonitor;
        config.mRequestedPreDhcpActionMs = p.requestedPreDhcpActionMs;
        config.mInitialConfig = InitialConfiguration.fromStableParcelable(p.initialConfig);
        if (p.staticIpConfig != null) {
            staticIpConfiguration = new StaticIpConfiguration(p.staticIpConfig);
        }
        config.mStaticIpConfig = staticIpConfiguration;
        config.mApfCapabilities = p.apfCapabilities;
        config.mProvisioningTimeoutMs = p.provisioningTimeoutMs;
        config.mIPv6AddrGenMode = p.ipv6AddrGenMode;
        config.mNetwork = p.network;
        config.mDisplayName = p.displayName;
        return config;
    }

    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ", getClass().getSimpleName() + "{", "}");
        StringJoiner add = stringJoiner.add("mEnableIPv4: " + this.mEnableIPv4);
        StringJoiner add2 = add.add("mEnableIPv6: " + this.mEnableIPv6);
        StringJoiner add3 = add2.add("mUsingMultinetworkPolicyTracker: " + this.mUsingMultinetworkPolicyTracker);
        StringJoiner add4 = add3.add("mUsingIpReachabilityMonitor: " + this.mUsingIpReachabilityMonitor);
        StringJoiner add5 = add4.add("mRequestedPreDhcpActionMs: " + this.mRequestedPreDhcpActionMs);
        StringJoiner add6 = add5.add("mInitialConfig: " + this.mInitialConfig);
        StringJoiner add7 = add6.add("mStaticIpConfig: " + this.mStaticIpConfig);
        StringJoiner add8 = add7.add("mApfCapabilities: " + this.mApfCapabilities);
        StringJoiner add9 = add8.add("mProvisioningTimeoutMs: " + this.mProvisioningTimeoutMs);
        StringJoiner add10 = add9.add("mIPv6AddrGenMode: " + this.mIPv6AddrGenMode);
        StringJoiner add11 = add10.add("mNetwork: " + this.mNetwork);
        return add11.add("mDisplayName: " + this.mDisplayName).toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ProvisioningConfiguration)) {
            return false;
        }
        ProvisioningConfiguration other = (ProvisioningConfiguration) obj;
        if (this.mEnableIPv4 == other.mEnableIPv4 && this.mEnableIPv6 == other.mEnableIPv6 && this.mUsingMultinetworkPolicyTracker == other.mUsingMultinetworkPolicyTracker && this.mUsingIpReachabilityMonitor == other.mUsingIpReachabilityMonitor && this.mRequestedPreDhcpActionMs == other.mRequestedPreDhcpActionMs && Objects.equals(this.mInitialConfig, other.mInitialConfig) && Objects.equals(this.mStaticIpConfig, other.mStaticIpConfig) && Objects.equals(this.mApfCapabilities, other.mApfCapabilities) && this.mProvisioningTimeoutMs == other.mProvisioningTimeoutMs && this.mIPv6AddrGenMode == other.mIPv6AddrGenMode && Objects.equals(this.mNetwork, other.mNetwork) && Objects.equals(this.mDisplayName, other.mDisplayName)) {
            return true;
        }
        return false;
    }

    public boolean isValid() {
        InitialConfiguration initialConfiguration = this.mInitialConfig;
        return initialConfiguration == null || initialConfiguration.isValid();
    }
}
