package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class al extends JceStruct {
    static ArrayList<am> bu = new ArrayList();
    public ArrayList<am> bt = null;

    static {
        bu.add(new am());
    }

    public JceStruct newInit() {
        return new al();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bt = (ArrayList) jceInputStream.read(bu, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bt, 0);
    }
}
