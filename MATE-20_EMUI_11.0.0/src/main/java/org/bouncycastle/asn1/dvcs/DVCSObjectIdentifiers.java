package org.bouncycastle.asn1.dvcs;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface DVCSObjectIdentifiers {
    public static final ASN1ObjectIdentifier id_aa_dvcs_dvc = id_smime.branch("2.29");
    public static final ASN1ObjectIdentifier id_ad_dvcs = id_pkix.branch("48.4");
    public static final ASN1ObjectIdentifier id_ct_DVCSRequestData = id_smime.branch("1.7");
    public static final ASN1ObjectIdentifier id_ct_DVCSResponseData = id_smime.branch("1.8");
    public static final ASN1ObjectIdentifier id_kp_dvcs = id_pkix.branch("3.10");
    public static final ASN1ObjectIdentifier id_pkix = new ASN1ObjectIdentifier("1.3.6.1.5.5.7");
    public static final ASN1ObjectIdentifier id_smime = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16");
}
