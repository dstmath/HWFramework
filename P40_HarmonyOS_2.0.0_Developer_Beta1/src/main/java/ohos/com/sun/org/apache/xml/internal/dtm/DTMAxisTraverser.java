package ohos.com.sun.org.apache.xml.internal.dtm;

public abstract class DTMAxisTraverser {
    public abstract int next(int i, int i2);

    public abstract int next(int i, int i2, int i3);

    public int first(int i) {
        return next(i, i);
    }

    public int first(int i, int i2) {
        return next(i, i, i2);
    }
}
