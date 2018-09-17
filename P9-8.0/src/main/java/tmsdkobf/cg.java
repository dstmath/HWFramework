package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class cg extends JceStruct {
    public int eS = 0;
    public String eT = "";
    public String sender = "";
    public String sms = "";
    public int uiCheckFlag = 0;
    public int uiCheckType = 0;
    public int uiSmsInOut = 0;
    public int uiSmsType = 0;

    public JceStruct newInit() {
        return new cg();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.sender = jceInputStream.readString(0, true);
        this.sms = jceInputStream.readString(1, true);
        this.uiSmsType = jceInputStream.read(this.uiSmsType, 2, true);
        this.uiCheckType = jceInputStream.read(this.uiCheckType, 3, true);
        this.uiSmsInOut = jceInputStream.read(this.uiSmsInOut, 4, false);
        this.uiCheckFlag = jceInputStream.read(this.uiCheckFlag, 5, false);
        this.eS = jceInputStream.read(this.eS, 6, false);
        this.eT = jceInputStream.readString(7, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.sender, 0);
        jceOutputStream.write(this.sms, 1);
        jceOutputStream.write(this.uiSmsType, 2);
        jceOutputStream.write(this.uiCheckType, 3);
        if (this.uiSmsInOut != 0) {
            jceOutputStream.write(this.uiSmsInOut, 4);
        }
        if (this.uiCheckFlag != 0) {
            jceOutputStream.write(this.uiCheckFlag, 5);
        }
        if (this.eS != 0) {
            jceOutputStream.write(this.eS, 6);
        }
        if (this.eT != null) {
            jceOutputStream.write(this.eT, 7);
        }
    }
}
