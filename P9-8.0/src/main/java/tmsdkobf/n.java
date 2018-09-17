package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class n extends JceStruct {
    public int X = 0;
    public int Y = 0;

    public JceStruct newInit() {
        return new n();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.X = jceInputStream.read(this.X, 0, false);
        this.Y = jceInputStream.read(this.Y, 1, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.X != 0) {
            jceOutputStream.write(this.X, 0);
        }
        if (this.Y != 0) {
            jceOutputStream.write(this.Y, 1);
        }
    }
}
