package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class cf extends JceStruct {
    static ArrayList<ce> eR = new ArrayList();
    public ArrayList<ce> eQ = null;
    public int ey = 0;
    public int ez = 0;

    static {
        eR.add(new ce());
    }

    public JceStruct newInit() {
        return new cf();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ey = jceInputStream.read(this.ey, 0, false);
        this.ez = jceInputStream.read(this.ez, 1, false);
        this.eQ = (ArrayList) jceInputStream.read(eR, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.ey != 0) {
            jceOutputStream.write(this.ey, 0);
        }
        if (this.ez != 0) {
            jceOutputStream.write(this.ez, 1);
        }
        if (this.eQ != null) {
            jceOutputStream.write(this.eQ, 2);
        }
    }
}
