package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.qq.taf.jce.d;
import java.util.HashMap;
import java.util.Map;

public final class fq extends JceStruct {
    static final /* synthetic */ boolean bF;
    static byte[] mQ = null;
    static Map<String, String> mR = null;
    public short mG = (short) 0;
    public byte mH = (byte) 0;
    public int mI = 0;
    public int mJ = 0;
    public String mK = null;
    public String mL = null;
    public byte[] mM;
    public int mN = 0;
    public Map<String, String> mO;
    public Map<String, String> mP;

    static {
        boolean z = false;
        if (!fq.class.desiredAssertionStatus()) {
            z = true;
        }
        bF = z;
    }

    public boolean equals(Object o) {
        fq t = (fq) o;
        return d.equals(1, t.mG) && d.equals(1, t.mH) && d.equals(1, t.mI) && d.equals(1, t.mJ) && d.equals(Integer.valueOf(1), t.mK) && d.equals(Integer.valueOf(1), t.mL) && d.equals(Integer.valueOf(1), t.mM) && d.equals(1, t.mN) && d.equals(Integer.valueOf(1), t.mO) && d.equals(Integer.valueOf(1), t.mP);
    }

    public Object clone() {
        Object o = null;
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            if (bF) {
                return o;
            }
            throw new AssertionError();
        }
    }

    public void writeTo(JceOutputStream _os) {
        _os.write(this.mG, 1);
        _os.write(this.mH, 2);
        _os.write(this.mI, 3);
        _os.write(this.mJ, 4);
        _os.write(this.mK, 5);
        _os.write(this.mL, 6);
        _os.write(this.mM, 7);
        _os.write(this.mN, 8);
        _os.write(this.mO, 9);
        _os.write(this.mP, 10);
    }

    public void readFrom(JceInputStream _is) {
        try {
            this.mG = (short) _is.read(this.mG, 1, true);
            this.mH = (byte) _is.read(this.mH, 2, true);
            this.mI = _is.read(this.mI, 3, true);
            this.mJ = _is.read(this.mJ, 4, true);
            this.mK = _is.readString(5, true);
            this.mL = _is.readString(6, true);
            if (mQ == null) {
                mQ = new byte[1];
            }
            this.mM = _is.read(mQ, 7, true);
            this.mN = _is.read(this.mN, 8, true);
            if (mR == null) {
                mR = new HashMap();
                mR.put("", "");
            }
            this.mO = (Map) _is.read(mR, 9, true);
            if (mR == null) {
                mR = new HashMap();
                mR.put("", "");
            }
            this.mP = (Map) _is.read(mR, 10, true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("RequestPacket decode error " + fp.c(this.mM));
            throw new RuntimeException(e);
        }
    }
}
