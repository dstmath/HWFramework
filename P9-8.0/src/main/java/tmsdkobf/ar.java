package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ar extends JceStruct {
    static ArrayList<byte[]> bQ = new ArrayList();
    public int bK = 0;
    public int bL = 0;
    public int bM = 0;
    public ArrayList<byte[]> bN = null;
    public int bO = 0;
    public boolean bP = false;

    static {
        byte[] bArr = new byte[1];
        bArr[0] = (byte) 0;
        bQ.add(bArr);
    }

    public JceStruct newInit() {
        return new ar();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bK = jceInputStream.read(this.bK, 0, true);
        this.bL = jceInputStream.read(this.bL, 1, true);
        this.bM = jceInputStream.read(this.bM, 2, true);
        this.bN = (ArrayList) jceInputStream.read(bQ, 3, true);
        this.bO = jceInputStream.read(this.bO, 4, false);
        this.bP = jceInputStream.read(this.bP, 5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bK, 0);
        jceOutputStream.write(this.bL, 1);
        jceOutputStream.write(this.bM, 2);
        jceOutputStream.write(this.bN, 3);
        if (this.bO != 0) {
            jceOutputStream.write(this.bO, 4);
        }
        if (this.bP) {
            jceOutputStream.write(this.bP, 5);
        }
    }
}
