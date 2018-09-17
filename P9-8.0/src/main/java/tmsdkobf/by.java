package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class by extends JceStruct {
    public String et = "";

    public JceStruct newInit() {
        return new by();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.et = jceInputStream.readString(0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.et, 0);
    }
}
