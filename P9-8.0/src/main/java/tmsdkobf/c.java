package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class c extends JceStruct {
    public int hash = 0;

    public JceStruct newInit() {
        return new c();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hash = jceInputStream.read(this.hash, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hash, 0);
    }
}
