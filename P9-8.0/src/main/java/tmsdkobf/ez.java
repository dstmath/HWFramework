package tmsdkobf;

import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import com.qq.taf.jce.JceDisplayer;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class ez extends JceStruct implements Cloneable {
    static final /* synthetic */ boolean bF = (!ez.class.desiredAssertionStatus());
    static int ld = 0;
    static int le = 0;
    public String body = "";
    public int kZ = fa.lg.value();
    public String la = "";
    public int lb = 0;
    public int lc = 0;
    public int mainHarmId = fa.lg.value();
    public int seq = 0;
    public String title = "";
    public String url = "";

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

    public int d() {
        return this.lc;
    }

    public void display(StringBuilder stringBuilder, int i) {
        JceDisplayer jceDisplayer = new JceDisplayer(stringBuilder, i);
        jceDisplayer.display(this.url, CheckVersionField.CHECK_VERSION_SERVER_URL);
        jceDisplayer.display(this.mainHarmId, "mainHarmId");
        jceDisplayer.display(this.kZ, "subHarmId");
        jceDisplayer.display(this.seq, "seq");
        jceDisplayer.display(this.la, "desc");
        jceDisplayer.display(this.lb, "UrlType");
        jceDisplayer.display(this.title, "title");
        jceDisplayer.display(this.body, "body");
        jceDisplayer.display(this.lc, "evilclass");
    }

    public void displaySimple(StringBuilder stringBuilder, int i) {
        JceDisplayer jceDisplayer = new JceDisplayer(stringBuilder, i);
        jceDisplayer.displaySimple(this.url, true);
        jceDisplayer.displaySimple(this.mainHarmId, true);
        jceDisplayer.displaySimple(this.kZ, true);
        jceDisplayer.displaySimple(this.seq, true);
        jceDisplayer.displaySimple(this.la, true);
        jceDisplayer.displaySimple(this.lb, true);
        jceDisplayer.displaySimple(this.title, true);
        jceDisplayer.displaySimple(this.body, true);
        jceDisplayer.displaySimple(this.lc, false);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ez ezVar = (ez) obj;
        if (d.equals(this.url, ezVar.url) && d.equals(this.mainHarmId, ezVar.mainHarmId) && d.equals(this.kZ, ezVar.kZ) && d.equals(this.seq, ezVar.seq) && d.equals(this.la, ezVar.la) && d.equals(this.lb, ezVar.lb) && d.equals(this.title, ezVar.title) && d.equals(this.body, ezVar.body) && d.equals(this.lc, ezVar.lc)) {
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
        this.mainHarmId = jceInputStream.read(this.mainHarmId, 1, true);
        this.kZ = jceInputStream.read(this.kZ, 2, false);
        this.seq = jceInputStream.read(this.seq, 3, false);
        this.la = jceInputStream.readString(4, false);
        this.lb = jceInputStream.read(this.lb, 5, false);
        this.title = jceInputStream.readString(6, false);
        this.body = jceInputStream.readString(7, false);
        this.lc = jceInputStream.read(this.lc, 8, false);
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.url, 0);
        jceOutputStream.write(this.mainHarmId, 1);
        jceOutputStream.write(this.kZ, 2);
        jceOutputStream.write(this.seq, 3);
        if (this.la != null) {
            jceOutputStream.write(this.la, 4);
        }
        jceOutputStream.write(this.lb, 5);
        if (this.title != null) {
            jceOutputStream.write(this.title, 6);
        }
        if (this.body != null) {
            jceOutputStream.write(this.body, 7);
        }
        jceOutputStream.write(this.lc, 8);
    }
}
