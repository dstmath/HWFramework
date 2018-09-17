package org.apache.xml.dtm;

public interface DTMIterator {
    public static final short FILTER_ACCEPT = (short) 1;
    public static final short FILTER_REJECT = (short) 2;
    public static final short FILTER_SKIP = (short) 3;

    void allowDetachToRelease(boolean z);

    Object clone() throws CloneNotSupportedException;

    DTMIterator cloneWithReset() throws CloneNotSupportedException;

    void detach();

    int getAxis();

    int getCurrentNode();

    int getCurrentPos();

    DTM getDTM(int i);

    DTMManager getDTMManager();

    boolean getExpandEntityReferences();

    int getLength();

    int getRoot();

    int getWhatToShow();

    boolean isDocOrdered();

    boolean isFresh();

    boolean isMutable();

    int item(int i);

    int nextNode();

    int previousNode();

    void reset();

    void runTo(int i);

    void setCurrentPos(int i);

    void setItem(int i, int i2);

    void setRoot(int i, Object obj);

    void setShouldCacheNodes(boolean z);
}
