package gov.nist.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class HostPort extends GenericObject {
    private static final long serialVersionUID = -7103412227431884523L;
    protected Host host = null;
    protected int port = -1;

    @Override // gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        this.host.encode(buffer);
        if (this.port != -1) {
            buffer.append(Separators.COLON);
            buffer.append(this.port);
        }
        return buffer;
    }

    @Override // gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        HostPort that = (HostPort) other;
        if (this.port != that.port || !this.host.equals(that.host)) {
            return false;
        }
        return true;
    }

    public Host getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public boolean hasPort() {
        return this.port != -1;
    }

    public void removePort() {
        this.port = -1;
    }

    public void setHost(Host h) {
        this.host = h;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        Host host2 = this.host;
        if (host2 == null) {
            return null;
        }
        return host2.getInetAddress();
    }

    @Override // gov.nist.core.GenericObject
    public void merge(Object mergeObject) {
        super.merge(mergeObject);
        if (this.port == -1) {
            this.port = ((HostPort) mergeObject).port;
        }
    }

    @Override // java.lang.Object, gov.nist.core.GenericObject
    public Object clone() {
        HostPort retval = (HostPort) super.clone();
        Host host2 = this.host;
        if (host2 != null) {
            retval.host = (Host) host2.clone();
        }
        return retval;
    }

    public String toString() {
        return encode();
    }

    public int hashCode() {
        return this.host.hashCode() + this.port;
    }
}
