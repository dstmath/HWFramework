package com.huawei.android.pushagent.model.flowcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.exception.PushException.ErrorType;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.utils.d.c;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class a {
    private static long df = 300000;
    private static long dg = 600000;
    private static long dh = 1800000;
    private static int di = 4;
    private static String dk = "00:00";
    private static String dl = "06:00";
    private static long dm = 300000;
    private static long dn = 600000;
    private static a do = null;
    private static final /* synthetic */ int[] dp = null;
    private ArrayList<b> de = new ArrayList();
    private int dj = 0;

    private static /* synthetic */ int[] ma() {
        if (dp != null) {
            return dp;
        }
        int[] iArr = new int[ReconnectMgr$RECONNECTEVENT.values().length];
        try {
            iArr[ReconnectMgr$RECONNECTEVENT.NETWORK_CHANGE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ReconnectMgr$RECONNECTEVENT.SOCKET_CLOSE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ReconnectMgr$RECONNECTEVENT.SOCKET_CONNECTED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ReconnectMgr$RECONNECTEVENT.SOCKET_REG_SUCCESS.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ReconnectMgr$RECONNECTEVENT.TRS_QUERIED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        dp = iArr;
        return iArr;
    }

    private a() {
    }

    public static synchronized a lk(Context context) {
        a aVar;
        synchronized (a.class) {
            if (do == null) {
                do = new a();
            }
            if (do.de.isEmpty()) {
                do.lv(context);
            }
            aVar = do;
        }
        return aVar;
    }

    private void lv(Context context) {
        int i;
        int size;
        lw(context);
        String rt = new com.huawei.android.pushagent.utils.d.a(context, "PushConnectControl").rt("connectPushSvrInfos");
        if (!TextUtils.isEmpty(rt)) {
            c.sg("PushLog2951", "connectPushSvrInfos is " + rt);
            for (String str : rt.split("\\|")) {
                b bVar = new b();
                if (bVar.me(str)) {
                    this.de.add(bVar);
                }
            }
        }
        Collections.sort(this.de);
        if (this.de.size() > di) {
            Collection arrayList = new ArrayList();
            size = this.de.size() - di;
            for (i = 0; i < size; i++) {
                arrayList.add((b) this.de.get(i));
            }
            this.de.removeAll(arrayList);
        }
    }

    private void lx(Context context, boolean z) {
        c.sg("PushLog2951", "save connection info " + z);
        long currentTimeMillis = System.currentTimeMillis();
        long j = lu() ? dh : dg;
        Collection arrayList = new ArrayList();
        for (b bVar : this.de) {
            if (currentTimeMillis < bVar.mc() || currentTimeMillis - bVar.mc() > j) {
                arrayList.add(bVar);
            }
        }
        if (!arrayList.isEmpty()) {
            c.sg("PushLog2951", "some connection info is expired:" + arrayList.size());
            this.de.removeAll(arrayList);
        }
        b bVar2 = new b();
        bVar2.mf(z);
        bVar2.mg(System.currentTimeMillis());
        if (this.de.size() < di) {
            this.de.add(bVar2);
        } else {
            this.de.remove(0);
            this.de.add(bVar2);
        }
        String str = "|";
        StringBuffer stringBuffer = new StringBuffer();
        for (b bVar22 : this.de) {
            stringBuffer.append(bVar22.toString());
            stringBuffer.append(str);
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        new com.huawei.android.pushagent.utils.d.a(context, "PushConnectControl").rv("connectPushSvrInfos", stringBuffer.toString());
    }

    private void lz(Context context) {
        if (!ls(context)) {
            c.sg("PushLog2951", "It is not bad network mode, do nothing");
        } else if (this.de.isEmpty()) {
            ly(context, false);
        } else {
            b bVar = (b) this.de.get(this.de.size() - 1);
            if (bVar.md()) {
                c.sg("PushLog2951", "last connection is success");
                long currentTimeMillis = System.currentTimeMillis();
                long mc = bVar.mc();
                if (currentTimeMillis - mc > df || currentTimeMillis < mc) {
                    c.sg("PushLog2951", df + " has passed since last connect");
                    ly(context, false);
                } else {
                    c.sg("PushLog2951", "connection keep too short , still in bad network mode");
                }
            } else {
                c.sg("PushLog2951", "last connection result is false , still in bad network mode");
            }
        }
    }

    public long lq(Context context) {
        long lp = lp(context);
        long lo = lo(context);
        if (lp > 0 && lp >= lo) {
            com.huawei.android.pushagent.a.a.xx(44, String.valueOf(lp));
        } else if (lo > 0 && lp < lo) {
            com.huawei.android.pushagent.a.a.xx(45, String.valueOf(lo));
        }
        return Math.max(lp, lo);
    }

    private long lp(Context context) {
        if (this.de.isEmpty()) {
            c.sg("PushLog2951", "first connection, return 0");
            return 0;
        }
        long bw;
        switch (this.dj) {
            case 0:
                bw = g.aq(context).bw() * 1000;
                break;
            case 1:
                bw = g.aq(context).bx() * 1000;
                break;
            case 2:
                bw = g.aq(context).by() * 1000;
                break;
            case 3:
                bw = g.aq(context).bz() * 1000;
                break;
            case 4:
                bw = g.aq(context).ca() * 1000;
                break;
            case 5:
                bw = g.aq(context).cb() * 1000;
                break;
            case 6:
                bw = g.aq(context).cc() * 1000;
                break;
            default:
                bw = g.aq(context).cd() * 1000;
                break;
        }
        if (((long) this.dj) == g.aq(context).ce()) {
            com.huawei.android.pushagent.model.d.c.jz(context).kg();
            c.sh("PushLog2951", "reconnect pushserver failed " + this.dj + " times, set force query TRS at next connect.");
        }
        long currentTimeMillis = System.currentTimeMillis();
        long mh = ((b) this.de.get(this.de.size() - 1)).dx;
        if (currentTimeMillis < mh) {
            c.sg("PushLog2951", "now is less than last connect time");
            mh = 0;
        } else {
            mh = Math.max((mh + bw) - currentTimeMillis, 0);
        }
        c.sh("PushLog2951", "reconnect pushserver failed, the next reconnect time is:" + this.dj + " after " + mh + " ms");
        return mh;
    }

    private long lo(Context context) {
        if (lt()) {
            ly(context, true);
        }
        boolean ls = ls(context);
        c.sg("PushLog2951", "bad network mode is " + ls);
        if (!ls || this.de.isEmpty()) {
            return 0;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long mh = ((b) this.de.get(this.de.size() - 1)).dx;
        long j = lu() ? dn : dm;
        if (currentTimeMillis < mh) {
            c.sg("PushLog2951", "now is less than last connect time");
            j = 0;
        } else {
            j = Math.max((j + mh) - currentTimeMillis, 0);
        }
        c.sg("PushLog2951", "It is in bad network mode, connect limit interval is " + j);
        return j;
    }

    private boolean ls(Context context) {
        return i.ea(context).el();
    }

    private void ly(Context context, boolean z) {
        c.sg("PushLog2951", "set bad network mode " + z);
        i.ea(context).em(z);
    }

    private boolean lu() {
        try {
            String format = new SimpleDateFormat("HH:mm").format(new Date());
            if (format.compareTo(dk) > 0 && format.compareTo(dl) < 0) {
                c.sg("PushLog2951", "It is in Idle period.");
                return true;
            }
        } catch (RuntimeException e) {
            c.sg("PushLog2951", "format idle perild time RuntimeException.");
        } catch (Exception e2) {
            c.sg("PushLog2951", "format idle perild time exception.");
        }
        return false;
    }

    private boolean lt() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = lu() ? dh : dg;
        int i = 0;
        for (b bVar : this.de) {
            if (currentTimeMillis > bVar.mc() && currentTimeMillis - bVar.mc() < j) {
                i++;
            }
            i = i;
        }
        c.sh("PushLog2951", "The connect range limit is: " + di + " times in " + j + ", " + "current count is:" + i);
        if (i < di) {
            return false;
        }
        return true;
    }

    private void ln() {
        this.dj = 0;
    }

    private void lr() {
        this.dj++;
    }

    public void lm(Context context, ReconnectMgr$RECONNECTEVENT reconnectMgr$RECONNECTEVENT, Bundle bundle) {
        c.sg("PushLog2951", "receive reconnectevent:" + reconnectMgr$RECONNECTEVENT);
        switch (ma()[reconnectMgr$RECONNECTEVENT.ordinal()]) {
            case 1:
                ln();
                return;
            case 2:
                ErrorType errorType = ErrorType.Err_unKnown;
                lz(context);
                if (bundle.containsKey("errorType")) {
                    if (ErrorType.Err_Connect == ((ErrorType) bundle.getSerializable("errorType"))) {
                        lx(context, false);
                        com.huawei.android.pushagent.a.a.xv(54);
                    } else {
                        c.sg("PushLog2951", "socket close not caused by connect error, do not need save connection info");
                    }
                } else {
                    c.sg("PushLog2951", "socket close not caused by pushException");
                }
                lr();
                PushService.yx(new Intent("com.huawei.action.CONNECT_PUSHSRV"));
                return;
            case 3:
                lx(context, true);
                return;
            case 4:
                ln();
                return;
            case 5:
                ln();
                return;
            default:
                return;
        }
    }

    private void lw(Context context) {
        di = g.aq(context).cf();
        dg = g.aq(context).cg();
        dh = g.aq(context).ch();
        df = g.aq(context).ci();
        dm = g.aq(context).cj();
        dn = g.aq(context).ck();
        dk = g.aq(context).cl();
        dl = g.aq(context).cm();
    }

    public void ll(Context context) {
        c.sh("PushLog2951", "enter resetReconnectConfig");
        lw(context);
        this.de.clear();
        ly(context, false);
        new com.huawei.android.pushagent.utils.d.a(context, "PushConnectControl").rr("connectPushSvrInfos");
    }
}
