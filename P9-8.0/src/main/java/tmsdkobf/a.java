package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class a extends JceStruct {
    static ArrayList<b> b = new ArrayList();
    public ArrayList<b> a = null;

    static {
        b.add(new b());
    }

    public JceStruct newInit() {
        return new a();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.a = (ArrayList) jceInputStream.read(b, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.a, 0);
    }
}
