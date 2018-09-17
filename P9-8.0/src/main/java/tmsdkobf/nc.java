package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class nc extends JceStruct {
    static nd BI = new nd();
    static ArrayList<nb> BJ = new ArrayList();
    static ArrayList<nb> BK = new ArrayList();
    public nd BF = null;
    public ArrayList<nb> BG = null;
    public ArrayList<nb> BH = null;

    static {
        BJ.add(new nb());
        BK.add(new nb());
    }

    public JceStruct newInit() {
        return new nc();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.BF = (nd) jceInputStream.read(BI, 0, true);
        this.BG = (ArrayList) jceInputStream.read(BJ, 1, true);
        this.BH = (ArrayList) jceInputStream.read(BK, 2, true);
    }

    public String toString() {
        return "SCCloudResp [scResult=" + this.BF + ", vecBlacks=" + this.BG + ", vecWhites=" + this.BH + "]";
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.BF, 0);
        jceOutputStream.write(this.BG, 1);
        jceOutputStream.write(this.BH, 2);
    }
}
