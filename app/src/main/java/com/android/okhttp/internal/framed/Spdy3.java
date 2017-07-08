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
import java.net.ProtocolException;
import java.util.List;
import java.util.zip.Deflater;

public final class Spdy3 implements Variant {
    static final byte[] DICTIONARY = null;
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
        private final NameValueBlockReader headerBlockReader;
        private final BufferedSource source;

        Reader(BufferedSource source, boolean client) {
            this.source = source;
            this.headerBlockReader = new NameValueBlockReader(this.source);
            this.client = client;
        }

        public void readConnectionPreface() {
        }

        public boolean nextFrame(Handler handler) throws IOException {
            boolean control = false;
            try {
                int w1 = this.source.readInt();
                int w2 = this.source.readInt();
                if ((Integer.MIN_VALUE & w1) != 0) {
                    control = true;
                }
                int flags = (-16777216 & w2) >>> 24;
                int length = w2 & 16777215;
                if (control) {
                    int version = (2147418112 & w1) >>> 16;
                    int type = w1 & 65535;
                    if (version != Spdy3.VERSION) {
                        throw new ProtocolException("version != 3: " + version);
                    }
                    switch (type) {
                        case Spdy3.TYPE_SYN_STREAM /*1*/:
                            readSynStream(handler, flags, length);
                            return true;
                        case Spdy3.TYPE_SYN_REPLY /*2*/:
                            readSynReply(handler, flags, length);
                            return true;
                        case Spdy3.VERSION /*3*/:
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
                handler.data((flags & Spdy3.TYPE_SYN_STREAM) != 0, w1 & Integer.MAX_VALUE, this.source, length);
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
            handler.headers((flags & Spdy3.TYPE_SYN_REPLY) != 0, (flags & Spdy3.TYPE_SYN_STREAM) != 0, streamId, associatedStreamId, headerBlock, HeadersMode.SPDY_SYN_STREAM);
        }

        private void readSynReply(Handler handler, int flags, int length) throws IOException {
            handler.headers(false, (flags & Spdy3.TYPE_SYN_STREAM) != 0, this.source.readInt() & Integer.MAX_VALUE, -1, this.headerBlockReader.readNameValueBlock(length - 4), HeadersMode.SPDY_REPLY);
        }

        private void readRstStream(Handler handler, int flags, int length) throws IOException {
            if (length != Spdy3.TYPE_HEADERS) {
                Object[] objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(length);
                throw ioException("TYPE_RST_STREAM length: %d != 8", objArr);
            }
            int streamId = this.source.readInt() & Integer.MAX_VALUE;
            int errorCodeInt = this.source.readInt();
            ErrorCode errorCode = ErrorCode.fromSpdy3Rst(errorCodeInt);
            if (errorCode == null) {
                objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(errorCodeInt);
                throw ioException("TYPE_RST_STREAM unexpected error code: %d", objArr);
            }
            handler.rstStream(streamId, errorCode);
        }

        private void readHeaders(Handler handler, int flags, int length) throws IOException {
            handler.headers(false, false, this.source.readInt() & Integer.MAX_VALUE, -1, this.headerBlockReader.readNameValueBlock(length - 4), HeadersMode.SPDY_HEADERS);
        }

        private void readWindowUpdate(Handler handler, int flags, int length) throws IOException {
            if (length != Spdy3.TYPE_HEADERS) {
                Object[] objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(length);
                throw ioException("TYPE_WINDOW_UPDATE length: %d != 8", objArr);
            }
            int streamId = this.source.readInt() & Integer.MAX_VALUE;
            long increment = (long) (this.source.readInt() & Integer.MAX_VALUE);
            if (increment == 0) {
                objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Long.valueOf(increment);
                throw ioException("windowSizeIncrement was 0", objArr);
            }
            handler.windowUpdate(streamId, increment);
        }

        private void readPing(Handler handler, int flags, int length) throws IOException {
            boolean ack = true;
            if (length != Spdy3.TYPE_SETTINGS) {
                Object[] objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(length);
                throw ioException("TYPE_PING length: %d != 4", objArr);
            }
            boolean z;
            int id = this.source.readInt();
            boolean z2 = this.client;
            if ((id & Spdy3.TYPE_SYN_STREAM) == Spdy3.TYPE_SYN_STREAM) {
                z = true;
            } else {
                z = Spdy3.TYPE_DATA;
            }
            if (z2 != z) {
                ack = false;
            }
            handler.ping(ack, id, Spdy3.TYPE_DATA);
        }

        private void readGoAway(Handler handler, int flags, int length) throws IOException {
            if (length != Spdy3.TYPE_HEADERS) {
                Object[] objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(length);
                throw ioException("TYPE_GOAWAY length: %d != 8", objArr);
            }
            int lastGoodStreamId = this.source.readInt() & Integer.MAX_VALUE;
            int errorCodeInt = this.source.readInt();
            ErrorCode errorCode = ErrorCode.fromSpdyGoAway(errorCodeInt);
            if (errorCode == null) {
                objArr = new Object[Spdy3.TYPE_SYN_STREAM];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(errorCodeInt);
                throw ioException("TYPE_GOAWAY unexpected error code: %d", objArr);
            }
            handler.goAway(lastGoodStreamId, errorCode, ByteString.EMPTY);
        }

        private void readSettings(Handler handler, int flags, int length) throws IOException {
            boolean clearPrevious = true;
            int numberOfEntries = this.source.readInt();
            if (length != (numberOfEntries * Spdy3.TYPE_HEADERS) + Spdy3.TYPE_SETTINGS) {
                Object[] objArr = new Object[Spdy3.TYPE_SYN_REPLY];
                objArr[Spdy3.TYPE_DATA] = Integer.valueOf(length);
                objArr[Spdy3.TYPE_SYN_STREAM] = Integer.valueOf(numberOfEntries);
                throw ioException("TYPE_SETTINGS length: %d != 4 + 8 * %d", objArr);
            }
            Settings settings = new Settings();
            for (int i = Spdy3.TYPE_DATA; i < numberOfEntries; i += Spdy3.TYPE_SYN_STREAM) {
                int w1 = this.source.readInt();
                int id = w1 & 16777215;
                settings.set(id, (-16777216 & w1) >>> 24, this.source.readInt());
            }
            if ((flags & Spdy3.TYPE_SYN_STREAM) == 0) {
                clearPrevious = false;
            }
            handler.settings(clearPrevious, settings);
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
        private final Buffer headerBlockBuffer;
        private final BufferedSink headerBlockOut;
        private final BufferedSink sink;

        Writer(BufferedSink sink, boolean client) {
            this.sink = sink;
            this.client = client;
            Deflater deflater = new Deflater();
            deflater.setDictionary(Spdy3.DICTIONARY);
            this.headerBlockBuffer = new Buffer();
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
            int i = Spdy3.TYPE_SYN_STREAM;
            int i2 = Spdy3.TYPE_DATA;
            synchronized (this) {
                if (this.closed) {
                    throw new IOException("closed");
                }
                writeNameValueBlockToBuffer(headerBlock);
                int length = (int) (this.headerBlockBuffer.size() + 10);
                if (!outFinished) {
                    i = Spdy3.TYPE_DATA;
                }
                if (inFinished) {
                    i2 = Spdy3.TYPE_SYN_REPLY;
                }
                int flags = i | i2;
                this.sink.writeInt(-2147287039);
                this.sink.writeInt(((flags & 255) << 24) | (16777215 & length));
                this.sink.writeInt(streamId & Integer.MAX_VALUE);
                this.sink.writeInt(associatedStreamId & Integer.MAX_VALUE);
                this.sink.writeShort(Spdy3.TYPE_DATA);
                this.sink.writeAll(this.headerBlockBuffer);
                this.sink.flush();
            }
        }

        public synchronized void synReply(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            writeNameValueBlockToBuffer(headerBlock);
            int flags = outFinished ? Spdy3.TYPE_SYN_STREAM : Spdy3.TYPE_DATA;
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
            sendDataFrame(streamId, outFinished ? Spdy3.TYPE_SYN_STREAM : Spdy3.TYPE_DATA, source, byteCount);
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
            for (int i = Spdy3.TYPE_DATA; i < size; i += Spdy3.TYPE_SYN_STREAM) {
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
            for (int i = Spdy3.TYPE_DATA; i <= 10; i += Spdy3.TYPE_SYN_STREAM) {
                if (settings.isSet(i)) {
                    this.sink.writeInt(((settings.flags(i) & 255) << 24) | (i & 16777215));
                    this.sink.writeInt(settings.get(i));
                }
            }
            this.sink.flush();
        }

        public synchronized void ping(boolean reply, int payload1, int payload2) throws IOException {
            boolean payloadIsReply = true;
            synchronized (this) {
                if (this.closed) {
                    throw new IOException("closed");
                }
                boolean z;
                boolean z2 = this.client;
                if ((payload1 & Spdy3.TYPE_SYN_STREAM) == Spdy3.TYPE_SYN_STREAM) {
                    z = true;
                } else {
                    z = Spdy3.TYPE_DATA;
                }
                if (z2 == z) {
                    payloadIsReply = false;
                }
                if (reply != payloadIsReply) {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.Spdy3.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.Spdy3.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.Spdy3.<clinit>():void");
    }

    public Protocol getProtocol() {
        return Protocol.SPDY_3;
    }

    public FrameReader newReader(BufferedSource source, boolean client) {
        return new Reader(source, client);
    }

    public FrameWriter newWriter(BufferedSink sink, boolean client) {
        return new Writer(sink, client);
    }
}
