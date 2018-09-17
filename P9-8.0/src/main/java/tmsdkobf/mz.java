package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class mz extends JceStruct {
    public int BD = 0;
    public boolean BE = true;

    public JceStruct newInit() {
        return new mz();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.BD = jceInputStream.read(this.BD, 0, true);
        this.BE = jceInputStream.read(this.BE, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.BD, 0);
        jceOutputStream.write(this.BE, 1);
    }
}
