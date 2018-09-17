package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class df extends JceStruct {
    static dj gA = new dj();
    static ArrayList<dk> gB = new ArrayList();
    public dj gy = null;
    public ArrayList<dk> gz = null;

    static {
        gB.add(new dk());
    }

    public JceStruct newInit() {
        return new df();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gy = (dj) jceInputStream.read(gA, 0, false);
        this.gz = (ArrayList) jceInputStream.read(gB, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gy != null) {
            jceOutputStream.write(this.gy, 0);
        }
        if (this.gz != null) {
            jceOutputStream.write(this.gz, 1);
        }
    }
}
