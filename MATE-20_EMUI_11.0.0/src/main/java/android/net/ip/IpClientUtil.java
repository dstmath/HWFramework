package android.net.ip;

import android.content.Context;
import android.net.DhcpResultsParcelable;
import android.net.LinkProperties;
import android.net.NetworkStackClient;
import android.net.ip.IIpClientCallbacks;
import android.net.shared.IpConfigurationParcelableUtil;
import android.os.ConditionVariable;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class IpClientUtil {
    public static final String DUMP_ARG = "ipclient";

    public static class WaitForProvisioningCallbacks extends IpClientCallbacks {
        private final ConditionVariable mCV = new ConditionVariable();
        private LinkProperties mCallbackLinkProperties;

        public LinkProperties waitForProvisioning() {
            this.mCV.block();
            return this.mCallbackLinkProperties;
        }

        @Override // android.net.ip.IpClientCallbacks
        public void onProvisioningSuccess(LinkProperties newLp) {
            this.mCallbackLinkProperties = newLp;
            this.mCV.open();
        }

        @Override // android.net.ip.IpClientCallbacks
        public void onProvisioningFailure(LinkProperties newLp) {
            this.mCallbackLinkProperties = null;
            this.mCV.open();
        }
    }

    public static void makeIpClient(Context context, String ifName, IpClientCallbacks callback) {
        NetworkStackClient.getInstance().makeIpClient(ifName, new IpClientCallbacksProxy(callback));
    }

    private static class IpClientCallbacksProxy extends IIpClientCallbacks.Stub {
        protected final IpClientCallbacks mCb;

        public IpClientCallbacksProxy(IpClientCallbacks cb) {
            this.mCb = cb;
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onIpClientCreated(IIpClient ipClient) {
            this.mCb.onIpClientCreated(ipClient);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onPreDhcpAction() {
            this.mCb.onPreDhcpAction();
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onPostDhcpAction() {
            this.mCb.onPostDhcpAction();
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onNewDhcpResults(DhcpResultsParcelable dhcpResults) {
            this.mCb.onNewDhcpResults(IpConfigurationParcelableUtil.fromStableParcelable(dhcpResults));
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onProvisioningSuccess(LinkProperties newLp) {
            this.mCb.onProvisioningSuccess(newLp);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onProvisioningFailure(LinkProperties newLp) {
            this.mCb.onProvisioningFailure(newLp);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onLinkPropertiesChange(LinkProperties newLp) {
            this.mCb.onLinkPropertiesChange(newLp);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onReachabilityLost(String logMsg) {
            this.mCb.onReachabilityLost(logMsg);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onQuit() {
            this.mCb.onQuit();
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void installPacketFilter(byte[] filter) {
            this.mCb.installPacketFilter(filter);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void startReadPacketFilter() {
            this.mCb.startReadPacketFilter();
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void setFallbackMulticastFilter(boolean enabled) {
            this.mCb.setFallbackMulticastFilter(enabled);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void setNeighborDiscoveryOffload(boolean enable) {
            this.mCb.setNeighborDiscoveryOffload(enable);
        }

        @Override // android.net.ip.IIpClientCallbacks
        public int getInterfaceVersion() {
            return 3;
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void doArpDetection(int type, String uniqueStr, DhcpResultsParcelable dhcpResults) {
            this.mCb.doArpDetection(type, uniqueStr, IpConfigurationParcelableUtil.fromStableParcelable(dhcpResults));
        }
    }

    public static void dumpIpClient(IIpClient connector, FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("IpClient logs have moved to dumpsys network_stack");
    }
}
