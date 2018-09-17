package com.huawei.android.pushagent.a.a;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;
import java.util.ArrayList;
import java.util.List;

public class a extends b {
    private List<String> hl = new ArrayList();
    private long hm;
    private boolean hn = false;
    private long ho = 0;
    private int hp;
    private int hq;
    private long hr;
    private int hs;
    private String ht = null;

    protected void xn(String str, String str2) {
        c.sg("PushLog2951", "reportEvent eventId:" + str + "; extra:" + str2);
        if (this.hl.size() >= this.hq) {
            c.sj("PushLog2951", "events overflow, abandon");
        } else {
            if (str2 == null) {
                str2 = "";
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(xj()).append("|").append(b.tm(this.appCtx)).append("|").append(str).append("|").append(str2);
            this.hl.add(stringBuilder.toString());
        }
        c.sg("PushLog2951", "reportEvent eventList.size:" + this.hl.size());
        if (this.hl.size() >= this.hs && this.ht == null) {
            StringBuilder stringBuilder2 = new StringBuilder();
            int i = this.hs;
            int size = this.hl.size();
            int i2 = 0;
            int i3 = 0;
            int i4 = i;
            while (i2 < size) {
                String str3 = (String) this.hl.remove(0);
                i3 += xi(str3);
                if (i3 < this.hp) {
                    if (i4 != this.hs) {
                        stringBuilder2.append("#");
                    }
                    stringBuilder2.append(str3);
                    i4--;
                    if (i4 == 0) {
                        break;
                    }
                    i2++;
                } else {
                    this.hl.add(0, str3);
                    break;
                }
            }
            this.ht = stringBuilder2.toString();
        }
        if (xl()) {
            c.sg("PushLog2951", "reportEvent begin to send events");
            this.hn = false;
            this.ho = System.currentTimeMillis();
            xp(this.ht);
            xq(this.appCtx);
        }
    }

    private int xi(String str) {
        int length = "#".length();
        if (str == null) {
            return length;
        }
        return length + str.length();
    }

    private boolean xl() {
        long currentTimeMillis = System.currentTimeMillis();
        c.sg("PushLog2951", "reportEvent now:" + currentTimeMillis + "; lastReportTime:" + this.ho + "; AvailableNetwork:" + b.tm(this.appCtx) + "; hasConnection:" + com.huawei.android.pushagent.model.channel.a.hk().gc() + "; lastReportSuccess:" + this.hn);
        if (currentTimeMillis <= (this.hn ? this.hr : this.hm) + this.ho || 1 != b.tm(this.appCtx) || this.ht == null) {
            return false;
        }
        com.huawei.android.pushagent.model.channel.entity.b hk = com.huawei.android.pushagent.model.channel.a.hk();
        if (!(hk instanceof com.huawei.android.pushagent.model.channel.entity.a.c)) {
            return false;
        }
        com.huawei.android.pushagent.model.channel.entity.a.c cVar = (com.huawei.android.pushagent.model.channel.entity.a.c) hk;
        c.sg("PushLog2951", "reportEvent hasRegist:" + cVar.fn());
        return cVar.gc() ? cVar.fn() : false;
    }

    private void xp(String str) {
        PushService.yx(new Intent("com.huawei.action.push.intent.REPORT_EVENTS").setPackage(this.appCtx.getPackageName()).putExtra("events", str));
    }

    protected void xm(boolean z) {
        if (z) {
            this.hn = true;
            this.ht = null;
        }
        c.sg("PushLog2951", "reportEvent report result is:" + z);
    }

    private String xj() {
        return String.valueOf(System.currentTimeMillis());
    }

    protected void xk(Context context) {
        xq(context);
    }

    protected void xo(long j, String str, String str2, String str3) {
        c.sg("PushLog2951", "reportEvent eventId:" + str2 + "; extra:" + str3);
        c.sg("PushLog2951", "reportEvent eventList:" + this.hl.toString());
        if (this.hl.size() >= this.hq) {
            c.sj("PushLog2951", "reportEvent events overflow, abandon");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(j).append("|").append(str).append("|").append(str2).append("|").append(str3);
            this.hl.add(stringBuilder.toString());
        }
        c.sg("PushLog2951", "reportEvent eventList.size:" + this.hl.size());
    }

    private void xq(Context context) {
        this.hr = g.aq(context).bb();
        this.hm = g.aq(context).bc();
        this.hq = g.aq(context).bd();
        this.hs = g.aq(context).be();
        this.hp = g.aq(context).bf();
    }
}
