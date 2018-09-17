package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class aq extends JceStruct {
    static Map<Integer, ArrayList<String>> bJ = new HashMap();
    public int bC = 0;
    public Map<Integer, ArrayList<String>> bI = null;

    static {
        Integer valueOf = Integer.valueOf(0);
        ArrayList arrayList = new ArrayList();
        arrayList.add("");
        bJ.put(valueOf, arrayList);
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bC = jceInputStream.read(this.bC, 0, true);
        this.bI = (Map) jceInputStream.read(bJ, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bC, 0);
        jceOutputStream.write(this.bI, 1);
    }
}
