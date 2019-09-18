package android.net.ip;

import android.content.Context;
import android.net.Network;
import android.net.StaticIpConfiguration;
import android.net.apf.ApfCapabilities;
import android.net.ip.IpClient;

public class IpManager extends IpClient {

    public static class Callback extends IpClient.Callback {
    }

    public static class InitialConfiguration extends IpClient.InitialConfiguration {
    }

    public static class ProvisioningConfiguration extends IpClient.ProvisioningConfiguration {

        public static class Builder extends IpClient.ProvisioningConfiguration.Builder {
            public Builder withoutIPv4() {
                super.withoutIPv4();
                return this;
            }

            public Builder withoutIPv6() {
                super.withoutIPv6();
                return this;
            }

            public Builder withoutIpReachabilityMonitor() {
                super.withoutIpReachabilityMonitor();
                return this;
            }

            public Builder withPreDhcpAction() {
                super.withPreDhcpAction();
                return this;
            }

            public Builder withPreDhcpAction(int dhcpActionTimeoutMs) {
                super.withPreDhcpAction(dhcpActionTimeoutMs);
                return this;
            }

            public Builder withInitialConfiguration(InitialConfiguration initialConfig) {
                super.withInitialConfiguration(initialConfig);
                return this;
            }

            public Builder withStaticConfiguration(StaticIpConfiguration staticConfig) {
                super.withStaticConfiguration(staticConfig);
                return this;
            }

            public Builder withApfCapabilities(ApfCapabilities apfCapabilities) {
                super.withApfCapabilities(apfCapabilities);
                return this;
            }

            public Builder withProvisioningTimeoutMs(int timeoutMs) {
                super.withProvisioningTimeoutMs(timeoutMs);
                return this;
            }

            public Builder withNetwork(Network network) {
                super.withNetwork(network);
                return this;
            }

            public Builder withDisplayName(String displayName) {
                super.withDisplayName(displayName);
                return this;
            }

            public ProvisioningConfiguration build() {
                return new ProvisioningConfiguration(super.build());
            }
        }

        public ProvisioningConfiguration(IpClient.ProvisioningConfiguration ipcConfig) {
            super(ipcConfig);
        }
    }

    public static ProvisioningConfiguration.Builder buildProvisioningConfiguration() {
        return new ProvisioningConfiguration.Builder();
    }

    public IpManager(Context context, String ifName, Callback callback) {
        super(context, ifName, callback);
    }

    public void startProvisioning(ProvisioningConfiguration req) {
        super.startProvisioning((IpClient.ProvisioningConfiguration) req);
    }
}
