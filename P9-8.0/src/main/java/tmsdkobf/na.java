package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class na extends JceStruct {
    public int iCid = 0;
    public int iLac = 0;
    public short sBsss = (short) 0;
    public short sNetworkType = (short) 0;

    public JceStruct newInit() {
        return new na();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.sNetworkType = (short) jceInputStream.read(this.sNetworkType, 0, true);
        this.iCid = jceInputStream.read(this.iCid, 1, true);
        this.iLac = jceInputStream.read(this.iLac, 2, true);
        this.sBsss = (short) jceInputStream.read(this.sBsss, 3, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.sNetworkType, 0);
        jceOutputStream.write(this.iCid, 1);
        jceOutputStream.write(this.iLac, 2);
        jceOutputStream.write(this.sBsss, 3);
    }
}
