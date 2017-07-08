package gov.nist.javax.sip.address;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.Separators;

public class Authority extends NetObject {
    private static final long serialVersionUID = -3570349777347017894L;
    protected HostPort hostPort;
    protected UserInfo userInfo;

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.userInfo != null) {
            this.userInfo.encode(buffer);
            buffer.append(Separators.AT);
            this.hostPort.encode(buffer);
        } else {
            this.hostPort.encode(buffer);
        }
        return buffer;
    }

    public boolean equals(Object other) {
        if (other == null || other.getClass() != getClass()) {
            return false;
        }
        Authority otherAuth = (Authority) other;
        if (!this.hostPort.equals(otherAuth.hostPort)) {
            return false;
        }
        if (this.userInfo == null || otherAuth.userInfo == null || this.userInfo.equals(otherAuth.userInfo)) {
            return true;
        }
        return false;
    }

    public HostPort getHostPort() {
        return this.hostPort;
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public String getPassword() {
        if (this.userInfo == null) {
            return null;
        }
        return this.userInfo.password;
    }

    public String getUser() {
        return this.userInfo != null ? this.userInfo.user : null;
    }

    public Host getHost() {
        if (this.hostPort == null) {
            return null;
        }
        return this.hostPort.getHost();
    }

    public int getPort() {
        if (this.hostPort == null) {
            return -1;
        }
        return this.hostPort.getPort();
    }

    public void removePort() {
        if (this.hostPort != null) {
            this.hostPort.removePort();
        }
    }

    public void setPassword(String passwd) {
        if (this.userInfo == null) {
            this.userInfo = new UserInfo();
        }
        this.userInfo.setPassword(passwd);
    }

    public void setUser(String user) {
        if (this.userInfo == null) {
            this.userInfo = new UserInfo();
        }
        this.userInfo.setUser(user);
    }

    public void setHost(Host host) {
        if (this.hostPort == null) {
            this.hostPort = new HostPort();
        }
        this.hostPort.setHost(host);
    }

    public void setPort(int port) {
        if (this.hostPort == null) {
            this.hostPort = new HostPort();
        }
        this.hostPort.setPort(port);
    }

    public void setHostPort(HostPort h) {
        this.hostPort = h;
    }

    public void setUserInfo(UserInfo u) {
        this.userInfo = u;
    }

    public void removeUserInfo() {
        this.userInfo = null;
    }

    public Object clone() {
        Authority retval = (Authority) super.clone();
        if (this.hostPort != null) {
            retval.hostPort = (HostPort) this.hostPort.clone();
        }
        if (this.userInfo != null) {
            retval.userInfo = (UserInfo) this.userInfo.clone();
        }
        return retval;
    }

    public int hashCode() {
        if (this.hostPort != null) {
            return this.hostPort.encode().hashCode();
        }
        throw new UnsupportedOperationException("Null hostPort cannot compute hashcode");
    }
}
