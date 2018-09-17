package tmsdk.common.module.qscanner.impl;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class a extends JceStruct {
    public int BQ = 0;
    public String bZ = "";
    public String nf = "";
    public String path = "";
    public int size = 0;
    public String softName = "";
    public String version = "";
    public int versionCode = 0;

    public a(String str, String str2, String str3, String str4, int i, int i2, String str5, int i3) {
        this.nf = str;
        this.softName = str2;
        this.bZ = str3;
        this.version = str4;
        this.versionCode = i;
        this.size = i2;
        this.path = str5;
        this.BQ = i3;
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.nf = jceInputStream.readString(0, true);
        this.softName = jceInputStream.readString(1, true);
        this.bZ = jceInputStream.readString(2, true);
        this.version = jceInputStream.readString(3, true);
        this.versionCode = jceInputStream.read(this.versionCode, 4, true);
        this.size = jceInputStream.read(this.size, 5, true);
        this.path = jceInputStream.readString(6, false);
        this.BQ = jceInputStream.read(this.BQ, 7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.nf, 0);
        jceOutputStream.write(this.softName, 1);
        jceOutputStream.write(this.bZ, 2);
        jceOutputStream.write(this.version, 3);
        jceOutputStream.write(this.versionCode, 4);
        jceOutputStream.write(this.size, 5);
        if (this.path != null) {
            jceOutputStream.write(this.path, 6);
        }
        jceOutputStream.write(this.BQ, 7);
    }
}
