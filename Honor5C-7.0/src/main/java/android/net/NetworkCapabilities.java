package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public final class NetworkCapabilities implements Parcelable {
    public static final Creator<NetworkCapabilities> CREATOR = null;
    private static final long DEFAULT_CAPABILITIES = 57344;
    public static final String MATCH_ALL_REQUESTS_NETWORK_SPECIFIER = "*";
    private static final int MAX_NET_CAPABILITY = 24;
    private static final int MAX_TRANSPORT = 4;
    private static final int MIN_NET_CAPABILITY = 0;
    private static final int MIN_TRANSPORT = 0;
    private static final long MUTABLE_CAPABILITIES = 212992;
    public static final int NET_CAPABILITY_BIP0 = 18;
    public static final int NET_CAPABILITY_BIP1 = 19;
    public static final int NET_CAPABILITY_BIP2 = 20;
    public static final int NET_CAPABILITY_BIP3 = 21;
    public static final int NET_CAPABILITY_BIP4 = 22;
    public static final int NET_CAPABILITY_BIP5 = 23;
    public static final int NET_CAPABILITY_BIP6 = 24;
    public static final int NET_CAPABILITY_CAPTIVE_PORTAL = 17;
    public static final int NET_CAPABILITY_CBS = 5;
    public static final int NET_CAPABILITY_DUN = 2;
    public static final int NET_CAPABILITY_EIMS = 10;
    public static final int NET_CAPABILITY_FOTA = 3;
    public static final int NET_CAPABILITY_IA = 7;
    public static final int NET_CAPABILITY_IMS = 4;
    public static final int NET_CAPABILITY_INTERNET = 12;
    public static final int NET_CAPABILITY_MMS = 0;
    public static final int NET_CAPABILITY_NOT_METERED = 11;
    public static final int NET_CAPABILITY_NOT_RESTRICTED = 13;
    public static final int NET_CAPABILITY_NOT_VPN = 15;
    public static final int NET_CAPABILITY_RCS = 8;
    public static final int NET_CAPABILITY_SUPL = 1;
    public static final int NET_CAPABILITY_TRUSTED = 14;
    public static final int NET_CAPABILITY_VALIDATED = 16;
    public static final int NET_CAPABILITY_WIFI_P2P = 6;
    public static final int NET_CAPABILITY_XCAP = 9;
    private static final long NON_REQUESTABLE_CAPABILITIES = 196608;
    private static final long RESTRICTED_CAPABILITIES = 1980;
    public static final int SIGNAL_STRENGTH_UNSPECIFIED = Integer.MIN_VALUE;
    public static final int TRANSPORT_BLUETOOTH = 2;
    public static final int TRANSPORT_CELLULAR = 0;
    public static final int TRANSPORT_ETHERNET = 3;
    public static final int TRANSPORT_VPN = 4;
    public static final int TRANSPORT_WIFI = 1;
    private String mDualCellDataEnabled;
    private int mLinkDownBandwidthKbps;
    private int mLinkUpBandwidthKbps;
    private long mNetworkCapabilities;
    private String mNetworkSpecifier;
    private int mSignalStrength;
    private long mTransportTypes;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkCapabilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.NetworkCapabilities.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkCapabilities.<clinit>():void");
    }

    public android.net.NetworkCapabilities removeCapability(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkCapabilities.removeCapability(int):android.net.NetworkCapabilities
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkCapabilities.removeCapability(int):android.net.NetworkCapabilities");
    }

    public android.net.NetworkCapabilities removeTransportType(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.NetworkCapabilities.removeTransportType(int):android.net.NetworkCapabilities
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.NetworkCapabilities.removeTransportType(int):android.net.NetworkCapabilities");
    }

    public NetworkCapabilities() {
        clearAll();
        this.mNetworkCapabilities = DEFAULT_CAPABILITIES;
    }

    public NetworkCapabilities(NetworkCapabilities nc) {
        if (nc != null) {
            this.mNetworkCapabilities = nc.mNetworkCapabilities;
            this.mTransportTypes = nc.mTransportTypes;
            this.mLinkUpBandwidthKbps = nc.mLinkUpBandwidthKbps;
            this.mLinkDownBandwidthKbps = nc.mLinkDownBandwidthKbps;
            this.mNetworkSpecifier = nc.mNetworkSpecifier;
            this.mSignalStrength = nc.mSignalStrength;
            this.mDualCellDataEnabled = nc.mDualCellDataEnabled;
        }
    }

    public void clearAll() {
        this.mTransportTypes = 0;
        this.mNetworkCapabilities = 0;
        this.mLinkDownBandwidthKbps = TRANSPORT_CELLULAR;
        this.mLinkUpBandwidthKbps = TRANSPORT_CELLULAR;
        this.mNetworkSpecifier = null;
        this.mSignalStrength = SIGNAL_STRENGTH_UNSPECIFIED;
    }

    public NetworkCapabilities addCapability(int capability) {
        if (capability < 0 || capability > NET_CAPABILITY_BIP6) {
            throw new IllegalArgumentException("NetworkCapability out of range");
        }
        this.mNetworkCapabilities |= (long) (TRANSPORT_WIFI << capability);
        return this;
    }

    public int[] getCapabilities() {
        return enumerateBits(this.mNetworkCapabilities);
    }

    public boolean hasCapability(int capability) {
        boolean z = true;
        if (capability < 0 || capability > NET_CAPABILITY_BIP6) {
            return false;
        }
        if ((this.mNetworkCapabilities & ((long) (TRANSPORT_WIFI << capability))) == 0) {
            z = false;
        }
        return z;
    }

    private int[] enumerateBits(long val) {
        int[] result = new int[Long.bitCount(val)];
        int resource = TRANSPORT_CELLULAR;
        int index = TRANSPORT_CELLULAR;
        while (val > 0) {
            int index2;
            if ((val & 1) == 1) {
                index2 = index + TRANSPORT_WIFI;
                result[index] = resource;
            } else {
                index2 = index;
            }
            val >>= TRANSPORT_WIFI;
            resource += TRANSPORT_WIFI;
            index = index2;
        }
        return result;
    }

    private void combineNetCapabilities(NetworkCapabilities nc) {
        this.mNetworkCapabilities |= nc.mNetworkCapabilities;
    }

    public String describeFirstNonRequestableCapability() {
        if (hasCapability(NET_CAPABILITY_VALIDATED)) {
            return "NET_CAPABILITY_VALIDATED";
        }
        if (hasCapability(NET_CAPABILITY_CAPTIVE_PORTAL)) {
            return "NET_CAPABILITY_CAPTIVE_PORTAL";
        }
        if ((this.mNetworkCapabilities & NON_REQUESTABLE_CAPABILITIES) != 0) {
            return "unknown non-requestable capabilities " + Long.toHexString(this.mNetworkCapabilities);
        }
        if (this.mLinkUpBandwidthKbps != 0 || this.mLinkDownBandwidthKbps != 0) {
            return "link bandwidth";
        }
        if (hasSignalStrength()) {
            return "signalStrength";
        }
        return null;
    }

    private boolean satisfiedByNetCapabilities(NetworkCapabilities nc, boolean onlyImmutable) {
        long networkCapabilities = this.mNetworkCapabilities;
        if (onlyImmutable) {
            networkCapabilities &= -212993;
        }
        return (nc.mNetworkCapabilities & networkCapabilities) == networkCapabilities;
    }

    public boolean equalsNetCapabilities(NetworkCapabilities nc) {
        return nc.mNetworkCapabilities == this.mNetworkCapabilities;
    }

    private boolean equalsNetCapabilitiesImmutable(NetworkCapabilities that) {
        return (this.mNetworkCapabilities & -212993) == (that.mNetworkCapabilities & -212993);
    }

    public void maybeMarkCapabilitiesRestricted() {
        if ((this.mNetworkCapabilities & -59325) == 0 && (this.mNetworkCapabilities & RESTRICTED_CAPABILITIES) != 0) {
            removeCapability(NET_CAPABILITY_NOT_RESTRICTED);
        }
    }

    public NetworkCapabilities addTransportType(int transportType) {
        if (transportType < 0 || transportType > TRANSPORT_VPN) {
            throw new IllegalArgumentException("TransportType out of range");
        }
        this.mTransportTypes |= (long) (TRANSPORT_WIFI << transportType);
        setNetworkSpecifier(this.mNetworkSpecifier);
        return this;
    }

    public int[] getTransportTypes() {
        return enumerateBits(this.mTransportTypes);
    }

    public boolean hasTransport(int transportType) {
        boolean z = true;
        if (transportType < 0 || transportType > TRANSPORT_VPN) {
            return false;
        }
        if ((this.mTransportTypes & ((long) (TRANSPORT_WIFI << transportType))) == 0) {
            z = false;
        }
        return z;
    }

    private void combineTransportTypes(NetworkCapabilities nc) {
        this.mTransportTypes |= nc.mTransportTypes;
    }

    private boolean satisfiedByTransportTypes(NetworkCapabilities nc) {
        if (this.mTransportTypes == 0 || (this.mTransportTypes & nc.mTransportTypes) != 0) {
            return true;
        }
        return false;
    }

    public boolean equalsTransportTypes(NetworkCapabilities nc) {
        return nc.mTransportTypes == this.mTransportTypes;
    }

    public void setLinkUpstreamBandwidthKbps(int upKbps) {
        this.mLinkUpBandwidthKbps = upKbps;
    }

    public int getLinkUpstreamBandwidthKbps() {
        return this.mLinkUpBandwidthKbps;
    }

    public void setLinkDownstreamBandwidthKbps(int downKbps) {
        this.mLinkDownBandwidthKbps = downKbps;
    }

    public int getLinkDownstreamBandwidthKbps() {
        return this.mLinkDownBandwidthKbps;
    }

    private void combineLinkBandwidths(NetworkCapabilities nc) {
        this.mLinkUpBandwidthKbps = Math.max(this.mLinkUpBandwidthKbps, nc.mLinkUpBandwidthKbps);
        this.mLinkDownBandwidthKbps = Math.max(this.mLinkDownBandwidthKbps, nc.mLinkDownBandwidthKbps);
    }

    private boolean satisfiedByLinkBandwidths(NetworkCapabilities nc) {
        if (this.mLinkUpBandwidthKbps > nc.mLinkUpBandwidthKbps || this.mLinkDownBandwidthKbps > nc.mLinkDownBandwidthKbps) {
            return false;
        }
        return true;
    }

    private boolean equalsLinkBandwidths(NetworkCapabilities nc) {
        if (this.mLinkUpBandwidthKbps == nc.mLinkUpBandwidthKbps && this.mLinkDownBandwidthKbps == nc.mLinkDownBandwidthKbps) {
            return true;
        }
        return false;
    }

    public NetworkCapabilities setNetworkSpecifier(String networkSpecifier) {
        if (TextUtils.isEmpty(networkSpecifier) || Long.bitCount(this.mTransportTypes) == TRANSPORT_WIFI) {
            this.mNetworkSpecifier = networkSpecifier;
            return this;
        }
        throw new IllegalStateException("Must have a single transport specified to use setNetworkSpecifier");
    }

    public String getNetworkSpecifier() {
        return this.mNetworkSpecifier;
    }

    private void combineSpecifiers(NetworkCapabilities nc) {
        String otherSpecifier = nc.getNetworkSpecifier();
        if (!TextUtils.isEmpty(otherSpecifier)) {
            if (TextUtils.isEmpty(this.mNetworkSpecifier)) {
                setNetworkSpecifier(otherSpecifier);
                return;
            }
            throw new IllegalStateException("Can't combine two networkSpecifiers");
        }
    }

    private boolean satisfiedBySpecifier(NetworkCapabilities nc) {
        if (TextUtils.isEmpty(this.mNetworkSpecifier) || this.mNetworkSpecifier.equals(nc.mNetworkSpecifier)) {
            return true;
        }
        return MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(nc.mNetworkSpecifier);
    }

    private boolean equalsSpecifier(NetworkCapabilities nc) {
        if (TextUtils.isEmpty(this.mNetworkSpecifier)) {
            return TextUtils.isEmpty(nc.mNetworkSpecifier);
        }
        return this.mNetworkSpecifier.equals(nc.mNetworkSpecifier);
    }

    public void setSignalStrength(int signalStrength) {
        this.mSignalStrength = signalStrength;
    }

    public boolean hasSignalStrength() {
        return this.mSignalStrength > SIGNAL_STRENGTH_UNSPECIFIED;
    }

    public int getSignalStrength() {
        return this.mSignalStrength;
    }

    private void combineSignalStrength(NetworkCapabilities nc) {
        this.mSignalStrength = Math.max(this.mSignalStrength, nc.mSignalStrength);
    }

    private boolean satisfiedBySignalStrength(NetworkCapabilities nc) {
        return this.mSignalStrength <= nc.mSignalStrength;
    }

    private boolean equalsSignalStrength(NetworkCapabilities nc) {
        return this.mSignalStrength == nc.mSignalStrength;
    }

    public void combineCapabilities(NetworkCapabilities nc) {
        combineNetCapabilities(nc);
        combineTransportTypes(nc);
        combineLinkBandwidths(nc);
        combineSpecifiers(nc);
        combineSignalStrength(nc);
    }

    public void combineCapabilitiesWithNoSpecifiers(NetworkCapabilities nc) {
        combineNetCapabilities(nc);
        combineTransportTypes(nc);
        combineLinkBandwidths(nc);
    }

    private boolean satisfiedByNetworkCapabilities(NetworkCapabilities nc, boolean onlyImmutable) {
        if (nc == null || !satisfiedByNetCapabilities(nc, onlyImmutable) || !satisfiedByTransportTypes(nc) || ((!onlyImmutable && !satisfiedByLinkBandwidths(nc)) || !satisfiedBySpecifier(nc))) {
            return false;
        }
        if (onlyImmutable) {
            return true;
        }
        return satisfiedBySignalStrength(nc);
    }

    public boolean satisfiedByNetworkCapabilities(NetworkCapabilities nc) {
        return satisfiedByNetworkCapabilities(nc, false);
    }

    public boolean satisfiedByImmutableNetworkCapabilities(NetworkCapabilities nc) {
        return satisfiedByNetworkCapabilities(nc, true);
    }

    public boolean equalImmutableCapabilities(NetworkCapabilities nc) {
        boolean z = false;
        if (nc == null) {
            return false;
        }
        if (equalsNetCapabilitiesImmutable(nc) && equalsTransportTypes(nc)) {
            z = equalsSpecifier(nc);
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof NetworkCapabilities)) {
            return false;
        }
        NetworkCapabilities that = (NetworkCapabilities) obj;
        if (equalsNetCapabilities(that) && equalsTransportTypes(that) && equalsLinkBandwidths(that) && equalsSignalStrength(that) && equalsSpecifier(that)) {
            z = equalsDualCellDataEnabled(that);
        }
        return z;
    }

    private boolean equalsDualCellDataEnabled(NetworkCapabilities nc) {
        if (TextUtils.isEmpty(this.mDualCellDataEnabled)) {
            return TextUtils.isEmpty(nc.mDualCellDataEnabled);
        }
        return this.mDualCellDataEnabled.equals(nc.mDualCellDataEnabled);
    }

    public int hashCode() {
        int i;
        int i2 = TRANSPORT_CELLULAR;
        int i3 = (this.mLinkDownBandwidthKbps * NET_CAPABILITY_NOT_RESTRICTED) + ((((((int) (this.mNetworkCapabilities & -1)) + (((int) (this.mNetworkCapabilities >> 32)) * TRANSPORT_ETHERNET)) + (((int) (this.mTransportTypes & -1)) * NET_CAPABILITY_CBS)) + (((int) (this.mTransportTypes >> 32)) * NET_CAPABILITY_IA)) + (this.mLinkUpBandwidthKbps * NET_CAPABILITY_NOT_METERED));
        if (TextUtils.isEmpty(this.mNetworkSpecifier)) {
            i = TRANSPORT_CELLULAR;
        } else {
            i = this.mNetworkSpecifier.hashCode() * NET_CAPABILITY_CAPTIVE_PORTAL;
        }
        i = (i + i3) + (this.mSignalStrength * NET_CAPABILITY_BIP1);
        if (!TextUtils.isEmpty(this.mDualCellDataEnabled)) {
            i2 = this.mDualCellDataEnabled.hashCode() * NET_CAPABILITY_BIP1;
        }
        return i + i2;
    }

    public int describeContents() {
        return TRANSPORT_CELLULAR;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mNetworkCapabilities);
        dest.writeLong(this.mTransportTypes);
        dest.writeInt(this.mLinkUpBandwidthKbps);
        dest.writeInt(this.mLinkDownBandwidthKbps);
        dest.writeString(this.mNetworkSpecifier);
        dest.writeInt(this.mSignalStrength);
        dest.writeString(this.mDualCellDataEnabled);
    }

    public void setDualCellData(String dualCellDataEnable) {
        this.mDualCellDataEnabled = dualCellDataEnable;
    }

    public boolean isDualCellData() {
        return "true".equals(this.mDualCellDataEnabled);
    }

    public String toString() {
        String upBand;
        String dnBand;
        int[] types = getTransportTypes();
        String transports = types.length > 0 ? " Transports: " + transportNamesOf(types) : ProxyInfo.LOCAL_EXCL_LIST;
        types = getCapabilities();
        String capabilities = types.length > 0 ? " Capabilities: " : ProxyInfo.LOCAL_EXCL_LIST;
        int i = TRANSPORT_CELLULAR;
        while (i < types.length) {
            switch (types[i]) {
                case TRANSPORT_CELLULAR /*0*/:
                    capabilities = capabilities + "MMS";
                    break;
                case TRANSPORT_WIFI /*1*/:
                    capabilities = capabilities + "SUPL";
                    break;
                case TRANSPORT_BLUETOOTH /*2*/:
                    capabilities = capabilities + "DUN";
                    break;
                case TRANSPORT_ETHERNET /*3*/:
                    capabilities = capabilities + "FOTA";
                    break;
                case TRANSPORT_VPN /*4*/:
                    capabilities = capabilities + "IMS";
                    break;
                case NET_CAPABILITY_CBS /*5*/:
                    capabilities = capabilities + "CBS";
                    break;
                case NET_CAPABILITY_WIFI_P2P /*6*/:
                    capabilities = capabilities + "WIFI_P2P";
                    break;
                case NET_CAPABILITY_IA /*7*/:
                    capabilities = capabilities + "IA";
                    break;
                case NET_CAPABILITY_RCS /*8*/:
                    capabilities = capabilities + "RCS";
                    break;
                case NET_CAPABILITY_XCAP /*9*/:
                    capabilities = capabilities + "XCAP";
                    break;
                case NET_CAPABILITY_EIMS /*10*/:
                    capabilities = capabilities + "EIMS";
                    break;
                case NET_CAPABILITY_NOT_METERED /*11*/:
                    capabilities = capabilities + "NOT_METERED";
                    break;
                case NET_CAPABILITY_INTERNET /*12*/:
                    capabilities = capabilities + "INTERNET";
                    break;
                case NET_CAPABILITY_NOT_RESTRICTED /*13*/:
                    capabilities = capabilities + "NOT_RESTRICTED";
                    break;
                case NET_CAPABILITY_TRUSTED /*14*/:
                    capabilities = capabilities + "TRUSTED";
                    break;
                case NET_CAPABILITY_NOT_VPN /*15*/:
                    capabilities = capabilities + "NOT_VPN";
                    break;
                case NET_CAPABILITY_VALIDATED /*16*/:
                    capabilities = capabilities + "VALIDATED";
                    break;
                case NET_CAPABILITY_CAPTIVE_PORTAL /*17*/:
                    capabilities = capabilities + "CAPTIVE_PORTAL";
                    break;
                case NET_CAPABILITY_BIP0 /*18*/:
                    capabilities = capabilities + "BIP0";
                    break;
                case NET_CAPABILITY_BIP1 /*19*/:
                    capabilities = capabilities + "BIP1";
                    break;
                case NET_CAPABILITY_BIP2 /*20*/:
                    capabilities = capabilities + "BIP2";
                    break;
                case NET_CAPABILITY_BIP3 /*21*/:
                    capabilities = capabilities + "BIP3";
                    break;
                case NET_CAPABILITY_BIP4 /*22*/:
                    capabilities = capabilities + "BIP4";
                    break;
                case NET_CAPABILITY_BIP5 /*23*/:
                    capabilities = capabilities + "BIP5";
                    break;
                case NET_CAPABILITY_BIP6 /*24*/:
                    capabilities = capabilities + "BIP6";
                    break;
            }
            i += TRANSPORT_WIFI;
            if (i < types.length) {
                capabilities = capabilities + "&";
            }
        }
        if (this.mLinkUpBandwidthKbps > 0) {
            upBand = " LinkUpBandwidth>=" + this.mLinkUpBandwidthKbps + "Kbps";
        } else {
            upBand = ProxyInfo.LOCAL_EXCL_LIST;
        }
        if (this.mLinkDownBandwidthKbps > 0) {
            dnBand = " LinkDnBandwidth>=" + this.mLinkDownBandwidthKbps + "Kbps";
        } else {
            dnBand = ProxyInfo.LOCAL_EXCL_LIST;
        }
        return "[" + transports + capabilities + upBand + dnBand + (this.mNetworkSpecifier == null ? ProxyInfo.LOCAL_EXCL_LIST : " Specifier: <" + this.mNetworkSpecifier + ">") + (hasSignalStrength() ? " SignalStrength: " + this.mSignalStrength : ProxyInfo.LOCAL_EXCL_LIST) + "]";
    }

    public static String transportNamesOf(int[] types) {
        String transports = ProxyInfo.LOCAL_EXCL_LIST;
        int i = TRANSPORT_CELLULAR;
        while (i < types.length) {
            switch (types[i]) {
                case TRANSPORT_CELLULAR /*0*/:
                    transports = transports + "CELLULAR";
                    break;
                case TRANSPORT_WIFI /*1*/:
                    transports = transports + "WIFI";
                    break;
                case TRANSPORT_BLUETOOTH /*2*/:
                    transports = transports + "BLUETOOTH";
                    break;
                case TRANSPORT_ETHERNET /*3*/:
                    transports = transports + "ETHERNET";
                    break;
                case TRANSPORT_VPN /*4*/:
                    transports = transports + "VPN";
                    break;
            }
            i += TRANSPORT_WIFI;
            if (i < types.length) {
                transports = transports + "|";
            }
        }
        return transports;
    }
}
