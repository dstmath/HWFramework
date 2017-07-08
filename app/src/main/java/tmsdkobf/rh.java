package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public abstract class rh<T> {
    protected List<T> NE;
    protected int NF;
    private final String TAG;
    protected int pE;

    public rh(List<T> list, int i) {
        this.TAG = "BaseScanTask";
        this.pE = 0;
        this.NE = new ArrayList(list);
        this.NF = i;
    }

    public void bf() {
        bl();
        if (jC()) {
            bn();
        }
    }

    abstract void bl();

    protected void bn() {
        d.e("BaseScanTask", "onScanCancel");
        if (!jE()) {
            cH(2);
        }
    }

    public void cH(int i) {
        this.pE = i;
    }

    public boolean jC() {
        return this.pE == 1;
    }

    public boolean jD() {
        return this.pE == 2 || this.pE == 7;
    }

    public boolean jE() {
        return this.pE == 7;
    }
}
