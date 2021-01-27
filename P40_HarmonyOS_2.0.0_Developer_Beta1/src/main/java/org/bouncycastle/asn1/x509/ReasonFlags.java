package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.DERBitString;

public class ReasonFlags extends DERBitString {
    public static final int AA_COMPROMISE = 32768;
    public static final int AFFILIATION_CHANGED = 16;
    public static final int CA_COMPROMISE = 32;
    public static final int CERTIFICATE_HOLD = 2;
    public static final int CESSATION_OF_OPERATION = 4;
    public static final int KEY_COMPROMISE = 64;
    public static final int PRIVILEGE_WITHDRAWN = 1;
    public static final int SUPERSEDED = 8;
    public static final int UNUSED = 128;
    public static final int aACompromise = 32768;
    public static final int affiliationChanged = 16;
    public static final int cACompromise = 32;
    public static final int certificateHold = 2;
    public static final int cessationOfOperation = 4;
    public static final int keyCompromise = 64;
    public static final int privilegeWithdrawn = 1;
    public static final int superseded = 8;
    public static final int unused = 128;

    public ReasonFlags(int i) {
        super(getBytes(i), getPadBits(i));
    }

    public ReasonFlags(DERBitString dERBitString) {
        super(dERBitString.getBytes(), dERBitString.getPadBits());
    }
}
