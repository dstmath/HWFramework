package com.huawei.security.keystore;

import android.util.Log;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterDefs;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class HwUniversalKeyStoreSpi extends KeyStoreSpi {
    public static final String TAG = "HwUniversalKeyStore";
    private HwKeystoreManager mKeyStore;
    private int mUid = -1;

    static class HwKeyStoreX509Certificate extends HwDelegatingX509Certificate {
        private static final long serialVersionUID = 1;
        private final String mPrivateKeyAlias;
        private final int mPrivateKeyUid;

        HwKeyStoreX509Certificate(String privateKeyAlias, int privateKeyUid, X509Certificate delegate) {
            super(delegate);
            this.mPrivateKeyAlias = privateKeyAlias;
            this.mPrivateKeyUid = privateKeyUid;
        }

        public PublicKey getPublicKey() {
            PublicKey original = super.getPublicKey();
            return HwUniversalKeyStoreProvider.getAndroidKeyStorePublicKey(this.mPrivateKeyAlias, this.mPrivateKeyUid, original.getAlgorithm(), original.getEncoded());
        }
    }

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (!isPrivateKeyEntry(alias)) {
            return null;
        }
        return HwUniversalKeyStoreProvider.loadAndroidKeyStorePrivateKeyFromKeystore(this.mKeyStore, HwCredentials.USER_PRIVATE_KEY + alias, this.mUid);
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        Certificate[] caList;
        if (alias != null) {
            X509Certificate leaf = (X509Certificate) engineGetCertificate(alias);
            if (leaf == null) {
                Log.e("HwUniversalKeyStore", "engineGetCertificateChain leaf == null");
                return null;
            }
            HwExportResult result = this.mKeyStore.get(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid);
            if (result == null || result.resultCode != 1) {
                Log.e("HwUniversalKeyStore", "engineGetCertificateChain get certificate chain failed!");
                return null;
            }
            byte[] chainBytes = result.exportData;
            if (chainBytes != null) {
                Collection<X509Certificate> caChain = toCertificates(chainBytes);
                caList = new Certificate[caChain.size()];
                int i = 0;
                for (X509Certificate x509Certificate : caChain) {
                    caList[i] = x509Certificate;
                    i++;
                }
            } else {
                caList = new Certificate[1];
            }
            Certificate[] caList2 = caList;
            caList2[0] = leaf;
            return caList2;
        }
        Log.e("HwUniversalKeyStore", "engineGetCertificateChain alias == null");
        throw new NullPointerException("alias == null");
    }

    public Certificate engineGetCertificate(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            HwExportResult result = hwKeystoreManager.get(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid);
            byte[] encodedCertChain = result.exportData;
            if (result.resultCode == 1 && encodedCertChain != null) {
                try {
                    return getKeyStoreCertificate(alias, (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(encodedCertChain)).iterator().next());
                } catch (CertificateException e) {
                    Log.e("HwUniversalKeyStore", "Couldn't get certificate!", e);
                }
            }
            return null;
        }
        throw new NullPointerException("alias == null");
    }

    public Date engineGetCreationDate(String alias) {
        return null;
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        throw new UnsupportedOperationException();
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        if (key != null) {
            throw new UnsupportedOperationException();
        } else if (chain == null) {
            throw new NullPointerException("Certificate chain is null.");
        } else if (alias == null || alias.isEmpty()) {
            throw new NullPointerException("alias is null.");
        } else if (chain.length < 2) {
            throw new UnsupportedOperationException();
        } else if (isPrivateKeyEntry(alias)) {
            int chainBytesLength = 0;
            int i = 0;
            while (i < chain.length) {
                try {
                    chainBytesLength += chain[i].getEncoded().length;
                    i++;
                } catch (CertificateEncodingException e) {
                    throw new ProviderException("Failed to get certificate encoded");
                }
            }
            byte[] chainBytes = new byte[chainBytesLength];
            int countLength = 0;
            for (int j = 0; j < chain.length; j++) {
                System.arraycopy(chain[j].getEncoded(), 0, chainBytes, countLength, chain[j].getEncoded().length);
                countLength += chain[j].getEncoded().length;
            }
            HwKeymasterBlob blob = new HwKeymasterBlob(chainBytes);
            int insertErrorCode = this.mKeyStore.set(HwCredentials.CERTIFICATE_CHAIN + alias, blob, this.mUid);
            if (insertErrorCode != 1) {
                throw new ProviderException("Failed to set certificate chain", HwKeystoreManager.getKeyStoreException(insertErrorCode));
            }
        } else {
            throw new KeyStoreException("Entry not exists.");
        }
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        throw new UnsupportedOperationException();
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (!HwCredentials.deleteAllTypesForAlias(this.mKeyStore, alias, this.mUid)) {
            throw new KeyStoreException("Failed to delete entry");
        }
    }

    private Set<String> getUniqueAliases() {
        String[] rawAliases = this.mKeyStore.list("", this.mUid);
        if (rawAliases == null) {
            return new HashSet();
        }
        Set<String> aliases = new HashSet<>(rawAliases.length);
        for (String alias : rawAliases) {
            int idx = alias.indexOf(95);
            if (idx == -1 || alias.length() <= idx) {
                Log.e("HwUniversalKeyStore", "invalid alias");
            } else {
                aliases.add(alias.substring(idx + 1));
            }
        }
        return aliases;
    }

    public Enumeration<String> engineAliases() {
        return Collections.enumeration(getUniqueAliases());
    }

    public boolean engineContainsAlias(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            if (!hwKeystoreManager.contains(HwCredentials.USER_PRIVATE_KEY + alias, this.mUid)) {
                HwKeystoreManager hwKeystoreManager2 = this.mKeyStore;
                if (!hwKeystoreManager2.contains(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid)) {
                    return false;
                }
            }
            return true;
        }
        throw new NullPointerException("alias == null");
    }

    public int engineSize() {
        return getUniqueAliases().size();
    }

    public boolean engineIsKeyEntry(String alias) {
        return isPrivateKeyEntry(alias);
    }

    public boolean engineIsCertificateEntry(String alias) {
        return !isPrivateKeyEntry(alias) && isCertificateEntry(alias);
    }

    public String engineGetCertificateAlias(Certificate cert) {
        if (cert == null || !"X.509".equalsIgnoreCase(cert.getType())) {
            return null;
        }
        try {
            byte[] targetCertBytes = cert.getEncoded();
            if (targetCertBytes == null) {
                return null;
            }
            Set<String> nonCaEntries = new HashSet<>();
            String[] certAliases = this.mKeyStore.list(HwCredentials.USER_CERTIFICATE, this.mUid);
            if (certAliases != null) {
                for (String alias : certAliases) {
                    byte[] certBytes = this.mKeyStore.get(HwCredentials.USER_CERTIFICATE + alias, this.mUid).exportData;
                    if (certBytes != null) {
                        nonCaEntries.add(alias);
                        if (Arrays.equals(certBytes, targetCertBytes)) {
                            return alias;
                        }
                    }
                }
            }
            String[] caAliases = this.mKeyStore.list(HwCredentials.CA_CERTIFICATE, this.mUid);
            if (certAliases != null) {
                for (String alias2 : caAliases) {
                    if (!nonCaEntries.contains(alias2)) {
                        byte[] certBytes2 = this.mKeyStore.get(HwCredentials.CA_CERTIFICATE + alias2, this.mUid).exportData;
                        if (certBytes2 != null && Arrays.equals(certBytes2, targetCertBytes)) {
                            return alias2;
                        }
                    }
                }
            }
            return null;
        } catch (CertificateEncodingException e) {
            return null;
        }
    }

    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException("Can not serialize HwUniversalKeyStore to OutputStream");
    }

    public void engineStore(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        super.engineStore(param);
    }

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

    public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            this.mKeyStore = HwKeystoreManager.getInstance();
            this.mUid = -1;
            return;
        }
        throw new IllegalArgumentException("Unsupported param type: " + param.getClass());
    }

    public KeyStore.Entry engineGetEntry(String alias, KeyStore.ProtectionParameter protParam) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return super.engineGetEntry(alias, protParam);
    }

    public void engineSetEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (alias == null || alias.isEmpty()) {
            throw new NullPointerException("alias is null.");
        }
        String entryAlias = HwCredentials.USER_PRIVATE_KEY + alias;
        if (!isPrivateKeyEntry(alias)) {
            throw new KeyStoreException("Entry not exists.");
        } else if (protParam instanceof AdditionalKeyProtection) {
            int errorCode = this.mKeyStore.setKeyProtection(entryAlias, constructKeyGenerationArguments((AdditionalKeyProtection) protParam));
            if (errorCode != 1) {
                throw new ProviderException("Failed to set entry", HwKeystoreManager.getKeyStoreException(errorCode));
            }
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

    public boolean engineEntryInstanceOf(String alias, Class<? extends KeyStore.Entry> entryClass) {
        return super.engineEntryInstanceOf(alias, entryClass);
    }

    private boolean isPrivateKeyEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            return hwKeystoreManager.contains(HwCredentials.USER_PRIVATE_KEY + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private boolean isCertificateEntry(String alias) {
        if (alias != null) {
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            return hwKeystoreManager.contains(HwCredentials.CERTIFICATE_CHAIN + alias, this.mUid);
        }
        throw new NullPointerException("alias == null");
    }

    private static X509Certificate toCertificate(byte[] bytes) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            Log.w("HwUniversalKeyStore", "Couldn't parse certificate in keystore", e);
            return null;
        }
    }

    private static Collection<X509Certificate> toCertificates(byte[] bytes) {
        try {
            return CertificateFactory.getInstance("X.509").generateCertificates(new ByteArrayInputStream(bytes));
        } catch (CertificateException e) {
            Log.w("HwUniversalKeyStore", "Couldn't parse certificates in keystore", e);
            return new ArrayList();
        }
    }

    private Certificate getCertificateForPrivateKeyEntry(String alias, byte[] encodedCert) {
        X509Certificate cert = toCertificate(encodedCert);
        if (cert != null) {
            return getKeyStoreCertificate(alias, cert);
        }
        Log.w("HwUniversalKeyStore", "getCertificateForPrivateKeyEntry cert is null");
        return null;
    }

    private Certificate getKeyStoreCertificate(String alias, X509Certificate cert) {
        String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + alias;
        if (this.mKeyStore.contains(privateKeyAlias, this.mUid)) {
            return wrapIntoKeyStoreCertificate(privateKeyAlias, this.mUid, cert);
        }
        return cert;
    }

    private static HwKeyStoreX509Certificate wrapIntoKeyStoreCertificate(String privateKeyAlias, int uid, X509Certificate certificate) {
        if (certificate != null) {
            return new HwKeyStoreX509Certificate(privateKeyAlias, uid, certificate);
        }
        return null;
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
}
