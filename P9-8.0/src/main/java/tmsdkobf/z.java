package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class z extends JceStruct {
    public String T = "";
    public int av = 0;
    public int aw = 0;
    public int bgColor = 0;
    public String title = "";
    public int type = 0;

    public JceStruct newInit() {
        return new z();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.title = jceInputStream.readString(0, false);
        this.T = jceInputStream.readString(1, false);
        this.type = jceInputStream.read(this.type, 2, false);
        this.av = jceInputStream.read(this.av, 3, false);
        this.aw = jceInputStream.read(this.aw, 4, false);
        this.bgColor = jceInputStream.read(this.bgColor, 5, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.title != null) {
            jceOutputStream.write(this.title, 0);
        }
        if (this.T != null) {
            jceOutputStream.write(this.T, 1);
        }
        if (this.type != 0) {
            jceOutputStream.write(this.type, 2);
        }
        if (this.av != 0) {
            jceOutputStream.write(this.av, 3);
        }
        if (this.aw != 0) {
            jceOutputStream.write(this.aw, 4);
        }
        if (this.bgColor != 0) {
            jceOutputStream.write(this.bgColor, 5);
        }
    }
}
