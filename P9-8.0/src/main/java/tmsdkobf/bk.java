package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bk extends JceStruct {
    static bj cM = new bj();
    static ArrayList<bj> cN = new ArrayList();
    public bj cK = null;
    public ArrayList<bj> cL = null;

    static {
        cN.add(new bj());
    }

    public JceStruct newInit() {
        return new bk();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cK = (bj) jceInputStream.read(cM, 0, true);
        this.cL = (ArrayList) jceInputStream.read(cN, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cK, 0);
        if (this.cL != null) {
            jceOutputStream.write(this.cL, 1);
        }
    }
}
