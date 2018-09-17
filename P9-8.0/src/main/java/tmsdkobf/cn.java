package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cn extends JceStruct {
    public int fg = 0;
    public int fh = 0;

    public JceStruct newInit() {
        return new cn();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fg = jceInputStream.read(this.fg, 0, true);
        this.fh = jceInputStream.read(this.fh, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fg, 0);
        jceOutputStream.write(this.fh, 1);
    }
}
