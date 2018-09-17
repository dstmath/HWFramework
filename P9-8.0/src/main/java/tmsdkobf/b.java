package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class b extends JceStruct {
    static ArrayList<Integer> f = new ArrayList();
    static ArrayList<String> g = new ArrayList();
    public int c = 0;
    public int count = 1;
    public ArrayList<Integer> d = null;
    public ArrayList<String> e = null;
    public int timestamp = 0;

    static {
        f.add(Integer.valueOf(0));
        g.add("");
    }

    public JceStruct newInit() {
        return new b();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.c = jceInputStream.read(this.c, 0, true);
        this.timestamp = jceInputStream.read(this.timestamp, 1, true);
        this.count = jceInputStream.read(this.count, 2, false);
        this.d = (ArrayList) jceInputStream.read(f, 3, false);
        this.e = (ArrayList) jceInputStream.read(g, 4, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.c, 0);
        jceOutputStream.write(this.timestamp, 1);
        if (this.count != 1) {
            jceOutputStream.write(this.count, 2);
        }
        if (this.d != null) {
            jceOutputStream.write(this.d, 3);
        }
        if (this.e != null) {
            jceOutputStream.write(this.e, 4);
        }
    }
}
