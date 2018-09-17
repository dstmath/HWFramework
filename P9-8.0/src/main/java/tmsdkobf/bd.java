package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bd extends JceStruct {
    static ArrayList<bp> cl = new ArrayList();
    static bg cq = new bg();
    public int cj = 0;
    public ArrayList<bp> ck = null;
    public bg cm = null;
    public int cp = 0;
    public String imsi = "";
    public String sms = "";
    public int time = 0;

    static {
        cl.add(new bp());
    }

    public JceStruct newInit() {
        return new bd();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.sms = jceInputStream.readString(0, true);
        this.time = jceInputStream.read(this.time, 1, true);
        this.cm = (bg) jceInputStream.read(cq, 2, true);
        this.cp = jceInputStream.read(this.cp, 3, true);
        this.ck = (ArrayList) jceInputStream.read(cl, 4, false);
        this.imsi = jceInputStream.readString(5, false);
        this.cj = jceInputStream.read(this.cj, 6, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.sms, 0);
        jceOutputStream.write(this.time, 1);
        jceOutputStream.write(this.cm, 2);
        jceOutputStream.write(this.cp, 3);
        if (this.ck != null) {
            jceOutputStream.write(this.ck, 4);
        }
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 5);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 6);
        }
    }
}
