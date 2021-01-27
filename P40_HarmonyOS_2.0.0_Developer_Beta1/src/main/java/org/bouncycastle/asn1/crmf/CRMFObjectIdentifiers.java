package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

public interface CRMFObjectIdentifiers {
    public static final ASN1ObjectIdentifier id_alg = id_pkix.branch("6");
    public static final ASN1ObjectIdentifier id_alg_dh_pop = id_alg.branch("4");
    public static final ASN1ObjectIdentifier id_ct_encKeyWithID = PKCSObjectIdentifiers.id_ct.branch("21");
    public static final ASN1ObjectIdentifier id_dh_sig_hmac_sha1 = id_alg.branch("3");
    public static final ASN1ObjectIdentifier id_pkip = id_pkix.branch("5");
    public static final ASN1ObjectIdentifier id_pkix = new ASN1ObjectIdentifier("1.3.6.1.5.5.7");
    public static final ASN1ObjectIdentifier id_regCtrl = id_pkip.branch("1");
    public static final ASN1ObjectIdentifier id_regCtrl_authenticator = id_regCtrl.branch("2");
    public static final ASN1ObjectIdentifier id_regCtrl_pkiArchiveOptions = id_regCtrl.branch("4");
    public static final ASN1ObjectIdentifier id_regCtrl_pkiPublicationInfo = id_regCtrl.branch("3");
    public static final ASN1ObjectIdentifier id_regCtrl_regToken = id_regCtrl.branch("1");
}
