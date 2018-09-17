package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ax extends JceStruct {
    public String K = "";

    public JceStruct newInit() {
        return new ax();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.K = jceInputStream.readString(0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.K != null) {
            jceOutputStream.write(this.K, 0);
        }
    }
}
