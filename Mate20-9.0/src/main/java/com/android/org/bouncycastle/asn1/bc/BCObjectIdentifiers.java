package com.android.org.bouncycastle.asn1.bc;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface BCObjectIdentifiers {
    public static final ASN1ObjectIdentifier bc = new ASN1ObjectIdentifier("1.3.6.1.4.1.22554");
    public static final ASN1ObjectIdentifier bc_pbe = bc.branch("1");
    public static final ASN1ObjectIdentifier bc_pbe_sha1 = bc_pbe.branch("1");
    public static final ASN1ObjectIdentifier bc_pbe_sha1_pkcs12 = bc_pbe_sha1.branch("2");
    public static final ASN1ObjectIdentifier bc_pbe_sha1_pkcs12_aes128_cbc = bc_pbe_sha1_pkcs12.branch("1.2");
    public static final ASN1ObjectIdentifier bc_pbe_sha1_pkcs12_aes192_cbc = bc_pbe_sha1_pkcs12.branch("1.22");
    public static final ASN1ObjectIdentifier bc_pbe_sha1_pkcs12_aes256_cbc = bc_pbe_sha1_pkcs12.branch("1.42");
    public static final ASN1ObjectIdentifier bc_pbe_sha1_pkcs5 = bc_pbe_sha1.branch("1");
    public static final ASN1ObjectIdentifier bc_pbe_sha224 = bc_pbe.branch("2.4");
    public static final ASN1ObjectIdentifier bc_pbe_sha256 = bc_pbe.branch("2.1");
    public static final ASN1ObjectIdentifier bc_pbe_sha256_pkcs12 = bc_pbe_sha256.branch("2");
    public static final ASN1ObjectIdentifier bc_pbe_sha256_pkcs12_aes128_cbc = bc_pbe_sha256_pkcs12.branch("1.2");
    public static final ASN1ObjectIdentifier bc_pbe_sha256_pkcs12_aes192_cbc = bc_pbe_sha256_pkcs12.branch("1.22");
    public static final ASN1ObjectIdentifier bc_pbe_sha256_pkcs12_aes256_cbc = bc_pbe_sha256_pkcs12.branch("1.42");
    public static final ASN1ObjectIdentifier bc_pbe_sha256_pkcs5 = bc_pbe_sha256.branch("1");
    public static final ASN1ObjectIdentifier bc_pbe_sha384 = bc_pbe.branch("2.2");
    public static final ASN1ObjectIdentifier bc_pbe_sha512 = bc_pbe.branch("2.3");
    public static final ASN1ObjectIdentifier bc_sig = bc.branch("2");
}
