package ohos.wifi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import ohos.annotation.SystemApi;
import ohos.net.LinkAddress;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public class IpConfig implements Sequenceable {
    private static final int MAX_DNS_SERVERS = 8;
    private final ArrayList<InetAddress> dnsServers = new ArrayList<>();
    private String domains;
    private InetAddress gateway;
    private LinkAddress ipAddress;

    private void writeInetAddress(Parcel parcel, InetAddress inetAddress) {
        parcel.writeByteArray(inetAddress != null ? inetAddress.getAddress() : null);
    }

    private InetAddress readInetAddress(Parcel parcel) {
        try {
            return InetAddress.getByAddress(parcel.readByteArray());
        } catch (UnknownHostException unused) {
            return null;
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(this.ipAddress);
        writeInetAddress(parcel, this.gateway);
        boolean writeInt = parcel.writeInt(this.dnsServers.size());
        Iterator<InetAddress> it = this.dnsServers.iterator();
        while (it.hasNext()) {
            writeInetAddress(parcel, it.next());
        }
        return writeInt && parcel.writeString(this.domains);
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.ipAddress = new LinkAddress();
        parcel.readSequenceable(this.ipAddress);
        this.gateway = readInetAddress(parcel);
        this.dnsServers.clear();
        int readInt = parcel.readInt();
        if (readInt < 0 || readInt > 8) {
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            InetAddress readInetAddress = readInetAddress(parcel);
            if (readInetAddress != null) {
                this.dnsServers.add(readInetAddress);
            }
        }
        this.domains = parcel.readString();
        return true;
    }
}
