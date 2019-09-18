package java.security.cert;

public class LDAPCertStoreParameters implements CertStoreParameters {
    private static final int LDAP_DEFAULT_PORT = 389;
    private int port;
    private String serverName;

    public LDAPCertStoreParameters(String serverName2, int port2) {
        if (serverName2 != null) {
            this.serverName = serverName2;
            this.port = port2;
            return;
        }
        throw new NullPointerException();
    }

    public LDAPCertStoreParameters(String serverName2) {
        this(serverName2, LDAP_DEFAULT_PORT);
    }

    public LDAPCertStoreParameters() {
        this("localhost", LDAP_DEFAULT_PORT);
    }

    public String getServerName() {
        return this.serverName;
    }

    public int getPort() {
        return this.port;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LDAPCertStoreParameters: [\n");
        sb.append("  serverName: " + this.serverName + "\n");
        sb.append("  port: " + this.port + "\n");
        sb.append("]");
        return sb.toString();
    }
}
