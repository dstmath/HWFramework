package org.apache.xalan.templates;

import java.util.Vector;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathContext;

public class AVTPartSimple extends AVTPart {
    static final long serialVersionUID = -3744957690598727913L;
    private String m_val;

    public AVTPartSimple(String val) {
        this.m_val = val;
    }

    public String getSimpleString() {
        return this.m_val;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }

    public void evaluate(XPathContext xctxt, FastStringBuffer buf, int context, PrefixResolver nsNode) {
        buf.append(this.m_val);
    }

    public void callVisitors(XSLTVisitor visitor) {
    }
}
