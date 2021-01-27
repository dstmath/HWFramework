package org.bouncycastle.asn1.oiw;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface OIWObjectIdentifiers {
    public static final ASN1ObjectIdentifier desCBC = new ASN1ObjectIdentifier("1.3.14.3.2.7");
    public static final ASN1ObjectIdentifier desCFB = new ASN1ObjectIdentifier("1.3.14.3.2.9");
    public static final ASN1ObjectIdentifier desECB = new ASN1ObjectIdentifier("1.3.14.3.2.6");
    public static final ASN1ObjectIdentifier desEDE = new ASN1ObjectIdentifier("1.3.14.3.2.17");
    public static final ASN1ObjectIdentifier desOFB = new ASN1ObjectIdentifier("1.3.14.3.2.8");
    public static final ASN1ObjectIdentifier dsaWithSHA1 = new ASN1ObjectIdentifier("1.3.14.3.2.27");
    public static final ASN1ObjectIdentifier elGamalAlgorithm = new ASN1ObjectIdentifier("1.3.14.7.2.1.1");
    public static final ASN1ObjectIdentifier idSHA1 = new ASN1ObjectIdentifier("1.3.14.3.2.26");
    public static final ASN1ObjectIdentifier md4WithRSA = new ASN1ObjectIdentifier("1.3.14.3.2.2");
    public static final ASN1ObjectIdentifier md4WithRSAEncryption = new ASN1ObjectIdentifier("1.3.14.3.2.4");
    public static final ASN1ObjectIdentifier md5WithRSA = new ASN1ObjectIdentifier("1.3.14.3.2.3");
    public static final ASN1ObjectIdentifier sha1WithRSA = new ASN1ObjectIdentifier("1.3.14.3.2.29");
}
