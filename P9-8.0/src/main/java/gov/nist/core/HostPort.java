package gov.nist.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class HostPort extends GenericObject {
    private static final long serialVersionUID = -7103412227431884523L;
    protected Host host = null;
    protected int port = -1;

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        this.host.encode(buffer);
        if (this.port != -1) {
            buffer.append(Separators.COLON).append(this.port);
        }
        return buffer;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        HostPort that = (HostPort) other;
        if (this.port == that.port) {
            z = this.host.equals(that.host);
        }
        return z;
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
        if (this.host == null) {
            return null;
        }
        return this.host.getInetAddress();
    }

    public void merge(Object mergeObject) {
        super.merge(mergeObject);
        if (this.port == -1) {
            this.port = ((HostPort) mergeObject).port;
        }
    }

    public Object clone() {
        HostPort retval = (HostPort) super.clone();
        if (this.host != null) {
            retval.host = (Host) this.host.clone();
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
