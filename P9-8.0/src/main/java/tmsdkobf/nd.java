package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class nd extends JceStruct {
    public boolean BE = true;
    public int BL = 0;
    public float BM = 0.0f;
    public int BN = 0;

    public JceStruct newInit() {
        return new nd();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.BL = jceInputStream.read(this.BL, 0, true);
        this.BE = jceInputStream.read(this.BE, 1, true);
        this.BM = jceInputStream.read(this.BM, 2, true);
        this.BN = jceInputStream.read(this.BN, 3, true);
    }

    public String toString() {
        return "SCCloudResult [eCloudFakeType=" + this.BL + ", bLastSmsIsFake=" + this.BE + ", fCloudScore=" + this.BM + ", usSmsType=" + this.BN + "]";
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.BL, 0);
        jceOutputStream.write(this.BE, 1);
        jceOutputStream.write(this.BM, 2);
        jceOutputStream.write(this.BN, 3);
    }
}
