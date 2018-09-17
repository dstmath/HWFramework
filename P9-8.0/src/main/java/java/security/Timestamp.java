package java.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;

public final class Timestamp implements Serializable {
    private static final long serialVersionUID = -5502683707821851294L;
    private transient int myhash = -1;
    private CertPath signerCertPath;
    private Date timestamp;

    public Timestamp(Date timestamp, CertPath signerCertPath) {
        if (timestamp == null || signerCertPath == null) {
            throw new NullPointerException();
        }
        this.timestamp = new Date(timestamp.getTime());
        this.signerCertPath = signerCertPath;
    }

    public Date getTimestamp() {
        return new Date(this.timestamp.getTime());
    }

    public CertPath getSignerCertPath() {
        return this.signerCertPath;
    }

    public int hashCode() {
        if (this.myhash == -1) {
            this.myhash = this.timestamp.hashCode() + this.signerCertPath.hashCode();
        }
        return this.myhash;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || ((obj instanceof Timestamp) ^ 1) != 0) {
            return false;
        }
        Timestamp that = (Timestamp) obj;
        if (this == that) {
            return true;
        }
        if (this.timestamp.equals(that.getTimestamp())) {
            z = this.signerCertPath.equals(that.getSignerCertPath());
        }
        return z;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("timestamp: " + this.timestamp);
        List<? extends Certificate> certs = this.signerCertPath.getCertificates();
        if (certs.isEmpty()) {
            sb.append("TSA: <empty>");
        } else {
            sb.append("TSA: " + certs.get(0));
        }
        sb.append(")");
        return sb.toString();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.myhash = -1;
        this.timestamp = new Date(this.timestamp.getTime());
    }
}
