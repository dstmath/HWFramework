package org.bouncycastle.asn1.iana;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface IANAObjectIdentifiers {
    public static final ASN1ObjectIdentifier SNMPv2 = internet.branch("6");
    public static final ASN1ObjectIdentifier _private = internet.branch("4");
    public static final ASN1ObjectIdentifier directory = internet.branch("1");
    public static final ASN1ObjectIdentifier experimental = internet.branch("3");
    public static final ASN1ObjectIdentifier hmacMD5 = isakmpOakley.branch("1");
    public static final ASN1ObjectIdentifier hmacRIPEMD160 = isakmpOakley.branch("4");
    public static final ASN1ObjectIdentifier hmacSHA1 = isakmpOakley.branch("2");
    public static final ASN1ObjectIdentifier hmacTIGER = isakmpOakley.branch("3");
    public static final ASN1ObjectIdentifier internet = new ASN1ObjectIdentifier("1.3.6.1");
    public static final ASN1ObjectIdentifier ipsec = security_mechanisms.branch("8");
    public static final ASN1ObjectIdentifier isakmpOakley = ipsec.branch("1");
    public static final ASN1ObjectIdentifier mail = internet.branch("7");
    public static final ASN1ObjectIdentifier mgmt = internet.branch("2");
    public static final ASN1ObjectIdentifier pkix = security_mechanisms.branch("6");
    public static final ASN1ObjectIdentifier security = internet.branch("5");
    public static final ASN1ObjectIdentifier security_mechanisms = security.branch("5");
    public static final ASN1ObjectIdentifier security_nametypes = security.branch("6");
}
