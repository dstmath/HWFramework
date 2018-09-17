package sun.net.www.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import sun.net.www.MessageHeader;

public class ChunkedInputStream extends InputStream implements Hurryable {
    private static final int MAX_CHUNK_HEADER_SIZE = 2050;
    static final int STATE_AWAITING_CHUNK_EOL = 3;
    static final int STATE_AWAITING_CHUNK_HEADER = 1;
    static final int STATE_AWAITING_TRAILERS = 4;
    static final int STATE_DONE = 5;
    static final int STATE_READING_CHUNK = 2;
    private int chunkCount;
    private byte[] chunkData;
    private int chunkPos;
    private int chunkRead;
    private int chunkSize;
    private boolean closed;
    private boolean error;
    private HttpClient hc;
    private InputStream in;
    private int rawCount;
    private byte[] rawData;
    private int rawPos;
    private MessageHeader responses;
    private int state;

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("stream is closed");
        }
    }

    private void ensureRawAvailable(int size) {
        if (this.rawCount + size > this.rawData.length) {
            int used = this.rawCount - this.rawPos;
            if (used + size > this.rawData.length) {
                byte[] tmp = new byte[(used + size)];
                if (used > 0) {
                    System.arraycopy(this.rawData, this.rawPos, tmp, 0, used);
                }
                this.rawData = tmp;
            } else if (used > 0) {
                System.arraycopy(this.rawData, this.rawPos, this.rawData, 0, used);
            }
            this.rawCount = used;
            this.rawPos = 0;
        }
    }

    private void closeUnderlying() throws IOException {
        if (this.in != null) {
            if (!this.error && this.state == STATE_DONE) {
                this.hc.finished();
            } else if (!hurry()) {
                this.hc.closeServer();
            }
            this.in = null;
        }
    }

    private int fastRead(byte[] b, int off, int len) throws IOException {
        int cnt;
        int remaining = this.chunkSize - this.chunkRead;
        if (remaining < len) {
            cnt = remaining;
        } else {
            cnt = len;
        }
        if (cnt <= 0) {
            return 0;
        }
        try {
            int nread = this.in.read(b, off, cnt);
            if (nread > 0) {
                this.chunkRead += nread;
                if (this.chunkRead >= this.chunkSize) {
                    this.state = STATE_AWAITING_CHUNK_EOL;
                }
                return nread;
            }
            this.error = true;
            throw new IOException("Premature EOF");
        } catch (IOException e) {
            this.error = true;
            throw e;
        }
    }

    private void processRaw() throws IOException {
        while (this.state != STATE_DONE) {
            int pos;
            int i;
            switch (this.state) {
                case STATE_AWAITING_CHUNK_HEADER /*1*/:
                    pos = this.rawPos;
                    while (pos < this.rawCount && this.rawData[pos] != 10) {
                        pos += STATE_AWAITING_CHUNK_HEADER;
                        if (pos - this.rawPos >= MAX_CHUNK_HEADER_SIZE) {
                            this.error = true;
                            throw new IOException("Chunk header too long");
                        }
                    }
                    if (pos < this.rawCount) {
                        String header = new String(this.rawData, this.rawPos, (pos - this.rawPos) + STATE_AWAITING_CHUNK_HEADER, "US-ASCII");
                        i = 0;
                        while (i < header.length() && Character.digit(header.charAt(i), 16) != -1) {
                            i += STATE_AWAITING_CHUNK_HEADER;
                        }
                        try {
                            this.chunkSize = Integer.parseInt(header.substring(0, i), 16);
                            this.rawPos = pos + STATE_AWAITING_CHUNK_HEADER;
                            this.chunkRead = 0;
                            if (this.chunkSize <= 0) {
                                this.state = STATE_AWAITING_TRAILERS;
                                break;
                            } else {
                                this.state = STATE_READING_CHUNK;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            this.error = true;
                            throw new IOException("Bogus chunk size");
                        }
                    }
                    return;
                case STATE_READING_CHUNK /*2*/:
                    if (this.rawPos < this.rawCount) {
                        int copyLen = Math.min(this.chunkSize - this.chunkRead, this.rawCount - this.rawPos);
                        if (this.chunkData.length < this.chunkCount + copyLen) {
                            int cnt = this.chunkCount - this.chunkPos;
                            if (this.chunkData.length < cnt + copyLen) {
                                byte[] tmp = new byte[(cnt + copyLen)];
                                System.arraycopy(this.chunkData, this.chunkPos, tmp, 0, cnt);
                                this.chunkData = tmp;
                            } else {
                                System.arraycopy(this.chunkData, this.chunkPos, this.chunkData, 0, cnt);
                            }
                            this.chunkPos = 0;
                            this.chunkCount = cnt;
                        }
                        System.arraycopy(this.rawData, this.rawPos, this.chunkData, this.chunkCount, copyLen);
                        this.rawPos += copyLen;
                        this.chunkCount += copyLen;
                        this.chunkRead += copyLen;
                        if (this.chunkSize - this.chunkRead <= 0) {
                            this.state = STATE_AWAITING_CHUNK_EOL;
                            break;
                        }
                        return;
                    }
                    return;
                case STATE_AWAITING_CHUNK_EOL /*3*/:
                    if (this.rawPos + STATE_AWAITING_CHUNK_HEADER < this.rawCount) {
                        if (this.rawData[this.rawPos] == 13) {
                            if (this.rawData[this.rawPos + STATE_AWAITING_CHUNK_HEADER] == 10) {
                                this.rawPos += STATE_READING_CHUNK;
                                this.state = STATE_AWAITING_CHUNK_HEADER;
                                break;
                            }
                            this.error = true;
                            throw new IOException("missing LF");
                        }
                        this.error = true;
                        throw new IOException("missing CR");
                    }
                    return;
                case STATE_AWAITING_TRAILERS /*4*/:
                    pos = this.rawPos;
                    while (pos < this.rawCount && this.rawData[pos] != 10) {
                        pos += STATE_AWAITING_CHUNK_HEADER;
                    }
                    if (pos < this.rawCount) {
                        if (pos != this.rawPos) {
                            if (this.rawData[pos - 1] == 13) {
                                if (pos != this.rawPos + STATE_AWAITING_CHUNK_HEADER) {
                                    String trailer = new String(this.rawData, this.rawPos, pos - this.rawPos, "US-ASCII");
                                    i = trailer.indexOf(58);
                                    if (i != -1) {
                                        this.responses.add(trailer.substring(0, i).trim(), trailer.substring(i + STATE_AWAITING_CHUNK_HEADER, trailer.length()).trim());
                                        this.rawPos = pos + STATE_AWAITING_CHUNK_HEADER;
                                        break;
                                    }
                                    throw new IOException("Malformed tailer - format should be key:value");
                                }
                                this.state = STATE_DONE;
                                closeUnderlying();
                                return;
                            }
                            this.error = true;
                            throw new IOException("LF should be proceeded by CR");
                        }
                        this.error = true;
                        throw new IOException("LF should be proceeded by CR");
                    }
                    return;
                    break;
                default:
                    break;
            }
        }
    }

    private int readAheadNonBlocking() throws IOException {
        int avail = this.in.available();
        if (avail > 0) {
            ensureRawAvailable(avail);
            try {
                int nread = this.in.read(this.rawData, this.rawCount, avail);
                if (nread < 0) {
                    this.error = true;
                    return -1;
                }
                this.rawCount += nread;
                processRaw();
            } catch (IOException e) {
                this.error = true;
                throw e;
            }
        }
        return this.chunkCount - this.chunkPos;
    }

    private int readAheadBlocking() throws IOException {
        while (this.state != STATE_DONE) {
            ensureRawAvailable(32);
            try {
                int nread = this.in.read(this.rawData, this.rawCount, this.rawData.length - this.rawCount);
                if (nread < 0) {
                    this.error = true;
                    throw new IOException("Premature EOF");
                }
                this.rawCount += nread;
                processRaw();
                if (this.chunkCount > 0) {
                    return this.chunkCount - this.chunkPos;
                }
            } catch (IOException e) {
                this.error = true;
                throw e;
            }
        }
        return -1;
    }

    private int readAhead(boolean allowBlocking) throws IOException {
        if (this.state == STATE_DONE) {
            return -1;
        }
        if (this.chunkPos >= this.chunkCount) {
            this.chunkCount = 0;
            this.chunkPos = 0;
        }
        if (allowBlocking) {
            return readAheadBlocking();
        }
        return readAheadNonBlocking();
    }

    public ChunkedInputStream(InputStream in, HttpClient hc, MessageHeader responses) throws IOException {
        this.chunkData = new byte[Spliterator.CONCURRENT];
        this.rawData = new byte[32];
        this.in = in;
        this.responses = responses;
        this.hc = hc;
        this.state = STATE_AWAITING_CHUNK_HEADER;
    }

    public synchronized int read() throws IOException {
        ensureOpen();
        if (this.chunkPos >= this.chunkCount && readAhead(true) <= 0) {
            return -1;
        }
        byte[] bArr = this.chunkData;
        int i = this.chunkPos;
        this.chunkPos = i + STATE_AWAITING_CHUNK_HEADER;
        return bArr[i] & 255;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off >= 0 && off <= b.length && len >= 0) {
            if (off + len <= b.length && off + len >= 0) {
                if (len == 0) {
                    return 0;
                }
                int avail = this.chunkCount - this.chunkPos;
                if (avail <= 0) {
                    if (this.state == STATE_READING_CHUNK) {
                        return fastRead(b, off, len);
                    }
                    avail = readAhead(true);
                    if (avail < 0) {
                        return -1;
                    }
                }
                int cnt = avail < len ? avail : len;
                System.arraycopy(this.chunkData, this.chunkPos, b, off, cnt);
                this.chunkPos += cnt;
                return cnt;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public synchronized int available() throws IOException {
        ensureOpen();
        int avail = this.chunkCount - this.chunkPos;
        if (avail > 0) {
            return avail;
        }
        avail = readAhead(false);
        if (avail < 0) {
            return 0;
        }
        return avail;
    }

    public synchronized void close() throws IOException {
        if (!this.closed) {
            closeUnderlying();
            this.closed = true;
        }
    }

    public synchronized boolean hurry() {
        boolean z = false;
        synchronized (this) {
            if (this.in == null || this.error) {
                return false;
            }
            try {
                readAhead(false);
                if (this.error) {
                    return false;
                }
                if (this.state == STATE_DONE) {
                    z = true;
                }
                return z;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
