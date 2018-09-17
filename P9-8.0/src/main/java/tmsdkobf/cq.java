package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cq extends JceStruct {
    public String eOperator = "";
    public String fA = "";
    public int fB = 0;
    public int fC = 0;
    public long fD = 0;
    public String fE = "";
    public int fF = 0;
    public int fG = 0;
    public String fH = "";
    public String fI = "";
    public String fJ = "";
    public int fK = 0;
    public String fL = "";
    public String fM = "";
    public int fN = 0;
    public int fO = 0;
    public String fe = "";
    public String fx = "";
    public String fy = "";
    public String fz = "";
    public String location = "";
    public String source = "";
    public int tagCount = 0;
    public int tagType = 0;

    public JceStruct newInit() {
        return new cq();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fe = jceInputStream.readString(1, true);
        this.tagType = jceInputStream.read(this.tagType, 2, true);
        this.tagCount = jceInputStream.read(this.tagCount, 3, true);
        this.fx = jceInputStream.readString(4, false);
        this.fy = jceInputStream.readString(5, false);
        this.fz = jceInputStream.readString(6, false);
        this.source = jceInputStream.readString(7, false);
        this.fA = jceInputStream.readString(8, false);
        this.fB = jceInputStream.read(this.fB, 9, false);
        this.fC = jceInputStream.read(this.fC, 10, false);
        this.fD = jceInputStream.read(this.fD, 11, false);
        this.fE = jceInputStream.readString(12, false);
        this.fF = jceInputStream.read(this.fF, 13, false);
        this.location = jceInputStream.readString(14, false);
        this.eOperator = jceInputStream.readString(15, false);
        this.fG = jceInputStream.read(this.fG, 16, false);
        this.fH = jceInputStream.readString(17, false);
        this.fI = jceInputStream.readString(18, false);
        this.fJ = jceInputStream.readString(19, false);
        this.fK = jceInputStream.read(this.fK, 20, false);
        this.fL = jceInputStream.readString(21, false);
        this.fM = jceInputStream.readString(22, false);
        this.fN = jceInputStream.read(this.fN, 23, false);
        this.fO = jceInputStream.read(this.fO, 24, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fe, 1);
        jceOutputStream.write(this.tagType, 2);
        jceOutputStream.write(this.tagCount, 3);
        if (this.fx != null) {
            jceOutputStream.write(this.fx, 4);
        }
        if (this.fy != null) {
            jceOutputStream.write(this.fy, 5);
        }
        if (this.fz != null) {
            jceOutputStream.write(this.fz, 6);
        }
        if (this.source != null) {
            jceOutputStream.write(this.source, 7);
        }
        if (this.fA != null) {
            jceOutputStream.write(this.fA, 8);
        }
        if (this.fB != 0) {
            jceOutputStream.write(this.fB, 9);
        }
        if (this.fC != 0) {
            jceOutputStream.write(this.fC, 10);
        }
        if (this.fD != 0) {
            jceOutputStream.write(this.fD, 11);
        }
        if (this.fE != null) {
            jceOutputStream.write(this.fE, 12);
        }
        if (this.fF != 0) {
            jceOutputStream.write(this.fF, 13);
        }
        if (this.location != null) {
            jceOutputStream.write(this.location, 14);
        }
        if (this.eOperator != null) {
            jceOutputStream.write(this.eOperator, 15);
        }
        if (this.fG != 0) {
            jceOutputStream.write(this.fG, 16);
        }
        if (this.fH != null) {
            jceOutputStream.write(this.fH, 17);
        }
        if (this.fI != null) {
            jceOutputStream.write(this.fI, 18);
        }
        if (this.fJ != null) {
            jceOutputStream.write(this.fJ, 19);
        }
        if (this.fK != 0) {
            jceOutputStream.write(this.fK, 20);
        }
        if (this.fL != null) {
            jceOutputStream.write(this.fL, 21);
        }
        if (this.fM != null) {
            jceOutputStream.write(this.fM, 22);
        }
        if (this.fN != 0) {
            jceOutputStream.write(this.fN, 23);
        }
        if (this.fO != 0) {
            jceOutputStream.write(this.fO, 24);
        }
    }
}
