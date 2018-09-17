package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class cz extends JceStruct {
    static ArrayList<Integer> gi = new ArrayList();
    public ArrayList<Integer> gh = null;

    static {
        gi.add(Integer.valueOf(0));
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gh = (ArrayList) jceInputStream.read(gi, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.gh != null) {
            jceOutputStream.write(this.gh, 0);
        }
    }
}
