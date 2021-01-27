package ohos.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import ohos.event.intentagent.IntentAgent;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class VpnConfig implements Sequenceable {
    public List<String> acceptedApplications;
    public List<String> dnsAddresses;
    public HttpProxy httpProxy;
    public IntentAgent intentAgent;
    public List<LinkAddress> interfaceAddresses = new ArrayList();
    public String interfaze;
    public boolean isAcceptBypass;
    public boolean isAcceptIPv4;
    public boolean isAcceptIPv6;
    public boolean isBlock;
    public boolean isLegacy;
    public boolean isMetered = true;
    public int mtu = -1;
    public List<String> refusedApplications;
    public List<RouteInfo> routes = new ArrayList();
    public List<String> searchDomains;
    public String sessionName;
    public long startTime = -1;
    public NetHandle[] underlyingNetHandle;
    public String user;

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return true;
    }

    public void updateAcceptAddressType(InetAddress inetAddress) {
        if (inetAddress instanceof Inet4Address) {
            this.isAcceptIPv4 = true;
        } else {
            this.isAcceptIPv6 = true;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if (parcel == null) {
            return false;
        }
        parcel.writeString(this.user);
        parcel.writeString(this.interfaze);
        parcel.writeString(this.sessionName);
        parcel.writeInt(this.mtu);
        parcel.writeSequenceableList(this.interfaceAddresses);
        parcel.writeSequenceableList(this.routes);
        parcel.writeStringList(this.dnsAddresses);
        parcel.writeStringList(this.searchDomains);
        parcel.writeStringList(this.acceptedApplications);
        parcel.writeStringList(this.refusedApplications);
        parcel.writeSequenceable(null);
        parcel.writeLong(this.startTime);
        parcel.writeInt(this.isLegacy ? 1 : 0);
        parcel.writeInt(this.isBlock ? 1 : 0);
        parcel.writeInt(this.isAcceptBypass ? 1 : 0);
        parcel.writeInt(this.isAcceptIPv4 ? 1 : 0);
        parcel.writeInt(this.isAcceptIPv6 ? 1 : 0);
        parcel.writeInt(this.isMetered ? 1 : 0);
        parcel.writeTypedSequenceableArray(null);
        parcel.writeSequenceable(null);
        return true;
    }
}
