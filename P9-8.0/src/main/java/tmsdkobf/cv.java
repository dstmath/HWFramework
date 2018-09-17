package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cv extends JceStruct {
    static cx gc = new cx();
    public cx gb = null;

    public JceStruct newInit() {
        return new cv();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.gb = (cx) jceInputStream.read(gc, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.gb, 0);
    }
}
