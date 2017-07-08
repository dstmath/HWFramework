package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.ChannelMgr;

/* renamed from: u */
class u extends q {
    private long ap;

    public u(Context context) {
        super(context);
        this.ap = -1;
        bg();
    }

    public String bd() {
        return "Push_PollingHBeat";
    }

    public q bg() {
        this.ad = new bt(this.context, bd()).getLong("lastHeartBeatTime");
        return this;
    }

    public void bh() {
        try {
            ChannelMgr.aX().a(false);
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
        }
    }

    protected boolean bi() {
        return false;
    }

    public long e(boolean z) {
        if (-1 == au.G(this.context)) {
            return ae.l(this.context).C() * 1000;
        }
        if (bi()) {
            bg();
        }
        if (this.ap > 0) {
            return this.ap;
        }
        long O = ae.l(this.context).O() * 1000;
        long currentTimeMillis = System.currentTimeMillis();
        if (be() >= currentTimeMillis) {
            h(0);
        }
        return be() <= currentTimeMillis - (ae.l(this.context).O() * 1000) ? ae.l(this.context).O() * 1000 : (be() > currentTimeMillis || currentTimeMillis > be() + (ae.l(this.context).O() * 1000)) ? O : (be() + (ae.l(this.context).O() * 1000)) - currentTimeMillis;
    }

    public void f(boolean z) {
    }

    public boolean i(long j) {
        this.ap = j;
        return true;
    }
}
