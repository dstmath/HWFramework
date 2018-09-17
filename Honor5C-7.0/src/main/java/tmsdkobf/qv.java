package tmsdkobf;

import tmsdk.common.tcc.QFile;

/* compiled from: Unknown */
public abstract class qv {
    protected long mFlag;
    protected a mListener;
    protected int mType;

    /* compiled from: Unknown */
    public interface a {
        void onFound(int i, QFile qFile);
    }

    public qv(int i, long j) {
        this.mType = i;
        this.mFlag = j;
    }

    public void cancleScan() {
        doCancleScan();
    }

    protected abstract void doCancleScan();

    protected abstract void doStartScan(String str);

    public void setListener(a aVar) {
        this.mListener = aVar;
    }

    public void startScan(String str) {
        doStartScan(str);
    }
}
