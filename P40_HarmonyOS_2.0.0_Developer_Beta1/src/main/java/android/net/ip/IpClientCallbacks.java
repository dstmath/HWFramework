package android.net.ip;

import android.net.DhcpResults;
import android.net.LinkProperties;

public class IpClientCallbacks {
    public void onIpClientCreated(IIpClient ipClient) {
    }

    public void onPreDhcpAction() {
    }

    public void onPostDhcpAction() {
    }

    public void onNewDhcpResults(DhcpResults dhcpResults) {
    }

    public void onProvisioningSuccess(LinkProperties newLp) {
    }

    public void onProvisioningFailure(LinkProperties newLp) {
    }

    public void onLinkPropertiesChange(LinkProperties newLp) {
    }

    public void onReachabilityLost(String logMsg) {
    }

    public void onQuit() {
    }

    public void installPacketFilter(byte[] filter) {
    }

    public void startReadPacketFilter() {
    }

    public void setFallbackMulticastFilter(boolean enabled) {
    }

    public void setNeighborDiscoveryOffload(boolean enable) {
    }

    public void doArpDetection(int type, String uniqueStr, DhcpResults dhcpResults) {
    }
}
