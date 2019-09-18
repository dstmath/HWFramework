package org.bouncycastle.est.jcajce;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.bouncycastle.est.ESTClientSourceProvider;
import org.bouncycastle.est.Source;
import org.bouncycastle.util.Strings;

class DefaultESTClientSourceProvider implements ESTClientSourceProvider {
    private final Long absoluteLimit;
    private final ChannelBindingProvider bindingProvider;
    private final Set<String> cipherSuites;
    private final boolean filterSupportedSuites;
    private final JsseHostnameAuthorizer hostNameAuthorizer;
    private final SSLSocketFactory sslSocketFactory;
    private final int timeout;

    public DefaultESTClientSourceProvider(SSLSocketFactory sSLSocketFactory, JsseHostnameAuthorizer jsseHostnameAuthorizer, int i, ChannelBindingProvider channelBindingProvider, Set<String> set, Long l, boolean z) throws GeneralSecurityException {
        this.sslSocketFactory = sSLSocketFactory;
        this.hostNameAuthorizer = jsseHostnameAuthorizer;
        this.timeout = i;
        this.bindingProvider = channelBindingProvider;
        this.cipherSuites = set;
        this.absoluteLimit = l;
        this.filterSupportedSuites = z;
    }

    public Source makeSource(String str, int i) throws IOException {
        Object[] objArr;
        SSLSocket sSLSocket = (SSLSocket) this.sslSocketFactory.createSocket(str, i);
        sSLSocket.setSoTimeout(this.timeout);
        if (this.cipherSuites != null && !this.cipherSuites.isEmpty()) {
            if (this.filterSupportedSuites) {
                HashSet hashSet = new HashSet();
                String[] supportedCipherSuites = sSLSocket.getSupportedCipherSuites();
                for (int i2 = 0; i2 != supportedCipherSuites.length; i2++) {
                    hashSet.add(supportedCipherSuites[i2]);
                }
                ArrayList arrayList = new ArrayList();
                for (String next : this.cipherSuites) {
                    if (hashSet.contains(next)) {
                        arrayList.add(next);
                    }
                }
                if (!arrayList.isEmpty()) {
                    objArr = arrayList.toArray(new String[arrayList.size()]);
                } else {
                    throw new IllegalStateException("No supplied cipher suite is supported by the provider.");
                }
            } else {
                objArr = this.cipherSuites.toArray(new String[this.cipherSuites.size()]);
            }
            sSLSocket.setEnabledCipherSuites((String[]) objArr);
        }
        sSLSocket.startHandshake();
        if (this.hostNameAuthorizer == null || this.hostNameAuthorizer.verified(str, sSLSocket.getSession())) {
            String lowerCase = Strings.toLowerCase(sSLSocket.getSession().getCipherSuite());
            if (lowerCase.contains("_des_") || lowerCase.contains("_des40_") || lowerCase.contains("_3des_")) {
                throw new IOException("EST clients must not use DES ciphers");
            } else if (Strings.toLowerCase(sSLSocket.getSession().getCipherSuite()).contains("null")) {
                throw new IOException("EST clients must not use NULL ciphers");
            } else if (Strings.toLowerCase(sSLSocket.getSession().getCipherSuite()).contains("anon")) {
                throw new IOException("EST clients must not use anon ciphers");
            } else if (Strings.toLowerCase(sSLSocket.getSession().getCipherSuite()).contains("export")) {
                throw new IOException("EST clients must not use export ciphers");
            } else if (sSLSocket.getSession().getProtocol().equalsIgnoreCase("tlsv1")) {
                try {
                    sSLSocket.close();
                } catch (Exception e) {
                }
                throw new IOException("EST clients must not use TLSv1");
            } else if (this.hostNameAuthorizer == null || this.hostNameAuthorizer.verified(str, sSLSocket.getSession())) {
                return new LimitedSSLSocketSource(sSLSocket, this.bindingProvider, this.absoluteLimit);
            } else {
                throw new IOException("Hostname was not verified: " + str);
            }
        } else {
            throw new IOException("Host name could not be verified.");
        }
    }
}
