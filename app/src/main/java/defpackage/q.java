package defpackage;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import java.util.Date;

/* renamed from: q */
public abstract class q {
    public long ad;
    public boolean ae;
    protected PushBastet af;
    public int batteryStatus;
    public Context context;

    public q(Context context) {
        this.ad = 0;
        this.ae = false;
        this.context = null;
        this.batteryStatus = 1;
        this.context = context;
        this.af = PushBastet.ac(context);
    }

    private void a(long j, long j2, boolean z) {
        if (this.af.bZ()) {
            aw.d("PushLog2828", "support bastet, need not to send delayed heartbeat");
            return;
        }
        Intent intent = new Intent("com.huawei.intent.action.PUSH");
        intent.putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ");
        intent.putExtra("heartbeat_interval", j);
        intent.putExtra("isHeartbeatReq", z);
        intent.setPackage(this.context.getPackageName());
        bq.a(this.context, intent, j2);
    }

    public void a(int i) {
        this.batteryStatus = i;
    }

    public void bb() {
        if (ChannelMgr.h(this.context) == this) {
            long e = e(false);
            aw.d("PushLog2828", "after delayHeartBeatReq, nextHeartBeatTime, will be " + e + "ms later");
            a(e, e, true);
        }
    }

    public void bc() {
        if (ChannelMgr.h(this.context) == this) {
            long bf = bf() - System.currentTimeMillis();
            aw.d("PushLog2828", "after updateHeartBeatReq, nextHeartBeatTime, will be " + bf + "ms later");
            a(bf, bf, true);
        }
    }

    public String bd() {
        return getClass().getSimpleName();
    }

    public long be() {
        return this.ad;
    }

    public long bf() {
        long currentTimeMillis = System.currentTimeMillis();
        long e = e(false);
        return (be() > currentTimeMillis || be() + e <= currentTimeMillis) ? currentTimeMillis + e : be() + e;
    }

    public abstract q bg();

    public abstract void bh();

    protected abstract boolean bi();

    public void d(boolean z) {
        this.ae = z;
    }

    public abstract long e(boolean z);

    public abstract void f(boolean z);

    public void h(long j) {
        this.ad = j;
        new bt(this.context, bd()).a("lastHeartBeatTime", Long.valueOf(j));
    }

    public abstract boolean i(long j);

    public void j(Context context) {
        int G = au.G(context);
        long j = 0;
        if (G == 0) {
            j = ae.l(context).an();
        } else if (1 == G) {
            j = ae.l(context).ao();
        }
        a(e(false), j, false);
    }

    public String toString() {
        return new StringBuffer().append("lastHeartBeatTime").append(new Date(this.ad)).append(" heartBeatInterval").append(e(false)).toString();
    }
}
