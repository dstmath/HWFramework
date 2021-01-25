package ohos.bluetooth;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SppSocket {
    private static final int PARAM_LEN = 20;
    private static final int PORT_LEN = 4;
    private static final int SHORT_MASK = 65535;
    public static final int SOCKET_L2CAP = 3;
    public static final int SOCKET_L2CAP_BREDR = 3;
    public static final int SOCKET_L2CAP_LE = 4;
    public static final int SOCKET_RFCOMM = 1;
    public static final int SOCKET_SCO = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "SppSocket");
    private int mConnectionType;
    private SppInputStream mInputStream;
    private int mMaxRxBufferSize;
    private int mMaxTxBufferSize;
    private SppOutputStream mOutputStream;
    private int mPort;
    private ByteBuffer mPreparingData;

    public SppSocket(int i) {
        this.mConnectionType = i;
    }

    public void createSppSocket(FileDescriptor fileDescriptor) throws IOException {
        if (fileDescriptor != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor);
            FileInputStream fileInputStream = new FileInputStream(fileDescriptor);
            this.mOutputStream = new SppOutputStream(fileOutputStream);
            this.mInputStream = new SppInputStream(fileInputStream);
            if (!readParameter(fileInputStream)) {
                throw new IOException("read remote parameter fail");
            }
            return;
        }
        throw new IOException("createSppSocket when fd is null");
    }

    public int getSppClientConnectionType() {
        return this.mConnectionType;
    }

    public InputStream getInputStream() {
        return this.mInputStream;
    }

    public OutputStream getOutputStream() {
        return this.mOutputStream;
    }

    public int getMaxReceivePacketSize() {
        return this.mMaxRxBufferSize;
    }

    public int getMaxTransmitPacketSize() {
        return this.mMaxTxBufferSize;
    }

    public int getPort() {
        return this.mPort;
    }

    public void close() {
        SppOutputStream sppOutputStream = this.mOutputStream;
        if (sppOutputStream != null) {
            sppOutputStream.close();
        }
        SppInputStream sppInputStream = this.mInputStream;
        if (sppInputStream != null) {
            sppInputStream.close();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int fillBytes(InputStream inputStream, byte[] bArr) {
        try {
            int length = bArr.length;
            int length2 = bArr.length;
            while (length2 > 0) {
                int read = inputStream.read(bArr, length - length2, length2);
                if (read < 0) {
                    return -1;
                }
                length2 -= read;
            }
            if (length2 == 0) {
                return length;
            }
            return -1;
        } catch (IOException unused) {
        }
    }

    /* access modifiers changed from: package-private */
    public boolean readParameter(InputStream inputStream) {
        byte[] bArr = new byte[20];
        byte[] bArr2 = new byte[4];
        if (inputStream != null && fillBytes(inputStream, bArr2) == 4) {
            ByteBuffer wrap = ByteBuffer.wrap(bArr2);
            wrap.order(ByteOrder.nativeOrder());
            this.mPort = wrap.getInt();
            if (fillBytes(inputStream, bArr) == 20) {
                ByteBuffer wrap2 = ByteBuffer.wrap(bArr);
                wrap2.order(ByteOrder.nativeOrder());
                wrap2.getInt();
                wrap2.get(new byte[6]);
                wrap2.getInt();
                wrap2.getInt();
                this.mMaxTxBufferSize = wrap2.getShort() & 65535;
                this.mMaxRxBufferSize = wrap2.getShort() & 65535;
                int i = this.mConnectionType;
                if (i != 3 && i != 4) {
                    return true;
                }
                this.mPreparingData = ByteBuffer.wrap(new byte[this.mMaxRxBufferSize]);
                this.mPreparingData.limit(0);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public class SppInputStream extends InputStream {
        private FileInputStream mFileInputStream;

        public SppInputStream(FileInputStream fileInputStream) {
            this.mFileInputStream = fileInputStream;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            return read(new byte[1], 0, 1);
        }

        @Override // java.io.InputStream
        public int read(byte[] bArr, int i, int i2) throws IOException {
            int i3;
            if (SppSocket.this.mConnectionType == 3 || SppSocket.this.mConnectionType == 4) {
                if (SppSocket.this.mPreparingData.remaining() == 0) {
                    SppSocket sppSocket = SppSocket.this;
                    int fillBytes = sppSocket.fillBytes(this.mFileInputStream, sppSocket.mPreparingData.array());
                    if (fillBytes == -1) {
                        SppSocket.this.mPreparingData.limit(0);
                        return -1;
                    }
                    SppSocket.this.mPreparingData.limit(fillBytes);
                }
                if (i2 > SppSocket.this.mPreparingData.remaining()) {
                    i2 = SppSocket.this.mPreparingData.remaining();
                }
                SppSocket.this.mPreparingData.get(bArr, i, i2);
                i3 = i2;
            } else {
                i3 = this.mFileInputStream.read(bArr, i, i2);
            }
            if (i3 >= 0) {
                return i3;
            }
            throw new IOException("bt socket closed, read return: " + i3);
        }

        @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            try {
                this.mFileInputStream.close();
            } catch (IOException unused) {
                HiLog.error(SppSocket.TAG, "close input fail", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class SppOutputStream extends OutputStream {
        private FileOutputStream mFileOutputStream;

        public SppOutputStream(FileOutputStream fileOutputStream) {
            this.mFileOutputStream = fileOutputStream;
        }

        @Override // java.io.OutputStream
        public void write(byte[] bArr, int i, int i2) throws IOException {
            if (SppSocket.this.mConnectionType != 3 && SppSocket.this.mConnectionType != 4) {
                this.mFileOutputStream.write(bArr, i, i2);
            } else if (i2 <= SppSocket.this.mMaxTxBufferSize) {
                this.mFileOutputStream.write(bArr, i, i2);
            } else {
                while (i2 > 0) {
                    int i3 = i2 > SppSocket.this.mMaxTxBufferSize ? SppSocket.this.mMaxTxBufferSize : i2;
                    this.mFileOutputStream.write(bArr, i, i3);
                    i += i3;
                    i2 -= i3;
                }
            }
        }

        @Override // java.io.OutputStream
        public void write(int i) throws IOException {
            write(new byte[]{(byte) i}, 0, 1);
        }

        @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            try {
                this.mFileOutputStream.close();
            } catch (IOException unused) {
                HiLog.error(SppSocket.TAG, "close output fail", new Object[0]);
            }
        }
    }
}
