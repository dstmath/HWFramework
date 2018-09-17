package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class aa extends JceStruct {
    static ArrayList<ad> az = new ArrayList();
    public ArrayList<ad> ax = null;
    public int ay = 0;

    static {
        az.add(new ad());
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ax = (ArrayList) jceInputStream.read(az, 0, true);
        this.ay = jceInputStream.read(this.ay, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ax, 0);
        if (this.ay != 0) {
            jceOutputStream.write(this.ay, 1);
        }
    }
}
