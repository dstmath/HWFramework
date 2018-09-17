package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class af extends JceStruct {
    static ArrayList<ag> aR = new ArrayList();
    public ArrayList<ag> aQ = null;

    static {
        aR.add(new ag());
    }

    public JceStruct newInit() {
        return new af();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aQ = (ArrayList) jceInputStream.read(aR, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aQ, 0);
    }
}
