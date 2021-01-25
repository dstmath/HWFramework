package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.Serializable;

public class NameSpace implements Serializable {
    static final long serialVersionUID = 1471232939184881839L;
    public NameSpace m_next = null;
    public String m_prefix;
    public String m_uri;

    public NameSpace(String str, String str2) {
        this.m_prefix = str;
        this.m_uri = str2;
    }
}
