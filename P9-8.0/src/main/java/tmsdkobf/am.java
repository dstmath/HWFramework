package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class am extends JceStruct {
    static ArrayList<Map<Integer, String>> bx = new ArrayList();
    static byte[] by = new byte[1];
    public ArrayList<Map<Integer, String>> bv = null;
    public byte[] bw = null;

    static {
        Map hashMap = new HashMap();
        hashMap.put(Integer.valueOf(0), "");
        bx.add(hashMap);
        by[0] = (byte) 0;
    }

    public JceStruct newInit() {
        return new am();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bv = (ArrayList) jceInputStream.read(bx, 0, true);
        this.bw = jceInputStream.read(by, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bv, 0);
        if (this.bw != null) {
            jceOutputStream.write(this.bw, 1);
        }
    }
}
