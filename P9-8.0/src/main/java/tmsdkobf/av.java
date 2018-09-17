package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class av extends JceStruct {
    public String aS = "";
    public String bZ = "";
    public long ca = 0;
    public boolean cb = true;
    public String cc = "";
    public long cd = 0;
    public long ce = 0;
    public String packageName = "";
    public String softName = "";
    public String version = "";

    public JceStruct newInit() {
        return new av();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.packageName = jceInputStream.readString(0, false);
        this.softName = jceInputStream.readString(1, false);
        this.bZ = jceInputStream.readString(2, false);
        this.ca = jceInputStream.read(this.ca, 3, false);
        this.cb = jceInputStream.read(this.cb, 4, false);
        this.cc = jceInputStream.readString(5, false);
        this.aS = jceInputStream.readString(6, false);
        this.cd = jceInputStream.read(this.cd, 7, false);
        this.version = jceInputStream.readString(8, false);
        this.ce = jceInputStream.read(this.ce, 9, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.packageName != null) {
            jceOutputStream.write(this.packageName, 0);
        }
        if (this.softName != null) {
            jceOutputStream.write(this.softName, 1);
        }
        if (this.bZ != null) {
            jceOutputStream.write(this.bZ, 2);
        }
        if (this.ca != 0) {
            jceOutputStream.write(this.ca, 3);
        }
        jceOutputStream.write(this.cb, 4);
        if (this.cc != null) {
            jceOutputStream.write(this.cc, 5);
        }
        if (this.aS != null) {
            jceOutputStream.write(this.aS, 6);
        }
        if (this.cd != 0) {
            jceOutputStream.write(this.cd, 7);
        }
        if (this.version != null) {
            jceOutputStream.write(this.version, 8);
        }
        if (this.ce != 0) {
            jceOutputStream.write(this.ce, 9);
        }
    }
}
