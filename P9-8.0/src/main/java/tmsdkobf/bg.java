package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bg extends JceStruct {
    public String cC = "";
    public String port = "";
    public int status = 0;

    public JceStruct newInit() {
        return new bg();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.cC = jceInputStream.readString(0, true);
        this.port = jceInputStream.readString(1, true);
        this.status = jceInputStream.read(this.status, 2, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.cC, 0);
        jceOutputStream.write(this.port, 1);
        if (this.status != 0) {
            jceOutputStream.write(this.status, 2);
        }
    }
}
