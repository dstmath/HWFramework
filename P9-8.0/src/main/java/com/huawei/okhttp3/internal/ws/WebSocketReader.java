package com.huawei.okhttp3.internal.ws;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.facerecognition.FaceCamera;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.ByteString;
import com.huawei.okio.Timeout;
import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

final class WebSocketReader {
    boolean closed;
    long frameBytesRead;
    final FrameCallback frameCallback;
    long frameLength;
    final boolean isClient;
    boolean isControlFrame;
    boolean isFinalFrame;
    boolean isMasked;
    final byte[] maskBuffer = new byte[8192];
    final byte[] maskKey = new byte[4];
    int opcode;
    final BufferedSource source;

    public interface FrameCallback {
        void onReadClose(int i, String str);

        void onReadMessage(ByteString byteString) throws IOException;

        void onReadMessage(String str) throws IOException;

        void onReadPing(ByteString byteString);

        void onReadPong(ByteString byteString);
    }

    WebSocketReader(boolean isClient, BufferedSource source, FrameCallback frameCallback) {
        if (source == null) {
            throw new NullPointerException("source == null");
        } else if (frameCallback == null) {
            throw new NullPointerException("frameCallback == null");
        } else {
            this.isClient = isClient;
            this.source = source;
            this.frameCallback = frameCallback;
        }
    }

    void processNextFrame() throws IOException {
        readHeader();
        if (this.isControlFrame) {
            readControlFrame();
        } else {
            readMessageFrame();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readHeader() throws IOException {
        int b0;
        boolean z;
        boolean reservedFlag1;
        boolean reservedFlag2;
        boolean reservedFlag3;
        boolean z2 = true;
        TimeUnit timeUnit = null;
        if (this.closed) {
            throw new IOException("closed");
        }
        Timeout readByte;
        long timeoutBefore = this.source.timeout().timeoutNanos();
        this.source.timeout().clearTimeout();
        try {
            readByte = this.source.readByte();
            b0 = readByte & 255;
        } finally {
            Timeout timeout = readByte;
            z2 = this.source.timeout();
            timeUnit = TimeUnit.NANOSECONDS;
            z2.timeout(timeoutBefore, timeUnit);
            if (readByte == null) {
            }
            this.isFinalFrame = z;
            if ((b0 & 8) == 0) {
            }
            this.isControlFrame = z;
            if (this.isControlFrame) {
            }
            if ((b0 & 64) == 0) {
            }
            if ((b0 & 32) == 0) {
            }
            if ((b0 & 16) == 0) {
            }
            if (!reservedFlag1) {
            }
            throw new ProtocolException("Reserved flags are unsupported.");
        }
        if (readByte == null) {
            z = z2;
        } else {
            z = timeUnit;
        }
        this.isFinalFrame = z;
        if ((b0 & 8) == 0) {
            z = z2;
        } else {
            z = timeUnit;
        }
        this.isControlFrame = z;
        if (this.isControlFrame || (this.isFinalFrame ^ 1) == 0) {
            reservedFlag1 = (b0 & 64) == 0;
            reservedFlag2 = (b0 & 32) == 0;
            reservedFlag3 = (b0 & 16) == 0;
            if (reservedFlag1 || reservedFlag2 || reservedFlag3) {
                throw new ProtocolException("Reserved flags are unsupported.");
            }
            int b1 = this.source.readByte() & 255;
            if ((b1 & AppOpsManagerEx.TYPE_MICROPHONE) == 0) {
                z2 = timeUnit;
            }
            this.isMasked = z2;
            if (this.isMasked == this.isClient) {
                String str;
                if (this.isClient) {
                    str = "Server-sent frames must not be masked.";
                } else {
                    str = "Client-sent frames must be masked.";
                }
                throw new ProtocolException(str);
            }
            this.frameLength = (long) (b1 & 127);
            if (this.frameLength == 126) {
                this.frameLength = ((long) this.source.readShort()) & 65535;
            } else if (this.frameLength == 127) {
                this.frameLength = this.source.readLong();
                if (this.frameLength < 0) {
                    throw new ProtocolException("Frame length 0x" + Long.toHexString(this.frameLength) + " > 0x7FFFFFFFFFFFFFFF");
                }
            }
            this.frameBytesRead = 0;
            if (this.isControlFrame && this.frameLength > 125) {
                throw new ProtocolException("Control frame must be less than 125B.");
            } else if (this.isMasked) {
                this.source.readFully(this.maskKey);
                return;
            } else {
                return;
            }
        }
        throw new ProtocolException("Control frames must be final.");
    }

    private void readControlFrame() throws IOException {
        Buffer buffer = new Buffer();
        if (this.frameBytesRead < this.frameLength) {
            if (this.isClient) {
                this.source.readFully(buffer, this.frameLength);
            } else {
                while (this.frameBytesRead < this.frameLength) {
                    int read = this.source.read(this.maskBuffer, 0, (int) Math.min(this.frameLength - this.frameBytesRead, (long) this.maskBuffer.length));
                    if (read == -1) {
                        throw new EOFException();
                    }
                    WebSocketProtocol.toggleMask(this.maskBuffer, (long) read, this.maskKey, this.frameBytesRead);
                    buffer.write(this.maskBuffer, 0, read);
                    this.frameBytesRead += (long) read;
                }
            }
        }
        switch (this.opcode) {
            case 8:
                int code = FaceCamera.RET_REPEAT_REQUEST_OK;
                String reason = "";
                long bufferSize = buffer.size();
                if (bufferSize == 1) {
                    throw new ProtocolException("Malformed close payload length of 1.");
                }
                if (bufferSize != 0) {
                    code = buffer.readShort();
                    reason = buffer.readUtf8();
                    String codeExceptionMessage = WebSocketProtocol.closeCodeExceptionMessage(code);
                    if (codeExceptionMessage != null) {
                        throw new ProtocolException(codeExceptionMessage);
                    }
                }
                this.frameCallback.onReadClose(code, reason);
                this.closed = true;
                return;
            case 9:
                this.frameCallback.onReadPing(buffer.readByteString());
                return;
            case 10:
                this.frameCallback.onReadPong(buffer.readByteString());
                return;
            default:
                throw new ProtocolException("Unknown control opcode: " + Integer.toHexString(this.opcode));
        }
    }

    private void readMessageFrame() throws IOException {
        int opcode = this.opcode;
        if (opcode == 1 || opcode == 2) {
            Buffer message = new Buffer();
            readMessage(message);
            if (opcode == 1) {
                this.frameCallback.onReadMessage(message.readUtf8());
                return;
            } else {
                this.frameCallback.onReadMessage(message.readByteString());
                return;
            }
        }
        throw new ProtocolException("Unknown opcode: " + Integer.toHexString(opcode));
    }

    void readUntilNonControlFrame() throws IOException {
        while (!this.closed) {
            readHeader();
            if (this.isControlFrame) {
                readControlFrame();
            } else {
                return;
            }
        }
    }

    private void readMessage(Buffer sink) throws IOException {
        while (!this.closed) {
            long read;
            if (this.frameBytesRead == this.frameLength) {
                if (!this.isFinalFrame) {
                    readUntilNonControlFrame();
                    if (this.opcode != 0) {
                        throw new ProtocolException("Expected continuation opcode. Got: " + Integer.toHexString(this.opcode));
                    } else if (this.isFinalFrame && this.frameLength == 0) {
                        return;
                    }
                }
                return;
            }
            long toRead = this.frameLength - this.frameBytesRead;
            if (this.isMasked) {
                read = (long) this.source.read(this.maskBuffer, 0, (int) Math.min(toRead, (long) this.maskBuffer.length));
                if (read == -1) {
                    throw new EOFException();
                }
                WebSocketProtocol.toggleMask(this.maskBuffer, read, this.maskKey, this.frameBytesRead);
                sink.write(this.maskBuffer, 0, (int) read);
            } else {
                read = this.source.read(sink, toRead);
                if (read == -1) {
                    throw new EOFException();
                }
            }
            this.frameBytesRead += read;
        }
        throw new IOException("closed");
    }
}
