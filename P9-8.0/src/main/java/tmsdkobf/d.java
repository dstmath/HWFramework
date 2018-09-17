package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class d extends JceStruct {
    static e k = new e();
    public int hash = 0;
    public e j = null;

    public JceStruct newInit() {
        return new d();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.hash = jceInputStream.read(this.hash, 0, true);
        this.j = (e) jceInputStream.read(k, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.hash, 0);
        if (this.j != null) {
            jceOutputStream.write(this.j, 1);
        }
    }
}
