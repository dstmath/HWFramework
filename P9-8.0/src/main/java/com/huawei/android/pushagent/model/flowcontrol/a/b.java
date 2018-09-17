package com.huawei.android.pushagent.model.flowcontrol.a;

import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.c;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class b implements a {
    private long da;
    private long db;
    private long dc;
    private long dd;

    public b(long j, long j2) {
        this.dc = j;
        this.dd = j2;
        this.da = 0;
        this.db = 0;
    }

    public boolean lf(a aVar) {
        boolean z = false;
        if (!(aVar instanceof b)) {
            return false;
        }
        b bVar = (b) aVar;
        if (this.dc == bVar.dc && this.dd == bVar.dd) {
            z = true;
        }
        return z;
    }

    public boolean lg(long j) {
        c.sg("PushLog2951", "enter FlowSimpleControl::canApply(num:" + j + ", curVol:" + this.da + ", maxVol:" + this.dd + ")");
        Long valueOf = Long.valueOf(System.currentTimeMillis());
        if (valueOf.longValue() < this.db || valueOf.longValue() - this.db >= this.dc) {
            c.sg("PushLog2951", " fistrControlTime:" + new Date(this.db) + " interval:" + (valueOf.longValue() - this.db) + " statInterval:" + this.dc + " change fistrControlTime to cur");
            this.db = valueOf.longValue();
            this.da = 0;
        } else {
            try {
                Calendar instance = Calendar.getInstance(Locale.getDefault());
                instance.setTimeInMillis(this.db);
                int i = instance.get(2);
                instance.setTimeInMillis(valueOf.longValue());
                if (i != instance.get(2)) {
                    this.db = valueOf.longValue();
                    this.da = 0;
                }
            } catch (Throwable e) {
                c.se("PushLog2951", e.toString(), e);
            } catch (Throwable e2) {
                c.se("PushLog2951", e2.toString(), e2);
            } catch (Throwable e22) {
                c.se("PushLog2951", e22.toString(), e22);
            }
        }
        if (this.da + j <= this.dd) {
            return true;
        }
        return false;
    }

    public boolean lh(long j) {
        this.da += j;
        return true;
    }

    public String li() {
        String str = ";";
        return new StringBuffer().append(4).append(str).append(this.dc).append(str).append(this.dd).append(str).append(this.da).append(str).append(this.db).toString();
    }

    public boolean lj(String str) {
        try {
            if (TextUtils.isEmpty(str)) {
                c.sh("PushLog2951", "in loadFromString, info is empty!");
                return false;
            }
            c.sg("PushLog2951", "begin to parse:" + str);
            String[] split = str.split(";");
            if (split.length == 0) {
                return false;
            }
            int parseInt = Integer.parseInt(split[0]);
            if (parseInt == 4 && parseInt == split.length - 1) {
                this.dc = Long.parseLong(split[1]);
                this.dd = Long.parseLong(split[2]);
                this.da = Long.parseLong(split[3]);
                this.db = Long.parseLong(split[4]);
                return true;
            }
            c.sf("PushLog2951", "in fileNum:" + parseInt + ", but need " + 4 + " parse " + str + " failed");
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
