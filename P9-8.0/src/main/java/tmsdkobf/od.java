package tmsdkobf;

import android.content.Context;
import java.util.Iterator;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdkobf.nw.f;
import tmsdkobf.og.d;

public class od implements nm {
    private nl CT;
    private boolean CX = false;
    private nw Dm;
    private a EW;
    private ng Hx;
    private og Hy;
    private om Hz;
    private boolean oc = false;

    public interface a {
        void a(boolean z, int i, byte[] bArr, f fVar);

        void b(boolean z, int i, f fVar);
    }

    public od(boolean z, Context context, nl nlVar, boolean z2, a aVar, d dVar, nw.d dVar2, nw nwVar, String str) {
        this.oc = z;
        this.EW = aVar;
        this.CT = nlVar;
        this.Dm = nwVar;
        this.CX = z2;
        if (this.oc) {
            this.Hz = new nj(context, z2, nlVar, str);
            this.Hx = new ng(context, nlVar, this.Hz, this.CX);
            this.Hy = new og(nlVar, this.Hz, aVar, dVar, this, dVar2, this.Dm);
        } else if (nu.aC()) {
            this.Hz = new nj(context, z2, nlVar, str);
            this.Hx = new ng(context, nlVar, this.Hz, this.CX);
        }
    }

    public static void a(f fVar, int i, int i2, int i3) {
        if (fVar != null && fVar.Ft != null) {
            int size = fVar.Ft.size();
            for (int i4 = 0; i4 < size; i4++) {
                bw bwVar = (bw) fVar.Ft.get(i4);
                if (bwVar != null) {
                    String str;
                    nt ga = nt.ga();
                    int i5 = bwVar.bz;
                    int i6 = bwVar.ey;
                    String str2 = "SharkWharf";
                    if (i3 <= 0) {
                        str = null;
                    } else {
                        str = String.format("%d/%d", new Object[]{Integer.valueOf(i3), Integer.valueOf(size)});
                    }
                    ga.a(str2, i5, i6, bwVar, i, i2, str);
                }
            }
        }
    }

    public void a(final f fVar, int i) {
        boolean z = false;
        if (fVar == null) {
            StringBuilder append = new StringBuilder().append("onSendTcpFailed() sharkSend is null? ");
            String str = "SharkWharf";
            if (fVar == null) {
                z = true;
            }
            mb.o(str, append.append(z).toString());
        } else if (fVar.Fh == IncomingSmsFilterConsts.PAY_SMS) {
            mb.o("SharkWharf", "onSendTcpFailed(), user set only use tcp, so really fail");
            this.EW.b(true, i, fVar);
        } else if (fVar.gq()) {
            mb.d("SharkWharf", "onSendTcpFailed(), isTcpVip, so really fail");
            this.EW.b(true, i, fVar);
        } else {
            mb.n("SharkWharf", "onSendTcpFailed(), tcp通道发送失败，转http通道");
            fVar.Fn = false;
            byte[] a = nh.a(fVar, false, this.Dm.b(), this.CT);
            if (a != null) {
                a(fVar, 15, 0, a.length);
                this.Hx.a(fVar, a, new tmsdkobf.nf.a() {
                    public void b(int i, byte[] bArr) {
                        if (i != 0) {
                            i -= 42000000;
                        }
                        mb.d("SharkWharf", "onSendTcpFailed(), retry with http, http errCode: " + i);
                        od.a(fVar, 16, i, 0);
                        od.this.EW.a(false, i, bArr, fVar);
                    }
                });
                return;
            }
            mb.s("SharkWharf", "[tcp_control][http_control][shark_v4]onSendTcpFailed(), ConverterUtil.createSendBytes() return null!");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0112  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0123  */
    /* JADX WARNING: Missing block: B:26:0x0047, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void d(final f fVar) {
        boolean z = false;
        synchronized (this) {
            if (!this.oc) {
                if (!nu.aC()) {
                    throw new RuntimeException("sendData(), not in sending or semiSending process!");
                }
            }
            if (fVar != null) {
                boolean z2;
                if (fVar.Ft != null) {
                    if (fVar.Ft.size() > 0) {
                        int i = 0;
                        Iterator it = fVar.Ft.iterator();
                        while (it.hasNext()) {
                            bw bwVar = (bw) it.next();
                            mb.n("SharkWharf_CMDID", "[" + i + "]发包：cmd id:[" + bwVar.bz + "]seqNo:[" + bwVar.ey + "]refSeqNo:[" + bwVar.ez + "]retCode:[" + bwVar.eB + "]dataRetCode:[" + bwVar.eC + "]");
                            i++;
                        }
                    }
                }
                if (fVar.gq()) {
                    z2 = false;
                    if (fVar.Fo) {
                        mb.s("SharkWharf", "[tcp_control][http_control]sendData(), cloudcmd not allow tcp and this is tcp vip, failed!");
                        this.EW.b(true, -30000007, fVar);
                        return;
                    }
                }
                boolean z3;
                boolean z4;
                if (this.Hy != null) {
                    if (!this.Hy.gU()) {
                        z3 = true;
                        z4 = z3 && fVar.Fh != IncomingSmsFilterConsts.PAY_SMS;
                        z2 = nu.aC() || fVar.Fh == 2048 || fVar.Fh == 512 || fVar.Fo || z4;
                        if (z2) {
                            String str = "SharkWharf";
                            StringBuilder append = new StringBuilder().append("[tcp_control][http_control]sendData(), use http channel, for:  only http enable? false isSemiSendProcess? ").append(nu.aC()).append(" user select CHANNEL_LARGE_DATA ? ").append(fVar.Fh == 2048).append(" user select ONLY_HTTP ? ");
                            if (fVar.Fh == 512) {
                                z = true;
                            }
                            mb.s(str, append.append(z).append(" cloud cmd not allow tcp? ").append(fVar.Fo).append(" prefer http? ").append(z4).toString());
                        }
                    }
                }
                z3 = false;
                if (z3) {
                    if (nu.aC()) {
                        if (z2) {
                        }
                    }
                    if (z2) {
                    }
                }
                if (nu.aC()) {
                }
                if (z2) {
                }
                if (z2) {
                    mb.n("SharkWharf", "[tcp_control][http_control]sendData(), use http channel");
                    fVar.Fn = false;
                    byte[] a = nh.a(fVar, false, this.Dm.b(), this.CT);
                    if (a != null) {
                        a(fVar, 15, 0, a.length);
                        this.Hx.a(fVar, a, new tmsdkobf.nf.a() {
                            public void b(int i, byte[] bArr) {
                                if (i != 0) {
                                    i -= 42000000;
                                }
                                od.a(fVar, 16, i, 0);
                                mb.d("SharkWharf", "[tcp_control][http_control]sendData(), http callback, errCode: " + i);
                                od.this.EW.a(false, i, bArr, fVar);
                            }
                        });
                    } else {
                        mb.s("SharkWharf", "[tcp_control][http_control][shark_v4]sendData(), ConverterUtil.createSendBytes() return null!");
                        return;
                    }
                }
                mb.n("SharkWharf", "[tcp_control][http_control]sendData(), use tcp channel");
                fVar.Fn = true;
                if (fVar.Fm) {
                    this.Hy.f(fVar);
                } else if (fVar.gr()) {
                    this.Hy.f(fVar);
                } else {
                    this.Hy.e(fVar);
                }
            } else {
                mb.o("SharkWharf", "sendData(), sharkSend is null");
            }
        }
    }

    public om gQ() {
        return this.Hz;
    }

    public og gj() {
        if (this.oc) {
            return this.Hy;
        }
        throw new RuntimeException("getTmsTcpManager(), not in sending process!");
    }
}
