package ohos.security.seckeychain;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import ohos.aafwk.content.Intent;
import ohos.app.AbilityContext;
import ohos.bundle.ElementName;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

class SecKeyChainProxy {
    SecKeyChainProxy() {
    }

    public static void selectSecPrivateKey(AbilityContext abilityContext, String[] strArr, Principal[] principalArr, String str, int i, final SecKeyAliasCallback secKeyAliasCallback, String str2) {
        KeyChain.choosePrivateKeyAlias((Activity) abilityContext.getHostContext(), new KeyChainAliasCallback() {
            /* class ohos.security.seckeychain.SecKeyChainProxy.AnonymousClass1 */

            @Override // android.security.KeyChainAliasCallback
            public void alias(String str) {
                SecKeyAliasCallback.this.onKeyAliasSelected(str);
            }
        }, strArr, principalArr, str, i, str2);
    }

    public static void selectSecPrivateKey(AbilityContext abilityContext, String[] strArr, Principal[] principalArr, Uri uri, final SecKeyAliasCallback secKeyAliasCallback, String str) {
        KeyChain.choosePrivateKeyAlias((Activity) abilityContext.getHostContext(), new KeyChainAliasCallback() {
            /* class ohos.security.seckeychain.SecKeyChainProxy.AnonymousClass2 */

            @Override // android.security.KeyChainAliasCallback
            public void alias(String str) {
                SecKeyAliasCallback.this.onKeyAliasSelected(str);
            }
        }, strArr, principalArr, UriConverter.convertToAndroidContentUri(uri), str);
    }

    public static Intent getInstallationIntent() {
        Intent intent = new Intent();
        intent.addFlags(16);
        android.content.Intent createInstallIntent = KeyChain.createInstallIntent();
        intent.setAction(createInstallIntent.getAction());
        ComponentName component = createInstallIntent.getComponent();
        intent.setElement(new ElementName("", component.getPackageName(), component.getClassName()));
        return intent;
    }

    public static X509Certificate[] getSecCertificateChain(AbilityContext abilityContext, String str) throws SecKeyChainException, InterruptedException {
        if (!(abilityContext.getHostContext() instanceof Context)) {
            return null;
        }
        try {
            return KeyChain.getCertificateChain((Context) abilityContext.getHostContext(), str);
        } catch (KeyChainException e) {
            throw new SecKeyChainException(e.getMessage());
        } catch (InterruptedException e2) {
            throw e2;
        }
    }

    public static PrivateKey getSecPrivateKey(AbilityContext abilityContext, String str) throws SecKeyChainException, InterruptedException {
        if (!(abilityContext.getHostContext() instanceof Context)) {
            return null;
        }
        try {
            return KeyChain.getPrivateKey((Context) abilityContext.getHostContext(), str);
        } catch (KeyChainException e) {
            throw new SecKeyChainException(e.getMessage());
        } catch (InterruptedException e2) {
            throw e2;
        }
    }

    public static boolean isSupported(String str) {
        return KeyChain.isKeyAlgorithmSupported(str);
    }
}
