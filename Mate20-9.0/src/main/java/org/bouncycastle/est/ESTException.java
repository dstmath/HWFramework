package org.bouncycastle.est;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;

public class ESTException extends IOException {
    private static final long MAX_ERROR_BODY = 8192;
    private InputStream body;
    private Throwable cause;
    private int statusCode;

    public ESTException(String str) {
        this(str, null);
    }

    public ESTException(String str, Throwable th) {
        super(str);
        this.cause = th;
        this.body = null;
        this.statusCode = 0;
    }

    public ESTException(String str, Throwable th, int i, InputStream inputStream) {
        super(str);
        this.cause = th;
        this.statusCode = i;
        if (inputStream != null) {
            byte[] bArr = new byte[PKIFailureInfo.certRevoked];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                try {
                    int read = inputStream.read(bArr);
                    if (read < 0) {
                        break;
                    } else if (((long) (byteArrayOutputStream.size() + read)) > MAX_ERROR_BODY) {
                        byteArrayOutputStream.write(bArr, 0, PKIFailureInfo.certRevoked - byteArrayOutputStream.size());
                        break;
                    } else {
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                } catch (Exception e) {
                    return;
                }
            }
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            this.body = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            inputStream.close();
            return;
        }
        this.body = null;
    }

    public InputStream getBody() {
        return this.body;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public String getMessage() {
        return super.getMessage() + " HTTP Status Code: " + this.statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
