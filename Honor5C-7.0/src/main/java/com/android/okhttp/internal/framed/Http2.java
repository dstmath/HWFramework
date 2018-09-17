package com.android.okhttp.internal.framed;

import com.android.okhttp.Protocol;
import com.android.okhttp.internal.framed.FrameReader.Handler;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.Source;
import com.android.okhttp.okio.Timeout;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Http2 implements Variant {
    private static final ByteString CONNECTION_PREFACE = null;
    static final byte FLAG_ACK = (byte) 1;
    static final byte FLAG_COMPRESSED = (byte) 32;
    static final byte FLAG_END_HEADERS = (byte) 4;
    static final byte FLAG_END_PUSH_PROMISE = (byte) 4;
    static final byte FLAG_END_STREAM = (byte) 1;
    static final byte FLAG_NONE = (byte) 0;
    static final byte FLAG_PADDED = (byte) 8;
    static final byte FLAG_PRIORITY = (byte) 32;
    static final int INITIAL_MAX_FRAME_SIZE = 16384;
    static final byte TYPE_CONTINUATION = (byte) 9;
    static final byte TYPE_DATA = (byte) 0;
    static final byte TYPE_GOAWAY = (byte) 7;
    static final byte TYPE_HEADERS = (byte) 1;
    static final byte TYPE_PING = (byte) 6;
    static final byte TYPE_PRIORITY = (byte) 2;
    static final byte TYPE_PUSH_PROMISE = (byte) 5;
    static final byte TYPE_RST_STREAM = (byte) 3;
    static final byte TYPE_SETTINGS = (byte) 4;
    static final byte TYPE_WINDOW_UPDATE = (byte) 8;
    private static final Logger logger = null;

    static final class ContinuationSource implements Source {
        byte flags;
        int left;
        int length;
        short padding;
        private final BufferedSource source;
        int streamId;

        public ContinuationSource(BufferedSource source) {
            this.source = source;
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            while (this.left == 0) {
                this.source.skip((long) this.padding);
                this.padding = (short) 0;
                if ((this.flags & 4) != 0) {
                    return -1;
                }
                readContinuationHeader();
            }
            long read = this.source.read(sink, Math.min(byteCount, (long) this.left));
            if (read == -1) {
                return -1;
            }
            this.left = (int) (((long) this.left) - read);
            return read;
        }

        public Timeout timeout() {
            return this.source.timeout();
        }

        public void close() throws IOException {
        }

        private void readContinuationHeader() throws IOException {
            int previousStreamId = this.streamId;
            int -wrap1 = Http2.readMedium(this.source);
            this.left = -wrap1;
            this.length = -wrap1;
            byte type = (byte) (this.source.readByte() & 255);
            this.flags = (byte) (this.source.readByte() & 255);
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(FrameLogger.formatHeader(true, this.streamId, this.length, type, this.flags));
            }
            this.streamId = this.source.readInt() & Integer.MAX_VALUE;
            if (type != 9) {
                throw Http2.ioException("%s != TYPE_CONTINUATION", Byte.valueOf(type));
            } else if (this.streamId != previousStreamId) {
                throw Http2.ioException("TYPE_CONTINUATION streamId changed", new Object[0]);
            }
        }
    }

    static final class FrameLogger {
        private static final String[] BINARY = null;
        private static final String[] FLAGS = null;
        private static final String[] TYPES = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.Http2.FrameLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.Http2.FrameLogger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.Http2.FrameLogger.<clinit>():void");
        }

        FrameLogger() {
        }

        static String formatHeader(boolean inbound, int streamId, int length, byte type, byte flags) {
            String formattedType = type < TYPES.length ? TYPES[type] : String.format("0x%02x", new Object[]{Byte.valueOf(type)});
            String formattedFlags = formatFlags(type, flags);
            String str = "%s 0x%08x %5d %-13s %s";
            Object[] objArr = new Object[5];
            objArr[0] = inbound ? "<<" : ">>";
            objArr[1] = Integer.valueOf(streamId);
            objArr[2] = Integer.valueOf(length);
            objArr[3] = formattedType;
            objArr[4] = formattedFlags;
            return String.format(str, objArr);
        }

        static String formatFlags(byte type, byte flags) {
            if (flags == null) {
                return "";
            }
            switch (type) {
                case (byte) 2:
                case (byte) 3:
                case (byte) 7:
                case (byte) 8:
                    return BINARY[flags];
                case (byte) 4:
                case (byte) 6:
                    return flags == 1 ? "ACK" : BINARY[flags];
                default:
                    String result = flags < FLAGS.length ? FLAGS[flags] : BINARY[flags];
                    if (type == 5 && (flags & 4) != 0) {
                        return result.replace("HEADERS", "PUSH_PROMISE");
                    }
                    if (type != null || (flags & 32) == 0) {
                        return result;
                    }
                    return result.replace("PRIORITY", "COMPRESSED");
            }
        }
    }

    static final class Reader implements FrameReader {
        private final boolean client;
        private final ContinuationSource continuation;
        final Reader hpackReader;
        private final BufferedSource source;

        Reader(BufferedSource source, int headerTableSize, boolean client) {
            this.source = source;
            this.client = client;
            this.continuation = new ContinuationSource(this.source);
            this.hpackReader = new Reader(headerTableSize, this.continuation);
        }

        public void readConnectionPreface() throws IOException {
            if (!this.client) {
                ByteString connectionPreface = this.source.readByteString((long) Http2.CONNECTION_PREFACE.size());
                if (Http2.logger.isLoggable(Level.FINE)) {
                    Http2.logger.fine(String.format("<< CONNECTION %s", new Object[]{connectionPreface.hex()}));
                }
                if (!Http2.CONNECTION_PREFACE.equals(connectionPreface)) {
                    throw Http2.ioException("Expected a connection header but was %s", connectionPreface.utf8());
                }
            }
        }

        public boolean nextFrame(Handler handler) throws IOException {
            try {
                this.source.require(9);
                int length = Http2.readMedium(this.source);
                if (length < 0 || length > Http2.INITIAL_MAX_FRAME_SIZE) {
                    throw Http2.ioException("FRAME_SIZE_ERROR: %s", Integer.valueOf(length));
                }
                byte type = (byte) (this.source.readByte() & 255);
                byte flags = (byte) (this.source.readByte() & 255);
                int streamId = this.source.readInt() & Integer.MAX_VALUE;
                if (Http2.logger.isLoggable(Level.FINE)) {
                    Http2.logger.fine(FrameLogger.formatHeader(true, streamId, length, type, flags));
                }
                switch (type) {
                    case (byte) 0:
                        readData(handler, length, flags, streamId);
                        break;
                    case (byte) 1:
                        readHeaders(handler, length, flags, streamId);
                        break;
                    case (byte) 2:
                        readPriority(handler, length, flags, streamId);
                        break;
                    case (byte) 3:
                        readRstStream(handler, length, flags, streamId);
                        break;
                    case (byte) 4:
                        readSettings(handler, length, flags, streamId);
                        break;
                    case (byte) 5:
                        readPushPromise(handler, length, flags, streamId);
                        break;
                    case (byte) 6:
                        readPing(handler, length, flags, streamId);
                        break;
                    case (byte) 7:
                        readGoAway(handler, length, flags, streamId);
                        break;
                    case (byte) 8:
                        readWindowUpdate(handler, length, flags, streamId);
                        break;
                    default:
                        this.source.skip((long) length);
                        break;
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        private void readHeaders(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (streamId == 0) {
                throw Http2.ioException("PROTOCOL_ERROR: TYPE_HEADERS streamId == 0", new Object[0]);
            }
            boolean endStream = (flags & 1) != 0;
            short readByte = (flags & 8) != 0 ? (short) (this.source.readByte() & 255) : (short) 0;
            if ((flags & 32) != 0) {
                readPriority(handler, streamId);
                length -= 5;
            }
            handler.headers(false, endStream, streamId, -1, readHeaderBlock(Http2.lengthWithoutPadding(length, flags, readByte), readByte, flags, streamId), HeadersMode.HTTP_20_HEADERS);
        }

        private List<Header> readHeaderBlock(int length, short padding, byte flags, int streamId) throws IOException {
            ContinuationSource continuationSource = this.continuation;
            this.continuation.left = length;
            continuationSource.length = length;
            this.continuation.padding = padding;
            this.continuation.flags = flags;
            this.continuation.streamId = streamId;
            this.hpackReader.readHeaders();
            return this.hpackReader.getAndResetHeaderList();
        }

        private void readData(Handler handler, int length, byte flags, int streamId) throws IOException {
            boolean gzipped;
            short padding = (short) 0;
            boolean inFinished = (flags & 1) != 0;
            if ((flags & 32) != 0) {
                gzipped = true;
            } else {
                gzipped = false;
            }
            if (gzipped) {
                throw Http2.ioException("PROTOCOL_ERROR: FLAG_COMPRESSED without SETTINGS_COMPRESS_DATA", new Object[0]);
            }
            if ((flags & 8) != 0) {
                padding = (short) (this.source.readByte() & 255);
            }
            handler.data(inFinished, streamId, this.source, Http2.lengthWithoutPadding(length, flags, padding));
            this.source.skip((long) padding);
        }

        private void readPriority(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (length != 5) {
                throw Http2.ioException("TYPE_PRIORITY length: %d != 5", Integer.valueOf(length));
            } else if (streamId == 0) {
                throw Http2.ioException("TYPE_PRIORITY streamId == 0", new Object[0]);
            } else {
                readPriority(handler, streamId);
            }
        }

        private void readPriority(Handler handler, int streamId) throws IOException {
            int w1 = this.source.readInt();
            handler.priority(streamId, w1 & Integer.MAX_VALUE, (this.source.readByte() & 255) + 1, (Integer.MIN_VALUE & w1) != 0);
        }

        private void readRstStream(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (length != 4) {
                throw Http2.ioException("TYPE_RST_STREAM length: %d != 4", Integer.valueOf(length));
            } else if (streamId == 0) {
                throw Http2.ioException("TYPE_RST_STREAM streamId == 0", new Object[0]);
            } else {
                ErrorCode errorCode = ErrorCode.fromHttp2(this.source.readInt());
                if (errorCode == null) {
                    throw Http2.ioException("TYPE_RST_STREAM unexpected error code: %d", Integer.valueOf(errorCodeInt));
                } else {
                    handler.rstStream(streamId, errorCode);
                }
            }
        }

        private void readSettings(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (streamId != 0) {
                throw Http2.ioException("TYPE_SETTINGS streamId != 0", new Object[0]);
            } else if ((flags & 1) != 0) {
                if (length != 0) {
                    throw Http2.ioException("FRAME_SIZE_ERROR ack frame should be empty!", new Object[0]);
                }
                handler.ackSettings();
            } else if (length % 6 != 0) {
                throw Http2.ioException("TYPE_SETTINGS length %% 6 != 0: %s", Integer.valueOf(length));
            } else {
                Settings settings = new Settings();
                for (int i = 0; i < length; i += 6) {
                    short id = this.source.readShort();
                    int value = this.source.readInt();
                    switch (id) {
                        case (short) 1:
                        case (short) 6:
                            break;
                        case (short) 2:
                            if (!(value == 0 || value == 1)) {
                                throw Http2.ioException("PROTOCOL_ERROR SETTINGS_ENABLE_PUSH != 0 or 1", new Object[0]);
                            }
                        case (short) 3:
                            id = (short) 4;
                            break;
                        case (short) 4:
                            id = (short) 7;
                            if (value >= 0) {
                                break;
                            }
                            throw Http2.ioException("PROTOCOL_ERROR SETTINGS_INITIAL_WINDOW_SIZE > 2^31 - 1", new Object[0]);
                        case (short) 5:
                            if (value >= Http2.INITIAL_MAX_FRAME_SIZE && value <= 16777215) {
                                break;
                            }
                            throw Http2.ioException("PROTOCOL_ERROR SETTINGS_MAX_FRAME_SIZE: %s", Integer.valueOf(value));
                            break;
                        default:
                            throw Http2.ioException("PROTOCOL_ERROR invalid settings id: %s", Short.valueOf(id));
                    }
                    settings.set(id, 0, value);
                }
                handler.settings(false, settings);
                if (settings.getHeaderTableSize() >= 0) {
                    this.hpackReader.headerTableSizeSetting(settings.getHeaderTableSize());
                }
            }
        }

        private void readPushPromise(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (streamId == 0) {
                throw Http2.ioException("PROTOCOL_ERROR: TYPE_PUSH_PROMISE streamId == 0", new Object[0]);
            }
            short readByte = (flags & 8) != 0 ? (short) (this.source.readByte() & 255) : (short) 0;
            handler.pushPromise(streamId, this.source.readInt() & Integer.MAX_VALUE, readHeaderBlock(Http2.lengthWithoutPadding(length - 4, flags, readByte), readByte, flags, streamId));
        }

        private void readPing(Handler handler, int length, byte flags, int streamId) throws IOException {
            boolean ack = true;
            if (length != 8) {
                throw Http2.ioException("TYPE_PING length != 8: %s", Integer.valueOf(length));
            } else if (streamId != 0) {
                throw Http2.ioException("TYPE_PING streamId != 0", new Object[0]);
            } else {
                int payload1 = this.source.readInt();
                int payload2 = this.source.readInt();
                if ((flags & 1) == 0) {
                    ack = false;
                }
                handler.ping(ack, payload1, payload2);
            }
        }

        private void readGoAway(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (length < 8) {
                throw Http2.ioException("TYPE_GOAWAY length < 8: %s", Integer.valueOf(length));
            } else if (streamId != 0) {
                throw Http2.ioException("TYPE_GOAWAY streamId != 0", new Object[0]);
            } else {
                int lastStreamId = this.source.readInt();
                int opaqueDataLength = length - 8;
                ErrorCode errorCode = ErrorCode.fromHttp2(this.source.readInt());
                if (errorCode == null) {
                    throw Http2.ioException("TYPE_GOAWAY unexpected error code: %d", Integer.valueOf(errorCodeInt));
                }
                ByteString debugData = ByteString.EMPTY;
                if (opaqueDataLength > 0) {
                    debugData = this.source.readByteString((long) opaqueDataLength);
                }
                handler.goAway(lastStreamId, errorCode, debugData);
            }
        }

        private void readWindowUpdate(Handler handler, int length, byte flags, int streamId) throws IOException {
            if (length != 4) {
                throw Http2.ioException("TYPE_WINDOW_UPDATE length !=4: %s", Integer.valueOf(length));
            }
            long increment = ((long) this.source.readInt()) & 2147483647L;
            if (increment == 0) {
                throw Http2.ioException("windowSizeIncrement was 0", Long.valueOf(increment));
            } else {
                handler.windowUpdate(streamId, increment);
            }
        }

        public void close() throws IOException {
            this.source.close();
        }
    }

    static final class Writer implements FrameWriter {
        private final boolean client;
        private boolean closed;
        private final Buffer hpackBuffer;
        private final Writer hpackWriter;
        private int maxFrameSize;
        private final BufferedSink sink;

        Writer(BufferedSink sink, boolean client) {
            this.sink = sink;
            this.client = client;
            this.hpackBuffer = new Buffer();
            this.hpackWriter = new Writer(this.hpackBuffer);
            this.maxFrameSize = Http2.INITIAL_MAX_FRAME_SIZE;
        }

        public synchronized void flush() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.sink.flush();
        }

        public synchronized void ackSettings(Settings peerSettings) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.maxFrameSize = peerSettings.getMaxFrameSize(this.maxFrameSize);
            frameHeader(0, 0, Http2.TYPE_SETTINGS, Http2.TYPE_HEADERS);
            this.sink.flush();
        }

        public synchronized void connectionPreface() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (this.client) {
                if (Http2.logger.isLoggable(Level.FINE)) {
                    Http2.logger.fine(String.format(">> CONNECTION %s", new Object[]{Http2.CONNECTION_PREFACE.hex()}));
                }
                this.sink.write(Http2.CONNECTION_PREFACE.toByteArray());
                this.sink.flush();
            }
        }

        public synchronized void synStream(boolean outFinished, boolean inFinished, int streamId, int associatedStreamId, List<Header> headerBlock) throws IOException {
            if (inFinished) {
                throw new UnsupportedOperationException();
            } else if (this.closed) {
                throw new IOException("closed");
            } else {
                headers(outFinished, streamId, headerBlock);
            }
        }

        public synchronized void synReply(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            headers(outFinished, streamId, headerBlock);
        }

        public synchronized void headers(int streamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            headers(false, streamId, headerBlock);
        }

        public synchronized void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.hpackWriter.writeHeaders(requestHeaders);
            long byteCount = this.hpackBuffer.size();
            int length = (int) Math.min((long) (this.maxFrameSize - 4), byteCount);
            frameHeader(streamId, length + 4, Http2.TYPE_PUSH_PROMISE, byteCount == ((long) length) ? Http2.TYPE_SETTINGS : Http2.TYPE_DATA);
            this.sink.writeInt(Integer.MAX_VALUE & promisedStreamId);
            this.sink.write(this.hpackBuffer, (long) length);
            if (byteCount > ((long) length)) {
                writeContinuationFrames(streamId, byteCount - ((long) length));
            }
        }

        void headers(boolean outFinished, int streamId, List<Header> headerBlock) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            this.hpackWriter.writeHeaders(headerBlock);
            long byteCount = this.hpackBuffer.size();
            int length = (int) Math.min((long) this.maxFrameSize, byteCount);
            byte flags = byteCount == ((long) length) ? Http2.TYPE_SETTINGS : Http2.TYPE_DATA;
            if (outFinished) {
                flags = (byte) (flags | 1);
            }
            frameHeader(streamId, length, Http2.TYPE_HEADERS, flags);
            this.sink.write(this.hpackBuffer, (long) length);
            if (byteCount > ((long) length)) {
                writeContinuationFrames(streamId, byteCount - ((long) length));
            }
        }

        private void writeContinuationFrames(int streamId, long byteCount) throws IOException {
            while (byteCount > 0) {
                int length = (int) Math.min((long) this.maxFrameSize, byteCount);
                byteCount -= (long) length;
                frameHeader(streamId, length, Http2.TYPE_CONTINUATION, byteCount == 0 ? Http2.TYPE_SETTINGS : Http2.TYPE_DATA);
                this.sink.write(this.hpackBuffer, (long) length);
            }
        }

        public synchronized void rstStream(int streamId, ErrorCode errorCode) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (errorCode.httpCode == -1) {
                throw new IllegalArgumentException();
            } else {
                frameHeader(streamId, 4, Http2.TYPE_RST_STREAM, Http2.TYPE_DATA);
                this.sink.writeInt(errorCode.httpCode);
                this.sink.flush();
            }
        }

        public int maxDataLength() {
            return this.maxFrameSize;
        }

        public synchronized void data(boolean outFinished, int streamId, Buffer source, int byteCount) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            byte flags = Http2.TYPE_DATA;
            if (outFinished) {
                flags = (byte) 1;
            }
            dataFrame(streamId, flags, source, byteCount);
        }

        void dataFrame(int streamId, byte flags, Buffer buffer, int byteCount) throws IOException {
            frameHeader(streamId, byteCount, Http2.TYPE_DATA, flags);
            if (byteCount > 0) {
                this.sink.write(buffer, (long) byteCount);
            }
        }

        public synchronized void settings(Settings settings) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            frameHeader(0, settings.size() * 6, Http2.TYPE_SETTINGS, Http2.TYPE_DATA);
            for (int i = 0; i < 10; i++) {
                if (settings.isSet(i)) {
                    int id = i;
                    if (i == 4) {
                        id = 3;
                    } else if (id == 7) {
                        id = 4;
                    }
                    this.sink.writeShort(id);
                    this.sink.writeInt(settings.get(i));
                }
            }
            this.sink.flush();
        }

        public synchronized void ping(boolean ack, int payload1, int payload2) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            frameHeader(0, 8, Http2.TYPE_PING, ack ? Http2.TYPE_HEADERS : Http2.TYPE_DATA);
            this.sink.writeInt(payload1);
            this.sink.writeInt(payload2);
            this.sink.flush();
        }

        public synchronized void goAway(int lastGoodStreamId, ErrorCode errorCode, byte[] debugData) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (errorCode.httpCode == -1) {
                throw Http2.illegalArgument("errorCode.httpCode == -1", new Object[0]);
            } else {
                frameHeader(0, debugData.length + 8, Http2.TYPE_GOAWAY, Http2.TYPE_DATA);
                this.sink.writeInt(lastGoodStreamId);
                this.sink.writeInt(errorCode.httpCode);
                if (debugData.length > 0) {
                    this.sink.write(debugData);
                }
                this.sink.flush();
            }
        }

        public synchronized void windowUpdate(int streamId, long windowSizeIncrement) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            } else if (windowSizeIncrement == 0 || windowSizeIncrement > 2147483647L) {
                throw Http2.illegalArgument("windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL: %s", Long.valueOf(windowSizeIncrement));
            } else {
                frameHeader(streamId, 4, Http2.TYPE_WINDOW_UPDATE, Http2.TYPE_DATA);
                this.sink.writeInt((int) windowSizeIncrement);
                this.sink.flush();
            }
        }

        public synchronized void close() throws IOException {
            this.closed = true;
            this.sink.close();
        }

        void frameHeader(int streamId, int length, byte type, byte flags) throws IOException {
            if (Http2.logger.isLoggable(Level.FINE)) {
                Http2.logger.fine(FrameLogger.formatHeader(false, streamId, length, type, flags));
            }
            if (length > this.maxFrameSize) {
                throw Http2.illegalArgument("FRAME_SIZE_ERROR length > %d: %d", Integer.valueOf(this.maxFrameSize), Integer.valueOf(length));
            } else if ((Integer.MIN_VALUE & streamId) != 0) {
                throw Http2.illegalArgument("reserved bit set: %s", Integer.valueOf(streamId));
            } else {
                Http2.writeMedium(this.sink, length);
                this.sink.writeByte(type & 255);
                this.sink.writeByte(flags & 255);
                this.sink.writeInt(Integer.MAX_VALUE & streamId);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.Http2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.Http2.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.Http2.<clinit>():void");
    }

    public Http2() {
    }

    public Protocol getProtocol() {
        return Protocol.HTTP_2;
    }

    public FrameReader newReader(BufferedSource source, boolean client) {
        return new Reader(source, 4096, client);
    }

    public FrameWriter newWriter(BufferedSink sink, boolean client) {
        return new Writer(sink, client);
    }

    private static IllegalArgumentException illegalArgument(String message, Object... args) {
        throw new IllegalArgumentException(String.format(message, args));
    }

    private static IOException ioException(String message, Object... args) throws IOException {
        throw new IOException(String.format(message, args));
    }

    private static int lengthWithoutPadding(int length, byte flags, short padding) throws IOException {
        if ((flags & 8) != 0) {
            short length2 = length - 1;
        }
        if (padding <= length2) {
            return (short) (length2 - padding);
        }
        throw ioException("PROTOCOL_ERROR padding %s > remaining length %s", Short.valueOf(padding), Integer.valueOf(length2));
    }

    private static int readMedium(BufferedSource source) throws IOException {
        return (((source.readByte() & 255) << 16) | ((source.readByte() & 255) << 8)) | (source.readByte() & 255);
    }

    private static void writeMedium(BufferedSink sink, int i) throws IOException {
        sink.writeByte((i >>> 16) & 255);
        sink.writeByte((i >>> 8) & 255);
        sink.writeByte(i & 255);
    }
}
