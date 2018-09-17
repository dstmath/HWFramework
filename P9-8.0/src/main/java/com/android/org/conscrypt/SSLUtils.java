package com.android.org.conscrypt;

import java.nio.ByteBuffer;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

public final class SSLUtils {
    private static final int MAX_ENCRYPTION_OVERHEAD_DIFF = 2147483562;
    private static final int MAX_ENCRYPTION_OVERHEAD_LENGTH = 85;
    static final int MAX_PROTOCOL_LENGTH = 255;
    static final boolean USE_ENGINE_SOCKET_BY_DEFAULT = Boolean.parseBoolean(System.getProperty("com.android.org.conscrypt.useEngineSocketByDefault"));

    public static int calculateOutNetBufSize(int pendingBytes) {
        return Math.min(NativeConstants.SSL3_RT_MAX_PACKET_SIZE, Math.min(MAX_ENCRYPTION_OVERHEAD_DIFF, pendingBytes) + MAX_ENCRYPTION_OVERHEAD_LENGTH);
    }

    static SSLHandshakeException toSSLHandshakeException(Throwable e) {
        if (e instanceof SSLHandshakeException) {
            return (SSLHandshakeException) e;
        }
        return (SSLHandshakeException) new SSLHandshakeException(e.getMessage()).initCause(e);
    }

    static SSLException toSSLException(Throwable e) {
        if (e instanceof SSLException) {
            return (SSLException) e;
        }
        return new SSLException(e);
    }

    public static int getEncryptedPacketLength(ByteBuffer[] buffers, int offset) {
        ByteBuffer buffer = buffers[offset];
        if (buffer.remaining() >= 5) {
            return getEncryptedPacketLength(buffer);
        }
        ByteBuffer tmp = ByteBuffer.allocate(5);
        while (true) {
            int offset2 = offset + 1;
            buffer = buffers[offset];
            int pos = buffer.position();
            int limit = buffer.limit();
            if (buffer.remaining() > tmp.remaining()) {
                buffer.limit(tmp.remaining() + pos);
            }
            try {
                tmp.put(buffer);
                if (tmp.hasRemaining()) {
                    offset = offset2;
                } else {
                    tmp.flip();
                    return getEncryptedPacketLength(tmp);
                }
            } finally {
                buffer.limit(limit);
                buffer.position(pos);
            }
        }
    }

    public static byte[] toLengthPrefixedList(String... protocols) {
        int i;
        int protocolLength;
        int length = 0;
        for (i = 0; i < protocols.length; i++) {
            protocolLength = protocols[i].length();
            if (protocolLength == 0 || protocolLength > MAX_PROTOCOL_LENGTH) {
                throw new IllegalArgumentException("Protocol has invalid length (" + protocolLength + "): " + protocols[i]);
            }
            length += protocolLength + 1;
        }
        byte[] data = new byte[length];
        int dataIndex = 0;
        i = 0;
        while (i < protocols.length) {
            String protocol = protocols[i];
            protocolLength = protocol.length();
            int dataIndex2 = dataIndex + 1;
            data[dataIndex] = (byte) protocolLength;
            int ci = 0;
            while (ci < protocolLength) {
                char c = protocol.charAt(ci);
                if (c > 127) {
                    throw new IllegalArgumentException("Protocol contains invalid character: " + c + "(protocol=" + protocol + ")");
                }
                dataIndex = dataIndex2 + 1;
                data[dataIndex2] = (byte) c;
                ci++;
                dataIndex2 = dataIndex;
            }
            i++;
            dataIndex = dataIndex2;
        }
        return data;
    }

    private static int getEncryptedPacketLength(ByteBuffer buffer) {
        int pos = buffer.position();
        switch (unsignedByte(buffer.get(pos))) {
            case NativeConstants.SSL3_RT_CHANGE_CIPHER_SPEC /*20*/:
            case NativeConstants.SSL3_RT_ALERT /*21*/:
            case NativeConstants.SSL3_RT_HANDSHAKE /*22*/:
            case NativeConstants.SSL3_RT_APPLICATION_DATA /*23*/:
                if (unsignedByte(buffer.get(pos + 1)) != 3) {
                    return -1;
                }
                int packetLength = unsignedShort(buffer.getShort(pos + 3)) + 5;
                if (packetLength <= 5) {
                    return -1;
                }
                return packetLength;
            default:
                return -1;
        }
    }

    private static short unsignedByte(byte b) {
        return (short) (b & MAX_PROTOCOL_LENGTH);
    }

    private static int unsignedShort(short s) {
        return 65535 & s;
    }

    private SSLUtils() {
    }
}
