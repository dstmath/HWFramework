package com.huawei.security.hccm.common.connection.cmp;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import com.huawei.security.hccm.EnrollmentException;
import com.huawei.security.hccm.common.bcextension.JcaContentSignerExtensionBuilder;
import com.huawei.security.hccm.common.connection.CAConnection;
import com.huawei.security.hccm.common.connection.HttpConnection;
import com.huawei.security.hccm.common.connection.exception.MalFormedPKIMessageException;
import com.huawei.security.hccm.common.connection.exception.RAConnectionException;
import com.huawei.security.hccm.param.EnrollmentParamsSpec;
import com.huawei.security.hccm.param.ProtocolParamCMP;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cmp.CMPCertificate;
import org.bouncycastle.asn1.cmp.CertRepMessage;
import org.bouncycastle.asn1.cmp.CertResponse;
import org.bouncycastle.asn1.cmp.ErrorMsgContent;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.asn1.cmp.PKIMessages;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.cmp.RevDetails;
import org.bouncycastle.asn1.cmp.RevReqContent;
import org.bouncycastle.asn1.crmf.CertReqMessages;
import org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import org.bouncycastle.asn1.crmf.OptionalValidity;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.cmp.CMPException;
import org.bouncycastle.cert.cmp.GeneralPKIMessage;
import org.bouncycastle.cert.cmp.ProtectedPKIMessage;
import org.bouncycastle.cert.cmp.ProtectedPKIMessageBuilder;
import org.bouncycastle.cert.crmf.jcajce.JcaCertificateRequestMessageBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.json.JSONObject;

public class CMPConnection implements CAConnection {
    private static final int CA_CENTER_CERT_INDEX = 1;
    private static final int CA_CERT_FROM_PKI_INDEX = 1;
    private static final int ISSUED_CERT_CHAIN_SIZE = 2;
    private static final int ISSUED_CERT_INDEX = 0;
    private static final int PKI_EXTRA_CERT_CHAIN_SIZE = 3;
    private static final int RA_CERT_INDEX = 0;
    private static final int ROOT_CERT_INDEX = 2;
    private static final int SENDER_NONCE_LEN = 16;
    private static final String TAG = "CMPConnection";
    private static final int TRANSACTION_ID_LEN = 16;
    private byte[] innerTid = new byte[16];
    private byte[] senderNonce = new byte[16];

    public Certificate[] enroll(@NonNull String keyStoreType, @NonNull String keyStoreProvider, @NonNull EnrollmentParamsSpec params, @NonNull JSONObject connectionConfig) throws Exception {
        try {
            return processPKIResponse(keyStoreType, params, transceive(params.getEnrollmentURL(), new GeneralPKIMessage(generateRequest(keyStoreType, keyStoreProvider, params, 0).toASN1Structure()), connectionConfig));
        } catch (EnrollmentException e) {
            throw e;
        } catch (ConnectException e2) {
            Log.e(TAG, "connection failed " + e2.getMessage());
            throw new EnrollmentException(e2.getMessage(), -11);
        } catch (MalFormedPKIMessageException e3) {
            Log.e(TAG, "malformed pki massage " + e3.getMessage());
            throw new EnrollmentException("malformed pki massage " + e3.getMessage(), e3.getErrorCode());
        } catch (RAConnectionException e4) {
            Log.e(TAG, "ra connection failed");
            throw new EnrollmentException(e4.getMessage(), -19);
        } catch (CertificateException e5) {
            Log.e(TAG, "convert certificate failed");
            throw new EnrollmentException(e5.getMessage(), -23);
        } catch (KeyStoreException e6) {
            Log.e(TAG, "key store error during cert enroll " + e6.getMessage(), e6);
            throw new EnrollmentException(e6.getMessage(), -5);
        } catch (Exception e7) {
            e7.printStackTrace();
            Log.e(TAG, "unhandled exception");
            throw new EnrollmentException(e7.getMessage(), -9);
        }
    }

    private ProtectedPKIMessage generateRequest(@NonNull String keyStoreType, @NonNull String keyStoreProvider, @NonNull EnrollmentParamsSpec params, @NonNull int requestType) throws Exception {
        String algPadding;
        int i = requestType;
        String alias = params.getAlias();
        KeyStore keystore = KeyStore.getInstance(keyStoreType, keyStoreProvider);
        keystore.load(null);
        X509Certificate attestationCert = (X509Certificate) keystore.getCertificate(alias);
        PublicKey publicKey = attestationCert.getPublicKey();
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(publicKey.getEncoded()));
        if (params.getSigAlgPadding() == null || !"PSS".equals(params.getSigAlgPadding())) {
            algPadding = attestationCert.getSigAlgName();
        } else {
            algPadding = JcaContentSignerExtensionBuilder.SHA256PSS;
        }
        ContentSigner contentSigner = new JcaContentSignerExtensionBuilder(algPadding).setProvider(keystore.getProvider()).build((PrivateKey) keystore.getKey(alias, null));
        X509Certificate deviceCert = (X509Certificate) keystore.getCertificateChain(alias)[1];
        JcaCertificateRequestMessageBuilder requestBuilder = new JcaCertificateRequestMessageBuilder(BigInteger.ZERO);
        requestBuilder.setSubject(params.getEnrollmentCertSubject());
        requestBuilder.setPublicKey(subjectPublicKeyInfo);
        params.getProtocolParam();
        requestBuilder.setIssuer(attestationCert.getIssuerX500Principal());
        GeneralName sender = new GeneralName(new JcaX509CertificateHolder(attestationCert).getSubject());
        GeneralName receiver = new GeneralName(new JcaX509CertificateHolder(attestationCert).getSubject());
        if (i == 0 || i == 2) {
            KeyStore keyStore = keystore;
            PublicKey publicKey2 = publicKey;
            SubjectPublicKeyInfo subjectPublicKeyInfo2 = subjectPublicKeyInfo;
            String str = algPadding;
            return new ProtectedPKIMessageBuilder(sender, receiver).setMessageTime(new Date()).setBody(new PKIBody(i, new CertReqMessages(requestBuilder.build().toASN1Structure()))).setTransactionID(this.innerTid).setSenderNonce(this.senderNonce).addCMPCertificate(new JcaX509CertificateHolder(attestationCert)).addCMPCertificate(new JcaX509CertificateHolder(deviceCert)).build(contentSigner);
        } else if (i == 11) {
            try {
                JcaX509CertificateHolder certificateHolder = new JcaX509CertificateHolder(attestationCert);
                String str2 = alias;
                try {
                    KeyStore keyStore2 = keystore;
                } catch (CMPException e) {
                    e = e;
                    KeyStore keyStore3 = keystore;
                    PublicKey publicKey3 = publicKey;
                    SubjectPublicKeyInfo subjectPublicKeyInfo3 = subjectPublicKeyInfo;
                    String str3 = algPadding;
                    Log.e(TAG, "build protected pki msg failed" + e.getMessage());
                    throw new EnrollmentException(e.getMessage(), -2);
                }
                try {
                    PublicKey publicKey4 = publicKey;
                    try {
                        SubjectPublicKeyInfo subjectPublicKeyInfo4 = subjectPublicKeyInfo;
                    } catch (CMPException e2) {
                        e = e2;
                        SubjectPublicKeyInfo subjectPublicKeyInfo5 = subjectPublicKeyInfo;
                        String str4 = algPadding;
                        Log.e(TAG, "build protected pki msg failed" + e.getMessage());
                        throw new EnrollmentException(e.getMessage(), -2);
                    }
                } catch (CMPException e3) {
                    e = e3;
                    PublicKey publicKey5 = publicKey;
                    SubjectPublicKeyInfo subjectPublicKeyInfo6 = subjectPublicKeyInfo;
                    String str5 = algPadding;
                    Log.e(TAG, "build protected pki msg failed" + e.getMessage());
                    throw new EnrollmentException(e.getMessage(), -2);
                }
                try {
                    String str6 = algPadding;
                    try {
                        JcaX509CertificateHolder jcaX509CertificateHolder = certificateHolder;
                        return new ProtectedPKIMessageBuilder(sender, receiver).setMessageTime(new Date()).setBody(new PKIBody(11, new RevReqContent(new RevDetails(new CertTemplateBuilder().setVersion(certificateHolder.getVersionNumber()).setSerialNumber(new ASN1Integer(certificateHolder.getSerialNumber())).setSigningAlg(certificateHolder.getSignatureAlgorithm()).setIssuer(certificateHolder.getIssuer()).setValidity(new OptionalValidity(new Time(certificateHolder.getNotBefore()), new Time(certificateHolder.getNotAfter()))).setSubject(certificateHolder.getSubject()).setPublicKey(certificateHolder.getSubjectPublicKeyInfo()).setExtensions(certificateHolder.getExtensions()).build())))).setTransactionID(this.innerTid).setSenderNonce(this.senderNonce).addCMPCertificate(new JcaX509CertificateHolder(attestationCert)).addCMPCertificate(new JcaX509CertificateHolder(deviceCert)).build(contentSigner);
                    } catch (CMPException e4) {
                        e = e4;
                        Log.e(TAG, "build protected pki msg failed" + e.getMessage());
                        throw new EnrollmentException(e.getMessage(), -2);
                    }
                } catch (CMPException e5) {
                    e = e5;
                    String str7 = algPadding;
                    Log.e(TAG, "build protected pki msg failed" + e.getMessage());
                    throw new EnrollmentException(e.getMessage(), -2);
                }
            } catch (CMPException e6) {
                e = e6;
                String str8 = alias;
                KeyStore keyStore4 = keystore;
                PublicKey publicKey6 = publicKey;
                SubjectPublicKeyInfo subjectPublicKeyInfo7 = subjectPublicKeyInfo;
                String str9 = algPadding;
                Log.e(TAG, "build protected pki msg failed" + e.getMessage());
                throw new EnrollmentException(e.getMessage(), -2);
            }
        } else {
            KeyStore keyStore5 = keystore;
            PublicKey publicKey7 = publicKey;
            SubjectPublicKeyInfo subjectPublicKeyInfo8 = subjectPublicKeyInfo;
            String str10 = algPadding;
            throw new CMPException("Not supported CMP request type [" + i + "]");
        }
    }

    private boolean verifyCert(X509Certificate subCert, X509Certificate rootCert) {
        try {
            Signature signature = Signature.getInstance(subCert.getSigAlgName());
            signature.initVerify(rootCert.getPublicKey());
            signature.update(subCert.getTBSCertificate());
            if (signature.verify(subCert.getSignature())) {
                return true;
            }
            Log.e(TAG, "failed to verify this cert");
            return false;
        } catch (InvalidKeyException e) {
            Log.e(TAG, "invalid key exception occurred during cert verify");
            return false;
        } catch (SignatureException e2) {
            Log.e(TAG, "signature exception occurred during cert verify");
            return false;
        } catch (NoSuchAlgorithmException e3) {
            Log.e(TAG, "no such algorithm exception occurred during cert verify");
            return false;
        } catch (CertificateEncodingException e4) {
            Log.e(TAG, "cert encoding exception occurred during cert verify");
            return false;
        }
    }

    private Certificate[] getCertChainCmp(@NonNull String keyStoreType, @NonNull EnrollmentParamsSpec params, @NonNull GeneralPKIMessage response, @NonNull CertResponse certResponse) throws Exception {
        GeneralPKIMessage generalPKIMessage = response;
        if (response.hasProtection()) {
            CMPCertificate[] extraCerts = response.toASN1Structure().getExtraCerts();
            if (extraCerts == null || extraCerts.length != 3) {
                throw new MalFormedPKIMessageException("extraCerts is null or length is not correct", -18);
            }
            ProtocolParamCMP cmpParam = (ProtocolParamCMP) params.getProtocolParam();
            X509Certificate rootCertFromApp = cmpParam.getRootCertificate();
            X509Certificate rootCertFromPKI = (X509Certificate) convert(extraCerts[2]);
            if (isCertEqual(rootCertFromApp, rootCertFromPKI)) {
                X509Certificate caCert = (X509Certificate) convert(extraCerts[1]);
                if (verifyCert(caCert, rootCertFromPKI)) {
                    X509Certificate raCert = (X509Certificate) convert(extraCerts[0]);
                    if (verifyCert(raCert, caCert)) {
                        X509Certificate raCertFromApp = cmpParam.getRaCertificate();
                        boolean isVerified = false;
                        if (raCertFromApp != null) {
                            isVerified = new ProtectedPKIMessage(generalPKIMessage).verify(new JcaContentVerifierProviderBuilder().build(raCertFromApp.getPublicKey()));
                            if (!isVerified) {
                                Log.w(TAG, "failed to verify the protected msg with ra cert from app");
                            }
                        }
                        if (!isVerified && !isCertEqual(raCert, raCertFromApp)) {
                            Log.d(TAG, "verify pki message with ra from pki");
                            isVerified = new ProtectedPKIMessage(generalPKIMessage).verify(new JcaContentVerifierProviderBuilder().build(raCert.getPublicKey()));
                        }
                        if (isVerified) {
                            X509Certificate issuedCert = (X509Certificate) convert(certResponse.getCertifiedKeyPair().getCertOrEncCert().getCertificate());
                            KeyStore ks = KeyStore.getInstance(keyStoreType);
                            ks.load(null);
                            CMPCertificate[] cMPCertificateArr = extraCerts;
                            if (!Arrays.equals(((X509Certificate) ks.getCertificate(params.getAlias())).getPublicKey().getEncoded(), issuedCert.getPublicKey().getEncoded())) {
                                throw new MalFormedPKIMessageException("Public key in attestation certificate does not match that in the issued certificate!", -14);
                            } else if (verifyCert(issuedCert, caCert)) {
                                return new Certificate[]{issuedCert, caCert};
                            } else {
                                printCertificate(issuedCert, "Issued certificate");
                                ProtocolParamCMP protocolParamCMP = cmpParam;
                                throw new MalFormedPKIMessageException("Failed to verify the issued certificate!", -13);
                            }
                        } else {
                            ProtocolParamCMP protocolParamCMP2 = cmpParam;
                            throw new MalFormedPKIMessageException("Failed to validate the ProtectedPKIMessage", -16);
                        }
                    } else {
                        ProtocolParamCMP protocolParamCMP3 = cmpParam;
                        printCertificate(raCert, "ra certificate");
                        throw new MalFormedPKIMessageException("Failed to verify the RA certificate!", -34);
                    }
                } else {
                    ProtocolParamCMP protocolParamCMP4 = cmpParam;
                    printCertificate(caCert, "ca certificate");
                    throw new MalFormedPKIMessageException("Failed to verify the CA certificate!", -33);
                }
            } else {
                ProtocolParamCMP protocolParamCMP5 = cmpParam;
                printCertificate(rootCertFromPKI, "root cert form server");
                Log.e(TAG, "The root cert do not match!");
                throw new MalFormedPKIMessageException("root cert not correct", -15);
            }
        } else {
            throw new MalFormedPKIMessageException("The response PKIMessage was not protected!", -17);
        }
    }

    private Certificate[] processPKIResponse(@NonNull String keyStoreType, @NonNull EnrollmentParamsSpec params, @NonNull GeneralPKIMessage response) throws Exception {
        verifyPKIResponseOK(response);
        PKIBody pkiBody = response.getBody();
        int pkiBodyType = pkiBody.getType();
        if (pkiBodyType == 20) {
            PKIMessage[] messageArray = PKIMessages.getInstance(pkiBody.getContent()).toPKIMessageArray();
            if (messageArray.length <= 0 || messageArray[0].getHeader().getRecipNonce() == null || !Arrays.equals(this.senderNonce, messageArray[0].getHeader().getRecipNonce().getOctets())) {
                throw new MalFormedPKIMessageException("(Nested Message) The receipt nonce should be the same as the sender nonce!", -12);
            }
            pkiBody = messageArray[0].getBody();
            pkiBodyType = pkiBody.getType();
        }
        if (pkiBodyType == 1) {
            CertResponse[] respCert = CertRepMessage.getInstance(pkiBody.getContent()).getResponse();
            if (respCert.length > 0) {
                CertResponse certResponse = respCert[0];
                PKIStatusInfo pkiStatusInfo = certResponse.getStatus();
                int pkiStatusInfoValue = pkiStatusInfo.getStatus().intValue();
                switch (pkiStatusInfoValue) {
                    case 0:
                    case 1:
                        return getCertChainCmp(keyStoreType, params, response, certResponse);
                    case 2:
                        throw new RAConnectionException("Certificate request is rejected: " + pkiStatusInfo.getStatusString().getStringAt(0));
                    default:
                        throw new MalFormedPKIMessageException("Unexpected PKIStatus: " + pkiStatusInfoValue, -20);
                }
            } else {
                throw new MalFormedPKIMessageException("CertResponse is empty!", -21);
            }
        } else {
            throw new MalFormedPKIMessageException("Unexpected PKIBody Type (" + pkiBodyType + ")", -22);
        }
    }

    private void verifyPKIResponseOK(@NonNull GeneralPKIMessage response) throws RAConnectionException, MalFormedPKIMessageException {
        if (response.getBody().getType() == 23) {
            PKIStatusInfo pkiStatusInfo = ErrorMsgContent.getInstance(response.getBody().getContent()).getPKIStatusInfo();
            throw new RAConnectionException("Server returned error: errorCode:" + pkiStatusInfo.getStatus().intValue() + " errorDetail: " + pkiStatusInfo.getStatusString().getStringAt(0));
        } else if (response.getHeader().getRecipNonce() == null || !Arrays.equals(this.senderNonce, response.getHeader().getRecipNonce().getOctets())) {
            throw new MalFormedPKIMessageException("The receipt nonce should be the same as the sender nonce!");
        }
    }

    private GeneralPKIMessage transceive(@NonNull URL url, @NonNull GeneralPKIMessage req, JSONObject config) throws EnrollmentException {
        try {
            return new GeneralPKIMessage(setupConnection(url, config).send(req.toASN1Structure().getEncoded()));
        } catch (IOException e) {
            Log.e(TAG, "connect failed" + e.getMessage());
            throw new EnrollmentException("https connect failed", -11);
        } catch (MalFormedPKIMessageException e2) {
            throw new EnrollmentException("connect failed " + e2.getMessage(), e2.getErrorCode());
        }
    }

    private HttpConnection setupConnection(@NonNull URL url, JSONObject config) throws EnrollmentException {
        HttpConnection conn = new HttpConnection(HttpConnection.HttpHeaders.POST);
        try {
            conn.initialize(url, config.optJSONObject("cmp_headers"));
            conn.setUserConfig(config.optJSONObject("user_settings"));
            return conn;
        } catch (IOException ioe) {
            Log.e(TAG, "Got unexpected IOException while setting up the connection with settings.");
            throw new EnrollmentException("Got unexpected IOException while setting up the connection with settings.", (Throwable) ioe);
        }
    }

    private Certificate convert(@NonNull CMPCertificate cert) throws CertificateException, IOException {
        return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
    }

    private boolean isCertEqual(X509Certificate cert1, X509Certificate cert2) {
        if (cert1 == null || cert2 == null) {
            Log.e(TAG, "one of the cert is null");
            return false;
        }
        try {
            return Arrays.equals(cert1.getEncoded(), cert2.getEncoded());
        } catch (CertificateEncodingException e) {
            Log.e(TAG, "cert encoding failed during the cert compare");
            return false;
        }
    }

    private void printCertificate(X509Certificate certificate, String description) throws Exception {
        Log.d(TAG, description);
        Log.d(TAG, "-----BEGIN CERTIFICATE-----");
        Log.d(TAG, Base64.encodeToString(certificate.getEncoded(), 0));
        Log.d(TAG, "-----END CERTIFICATE-----");
    }
}
