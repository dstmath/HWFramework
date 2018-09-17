package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.HashMap;
import java.util.Map;

public final class dh extends JceStruct {
    static Map<Integer, Integer> gH = new HashMap();
    public Map<Integer, Integer> gE = null;
    public int gF = 0;
    public int gG = 0;

    static {
        gH.put(Integer.valueOf(0), Integer.valueOf(0));
    }

    public JceStruct newInit() {
        return new dh();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gE = (Map) jceInputStream.read(gH, 0, false);
        this.gF = jceInputStream.read(this.gF, 1, false);
        this.gG = jceInputStream.read(this.gG, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gE != null) {
            jceOutputStream.write(this.gE, 0);
        }
        if (this.gF != 0) {
            jceOutputStream.write(this.gF, 1);
        }
        if (this.gG != 0) {
            jceOutputStream.write(this.gG, 2);
        }
    }
}
