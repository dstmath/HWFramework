package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.util.Strings;

public class URLAndHash {
    protected byte[] sha1Hash;
    protected String url;

    public URLAndHash(String str, byte[] bArr) {
        if (str == null || str.length() < 1 || str.length() >= 65536) {
            throw new IllegalArgumentException("'url' must have length from 1 to (2^16 - 1)");
        } else if (bArr == null || bArr.length == 20) {
            this.url = str;
            this.sha1Hash = bArr;
        } else {
            throw new IllegalArgumentException("'sha1Hash' must have length == 20, if present");
        }
    }

    public static URLAndHash parse(TlsContext tlsContext, InputStream inputStream) throws IOException {
        byte[] readOpaque16 = TlsUtils.readOpaque16(inputStream);
        if (readOpaque16.length >= 1) {
            String fromByteArray = Strings.fromByteArray(readOpaque16);
            byte[] bArr = null;
            switch (TlsUtils.readUint8(inputStream)) {
                case 0:
                    if (TlsUtils.isTLSv12(tlsContext)) {
                        throw new TlsFatalAlert(47);
                    }
                    break;
                case 1:
                    bArr = TlsUtils.readFully(20, inputStream);
                    break;
                default:
                    throw new TlsFatalAlert(47);
            }
            return new URLAndHash(fromByteArray, bArr);
        }
        throw new TlsFatalAlert(47);
    }

    public void encode(OutputStream outputStream) throws IOException {
        TlsUtils.writeOpaque16(Strings.toByteArray(this.url), outputStream);
        if (this.sha1Hash == null) {
            TlsUtils.writeUint8(0, outputStream);
            return;
        }
        TlsUtils.writeUint8(1, outputStream);
        outputStream.write(this.sha1Hash);
    }

    public byte[] getSHA1Hash() {
        return this.sha1Hash;
    }

    public String getURL() {
        return this.url;
    }
}
