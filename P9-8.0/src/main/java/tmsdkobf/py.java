package tmsdkobf;

import tmsdk.common.tcc.QFile;

public abstract class py {
    protected long mFlag;
    protected a mListener;
    protected int mType;

    public interface a {
        void onFound(int i, QFile qFile);
    }

    public py(int i, long j) {
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
