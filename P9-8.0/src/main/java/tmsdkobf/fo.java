package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.d;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class fo extends fn {
    static HashMap<String, byte[]> mB = null;
    static HashMap<String, HashMap<String, byte[]>> mC = null;
    protected fq mA;
    private int mD;

    public fo() {
        this.mA = new fq();
        this.mD = 0;
        this.mA.mG = (short) 2;
    }

    public fo(boolean useVersion3) {
        this.mA = new fq();
        this.mD = 0;
        if (useVersion3) {
            m();
        } else {
            this.mA.mG = (short) 2;
        }
    }

    public <T> void put(String name, T t) {
        if (name.startsWith(".")) {
            throw new IllegalArgumentException("put name can not startwith . , now is " + name);
        }
        super.put(name, t);
    }

    public void m() {
        super.m();
        this.mA.mG = (short) 3;
    }

    public byte[] l() {
        if (this.mA.mG != (short) 2) {
            if (this.mA.mK == null) {
                this.mA.mK = "";
            }
            if (this.mA.mL == null) {
                this.mA.mL = "";
            }
        } else if (this.mA.mK == null || this.mA.mK.equals("")) {
            throw new IllegalArgumentException("servantName can not is null");
        } else if (this.mA.mL == null || this.mA.mL.equals("")) {
            throw new IllegalArgumentException("funcName can not is null");
        }
        JceOutputStream _os = new JceOutputStream(0);
        _os.setServerEncoding(this.mx);
        if (this.mA.mG != (short) 2) {
            _os.write(this.mz, 0);
        } else {
            _os.write(this.mu, 0);
        }
        this.mA.mM = d.a(_os.getByteBuffer());
        _os = new JceOutputStream(0);
        _os.setServerEncoding(this.mx);
        this.mA.writeTo(_os);
        byte[] bodys = d.a(_os.getByteBuffer());
        int size = bodys.length;
        ByteBuffer buf = ByteBuffer.allocate(size + 4);
        buf.putInt(size + 4).put(bodys).flip();
        return buf.array();
    }

    private void n() {
        JceInputStream _is = new JceInputStream(this.mA.mM);
        _is.setServerEncoding(this.mx);
        if (mB == null) {
            mB = new HashMap();
            mB.put("", new byte[0]);
        }
        this.mz = _is.readMap(mB, 0, false);
    }

    private void o() {
        JceInputStream _is = new JceInputStream(this.mA.mM);
        _is.setServerEncoding(this.mx);
        if (mC == null) {
            mC = new HashMap();
            HashMap<String, byte[]> h = new HashMap();
            h.put("", new byte[0]);
            mC.put("", h);
        }
        this.mu = _is.readMap(mC, 0, false);
        this.mv = new HashMap();
    }

    public void b(byte[] buffer) {
        if (buffer.length >= 4) {
            try {
                JceInputStream _is = new JceInputStream(buffer, 4);
                _is.setServerEncoding(this.mx);
                this.mA.readFrom(_is);
                if (this.mA.mG != (short) 3) {
                    this.mz = null;
                    o();
                    return;
                }
                n();
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("decode package must include size head");
    }

    public void C(String servantName) {
        this.mA.mK = servantName;
    }

    public void D(String sFuncName) {
        this.mA.mL = sFuncName;
    }

    public int p() {
        return this.mA.mJ;
    }

    public void E(int iRequestId) {
        this.mA.mJ = iRequestId;
    }
}
