package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class mx extends JceStruct {
    static ArrayList<my> Bx = new ArrayList();
    public ArrayList<my> Bu = null;
    public String Bv = "";
    public String Bw = "";

    static {
        Bx.add(new my());
    }

    public JceStruct newInit() {
        return new mx();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.Bu = (ArrayList) jceInputStream.read(Bx, 0, true);
        this.Bv = jceInputStream.readString(1, true);
        this.Bw = jceInputStream.readString(2, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.Bu, 0);
        jceOutputStream.write(this.Bv, 1);
        jceOutputStream.write(this.Bw, 2);
    }
}
