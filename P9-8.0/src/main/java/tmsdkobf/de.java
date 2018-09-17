package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class de extends JceStruct {
    static ArrayList<String> gw = new ArrayList();
    static ArrayList<String> gx = new ArrayList();
    public int gq = 0;
    public int gr = 0;
    public int gs = 0;
    public ArrayList<String> gt = null;
    public ArrayList<String> gu = null;
    public String gv = "";

    static {
        gw.add("");
        gx.add("");
    }

    public JceStruct newInit() {
        return new de();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gq = jceInputStream.read(this.gq, 0, false);
        this.gr = jceInputStream.read(this.gr, 1, false);
        this.gs = jceInputStream.read(this.gs, 2, false);
        this.gt = (ArrayList) jceInputStream.read(gw, 3, false);
        this.gu = (ArrayList) jceInputStream.read(gx, 4, false);
        this.gv = jceInputStream.readString(5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.gq, 0);
        if (this.gr != 0) {
            jceOutputStream.write(this.gr, 1);
        }
        if (this.gs != 0) {
            jceOutputStream.write(this.gs, 2);
        }
        if (this.gt != null) {
            jceOutputStream.write(this.gt, 3);
        }
        if (this.gu != null) {
            jceOutputStream.write(this.gu, 4);
        }
        if (this.gv != null) {
            jceOutputStream.write(this.gv, 5);
        }
    }
}
