package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bf extends JceStruct {
    static be cB = new be();
    public boolean cA = true;
    public be cz = null;

    public JceStruct newInit() {
        return new bf();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cz = (be) jceInputStream.read(cB, 0, true);
        this.cA = jceInputStream.read(this.cA, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cz, 0);
        jceOutputStream.write(this.cA, 1);
    }
}
