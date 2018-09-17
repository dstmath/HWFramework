package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bb extends JceStruct {
    static ArrayList<bp> cl = new ArrayList();
    static bg cq = new bg();
    static bm cr = new bm();
    public int cj = 0;
    public ArrayList<bp> ck = null;
    public bg cm = null;
    public int cn = 3;
    public bm co = null;
    public int cp = 1;
    public String imsi = "";
    public String sms = "";
    public int time = 0;
    public int type = 0;

    static {
        cl.add(new bp());
    }

    public JceStruct newInit() {
        return new bb();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.sms = jceInputStream.readString(0, true);
        this.time = jceInputStream.read(this.time, 1, true);
        this.cm = (bg) jceInputStream.read(cq, 2, true);
        this.type = jceInputStream.read(this.type, 3, true);
        this.ck = (ArrayList) jceInputStream.read(cl, 4, false);
        this.cn = jceInputStream.read(this.cn, 5, false);
        this.co = (bm) jceInputStream.read(cr, 6, false);
        this.cp = jceInputStream.read(this.cp, 7, false);
        this.imsi = jceInputStream.readString(8, false);
        this.cj = jceInputStream.read(this.cj, 9, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.sms, 0);
        jceOutputStream.write(this.time, 1);
        jceOutputStream.write(this.cm, 2);
        jceOutputStream.write(this.type, 3);
        if (this.ck != null) {
            jceOutputStream.write(this.ck, 4);
        }
        if (3 != this.cn) {
            jceOutputStream.write(this.cn, 5);
        }
        if (this.co != null) {
            jceOutputStream.write(this.co, 6);
        }
        if (1 != this.cp) {
            jceOutputStream.write(this.cp, 7);
        }
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 8);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 9);
        }
    }
}
