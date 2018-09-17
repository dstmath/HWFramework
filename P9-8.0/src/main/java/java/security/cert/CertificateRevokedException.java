package java.security.cert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.security.auth.x500.X500Principal;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.Extension;
import sun.security.x509.InvalidityDateExtension;

public class CertificateRevokedException extends CertificateException {
    private static final long serialVersionUID = 7839996631571608627L;
    private final X500Principal authority;
    private transient Map<String, Extension> extensions;
    private final CRLReason reason;
    private Date revocationDate;

    public CertificateRevokedException(Date revocationDate, CRLReason reason, X500Principal authority, Map<String, Extension> extensions) {
        if (revocationDate == null || reason == null || authority == null || extensions == null) {
            throw new NullPointerException();
        }
        this.revocationDate = new Date(revocationDate.getTime());
        this.reason = reason;
        this.authority = authority;
        this.extensions = Collections.checkedMap(new HashMap(), String.class, Extension.class);
        this.extensions.putAll(extensions);
    }

    public Date getRevocationDate() {
        return (Date) this.revocationDate.clone();
    }

    public CRLReason getRevocationReason() {
        return this.reason;
    }

    public X500Principal getAuthorityName() {
        return this.authority;
    }

    public Date getInvalidityDate() {
        Extension ext = (Extension) getExtensions().get("2.5.29.24");
        if (ext == null) {
            return null;
        }
        try {
            return new Date(InvalidityDateExtension.toImpl(ext).get("DATE").getTime());
        } catch (IOException e) {
            return null;
        }
    }

    public Map<String, Extension> getExtensions() {
        return Collections.unmodifiableMap(this.extensions);
    }

    public String getMessage() {
        return "Certificate has been revoked, reason: " + this.reason + ", revocation date: " + this.revocationDate + ", authority: " + this.authority + ", extension OIDs: " + this.extensions.keySet();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(this.extensions.size());
        for (Entry<String, Extension> entry : this.extensions.entrySet()) {
            Extension ext = (Extension) entry.getValue();
            oos.writeObject(ext.getId());
            oos.writeBoolean(ext.isCritical());
            byte[] extVal = ext.getValue();
            oos.writeInt(extVal.length);
            oos.write(extVal);
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.revocationDate = new Date(this.revocationDate.getTime());
        int size = ois.readInt();
        if (size == 0) {
            this.extensions = Collections.emptyMap();
        } else {
            this.extensions = new HashMap(size);
        }
        for (int i = 0; i < size; i++) {
            String oid = (String) ois.readObject();
            boolean critical = ois.readBoolean();
            byte[] extVal = new byte[ois.readInt()];
            ois.readFully(extVal);
            this.extensions.put(oid, Extension.newExtension(new ObjectIdentifier(oid), critical, extVal));
        }
    }
}
