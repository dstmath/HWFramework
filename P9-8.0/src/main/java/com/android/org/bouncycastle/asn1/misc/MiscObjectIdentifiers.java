package com.android.org.bouncycastle.asn1.misc;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface MiscObjectIdentifiers {
    public static final ASN1ObjectIdentifier as_sys_sec_alg_ideaCBC = new ASN1ObjectIdentifier("1.3.6.1.4.1.188.7.1.1.2");
    public static final ASN1ObjectIdentifier blake2 = new ASN1ObjectIdentifier("1.3.6.1.4.1.1722.12.2");
    public static final ASN1ObjectIdentifier cast5CBC = entrust.branch("66.10");
    public static final ASN1ObjectIdentifier cryptlib = new ASN1ObjectIdentifier("1.3.6.1.4.1.3029");
    public static final ASN1ObjectIdentifier cryptlib_algorithm = cryptlib.branch("1");
    public static final ASN1ObjectIdentifier cryptlib_algorithm_blowfish_CBC = cryptlib_algorithm.branch("1.2");
    public static final ASN1ObjectIdentifier cryptlib_algorithm_blowfish_CFB = cryptlib_algorithm.branch("1.3");
    public static final ASN1ObjectIdentifier cryptlib_algorithm_blowfish_ECB = cryptlib_algorithm.branch("1.1");
    public static final ASN1ObjectIdentifier cryptlib_algorithm_blowfish_OFB = cryptlib_algorithm.branch("1.4");
    public static final ASN1ObjectIdentifier entrust = new ASN1ObjectIdentifier("1.2.840.113533.7");
    public static final ASN1ObjectIdentifier entrustVersionExtension = entrust.branch("65.0");
    public static final ASN1ObjectIdentifier id_blake2b160 = blake2.branch("1.5");
    public static final ASN1ObjectIdentifier id_blake2b256 = blake2.branch("1.8");
    public static final ASN1ObjectIdentifier id_blake2b384 = blake2.branch("1.12");
    public static final ASN1ObjectIdentifier id_blake2b512 = blake2.branch("1.16");
    public static final ASN1ObjectIdentifier netscape = new ASN1ObjectIdentifier("2.16.840.1.113730.1");
    public static final ASN1ObjectIdentifier netscapeBaseURL = netscape.branch("2");
    public static final ASN1ObjectIdentifier netscapeCARevocationURL = netscape.branch("4");
    public static final ASN1ObjectIdentifier netscapeCApolicyURL = netscape.branch("8");
    public static final ASN1ObjectIdentifier netscapeCertComment = netscape.branch("13");
    public static final ASN1ObjectIdentifier netscapeCertType = netscape.branch("1");
    public static final ASN1ObjectIdentifier netscapeRenewalURL = netscape.branch("7");
    public static final ASN1ObjectIdentifier netscapeRevocationURL = netscape.branch("3");
    public static final ASN1ObjectIdentifier netscapeSSLServerName = netscape.branch("12");
    public static final ASN1ObjectIdentifier novell = new ASN1ObjectIdentifier("2.16.840.1.113719");
    public static final ASN1ObjectIdentifier novellSecurityAttribs = novell.branch("1.9.4.1");
    public static final ASN1ObjectIdentifier verisign = new ASN1ObjectIdentifier("2.16.840.1.113733.1");
    public static final ASN1ObjectIdentifier verisignBitString_6_13 = verisign.branch("6.13");
    public static final ASN1ObjectIdentifier verisignCzagExtension = verisign.branch("6.3");
    public static final ASN1ObjectIdentifier verisignDnbDunsNumber = verisign.branch("6.15");
    public static final ASN1ObjectIdentifier verisignIssStrongCrypto = verisign.branch("8.1");
    public static final ASN1ObjectIdentifier verisignOnSiteJurisdictionHash = verisign.branch("6.11");
    public static final ASN1ObjectIdentifier verisignPrivate_6_9 = verisign.branch("6.9");
}
