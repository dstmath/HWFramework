package ohos.net;

import java.util.Objects;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class NetCapabilities implements Sequenceable {
    public static final int BEARER_BLUETOOTH = 2;
    public static final int BEARER_CELLULAR = 0;
    public static final int BEARER_ETHERNET = 3;
    public static final int BEARER_LOWPAN = 6;
    public static final int BEARER_TEST = 7;
    public static final int BEARER_VPN = 4;
    public static final int BEARER_WIFI = 1;
    public static final int BEARER_WIFI_AWARE = 5;
    private static final long DEFAULT_CAPABILITIES = 57344;
    private static final long FORCE_RESTRICTED_CAPABILITIES = 4194304;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NetCap");
    public static final int LINK_BANDWIDTH_UNSPECIFIED = 0;
    private static final int MAX_BEARER = 7;
    private static final int MAX_NET_CAPABILITY = 32;
    private static final int MAX_VECTOR_SIZE = 1024;
    private static final int MIN_BEARER = 0;
    private static final int MIN_NET_CAPABILITY = 0;
    public static final int NET_CAPABILITY_BIP0 = 25;
    public static final int NET_CAPABILITY_BIP1 = 26;
    public static final int NET_CAPABILITY_BIP2 = 27;
    public static final int NET_CAPABILITY_BIP3 = 28;
    public static final int NET_CAPABILITY_BIP4 = 29;
    public static final int NET_CAPABILITY_BIP5 = 30;
    public static final int NET_CAPABILITY_BIP6 = 31;
    public static final int NET_CAPABILITY_CAPTIVE_PORTAL = 17;
    public static final int NET_CAPABILITY_CBS = 5;
    public static final int NET_CAPABILITY_DUN = 2;
    public static final int NET_CAPABILITY_EIMS = 10;
    public static final int NET_CAPABILITY_FOREGROUND = 19;
    public static final int NET_CAPABILITY_FOTA = 3;
    public static final int NET_CAPABILITY_HW_BASE = 24;
    public static final int NET_CAPABILITY_IA = 7;
    public static final int NET_CAPABILITY_IMS = 4;
    public static final int NET_CAPABILITY_INTERNAL_DEFAULT = 32;
    public static final int NET_CAPABILITY_INTERNET = 12;
    public static final int NET_CAPABILITY_MCX = 23;
    public static final int NET_CAPABILITY_MMS = 0;
    public static final int NET_CAPABILITY_NOT_CONGESTED = 20;
    public static final int NET_CAPABILITY_NOT_METERED = 11;
    public static final int NET_CAPABILITY_NOT_RESTRICTED = 13;
    public static final int NET_CAPABILITY_NOT_ROAMING = 18;
    public static final int NET_CAPABILITY_NOT_SUSPENDED = 21;
    public static final int NET_CAPABILITY_NOT_VPN = 15;
    public static final int NET_CAPABILITY_OEM_PAID = 22;
    public static final int NET_CAPABILITY_PARTIAL_CONNECTIVITY = 24;
    public static final int NET_CAPABILITY_RCS = 8;
    public static final int NET_CAPABILITY_SUPL = 1;
    public static final int NET_CAPABILITY_TRUSTED = 14;
    public static final int NET_CAPABILITY_VALIDATED = 16;
    public static final int NET_CAPABILITY_WIFI_P2P = 6;
    public static final int NET_CAPABILITY_XCAP = 9;
    public static final long RESTRICTED_CAPABILITIES = 8390588;
    public static final int SIGNAL_STRENGTH_UNSPECIFIED = Integer.MIN_VALUE;
    public static final long UNRESTRICTED_CAPABILITIES = 4294971459L;
    private BearerPrivateIdentifier bearerPrivateIdentifier;
    private String dualCellDataEnabled;
    private int linkDownBandwidthKbps;
    private int linkUpBandwidthKbps;
    private long networkCap;
    private int signalStrength;
    private String ssid;
    private TransportInfo transportInfo;
    private long transportTypes;
    private UidRange uids;
    private long unwantedNetworkCap;

    public NetCapabilities() {
        clearAll();
        this.networkCap = DEFAULT_CAPABILITIES;
    }

    public NetCapabilities(NetCapabilities netCapabilities) {
        if (netCapabilities != null) {
            set(netCapabilities);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NetCapabilities)) {
            return false;
        }
        NetCapabilities netCapabilities = (NetCapabilities) obj;
        if (this.networkCap == netCapabilities.networkCap && this.unwantedNetworkCap == netCapabilities.unwantedNetworkCap && this.transportTypes == netCapabilities.transportTypes && this.signalStrength == netCapabilities.signalStrength && Objects.equals(this.ssid, netCapabilities.ssid) && Objects.equals(this.dualCellDataEnabled, netCapabilities.dualCellDataEnabled)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        long j = this.unwantedNetworkCap;
        int i = ((int) (this.networkCap & -1)) + (((int) (j >> 32)) * 3) + (((int) (j & -1)) * 5) + (((int) (j >> 32)) * 7);
        long j2 = this.transportTypes;
        return i + (((int) (-1 & j2)) * 11) + (((int) (j2 >> 32)) * 13) + (this.linkUpBandwidthKbps * 17) + (this.linkDownBandwidthKbps * 19) + (Objects.hashCode(this.bearerPrivateIdentifier) * 23) + (this.signalStrength * 29) + (Objects.hashCode(this.uids) * 31) + (Objects.hashCode(this.ssid) * 37) + (Objects.hashCode(this.transportInfo) * 41) + (Objects.hashCode(this.dualCellDataEnabled) * 47);
    }

    public void clearAll() {
        this.unwantedNetworkCap = 0;
        this.transportTypes = 0;
        this.networkCap = 0;
        this.linkDownBandwidthKbps = 0;
        this.linkUpBandwidthKbps = 0;
        this.bearerPrivateIdentifier = null;
        this.transportInfo = null;
        this.signalStrength = Integer.MIN_VALUE;
        this.uids = null;
        this.ssid = null;
        this.dualCellDataEnabled = null;
    }

    public NetCapabilities setSingleUid(int i) {
        this.uids = new UidRange(i, i);
        return this;
    }

    public NetCapabilities setUids(UidRange uidRange) {
        if (uidRange == null) {
            this.uids = null;
        } else {
            this.uids = new UidRange(uidRange.start, uidRange.stop);
        }
        return this;
    }

    public TransportInfo getTransportInfo() {
        return this.transportInfo;
    }

    public NetCapabilities addCapability(int i) {
        if (i >= 0 && i <= 32) {
            long j = 1 << i;
            this.networkCap |= j;
            this.unwantedNetworkCap &= ~j;
        }
        return this;
    }

    public NetCapabilities removeCapability(int i) {
        if (i >= 0 && i <= 32) {
            long j = ~(1 << i);
            this.networkCap &= j;
            this.unwantedNetworkCap = j & this.unwantedNetworkCap;
        }
        return this;
    }

    public NetCapabilities addBearer(int i) {
        if (i >= 0 && i <= 7) {
            this.transportTypes |= (long) (1 << i);
            setBearerPrivateIdentifier(this.bearerPrivateIdentifier);
        }
        return this;
    }

    public NetCapabilities removeBearer(int i) {
        if (i >= 0 && i <= 7) {
            this.transportTypes &= (long) (~(1 << i));
            setBearerPrivateIdentifier(this.bearerPrivateIdentifier);
        }
        return this;
    }

    public NetCapabilities setBearerPrivateIdentifier(BearerPrivateIdentifier bearerPrivateIdentifier2) {
        if (bearerPrivateIdentifier2 != null && Long.bitCount(this.transportTypes) != 1) {
            return this;
        }
        this.bearerPrivateIdentifier = bearerPrivateIdentifier2;
        return this;
    }

    public void maybeMarkCapabilitiesRestricted() {
        boolean z = true;
        boolean z2 = (this.networkCap & FORCE_RESTRICTED_CAPABILITIES) != 0;
        boolean z3 = (this.networkCap & UNRESTRICTED_CAPABILITIES) != 0;
        if ((this.networkCap & RESTRICTED_CAPABILITIES) == 0) {
            z = false;
        }
        if (z2 || (z && !z3)) {
            removeCapability(13);
        }
    }

    public boolean hasCapability(int i) {
        if (i >= 0 && i <= 32) {
            if (((1 << i) & this.networkCap) != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBearer(int i) {
        if (i >= 0 && i <= 7) {
            if (((1 << i) & this.transportTypes) != 0) {
                return true;
            }
        }
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeLong(this.networkCap);
        parcel.writeLong(this.unwantedNetworkCap);
        parcel.writeLong(this.transportTypes);
        parcel.writeInt(this.linkUpBandwidthKbps);
        parcel.writeInt(this.linkDownBandwidthKbps);
        BearerPrivateIdentifier bearerPrivateIdentifier2 = this.bearerPrivateIdentifier;
        if (bearerPrivateIdentifier2 == null) {
            parcel.writeString(null);
        } else if (bearerPrivateIdentifier2 instanceof StringBearerPrivateIdentifier) {
            parcel.writeString("android.net.StringNetworkSpecifier");
            ((StringBearerPrivateIdentifier) this.bearerPrivateIdentifier).marshalling(parcel);
        } else {
            parcel.writeString(null);
        }
        parcel.writeString(null);
        parcel.writeInt(this.signalStrength);
        parcel.writeInt(-1);
        parcel.writeString(this.ssid);
        parcel.writeString(this.dualCellDataEnabled);
        parcel.writeString("");
        parcel.writeString("");
        parcel.writeByte((byte) 0);
        parcel.writeInt(0);
        parcel.writeByte((byte) 0);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.networkCap = parcel.readLong();
        this.unwantedNetworkCap = parcel.readLong();
        this.transportTypes = parcel.readLong();
        this.linkUpBandwidthKbps = parcel.readInt();
        this.linkDownBandwidthKbps = parcel.readInt();
        if ("android.net.StringNetworkSpecifier".equals(parcel.readString())) {
            StringBearerPrivateIdentifier stringBearerPrivateIdentifier = new StringBearerPrivateIdentifier();
            stringBearerPrivateIdentifier.unmarshalling(parcel);
            this.bearerPrivateIdentifier = stringBearerPrivateIdentifier;
        }
        String readString = parcel.readString();
        if (readString != null) {
            HiLog.error(LABEL, "transportInfo: %{public}s", readString);
        }
        this.transportInfo = null;
        this.signalStrength = parcel.readInt();
        int readInt = parcel.readInt();
        if (readInt > 0 && readInt < 1024) {
            for (int i = 0; i < readInt; i++) {
                HiLog.debug(LABEL, "uid:%{public}d, %{public}s, %{public}d, %{public}d", Integer.valueOf(parcel.readInt()), parcel.readString(), Integer.valueOf(parcel.readInt()), Integer.valueOf(parcel.readInt()));
            }
        }
        this.ssid = parcel.readString();
        this.dualCellDataEnabled = parcel.readString();
        parcel.readString();
        parcel.readString();
        parcel.readByte();
        parcel.readInt();
        parcel.readByte();
        return true;
    }

    private void set(NetCapabilities netCapabilities) {
        this.networkCap = netCapabilities.networkCap;
        this.transportTypes = netCapabilities.transportTypes;
        this.linkUpBandwidthKbps = netCapabilities.linkUpBandwidthKbps;
        this.linkDownBandwidthKbps = netCapabilities.linkDownBandwidthKbps;
        this.bearerPrivateIdentifier = netCapabilities.bearerPrivateIdentifier;
        this.transportInfo = netCapabilities.transportInfo;
        this.signalStrength = netCapabilities.signalStrength;
        setUids(netCapabilities.uids);
        this.unwantedNetworkCap = netCapabilities.unwantedNetworkCap;
        this.ssid = netCapabilities.ssid;
        this.dualCellDataEnabled = netCapabilities.dualCellDataEnabled;
    }

    public int getLinkDownstreamBandwidthKbps() {
        return this.linkDownBandwidthKbps;
    }

    public int getLinkUpstreamBandwidthKbps() {
        return this.linkUpBandwidthKbps;
    }
}
