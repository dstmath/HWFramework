package com.huawei.okhttp3.internal.ws;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.ByteString;
import com.huawei.okio.Sink;
import com.huawei.okio.Timeout;
import java.io.IOException;
import java.util.Random;

final class WebSocketWriter {
    static final /* synthetic */ boolean -assertionsDisabled = (WebSocketWriter.class.desiredAssertionStatus() ^ 1);
    boolean activeWriter;
    final Buffer buffer = new Buffer();
    final FrameSink frameSink = new FrameSink();
    final boolean isClient;
    final byte[] maskBuffer;
    final byte[] maskKey;
    final Random random;
    final BufferedSink sink;
    boolean writerClosed;

    final class FrameSink implements Sink {
        boolean closed;
        long contentLength;
        int formatOpcode;
        boolean isFirstFrame;

        FrameSink() {
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            WebSocketWriter.this.buffer.write(source, byteCount);
            boolean deferWrite = (!this.isFirstFrame || this.contentLength == -1) ? false : WebSocketWriter.this.buffer.size() > this.contentLength - 8192;
            long emitCount = WebSocketWriter.this.buffer.completeSegmentByteCount();
            if (emitCount > 0 && (deferWrite ^ 1) != 0) {
                synchronized (WebSocketWriter.this) {
                    WebSocketWriter.this.writeMessageFrameSynchronized(this.formatOpcode, emitCount, this.isFirstFrame, false);
                }
                this.isFirstFrame = false;
            }
        }

        public void flush() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            synchronized (WebSocketWriter.this) {
                WebSocketWriter.this.writeMessageFrameSynchronized(this.formatOpcode, WebSocketWriter.this.buffer.size(), this.isFirstFrame, false);
            }
            this.isFirstFrame = false;
        }

        public Timeout timeout() {
            return WebSocketWriter.this.sink.timeout();
        }

        public void close() throws IOException {
            if (this.closed) {
                throw new IOException("closed");
            }
            synchronized (WebSocketWriter.this) {
                WebSocketWriter.this.writeMessageFrameSynchronized(this.formatOpcode, WebSocketWriter.this.buffer.size(), this.isFirstFrame, true);
            }
            this.closed = true;
            WebSocketWriter.this.activeWriter = false;
        }
    }

    WebSocketWriter(boolean isClient, BufferedSink sink, Random random) {
        byte[] bArr = null;
        if (sink == null) {
            throw new NullPointerException("sink == null");
        } else if (random == null) {
            throw new NullPointerException("random == null");
        } else {
            byte[] bArr2;
            this.isClient = isClient;
            this.sink = sink;
            this.random = random;
            if (isClient) {
                bArr2 = new byte[4];
            } else {
                bArr2 = null;
            }
            this.maskKey = bArr2;
            if (isClient) {
                bArr = new byte[8192];
            }
            this.maskBuffer = bArr;
        }
    }

    void writePing(ByteString payload) throws IOException {
        synchronized (this) {
            writeControlFrameSynchronized(9, payload);
        }
    }

    void writePong(ByteString payload) throws IOException {
        synchronized (this) {
            writeControlFrameSynchronized(10, payload);
        }
    }

    void writeClose(int code, ByteString reason) throws IOException {
        ByteString payload = ByteString.EMPTY;
        if (!(code == 0 && reason == null)) {
            if (code != 0) {
                WebSocketProtocol.validateCloseCode(code);
            }
            Buffer buffer = new Buffer();
            buffer.writeShort(code);
            if (reason != null) {
                buffer.write(reason);
            }
            payload = buffer.readByteString();
        }
        synchronized (this) {
            try {
                writeControlFrameSynchronized(8, payload);
                this.writerClosed = true;
            } catch (Throwable th) {
                this.writerClosed = true;
            }
        }
    }

    private void writeControlFrameSynchronized(int opcode, ByteString payload) throws IOException {
        if (!-assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.writerClosed) {
            throw new IOException("closed");
        } else {
            int length = payload.size();
            if (((long) length) > 125) {
                throw new IllegalArgumentException("Payload size must be less than or equal to 125");
            }
            this.sink.writeByte(opcode | AppOpsManagerEx.TYPE_MICROPHONE);
            int b1 = length;
            if (this.isClient) {
                this.sink.writeByte(length | AppOpsManagerEx.TYPE_MICROPHONE);
                this.random.nextBytes(this.maskKey);
                this.sink.write(this.maskKey);
                byte[] bytes = payload.toByteArray();
                WebSocketProtocol.toggleMask(bytes, (long) bytes.length, this.maskKey, 0);
                this.sink.write(bytes);
            } else {
                this.sink.writeByte(length);
                this.sink.write(payload);
            }
            this.sink.flush();
        }
    }

    Sink newMessageSink(int formatOpcode, long contentLength) {
        if (this.activeWriter) {
            throw new IllegalStateException("Another message writer is active. Did you call close()?");
        }
        this.activeWriter = true;
        this.frameSink.formatOpcode = formatOpcode;
        this.frameSink.contentLength = contentLength;
        this.frameSink.isFirstFrame = true;
        this.frameSink.closed = false;
        return this.frameSink;
    }

    void writeMessageFrameSynchronized(int formatOpcode, long byteCount, boolean isFirstFrame, boolean isFinal) throws IOException {
        if (!-assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.writerClosed) {
            throw new IOException("closed");
        } else {
            int b0 = isFirstFrame ? formatOpcode : 0;
            if (isFinal) {
                b0 |= AppOpsManagerEx.TYPE_MICROPHONE;
            }
            this.sink.writeByte(b0);
            int b1 = 0;
            if (this.isClient) {
                b1 = AppOpsManagerEx.TYPE_MICROPHONE;
            }
            if (byteCount <= 125) {
                this.sink.writeByte(b1 | ((int) byteCount));
            } else if (byteCount <= 65535) {
                this.sink.writeByte(b1 | JlogConstantsEx.JLID_NEW_CONTACT_SELECT_ACCOUNT);
                this.sink.writeShort((int) byteCount);
            } else {
                this.sink.writeByte(b1 | 127);
                this.sink.writeLong(byteCount);
            }
            if (this.isClient) {
                this.random.nextBytes(this.maskKey);
                this.sink.write(this.maskKey);
                long written = 0;
                while (written < byteCount) {
                    int read = this.buffer.read(this.maskBuffer, 0, (int) Math.min(byteCount, (long) this.maskBuffer.length));
                    if (read == -1) {
                        throw new AssertionError();
                    }
                    WebSocketProtocol.toggleMask(this.maskBuffer, (long) read, this.maskKey, written);
                    this.sink.write(this.maskBuffer, 0, read);
                    written += (long) read;
                }
            } else {
                this.sink.write(this.buffer, byteCount);
            }
            this.sink.emit();
        }
    }
}
