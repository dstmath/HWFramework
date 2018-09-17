package org.apache.xml.dtm;

public interface DTMWSFilter {
    public static final short INHERIT = (short) 3;
    public static final short NOTSTRIP = (short) 1;
    public static final short STRIP = (short) 2;

    short getShouldStripSpace(int i, DTM dtm);
}
