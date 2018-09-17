package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ck extends JceStruct {
    static ArrayList<cp> fb = new ArrayList();
    public ArrayList<cp> eZ = null;
    public int fa = 0;

    static {
        fb.add(new cp());
    }

    public JceStruct newInit() {
        return new ck();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.eZ = (ArrayList) jceInputStream.read(fb, 0, true);
        this.fa = jceInputStream.read(this.fa, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.eZ, 0);
        if (this.fa != 0) {
            jceOutputStream.write(this.fa, 1);
        }
    }
}
