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

    void read(Reader policy) throws ParsingException, IOException {
    }

    CryptoPermission[] getPermissions() {
        return null;
    }
}
