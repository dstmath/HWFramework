package org.apache.xml.utils;

import java.io.Serializable;

public class NameSpace implements Serializable {
    static final long serialVersionUID = 1471232939184881839L;
    public NameSpace m_next = null;
    public String m_prefix;
    public String m_uri;

    public NameSpace(String prefix, String uri) {
        this.m_prefix = prefix;
        this.m_uri = uri;
    }
}
