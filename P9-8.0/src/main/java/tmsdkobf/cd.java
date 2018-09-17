package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cd extends JceStruct {
    public long ex = 0;

    public JceStruct newInit() {
        return new cd();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.ex = jceInputStream.read(this.ex, 0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.ex != 0) {
            jceOutputStream.write(this.ex, 0);
        }
    }
}
