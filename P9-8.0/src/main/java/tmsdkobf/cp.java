package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class cp extends JceStruct {
    static ArrayList<cn> fv = new ArrayList();
    static ArrayList<cs> fw = new ArrayList();
    public int fk = 0;
    public int fl = 0;
    public int fm = 0;
    public int fn = 0;
    public ArrayList<cn> fo = null;
    public int fp = 0;
    public ArrayList<cs> fq = null;
    public String fr = "";
    public int fs = 0;
    public int ft = 0;
    public String fu = "ETS_NONE";
    public String sender = "";
    public String sms = "";

    static {
        fv.add(new cn());
        fw.add(new cs());
    }

    public JceStruct newInit() {
        return new cp();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.sender = jceInputStream.readString(0, true);
        this.sms = jceInputStream.readString(1, true);
        this.fk = jceInputStream.read(this.fk, 2, true);
        this.fl = jceInputStream.read(this.fl, 3, true);
        this.fm = jceInputStream.read(this.fm, 4, true);
        this.fn = jceInputStream.read(this.fn, 5, false);
        this.fo = (ArrayList) jceInputStream.read(fv, 6, false);
        this.fp = jceInputStream.read(this.fp, 7, false);
        this.fq = (ArrayList) jceInputStream.read(fw, 8, false);
        this.fr = jceInputStream.readString(9, false);
        this.fs = jceInputStream.read(this.fs, 10, false);
        this.ft = jceInputStream.read(this.ft, 11, false);
        this.fu = jceInputStream.readString(12, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.sender, 0);
        jceOutputStream.write(this.sms, 1);
        jceOutputStream.write(this.fk, 2);
        jceOutputStream.write(this.fl, 3);
        jceOutputStream.write(this.fm, 4);
        if (this.fn != 0) {
            jceOutputStream.write(this.fn, 5);
        }
        if (this.fo != null) {
            jceOutputStream.write(this.fo, 6);
        }
        if (this.fp != 0) {
            jceOutputStream.write(this.fp, 7);
        }
        if (this.fq != null) {
            jceOutputStream.write(this.fq, 8);
        }
        if (this.fr != null) {
            jceOutputStream.write(this.fr, 9);
        }
        if (this.fs != 0) {
            jceOutputStream.write(this.fs, 10);
        }
        if (this.ft != 0) {
            jceOutputStream.write(this.ft, 11);
        }
        if (this.fu != null) {
            jceOutputStream.write(this.fu, 12);
        }
    }
}
