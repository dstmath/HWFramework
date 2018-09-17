package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keymaster.KeymasterDefs;
import android.system.OsConstants;
import android.util.Pair;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;

public class LinkAddress implements Parcelable {
    public static final Creator<LinkAddress> CREATOR = null;
    private InetAddress address;
    private int flags;
    private int prefixLength;
    private int scope;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.LinkAddress.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.LinkAddress.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.net.LinkAddress.<clinit>():void");
    }

    static int scopeForUnicastAddress(InetAddress addr) {
        if (addr.isAnyLocalAddress()) {
            return OsConstants.RT_SCOPE_HOST;
        }
        if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
            return OsConstants.RT_SCOPE_LINK;
        }
        if ((addr instanceof Inet4Address) || !addr.isSiteLocalAddress()) {
            return OsConstants.RT_SCOPE_UNIVERSE;
        }
        return OsConstants.RT_SCOPE_SITE;
    }

    private boolean isIPv6ULA() {
        boolean z = false;
        if (this.address == null || !(this.address instanceof Inet6Address)) {
            return false;
        }
        if ((this.address.getAddress()[0] & -4) == -4) {
            z = true;
        }
        return z;
    }

    private void init(InetAddress address, int prefixLength, int flags, int scope) {
        if (address == null || address.isMulticastAddress() || prefixLength < 0 || (((address instanceof Inet4Address) && prefixLength > 32) || prefixLength > KeymasterDefs.KM_ALGORITHM_HMAC)) {
            throw new IllegalArgumentException("Bad LinkAddress params " + address + "/" + prefixLength);
        }
        this.address = address;
        this.prefixLength = prefixLength;
        this.flags = flags;
        this.scope = scope;
    }

    public LinkAddress(InetAddress address, int prefixLength, int flags, int scope) {
        init(address, prefixLength, flags, scope);
    }

    public LinkAddress(InetAddress address, int prefixLength) {
        this(address, prefixLength, 0, 0);
        this.scope = scopeForUnicastAddress(address);
    }

    public LinkAddress(InterfaceAddress interfaceAddress) {
        this(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength());
    }

    public LinkAddress(String address) {
        this(address, 0, 0);
        this.scope = scopeForUnicastAddress(this.address);
    }

    public LinkAddress(String address, int flags, int scope) {
        Pair<InetAddress, Integer> ipAndMask = NetworkUtils.parseIpAndMask(address);
        init((InetAddress) ipAndMask.first, ((Integer) ipAndMask.second).intValue(), flags, scope);
    }

    public String toString() {
        return this.address.getHostAddress() + "/" + this.prefixLength;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof LinkAddress)) {
            return false;
        }
        LinkAddress linkAddress = (LinkAddress) obj;
        if (this.address.equals(linkAddress.address) && this.prefixLength == linkAddress.prefixLength && this.flags == linkAddress.flags && this.scope == linkAddress.scope) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return ((this.address.hashCode() + (this.prefixLength * 11)) + (this.flags * 19)) + (this.scope * 43);
    }

    public boolean isSameAddressAs(LinkAddress other) {
        return this.address.equals(other.address) && this.prefixLength == other.prefixLength;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public int getNetworkPrefixLength() {
        return getPrefixLength();
    }

    public int getFlags() {
        return this.flags;
    }

    public int getScope() {
        return this.scope;
    }

    public boolean isGlobalPreferred() {
        if (this.scope != OsConstants.RT_SCOPE_UNIVERSE || isIPv6ULA() || ((long) (this.flags & (OsConstants.IFA_F_DADFAILED | OsConstants.IFA_F_DEPRECATED))) != 0) {
            return false;
        }
        if (((long) (this.flags & OsConstants.IFA_F_TENTATIVE)) == 0 || ((long) (this.flags & OsConstants.IFA_F_OPTIMISTIC)) != 0) {
            return true;
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.address.getAddress());
        dest.writeInt(this.prefixLength);
        dest.writeInt(this.flags);
        dest.writeInt(this.scope);
    }
}
