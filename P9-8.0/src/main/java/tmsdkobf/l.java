package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class l extends JceStruct {
    static ArrayList<q> W = new ArrayList();
    public ArrayList<q> V = null;

    static {
        W.add(new q());
    }

    public JceStruct newInit() {
        return new l();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.V = (ArrayList) jceInputStream.read(W, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.V != null) {
            jceOutputStream.write(this.V, 0);
        }
    }
}
