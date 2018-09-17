package javax.security.auth.x500;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import sun.security.util.DerValue;
import sun.security.util.ResourcesMgr;
import sun.security.x509.X500Name;

public final class X500Principal implements Principal, Serializable {
    public static final String CANONICAL = "CANONICAL";
    public static final String RFC1779 = "RFC1779";
    public static final String RFC2253 = "RFC2253";
    private static final long serialVersionUID = -500463348111345721L;
    private transient X500Name thisX500Name;

    X500Principal(X500Name x500Name) {
        this.thisX500Name = x500Name;
    }

    public X500Principal(String name) {
        this(name, Collections.emptyMap());
    }

    public X500Principal(String name, Map<String, String> keywordMap) {
        if (name == null) {
            throw new NullPointerException(ResourcesMgr.getString("provided.null.name"));
        } else if (keywordMap == null) {
            throw new NullPointerException(ResourcesMgr.getString("provided.null.keyword.map"));
        } else {
            try {
                this.thisX500Name = new X500Name(name, (Map) keywordMap);
            } catch (Exception e) {
                IllegalArgumentException iae = new IllegalArgumentException("improperly specified input name: " + name);
                iae.initCause(e);
                throw iae;
            }
        }
    }

    public X500Principal(byte[] name) {
        try {
            this.thisX500Name = new X500Name(name);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException("improperly specified input name");
            iae.initCause(e);
            throw iae;
        }
    }

    public X500Principal(InputStream is) {
        IllegalArgumentException iae;
        if (is == null) {
            throw new NullPointerException("provided null input stream");
        }
        try {
            if (is.markSupported()) {
                is.mark(is.available() + 1);
            }
            this.thisX500Name = new X500Name(new DerValue(is).data);
        } catch (Exception e) {
            if (is.markSupported()) {
                try {
                    is.reset();
                } catch (IOException e2) {
                    iae = new IllegalArgumentException("improperly specified input stream and unable to reset input stream");
                    iae.initCause(e);
                    throw iae;
                }
            }
            iae = new IllegalArgumentException("improperly specified input stream");
            iae.initCause(e);
            throw iae;
        }
    }

    public String getName() {
        return getName(RFC2253);
    }

    public String getName(String format) {
        if (format != null) {
            if (format.equalsIgnoreCase(RFC1779)) {
                return this.thisX500Name.getRFC1779Name();
            }
            if (format.equalsIgnoreCase(RFC2253)) {
                return this.thisX500Name.getRFC2253Name();
            }
            if (format.equalsIgnoreCase(CANONICAL)) {
                return this.thisX500Name.getRFC2253CanonicalName();
            }
        }
        throw new IllegalArgumentException("invalid format specified");
    }

    public String getName(String format, Map<String, String> oidMap) {
        if (oidMap == null) {
            throw new NullPointerException(ResourcesMgr.getString("provided.null.OID.map"));
        }
        if (format != null) {
            if (format.equalsIgnoreCase(RFC1779)) {
                return this.thisX500Name.getRFC1779Name(oidMap);
            }
            if (format.equalsIgnoreCase(RFC2253)) {
                return this.thisX500Name.getRFC2253Name(oidMap);
            }
        }
        throw new IllegalArgumentException("invalid format specified");
    }

    public byte[] getEncoded() {
        try {
            return this.thisX500Name.getEncoded();
        } catch (IOException e) {
            throw new RuntimeException("unable to get encoding", e);
        }
    }

    public String toString() {
        return this.thisX500Name.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof X500Principal)) {
            return false;
        }
        return this.thisX500Name.equals(((X500Principal) o).thisX500Name);
    }

    public int hashCode() {
        return this.thisX500Name.hashCode();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.writeObject(this.thisX500Name.getEncodedInternal());
    }

    private void readObject(ObjectInputStream s) throws IOException, NotActiveException, ClassNotFoundException {
        this.thisX500Name = new X500Name((byte[]) s.readObject());
    }
}
