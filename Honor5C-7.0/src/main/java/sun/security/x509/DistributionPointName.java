package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DistributionPointName {
    private static final byte TAG_FULL_NAME = (byte) 0;
    private static final byte TAG_RELATIVE_NAME = (byte) 1;
    private GeneralNames fullName;
    private volatile int hashCode;
    private RDN relativeName;

    public DistributionPointName(GeneralNames fullName) {
        this.fullName = null;
        this.relativeName = null;
        if (fullName == null) {
            throw new IllegalArgumentException("fullName must not be null");
        }
        this.fullName = fullName;
    }

    public DistributionPointName(RDN relativeName) {
        this.fullName = null;
        this.relativeName = null;
        if (relativeName == null) {
            throw new IllegalArgumentException("relativeName must not be null");
        }
        this.relativeName = relativeName;
    }

    public DistributionPointName(DerValue encoding) throws IOException {
        this.fullName = null;
        this.relativeName = null;
        if (encoding.isContextSpecific(TAG_FULL_NAME) && encoding.isConstructed()) {
            encoding.resetTag(DerValue.tag_SequenceOf);
            this.fullName = new GeneralNames(encoding);
        } else if (encoding.isContextSpecific(TAG_RELATIVE_NAME) && encoding.isConstructed()) {
            encoding.resetTag(DerValue.tag_SetOf);
            this.relativeName = new RDN(encoding);
        } else {
            throw new IOException("Invalid encoding for DistributionPointName");
        }
    }

    public GeneralNames getFullName() {
        return this.fullName;
    }

    public RDN getRelativeName() {
        return this.relativeName;
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream theChoice = new DerOutputStream();
        if (this.fullName != null) {
            this.fullName.encode(theChoice);
            out.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_FULL_NAME), theChoice);
            return;
        }
        this.relativeName.encode(theChoice);
        out.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_RELATIVE_NAME), theChoice);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DistributionPointName)) {
            return false;
        }
        DistributionPointName other = (DistributionPointName) obj;
        if (equals(this.fullName, other.fullName)) {
            z = equals(this.relativeName, other.relativeName);
        }
        return z;
    }

    public int hashCode() {
        int hash = this.hashCode;
        if (hash == 0) {
            if (this.fullName != null) {
                hash = this.fullName.hashCode() + 1;
            } else {
                hash = this.relativeName.hashCode() + 1;
            }
            this.hashCode = hash;
        }
        return hash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.fullName != null) {
            sb.append("DistributionPointName:\n     ").append(this.fullName).append("\n");
        } else {
            sb.append("DistributionPointName:\n     ").append(this.relativeName).append("\n");
        }
        return sb.toString();
    }

    private static boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }
}
