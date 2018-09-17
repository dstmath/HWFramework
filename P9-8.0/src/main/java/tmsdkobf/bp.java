package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bp extends JceStruct {
    public int db = 0;
    public int dc = 0;

    public JceStruct newInit() {
        return new bp();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.db = jceInputStream.read(this.db, 0, true);
        this.dc = jceInputStream.read(this.dc, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.db, 0);
        jceOutputStream.write(this.dc, 1);
    }
}
