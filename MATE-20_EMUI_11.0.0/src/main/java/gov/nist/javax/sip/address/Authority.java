package gov.nist.javax.sip.address;

import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.Separators;

public class Authority extends NetObject {
    private static final long serialVersionUID = -3570349777347017894L;
    protected HostPort hostPort;
    protected UserInfo userInfo;

    @Override // gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        UserInfo userInfo2 = this.userInfo;
        if (userInfo2 != null) {
            userInfo2.encode(buffer);
            buffer.append(Separators.AT);
            this.hostPort.encode(buffer);
        } else {
            this.hostPort.encode(buffer);
        }
        return buffer;
    }

    @Override // gov.nist.javax.sip.address.NetObject, gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        UserInfo userInfo2;
        if (other == null || other.getClass() != getClass()) {
            return false;
        }
        Authority otherAuth = (Authority) other;
        if (!this.hostPort.equals(otherAuth.hostPort)) {
            return false;
        }
        UserInfo userInfo3 = this.userInfo;
        if (userInfo3 == null || (userInfo2 = otherAuth.userInfo) == null || userInfo3.equals(userInfo2)) {
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
        UserInfo userInfo2 = this.userInfo;
        if (userInfo2 == null) {
            return null;
        }
        return userInfo2.password;
    }

    public String getUser() {
        UserInfo userInfo2 = this.userInfo;
        if (userInfo2 != null) {
            return userInfo2.user;
        }
        return null;
    }

    public Host getHost() {
        HostPort hostPort2 = this.hostPort;
        if (hostPort2 == null) {
            return null;
        }
        return hostPort2.getHost();
    }

    public int getPort() {
        HostPort hostPort2 = this.hostPort;
        if (hostPort2 == null) {
            return -1;
        }
        return hostPort2.getPort();
    }

    public void removePort() {
        HostPort hostPort2 = this.hostPort;
        if (hostPort2 != null) {
            hostPort2.removePort();
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

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        Authority retval = (Authority) super.clone();
        HostPort hostPort2 = this.hostPort;
        if (hostPort2 != null) {
            retval.hostPort = (HostPort) hostPort2.clone();
        }
        UserInfo userInfo2 = this.userInfo;
        if (userInfo2 != null) {
            retval.userInfo = (UserInfo) userInfo2.clone();
        }
        return retval;
    }

    @Override // java.lang.Object
    public int hashCode() {
        HostPort hostPort2 = this.hostPort;
        if (hostPort2 != null) {
            return hostPort2.encode().hashCode();
        }
        throw new UnsupportedOperationException("Null hostPort cannot compute hashcode");
    }
}
