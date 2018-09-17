package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class g extends JceStruct {
    static ArrayList<String> u = new ArrayList();
    static Map<String, String> v = new HashMap();
    public int hash = 0;
    public ArrayList<String> p = null;
    public int q = 0;
    public boolean r = false;
    public int s = 0;
    public Map<String, String> t = null;

    static {
        u.add("");
        v.put("", "");
    }

    public JceStruct newInit() {
        return new g();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hash = jceInputStream.read(this.hash, 0, true);
        this.p = (ArrayList) jceInputStream.read(u, 1, true);
        this.q = jceInputStream.read(this.q, 2, true);
        this.r = jceInputStream.read(this.r, 3, false);
        this.s = jceInputStream.read(this.s, 4, false);
        this.t = (Map) jceInputStream.read(v, 5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hash, 0);
        jceOutputStream.write(this.p, 1);
        jceOutputStream.write(this.q, 2);
        if (this.r) {
            jceOutputStream.write(this.r, 3);
        }
        jceOutputStream.write(this.s, 4);
        if (this.t != null) {
            jceOutputStream.write(this.t, 5);
        }
    }
}
