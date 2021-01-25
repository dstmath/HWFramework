package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.encoders.Base64;

class PEMUtil {
    private final Boundaries[] _supportedBoundaries;

    /* access modifiers changed from: private */
    public class Boundaries {
        private final String _footer;
        private final String _header;

        private Boundaries(String str) {
            this._header = "-----BEGIN " + str + "-----";
            this._footer = "-----END " + str + "-----";
        }

        public boolean isTheExpectedFooter(String str) {
            return str.startsWith(this._footer);
        }

        public boolean isTheExpectedHeader(String str) {
            return str.startsWith(this._header);
        }
    }

    PEMUtil(String str) {
        this._supportedBoundaries = new Boundaries[]{new Boundaries(str), new Boundaries("X509 " + str), new Boundaries("PKCS7")};
    }

    private Boundaries getBoundaries(String str) {
        Boundaries boundaries;
        int i = 0;
        while (true) {
            Boundaries[] boundariesArr = this._supportedBoundaries;
            if (i == boundariesArr.length) {
                return null;
            }
            boundaries = boundariesArr[i];
            if (boundaries.isTheExpectedHeader(str) || boundaries.isTheExpectedFooter(str)) {
                break;
            }
            i++;
        }
        return boundaries;
    }

    private String readLine(InputStream inputStream) throws IOException {
        int read;
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            read = inputStream.read();
            if (read != 13 && read != 10 && read >= 0) {
                stringBuffer.append((char) read);
            } else if (read < 0 || stringBuffer.length() != 0) {
                break;
            }
        }
        if (read >= 0) {
            if (read == 13) {
                inputStream.mark(1);
                int read2 = inputStream.read();
                if (read2 == 10) {
                    inputStream.mark(1);
                }
                if (read2 > 0) {
                    inputStream.reset();
                }
            }
            return stringBuffer.toString();
        } else if (stringBuffer.length() == 0) {
            return null;
        } else {
            return stringBuffer.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public ASN1Sequence readPEMObject(InputStream inputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        Boundaries boundaries = null;
        while (boundaries == null) {
            String readLine = readLine(inputStream);
            if (readLine == null) {
                break;
            }
            boundaries = getBoundaries(readLine);
            if (boundaries != null && !boundaries.isTheExpectedHeader(readLine)) {
                throw new IOException("malformed PEM data: found footer where header was expected");
            }
        }
        if (boundaries != null) {
            Boundaries boundaries2 = null;
            while (boundaries2 == null) {
                String readLine2 = readLine(inputStream);
                if (readLine2 == null) {
                    break;
                }
                boundaries2 = getBoundaries(readLine2);
                if (boundaries2 == null) {
                    stringBuffer.append(readLine2);
                } else if (!boundaries.isTheExpectedFooter(readLine2)) {
                    throw new IOException("malformed PEM data: header/footer mismatch");
                }
            }
            if (boundaries2 == null) {
                throw new IOException("malformed PEM data: no footer found");
            } else if (stringBuffer.length() == 0) {
                return null;
            } else {
                try {
                    return ASN1Sequence.getInstance(Base64.decode(stringBuffer.toString()));
                } catch (Exception e) {
                    throw new IOException("malformed PEM data encountered");
                }
            }
        } else {
            throw new IOException("malformed PEM data: no header found");
        }
    }
}
