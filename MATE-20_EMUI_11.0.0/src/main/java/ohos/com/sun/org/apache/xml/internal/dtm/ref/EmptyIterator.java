package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;

public final class EmptyIterator implements DTMAxisIterator {
    private static final EmptyIterator INSTANCE = new EmptyIterator();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final DTMAxisIterator cloneIterator() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final int getLast() {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final int getNodeByPosition(int i) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final int getPosition() {
        return 1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final int getStartNode() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final void gotoMark() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final boolean isReverse() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final int next() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final DTMAxisIterator reset() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final void setMark() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final void setRestartable(boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator
    public final DTMAxisIterator setStartNode(int i) {
        return this;
    }

    public static DTMAxisIterator getInstance() {
        return INSTANCE;
    }

    private EmptyIterator() {
    }
}
