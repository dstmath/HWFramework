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
import com.huawei.security.hccm.param.ProtocolParam;
import com.huawei.security.hccm.param.ProtocolParamCMP;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
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
import org.bouncycastle.cert.crmf.CRMFException;
import org.bouncycastle.cert.crmf.jcajce.JcaCertificateRequestMessageBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
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
    private byte[] mInnerTids = new byte[16];
    private byte[] mSenderNonces = new byte[16];

    @Override // com.huawei.security.hccm.common.connection.CAConnection
    @NonNull
    public Certificate[] enroll(@NonNull String keyStoreType, @NonNull String keyStoreProvider, @NonNull EnrollmentParamsSpec params, @NonNull JSONObject connectionConfig) throws EnrollmentException {
        try {
            return processPKIResponse(keyStoreType, params, transceive(params.getEnrollmentURL(), new GeneralPKIMessage(generateRequest(keyStoreType, keyStoreProvider, params, 0).toASN1Structure()), connectionConfig));
        } catch (EnrollmentException e) {
            throw e;
        } catch (MalFormedPKIMessageException e2) {
            Log.e(TAG, "Malformed pki massage: " + e2.getMessage());
            throw new EnrollmentException("Malformed pki massage " + e2.getMessage(), e2.getErrorCode());
        } catch (RAConnectionException e3) {
            Log.e(TAG, "RA connection failed");
            throw new EnrollmentException(e3.getMessage(), -19);
        } catch (Exception e4) {
            Log.e(TAG, "Unhandled exception: " + e4.getMessage());
            throw new EnrollmentException(e4.getMessage(), -9);
        }
    }

    @NonNull
    private ProtectedPKIMessage generateRequest(@NonNull String keyStoreType, @NonNull String keyStoreProvider, @NonNull EnrollmentParamsSpec params, int requestType) throws EnrollmentException {
        try {
            String alias = params.getAlias();
            KeyStore keystore = KeyStore.getInstance(keyStoreType, keyStoreProvider);
            keystore.load(null);
            X509Certificate attestationCert = (X509Certificate) keystore.getCertificate(alias);
            X509Certificate deviceCert = (X509Certificate) keystore.getCertificateChain(alias)[1];
            ContentSigner contentSigner = generateContentSigner(keystore, attestationCert, params);
            if (requestType == 0 || requestType == 2) {
                return generateRequestPkiMessage(attestationCert, deviceCert, contentSigner, generateCertReqMessages(params, attestationCert), requestType);
            }
            if (requestType == 11) {
                return generateRequestPkiMessage(attestationCert, deviceCert, contentSigner, generateRevReqContent(attestationCert), requestType);
            }
            throw new CMPException("Not supported CMP request type [" + requestType + "]");
        } catch (CMPException e) {
            Log.e(TAG, "Failed to generate request, build protected pki msg failed: " + e.getMessage());
            throw new EnrollmentException(e.getMessage(), -2);
        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | UnrecoverableKeyException | CertificateEncodingException | CRMFException | OperatorCreationException e2) {
            Log.e(TAG, "Failed to generate request: " + e2.getMessage());
            throw new EnrollmentException(e2.getMessage(), -9);
        } catch (KeyStoreException e3) {
            Log.e(TAG, "Failed to generate request, key store error: " + e3.getMessage());
            throw new EnrollmentException(e3.getMessage(), -5);
        } catch (CertificateException e4) {
            Log.e(TAG, "Failed to generate request, convert certificate failed: " + e4.getMessage());
            throw new EnrollmentException(e4.getMessage(), -23);
        }
    }

    private ContentSigner generateContentSigner(KeyStore keystore, X509Certificate attestationCert, EnrollmentParamsSpec params) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, OperatorCreationException {
        String algPadding;
        if (params.getSigAlgPadding() == null || !"PSS".equals(params.getSigAlgPadding())) {
            algPadding = attestationCert.getSigAlgName();
        } else {
            algPadding = JcaContentSignerExtensionBuilder.SHA256_PSS;
        }
        return new JcaContentSignerExtensionBuilder(algPadding).setProvider(keystore.getProvider()).build((PrivateKey) keystore.getKey(params.getAlias(), null));
    }

    @NonNull
    private CertReqMessages generateCertReqMessages(EnrollmentParamsSpec params, X509Certificate attestationCert) throws CRMFException {
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(attestationCert.getPublicKey().getEncoded()));
        JcaCertificateRequestMessageBuilder requestBuilder = new JcaCertificateRequestMessageBuilder(BigInteger.ZERO);
        requestBuilder.setSubject(params.getEnrollmentCertSubject());
        requestBuilder.setPublicKey(subjectPublicKeyInfo);
        requestBuilder.setIssuer(attestationCert.getIssuerX500Principal());
        return new CertReqMessages(requestBuilder.build().toASN1Structure());
    }

    @NonNull
    private RevReqContent generateRevReqContent(X509Certificate attestationCert) throws CertificateEncodingException {
        JcaX509CertificateHolder certificateHolder = new JcaX509CertificateHolder(attestationCert);
        return new RevReqContent(new RevDetails(new CertTemplateBuilder().setVersion(certificateHolder.getVersionNumber()).setSerialNumber(new ASN1Integer(certificateHolder.getSerialNumber())).setSigningAlg(certificateHolder.getSignatureAlgorithm()).setIssuer(certificateHolder.getIssuer()).setValidity(new OptionalValidity(new Time(certificateHolder.getNotBefore()), new Time(certificateHolder.getNotAfter()))).setSubject(certificateHolder.getSubject()).setPublicKey(certificateHolder.getSubjectPublicKeyInfo()).setExtensions(certificateHolder.getExtensions()).build()));
    }

    private ProtectedPKIMessage generateRequestPkiMessage(X509Certificate attestationCert, X509Certificate deviceCert, ContentSigner contentSigner, ASN1Object bodyContent, int requestType) throws CMPException, CertificateEncodingException {
        return new ProtectedPKIMessageBuilder(new GeneralName(new JcaX509CertificateHolder(attestationCert).getSubject()), new GeneralName(new JcaX509CertificateHolder(attestationCert).getSubject())).setMessageTime(new Date()).setBody(new PKIBody(requestType, bodyContent)).setTransactionID(this.mInnerTids).setSenderNonce(this.mSenderNonces).addCMPCertificate(new JcaX509CertificateHolder(attestationCert)).addCMPCertificate(new JcaX509CertificateHolder(deviceCert)).build(contentSigner);
    }

    private boolean verifyCert(X509Certificate subCert, X509Certificate rootCert) {
        try {
            Signature signature = Signature.getInstance(subCert.getSigAlgName());
            signature.initVerify(rootCert.getPublicKey());
            signature.update(subCert.getTBSCertificate());
            return signature.verify(subCert.getSignature());
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Invalid key exception occurred during cert verify");
            return false;
        } catch (SignatureException e2) {
            Log.e(TAG, "Signature exception occurred during cert verify");
            return false;
        } catch (NoSuchAlgorithmException e3) {
            Log.e(TAG, "No such algorithm exception occurred during cert verify");
            return false;
        } catch (CertificateEncodingException e4) {
            Log.e(TAG, "Cert encoding exception occurred during cert verify");
            return false;
        }
    }

    @NonNull
    private Certificate[] getCertChainCmp(@NonNull String keyStoreType, @NonNull EnrollmentParamsSpec params, @NonNull GeneralPKIMessage response, @NonNull CertResponse certResponse) throws MalFormedPKIMessageException, EnrollmentException {
        try {
            if (response.hasProtection()) {
                CMPCertificate[] extraCerts = response.toASN1Structure().getExtraCerts();
                if (extraCerts == null || extraCerts.length != 3) {
                    throw new MalFormedPKIMessageException("extraCerts is null or length is not correct", -18);
                }
                verifyCertChainAndPKIMessage(response, params, extraCerts);
                X509Certificate issuedCert = (X509Certificate) convert(certResponse.getCertifiedKeyPair().getCertOrEncCert().getCertificate());
                KeyStore ks = KeyStore.getInstance(keyStoreType);
                ks.load(null);
                if (Arrays.equals(((X509Certificate) ks.getCertificate(params.getAlias())).getPublicKey().getEncoded(), issuedCert.getPublicKey().getEncoded())) {
                    X509Certificate caCert = (X509Certificate) convert(extraCerts[1]);
                    if (verifyCert(issuedCert, caCert)) {
                        return new Certificate[]{issuedCert, caCert};
                    }
                    printCertificate(issuedCert, "Issued certificate");
                    throw new MalFormedPKIMessageException("Failed to verify the issued certificate!", -13);
                }
                throw new MalFormedPKIMessageException("Public key in attestation certificate does not match that in the issued certificate!", -14);
            }
            throw new MalFormedPKIMessageException("The response PKIMessage was not protected!", -17);
        } catch (IOException | NoSuchAlgorithmException | CertificateEncodingException | CMPException | OperatorCreationException e) {
            Log.e(TAG, "Failed to get cert chain cmp: " + e.getMessage());
            throw new EnrollmentException(e.getMessage(), -9);
        } catch (KeyStoreException e2) {
            Log.e(TAG, "Failed to get cert chain cmp, key store error: " + e2.getMessage());
            throw new EnrollmentException(e2.getMessage(), -5);
        } catch (CertificateException e3) {
            Log.e(TAG, "Failed to get cert chain cmp, convert certificate failed: " + e3.getMessage());
            throw new EnrollmentException(e3.getMessage(), -23);
        }
    }

    private void verifyCertChainAndPKIMessage(@NonNull GeneralPKIMessage response, @NonNull EnrollmentParamsSpec params, CMPCertificate[] extraCerts) throws CertificateException, IOException, MalFormedPKIMessageException, CMPException, OperatorCreationException {
        ProtocolParam<?> protocolParam = params.getProtocolParam();
        if (protocolParam instanceof ProtocolParamCMP) {
            ProtocolParamCMP cmpParam = (ProtocolParamCMP) protocolParam;
            X509Certificate rootCertFromApp = cmpParam.getRootCertificate();
            X509Certificate rootCertFromPKI = (X509Certificate) convert(extraCerts[2]);
            if (isCertEqual(rootCertFromApp, rootCertFromPKI)) {
                X509Certificate caCert = (X509Certificate) convert(extraCerts[1]);
                if (verifyCert(caCert, rootCertFromPKI)) {
                    X509Certificate raCert = (X509Certificate) convert(extraCerts[0]);
                    if (verifyCert(raCert, caCert)) {
                        X509Certificate raCertFromApp = cmpParam.getRaCertificate();
                        boolean isVerified = false;
                        if (raCertFromApp != null && !(isVerified = verifyPKIMessage(response, raCertFromApp.getPublicKey()))) {
                            Log.w(TAG, "Failed to verify the protected msg with ra cert from app");
                        }
                        if (!isVerified && !isCertEqual(raCert, raCertFromApp)) {
                            Log.d(TAG, "Verify pki message with ra from pki");
                            isVerified = verifyPKIMessage(response, raCert.getPublicKey());
                        }
                        if (!isVerified) {
                            throw new MalFormedPKIMessageException("Failed to validate the ProtectedPKIMessage", -16);
                        }
                        return;
                    }
                    printCertificate(raCert, "ra certificate");
                    throw new MalFormedPKIMessageException("Failed to verify the RA certificate!", -34);
                }
                printCertificate(caCert, "ca certificate");
                throw new MalFormedPKIMessageException("Failed to verify the CA certificate!", -33);
            }
            printCertificate(rootCertFromPKI, "root cert form server");
            Log.e(TAG, "The root cert do not match!");
            throw new MalFormedPKIMessageException("Root cert not correct", -15);
        }
        throw new MalFormedPKIMessageException("ProtocolParam is not CMP", -24);
    }

    private boolean verifyPKIMessage(GeneralPKIMessage response, PublicKey publicKey) throws CMPException, OperatorCreationException {
        return new ProtectedPKIMessage(response).verify(new JcaContentVerifierProviderBuilder().build(publicKey));
    }

    @NonNull
    private Certificate[] processPKIResponse(@NonNull String keyStoreType, @NonNull EnrollmentParamsSpec params, @NonNull GeneralPKIMessage response) throws MalFormedPKIMessageException, EnrollmentException, RAConnectionException {
        verifyPKIMessage(response);
        PKIBody pkiBody = response.getBody();
        int pkiBodyType = pkiBody.getType();
        if (pkiBodyType == 20) {
            PKIMessage[] messageArray = PKIMessages.getInstance(pkiBody.getContent()).toPKIMessageArray();
            if (messageArray.length <= 0 || messageArray[0].getHeader().getRecipNonce() == null || !Arrays.equals(this.mSenderNonces, messageArray[0].getHeader().getRecipNonce().getOctets())) {
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
                if (pkiStatusInfoValue == 0 || pkiStatusInfoValue == 1) {
                    return getCertChainCmp(keyStoreType, params, response, certResponse);
                }
                if (pkiStatusInfoValue != 2) {
                    throw new MalFormedPKIMessageException("Unexpected PKIStatus: " + pkiStatusInfoValue, -20);
                }
                throw new RAConnectionException("Certificate request is rejected: " + pkiStatusInfo.getStatusString().getStringAt(0));
            }
            throw new MalFormedPKIMessageException("CertResponse is empty!", -21);
        }
        throw new MalFormedPKIMessageException("Unexpected PKIBody Type (" + pkiBodyType + ")", -22);
    }

    private void verifyPKIMessage(@NonNull GeneralPKIMessage response) throws RAConnectionException, MalFormedPKIMessageException {
        if (response.getBody().getType() == 23) {
            PKIStatusInfo pkiStatusInfo = ErrorMsgContent.getInstance(response.getBody().getContent()).getPKIStatusInfo();
            throw new RAConnectionException("Server returned error: errorCode:" + pkiStatusInfo.getStatus().intValue() + " errorDetail: " + pkiStatusInfo.getStatusString().getStringAt(0));
        } else if (response.getHeader().getRecipNonce() == null || !Arrays.equals(this.mSenderNonces, response.getHeader().getRecipNonce().getOctets())) {
            throw new MalFormedPKIMessageException("The receipt nonce should be the same as the sender nonce!");
        }
    }

    private GeneralPKIMessage transceive(@NonNull URL url, @NonNull GeneralPKIMessage requestMessage, JSONObject config) throws EnrollmentException {
        try {
            return new GeneralPKIMessage(setupConnection(url, config).send(requestMessage.toASN1Structure().getEncoded()));
        } catch (IOException e) {
            Log.e(TAG, "Connect failed" + e.getMessage());
            throw new EnrollmentException("Https connect failed: " + e.getMessage(), -11);
        } catch (MalFormedPKIMessageException e2) {
            throw new EnrollmentException("Connect failed " + e2.getMessage(), e2.getErrorCode());
        }
    }

    private HttpConnection setupConnection(@NonNull URL url, JSONObject config) throws EnrollmentException {
        try {
            HttpConnection connection = new HttpConnection(HttpConnection.HttpHeaders.POST);
            connection.initialize(url, config.optJSONObject("cmp_headers"));
            connection.setUserConfig(config.optJSONObject("user_settings"));
            return connection;
        } catch (IOException e) {
            Log.e(TAG, "Got unexpected IOException while setting up the connection with settings.");
            throw new EnrollmentException("Got unexpected IOException while setting up the connection with settings.", e);
        }
    }

    private Certificate convert(@NonNull CMPCertificate cert) throws CertificateException, IOException {
        return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
    }

    private boolean isCertEqual(X509Certificate cert1, X509Certificate cert2) {
        if (cert1 == null || cert2 == null) {
            Log.e(TAG, "One of the cert is null");
            return false;
        }
        try {
            return Arrays.equals(cert1.getEncoded(), cert2.getEncoded());
        } catch (CertificateEncodingException e) {
            Log.e(TAG, "Cert encoding failed during the cert compare.");
            return false;
        }
    }

    private void printCertificate(X509Certificate certificate, String description) throws CertificateEncodingException {
        Log.d(TAG, description);
        Log.d(TAG, "-----BEGIN CERTIFICATE-----");
        Log.d(TAG, Base64.encodeToString(certificate.getEncoded(), 0));
        Log.d(TAG, "-----END CERTIFICATE-----");
    }
}
