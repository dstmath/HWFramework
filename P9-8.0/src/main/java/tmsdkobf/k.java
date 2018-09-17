package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class k extends JceStruct {
    public String T = "";
    public int U = 0;
    public String title = "";
    public int type = 0;

    public void readFrom(JceInputStream jceInputStream) {
        this.title = jceInputStream.readString(0, true);
        this.T = jceInputStream.readString(1, true);
        this.type = jceInputStream.read(this.type, 2, true);
        this.U = jceInputStream.read(this.U, 3, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.title, 0);
        jceOutputStream.write(this.T, 1);
        jceOutputStream.write(this.type, 2);
        jceOutputStream.write(this.U, 3);
    }
}
