package ohos.com.sun.org.apache.xml.internal.dtm;

public interface DTMWSFilter {
    public static final short INHERIT = 3;
    public static final short NOTSTRIP = 1;
    public static final short STRIP = 2;

    short getShouldStripSpace(int i, DTM dtm);
}
