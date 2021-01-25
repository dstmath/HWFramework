package org.bouncycastle.asn1.bsi;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface BSIObjectIdentifiers {
    public static final ASN1ObjectIdentifier algorithm = bsi_de.branch("1");
    public static final ASN1ObjectIdentifier bsi_de = new ASN1ObjectIdentifier("0.4.0.127.0.7");
    public static final ASN1ObjectIdentifier ecdsa_plain_RIPEMD160 = ecdsa_plain_signatures.branch("6");
    public static final ASN1ObjectIdentifier ecdsa_plain_SHA1 = ecdsa_plain_signatures.branch("1");
    public static final ASN1ObjectIdentifier ecdsa_plain_SHA224 = ecdsa_plain_signatures.branch("2");
    public static final ASN1ObjectIdentifier ecdsa_plain_SHA256 = ecdsa_plain_signatures.branch("3");
    public static final ASN1ObjectIdentifier ecdsa_plain_SHA384 = ecdsa_plain_signatures.branch("4");
    public static final ASN1ObjectIdentifier ecdsa_plain_SHA512 = ecdsa_plain_signatures.branch("5");
    public static final ASN1ObjectIdentifier ecdsa_plain_signatures = id_ecc.branch("4.1");
    public static final ASN1ObjectIdentifier ecka_eg = id_ecc.branch("5.1");
    public static final ASN1ObjectIdentifier ecka_eg_SessionKDF = ecka_eg.branch("2");
    public static final ASN1ObjectIdentifier ecka_eg_SessionKDF_3DES = ecka_eg_SessionKDF.branch("1");
    public static final ASN1ObjectIdentifier ecka_eg_SessionKDF_AES128 = ecka_eg_SessionKDF.branch("2");
    public static final ASN1ObjectIdentifier ecka_eg_SessionKDF_AES192 = ecka_eg_SessionKDF.branch("3");
    public static final ASN1ObjectIdentifier ecka_eg_SessionKDF_AES256 = ecka_eg_SessionKDF.branch("4");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf = ecka_eg.branch("1");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf_RIPEMD160 = ecka_eg_X963kdf.branch("6");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf_SHA1 = ecka_eg_X963kdf.branch("1");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf_SHA224 = ecka_eg_X963kdf.branch("2");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf_SHA256 = ecka_eg_X963kdf.branch("3");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf_SHA384 = ecka_eg_X963kdf.branch("4");
    public static final ASN1ObjectIdentifier ecka_eg_X963kdf_SHA512 = ecka_eg_X963kdf.branch("5");
    public static final ASN1ObjectIdentifier id_ecc = bsi_de.branch("1.1");
}
