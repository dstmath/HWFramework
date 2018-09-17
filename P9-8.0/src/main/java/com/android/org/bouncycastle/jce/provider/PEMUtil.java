package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.util.encoders.Base64;
import java.io.IOException;
import java.io.InputStream;

public class PEMUtil {
    private final String _footer1;
    private final String _footer2;
    private final String _header1;
    private final String _header2;

    PEMUtil(String type) {
        this._header1 = "-----BEGIN " + type + "-----";
        this._header2 = "-----BEGIN X509 " + type + "-----";
        this._footer1 = "-----END " + type + "-----";
        this._footer2 = "-----END X509 " + type + "-----";
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0026  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0024  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readLine(InputStream in) throws IOException {
        int c;
        StringBuffer l = new StringBuffer();
        while (true) {
            c = in.read();
            if (c == 13 || c == 10 || c < 0) {
                if (c < 0 || l.length() != 0) {
                    if (c >= 0) {
                        return null;
                    }
                    return l.toString();
                }
            } else if (c != 13) {
                l.append((char) c);
            }
        }
        if (c >= 0) {
        }
    }

    ASN1Sequence readPEMObject(InputStream in) throws IOException {
        String line;
        StringBuffer pemBuf = new StringBuffer();
        do {
            line = readLine(in);
            if (line == null || line.startsWith(this._header1)) {
                while (true) {
                    line = readLine(in);
                    pemBuf.append(line);
                }
            }
        } while (!line.startsWith(this._header2));
        while (true) {
            line = readLine(in);
            if (line != null && !line.startsWith(this._footer1) && !line.startsWith(this._footer2)) {
                pemBuf.append(line);
            }
        }
        if (pemBuf.length() == 0) {
            return null;
        }
        ASN1Primitive o = new ASN1InputStream(Base64.decode(pemBuf.toString())).readObject();
        if (o instanceof ASN1Sequence) {
            return (ASN1Sequence) o;
        }
        throw new IOException("malformed PEM data encountered");
    }
}
