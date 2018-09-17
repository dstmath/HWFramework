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
        int totalRead = 0;
        while (true) {
            int numRead = this._currentStream.read(b, off + totalRead, len - totalRead);
            if (numRead >= 0) {
                totalRead += numRead;
                if (totalRead == len) {
                    return totalRead;
                }
            } else {
                ASN1OctetStringParser aos = (ASN1OctetStringParser) this._parser.readObject();
                if (aos == null) {
                    this._currentStream = null;
                    if (totalRead < 1) {
                        totalRead = -1;
                    }
                    return totalRead;
                }
                this._currentStream = aos.getOctetStream();
            }
        }
    }

    public int read() throws IOException {
        ASN1OctetStringParser s;
        if (this._currentStream == null) {
            if (!this._first) {
                return -1;
            }
            s = (ASN1OctetStringParser) this._parser.readObject();
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
            s = (ASN1OctetStringParser) this._parser.readObject();
            if (s == null) {
                this._currentStream = null;
                return -1;
            }
            this._currentStream = s.getOctetStream();
        }
    }
}
