package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.HashMap;
import java.util.Map;

public final class aj extends JceStruct {
    static byte[] aI = new byte[1];
    static byte[] bo = new byte[1];
    static byte[] bp = new byte[1];
    static Map<String, String> bq = new HashMap();
    public int aE = 0;
    public byte[] aF = null;
    public boolean bf = false;
    public byte[] bg = null;
    public byte[] bh = null;
    public int bi = 0;
    public long bj = 0;
    public int bk = 0;
    public int bl = 0;
    public String bm = "";
    public Map<String, String> bn = null;
    public int fileSize = 0;
    public int timestamp = 0;
    public String url = "";

    static {
        aI[0] = (byte) 0;
        bo[0] = (byte) 0;
        bp[0] = (byte) 0;
        bq.put("", "");
    }

    public JceStruct newInit() {
        return new aj();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.aE = jceInputStream.read(this.aE, 0, true);
        this.aF = jceInputStream.read(aI, 1, true);
        this.timestamp = jceInputStream.read(this.timestamp, 2, true);
        this.url = jceInputStream.readString(3, false);
        this.bf = jceInputStream.read(this.bf, 4, false);
        this.bg = jceInputStream.read(bo, 5, false);
        this.bh = jceInputStream.read(bp, 6, false);
        this.bi = jceInputStream.read(this.bi, 7, false);
        this.fileSize = jceInputStream.read(this.fileSize, 8, false);
        this.bj = jceInputStream.read(this.bj, 9, false);
        this.bk = jceInputStream.read(this.bk, 10, false);
        this.bl = jceInputStream.read(this.bl, 11, false);
        this.bm = jceInputStream.readString(12, false);
        this.bn = (Map) jceInputStream.read(bq, 13, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.aE, 0);
        jceOutputStream.write(this.aF, 1);
        jceOutputStream.write(this.timestamp, 2);
        if (this.url != null) {
            jceOutputStream.write(this.url, 3);
        }
        if (this.bf) {
            jceOutputStream.write(this.bf, 4);
        }
        if (this.bg != null) {
            jceOutputStream.write(this.bg, 5);
        }
        if (this.bh != null) {
            jceOutputStream.write(this.bh, 6);
        }
        if (this.bi != 0) {
            jceOutputStream.write(this.bi, 7);
        }
        if (this.fileSize != 0) {
            jceOutputStream.write(this.fileSize, 8);
        }
        if (this.bj != 0) {
            jceOutputStream.write(this.bj, 9);
        }
        if (this.bk != 0) {
            jceOutputStream.write(this.bk, 10);
        }
        if (this.bl != 0) {
            jceOutputStream.write(this.bl, 11);
        }
        if (this.bm != null) {
            jceOutputStream.write(this.bm, 12);
        }
        if (this.bn != null) {
            jceOutputStream.write(this.bn, 13);
        }
    }
}
