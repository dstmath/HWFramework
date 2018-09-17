package com.huawei.android.pushagent.model.d;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.http.server.TrsRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr$RECONNECTEVENT;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.f;
import com.huawei.android.pushagent.utils.threadpool.a;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Date;

public class c {
    private static final byte[] cm = new byte[0];
    private static c cn = null;
    private Context appCtx;
    private boolean co = false;
    private g cp = null;

    public static c jz(Context context) {
        c cVar;
        synchronized (cm) {
            if (cn == null) {
                cn = new c(context);
            }
            cVar = cn;
        }
        return cVar;
    }

    private c(Context context) {
        this.appCtx = context.getApplicationContext();
        this.cp = g.aq(context);
    }

    public InetSocketAddress ke(boolean z) {
        boolean ka = ka(z);
        if (!this.cp.isValid() || ka) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "in getPushSrvAddr, have no invalid addr");
            return null;
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "return valid PushSrvAddr");
        return new InetSocketAddress(this.cp.getServerIP(), this.cp.getServerPort());
    }

    public void kg() {
        g.aq(this.appCtx).bl(0);
        this.co = true;
    }

    /* JADX WARNING: Missing block: B:20:0x0073, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean ka(boolean z) {
        if (-1 == b.tm(this.appCtx)) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "in queryTRSInfo no network");
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis() - i.ea(this.appCtx).ec();
        if (this.cp.isNotAllowedPush() && currentTimeMillis > 0 && currentTimeMillis < this.cp.getNextConnectTrsInterval()) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "Not allow to use push in this area, and not pass interval,result code: " + this.cp.getResult());
            return false;
        } else if (!kd()) {
            return false;
        } else {
            if ((!z && (!kb() || (kc() ^ 1) != 0)) || !com.huawei.android.pushagent.model.flowcontrol.c.mj(this.appCtx)) {
                return false;
            }
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "need Connect TRS");
            return kf();
        }
    }

    private synchronized boolean kf() {
        a.oq(new g(this));
        i.ea(this.appCtx).ed(System.currentTimeMillis());
        i.ea(this.appCtx).ef(i.ea(this.appCtx).ee() + 1);
        return true;
    }

    private boolean kh(TrsRsp trsRsp) {
        if (trsRsp == null || (trsRsp.isValid() ^ 1) != 0) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "in PushSrvInfo:trsConfig, trsConfig is null or invalid");
            return false;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "queryTrs success!");
        if (!(this.cp.getWifiMinHeartbeat() == trsRsp.getWifiMinHeartbeat() && this.cp.getWifiMaxHeartbeat() == trsRsp.getWifiMaxHeartbeat() && this.cp.get3GMinHeartbeat() == trsRsp.get3GMinHeartbeat() && this.cp.get3GMaxHeartbeat() == trsRsp.get3GMaxHeartbeat())) {
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "heart beat range change.");
            PushService.yx(new Intent("com.huawei.android.push.intent.HEARTBEAT_RANGE_CHANGE"));
        }
        ki(trsRsp.getAnalyticUrl());
        trsRsp.encryptRsaPubKey();
        trsRsp.encryptConnectionId();
        trsRsp.encryptDeviceId();
        Object deviceId = trsRsp.getDeviceId();
        trsRsp.removeDeviceId();
        if (!TextUtils.isEmpty(deviceId)) {
            h.dp(this.appCtx).ds(deviceId);
        }
        this.cp.u(trsRsp.getAll());
        this.cp.bl((this.cp.bo() * 1000) + System.currentTimeMillis());
        i.ea(this.appCtx).ef(0);
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "write the lastQueryTRSsucc_time to the pushConfig.xml file ");
        i.ea(this.appCtx).eg(System.currentTimeMillis());
        this.co = false;
        com.huawei.android.pushagent.model.flowcontrol.a.lk(this.appCtx).lm(this.appCtx, ReconnectMgr$RECONNECTEVENT.TRS_QUERIED, new Bundle());
        PushService.yx(new Intent("com.huawei.android.push.intent.TRS_QUERY_SUCCESS"));
        return true;
    }

    private void ki(String str) {
        if (TextUtils.isEmpty(str)) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "analytic url is empty");
            return;
        }
        String analyticUrl = this.cp.getAnalyticUrl();
        long eh = i.ea(this.appCtx).eh();
        long currentTimeMillis = System.currentTimeMillis();
        if (!str.equals(analyticUrl) || currentTimeMillis - eh >= this.cp.bp()) {
            i.ea(this.appCtx).ei(currentTimeMillis);
            Intent intent = new Intent("com.huawei.android.push.intent.NC_CONTROL_INFO");
            intent.putExtra("control_type", 1);
            intent.setPackage("com.huawei.android.pushagent");
            intent.putExtra("report_url", str);
            try {
                intent.putExtra("device_id", e.nw(b.ua(this.appCtx), b.ug().getBytes("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "fail to encrypt deviceId");
            }
            b.tr(this.appCtx, intent, f.vb());
            return;
        }
        com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "analytic url not changed and not pass interval");
    }

    private boolean kd() {
        long bq = this.cp.bq();
        long ej = i.ea(this.appCtx).ej();
        long ec = i.ea(this.appCtx).ec();
        if (this.cp.isValid() && (this.co ^ 1) != 0 && bq >= System.currentTimeMillis() && System.currentTimeMillis() > ej) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "config still valid, need not query TRS");
            return false;
        } else if (!this.cp.isValid() || !this.co || System.currentTimeMillis() - ec >= this.cp.br() * 1000 || System.currentTimeMillis() <= ec) {
            return true;
        } else {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", " cannot query TRS in trsValid_min");
            return false;
        }
    }

    private boolean kb() {
        long ej = i.ea(this.appCtx).ej();
        if (System.currentTimeMillis() - ej >= this.cp.br() * 1000 || System.currentTimeMillis() <= ej) {
            return true;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "can not contect TRS Service when  the connect more than " + this.cp.br() + " sec last contected success time," + "lastQueryTRSsucc_time = " + new Date(ej));
        return false;
    }

    private boolean kc() {
        long ec = i.ea(this.appCtx).ec();
        long bs = this.cp.bs() * 1000;
        if (i.ea(this.appCtx).ee() > this.cp.bt()) {
            bs = this.cp.bu() * 1000;
        }
        if (System.currentTimeMillis() - ec >= bs || System.currentTimeMillis() <= ec) {
            return true;
        }
        com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "can't connect TRS, not exceed retry interval, " + (bs / 1000) + "sec than  last contectting time,lastQueryTRSTime =" + new Date(ec));
        return false;
    }
}
