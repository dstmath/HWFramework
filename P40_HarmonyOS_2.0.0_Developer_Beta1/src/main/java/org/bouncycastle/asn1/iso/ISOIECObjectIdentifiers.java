package org.bouncycastle.asn1.iso;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface ISOIECObjectIdentifiers {
    public static final ASN1ObjectIdentifier hash_algorithms = iso_encryption_algorithms.branch("3.0");
    public static final ASN1ObjectIdentifier id_ac_generic_hybrid = is18033_2.branch("1.2");
    public static final ASN1ObjectIdentifier id_kem_rsa = is18033_2.branch("2.4");
    public static final ASN1ObjectIdentifier is18033_2 = new ASN1ObjectIdentifier("1.0.18033.2");
    public static final ASN1ObjectIdentifier iso_encryption_algorithms = new ASN1ObjectIdentifier("1.0.10118");
    public static final ASN1ObjectIdentifier ripemd128 = hash_algorithms.branch("50");
    public static final ASN1ObjectIdentifier ripemd160 = hash_algorithms.branch("49");
    public static final ASN1ObjectIdentifier whirlpool = hash_algorithms.branch("55");
}
