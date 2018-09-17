package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class t extends JceStruct {
    static ArrayList<String> as = new ArrayList();
    public ArrayList<String> ar = null;

    static {
        as.add("");
    }

    public JceStruct newInit() {
        return new t();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ar = (ArrayList) jceInputStream.read(as, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.ar != null) {
            jceOutputStream.write(this.ar, 0);
        }
    }
}
