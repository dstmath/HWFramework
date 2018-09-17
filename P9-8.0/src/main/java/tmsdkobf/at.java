package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class at extends JceStruct {
    public int bK = 0;
    public int bY = 0;

    public JceStruct newInit() {
        return new at();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.bK = jceInputStream.read(this.bK, 0, false);
        this.bY = jceInputStream.read(this.bY, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.bK, 0);
        jceOutputStream.write(this.bY, 1);
    }
}
