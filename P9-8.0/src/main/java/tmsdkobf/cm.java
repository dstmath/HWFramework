package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cm extends JceStruct {
    public String fe = "";
    public int ff = 0;

    public JceStruct newInit() {
        return new cm();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fe = jceInputStream.readString(0, true);
        this.ff = jceInputStream.read(this.ff, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fe, 0);
        if (this.ff != 0) {
            jceOutputStream.write(this.ff, 1);
        }
    }
}
