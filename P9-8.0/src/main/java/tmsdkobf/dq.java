package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class dq extends JceStruct {
    static ArrayList<String> hO;
    static ArrayList<String> hP;
    public ArrayList<String> banIps = null;
    public ArrayList<String> banUrls = null;
    public int hM = 0;
    public int hN = 0;
    public int id = 0;
    public String name = "";
    public int type = 0;

    public void readFrom(JceInputStream jceInputStream) {
        this.id = jceInputStream.read(this.id, 0, true);
        this.type = jceInputStream.read(this.type, 1, true);
        this.hM = jceInputStream.read(this.hM, 2, true);
        this.hN = jceInputStream.read(this.hN, 3, true);
        if (hO == null) {
            hO = new ArrayList();
            hO.add("");
        }
        this.banUrls = (ArrayList) jceInputStream.read(hO, 4, true);
        if (hP == null) {
            hP = new ArrayList();
            hP.add("");
        }
        this.banIps = (ArrayList) jceInputStream.read(hP, 5, true);
        this.name = jceInputStream.readString(6, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.id, 0);
        jceOutputStream.write(this.type, 1);
        jceOutputStream.write(this.hM, 2);
        jceOutputStream.write(this.hN, 3);
        jceOutputStream.write(this.banUrls, 4);
        jceOutputStream.write(this.banIps, 5);
        jceOutputStream.write(this.name, 6);
    }
}
