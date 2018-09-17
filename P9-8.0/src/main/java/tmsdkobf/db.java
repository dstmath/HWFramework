package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class db extends JceStruct {
    static dd gl = new dd();
    static ArrayList<String> gm = new ArrayList();
    public dd gj = null;
    public ArrayList<String> gk = null;

    static {
        gm.add("");
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gj = (dd) jceInputStream.read(gl, 0, false);
        this.gk = (ArrayList) jceInputStream.read(gm, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gj != null) {
            jceOutputStream.write(this.gj, 0);
        }
        if (this.gk != null) {
            jceOutputStream.write(this.gk, 1);
        }
    }
}
