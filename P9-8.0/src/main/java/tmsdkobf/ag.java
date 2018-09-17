package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ag extends JceStruct {
    public String aS = "";
    public int aT = 0;
    public String fileName = "";
    public int type = 1;

    public JceStruct newInit() {
        return new ag();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fileName = jceInputStream.readString(0, true);
        this.aS = jceInputStream.readString(1, true);
        this.type = jceInputStream.read(this.type, 2, false);
        this.aT = jceInputStream.read(this.aT, 3, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fileName, 0);
        jceOutputStream.write(this.aS, 1);
        if (1 != this.type) {
            jceOutputStream.write(this.type, 2);
        }
        jceOutputStream.write(this.aT, 3);
    }
}
