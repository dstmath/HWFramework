package sun.security.x509;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class URIName implements GeneralNameInterface {
    private String host;
    private DNSName hostDNS;
    private IPAddressName hostIP;
    private URI uri;

    public URIName(DerValue derValue) throws IOException {
        this(derValue.getIA5String());
    }

    public URIName(String name) throws IOException {
        try {
            this.uri = new URI(name);
            if (this.uri.getScheme() != null) {
                this.host = this.uri.getHost();
                if (this.host == null) {
                    return;
                }
                if (this.host.charAt(0) == '[') {
                    try {
                        this.hostIP = new IPAddressName(this.host.substring(1, this.host.length() - 1));
                    } catch (IOException e) {
                        throw new IOException("invalid URI name (host portion is not a valid IPv6 address):" + name);
                    }
                } else {
                    try {
                        this.hostDNS = new DNSName(this.host);
                    } catch (IOException e2) {
                        try {
                            this.hostIP = new IPAddressName(this.host);
                        } catch (Exception e3) {
                            throw new IOException("invalid URI name (host portion is not a valid DNS name, IPv4 address, or IPv6 address):" + name);
                        }
                    }
                }
            } else {
                throw new IOException("URI name must include scheme:" + name);
            }
        } catch (URISyntaxException use) {
            throw new IOException("invalid URI name:" + name, use);
        }
    }

    public static URIName nameConstraint(DerValue value) throws IOException {
        DNSName hostDNS2;
        String name = value.getIA5String();
        try {
            URI uri2 = new URI(name);
            if (uri2.getScheme() == null) {
                String host2 = uri2.getSchemeSpecificPart();
                try {
                    if (host2.startsWith(".")) {
                        hostDNS2 = new DNSName(host2.substring(1));
                    } else {
                        hostDNS2 = new DNSName(host2);
                    }
                    return new URIName(uri2, host2, hostDNS2);
                } catch (IOException ioe) {
                    throw new IOException("invalid URI name constraint:" + name, ioe);
                }
            } else {
                throw new IOException("invalid URI name constraint (should not include scheme):" + name);
            }
        } catch (URISyntaxException use) {
            throw new IOException("invalid URI name constraint:" + name, use);
        }
    }

    URIName(URI uri2, String host2, DNSName hostDNS2) {
        this.uri = uri2;
        this.host = host2;
        this.hostDNS = hostDNS2;
    }

    public int getType() {
        return 6;
    }

    public void encode(DerOutputStream out) throws IOException {
        out.putIA5String(this.uri.toASCIIString());
    }

    public String toString() {
        return "URIName: " + this.uri.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URIName)) {
            return false;
        }
        return this.uri.equals(((URIName) obj).getURI());
    }

    public URI getURI() {
        return this.uri;
    }

    public String getName() {
        return this.uri.toString();
    }

    public String getScheme() {
        return this.uri.getScheme();
    }

    public String getHost() {
        return this.host;
    }

    public Object getHostObject() {
        if (this.hostIP != null) {
            return this.hostIP;
        }
        return this.hostDNS;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null) {
            return -1;
        }
        if (inputName.getType() != 6) {
            return -1;
        }
        String otherHost = ((URIName) inputName).getHost();
        if (otherHost.equalsIgnoreCase(this.host)) {
            return 0;
        }
        Object otherHostObject = ((URIName) inputName).getHostObject();
        if (this.hostDNS == null || !(otherHostObject instanceof DNSName)) {
            return 3;
        }
        boolean otherDomain = false;
        boolean thisDomain = this.host.charAt(0) == '.';
        if (otherHost.charAt(0) == '.') {
            otherDomain = true;
        }
        int constraintType2 = this.hostDNS.constrains((DNSName) otherHostObject);
        if (thisDomain || otherDomain || !(constraintType2 == 2 || constraintType2 == 1)) {
            constraintType = constraintType2;
        } else {
            constraintType = 3;
        }
        if (thisDomain != otherDomain && constraintType == 0) {
            if (!thisDomain) {
                return 1;
            }
            constraintType = 2;
        }
        return constraintType;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        try {
            return new DNSName(this.host).subtreeDepth();
        } catch (IOException ioe) {
            throw new UnsupportedOperationException(ioe.getMessage());
        }
    }
}
