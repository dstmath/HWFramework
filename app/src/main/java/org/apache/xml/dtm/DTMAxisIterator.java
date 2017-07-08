package org.apache.xml.dtm;

public interface DTMAxisIterator extends Cloneable {
    public static final int END = -1;

    DTMAxisIterator cloneIterator();

    int getLast();

    int getNodeByPosition(int i);

    int getPosition();

    int getStartNode();

    void gotoMark();

    boolean isReverse();

    int next();

    DTMAxisIterator reset();

    void setMark();

    void setRestartable(boolean z);

    DTMAxisIterator setStartNode(int i);
}
