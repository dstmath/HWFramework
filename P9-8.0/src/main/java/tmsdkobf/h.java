package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class h extends JceStruct {
    static ArrayList<Integer> G = new ArrayList();
    static ArrayList<f> H = new ArrayList();
    static e k = new e();
    public ArrayList<f> A = null;
    public int B = 0;
    public boolean C = true;
    public boolean D = true;
    public int E = 120;
    public int F = 10;
    public int hash = 0;
    public int interval = 0;
    public e j = null;
    public ArrayList<Integer> z = null;

    static {
        G.add(Integer.valueOf(0));
        H.add(new f());
    }

    public JceStruct newInit() {
        return new h();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hash = jceInputStream.read(this.hash, 0, true);
        this.interval = jceInputStream.read(this.interval, 1, false);
        this.z = (ArrayList) jceInputStream.read(G, 2, false);
        this.A = (ArrayList) jceInputStream.read(H, 3, false);
        this.B = jceInputStream.read(this.B, 4, false);
        this.C = jceInputStream.read(this.C, 5, false);
        this.D = jceInputStream.read(this.D, 6, false);
        this.E = jceInputStream.read(this.E, 7, false);
        this.F = jceInputStream.read(this.F, 8, false);
        this.j = (e) jceInputStream.read(k, 9, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hash, 0);
        if (this.interval != 0) {
            jceOutputStream.write(this.interval, 1);
        }
        if (this.z != null) {
            jceOutputStream.write(this.z, 2);
        }
        if (this.A != null) {
            jceOutputStream.write(this.A, 3);
        }
        if (this.B != 0) {
            jceOutputStream.write(this.B, 4);
        }
        jceOutputStream.write(this.C, 5);
        jceOutputStream.write(this.D, 6);
        if (this.E != 120) {
            jceOutputStream.write(this.E, 7);
        }
        if (this.F != 10) {
            jceOutputStream.write(this.F, 8);
        }
        if (this.j != null) {
            jceOutputStream.write(this.j, 9);
        }
    }
}
