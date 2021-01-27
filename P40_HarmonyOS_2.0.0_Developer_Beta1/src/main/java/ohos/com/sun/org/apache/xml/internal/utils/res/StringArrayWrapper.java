package ohos.com.sun.org.apache.xml.internal.utils.res;

public class StringArrayWrapper {
    private String[] m_string;

    public StringArrayWrapper(String[] strArr) {
        this.m_string = strArr;
    }

    public String getString(int i) {
        return this.m_string[i];
    }

    public int getLength() {
        return this.m_string.length;
    }
}
