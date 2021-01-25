package ohos.security.seckeychain;

import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import ohos.aafwk.content.Intent;
import ohos.app.AbilityContext;
import ohos.utils.net.Uri;

public class SecKeyChain {
    public static final String SPEC_ALIAS = "name";
    public static final String SPEC_CERTIFICATE = "CERT";
    public static final String SPEC_PKCS12 = "PKCS12";

    public static void selectSecPrivateKey(AbilityContext abilityContext, String[] strArr, Principal[] principalArr, String str, int i, SecKeyAliasCallback secKeyAliasCallback, String str2) {
        SecKeyChainProxy.selectSecPrivateKey(abilityContext, strArr, principalArr, str, i, secKeyAliasCallback, str2);
    }

    public static void selectSecPrivateKey(AbilityContext abilityContext, String[] strArr, Principal[] principalArr, Uri uri, SecKeyAliasCallback secKeyAliasCallback, String str) {
        SecKeyChainProxy.selectSecPrivateKey(abilityContext, strArr, principalArr, uri, secKeyAliasCallback, str);
    }

    public static Intent getInstallationIntent() {
        return SecKeyChainProxy.getInstallationIntent();
    }

    public static X509Certificate[] getSecCertificateChain(AbilityContext abilityContext, String str) throws SecKeyChainException, InterruptedException {
        try {
            return SecKeyChainProxy.getSecCertificateChain(abilityContext, str);
        } catch (SecKeyChainException e) {
            throw e;
        } catch (InterruptedException e2) {
            throw e2;
        }
    }

    public static PrivateKey getSecPrivateKey(AbilityContext abilityContext, String str) throws SecKeyChainException, InterruptedException {
        try {
            return SecKeyChainProxy.getSecPrivateKey(abilityContext, str);
        } catch (SecKeyChainException e) {
            throw e;
        } catch (InterruptedException e2) {
            throw e2;
        }
    }

    public static boolean isSupported(String str) {
        return SecKeyChainProxy.isSupported(str);
    }
}
