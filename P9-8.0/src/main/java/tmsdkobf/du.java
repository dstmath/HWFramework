package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class du extends JceStruct {
    static fd ia = new fd();
    public fd hZ = null;

    public JceStruct newInit() {
        return new du();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hZ = (fd) jceInputStream.read(ia, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hZ, 0);
    }
}
