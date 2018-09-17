package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class dl extends JceStruct {
    static ArrayList<Integer> hd = new ArrayList();
    public int gS = 0;
    public int gT = 0;
    public int gU = 0;
    public ArrayList<Integer> gV = null;
    public int gW = 0;
    public boolean gX = false;
    public int gY = 0;
    public int gn = 0;
    public String hA = "";
    public String hB = "";
    public int hv = 0;
    public String hw = "";
    public String hx = "";
    public int hy = 0;
    public String hz = "";
    public int official = 0;

    static {
        hd.add(Integer.valueOf(0));
    }

    public JceStruct newInit() {
        return new dl();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hv = jceInputStream.read(this.hv, 0, true);
        this.gS = jceInputStream.read(this.gS, 1, false);
        this.gT = jceInputStream.read(this.gT, 2, false);
        this.gU = jceInputStream.read(this.gU, 3, false);
        this.gV = (ArrayList) jceInputStream.read(hd, 4, false);
        this.gW = jceInputStream.read(this.gW, 5, false);
        this.gX = jceInputStream.read(this.gX, 6, false);
        this.gY = jceInputStream.read(this.gY, 7, false);
        this.official = jceInputStream.read(this.official, 8, false);
        this.hw = jceInputStream.readString(9, false);
        this.hx = jceInputStream.readString(10, false);
        this.hy = jceInputStream.read(this.hy, 11, false);
        this.hz = jceInputStream.readString(12, false);
        this.hA = jceInputStream.readString(13, false);
        this.hB = jceInputStream.readString(14, false);
        this.gn = jceInputStream.read(this.gn, 15, false);
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
        if (this.gW != 0) {
            jceOutputStream.write(this.gW, 5);
        }
        jceOutputStream.write(this.gX, 6);
        if (this.gY != 0) {
            jceOutputStream.write(this.gY, 7);
        }
        jceOutputStream.write(this.official, 8);
        if (this.hw != null) {
            jceOutputStream.write(this.hw, 9);
        }
        if (this.hx != null) {
            jceOutputStream.write(this.hx, 10);
        }
        jceOutputStream.write(this.hy, 11);
        if (this.hz != null) {
            jceOutputStream.write(this.hz, 12);
        }
        if (this.hA != null) {
            jceOutputStream.write(this.hA, 13);
        }
        if (this.hB != null) {
            jceOutputStream.write(this.hB, 14);
        }
        jceOutputStream.write(this.gn, 15);
    }
}
