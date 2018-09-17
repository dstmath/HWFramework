package sun.security.x509;

import java.io.IOException;
import java.util.Objects;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DistributionPointName {
    private static final byte TAG_FULL_NAME = (byte) 0;
    private static final byte TAG_RELATIVE_NAME = (byte) 1;
    private GeneralNames fullName = null;
    private volatile int hashCode;
    private RDN relativeName = null;

    public DistributionPointName(GeneralNames fullName) {
        if (fullName == null) {
            throw new IllegalArgumentException("fullName must not be null");
        }
        this.fullName = fullName;
    }

    public DistributionPointName(RDN relativeName) {
        if (relativeName == null) {
            throw new IllegalArgumentException("relativeName must not be null");
        }
        this.relativeName = relativeName;
    }

    public DistributionPointName(DerValue encoding) throws IOException {
        if (encoding.isContextSpecific((byte) 0) && encoding.isConstructed()) {
            encoding.resetTag((byte) 48);
            this.fullName = new GeneralNames(encoding);
        } else if (encoding.isContextSpecific((byte) 1) && encoding.isConstructed()) {
            encoding.resetTag((byte) 49);
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
            out.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), theChoice);
            return;
        }
        this.relativeName.encode(theChoice);
        out.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 1), theChoice);
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
        if (Objects.equals(this.fullName, other.fullName)) {
            z = Objects.equals(this.relativeName, other.relativeName);
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
        return sb.-java_util_stream_Collectors-mthref-7();
    }
}
