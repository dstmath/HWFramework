package gov.nist.javax.sip.address;

import gov.nist.core.Separators;

public final class UserInfo extends NetObject {
    public static final int TELEPHONE_SUBSCRIBER = 1;
    public static final int USER = 2;
    private static final long serialVersionUID = 7268593273924256144L;
    protected String password;
    protected String user;
    protected int userType;

    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserInfo other = (UserInfo) obj;
        if (this.userType != other.userType || !this.user.equalsIgnoreCase(other.user)) {
            return false;
        }
        if (this.password != null && other.password == null) {
            return false;
        }
        if (other.password != null && this.password == null) {
            return false;
        }
        if (this.password == other.password) {
            return true;
        }
        return this.password.equals(other.password);
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.password != null) {
            buffer.append(this.user).append(Separators.COLON).append(this.password);
        } else {
            buffer.append(this.user);
        }
        return buffer;
    }

    public void clearPassword() {
        this.password = null;
    }

    public int getUserType() {
        return this.userType;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUser(String user) {
        this.user = user;
        if (user == null || (user.indexOf(Separators.POUND) < 0 && user.indexOf(Separators.SEMICOLON) < 0)) {
            setUserType(2);
        } else {
            setUserType(1);
        }
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public void setUserType(int type) throws IllegalArgumentException {
        if (type == 1 || type == 2) {
            this.userType = type;
            return;
        }
        throw new IllegalArgumentException("Parameter not in range");
    }
}
