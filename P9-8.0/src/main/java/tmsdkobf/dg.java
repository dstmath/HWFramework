package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class dg extends JceStruct {
    static dj gA = new dj();
    static ArrayList<di> gD = new ArrayList();
    public ArrayList<di> gC = null;
    public dj gy = null;

    static {
        gD.add(new di());
    }

    public JceStruct newInit() {
        return new dg();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gy = (dj) jceInputStream.read(gA, 0, false);
        this.gC = (ArrayList) jceInputStream.read(gD, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gy != null) {
            jceOutputStream.write(this.gy, 0);
        }
        if (this.gC != null) {
            jceOutputStream.write(this.gC, 1);
        }
    }
}
