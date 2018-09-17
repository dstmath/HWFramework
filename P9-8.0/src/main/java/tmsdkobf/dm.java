package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class dm extends JceStruct {
    static ArrayList<Integer> hd = new ArrayList();
    public int gS = 0;
    public int gT = 0;
    public int gU = 0;
    public ArrayList<Integer> gV = null;
    public int gY = 0;
    public int gn = 0;
    public int hv = 0;
    public int hy = 0;
    public String hz = "";

    static {
        hd.add(Integer.valueOf(0));
    }

    public JceStruct newInit() {
        return new dm();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hv = jceInputStream.read(this.hv, 0, true);
        this.gS = jceInputStream.read(this.gS, 1, false);
        this.gT = jceInputStream.read(this.gT, 2, false);
        this.gU = jceInputStream.read(this.gU, 3, false);
        this.gV = (ArrayList) jceInputStream.read(hd, 4, false);
        this.gY = jceInputStream.read(this.gY, 5, false);
        this.hy = jceInputStream.read(this.hy, 6, false);
        this.hz = jceInputStream.readString(7, false);
        this.gn = jceInputStream.read(this.gn, 8, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hv, 0);
        jceOutputStream.write(this.gS, 1);
        if (this.gT != 0) {
            jceOutputStream.write(this.gT, 2);
        }
        if (this.gU != 0) {
            jceOutputStream.write(this.gU, 3);
        }
        if (this.gV != null) {
            jceOutputStream.write(this.gV, 4);
        }
        if (this.gY != 0) {
            jceOutputStream.write(this.gY, 5);
        }
        jceOutputStream.write(this.hy, 6);
        if (this.hz != null) {
            jceOutputStream.write(this.hz, 7);
        }
        jceOutputStream.write(this.gn, 8);
    }
}
