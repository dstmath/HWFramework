package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathFactory;
import org.apache.xpath.compiler.XPathParser;
import org.apache.xpath.objects.XObject;

public class AVTPartXPath extends AVTPart {
    static final long serialVersionUID = -4460373807550527675L;
    private XPath m_xpath;

    public void fixupVariables(Vector vars, int globalsSize) {
        this.m_xpath.fixupVariables(vars, globalsSize);
    }

    public boolean canTraverseOutsideSubtree() {
        return this.m_xpath.getExpression().canTraverseOutsideSubtree();
    }

    public AVTPartXPath(XPath xpath) {
        this.m_xpath = xpath;
    }

    public AVTPartXPath(String val, PrefixResolver nsNode, XPathParser xpathProcessor, XPathFactory factory, XPathContext liaison) throws TransformerException {
        this.m_xpath = new XPath(val, null, nsNode, 0, liaison.getErrorListener());
    }

    public String getSimpleString() {
        return "{" + this.m_xpath.getPatternString() + "}";
    }

    public void evaluate(XPathContext xctxt, FastStringBuffer buf, int context, PrefixResolver nsNode) throws TransformerException {
        XObject xobj = this.m_xpath.execute(xctxt, context, nsNode);
        if (xobj != null) {
            xobj.appendToFsb(buf);
        }
    }

    public void callVisitors(XSLTVisitor visitor) {
        this.m_xpath.getExpression().callVisitors(this.m_xpath, visitor);
    }
}
