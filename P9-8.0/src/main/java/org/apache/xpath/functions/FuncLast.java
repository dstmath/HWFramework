package org.apache.xpath.functions;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

public class FuncLast extends Function {
    static final long serialVersionUID = 9205812403085432943L;
    private boolean m_isTopLevel;

    public void postCompileStep(Compiler compiler) {
        this.m_isTopLevel = compiler.getLocationPathDepth() == -1;
    }

    public int getCountOfContextNodeList(XPathContext xctxt) throws TransformerException {
        SubContextList iter = this.m_isTopLevel ? null : xctxt.getSubContextList();
        if (iter != null) {
            return iter.getLastPos(xctxt);
        }
        int count;
        DTMIterator cnl = xctxt.getContextNodeList();
        if (cnl != null) {
            count = cnl.getLength();
        } else {
            count = 0;
        }
        return count;
    }

    public XObject execute(XPathContext xctxt) throws TransformerException {
        return new XNumber((double) getCountOfContextNodeList(xctxt));
    }

    public void fixupVariables(Vector vars, int globalsSize) {
    }
}
