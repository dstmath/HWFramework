package org.bouncycastle.asn1.eac;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface EACObjectIdentifiers {
    public static final ASN1ObjectIdentifier bsi_de = new ASN1ObjectIdentifier("0.4.0.127.0.7");
    public static final ASN1ObjectIdentifier id_CA = bsi_de.branch("2.2.3");
    public static final ASN1ObjectIdentifier id_CA_DH = id_CA.branch("1");
    public static final ASN1ObjectIdentifier id_CA_DH_3DES_CBC_CBC = id_CA_DH.branch("1");
    public static final ASN1ObjectIdentifier id_CA_ECDH = id_CA.branch("2");
    public static final ASN1ObjectIdentifier id_CA_ECDH_3DES_CBC_CBC = id_CA_ECDH.branch("1");
    public static final ASN1ObjectIdentifier id_EAC_ePassport = bsi_de.branch("3.1.2.1");
    public static final ASN1ObjectIdentifier id_PK = bsi_de.branch("2.2.1");
    public static final ASN1ObjectIdentifier id_PK_DH = id_PK.branch("1");
    public static final ASN1ObjectIdentifier id_PK_ECDH = id_PK.branch("2");
    public static final ASN1ObjectIdentifier id_TA = bsi_de.branch("2.2.2");
    public static final ASN1ObjectIdentifier id_TA_ECDSA = id_TA.branch("2");
    public static final ASN1ObjectIdentifier id_TA_ECDSA_SHA_1 = id_TA_ECDSA.branch("1");
    public static final ASN1ObjectIdentifier id_TA_ECDSA_SHA_224 = id_TA_ECDSA.branch("2");
    public static final ASN1ObjectIdentifier id_TA_ECDSA_SHA_256 = id_TA_ECDSA.branch("3");
    public static final ASN1ObjectIdentifier id_TA_ECDSA_SHA_384 = id_TA_ECDSA.branch("4");
    public static final ASN1ObjectIdentifier id_TA_ECDSA_SHA_512 = id_TA_ECDSA.branch("5");
    public static final ASN1ObjectIdentifier id_TA_RSA = id_TA.branch("1");
    public static final ASN1ObjectIdentifier id_TA_RSA_PSS_SHA_1 = id_TA_RSA.branch("3");
    public static final ASN1ObjectIdentifier id_TA_RSA_PSS_SHA_256 = id_TA_RSA.branch("4");
    public static final ASN1ObjectIdentifier id_TA_RSA_PSS_SHA_512 = id_TA_RSA.branch("6");
    public static final ASN1ObjectIdentifier id_TA_RSA_v1_5_SHA_1 = id_TA_RSA.branch("1");
    public static final ASN1ObjectIdentifier id_TA_RSA_v1_5_SHA_256 = id_TA_RSA.branch("2");
    public static final ASN1ObjectIdentifier id_TA_RSA_v1_5_SHA_512 = id_TA_RSA.branch("5");
}
