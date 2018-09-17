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
            if (this.uri.getScheme() == null) {
                throw new IOException("URI name must include scheme:" + name);
            }
            this.host = this.uri.getHost();
            if (this.host == null) {
                return;
            }
            if (this.host.charAt(0) == '[') {
                try {
                    this.hostIP = new IPAddressName(this.host.substring(1, this.host.length() - 1));
                    return;
                } catch (IOException e) {
                    throw new IOException("invalid URI name (host portion is not a valid IPv6 address):" + name);
                }
            }
            try {
                this.hostDNS = new DNSName(this.host);
            } catch (IOException e2) {
                try {
                    this.hostIP = new IPAddressName(this.host);
                } catch (Exception e3) {
                    throw new IOException("invalid URI name (host portion is not a valid DNS name, IPv4 address, or IPv6 address):" + name);
                }
            }
        } catch (URISyntaxException use) {
            throw new IOException("invalid URI name:" + name, use);
        }
    }

    public static URIName nameConstraint(DerValue value) throws IOException {
        String name = value.getIA5String();
        try {
            URI uri = new URI(name);
            if (uri.getScheme() == null) {
                String host = uri.getSchemeSpecificPart();
                try {
                    DNSName hostDNS;
                    if (host.charAt(0) == '.') {
                        hostDNS = new DNSName(host.substring(1));
                    } else {
                        hostDNS = new DNSName(host);
                    }
                    return new URIName(uri, host, hostDNS);
                } catch (IOException ioe) {
                    throw new IOException("invalid URI name constraint:" + name, ioe);
                }
            }
            throw new IOException("invalid URI name constraint (should not include scheme):" + name);
        } catch (URISyntaxException use) {
            throw new IOException("invalid URI name constraint:" + name, use);
        }
    }

    URIName(URI uri, String host, DNSName hostDNS) {
        this.uri = uri;
        this.host = host;
        this.hostDNS = hostDNS;
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
        DNSName otherHostObject = ((URIName) inputName).getHostObject();
        if (this.hostDNS == null || ((otherHostObject instanceof DNSName) ^ 1) != 0) {
            return 3;
        }
        boolean thisDomain = this.host.charAt(0) == '.';
        boolean otherDomain = otherHost.charAt(0) == '.';
        int constraintType = this.hostDNS.constrains(otherHostObject);
        if (!(thisDomain || (otherDomain ^ 1) == 0 || (constraintType != 2 && constraintType != 1))) {
            constraintType = 3;
        }
        if (thisDomain == otherDomain || constraintType != 0) {
            return constraintType;
        }
        if (thisDomain) {
            return 2;
        }
        return 1;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        try {
            return new DNSName(this.host).subtreeDepth();
        } catch (IOException ioe) {
            throw new UnsupportedOperationException(ioe.getMessage());
        }
    }
}
