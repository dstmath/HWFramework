package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class cj extends JceStruct {
    static ArrayList<cm> eY = new ArrayList();
    public ArrayList<cm> eV = null;
    public int eW = 0;
    public int eX = 0;
    public int version = 0;

    static {
        eY.add(new cm());
    }

    public JceStruct newInit() {
        return new cj();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.eV = (ArrayList) jceInputStream.read(eY, 0, true);
        this.eW = jceInputStream.read(this.eW, 1, false);
        this.eX = jceInputStream.read(this.eX, 2, false);
        this.version = jceInputStream.read(this.version, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.eV, 0);
        if (this.eW != 0) {
            jceOutputStream.write(this.eW, 1);
        }
        if (this.eX != 0) {
            jceOutputStream.write(this.eX, 2);
        }
        if (this.version != 0) {
            jceOutputStream.write(this.version, 3);
        }
    }
}
