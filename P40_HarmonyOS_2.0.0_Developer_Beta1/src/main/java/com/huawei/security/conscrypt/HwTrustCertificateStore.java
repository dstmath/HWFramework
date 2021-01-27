package com.huawei.security.conscrypt;

import android.util.Log;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;

public class HwTrustCertificateStore {
    private static final String[] ROOT_CERT = {"-----BEGIN CERTIFICATE-----\nMIICGjCCAaGgAwIBAgIIShhpn519jNAwCgYIKoZIzj0EAwMwUzELMAkGA1UEBhMC\nQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UECwwKSHVhd2VpIENCRzEeMBwGA1UE\nAwwVSHVhd2VpIENCRyBSb290IENBIEcyMB4XDTIwMDMxNjAzMDQzOVoXDTQ5MDMx\nNjAzMDQzOVowUzELMAkGA1UEBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UE\nCwwKSHVhd2VpIENCRzEeMBwGA1UEAwwVSHVhd2VpIENCRyBSb290IENBIEcyMHYw\nEAYHKoZIzj0CAQYFK4EEACIDYgAEWidkGnDSOw3/HE2y2GHl+fpWBIa5S+IlnNrs\nGUvwC1I2QWvtqCHWmwFlFK95zKXiM8s9yV3VVXh7ivN8ZJO3SC5N1TCrvB2lpHMB\nwcz4DA0kgHCMm/wDec6kOHx1xvCRo0IwQDAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0T\nAQH/BAUwAwEB/zAdBgNVHQ4EFgQUo45a9Vq8cYwqaiVyfkiS4pLcIAAwCgYIKoZI\nzj0EAwMDZwAwZAIwMypeB7P0IbY7c6gpWcClhRznOJFj8uavrNu2PIoz9KIqr3jn\nBlBHJs0myI7ntYpEAjBbm8eDMZY5zq5iMZUC6H7UzYSix4Uy1YlsLVV738PtKP9h\nFTjgDHctXJlC5L7+ZDY=\n-----END CERTIFICATE-----\n", "-----BEGIN CERTIFICATE-----\nMIIFZDCCA0ygAwIBAgIIYsLLTehAXpYwDQYJKoZIhvcNAQELBQAwUDELMAkGA1UE\nBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UECwwKSHVhd2VpIENCRzEbMBkG\nA1UEAwwSSHVhd2VpIENCRyBSb290IENBMB4XDTE3MDgyMTEwNTYyN1oXDTQyMDgx\nNTEwNTYyN1owUDELMAkGA1UEBhMCQ04xDzANBgNVBAoMBkh1YXdlaTETMBEGA1UE\nCwwKSHVhd2VpIENCRzEbMBkGA1UEAwwSSHVhd2VpIENCRyBSb290IENBMIICIjAN\nBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1OyKm3Ig/6eibB7Uz2o93UqGk2M7\n84WdfF8mvffvu218d61G5M3Px54E3kefUTk5Ky1ywHvw7Rp9KDuYv7ktaHkk+yr5\n9Ihseu3a7iM/C6SnMSGt+LfB/Bcob9Abw95EigXQ4yQddX9hbNrin3AwZw8wMjEI\nSYYDo5GuYDL0NbAiYg2Y5GpfYIqRzoi6GqDz+evLrsl20kJeCEPgJZN4Jg00Iq9k\n++EKOZ5Jc/Zx22ZUgKpdwKABkvzshEgG6WWUPB+gosOiLv++inu/9blDpEzQZhjZ\n9WVHpURHDK1YlCvubVAMhDpnbqNHZ0AxlPletdoyugrH/OLKl5inhMXNj3Re7Hl8\nWsBWLUKp6sXFf0dvSFzqnr2jkhicS+K2IYZnjghC9cOBRO8fnkonh0EBt0evjUIK\nr5ClbCKioBX8JU+d4ldtWOpp2FlxeFTLreDJ5ZBU4//bQpTwYMt7gwMK+MO5Wtok\nUx3UF98Z6GdUgbl6nBjBe82c7oIQXhHGHPnURQO7DDPgyVnNOnTPIkmiHJh/e3vk\nVhiZNHFCCLTip6GoJVrLxwb9i4q+d0thw4doxVJ5NB9OfDMV64/ybJgpf7m3Ld2y\nE0gsf1prrRlDFDXjlYyqqpf1l9Y0u3ctXo7UpXMgbyDEpUQhq3a7txZQO/17luTD\noA6Tz1ADavvBwHkCAwEAAaNCMEAwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQF\nMAMBAf8wHQYDVR0OBBYEFKrE03lH6G4ja+/wqWwicz16GWmhMA0GCSqGSIb3DQEB\nCwUAA4ICAQC1d3TMB+VHZdGrWJbfaBShFNiCTN/MceSHOpzBn6JumQP4N7mxCOwd\nRSsGKQxV2NPH7LTXWNhUvUw5Sek96FWx/+Oa7jsj3WNAVtmS3zKpCQ5iGb08WIRO\ncFnx3oUQ5rcO8r/lUk7Q2cN0E+rF4xsdQrH9k2cd3kAXZXBjfxfKPJTdPy1XnZR/\nh8H5EwEK5DWjSzK1wKd3G/Fxdm3E23pcr4FZgdYdOlFSiqW2TJ3Qe6lF4GOKOOyd\nWHkpu54ieTsqoYcuMKnKMjT2SLNNgv9Gu5ipaG8Olz6g9C7Htp943lmK/1Vtnhgg\npL3rDTsFX/+ehk7OtxuNzRMD9lXUtEfok7f8XB0dcL4ZjnEhDmp5QZqC1kMubHQt\nQnTauEiv0YkSGOwJAUZpK1PIff5GgxXYfaHfBC6Op4q02ppl5Q3URl7XIjYLjvs9\nt4S9xPe8tb6416V2fe1dZ62vOXMMKHkZjVihh+IceYpJYHuyfKoYJyahLOQXZykG\nK5iPAEEtq3HPfMVF43RKHOwfhrAH5KwelUA/0EkcR4Gzth1MKEqojdnYNemkkSy7\naNPPT4LEm5R7sV6vG1CjwbgvQrWCgc4nMb8ngdfnVF7Ydqjqi9SAqUzIk4+Uf0ZY\n+6RY5IcHdCaiPaWIE1xURQ8B0DRUURsQwXdjZhgLN/DKJpCl5aCCxg==\n-----END CERTIFICATE-----"};
    private static final String TAG = "HwTrustCertificateStore";
    private HwKeystoreManager mKeyStore = HwKeystoreManager.getInstance();

    /* access modifiers changed from: private */
    public interface CertSelector {
        boolean match(X509Certificate x509Certificate, X509Certificate x509Certificate2);
    }

    public HwTrustCertificateStore(HwKeystoreManager keystore) {
        this.mKeyStore = keystore;
    }

    private static X509Certificate[] toCertificates(List<byte[]> bytes) {
        if (bytes == null || bytes.size() == 0) {
            Log.e(TAG, "Invalid param.");
            return null;
        }
        try {
            Log.d(TAG, "toCertificates bytes.size:" + bytes.size());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate[] certs = new X509Certificate[(bytes.size() + 2)];
            int i = 0;
            do {
                byte[] data = bytes.get(i);
                if (data != null) {
                    if (data.length != 0) {
                        certs[i] = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(data));
                        i++;
                    }
                }
                Log.e(TAG, "data is null");
                return null;
            } while (i < bytes.size());
            for (int index = 0; index < ROOT_CERT.length; index++) {
                certs[i] = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(ROOT_CERT[index].getBytes("UTF-8")));
                i++;
            }
            return certs;
        } catch (CertificateException e) {
            Log.w(TAG, "Couldn't parse certificates in keystore CertificateException", e);
            return null;
        } catch (UnsupportedEncodingException e2) {
            Log.e(TAG, "string to byte failed!", e2);
            return null;
        }
    }

    private X509Certificate[] loadTrustCert() {
        if (this.mKeyStore == null) {
            Log.e(TAG, "mKeyStore is null!");
            return null;
        }
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        if (this.mKeyStore.exportTrustCert(outChain) != 1) {
            Log.e(TAG, "exportTrustCert failed!");
            return null;
        }
        List<byte[]> certsByte = outChain.getCertificates();
        if (certsByte != null) {
            return toCertificates(certsByte);
        }
        Log.e(TAG, "findCert failed!");
        return null;
    }

    public X509Certificate getTrustAnchor(X509Certificate c) {
        X509Certificate trustCert = (X509Certificate) findCert(c, c.getSubjectX500Principal(), new GetTrustAnchorCertSelector(), X509Certificate.class);
        if (trustCert != null) {
            return trustCert;
        }
        Log.e(TAG, "trust cert is null!");
        return null;
    }

    public X509Certificate findIssuer(X509Certificate c) {
        X509Certificate cert = (X509Certificate) findCert(c, c.getIssuerX500Principal(), new FindIssuerCertSelector(), X509Certificate.class);
        if (cert != null) {
            return cert;
        }
        return null;
    }

    private <T> T findCert(X509Certificate c, X500Principal subject, CertSelector selector, Class<T> desiredReturnType) {
        if (this.mKeyStore == null) {
            Log.e(TAG, "mKeyStore is null!");
            return null;
        }
        X509Certificate[] certList = loadTrustCert();
        if (certList == null) {
            Log.e(TAG, "Load trust device cert failed!");
            return null;
        }
        for (X509Certificate x509Certificate : certList) {
            T t = (T) x509Certificate;
            boolean match = selector.match(t, c);
            boolean equals = subject != null ? subject.getName().equals(t.getSubjectX500Principal().getName()) : false;
            if (!match || !equals) {
                Log.d(TAG, "exportTrustCert match:" + match + " equals:" + equals);
            }
            if (match && equals) {
                Log.d(TAG, "findCert find the trust cert!");
                if (desiredReturnType == X509Certificate.class) {
                    return t;
                }
                if (desiredReturnType == Boolean.class) {
                    return (T) Boolean.TRUE;
                }
                throw new AssertionError();
            }
        }
        Log.e(TAG, "Find cert failed!");
        return null;
    }

    static class GetTrustAnchorCertSelector implements CertSelector {
        GetTrustAnchorCertSelector() {
        }

        @Override // com.huawei.security.conscrypt.HwTrustCertificateStore.CertSelector
        public boolean match(X509Certificate ca, X509Certificate c) {
            return ca.getPublicKey().equals(c.getPublicKey());
        }
    }

    static class FindIssuerCertSelector implements CertSelector {
        FindIssuerCertSelector() {
        }

        @Override // com.huawei.security.conscrypt.HwTrustCertificateStore.CertSelector
        public boolean match(X509Certificate ca, X509Certificate c) {
            try {
                c.verify(ca.getPublicKey());
                return true;
            } catch (CertificateException e) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail CertificateException!");
                return false;
            } catch (NoSuchAlgorithmException e2) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail NoSuchAlgorithmException!");
                return false;
            } catch (InvalidKeyException e3) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail InvalidKeyException!");
                return false;
            } catch (NoSuchProviderException e4) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail NoSuchProviderException!");
                return false;
            } catch (SignatureException e5) {
                Log.e(HwTrustCertificateStore.TAG, "FindIssuerCertSelector match fail SignatureException!");
                return false;
            }
        }
    }
}
