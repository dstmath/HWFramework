package com.huawei.server.security.securityprofile;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class CertificateVerifier {
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final String ROOT_PUB = "MIIF+TCCA+GgAwIBAgIIe3K3vlLOwNMwDQYJKoZIhvcNAQELBQAwUDELMAkGA1UEBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UECwwKSHVhd2VpIENCRzEbMBkGA1UEAwwSSHVhd2VpIENCRyBSb290IENBMB4XDTE4MDEzMDA3NDUwNFoXDTM4MDEyNTA3NDUwNFowXzELMAkGA1UEBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UECwwKSHVhd2VpIENCRzEqMCgGA1UEAwwhSHVhd2VpIENCRyBEZXZlbG9wZXIgUmVsYXRpb25zIENBMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA9kymK244nkScy3sQ5FJueGg9qRvmyvXV4Nv0iH8ZjgoTBB+LXdipdblwW8b41W2yB0V9Z0h71qkm3iX/3A4eCXEimpbKXP5NbrBynrinKAHNKKK2uWo+fnB2IpxHG+EU8W3g4VghbvCq7sTIC4nT1s2ngDbyVwRZ0bY7IRyY6q1GzKBUuRZ9l21TQy9iNEuSDIDKp1j7lpfHO4Lp6gO1gMXAK2gWLSgOHc77WZDVh2b7Ej/XNpjXpzq8zMvLoEicbHCMiOgDXg1Nky4yzn8ojjkB85LrYCQhF8+qeXSUxdQGOnU5Ruc+6mxvxs9FiSoYKYeplopjgcEkMglOTSbkqRLpjxvmgBuozQKXQm+1e75fIuzWam8X2q/jreg8J+28GmWbA6sdMepvxr6Ty57jHIHTQOqOhVLdP3GQjwFffiUK5T95HOim1BIoBVQGhbNLGaR7OT7hWtUWowG6O7+cQuKoUk9wqhs7jaUejgtrGiJJEFk+9UmkwRsgEt2HTcOl4dFZWJShPgi6mPdmk0uYIoZRR8BBnEwasdY5B7RkFKocB49J0FHA9Dz0ltsMZD8DktFlOZ4N6VNOEiV9uo3wGcx53hoNfTs5meO+iLtUuRXHpbCZuUMkLe3JQ1wjGSDRX5GTy0R2wvXCrf5Ln4Jo4IhyaJllA7mPvuEv+TbA2XUCAwEAAaOBxzCBxDAfBgNVHSMEGDAWgBSqxNN5R+huI2vv8KlsInM9ehlpoTAdBgNVHQ4EFgQUa6kw6pc/k86KdBPmR3NFv1/Jd3gwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwYQYDVR0fBFowWDBWoFSgUoZQaHR0cDovL2Nwa2ktY2F3ZWIuaHVhd2VpLmNvbS9jcGtpL3NlcnZsZXQvY3JsRmlsZURvd24uY3JsP2NlcnR5cGU9MSYvcm9vdGNybC5jcmwwDQYJKoZIhvcNAQELBQADggIBADiSGXD75951e+NJGaKxtsPpys2PsoL4EYUEdalI23yUMIdrCEU0bV+oeICB/0SZt2DizQa2zNkKLCtlA2B9qyNsj47BgX3LALFL3FtpfLxcTr0CUjhhyehu/JFORkKkieCeKdCbrVGjDs1a6WspldtTPfvdLwc50LEEAMoAWz81EvuNwTaQ8vbyO32cnfNmnrT74jhj+mSbZ6B5ECcMPVvcieOVlwHz5a7W5AAhHYNt5mphgsBToCPI6O6E0jqTcqBpw1gcheKu+jVkB/HPtz9YgRROTRv1DPhpiiLmtYoUn6iGmwGOT9GA84UcMZqWqJLtksqVJmRQleuH6qwnu7p7/nhITvE1+WL+28vCrvabmsmmVfkms3cA7x8GguY08rC+BPVIbTRhVGg6w3EszYL1OuaO80TU/nc5RouxgyR9HT/TkFmhcyskcOVQwA37uVArD891iw0cO1Dl1HT1r6/0dEZQNBmCQlBj/RumfpcTUDrGbRFCKeO74qnNzlMhW4/iuZe/ZLcSyFrHvYawy/z+vmIvwtSrUAbKSuD+9qevCNC+wiVoaOQy5RyCxiTs08Za4tRNFpOUCqALv6kLk9xjdGrBk27llN2i5vzb1b4wTVYuXEf1vZss3Tjml9wH2jE1vOIxtiHqMtKnOVWLxDSr8v+ruO7UXu1EVfbeNyXS";
    private static final String TAG = "SecurityProfileService";
    private KeyStore mTrustStore;

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x003b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0040, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0041, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0044, code lost:
        throw r3;
     */
    public CertificateVerifier() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(ROOT_PUB));
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            this.mTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            this.mTrustStore.load(null, null);
            this.mTrustStore.setCertificateEntry("root", factory.generateCertificate(bais));
            bais.close();
        } catch (CertificateException e) {
            Log.e(TAG, "CertificateException: " + e.getMessage());
        } catch (NoSuchAlgorithmException e2) {
            Log.e(TAG, "NoSuchAlgorithmException: " + e2.getMessage());
        } catch (KeyStoreException e3) {
            Log.e(TAG, "KeyStoreException: " + e3.getMessage());
        } catch (IOException e4) {
            Log.e(TAG, "IOException: " + e4.getMessage());
        }
    }

    public boolean verifyCertificateChain(List<Certificate> certChain, Date signingDate) {
        try {
            CertPath certPath = CertificateFactory.getInstance("X.509").generateCertPath(new ArrayList<>(certChain));
            CertPathValidator validator = CertPathValidator.getInstance("PKIX");
            PKIXParameters params = new PKIXParameters(this.mTrustStore);
            params.setRevocationEnabled(false);
            params.setDate(signingDate);
            params.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters()));
            CertPathValidatorResult validatorResult = validator.validate(certPath, params);
            if (!(validatorResult instanceof PKIXCertPathValidatorResult)) {
                Log.e(TAG, "validator result not target type");
                return false;
            }
            PKIXCertPathValidatorResult pkixValidatorResult = (PKIXCertPathValidatorResult) validatorResult;
            if (params.getTrustAnchors().contains(pkixValidatorResult.getTrustAnchor())) {
                return true;
            }
            Log.e(TAG, "anchor is not trusted: " + Base64.getEncoder().encodeToString(pkixValidatorResult.getTrustAnchor().getTrustedCert().getEncoded()));
            return false;
        } catch (CertPathValidatorException e) {
            Log.e(TAG, "CertPathValidatorException: " + e.getMessage());
            return false;
        } catch (InvalidAlgorithmParameterException e2) {
            Log.e(TAG, "InvalidAlgorithmParameterException: " + e2.getMessage());
            return false;
        } catch (CertificateEncodingException e3) {
            Log.e(TAG, "CertificateEncodingException: " + e3.getMessage());
            return false;
        } catch (CertificateException e4) {
            Log.e(TAG, "CertificateException: " + e4.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e5) {
            Log.e(TAG, "NoSuchAlgorithmException: " + e5.getMessage());
            return false;
        } catch (KeyStoreException e6) {
            Log.e(TAG, "KeyStoreException: " + e6.getMessage());
            return false;
        }
    }
}
