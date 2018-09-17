package tmsdkobf;

import tmsdk.common.TMServiceFactory;

/* compiled from: Unknown */
public class qi {
    private int EU;
    private lf Js;
    private long Jt;
    private int Ju;
    private long Jv;
    private long Jw;

    public qi(String str, long j, int i) {
        this.EU = 0;
        this.Jt = 0;
        this.Ju = 0;
        this.Jv = 0;
        this.Jw = 0;
        this.Js = TMServiceFactory.getPreferenceService("freq_ctrl_" + str);
        this.EU = i;
        this.Jt = j;
        this.Ju = this.Js.getInt("times_now", this.Ju);
        this.Jv = this.Js.getLong("time_span_start", this.Jv);
        this.Jw = this.Js.getLong("time_span_end", this.Jw);
        this.Js.e("times", i);
        this.Js.d("time_span", j);
    }

    private void cq(int i) {
        this.Ju = i;
        this.Js.e("times_now", this.Ju);
    }

    private void u(long j) {
        this.Jv = j;
        this.Jw = this.Jt + j;
        this.Js.d("time_span_start", this.Jv);
        this.Js.d("time_span_end", this.Jw);
    }

    public boolean hK() {
        if (this.Jv == 0) {
            return true;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Ju >= this.EU) {
            if (!(currentTimeMillis >= this.Jw)) {
                return false;
            }
        }
        return true;
    }

    public void hL() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Jv == 0) {
            u(currentTimeMillis);
            cq(0);
        } else {
            if ((currentTimeMillis < this.Jw ? 1 : 0) == 0) {
                u(currentTimeMillis);
                cq(0);
            }
        }
        cq(this.Ju + 1);
    }
}
