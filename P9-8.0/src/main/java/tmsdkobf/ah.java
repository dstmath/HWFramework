package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ah extends JceStruct {
    static ArrayList<am> aV = new ArrayList();
    public ArrayList<am> aU = null;

    static {
        aV.add(new am());
    }

    public JceStruct newInit() {
        return new ah();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aU = (ArrayList) jceInputStream.read(aV, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aU, 0);
    }
}
