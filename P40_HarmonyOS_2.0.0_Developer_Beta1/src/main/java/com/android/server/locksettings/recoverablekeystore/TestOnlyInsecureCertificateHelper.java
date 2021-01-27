package com.android.server.locksettings.recoverablekeystore;

import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.security.keystore.recovery.TrustedRootCertificates;
import android.util.Log;
import android.util.Pair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

public class TestOnlyInsecureCertificateHelper {
    private static final String TAG = "TestCertHelper";

    public X509Certificate getRootCertificate(String rootCertificateAlias) throws RemoteException {
        String rootCertificateAlias2 = getDefaultCertificateAliasIfEmpty(rootCertificateAlias);
        if (isTestOnlyCertificateAlias(rootCertificateAlias2)) {
            return TrustedRootCertificates.getTestOnlyInsecureCertificate();
        }
        X509Certificate rootCertificate = TrustedRootCertificates.getRootCertificate(rootCertificateAlias2);
        if (rootCertificate != null) {
            return rootCertificate;
        }
        throw new ServiceSpecificException(28, "The provided root certificate alias is invalid");
    }

    public String getDefaultCertificateAliasIfEmpty(String rootCertificateAlias) {
        if (rootCertificateAlias != null && !rootCertificateAlias.isEmpty()) {
            return rootCertificateAlias;
        }
        Log.e(TAG, "rootCertificateAlias is null or empty - use secure default value");
        return "GoogleCloudKeyVaultServiceV1";
    }

    public boolean isTestOnlyCertificateAlias(String rootCertificateAlias) {
        return "TEST_ONLY_INSECURE_CERTIFICATE_ALIAS".equals(rootCertificateAlias);
    }

    public boolean isValidRootCertificateAlias(String rootCertificateAlias) {
        return TrustedRootCertificates.getRootCertificates().containsKey(rootCertificateAlias) || isTestOnlyCertificateAlias(rootCertificateAlias);
    }

    public boolean doesCredentialSupportInsecureMode(int credentialType, byte[] credential) {
        if (credential == null || credentialType != 2) {
            return false;
        }
        byte[] insecurePasswordPrefixBytes = "INSECURE_PSWD_".getBytes();
        if (credential.length < insecurePasswordPrefixBytes.length) {
            return false;
        }
        for (int i = 0; i < insecurePasswordPrefixBytes.length; i++) {
            if (credential[i] != insecurePasswordPrefixBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public Map<String, Pair<SecretKey, byte[]>> keepOnlyWhitelistedInsecureKeys(Map<String, Pair<SecretKey, byte[]>> rawKeys) {
        if (rawKeys == null) {
            return null;
        }
        Map<String, Pair<SecretKey, byte[]>> filteredKeys = new HashMap<>();
        for (Map.Entry<String, Pair<SecretKey, byte[]>> entry : rawKeys.entrySet()) {
            String alias = entry.getKey();
            if (alias != null && alias.startsWith("INSECURE_KEY_ALIAS_KEY_MATERIAL_IS_NOT_PROTECTED_")) {
                filteredKeys.put(entry.getKey(), Pair.create((SecretKey) entry.getValue().first, (byte[]) entry.getValue().second));
                Log.d(TAG, "adding key with insecure alias " + alias + " to the recovery snapshot");
            }
        }
        return filteredKeys;
    }
}
