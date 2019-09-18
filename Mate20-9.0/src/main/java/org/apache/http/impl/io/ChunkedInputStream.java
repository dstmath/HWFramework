package org.apache.http.impl.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.ExceptionUtils;

@Deprecated
public class ChunkedInputStream extends InputStream {
    private boolean bof = true;
    private final CharArrayBuffer buffer;
    private int chunkSize;
    private boolean closed = false;
    private boolean eof = false;
    private Header[] footers = new Header[0];
    private SessionInputBuffer in;
    private int pos;

    public ChunkedInputStream(SessionInputBuffer in2) {
        if (in2 != null) {
            this.in = in2;
            this.pos = 0;
            this.buffer = new CharArrayBuffer(16);
            return;
        }
        throw new IllegalArgumentException("Session input buffer may not be null");
    }

    public int read() throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        } else if (this.eof) {
            return -1;
        } else {
            if (this.pos >= this.chunkSize) {
                nextChunk();
                if (this.eof) {
                    return -1;
                }
            }
            this.pos++;
            return this.in.read();
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        } else if (this.eof) {
            return -1;
        } else {
            if (this.pos >= this.chunkSize) {
                nextChunk();
                if (this.eof) {
                    return -1;
                }
            }
            int count = this.in.read(b, off, Math.min(len, this.chunkSize - this.pos));
            this.pos += count;
            return count;
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    private void nextChunk() throws IOException {
        this.chunkSize = getChunkSize();
        if (this.chunkSize >= 0) {
            this.bof = false;
            this.pos = 0;
            if (this.chunkSize == 0) {
                this.eof = true;
                parseTrailerHeaders();
                return;
            }
            return;
        }
        throw new MalformedChunkCodingException("Negative chunk size");
    }

    private int getChunkSize() throws IOException {
        if (!this.bof) {
            int cr = this.in.read();
            int lf = this.in.read();
            if (!(cr == 13 && lf == 10)) {
                throw new MalformedChunkCodingException("CRLF expected at end of chunk");
            }
        }
        this.buffer.clear();
        if (this.in.readLine(this.buffer) != -1) {
            int separator = this.buffer.indexOf(59);
            if (separator < 0) {
                separator = this.buffer.length();
            }
            try {
                return Integer.parseInt(this.buffer.substringTrimmed(0, separator), 16);
            } catch (NumberFormatException e) {
                throw new MalformedChunkCodingException("Bad chunk header");
            }
        } else {
            throw new MalformedChunkCodingException("Chunked stream ended unexpectedly");
        }
    }

    private void parseTrailerHeaders() throws IOException {
        try {
            this.footers = AbstractMessageParser.parseHeaders(this.in, -1, -1, null);
        } catch (HttpException e) {
            IOException ioe = new MalformedChunkCodingException("Invalid footer: " + e.getMessage());
            ExceptionUtils.initCause(ioe, e);
            throw ioe;
        }
    }

    public void close() throws IOException {
        if (!this.closed) {
            try {
                if (!this.eof) {
                    exhaustInputStream(this);
                }
            } finally {
                this.eof = true;
                this.closed = true;
            }
        }
    }

    public Header[] getFooters() {
        return (Header[]) this.footers.clone();
    }

    static void exhaustInputStream(InputStream inStream) throws IOException {
        do {
        } while (inStream.read(new byte[1024]) >= 0);
    }
}
