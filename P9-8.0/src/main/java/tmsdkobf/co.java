package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class co extends JceStruct {
    static ArrayList<cq> fj = new ArrayList();
    public ArrayList<cq> fi = null;

    static {
        fj.add(new cq());
    }

    public JceStruct newInit() {
        return new co();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fi = (ArrayList) jceInputStream.read(fj, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fi, 0);
    }
}
