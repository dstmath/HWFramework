package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.InputStream;

class ConstructedOctetStream extends InputStream {
    private InputStream _currentStream;
    private boolean _first = true;
    private final ASN1StreamParser _parser;

    ConstructedOctetStream(ASN1StreamParser parser) {
        this._parser = parser;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int totalRead = 0;
        int i = -1;
        if (this._currentStream == null) {
            if (!this._first) {
                return -1;
            }
            ASN1OctetStringParser s = (ASN1OctetStringParser) this._parser.readObject();
            if (s == null) {
                return -1;
            }
            this._first = false;
            this._currentStream = s.getOctetStream();
        }
        while (true) {
            int totalRead2 = totalRead;
            int numRead = this._currentStream.read(b, off + totalRead2, len - totalRead2);
            if (numRead >= 0) {
                totalRead2 += numRead;
                if (totalRead2 == len) {
                    return totalRead2;
                }
            } else {
                ASN1OctetStringParser aos = (ASN1OctetStringParser) this._parser.readObject();
                if (aos == null) {
                    this._currentStream = null;
                    if (totalRead2 >= 1) {
                        i = totalRead2;
                    }
                    return i;
                }
                this._currentStream = aos.getOctetStream();
            }
            totalRead = totalRead2;
        }
    }

    public int read() throws IOException {
        if (this._currentStream == null) {
            if (!this._first) {
                return -1;
            }
            ASN1OctetStringParser s = (ASN1OctetStringParser) this._parser.readObject();
            if (s == null) {
                return -1;
            }
            this._first = false;
            this._currentStream = s.getOctetStream();
        }
        while (true) {
            int b = this._currentStream.read();
            if (b >= 0) {
                return b;
            }
            ASN1OctetStringParser s2 = (ASN1OctetStringParser) this._parser.readObject();
            if (s2 == null) {
                this._currentStream = null;
                return -1;
            }
            this._currentStream = s2.getOctetStream();
        }
    }
}
