package org.apache.xalan.templates;

import java.io.Serializable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathContext;

public abstract class AVTPart implements Serializable, XSLTVisitable {
    static final long serialVersionUID = -1747749903613916025L;

    public abstract void evaluate(XPathContext xPathContext, FastStringBuffer fastStringBuffer, int i, PrefixResolver prefixResolver) throws TransformerException;

    public abstract void fixupVariables(Vector vector, int i);

    public abstract String getSimpleString();

    public void setXPathSupport(XPathContext support) {
    }

    public boolean canTraverseOutsideSubtree() {
        return false;
    }
}
