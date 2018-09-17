package com.huawei.android.pushagent.model.channel.entity;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.bastet.a;
import java.util.Date;

public abstract class c {
    public Context bj = null;
    public boolean bk = false;
    protected a bl;
    public long bm = 0;

    public abstract void fb(boolean z);

    public abstract long ff(boolean z);

    public abstract c fh();

    public abstract void fj();

    public c(Context context) {
        this.bj = context;
        this.bl = a.ra(context);
    }

    public void gn(boolean z) {
        this.bk = z;
    }

    public void gq() {
        if (com.huawei.android.pushagent.model.channel.a.hp(this.bj) == this) {
            long ff = ff(false);
            com.huawei.android.pushagent.utils.d.c.sg("PushLog2951", "after delayHeartBeatReq, nextHeartBeatTime, will be " + ff + "ms later");
            gr(ff, true);
        }
    }

    public void gp(Context context) {
        int tm = b.tm(context);
        long j = 0;
        if (tm == 0) {
            j = g.aq(context).dl();
        } else if (1 == tm) {
            j = g.aq(context).dm();
        }
        ff(false);
        gr(j, false);
    }

    private void gr(long j, boolean z) {
        if (this.bl.rb()) {
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "support bastet, need not to send delayed heartbeat");
            return;
        }
        Intent intent = new Intent("com.huawei.intent.action.PUSH");
        intent.putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ");
        intent.putExtra("isHeartbeatReq", z);
        intent.setPackage(this.bj.getPackageName());
        com.huawei.android.pushagent.utils.tools.a.qf(this.bj, intent, j);
    }

    public String fc() {
        return getClass().getSimpleName();
    }

    public void go(long j) {
        this.bm = j;
        new com.huawei.android.pushagent.utils.d.a(this.bj, fc()).sa("lastHeartBeatTime", Long.valueOf(j));
    }

    public String toString() {
        return new StringBuffer().append("lastHeartBeatTime").append(new Date(this.bm)).append(" heartBeatInterval").append(ff(false)).toString();
    }
}
