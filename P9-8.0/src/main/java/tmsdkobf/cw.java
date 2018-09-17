package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cw extends JceStruct {
    public String url = "";

    public JceStruct newInit() {
        return new cw();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.url = jceInputStream.readString(0, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.url != null) {
            jceOutputStream.write(this.url, 0);
        }
    }
}
