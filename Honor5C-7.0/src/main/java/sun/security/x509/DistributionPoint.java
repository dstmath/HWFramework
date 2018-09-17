package sun.security.x509;

import java.io.IOException;
import java.util.Arrays;
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
    private static final String[] REASON_STRINGS = null;
    public static final int SUPERSEDED = 4;
    private static final byte TAG_DIST_PT = (byte) 0;
    private static final byte TAG_FULL_NAME = (byte) 0;
    private static final byte TAG_ISSUER = (byte) 2;
    private static final byte TAG_REASONS = (byte) 1;
    private static final byte TAG_REL_NAME = (byte) 1;
    private GeneralNames crlIssuer;
    private GeneralNames fullName;
    private volatile int hashCode;
    private boolean[] reasonFlags;
    private RDN relativeName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.DistributionPoint.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.DistributionPoint.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.DistributionPoint.<clinit>():void");
    }

    public DistributionPoint(GeneralNames fullName, boolean[] reasonFlags, GeneralNames crlIssuer) {
        if (fullName == null && crlIssuer == null) {
            throw new IllegalArgumentException("fullName and crlIssuer may not both be null");
        }
        this.fullName = fullName;
        this.reasonFlags = reasonFlags;
        this.crlIssuer = crlIssuer;
    }

    public DistributionPoint(RDN relativeName, boolean[] reasonFlags, GeneralNames crlIssuer) {
        if (relativeName == null && crlIssuer == null) {
            throw new IllegalArgumentException("relativeName and crlIssuer may not both be null");
        }
        this.relativeName = relativeName;
        this.reasonFlags = reasonFlags;
        this.crlIssuer = crlIssuer;
    }

    public DistributionPoint(DerValue val) throws IOException {
        if (val.tag != DerValue.tag_SequenceOf) {
            throw new IOException("Invalid encoding of DistributionPoint.");
        }
        while (val.data != null && val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            if (opt.isContextSpecific(TAG_FULL_NAME) && opt.isConstructed()) {
                if (this.fullName == null && this.relativeName == null) {
                    DerValue distPnt = opt.data.getDerValue();
                    if (distPnt.isContextSpecific(TAG_FULL_NAME) && distPnt.isConstructed()) {
                        distPnt.resetTag(DerValue.tag_SequenceOf);
                        this.fullName = new GeneralNames(distPnt);
                    } else if (distPnt.isContextSpecific(TAG_REL_NAME) && distPnt.isConstructed()) {
                        distPnt.resetTag(DerValue.tag_SetOf);
                        this.relativeName = new RDN(distPnt);
                    } else {
                        throw new IOException("Invalid DistributionPointName in DistributionPoint");
                    }
                }
                throw new IOException("Duplicate DistributionPointName in DistributionPoint.");
            } else if (!opt.isContextSpecific(TAG_REL_NAME) || opt.isConstructed()) {
                if (!opt.isContextSpecific(TAG_ISSUER) || !opt.isConstructed()) {
                    throw new IOException("Invalid encoding of DistributionPoint.");
                } else if (this.crlIssuer != null) {
                    throw new IOException("Duplicate CRLIssuer in DistributionPoint.");
                } else {
                    opt.resetTag(DerValue.tag_SequenceOf);
                    this.crlIssuer = new GeneralNames(opt);
                }
            } else if (this.reasonFlags != null) {
                throw new IOException("Duplicate Reasons in DistributionPoint.");
            } else {
                opt.resetTag((byte) 3);
                this.reasonFlags = opt.getUnalignedBitString().toBooleanArray();
            }
        }
        if (this.crlIssuer == null && this.fullName == null && this.relativeName == null) {
            throw new IOException("One of fullName, relativeName,  and crlIssuer has to be set");
        }
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
            DerOutputStream derOut;
            if (this.fullName != null) {
                derOut = new DerOutputStream();
                this.fullName.encode(derOut);
                distributionPoint.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_FULL_NAME), derOut);
            } else if (this.relativeName != null) {
                derOut = new DerOutputStream();
                this.relativeName.encode(derOut);
                distributionPoint.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_REL_NAME), derOut);
            }
            tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_FULL_NAME), distributionPoint);
        }
        if (this.reasonFlags != null) {
            DerOutputStream reasons = new DerOutputStream();
            reasons.putTruncatedUnalignedBitString(new BitArray(this.reasonFlags));
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_REL_NAME), reasons);
        }
        if (this.crlIssuer != null) {
            DerOutputStream issuer = new DerOutputStream();
            this.crlIssuer.encode(issuer);
            tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_ISSUER), issuer);
        }
        out.write((byte) DerValue.tag_SequenceOf, tagged);
    }

    private static boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    public boolean equals(Object obj) {
        boolean equal = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DistributionPoint)) {
            return false;
        }
        DistributionPoint other = (DistributionPoint) obj;
        if (equals(this.fullName, other.fullName) && equals(this.relativeName, other.relativeName) && equals(this.crlIssuer, other.crlIssuer)) {
            equal = Arrays.equals(this.reasonFlags, other.reasonFlags);
        }
        return equal;
    }

    public int hashCode() {
        int hash = this.hashCode;
        if (hash == 0) {
            hash = KEY_COMPROMISE;
            if (this.fullName != null) {
                hash = this.fullName.hashCode() + KEY_COMPROMISE;
            }
            if (this.relativeName != null) {
                hash += this.relativeName.hashCode();
            }
            if (this.crlIssuer != null) {
                hash += this.crlIssuer.hashCode();
            }
            if (this.reasonFlags != null) {
                for (int i = 0; i < this.reasonFlags.length; i += KEY_COMPROMISE) {
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
        if (reason <= 0 || reason >= REASON_STRINGS.length) {
            return "Unknown reason " + reason;
        }
        return REASON_STRINGS[reason];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.fullName != null) {
            sb.append("DistributionPoint:\n     ").append(this.fullName).append("\n");
        }
        if (this.relativeName != null) {
            sb.append("DistributionPoint:\n     ").append(this.relativeName).append("\n");
        }
        if (this.reasonFlags != null) {
            sb.append("   ReasonFlags:\n");
            for (int i = 0; i < this.reasonFlags.length; i += KEY_COMPROMISE) {
                if (this.reasonFlags[i]) {
                    sb.append("    ").append(reasonToString(i)).append("\n");
                }
            }
        }
        if (this.crlIssuer != null) {
            sb.append("   CRLIssuer:").append(this.crlIssuer).append("\n");
        }
        return sb.toString();
    }
}
