package tmsdkobf;

import android.os.Process;
import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;
import tmsdk.common.creator.ManagerCreatorC;

public class ob implements oa {
    private static boolean Hn = false;
    private nz Hl;
    private ny Hm;
    protected long mr;

    public ob(long j) {
        this.mr = j;
    }

    public static synchronized void a(nl nlVar, boolean z, boolean z2, String str) {
        boolean z3 = false;
        synchronized (ob.class) {
            if (!nu.Eu) {
                nu.H(z2);
                nu.cj(str);
                if (!z) {
                    z3 = true;
                }
                nu.L(z3);
                nu.I(nlVar.aB());
                nu.J(nlVar.aC());
                nu.K(nlVar.aD());
                ((nz) ManagerCreatorC.getManager(nz.class)).c(nlVar);
                nu.Eu = true;
            }
        }
    }

    private ny gE() {
        if (nu.ge() && !nu.Eu) {
            throw new RuntimeException("getSharkProcessProxy too early");
        }
        synchronized (this) {
            if (this.Hm == null) {
                this.Hm = ny.gv();
            }
        }
        return this.Hm;
    }

    private nz gF() {
        if (nu.ge() && !nu.Eu) {
            throw new RuntimeException("getSharkProtocolQueue too early");
        }
        synchronized (this) {
            if (this.Hl == null) {
                this.Hl = (nz) ManagerCreatorC.getManager(nz.class);
            }
        }
        return this.Hl;
    }

    public static synchronized void gy() {
        synchronized (ob.class) {
            if (nu.ge()) {
                if (!nu.Eu) {
                    throw new RuntimeException("must call initSync() before initAsync()!");
                }
            }
            if (!Hn) {
                Hn = true;
                ((ki) fj.D(4)).a(new Runnable() {
                    public void run() {
                        ((nz) ManagerCreatorC.getManager(nz.class)).gy();
                    }
                }, "init SharkProtocolQueue async");
            }
        }
    }

    public WeakReference<kd> a(int i, JceStruct jceStruct, JceStruct jceStruct2, int i2, jy jyVar) {
        return a(i, jceStruct, jceStruct2, i2, jyVar, 0);
    }

    public WeakReference<kd> a(int i, JceStruct jceStruct, JceStruct jceStruct2, int i2, jy jyVar, long j) {
        return a(i, jceStruct, jceStruct2, i2, jyVar, j, 0);
    }

    public WeakReference<kd> a(int i, JceStruct jceStruct, JceStruct jceStruct2, int i2, jy jyVar, long j, long j2) {
        return c(Process.myPid(), 0, 0, 0, this.mr, i, jceStruct, null, jceStruct2, i2, jyVar, null, j, j2);
    }

    public void a(int i, long j, int i2, JceStruct jceStruct, int i3) {
        c(Process.myPid(), 0, i, j, this.mr, i2, jceStruct, null, null, i3 | 1073741824, null, null, 0, 0);
    }

    public void a(int i, JceStruct jceStruct, int i2, ka kaVar) {
        a(0, i, jceStruct, i2, kaVar, false);
    }

    public void a(long -l_8_J, int i, JceStruct jceStruct, int i2, ka kaVar, boolean z) {
        if (nu.aB()) {
            mb.d("SharkProcessProxy", "sending process registerSharkPush() from cmdId: " + i + " flag: " + i2);
            if (!z) {
                -l_8_J = this.mr;
            }
            gF().a(-l_8_J, i, jceStruct, i2, kaVar, z);
            return;
        }
        mb.d("SharkProcessProxy", "other process registerSharkPush() from cmdId: " + i + " flag: " + i2);
        gE().a(this.mr, i, jceStruct, i2, kaVar);
    }

    public String b() {
        return gF().b();
    }

    public void b(int i, long j, int i2, JceStruct jceStruct) {
        a(i, j, i2, jceStruct, 0);
    }

    public WeakReference<kd> c(int i, int i2, int i3, long j, long j2, int i4, JceStruct jceStruct, byte[] bArr, JceStruct jceStruct2, int i5, jy jyVar, jz jzVar, long j3, long j4) {
        mb.d("SharkProcessProxy", Process.myPid() + " sendShark() from pid: " + i + " ipcSeqNo: " + i2 + " callerIdent: " + j2 + " cmdId: " + i4 + " flag: " + i5 + " callBackTimeout: " + j3);
        if (nu.aB()) {
            if (!kc.an(i5)) {
                return gF().c(i, i2, i3, j, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar, j3, j4);
            }
            nl gl = gF().gl();
            return gl != null ? gl.a(i, i2, i3, j, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar, j3, j4) : null;
        } else if (!nu.aC() || (nu.aD() && (i5 & 4096) == 0)) {
            gE().a(i, this.mr, i3, j, i4, jceStruct, jceStruct2, i5, jyVar, j3, j4);
            return null;
        } else if ((i5 & 2048) != 0 || (i5 & 512) != 0) {
            return gF().c(i, i2, i3, j, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar, j3, j4);
        } else {
            throw new IllegalArgumentException("semi-send process can only use http channel!");
        }
    }

    public void gB() {
        if (nu.aB()) {
            gF().gB();
        }
    }

    public void gC() {
        gF().gC();
    }

    public void gD() {
        gF().gD();
    }

    public nl gl() {
        return gF().gl();
    }

    public void gm() {
        if (nu.aB()) {
            gF().gm();
        }
    }

    public ka v(int i, int i2) {
        mb.d("SharkProcessProxy", Process.myPid() + " unregisterSharkPush() from cmdId: " + i + " flag: " + i2);
        return !nu.aB() ? gE().v(i, i2) : gF().v(i, i2);
    }
}
