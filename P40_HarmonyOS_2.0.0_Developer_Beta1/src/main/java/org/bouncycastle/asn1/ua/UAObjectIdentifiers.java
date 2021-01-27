package org.bouncycastle.asn1.ua;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface UAObjectIdentifiers {
    public static final ASN1ObjectIdentifier UaOid = new ASN1ObjectIdentifier("1.2.804.2.1.1.1");
    public static final ASN1ObjectIdentifier dstu4145be = UaOid.branch("1.3.1.1.1.1");
    public static final ASN1ObjectIdentifier dstu4145le = UaOid.branch("1.3.1.1");
    public static final ASN1ObjectIdentifier dstu7564digest_256 = UaOid.branch("1.2.2.1");
    public static final ASN1ObjectIdentifier dstu7564digest_384 = UaOid.branch("1.2.2.2");
    public static final ASN1ObjectIdentifier dstu7564digest_512 = UaOid.branch("1.2.2.3");
    public static final ASN1ObjectIdentifier dstu7564mac_256 = UaOid.branch("1.2.2.4");
    public static final ASN1ObjectIdentifier dstu7564mac_384 = UaOid.branch("1.2.2.5");
    public static final ASN1ObjectIdentifier dstu7564mac_512 = UaOid.branch("1.2.2.6");
    public static final ASN1ObjectIdentifier dstu7624cbc_128 = UaOid.branch("1.1.3.5.1");
    public static final ASN1ObjectIdentifier dstu7624cbc_256 = UaOid.branch("1.1.3.5.2");
    public static final ASN1ObjectIdentifier dstu7624cbc_512 = UaOid.branch("1.1.3.5.3");
    public static final ASN1ObjectIdentifier dstu7624ccm_128 = UaOid.branch("1.1.3.8.1");
    public static final ASN1ObjectIdentifier dstu7624ccm_256 = UaOid.branch("1.1.3.8.2");
    public static final ASN1ObjectIdentifier dstu7624ccm_512 = UaOid.branch("1.1.3.8.3");
    public static final ASN1ObjectIdentifier dstu7624cfb_128 = UaOid.branch("1.1.3.3.1");
    public static final ASN1ObjectIdentifier dstu7624cfb_256 = UaOid.branch("1.1.3.3.2");
    public static final ASN1ObjectIdentifier dstu7624cfb_512 = UaOid.branch("1.1.3.3.3");
    public static final ASN1ObjectIdentifier dstu7624cmac_128 = UaOid.branch("1.1.3.4.1");
    public static final ASN1ObjectIdentifier dstu7624cmac_256 = UaOid.branch("1.1.3.4.2");
    public static final ASN1ObjectIdentifier dstu7624cmac_512 = UaOid.branch("1.1.3.4.3");
    public static final ASN1ObjectIdentifier dstu7624ctr_128 = UaOid.branch("1.1.3.2.1");
    public static final ASN1ObjectIdentifier dstu7624ctr_256 = UaOid.branch("1.1.3.2.2");
    public static final ASN1ObjectIdentifier dstu7624ctr_512 = UaOid.branch("1.1.3.2.3");
    public static final ASN1ObjectIdentifier dstu7624ecb_128 = UaOid.branch("1.1.3.1.1");
    public static final ASN1ObjectIdentifier dstu7624ecb_256 = UaOid.branch("1.1.3.1.2");
    public static final ASN1ObjectIdentifier dstu7624ecb_512 = UaOid.branch("1.1.3.1.3");
    public static final ASN1ObjectIdentifier dstu7624gmac_128 = UaOid.branch("1.1.3.7.1");
    public static final ASN1ObjectIdentifier dstu7624gmac_256 = UaOid.branch("1.1.3.7.2");
    public static final ASN1ObjectIdentifier dstu7624gmac_512 = UaOid.branch("1.1.3.7.3");
    public static final ASN1ObjectIdentifier dstu7624kw_128 = UaOid.branch("1.1.3.10.1");
    public static final ASN1ObjectIdentifier dstu7624kw_256 = UaOid.branch("1.1.3.10.2");
    public static final ASN1ObjectIdentifier dstu7624kw_512 = UaOid.branch("1.1.3.10.3");
    public static final ASN1ObjectIdentifier dstu7624ofb_128 = UaOid.branch("1.1.3.6.1");
    public static final ASN1ObjectIdentifier dstu7624ofb_256 = UaOid.branch("1.1.3.6.2");
    public static final ASN1ObjectIdentifier dstu7624ofb_512 = UaOid.branch("1.1.3.6.3");
    public static final ASN1ObjectIdentifier dstu7624xts_128 = UaOid.branch("1.1.3.9.1");
    public static final ASN1ObjectIdentifier dstu7624xts_256 = UaOid.branch("1.1.3.9.2");
    public static final ASN1ObjectIdentifier dstu7624xts_512 = UaOid.branch("1.1.3.9.3");
}
