package com.android.okhttp.internal.framed;

import com.android.okhttp.Protocol;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.framed.FrameReader.Handler;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.DeflaterSink;
import com.android.okhttp.okio.Okio;
import com.squareup.okhttp.internal.framed.Header;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.util.List;
import java.util.zip.Deflater;

public final class Spdy3 implements Variant {
    static final byte[] DICTIONARY;
    static final int FLAG_FIN = 1;
    static final int FLAG_UNIDIRECTIONAL = 2;
    static final int TYPE_DATA = 0;
    static final int TYPE_GOAWAY = 7;
    static final int TYPE_HEADERS = 8;
    static final int TYPE_PING = 6;
    static final int TYPE_RST_STREAM = 3;
    static final int TYPE_SETTINGS = 4;
    static final int TYPE_SYN_REPLY = 2;
    static final int TYPE_SYN_STREAM = 1;
    static final int TYPE_WINDOW_UPDATE = 9;
    static final int VERSION = 3;

    static final class Reader implements FrameReader {
        private final boolean client;
        private final NameValueBlockReader headerBlockReader = new NameValueBlockReader(this.source);
        private final BufferedSource source;

        Reader(BufferedSource source, boolean client) {
            this.source = source;
            this.client = client;
        }

        public void readConnectionPreface() {
        }

        public boolean nextFrame(Handler handler) throws IOException {
            try {
                int w1 = this.source.readInt();
                int w2 = this.source.readInt();
                int flags = (-16777216 & w2) >>> 24;
                int length = w2 & 16777215;
                if ((Integer.MIN_VALUE & w1) != 0) {
                    int version = (2147418112 & w1) >>> 16;
                    int type = w1 & 65535;
                    if (version != 3) {
                        throw new ProtocolException("version != 3: " + version);
                    }
                    switch (type) {
                        case 1:
                            readSynStream(handler, flags, length);
                            return true;
                        case 2:
                            readSynReply(handler, flags, length);
                            return true;
                        case 3:
                            readRstStream(handler, flags, length);
                            return true;
                        case Spdy3.TYPE_SETTINGS /*4*/:
                            readSettings(handler, flags, length);
                            return true;
                        case Spdy3.TYPE_PING /*6*/:
                            readPing(handler, flags, length);
                            return true;
                        case Spdy3.TYPE_GOAWAY /*7*/:
                            readGoAway(handler, flags, length);
                            return true;
                        case Spdy3.TYPE_HEADERS /*8*/:
                            readHeaders(handler, flags, length);
                            return true;
                        case Spdy3.TYPE_WINDOW_UPDATE /*9*/:
                            readWindowUpdate(handler, flags, length);
                            return true;
                        default:
                            this.source.skip((long) length);
                            return true;
                    }
                }
                handler.data((flags & 1) != 0, w1 & Integer.MAX_VALUE, this.source, length);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        private void readSynStream(Handler handler, int flags, int length) throws IOException {
            int streamId = this.source.readInt() & Integer.MAX_VALUE;
            int associatedStreamId = this.source.readInt() & Integer.MAX_VALUE;
            this.source.readShort();
            List<Header> headerBlock = this.headerBlockReader.readNameValueBlock(length - 10);
            handler.headers((flags & 2) != 0, (flags & 1) != 0, streamId, associatedStreamId, headerBlock, HeadersMode.SPDY_SYN_STREAM);
        }

        private void readSynReply(Handler handler, int flags, int length) throws IOException {
            handler.headers(false, (flags & 1) != 0, this.source.readInt() & Integer.MAX_VALUE, -1, this.headerBlockReader.readNameValueBlock(length - 4), HeadersMode.SPDY_REPLY);
        }

        private void readRstStream(Handler handler, int flags, int length) throws IOException {
            if (length != Spdy3.TYPE_HEADERS) {
                throw ioException("TYPE_RST_STREAM length: %d != 8", Integer.valueOf(length));
            }
            int streamId = this.source.readInt() & Integer.MAX_VALUE;
            ErrorCode errorCode = ErrorCode.fromSpdy3Rst(this.source.readInt());
            if (errorCode == null) {
                throw ioException("TYPE_RST_STREAM unexpected error code: %d", Integer.valueOf(errorCodeInt));
            } else {
                handler.rstStream(streamId, errorCode);
            }
        }

        private void readHeaders(Handler handler, int flags, int length) throws IOException {
            handler.headers(false, false, this.source.readInt() & Integer.MAX_VALUE, -1, this.headerBlockReader.readNameValueBlock(length - 4), HeadersMode.SPDY_HEADERS);
        }

        private void readWindowUpdate(Handler handler, int flags, int length) throws IOException {
            if (length != Spdy3.TYPE_HEADERS) {
                throw ioException("TYPE_WINDOW_UPDATE length: %d != 8", Integer.valueOf(length));
            }
            int streamId = this.source.readInt() & Integer.MAX_VALUE;
            long increment = (long) (this.source.readInt() & Integer.MAX_VALUE);
            if (increment == 0) {
                throw ioException("windowSizeIncrement was 0", Long.valueOf(increment));
            } else {
                handler.windowUpdate(streamId, increment);
            }
        }

        private void readPing(Handler handler, int flags, int length) throws IOException {
            boolean z = true;
            if (length != Spdy3.TYPE_SETTINGS) {
                throw ioException("TYPE_PING length: %d != 4", Integer.valueOf(length));
            }
            int id = this.source.readInt();
            boolean z2 = this.client;
            if ((id & 1) != 1) {
                z = Spdy3.TYPE_DATA;
            }
            handler.ping(z2 == z, id, Spdy3.TYPE_DATA);
        }

        private void readGoAway(Handler handler, int flags, int length) throws IOException {
            if (length != Spdy3.TYPE_HEADERS) {
                throw ioException("TYPE_GOAWAY length: %d != 8", Integer.valueOf(length));
            }
            int lastGoodStreamId = this.source.readInt() & Integer.MAX_VALUE;
            ErrorCode errorCode = ErrorCode.fromSpdyGoAway(this.source.readInt());
            if (errorCode == null) {
                throw ioException("TYPE_GOAWAY unexpected error code: %d", Integer.valueOf(errorCodeInt));
            } else {
                handler.goAway(lastGoodStreamId, errorCode, ByteString.EMPTY);
            }
        }

        private void readSettings(Handler handler, int flags, int length) throws IOException {
            int numberOfEntries = this.source.readInt();
            if (length != (numberOfEntries * Spdy3.TYPE_HEADERS) + Spdy3.TYPE_SETTINGS) {
                throw ioException("TYPE_SETTINGS length: %d != 4 + 8 * %d", Integer.valueOf(length), Integer.valueOf(numberOfEntries));
            }
            Settings settings = new Settings();
            for (int i = Spdy3.TYPE_DATA; i < numberOfEntries; i++) {
                int w1 = this.source.readInt();
                settings.set(w1 & 16777215, (-16777216 & w1) >>> 24, this.source.readInt());
            }
            handler.settings((flags & 1) != 0, settings);
        }

        private static IOException ioException(String message, Object... args) throws IOException {
            throw new IOException(String.format(message, args));
        }

        public void close() throws IOException {
            this.headerBlockReader.close();
        }
    }

    static final class Writer implements FrameWriter {
        private final boolean client;
        private boolean closed;
        private final Buffer headerBlockBuffer = new Buffer();
        private final BufferedSink headerBlockOut;
        private final BufferedSink sink;

        Writer(BufferedSink sink, boolean client) {
            this.sink = sink;
            this.client = client;
            Deflater deflater = new Deflater();
            deflater.setDictionary(Spdy3.DICTIONARY);
            this.headerBlockOut = Okio.buffer(new DeflaterSink(this.headerBlockBuffer, deflater));
        }

        public void ackSettings(Settings peerSettings) {
        }

        public void pushPromise(int streamId, int promisedStreamId, List<Header> list) throws IOException {
        }

        public synchronized void connectionPreface() {
        }

        public synchronized void flush() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.sink.flush();
        }

        public synchronized void synStream(boolean outFinished, boolean inFinished, int streamId, int associatedStreamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            writeNameValueBlockToBuffer(headerBlock);
            int length = (int) (this.headerBlockBuffer.size() + 10);
            int flags = (outFinished ? 1 : Spdy3.TYPE_DATA) | (inFinished ? 2 : Spdy3.TYPE_DATA);
            this.sink.writeInt(-2147287039);
            this.sink.writeInt(((flags & 255) << 24) | (16777215 & length));
            this.sink.writeInt(Integer.MAX_VALUE & streamId);
            this.sink.writeInt(Integer.MAX_VALUE & associatedStreamId);
            this.sink.writeShort(Spdy3.TYPE_DATA);
            this.sink.writeAll(this.headerBlockBuffer);
            this.sink.flush();
        }

        public synchronized void synReply(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            writeNameValueBlockToBuffer(headerBlock);
            int flags = outFinished ? 1 : Spdy3.TYPE_DATA;
            int length = (int) (this.headerBlockBuffer.size() + 4);
            this.sink.writeInt(-2147287038);
            this.sink.writeInt(((flags & 255) << 24) | (16777215 & length));
            this.sink.writeInt(Integer.MAX_VALUE & streamId);
            this.sink.writeAll(this.headerBlockBuffer);
            this.sink.flush();
        }

        public synchronized void headers(int streamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            writeNameValueBlockToBuffer(headerBlock);
            int length = (int) (this.headerBlockBuffer.size() + 4);
            this.sink.writeInt(-2147287032);
            this.sink.writeInt((16777215 & length) | Spdy3.TYPE_DATA);
            this.sink.writeInt(Integer.MAX_VALUE & streamId);
            this.sink.writeAll(this.headerBlockBuffer);
        }

        public synchronized void rstStream(int streamId, ErrorCode errorCode) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (errorCode.spdyRstCode == -1) {
                throw new IllegalArgumentException();
            } else {
                this.sink.writeInt(-2147287037);
                this.sink.writeInt(Spdy3.TYPE_HEADERS);
                this.sink.writeInt(Integer.MAX_VALUE & streamId);
                this.sink.writeInt(errorCode.spdyRstCode);
                this.sink.flush();
            }
        }

        public int maxDataLength() {
            return 16383;
        }

        public synchronized void data(boolean outFinished, int streamId, Buffer source, int byteCount) throws IOException {
            sendDataFrame(streamId, outFinished ? 1 : Spdy3.TYPE_DATA, source, byteCount);
        }

        void sendDataFrame(int streamId, int flags, Buffer buffer, int byteCount) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (((long) byteCount) > 16777215) {
                throw new IllegalArgumentException("FRAME_TOO_LARGE max size is 16Mib: " + byteCount);
            } else {
                this.sink.writeInt(Integer.MAX_VALUE & streamId);
                this.sink.writeInt(((flags & 255) << 24) | (16777215 & byteCount));
                if (byteCount > 0) {
                    this.sink.write(buffer, (long) byteCount);
                }
            }
        }

        private void writeNameValueBlockToBuffer(List<Header> headerBlock) throws IOException {
            this.headerBlockOut.writeInt(headerBlock.size());
            int size = headerBlock.size();
            for (int i = Spdy3.TYPE_DATA; i < size; i++) {
                ByteString name = ((Header) headerBlock.get(i)).name;
                this.headerBlockOut.writeInt(name.size());
                this.headerBlockOut.write(name);
                ByteString value = ((Header) headerBlock.get(i)).value;
                this.headerBlockOut.writeInt(value.size());
                this.headerBlockOut.write(value);
            }
            this.headerBlockOut.flush();
        }

        public synchronized void settings(Settings settings) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            int size = settings.size();
            int length = (size * Spdy3.TYPE_HEADERS) + Spdy3.TYPE_SETTINGS;
            this.sink.writeInt(-2147287036);
            this.sink.writeInt((length & 16777215) | Spdy3.TYPE_DATA);
            this.sink.writeInt(size);
            for (int i = Spdy3.TYPE_DATA; i <= 10; i++) {
                if (settings.isSet(i)) {
                    this.sink.writeInt(((settings.flags(i) & 255) << 24) | (i & 16777215));
                    this.sink.writeInt(settings.get(i));
                }
            }
            this.sink.flush();
        }

        public synchronized void ping(boolean reply, int payload1, int payload2) throws IOException {
            boolean z = true;
            synchronized (this) {
                if (this.closed) {
                    throw new IOException("closed");
                }
                boolean z2 = this.client;
                if ((payload1 & 1) != 1) {
                    z = Spdy3.TYPE_DATA;
                }
                if (reply != (z2 != z)) {
                    throw new IllegalArgumentException("payload != reply");
                }
                this.sink.writeInt(-2147287034);
                this.sink.writeInt(Spdy3.TYPE_SETTINGS);
                this.sink.writeInt(payload1);
                this.sink.flush();
            }
        }

        public synchronized void goAway(int lastGoodStreamId, ErrorCode errorCode, byte[] ignored) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (errorCode.spdyGoAwayCode == -1) {
                throw new IllegalArgumentException("errorCode.spdyGoAwayCode == -1");
            } else {
                this.sink.writeInt(-2147287033);
                this.sink.writeInt(Spdy3.TYPE_HEADERS);
                this.sink.writeInt(lastGoodStreamId);
                this.sink.writeInt(errorCode.spdyGoAwayCode);
                this.sink.flush();
            }
        }

        public synchronized void windowUpdate(int streamId, long increment) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (increment == 0 || increment > 2147483647L) {
                throw new IllegalArgumentException("windowSizeIncrement must be between 1 and 0x7fffffff: " + increment);
            } else {
                this.sink.writeInt(-2147287031);
                this.sink.writeInt(Spdy3.TYPE_HEADERS);
                this.sink.writeInt(streamId);
                this.sink.writeInt((int) increment);
                this.sink.flush();
            }
        }

        public synchronized void close() throws IOException {
            this.closed = true;
            Util.closeAll(this.sink, this.headerBlockOut);
        }
    }

    public Protocol getProtocol() {
        return Protocol.SPDY_3;
    }

    static {
        try {
            DICTIONARY = "\u0000\u0000\u0000\u0007options\u0000\u0000\u0000\u0004head\u0000\u0000\u0000\u0004post\u0000\u0000\u0000\u0003put\u0000\u0000\u0000\u0006delete\u0000\u0000\u0000\u0005trace\u0000\u0000\u0000\u0006accept\u0000\u0000\u0000\u000eaccept-charset\u0000\u0000\u0000\u000faccept-encoding\u0000\u0000\u0000\u000faccept-language\u0000\u0000\u0000\raccept-ranges\u0000\u0000\u0000\u0003age\u0000\u0000\u0000\u0005allow\u0000\u0000\u0000\rauthorization\u0000\u0000\u0000\rcache-control\u0000\u0000\u0000\nconnection\u0000\u0000\u0000\fcontent-base\u0000\u0000\u0000\u0010content-encoding\u0000\u0000\u0000\u0010content-language\u0000\u0000\u0000\u000econtent-length\u0000\u0000\u0000\u0010content-location\u0000\u0000\u0000\u000bcontent-md5\u0000\u0000\u0000\rcontent-range\u0000\u0000\u0000\fcontent-type\u0000\u0000\u0000\u0004date\u0000\u0000\u0000\u0004etag\u0000\u0000\u0000\u0006expect\u0000\u0000\u0000\u0007expires\u0000\u0000\u0000\u0004from\u0000\u0000\u0000\u0004host\u0000\u0000\u0000\bif-match\u0000\u0000\u0000\u0011if-modified-since\u0000\u0000\u0000\rif-none-match\u0000\u0000\u0000\bif-range\u0000\u0000\u0000\u0013if-unmodified-since\u0000\u0000\u0000\rlast-modified\u0000\u0000\u0000\blocation\u0000\u0000\u0000\fmax-forwards\u0000\u0000\u0000\u0006pragma\u0000\u0000\u0000\u0012proxy-authenticate\u0000\u0000\u0000\u0013proxy-authorization\u0000\u0000\u0000\u0005range\u0000\u0000\u0000\u0007referer\u0000\u0000\u0000\u000bretry-after\u0000\u0000\u0000\u0006server\u0000\u0000\u0000\u0002te\u0000\u0000\u0000\u0007trailer\u0000\u0000\u0000\u0011transfer-encoding\u0000\u0000\u0000\u0007upgrade\u0000\u0000\u0000\nuser-agent\u0000\u0000\u0000\u0004vary\u0000\u0000\u0000\u0003via\u0000\u0000\u0000\u0007warning\u0000\u0000\u0000\u0010www-authenticate\u0000\u0000\u0000\u0006method\u0000\u0000\u0000\u0003get\u0000\u0000\u0000\u0006status\u0000\u0000\u0000\u0006200 OK\u0000\u0000\u0000\u0007version\u0000\u0000\u0000\bHTTP/1.1\u0000\u0000\u0000\u0003url\u0000\u0000\u0000\u0006public\u0000\u0000\u0000\nset-cookie\u0000\u0000\u0000\nkeep-alive\u0000\u0000\u0000\u0006origin100101201202205206300302303304305306307402405406407408409410411412413414415416417502504505203 Non-Authoritative Information204 No Content301 Moved Permanently400 Bad Request401 Unauthorized403 Forbidden404 Not Found500 Internal Server Error501 Not Implemented503 Service UnavailableJan Feb Mar Apr May Jun Jul Aug Sept Oct Nov Dec 00:00:00 Mon, Tue, Wed, Thu, Fri, Sat, Sun, GMTchunked,text/html,image/png,image/jpg,image/gif,application/xml,application/xhtml+xml,text/plain,text/javascript,publicprivatemax-age=gzip,deflate,sdchcharset=utf-8charset=iso-8859-1,utf-,*,enq=0.".getBytes(Util.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    public FrameReader newReader(BufferedSource source, boolean client) {
        return new Reader(source, client);
    }

    public FrameWriter newWriter(BufferedSink sink, boolean client) {
        return new Writer(sink, client);
    }
}
