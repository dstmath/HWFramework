package tmsdkobf;

import java.io.IOException;
import java.io.InputStream;

public class lt {
    public static ls c(InputStream inputStream) {
        ls lsVar = new ls();
        byte[] bArr = new byte[4];
        try {
            inputStream.read(bArr);
            lsVar.sW = lq.k(bArr);
            inputStream.read(bArr);
            lsVar.yT = lq.k(bArr);
            bArr = new byte[16];
            inputStream.read(bArr);
            lsVar.yU = bArr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lsVar;
    }
}
