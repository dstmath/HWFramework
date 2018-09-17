package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class kq extends JceStruct {
    public String ch = "";
    public String url = "";
    public int wM = 0;

    public void readFrom(JceInputStream jceInputStream) {
        this.ch = jceInputStream.readString(0, true);
        this.url = jceInputStream.readString(1, true);
        this.wM = jceInputStream.read(this.wM, 2, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.ch, 0);
        jceOutputStream.write(this.url, 1);
        jceOutputStream.write(this.wM, 2);
    }
}
