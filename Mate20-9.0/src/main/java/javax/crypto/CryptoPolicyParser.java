package javax.crypto;

import java.io.IOException;
import java.io.Reader;
import java.security.GeneralSecurityException;

final class CryptoPolicyParser {

    static final class ParsingException extends GeneralSecurityException {
        ParsingException(String msg) {
            super("");
        }

        ParsingException(int line, String msg) {
            super("");
        }

        ParsingException(int line, String expect, String actual) {
            super("");
        }
    }

    CryptoPolicyParser() {
    }

    /* access modifiers changed from: package-private */
    public void read(Reader policy) throws ParsingException, IOException {
    }

    /* access modifiers changed from: package-private */
    public CryptoPermission[] getPermissions() {
        return null;
    }
}
