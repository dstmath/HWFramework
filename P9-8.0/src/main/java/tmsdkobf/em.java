package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class em extends JceStruct {
    static ArrayList<eu> ks;
    public ArrayList<eu> kr = null;

    public void readFrom(JceInputStream jceInputStream) {
        if (ks == null) {
            ks = new ArrayList();
            ks.add(new eu());
        }
        this.kr = (ArrayList) jceInputStream.read(ks, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kr, 1);
    }
}
