package java.security.cert;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;

public class CertPathValidatorException extends GeneralSecurityException {
    private static final long serialVersionUID = -3083180014971893139L;
    private CertPath certPath;
    private int index;
    private Reason reason;

    public enum BasicReason implements Reason {
        UNSPECIFIED,
        EXPIRED,
        NOT_YET_VALID,
        REVOKED,
        UNDETERMINED_REVOCATION_STATUS,
        INVALID_SIGNATURE,
        ALGORITHM_CONSTRAINED
    }

    public interface Reason extends Serializable {
    }

    public CertPathValidatorException() {
        this(null, null);
    }

    public CertPathValidatorException(String msg) {
        this(msg, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public CertPathValidatorException(Throwable cause) {
        this(cause == null ? null : cause.toString(), cause);
    }

    public CertPathValidatorException(String msg, Throwable cause) {
        this(msg, cause, null, -1);
    }

    public CertPathValidatorException(String msg, Throwable cause, CertPath certPath2, int index2) {
        this(msg, cause, certPath2, index2, BasicReason.UNSPECIFIED);
    }

    public CertPathValidatorException(String msg, Throwable cause, CertPath certPath2, int index2, Reason reason2) {
        super(msg, cause);
        this.index = -1;
        this.reason = BasicReason.UNSPECIFIED;
        if (certPath2 == null && index2 != -1) {
            throw new IllegalArgumentException();
        } else if (index2 < -1 || (certPath2 != null && index2 >= certPath2.getCertificates().size())) {
            throw new IndexOutOfBoundsException();
        } else if (reason2 != null) {
            this.certPath = certPath2;
            this.index = index2;
            this.reason = reason2;
        } else {
            throw new NullPointerException("reason can't be null");
        }
    }

    public CertPath getCertPath() {
        return this.certPath;
    }

    public int getIndex() {
        return this.index;
    }

    public Reason getReason() {
        return this.reason;
    }

    private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
        stream.defaultReadObject();
        if (this.reason == null) {
            this.reason = BasicReason.UNSPECIFIED;
        }
        if (this.certPath == null && this.index != -1) {
            throw new InvalidObjectException("certpath is null and index != -1");
        } else if (this.index < -1 || (this.certPath != null && this.index >= this.certPath.getCertificates().size())) {
            throw new InvalidObjectException("index out of range");
        }
    }
}
