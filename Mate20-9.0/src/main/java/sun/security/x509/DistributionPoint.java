package sun.security.x509;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DistributionPoint {
    public static final int AA_COMPROMISE = 8;
    public static final int AFFILIATION_CHANGED = 3;
    public static final int CA_COMPROMISE = 2;
    public static final int CERTIFICATE_HOLD = 6;
    public static final int CESSATION_OF_OPERATION = 5;
    public static final int KEY_COMPROMISE = 1;
    public static final int PRIVILEGE_WITHDRAWN = 7;
    private static final String[] REASON_STRINGS = {null, "key compromise", "CA compromise", "affiliation changed", ReasonFlags.SUPERSEDED, "cessation of operation", "certificate hold", "privilege withdrawn", "AA compromise"};
    public static final int SUPERSEDED = 4;
    private static final byte TAG_DIST_PT = 0;
    private static final byte TAG_FULL_NAME = 0;
    private static final byte TAG_ISSUER = 2;
    private static final byte TAG_REASONS = 1;
    private static final byte TAG_REL_NAME = 1;
    private GeneralNames crlIssuer;
    private GeneralNames fullName;
    private volatile int hashCode;
    private boolean[] reasonFlags;
    private RDN relativeName;

    public DistributionPoint(GeneralNames fullName2, boolean[] reasonFlags2, GeneralNames crlIssuer2) {
        if (fullName2 == null && crlIssuer2 == null) {
            throw new IllegalArgumentException("fullName and crlIssuer may not both be null");
        }
        this.fullName = fullName2;
        this.reasonFlags = reasonFlags2;
        this.crlIssuer = crlIssuer2;
    }

    public DistributionPoint(RDN relativeName2, boolean[] reasonFlags2, GeneralNames crlIssuer2) {
        if (relativeName2 == null && crlIssuer2 == null) {
            throw new IllegalArgumentException("relativeName and crlIssuer may not both be null");
        }
        this.relativeName = relativeName2;
        this.reasonFlags = reasonFlags2;
        this.crlIssuer = crlIssuer2;
    }

    public DistributionPoint(DerValue val) throws IOException {
        if (val.tag == 48) {
            while (val.data != null && val.data.available() != 0) {
                DerValue opt = val.data.getDerValue();
                if (!opt.isContextSpecific((byte) 0) || !opt.isConstructed()) {
                    if (!opt.isContextSpecific((byte) 1) || opt.isConstructed()) {
                        if (!opt.isContextSpecific((byte) 2) || !opt.isConstructed()) {
                            throw new IOException("Invalid encoding of DistributionPoint.");
                        } else if (this.crlIssuer == null) {
                            opt.resetTag((byte) 48);
                            this.crlIssuer = new GeneralNames(opt);
                        } else {
                            throw new IOException("Duplicate CRLIssuer in DistributionPoint.");
                        }
                    } else if (this.reasonFlags == null) {
                        opt.resetTag((byte) 3);
                        this.reasonFlags = opt.getUnalignedBitString().toBooleanArray();
                    } else {
                        throw new IOException("Duplicate Reasons in DistributionPoint.");
                    }
                } else if (this.fullName == null && this.relativeName == null) {
                    DerValue distPnt = opt.data.getDerValue();
                    if (distPnt.isContextSpecific((byte) 0) && distPnt.isConstructed()) {
                        distPnt.resetTag((byte) 48);
                        this.fullName = new GeneralNames(distPnt);
                    } else if (!distPnt.isContextSpecific((byte) 1) || !distPnt.isConstructed()) {
                        throw new IOException("Invalid DistributionPointName in DistributionPoint");
                    } else {
                        distPnt.resetTag((byte) 49);
                        this.relativeName = new RDN(distPnt);
                    }
                } else {
                    throw new IOException("Duplicate DistributionPointName in DistributionPoint.");
                }
            }
            if (this.crlIssuer == null && this.fullName == null && this.relativeName == null) {
                throw new IOException("One of fullName, relativeName,  and crlIssuer has to be set");
            }
            return;
        }
        throw new IOException("Invalid encoding of DistributionPoint.");
    }

    public GeneralNames getFullName() {
        return this.fullName;
    }

    public RDN getRelativeName() {
        return this.relativeName;
    }

    public boolean[] getReasonFlags() {
        return this.reasonFlags;
    }

    public GeneralNames getCRLIssuer() {
        return this.crlIssuer;
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tagged = new DerOutputStream();
        if (!(this.fullName == null && this.relativeName == null)) {
            DerOutputStream distributionPoint = new DerOutputStream();
            if (this.fullName != null) {
                DerOutputStream derOut = new DerOutputStream();
                this.fullName.encode(derOut);
                distributionPoint.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), derOut);
            } else if (this.relativeName != null) {
                DerOutputStream derOut2 = new DerOutputStream();
                this.relativeName.encode(derOut2);
                distributionPoint.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 1), derOut2);
            }
            tagged.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), distributionPoint);
        }
        if (this.reasonFlags != null) {
            DerOutputStream reasons = new DerOutputStream();
            reasons.putTruncatedUnalignedBitString(new BitArray(this.reasonFlags));
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 1), reasons);
        }
        if (this.crlIssuer != null) {
            DerOutputStream issuer = new DerOutputStream();
            this.crlIssuer.encode(issuer);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 2), issuer);
        }
        out.write((byte) 48, tagged);
    }

    public boolean equals(Object obj) {
        boolean equal = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DistributionPoint)) {
            return false;
        }
        DistributionPoint other = (DistributionPoint) obj;
        if (!Objects.equals(this.fullName, other.fullName) || !Objects.equals(this.relativeName, other.relativeName) || !Objects.equals(this.crlIssuer, other.crlIssuer) || !Arrays.equals(this.reasonFlags, other.reasonFlags)) {
            equal = false;
        }
        return equal;
    }

    public int hashCode() {
        int hash = this.hashCode;
        if (hash == 0) {
            hash = 1;
            if (this.fullName != null) {
                hash = 1 + this.fullName.hashCode();
            }
            if (this.relativeName != null) {
                hash += this.relativeName.hashCode();
            }
            if (this.crlIssuer != null) {
                hash += this.crlIssuer.hashCode();
            }
            if (this.reasonFlags != null) {
                for (int i = 0; i < this.reasonFlags.length; i++) {
                    if (this.reasonFlags[i]) {
                        hash += i;
                    }
                }
            }
            this.hashCode = hash;
        }
        return hash;
    }

    private static String reasonToString(int reason) {
        if (reason > 0 && reason < REASON_STRINGS.length) {
            return REASON_STRINGS[reason];
        }
        return "Unknown reason " + reason;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.fullName != null) {
            sb.append("DistributionPoint:\n     " + this.fullName + "\n");
        }
        if (this.relativeName != null) {
            sb.append("DistributionPoint:\n     " + this.relativeName + "\n");
        }
        if (this.reasonFlags != null) {
            sb.append("   ReasonFlags:\n");
            for (boolean z : this.reasonFlags) {
                if (z) {
                    sb.append("    " + reasonToString(i) + "\n");
                }
            }
        }
        if (this.crlIssuer != null) {
            sb.append("   CRLIssuer:" + this.crlIssuer + "\n");
        }
        return sb.toString();
    }
}
