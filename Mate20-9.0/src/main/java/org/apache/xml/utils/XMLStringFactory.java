package org.apache.xml.utils;

public abstract class XMLStringFactory {
    public abstract XMLString emptystr();

    public abstract XMLString newstr(String str);

    public abstract XMLString newstr(FastStringBuffer fastStringBuffer, int i, int i2);

    public abstract XMLString newstr(char[] cArr, int i, int i2);
}
