package org.bouncycastle.asn1.gnu;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface GNUObjectIdentifiers {
    public static final ASN1ObjectIdentifier CRC = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.14");
    public static final ASN1ObjectIdentifier CRC32 = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.14.1");
    public static final ASN1ObjectIdentifier Ed25519 = ellipticCurve.branch("1");
    public static final ASN1ObjectIdentifier GNU = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.1");
    public static final ASN1ObjectIdentifier GnuPG = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.2");
    public static final ASN1ObjectIdentifier GnuRadar = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.3");
    public static final ASN1ObjectIdentifier Serpent = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2");
    public static final ASN1ObjectIdentifier Serpent_128_CBC = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.2");
    public static final ASN1ObjectIdentifier Serpent_128_CFB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.4");
    public static final ASN1ObjectIdentifier Serpent_128_ECB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.1");
    public static final ASN1ObjectIdentifier Serpent_128_OFB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.3");
    public static final ASN1ObjectIdentifier Serpent_192_CBC = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.22");
    public static final ASN1ObjectIdentifier Serpent_192_CFB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.24");
    public static final ASN1ObjectIdentifier Serpent_192_ECB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.21");
    public static final ASN1ObjectIdentifier Serpent_192_OFB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.23");
    public static final ASN1ObjectIdentifier Serpent_256_CBC = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.42");
    public static final ASN1ObjectIdentifier Serpent_256_CFB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.44");
    public static final ASN1ObjectIdentifier Serpent_256_ECB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.41");
    public static final ASN1ObjectIdentifier Serpent_256_OFB = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13.2.43");
    public static final ASN1ObjectIdentifier Tiger_192 = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.12.2");
    public static final ASN1ObjectIdentifier digestAlgorithm = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.12");
    public static final ASN1ObjectIdentifier ellipticCurve = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.15");
    public static final ASN1ObjectIdentifier encryptionAlgorithm = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.13");
    public static final ASN1ObjectIdentifier notation = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.2.1");
    public static final ASN1ObjectIdentifier pkaAddress = new ASN1ObjectIdentifier("1.3.6.1.4.1.11591.2.1.1");
}
