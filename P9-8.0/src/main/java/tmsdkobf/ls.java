package tmsdkobf;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ls {
    public int sW = 0;
    public int yT = 0;
    public byte[] yU;

    public ls() {
        byte[] bArr = new byte[16];
        for (int i = 0; i < 15; i++) {
            bArr[i] = (byte) 0;
        }
        this.yU = bArr;
    }

    public byte[] eD() {
        try {
            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.write(lq.aO(this.sW));
            dataOutputStream.write(lq.aO(this.yT));
            dataOutputStream.write(this.yU);
            dataOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
