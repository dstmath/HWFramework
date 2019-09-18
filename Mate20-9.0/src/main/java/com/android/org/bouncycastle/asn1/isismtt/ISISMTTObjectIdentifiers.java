package com.android.org.bouncycastle.asn1.isismtt;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface ISISMTTObjectIdentifiers {
    public static final ASN1ObjectIdentifier id_isismtt = new ASN1ObjectIdentifier("1.3.36.8");
    public static final ASN1ObjectIdentifier id_isismtt_at = id_isismtt.branch("3");
    public static final ASN1ObjectIdentifier id_isismtt_at_PKReference = id_isismtt_at.branch("7");
    public static final ASN1ObjectIdentifier id_isismtt_at_additionalInformation = id_isismtt_at.branch("15");
    public static final ASN1ObjectIdentifier id_isismtt_at_admission = id_isismtt_at.branch("3");
    public static final ASN1ObjectIdentifier id_isismtt_at_certHash = id_isismtt_at.branch("13");
    public static final ASN1ObjectIdentifier id_isismtt_at_certInDirSince = id_isismtt_at.branch("12");
    public static final ASN1ObjectIdentifier id_isismtt_at_dateOfCertGen = id_isismtt_at.branch("1");
    public static final ASN1ObjectIdentifier id_isismtt_at_declarationOfMajority = id_isismtt_at.branch("5");
    public static final ASN1ObjectIdentifier id_isismtt_at_iCCSN = id_isismtt_at.branch("6");
    public static final ASN1ObjectIdentifier id_isismtt_at_liabilityLimitationFlag = new ASN1ObjectIdentifier("0.2.262.1.10.12.0");
    public static final ASN1ObjectIdentifier id_isismtt_at_monetaryLimit = id_isismtt_at.branch("4");
    public static final ASN1ObjectIdentifier id_isismtt_at_nameAtBirth = id_isismtt_at.branch("14");
    public static final ASN1ObjectIdentifier id_isismtt_at_namingAuthorities = id_isismtt_at.branch("11");
    public static final ASN1ObjectIdentifier id_isismtt_at_procuration = id_isismtt_at.branch("2");
    public static final ASN1ObjectIdentifier id_isismtt_at_requestedCertificate = id_isismtt_at.branch("10");
    public static final ASN1ObjectIdentifier id_isismtt_at_restriction = id_isismtt_at.branch("8");
    public static final ASN1ObjectIdentifier id_isismtt_at_retrieveIfAllowed = id_isismtt_at.branch("9");
    public static final ASN1ObjectIdentifier id_isismtt_cp = id_isismtt.branch("1");
    public static final ASN1ObjectIdentifier id_isismtt_cp_accredited = id_isismtt_cp.branch("1");
}
