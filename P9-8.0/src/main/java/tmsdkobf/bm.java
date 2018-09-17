package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bm extends JceStruct {
    static ArrayList<bi> cV = new ArrayList();
    public ArrayList<bi> cU = null;

    static {
        cV.add(new bi());
    }

    public JceStruct newInit() {
        return new bm();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cU = (ArrayList) jceInputStream.read(cV, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.cU != null) {
            jceOutputStream.write(this.cU, 0);
        }
    }
}
