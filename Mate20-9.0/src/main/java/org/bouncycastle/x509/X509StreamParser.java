package org.bouncycastle.x509;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Collection;
import org.bouncycastle.x509.X509Util;
import org.bouncycastle.x509.util.StreamParser;
import org.bouncycastle.x509.util.StreamParsingException;

public class X509StreamParser implements StreamParser {
    private Provider _provider;
    private X509StreamParserSpi _spi;

    private X509StreamParser(Provider provider, X509StreamParserSpi x509StreamParserSpi) {
        this._provider = provider;
        this._spi = x509StreamParserSpi;
    }

    private static X509StreamParser createParser(X509Util.Implementation implementation) {
        return new X509StreamParser(implementation.getProvider(), (X509StreamParserSpi) implementation.getEngine());
    }

    public static X509StreamParser getInstance(String str) throws NoSuchParserException {
        try {
            return createParser(X509Util.getImplementation("X509StreamParser", str));
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchParserException(e.getMessage());
        }
    }

    public static X509StreamParser getInstance(String str, String str2) throws NoSuchParserException, NoSuchProviderException {
        return getInstance(str, X509Util.getProvider(str2));
    }

    public static X509StreamParser getInstance(String str, Provider provider) throws NoSuchParserException {
        try {
            return createParser(X509Util.getImplementation("X509StreamParser", str, provider));
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchParserException(e.getMessage());
        }
    }

    public Provider getProvider() {
        return this._provider;
    }

    public void init(InputStream inputStream) {
        this._spi.engineInit(inputStream);
    }

    public void init(byte[] bArr) {
        this._spi.engineInit(new ByteArrayInputStream(bArr));
    }

    public Object read() throws StreamParsingException {
        return this._spi.engineRead();
    }

    public Collection readAll() throws StreamParsingException {
        return this._spi.engineReadAll();
    }
}
