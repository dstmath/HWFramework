package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

        protected CertPathRep(String type2, byte[] data2) {
            this.type = type2;
            this.data = data2;
        }

        /* access modifiers changed from: protected */
        public Object readResolve() throws ObjectStreamException {
            try {
                return CertificateFactory.getInstance(this.type).generateCertPath((InputStream) new ByteArrayInputStream(this.data));
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

    protected CertPath(String type2) {
        this.type = type2;
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
        if (!otherCP.getType().equals(this.type)) {
            return false;
        }
        return getCertificates().equals(otherCP.getCertificates());
    }

    public int hashCode() {
        return (31 * this.type.hashCode()) + getCertificates().hashCode();
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

    /* access modifiers changed from: protected */
    public Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(this.type, getEncoded());
        } catch (CertificateException ce) {
            NotSerializableException nse = new NotSerializableException("java.security.cert.CertPath: " + this.type);
            nse.initCause(ce);
            throw nse;
        }
    }
}
