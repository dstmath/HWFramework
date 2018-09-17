package tmsdkobf;

import tmsdk.common.utils.s;
import tmsdkobf.ju.a;
import tmsdkobf.ju.b;

public class li {
    private static li yo = null;
    private b yp = new b() {
        /* JADX WARNING: Missing block: B:4:0x0006, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void b(a aVar) {
            if (aVar != null) {
                if (aVar.tA != null) {
                    ju juVar = (ju) fj.D(17);
                    t tVar;
                    final boolean z;
                    if (1039 == aVar.tA.Y) {
                        o oVar = (o) nn.a(aVar.tA.ae, new o(), false);
                        if (com.tencent.tcuser.util.a.au(oVar.Z) != (byte) 1) {
                            gf.S().J(0);
                        } else {
                            int av = com.tencent.tcuser.util.a.av(oVar.aa);
                            gf.S().d((long) aVar.tA.ag.R);
                            gf.S().J(av);
                            ll.aM(0);
                            gf.S().K(0);
                        }
                    } else if (615 == aVar.tA.Y) {
                        gf.S().k(Boolean.valueOf(com.tencent.tcuser.util.a.au(((o) nn.a(aVar.tA.ae, new o(), false)).Z) == (byte) 1));
                        lj.ez();
                    } else if (1445 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() > 1) {
                                gf.S().f(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(0)) == (byte) 1));
                                gf.S().m(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(1)) == (byte) 1));
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (1446 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() > 0) {
                                z = com.tencent.tcuser.util.a.au((String) tVar.ar.get(0)) == (byte) 1;
                                gf.S().j(Boolean.valueOf(z));
                                new Thread(new Runnable() {
                                    public void run() {
                                        if (z) {
                                            if (im.bK() != null) {
                                                im.bK().gC();
                                            }
                                        } else if (im.bK() != null) {
                                            im.bK().gD();
                                        }
                                    }
                                }).start();
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (1463 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() > 1) {
                                kt.saveActionData(1320011);
                                gf.S().g(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(0)) == (byte) 1));
                                z = com.tencent.tcuser.util.a.au((String) tVar.ar.get(1)) == (byte) 1;
                                gf.S().h(Boolean.valueOf(z));
                                if (z) {
                                    lk.a(z, ir.rV, "OP_POST_NOTIFICATION");
                                    lk.a(z, ir.rV, "OP_SYSTEM_ALERT_WINDOW");
                                    lk.a(z, ir.rV, "OP_WRITE_SMS");
                                }
                                gf.S().i(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(2)) == (byte) 1));
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (1466 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() > 4) {
                                gf.S().a(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(0)) == (byte) 1));
                                gf.S().b(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(1)) == (byte) 1));
                                gf.S().c(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(2)) == (byte) 1));
                                gf.S().d(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(3)) == (byte) 1));
                                gf.S().e(Boolean.valueOf(com.tencent.tcuser.util.a.au((String) tVar.ar.get(4)) == (byte) 1));
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (519 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() > 0) {
                                pu.hW().b(aVar);
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (849 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() >= 1) {
                                gf.S().e(com.tencent.tcuser.util.a.aw((String) tVar.ar.get(0)));
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (1570 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() >= 1) {
                                z = com.tencent.tcuser.util.a.au((String) tVar.ar.get(0)) == (byte) 1;
                                gf.S().n(Boolean.valueOf(z));
                                if (z) {
                                    juVar.i();
                                } else {
                                    juVar.i();
                                    juVar.h();
                                }
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    } else if (1575 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() >= 8 && fr.r().a(tVar, aVar.tA.ag.R)) {
                                int av2 = com.tencent.tcuser.util.a.av((String) tVar.ar.get(0));
                                if (av2 != -1) {
                                    gf.S().K(av2);
                                    gf.S().J(0);
                                } else {
                                    s.bW(-1);
                                }
                                fr.s();
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        fr.s();
                        return;
                    } else if (1589 == aVar.tA.Y) {
                        tVar = (t) nn.a(aVar.tA.ae, new t(), false);
                        if (tVar != null) {
                            if (tVar.ar != null && tVar.ar.size() >= 1) {
                                la.j(tVar.ar);
                            }
                        }
                        juVar.a(aVar, 3, 2);
                        return;
                    }
                    juVar.a(aVar, 3, 1);
                }
            }
        }
    };

    public li() {
        ju juVar = (ju) fj.D(17);
        juVar.a(1039, this.yp);
        juVar.a(615, this.yp);
        juVar.a(-1, this.yp);
        juVar.a(1445, this.yp);
        juVar.a(1446, this.yp);
        juVar.a(1463, this.yp);
        juVar.a(1466, this.yp);
        juVar.a(519, this.yp);
        juVar.a(849, this.yp);
        juVar.a(1570, this.yp);
        juVar.a(1575, this.yp);
        juVar.a(1589, this.yp);
    }

    public static li ey() {
        if (yo == null) {
            Class cls = li.class;
            synchronized (li.class) {
                if (yo == null) {
                    yo = new li();
                }
            }
        }
        return yo;
    }

    public void C(int i) {
        ((ju) fj.D(17)).C(i);
    }
}
