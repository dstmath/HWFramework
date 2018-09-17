package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bi extends JceStruct {
    public String postfix = "";
    public String prefix = "";
    public int type = 0;
    public int unit = 0;

    public JceStruct newInit() {
        return new bi();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.unit = jceInputStream.read(this.unit, 0, true);
        this.type = jceInputStream.read(this.type, 1, true);
        this.prefix = jceInputStream.readString(2, true);
        this.postfix = jceInputStream.readString(3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.unit, 0);
        jceOutputStream.write(this.type, 1);
        jceOutputStream.write(this.prefix, 2);
        if (this.postfix != null) {
            jceOutputStream.write(this.postfix, 3);
        }
    }
}
