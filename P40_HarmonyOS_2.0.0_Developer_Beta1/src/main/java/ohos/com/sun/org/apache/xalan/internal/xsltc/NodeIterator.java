package ohos.com.sun.org.apache.xalan.internal.xsltc;

public interface NodeIterator extends Cloneable {
    public static final int END = -1;

    NodeIterator cloneIterator();

    int getLast();

    int getPosition();

    void gotoMark();

    boolean isReverse();

    int next();

    NodeIterator reset();

    void setMark();

    void setRestartable(boolean z);

    NodeIterator setStartNode(int i);
}
