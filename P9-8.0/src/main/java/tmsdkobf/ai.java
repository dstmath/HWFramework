package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ai extends JceStruct {
    static j bb = new j();
    static k bc = new k();
    static k bd = new k();
    static ArrayList<aj> be = new ArrayList();
    public String aW = "";
    public j aX = null;
    public k aY = null;
    public k aZ = null;
    public ArrayList<aj> ba = null;

    static {
        be.add(new aj());
    }

    public JceStruct newInit() {
        return new ai();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aW = jceInputStream.readString(0, true);
        this.aX = (j) jceInputStream.read(bb, 1, false);
        this.aY = (k) jceInputStream.read(bc, 2, false);
        this.aZ = (k) jceInputStream.read(bd, 3, false);
        this.ba = (ArrayList) jceInputStream.read(be, 4, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aW, 0);
        if (this.aX != null) {
            jceOutputStream.write(this.aX, 1);
        }
        if (this.aY != null) {
            jceOutputStream.write(this.aY, 2);
        }
        if (this.aZ != null) {
            jceOutputStream.write(this.aZ, 3);
        }
        if (this.ba != null) {
            jceOutputStream.write(this.ba, 4);
        }
    }
}
