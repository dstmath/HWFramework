package com.huawei.security.keystore;

import android.util.Log;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keystore.HwKeyProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class HwUniversalKeyStoreSpi extends KeyStoreSpi {
    private static final int EC_DEFAULT_KEY_SIZE = 256;
    private static final String TAG = "HwUniversalKeyStore";
    private HwKeystoreManager mKeyStore;
    private int mUid = -1;

    private static X509Certificate toCertificate(byte[] bytes) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            Log.w(TAG, "Couldn't parse certificate in keystore" + e.getMessage());
            return null;
        }
    }

    /* JADX DEBUG: Type inference failed for r1v4. Raw type applied. Possible types: java.util.Collection<? extends java.security.cert.Certificate>, java.util.Collection<java.security.cert.X509Certificate> */
    private static Collection<X509Certificate> toCertificates(byte[] bytes) {
        try {
            return CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            Log.w(TAG, "Couldn't parse certificates in keystore" + e.getMessage());
            return new ArrayList(0);
        }
    }

    private static HwKeyStoreX509Certificate wrapIntoKeyStoreCertificate(String privateKeyAlias, int uid, X509Certificate certificate) {
        if (certificate != null) {
            return new HwKeyStoreX509Certificate(privateKeyAlias, uid, certificate);
        }
        return null;
    }

    @Override // java.security.KeyStoreSpi
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (isPrivateKeyEntry(alias)) {
            return HwUniversalKeyStoreProvider.loadHwKeyStorePrivateKeyFromKeystore(this.mKeyStore, HwCredentials.USER_PRIVATE_KEY + alias, this.mUid);
        } else if (isSecretKeyEntry(alias)) {
            return HwUniversalKeyStoreProvider.loadHwKeyStoreSecretKeyFromKeystore(this.mKeyStore, HwCredentials.USER_SECRET_KEY + alias, this.mUid);
        } else if (isExternalPublicKeyEntry(alias)) {
            return HwUniversalKeyStoreProvider.loadHwKeyStorePublicKeyFromKeystore(this.mKeyStore, HwCredentials.USER_EXTERNAL_PUBLIC_KEY + alias, this.mUid);
        } else {
            Log.e(TAG, "engineGetKey can not find alias:" + alias + ", ERROR!");
            return null;
        }
    }

    @Override // java.security.KeyStoreSpi
    public Certificate[] engineGetCertificateChain(String alias) {
        Certificate[] retNull = new Certificate[0];
        if (alias == null) {
            Log.e(TAG, "engineGetCertificateChain alias == null");
            throw new NullPointerException("alias == null");
        } else if (this.mKeyStore == null) {
            Log.e(TAG, "get HwKeymaster mKeyStore == null");
            return retNull;
        } else {
            Log.i(TAG, "invoke get certificate chain!");
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            HwExportResult result = hwKeystoreManager.get(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid);
            if (result == null || result.resultCode != 1) {
                Log.e(TAG, "invoke get failed!");
                return retNull;
            }
            byte[] encodedCerts = result.exportData;
            if (encodedCerts == null) {
                Log.e(TAG, "encodedCerts is null!");
                return retNull;
            }
            Collection<X509Certificate> x509Chain = toCertificates(encodedCerts);
            if (x509Chain.size() == 0) {
                Log.e(TAG, "x509Chain is empty!");
                return retNull;
            }
            Certificate[] caList = (Certificate[]) x509Chain.toArray(new Certificate[x509Chain.size()]);
            caList[0] = getKeyStoreCertificate(alias, (X509Certificate) caList[0]);
            return caList;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0058, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005d, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005e, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0061, code lost:
        throw r6;
     */
    @Override // java.security.KeyStoreSpi
    public Certificate engineGetCertificate(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager == null) {
                Log.e(TAG, "get HwKeymaster mKeyStore == null");
                return null;
            }
            HwExportResult result = hwKeystoreManager.get(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid);
            if (result == null) {
                Log.e(TAG, "get HwKeymaster mKeyStore == null");
                return null;
            }
            byte[] encodedCertChain = result.exportData;
            if (result.resultCode == 1 && encodedCertChain != null) {
                try {
                    InputStream input = new ByteArrayInputStream(encodedCertChain);
                    Certificate keyStoreCertificate = getKeyStoreCertificate(alias, (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificates(input).iterator().next());
                    input.close();
                    return keyStoreCertificate;
                } catch (CertificateException e) {
                    Log.e(TAG, "Couldn't get certificate!" + e.getMessage());
                } catch (IOException e2) {
                    Log.e(TAG, "IO exception!");
                }
            }
            return null;
        }
        throw new NullPointerException("alias == null");
    }

    @Override // java.security.KeyStoreSpi
    public Date engineGetCreationDate(String alias) {
        return null;
    }

    @Override // java.security.KeyStoreSpi
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        throw new UnsupportedOperationException("Stub!");
    }

    /* JADX INFO: Multiple debug info for r4v4 byte[]: [D('i' int), D('chainBytes' byte[])] */
    @Override // java.security.KeyStoreSpi
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        if (key != null) {
            int errCode = setSm2ExternalPublicKeyEntry(alias, key, chain);
            if (errCode != 1) {
                throw new KeyStoreException("Failed to set external public key!", HwKeystoreManager.getKeyStoreException(errCode));
            }
        } else if (chain == null) {
            throw new KeyStoreException("Certificate chain is null.");
        } else if (alias == null || alias.isEmpty()) {
            throw new NullPointerException("Alias is null.");
        } else if (chain.length < 2) {
            throw new UnsupportedOperationException("Length of cert chain is less than 2");
        } else if (isPrivateKeyEntry(alias)) {
            int chainBytesLength = 0;
            try {
                int numOfChain = chain.length;
                for (Certificate certificate : chain) {
                    chainBytesLength += certificate.getEncoded().length;
                }
                byte[] chainBytes = new byte[chainBytesLength];
                int countLength = 0;
                for (int j = 0; j < numOfChain; j++) {
                    System.arraycopy(chain[j].getEncoded(), 0, chainBytes, countLength, chain[j].getEncoded().length);
                    countLength += chain[j].getEncoded().length;
                }
                HwKeymasterBlob blob = new HwKeymasterBlob(chainBytes);
                HwKeystoreManager hwKeystoreManager = this.mKeyStore;
                if (hwKeystoreManager != null) {
                    int insertErrorCode = hwKeystoreManager.set(HwCredentials.CERTIFICATE_CHAIN + alias, blob, this.mUid);
                    if (insertErrorCode != 1) {
                        throw new ProviderException("Failed to set certificate chain", HwKeystoreManager.getKeyStoreException(insertErrorCode));
                    }
                    return;
                }
                throw new KeyStoreException("mKeyStore is null.");
            } catch (CertificateEncodingException e) {
                throw new ProviderException("Failed to get certificate encoded: " + e.getMessage());
            }
        } else {
            throw new KeyStoreException("Entry not exists.");
        }
    }

    @Override // java.security.KeyStoreSpi
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // java.security.KeyStoreSpi
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (!HwCredentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid)) {
            throw new KeyStoreException("Failed to delete entry");
        }
    }

    private Set<String> getUniqueAliases() {
        HwKeystoreManager hwKeystoreManager = this.mKeyStore;
        if (hwKeystoreManager == null) {
            Log.e(TAG, "get HwKeymaster mKeyStore == null");
            return new HashSet(0);
        }
        String[] rawAliases = hwKeystoreManager.list(BuildConfig.FLAVOR, this.mUid);
        if (rawAliases == null) {
            return new HashSet(0);
        }
        Set<String> aliases = new HashSet<>(rawAliases.length);
        for (String alias : rawAliases) {
            int index = alias.indexOf(95);
            if (index == -1 || alias.length() <= index) {
                Log.e(TAG, "invalid alias");
            } else {
                aliases.add(alias.substring(index + 1));
            }
        }
        return aliases;
    }

    @Override // java.security.KeyStoreSpi
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(getUniqueAliases());
    }

    @Override // java.security.KeyStoreSpi
    public boolean engineContainsAlias(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager == null) {
                return false;
            }
            if (!hwKeystoreManager.contains(HwCredentials.USER_PRIVATE_KEY + alias, this.mUid)) {
                HwKeystoreManager hwKeystoreManager2 = this.mKeyStore;
                if (!hwKeystoreManager2.contains(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid)) {
                    HwKeystoreManager hwKeystoreManager3 = this.mKeyStore;
                    if (!hwKeystoreManager3.contains(HwCredentials.USER_SECRET_KEY + alias, this.mUid)) {
                        HwKeystoreManager hwKeystoreManager4 = this.mKeyStore;
                        if (!hwKeystoreManager4.contains(HwCredentials.USER_EXTERNAL_PUBLIC_KEY + alias, this.mUid)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        throw new NullPointerException("alias == null");
    }

    @Override // java.security.KeyStoreSpi
    public int engineSize() {
        return getUniqueAliases().size();
    }

    @Override // java.security.KeyStoreSpi
    public boolean engineIsKeyEntry(String alias) {
        return isPrivateKeyEntry(alias) || isSecretKeyEntry(alias);
    }

    @Override // java.security.KeyStoreSpi
    public boolean engineIsCertificateEntry(String alias) {
        return !isPrivateKeyEntry(alias) && isCertificateEntry(alias);
    }

    @Override // java.security.KeyStoreSpi
    public String engineGetCertificateAlias(Certificate cert) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // java.security.KeyStoreSpi
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException("Can not serialize HwUniversalKeyStore to OutputStream");
    }

    @Override // java.security.KeyStoreSpi
    public void engineStore(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        super.engineStore(param);
    }

    @Override // java.security.KeyStoreSpi
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (stream != null) {
            throw new IllegalArgumentException("InputStream not supported");
        } else if (password == null) {
            this.mKeyStore = HwKeystoreManager.getInstance();
            this.mUid = -1;
        } else {
            throw new IllegalArgumentException("password not supported");
        }
    }

    @Override // java.security.KeyStoreSpi
    public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            this.mKeyStore = HwKeystoreManager.getInstance();
            this.mUid = -1;
            return;
        }
        throw new IllegalArgumentException("Unsupported param type: " + param.getClass());
    }

    @Override // java.security.KeyStoreSpi
    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return super.engineGetEntry(alias, protParam);
    }

    @Override // java.security.KeyStoreSpi
    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (alias == null || alias.isEmpty()) {
            throw new NullPointerException("alias is null.");
        }
        String entryAlias = HwCredentials.USER_PRIVATE_KEY + alias;
        if (!isPrivateKeyEntry(alias)) {
            throw new KeyStoreException("Entry not exists.");
        } else if (protParam instanceof AdditionalKeyProtection) {
            AdditionalKeyProtection additionalKeyProtection = (AdditionalKeyProtection) protParam;
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager != null) {
                int errorCode = hwKeystoreManager.setKeyProtection(entryAlias, constructKeyGenerationArguments(additionalKeyProtection));
                if (errorCode != 1) {
                    throw new ProviderException("Failed to set entry", HwKeystoreManager.getKeyStoreException(errorCode));
                }
                return;
            }
            throw new ProviderException("get HwKeymaster mKeyStore == null");
        } else {
            throw new ProviderException("protParam is not an instance of AdditionalKeyProtection", new HwUniversalKeyStoreException(4, "System error"));
        }
    }

    private HwKeymasterArguments constructKeyGenerationArguments(AdditionalKeyProtection protParam) {
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addUnsignedInt(HwKeymasterDefs.KM_TAG_ADD_USER_AUTH_TYPE, (long) protParam.getBiometricType());
        if (protParam.isTemplateBound()) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_ADD_BIND_TEMPLATE);
        }
        return args;
    }

    @Override // java.security.KeyStoreSpi
    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return super.engineEntryInstanceOf(alias, entryClass);
    }

    private boolean isPrivateKeyEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager == null) {
                return false;
            }
            return hwKeystoreManager.contains(HwCredentials.USER_PRIVATE_KEY + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isSecretKeyEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager == null) {
                Log.e(TAG, "isSecretKeyEntry mKeyStore is null");
                return false;
            }
            return hwKeystoreManager.contains(HwCredentials.USER_SECRET_KEY + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isExternalPublicKeyEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager == null) {
                Log.e(TAG, "isExternalPublicKeyEntry mKeyStore is null");
                return false;
            }
            return hwKeystoreManager.contains(HwCredentials.USER_EXTERNAL_PUBLIC_KEY + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isCertificateEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (hwKeystoreManager == null) {
                return false;
            }
            return hwKeystoreManager.contains(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private Certificate getCertificateForPrivateKeyEntry(String alias, byte[] encodedCert) {
        X509Certificate cert = toCertificate(encodedCert);
        if (cert != null) {
            return getKeyStoreCertificate(alias, cert);
        }
        Log.w(TAG, "getCertificateForPrivateKeyEntry cert is null");
        return null;
    }

    private Certificate getKeyStoreCertificate(String alias, X509Certificate cert) {
        String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + alias;
        HwKeystoreManager hwKeystoreManager = this.mKeyStore;
        if (hwKeystoreManager == null) {
            return null;
        }
        if (hwKeystoreManager.contains(privateKeyAlias, this.mUid)) {
            return wrapIntoKeyStoreCertificate(privateKeyAlias, this.mUid, cert);
        }
        return cert;
    }

    private Certificate getCertificateForTrustedCertificateEntry(byte[] encodedCert) {
        return toCertificate(encodedCert);
    }

    /* access modifiers changed from: protected */
    public HwKeystoreManager getKeyStoreManager() {
        return this.mKeyStore;
    }

    /* access modifiers changed from: protected */
    public int getUid() {
        return this.mUid;
    }

    private int setSm2ExternalPublicKeyEntry(String alias, byte[] key, Certificate[] chain) {
        if (chain != null) {
            Log.e(TAG, "set Sm2 External Public Key:chain is not null!");
            return 4;
        } else if (key == null || key.length == 0) {
            Log.e(TAG, "set Sm2 External Public Key:key is null!");
            return 4;
        } else if (alias == null || alias.isEmpty()) {
            Log.e(TAG, "set Sm2 External Public Key:Alias is null!");
            return 4;
        } else if (this.mKeyStore == null) {
            Log.e(TAG, "set Sm2 External Public Key:mKeyStore is null!");
            return 4;
        } else {
            String externalSm2PubKeyAlias = HwCredentials.USER_EXTERNAL_PUBLIC_KEY + alias;
            HwKeymasterBlob blob = new HwKeymasterBlob(key);
            HwCredentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid);
            return this.mKeyStore.importKey(externalSm2PubKeyAlias, constructImportKeyArgumentsForSm2(), blob);
        }
    }

    private HwKeymasterArguments constructImportKeyArgumentsForSm2() {
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addUnsignedInt(HwKeymasterDefs.KM_TAG_KEY_SIZE, 256);
        args.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, 3);
        args.addBoolean(HwKeymasterDefs.KM_TAG_IS_FROM_GM);
        args.addEnums(HwKeymasterDefs.KM_TAG_PURPOSE, HwKeyProperties.Purpose.allToKeymaster(9));
        args.addEnums(HwKeymasterDefs.KM_TAG_DIGEST, HwKeyProperties.Digest.allToKeymaster(new String[]{HwKeyProperties.DIGEST_SM3}));
        return args;
    }

    /* access modifiers changed from: package-private */
    public static class HwKeyStoreX509Certificate extends HwDelegatingX509Certificate {
        private static final long serialVersionUID = 1;
        private final String mPrivateKeyAlias;
        private final int mPrivateKeyUid;

        HwKeyStoreX509Certificate(String privateKeyAlias, int privateKeyUid, X509Certificate delegate) {
            super(delegate);
            this.mPrivateKeyAlias = privateKeyAlias;
            this.mPrivateKeyUid = privateKeyUid;
        }

        @Override // com.huawei.security.keystore.HwDelegatingX509Certificate, java.security.cert.Certificate
        public PublicKey getPublicKey() {
            PublicKey original = super.getPublicKey();
            return HwUniversalKeyStoreProvider.getHwKeyStorePublicKey(this.mPrivateKeyAlias, this.mPrivateKeyUid, original.getAlgorithm(), original.getEncoded());
        }
    }
}
