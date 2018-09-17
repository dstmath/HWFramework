package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class w extends JceStruct {
    static ArrayList<s> au = new ArrayList();
    public ArrayList<s> at = null;
    public int result = 0;

    static {
        au.add(new s());
    }

    public JceStruct newInit() {
        return new w();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.result = jceInputStream.read(this.result, 0, false);
        this.at = (ArrayList) jceInputStream.read(au, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.result != 0) {
            jceOutputStream.write(this.result, 0);
        }
        if (this.at != null) {
            jceOutputStream.write(this.at, 1);
        }
    }
}
