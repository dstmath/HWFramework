package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;

public class og implements tmsdkobf.nx.a, tmsdkobf.oc.a, tmsdkobf.ol.b, tmsdkobf.ol.c, tmsdkobf.oq.a {
    private final Object Ba;
    private nl CT;
    private boolean CX;
    private nw Dm;
    protected tmsdkobf.od.a EW;
    private or HQ;
    private nm HR;
    private ka HS;
    private d HT;
    private oc HU;
    private ol HV;
    private int HW;
    private long HX;
    private long HY;
    private boolean HZ;
    private LinkedList<f> Ia;
    private byte Ib;
    private Handler Ic;
    private boolean Id;
    private op<f> Ie;
    private Context mContext;
    private HandlerThread te;

    public interface d {
        void a(tmsdkobf.nw.f fVar);
    }

    private abstract class c implements tmsdkobf.nw.b {
        int Ij = 0;
        int ey = 0;

        public c(int i, int i2) {
            this.Ij = i;
            this.ey = i2;
        }

        public void a(boolean z, int i, int i2, ArrayList<ce> arrayList) {
            if (i != 0) {
                w(i, -1);
            } else if (this.Ij == 10999 && i == 0) {
                e(null);
            } else if (arrayList == null || arrayList.size() == 0) {
                w(-41250000, -1);
            } else {
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    ce ceVar = (ce) it.next();
                    if (ceVar != null && ceVar.bz == this.Ij) {
                        if (ceVar.eB == 0 && ceVar.eC == 0) {
                            e(ceVar);
                            return;
                        } else {
                            w(ceVar.eB, ceVar.eC);
                            return;
                        }
                    }
                }
                w(-41250000, -1);
            }
        }

        protected abstract void e(ce ceVar);

        protected abstract void w(int i, int i2);
    }

    private class a extends c {
        private int Ig = 0;
        private String Ih = "";

        public a(int i, int i2, String str) {
            super(10997, i);
            this.Ig = i2;
            this.Ih = str;
        }

        protected void e(ce ceVar) {
            mb.n("TmsTcpManager", "[tcp_control][f_p]fp success, mRetryTimes: " + this.Ig);
            og.this.Id = false;
            og.this.Ib = (byte) (byte) 1;
            og.this.HZ = false;
            nt.ga().a("TmsTcpManager", 997, this.ey, ceVar, 30, 0);
            nt.ga().bq(this.ey);
            og.this.a(2, null, 0, 0, true);
        }

        protected void w(int i, int i2) {
            int bj = ne.bj(i);
            boolean z = this.Ig < 1 && ne.bk(bj) && og.this.HW < 3 && og.this.HU.gG() > 0;
            mb.s("TmsTcpManager", "[tcp_control][f_p]fp fail, retCode: " + bj + " dataRetCode: " + i2 + " mRetryTimes: " + this.Ig + " need retry? " + z);
            og.this.Id = true;
            og.this.Ib = (byte) (byte) 0;
            og.this.HZ = true;
            nt.ga().a("TmsTcpManager", 997, this.ey, (ce) null, 30, bj);
            nt.ga().b(this.ey, !z);
            og.this.a(3, null, bj, 0, true);
            if (z) {
                mb.n("TmsTcpManager", "[tcp_control][f_p]fp fail, ref count: " + og.this.HU.gG() + ", retry send fp in " + og.this.HU.az().E + "s");
                og.this.HW = og.this.HW + 1;
                og ogVar = og.this;
                String str = "delay_fp_retry:" + this.Ih + ":" + bj;
                int i3 = this.Ig + 1;
                this.Ig = i3;
                ogVar.a(11, str, i3, ((long) og.this.HU.az().E) * 1000, true);
                return;
            }
            mb.n("TmsTcpManager", "[tcp_control][f_p]fp fail, should not retry, retCode: " + bj);
        }
    }

    private class b extends c {
        private int Ig = 0;
        private String Ih = "";
        private byte Ii = (byte) 0;

        public b(int i, int i2, String str, byte b) {
            super(10999, i);
            this.Ig = i2;
            this.Ih = str;
            this.Ii = (byte) b;
        }

        protected void e(ce ceVar) {
            mb.n("TmsTcpManager", "[tcp_control][h_b]hb success, helloSeq: " + this.Ii + " mRetryTimes: " + this.Ig + " reason: " + this.Ih);
            nt.ga().a("TmsTcpManager", 999, this.ey, ceVar, 30, 0);
            nt.ga().bq(this.ey);
            nt.ga().b(this.Ii);
        }

        protected void w(int i, int i2) {
            boolean z = false;
            mb.n("TmsTcpManager", "[tcp_control][h_b]hb fail, retCode: " + i + " dataRetCode: " + i2 + " helloSeq: " + this.Ii + " mRetryTimes: " + this.Ig + " reason: " + this.Ih);
            if (this.Ig >= 1) {
                mb.r("TmsTcpManager", "[tcp_control][h_b]hb fail again, mark disconnect not handled for reconnect");
                og.this.Id = true;
                og.this.Ib = (byte) (byte) 0;
            }
            boolean z2 = this.Ig < 1 && ne.bk(i) && og.this.HW < 3 && og.this.HU.gG() > 0;
            nt.ga().a("TmsTcpManager", 999, this.ey, (ce) null, 30, i);
            nt ga = nt.ga();
            int i3 = this.ey;
            if (!z2) {
                z = true;
            }
            ga.b(i3, z);
            nt.ga().b(this.Ii);
            if (z2) {
                mb.n("TmsTcpManager", "[tcp_control][h_b]hb fail, retry");
                og ogVar = og.this;
                String str = "hb_retry:" + this.Ih + ":" + i;
                int i4 = this.Ig + 1;
                this.Ig = i4;
                ogVar.a(13, str, i4, 2000, true);
            }
        }
    }

    private class e extends Handler {
        public e(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:14:0x0041, code:
            r31 = 0;
            r34 = 0;
            r37 = null;
     */
        /* JADX WARNING: Missing block: B:15:0x004f, code:
            if (r30.Dh.gp() != false) goto L_0x0112;
     */
        /* JADX WARNING: Missing block: B:17:0x0059, code:
            if (r30.Dh.gr() == false) goto L_0x011e;
     */
        /* JADX WARNING: Missing block: B:18:0x005b, code:
            r38 = null;
     */
        /* JADX WARNING: Missing block: B:19:0x005d, code:
            if (r38 != null) goto L_0x012a;
     */
        /* JADX WARNING: Missing block: B:21:0x0067, code:
            if (tmsdkobf.og.i(r44.If) != false) goto L_0x0246;
     */
        /* JADX WARNING: Missing block: B:23:0x0075, code:
            if (tmsdkobf.og.g(r44.If).hl() == false) goto L_0x027d;
     */
        /* JADX WARNING: Missing block: B:24:0x0077, code:
            tmsdkobf.mb.n("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [vip] connection is ok");
     */
        /* JADX WARNING: Missing block: B:25:0x0080, code:
            if (r31 != 0) goto L_0x02a9;
     */
        /* JADX WARNING: Missing block: B:27:0x0088, code:
            if (r30.Dh.Ft != null) goto L_0x02c7;
     */
        /* JADX WARNING: Missing block: B:28:0x008a, code:
            r40 = tmsdkobf.nh.a(r30.Dh, true, tmsdkobf.og.k(r44.If).b(), tmsdkobf.og.l(r44.If));
     */
        /* JADX WARNING: Missing block: B:29:0x00a7, code:
            if (r40 == null) goto L_0x030f;
     */
        /* JADX WARNING: Missing block: B:30:0x00a9, code:
            r42 = java.lang.System.currentTimeMillis();
            r22 = r30.Ik;
     */
        /* JADX WARNING: Missing block: B:31:0x00b3, code:
            if (r22 == null) goto L_0x031c;
     */
        /* JADX WARNING: Missing block: B:32:0x00b5, code:
            r22.setState(1);
     */
        /* JADX WARNING: Missing block: B:33:0x00bf, code:
            if (r22.cJ() != false) goto L_0x0330;
     */
        /* JADX WARNING: Missing block: B:34:0x00c1, code:
            r31 = tmsdkobf.og.g(r44.If).a(r30.Dh, r40);
            r22.setState(2);
     */
        /* JADX WARNING: Missing block: B:35:0x00d9, code:
            r34 = java.lang.System.currentTimeMillis() - r42;
     */
        /* JADX WARNING: Missing block: B:36:0x00df, code:
            if (r31 == 0) goto L_0x0336;
     */
        /* JADX WARNING: Missing block: B:37:0x00e1, code:
            if (r31 != 0) goto L_0x0345;
     */
        /* JADX WARNING: Missing block: B:38:0x00e3, code:
            if (r37 != null) goto L_0x036e;
     */
        /* JADX WARNING: Missing block: B:39:0x00e5, code:
            if (r31 != 0) goto L_0x03ad;
     */
        /* JADX WARNING: Missing block: B:40:0x00e7, code:
            tmsdkobf.od.a(r30.Dh, 14, r31, 0);
            r39 = tmsdkobf.og.d(r44.If);
     */
        /* JADX WARNING: Missing block: B:41:0x00fb, code:
            monitor-enter(r39);
     */
        /* JADX WARNING: Missing block: B:44:0x0108, code:
            if (tmsdkobf.og.e(r44.If).isEmpty() == false) goto L_0x0443;
     */
        /* JADX WARNING: Missing block: B:45:0x010a, code:
            monitor-exit(r39);
     */
        /* JADX WARNING: Missing block: B:46:0x010b, code:
            r13 = r39;
     */
        /* JADX WARNING: Missing block: B:51:0x0112, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][time_out]MSG_SEND_TASK, send time out");
            r31 = -17;
     */
        /* JADX WARNING: Missing block: B:53:0x0124, code:
            if (r30.Dh.Fm != false) goto L_0x005b;
     */
        /* JADX WARNING: Missing block: B:54:0x0126, code:
            r38 = 1;
     */
        /* JADX WARNING: Missing block: B:56:0x0133, code:
            if (tmsdkobf.og.f(r44.If) == (byte) 1) goto L_0x018e;
     */
        /* JADX WARNING: Missing block: B:58:0x013e, code:
            if (tmsdkobf.og.f(r44.If) == (byte) 2) goto L_0x01f8;
     */
        /* JADX WARNING: Missing block: B:60:0x0148, code:
            if (tmsdkobf.og.f(r44.If) != (byte) 0) goto L_0x0080;
     */
        /* JADX WARNING: Missing block: B:62:0x0150, code:
            if (r30.Dh.Fl != false) goto L_0x0239;
     */
        /* JADX WARNING: Missing block: B:63:0x0152, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [others] fp not sent, send fp & enqueue this task");
            tmsdkobf.og.a(r44.If, r30.Dh);
            r40 = tmsdkobf.og.d(r44.If);
     */
        /* JADX WARNING: Missing block: B:64:0x016e, code:
            monitor-enter(r40);
     */
        /* JADX WARNING: Missing block: B:66:?, code:
            tmsdkobf.og.h(r44.If).add(r30);
     */
        /* JADX WARNING: Missing block: B:67:0x017c, code:
            monitor-exit(r40);
     */
        /* JADX WARNING: Missing block: B:68:0x017d, code:
            tmsdkobf.og.a(r44.If, 11, "others_depend_on_fp", 0, 0, true);
     */
        /* JADX WARNING: Missing block: B:69:0x018d, code:
            return;
     */
        /* JADX WARNING: Missing block: B:71:0x019a, code:
            if (tmsdkobf.og.g(r44.If).hl() == false) goto L_0x01a7;
     */
        /* JADX WARNING: Missing block: B:72:0x019c, code:
            tmsdkobf.mb.n("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [others] connection is ok");
     */
        /* JADX WARNING: Missing block: B:74:0x01ad, code:
            if (r30.Dh.Fl != false) goto L_0x01eb;
     */
        /* JADX WARNING: Missing block: B:75:0x01af, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [others] connection is broken, wait and resend fp");
            tmsdkobf.og.a(r44.If, r30.Dh);
            r40 = tmsdkobf.og.d(r44.If);
     */
        /* JADX WARNING: Missing block: B:76:0x01cb, code:
            monitor-enter(r40);
     */
        /* JADX WARNING: Missing block: B:78:?, code:
            tmsdkobf.og.h(r44.If).add(r30);
     */
        /* JADX WARNING: Missing block: B:79:0x01d9, code:
            monitor-exit(r40);
     */
        /* JADX WARNING: Missing block: B:80:0x01da, code:
            tmsdkobf.og.a(r44.If, 11, "conn_broken_didnt_monitored", 0, 0, true);
     */
        /* JADX WARNING: Missing block: B:81:0x01ea, code:
            return;
     */
        /* JADX WARNING: Missing block: B:82:0x01eb, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]handleMessage(), [hb] connection is broken, , ignore heartbeat");
     */
        /* JADX WARNING: Missing block: B:83:0x01f4, code:
            return;
     */
        /* JADX WARNING: Missing block: B:89:0x01fe, code:
            if (r30.Dh.Fl != false) goto L_0x022c;
     */
        /* JADX WARNING: Missing block: B:90:0x0200, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [others] sending fp, enqueue this task and wait");
            tmsdkobf.og.b(r44.If, r30.Dh);
            r40 = tmsdkobf.og.d(r44.If);
     */
        /* JADX WARNING: Missing block: B:91:0x021c, code:
            monitor-enter(r40);
     */
        /* JADX WARNING: Missing block: B:93:?, code:
            tmsdkobf.og.h(r44.If).add(r30);
     */
        /* JADX WARNING: Missing block: B:94:0x022a, code:
            monitor-exit(r40);
     */
        /* JADX WARNING: Missing block: B:95:0x022b, code:
            return;
     */
        /* JADX WARNING: Missing block: B:96:0x022c, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]handleMessage(), [hb] sending fp, ignore heartbeat");
     */
        /* JADX WARNING: Missing block: B:97:0x0235, code:
            return;
     */
        /* JADX WARNING: Missing block: B:102:0x0239, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]handleMessage(), [hb] fp not sent, ignore heartbeat");
     */
        /* JADX WARNING: Missing block: B:103:0x0242, code:
            return;
     */
        /* JADX WARNING: Missing block: B:108:0x0246, code:
            tmsdkobf.og.a(r44.If, false);
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [vip] last disconnect not handled, 1: reconnect");
            r31 = tmsdkobf.og.a(r44.If, "disconnected_before_send");
     */
        /* JADX WARNING: Missing block: B:109:0x0268, code:
            if (r30.Dh.Fm != false) goto L_0x0080;
     */
        /* JADX WARNING: Missing block: B:110:0x026a, code:
            tmsdkobf.og.a(r44.If, (byte) 0);
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [vip] last disconnect not handled, 2: not fp, mark fp_not_send");
     */
        /* JADX WARNING: Missing block: B:111:0x027d, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [vip] not connected, 1: connect");
            r31 = tmsdkobf.og.j(r44.If);
     */
        /* JADX WARNING: Missing block: B:112:0x0294, code:
            if (r30.Dh.Fm != false) goto L_0x0080;
     */
        /* JADX WARNING: Missing block: B:113:0x0296, code:
            tmsdkobf.og.a(r44.If, (byte) 0);
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), [vip] not connected, 2: not fp, mark fp_not_send");
     */
        /* JADX WARNING: Missing block: B:114:0x02a9, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]handleMessage(), connect failed: " + r31);
     */
        /* JADX WARNING: Missing block: B:116:0x02d1, code:
            if (r30.Dh.Ft.size() <= 0) goto L_0x008a;
     */
        /* JADX WARNING: Missing block: B:117:0x02d3, code:
            r40 = r30.Dh.Ft.iterator();
     */
        /* JADX WARNING: Missing block: B:119:0x02e1, code:
            if (r40.hasNext() == false) goto L_0x008a;
     */
        /* JADX WARNING: Missing block: B:120:0x02e3, code:
            r8 = (tmsdkobf.bw) r40.next();
     */
        /* JADX WARNING: Missing block: B:121:0x02e9, code:
            if (r8 == null) goto L_0x02dd;
     */
        /* JADX WARNING: Missing block: B:122:0x02eb, code:
            tmsdkobf.nt.ga().a("TmsTcpManager", r8.bz, r8.ey, r8, 11);
     */
        /* JADX WARNING: Missing block: B:123:0x02fd, code:
            if (r8.ez != 0) goto L_0x02dd;
     */
        /* JADX WARNING: Missing block: B:124:0x02ff, code:
            if (r37 == null) goto L_0x0309;
     */
        /* JADX WARNING: Missing block: B:125:0x0301, code:
            r37.bB(r8.bz);
     */
        /* JADX WARNING: Missing block: B:126:0x0309, code:
            r37 = new tmsdkobf.oe();
     */
        /* JADX WARNING: Missing block: B:127:0x030f, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][http_control][shark_v4]handleMessage(), ConverterUtil.createSendBytes() return null!");
            r31 = -1500;
     */
        /* JADX WARNING: Missing block: B:128:0x031c, code:
            r31 = tmsdkobf.og.g(r44.If).a(r30.Dh, r40);
     */
        /* JADX WARNING: Missing block: B:129:0x0330, code:
            r31 = -11;
            r37 = null;
     */
        /* JADX WARNING: Missing block: B:131:0x033a, code:
            if (r30.Dh == null) goto L_0x00e1;
     */
        /* JADX WARNING: Missing block: B:132:0x033c, code:
            r30.Dh.Fw = true;
     */
        /* JADX WARNING: Missing block: B:134:0x0349, code:
            if (r31 == -11) goto L_0x00e3;
     */
        /* JADX WARNING: Missing block: B:135:0x034b, code:
            r31 = r31 - 40000000;
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control]handleMessage(), tcp send failed: " + r31);
     */
        /* JADX WARNING: Missing block: B:136:0x036e, code:
            r37.HE = java.lang.String.valueOf(tmsdkobf.nh.w(tmsdkobf.og.m(r44.If)));
            r37.HG = r34;
            r37.HH = tmsdkobf.og.g(r44.If).hf();
            r37.errorCode = r31;
            r37.g(tmsdkobf.og.l(r44.If));
     */
        /* JADX WARNING: Missing block: B:138:0x03b1, code:
            if (r31 == -11) goto L_0x00e7;
     */
        /* JADX WARNING: Missing block: B:139:0x03b3, code:
            tmsdkobf.od.a(r30.Dh, 14, r31, 0);
            tmsdkobf.og.a(r44.If, r30, r31);
            r13 = new java.util.LinkedList();
            r8 = tmsdkobf.og.d(r44.If);
     */
        /* JADX WARNING: Missing block: B:140:0x03d9, code:
            monitor-enter(r8);
     */
        /* JADX WARNING: Missing block: B:142:?, code:
            r40 = (tmsdkobf.og.f) tmsdkobf.og.e(r44.If).poll();
     */
        /* JADX WARNING: Missing block: B:143:0x03eb, code:
            if (r40 != null) goto L_0x0456;
     */
        /* JADX WARNING: Missing block: B:144:0x03ed, code:
            monitor-exit(r8);
     */
        /* JADX WARNING: Missing block: B:146:0x03f2, code:
            if (r13.size() <= 0) goto L_0x0007;
     */
        /* JADX WARNING: Missing block: B:147:0x03f4, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control]handleMessage(), tcp send fail: " + r31 + ", notify tcp failed for other tasks: " + r13.size());
            r8 = r13.iterator();
     */
        /* JADX WARNING: Missing block: B:149:0x0427, code:
            if (r8.hasNext() == false) goto L_0x0007;
     */
        /* JADX WARNING: Missing block: B:150:0x0429, code:
            r21 = (tmsdkobf.og.f) r8.next();
            r22 = r21.Ik;
     */
        /* JADX WARNING: Missing block: B:151:0x0435, code:
            if (r22 != null) goto L_0x0460;
     */
        /* JADX WARNING: Missing block: B:152:0x0437, code:
            tmsdkobf.og.a(r44.If, r21, r31);
     */
        /* JADX WARNING: Missing block: B:154:?, code:
            tmsdkobf.og.a(r44.If, 0, null, 0, 0, true);
     */
        /* JADX WARNING: Missing block: B:159:?, code:
            r13.add(r40);
     */
        /* JADX WARNING: Missing block: B:163:0x0460, code:
            r22.setState(2);
     */
        /* JADX WARNING: Missing block: B:183:0x0613, code:
            return;
     */
        /* JADX WARNING: Missing block: B:188:0x063f, code:
            tmsdkobf.mb.s("TmsTcpManager", "[tcp_control][f_p]fp too frequency, waiting tasks count: " + r30.size());
            r33 = r30.iterator();
     */
        /* JADX WARNING: Missing block: B:190:0x0668, code:
            if (r33.hasNext() == false) goto L_0x0007;
     */
        /* JADX WARNING: Missing block: B:191:0x066a, code:
            r36 = (tmsdkobf.og.f) r33.next();
     */
        /* JADX WARNING: Missing block: B:192:0x0670, code:
            if (r36 == null) goto L_0x0664;
     */
        /* JADX WARNING: Missing block: B:194:0x0676, code:
            if (r36.Dh == null) goto L_0x0664;
     */
        /* JADX WARNING: Missing block: B:196:0x067e, code:
            if (r36.Dh.Ft == null) goto L_0x0664;
     */
        /* JADX WARNING: Missing block: B:198:0x068a, code:
            if (r36.Dh.Ft.size() <= 0) goto L_0x0664;
     */
        /* JADX WARNING: Missing block: B:199:0x068c, code:
            r37 = r36.Dh.Ft.iterator();
     */
        /* JADX WARNING: Missing block: B:201:0x069a, code:
            if (r37.hasNext() != false) goto L_0x06ac;
     */
        /* JADX WARNING: Missing block: B:202:0x069c, code:
            tmsdkobf.og.a(r44.If, r36, -40001300);
     */
        /* JADX WARNING: Missing block: B:207:0x06ac, code:
            r13 = (tmsdkobf.bw) r37.next();
     */
        /* JADX WARNING: Missing block: B:208:0x06b2, code:
            if (r13 == null) goto L_0x0696;
     */
        /* JADX WARNING: Missing block: B:209:0x06b4, code:
            tmsdkobf.nt.ga().a("TmsTcpManager", r13.bz, r13.ey, r13, 10, -40001300, null);
     */
        /* JADX WARNING: Missing block: B:216:0x06e8, code:
            return;
     */
        /* JADX WARNING: Missing block: B:221:0x0714, code:
            tmsdkobf.nv.b("TmsTcpManager", "fp failed, waiting tasks count: " + r30.size(), null, null);
            r31 = r45.arg1;
            r33 = r30.iterator();
     */
        /* JADX WARNING: Missing block: B:223:0x0742, code:
            if (r33.hasNext() == false) goto L_0x0007;
     */
        /* JADX WARNING: Missing block: B:224:0x0744, code:
            r36 = (tmsdkobf.og.f) r33.next();
     */
        /* JADX WARNING: Missing block: B:225:0x074a, code:
            if (r36 == null) goto L_0x073e;
     */
        /* JADX WARNING: Missing block: B:227:0x0750, code:
            if (r36.Dh == null) goto L_0x073e;
     */
        /* JADX WARNING: Missing block: B:229:0x0758, code:
            if (r36.Dh.Ft == null) goto L_0x073e;
     */
        /* JADX WARNING: Missing block: B:231:0x0764, code:
            if (r36.Dh.Ft.size() <= 0) goto L_0x073e;
     */
        /* JADX WARNING: Missing block: B:232:0x0766, code:
            r37 = r36.Dh.Ft.iterator();
     */
        /* JADX WARNING: Missing block: B:234:0x0774, code:
            if (r37.hasNext() != false) goto L_0x0788;
     */
        /* JADX WARNING: Missing block: B:235:0x0776, code:
            tmsdkobf.og.a(r44.If, r36, -700000000 + r31);
     */
        /* JADX WARNING: Missing block: B:240:0x0788, code:
            r13 = (tmsdkobf.bw) r37.next();
     */
        /* JADX WARNING: Missing block: B:241:0x078e, code:
            if (r13 == null) goto L_0x0770;
     */
        /* JADX WARNING: Missing block: B:242:0x0790, code:
            tmsdkobf.nt.ga().a("TmsTcpManager", r13.bz, r13.ey, r13, 9, -700000000 + r31, null);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message message) {
            LinkedList linkedList;
            int i;
            String str;
            switch (message.what) {
                case 0:
                    mb.n("TmsTcpManager", "[tcp_control]handle MSG_SEND_TASK");
                    synchronized (og.this.Ba) {
                        f fVar = (f) og.this.Ie.poll();
                        if (fVar == null || fVar.Dh == null) {
                            mb.s("TmsTcpManager", "[tcp_control]no task for send");
                            return;
                        }
                    }
                    break;
                case 2:
                    linkedList = null;
                    synchronized (og.this.Ba) {
                        if (og.this.Ia != null) {
                            if (og.this.Ia.size() > 0) {
                                linkedList = (LinkedList) og.this.Ia.clone();
                                og.this.Ia.clear();
                            }
                        }
                    }
                    if (linkedList != null && linkedList.size() > 0) {
                        mb.n("TmsTcpManager", "[tcp_control]fp success. send waiting for fp tasks: " + linkedList.size());
                        synchronized (og.this.Ba) {
                            Iterator it = linkedList.iterator();
                            while (it.hasNext()) {
                                f fVar2 = (f) it.next();
                                if (fVar2 != null) {
                                    og.this.Ie.add(fVar2);
                                }
                            }
                        }
                        og.this.a(0, null, 0, 0, true);
                    } else {
                        mb.n("TmsTcpManager", "[tcp_control]fp success, no task waiting for fp");
                    }
                    mb.n("TmsTcpManager", "[tcp_control][h_b]restartHeartBeat after fp success");
                    og.this.gV();
                    break;
                case 3:
                    og.this.bD(3);
                    synchronized (og.this.Ba) {
                        if (og.this.Ia != null && og.this.Ia.size() > 0) {
                            linkedList = (LinkedList) og.this.Ia.clone();
                            og.this.Ia.clear();
                            break;
                        }
                    }
                    break;
                case 4:
                    og.this.gT();
                    break;
                case 9:
                    mb.d("TmsTcpManager", "[tcp_control][f_p] handle: MSG_ON_CHANGE_TO_CONNECTED");
                    if (og.this.HU.gG() > 0) {
                        if (og.this.HW < 3) {
                            mb.d("TmsTcpManager", "[tcp_control][f_p] handle connected msg, ref count: " + og.this.HU.gG() + ", wait for network become stable and send fp in: " + og.this.HU.az().F + "s");
                            og.this.HW = og.this.HW + 1;
                            og.this.a(11, "delay_fp_network_connected", 0, 1000 * ((long) og.this.HU.az().F), true);
                            break;
                        }
                        mb.s("TmsTcpManager", "[tcp_control][f_p] handle connected msg, ref count: " + og.this.HU.gG() + ", mReconnectTimes over limit: " + og.this.HW);
                        break;
                    }
                    mb.d("TmsTcpManager", "[tcp_control][f_p] handle connected msg: ref connt <= 0, no need to reconnect");
                    return;
                case 11:
                    i = message.arg1;
                    str = "" + message.obj;
                    mb.n("TmsTcpManager", "[tcp_control] handle msg: MSG_DELAY_SEND_FIRST_PKG, retryTimes: " + i + " reason: " + str);
                    og.this.l(i, str);
                    break;
                case 12:
                    synchronized (og.this.Ba) {
                        if (og.this.Ia != null && og.this.Ia.size() > 0) {
                            linkedList = (LinkedList) og.this.Ia.clone();
                            og.this.Ia.clear();
                            break;
                        }
                    }
                    break;
                case 13:
                    if (og.this.Ib == (byte) 1) {
                        i = message.arg1;
                        str = "" + message.obj;
                        mb.n("TmsTcpManager", "[tcp_control] handle msg: MSG_SEND_HB, retryTimes: " + i + " reason: " + str);
                        og.this.j(i, str);
                        break;
                    }
                    mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]handle msg: MSG_SEND_HB, fp not sent, donnot send hb!");
                    break;
            }
        }
    }

    private class f {
        public tmsdkobf.nw.f Dh = null;
        public kd Ik = null;
        public int eE = 0;

        public f(int i, kd kdVar, tmsdkobf.nw.f fVar) {
            this.eE = i;
            this.Ik = kdVar;
            this.Dh = fVar;
        }
    }

    public og(nl nlVar, om omVar, tmsdkobf.od.a aVar, d dVar, nm nmVar, tmsdkobf.nw.d dVar2, nw nwVar) {
        this.mContext = null;
        this.HQ = null;
        this.CX = false;
        this.HW = 0;
        this.HX = 15000;
        this.HY = 0;
        this.HZ = false;
        this.Ia = new LinkedList();
        this.Ba = new Object();
        this.Ib = (byte) 0;
        this.te = null;
        this.Ic = null;
        this.Id = false;
        this.Ie = new op(new Comparator<f>() {
            /* renamed from: a */
            public int compare(f fVar, f fVar2) {
                return kc.am(fVar2.eE) - kc.am(fVar.eE);
            }
        });
        this.mContext = TMSDKContext.getApplicaionContext();
        this.HQ = new or(this.mContext, this, omVar);
        O(omVar.ax());
        this.te = ((ki) fj.D(4)).newFreeHandlerThread("sendHandlerThread");
        this.te.start();
        this.Ic = new e(this.te.getLooper());
        this.CT = nlVar;
        this.EW = aVar;
        this.HR = nmVar;
        this.HT = dVar;
        this.Dm = nwVar;
        this.HV = new ol(this.mContext, this, this);
        this.HU = new oc(nlVar, this);
        b(dVar2);
        nx.gs().a((tmsdkobf.nx.a) this);
    }

    private void A(long j) {
        mb.n("TmsTcpManager", "[tcp_control] checkKeepAliveAndResetHeartBeat()");
        gY();
        this.HU.z(j);
    }

    private void O(boolean z) {
        this.CX = z;
        if (z) {
            this.HX = 15000;
        }
    }

    private oh<Long, Integer, JceStruct> a(long j, h hVar) {
        if (hVar != null) {
            this.HU.c(hVar);
            d dVar = new d();
            dVar.hash = hVar.hash;
            dVar.j = hVar.j;
            return new oh(Long.valueOf(j), Integer.valueOf(1101), dVar);
        }
        mb.s("TmsTcpManager", "[shark_push][shark_conf]handleSharkConfPush(), scSharkConf == null");
        return null;
    }

    private final void a(int i, Object obj, int i2, long j, boolean z) {
        if (this.Ic != null) {
            if (z) {
                this.Ic.removeMessages(i);
            }
            this.Ic.sendMessageDelayed(Message.obtain(this.Ic, i, i2, 0, obj), j);
        }
    }

    private void a(f fVar, int i) {
        if (fVar != null) {
            mb.n("TmsTcpManager", "[send_control] tcp fail, notify up level: " + i);
            if (this.HR != null) {
                this.HR.a(fVar.Dh, i);
            }
        }
    }

    private void b(tmsdkobf.nw.d dVar) {
        this.HS = new ka() {
            public oh<Long, Integer, JceStruct> a(int i, long j, int i2, JceStruct jceStruct) {
                if (jceStruct != null) {
                    switch (i2) {
                        case 11101:
                            return og.this.a(j, (h) jceStruct);
                        default:
                            return null;
                    }
                }
                mb.s("TmsTcpManager", "[shark_push][shark_conf]onRecvPush() null == push");
                return null;
            }
        };
        mb.n("TmsTcpManager", "[shark_push][shark_conf]registerSharkPush()");
        dVar.a(0, 11101, new h(), 0, this.HS, false);
    }

    private final void bD(int i) {
        if (this.Ic != null) {
            this.Ic.removeMessages(i);
        }
    }

    private int cl(String str) {
        mb.n("TmsTcpManager", "[tcp_control]reconnect(), reason: " + str);
        int hs = this.HQ.hs();
        mb.n("TmsTcpManager", "[tcp_control]reconnect(), ret: " + hs);
        return hs;
    }

    private void g(tmsdkobf.nw.f fVar) {
        if (fVar != null && fVar.Ft != null && fVar.Ft.size() > 0) {
            Iterator it = fVar.Ft.iterator();
            while (it.hasNext()) {
                bw bwVar = (bw) it.next();
                if (bwVar != null) {
                    nt.ga().a("TmsTcpManager", bwVar.bz, bwVar.ey, bwVar, 6);
                }
            }
        }
    }

    private int gS() {
        mb.n("TmsTcpManager", "[tcp_control]connectIfNeed()");
        int i = 0;
        if (this.HQ.hl()) {
            mb.n("TmsTcpManager", "[tcp_control]connectIfNeed(), already connected");
        } else {
            i = this.HQ.hm() ? this.HQ.hr() : -220000;
        }
        mb.n("TmsTcpManager", "[tcp_control]connectIfNeed(), ret: " + i);
        return i;
    }

    private void gT() {
        mb.n("TmsTcpManager", "[tcp_control]tryCloseConnectionSync()");
        if (this.HU.gG() <= 0) {
            this.HU.gH();
            mb.n("TmsTcpManager", "[tcp_control]tryCloseConnectionSync(), update: fp not send");
            this.Ib = (byte) 0;
            this.HW = 0;
            gX();
            synchronized (this.Ba) {
                this.Ie.clear();
            }
            long currentTimeMillis = System.currentTimeMillis();
            int hq = this.HQ.hq();
            long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
            om gQ = this.HQ.gQ();
            if (!(gQ == null || gQ.B(true) == null)) {
                oe oeVar = new oe();
                tmsdkobf.on.b B = gQ.B(true);
                oeVar.HB = B.hd();
                oeVar.HC = String.valueOf(B.getPort());
                oeVar.HE = String.valueOf(nh.w(this.mContext));
                oeVar.HH = this.HQ.hf();
                oeVar.errorCode = hq;
                oeVar.HG = currentTimeMillis2;
                oeVar.e(this.CT);
            }
            return;
        }
        mb.d("TmsTcpManager", "[tcp_control]tryCloseConnectionSync(), not allow, ref connt: " + this.HU.gG());
    }

    private synchronized void gV() {
        gX();
        gW();
    }

    private synchronized void gW() {
        mb.n("TmsTcpManager", "[h_b]startHeartBeat");
        if (this.HV != null) {
            this.HV.start();
        }
    }

    private synchronized void gX() {
        mb.n("TmsTcpManager", "[h_b]stopHeartBeat");
        if (this.HV != null) {
            this.HV.stop();
        }
    }

    private synchronized void gY() {
        mb.n("TmsTcpManager", "[h_b]resetHeartBeat");
        if (this.HV != null) {
            this.HV.reset();
        }
    }

    private void h(tmsdkobf.nw.f fVar) {
        if (fVar != null && fVar.Ft != null && fVar.Ft.size() > 0) {
            Iterator it = fVar.Ft.iterator();
            while (it.hasNext()) {
                bw bwVar = (bw) it.next();
                if (bwVar != null) {
                    nt.ga().a("TmsTcpManager", bwVar.bz, bwVar.ey, bwVar, 8);
                }
            }
        }
    }

    private void i(tmsdkobf.nw.f fVar) {
        if (fVar != null && fVar.Ft != null && fVar.Ft.size() > 0) {
            Iterator it = fVar.Ft.iterator();
            while (it.hasNext()) {
                bw bwVar = (bw) it.next();
                if (bwVar != null) {
                    nt.ga().a("TmsTcpManager", bwVar.bz, bwVar.ey, bwVar, 7);
                }
            }
        }
    }

    private void j(int i, String str) {
        mb.n("TmsTcpManager", "[tcp_control][h_b][shark_conf]sendHeartBeat(), retryTimes: " + i + " reason: " + str);
        ArrayList arrayList = new ArrayList();
        bw bwVar = new bw();
        bwVar.bz = 999;
        bwVar.ey = ns.fW().fP();
        arrayList.add(bwVar);
        byte fZ = ns.fY().fZ();
        long j = i >= 1 ? 60 : 30;
        tmsdkobf.nw.f fVar = new tmsdkobf.nw.f(IncomingSmsFilterConsts.PAY_SMS, false, true, false, 0, arrayList, new b(bwVar.ey, i, str, fZ), 1000 * j);
        fVar.Fx = (byte) fZ;
        nt.ga().a(bwVar.ey, 1000 * j, str);
        nt.ga().a(fZ, fVar.Fq);
        this.HT.a(fVar);
    }

    private void l(int i, String str) {
        if (this.Ib == (byte) 1 || this.Ib == (byte) 2) {
            mb.n("TmsTcpManager", "[tcp_control][f_p]sending or received fp, no more send, is received ? " + (this.Ib == (byte) 1));
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if ((Math.abs(currentTimeMillis - this.HY) >= this.HX ? 1 : null) == null) {
            mb.s("TmsTcpManager", "[tcp_control][f_p]first pkg too frequency, send delay");
            a(12, null, 0, 0, true);
            a(11, "delay_too_freq:" + str, i, this.HX, true);
            return;
        }
        int i2 = this.HU.az().F;
        if (np.fS().x(((long) i2) * 1000)) {
            mb.s("TmsTcpManager", "[tcp_control][f_p]net state changing, send fp delay(s): " + i2);
            a(11, "delay_waitfor_stable:" + str, i, ((long) i2) * 1000, true);
            return;
        }
        this.HY = currentTimeMillis;
        this.Ib = (byte) 2;
        bD(11);
        mb.s("TmsTcpManager", "[tcp_control][f_p]send first pkg, reason: " + str + " retryTimes: " + i);
        bw bwVar = new bw();
        bwVar.bz = 997;
        bwVar.ey = ns.fW().fP();
        bwVar.data = nh.a(this.mContext, null, bwVar.bz, bwVar);
        ArrayList arrayList = new ArrayList();
        arrayList.add(bwVar);
        nt.ga().a(bwVar.ey, -1, str);
        this.HT.a(new tmsdkobf.nw.f(IncomingSmsFilterConsts.PAY_SMS, false, false, true, 0, arrayList, new a(bwVar.ey, i, str), 0));
    }

    public void a(int i, Object obj) {
        mb.d("TmsTcpManager", "[tcp_control]onTcpError(), errCode: " + i + " msg: " + obj);
        switch (i) {
            case 10:
            case 11:
            case 12:
                this.Id = true;
                this.Ib = (byte) 0;
                if (this.HU.gG() <= 0) {
                    return;
                }
                if (this.HW >= 3) {
                    mb.s("TmsTcpManager", "[tcp_control][f_p]tcp_connect_broken, ref count: " + this.HU.gG() + ", mReconnectTimes over limit: " + this.HW);
                    return;
                }
                mb.s("TmsTcpManager", "[tcp_control][f_p]tcp_connect_broken, ref count: " + this.HU.gG() + ", delay send fp in " + this.HU.az().E + "s");
                this.HW++;
                a(11, "delay_fp_for_connect_broken" + i, 0, 1000 * ((long) this.HU.az().E), true);
                return;
            default:
                return;
        }
    }

    public void bE(int i) {
        mb.d("TmsTcpManager", "[tcp_control]onTcpEvent(), eventCode: " + i);
    }

    public void d(int i, byte[] bArr) {
        mb.d("TmsTcpManager", "[tcp_control]onReceiveData()");
        this.HW = 0;
        if (!((!this.CX && !qg.bV(65539)) || bArr == null || nu.t(bArr))) {
            nv.a("TmsTcpManager", bArr);
        }
        this.EW.a(true, 0, bArr, null);
    }

    void e(tmsdkobf.nw.f fVar) {
        mb.n("TmsTcpManager", "[tcp_control] sendCheckFirst()");
        if (!this.HQ.hm()) {
            mb.s("TmsTcpManager", "[tcp_control] sendCheckFirst(), no connect");
            this.EW.b(true, -40220000, fVar);
        } else if (lw.eJ()) {
            mb.s("TmsTcpManager", "[tcp_control] sendCheckFirst(), cmd could not connect");
            this.EW.b(true, -40230000, fVar);
        } else {
            f fVar2 = new f(32, null, fVar);
            if (this.Ib == (byte) 1) {
                f(fVar);
            } else if (this.Ib != (byte) 2) {
                if (this.Ib == (byte) 0) {
                    if (fVar.Fl) {
                        mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]sendCheckFirst(),fp is not sent ignore heartbeat");
                        return;
                    }
                    mb.n("TmsTcpManager", "[tcp_control] fp is not sent, send fp & enqueue this task");
                    h(fVar);
                    synchronized (this.Ba) {
                        this.Ia.add(fVar2);
                    }
                    a(11, "delay_send_for_others", 0, 0, true);
                }
            } else if (fVar.Fl) {
                mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]sendCheckFirst(),sending fp ignore heartbeat");
            } else {
                mb.n("TmsTcpManager", "[tcp_control] sending fp, enqueue this task");
                i(fVar);
                synchronized (this.Ba) {
                    this.Ia.add(fVar2);
                }
            }
        }
    }

    void f(tmsdkobf.nw.f fVar) {
        mb.n("TmsTcpManager", "[tcp_control] send(), isFP: " + fVar.Fm + ", isHB: " + fVar.Fl);
        if (!this.HQ.hm()) {
            mb.s("TmsTcpManager", "[tcp_control] send(), no connect");
            this.EW.b(true, -40220000, fVar);
        } else if (lw.eJ()) {
            mb.s("TmsTcpManager", "[tcp_control] send(), cmd could not connect");
            this.EW.b(true, -40230000, fVar);
        } else {
            bD(4);
            g(fVar);
            if (!fVar.Fl) {
                A(fVar.Fs);
            }
            f fVar2 = new f(32, null, fVar);
            synchronized (this.Ba) {
                this.Ie.add(fVar2);
            }
            a(0, null, 0, 0, true);
        }
    }

    synchronized void gA() {
        mb.n("TmsTcpManager", "get couldNotConnect cmd");
        if (lw.eJ()) {
            mb.n("TmsTcpManager", "could not connect");
            this.HU.gI();
        }
    }

    public void gC() {
        this.HU.gC();
    }

    public void gD() {
        this.HU.gD();
    }

    public void gP() {
        k(0, "tcp_control");
    }

    boolean gU() {
        long abs;
        if (this.Ib == (byte) 1) {
            mb.n("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), fp succ, prefer tcp");
            return true;
        } else if (this.Ib != (byte) 2) {
            if (this.HY > 0) {
                abs = Math.abs(System.currentTimeMillis() - this.HY);
                if (!(abs <= 1800000)) {
                    mb.n("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), over 30 mins since last fp, try again, prefer tcp: " + abs);
                    return true;
                } else if (this.HZ) {
                    mb.s("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), fp failed within 30 mins, network not reconnected, prefer http: " + abs);
                    return false;
                } else {
                    mb.n("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), no fp fail record or network reconnected within 30 mins, prefer tcp: " + abs);
                    return true;
                }
            }
            mb.n("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), fp first time, prefer tcp");
            return true;
        } else {
            abs = Math.abs(System.currentTimeMillis() - this.HY);
            if (!(this.HY <= 0)) {
                if (!(abs >= 10000)) {
                    mb.n("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), fp sending within 10s, prefer tcp: " + abs);
                    return true;
                }
            }
            mb.s("TmsTcpManager", "[tcp_control]guessTcpWillSucc(), fp sending over 10s, prefer http: " + abs);
            return false;
        }
    }

    public void gZ() {
        if (this.Ib != (byte) 1) {
            mb.s("TmsTcpManager", "[tcp_control][f_p][h_b]onHeartBeat(), fp not sent, donnot send hb!");
            return;
        }
        a(13, "onHeartBeat", 0, 0, true);
    }

    public int ha() {
        return this.HU.az().interval;
    }

    void k(int i, String str) {
        a(11, "" + str, i, 0, true);
    }

    public void onClose() {
        a(4, null, 0, 0, true);
    }

    public void onConnected() {
        this.HZ = false;
        int gG = this.HU.gG();
        if (gG > 0) {
            mb.n("TmsTcpManager", "[tcp_control]onConnected(), with tcp ref, send MSG_ON_CHANGE_TO_CONNECTED, refCount: " + gG);
            a(9, null, 0, 0, true);
            return;
        }
        mb.n("TmsTcpManager", "[tcp_control]onConnected(), no tcp ref, ignore, refCount: " + gG);
    }

    public void onDisconnected() {
        String str = "TmsTcpManager";
        mb.n(str, "[tcp_control]onDisconnected(), update: disconnected & fp not send, refCount: " + this.HU.gG());
        this.Id = true;
        this.Ib = (byte) 0;
        bD(9);
    }
}
