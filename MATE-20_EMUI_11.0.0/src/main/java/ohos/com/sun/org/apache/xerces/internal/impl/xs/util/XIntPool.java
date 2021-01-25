package ohos.com.sun.org.apache.xerces.internal.impl.xs.util;

public final class XIntPool {
    private static final short POOL_SIZE = 10;
    private static final XInt[] fXIntPool = new XInt[10];

    static {
        for (int i = 0; i < 10; i++) {
            fXIntPool[i] = new XInt(i);
        }
    }

    public final XInt getXInt(int i) {
        if (i >= 0) {
            XInt[] xIntArr = fXIntPool;
            if (i < xIntArr.length) {
                return xIntArr[i];
            }
        }
        return new XInt(i);
    }
}
