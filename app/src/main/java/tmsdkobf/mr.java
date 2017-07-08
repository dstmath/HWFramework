package tmsdkobf;

import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public class mr {
    public static mq c(InputStream inputStream) {
        mq mqVar = new mq();
        byte[] bArr = new byte[4];
        try {
            inputStream.read(bArr);
            mqVar.vR = mo.k(bArr);
            inputStream.read(bArr);
            mqVar.Bi = mo.k(bArr);
            bArr = new byte[16];
            inputStream.read(bArr);
            mqVar.Bj = bArr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mqVar;
    }
}
