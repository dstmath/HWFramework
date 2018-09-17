package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ab extends JceStruct {
    static ArrayList<ae> aB = new ArrayList();
    public ArrayList<ae> aA = null;

    static {
        aB.add(new ae());
    }

    public JceStruct newInit() {
        return new ab();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aA = (ArrayList) jceInputStream.read(aB, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.aA != null) {
            jceOutputStream.write(this.aA, 0);
        }
    }
}
