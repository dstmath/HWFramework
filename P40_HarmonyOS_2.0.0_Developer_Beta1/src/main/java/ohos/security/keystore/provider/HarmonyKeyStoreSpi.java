package ohos.security.keystore.provider;

import android.security.keystore.AndroidKeyStoreSpi;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import ohos.security.keystore.KeyStoreProtectionParameter;

public class HarmonyKeyStoreSpi extends AndroidKeyStoreSpi {
    public Key engineGetKey(String str, char[] cArr) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return TransferUtils.toHarmonyKey(HarmonyKeyStoreSpi.super.engineGetKey(str, cArr));
    }

    public Certificate[] engineGetCertificateChain(String str) {
        return HarmonyKeyStoreSpi.super.engineGetCertificateChain(str);
    }

    public Certificate engineGetCertificate(String str) {
        return HarmonyKeyStoreSpi.super.engineGetCertificate(str);
    }

    public Date engineGetCreationDate(String str) {
        return HarmonyKeyStoreSpi.super.engineGetCreationDate(str);
    }

    public void engineSetKeyEntry(String str, Key key, char[] cArr, Certificate[] certificateArr) throws KeyStoreException {
        HarmonyKeyStoreSpi.super.engineSetKeyEntry(str, TransferUtils.toAndroidKey(key), cArr, certificateArr);
    }

    public void engineSetKeyEntry(String str, byte[] bArr, Certificate[] certificateArr) throws KeyStoreException {
        throw new KeyStoreException("Operation not supported becaused key encoding is unknown");
    }

    public void engineSetCertificateEntry(String str, Certificate certificate) throws KeyStoreException {
        HarmonyKeyStoreSpi.super.engineSetCertificateEntry(str, certificate);
    }

    public void engineDeleteEntry(String str) throws KeyStoreException {
        HarmonyKeyStoreSpi.super.engineDeleteEntry(str);
    }

    public Enumeration<String> engineAliases() {
        return HarmonyKeyStoreSpi.super.engineAliases();
    }

    public boolean engineContainsAlias(String str) {
        return HarmonyKeyStoreSpi.super.engineContainsAlias(str);
    }

    public int engineSize() {
        return HarmonyKeyStoreSpi.super.engineSize();
    }

    public boolean engineIsKeyEntry(String str) {
        return HarmonyKeyStoreSpi.super.engineIsKeyEntry(str);
    }

    public boolean engineIsCertificateEntry(String str) {
        return HarmonyKeyStoreSpi.super.engineIsCertificateEntry(str);
    }

    public String engineGetCertificateAlias(Certificate certificate) {
        return HarmonyKeyStoreSpi.super.engineGetCertificateAlias(certificate);
    }

    public void engineStore(OutputStream outputStream, char[] cArr) {
        throw new UnsupportedOperationException("Can not serialize HarmonyKeyStore to OutputStream");
    }

    public void engineLoad(InputStream inputStream, char[] cArr) throws IOException, NoSuchAlgorithmException, CertificateException {
        HarmonyKeyStoreSpi.super.engineLoad(inputStream, cArr);
    }

    public void engineLoad(KeyStore.LoadStoreParameter loadStoreParameter) throws IOException, NoSuchAlgorithmException, CertificateException {
        HarmonyKeyStoreSpi.super.engineLoad(loadStoreParameter);
    }

    public void engineSetEntry(String str, KeyStore.Entry entry, KeyStore.ProtectionParameter protectionParameter) throws KeyStoreException {
        if (protectionParameter instanceof KeyStoreProtectionParameter) {
            protectionParameter = TransferUtils.convertParam(protectionParameter);
        }
        HarmonyKeyStoreSpi.super.engineSetEntry(str, entry, protectionParameter);
    }
}
