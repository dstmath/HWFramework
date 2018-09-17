package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bs extends JceStruct {
    public String ep = "";

    public JceStruct newInit() {
        return new bs();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ep = jceInputStream.readString(0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ep, 0);
    }
}
