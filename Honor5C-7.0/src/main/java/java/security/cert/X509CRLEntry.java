package java.security.cert;

import java.math.BigInteger;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import sun.security.x509.X509CRLEntryImpl;

public abstract class X509CRLEntry implements X509Extension {
    public abstract byte[] getEncoded() throws CRLException;

    public abstract Date getRevocationDate();

    public abstract BigInteger getSerialNumber();

    public abstract boolean hasExtensions();

    public abstract String toString();

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof X509CRLEntry)) {
            return false;
        }
        try {
            byte[] thisCRLEntry = getEncoded();
            byte[] otherCRLEntry = ((X509CRLEntry) other).getEncoded();
            if (thisCRLEntry.length != otherCRLEntry.length) {
                return false;
            }
            for (int i = 0; i < thisCRLEntry.length; i++) {
                if (thisCRLEntry[i] != otherCRLEntry[i]) {
                    return false;
                }
            }
            return true;
        } catch (CRLException e) {
            return false;
        }
    }

    public int hashCode() {
        int retval = 0;
        try {
            byte[] entryData = getEncoded();
            for (int i = 1; i < entryData.length; i++) {
                retval += entryData[i] * i;
            }
            return retval;
        } catch (CRLException e) {
            return 0;
        }
    }

    public X500Principal getCertificateIssuer() {
        return null;
    }

    public CRLReason getRevocationReason() {
        if (hasExtensions()) {
            return X509CRLEntryImpl.getRevocationReason(this);
        }
        return null;
    }
}
