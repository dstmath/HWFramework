package sun.security.ssl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

final class ProtocolList {
    final ProtocolVersion helloVersion;
    final ProtocolVersion max;
    final ProtocolVersion min;
    private String[] protocolNames;
    private final ArrayList<ProtocolVersion> protocols;

    ProtocolList(String[] names) {
        this(convert(names));
    }

    ProtocolList(ArrayList<ProtocolVersion> versions) {
        this.protocols = versions;
        if (this.protocols.size() == 1 && this.protocols.contains(ProtocolVersion.SSL20Hello)) {
            throw new IllegalArgumentException("SSLv2Hello cannot be enabled unless at least one other supported version is also enabled.");
        } else if (this.protocols.size() != 0) {
            Collections.sort(this.protocols);
            this.min = (ProtocolVersion) this.protocols.get(0);
            this.max = (ProtocolVersion) this.protocols.get(this.protocols.size() - 1);
            this.helloVersion = (ProtocolVersion) this.protocols.get(0);
        } else {
            this.min = ProtocolVersion.NONE;
            this.max = ProtocolVersion.NONE;
            this.helloVersion = ProtocolVersion.NONE;
        }
    }

    private static ArrayList<ProtocolVersion> convert(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("Protocols may not be null");
        }
        ArrayList<ProtocolVersion> versions = new ArrayList(3);
        for (String valueOf : names) {
            ProtocolVersion version = ProtocolVersion.valueOf(valueOf);
            if (!versions.contains(version)) {
                versions.add(version);
            }
        }
        return versions;
    }

    boolean contains(ProtocolVersion protocolVersion) {
        if (protocolVersion == ProtocolVersion.SSL20Hello) {
            return false;
        }
        return this.protocols.contains(protocolVersion);
    }

    Collection<ProtocolVersion> collection() {
        return this.protocols;
    }

    ProtocolVersion selectProtocolVersion(ProtocolVersion protocolVersion) {
        ProtocolVersion selectedVersion = null;
        for (ProtocolVersion pv : this.protocols) {
            if (pv.v > protocolVersion.v) {
                break;
            }
            selectedVersion = pv;
        }
        return selectedVersion;
    }

    synchronized String[] toStringArray() {
        if (this.protocolNames == null) {
            this.protocolNames = new String[this.protocols.size()];
            int i = 0;
            for (ProtocolVersion version : this.protocols) {
                int i2 = i + 1;
                this.protocolNames[i] = version.name;
                i = i2;
            }
        }
        return (String[]) this.protocolNames.clone();
    }

    public String toString() {
        return this.protocols.toString();
    }
}
