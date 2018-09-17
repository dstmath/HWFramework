package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class eo extends JceStruct {
    static fg kx = new fg();
    static ArrayList<ff> ky = new ArrayList();
    public fg kv = null;
    public ArrayList<ff> kw = null;

    static {
        ky.add(new ff());
    }

    public JceStruct newInit() {
        return new eo();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.kv = (fg) jceInputStream.read(kx, 0, true);
        this.kw = (ArrayList) jceInputStream.read(ky, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kv, 0);
        jceOutputStream.write(this.kw, 1);
    }
}
