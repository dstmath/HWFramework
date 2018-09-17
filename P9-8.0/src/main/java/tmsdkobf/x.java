package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class x extends JceStruct {
    static ArrayList<s> au = new ArrayList();
    public ArrayList<s> at = null;

    static {
        au.add(new s());
    }

    public JceStruct newInit() {
        return new x();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.at = (ArrayList) jceInputStream.read(au, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.at != null) {
            jceOutputStream.write(this.at, 0);
        }
    }
}
