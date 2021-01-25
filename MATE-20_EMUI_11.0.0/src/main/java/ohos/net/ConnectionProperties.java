package ohos.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ConnectionProperties implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "ConnectionProperties");
    private static final int MAX_VECTOR_SIZE = 1024;
    private static final int READ_PARCEL = 4;
    private ArrayList<InetAddress> mDnses;
    private String mDomains;
    private HttpProxy mHttpProxy;
    private String mInterfaceName;
    private boolean mIsUsePrivateDns;
    private final ArrayList<LinkAddress> mLinkAddresses;
    private int mMtu;
    private IpPrefix mNat64Prefix;
    private String mPrivateDnsServerName;
    private ArrayList<RouteInfo> mRoutes;
    private String mTcpBufferSizes;

    public boolean addStackedLink(ConnectionProperties connectionProperties) {
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return true;
    }

    public ConnectionProperties() {
        this.mRoutes = new ArrayList<>();
        this.mDnses = new ArrayList<>();
        this.mLinkAddresses = new ArrayList<>();
        this.mInterfaceName = null;
    }

    public ConnectionProperties(ConnectionProperties connectionProperties) {
        this.mRoutes = new ArrayList<>();
        this.mDnses = new ArrayList<>();
        this.mLinkAddresses = new ArrayList<>();
        if (connectionProperties != null) {
            this.mInterfaceName = connectionProperties.mInterfaceName;
            this.mIsUsePrivateDns = connectionProperties.mIsUsePrivateDns;
            this.mPrivateDnsServerName = connectionProperties.mPrivateDnsServerName;
            this.mDomains = connectionProperties.mDomains;
            HttpProxy httpProxy = connectionProperties.mHttpProxy;
            this.mHttpProxy = httpProxy == null ? null : new HttpProxy(httpProxy);
            this.mLinkAddresses.addAll(connectionProperties.mLinkAddresses);
            this.mDnses.addAll(connectionProperties.mDnses);
            this.mRoutes.addAll(connectionProperties.mRoutes);
            setMtu(connectionProperties.mMtu);
            this.mNat64Prefix = null;
        }
    }

    public void setInterfaceName(String str) {
        this.mInterfaceName = str;
    }

    public boolean addRoute(RouteInfo routeInfo) {
        String str = routeInfo.getInterface();
        if (str != null && !Objects.equals(str, this.mInterfaceName)) {
            return false;
        }
        RouteInfo routeInfo2 = new RouteInfo(routeInfo.getDestination(), routeInfo.getGateway(), this.mInterfaceName, 0);
        ArrayList<RouteInfo> arrayList = this.mRoutes;
        if (arrayList == null || arrayList.indexOf(routeInfo2) >= 0) {
            return false;
        }
        this.mRoutes.add(routeInfo2);
        return true;
    }

    public void clear() {
        this.mInterfaceName = null;
        this.mPrivateDnsServerName = null;
        this.mDomains = null;
        this.mHttpProxy = null;
        this.mIsUsePrivateDns = false;
        this.mMtu = 0;
        this.mLinkAddresses.clear();
        this.mDnses.clear();
        this.mRoutes.clear();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConnectionProperties)) {
            return false;
        }
        ConnectionProperties connectionProperties = (ConnectionProperties) obj;
        return isInterfaceNameEquals(connectionProperties) && isAddressesEquals(connectionProperties) && isDnsesEquals(connectionProperties) && isPrivateDnsEquals(connectionProperties) && isRoutesEquals(connectionProperties) && isHttpProxyEquals(connectionProperties) && isMtuEquals(connectionProperties);
    }

    public int hashCode() {
        String str = this.mInterfaceName;
        int i = 0;
        if (str == null) {
            return 0;
        }
        int hashCode = str.hashCode();
        String str2 = this.mDomains;
        int hashCode2 = hashCode + (str2 == null ? 0 : str2.hashCode());
        HttpProxy httpProxy = this.mHttpProxy;
        int hashCode3 = hashCode2 + (httpProxy == null ? 0 : httpProxy.hashCode());
        String str3 = this.mTcpBufferSizes;
        int hashCode4 = hashCode3 + (str3 == null ? 0 : str3.hashCode());
        String str4 = this.mPrivateDnsServerName;
        int hashCode5 = hashCode4 + (str4 == null ? 0 : str4.hashCode());
        if (this.mIsUsePrivateDns) {
            i = 57;
        }
        return hashCode5 + i + (this.mLinkAddresses.size() * 31) + (this.mDnses.size() * 37) + (this.mRoutes.size() * 41) + (this.mMtu * 51);
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        String readString = parcel.readString();
        if (readString != null) {
            setInterfaceName(readString);
        }
        readLinkAddressFromParcel(parcel);
        readDnsServerFromParcel(parcel);
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            HiLog.error(LABEL, "invalid addressCount", new Object[0]);
            return false;
        }
        for (int i = 0; i < readInt; i++) {
            parcel.readByteArray();
        }
        setUsePrivateDns(parcel.readBoolean());
        setPrivateDnsServerName(parcel.readString());
        int readInt2 = parcel.readInt();
        if (readInt2 > 1024) {
            HiLog.error(LABEL, "invalid pcfSize", new Object[0]);
            return false;
        }
        for (int i2 = 0; i2 < readInt2; i2++) {
            parcel.readByteArray();
        }
        setDomains(parcel.readString());
        setMtu(parcel.readInt());
        setTcpBufferSizes(parcel.readString());
        readRouteFromParcel(parcel);
        HttpProxy httpProxy = new HttpProxy();
        if (parcel.readByte() == 1 && parcel.readString() != null) {
            httpProxy.unmarshalling(parcel);
            setHttpProxy(httpProxy);
        }
        if (parcel.readString() != null) {
            this.mNat64Prefix = new IpPrefix();
            this.mNat64Prefix.unmarshalling(parcel);
        }
        readConnectionListFromParcel(parcel);
        return true;
    }

    public List<InetAddress> getDnsServers() {
        return Collections.unmodifiableList(this.mDnses);
    }

    public String getDomains() {
        return this.mDomains;
    }

    public void setHttpProxy(HttpProxy httpProxy) {
        this.mHttpProxy = httpProxy;
    }

    public HttpProxy getHttpProxy() {
        return this.mHttpProxy;
    }

    public String getInterfaceName() {
        return this.mInterfaceName;
    }

    public void setLinkAddresses(Collection<LinkAddress> collection) {
        if (collection != null) {
            this.mLinkAddresses.clear();
            collection.iterator().forEachRemaining(new Consumer() {
                /* class ohos.net.$$Lambda$ConnectionProperties$gta2lHToD3lh6ECUKgfQAYGYZM */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ConnectionProperties.this.lambda$setLinkAddresses$0$ConnectionProperties((LinkAddress) obj);
                }
            });
        }
    }

    public List<LinkAddress> getLinkAddresses() {
        return Collections.unmodifiableList(this.mLinkAddresses);
    }

    public int getMtu() {
        return this.mMtu;
    }

    public String getPrivateDnsServerName() {
        return this.mPrivateDnsServerName;
    }

    public List<RouteInfo> getRoutes() {
        return Collections.unmodifiableList(this.mRoutes);
    }

    public boolean isPrivateDnsActive() {
        return this.mIsUsePrivateDns;
    }

    public void setMtu(int i) {
        if (i >= 0) {
            this.mMtu = i;
        }
    }

    public void setDomains(String str) {
        this.mDomains = str;
    }

    public void setDnsServers(Collection<InetAddress> collection) {
        if (collection != null) {
            this.mDnses.clear();
            collection.iterator().forEachRemaining(new Consumer() {
                /* class ohos.net.$$Lambda$ConnectionProperties$NA36lOdymWqrnQyMbARTPAScl0 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ConnectionProperties.this.lambda$setDnsServers$1$ConnectionProperties((InetAddress) obj);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: addDnsServer */
    public boolean lambda$setDnsServers$1$ConnectionProperties(InetAddress inetAddress) {
        if (inetAddress == null || this.mDnses.contains(inetAddress)) {
            return false;
        }
        this.mDnses.add(inetAddress);
        return true;
    }

    private int findLinkAddressIndex(LinkAddress linkAddress) {
        if (linkAddress == null) {
            return -1;
        }
        for (int i = 0; i < this.mLinkAddresses.size(); i++) {
            if (Objects.equals(this.mLinkAddresses.get(i).address, linkAddress.address)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* renamed from: addLinkAddress */
    public boolean lambda$setLinkAddresses$0$ConnectionProperties(LinkAddress linkAddress) {
        if (linkAddress == null) {
            return false;
        }
        int findLinkAddressIndex = findLinkAddressIndex(linkAddress);
        if (findLinkAddressIndex < 0) {
            this.mLinkAddresses.add(linkAddress);
            return true;
        } else if (this.mLinkAddresses.get(findLinkAddressIndex).equals(linkAddress)) {
            return false;
        } else {
            this.mLinkAddresses.set(findLinkAddressIndex, linkAddress);
            return true;
        }
    }

    private void setUsePrivateDns(boolean z) {
        this.mIsUsePrivateDns = z;
    }

    private void setPrivateDnsServerName(String str) {
        this.mPrivateDnsServerName = str;
    }

    private void setTcpBufferSizes(String str) {
        this.mTcpBufferSizes = str;
    }

    private boolean isInterfaceNameEquals(ConnectionProperties connectionProperties) {
        return Objects.equals(this.mInterfaceName, connectionProperties.getInterfaceName());
    }

    private boolean isAddressesEquals(ConnectionProperties connectionProperties) {
        List<InetAddress> addresses = connectionProperties.getAddresses();
        List<InetAddress> addresses2 = getAddresses();
        if (addresses2.size() == addresses.size()) {
            return Objects.equals(addresses2, addresses);
        }
        return false;
    }

    private boolean isDnsesEquals(ConnectionProperties connectionProperties) {
        List<InetAddress> dnsServers = connectionProperties.getDnsServers();
        if (Objects.equals(this.mDomains, connectionProperties.getDomains()) && this.mDnses.size() == dnsServers.size()) {
            return Objects.equals(this.mDnses, dnsServers);
        }
        return false;
    }

    private boolean isPrivateDnsEquals(ConnectionProperties connectionProperties) {
        return isPrivateDnsActive() == connectionProperties.isPrivateDnsActive() && Objects.equals(this.mPrivateDnsServerName, connectionProperties.getPrivateDnsServerName());
    }

    private boolean isRoutesEquals(ConnectionProperties connectionProperties) {
        List<RouteInfo> routes = connectionProperties.getRoutes();
        if (this.mRoutes.size() == routes.size()) {
            return Objects.equals(this.mRoutes, routes);
        }
        return false;
    }

    private boolean isHttpProxyEquals(ConnectionProperties connectionProperties) {
        return Objects.equals(getHttpProxy(), connectionProperties.getHttpProxy());
    }

    private boolean isMtuEquals(ConnectionProperties connectionProperties) {
        return Objects.equals(Integer.valueOf(getMtu()), Integer.valueOf(connectionProperties.getMtu()));
    }

    private List<InetAddress> getAddresses() {
        ArrayList arrayList = new ArrayList();
        this.mLinkAddresses.iterator().forEachRemaining(new Consumer(arrayList) {
            /* class ohos.net.$$Lambda$ConnectionProperties$WCvRoTQzHOaryxOK2U8AQD2H0A */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ConnectionProperties.lambda$getAddresses$2(this.f$0, (LinkAddress) obj);
            }
        });
        return Collections.unmodifiableList(arrayList);
    }

    private void readLinkAddressFromParcel(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            HiLog.error(LABEL, "readLinkAddressFromParcel: faild", new Object[0]);
            return;
        }
        for (int i = 0; i < readInt; i++) {
            if (parcel.readString() != null) {
                LinkAddress linkAddress = new LinkAddress();
                linkAddress.unmarshalling(parcel);
                lambda$setLinkAddresses$0$ConnectionProperties(linkAddress);
            }
        }
    }

    private void readDnsServerFromParcel(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            HiLog.error(LABEL, "readDnsServerFromParcel: faild", new Object[0]);
            return;
        }
        for (int i = 0; i < readInt; i++) {
            try {
                lambda$setDnsServers$1$ConnectionProperties(InetAddress.parseNumericAddress(parcel.readString()));
            } catch (IllegalArgumentException unused) {
                HiLog.warn(LABEL, "addDnsServer: faild", new Object[0]);
            }
        }
    }

    private void readRouteFromParcel(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt > 1024) {
            HiLog.error(LABEL, "readRouteFromParcel: faild", new Object[0]);
            return;
        }
        for (int i = 0; i < readInt; i++) {
            if (parcel.readString() != null) {
                RouteInfo routeInfo = new RouteInfo();
                routeInfo.unmarshalling(parcel);
                addRoute(routeInfo);
            }
        }
    }

    private void readConnectionListFromParcel(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt > 0 && readInt <= 1024) {
            ArrayList arrayList = new ArrayList(readInt);
            for (int i = 0; i < readInt; i++) {
                if (parcel.readInt() == 4) {
                    parcel.readString();
                    ConnectionProperties connectionProperties = new ConnectionProperties();
                    connectionProperties.unmarshalling(parcel);
                    arrayList.add(connectionProperties);
                }
            }
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                addStackedLink((ConnectionProperties) it.next());
            }
        }
    }
}
