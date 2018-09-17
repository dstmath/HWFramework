package tmsdkobf;

import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.TelephonyEntity;

/* compiled from: Unknown */
public final class iq {
    private Object mLock;
    private int[] st;
    private a[] su;

    /* compiled from: Unknown */
    static abstract class a {
        private TelephonyEntity mData;
        private Object[] mParams;
        private int mState;
        private FilterResult sv;
        private int sw;
        private Object sx;

        a() {
        }

        public void a(Object obj) {
            this.sx = obj;
        }

        public void a(FilterResult filterResult) {
            this.sv = filterResult;
        }

        public TelephonyEntity bT() {
            return this.mData;
        }

        public int bU() {
            return this.mState;
        }

        public Object[] bV() {
            return this.mParams;
        }

        public int bW() {
            return this.sw;
        }

        public Object bX() {
            return this.sx;
        }

        abstract boolean bY();

        abstract void bZ();
    }

    public iq() {
        this.mLock = new Object();
    }

    private FilterResult a(int i, int i2, TelephonyEntity telephonyEntity, FilterConfig filterConfig, Object... objArr) {
        FilterResult filterResult = null;
        a aVar = this.su[i];
        if (aVar != null) {
            synchronized (this.su) {
                aVar.mData = telephonyEntity;
                aVar.mState = i2;
                aVar.mParams = objArr;
                aVar.sw = this.st[i];
                if (aVar.bY()) {
                    aVar.bZ();
                }
                filterResult = aVar.sv;
                aVar.sx = null;
                aVar.mData = null;
                aVar.a(null);
                aVar.mParams = null;
            }
        }
        return filterResult;
    }

    private int aU(int i) {
        for (int i2 = 0; i2 < this.st.length; i2++) {
            if (this.st[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public FilterResult a(TelephonyEntity telephonyEntity, FilterConfig filterConfig, Object... objArr) {
        FilterResult filterResult = null;
        if (!(this.st == null || this.su == null || filterConfig == null)) {
            synchronized (this.mLock) {
                int i = 0;
                while (true) {
                    if (i < this.st.length) {
                        int i2 = filterConfig.get(this.st[i]);
                        if (!(i2 == 4 || i2 == 3)) {
                            filterResult = a(i, i2, telephonyEntity, filterConfig, objArr);
                        }
                        if (filterResult != null) {
                            break;
                        }
                        i++;
                    } else {
                        break;
                    }
                }
            }
        }
        return filterResult;
    }

    public void a(int i, a aVar) {
        int aU = aU(i);
        if (aU < 0) {
            throw new IndexOutOfBoundsException("the filed " + i + "is not define from setOrderedFileds method.");
        }
        this.su[aU] = aVar;
    }

    public void b(int... iArr) {
        this.st = iArr;
        this.su = new a[this.st.length];
    }
}
