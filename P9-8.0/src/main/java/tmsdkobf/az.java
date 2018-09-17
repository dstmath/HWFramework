package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class az extends JceStruct {
    static ArrayList<bp> cl = new ArrayList();
    public int cj = 0;
    public ArrayList<bp> ck = null;
    public String imsi = "";

    static {
        cl.add(new bp());
    }

    public JceStruct newInit() {
        return new az();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ck = (ArrayList) jceInputStream.read(cl, 0, true);
        this.imsi = jceInputStream.readString(1, false);
        this.cj = jceInputStream.read(this.cj, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ck, 0);
        if (this.imsi != null) {
            jceOutputStream.write(this.imsi, 1);
        }
        if (this.cj != 0) {
            jceOutputStream.write(this.cj, 2);
        }
    }
}
