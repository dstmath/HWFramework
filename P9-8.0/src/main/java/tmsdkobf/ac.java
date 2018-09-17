package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ac extends JceStruct {
    static ArrayList<String> aD = new ArrayList();
    public ArrayList<String> aC = null;

    static {
        aD.add("");
    }

    public JceStruct newInit() {
        return new ac();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aC = (ArrayList) jceInputStream.read(aD, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aC, 0);
    }
}
