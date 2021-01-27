package org.bouncycastle.jce.provider;

import com.huawei.security.hccm.common.connection.HttpConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPRequest;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.Request;
import org.bouncycastle.asn1.ocsp.ResponseBytes;
import org.bouncycastle.asn1.ocsp.ResponseData;
import org.bouncycastle.asn1.ocsp.SingleResponse;
import org.bouncycastle.asn1.ocsp.TBSRequest;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.util.io.Streams;

/* access modifiers changed from: package-private */
public class OcspCache {
    private static final int DEFAULT_MAX_RESPONSE_SIZE = 32768;
    private static final int DEFAULT_TIMEOUT = 15000;
    private static Map<URI, WeakReference<Map<CertID, OCSPResponse>>> cache = Collections.synchronizedMap(new WeakHashMap());

    OcspCache() {
    }

    static OCSPResponse getOcspResponse(CertID certID, PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters, URI uri, X509Certificate x509Certificate, List<Extension> list, JcaJceHelper jcaJceHelper) throws CertPathValidatorException {
        IOException e;
        OCSPResponse oCSPResponse;
        ASN1GeneralizedTime nextUpdate;
        WeakReference<Map<CertID, OCSPResponse>> weakReference = cache.get(uri);
        Map<CertID, OCSPResponse> map = weakReference != null ? weakReference.get() : null;
        boolean z = false;
        if (!(map == null || (oCSPResponse = map.get(certID)) == null)) {
            ASN1Sequence responses = ResponseData.getInstance(BasicOCSPResponse.getInstance(ASN1OctetString.getInstance(oCSPResponse.getResponseBytes().getResponse()).getOctets()).getTbsResponseData()).getResponses();
            for (int i = 0; i != responses.size(); i++) {
                SingleResponse instance = SingleResponse.getInstance(responses.getObjectAt(i));
                if (certID.equals(instance.getCertID()) && (nextUpdate = instance.getNextUpdate()) != null) {
                    try {
                        if (pKIXCertRevocationCheckerParameters.getValidDate().after(nextUpdate.getDate())) {
                            map.remove(certID);
                            oCSPResponse = null;
                        }
                    } catch (ParseException e2) {
                        map.remove(certID);
                    }
                }
            }
            if (oCSPResponse != null) {
                return oCSPResponse;
            }
        }
        try {
            URL url = uri.toURL();
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            aSN1EncodableVector.add(new Request(certID, null));
            ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
            byte[] bArr = null;
            for (int i2 = 0; i2 != list.size(); i2++) {
                Extension extension = list.get(i2);
                byte[] value = extension.getValue();
                if (OCSPObjectIdentifiers.id_pkix_ocsp_nonce.getId().equals(extension.getId())) {
                    bArr = value;
                }
                aSN1EncodableVector2.add(new org.bouncycastle.asn1.x509.Extension(new ASN1ObjectIdentifier(extension.getId()), extension.isCritical(), value));
            }
            try {
                byte[] encoded = new OCSPRequest(new TBSRequest((GeneralName) null, new DERSequence(aSN1EncodableVector), Extensions.getInstance(new DERSequence(aSN1EncodableVector2))), null).getEncoded();
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod(HttpConnection.HttpHeaders.POST);
                httpURLConnection.setRequestProperty("Content-type", "application/ocsp-request");
                httpURLConnection.setRequestProperty("Content-length", String.valueOf(encoded.length));
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(encoded);
                outputStream.flush();
                InputStream inputStream = httpURLConnection.getInputStream();
                int contentLength = httpURLConnection.getContentLength();
                if (contentLength < 0) {
                    contentLength = 32768;
                }
                OCSPResponse instance2 = OCSPResponse.getInstance(Streams.readAllLimited(inputStream, contentLength));
                if (instance2.getResponseStatus().getValue().intValueExact() == 0) {
                    ResponseBytes instance3 = ResponseBytes.getInstance(instance2.getResponseBytes());
                    if (instance3.getResponseType().equals((ASN1Primitive) OCSPObjectIdentifiers.id_pkix_ocsp_basic)) {
                        try {
                            z = ProvOcspRevocationChecker.validatedOcspResponse(BasicOCSPResponse.getInstance(instance3.getResponse().getOctets()), pKIXCertRevocationCheckerParameters, bArr, x509Certificate, jcaJceHelper);
                        } catch (IOException e3) {
                            e = e3;
                            throw new CertPathValidatorException("configuration error: " + e.getMessage(), e, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                        }
                    }
                    if (z) {
                        WeakReference<Map<CertID, OCSPResponse>> weakReference2 = cache.get(uri);
                        if (weakReference2 != null) {
                            weakReference2.get().put(certID, instance2);
                        } else {
                            HashMap hashMap = new HashMap();
                            hashMap.put(certID, instance2);
                            cache.put(uri, new WeakReference<>(hashMap));
                        }
                        return instance2;
                    }
                    throw new CertPathValidatorException("OCSP response failed to validate", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                }
                throw new CertPathValidatorException("OCSP responder failed: " + instance2.getResponseStatus().getValue(), null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
            } catch (IOException e4) {
                e = e4;
                throw new CertPathValidatorException("configuration error: " + e.getMessage(), e, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
            }
        } catch (MalformedURLException e5) {
            throw new CertPathValidatorException("configuration error: " + e5.getMessage(), e5, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        }
    }
}
