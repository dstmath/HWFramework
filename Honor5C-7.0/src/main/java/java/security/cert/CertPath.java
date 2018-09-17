package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public abstract class CertPath implements Serializable {
    private static final long serialVersionUID = 6068470306649138683L;
    private String type;

    protected static class CertPathRep implements Serializable {
        private static final long serialVersionUID = 3015633072427920915L;
        private byte[] data;
        private String type;

        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        protected Object readResolve() throws ObjectStreamException {
            try {
                return CertificateFactory.getInstance(this.type).generateCertPath(new ByteArrayInputStream(this.data));
            } catch (CertificateException ce) {
                NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + this.type);
                nse.initCause(ce);
                throw nse;
            }
        }
    }

    public abstract List<? extends Certificate> getCertificates();

    public abstract byte[] getEncoded() throws CertificateEncodingException;

    public abstract byte[] getEncoded(String str) throws CertificateEncodingException;

    public abstract Iterator<String> getEncodings();

    protected CertPath(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CertPath)) {
            return false;
        }
        CertPath otherCP = (CertPath) other;
        if (otherCP.getType().equals(this.type)) {
            return getCertificates().equals(otherCP.getCertificates());
        }
        return false;
    }

    public int hashCode() {
        return (this.type.hashCode() * 31) + getCertificates().hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n" + this.type + " Cert Path: length = " + getCertificates().size() + ".\n");
        sb.append("[\n");
        int i = 1;
        for (Certificate stringCert : getCertificates()) {
            sb.append("=========================================================Certificate " + i + " start.\n");
            sb.append(stringCert.toString());
            sb.append("\n=========================================================Certificate " + i + " end.\n\n\n");
            i++;
        }
        sb.append("\n]");
        return sb.toString();
    }

    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(this.type, getEncoded());
        } catch (CertificateException ce) {
            NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + this.type);
            nse.initCause(ce);
            throw nse;
        }
    }
}
