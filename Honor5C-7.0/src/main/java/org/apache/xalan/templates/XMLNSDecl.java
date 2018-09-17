package org.apache.xalan.templates;

import java.io.Serializable;

public class XMLNSDecl implements Serializable {
    static final long serialVersionUID = 6710237366877605097L;
    private boolean m_isExcluded;
    private String m_prefix;
    private String m_uri;

    public XMLNSDecl(String prefix, String uri, boolean isExcluded) {
        this.m_prefix = prefix;
        this.m_uri = uri;
        this.m_isExcluded = isExcluded;
    }

    public String getPrefix() {
        return this.m_prefix;
    }

    public String getURI() {
        return this.m_uri;
    }

    public boolean getIsExcluded() {
        return this.m_isExcluded;
    }
}
