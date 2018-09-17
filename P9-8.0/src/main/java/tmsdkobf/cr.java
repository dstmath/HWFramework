package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cr extends JceStruct {
    public int fP = 0;
    public int fQ = 0;
    public int fR = 0;
    public int fS = 0;
    public int fT = 0;
    public String fU = "";
    public String fe = "";
    public int localTagType = 0;
    public String originName = "";
    public int scene = 0;
    public int tagType = 0;
    public String userDefineName = "";

    public JceStruct newInit() {
        return new cr();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fe = jceInputStream.readString(0, true);
        this.fP = jceInputStream.read(this.fP, 1, true);
        this.fQ = jceInputStream.read(this.fQ, 2, false);
        this.fR = jceInputStream.read(this.fR, 3, false);
        this.fS = jceInputStream.read(this.fS, 4, false);
        this.fT = jceInputStream.read(this.fT, 5, false);
        this.tagType = jceInputStream.read(this.tagType, 6, false);
        this.originName = jceInputStream.readString(7, false);
        this.userDefineName = jceInputStream.readString(8, false);
        this.scene = jceInputStream.read(this.scene, 9, false);
        this.localTagType = jceInputStream.read(this.localTagType, 10, false);
        this.fU = jceInputStream.readString(11, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fe, 0);
        jceOutputStream.write(this.fP, 1);
        if (this.fQ != 0) {
            jceOutputStream.write(this.fQ, 2);
        }
        if (this.fR != 0) {
            jceOutputStream.write(this.fR, 3);
        }
        if (this.fS != 0) {
            jceOutputStream.write(this.fS, 4);
        }
        if (this.fT != 0) {
            jceOutputStream.write(this.fT, 5);
        }
        if (this.tagType != 0) {
            jceOutputStream.write(this.tagType, 6);
        }
        if (this.originName != null) {
            jceOutputStream.write(this.originName, 7);
        }
        if (this.userDefineName != null) {
            jceOutputStream.write(this.userDefineName, 8);
        }
        if (this.scene != 0) {
            jceOutputStream.write(this.scene, 9);
        }
        if (this.localTagType != 0) {
            jceOutputStream.write(this.localTagType, 10);
        }
        if (this.fU != null) {
            jceOutputStream.write(this.fU, 11);
        }
    }
}
