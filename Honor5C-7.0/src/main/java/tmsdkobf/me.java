package tmsdkobf;

import tmsdkobf.lb.a;
import tmsdkobf.lb.b;

/* compiled from: Unknown */
public class me {
    private static me AA;
    private b AB;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.me.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.me.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.me.<clinit>():void");
    }

    public me() {
        this.AB = new b() {
            final /* synthetic */ me AC;

            /* compiled from: Unknown */
            /* renamed from: tmsdkobf.me.1.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ boolean AD;
                final /* synthetic */ AnonymousClass1 AE;

                AnonymousClass1(AnonymousClass1 anonymousClass1, boolean z) {
                    this.AE = anonymousClass1;
                    this.AD = z;
                }

                public void run() {
                    if (this.AD) {
                        pg.gB().start();
                    } else {
                        pg.stop();
                    }
                }
            }

            {
                this.AC = r1;
            }

            public void a(a aVar) {
                boolean z = false;
                if (aVar != null && aVar.wx != null) {
                    lb lbVar = (lb) fe.ad(17);
                    if (1039 == aVar.wx.H) {
                        l lVar = (l) ot.a(aVar.wx.N, new l(), false);
                        if (com.tencent.tcuser.util.a.bt(lVar.I) != (byte) 1) {
                            fw.w().ai(0);
                        } else {
                            int bu = com.tencent.tcuser.util.a.bu(lVar.J);
                            fw.w().e((long) aVar.wx.P.A);
                            fw.w().ai(bu);
                        }
                    } else if (1427 == aVar.wx.H) {
                        r1 = (q) ot.a(aVar.wx.N, new q(), false);
                        if (r1 == null || r1.aa == null || r1.aa.size() <= 0) {
                            lbVar.a(aVar, 3, 2);
                            return;
                        }
                        byte bt = com.tencent.tcuser.util.a.bt((String) r1.aa.get(0));
                        fw.w().f(Boolean.valueOf(bt == (byte) 1));
                        ma.p(29989, bt);
                    } else if (615 == aVar.wx.H) {
                        if (com.tencent.tcuser.util.a.bt(((l) ot.a(aVar.wx.N, new l(), false)).I) == (byte) 1) {
                            z = true;
                        }
                        fw.w().k(Boolean.valueOf(z));
                        mf.eL();
                    } else if (1445 == aVar.wx.H) {
                        r1 = (q) ot.a(aVar.wx.N, new q(), false);
                        if (r1 == null || r1.aa == null || r1.aa.size() <= 0) {
                            lbVar.a(aVar, 3, 2);
                            return;
                        }
                        if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1) {
                            z = true;
                        }
                        fw.w().i(Boolean.valueOf(z));
                    } else if (1446 == aVar.wx.H) {
                        r1 = (q) ot.a(aVar.wx.N, new q(), false);
                        if (r1 == null || r1.aa == null || r1.aa.size() <= 0) {
                            lbVar.a(aVar, 3, 2);
                            return;
                        }
                        if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1) {
                            z = true;
                        }
                        fw.w().j(Boolean.valueOf(z));
                        new Thread(new AnonymousClass1(this, z)).start();
                    } else if (1463 == aVar.wx.H) {
                        r1 = (q) ot.a(aVar.wx.N, new q(), false);
                        if (r1 == null || r1.aa == null || r1.aa.size() <= 1) {
                            lbVar.a(aVar, 3, 2);
                            return;
                        }
                        boolean z2 = com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1;
                        fw.w().g(Boolean.valueOf(z2));
                        if (z2) {
                            ma.bx(1320011);
                        }
                        if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(1)) == (byte) 1) {
                            z = true;
                        }
                        fw.w().h(Boolean.valueOf(z));
                    } else if (1466 == aVar.wx.H) {
                        r1 = (q) ot.a(aVar.wx.N, new q(), false);
                        if (r1 == null || r1.aa == null || r1.aa.size() <= 4) {
                            lbVar.a(aVar, 3, 2);
                            return;
                        }
                        fw.w().a(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1));
                        fw.w().b(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(1)) == (byte) 1));
                        fw.w().c(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(2)) == (byte) 1));
                        fw.w().d(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(3)) == (byte) 1));
                        if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(4)) == (byte) 1) {
                            z = true;
                        }
                        fw.w().e(Boolean.valueOf(z));
                    }
                    lbVar.a(aVar, 3, 1);
                }
            }
        };
        lb lbVar = (lb) fe.ad(17);
        lbVar.a(1427, this.AB);
        lbVar.a(1039, this.AB);
        lbVar.a(615, this.AB);
        lbVar.a(-1, this.AB);
        lbVar.a(1445, this.AB);
        lbVar.a(1446, this.AB);
        lbVar.a(1463, this.AB);
        lbVar.a(1466, this.AB);
    }

    public static me eK() {
        if (AA == null) {
            synchronized (me.class) {
                if (AA == null) {
                    AA = new me();
                }
            }
        }
        return AA;
    }

    public void ac(int i) {
        ((lb) fe.ad(17)).ac(i);
    }
}
