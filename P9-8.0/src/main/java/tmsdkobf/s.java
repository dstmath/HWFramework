package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class s extends JceStruct {
    static ArrayList<p> aq = new ArrayList();
    public long al = 0;
    public long am = 0;
    public ArrayList<p> ap = null;

    static {
        aq.add(new p());
    }

    public JceStruct newInit() {
        return new s();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.al = jceInputStream.read(this.al, 0, false);
        this.am = jceInputStream.read(this.am, 1, false);
        this.ap = (ArrayList) jceInputStream.read(aq, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.al != 0) {
            jceOutputStream.write(this.al, 0);
        }
        if (this.am != 0) {
            jceOutputStream.write(this.am, 1);
        }
        if (this.ap != null) {
            jceOutputStream.write(this.ap, 2);
        }
    }
}
