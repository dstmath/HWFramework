package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class nb extends JceStruct {
    public int iCid = 0;
    public int iLac = 0;
    public short sMnc = (short) 0;

    public JceStruct newInit() {
        return new nb();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.iCid = jceInputStream.read(this.iCid, 0, true);
        this.iLac = jceInputStream.read(this.iLac, 1, true);
        this.sMnc = (short) jceInputStream.read(this.sMnc, 2, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.iCid, 0);
        jceOutputStream.write(this.iLac, 1);
        jceOutputStream.write(this.sMnc, 2);
    }
}
