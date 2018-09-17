package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class ey extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF;
    public String I = "";
    public String kX = "";
    public int kY = 28;
    public int seq = 0;
    public String url = "";
    public int version = 0;

    static {
        boolean z = false;
        if (!ey.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public Object clone() {
        Object obj = null;
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            if (bF) {
                return obj;
            }
            throw new AssertionError();
        }
    }

    public void display(StringBuilder stringBuilder, int i) {
        JceDisplayer jceDisplayer = new JceDisplayer(stringBuilder, i);
        jceDisplayer.display(this.url, CheckVersionField.CHECK_VERSION_SERVER_URL);
        jceDisplayer.display(this.kX, "ext");
        jceDisplayer.display(this.seq, "seq");
        jceDisplayer.display(this.version, "version");
        jceDisplayer.display(this.I, "guid");
        jceDisplayer.display(this.kY, "appId");
    }

    public void displaySimple(StringBuilder stringBuilder, int i) {
        JceDisplayer jceDisplayer = new JceDisplayer(stringBuilder, i);
        jceDisplayer.displaySimple(this.url, true);
        jceDisplayer.displaySimple(this.kX, true);
        jceDisplayer.displaySimple(this.seq, true);
        jceDisplayer.displaySimple(this.version, true);
        jceDisplayer.displaySimple(this.I, true);
        jceDisplayer.displaySimple(this.kY, false);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ey eyVar = (ey) obj;
        if (d.equals(this.url, eyVar.url) && d.equals(this.kX, eyVar.kX) && d.equals(this.seq, eyVar.seq) && d.equals(this.version, eyVar.version) && d.equals(this.I, eyVar.I) && d.equals(this.kY, eyVar.kY)) {
            z = true;
        }
        return z;
    }

    public String getUrl() {
        return this.url;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.url = jceInputStream.readString(0, true);
        this.kX = jceInputStream.readString(1, false);
        this.seq = jceInputStream.read(this.seq, 2, false);
        this.version = jceInputStream.read(this.version, 3, false);
        this.I = jceInputStream.readString(4, false);
        this.kY = jceInputStream.read(this.kY, 5, false);
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.url, 0);
        if (this.kX != null) {
            jceOutputStream.write(this.kX, 1);
        }
        jceOutputStream.write(this.seq, 2);
        jceOutputStream.write(this.version, 3);
        if (this.I != null) {
            jceOutputStream.write(this.I, 4);
        }
        jceOutputStream.write(this.kY, 5);
    }
}
