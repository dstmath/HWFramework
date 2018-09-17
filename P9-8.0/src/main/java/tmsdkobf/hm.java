package tmsdkobf;

import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.TelephonyEntity;

public final class hm {
    private Object mLock = new Object();
    private int[] pU;
    private a[] pV;

    static abstract class a {
        private TelephonyEntity mData;
        private Object[] mParams;
        private int mState;
        private FilterResult pW;
        private int pX;
        private Object pY;

        a() {
        }

        public void a(Object obj) {
            this.pY = obj;
        }

        public void a(FilterResult filterResult) {
            this.pW = filterResult;
        }

        public TelephonyEntity bm() {
            return this.mData;
        }

        public int bn() {
            return this.mState;
        }

        public Object[] bo() {
            return this.mParams;
        }

        public int bp() {
            return this.pX;
        }

        public Object bq() {
            return this.pY;
        }

        abstract boolean br();

        abstract void bs();
    }

    private FilterResult a(int i, int i2, TelephonyEntity telephonyEntity, FilterConfig filterConfig, Object... objArr) {
        FilterResult filterResult = null;
        a aVar = this.pV[i];
        if (aVar != null) {
            synchronized (this.pV) {
                aVar.mData = telephonyEntity;
                aVar.mState = i2;
                aVar.mParams = objArr;
                aVar.pX = this.pU[i];
                if (aVar.br()) {
                    aVar.bs();
                }
                filterResult = aVar.pW;
                aVar.pY = null;
                aVar.mData = null;
                aVar.a(null);
                aVar.mParams = null;
            }
        }
        return filterResult;
    }

    private int ab(int i) {
        for (int i2 = 0; i2 < this.pU.length; i2++) {
            if (this.pU[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    public FilterResult a(TelephonyEntity telephonyEntity, FilterConfig filterConfig, Object... objArr) {
        FilterResult filterResult = null;
        if (!(this.pU == null || this.pV == null || filterConfig == null)) {
            synchronized (this.mLock) {
                for (int i = 0; i < this.pU.length; i++) {
                    int i2 = filterConfig.get(this.pU[i]);
                    if (!(i2 == 4 || i2 == 3)) {
                        filterResult = a(i, i2, telephonyEntity, filterConfig, objArr);
                    }
                    if (filterResult != null) {
                        break;
                    }
                }
            }
        }
        return filterResult;
    }

    public void a(int i, a aVar) {
        int ab = ab(i);
        if (ab < 0) {
            throw new IndexOutOfBoundsException("the filed " + i + "is not define from setOrderedFileds method.");
        }
        this.pV[ab] = aVar;
    }

    public void a(int... iArr) {
        this.pU = iArr;
        this.pV = new a[this.pU.length];
    }
}
