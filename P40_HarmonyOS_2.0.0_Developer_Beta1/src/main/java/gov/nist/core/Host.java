package gov.nist.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host extends GenericObject {
    protected static final int HOSTNAME = 1;
    protected static final int IPV4ADDRESS = 2;
    protected static final int IPV6ADDRESS = 3;
    private static final long serialVersionUID = -7233564517978323344L;
    protected int addressType;
    protected String hostname;
    private InetAddress inetAddress;
    private boolean stripAddressScopeZones;

    public Host() {
        this.stripAddressScopeZones = false;
        this.addressType = 1;
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
    }

    public Host(String hostName) throws IllegalArgumentException {
        this.stripAddressScopeZones = false;
        if (hostName != null) {
            this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
            setHost(hostName, 2);
            return;
        }
        throw new IllegalArgumentException("null host name");
    }

    public Host(String name, int addrType) {
        this.stripAddressScopeZones = false;
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
        setHost(name, addrType);
    }

    @Override // gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        if (this.addressType != 3 || isIPv6Reference(this.hostname)) {
            buffer.append(this.hostname);
        } else {
            buffer.append('[');
            buffer.append(this.hostname);
            buffer.append(']');
        }
        return buffer;
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object obj) {
        if (obj != null && getClass().equals(obj.getClass())) {
            return ((Host) obj).hostname.equals(this.hostname);
        }
        return false;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getAddress() {
        return this.hostname;
    }

    public String getIpAddress() {
        String str = this.hostname;
        if (str == null) {
            return null;
        }
        if (this.addressType != 1) {
            return this.hostname;
        }
        try {
            if (this.inetAddress == null) {
                this.inetAddress = InetAddress.getByName(str);
            }
            return this.inetAddress.getHostAddress();
        } catch (UnknownHostException ex) {
            dbgPrint("Could not resolve hostname " + ex);
            return null;
        }
    }

    public void setHostname(String h) {
        setHost(h, 1);
    }

    public void setHostAddress(String address) {
        setHost(address, 2);
    }

    private void setHost(String host, int type) {
        int zoneStart;
        this.inetAddress = null;
        if (isIPv6Address(host)) {
            this.addressType = 3;
        } else {
            this.addressType = type;
        }
        if (host != null) {
            this.hostname = host.trim();
            if (this.addressType == 1) {
                this.hostname = this.hostname.toLowerCase();
            }
            if (this.addressType == 3 && this.stripAddressScopeZones && (zoneStart = this.hostname.indexOf(37)) != -1) {
                this.hostname = this.hostname.substring(0, zoneStart);
            }
        }
    }

    public void setAddress(String address) {
        setHostAddress(address);
    }

    public boolean isHostname() {
        return this.addressType == 1;
    }

    public boolean isIPAddress() {
        return this.addressType != 1;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        String str = this.hostname;
        if (str == null) {
            return null;
        }
        InetAddress inetAddress2 = this.inetAddress;
        if (inetAddress2 != null) {
            return inetAddress2;
        }
        this.inetAddress = InetAddress.getByName(str);
        return this.inetAddress;
    }

    private boolean isIPv6Address(String address) {
        return (address == null || address.indexOf(58) == -1) ? false : true;
    }

    public static boolean isIPv6Reference(String address) {
        return address.charAt(0) == '[' && address.charAt(address.length() - 1) == ']';
    }

    @Override // java.lang.Object
    public int hashCode() {
        return getHostname().hashCode();
    }
}
