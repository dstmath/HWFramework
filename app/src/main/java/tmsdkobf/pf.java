package tmsdkobf;

import android.os.Process;
import java.lang.ref.WeakReference;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class pf implements pe {
    private pd Hr;
    private pc Hs;
    private ov Ht;
    private boolean Hu;
    protected long lU;

    public pf(long j) {
        this.Hu = false;
        this.lU = j;
    }

    private ov gA() {
        if (this.Hu || lk.wC) {
            if (this.Ht == null) {
                this.Ht = (ov) ManagerCreatorC.getManager(ov.class);
            }
            return this.Ht;
        }
        throw new RuntimeException("getOldProtocolImp too early");
    }

    private pc gy() {
        if (this.Hu || lk.wC) {
            if (this.Hs == null) {
                this.Hs = pc.gt();
            }
            return pc.gt();
        }
        throw new RuntimeException("getSharkProcessProxy too early");
    }

    private pd gz() {
        if (this.Hu || lk.wC) {
            if (this.Hr == null) {
                this.Hr = (pd) ManagerCreatorC.getManager(pd.class);
            }
            return this.Hr;
        }
        throw new RuntimeException("getSharkProtocolQueue too early");
    }

    public WeakReference<ll> a(int i, fs fsVar, fs fsVar2, int i2, lg lgVar) {
        return c(Process.myPid(), 0, 0, 0, this.lU, i, fsVar, null, fsVar2, i2, lgVar, null, -1, 0);
    }

    public WeakReference<ll> a(int i, fs fsVar, fs fsVar2, int i2, lg lgVar, long j) {
        return c(Process.myPid(), 0, 0, 0, this.lU, i, fsVar, null, fsVar2, i2, lgVar, null, j, 0);
    }

    public WeakReference<ll> a(qs qsVar, ld ldVar) {
        return gA().a(this.lU, qsVar, ldVar);
    }

    public void a(int i, fs fsVar, int i2, li liVar) {
        a(0, i, fsVar, i2, liVar, false);
    }

    public void a(long j, int i, fs fsVar, int i2, li liVar, boolean z) {
        if (gz().gv()) {
            d.e("SharkProcessProxy", "sending process registerSharkPush() from cmdId: " + i + " flag: " + i2);
            gz().a(!z ? this.lU : j, i, fsVar, i2, liVar, z);
            return;
        }
        d.e("SharkProcessProxy", "unsending process registerSharkPush() from cmdId: " + i + " flag: " + i2);
        gy().a(this.lU, i, fsVar, i2, liVar);
    }

    public void b(int i, long j, int i2, fs fsVar) {
        c(Process.myPid(), 0, i, j, this.lU, i2, fsVar, null, null, 1073741824, null, null, -1, 0);
    }

    public String c() {
        return gz().c();
    }

    public WeakReference<ll> c(int i, int i2, int i3, long j, long j2, int i4, fs fsVar, byte[] bArr, fs fsVar2, int i5, lg lgVar, lh lhVar, long j3, long j4) {
        d.e("SharkProcessProxy", Process.myPid() + " sendShark() from pid: " + i + " ipcSeqNo: " + i2 + " callerIdent: " + j2 + " cmdId: " + i4 + " flag: " + i5 + " callBackTimeout: " + j3);
        if (!gz().gv()) {
            gy().a(i, this.lU, i3, j, i4, fsVar, fsVar2, i5, lgVar, j3, j4);
            return null;
        } else if (!lk.bh(i5)) {
            return gz().c(i, i2, i3, j, j2, i4, fsVar, bArr, fsVar2, i5, lgVar, lhVar, j3, j4);
        } else {
            oq gm = gz().gm();
            return gm != null ? gm.a(i, i2, i3, j, j2, i4, fsVar, bArr, fsVar2, i5, lgVar, lhVar, j3, j4) : null;
        }
    }

    public void ce(int i) {
        gz().ce(i);
    }

    public void cf(int i) {
        gz().cf(i);
    }

    public void gl() {
        gz().gl();
    }

    public void init(boolean z) {
        this.Hu = z;
    }

    public void onImsiChanged() {
        gz().onImsiChanged();
    }

    public li v(int i, int i2) {
        d.e("SharkProcessProxy", Process.myPid() + " unregisterSharkPush() from cmdId: " + i + " flag: " + i2);
        return !gz().gv() ? gy().v(i, i2) : gz().v(i, i2);
    }
}
