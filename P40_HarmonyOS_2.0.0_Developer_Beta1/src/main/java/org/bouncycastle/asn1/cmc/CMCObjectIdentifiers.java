package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface CMCObjectIdentifiers {
    public static final ASN1ObjectIdentifier id_cct = id_pkix.branch("12");
    public static final ASN1ObjectIdentifier id_cct_PKIData = id_cct.branch("2");
    public static final ASN1ObjectIdentifier id_cct_PKIResponse = id_cct.branch("3");
    public static final ASN1ObjectIdentifier id_cmc = id_pkix.branch("7");
    public static final ASN1ObjectIdentifier id_cmc_addExtensions = id_cmc.branch("8");
    public static final ASN1ObjectIdentifier id_cmc_authData = id_cmc.branch("27");
    public static final ASN1ObjectIdentifier id_cmc_batchRequests = id_cmc.branch("28");
    public static final ASN1ObjectIdentifier id_cmc_batchResponses = id_cmc.branch("29");
    public static final ASN1ObjectIdentifier id_cmc_confirmCertAcceptance = id_cmc.branch("24");
    public static final ASN1ObjectIdentifier id_cmc_controlProcessed = id_cmc.branch("32");
    public static final ASN1ObjectIdentifier id_cmc_dataReturn = id_cmc.branch("4");
    public static final ASN1ObjectIdentifier id_cmc_decryptedPOP = id_cmc.branch("10");
    public static final ASN1ObjectIdentifier id_cmc_encryptedPOP = id_cmc.branch("9");
    public static final ASN1ObjectIdentifier id_cmc_getCRL = id_cmc.branch("16");
    public static final ASN1ObjectIdentifier id_cmc_getCert = id_cmc.branch("15");
    public static final ASN1ObjectIdentifier id_cmc_identification = id_cmc.branch("2");
    public static final ASN1ObjectIdentifier id_cmc_identityProof = id_cmc.branch("3");
    public static final ASN1ObjectIdentifier id_cmc_identityProofV2 = id_cmc.branch("34");
    public static final ASN1ObjectIdentifier id_cmc_lraPOPWitness = id_cmc.branch("11");
    public static final ASN1ObjectIdentifier id_cmc_modCertTemplate = id_cmc.branch("31");
    public static final ASN1ObjectIdentifier id_cmc_popLinkRandom = id_cmc.branch("22");
    public static final ASN1ObjectIdentifier id_cmc_popLinkWitness = id_cmc.branch("23");
    public static final ASN1ObjectIdentifier id_cmc_popLinkWitnessV2 = id_cmc.branch("33");
    public static final ASN1ObjectIdentifier id_cmc_publishCert = id_cmc.branch("30");
    public static final ASN1ObjectIdentifier id_cmc_queryPending = id_cmc.branch("21");
    public static final ASN1ObjectIdentifier id_cmc_recipientNonce = id_cmc.branch("7");
    public static final ASN1ObjectIdentifier id_cmc_regInfo = id_cmc.branch("18");
    public static final ASN1ObjectIdentifier id_cmc_responseInfo = id_cmc.branch("19");
    public static final ASN1ObjectIdentifier id_cmc_revokeRequest = id_cmc.branch("17");
    public static final ASN1ObjectIdentifier id_cmc_senderNonce = id_cmc.branch("6");
    public static final ASN1ObjectIdentifier id_cmc_statusInfo = id_cmc.branch("1");
    public static final ASN1ObjectIdentifier id_cmc_statusInfoV2 = id_cmc.branch("25");
    public static final ASN1ObjectIdentifier id_cmc_transactionId = id_cmc.branch("5");
    public static final ASN1ObjectIdentifier id_cmc_trustedAnchors = id_cmc.branch("26");
    public static final ASN1ObjectIdentifier id_pkix = new ASN1ObjectIdentifier("1.3.6.1.5.5.7");
}
