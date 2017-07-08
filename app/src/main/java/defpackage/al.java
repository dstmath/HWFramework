package defpackage;

import android.text.TextUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* renamed from: al */
public class al implements am {
    private long bq;
    private long br;
    private long bs;
    private long interval;

    public al(long j, long j2) {
        this.interval = j;
        this.bq = j2;
        this.br = 0;
        this.bs = 0;
    }

    public boolean a(am amVar) {
        if (!(amVar instanceof al)) {
            return false;
        }
        al alVar = (al) amVar;
        return this.interval == alVar.interval && this.bq == alVar.bq;
    }

    public String bG() {
        String str = ";";
        return new StringBuffer().append(4).append(str).append(this.interval).append(str).append(this.bq).append(str).append(this.br).append(str).append(this.bs).toString();
    }

    public boolean i(String str) {
        try {
            if (TextUtils.isEmpty(str)) {
                aw.i("PushLog2828", "in loadFromString, info is empty!");
                return false;
            }
            aw.d("PushLog2828", "begin to parse:" + str);
            String[] split = str.split(";");
            if (split.length == 0) {
                return false;
            }
            int parseInt = Integer.parseInt(split[0]);
            if (parseInt == 4 && parseInt == split.length - 1) {
                this.interval = Long.parseLong(split[1]);
                this.bq = Long.parseLong(split[2]);
                this.br = Long.parseLong(split[3]);
                this.bs = Long.parseLong(split[4]);
                return true;
            }
            aw.e("PushLog2828", "in fileNum:" + parseInt + ", but need " + 4 + " parse " + str + " failed");
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean k(long j) {
        aw.d("PushLog2828", "enter FlowSimpleControl::canApply(num:" + j + ", curVol:" + this.br + ", maxVol:" + this.bq + ")");
        Long valueOf = Long.valueOf(System.currentTimeMillis());
        if (valueOf.longValue() < this.bs || valueOf.longValue() - this.bs >= this.interval) {
            aw.d("PushLog2828", " fistrControlTime:" + new Date(this.bs) + " interval:" + (valueOf.longValue() - this.bs) + " statInterval:" + this.interval + " change fistrControlTime to cur");
            this.bs = valueOf.longValue();
            this.br = 0;
        } else {
            try {
                Calendar instance = Calendar.getInstance(Locale.getDefault());
                instance.setTimeInMillis(this.bs);
                int i = instance.get(2);
                instance.setTimeInMillis(valueOf.longValue());
                if (i != instance.get(2)) {
                    this.bs = valueOf.longValue();
                    this.br = 0;
                }
            } catch (Throwable e) {
                aw.d("PushLog2828", e.toString(), e);
            } catch (Throwable e2) {
                aw.d("PushLog2828", e2.toString(), e2);
            } catch (Throwable e22) {
                aw.d("PushLog2828", e22.toString(), e22);
            }
        }
        return this.br + j <= this.bq;
    }

    public boolean l(long j) {
        this.br += j;
        return true;
    }
}
