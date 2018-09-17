package tmsdkobf;

import tmsdk.common.TMServiceFactory;

public class pf {
    private int DL = 0;
    private jx Js;
    private long Jt = 0;
    private int Ju = 0;
    private long Jv = 0;
    private long Jw = 0;

    public pf(String str, long j, int i) {
        this.Js = TMServiceFactory.getPreferenceService("freq_ctrl_" + str);
        this.DL = i;
        this.Jt = j;
        this.Ju = this.Js.getInt("times_now", this.Ju);
        this.Jv = this.Js.getLong("time_span_start", this.Jv);
        this.Jw = this.Js.getLong("time_span_end", this.Jw);
        this.Js.putInt("times", i);
        this.Js.putLong("time_span", j);
    }

    private void B(long j) {
        this.Jv = j;
        this.Jw = this.Jt + j;
        this.Js.putLong("time_span_start", this.Jv);
        this.Js.putLong("time_span_end", this.Jw);
    }

    private void bG(int i) {
        this.Ju = i;
        this.Js.putInt("times_now", this.Ju);
    }

    public boolean hI() {
        if (this.Jv == 0) {
            return true;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Ju >= this.DL) {
            if (!(currentTimeMillis >= this.Jw)) {
                return false;
            }
        }
        return true;
    }

    public void hJ() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Jv == 0) {
            B(currentTimeMillis);
            bG(0);
        } else {
            if ((currentTimeMillis < this.Jw ? 1 : 0) == 0) {
                B(currentTimeMillis);
                bG(0);
            }
        }
        bG(this.Ju + 1);
    }
}
