package org.bouncycastle.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Strings;

class LineReader {
    private int lastC = -1;
    private final InputStream src;

    LineReader(InputStream inputStream) {
        this.src = inputStream;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0038  */
    public String readLine() throws IOException {
        int read;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = this.lastC;
        if (i != -1) {
            if (i == 13) {
                return "";
            }
            this.lastC = -1;
            if (i >= 0 || i == 13 || i == 10) {
                if (i == 13 && (read = this.src.read()) != 10 && read >= 0) {
                    this.lastC = read;
                }
                if (i >= 0) {
                    return null;
                }
                return Strings.fromUTF8ByteArray(byteArrayOutputStream.toByteArray());
            }
            byteArrayOutputStream.write(i);
        }
        i = this.src.read();
        if (i >= 0) {
        }
        this.lastC = read;
        if (i >= 0) {
        }
    }
}
