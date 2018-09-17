package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class bn extends JceStruct {
    static ArrayList<bk> cY = new ArrayList();
    public long cW = 0;
    public ArrayList<bk> cX = null;

    static {
        cY.add(new bk());
    }

    public JceStruct newInit() {
        return new bn();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cW = jceInputStream.read(this.cW, 0, true);
        this.cX = (ArrayList) jceInputStream.read(cY, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cW, 0);
        if (this.cX != null) {
            jceOutputStream.write(this.cX, 1);
        }
    }
}
