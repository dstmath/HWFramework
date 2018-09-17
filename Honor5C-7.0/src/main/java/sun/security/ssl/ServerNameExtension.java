package sun.security.ssl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLProtocolException;

/* compiled from: HelloExtensions */
final class ServerNameExtension extends HelloExtension {
    static final int NAME_HOST_NAME = 0;
    private int listLength;
    private List<ServerName> names;

    /* compiled from: HelloExtensions */
    static class ServerName {
        final byte[] data;
        final String hostname;
        final int length;
        final int type;

        ServerName(int type, String hostname) throws IOException {
            this.type = type;
            this.hostname = hostname;
            this.data = hostname.getBytes("UTF8");
            this.length = this.data.length + 3;
        }

        ServerName(HandshakeInStream s) throws IOException {
            this.type = s.getInt8();
            this.data = s.getBytes16();
            this.length = this.data.length + 3;
            if (this.type == 0) {
                this.hostname = new String(this.data, "UTF8");
            } else {
                this.hostname = null;
            }
        }

        public String toString() {
            if (this.type == 0) {
                return "host_name: " + this.hostname;
            }
            return "unknown-" + this.type + ": " + Debug.toString(this.data);
        }
    }

    ServerNameExtension(List<String> hostnames) throws IOException {
        super(ExtensionType.EXT_SERVER_NAME);
        this.listLength = 0;
        this.names = new ArrayList(hostnames.size());
        for (String hostname : hostnames) {
            if (!(hostname == null || hostname.length() == 0)) {
                ServerName serverName = new ServerName(0, hostname);
                this.names.add(serverName);
                this.listLength += serverName.length;
            }
        }
        if (this.names.size() > 1) {
            throw new SSLProtocolException("The ServerNameList MUST NOT contain more than one name of the same name_type");
        } else if (this.listLength == 0) {
            throw new SSLProtocolException("The ServerNameList cannot be empty");
        }
    }

    ServerNameExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_SERVER_NAME);
        int remains = len;
        if (len >= 2) {
            this.listLength = s.getInt16();
            if (this.listLength == 0 || this.listLength + 2 != len) {
                throw new SSLProtocolException("Invalid " + this.type + " extension");
            }
            remains = len - 2;
            this.names = new ArrayList();
            while (remains > 0) {
                ServerName name = new ServerName(s);
                this.names.add(name);
                remains -= name.length;
            }
        } else if (len == 0) {
            this.listLength = 0;
            this.names = Collections.emptyList();
        }
        if (remains != 0) {
            throw new SSLProtocolException("Invalid server_name extension");
        }
    }

    int length() {
        return this.listLength == 0 ? 4 : this.listLength + 6;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        s.putInt16(this.listLength + 2);
        if (this.listLength != 0) {
            s.putInt16(this.listLength);
            for (ServerName name : this.names) {
                s.putInt8(name.type);
                s.putBytes16(name.data);
            }
        }
    }

    public String toString() {
        Object buffer = new StringBuffer();
        for (Object name : this.names) {
            buffer.append("[" + name + "]");
        }
        return "Extension " + this.type + ", server_name: " + buffer;
    }
}
