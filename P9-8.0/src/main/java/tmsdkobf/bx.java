package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bx extends JceStruct {
    static i eK = new i();
    static ArrayList<bw> eL = new ArrayList();
    public int eH = 1;
    public i eI = null;
    public ArrayList<bw> eJ = null;
    public int ey = 0;
    public int ez = 0;

    static {
        eL.add(new bw());
    }

    public JceStruct newInit() {
        return new bx();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ey = jceInputStream.read(this.ey, 0, false);
        this.ez = jceInputStream.read(this.ez, 1, false);
        this.eH = jceInputStream.read(this.eH, 2, false);
        this.eI = (i) jceInputStream.read(eK, 3, false);
        this.eJ = (ArrayList) jceInputStream.read(eL, 4, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.ey != 0) {
            jceOutputStream.write(this.ey, 0);
        }
        if (this.ez != 0) {
            jceOutputStream.write(this.ez, 1);
        }
        if (this.eH != 1) {
            jceOutputStream.write(this.eH, 2);
        }
        if (this.eI != null) {
            jceOutputStream.write(this.eI, 3);
        }
        if (this.eJ != null) {
            jceOutputStream.write(this.eJ, 4);
        }
    }
}
