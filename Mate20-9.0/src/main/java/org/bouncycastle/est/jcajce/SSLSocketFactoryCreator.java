package org.bouncycastle.est.jcajce;

import javax.net.ssl.SSLSocketFactory;

public interface SSLSocketFactoryCreator {
    SSLSocketFactory createFactory() throws Exception;

    boolean isTrusted();
}
