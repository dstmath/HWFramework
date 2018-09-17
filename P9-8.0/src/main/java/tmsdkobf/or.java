package tmsdkobf;

import android.content.Context;
import tmsdkobf.nw.f;
import tmsdkobf.oq.a;

public class or {
    private final int CP = 3;
    private oq IR = null;
    private Context context = null;

    public or(Context context, a aVar, om omVar) {
        this.context = context;
        this.IR = new oq(context, aVar, omVar);
    }

    public int a(f fVar, byte[] bArr) {
        if (fVar == null || bArr == null) {
            return -10;
        }
        int i = -1;
        int i2 = 0;
        while (i2 < 3) {
            if (!fVar.gp()) {
                i = this.IR.a(fVar, bArr);
                mb.d("TmsTcpNetwork", "[tcp_control]sendDataAsync(), ret: " + i + " times: " + (i2 + 1) + " data.length: " + bArr.length);
                if (i == 0) {
                    break;
                }
                if (2 != i2) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        mb.o("TmsTcpNetwork", "[tcp_control]sendDataAsync() InterruptedException e: " + e.toString());
                    }
                }
                i2++;
            } else {
                mb.o("TmsTcpNetwork", "[tcp_control][time_out]sendDataAsync(), send time out");
                i = -17;
                break;
            }
        }
        return i;
    }

    public om gQ() {
        return this.IR.gQ();
    }

    public String hf() {
        return this.IR.hf();
    }

    public boolean hl() {
        return this.IR.hl();
    }

    public boolean hm() {
        return this.IR.hm();
    }

    public int hq() {
        mb.n("TmsTcpNetwork", "[tcp_control]close()");
        qg.d(65541, "[ocean] close");
        return this.IR.stop();
    }

    public int hr() {
        mb.n("TmsTcpNetwork", "[tcp_control]connect()");
        if (lw.eJ()) {
            mb.s("TmsTcpNetwork", "connect HttpConnection.couldNotConnect()");
            return -230000;
        }
        qg.d(65541, "[ocean] connect |ret|" + this.IR.C(this.context));
        return this.IR.C(this.context);
    }

    public int hs() {
        if (lw.eJ()) {
            mb.s("TmsTcpNetwork", "[tcp_control]reconnect(), HttpConnection.couldNotConnect()");
            return -230000;
        }
        qg.d(65541, "[ocean] reconnect |ret|" + this.IR.hg());
        return this.IR.hg();
    }
}
