package tmsdkobf;

import java.util.List;

/* compiled from: Unknown */
public abstract class gz {
    private final String TAG;
    private int pO;
    protected he pP;
    protected int pQ;

    public gz(int i) {
        this.TAG = "BaseScanTask";
        this.pQ = 0;
        this.pO = i;
    }

    protected void a(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
        if (this.pP != null) {
            this.pP.a(i, list, z, j, str, str2, str3);
        }
    }

    public void a(he heVar) {
        this.pP = heVar;
    }

    protected void aA(int i) {
        this.pQ = 1;
        if (this.pP != null) {
            this.pP.aA(i);
        }
    }

    public void bd() {
        this.pQ = 0;
        bj();
    }

    public void be() {
        jq.ct().a(new Runnable() {
            final /* synthetic */ gz pR;

            {
                this.pR = r1;
            }

            public void run() {
                this.pR.aA(this.pR.pO);
                this.pR.bk();
                this.pR.bg();
            }
        }, null);
    }

    public void bf() {
        bl();
    }

    protected void bg() {
        this.pQ = 2;
        if (this.pP != null) {
            this.pP.bg();
        }
    }

    public int bh() {
        return this.pQ;
    }

    abstract void bi();

    abstract void bj();

    abstract void bk();

    abstract void bl();

    public void preLoad() {
        jq.ct().a(new Runnable() {
            final /* synthetic */ gz pR;

            {
                this.pR = r1;
            }

            public void run() {
                this.pR.bi();
            }
        }, null);
    }
}
