package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class fu extends JceStruct {
    public String nf = "";
    public String nn = "";

    public void readFrom(JceInputStream jceInputStream) {
        this.nf = jceInputStream.readString(0, true);
        this.nn = jceInputStream.readString(1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.nf, 0);
        jceOutputStream.write(this.nn, 1);
    }
}
