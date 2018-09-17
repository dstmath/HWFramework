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
        if (hostName == null) {
            throw new IllegalArgumentException("null host name");
        }
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
        setHost(hostName, 2);
    }

    public Host(String name, int addrType) {
        this.stripAddressScopeZones = false;
        this.stripAddressScopeZones = Boolean.getBoolean("gov.nist.core.STRIP_ADDR_SCOPES");
        setHost(name, addrType);
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.addressType != 3 || (isIPv6Reference(this.hostname) ^ 1) == 0) {
            buffer.append(this.hostname);
        } else {
            buffer.append('[').append(this.hostname).append(']');
        }
        return buffer;
    }

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
        String rawIpAddress = null;
        if (this.hostname == null) {
            return null;
        }
        if (this.addressType == 1) {
            try {
                if (this.inetAddress == null) {
                    this.inetAddress = InetAddress.getByName(this.hostname);
                }
                rawIpAddress = this.inetAddress.getHostAddress();
            } catch (UnknownHostException ex) {
                dbgPrint("Could not resolve hostname " + ex);
            }
        } else {
            rawIpAddress = this.hostname;
        }
        return rawIpAddress;
    }

    public void setHostname(String h) {
        setHost(h, 1);
    }

    public void setHostAddress(String address) {
        setHost(address, 2);
    }

    private void setHost(String host, int type) {
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
            if (this.addressType == 3 && this.stripAddressScopeZones) {
                int zoneStart = this.hostname.indexOf(37);
                if (zoneStart != -1) {
                    this.hostname = this.hostname.substring(0, zoneStart);
                }
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
        if (this.hostname == null) {
            return null;
        }
        if (this.inetAddress != null) {
            return this.inetAddress;
        }
        this.inetAddress = InetAddress.getByName(this.hostname);
        return this.inetAddress;
    }

    private boolean isIPv6Address(String address) {
        return (address == null || address.indexOf(58) == -1) ? false : true;
    }

    public static boolean isIPv6Reference(String address) {
        if (address.charAt(0) == '[' && address.charAt(address.length() - 1) == ']') {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return getHostname().hashCode();
    }
}
