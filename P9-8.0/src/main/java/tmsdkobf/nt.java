package tmsdkobf;

import android.os.PowerManager;
import android.text.TextUtils;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdkobf.nx.b;

public class nt {
    private static nt Ef = null;
    private nl CT = null;
    private pe<Integer, a> Ee = new pe(SmsCheckResult.ESCT_200);
    private PowerManager Eg = null;
    public Map<Byte, Integer> Eh = new HashMap();

    public static class a {
        public BitSet Ej = new BitSet();
        public String Ek = "";
        public int El = 0;
        public boolean Em = false;
        public boolean En = false;
        public long Eo = 0;
        public int Ep = 0;
        public int Eq = 0;
        public long Er = System.currentTimeMillis();
        public String Es = "";
        public String Et = "";
        public int bz = 0;
        public long createTime = System.currentTimeMillis();
        public int eB = 0;
    }

    public static nt ga() {
        Class cls = nt.class;
        synchronized (nt.class) {
            if (Ef == null) {
                Ef = new nt();
            }
            return Ef;
        }
    }

    public void a(byte b, int i) {
        synchronized (this.Eh) {
            this.Eh.put(Byte.valueOf(b), Integer.valueOf(i));
        }
    }

    public synchronized void a(int i, long j, String str) {
        a aVar = new a();
        aVar.Ek = str;
        aVar.Eo = j;
        aVar.El = np.fS().c(false, false);
        if (this.Eg != null) {
            try {
                aVar.Em = this.Eg.isScreenOn();
            } catch (Throwable th) {
            }
        }
        this.Ee.put(Integer.valueOf(i), aVar);
    }

    public synchronized void a(String str, int i, int i2, bw bwVar, int i3) {
        a(str, i, i2, bwVar, i3, 0, null);
    }

    public synchronized void a(String str, int i, int i2, bw bwVar, int i3, int i4, String str2) {
        a aVar = (a) this.Ee.get(Integer.valueOf(i2));
        if (aVar != null) {
            nv.r("" + str, "[ocean][shark_funnel]|seqNo|seq_" + i2 + "|step|" + i3 + "|cmdId|cmd_" + i + "|stepTime|" + (System.currentTimeMillis() - aVar.Er) + "|retCode|" + i4 + "|flow|" + str2);
            if (i == 21) {
                qg.a(65542, "|step|" + i3 + "|cmdId|" + i + "|retCode|" + i4);
            }
            aVar.bz = i;
            aVar.Ej.set(i3, true);
            if (str2 != null) {
                aVar.Es = str2;
            }
            if (i3 == 14 || i3 == 9 || i3 == 10) {
                aVar.Ep = i4;
                if (this.CT != null) {
                    this.CT.c(i, i4);
                }
            } else if (i3 != 16) {
                aVar.eB = i4;
            } else {
                aVar.Eq = i4;
                if (this.CT != null) {
                    this.CT.d(i, i4);
                }
            }
            aVar.Er = System.currentTimeMillis();
        }
    }

    public synchronized void a(String str, int i, int i2, ce ceVar, int i3, int i4) {
        a(str, i, i2, ceVar, i3, i4, null);
    }

    public synchronized void a(String str, int i, int i2, ce ceVar, int i3, int i4, String str2) {
        a aVar = (a) this.Ee.get(Integer.valueOf(i2));
        if (aVar != null) {
            nv.r("" + str, "[ocean][shark_funnel]|seqNo|seq_" + i2 + "|step|" + i3 + "|cmdId|cmd_" + i + "|stepTime|" + (System.currentTimeMillis() - aVar.Er) + "|retCode|" + i4 + "|flow|" + str2);
            if (i == 10021) {
                qg.a(65542, "|step|" + i3 + "|cmdId|" + i + "|retCode|" + i4);
            }
            aVar.bz = i;
            if (str2 != null) {
                aVar.Et = str2;
            }
            aVar.Ej.set(i3, true);
            if (i3 == 14) {
                aVar.Ep = i4;
            } else if (i3 != 16) {
                aVar.eB = i4;
            } else {
                aVar.Eq = i4;
            }
            aVar.Er = System.currentTimeMillis();
        }
    }

    public void b(byte b) {
        synchronized (this.Eh) {
            this.Eh.remove(Byte.valueOf(b));
        }
    }

    public synchronized void b(nl nlVar) {
        this.CT = nlVar;
        try {
            this.Eg = (PowerManager) TMSDKContext.getApplicaionContext().getSystemService("power");
        } catch (Throwable th) {
        }
        nx.gs().a(new b() {
            public void gb() {
                synchronized (nt.this) {
                    if (nt.this.Ee.size() > 0) {
                        mb.n("SharkFunnelModel", "[tcp_control]mark network changed for every running task, seqNos: " + nt.this.Ee.hH().keySet());
                        for (Entry value : nt.this.Ee.hH().entrySet()) {
                            ((a) value.getValue()).En = true;
                        }
                    }
                }
            }
        });
    }

    /* JADX WARNING: Missing block: B:22:0x0112, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean b(int i, boolean z) {
        a aVar = (a) this.Ee.get(Integer.valueOf(i));
        if (aVar == null) {
            return false;
        }
        int i2;
        this.Ee.f(Integer.valueOf(i));
        boolean z2 = aVar.Ej.get(15);
        int i3 = aVar.eB;
        if (i3 != 0) {
            int bi = ne.bi(i3);
            i2 = aVar.El;
            if (i2 == -2) {
                i3 = (i3 - bi) - 160000;
            } else if (bi == -50000) {
                int i4 = bi;
                if (aVar.En) {
                    i4 = -550000;
                } else if (i2 == -4) {
                    i4 = -530000;
                } else if (i2 == -1) {
                    i4 = -220000;
                } else if (i2 == -3) {
                    i4 = -540000;
                }
                i3 = (i3 - bi) + i4;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("|cmd|cmd_");
        stringBuilder.append(aVar.bz);
        stringBuilder.append("|seqNo|seq_");
        stringBuilder.append(i);
        if (!TextUtils.isEmpty(aVar.Ek)) {
            stringBuilder.append("|reason|");
            stringBuilder.append(aVar.Ek);
        }
        stringBuilder.append("|channel|");
        stringBuilder.append(!z2 ? "tcp" : "http");
        stringBuilder.append("|step|");
        stringBuilder.append(aVar.Ej.toString());
        stringBuilder.append("|netState|");
        stringBuilder.append(np.bn(aVar.El));
        stringBuilder.append("|isScreenOn|");
        stringBuilder.append(aVar.Em);
        stringBuilder.append("|isNetworkChanged|");
        stringBuilder.append(aVar.En);
        stringBuilder.append("|tcpRetCode|");
        stringBuilder.append(aVar.Ep);
        stringBuilder.append("|httpRecCode|");
        stringBuilder.append(aVar.Eq);
        stringBuilder.append("|retCode|");
        if (i3 != aVar.eB) {
            stringBuilder.append(aVar.eB).append("->").append(i3);
        } else {
            stringBuilder.append(aVar.eB);
        }
        stringBuilder.append("|timeOut|");
        stringBuilder.append(aVar.Eo);
        stringBuilder.append("|totalTime|");
        stringBuilder.append(System.currentTimeMillis() - aVar.createTime);
        stringBuilder.append("|sendFlow|");
        stringBuilder.append(aVar.Es);
        stringBuilder.append("|recFlow|");
        stringBuilder.append(aVar.Et);
        if (aVar.eB == 0) {
            nv.z("SharkFunnelModel", "[shark_funnel]" + stringBuilder.toString());
        } else if (z) {
            nv.A("SharkFunnelModel", "xxxxxxxxxxxx [shark_funnel]" + stringBuilder.toString());
        } else {
            nv.A("SharkFunnelModel", "tttt [shark_funnel]" + stringBuilder.toString());
        }
        if (this.CT != null) {
            i2 = aVar.bz <= 10000 ? aVar.bz : aVar.bz - 10000;
            if (!(i2 == 999 || i2 == 794 || i2 == 797 || i2 == 782)) {
                if (i3 == 0) {
                    this.CT.e(i2, i3);
                } else if (z) {
                    this.CT.e(i2, i3);
                }
            }
        }
    }

    public synchronized void bp(int i) {
        this.Ee.f(Integer.valueOf(i));
    }

    public synchronized boolean bq(int i) {
        return b(i, true);
    }

    public int c(byte b) {
        synchronized (this.Eh) {
            Integer num = (Integer) this.Eh.get(Byte.valueOf(b));
            if (num == null) {
                return -1;
            }
            int intValue = num.intValue();
            return intValue;
        }
    }
}
