package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ca extends JceStruct {
    public String I = "";

    public JceStruct newInit() {
        return new ca();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.I = jceInputStream.readString(0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.I, 0);
    }
}
