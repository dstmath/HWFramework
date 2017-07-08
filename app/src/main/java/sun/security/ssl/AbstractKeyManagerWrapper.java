package sun.security.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

/* compiled from: SSLContextImpl */
final class AbstractKeyManagerWrapper extends X509ExtendedKeyManager {
    private final X509KeyManager km;

    AbstractKeyManagerWrapper(X509KeyManager km) {
        this.km = km;
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return this.km.getClientAliases(keyType, issuers);
    }

    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return this.km.chooseClientAlias(keyType, issuers, socket);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return this.km.getServerAliases(keyType, issuers);
    }

    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return this.km.chooseServerAlias(keyType, issuers, socket);
    }

    public X509Certificate[] getCertificateChain(String alias) {
        return this.km.getCertificateChain(alias);
    }

    public PrivateKey getPrivateKey(String alias) {
        return this.km.getPrivateKey(alias);
    }
}
