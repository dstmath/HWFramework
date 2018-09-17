package tmsdk.common.module.qscanner.impl;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.HashMap;
import java.util.Map;

public final class g extends JceStruct {
    static Map<Integer, Integer> gH = new HashMap();
    public Map<Integer, Integer> gE = null;
    public int gF = 0;
    public int gG = 0;
    public String path = "";

    static {
        gH.put(Integer.valueOf(0), Integer.valueOf(0));
    }

    public JceStruct newInit() {
        return new g();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.path = jceInputStream.readString(0, false);
        this.gE = (Map) jceInputStream.read(gH, 1, false);
        this.gF = jceInputStream.read(this.gF, 2, false);
        this.gG = jceInputStream.read(this.gG, 3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.path != null) {
            jceOutputStream.write(this.path, 0);
        }
        if (this.gE != null) {
            jceOutputStream.write(this.gE, 1);
        }
        if (this.gF != 0) {
            jceOutputStream.write(this.gF, 2);
        }
        if (this.gG != 0) {
            jceOutputStream.write(this.gG, 3);
        }
    }
}
