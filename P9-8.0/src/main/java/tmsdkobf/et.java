package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;

public final class et extends JceStruct implements Comparable<et> {
    public int category = 0;
    public int ib = 0;
    public String kG = "";
    public String kH = "";
    public String kI = "";
    public String kJ = "";
    public int kK = 0;
    public String kL = "";
    public int kM = 0;
    public int kN = 0;
    public int kO = 0;
    public int kP = 0;
    public int kQ = 0;
    public int kR = 0;
    public String kS = "";
    public String name = "";
    public int source = 0;
    public String version = "";

    /* renamed from: a */
    public int compareTo(et etVar) {
        int[] iArr = new int[]{d.a(this.kG, etVar.kG), d.a(this.kH, etVar.kH), d.a(this.version, etVar.version), d.a(this.kI, etVar.kI)};
        for (int i = 0; i < iArr.length; i++) {
            if (iArr[i] != 0) {
                return iArr[i];
            }
        }
        return 0;
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.kG = jceInputStream.readString(0, true);
        this.kH = jceInputStream.readString(1, true);
        this.version = jceInputStream.readString(2, true);
        this.kI = jceInputStream.readString(3, false);
        this.kJ = jceInputStream.readString(4, false);
        this.kK = jceInputStream.read(this.kK, 5, false);
        this.name = jceInputStream.readString(6, false);
        this.ib = jceInputStream.read(this.ib, 7, false);
        this.kL = jceInputStream.readString(8, false);
        this.kM = jceInputStream.read(this.kM, 9, false);
        this.kN = jceInputStream.read(this.kN, 10, false);
        this.category = jceInputStream.read(this.category, 11, false);
        this.kO = jceInputStream.read(this.kO, 12, false);
        this.source = jceInputStream.read(this.source, 13, false);
        this.kP = jceInputStream.read(this.kP, 14, false);
        this.kQ = jceInputStream.read(this.kQ, 15, false);
        this.kR = jceInputStream.read(this.kR, 16, false);
        this.kS = jceInputStream.readString(17, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.kG, 0);
        jceOutputStream.write(this.kH, 1);
        jceOutputStream.write(this.version, 2);
        if (this.kI != null) {
            jceOutputStream.write(this.kI, 3);
        }
        if (this.kJ != null) {
            jceOutputStream.write(this.kJ, 4);
        }
        jceOutputStream.write(this.kK, 5);
        if (this.name != null) {
            jceOutputStream.write(this.name, 6);
        }
        jceOutputStream.write(this.ib, 7);
        if (this.kL != null) {
            jceOutputStream.write(this.kL, 8);
        }
        jceOutputStream.write(this.kM, 9);
        jceOutputStream.write(this.kN, 10);
        jceOutputStream.write(this.category, 11);
        jceOutputStream.write(this.kO, 12);
        jceOutputStream.write(this.source, 13);
        jceOutputStream.write(this.kP, 14);
        jceOutputStream.write(this.kQ, 15);
        jceOutputStream.write(this.kR, 16);
        if (this.kS != null) {
            jceOutputStream.write(this.kS, 17);
        }
    }
}
