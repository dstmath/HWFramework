package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class bt extends JceStruct {
    static br eu = new br();
    public br eq = null;
    public String er = "";
    public String es = "";
    public String et = "";

    public JceStruct newInit() {
        return new bt();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.eq = (br) jceInputStream.read(eu, 0, true);
        this.er = jceInputStream.readString(1, true);
        this.es = jceInputStream.readString(2, false);
        this.et = jceInputStream.readString(3, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.eq, 0);
        jceOutputStream.write(this.er, 1);
        if (this.es != null) {
            jceOutputStream.write(this.es, 2);
        }
        if (this.et != null) {
            jceOutputStream.write(this.et, 3);
        }
    }
}
