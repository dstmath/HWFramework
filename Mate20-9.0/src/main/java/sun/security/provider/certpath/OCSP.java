package sun.security.provider.certpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.cert.CRLReason;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.Extension;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import sun.security.action.GetIntegerAction;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.URIName;
import sun.security.x509.X509CertImpl;

public final class OCSP {
    private static final int CONNECT_TIMEOUT = initializeTimeout();
    private static final int DEFAULT_CONNECT_TIMEOUT = 15000;
    static final ObjectIdentifier NONCE_EXTENSION_OID = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 1, 2});
    private static final Debug debug = Debug.getInstance("certpath");

    public interface RevocationStatus {

        public enum CertStatus {
            GOOD,
            REVOKED,
            UNKNOWN
        }

        CertStatus getCertStatus();

        CRLReason getRevocationReason();

        Date getRevocationTime();

        Map<String, Extension> getSingleExtensions();
    }

    private static int initializeTimeout() {
        Integer tmp = (Integer) AccessController.doPrivileged(new GetIntegerAction("com.sun.security.ocsp.timeout"));
        if (tmp == null || tmp.intValue() < 0) {
            return DEFAULT_CONNECT_TIMEOUT;
        }
        return tmp.intValue() * 1000;
    }

    private OCSP() {
    }

    public static RevocationStatus check(X509Certificate cert, X509Certificate issuerCert) throws IOException, CertPathValidatorException {
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            URI responderURI = getResponderURI(certImpl);
            if (responderURI != null) {
                CertId certId = new CertId(issuerCert, certImpl.getSerialNumberObject());
                return check((List<CertId>) Collections.singletonList(certId), responderURI, issuerCert, (X509Certificate) null, (Date) null, (List<Extension>) Collections.emptyList()).getSingleResponse(certId);
            }
            throw new CertPathValidatorException("No OCSP Responder URI in certificate");
        } catch (IOException | CertificateException e) {
            throw new CertPathValidatorException("Exception while encoding OCSPRequest", e);
        }
    }

    public static RevocationStatus check(X509Certificate cert, X509Certificate issuerCert, URI responderURI, X509Certificate responderCert, Date date) throws IOException, CertPathValidatorException {
        return check(cert, issuerCert, responderURI, responderCert, date, (List<Extension>) Collections.emptyList());
    }

    public static RevocationStatus check(X509Certificate cert, X509Certificate issuerCert, URI responderURI, X509Certificate responderCert, Date date, List<Extension> extensions) throws IOException, CertPathValidatorException {
        try {
            CertId certId = new CertId(issuerCert, X509CertImpl.toImpl(cert).getSerialNumberObject());
            return check((List<CertId>) Collections.singletonList(certId), responderURI, issuerCert, responderCert, date, extensions).getSingleResponse(certId);
        } catch (IOException | CertificateException e) {
            throw new CertPathValidatorException("Exception while encoding OCSPRequest", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x0124 A[SYNTHETIC, Splitter:B:69:0x0124] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x012d A[SYNTHETIC, Splitter:B:75:0x012d] */
    static OCSPResponse check(List<CertId> certIds, URI responderURI, X509Certificate issuerCert, X509Certificate responderCert, Date date, List<Extension> extensions) throws IOException, CertPathValidatorException {
        IOException ioe;
        try {
            List<CertId> list = certIds;
            try {
                OCSPRequest request = new OCSPRequest(list, extensions);
                byte[] bytes = request.encodeBytes();
                InputStream in = null;
                OutputStream out = null;
                try {
                    URL url = responderURI.toURL();
                    if (debug != null) {
                        Debug debug2 = debug;
                        debug2.println("connecting to OCSP service at: " + url);
                    }
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(CONNECT_TIMEOUT);
                    con.setReadTimeout(CONNECT_TIMEOUT);
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-type", "application/ocsp-request");
                    con.setRequestProperty("Content-length", String.valueOf(bytes.length));
                    OutputStream out2 = con.getOutputStream();
                    try {
                        out2.write(bytes);
                        out2.flush();
                        if (!(debug == null || con.getResponseCode() == 200)) {
                            Debug debug3 = debug;
                            debug3.println("Received HTTP error: " + con.getResponseCode() + " - " + con.getResponseMessage());
                        }
                        InputStream in2 = con.getInputStream();
                        int contentLength = con.getContentLength();
                        if (contentLength == -1) {
                            contentLength = Integer.MAX_VALUE;
                        }
                        int i = 2048;
                        if (contentLength <= 2048) {
                            i = contentLength;
                        }
                        byte[] response = new byte[i];
                        int total = 0;
                        while (true) {
                            if (total >= contentLength) {
                                break;
                            }
                            int count = in2.read(response, total, response.length - total);
                            if (count < 0) {
                                break;
                            }
                            total += count;
                            if (total >= response.length && total < contentLength) {
                                response = Arrays.copyOf(response, total * 2);
                            }
                        }
                        byte[] response2 = Arrays.copyOf(response, total);
                        if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException ioe2) {
                                IOException iOException = ioe2;
                                throw ioe2;
                            }
                        }
                        if (out2 != null) {
                            try {
                                out2.close();
                            } catch (IOException ioe3) {
                                IOException iOException2 = ioe3;
                                throw ioe3;
                            }
                        }
                        try {
                            OCSPResponse ocspResponse = new OCSPResponse(response2);
                            ocspResponse.verify(list, issuerCert, responderCert, date, request.getNonce());
                            return ocspResponse;
                        } catch (IOException ioe4) {
                            throw new CertPathValidatorException((Throwable) ioe4);
                        }
                    } catch (IOException e) {
                        ioe = e;
                        out = out2;
                        try {
                            CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Unable to determine revocation status due to network error", ioe, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                            throw certPathValidatorException;
                        } catch (Throwable th) {
                            ioe = th;
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException ioe5) {
                                    IOException iOException3 = ioe5;
                                    throw ioe5;
                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException ioe6) {
                                    IOException iOException4 = ioe6;
                                    throw ioe6;
                                }
                            }
                            throw ioe;
                        }
                    } catch (Throwable th2) {
                        ioe = th2;
                        out = out2;
                        if (in != null) {
                        }
                        if (out != null) {
                        }
                        throw ioe;
                    }
                } catch (IOException e2) {
                    ioe = e2;
                    CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("Unable to determine revocation status due to network error", ioe, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                    throw certPathValidatorException2;
                }
            } catch (IOException e3) {
                ioe = e3;
                throw new CertPathValidatorException("Exception while encoding OCSPRequest", ioe);
            }
        } catch (IOException e4) {
            ioe = e4;
            List<CertId> list2 = certIds;
            List<Extension> list3 = extensions;
            throw new CertPathValidatorException("Exception while encoding OCSPRequest", ioe);
        }
    }

    public static URI getResponderURI(X509Certificate cert) {
        try {
            return getResponderURI(X509CertImpl.toImpl(cert));
        } catch (CertificateException e) {
            return null;
        }
    }

    static URI getResponderURI(X509CertImpl certImpl) {
        AuthorityInfoAccessExtension aia = certImpl.getAuthorityInfoAccessExtension();
        if (aia == null) {
            return null;
        }
        for (AccessDescription description : aia.getAccessDescriptions()) {
            if (description.getAccessMethod().equals((Object) AccessDescription.Ad_OCSP_Id)) {
                GeneralName generalName = description.getAccessLocation();
                if (generalName.getType() == 6) {
                    return ((URIName) generalName.getName()).getURI();
                }
            }
        }
        return null;
    }
}
